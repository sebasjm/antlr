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

    public static final String TOKEN_RULENAME = "Tokens";
	public static final String FRAGMENT_RULE_MODIFIER = "fragment";

    public static final int LEXER = 1;
    public static final int PARSER = 2;
	public static final int TREE_PARSER = 3;
	public static final int COMBINED = 4;

	/** Combine the info associated with a rule; I'm using like a struct */
	public static class Rule {
		public static final Set predefinedRuleProperties = new HashSet();
		static {
			predefinedRuleProperties.add("start");
			predefinedRuleProperties.add("stop");
			predefinedRuleProperties.add("tree");
		}
		public String name;
		public int index;
		public String modifier;
		public Map options;
		public NFAState startState;
		public NFAState stopState;
		public GrammarAST tree;
		public GrammarAST lexerAction;
		/** The return values of a rule and predefined rule attributes */
		public AttributeScope returnScope;
		public AttributeScope parameterScope;
		/** the attributes defined with "scope {...}" inside a rule */
		public AttributeScope ruleScope;
		/** A list of scope names (String) used by this rule */
		public List useScopes;
		/** A list of all LabelElementPair attached to tokens like id=ID */
		public LinkedHashMap tokenLabels;
		/** A list of all LabelElementPair attached to rule references like f=field */
		public LinkedHashMap ruleLabels;
		public String toString() { // used for testing
			if ( modifier!=null ) {
				return modifier+" "+name;
			}
			return name;
		}
	}

	public static class Decision {
		public int decision;
		public NFAState startState;
		public Map options;
		public DFA dfa;
	}

	public static class LabelElementPair {
		public antlr.Token label;
		public GrammarAST elementRef;
		public LabelElementPair(antlr.Token label, GrammarAST elementRef) {
			this.label = label;
			this.elementRef = elementRef;
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
     *  like EPSILON. Char literals and token types overlap in the same
	 *  space, however.
     */
    protected int maxTokenType = Label.MIN_TOKEN_TYPE-1;

	protected IntSet charVocabulary = IntervalSet.of(0x0000,0xFFFE);

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

	/** For interpreting and testing, you sometimes want to import token
	 *  definitions from another grammar (instead of reading token defs from
	 *  a file).
	 */
	protected Grammar importTokenVocabularyFromGrammar;

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
			"lexer grammar <name>Lexer;\n" +
			"\n" +
			"<literals:{<it.ruleName> : <it.literal> ;\n}>\n" +
			"<rules; separator=\"\n\n\">",
			AngleBracketTemplateLexer.class
		);

	/** What file name holds this grammar? */
	protected String fileName;

	public Grammar() {
		initTokenSymbolTables();
	}

	public Grammar(String grammarString)
			throws antlr.RecognitionException, antlr.TokenStreamException
	{
		this("<string>",new StringReader(grammarString));
	}

    /** Create a grammar from a Reader.  Parse the grammar, building a tree
     *  and loading a symbol table of sorts here in Grammar.  Then create
     *  an NFA and associated factory.  Walk the AST representing the grammar,
     *  building the state clusters of the NFA.
     */
    public Grammar(String fileName, Reader r)
            throws antlr.RecognitionException, antlr.TokenStreamException
    {
        this();
		this.fileName = fileName;
		setGrammarContent(r);
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
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
		//System.out.println(grammarTree.toStringList());

		/*
		System.out.println("### print grammar");
		ANTLRTreePrinter printer = new ANTLRTreePrinter();
		printer.setASTNodeClass("org.antlr.tool.GrammarAST");
		System.out.println(printer.toString(grammarTree));
		*/

		// ASSIGN TOKEN TYPES
		System.out.println("### assign types");
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
		System.out.println("### define rules");
		DefineGrammarItemsWalker defineItemsWalker = new DefineGrammarItemsWalker();
		defineItemsWalker.setASTNodeClass("org.antlr.tool.GrammarAST");
		try {
			defineItemsWalker.grammar(grammarTree, this);
		}
		catch (RecognitionException re) {
			ErrorManager.error(ErrorManager.MSG_BAD_AST_STRUCTURE,
							   re);
		}
	}

	/** If he grammar is a merged grammar, return the text of the implicit
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
        while (iter.hasNext()) {
            Rule r = (Rule) iter.next();
			// only add real token rules to Tokens rule
			if ( r.modifier==null ||
				 !r.modifier.equals(FRAGMENT_RULE_MODIFIER) )
			{
            	matchTokenRuleST.setAttribute("rules", r.name);
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
        parser.setASTNodeClass("org.antlr.tool.GrammarAST");
        try {
            parser.rule();
            grammarTree.addChild(parser.getAST());
        }
        catch (Exception e) {
            ErrorManager.error(ErrorManager.MSG_ERROR_CREATING_ARTIFICIAL_RULE,e);
        }
		antlr.Token ruleToken = new antlr.TokenWithIndex(ANTLRParser.ID,TOKEN_RULENAME);
		defineRule(ruleToken, null, null, (GrammarAST)parser.getAST());
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
		System.out.println("### create NFAs");
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
		if ( Tool.GENERATE_NFA_DOT ) {
			try {
				new DOTGenerator(this).writeDOTFilesForAllRuleNFAs();
			}
			catch (IOException ioe) {
				ErrorManager.error(ErrorManager.MSG_CANNOT_WRITE_FILE, ioe);
			}
		}
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
		System.out.println("### create DFAs");
        for (int decision=1; decision<=getNumberOfDecisions(); decision++) {
            NFAState decisionStartState = getDecisionNFAStartState(decision);
            if ( decisionStartState.getNumberOfTransitions()>1 ) {
				/*
				System.out.println("--------------------\nbuilding lookahead DFA (d="
                        +decisionStartState.getDecisionNumber()+") for "+
                        decisionStartState.getDescription());
				long start = System.currentTimeMillis();
				*/
				DFA lookaheadDFA = new DFA(decisionStartState);
				if ( lookaheadDFA.analysisAborted() ) { // did analysis bug out?
					Map options = getDecisionOptions(decision);
					options.put("k", new Integer(1)); // set k=1 option and try again
					lookaheadDFA = new DFA(decisionStartState);
				}

				setLookaheadDFA(decision, lookaheadDFA);
				/*
				long stop = System.currentTimeMillis();
				System.out.println("cost: "+lookaheadDFA.getNumberOfStates()+
					" states, "+(int)(stop-start)+" ms");
				*/
				if ( Tool.GENERATE_DFA_DOT ) {
					DOTGenerator dotGenerator = new DOTGenerator(nfa.grammar);
					String dot = dotGenerator.getDOT( lookaheadDFA.startState );
					String dotFileName = "/tmp/"+name+"_dec-"+decision;
					try {
						dotGenerator.writeDOTFile(dotFileName, dot);
					}
					catch(IOException ioe) {
						ErrorManager.error(ErrorManager.MSG_CANNOT_GEN_DOT_FILE,
										   dotFileName,
										   ioe);
					}
				}
			}
		}
	}

	/** Define a token at a particular token type value.  Blast an
	 *  old value with a new one.  This is called directly during import vocab
     *  operation to set up tokens with specific values.
     */
    public void defineToken(String text, int tokenType) {
		// the index in the typeToTokenList table is actually shifted by
		// NUM_FAUX_LABELS as you cannot have negative indices.
		// For example, EOT is 6+-2-1 == 4
        if ( text.charAt(0)=='"' ) {
            stringLiteralToTypeMap.put(text, new Integer(tokenType));
        }
        else if ( text.charAt(0)=='\'' ) {
            charLiteralToTypeMap.put(text, new Integer(tokenType));
        }
        else { // must be a label like ID
            tokenNameToTypeMap.put(text, new Integer(tokenType));
        }
		int index = Label.NUM_FAUX_LABELS+(tokenType)-Label.MIN_TOKEN_TYPE;
		//System.out.println("defining token "+text+" at type="+tokenType+", index="+index);
		this.maxTokenType = Math.max(this.maxTokenType, tokenType);
        if ( index>=typeToTokenList.size() ) {
			typeToTokenList.setSize(index+1);
		}
        typeToTokenList.set(index, text);
    }

    /** Define a new token name, string or char literal token.
     *  A new token type is created by incrementing maxTokenType.
     *  Do nothing though if we've seen this token before or
     *  if the token is a string/char and we are in a lexer grammar.
     *  In a lexer, strings are simply matched and do not define
     *  a new token type.  Strings have token types in the lexer
     *  only when they are imported from another grammar such as
     *  a parser.  Do not define char literals as tokens in the
     *  lexer either unless they are imported.
     *
	 *  Return the new token type value or Label.INVALID.
	 *
	 *  Token rule fragments are assigned token types; you never know
	 *  when it will be useful and I assign token types during a single
	 *  pass--I cannot see ahead to see which token rules will be fragments.
	 */
	/*
	public int defineToken(String text) {
		int ttype = getTokenType(text);
		if ( ttype==Label.INVALID ) {
			//System.out.println("defineToken("+text+")");
			// if not in the lexer, then literals become token types
			boolean isLiteral = text.charAt(0)=='"'||text.charAt(0)=='\'';
			if ( getType()!=LEXER || !isLiteral ) {
				ttype = this.maxTokenType;
				defineToken(text, this.maxTokenType);
				this.maxTokenType++;
			}
		}
        return ttype;
    }
	*/

	/** Define a new rule.  A new rule index is created by incrementing
     *  ruleIndex.
     */
    public int defineRule(antlr.Token ruleToken,
						  String modifier,
						  Map options,
						  GrammarAST tree)
	{
		String ruleName = ruleToken.getText();
		/*
		System.out.println("defineRule("+ruleName+",modifier="+modifier+
						   "): index="+ruleIndex);
		*/
		if ( getRule(ruleName)!=null ) {
            ErrorManager.grammarError(ErrorManager.MSG_RULE_REDEFINITION,
									  this,
									  ruleToken,
									  ruleName);
            return INVALID_RULE_INDEX;
        }
		/*
        if ( type==PARSER && Character.isUpperCase(ruleName.charAt(0)) ) {
			ErrorManager.error(ErrorManager.MSG_BAD_AST_STRUCTURE,
									  this,
									  parser,
									  ruleToken,
									  ruleName);
            return INVALID_RULE_INDEX;
        }
        if ( type==LEXER && Character.isLowerCase(ruleName.charAt(0)) ) {
			ErrorManager.grammarError(ErrorManager.MSG_PARSER_RULES_NOT_ALLOWED,
									  this,
									  parser,
									  ruleToken,
									  ruleName);
            return INVALID_RULE_INDEX;
        }
		if ( type==LEXER && !ruleName.equals(TOKEN_RULENAME)) {
            // rules are also tokens in lexers
            defineToken(ruleName);
        }
		*/
		Rule r = new Rule();
		r.index = ruleIndex;
		r.name = ruleName;
		r.modifier = modifier;
		r.options = options;
        nameToRuleMap.put(ruleName, r);
		setRuleAST(ruleName, tree);
        ruleIndexToRuleList.setSize(ruleIndex+1);
        ruleIndexToRuleList.set(ruleIndex, ruleName);
        ruleIndex++;
		// all rules have predefined attributes available in the return scope
		r.returnScope = defineReturnScope(ruleName);
        return ruleIndex;
	}

	public void defineLexerRuleFoundInParser(String ruleName, String ruleText) {
		lexerGrammarST.setAttribute("rules", ruleText);
	}

	public void defineLexerRuleForStringLiteral(String literal, int tokenType) {
		lexerGrammarST.setAttribute("literals.{ruleName,type,literal}",
									computeTokenNameFromLiteral(tokenType,literal),
									new Integer(tokenType),
									Grammar.getANTLREscapedStringLiteral(literal));
	}

	public void defineLexerRuleForCharLiteral(String literal, int tokenType) {
		lexerGrammarST.setAttribute("literals.{ruleName,type,literal}",
									computeTokenNameFromLiteral(tokenType,literal),
									new Integer(tokenType),
									Grammar.getANTLREscapedStringLiteral(literal));
	}

	public Rule getRule(String ruleName) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		return r;
	}

	public GrammarAST getLexerRuleAction(String ruleName) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		return r.lexerAction;
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

	public void defineScope(String name, AttributeScope scope) {
		scopes.put(name,scope);
	}

	/** Define a either a global or rule scope such as return values. Rule
	 *  objects track the scopes as well.  This global list is used for
	 *  code generation; some targets need to do header files etc...
	 */
	public AttributeScope defineScope(String name) {
		AttributeScope scope = new AttributeScope(name);
		scopes.put(name,scope);
		return scope;
	}

	public AttributeScope defineReturnScope(String ruleName) {
		AttributeScope scope = new AttributeScope(ruleName);
		scope.isReturnScope = true;
		scopes.put(scope.getName(),scope);
		return scope;
	}

	public AttributeScope defineParameterScope(String ruleName) {
		AttributeScope scope = new AttributeScope(ruleName);
		scope.isParameterScope = true;
		scopes.put(scope.getName(),scope);
		return scope;
	}

	public AttributeScope getScope(String name) {
		return (AttributeScope)scopes.get(name);
	}

	/** Is id a valid scope name or r's name? */
	public boolean isValidScope(Rule r, String id) {
		if ( r==null ) {
			return getScope(id)!=null;
		}
		return id.equals(r.name) || getScope(id)!=null;
	}

	public AttributeScope getScopeContainingAttribute(Rule r,
													  String scopeName,
													  String attrName)
	{
		if ( r==null ) { // must be action not in a rule
			if ( scopeName==null ) {
				System.err.println("no scope: "+scopeName);
				return null;
			}
			AttributeScope scope = getScope(scopeName);
			return scope;
		}
		if ( scopeName!=null && !scopeName.equals(r.name) ) {
			AttributeScope scope = getScope(scopeName);
			// TODO: what to do if no attrName in scope?
			if ( scope.attributes.get(attrName)==null ) {
				System.err.println("no "+attrName+" in scope "+scopeName);
			}
			return scope;
		}
		if ( r.returnScope!=null && r.returnScope.attributes.get(attrName)!=null ) {
			return r.returnScope;
		}
		if ( r.parameterScope!=null && r.parameterScope.attributes.get(attrName)!=null ) {
			return r.parameterScope;
		}
		if ( r.ruleScope!=null && r.ruleScope.attributes.get(attrName)!=null ) {
			return r.ruleScope;
		}
		System.err.println("no scope for "+attrName+" (tried scope "+scopeName+")");
		return null;
	}

	public Map getScopes() {
		return scopes;
	}

	public void defineTokenRefLabel(String ruleName,
									antlr.Token label,
									GrammarAST tokenRef)
	{
        Rule r = getRule(ruleName);
		if ( r!=null ) {
			if ( r.tokenLabels==null ) {
				r.tokenLabels = new LinkedHashMap();
			}
			r.tokenLabels.put(label.getText(),
							  new LabelElementPair(label,tokenRef));
		}
	}

	public void defineRuleRefLabel(String ruleName,
								   antlr.Token label,
								   GrammarAST ruleRef)
	{
		Rule r = getRule(ruleName);
		if ( r!=null ) {
			if ( r.ruleLabels==null ) {
				r.ruleLabels = new LinkedHashMap();
			}
			r.ruleLabels.put(label.getText(),
							 new LabelElementPair(label,ruleRef));
		}
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
		//System.out.println("grammar type "+getType()+" "+tokenName+"->"+i);
        return i;
    }

	/** Get the list of tokens that are IDs like BLOCK and LPAREN */
	public Set getTokenIDs() {
		return tokenNameToTypeMap.keySet();
	}

	/** Get a list of all token IDs, literals that have an associated
	 *  token type.
	 */
	public Set getTokenNames() {
		Set names = new HashSet();
		for (int type=Label.MIN_TOKEN_TYPE; type<=getMaxTokenType(); type++) {
			names.add(getTokenName(type));
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
	 *  that has escaped chars instead of the real char value.
	 */
	public static String getANTLREscapedCharLiteral(String literal) {
		return CodeGenerator.getJavaEscapedCharFromANTLRLiteral(literal);
	}

	/** Convert a string literal as read by antlr.g back to a grammar literal
	 *  that has escaped chars instead of the real char values.
	 */
	public static String getANTLREscapedStringLiteral(String literal) {
		return CodeGenerator.getJavaEscapedStringFromANTLRLiteral(literal);
	}

	/** Given a literal like (the 3 char sequence in single quotes) 'a',
	 *  return the int value of 'a'.
     *  Escaped stuff is already converted to single char in single-quotes.
     */
    public static int getCharValueFromANTLRGrammarLiteral(String literal) {
        if ( literal.length()==3 ) {
            // no escape
            return literal.charAt(1);
        }
		ErrorManager.internalError("getCharValueFromANTLRGrammarLiteral: "+
								   literal+"; len="+literal.length());
        return Label.INVALID;
    }

    /** Return a set of all possible token/char types for this grammar */
	public IntSet getTokenTypes() {
		if ( type==LEXER ) {
			return charVocabulary;
		}
		return IntervalSet.of(Label.MIN_TOKEN_TYPE, getMaxTokenType());
	}

	public void importTokenVocabulary(Grammar g) {
		importTokenVocabularyFromGrammar = g;
	}

    public Grammar getGrammarWithTokenVocabularyToImport() {
		return importTokenVocabularyFromGrammar;
	}

	/** Given a token type, get a meaningful name for it such as the ID
	 *  or string literal etc...
	 */
	public String getTokenName(int ttype) {
		String tokenName = null;
		int index=0;
		// inside char range and lexer grammar?
		if ( this.type==LEXER && ttype >= Label.MIN_CHAR_VALUE && ttype <= Label.MAX_CHAR_VALUE ) {
			tokenName = "'"+CodeGenerator.getJavaUnicodeEscapeString(ttype)+"'";
		}
		// faux label?
		else if ( ttype<0 ) {
			tokenName = (String)typeToTokenList.get(Label.NUM_FAUX_LABELS+ttype);
		}
		else {
			index = ttype-Label.MIN_TOKEN_TYPE; // normalize index to 0..n
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
		//System.out.println("ttype="+ttype+", index="+index+", name="+tokenName);
		return tokenName;
	}

	/** Get a meaningful name for a token type useful during code generation. */
    public String getTokenTypeAsLabel(int ttype) {
        String name = getTokenName(ttype);
		// if it's not a lexer, then can't use char value, must have token type name
        if ( type!=LEXER && (name.charAt(0)=='"' || name.charAt(0)=='\'') ) {
            return String.valueOf(ttype);
        }
        if ( ttype==Label.EOF ) {
            return String.valueOf(CharStream.EOF);
        }
        return name;
    }

    /** Save the option key/value pair and process it */
    public void setOption(String key, Object value) {
        options.put(key, value);
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

    public Collection getRules() {
        return nameToRuleMap.values();
    }

	public void setRuleAST(String ruleName, GrammarAST t) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		if ( r!=null ) {
			r.tree = t;
		}
	}

	/** When interpreting a lexer grammar, one wants to be able exec
	 *  lexer actions to set the channel and token type.  For now,
	 *  only the last action found is tracked per rule.
	 */
	public void setLexerRuleAction(String ruleName, GrammarAST t) {
		Rule r = (Rule)nameToRuleMap.get(ruleName);
		if ( r!=null ) {
			r.lexerAction = t;
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

    public Map getDecisionOptions(int decision) {
		Decision d = getDecision(decision);
		if ( d==null ) {
			return null;
		}
		return d.options;
    }

    public int getNumberOfDecisions() {
        return decisionNumber;
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

	public void setDecisionOptions(int decision, Map options) {
		Decision d = createDecision(decision);
		d.options = options;
	}

    /** How many token types have been allocated so far? */
    public int getMaxTokenType() {
        return maxTokenType;
    }

    /** For lexer grammars, return everything in unicode not in set.
     *  For parser and tree grammars, return everything in token space
     *  from MIN_TOKEN_TYPE to last valid token type.
     */
    public IntSet complement(IntSet set) {
        if ( type == LEXER ) {
            return set.complement(Label.ALLCHAR);
        }
        System.out.println("complement "+set.toString(this));
        System.out.println("vocabulary "+getTokenTypes().toString(this));
        IntSet c = set.complement(getTokenTypes());
        System.out.println("result="+c.toString(this));
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
            NFAState q = (NFAState)p.transition(0).target;
            while ( q.transition(0)!=null ) {
                q = (NFAState)p.transition(0).target;
            }
            alts.add( p.transition(0) );
            if ( p.transition(1)!=null ) {
                p = (NFAState)p.transition(1).target;
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
     *  alternative number.
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
/*
if ( sl.member(Label.EOF) ) {
				System.err.println("uh oh; set has EOF: "+sl.toString(this));
				LookaheadSet e = LookaheadSet.EOF();
				sl.remove(Label.EOF);
				e.tokenTypeSet = new Lookah ead.of(sl);
				return e;
			}
			return new LookaheadSet(BitSet.of(sl));
			*/
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
        String s = null;
        try {
            s = t.getLine()+":"+t.getColumn()+": ";
            s += new ANTLRTreePrinter().toString((AST)t, this);
        }
        catch (Exception e) {
            ErrorManager.error(ErrorManager.MSG_BAD_AST_STRUCTURE,
							   t,
							   e);
        }
        return s;
    }

}
