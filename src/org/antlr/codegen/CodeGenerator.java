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
import org.antlr.misc.*;
import org.antlr.Tool;
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
 */
public class CodeGenerator {
    public static final String VOCAB_FILE_EXTENSION = ".tokens";

    /** Which grammar are we generating code for?  Each generator
     *  is attached to a specific grammar.
     */
    protected Grammar grammar;

    /** Where are the templates this generator should use to generate code? */
    protected StringTemplateGroup templates;

    /** A reference to the ANTLR tool so we can learn about output directories
     *  and such.
     */
    protected Tool tool;

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
        ClassLoader cl = this.getClass().getClassLoader();
        InputStream is = cl.getResourceAsStream("org/antlr/codegen/templates/"+language+".stg");
        if ( is==null ) {
            System.err.println("can't load '"+"org/antlr/codegen/templates/"+language+".stg"+"' as resource");
            return;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        templates = new StringTemplateGroup(br,
                AngleBracketTemplateLexer.class,
                null);
    }

    /** Given the grammar to which we are attached, walk the AST associated
     *  with that grammar to create NFAs.  Then create the DFAs for all
     *  decision points in the grammar by converting the NFAs to DFAs.
     *  Finally, walk the AST again to generate code.  Result is a StringTemplate
     *  that is written out to a file.
     */
    public void genRecognizer() {
        StringTemplate outputFileST = templates.getInstanceOf("outputFile");
        StringTemplate recognizerST;
        if ( grammar.getType()==Grammar.LEXER ) {
            recognizerST = templates.getInstanceOf("lexer");
            outputFileST.setAttribute("LEXER", "true");
            outputFileST.setAttribute("streamType", "lexerStreamType");
        }
        else if ( grammar.getType()==Grammar.PARSER ) {
            recognizerST = templates.getInstanceOf("parser");
            outputFileST.setAttribute("PARSER", "true");
            outputFileST.setAttribute("streamType", "parserStreamType");
        }
        else {
            recognizerST = templates.getInstanceOf("treeParser");
            outputFileST.setAttribute("TREE_PARSER", "true");
        }
        outputFileST.setAttribute("recognizer", recognizerST);

        // Build NFAs from the grammar AST
        try {
            grammar.createNFAs();
        }
        catch (RecognitionException re) {
            System.err.println("problems creating NFAs from grammar AST for "+
                    grammar.getName());
            return;
        }

        // Create the DFA predictors for each decision
        grammar.createLookaheadDFAs();

        // Walk the AST again, this time generating code
        // Decisions are generated by using the precomputed DFAs
        CodeGenTreeWalker gen = new CodeGenTreeWalker();
        try {
            gen.grammar((AST)grammar.getGrammarTree(), grammar, recognizerST, outputFileST);
            String fileName = getRecognizerFileName();
            StringTemplate.setLintMode(true);
            // do actual write of code to the output
            write(outputFileST, fileName);
            // write out the vocab interchange file
            write(genTokenVocabOutput(), getVocabFileName());
        }
        catch (IOException ioe) {
            System.err.println("could not write generated code for "+
                    grammar.getName()+":"+ioe);
        }
        catch (RecognitionException re) {
            System.err.println("problems walking tree to generate code for "+
                    grammar.getName()+":"+re);
        }
    }

    public void write(StringTemplate code, String fileName) throws IOException {
        System.out.println("writing "+fileName);
        FileWriter fw = tool.getOutputFile(fileName);
        fw.write(code.toString());
        fw.close();
    }

    public StringTemplate genLookaheadDecision(StringTemplateGroup templates,
                                               StringTemplate recognizerTemplate,
                                               DFA dfa)
    {
        maxK = 1;
        StringTemplate decisionST;
        if ( !dfa.isCyclic() /* TODO: or too big */ ) {
            decisionST = genFixedLookaheadDecision(templates, dfa);
        }
        else {
            StringTemplate dfaST = genCyclicLookaheadDecision(templates, dfa);
            recognizerTemplate.setAttribute("DFAs", dfaST);
            decisionST = templates.getInstanceOf("dfaDecision");
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
        String description = dfa.getNFADecisionStartState().getDescription();
        if ( description!=null ) {
            dfaST.setAttribute("description", Utils.replace(description,"\"", "\\\""));
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
            stateST.setAttribute("predictAlt", new Integer(s.getUniquelyPredictedAlt()));
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
        while (iter.hasNext()) {
            Interval I = (Interval) iter.next();
            int a = I.getA();
            int b = I.getB();
            if ( a==b ) {
                StringTemplate eST = templates.getInstanceOf("lookaheadTest");
                eST.setAttribute("atom", grammar.getTokenTypeAsLabel(a));
                setST.setAttribute("ranges", eST);
                eST.setAttribute("k",new Integer(k));
            }
            else {
                StringTemplate eST = templates.getInstanceOf("lookaheadRangeTest");
                eST.setAttribute("lower",grammar.getTokenTypeAsLabel(a));
                eST.setAttribute("upper",grammar.getTokenTypeAsLabel(b));
                eST.setAttribute("k",new Integer(k));
                setST.setAttribute("ranges", eST);
            }
        }
        return setST;
    }

    public void genTokenTypeDefinitions(StringTemplate code) {
        // make constants for the token names
        Iterator tokenNames = grammar.getTokenNames().iterator();
        while (tokenNames.hasNext()) {
            String tokenName = (String) tokenNames.next();
            int tokenType = grammar.getTokenType(tokenName);
            if ( tokenType>=Label.MIN_TOKEN_TYPE ) {
                code.setAttribute("tokens.{name,type}", tokenName, new Integer(tokenType));
            }
        }

        if ( grammar.getType()==Grammar.LEXER ) {
            // make a literals hash table or whatever target requires for literals
            Iterator stringLiterals = grammar.getStringLiterals().iterator();
            while (stringLiterals.hasNext()) {
                String literal = (String) stringLiterals.next();
                int tokenType = grammar.getTokenType(literal);
                code.setAttribute("literals.{name,type}", literal, new Integer(tokenType));
            }
        }
    }

    /** Generate a token vocab file with all the token names, string/char
     *  literals.  For example:
     *  "long"=80040
     *  D=77777
     *  ';'=22
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

        // make a literals hash table or whatever target requires for literals
        Iterator stringLiterals = grammar.getStringLiterals().iterator();
        while (stringLiterals.hasNext()) {
            String literal = (String) stringLiterals.next();
            int tokenType = grammar.getTokenType(literal);
            vocabFileST.setAttribute("literals.{name,type}", literal, new Integer(tokenType));
        }
        return vocabFileST;
    }

    public StringTemplateGroup getTemplates() {
        return templates;
    }

    public String getRecognizerFileName() {
        StringTemplate extST = templates.getInstanceOf("codeFileExtension");
        return grammar.getName()+extST.toString();
    }

    public String getVocabFileName() {
        return grammar.getName()+VOCAB_FILE_EXTENSION;
    }
}
