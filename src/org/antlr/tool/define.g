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

class DefineGrammarItemsWalker extends TreeParser;

options {
	importVocab = ANTLR;
	ASTLabelType = "GrammarAST";
    codeGenBitsetTestThreshold=999;
}

{
protected Grammar grammar;
protected GrammarAST root;
protected List lexerRules = new LinkedList();

	/** Parser error-reporting function can be overridden in subclass */
	public void reportError(RecognitionException ex) {
		System.out.println("define rules: "+ex.toString());
	}

	/** Parser error-reporting function can be overridden in subclass */
	public void reportError(String s) {
		System.out.println("define rules: error: " + s);
	}

	/** Remove any lexer rules from a COMBINED; already passed to lexer */
	protected void trimGrammar() {
		if ( grammar.type!=Grammar.COMBINED ) {
			return;
		}
		// form is ( grammar ID ... ( rule ... ) ( rule ... ) ... )
		GrammarAST p = (GrammarAST)root.getFirstChild();
		// look for first RULE def
		GrammarAST prev = p; // points to the ID (grammar name)
		p = (GrammarAST)p.getNextSibling(); // either first rule or options etc ...
		while ( p.getType()!=RULE ) {
			prev = p;
			p = (GrammarAST)p.getNextSibling();
		}
		// prev points at last node before first rule subtree at this point
		while ( p!=null ) {
			String ruleName = p.getFirstChild().getText();
			//System.out.println("rule "+ruleName+" prev="+prev.getText());
			if ( Character.isUpperCase(ruleName.charAt(0)) ) {
				// remove lexer rule
				prev.setNextSibling(p.getNextSibling());
			}
			else {
				prev = p; // non-lexer rule; move on
			}
			p = (GrammarAST)p.getNextSibling();
		}
		//System.out.println("root after removal is: "+root.toStringList());
	}
}

grammar[Grammar g]
{
grammar = g;
root = #grammar;
}
    :   (headerSpec)*
	    ( #( LEXER_GRAMMAR 	  {grammar.type = Grammar.LEXER;} 	  grammarSpec )
	    | #( PARSER_GRAMMAR   {grammar.type = Grammar.PARSER;}      grammarSpec )
	    | #( TREE_GRAMMAR     {grammar.type = Grammar.TREE_PARSER;} grammarSpec )
	    | #( COMBINED_GRAMMAR {grammar.type = Grammar.COMBINED;}    grammarSpec )
	    )
	    {trimGrammar();}
    ;

headerSpec
    :   #( "header" a:ACTION )
    ;

grammarSpec
{Map opts=null;}
	:	id:ID
		(cmt:DOC_COMMENT)?
        (#(OPTIONS .))? // already parsed these in assign.types.g
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
        {opts.put(key,value);}
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
	:	t:TOKEN_REF
	|	#( ASSIGN
		   t2:TOKEN_REF
		   ( s:STRING_LITERAL
		   | c:CHAR_LITERAL
		   )
		 )
	;

rules
    :   ( rule )+
    ;

rule
{
String mod=null;
String name=null;
Map opts=null;
}
    :   #( RULE id:ID (mod=modifier)? (opts=optionsSpec)? b:block EOR )
		{
		name = #id.getText();
		if ( Character.isUpperCase(name.charAt(0)) &&
			 grammar.type==Grammar.COMBINED )
		{
			// a merged grammar spec, track lexer rules and send to another grammar
			//System.out.println("rule tree is:\n"+#rule.toStringTree());
			ANTLRTreePrinter printer = new ANTLRTreePrinter();
			printer.setASTNodeClass("org.antlr.tool.GrammarAST");
			String ruleText = printer.toString(#rule);
			//System.out.println("rule text is:\n"+ruleText);
			grammar.defineLexerRuleFoundInParser(name, ruleText);
			lexerRules.add(#rule); // track rules to remove from parser later
		}
		else {
			grammar.defineRule(#id.getToken(), mod, opts, #rule);
		}
		}
    ;

modifier returns [String mod]
{
mod = #modifier.getText();
}
	:	"protected"
	|	"public"
	|	"private"
	|	"fragment"
	;

block
{Map opts=null;}
    :   #(  BLOCK
            (opts=optionsSpec {#block.setOptions(opts);})?
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
    |   t:TOKEN_REF
    |   c:CHAR_LITERAL
    |   s:STRING_LITERAL
    |   WILDCARD
    |	set
    ;

set :   #(SET (setElement)+)
    ;

setElement
    :   c:CHAR_LITERAL
    |   t:TOKEN_REF
    |   s:STRING_LITERAL
    |	#(CHAR_RANGE c1:CHAR_LITERAL c2:CHAR_LITERAL)
    ;
