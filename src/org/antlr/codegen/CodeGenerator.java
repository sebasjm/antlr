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
import org.antlr.tool.*;
import org.antlr.misc.*;
import org.antlr.Tool;
import org.antlr.runtime.Token;
import org.antlr.codegen.bytecode.ClassFile;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.TokenWithIndex;

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

	public static int escapedCharValue[] = new int[255];
	public static String charValueEscape[] = new String[255];

	static {
		escapedCharValue['n'] = '\n';
		escapedCharValue['r'] = '\r';
		escapedCharValue['t'] = '\t';
		escapedCharValue['b'] = '\b';
		escapedCharValue['f'] = '\f';
		escapedCharValue['\\'] = '\\';
		escapedCharValue['\''] = '\'';
		charValueEscape['\n'] = "\\n";
		charValueEscape['\r'] = "\\r";
		charValueEscape['\t'] = "\\t";
		charValueEscape['\b'] = "\\b";
		charValueEscape['\f'] = "\\f";
		charValueEscape['\\'] = "\\\\";
		charValueEscape['\''] = "\\'";
	}

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

	protected StringTemplate recognizerST;
	protected StringTemplate outputFileST;
	protected StringTemplate headerFileST;

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
		// CREATE NFA FROM GRAMMAR, CREATE DFA FROM NFA
		target.performGrammarAnalysis(this, grammar);

		// OPTIMIZE DFA
        DFAOptimizer optimizer = new DFAOptimizer(grammar);
		optimizer.optimize();

		// OUTPUT FILE (contains recognizerST)
		outputFileST = templates.getInstanceOf("outputFile");

		// RECOGNIZER
		if ( grammar.type==Grammar.LEXER ) {
			recognizerST = templates.getInstanceOf("lexer");
			outputFileST.setAttribute("LEXER", "true");
		}
		else if ( grammar.type==Grammar.PARSER ||
			      grammar.type==Grammar.COMBINED )
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
		if ( templates.isDefined("headerFile") ) {
			headerFileST = templates.getInstanceOf("headerFile");
		}
		else {
			// create a dummy to avoid null-checks all over code generator
			headerFileST = new StringTemplate(templates,"");
		}

		// GENERATE RECOGNIZER
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
		genTokenTypeConstants(recognizerST);
		genTokenTypeConstants(outputFileST);
		genTokenTypeConstants(headerFileST);

		if ( grammar.type!=Grammar.LEXER ) {
			genTokenTypeNames(recognizerST);
			genTokenTypeNames(outputFileST);
			genTokenTypeNames(headerFileST);
		}

		// WRITE FILES
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

	/*
	protected void fillFOLLOWSets(StringTemplate recognizerST,
								  StringTemplate outputFileST,
								  StringTemplate headerFileST)
	{
		long start = System.currentTimeMillis();
		for (Iterator itr = grammar.getRules().iterator(); itr.hasNext();) {
			Grammar.Rule r = (Grammar.Rule) itr.next();
			LookaheadSet follow = grammar.FOLLOW(r.name);
			//LookaheadSet follow = new LookaheadSet();
			System.out.println("FOLLOW("+r.name+")="+follow.toString(grammar));
			// TODO: not sending in EOF for FOLLOW sets! (might not need;
			// consume until will know to stop at EOF)
			long[] words = null;
			if ( follow.tokenTypeSet==null ) {
				words = new long[1];
			}
			else {
				BitSet bits = BitSet.of(follow.tokenTypeSet);
				//bits.remove(Label.EOF);
				words = bits.toPackedArray();
			}
			recognizerST.setAttribute("bitsets.{name,inName,bits}",
									  r.name,
									  words);
			outputFileST.setAttribute("bitsets.{name,inName,bits}",
									  r.name,
									  words);
			headerFileST.setAttribute("bitsets.{name,inName,bits}",
									  r.name,
									  words);
		}
		long stop = System.currentTimeMillis();
		System.out.println("FOLLOW sets computed in "+(int)(stop-start)+" ms");
	}
    */

	/** Error recovery in ANTLR recognizers.
	 *
	 *  Based upon original ideas:
	 *
	 *  Algorithms + Data Structures = Programs by Niklaus Wirth
	 *
	 *  and
	 *
	 *  A note on error recovery in recursive descent parsers:
	 *  http://portal.acm.org/citation.cfm?id=947902.947905
	 *
	 *  Later, Josef Grosch had some good ideas:
	 *  Efficient and Comfortable Error Recovery in Recursive Descent Parsers:
	 *  ftp://www.cocolab.com/products/cocktail/doca4.ps/ell.ps.zip
	 *
	 *  Like Grosch I implemented local FOLLOW sets that are combined at run-time
	 *  upon error to avoid parsing overhead.
	 */
	public void generateLocalFOLLOW(GrammarAST referencedElementNode,
									String referencedElementName,
									String enclosingRuleName)
	{
		NFAState followingNFAState = referencedElementNode.followingNFAState;
		int i = ((TokenWithIndex)referencedElementNode.getToken()).getIndex();
		/*
		System.out.print("compute FOLLOW "+referencedElementNode.toString()+
						 " for "+referencedElementName+"#"+i+" in "+
						 enclosingRuleName);
		*/
		LookaheadSet follow = grammar.LOOK(followingNFAState);
		//System.out.println(" "+follow);

		long[] words = null;
		if ( follow.tokenTypeSet==null ) {
			words = new long[1];
		}
		else {
			BitSet bits = BitSet.of(follow.tokenTypeSet);
			words = bits.toPackedArray();
		}
		recognizerST.setAttribute("bitsets.{name,inName,bits,tokenIndex}",
								  referencedElementName,
								  enclosingRuleName,
								  words,
								  new Integer(i));
		outputFileST.setAttribute("bitsets.{name,inName,bits,tokenIndex}",
								  referencedElementName,
								  enclosingRuleName,
								  words,
								  new Integer(i));
		headerFileST.setAttribute("bitsets.{name,inName,bits,tokenIndex}",
								  referencedElementName,
								  enclosingRuleName,
								  words,
								  new Integer(i));
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
			String description = dfa.getNFADecisionStartState().getDescription();
			if ( description!=null ) {
				description = Utils.replace(description,"\"", "\\\"");
				decisionST.setAttribute("description", description);
			}
            decisionST.setAttribute("decisionNumber",
                                    new Integer(dfa.getDecisionNumber()));
        }
        return decisionST;
    }

    public StringTemplate genFixedLookaheadDecision(StringTemplateGroup templates,
                                                    DFA dfa)
    {
        return walkFixedDFACreatingEdges(templates, dfa, dfa.startState, 1);
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
		GrammarAST decisionASTNode =
			dfa.getNFADecisionStartState().getDecisionASTNode();
        StringTemplate dfaST = templates.getInstanceOf("dfaState");
		if ( decisionASTNode.getType()==ANTLRParser.EOB ) {
			dfaST = templates.getInstanceOf("dfaLoopbackState");
		}
		else if ( decisionASTNode.getType()==ANTLRParser.OPTIONAL ) {
			dfaST = templates.getInstanceOf("dfaOptionalBlockState");			
		}
		dfaST.setAttribute("stateNumber", new Integer(s.stateNumber));
        String description = dfa.getNFADecisionStartState().getDescription();
		//System.out.println("DFA: "+description+" associated with AST "+decisionASTNode);
        if ( description!=null ) {
			description = Utils.replace(description,"\"", "\\\"");
            dfaST.setAttribute("description", description);
        }
        int EOTPredicts = NFA.INVALID_ALT_NUMBER;
        for (int i = 0; i < s.getNumberOfTransitions(); i++) {
            Transition edge = (Transition) s.transition(i);
            if ( edge.label.getAtom()==Label.EOT ) {
                // don't generate a real edge for EOT; track what EOT predicts
                DFAState target = (DFAState)edge.target;
                EOTPredicts = target.getUniquelyPredictedAlt();
                continue;
            }
			StringTemplate edgeST = templates.getInstanceOf("dfaEdge");
            edgeST.setAttribute("labelExpr",
                                genLabelExpr(templates,edge.label,k));
            StringTemplate targetST =
                    walkFixedDFACreatingEdges(templates,
                                              dfa,
                                              (DFAState)edge.target,
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
        walkCyclicDFACreatingStates(templates, dfaST, dfa.startState);
        return dfaST;
    }

    protected void walkCyclicDFACreatingStates(
            StringTemplateGroup templates,
            StringTemplate dfaST,
            DFAState s)
    {
        if ( visited.member(s.stateNumber) ) {
            return; // already visited
        }
        visited.add(s.stateNumber);

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
        stateST.setAttribute("stateNumber", new Integer(s.stateNumber));
        StringTemplate eotST = null;
        for (int i = 0; i < s.getNumberOfTransitions(); i++) {
            Transition edge = (Transition) s.transition(i);
            StringTemplate edgeST;
            if ( edge.label.getAtom()==Label.EOT ) {
                // this is the default clause; has to held until last
                edgeST = templates.getInstanceOf("eotDFAEdge");
                stateST.removeAttribute("needErrorClause");
                eotST = edgeST;
            }
            else {
                edgeST = templates.getInstanceOf("cyclicDFAEdge");
                edgeST.setAttribute("labelExpr",
                        genLabelExpr(templates,edge.label,1));
            }
			edgeST.setAttribute("edgeNumber", new Integer(i+1));
            edgeST.setAttribute("targetStateNumber",
                                 new Integer(edge.target.stateNumber));
            if ( edge.label.getAtom()!=Label.EOT ) {
                stateST.setAttribute("edges", edgeST);
            }
            // now check other states
            walkCyclicDFACreatingStates(templates,
                                  dfaST,
                                  (DFAState)edge.target);
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
            int a = I.a;
            int b = I.b;
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
	 *  rather a list of token type ID needed by the recognizer.
	 */
	protected void genTokenTypeConstants(StringTemplate code) {
		// make constants for the token types
		Iterator tokenIDs = grammar.getTokenIDs().iterator();
		while (tokenIDs.hasNext()) {
			String tokenID = (String) tokenIDs.next();
			int tokenType = grammar.getTokenType(tokenID);
			if ( tokenType>=Label.MIN_TOKEN_TYPE ) { // don't do FAUX labels
				code.setAttribute("tokens.{name,type}", tokenID, new Integer(tokenType));
			}
		}
	}

	/** Generate a token names table that maps token type to a printable
	 *  name: either the label like INT or the literal like "begin".
	 */
	protected void genTokenTypeNames(StringTemplate code) {
		for (int t=Label.MIN_TOKEN_TYPE; t<=grammar.getMaxTokenType(); t++) {
			String tokenName = grammar.getTokenName(t);
			if ( tokenName.charAt(0)=='\"' ) {
				tokenName = Utils.replace(tokenName,"\"", "\\\"");
			}
			tokenName = '\"'+tokenName+'\"';
			code.setAttribute("tokenNames", tokenName);
		}
	}

    /** Generate a token vocab file with all the token names/types.  For example:
     *  ID=7
	 *  FOR=8
	 *  This is independent of the target language; used by antlr internally
     */
    protected StringTemplate genTokenVocabOutput() {
        StringTemplate vocabFileST =
                new StringTemplate(vocabFilePattern,
                                   AngleBracketTemplateLexer.class);
        // make constants for the token names
        Iterator tokenIDs = grammar.getTokenIDs().iterator();
        while (tokenIDs.hasNext()) {
            String tokenID = (String) tokenIDs.next();
            int tokenType = grammar.getTokenType(tokenID);
            if ( tokenType>=Label.MIN_TOKEN_TYPE ) {
                vocabFileST.setAttribute("tokens.{name,type}", tokenID, new Integer(tokenType));
            }
        }

        return vocabFileST;
    }

	// A C T I O N  T R A N S L A T I O N

	/** Given an action string with @x.y and @x references, convert it
	 *  to a StringTemplate (that will be inserted into the output StringTemplate)
	 *  Replace @ references to template references.  Targets can then say
	 *  how to translate these references with a template rather than code.
	 *
	 *  Jump from '@' to '@' in the action, building up a text buffer
	 *  doing appropriate rewrites to template refs.  Final step, create
	 *  the StringTemplate (make it part of the incoming group).
	 */
	public String translateAction(String ruleName,
								  String action)
	{
		Grammar.Rule r = null;
		if ( ruleName!=null ) {
			r = grammar.getRule(ruleName);
		}
		StringBuffer buf = new StringBuffer();
		for (int c=0; c<action.length(); c++) {
			int attrStart = 0;
			int attrStop = 0;
			if ( action.charAt(c)!='@' ) {
				buf.append(action.charAt(c));
				continue;
			}
			int scopeStart = c+1;
			int i = c+1;
			while ( i<action.length() &&
				    Character.isLetterOrDigit(action.charAt(i)) )
			{
				i++;
			}
			int scopeEnd = i-1; // i points at char past first ID
			String id = action.substring(scopeStart,scopeEnd+1);
			StringTemplate refST = null;
			if ( r!=null && r.tokenLabels!=null && r.tokenLabels.get(id)!=null ) {
				// not a scope, but is token ref
				String label = id;
				// get attribute name (if any)
				refST = templates.getInstanceOf("tokenLabelRef");
				if ( action.charAt(i)=='.' ) { // @scope.attr?
					i++;
					attrStart = i;
					while ( i<action.length() &&
				            Character.isLetterOrDigit(action.charAt(i)) ) {
						i++;
					}
					attrStop = i-1;
					id = action.substring(attrStart,attrStop+1);
					if ( Token.predefinedTokenProperties.contains(id) ) {
						refST = templates.getInstanceOf("tokenLabelPropertyRef_"+id);
					}
					c = attrStop;
				}
				else {
					c = scopeEnd;
				}
				refST.setAttribute("label",label);
				buf.append(refST.toString());
			}
			else if ( r!=null && r.ruleLabels!=null && r.ruleLabels.get(id)!=null ) {
				// not a scope, but is token ref
				String label = id;
				// get attribute name (if any)
				if ( action.charAt(i)=='.' ) { // @scope.attr?
					i++;
					attrStart = i;
					while ( i<action.length() &&
				            Character.isLetterOrDigit(action.charAt(i)) ) {
						i++;
					}
					attrStop = i-1;
					id = action.substring(attrStart,attrStop+1);
					if ( Grammar.Rule.predefinedRuleProperties.contains(id) ) {
						refST = templates.getInstanceOf("ruleLabelPropertyRef_"+id);
					}
					c = attrStop;
				}
				else {
					System.err.println("rule labels must be followed by a property reference.");
					c = scopeEnd;
				}
				refST.setAttribute("label",label);
				buf.append(refST.toString());
			}
			else if ( grammar.isValidScope(r,id) ) {
				String scopeName = id;
				// get attribute name (if any)
				if ( action.charAt(i)=='.' ) { // @scope.attr?
					i++;
					attrStart = i;
					while ( i<action.length() &&
				            Character.isLetterOrDigit(action.charAt(i)) ) {
						i++;
					}
					attrStop = i-1;
					id = action.substring(attrStart,attrStop+1);
					AttributeScope scope =
						grammar.getScopeContainingAttribute(r,scopeName,id);
					refST = scope.getAttributeReferenceTemplate(templates);
					refST.setAttribute("scope",scope);
					AttributeScope.Attribute attr =
						(AttributeScope.Attribute)scope.attributes.get(id);
					if ( attr==null ) {
						attr = new AttributeScope.Attribute();
						attr.name = id; // if can't identify, just spit out
					}
					refST.setAttribute("attr",attr);
					// do early evaluation of attribute ref
					buf.append(refST.toString());
					c = attrStop;
				}
				else { // reference to just @scope at this point
					refST = templates.getInstanceOf("scopeRef");
					AttributeScope scope = grammar.getScope(scopeName);
					refST.setAttribute("scope",scope);
					buf.append(refST.toString());
					c = scopeEnd;
				}
			}
			else { // must be reference to @attr; look up in rule,param,ret scope
				AttributeScope scope =
					grammar.getScopeContainingAttribute(r,null,id);
				if ( scope!=null ) {
					refST = scope.getAttributeReferenceTemplate(templates);
					refST.setAttribute("scope",scope);
					AttributeScope.Attribute attr =
						(AttributeScope.Attribute)scope.attributes.get(id);
					refST.setAttribute("attr",attr);
					buf.append(refST.toString());
				}
				else {
					buf.append(id);
				}
				c = scopeEnd;
			}
			//grammar.resolveToFullyQualified
		}
		//System.out.println("template="+buf.toString());
		return buf.toString();
	}

	// C H A R  T R A N S L A T I O N

	/** The antlr.g grammar converts the escaped literal '\'' to ''' and
	 *  all other escapes so char literals always have just a unicode 16-bit
	 *  char value.  Use this method to get the escaped grammar literal back out,
	 *  such as for printing out a grammar.
	 */
	public static String getJavaEscapedCharFromANTLRLiteral(String literal) {
		int c = Grammar.getCharValueFromANTLRGrammarLiteral(literal);
		return "'"+CodeGenerator.getJavaUnicodeEscapeString(c)+"'";
	}

	/** Given a literal like "\nfoo\\", antlr.g converts it to the actual
	 *  char sequence implied by the escaped char seq.  To get an escaped
	 *  literal back out for printing grammars and such, use this routine.
	 *  Leave the double quotes on the outside of the literal.
	 */
	public static String getJavaEscapedStringFromANTLRLiteral(String literal) {
		StringBuffer buf = new StringBuffer();
		buf.append('"');
		for (int i=1; i<literal.length()-1; i++) {
			int c = literal.charAt(i);
			if ( c<CodeGenerator.charValueEscape.length &&
				 CodeGenerator.charValueEscape[c]!=null )
			{
				buf.append(CodeGenerator.charValueEscape[c]);
			}
			else {
				buf.append((char)c);
			}
		}
		buf.append('"');
		return buf.toString();
	}

	/** Return a string representing the escaped char for code c.  E.g., If c
     *  has value 0x100, you will get "\u0100".  ASCII gets the usual
     *  char (non-hex) representation.  Control characters are spit out
     *  as unicode.  While this is specially set up for returning Java strings,
	 *  it can be used by any language target that has the same syntax. :)
	 *  Note that this method does NOT put the quotes around it so that the
	 *  method is more reusable.
     */
    public static String getJavaUnicodeEscapeString(int c) {
        if ( c<charValueEscape.length && charValueEscape[c]!=null ) {
            return charValueEscape[c];
        }
        if ( Character.UnicodeBlock.of((char)c)==Character.UnicodeBlock.BASIC_LATIN &&
             !Character.isISOControl((char)c) ) {
            if ( c=='\\' ) {
                return "\\\\";
            }
            if ( c=='\'') {
                return "\\'";
            }
            return Character.toString((char)c);
        }
        // turn on the bit above max '\uFFFF' value so that we pad with zeros
        // then only take last 4 digits
        String hex = Integer.toHexString(c|0x10000).toUpperCase().substring(1,5);
        String unicodeStr = "\\u"+hex;
        return unicodeStr;
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
		return grammar.name+extST.toString();
	}

    public String getVocabFileName() {
        return grammar.name+VOCAB_FILE_EXTENSION;
    }

	public void write(StringTemplate code, String fileName) throws IOException {
        System.out.println("writing "+fileName);
        FileWriter fw = tool.getOutputFile(fileName);
        fw.write(code.toString());
        fw.close();
    }

}
