header {
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
	import java.util.*;
	import org.antlr.analysis.*;
	import java.io.*;
}

/** Compute the token types for all literals and rules etc..  There are
 *  a few different cases to consider for grammar types and a few situations
 *  within.
 *
 *  CASE 1 : pure parser grammar
 *	a) Any reference to a token gets a token type.
 *  b) The tokens section may alias a token name to a string or char
 *
 *  CASE 2 : pure lexer grammar
 *  a) Import token vocabulary if available. Set token types for any new tokens
 *     to values above last imported token type
 *  b) token rule definitions get token types if not already defined
 *  c) literals do NOT get token types
 *
 *  CASE 3 : merged parser / lexer grammar
 *	a) Any char or string literal gets a token type in a parser rule
 *  b) Any reference to a token gets a token type if not referencing
 *     a fragment lexer rule
 *  c) The tokens section may alias a token name to a string or char
 *     which must add a rule to the lexer
 *  d) token rule definitions get token types if not already defined
 *  e) token rule definitions may also alias a token name to a literal.
 *     E.g., Rule 'FOR : "for";' will alias FOR to "for" in the sense that
 *     references to either in the parser grammar will yield the token type
 *
 *  What this pass does:
 *
 *  0. Collects basic info about the grammar like grammar name and type;
 *     Oh, I have go get the options in case they affect the token types.
 *     E.g., tokenVocab option.
 *     Imports any token vocab name/type pairs into a local hashtable.
 *  1. Finds a list of all literals and token names.
 *  2. Finds a list of all token name rule definitions;
 *     no token rules implies pure parser.
 *  3. Finds a list of all simple token rule defs of form "<NAME> : <literal>;"
 *     and aliases them.
 *  4. Walks token names table and assign types to any unassigned
 *  5. Walks aliases and assign types to referenced literals
 *  6. Walks literals, assigning types if untyped
 *  4. Informs the Grammar object of the type definitions such as:
 *     g.defineToken(<charliteral>, ttype);
 *     g.defineToken(<stringliteral>, ttype);
 *     g.defineToken(<tokenID>, ttype);
 *     where some of the ttype values will be the same for aliases tokens.
 */
class AssignTokenTypesWalker extends TreeParser;

options {
	importVocab = ANTLR;
	ASTLabelType = "GrammarAST";
    codeGenBitsetTestThreshold=999;
}

{
    public void reportError(RecognitionException ex) {
		Token token = null;
		if ( ex instanceof MismatchedTokenException ) {
			token = ((MismatchedTokenException)ex).token;
		}
		else if ( ex instanceof NoViableAltException ) {
			token = ((NoViableAltException)ex).token;
		}
        ErrorManager.syntaxError(
            ErrorManager.MSG_SYNTAX_ERROR,
            token,
            "assign.types: "+ex.toString(),
            ex);
    }

protected Grammar grammar;
protected Map charLiterals = new LinkedHashMap();   // Map<literal,Integer>
protected Map stringLiterals = new LinkedHashMap(); // Map<literal,Integer>
protected Map tokens = new LinkedHashMap();         // Map<name,Integer>
protected Map aliases = new LinkedHashMap();        // Map<name,literal>
protected String currentRuleName;
protected static final Integer UNASSIGNED = new Integer(-1);
protected static final Integer UNASSIGNED_IN_PARSER_RULE = new Integer(-2);
protected int tokenType = Label.MIN_TOKEN_TYPE;

protected int getNewTokenType() {
	int type = tokenType;
	tokenType++;
	return type;
}

/** Track characters in any non-lexer rule (could be in tokens{} section) */
protected void trackChar(GrammarAST t) {
	// if lexer don't allow aliasing in tokens section
	if ( currentRuleName==null && grammar.type==Grammar.LEXER ) {
		ErrorManager.grammarError(ErrorManager.MSG_CANNOT_ALIAS_TOKENS_IN_LEXER,
								  grammar,
								  t.token,
								  t.getText());
		return;
	}
	// if not in a combined grammar rule or lexer rule, cannot reference literals
	if ( grammar.type!=Grammar.COMBINED && grammar.type!=Grammar.LEXER ) {
		ErrorManager.grammarError(ErrorManager.MSG_LITERAL_NOT_ASSOCIATED_WITH_LEXER_RULE,
								  grammar,
								  t.token,
								  t.getText());
	}
	// otherwise add literal to token types if referenced from parser rule
	// or in the tokens{} section
	if ( currentRuleName==null ||
	     Character.isLowerCase(currentRuleName.charAt(0)) )
	{
		charLiterals.put(t.getText(), UNASSIGNED_IN_PARSER_RULE);
	}
}

/** Track string literals in any non-lexer rule (could be in tokens{} section) */
protected void trackString(GrammarAST t) {
	// if lexer don't allow aliasing in tokens section
	if ( currentRuleName==null && grammar.type==Grammar.LEXER ) {
		ErrorManager.grammarError(ErrorManager.MSG_CANNOT_ALIAS_TOKENS_IN_LEXER,
								  grammar,
								  t.token,
								  t.getText());
		return;
	}
	// if not in a combined grammar rule or lexer rule, cannot reference literals
	if ( grammar.type!=Grammar.COMBINED && grammar.type!=Grammar.LEXER ) {
		ErrorManager.grammarError(ErrorManager.MSG_LITERAL_NOT_ASSOCIATED_WITH_LEXER_RULE,
								  grammar,
								  t.token,
								  t.getText());
	}
	// otherwise add literal to token types if referenced from parser rule
	// or in the tokens{} section
	if ( currentRuleName==null ||
         Character.isLowerCase(currentRuleName.charAt(0)) )
	{
		stringLiterals.put(t.getText(), UNASSIGNED_IN_PARSER_RULE);
	}
}

protected void trackToken(GrammarAST t) {
	// imported token names might exist, only add if new
	if ( tokens.get(t.getText())==null ) {
		tokens.put(t.getText(), UNASSIGNED);
	}
}

protected void trackTokenRule(GrammarAST t,
							  GrammarAST modifier,
							  GrammarAST block)
{
	// imported token names might exist, only add if new
	if ( grammar.type==Grammar.LEXER || grammar.type==Grammar.COMBINED ) {
		if ( !Character.isUpperCase(t.getText().charAt(0)) ) {
			return;
		}
		Integer existing = (Integer)tokens.get(t.getText());
		if ( existing==null ) {
			tokens.put(t.getText(), UNASSIGNED);
		}
		// look for "<TOKEN> : <literal> ;" pattern
		GrammarAST stringAlias = #(#[BLOCK], #(#[ALT], #[STRING_LITERAL]));
		GrammarAST charAlias = #(#[BLOCK], #(#[ALT], #[CHAR_LITERAL]));
		if ( matchesStructure(block,stringAlias) ||
		     matchesStructure(block,charAlias) )
	    {
			alias(t, (GrammarAST)block.getFirstChild().getFirstChild());
		}
	}
	// else error
}

protected void defineToken(String tokenID, int ttype) {
	tokens.put(tokenID, new Integer(ttype));
}

protected boolean matchesStructure(AST a, AST b) {
	// the empty tree is always a subset of any tree.
	if (b == null || a==null) {
		return true;
	}

	// check roots first.
	if ( a.getType()!=b.getType() ) {
		return false;
	}

	// if roots match, do full list partial match test on children.
	if (a.getFirstChild() != null) {
		if (!matchesStructure(a.getFirstChild(),b.getFirstChild())) {
			return false;
		}
	}
	return true;
}

protected void alias(GrammarAST t, GrammarAST s) {
	aliases.put(t.getText(), s.getText());
}

protected void assignTypes() {
	/*
	System.out.println("charLiterals="+charLiterals);
	System.out.println("stringLiterals="+stringLiterals);
	System.out.println("tokens="+tokens);
	System.out.println("aliases="+aliases);
	*/

	assignTokenIDTypes();

	aliasTokenIDsAndLiterals();

	assignCharTypes();

	assignStringTypes();

	/*
	System.out.println("AFTER:");
	System.out.println("charLiterals="+charLiterals);
	System.out.println("stringLiterals="+stringLiterals);
	System.out.println("tokens="+tokens);
	System.out.println("aliases="+aliases);
	*/

	notifyGrammarObject();
}

	protected void assignStringTypes() {
		// walk string literals assigning types to unassigned ones
		Set s = stringLiterals.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String lit = (String) it.next();
			Integer oldTypeI = (Integer)stringLiterals.get(lit);
			int oldType = oldTypeI.intValue();
			if ( oldType<Label.MIN_TOKEN_TYPE ) {
				Integer typeI = new Integer(getNewTokenType());
				stringLiterals.put(lit, typeI);
				// if string referenced in combined grammar parser rule,
				// automatically define in the generated lexer
				grammar.defineLexerRuleForStringLiteral(lit, typeI.intValue());
			}
		}
	}

	protected void assignCharTypes() {
		Set s;
		// walk char literals assigning types to unassigned ones
		s = charLiterals.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String lit = (String) it.next();
			Integer oldTypeI = (Integer)charLiterals.get(lit);
			int oldType = oldTypeI.intValue();
			if ( oldType<Label.MIN_TOKEN_TYPE ) {
				Integer typeI = new Integer(getNewTokenType());
				charLiterals.put(lit, typeI);
				if ( oldTypeI == UNASSIGNED_IN_PARSER_RULE ) {
					grammar.defineLexerRuleForCharLiteral(lit, typeI.intValue());
				}
			}
		}
	}

	protected void aliasTokenIDsAndLiterals() {
		if ( grammar.type!=Grammar.COMBINED ) {
			return; // strings/chars are never token types 'cept in combined
		}
		// walk aliases if any and assign types to aliased literals if literal
		// was referenced
		Set s = aliases.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String tokenID = (String) it.next();
			String literal = (String)aliases.get(tokenID);
			if ( literal.charAt(0)=='"' && stringLiterals.get(literal)!=null ) {
				stringLiterals.put(literal, tokens.get(tokenID));
			}
			else if ( literal.charAt(0)=='\'' && charLiterals.get(literal)!=null ) {
				charLiterals.put(literal, tokens.get(tokenID));
			}
		}
	}

	protected void assignTokenIDTypes() {
		// walk token names, assigning values if unassigned
		Set s = tokens.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String tokenID = (String) it.next();
			if ( tokens.get(tokenID)==UNASSIGNED ) {
				tokens.put(tokenID, new Integer(getNewTokenType()));
			}
		}
	}

	protected void notifyGrammarObject() {
		Set s = tokens.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String tokenID = (String) it.next();
			int ttype = ((Integer)tokens.get(tokenID)).intValue();
			grammar.defineToken(tokenID, ttype);
		}
		s = charLiterals.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String lit = (String) it.next();
			int ttype = ((Integer)charLiterals.get(lit)).intValue();
			grammar.defineToken(lit, ttype);
		}
		s = stringLiterals.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String lit = (String) it.next();
			int ttype = ((Integer)stringLiterals.get(lit)).intValue();
			grammar.defineToken(lit, ttype);
		}
	}

	/** Pull your token definitions from an existing grammar in memory.
	 *  You must use Grammar() ctor then this method then setGrammarContent()
	 *  to make this work.  This is useful primarily for testing and
	 *  interpreting grammars.
	 */
	protected void importTokenVocabulary(Grammar g) {
		int maxTokenType=0;
		Set importedTokenIDs = g.getTokenIDs();
		for (Iterator it = importedTokenIDs.iterator(); it.hasNext();) {
			String tokenID = (String) it.next();
			int tokenType = g.getTokenType(tokenID);
			maxTokenType = Math.max(maxTokenType,tokenType);
			if ( tokenType>=Label.MIN_TOKEN_TYPE ) {
				//System.out.println("import token from grammar "+tokenID+"="+tokenType);
				defineToken(tokenID, tokenType);
			}
		}
		if ( maxTokenType>0 ) {
			this.tokenType = maxTokenType+1; // next type is defined above imported
		}
	}

	protected void importTokenVocab(String vocabName) {
		int maxTokenType = -1;
		try {
			BufferedReader br = grammar.getTool().getLibraryFile(vocabName+".tokens");
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
				String tokenID=tokenizer.nextToken();
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
				// System.out.println("import "+tokenID+"="+tokenType);
				maxTokenType = Math.max(maxTokenType,tokenType);
				defineToken(tokenID, tokenType);
				line = br.readLine();
				n++;
			}
			br.close();
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
			this.tokenType = maxTokenType+1; // next type is defined above imported
		}
	}

	protected void init(Grammar g) {
		this.grammar = g;
		Grammar importG = grammar.getGrammarWithTokenVocabularyToImport();
		if ( importG!=null ) {
			importTokenVocabulary(importG);
		}
	}
}

grammar[Grammar g]
{
	init(g);
}
    :   (headerSpec)*
	    ( #( LEXER_GRAMMAR 	  {grammar.type = Grammar.LEXER;} 	  grammarSpec )
	    | #( PARSER_GRAMMAR   {grammar.type = Grammar.PARSER;}      grammarSpec )
	    | #( TREE_GRAMMAR     {grammar.type = Grammar.TREE_PARSER;} grammarSpec )
	    | #( COMBINED_GRAMMAR {grammar.type = Grammar.COMBINED;}    grammarSpec )
	    )
        {assignTypes();}
    ;

headerSpec
    :   #( "header" a:ACTION )
    ;

grammarSpec
{Map opts=null;}
	:	id:ID {grammar.name = #id.getText();}
		(cmt:DOC_COMMENT)?
		(optionsSpec)?
/*
        ( {Token optionsStartToken=((GrammarAST)_t).getToken();}
          opts=optionsSpec {grammar.setOptions(opts, optionsStartToken);}
        )?
*/
        (tokensSpec)?
        (attrScope)*
        (ACTION)?
        rules
	;

attrScope
	:	#( "scope" ID ACTION )
	;

optionsSpec returns [Map opts=new HashMap()]
    :   #( OPTIONS (option[opts])+ )
    ;

option[Map opts]
{
    String key=null;
    Object value=null;
}
    :   #( ASSIGN id:ID {key=#id.getText();} value=optionValue )
        {
        opts.put(key,value);
        // check for grammar-level option to import vocabulary
        if ( currentRuleName==null && key.equals("tokenVocab") ) {
            importTokenVocab((String)value);
        }
        }
    ;

optionValue returns [Object value=null]
    :   id:ID			 {value = #id.getText();}
    |   s:STRING_LITERAL {String vs = #s.getText();
                          value=vs.substring(1,vs.length()-1);}
    |   c:CHAR_LITERAL   {String vs = #c.getText();
                          value=vs.substring(1,vs.length()-1);}
    |   i:INT            {value = new Integer(#i.getText());}
//  |   cs:charSet       {value = #cs;} // return set AST in this case
    ;

charSet
	:   #( CHARSET charSetElement )
	;

charSetElement
	:   c:CHAR_LITERAL
	|   #( OR c1:CHAR_LITERAL c2:CHAR_LITERAL )
	|   #( RANGE c3:CHAR_LITERAL c4:CHAR_LITERAL )
	;

tokensSpec
	:	#( TOKENS ( tokenSpec )+ )
	;

tokenSpec
	:	t:TOKEN_REF           {trackToken(t);}
	|	#( ASSIGN
		   t2:TOKEN_REF       {trackToken(t2);}
		   ( s:STRING_LITERAL {trackString(s); alias(t2,s);}
		   | c:CHAR_LITERAL   {trackChar(c); alias(t2,c);}
		   )
		 )
	;

rules
    :   ( rule )+
    ;

rule
    :   #( RULE id:ID {currentRuleName=#id.getText();}
           (m:modifier)?
           (ARG (ARG_ACTION)?)
           (RET (ARG_ACTION)?)
           (optionsSpec)?
           (ruleScopeSpec)?
           #( INITACTION (ACTION)? )
           b:block EOR
           {trackTokenRule(#id,#m,#b);}
         )
    ;

modifier
	:	"protected"
	|	"public"
	|	"private"
	|	"fragment"
	;

ruleScopeSpec
 	:	#( "scope" (ACTION)? ( ID )* )
 	;

block
    :   #(  BLOCK
            (optionsSpec)?
            alternative (  alternative)*
            EOB   
         )
    ;

alternative
    :   #( ALT (element)+ EOA )
    ;

element
    :   atom
    |   #(NOT atom)
    |   #(RANGE atom atom)
    |   #(CHAR_RANGE atom atom)
    |	#(ASSIGN ID atom)
    |	#(PLUS_ASSIGN ID atom)
    |   ebnf
    |   tree
    |   #( SYNPRED block ) 
    |   ACTION
    |   SEMPRED
    |   EPSILON 
    ;

ebnf:   block 
    |   #( OPTIONAL block ) 
    |   #( CLOSURE block )  
    |   #( POSITIVE_CLOSURE block ) 
    ;

tree:   #(TREE_BEGIN  atom (element)*  )
    ;

atom
    :   RULE_REF
    |   t:TOKEN_REF      {trackToken(t);}
    |   c:CHAR_LITERAL   {trackChar(c);}
    |   s:STRING_LITERAL {trackString(s);}
    |   WILDCARD
    |	set
    ;

set :   #(SET (setElement)+)
    ;

setElement
    :   c:CHAR_LITERAL   {trackChar(c);}
    |   t:TOKEN_REF      {trackToken(t);}
    |   s:STRING_LITERAL {trackString(s);}
    |	#(CHAR_RANGE c1:CHAR_LITERAL c2:CHAR_LITERAL)
    	{
    	trackChar(c1);
    	trackChar(c2);
    	}
    ;
