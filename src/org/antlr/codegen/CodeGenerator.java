/*
 [The "BSD licence"]
 Copyright (c) 2004 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.antlr.codegen;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.io.*;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.misc.StringTemplateTreeView;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.antlr.analysis.*;
import org.antlr.tool.Grammar;
import org.antlr.tool.ErrorManager;
import org.antlr.misc.*;
import org.antlr.Tool;
import org.antlr.codegen.bytecode.ClassFile;
import antlr.collections.AST;
import antlr.RecognitionException;

/** ANTLR's code generator.
 *
 *  Generate recognizers derived from grammars.  Language independence
 *  achieved through the use of StringTemplateGroup objects.  All output
 *  strings are completely encapsulated in the group files such as Java.stg.
 *  Some computations are done that are unused by a particular language.
 *  This generator just computes and sets the values into the templates;
 *  the templates are free to use or not use the information.
 *
 *  To make a new code generation target, define X.stg for language X
 *  by copying from existing Y.stg most closely releated to your language;
 *  e.g., to do CSharp.stg copy Java.stg.  The template group file has a
 *  bunch of templates that are needed by the code generator.  You can add
 *  a new target w/o even recompiling ANTLR itself.  The language=X option
 *  in a grammar file dictates which templates get loaded/used.
 *
 *  Some language need to handle cyclic DFAs differently than acyclic DFAs
 *  such as Java because it lacks a goto.  I had to build a set of templates
 *  that spit out bytecodes instead of Java (then I had to build the
 *  org.antlr.codegen.bytecode.* classes).  If you need to do them differently,
 *  define X_cyclicdfa.stg and the CodeGenerator will load that too else
 *  the cyclicDFATemplates just points at the normal templates.  There is
 *  a bit of overlap in the template names such as lookaheadTest.  The same
 *  code generation logic generations the edges for cyclic and acyclic DFAs
 *  so the X_cyclicdfa.stg file overrides those templates.  The CodeGenerator
 *  sets up X_cyclicdfa as a subgroup of X.
 *
 *  Some language like C need both parser files and header files.  Java needs
 *  to have a separate file for the cyclic DFA as ANTLR generates bytecodes
 *  directly (which cannot be in the generated parser Java file).  To facilitate
 *  this,
 *
 * cyclic can be in same file, but header, output must be searpate.  recognizer
 *  is in outptufile.
 */
public class CodeGenerator {
	// TODO move this and the templates to a separate file?
    public static final String VOCAB_FILE_EXTENSION = ".tokens";

    /** Which grammar are we generating code for?  Each generator
     *  is attached to a specific grammar.
     */
    protected Grammar grammar;

	/** The target specifies how to write out files and do other language
	 *  specific actions.
	 */
	protected Target target = null;

	/** Where are the templates this generator should use to generate code? */
	protected StringTemplateGroup templates;

	/** Where are the cyclic DFA templates this generator should
	 *  use to generate code?  We separate out the cyclic ones because
	 *  they could be very much more complicated than fixed lookahead.
	 *  For example, the java target generates bytecodes directly.
	 *  This group is set up as a subgroup of templates so you can override
	 *  just the templates that are different.
	 */
	protected StringTemplateGroup cyclicDFATemplates;

	/** The generated cyclic DFAs; need to be able to access from outside
	 *  to check bytecode gen for Java etc...
	 */
	protected StringTemplate cyclicDFAST = null;

    /** A reference to the ANTLR tool so we can learn about output directories
     *  and such.
     */
    protected Tool tool;

	// TODO move to separate file
	protected final String vocabFilePattern =
            "<tokens:{<attr.name>=<attr.type>\n}>" +
            "<literals:{<attr.name>=<attr.type>\n}>";

    /** Used by DFA state machine generator to avoid infinite recursion
     *  resulting from cycles int the DFA.  This is a set of int state #s.
     */
    protected IntSet visited;

    /** Used by the DFA state machine generator to get the max lookahead in
     *  the generated DFA for acyclic DFAs.
     */
    public int maxK;

    public CodeGenerator(Tool tool, Grammar grammar, String language) {
        this.tool = tool;
        this.grammar = grammar;
		loadLanguageTarget(language);
        loadTemplates(language);
		loadCyclicDFATemplates(language);
	}

	protected void loadLanguageTarget(String language) {
		String targetName = "org.antlr.codegen."+language+"Target";
		try {
			Class c = Class.forName(targetName);
			target = (Target)c.newInstance();
		}
		catch (ClassNotFoundException cnfe) {
			target = new Target(); // use default
		}
		catch (InstantiationException ie) {
			ErrorManager.error(ErrorManager.MSG_CANNOT_CREATE_TARGET_GENERATOR,
							   targetName,
							   ie);
		}
		catch (IllegalAccessException cnfe) {
			ErrorManager.error(ErrorManager.MSG_CANNOT_CREATE_TARGET_GENERATOR,
							   targetName,
							   cnfe);
		}
	}

	/** load the main language.stg template group file */
	protected void loadTemplates(String language) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String templateFileName = "org/antlr/codegen/templates/"+language+".stg";
		InputStream is = cl.getResourceAsStream(templateFileName);
		if ( is==null ) {
			ErrorManager.error(ErrorManager.MSG_MISSING_CODE_GEN_TEMPLATES,
							   language);
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		templates = new StringTemplateGroup(br,
				AngleBracketTemplateLexer.class,
				ErrorManager.getStringTemplateErrorListener());
		if ( !templates.isDefined("outputFile") ) {
			ErrorManager.error(ErrorManager.MSG_CODE_GEN_TEMPLATES_INCOMPLETE,
							   language);
		}
		try {
			br.close();
		}
		catch (IOException ioe) {
			ErrorManager.error(ErrorManager.MSG_CANNOT_CLOSE_FILE,
							   templateFileName,
							   ioe);
		}
	}

	protected void loadCyclicDFATemplates(String language) {
		// now load language_dfa.stg if available, else just use main .stg
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		String templateFileName = "org/antlr/codegen/templates/"+language+"_cyclicdfa.stg";
		InputStream is = cl.getResourceAsStream(templateFileName);
		if ( is!=null ) {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			cyclicDFATemplates =
				new StringTemplateGroup(br,
										AngleBracketTemplateLexer.class,
										ErrorManager.getStringTemplateErrorListener());
			// cyclic inherits from main X.stg templates so you can define
			// just the templates that are different
			cyclicDFATemplates.setSuperGroup(templates); // who's your daddy? ;)
			try {
				br.close();
			}
			catch (IOException ioe) {
				ErrorManager.error(ErrorManager.MSG_CANNOT_CLOSE_FILE,
								   templateFileName);
			}
		}
		else {
			cyclicDFATemplates = templates;
		}
		if ( cyclicDFATemplates.getInstanceOf("cyclicDFA")==null ) {
			ErrorManager.error(ErrorManager.MSG_MISSING_CYCLIC_DFA_CODE_GEN_TEMPLATES,
							   language);
		}
	}

    /** Given the grammar to which we are attached, walk the AST associated
     *  with that grammar to create NFAs.  Then create the DFAs for all
     *  decision points in the grammar by converting the NFAs to DFAs.
     *  Finally, walk the AST again to generate code.
	 *
	 *  Either 1, 2, or 3 files are written:
	 *
	 * 		recognizer: the main parser/lexer/treewalker item
	 * 		header file: language like C/C++ need extern definitions
	 * 		cyclic DFAs: might need cyclic DFAs in separate file; e.g., Java
	 * 				     generates bytecode directly to access goto instr.
	 *
	 *  The target, such as JavaTarget, dictates which files get written.
     */
    public void genRecognizer() {
		target.performGrammarAnalysis(this, grammar);

		// OUTPUT FILE (contains recognizerST)
		StringTemplate outputFileST = templates.getInstanceOf("outputFile");

		// RECOGNIZER
		StringTemplate recognizerST;
		if ( grammar.getType()==Grammar.LEXER ) {
			recognizerST = templates.getInstanceOf("lexer");
			outputFileST.setAttribute("LEXER", "true");
		}
		else if ( grammar.getType()==Grammar.PARSER ||
			      grammar.getType()==Grammar.COMBINED )
		{
			recognizerST = templates.getInstanceOf("parser");
			outputFileST.setAttribute("PARSER", "true");
		}
		else {
			recognizerST = templates.getInstanceOf("treeParser");
			outputFileST.setAttribute("TREE_PARSER", "true");
		}
		outputFileST.setAttribute("recognizer", recognizerST);

		// CYCLIC DFAs
		// Cyclic DFAs go into main recognizer ST by default

		cyclicDFAST = getCyclicDFATemplates().getInstanceOf("allCyclicDFAs");
		cyclicDFAST = target.chooseWhereCyclicDFAsGo(tool,
													 this,
													 grammar,
													 recognizerST,
													 cyclicDFAST);

		// HEADER FILE
		StringTemplate headerFileST = templates.getInstanceOf("headerFile");

		// Walk the AST holding the input grammar, this time generating code
		// Decisions are generated by using the precomputed DFAs
		// Fill in the various templates with data
		CodeGenTreeWalker gen = new CodeGenTreeWalker();
		try {
			gen.grammar((AST)grammar.getGrammarTree(),
						grammar,
						recognizerST,
						cyclicDFAST, // might point at recognizerST
						outputFileST,
						headerFileST);
		}
		catch (RecognitionException re) {
			ErrorManager.error(ErrorManager.MSG_BAD_AST_STRUCTURE,
							   re);
		}
		genTokenTypeDefinitions(recognizerST);
		genTokenTypeDefinitions(outputFileST);
		genTokenTypeDefinitions(headerFileST);

		try {
			target.genRecognizerFile(tool,this,grammar,outputFileST);
			target.genRecognizerHeaderFile(tool,this,grammar,headerFileST);
			target.genCyclicDFAFile(tool, this, grammar, cyclicDFAST);
			// write out the vocab interchange file; used by antlr,
			// does not change per target
			StringTemplate tokenVocabSerialization = genTokenVocabOutput();
			write(tokenVocabSerialization, getVocabFileName());
		}
		catch (IOException ioe) {
			ErrorManager.error(ErrorManager.MSG_CANNOT_WRITE_FILE,
							   getVocabFileName(),
							   ioe);
		}
	}

	// L O O K A H E A D  D E C I S I O N  G E N E R A T I O N

    /** Generate code that computes the predicted alt given a DFA.  The
	 *  cyclicDFATemplate can be either the main generated recognizerTemplate
	 *  for storage in the main parser file or a separate file.  It's up to
	 *  the code that ultimately invokes the codegen.g grammar rule (you pass
	 *  in where you want all cyclic DFAs to be stored).
	 */
	public StringTemplate genLookaheadDecision(StringTemplate cyclicDFATemplate,
                                               DFA dfa)
    {
        maxK = 1;
        StringTemplate decisionST;
        if ( !dfa.isCyclic() /* TODO: or too big */ ) {
            decisionST = genFixedLookaheadDecision(getTemplates(), dfa);
        }
        else {
            StringTemplate dfaST =
				genCyclicLookaheadDecision(getCyclicDFATemplates(), dfa);
            cyclicDFATemplate.setAttribute("cyclicDFAs", dfaST);
            decisionST = cyclicDFATemplates.getInstanceOf("dfaDecision");
			decisionST.setAttribute("description",
                                    dfa.getNFADecisionStartState().getDescription());
            decisionST.setAttribute("decisionNumber",
                                    new Integer(dfa.getDecisionNumber()));
        }
        return decisionST;
    }

    public StringTemplate genFixedLookaheadDecision(StringTemplateGroup templates,
                                                    DFA dfa)
    {
        return walkFixedDFACreatingEdges(templates, dfa, dfa.getStartState(), 1);
    }

    protected StringTemplate walkFixedDFACreatingEdges(
            StringTemplateGroup templates,
            DFA dfa,
            DFAState s,
            int k)
    {
        if ( s.isAcceptState() ) {
            StringTemplate dfaST = templates.getInstanceOf("dfaAcceptState");
            dfaST.setAttribute("alt", new Integer(s.getUniquelyPredictedAlt()));
            return dfaST;
        }
        if( k > maxK ) {
            // track max, but don't count the accept state...
            maxK = k;
        }
        StringTemplate dfaST = templates.getInstanceOf("dfaState");
		dfaST.setAttribute("stateNumber", new Integer(s.getStateNumber()));
        String description = dfa.getNFADecisionStartState().getDescription();
        if ( description!=null ) {
			description = Utils.replace(description,"\"", "\\\"");
            //dfaST.setAttribute("description", description);
        }
        int EOTPredicts = NFA.INVALID_ALT_NUMBER;
        for (int i = 0; i < s.getNumberOfTransitions(); i++) {
            Transition edge = (Transition) s.transition(i);
            if ( edge.getLabel().getAtom()==Label.EOT ) {
                // don't generate a real edge for EOT; track what EOT predicts
                DFAState target = (DFAState)edge.getTarget();
                EOTPredicts = target.getUniquelyPredictedAlt();
                continue;
            }
            StringTemplate edgeST = templates.getInstanceOf("dfaEdge");
            edgeST.setAttribute("labelExpr",
                                genLabelExpr(templates,edge.getLabel(),k));
            StringTemplate targetST =
                    walkFixedDFACreatingEdges(templates,
                                              dfa,
                                              (DFAState)edge.getTarget(),
                                              k+1);
            edgeST.setAttribute("targetState", targetST);
            dfaST.setAttribute("edges", edgeST);
        }
        if ( EOTPredicts!=NFA.INVALID_ALT_NUMBER ) {
            dfaST.setAttribute("eotPredictsAlt", new Integer(EOTPredicts));
        }
        return dfaST;
    }

    public StringTemplate genCyclicLookaheadDecision(StringTemplateGroup templates,
													 DFA dfa)
    {
		StringTemplate dfaST = templates.getInstanceOf("cyclicDFA");
        int d = dfa.getDecisionNumber();
        dfaST.setAttribute("decision", new Integer(d));
        visited = new BitSet(dfa.getNumberOfStates());
        walkCyclicDFACreatingStates(templates, dfaST, dfa.getStartState());
        return dfaST;
    }

    protected void walkCyclicDFACreatingStates(
            StringTemplateGroup templates,
            StringTemplate dfaST,
            DFAState s)
    {
        if ( visited.member(s.getStateNumber()) ) {
            return; // already visited
        }
        visited.add(s.getStateNumber());

        StringTemplate stateST;
        if ( s.isAcceptState() ) {
            stateST = templates.getInstanceOf("cyclicDFAAcceptState");
            stateST.setAttribute("predictAlt",
								 new Integer(s.getUniquelyPredictedAlt()));
        }
        else {
            stateST = templates.getInstanceOf("cyclicDFAState");
            stateST.setAttribute("needErrorClause", new Boolean(true));
        }
        stateST.setAttribute("stateNumber", new Integer(s.getStateNumber()));
        StringTemplate eotST = null;
        for (int i = 0; i < s.getNumberOfTransitions(); i++) {
            Transition edge = (Transition) s.transition(i);
            StringTemplate edgeST;
            if ( edge.getLabel().getAtom()==Label.EOT ) {
                // this is the default clause; must be last
                edgeST = templates.getInstanceOf("eotDFAEdge");
                stateST.removeAttribute("needErrorClause");
                eotST = edgeST;
            }
            else {
                edgeST = templates.getInstanceOf("cyclicDFAEdge");
                edgeST.setAttribute("labelExpr",
                        genLabelExpr(templates,edge.getLabel(),1));
            }
			edgeST.setAttribute("edgeNumber", new Integer(i+1));
            edgeST.setAttribute("targetStateNumber",
                                 new Integer(edge.getTarget().getStateNumber()));
            if ( edge.getLabel().getAtom()!=Label.EOT ) {
                stateST.setAttribute("edges", edgeST);
            }
            // now check other states
            walkCyclicDFACreatingStates(templates,
                                  dfaST,
                                  (DFAState)edge.getTarget());
        }
        if ( eotST!=null ) {
            stateST.setAttribute("edges", eotST);
        }
        dfaST.setAttribute("states", stateST);
    }

    protected StringTemplate genLabelExpr(StringTemplateGroup templates,
                                          Label label,
                                          int k)
    {
        if ( label.isSemanticPredicate() ) {
            return genSemanticPredicateExpr(templates, label);
        }
        if ( label.isSet() ) {
            return genSetExpr(templates, label, k);
        }
        // must be simple label
        StringTemplate eST = templates.getInstanceOf("lookaheadTest");
        eST.setAttribute("atom", grammar.getTokenTypeAsLabel(label.getAtom()));
		eST.setAttribute("atomAsInt", new Integer(label.getAtom()));
        eST.setAttribute("k", new Integer(k));
        return eST;
    }

    protected StringTemplate genSemanticPredicateExpr(StringTemplateGroup templates,
                                                      Label label)
    {
        SemanticContext semCtx = label.getSemanticContext();
        return semCtx.genExpr(templates);
    }

    public StringTemplate genSetExpr(StringTemplateGroup templates,
                                     Label label,
                                     int k)
    {
        IntSet set = label.getSet();
        return genSetExpr(templates, set, k);
    }

    /** For intervals such as [3..3, 30..35], generate an expression that
     *  tests the lookahead similar to LA(1)==3 || (LA(1)>=30&&LA(1)<=35)
     */
    public StringTemplate genSetExpr(StringTemplateGroup templates,
                                     IntSet set,
                                     int k)
    {
        IntervalSet iset = (IntervalSet)set;
        if ( !(iset instanceof IntervalSet) ) {
            throw new IllegalArgumentException("unable to generate expressions for non IntervalSet objects");
        }
        if ( iset.getIntervals()==null || iset.getIntervals().size()==0 ) {
            return new StringTemplate(templates, "");
        }
        StringTemplate setST = templates.getInstanceOf("setTest");
        Iterator iter = iset.getIntervals().iterator();
        int rangeNumber = 1;
		while (iter.hasNext()) {
            Interval I = (Interval) iter.next();
            int a = I.getA();
            int b = I.getB();
			StringTemplate eST;
            if ( a==b ) {
                eST = templates.getInstanceOf("lookaheadTest");
				eST.setAttribute("atom", grammar.getTokenTypeAsLabel(a));
				eST.setAttribute("atomAsInt", new Integer(a));
                //eST.setAttribute("k",new Integer(k));
            }
            else {
                eST = templates.getInstanceOf("lookaheadRangeTest");
                eST.setAttribute("lower",grammar.getTokenTypeAsLabel(a));
				eST.setAttribute("lowerAsInt", new Integer(a));
				eST.setAttribute("upper",grammar.getTokenTypeAsLabel(b));
				eST.setAttribute("upperAsInt", new Integer(b));
				eST.setAttribute("rangeNumber",new Integer(rangeNumber));
            }
			eST.setAttribute("k",new Integer(k));
			setST.setAttribute("ranges", eST);
			rangeNumber++;
        }
        return setST;
    }

    // T O K E N  D E F I N I T I O N  G E N E R A T I O N

	/** Set attributes tokens and literals attributes in the incoming
	 *  code template.  This is not the token vocab interchange file, but
	 *  rather a list of tokens/literals possibly needed by the recognizer.
	 */
	public void genTokenTypeDefinitions(StringTemplate code) {
        // make constants for the token names
        Iterator tokenNames = grammar.getTokenNames().iterator();
        while (tokenNames.hasNext()) {
            String tokenName = (String) tokenNames.next();
            int tokenType = grammar.getTokenType(tokenName);
            if ( tokenType>=Label.MIN_TOKEN_TYPE ) { // don't do FAUX labels
                code.setAttribute("tokens.{name,type}", tokenName, new Integer(tokenType));
            }
        }
    }

    /** Generate a token vocab file with all the token names/types.  For example:
     *  ID=7
	 *  FOR=8
	 *  This is independent of the target language; used by antlr internally
     */
    public StringTemplate genTokenVocabOutput() {
        StringTemplate vocabFileST =
                new StringTemplate(vocabFilePattern,
                                   AngleBracketTemplateLexer.class);
        // make constants for the token names
        Iterator tokenNames = grammar.getTokenNames().iterator();
        while (tokenNames.hasNext()) {
            String tokenName = (String) tokenNames.next();
            int tokenType = grammar.getTokenType(tokenName);
            if ( tokenType>=Label.MIN_TOKEN_TYPE ) {
                vocabFileST.setAttribute("tokens.{name,type}", tokenName, new Integer(tokenType));
            }
        }

        return vocabFileST;
    }

	// M I S C

	public StringTemplateGroup getTemplates() {
		return templates;
	}

	/** If cyclic DFAs generate bytecode, this will return the ST with the
	 *  generated code if genRecognizer() has been invoked previously.
	 */
	public StringTemplate getCyclicDFAByteCodeST() {
		if ( cyclicDFATemplates!=null ) {
			return cyclicDFAST;
		}
		return null;
	}

	public StringTemplateGroup getCyclicDFATemplates() {
		if ( cyclicDFATemplates!=null ) {
			return cyclicDFATemplates;
		}
		return getTemplates(); // return main template lib if no special dfa one
	}

	public String getRecognizerFileName() {
		StringTemplate extST = templates.getInstanceOf("codeFileExtension");
		return grammar.getName()+extST.toString();
	}

    public String getVocabFileName() {
        return grammar.getName()+VOCAB_FILE_EXTENSION;
    }

	public void write(StringTemplate code, String fileName) throws IOException {
        System.out.println("writing "+fileName);
        FileWriter fw = tool.getOutputFile(fileName);
        fw.write(code.toString());
        fw.close();
    }

}
