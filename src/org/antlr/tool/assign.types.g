header {
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
	import java.util.*;
	import org.antlr.analysis.*;
	import java.io.*;
}

/** Compute the token types for all literals and rules etc..  There are
 *  a few different cases to consider for grammar types and a few situations
 *  within.
 *
 *  CASE 1 : pure parser grammar
 *	a) Any char or string literal gets a token type.
 *  b) Any reference to a token gets a token type.
 *  c) The tokens section may alias a token name to a string or char (n/a yet?)
 *
 *  CASE 2 : pure lexer grammar
 *  a) Import token vocabulary if available. Set token types for any new tokens
 *     to values above last imported token type
 *  b) token rule definitions get token types if not already defined
 *  c) literals do NOT get token types
 *
 *  CASE 3 : merged parser / lexer grammar
 *	a) Any char or string literal gets a token type.
 *  b) Any reference to a token gets a token type.
 *  c) The tokens section may alias a token name to a string or char (n/a yet?)
 *  d) token rule definitions get token types if not already defined
 *  e) token rule definitions may also alias a token name to a literal.
 *     E.g., Rule 'FOR : "for";' will alias FOR to "for" in the sense that
 *     references to either in the parser grammar will yield the
 *
 *  Errors/warnings
 *  a) References to undefined token names in the lexer are a problem.
 *  b) For merged lexers/parsers references to undefined token names in
 *     any rule is a problem.  Further, any reference to a literal in a
 *     parser rule that is not defined in the lexer causes an error.
 *  c) Unaliased literals in a pure parser cannot ever be matched as
 *     there is no connection to a lexer's token types.
 *  d) no tokens section allowed only in parser or merged spec
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
 *     g.defineToken(<tokenName>, ttype);
 *     where some of the ttype values will be the same for aliases tokens.
 */
class AssignTokenTypesWalker extends TreeParser;

options {
	importVocab = ANTLR;
	ASTLabelType = "GrammarAST";
    codeGenBitsetTestThreshold=999;
}

{
	/** Parser error-reporting function can be overridden in subclass */
	public void reportError(RecognitionException ex) {
		System.out.println("assign types: "+ex.toString());
	}

	/** Parser error-reporting function can be overridden in subclass */
	public void reportError(String s) {
		System.out.println("assign types: error: " + s);
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

protected void trackChar(GrammarAST t) {
	if ( grammar.getType()==Grammar.COMBINED &&
	     Character.isLowerCase(currentRuleName.charAt(0)) )
    {
		charLiterals.put(t.getText(), UNASSIGNED_IN_PARSER_RULE);
	}
	if ( grammar.getType()!=Grammar.LEXER ) {
		charLiterals.put(t.getText(), UNASSIGNED);
	}
}

protected void trackString(GrammarAST t) {
	if ( grammar.getType()==Grammar.COMBINED &&
	     Character.isLowerCase(currentRuleName.charAt(0)) )
    {
		stringLiterals.put(t.getText(), UNASSIGNED_IN_PARSER_RULE);
	}
	if ( grammar.getType()!=Grammar.LEXER ) {
		stringLiterals.put(t.getText(), UNASSIGNED);
	}
}

protected void trackToken(GrammarAST t) {
	// imported token names might exist, only add if new
	if ( tokens.get(t.getText())==null ) {
		tokens.put(t.getText(), UNASSIGNED);
	}
}

protected void trackTokenRule(GrammarAST t, GrammarAST block) {
	// imported token names might exist, only add if new
	if ( grammar.getType()==Grammar.LEXER || grammar.getType()==Grammar.COMBINED ) {
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

protected void defineToken(String tokenName, int ttype) {
	tokens.put(tokenName, new Integer(ttype));
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
	System.out.println("charLiterals="+charLiterals);
	System.out.println("stringLiterals="+stringLiterals);
	System.out.println("tokens="+tokens);
	System.out.println("aliases="+aliases);

	assignTokenNameTypes();

	aliasTokenNamesAndLiterals();

	assignCharTypes();

	assignStringTypes();

	System.out.println("AFTER:");
	System.out.println("charLiterals="+charLiterals);
	System.out.println("stringLiterals="+stringLiterals);
	System.out.println("tokens="+tokens);
	System.out.println("aliases="+aliases);

	notifyGrammarObject();
}

	protected void assignStringTypes() {
		// walk string literals assigning types to unassigned ones
		Set s = stringLiterals.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String lit = (String) it.next();
			System.out.println("lit="+lit);
			Integer oldTypeI = (Integer)stringLiterals.get(lit);
			int oldType = oldTypeI.intValue();
			if ( oldType<Label.MIN_TOKEN_TYPE ) {
				Integer typeI = new Integer(getNewTokenType());
				stringLiterals.put(lit, typeI);
				if ( oldTypeI == UNASSIGNED_IN_PARSER_RULE ) {
					grammar.defineLexerRuleForStringLiteral(lit, typeI.intValue());
				}
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

	protected void aliasTokenNamesAndLiterals() {
		Set s;
		// walk aliases if any and assign types to aliased literals
		s = aliases.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String tokenName = (String) it.next();
			String literal = (String)aliases.get(tokenName);
			if ( literal.charAt(0)=='"' ) {
				stringLiterals.put(literal, tokens.get(tokenName));
			}
			else if ( literal.charAt(0)=='\'' ) {
				charLiterals.put(literal, tokens.get(tokenName));
			}
		}
	}

	protected void assignTokenNameTypes() {
		// walk token names, assigning values if unassigned
		Set s = tokens.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String tokenName = (String) it.next();
			if ( tokens.get(tokenName)==UNASSIGNED ) {
				tokens.put(tokenName, new Integer(getNewTokenType()));
			}
		}
	}

	protected void notifyGrammarObject() {
		Set s = tokens.keySet();
		for (Iterator it = s.iterator(); it.hasNext();) {
			String tokenName = (String) it.next();
			int ttype = ((Integer)tokens.get(tokenName)).intValue();
			grammar.defineToken(tokenName, ttype);
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
				System.out.println("import "+tokenName+"="+tokenType);
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
			this.tokenType = maxTokenType+1; // next type is defined above imported
		}
	}
}

grammar[Grammar g]
{this.grammar = g;}
    :   (headerSpec)*
	    ( #( LEXER_GRAMMAR 	  {grammar.setType(Grammar.LEXER);} 	  grammarSpec )
	    | #( PARSER_GRAMMAR   {grammar.setType(Grammar.PARSER);}      grammarSpec )
	    | #( TREE_GRAMMAR     {grammar.setType(Grammar.TREE_PARSER);} grammarSpec )
	    | #( COMBINED_GRAMMAR {grammar.setType(Grammar.COMBINED);}    grammarSpec )
	    )
        {assignTypes();}
    ;

headerSpec
    :   #( "header" a:ACTION )
    ;

grammarSpec
{Map opts=null;}
	:	id:ID {grammar.setName(#id.getText());}
		(cmt:DOC_COMMENT)?
        (opts=optionsSpec {grammar.setOptions(opts);})?
        (tokensSpec)?
        rules
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
        if ( key.equals("tokenVocab") ) {
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
    :   #( RULE id:ID {currentRuleName=#id.getText();} (optionsSpec)? (modifier)?
           b:block EOR {trackTokenRule(id,b);}
         )
    ;

modifier
	:	"protected"
	|	"public"
	|	"private"
	|	"fragment"
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
    |   ebnf
    |   tree
    |   #( SYNPRED block ) 
    |   ACTION
    |	lexer_action
    |   SEMPRED
    |   EPSILON 
    ;

lexer_action
	:	#( LEXER_ACTION (lexer_assignment)+ )
	;

lexer_assignment
	:	#( ASSIGN ID lexer_expr )
	;

lexer_expr
	:	INT
	|	ID
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
