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
import antlr.LLkParser;

import java.io.*;
import java.util.*;
import org.antlr.analysis.*;
import org.antlr.analysis.DFA;
import org.antlr.runtime.*;
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
	public static final String NONTOKEN_LEXER_RULE_MODIFIER = "fragment";

    public static final int LEXER = 1;
    public static final int PARSER = 2;
    public static final int TREE_PARSER = 3;

	/** Combine the info associated with a rule; I'm using like a struct */
	protected class Rule {
		String name;
		int index;
		String modifier;
		Map options;
		NFAState startState;
		NFAState stopState;
		GrammarAST tree;
		GrammarAST lexerAction;
		public String getName() {return name;}
	}

	protected class Decision {
		int decision;
		NFAState startState;
		Map options;
		DFA dfa;
		//AST tree;
	}

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

	/** Map a rule to it's Rule object
	 *  TODO 1.4 depedency.
	 */
	protected LinkedHashMap nameToRuleMap = new LinkedHashMap();

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
			"<rules>",
			AngleBracketTemplateLexer.class
		);

	protected String fileName;

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
		ANTLRParser parser = new ANTLRParser(tokenBuffer, this);
		parser.setFilename(this.getFileName());
		parser.setASTNodeClass("org.antlr.tool.GrammarAST");
		parser.grammar();
		grammarTree = (GrammarAST)parser.getAST();
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
		lexerGrammarST.setAttribute("name", getName());
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
				 !r.modifier.equals(NONTOKEN_LEXER_RULE_MODIFIER) )
			{
            	matchTokenRuleST.setAttribute("rules", r.name);
			}
        }
		//System.out.println("rule: "+matchTokenRuleST.toString());

        ANTLRLexer lexer = new ANTLRLexer(new StringReader(matchTokenRuleST.toString()));
		lexer.setTokenObjectClass("antlr.TokenWithIndex");
		TokenStreamRewriteEngine tokenBuffer =
			new TokenStreamRewriteEngine(lexer);
        ANTLRParser parser = new ANTLRParser(tokenBuffer, this);
        parser.setASTNodeClass("org.antlr.tool.GrammarAST");
        try {
            parser.rule();
            grammarTree.addChild(parser.getAST());
        }
        catch (Exception e) {
            ErrorManager.error(ErrorManager.MSG_ERROR_CREATING_ARTIFICIAL_RULE,e);
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

	/** Pull your token definitions from an existing grammar in memory.
	 *  You must use Grammar() ctor then this method then setGrammarContent()
	 *  to make this work.  This is useful primarily for testing and
	 *  interpreting grammars.
	 */
	public void importTokenVocabulary(Grammar g) {
		Set importedTokenNames = g.getTokenNames();
		int maxTokenType = 0;
		for (Iterator it = importedTokenNames.iterator(); it.hasNext();) {
			String tokenName = (String) it.next();
			int tokenType = g.getTokenType(tokenName);
			maxTokenType = Math.max(maxTokenType,tokenType);
			if ( tokenType>=Label.MIN_TOKEN_TYPE ) {
				defineToken(tokenName, tokenType);
			}
		}
		if ( maxTokenType>0 ) {
			tokenType = maxTokenType+1; // next type is defined above imported
		}
	}

	protected void importTokenVocab(String vocabName) {
		int maxTokenType = -1;
		try {
			FileReader fr = new FileReader(vocabName+".tokens");
			BufferedReader br = new BufferedReader(fr);
			String line = br.readLine();
			int n = 1;
			while ( line!=null ) {
				if ( line.length()==0 ){
					continue; // ignore blank lines
				}
				StringTokenizer tokenizer = new StringTokenizer(line, "=", true);
				if ( !tokenizer.hasMoreTokens() ) {
					ErrorManager.error(ErrorManager.MSG_TOKENS_FILE_SYNTAX_ERROR,
									   vocabName+".tokens",
									   new Integer(n));
				}
				String tokenName=tokenizer.nextToken();
				if ( !tokenizer.hasMoreTokens() ) {
					ErrorManager.error(ErrorManager.MSG_TOKENS_FILE_SYNTAX_ERROR,
									   vocabName+".tokens",
									   new Integer(n));
				}
				tokenizer.nextToken(); // skip '='
				if ( !tokenizer.hasMoreTokens() ) {
					ErrorManager.error(ErrorManager.MSG_TOKENS_FILE_SYNTAX_ERROR,
									   vocabName+".tokens",
									   new Integer(n));
				}
				String tokenTypeS=tokenizer.nextToken();
				int tokenType = Integer.parseInt(tokenTypeS);
				//System.out.println("import "+tokenName+"="+tokenType);
				maxTokenType = Math.max(maxTokenType,tokenType);
				defineToken(tokenName, tokenType);
				line = br.readLine();
				n++;
			}
			br.close();
			fr.close();
		}
		catch (FileNotFoundException fnfe) {
			ErrorManager.error(ErrorManager.MSG_CANNOT_FIND_TOKENS_FILE,
							   vocabName+".tokens");
		}
		catch (IOException ioe) {
			ErrorManager.error(ErrorManager.MSG_ERROR_READING_TOKENS_FILE,
							   vocabName+".tokens",
							   ioe);
		}
		catch (Exception e) {
			ErrorManager.error(ErrorManager.MSG_ERROR_READING_TOKENS_FILE,
							   vocabName+".tokens",
							   e);
		}
		if ( maxTokenType>0 ) {
			tokenType = maxTokenType+1; // next type is defined above imported
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

    public void createNFAs() {
		nfa = new NFA(this); // create NFA that TreeToNFAConverter'll fill in
		NFAFactory factory = new NFAFactory(nfa);
		TreeToNFAConverter nfaBuilder = new TreeToNFAConverter(this, nfa, factory);
		try {
			nfaBuilder.grammar(grammarTree);
		}
		catch (RecognitionException re) {
			ErrorManager.error(ErrorManager.MSG_BAD_AST_STRUCTURE_DURING_BUILDNFA,
							   getName(),
							   re);
		}
		//System.out.println("NFA has "+factory.getNumberOfStates()+" states");
		try {
			new DOTGenerator(this).writeDOTFilesForAllRuleNFAs();
		}
		catch (IOException ioe) {
			ErrorManager.error(ErrorManager.MSG_CANNOT_WRITE_FILE);
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
	 *
	 *  TODO: generate NFA as option or another method
     */
    public void createLookaheadDFAs() {
        for (int decision=1; decision<=getNumberOfDecisions(); decision++) {
            NFAState decisionStartState = getDecisionNFAStartState(decision);
            if ( decisionStartState.getNumberOfTransitions()>1 ) {
                /*
				System.out.println("--------------------\nbuilding lookahead DFA (d="
                        +decisionStartState.getDecisionNumber()+") for "+
                        decisionStartState.getDescription());
                */
				long start = System.currentTimeMillis();
                DFA lookaheadDFA = new DFA(decisionStartState);
                long stop = System.currentTimeMillis();
				setLookaheadDFA(decision, lookaheadDFA);
				/*
				System.out.println("DFA (d="+lookaheadDFA.getDecisionNumber()+") cost: "+lookaheadDFA.getNumberOfStates()+
								   " states, "+(int)(stop-start)+" ms; descr="+decisionStartState.getDescription());
				*/
				if ( true ) {
					DOTGenerator dotGenerator = new DOTGenerator(nfa.getGrammar());
					String dot = dotGenerator.getDOT( lookaheadDFA.getStartState() );
					String dotFileName = "/tmp/dec-"+decision;
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

	/** Define either a token at a particular token type value.  Blast an
     *  old value with a new one.  This is called directly during import vocab
     *  operation to set up tokens with specific values.
     */
    public void defineToken(String text, int tokenType) {
        //System.out.println("defining token "+text+" at type="+tokenType);
        int index = Label.NUM_FAUX_LABELS+(tokenType)-Label.MIN_TOKEN_TYPE;
        if ( text.charAt(0)=='"' ) {
            stringLiteralToTypeMap.put(text, new Integer(tokenType));
			defineLexerRuleForStringLiteral(text, tokenType);
        }
        else if ( text.charAt(0)=='\'' ) {
            charLiteralToTypeMap.put(text, new Integer(tokenType));
            index = tokenType; // for char, token type is as sent in
			defineLexerRuleForCharLiteral(text, tokenType);
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
	public int defineToken(String text) {
		int ttype = getTokenType(text);
		if ( ttype==Label.INVALID ) {
			//System.out.println("defineToken("+text+")");
			// if not in the lexer, then literals become token types
			boolean isLiteral = text.charAt(0)=='"'||text.charAt(0)=='\'';
			if ( getType()!=LEXER || !isLiteral ) {
				ttype = this.tokenType;
				defineToken(text, this.tokenType);
				this.tokenType++;
			}
		}
        return ttype;
    }

    /** Define a new rule.  A new rule index is created by incrementing
     *  ruleIndex. Pass in the parser and token info so we can pass the
	 *  info on to the error manager if there is a problem.
     */
    public int defineRule(LLkParser parser,
						  antlr.Token ruleToken,
						  String modifier,
						  Map options)
	{
		String ruleName = ruleToken.getText();
        //System.out.println("defineRule("+ruleName+",modifier="+modifier+"): index="+ruleIndex);
        if ( getRule(ruleName)!=null ) {
            ErrorManager.grammarError(ErrorManager.MSG_RULE_REDEFINITION,
									  this,
									  parser,
									  ruleToken,
									  ruleName);
            return INVALID_RULE_INDEX;
        }
        if ( type==PARSER && Character.isUpperCase(ruleName.charAt(0)) ) {
			ErrorManager.grammarError(ErrorManager.MSG_LEXER_RULES_NOT_ALLOWED,
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
		Rule r = new Rule();
		r.index = ruleIndex;
		r.name = ruleName;
		r.modifier = modifier;
		r.options = options;
        nameToRuleMap.put(ruleName, r);
        ruleIndexToRuleList.setSize(ruleIndex+1);
        ruleIndexToRuleList.set(ruleIndex, ruleName);
        ruleIndex++;
        return ruleIndex;
	}

	public void defineLexerRuleFoundInParser(String ruleName, String ruleText) {
		lexerGrammarST.setAttribute("rules", ruleText);
	}

	public void defineLexerRuleForStringLiteral(String literal, int tokenType) {
		lexerGrammarST.setAttribute("literals.{ruleName,type,literal}",
									computeTokenNameFromLiteral(tokenType,literal),
									new Integer(tokenType),
									literal);
		defineToken(computeTokenNameFromLiteral(tokenType, literal), tokenType);
	}

	public void defineLexerRuleForCharLiteral(String literal, int tokenType) {
		lexerGrammarST.setAttribute("literals.{ruleName,type,literal}",
									computeTokenNameFromLiteral(tokenType,literal),
									new Integer(tokenType),
									literal);
		defineToken(computeTokenNameFromLiteral(tokenType, literal), tokenType);
	}

	protected Rule getRule(String ruleName) {
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

    /** Return a set of all possible token types for this grammar */
    public IntSet getTokenTypes() {
        return IntervalSet.of(Label.MIN_TOKEN_TYPE,
                              Label.MIN_TOKEN_TYPE+getNumberOfTokenTypes()-1);
    }

    public String getTokenName(int ttype) {
		// inside char range and lexer grammar?
		if ( this.type==LEXER && ttype >= Label.MIN_LABEL_VALUE && ttype <= '\uFFFF' ) {
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
            return String.valueOf(CharStream.EOF);
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
            s += new ANTLRTreePrinter().toString((AST)t);
        }
        catch (Exception e) {
            ErrorManager.error(ErrorManager.MSG_BAD_AST_STRUCTURE_DURING_PRINTING,
							   t,
							   e);
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
