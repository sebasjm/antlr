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
package org.antlr.tool;

import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.TokenStreamException;

import java.io.*;
import java.util.*;
import org.antlr.analysis.*;
import org.antlr.runtime.IntegerStream;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.antlr.codegen.CodeGenerator;
import org.antlr.misc.IntSet;
import org.antlr.misc.IntervalSet;

/** Represents a grammar in memory. */
public class Grammar {
    public static final int INITIAL_DECISION_LIST_SIZE = 300;
    public static final int INVALID_RULE_INDEX = -1;

    public static final String TOKEN_RULENAME = "Tokens";

    public static final int LEXER = 1;
    public static final int PARSER = 2;
    public static final int TREE_PARSER = 3;

    /** What name did the user provide for this grammar? */
    protected String name;

    /** What type of grammar is this: lexer, parser, tree walker */
    protected int type;

    /** A list of options specified at the grammar level such as language=Java.
     *  The value can be an AST for complicated values such as character sets.
     *  There may be code generator specific options in here.  I do no
     *  interpretation of the key/value pairs...they are simply available for
     *  who wants them.
     *  TODO: do error checking on unknown option names?
     */
    protected Map options = new HashMap();

    public static final Map defaultOptions =
            new HashMap() {{put("language","Java");}};

    /** The NFA that represents the grammar with edges labelled with tokens
     *  or epsilon.  It is more suitable to analysis than an AST representation.
     */
    protected NFA nfa;

    /** Tokens/literals (anything but char) are uniquely indexed.
     *  with -1 implying EOF.  Characters are different; they go from
     *  -1 (EOF) to \uFFFE.  For example, 0 could be a binary byte you
     *  want to lexer.  Labels of DFA/NFA transitions can be both tokens
     *  and characters.  I use negative numbers for bookkeeping labels
     *  like EPSILON. Token types are above the max char '\uFFFF' so
     *  that char literals and token types can exist in the same space
     *  and not step on each other.
     */
    protected int tokenType = Label.MIN_TOKEN_TYPE;

    /** Map token like ID (but not literals like "while") to its token type */
    protected Map tokenNameToTypeMap = new HashMap();

    /** Map token literals like "while" to its token type.  It may be that
     *  WHILE="while"=35, in which case both tokenNameToTypeMap and this
     *  field will have entries both mapped to 35.
     */
    protected Map stringLiteralToTypeMap = new HashMap();

    /** Map char literals like 'a' to it's unicode int value as token type. */
    protected Map charLiteralToTypeMap = new HashMap();

    /** Map a token type to its token name.  Must subtract MIN_TOKEN_TYPE from index. */
    protected Vector typeToTokenList = new Vector();

    /** Be able to assign a number to every decision in grammar;
     *  decisions in 1..n
     */
    protected int decisionNumber = 0;

    /** Rules are uniquely labeled from 1..n */
    protected int ruleIndex = 1;

    /** Map a rule name to its index */
    protected Map ruleToIndexMap = new HashMap();

    /** Map a rule name to its start state in this NFA. */
    protected Map ruleToStartStateMap = new HashMap();

    /** Map a rule name to its stop state in this NFA. */
    protected Map ruleToStopStateMap = new HashMap();

    /** An AST that records entire input grammar with all rules.  A simple
     *  grammar with one rule, "grammar t; a : A | B ;", looks like:
     * ( grammar t ( rule a ( BLOCK ( ALT A ) ( ALT B ) ) <end-of-rule> ) )
     */
    protected GrammarAST grammarTree = null;

    /** Map a rule name to the AST created for it.  Points into
     *  the AST pointed at by grammarTree.
     */
    protected Map ruleToTreeMap = new HashMap();

    /** Each subrule/rule is a decision point and we must track them so we
     *  can go back later and build DFA predictors for them.  This includes
     *  all the rules, subrules, optional blocks, ()+, ()* etc...  The
     *  elements in this list are NFAState objects.
     */
    protected Vector decisionNFAStartStateList = new Vector(INITIAL_DECISION_LIST_SIZE);

    /** Subrules may have options.  These options apply to any and all decisions
     *  associated with the subrule.
     */
    protected Vector decisionOptionsList = new Vector(INITIAL_DECISION_LIST_SIZE);

    /** Track the DFA for the decision points.  Create after we know how
     *  many decisions there are.  use Vector as List can't increase size
     *  without add and I need to set/get via an index. :(
     */
    protected Vector decisionLookaheadDFAList;

    /** Map a rule index to its name; use a Vector on purpose as new
     *  collections stuff won't let me setSize and make it grow.  :(
     */
    protected Vector ruleIndexToRuleList = new Vector();

    /** If non-null, this is the code generator we will use to generate
     *  recognizers in the target language.
     */
    protected CodeGenerator generator;

    protected static int escapedCharValue[] = new int[255];
    protected static String charValueEscape[] = new String[255];

    static {
        escapedCharValue['n'] = '\n';
        escapedCharValue['r'] = '\r';
        escapedCharValue['t'] = '\t';
        escapedCharValue['b'] = '\b';
        escapedCharValue['f'] = '\f';
        charValueEscape['\n'] = "\\n";
        charValueEscape['\r'] = "\\r";
        charValueEscape['\t'] = "\\t";
        charValueEscape['\b'] = "\\b";
        charValueEscape['\f'] = "\\f";
    }

    public Grammar(String grammarString)
            throws antlr.RecognitionException, antlr.TokenStreamException
    {
        this(new StringReader(grammarString));
    }

    /** Create a grammar from a Reader.  Parse the grammar, building a tree
     *  and loading a symbol table of sorts here in Grammar.  Then create
     *  an NFA and associated factory.  Walk the AST representing the grammar,
     *  building the state clusters of the NFA.
     */
    public Grammar(Reader r)
            throws antlr.RecognitionException, antlr.TokenStreamException
    {
        initTokenSymbolTables();

        // BUILD AST FROM GRAMMAR
        ANTLRLexer lexer = new ANTLRLexer(r);
        ANTLRParser parser = new ANTLRParser(lexer, this);
        parser.setASTNodeClass("org.antlr.tool.GrammarAST");
        parser.grammar();
        grammarTree = (GrammarAST)parser.getAST();
    }

    /** Parse a rule we add artificially that is a list of the other lexer
     *  rules like this: "Tokens : ID | INT | SEMI ;"  nextToken() will invoke
     *  this to set the current token.  Add char literals before
     *  the rule references.
     */
    public void addArtificialMatchTokensRule() {
        StringTemplate matchTokenRuleST =
            new StringTemplate(
                    TOKEN_RULENAME+" : <rules; separator=\"|\">;",
                    AngleBracketTemplateLexer.class);

        // Add literals first as they are a special case of anything afterwards
        Set charLiterals = charLiteralToTypeMap.keySet();
        Iterator iter = charLiterals.iterator();
        while (iter.hasNext()) {
            String literal = (String) iter.next();
            StringTemplate emitST = generator.getTemplates().getInstanceOf("emit");
            emitST.setAttribute("type", charLiteralToTypeMap.get(literal));
            String charRef = literal+" {"+emitST.toString()+"}";
            matchTokenRuleST.setAttribute("rules", charRef);
        }

        // Now add token rule references
        Set ruleNames = getRules();
        iter = ruleNames.iterator();
        while (iter.hasNext()) {
            String name = (String) iter.next();
            matchTokenRuleST.setAttribute("rules", name);
        }

        ANTLRLexer lexer = new ANTLRLexer(new StringReader(matchTokenRuleST.toString()));
        ANTLRParser parser = new ANTLRParser(lexer, this);
        parser.setASTNodeClass("org.antlr.tool.GrammarAST");
        try {
            parser.rule();
            grammarTree.addChild(parser.getAST());
        }
        catch (Exception e) {
            System.err.println("problems adding artificial rule");
        }
    }

    protected void initTokenSymbolTables() {
        // the faux token types take first NUM_FAUX_LABELS positions
        typeToTokenList.setSize(Label.NUM_FAUX_LABELS); // ensure room
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.INVALID, "<INVALID>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EOT, "<EOT>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.SEMPRED, "<SEMPRED>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.SET, "<SET>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EPSILON, Label.EPSILON_STR);
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EOF, "<EOF>");
        tokenNameToTypeMap.put("<INVALID>", new Integer(Label.INVALID));
        tokenNameToTypeMap.put("<EOT>", new Integer(Label.EOT));
        tokenNameToTypeMap.put("<SEMPRED>", new Integer(Label.SEMPRED));
        tokenNameToTypeMap.put("<SET>", new Integer(Label.SET));
        tokenNameToTypeMap.put("<EPSILON>", new Integer(Label.EPSILON));
        tokenNameToTypeMap.put("<EOF>", new Integer(Label.EOF));
    }

    /** Look in the current directory for vocabName.tokens and load any
     *  definitions in there into the tokenNameToTypeMap.  The format of
     *  the file is a simple token=type for the most part:
     *
     *     "begin"=4
     *     LEXER=5
     *     ...
     *
     *  though sometimes you will see double assignments like:
     *
     *     BEGIN="begin"=342
     *
     *  indicating that the token has both a name and a literal string value.
     *  Also, you will see characters such as:
     *
     *     '0'=48
     *     '\u00FF'=255
     *
     *  which are available so that a lexer can import the vocab and
     *  match the appropriate chars as token types.
     *
     *  TODO: the double assign doesn't work yet
     */
    protected void importTokenVocab(String vocabName) {
        try {
            FileReader fr = new FileReader(vocabName+".tokens");
            BufferedReader br = new BufferedReader(fr);
            //StringTokenizer tokenizer = new StringTokenizer()
            StreamTokenizer st = new StreamTokenizer(br);
            st.parseNumbers();
            st.wordChars('A', 'Z');
            st.wordChars('a', 'z');
            st.wordChars('_', '_');
            st.ordinaryChar('=');
            st.ordinaryChar(',');
            st.slashSlashComments(true);

            int token = st.nextToken();
            loop:
            while (token != StreamTokenizer.TT_EOF) {
                switch (token) {
                    case StreamTokenizer.TT_WORD:
                    case '"':
                    case '\'':
                        // get name or literal
                        String tokenName = st.sval;
                        if ( token=='"' ) {
                            tokenName = '"'+tokenName+'"';
                        }
                        else if ( token=='\'' ) {
                            tokenName = '\''+tokenName+'\'';
                        }
                        token = st.nextToken(); // get '='
                        if ( token!='=' ) {
                            throw new Exception("line "+st.lineno()+": missing '='");
                        }
                        token = st.nextToken(); // get type value
                        if ( token!=StreamTokenizer.TT_NUMBER ) {
                            throw new Exception("line "+st.lineno()+
                                                ": missing token type value at "+
                                                st.sval);
                        }
                        int tokenType = (int)st.nval;
                        //System.out.println("import "+tokenName+"="+tokenType);
                        defineToken(tokenName, tokenType);
                        token = st.nextToken(); // move to next assignment
                        break;
                    default:
                        char ch = (char)st.ttype;
                        throw new Exception("line "+st.lineno()+": unexpected char: '"+ch+"'");
                }
            }
            br.close();
            fr.close();
        }
        catch (FileNotFoundException fnfe) {
            System.err.println("can't find vocab file "+vocabName+".tokens");
        }
        catch (IOException ioe) {
            System.err.println("error reading vocab file "+vocabName+".tokens: "+
                    ioe.toString());
        }
        catch (Exception e) {
            System.err.println("error reading vocab file "+vocabName+".tokens: "+
                    e.toString());
            e.printStackTrace(System.err);
        }
    }

    /** Walk the list of options, altering this Grammar object according
     *  to any I recognize.
    protected void processOptions() {
        Iterator optionNames = options.keySet().iterator();
        while (optionNames.hasNext()) {
            String optionName = (String) optionNames.next();
            Object value = options.get(optionName);
            if ( optionName.equals("tokenVocab") ) {

            }
        }
    }
     */

    public void createNFAs()
        throws antlr.RecognitionException
    {
        System.out.println("building NFAs");
        nfa = new NFA(this); // create NFA that TreeToNFAConverter'll fill in
        NFAFactory factory = new NFAFactory(nfa);
        TreeToNFAConverter nfaBuilder = new TreeToNFAConverter(this, nfa, factory);
        nfaBuilder.grammar(grammarTree);
        System.out.println("NFA has "+factory.getNumberOfStates()+" states");
    }

    /** For each decision in this grammar, compute a single DFA using the
     *  NFA states associated with the decision.  The DFA construction
     *  determines whether or not the alternatives in the decision are
     *  separable using a regular lookahead language.
     *
     *  Store the lookahead DFAs in the AST created from the user's grammar
     *  so the code generator or whoever can easily access it.
     *
     *  This is a separate method because you might want to create a
     *  Grammar without doing the expensive analysis.
     */
    public void createLookaheadDFAs() {
        for (int decision=1; decision<=getNumberOfDecisions(); decision++) {
            NFAState decisionStartState = getDecisionNFAStartState(decision);
            if ( decisionStartState.getNumberOfTransitions()>1 ) {
                System.out.println("--------------------\nbuilding lookahead DFA (d="
                        +decisionStartState.getDecisionNumber()+") for "+
                        decisionStartState.getDescription());
                long start = System.currentTimeMillis();
                DFA lookaheadDFA = new DFA(decisionStartState);
                long stop = System.currentTimeMillis();
                if ( !lookaheadDFA.isReduced() ) {
                    System.err.println("problems with DFA for "+
                            decisionStartState.getDescription());
                }
                System.out.println("DFA (d="+lookaheadDFA.getDecisionNumber()+") cost: "+lookaheadDFA.getNumberOfStates()+
                        " states, "+(int)(stop-start)+" ms; descr="+decisionStartState.getDescription());
                DOTGenerator dotGenerator = new DOTGenerator(nfa.getGrammar());
                String dot = dotGenerator.getDOT( lookaheadDFA.getStartState() );
                try {
                    dotGenerator.writeDOTFile("/tmp/dec-"+decision, dot);
                }
                catch(IOException ioe) {
                    System.err.println("Cannot gen DOT");
                }
                setLookaheadDFA(decision, lookaheadDFA);
                List nonDetAlts = lookaheadDFA.getUnreachableAlts();
                if ( nonDetAlts.size()>0 ) {
                    System.out.println("alts w/o predict state="+nonDetAlts);
                }
            }
        }
    }

    /** Set the lookahead DFA for a particular decision.  This means
     *  that the appropriate AST node must updated to have the new lookahead
     *  DFA.  This method could be used to properly set the DFAs without
     *  using the createLookaheadDFAs() method.  You could do this
     *
     *    Grammar g = new Grammar("...");
     *    g.setLookahead(1, dfa1);
     *    g.setLookahead(2, dfa2);
     *    ...
     */
    public void setLookaheadDFA(int decision, DFA lookaheadDFA) {
        if ( decisionLookaheadDFAList==null ) {
            decisionLookaheadDFAList =
                    new Vector(getNumberOfDecisions());
            decisionLookaheadDFAList.setSize(getNumberOfDecisions());
        }
        NFAState decisionStartState = getDecisionNFAStartState(decision);
        GrammarAST ast = decisionStartState.getDecisionASTNode();
        ast.setLookaheadDFA(lookaheadDFA);
        decisionLookaheadDFAList.set(decision-1, lookaheadDFA);
    }

    public DFA getLookaheadDFA(int decision) {
        return (DFA)decisionLookaheadDFAList.get(decision-1);
    }

    public void parse(String startRule, IntegerStream input)
            throws Exception
    {
        Stack ruleInvocationStack = new Stack();
        NFAState start = getRuleStartState(startRule);
        parseEngine(start, input, ruleInvocationStack);
    }

    protected void parseEngine(NFAState start,
                               IntegerStream input,
                               Stack ruleInvocationStack)
        throws Exception
    {
        NFAState s = start;
        int t = input.LA(1);
        while ( t!=IntegerStream.EOF ) {
            System.out.println("parse state "+s.getStateNumber()+" input="+
                    getTokenName(t));
            // CASE 1: decision state
            if ( s.getDecisionNumber()>0 ) {
                // decision point, must predict and jump to alt
                DFA dfa = getLookaheadDFA(s.getDecisionNumber());
                int m = input.mark();
                int predictedAlt = dfa.predict(input);
                if ( predictedAlt == NFA.INVALID_ALT_NUMBER ) {
                    int position = input.index();
                    throw new Exception("parsing error: no viable alternative at position="+position+
                        " input symbol: "+getTokenName(t));
                }
                input.rewind(m);
                NFAState alt = getNFAStateForAltOfDecision(s, predictedAlt);
                s = (NFAState)alt.transition(0).getTarget();
                continue;
            }

            // CASE 2: finished matching a rule
            if ( s.isAcceptState() ) { // end of rule node
                if ( ruleInvocationStack.empty() ) {
                    return; // done parsing.  Hit the start state.
                }
                // pop invoking state off the stack to know where to return to
                NFAState invokingState = (NFAState)ruleInvocationStack.pop();
                RuleClosureTransition invokingTransition =
                        (RuleClosureTransition)invokingState.transition(0);
                // move to node after state that invoked this rule
                s = invokingTransition.getFollowState();
                continue;
            }

            Transition trans = s.transition(0);
            Label label = trans.getLabel();
            // CASE 3: epsilon transition
            if ( label.isEpsilon() ) {
                // CASE 3a: rule invocation state
                if ( trans instanceof RuleClosureTransition ) {
                    ruleInvocationStack.push(s);
                }
                // CASE 3b: plain old epsilon transition, just move
                s = (NFAState)trans.getTarget();
            }

            // CASE 4: match label on transition
            else if ( label.matches(t) ) {
                s = (NFAState)s.transition(0).getTarget();
                input.consume();
                t = input.LA(1);
            }

            // CASE 5: error condition; label is inconsistent with input
            else {
                int position = input.index();
                throw new Exception("parsing error at position="+position+
                    " input symbol: "+getTokenName(t));
            }
        }
    }

    /** Define either a token at a particular token type value.  Blast an
     *  old value with a new one.  This is called directly during import vocab
     *  operation to set up tokens with specific values.
     */
    public void defineToken(String text, int tokenType) {
        //System.out.println("defining token "+text+" at type="+tokenType);
        // There is a hole between the faux labels (negative numbers)
        // and the first token type.  We skip over the valid 16-bit char values.
        int index = Label.NUM_FAUX_LABELS+(tokenType)-Label.MIN_TOKEN_TYPE;
        if ( text.charAt(0)=='"' ) {
            stringLiteralToTypeMap.put(text, new Integer(tokenType));
        }
        else if ( text.charAt(0)=='\'' ) {
            charLiteralToTypeMap.put(text, new Integer(tokenType));
            index = tokenType; // for char, token type is as sent in
        }
        else { // must be a label like ID
            tokenNameToTypeMap.put(text, new Integer(tokenType));
        }
        typeToTokenList.setSize(index+1);
        typeToTokenList.set(index, text);
    }

    /** Define a new token name, string or char literal token.
     *  A new token type is created by incrementing tokenType.
     *  Do nothing though if we've seen this token before or
     *  if the token is a string and we are in a lexer grammar.
     *  In a lexer, strings are simply matched and do not define
     *  a new token type.  Strings have token types in the lexer
     *  only when they are imported from another grammar such as
     *  a parser.  Do not define char literals as tokens in the
     *  lexer either unless they are imported.
     *
     *  Return the new token type value or Label.INVALID.
     */
    public int defineToken(String text) {
        int ttype = getTokenType(text);
        if ( ttype==Label.INVALID ) {
            //System.out.println("defineToken("+text+")");
            if ( (text.charAt(0)=='"'&&stringLiteralToTypeMap.get(text)==null) ||
                 tokenNameToTypeMap.get(text)==null )
            {
                ttype = this.tokenType;
                defineToken(text, this.tokenType);
                this.tokenType++;
            }
        }
        return ttype;
    }

    /** Define a new rule.  A new rule index is created by incrementing
     *  ruleIndex.
     */
    public int defineRule(String ruleName) {
        //System.out.println("defineRule("+ruleName+"): index="+ruleIndex);
        if ( ruleToIndexMap.get(ruleName)!=null ) {
            // rule redefinition
            System.err.println("redefinition of "+ruleName);
            return INVALID_RULE_INDEX;
        }
        if ( type==PARSER && Character.isUpperCase(ruleName.charAt(0)) ) {
            System.err.println("lexer rules not allowed in parser: "+ruleName);
            return INVALID_RULE_INDEX;
        }
        if ( type==LEXER && Character.isLowerCase(ruleName.charAt(0)) ) {
            System.err.println("parser rules not allowed in lexer: "+ruleName);
            return INVALID_RULE_INDEX;
        }
        if ( type==LEXER && !ruleName.equals(TOKEN_RULENAME)) {
            // rules are also tokens in lexers
            defineToken(ruleName);
        }
        Integer rI = new Integer(ruleIndex);
        ruleToIndexMap.put(ruleName, rI);
        ruleIndexToRuleList.setSize(ruleIndex+1);
        ruleIndexToRuleList.set(ruleIndex, ruleName);
        ruleIndex++;
        return rI.intValue();
    }

    public int getRuleIndex(String ruleName) {
        Integer I = (Integer)ruleToIndexMap.get(ruleName);
        int i = (I!=null)?I.intValue():0;
        return i;
    }

    public String getRuleName(int ruleIndex) {
        return (String)ruleIndexToRuleList.get(ruleIndex);
    }

    public int getTokenType(String tokenName) {
        Integer I = null;
        if ( tokenName.charAt(0)=='"') {
            I = (Integer)stringLiteralToTypeMap.get(tokenName);
        }
        else if ( tokenName.charAt(0)=='\'') {
            I = (Integer)charLiteralToTypeMap.get(tokenName);
        }
        else { // must be a label like ID
            I = (Integer)tokenNameToTypeMap.get(tokenName);
        }
        int i = (I!=null)?I.intValue():Label.INVALID;
        return i;
    }

    public Set getTokenNames() {
        return tokenNameToTypeMap.keySet();
    }

    public Set getStringLiterals() {
        return stringLiteralToTypeMap.keySet();
    }

    /** Return a set of all possible token types for this grammar */
    public IntSet getTokenTypes() {
        return IntervalSet.of(Label.MIN_TOKEN_TYPE,
                              Label.MIN_TOKEN_TYPE+getNumberOfTokenTypes()-1);
    }

    public String getTokenName(int ttype) {
        // inside char range?
        if ( ttype >= Label.MIN_LABEL_VALUE && ttype <= '\uFFFF' ) {
            return getUnicodeEscapeString(ttype);
        }
        // faux label?
        if ( ttype<Label.MIN_LABEL_VALUE ) {
            return (String)typeToTokenList.get(Label.NUM_FAUX_LABELS+ttype);
        }
        int index = ttype-Label.MIN_TOKEN_TYPE; // normalize index to 0..n
        index += Label.NUM_FAUX_LABELS;         // jump over faux tokens

        String tokenName = null;
        if ( index<typeToTokenList.size() ) {
            tokenName = (String)typeToTokenList.get(index);
        }
        else {
            tokenName = String.valueOf(ttype);
        }
        return tokenName;
    }

    public String getTokenTypeAsLabel(int ttype) {
        String name = getTokenName(ttype);
        if ( name.charAt(0)=='"' ) {
            return String.valueOf(ttype);
        }
        if ( ttype==Label.EOF ) {
            return String.valueOf(IntegerStream.EOF);
        }
        return name;
    }

    /** Save the option key/value pair and process it */
    public void setOption(String key, Object value) {
        options.put(key, value);
        if ( key.equals("tokenVocab") ) {
            importTokenVocab((String)value);
        }
    }

    public void setOptions(Map options) {
        Set keys = options.keySet();
        for (Iterator it = keys.iterator(); it.hasNext();) {
            String optionName = (String) it.next();
            Object optionValue = options.get(optionName);
            setOption(optionName, optionValue);
        }
    }

    public Object getOption(String key) {
        Object v = options.get(key);
        if ( v!=null ) {
            return v;
        }
        return defaultOptions.get(key);
    }

    public Set getRules() {
        return ruleToIndexMap.keySet();
    }

    public void mapRuleToTree(String ruleName, GrammarAST t) {
        ruleToTreeMap.put(ruleName, t);
    }

    public void defineRuleStartState(String ruleName, NFAState startState) {
        ruleToStartStateMap.put(ruleName, startState);
    }

    public void defineRuleStopState(String ruleName, NFAState startState) {
        ruleToStopStateMap.put(ruleName, startState);
    }

    public NFAState getRuleStartState(String ruleName) {
        return (NFAState)ruleToStartStateMap.get(ruleName);
    }

    public NFAState getRuleStopState(String ruleName) {
        return (NFAState)ruleToStopStateMap.get(ruleName);
    }

    public Collection getRuleStopStates() {
        return ruleToStopStateMap.values();
    }

    public Collection getRuleStartStates() {
        return ruleToStartStateMap.values();
    }

    public int assignDecisionNumber(NFAState state) {
        decisionNumber++;
        state.setDecisionNumber(decisionNumber);
        return decisionNumber;
    }

    public List getDecisionNFAStartStateList() {
        return decisionNFAStartStateList;
    }

    public NFAState getDecisionNFAStartState(int decision) {
        return (NFAState)decisionNFAStartStateList.get(decision-1);
    }

    public void setDecisionNFA(int decision, NFAState state) {
        decisionNFAStartStateList.setSize(getNumberOfDecisions());
        decisionNFAStartStateList.set(decision-1, state);
    }

    public void setDecisionOptions(int decision, Map options) {
        decisionOptionsList.setSize(getNumberOfDecisions());
        decisionOptionsList.set(decision-1, options);
    }

    public Map getDecisionOptions(int decision) {
        return (Map)decisionOptionsList.get(decision-1);
    }

    public int getNumberOfDecisions() {
        return decisionNumber;
    }

    /** How many token types have been allocated so far? */
    public int getNumberOfTokenTypes() {
        return tokenType-Label.MIN_TOKEN_TYPE;
    }

    /** For lexer grammars, return everything in unicode not in set.
     *  For parser and tree grammars, return everything in token space
     *  from MIN_TOKEN_TYPE to last valid token type.
     */
    public IntSet complement(IntSet set) {
        if ( type == LEXER ) {
            return set.complement(Label.ALLCHAR);
        }
        System.out.println("complement "+set);
        System.out.println("vocabulary "+getTokenTypes());
        IntSet c = set.complement(getTokenTypes());
        System.out.println("c="+c);
        return c;
    }

    public IntSet complement(int atom) {
        return complement(IntervalSet.of(atom));
    }

    /** Decisions are linked together with transition(1).  Count how
     *  many there are.  This is here rather than in NFAState because
     *  a grammar decides how NFAs are put together to form a decision.
     */
    public int getNumberOfAltsForDecisionNFA(NFAState decisionState) {
        if ( decisionState==null ) {
            return 0;
        }
        int n = 1;
        NFAState p = decisionState;
        while ( p.transition(1)!=null ) {
            n++;
            p = (NFAState)p.transition(1).getTarget();
        }
        return n;
    }

    /** Given an alt block NFA, return a list of the alts
     *
     *  o->o-A->o->o
     *  |          ^
     *  o->o-B->o--|
     *  |          |
     *  ...        |
     *  |          |
     *  o->o-Z->o--|
    public List getBlockListOfAltStateClusters(NFAState blk) {
        List alts = new LinkedList();
        NFAState p = blk;
        while ( p!=null ) {
            // look for right end (just before end block)
            NFAState q = (NFAState)p.transition(0).getTarget();
            while ( q.transition(0)!=null ) {
                q = (NFAState)p.transition(0).getTarget();
            }
            alts.add( p.transition(0) );
            if ( p.transition(1)!=null ) {
                p = (NFAState)p.transition(1).getTarget();
            }
            else {
                p = null;
            }
        }
        return alts;
    }
     */

    /** Get the ith alternative (1..n) from a decision; return null when
     *  an invalid alt is requested.  I must count in to find the right
     *  alternative number because the alt num stored in the states is
     *  not what I want for loopback decisions.
     */
    public NFAState getNFAStateForAltOfDecision(NFAState decisionState, int alt) {
        if ( decisionState==null || alt<=0 ) {
            return null;
        }
        int n = 1;
        NFAState p = decisionState;
        while ( p!=null ) {
            if ( n==alt ) {
                return p;
            }
            n++;
            Transition next = p.transition(1);
            p = null;
            if ( next!=null ) {
                p = (NFAState)next.getTarget();
            }
        }
        return null;
    }

    public void setCodeGenerator(CodeGenerator generator) {
        this.generator = generator;
    }

    public CodeGenerator getCodeGenerator() {
        return generator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public NFA getNFA() {
        return nfa;
    }

    public GrammarAST getGrammarTree() {
        return grammarTree;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String toString() {
        return grammarTreeToString(grammarTree);
    }

    public String grammarTreeToString(GrammarAST t) {
        String s = null;
        try {
            s = t.getLine()+":"+t.getColumn()+": ";
            s += new ANTLRTreePrinter().toString((AST)t);
        }
        catch (Exception e) {
            System.err.println("Problems printing tree: "+t);
            e.printStackTrace(System.err);
        }
        return s;
    }

    /** Return a string representing the unicode escape char for c. If c
     *  has value 0x100, you will get '\u0100'.  ASCII gets the usual
     *  char (non-hex) representation.  Control characters are spit out
     *  as unicode.
     *
     *  TODO: It does NOT handle supplemental unicode chars; only <=16 bits
     */
    public static String getUnicodeEscapeString(int c) {
        if ( c<charValueEscape.length && charValueEscape[c]!=null ) {
            return "'"+charValueEscape[c]+"'";
        }
        if ( Character.UnicodeBlock.of((char)c)==Character.UnicodeBlock.BASIC_LATIN &&
             !Character.isISOControl((char)c) ) {
            if ( c=='\\' ) {
                return "'\\\\'";
            }
            if ( c=='\'') {
                return "'\\''";
            }
            return "'"+Character.toString((char)c)+"'";
        }
        // turn on the bit above max '\uFFFF' value so that we pad with zeros
        // then only take last 4 digits
        String hex = Integer.toHexString(c|0x10000).toUpperCase().substring(1,5);
        String unicodeStr = "'\\u"+hex+"'";
        return unicodeStr;
    }

    /** Given a literal like 'a', return the int value of 'a'.
     *  For '\\n', return int 10 ('\n').  Handles 16-bit char only.
     */
    public static int getCharValueFromLiteral(String literal) {
        //System.out.println("getCharValueFromLiteral: "+literal+"; len="+literal.length());
        if ( literal.length()==3 ) {
            // no escape
            return literal.charAt(1);
        }
        /*
        if ( literal.equals("'\\\\'") ) { // if string is '\\', that's just \
            return '\\';
        }
        */
        if ( literal.length()==4 ) {
            int special = escapedCharValue[literal.charAt(2)];
            if ( special>0 ) {
                return special;
            }
            // must be an escape regular char, return x in '\x'.
            return literal.charAt(2);
        }
        return Label.INVALID;
    }
}
