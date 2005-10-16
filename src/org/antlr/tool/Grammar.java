/*
 [The "BSD licence"]
 Copyright (c) 2005 Terence Parr
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
import antlr.TokenStreamRewriteEngine;
import antlr.TreeParser;

import java.io.*;
import java.util.*;

import org.antlr.analysis.*;
import org.antlr.analysis.DFA;
import org.antlr.runtime.*;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.antlr.codegen.CodeGenerator;
import org.antlr.misc.*;
import org.antlr.misc.BitSet;
import org.antlr.Tool;

/** Represents a grammar in memory. */
public class Grammar {
    public static final int INITIAL_DECISION_LIST_SIZE = 300;
	public static final int INVALID_RULE_INDEX = -1;

	public static final int RULE_LABEL = 1;
	public static final int TOKEN_LABEL = 2;
	public static final int RULE_LIST_LABEL = 3;
	public static final int TOKEN_LIST_LABEL = 4;

	public static String[] LabelTypeToString =
		{"<invalid>", "rule", "token", "rule-list", "token-list"};

    public static final String TOKEN_RULENAME = "Tokens";
	public static final String FRAGMENT_RULE_MODIFIER = "fragment";

	/** When converting ANTLR char and string literals, here is the
	 *  value set of escape chars.
	 */
	public static int escapedCharValue[] = new int[255];

	/** Given a char, we need to be able to show as an ANTLR literal.
	 */
	public static String charValueEscape[] = new String[255];

	static {
		escapedCharValue['n'] = '\n';
		escapedCharValue['r'] = '\r';
		escapedCharValue['t'] = '\t';
		escapedCharValue['b'] = '\b';
		escapedCharValue['f'] = '\f';
		escapedCharValue['\\'] = '\\';
		escapedCharValue['\''] = '\'';
		escapedCharValue['"'] = '"';
		charValueEscape['\n'] = "\\n";
		charValueEscape['\r'] = "\\r";
		charValueEscape['\t'] = "\\t";
		charValueEscape['\b'] = "\\b";
		charValueEscape['\f'] = "\\f";
		charValueEscape['\\'] = "\\\\";
		charValueEscape['\''] = "\\'";
	}

    public static final int LEXER = 1;
    public static final int PARSER = 2;
	public static final int TREE_PARSER = 3;
	public static final int COMBINED = 4;
	public static final String[] grammarTypeToString = new String[] {
		"<invalid>",
		"lexer",
		"parser",
		"treeparser",
		"combined"
	};

	public static class Decision {
		public int decision;
		public NFAState startState;
		public GrammarAST blockAST;
		public DFA dfa;
	}

	public class LabelElementPair {
		public antlr.Token label;
		public GrammarAST elementRef;
		public String referencedRuleName;
		public int type; // in {RULE_LABEL,TOKEN_LABEL,RULE_LIST_LABEL,TOKEN_LIST_LABEL}
		public LabelElementPair(antlr.Token label, GrammarAST elementRef) {
			this.label = label;
			this.elementRef = elementRef;
			this.referencedRuleName = elementRef.getText();
		}
		public Rule getReferencedRule() {
			return getRule(referencedRuleName);
		}
		public String toString() {
			return elementRef.toString();
		}
	}

	/** What name did the user provide for this grammar? */
	public String name;

    /** What type of grammar is this: lexer, parser, tree walker */
    public int type;

    /** A list of options specified at the grammar level such as language=Java.
     *  The value can be an AST for complicated values such as character sets.
     *  There may be code generator specific options in here.  I do no
     *  interpretation of the key/value pairs...they are simply available for
     *  who wants them.
     */
    protected Map options;

	public static final Set legalOptions =
			new HashSet() {
				{add("language"); add("tokenVocab");
				 add("output"); add("ASTLabelType");
				 add("superClass");
				}
			};

	public static final Set doNotCopyOptionsToLexer =
		new HashSet() {
			{add("output"); add("ASTLabelType"); add("superClass");}
		};

	public static final Map defaultOptions =
			new HashMap() {
				{put("language","Java");
				}
			};

    /** The NFA that represents the grammar with edges labelled with tokens
     *  or epsilon.  It is more suitable to analysis than an AST representation.
     */
    protected NFA nfa;

    /** Token names and literal tokens like "void" are uniquely indexed.
     *  with -1 implying EOF.  Characters are different; they go from
     *  -1 (EOF) to \uFFFE.  For example, 0 could be a binary byte you
     *  want to lexer.  Labels of DFA/NFA transitions can be both tokens
     *  and characters.  I use negative numbers for bookkeeping labels
     *  like EPSILON. Char/String literals and token types overlap in the same
	 *  space, however.
     */
    protected int maxTokenType = Label.MIN_TOKEN_TYPE-1;

	/** TODO: hook this to the charVocabulary option */
	protected IntSet charVocabulary = null;

    /** Map token like ID (but not literals like "while") to its token type */
    protected Map tokenIDToTypeMap = new HashMap();

    /** Map token literals like "while" to its token type.  It may be that
     *  WHILE="while"=35, in which case both tokenNameToTypeMap and this
     *  field will have entries both mapped to 35.
     */
    protected Map stringLiteralToTypeMap = new HashMap();

    /** Map char literals like 'a' to it's unicode int value as token type. */
    protected Map charLiteralToTypeMap = new HashMap();

    /** Map a token type to its token name.
	 *  Must subtract MIN_TOKEN_TYPE from index.
	 */
    protected Vector typeToTokenList = new Vector();

	/** For interpreting and testing, you sometimes want to import token
	 *  definitions from another grammar (instead of reading token defs from
	 *  a file).
	 */
	protected Grammar importTokenVocabularyFromGrammar;

	/** For ANTLRWorks, we want to be able to map a line:col to a specific
	 *  decision DFA so it can display DFA.
	 */
	Map lineColumnToLookaheadDFAMap = new HashMap();

	protected Tool tool;

	/** The unique set of all rule references in any rule */
	protected Set ruleRefs = new HashSet();

	/** The unique set of all token ID references in any rule */
	protected Set tokenIDRefs = new HashSet();

	/** If combined or lexer grammar, track the rules; Set<String>.
	 * 	Track lexer rules so we can warn about undefined tokens.
 	 */
	protected Set lexerRules = new HashSet();

    /** Be able to assign a number to every decision in grammar;
     *  decisions in 1..n
     */
    protected int decisionNumber = 0;

    /** Rules are uniquely labeled from 1..n */
    protected int ruleIndex = 1;

	/** Map a rule to it's Rule object
	 */
	protected LinkedHashMap nameToRuleMap = new LinkedHashMap();

	/** Track the scopes defined outside of rules and the scopes associated
	 *  with all rules (even if empty).
	 */
	protected Map scopes = new HashMap();

	/** Map a rule index to its name; use a Vector on purpose as new
	 *  collections stuff won't let me setSize and make it grow.  :(
	 *  I need a specific guaranteed index, which the Collections stuff
	 *  won't let me have.
	 */
	protected Vector ruleIndexToRuleList = new Vector();

    /** An AST that records entire input grammar with all rules.  A simple
     *  grammar with one rule, "grammar t; a : A | B ;", looks like:
     * ( grammar t ( rule a ( BLOCK ( ALT A ) ( ALT B ) ) <end-of-rule> ) )
     */
    protected GrammarAST grammarTree = null;

    /** Each subrule/rule is a decision point and we must track them so we
     *  can go back later and build DFA predictors for them.  This includes
     *  all the rules, subrules, optional blocks, ()+, ()* etc...  The
     *  elements in this list are NFAState objects.
     */
	protected Vector indexToDecision = new Vector(INITIAL_DECISION_LIST_SIZE);

    /** If non-null, this is the code generator we will use to generate
     *  recognizers in the target language.
     */
    protected CodeGenerator generator;

	NameSpaceChecker nameSpaceChecker = new NameSpaceChecker(this);

	/** Used during LOOK to detect computation cycles */
	protected Set lookBusy = new HashSet();

	/** For merged lexer/parsers, we must construct a separate lexer spec.
	 *  This is the template for lexer; put the literals first then the
	 *  regular rules.  We don't need to specify a token vocab import as
	 *  I make the new grammar import from the old all in memory; don't want
	 *  to force it to read from the disk.
	 */
	protected StringTemplate lexerGrammarST =
		new StringTemplate(
			"header {<header>}\n" +
			"lexer grammar <name>Lexer;\n" +
			"<if(options)>" +
			"options {\n" +
			"  <options:{<it.name>=<it.value>;<\\n>}>\n" +
			"}<\\n>\n" +
			"<endif>\n" +
			"<literals:{<it.ruleName> : <it.literal> ;\n}>\n" +
			"<rules; separator=\"\n\n\">",
			AngleBracketTemplateLexer.class
		);

	/** What file name holds this grammar? */
	protected String fileName;

	/** How long in ms did it take to build DFAs for this grammar?
	 *  If this grammar is a combined grammar, it only records time for
	 *  the parser grammar component.  This only records the time to
	 *  do the LL(*) work; NFA->DFA conversion.
	 */
	public long DFACreationWallClockTimeInMS;

	public int numberOfSemanticPredicates = 0;
	public int numberOfManualLookaheadOptions = 0;
	public Set setOfNondeterministicDecisionNumbers = new HashSet();
	public Set setOfNondeterministicDecisionNumbersResolvedWithPredicates = new HashSet();
	public int numberOfDFAConversionsTerminatedEarly = 0;

	public Grammar() {
		initTokenSymbolTables();
	}

	public Grammar(String grammarString)
			throws antlr.RecognitionException, antlr.TokenStreamException
	{
		this(null, "<string>",new StringReader(grammarString));
	}

	public Grammar(String fileName, String grammarString)
			throws antlr.RecognitionException, antlr.TokenStreamException
	{
		this(null, fileName, new StringReader(grammarString));
	}

    /** Create a grammar from a Reader.  Parse the grammar, building a tree
     *  and loading a symbol table of sorts here in Grammar.  Then create
     *  an NFA and associated factory.  Walk the AST representing the grammar,
     *  building the state clusters of the NFA.
     */
    public Grammar(Tool tool, String fileName, Reader r)
            throws antlr.RecognitionException, antlr.TokenStreamException
    {
        this();
		setTool(tool);
		setFileName(fileName);
		setGrammarContent(r);
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	/** Return the directory containing the grammar file for this grammar.
	 *  normally this is a relative path from current directory.  People will
	 *  often do "java org.antlr.Tool grammars/*.g"  So the file will be
	 *  "grammar/foo.g" etc...  This method returns "grammar".
	 */
	public String getFileDirectory() {
		File f = new File(fileName);
		String parentDir = f.getParent();
		return parentDir;
	}

	public void setGrammarContent(String grammarString)
		throws antlr.RecognitionException, antlr.TokenStreamException
	{
		setGrammarContent(new StringReader(grammarString));
	}

	public void setGrammarContent(Reader r)
		throws antlr.RecognitionException, antlr.TokenStreamException
	{
		// BUILD AST FROM GRAMMAR
		ANTLRLexer lexer = new ANTLRLexer(r);
		lexer.setFilename(this.getFileName());
		// use the rewrite engine because we want to buffer up all tokens
		// in case they have a merged lexer/parser, send lexer rules to
		// new grammar.
		lexer.setTokenObjectClass("antlr.TokenWithIndex");
		TokenStreamRewriteEngine tokenBuffer =
			new TokenStreamRewriteEngine(lexer);
		tokenBuffer.discard(ANTLRParser.WS);
		tokenBuffer.discard(ANTLRParser.ML_COMMENT);
		tokenBuffer.discard(ANTLRParser.COMMENT);
		tokenBuffer.discard(ANTLRParser.SL_COMMENT);
		ANTLRParser parser = new ANTLRParser(tokenBuffer);
		parser.setFilename(this.getFileName());
		parser.setASTNodeClass("org.antlr.tool.GrammarAST");
		parser.grammar();
		grammarTree = (GrammarAST)parser.getAST();
		if ( Tool.internalOption_PrintGrammarTree ) {
			System.out.println(grammarTree.toStringList());
		}

		/*
		System.out.println("### print grammar");
		ANTLRTreePrinter printer = new ANTLRTreePrinter();
		printer.setASTNodeClass("org.antlr.tool.GrammarAST");
		System.out.println(printer.toString(grammarTree));
		*/

		// ASSIGN TOKEN TYPES
		//System.out.println("### assign types");
		AssignTokenTypesWalker ttypesWalker = new AssignTokenTypesWalker();
		ttypesWalker.setASTNodeClass("org.antlr.tool.GrammarAST");
		try {
			ttypesWalker.grammar(grammarTree, this);
		}
		catch (RecognitionException re) {
			ErrorManager.error(ErrorManager.MSG_BAD_AST_STRUCTURE,
							   re);
		}

		// DEFINE RULES
		//System.out.println("### define rules");
		DefineGrammarItemsWalker defineItemsWalker = new DefineGrammarItemsWalker();
		defineItemsWalker.setASTNodeClass("org.antlr.tool.GrammarAST");
		try {
			defineItemsWalker.grammar(grammarTree, this);
		}
		catch (RecognitionException re) {
			ErrorManager.error(ErrorManager.MSG_BAD_AST_STRUCTURE,
							   re);
		}

		nameSpaceChecker.checkConflicts();
	}

	/** If the grammar is a merged grammar, return the text of the implicit
	 *  lexer grammar.
	 */
	public String getLexerGrammar() {
		if ( lexerGrammarST.getAttribute("literals")==null &&
			 lexerGrammarST.getAttribute("rules")==null )
		{
			// if no rules, return nothing
			return null;
		}
		lexerGrammarST.setAttribute("name", name);
		// make sure generated grammar has the same options
		if ( options!=null ) {
			Iterator optionNames = options.keySet().iterator();
			while (optionNames.hasNext()) {
				String optionName = (String) optionNames.next();
				if ( !doNotCopyOptionsToLexer.contains(optionName) ) {
					Object value = options.get(optionName);
					lexerGrammarST.setAttribute("options.{name,value}", optionName, value);
				}
			}
		}
		return lexerGrammarST.toString();
	}

    /** Parse a rule we add artificially that is a list of the other lexer
     *  rules like this: "Tokens : ID | INT | SEMI ;"  nextToken() will invoke
     *  this to set the current token.  Add char literals before
     *  the rule references.
	 *
	 *  Note: the NFA created for this is specially built by
	 *  NFAFactory.build_ArtificialMatchTokensRuleNFA() not the usual
	 *  mechanism.  So, this creates the rule definition and associated tree,
	 *  but the NFA is created by the NFAFactory.
     */
    public void addArtificialMatchTokensRule() {
        StringTemplate matchTokenRuleST =
            new StringTemplate(
                    TOKEN_RULENAME+" : <rules; separator=\"|\">;",
                    AngleBracketTemplateLexer.class);

        // Now add token rule references
        Collection ruleNames = getRules();
        Iterator iter = ruleNames.iterator();
		int numAlts = 0;
        while (iter.hasNext()) {
            Rule r = (Rule) iter.next();
			// only add real token rules to Tokens rule
			if ( r.modifier==null ||
				 !r.modifier.equals(FRAGMENT_RULE_MODIFIER) )
			{
            	matchTokenRuleST.setAttribute("rules", r.name);
				numAlts++;
			}
        }
		//System.out.println("tokens rule: "+matchTokenRuleST.toString());

        ANTLRLexer lexer = new ANTLRLexer(new StringReader(matchTokenRuleST.toString()));
		lexer.setTokenObjectClass("antlr.TokenWithIndex");
		TokenStreamRewriteEngine tokenBuffer =
			new TokenStreamRewriteEngine(lexer);
		tokenBuffer.discard(ANTLRParser.WS);
		tokenBuffer.discard(ANTLRParser.ML_COMMENT);
		tokenBuffer.discard(ANTLRParser.COMMENT);
		tokenBuffer.discard(ANTLRParser.SL_COMMENT);
        ANTLRParser parser = new ANTLRParser(tokenBuffer);
		parser.gtype = ANTLRParser.LEXER_GRAMMAR;
        parser.setASTNodeClass("org.antlr.tool.GrammarAST");
        try {
            parser.rule();
			if ( Tool.internalOption_PrintGrammarTree ) {
				System.out.println("Tokens rule: "+parser.getAST().toStringTree());
			}
			GrammarAST p = grammarTree;
			while ( p.getType()!=ANTLRParser.LEXER_GRAMMAR ) {
				p = (GrammarAST)p.getNextSibling();
			}
			p.addChild(parser.getAST());
        }
        catch (Exception e) {
            ErrorManager.error(ErrorManager.MSG_ERROR_CREATING_ARTIFICIAL_RULE,e);
        }
		antlr.Token ruleToken = new antlr.TokenWithIndex(ANTLRParser.ID,TOKEN_RULENAME);
		defineRule(ruleToken, null, null, (GrammarAST)parser.getAST(), null, numAlts);
	}

    protected void initTokenSymbolTables() {
        // the faux token types take first NUM_FAUX_LABELS positions
		// then we must have room for the predefined runtime token types
		// like DOWN/UP used for tree parsing.
        typeToTokenList.setSize(Label.NUM_FAUX_LABELS+Label.MIN_TOKEN_TYPE-1);
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.INVALID, "<INVALID>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EOT, "<EOT>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.SEMPRED, "<SEMPRED>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.SET, "<SET>");
        typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EPSILON, Label.EPSILON_STR);
		typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EOF, "<EOF>");
		typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.EOR_TOKEN_TYPE-1, "Token.EOR_TOKEN_TYPE");
		typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.DOWN-1, "Token.DOWN");
		typeToTokenList.set(Label.NUM_FAUX_LABELS+Label.UP-1, "Token.UP");
        tokenIDToTypeMap.put("<INVALID>", new Integer(Label.INVALID));
        tokenIDToTypeMap.put("<EOT>", new Integer(Label.EOT));
        tokenIDToTypeMap.put("<SEMPRED>", new Integer(Label.SEMPRED));
        tokenIDToTypeMap.put("<SET>", new Integer(Label.SET));
        tokenIDToTypeMap.put("<EPSILON>", new Integer(Label.EPSILON));
		tokenIDToTypeMap.put("<EOF>", new Integer(Label.EOF));
		tokenIDToTypeMap.put("EOR", new Integer(Label.EOR_TOKEN_TYPE));
		tokenIDToTypeMap.put("DOWN", new Integer(Label.DOWN));
		tokenIDToTypeMap.put("UP", new Integer(Label.UP));
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

    public void createNFAs() {
		//System.out.println("### create NFAs");
		nfa = new NFA(this); // create NFA that TreeToNFAConverter'll fill in
		NFAFactory factory = new NFAFactory(nfa);
		TreeToNFAConverter nfaBuilder = new TreeToNFAConverter(this, nfa, factory);
		try {
			nfaBuilder.grammar(grammarTree);
		}
		catch (RecognitionException re) {
			ErrorManager.error(ErrorManager.MSG_BAD_AST_STRUCTURE,
							   name,
							   re);
		}
		//System.out.println("NFA has "+factory.getNumberOfStates()+" states");
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
		//System.out.println("### create DFAs");
		long start = System.currentTimeMillis();
        for (int decision=1; decision<=getNumberOfDecisions(); decision++) {
            NFAState decisionStartState = getDecisionNFAStartState(decision);
            if ( decisionStartState.getNumberOfTransitions()>1 ) {
				/*
				System.out.println("--------------------\nbuilding lookahead DFA (d="
                        +decisionStartState.getDecisionNumber()+") for "+
                        decisionStartState.getDescription());
				long start = System.currentTimeMillis();
				*/
				DFA lookaheadDFA = new DFA(decision, decisionStartState);
				if ( lookaheadDFA.analysisAborted() ) { // did analysis bug out?
					// set k=1 option and try again
					Decision d = getDecision(decision);
					d.blockAST.setOption(this, "k", new Integer(1));
					//setDecisionOption(decision, "k", new Integer(1));
					lookaheadDFA = new DFA(decision, decisionStartState);
				}

				setLookaheadDFA(decision, lookaheadDFA);

				// create map from line:col to decision DFA (for ANTLRWorks)
				GrammarAST decisionAST = lookaheadDFA.getDecisionASTNode();
				int line = decisionAST.getLine();
				int col = decisionAST.getColumn();
				lineColumnToLookaheadDFAMap.put(new StringBuffer().append(line + ":")
											.append(col).toString(), lookaheadDFA);

				/*
				long stop = System.currentTimeMillis();
				System.out.println("cost: "+lookaheadDFA.getNumberOfStates()+
					" states, "+(int)(stop-start)+" ms");
				*/
			}
		}
		long stop = System.currentTimeMillis();
		DFACreationWallClockTimeInMS = stop - start;
	}

	/** Define a token at a particular token type value.  Blast an
	 *  old value with a new one.  This is called directly during import vocab
     *  operation to set up tokens with specific values.
     */
    public void defineToken(String text, int tokenType) {
		// the index in the typeToTokenList table is actually shifted to
		// hold faux labels as you cannot have negative indices.
        if ( text.charAt(0)=='"' ) {
            stringLiteralToTypeMap.put(text, new Integer(tokenType));
        }
        else if ( text.charAt(0)=='\'' ) {
            charLiteralToTypeMap.put(text, new Integer(tokenType));
        }
        else { // must be a label like ID
            tokenIDToTypeMap.put(text, new Integer(tokenType));
        }
		int index = Label.NUM_FAUX_LABELS+tokenType-1;
		//System.out.println("defining token "+text+" at type="+tokenType+", index="+index);
		this.maxTokenType = Math.max(this.maxTokenType, tokenType);
        if ( index>=typeToTokenList.size() ) {
			typeToTokenList.setSize(index+1);
		}
        typeToTokenList.set(index, text);
    }

	/** Define a new rule.  A new rule index is created by incrementing
     *  ruleIndex.
     */
	public void defineRule(antlr.Token ruleToken,
						   String modifier,
						   Map options,
						   GrammarAST tree,
						   GrammarAST argActionAST,
						   int numAlts)
	{
		String ruleName = ruleToken.getText();
		/*
		System.out.println("defineRule("+ruleName+",modifier="+modifier+
						   "): index="+ruleIndex);
		*/
		if ( getRule(ruleName)!=null ) {
			ErrorManager.grammarError(ErrorManager.MSG_RULE_REDEFINITION,
									  this, ruleToken, ruleName);
        }

		Rule r = new Rule(this, ruleName, ruleIndex, numAlts);
		r.modifier = modifier;
        nameToRuleMap.put(ruleName, r);
		setRuleAST(ruleName, tree);
		r.setOptions(options, ruleToken);
		r.argActionAST = argActionAST;
        ruleIndexToRuleList.setSize(ruleIndex+1);
        ruleIndexToRuleList.set(ruleIndex, ruleName);
        ruleIndex++;
	}

	public void defineGrammarHeader(GrammarAST nameAST, GrammarAST actionAST) {
		// ignore name of header space for now
		lexerGrammarST.setAttribute("header", actionAST.getText());
	}

	public void defineLexerRuleFoundInParser(antlr.Token ruleToken, GrammarAST ruleAST) {
		//System.out.println("rule tree is:\n"+ruleAST.toStringTree());
		ANTLRTreePrinter printer = new ANTLRTreePrinter();
		printer.setASTNodeClass("org.antlr.tool.GrammarAST");
		try {
			String ruleText = printer.toString(ruleAST, this, true);
			//System.out.println("ruleText: "+ruleText);
			lexerGrammarST.setAttribute("rules", ruleText);
			lexerRules.add(ruleToken.getText());
		}
		catch (antlr.RecognitionException re) {
			ErrorManager.internalError("defineLexerRuleFoundInParser: cannot convert "+
									   "rule "+ruleToken.getText()+"'s AST to text");
		}
	}

	public void defineLexerRuleForStringLiteral(String literal, int tokenType) {
		lexerGrammarST.setAttribute("literals.{ruleName,type,literal}",
									computeTokenNameFromLiteral(tokenType,literal),
									new Integer(tokenType),
									literal);
	}

	public void defineLexerRuleForCharLiteral(String literal, int tokenType) {
		lexerGrammarST.setAttribute("literals.{ruleName,type,literal}",
									computeTokenNameFromLiteral(tokenType,literal),
									new Integer(tokenType),
									literal);
	}

	public Rule getRule(String ruleName) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		return r;
	}

    public int getRuleIndex(String ruleName) {
		Rule r = getRule(ruleName);
		if ( r!=null ) {
			return r.index;
		}
        return INVALID_RULE_INDEX;
    }

    public String getRuleName(int ruleIndex) {
        return (String)ruleIndexToRuleList.get(ruleIndex);
    }

	public AttributeScope defineGlobalScope(String name) {
		AttributeScope scope = new AttributeScope(name);
		scopes.put(name,scope);
		return scope;
	}

	public AttributeScope createReturnScope(String ruleName) {
		AttributeScope scope = new AttributeScope(ruleName+"_return");
		scope.isReturnScope = true;
		return scope;
	}

	public AttributeScope createRuleScope(String ruleName) {
		AttributeScope scope = new AttributeScope(ruleName);
		scope.isDynamicRuleScope = true;
		return scope;
	}

	public AttributeScope createParameterScope(String ruleName) {
		AttributeScope scope = new AttributeScope(ruleName+"_parameter");
		scope.isParameterScope = true;
		return scope;
	}

	/** Get a global scope */
	public AttributeScope getGlobalScope(String name) {
		return (AttributeScope)scopes.get(name);
	}

	public Map getGlobalScopes() {
		return scopes;
	}

	/** Define a label defined in a rule r; check the validity then ask the
	 *  Rule object to actually define it.
	 */
	protected void defineLabel(Rule r, antlr.Token label, GrammarAST element, int type) {
        boolean err = nameSpaceChecker.checkForLabelTypeMismatch(r, label, type);
		if ( err ) {
			return;
		}
		r.defineLabel(label, element, type);
	}

	public void defineTokenRefLabel(String ruleName,
									antlr.Token label,
									GrammarAST tokenRef)
	{
        Rule r = getRule(ruleName);
		if ( r!=null ) {
			defineLabel(r, label, tokenRef, TOKEN_LABEL);
		}
	}

	public void defineRuleRefLabel(String ruleName,
								   antlr.Token label,
								   GrammarAST ruleRef)
	{
		Rule r = getRule(ruleName);
		if ( r!=null ) {
			defineLabel(r, label, ruleRef, RULE_LABEL);
		}
	}

	public void defineTokenListLabel(String ruleName,
									 antlr.Token label,
									 GrammarAST element)
	{
		Rule r = getRule(ruleName);
		if ( r!=null ) {
			defineLabel(r, label, element, TOKEN_LIST_LABEL);
		}
	}

	public void defineRuleListLabel(String ruleName,
									antlr.Token label,
									GrammarAST element)
	{
		if ( !buildAST() ) {
			ErrorManager.grammarError(
				ErrorManager.MSG_LIST_LABEL_INVALID_UNLESS_AST,this,label,label.getText());
		}
		Rule r = getRule(ruleName);
		if ( r!=null ) {
			defineLabel(r, label, element, RULE_LIST_LABEL);
		}
	}

	/** Track a rule reference within an outermost alt of a rule.  Used
	 *  at the moment to decide if $ruleref refers to a unique rule ref in
	 *  the alt.  Rewrite rules force tracking of all rule AST results.
	 */
	public void altReferencesRule(String ruleName, GrammarAST refAST, int outerAltNum) {
		Rule r = getRule(ruleName);
		if ( r==null ) {
			return;
		}
		r.trackRuleReferenceInAlt(refAST, outerAltNum);
		antlr.Token refToken = refAST.getToken();
		if ( !ruleRefs.contains(refToken) ) {
			ruleRefs.add(refToken);
		}
	}

	/** Track a token reference within an outermost alt of a rule.  Used
	 *  to decide if $tokenref refers to a unique token ref in
	 *  the alt. Does not track literals!
	 *
	 *  Rewrite rules force tracking of all tokens.
	 */
	public void altReferencesTokenID(String ruleName, GrammarAST refAST, int outerAltNum) {
		Rule r = getRule(ruleName);
		if ( r==null ) {
			return;
		}
		r.trackTokenReferenceInAlt(refAST, outerAltNum);
		if ( !tokenIDRefs.contains(refAST.getToken()) ) {
			tokenIDRefs.add(refAST.getToken());
		}
	}

	/** To yield smaller, more readable code, track which rules have their
	 *  predefined attributes accessed.  If the rule has no user-defined
	 *  return values, then don't generate the return value scope classes
	 *  etc...  Make the rule have void return value.
	 */
	public void referenceRuleLabelPredefinedAttribute(String ruleName) {
		Rule r = getRule(ruleName);
		if ( r!=null ) {
			r.referencedPredefinedRuleAttributes = true;
		}
	}

	public void checkRuleReference(GrammarAST refAST,
								   GrammarAST argsAST,
								   String currentRuleName)
	{
		Rule r = getRule(refAST.getText());
		if ( refAST.getType()==ANTLRParser.RULE_REF ) {
			if ( argsAST!=null ) {
				// rule[args]; ref has args
                if ( r!=null && r.argActionAST==null ) {
					// but rule def has no args
					ErrorManager.grammarError(
						ErrorManager.MSG_RULE_HAS_NO_ARGS,
						this,
						argsAST.getToken(),
						r.name);
				}
			}
			else {
				// rule ref has no args
				if ( r!=null && r.argActionAST!=null ) {
					// but rule def has args
					ErrorManager.grammarError(
						ErrorManager.MSG_MISSING_RULE_ARGS,
						this,
						refAST.getToken(),
						r.name);
				}
			}
		}
		else if ( refAST.getType()==ANTLRParser.TOKEN_REF ) {
			if ( type!=LEXER ) {
				if ( argsAST!=null ) {
					// args on a token ref not in a lexer rule
					ErrorManager.grammarError(
						ErrorManager.MSG_ARGS_ON_TOKEN_REF,
						this,
						refAST.getToken(),
						refAST.getText());
				}
				return; // ignore token refs in nonlexers
			}
			if ( argsAST!=null ) {
				// tokenRef[args]; ref has args
				if ( r!=null && r.argActionAST==null ) {
					// but token rule def has no args
					ErrorManager.grammarError(
						ErrorManager.MSG_RULE_HAS_NO_ARGS,
						this,
						argsAST.getToken(),
						r.name);
				}
			}
			else {
				// token ref has no args
				if ( r!=null && r.argActionAST!=null ) {
					// but token rule def has args
					ErrorManager.grammarError(
						ErrorManager.MSG_MISSING_RULE_ARGS,
						this,
						refAST.getToken(),
						r.name);
				}
			}
		}
	}

	/** Rules like "a : ;" and "a : {...} ;" should not generate
	 *  try/catch blocks for RecognitionException.  To detect this
	 *  it's probably ok to just look for any reference to an atom
	 *  that can match some input.  W/o that, the rule is unlikey to have
	 *  any else.
	 */
	public boolean isEmptyRule(GrammarAST block) {
		GrammarAST aTokenRefNode =
			block.findFirstType(ANTLRParser.TOKEN_REF);
		GrammarAST aStringLiteralRefNode =
			block.findFirstType(ANTLRParser.STRING_LITERAL);
		GrammarAST aCharLiteralRefNode =
			block.findFirstType(ANTLRParser.CHAR_LITERAL);
		GrammarAST aWildcardRefNode =
			block.findFirstType(ANTLRParser.WILDCARD);
		GrammarAST aRuleRefNode =
			block.findFirstType(ANTLRParser.RULE_REF);
		if ( aTokenRefNode==null&&
			aStringLiteralRefNode==null&&
			aCharLiteralRefNode==null&&
			aWildcardRefNode==null&&
			aRuleRefNode==null )
		{
			return true;
		}
		return false;
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
            I = (Integer)tokenIDToTypeMap.get(tokenName);
        }
        int i = (I!=null)?I.intValue():Label.INVALID;
		//System.out.println("grammar type "+getType()+" "+tokenName+"->"+i);
        return i;
    }

	/** Get the list of tokens that are IDs like BLOCK and LPAREN */
	public Set getTokenIDs() {
		return tokenIDToTypeMap.keySet();
	}

	/** Get a list of all token IDs and literals that have an associated
	 *  token type.
	 */
	public Set getTokenDisplayNames() {
		Set names = new HashSet();
		for (int type=Label.MIN_TOKEN_TYPE; type<=getMaxTokenType(); type++) {
			names.add(getTokenDisplayName(type));
		}
		return names;
	}

	public Set getStringLiteralTokens() {
		return stringLiteralToTypeMap.keySet();
	}

	public Set getCharLiteralTokens() {
		return charLiteralToTypeMap.keySet();
	}

	/** Convert a char literal as read by antlr.g back to a grammar literal
	 *  that has escaped chars instead of the real char value.  If a
	 *  target is known, use its escape facility instead of the default Java
	 *  converter.
	public String getANTLREscapedCharLiteral(String literal) {
		if ( generator!=null ) {
			generator.target.getEscapedCharLiteralToken(literal);
		}
		return CodeGenerator.getJavaEscapedCharFromCharLiteralToken(literal);
	}
	 */

	/** Convert a string literal as read by antlr.g back to a grammar literal
	 *  that has escaped chars instead of the real char values.  If a
	 *  target is known, use its escape facility instead of the default Java
	 *  converter.
	public String getANTLREscapedStringLiteral(String literal) {
		if ( generator!=null ) {
			generator.target.getEscapedStringLiteralToken(literal);
		}
		return CodeGenerator.getJavaEscapedStringFromStringLiteralToken(literal);
	}
	 */

	/** Given a literal like (the 3 char sequence with single quotes) 'a',
	 *  return the int value of 'a'. Convert escape sequences here also.
	 *  ANTLR's antlr.g parser does not convert escape sequences.
     */
    public static int getCharValueFromGrammarCharLiteral(String literal) {
        if ( literal.length()==3 ) {
			// 'x'
            return literal.charAt(1); // no escape char
        }
		ErrorManager.assertTrue(literal.length()==4, "invalid char literals: "+literal);
		// '\x'  (antlr lexer will catch invalid char)
		int escChar = literal.charAt(2);
		int charVal = escapedCharValue[escChar];
		return charVal;
    }

	/** ANTLR does not convert escape sequences during the parse phase because
	 *  it could not know how to print String/char literals back out when
	 *  printing grammars etc...  Someone in China might use the real unicode
	 *  char in a literal as it will display on their screen; when printing
	 *  back out, I could not know whether to display or use a unicode escape.
	 *
	 *  This routine converts a string literal with possible escape sequences
	 *  into a pure string of 16-bit char values.  Escapes and unicode \u0000
	 *  specs are converted to pure chars.  return in a buffer; people may
	 *  want to walk/manipulate further.
	 *
	 *  The NFA construction routine must know the actual char values.
	 */
	public static StringBuffer getUnescapedStringFromGrammarStringLiteral(String literal) {
		StringBuffer buf = new StringBuffer();
		int last = literal.length()-1; // skip quotes on outside
		for (int i=1; i<last; i++) {
			char c = literal.charAt(i);
			if ( c=='\'' ) {
				i++;
				c = literal.charAt(i);
				if ( Character.toUpperCase(c)=='U' ) {
					// \u0000
					i++;
					String unicodeChars = literal.substring(i,i+4);
					// parse the unicode 16 bit hex value
					int val = Integer.parseInt(unicodeChars, 16);
					i+=4-1; // loop will inc by 1; only jump 3 then
					buf.append((char)val);
				}
				else {
					buf.append(escapedCharValue[c]); // normal \x escape
				}
			}
			else {
				buf.append(c); // simple char x
			}
		}
		return buf;
	}

	public void importTokenVocabulary(Grammar g) {
		importTokenVocabularyFromGrammar = g;
	}

    public Grammar getGrammarWithTokenVocabularyToImport() {
		return importTokenVocabularyFromGrammar;
	}

	/** Given a token type, get a meaningful name for it such as the ID
	 *  or string literal.  If this is a lexer and the ttype is in the
	 *  char vocabulary, compute an ANTLR-valid (possibly escaped) char literal.
	 */
	public String getTokenDisplayName(int ttype) {
		String tokenName = null;
		int index=0;
		// inside any target's char range and is lexer grammar?
		if ( this.type==LEXER &&
			 ttype >= Label.MIN_CHAR_VALUE && ttype <= Label.MAX_CHAR_VALUE )
		{
			return getANTLRCharLiteralForChar(ttype);
		}
		// faux label?
		else if ( ttype<0 ) {
			tokenName = (String)typeToTokenList.get(Label.NUM_FAUX_LABELS+ttype);
		}
		else {
			// compute index in typeToTokenList for ttype
			index = ttype-1; // normalize to 0..n-1
			index += Label.NUM_FAUX_LABELS;     // jump over faux tokens

			if ( index<typeToTokenList.size() ) {
				tokenName = (String)typeToTokenList.get(index);
			}
			else {
				tokenName = String.valueOf(ttype);
			}
		}
		ErrorManager.assertTrue(tokenName!=null,
								"null token name; ttype="+ttype+" tokens="+typeToTokenList);
		//System.out.println("getTokenDisplaYanme ttype="+ttype+", index="+index+", name="+tokenName);
		return tokenName;
	}

    /** Save the option key/value pair and process it; return the key
	 *  or null if invalid option.
	 */
    public String setOption(String key, Object value, antlr.Token optionsStartToken) {
		if ( !legalOptions.contains(key) ) {
			ErrorManager.grammarError(ErrorManager.MSG_ILLEGAL_OPTION,
									  this,
									  optionsStartToken,
									  key);
			return null;
		}
		if ( !optionIsValid(key, value) ) {
			return null;
		}
		if ( options==null ) {
			options = new HashMap();
		}
 		options.put(key, value);
		return key;
    }

    public void setOptions(Map options, antlr.Token optionsStartToken) {
		if ( options==null ) {
			this.options = null;
			return;
		}
        Set keys = options.keySet();
        for (Iterator it = keys.iterator(); it.hasNext();) {
            String optionName = (String) it.next();
            Object optionValue = options.get(optionName);
            String stored=setOption(optionName, optionValue, optionsStartToken);
			if ( stored==null ) {
				it.remove();
			}
        }
    }

    public Object getOption(String key) {
		Object value = null;
		if ( options!=null ) {
			value = options.get(key);
		}
		if ( value==null ) {
			value = defaultOptions.get(key);
		}
		return value;
    }

	public boolean optionIsValid(String key, Object value) {
		if ( key.equals("output") ) {
			if ( type==TREE_PARSER ) {
				ErrorManager.grammarError(ErrorManager.MSG_ILLEGAL_OPTION,
										  this,
										  null,
										  key);
				return false;
			}
		}
		return true;
	}

	public boolean buildAST() {
		String outputType = (String)getOption("output");
		if ( outputType!=null ) {
			return outputType.equals("AST");
		}
		return false;
	}

	public boolean buildTemplate() {
		String outputType = (String)getOption("output");
		if ( outputType!=null ) {
			return outputType.equals("text");
		}
		return false;
	}

    public Collection getRules() {
        return nameToRuleMap.values();
    }

	public void setRuleAST(String ruleName, GrammarAST t) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		if ( r!=null ) {
			r.tree = t;
			r.EORNode = t.getLastChild();
		}
	}

    public void setRuleStartState(String ruleName, NFAState startState) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		if ( r!=null ) {
	        r.startState = startState;
		}
    }

    public void setRuleStopState(String ruleName, NFAState stopState) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		if ( r!=null ) {
	        r.stopState = stopState;
		}
    }

	public NFAState getRuleStartState(String ruleName) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		if ( r!=null ) {
			return r.startState;
		}
		return null;
	}

	public String getRuleModifier(String ruleName) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		if ( r!=null ) {
			return r.modifier;
		}
		return null;
	}

    public NFAState getRuleStopState(String ruleName) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		if ( r!=null ) {
			return r.stopState;
		}
		return null;
    }

    public int assignDecisionNumber(NFAState state) {
        decisionNumber++;
        state.setDecisionNumber(decisionNumber);
        return decisionNumber;
    }

	protected Decision getDecision(int decision) {
		int index = decision-1;
		if ( index >= indexToDecision.size() ) {
			return null;
		}
		Decision d = (Decision)indexToDecision.get(index);
		return d;
	}

	protected Decision createDecision(int decision) {
		int index = decision-1;
		if ( index < indexToDecision.size() ) {
			return getDecision(decision); // don't recreate
		}
		Decision d = new Decision();
		d.decision = decision;
        indexToDecision.setSize(getNumberOfDecisions());
        indexToDecision.set(index, d);
		return d;
	}

    public List getDecisionNFAStartStateList() {
		List states = new ArrayList(100);
		for (int d = 0; d < indexToDecision.size(); d++) {
			Decision dec = (Decision) indexToDecision.elementAt(d);
			states.add(dec.startState);
		}
        return states;
    }

    public NFAState getDecisionNFAStartState(int decision) {
        Decision d = getDecision(decision);
		if ( d==null ) {
			return null;
		}
		return d.startState;
    }

	public DFA getLookaheadDFA(int decision) {
		Decision d = getDecision(decision);
		if ( d==null ) {
			return null;
		}
		return d.dfa;
	}

	public GrammarAST getDecisionBlockAST(int decision) {
		Decision d = getDecision(decision);
		if ( d==null ) {
			return null;
		}
		return d.blockAST;
	}

	/** returns a list of column numbers for all decisions
	 *  on a particular line so ANTLRWorks choose the decision
	 *  depending on the location of the cursor (otherwise,
	 *  ANTLRWorks has to give the *exact* location which
	 *  is not easy from the user point of view).
	 *
	 *  This is not particularly fast as it walks entire line:col->DFA map
	 *  looking for a prefix of "line:".
	 */
	public List getLookaheadDFAColumnsForLineInFile(int line) {
		String prefix = line+":";
		List columns = new ArrayList();
		for(Iterator iter = lineColumnToLookaheadDFAMap.keySet().iterator();
			iter.hasNext(); ) {
			String key = (String)iter.next();
			if(key.startsWith(prefix)) {
				columns.add(Integer.valueOf(key.substring(prefix.length())));
			}
		}
		return columns;
	}

	/** Useful for ANTLRWorks to map position in file to the DFA for display */
	public DFA getLookaheadDFAFromPositionInFile(int line, int col) {
		return (DFA)lineColumnToLookaheadDFAMap.get(
			new StringBuffer().append(line + ":").append(col).toString());
	}

	/*
	public void setDecisionOptions(int decision, Map options) {
		Decision d = createDecision(decision);
		d.options = options;
	}

	public void setDecisionOption(int decision, String name, Object value) {
		Decision d = getDecision(decision);
		if ( d!=null ) {
			if ( d.options==null ) {
				d.options = new HashMap();
			}
			d.options.put(name,value);
		}
	}

	public Map getDecisionOptions(int decision) {
		Decision d = getDecision(decision);
		if ( d==null ) {
			return null;
		}
		return d.options;
    }
    */

	public int getNumberOfDecisions() {
		return decisionNumber;
	}

	public int getNumberOfCyclicDecisions() {
		int n = 0;
		for (int i=1; i<=getNumberOfDecisions(); i++) {
			Decision d = getDecision(i);
			if ( d.dfa.isCyclic() ) {
				n++;
			}
		}
		return n;
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
		Decision d = createDecision(decision);
		d.dfa = lookaheadDFA;
		GrammarAST ast = d.startState.getDecisionASTNode();
		ast.setLookaheadDFA(lookaheadDFA);
	}

	public void setDecisionNFA(int decision, NFAState state) {
		Decision d = createDecision(decision);
		d.startState = state;
	}

	public void setDecisionBlockAST(int decision, GrammarAST blockAST) {
		Decision d = createDecision(decision);
		d.blockAST = blockAST;
	}

    /** How many token types have been allocated so far? */
    public int getMaxTokenType() {
        return maxTokenType;
    }

	/** What is the max char value possible for this grammar's target?  Use
	 *  unicode max if no target defined.
	 */
	public int getMaxCharValue() {
		if ( generator!=null ) {
			return generator.target.getMaxCharValue();
		}
		else {
			return Label.MAX_CHAR_VALUE;
		}
	}

	/** Return a set of all possible token or char types for this grammar */
	public IntSet getTokenTypes() {
		if ( type==LEXER ) {
			return getAllCharValues();
		}
		return IntervalSet.of(Label.MIN_TOKEN_TYPE, getMaxTokenType());
	}

	/** If there is a char vocabulary, use it; else return min to max char
	 *  as defined by the target.  If no target, use max unicode char value.
	 */
	public IntSet getAllCharValues() {
		if ( charVocabulary!=null ) {
			return charVocabulary;
		}
		IntSet allChar = IntervalSet.of(Label.MIN_CHAR_VALUE, getMaxCharValue());
		return allChar;
	}

	/** Return a string representing the escaped char for code c.  E.g., If c
	 *  has value 0x100, you will get "\u0100".  ASCII gets the usual
	 *  char (non-hex) representation.  Control characters are spit out
	 *  as unicode.  While this is specially set up for returning Java strings,
	 *  it can be used by any language target that has the same syntax. :)
	 *  Note that this method does NOT put the quotes around it so that the
	 *  method is more reusable.
	 */
	public static String getANTLRCharLiteralForChar(int c) {
		if ( c<Label.MIN_CHAR_VALUE ) {
			ErrorManager.internalError("invalid char value "+c);
			return "'<INVALID>'";
		}
		if ( c<charValueEscape.length && charValueEscape[c]!=null ) {
			return '\''+charValueEscape[c]+'\'';
		}
		if ( Character.UnicodeBlock.of((char)c)==Character.UnicodeBlock.BASIC_LATIN &&
			!Character.isISOControl((char)c) ) {
			if ( c=='\\' ) {
				return "'\\\\'";
			}
			if ( c=='\'') {
				return "'\\''";
			}
			return '\''+Character.toString((char)c)+'\'';
		}
		// turn on the bit above max '\uFFFF' value so that we pad with zeros
		// then only take last 4 digits
		String hex = Integer.toHexString(c|0x10000).toUpperCase().substring(1,5);
		String unicodeStr = "'\\u"+hex+"'";
		return unicodeStr;
	}

    /** For lexer grammars, return everything in unicode not in set.
     *  For parser and tree grammars, return everything in token space
     *  from MIN_TOKEN_TYPE to last valid token type or char value.
     */
    public IntSet complement(IntSet set) {
        //System.out.println("complement "+set.toString(this));
        //System.out.println("vocabulary "+getTokenTypes().toString(this));
        IntSet c = set.complement(getTokenTypes());
        //System.out.println("result="+c.toString(this));
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
            p = (NFAState)p.transition(1).target;
        }
        return n;
    }

    /** Get the ith alternative (1..n) from a decision; return null when
     *  an invalid alt is requested.  I must count in to find the right
     *  alternative number.  For (A|B), you get NFA structure (roughly):
	 *
	 *  o->o-A->o
	 *  |
	 *  o->o-B->o
	 *
	 *  This routine returns the leftmost state for each alt.  So alt=1, returns
	 *  the upperleft most state in this structure.
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
                p = (NFAState)next.target;
            }
        }
        return null;
    }

	/** From an NFA state, s, find the set of all labels reachable from s.
	 *  This computes FIRST, FOLLOW and any other lookahead computation
	 *  depending on where s is.
	 *
	 *  Record, with EOR_TOKEN_TYPE, if you hit the end of a rule so we can
	 *  know at runtime (when these sets are used) to start walking up the
	 *  follow chain to compute the real, correct follow set.
	 *
	 *  This routine will only be used on parser and tree parser grammars.
	 */
	public LookaheadSet LOOK(NFAState s) {
		lookBusy.clear();
		return _LOOK(s);
	}

	protected LookaheadSet _LOOK(NFAState s) {
		if ( s.isAcceptState() ) {
			return new LookaheadSet(Label.EOR_TOKEN_TYPE);
		}

		if ( lookBusy.contains(s) ) {
			// return a copy of an empty set; we may modify set inline
			return new LookaheadSet();
		}
		lookBusy.add(s);
		Transition transition0 = s.transition(0);
		if ( transition0==null ) {
			return null;
		}

		if ( transition0.label.isAtom() ) {
			int atom = transition0.label.getAtom();
			if ( atom==Label.EOF ) {
				return LookaheadSet.EOF();
			}
			return new LookaheadSet(atom);
		}
		if ( transition0.label.isSet() ) {
			IntSet sl = transition0.label.getSet();
			return new LookaheadSet(sl);
		}
        LookaheadSet tset = _LOOK((NFAState)transition0.target);

		Transition transition1 = s.transition(1);
		if ( transition1!=null ) {
			LookaheadSet tset1 = _LOOK((NFAState)transition1.target);
			tset.orInPlace(tset1);
		}
		return tset;
	}

    public void setCodeGenerator(CodeGenerator generator) {
        this.generator = generator;
    }

    public CodeGenerator getCodeGenerator() {
        return generator;
    }

    public GrammarAST getGrammarTree() {
        return grammarTree;
    }

	public Tool getTool() {
		return tool;
	}

	public void setTool(Tool tool) {
		this.tool = tool;
	}

	/** given a token type and the text of the literal, come up with a
	 *  decent token type label.  For now it's just T<type>.
	 */
	public String computeTokenNameFromLiteral(int tokenType, String literal) {
		return "T"+tokenType;
	}

    public String toString() {
        return grammarTreeToString(grammarTree);
    }

	public String grammarTreeToString(GrammarAST t) {
		return grammarTreeToString(t, true);
	}

	public String grammarTreeToString(GrammarAST t, boolean showActions) {
        String s = null;
        try {
            s = t.getLine()+":"+t.getColumn()+": ";
            s += new ANTLRTreePrinter().toString((AST)t, this, showActions);
        }
        catch (Exception e) {
            ErrorManager.error(ErrorManager.MSG_BAD_AST_STRUCTURE,
							   t,
							   e);
        }
        return s;
    }

}
