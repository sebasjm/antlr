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
protected String currentRuleName;
protected int outerAltNum = 0;
protected int blockLevel = 0;

	/** Parser error-reporting function can be overridden in subclass */
	public void reportError(RecognitionException ex) {
		System.out.println("define rules: "+ex.toString());
	}

	/** Parser error-reporting function can be overridden in subclass */
	public void reportError(String s) {
		System.out.println("define rules: error: " + s);
	}

	protected void finish() {
		trimGrammar();
	}

	/** Remove any lexer rules from a COMBINED; already passed to lexer */
	protected void trimGrammar() {
		if ( grammar.type!=Grammar.COMBINED ) {
			return;
		}
		// form is (header ... ) ( grammar ID (scope ...) ... ( rule ... ) ( rule ... ) ... )
		GrammarAST p = root;
		// find the grammar spec
		while ( !p.getText().equals("grammar") ) {
			p = (GrammarAST)p.getNextSibling();
		}
		p = (GrammarAST)p.getFirstChild(); // jump down to first child of grammar
		// look for first RULE def
		GrammarAST prev = p; // points to the ID (grammar name)
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
	    ( #( LEXER_GRAMMAR 	  {grammar.type = Grammar.LEXER;} 	    grammarSpec )
	    | #( PARSER_GRAMMAR   {grammar.type = Grammar.PARSER;}      grammarSpec )
	    | #( TREE_GRAMMAR     {grammar.type = Grammar.TREE_PARSER;} grammarSpec )
	    | #( COMBINED_GRAMMAR {grammar.type = Grammar.COMBINED;}    grammarSpec )
	    )
	    {finish();}
    ;

headerSpec
    :   #( "header" (name:ID)? a:ACTION
        {grammar.defineGrammarHeader(#name, #a);} )
    ;

attrScope
	:	#( "scope" name:ID attrs:ACTION )
		{
		AttributeScope scope = grammar.defineGlobalScope(name.getText());
		scope.isDynamicGlobalScope = true;
		scope.addAttributes(attrs.getText(), ";");
		}
	;

grammarSpec
{Map opts=null;}
	:	id:ID
		(cmt:DOC_COMMENT)?
        (#(OPTIONS .))? // already parsed these in assign.types.g
        (tokensSpec)?
        (attrScope)*
        (ACTION)?
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
    :   #( RULE id:ID
           (mod=modifier)?
           #( ARG (args:ARG_ACTION)? )
           #( RET (ret:ARG_ACTION)? )
           (opts=optionsSpec)?
			{
			name = #id.getText();
			currentRuleName = name;
			Rule r = null;
			if ( Character.isUpperCase(name.charAt(0)) &&
				 grammar.type==Grammar.COMBINED )
			{
				// a merged grammar spec, track lexer rules and send to another grammar
				grammar.defineLexerRuleFoundInParser(#id.getToken(), #rule);
			}
			else {
				int numAlts = countAltsForRule(#rule);
				grammar.defineRule(#id.getToken(), mod, opts, #rule, #args, numAlts);
				r = grammar.getRule(name);
				if ( #args!=null ) {
					r.parameterScope = grammar.createParameterScope(name);
					r.parameterScope.addAttributes(#args.getText(), ",");
				}
				if ( #ret!=null ) {
					r.returnScope = grammar.createReturnScope(name);
					r.returnScope.addAttributes(#ret.getText(), ",");
				}
			}
			}
           (ruleScopeSpec[r])?
           #( INITACTION (ACTION)? )
           {this.blockLevel=0;}
           b:block EOR
           {#b.setOptions(opts);}
         )
    ;

countAltsForRule returns [int n=0]
    :   #( RULE id:ID (modifier)? ARG RET (OPTIONS)? ("scope")? INITACTION
           #(  BLOCK (OPTIONS)? (ALT {n++;})+ EOB )
           EOR
         )
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

ruleScopeSpec[Rule r]
 	:	#( "scope"
 	       ( attrs:ACTION
 	         {
 	         r.ruleScope = grammar.createRuleScope(r.name);
			 r.ruleScope.isDynamicRuleScope = true;
			 r.ruleScope.addAttributes(#attrs.getText(), ";");
			 }
		   )?
 	       ( uses:ID
 	         {
 	         if ( r.useScopes==null ) {r.useScopes=new ArrayList();}
 	         r.useScopes.add(#uses.getText());
 	         }
 	       )*
 	     )
 	;

block
{
Map opts=null;
this.blockLevel++;
if ( this.blockLevel==1 ) {this.outerAltNum=1;}
}
    :   #(  BLOCK
            (opts=optionsSpec {#block.setOptions(opts);})?
            (alternative {if ( this.blockLevel==1 ) {this.outerAltNum++;}})+
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
    |	#(ASSIGN id:ID a:atom )
    	{
    	if ( #a.getType()==RULE_REF ) {
    		grammar.defineRuleRefLabel(currentRuleName,#id.getToken(),a);
    	}
    	else if ( #a.getType()!=CHAR_RANGE ) {
    		grammar.defineTokenRefLabel(currentRuleName,#id.getToken(),a);
    	}
    	}
    |	#(	PLUS_ASSIGN id2:ID a2:atom
    	    {grammar.defineListLabel(currentRuleName,#id2.getToken(),#a2);}
         )
    |   ebnf
    |   tree
    |   #( SYNPRED block )
    |   act:ACTION {#act.outerAltNum = this.outerAltNum;}
    |   SEMPRED
    |   EPSILON 
    ;

ebnf:   block
    |   #( OPTIONAL block )
    |   #( CLOSURE block )
    |   #( POSITIVE_CLOSURE block )
    ;

tree:   #(TREE_BEGIN atom (element)*)
    ;

atom
    :   #( r:RULE_REF (rarg:ARG_ACTION)? )
    	{
    	grammar.altReferencesRule(currentRuleName, #r, this.outerAltNum);
    	}
    |   #( t:TOKEN_REF (targ:ARG_ACTION)? )
    	{
    	grammar.altReferencesToken(currentRuleName, #t, this.outerAltNum);
    	}
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
