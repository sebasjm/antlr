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
            "define: "+ex.toString(),
            ex);
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
    :   ( #( LEXER_GRAMMAR 	  {grammar.type = Grammar.LEXER;} 	    grammarSpec )
	    | #( PARSER_GRAMMAR   {grammar.type = Grammar.PARSER;}      grammarSpec )
	    | #( TREE_GRAMMAR     {grammar.type = Grammar.TREE_PARSER;} grammarSpec )
	    | #( COMBINED_GRAMMAR {grammar.type = Grammar.COMBINED;}    grammarSpec )
	    )
	    {finish();}
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
{
Map opts=null;
Token optionsStartToken=null;
}
	:	id:ID
		(cmt:DOC_COMMENT)?
        //(#(OPTIONS .))? // already parsed these in assign.types.g
        ( {optionsStartToken=((GrammarAST)_t).getToken();}
          opts=optionsSpec {grammar.setOptions(opts, optionsStartToken);}
        )?
        (tokensSpec)?
        (attrScope)*
        (actions)?
        rules
	;

actions
	:	( action )+
	;

action
{
String scope=null;
GrammarAST nameAST=null, actionAST=null;
}
	:	#(amp:AMPERSAND id1:ID
			( id2:ID a1:ACTION
			  {scope=#id1.getText(); nameAST=#id2; actionAST=#a1;}
			| a2:ACTION
			  {scope=null; nameAST=#id1; actionAST=#a2;}
			)
		 )
		 {
		 grammar.defineAction(#amp,scope,nameAST,actionAST);
		 }
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
Rule r = null;
}
    :   #( RULE id:ID
           (mod=modifier)?
           #( ARG (args:ARG_ACTION)? )
           #( RET (ret:ARG_ACTION)? )
           (opts=optionsSpec)?
			{
			name = #id.getText();
			currentRuleName = name;
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
		   (ruleAction[r])*
           {this.blockLevel=0;}
           b:block
           (exceptionGroup)?
           EOR
           {
           // copy rule options into the block AST, which is where
           // the analysis will look for k option etc...
           #b.options = opts;
           }
         )
    ;

countAltsForRule returns [int n=0]
    :   #( RULE id:ID (modifier)? ARG RET (OPTIONS)? ("scope")? (AMPERSAND)*
           #(  BLOCK (OPTIONS)? (ALT (REWRITE)* {n++;})+ EOB )
           (exceptionGroup)?
           EOR
         )
	;

ruleAction[Rule r]
	:	#(amp:AMPERSAND id:ID a:ACTION ) {if (r!=null) r.defineAction(#amp,#id,#a);}
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
            (opts=optionsSpec {#block.setOptions(grammar,opts);})?
            (blockAction)*
            ( alternative rewrite
              {if ( this.blockLevel==1 ) {this.outerAltNum++;}}
            )+
            EOB
         )
    ;

// TODO: this does nothing now! subrules cannot have init actions. :(
blockAction
	:	#(amp:AMPERSAND id:ID a:ACTION ) // {r.defineAction(#amp,#id,#a);}
	;

alternative
{
if ( grammar.type!=Grammar.LEXER && grammar.getOption("output")!=null && blockLevel==1 ) {
	GrammarAST aRewriteNode = #alternative.findFirstType(REWRITE);
	if ( aRewriteNode!=null||
		 (#alternative.getNextSibling()!=null &&
		  #alternative.getNextSibling().getType()==REWRITE) )
	{
		Rule r = grammar.getRule(currentRuleName);
		r.trackAltsWithRewrites(this.outerAltNum);
	}
}
}
    :   #( ALT (element)+ EOA )
    ;

exceptionGroup
	:	( exceptionSpec )+
    ;

exceptionSpec
    :   #("exception" ( ARG_ACTION )? ( exceptionHandler )+ )
    ;

exceptionHandler
    :    #("catch" ARG_ACTION ACTION)
    ;

rewrite
	:	( #( REWRITE (SEMPRED)? (ALT|TEMPLATE|ACTION) ) )*
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
    	    {
    	    if ( #a2.getType()==RULE_REF ) {
    	    	grammar.defineRuleListLabel(currentRuleName,#id2.getToken(),#a2);
    	    }
    	    else {
    	    	grammar.defineTokenListLabel(currentRuleName,#id2.getToken(),#a2);
    	    }
    	    }
         )
    |   ebnf
    |   tree
    |   #( SYNPRED block )
    |   act:ACTION
    |   SEMPRED
    |   SYN_SEMPRED
    |   GATED_SEMPRED
    |   EPSILON 
    ;

ebnf:   (dotLoop)=> dotLoop // .* or .+
    |   block
    |   #( OPTIONAL block )
    |   #( CLOSURE block )
    |   #( POSITIVE_CLOSURE block )
    ;

/** Track the .* and .+ idioms and make them greedy by default.
 *  If someone specifies an option, it won't match these
 */
dotLoop
{
    GrammarAST block = (GrammarAST)#dotLoop.getFirstChild();
}
    :   (   #( CLOSURE dotBlock )           
        |   #( POSITIVE_CLOSURE dotBlock )
        )
        {
        Map opts=new HashMap();
        opts.put("greedy", "false");
        if ( grammar.type!=Grammar.LEXER ) {
            // parser grammars assume k=1 for .* loops
            // otherwise they look til EOF!
            opts.put("k", new Integer(1));
        }
        block.setOptions(grammar,opts);
        }
    ;

dotBlock
    :   #( BLOCK #( ALT WILDCARD EOA ) EOB )
    ;

tree:   #(TREE_BEGIN element (element)*)
    ;

atom
    :   r:RULE_REF
    	{grammar.altReferencesRule(currentRuleName, #r, this.outerAltNum);}
    |   t:TOKEN_REF
    	{
    	if ( grammar.type==Grammar.LEXER ) {
    		grammar.altReferencesRule(currentRuleName, #t, this.outerAltNum);
    	}
    	else {
    		grammar.altReferencesTokenID(currentRuleName, #t, this.outerAltNum);
    	}
    	}
    |   c:CHAR_LITERAL
    	{
    	if ( grammar.type!=Grammar.LEXER ) {
    		Rule rule = grammar.getRule(currentRuleName);
			if ( rule!=null ) {
				rule.trackTokenReferenceInAlt(#c, outerAltNum);
    		}
    	}
    	}
    |   s:STRING_LITERAL
    	{
    	if ( grammar.type!=Grammar.LEXER ) {
    		Rule rule = grammar.getRule(currentRuleName);
			if ( rule!=null ) {
				rule.trackTokenReferenceInAlt(#s, outerAltNum);
    		}
    	}
    	}
    |   WILDCARD
    |	set
    ;

ast_suffix
	:	ROOT
	|	RULEROOT
	|	BANG
	;

set :   #(SET (setElement)+ (ast_suffix)? )
    ;

setElement
    :   c:CHAR_LITERAL
    |   t:TOKEN_REF
    |   s:STRING_LITERAL
    |	#(CHAR_RANGE c1:CHAR_LITERAL c2:CHAR_LITERAL)
    ;
