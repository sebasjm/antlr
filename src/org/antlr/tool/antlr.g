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
import java.io.*;
import org.antlr.analysis.*;
import org.antlr.misc.*;
import antlr.*;
}

/** Read in an ANTLR grammar and build an AST.  Try not to do
 *  any actions, just build the tree.
 *
 *  The phases are:
 *
 *		antlr.g (this file)
 *		assign.types.g
 *		define.g
 *		buildnfa.g
 *		antlr.print.g (optional)
 *		codegen.g
 *
 *  Terence Parr
 *  University of San Francisco
 *  2005
 */
class ANTLRParser extends Parser;
options {
    buildAST = true;
	exportVocab=ANTLR;
    ASTLabelType="GrammarAST";
	k=2;
}

tokens {
	OPTIONS="options";
	TOKENS="tokens";
	PARSER="parser";
	
    LEXER;
    RULE;
    BLOCK;
    OPTIONAL;
    CLOSURE;
    POSITIVE_CLOSURE;
    SYNPRED;
    RANGE;
    CHAR_RANGE;
    EPSILON;
    ALT;
    EOR;
    EOB;
    EOA; // end of alt
    CHARSET;
    SET;
    ID;
    ARG;
    ARGLIST;
    RET;
    LEXER_GRAMMAR;
    PARSER_GRAMMAR;
    TREE_GRAMMAR;
    COMBINED_GRAMMAR;
    INITACTION;
    LABEL; // $x used in rewrite rules
    TEMPLATE;
    SCOPE="scope";
    GATED_SEMPRED; // {p}? =>
    SYN_SEMPRED; // (...) =>   it's a synpred converted to sempred
}

{
	Grammar grammar = null;
	protected int gtype = 0;
	protected String currentRuleName = null;

	protected GrammarAST setToBlockWithSet(GrammarAST b) {
		return #(#[BLOCK,"BLOCK"],
		           #(#[ALT,"ALT"],
		              #b,#[EOA,"<end-of-alt>"]
		            ),
		           #[EOB,"<end-of-block>"]
		        );
	}

	public GrammarAST createSimpleRuleAST(String name, GrammarAST block) {
		GrammarAST ruleAST =
		   #([RULE,"rule"],
                 [ID,name],[ARG,"ARG"],[RET,"RET"],
				 [SCOPE,"scope"],block,[EOR,"<end-of-rule>"]);
		ruleAST.setLine(block.getLine());
		ruleAST.setColumn(block.getColumn());
		return ruleAST;
	}

    public void reportError(RecognitionException ex) {
		Token token = null;
		try {
			token = LT(1);
		}
		catch (TokenStreamException tse) {
			ErrorManager.internalError("can't get token???", tse);
		}
		ErrorManager.syntaxError(
			ErrorManager.MSG_SYNTAX_ERROR,
			token,
			"antlr: "+ex.toString(),
			ex);
    }
}

grammar![Grammar g]
{
	this.grammar = g;
}
   :    //hdr:headerSpec
        ( ACTION )?
	    ( cmt:DOC_COMMENT  )?
        gr:grammarType gid:id SEMI
		    (opt:optionsSpec)?
		    (ts:tokensSpec!)?
        	scopes:attrScopes
		    (a:actions)?
	        r:rules
        EOF
        {
        #grammar = #(null, #(#gr, #gid, #cmt, #opt, #ts, #scopes, #a, #r));
        }
	;

grammarType
    :   (	"lexer"!  {gtype=LEXER_GRAMMAR;}    // pure lexer
    	|   "parser"! {gtype=PARSER_GRAMMAR;}   // pure parser
    	|   "tree"!   {gtype=TREE_GRAMMAR;}     // a tree parser
    	|			  {gtype=COMBINED_GRAMMAR;} // merged parser/lexer
    	)
    	gr:"grammar" {#gr.setType(gtype);}
    ;

actions
	:	(action)+
	;

/** Match stuff like @parser::members {int i;} */
action
	:	AMPERSAND^ (actionScopeName COLON! COLON!)? id ACTION
	;

/** Sometimes the scope names will collide with keywords; allow them as
 *  ids for action scopes.
 */
actionScopeName
	:	id
	|	l:"lexer"	{#l.setType(ID);}
    |   p:"parser"	{#p.setType(ID);}
	;

optionsSpec
	:	OPTIONS^ (option SEMI!)+ RCURLY!
	;

option
    :   id ASSIGN^ optionValue
    ;

optionValue
	:	id
	|   STRING_LITERAL
	|	CHAR_LITERAL
	|	INT
//	|   cs:charSet       {value = #cs;} // return set AST in this case
	;

/*
will probably need for char vocab spec later
charSet
	:   LPAREN^ {#LPAREN.setType(CHARSET);}
	        charSetElement ( OR^ charSetElement )*
	    RPAREN!
	;

charSetElement
	:   c1:CHAR_LITERAL
	|   c2:CHAR_LITERAL RANGE^ c3:CHAR_LITERAL
	;
*/

tokensSpec
	:	TOKENS^
			( tokenSpec	)+
		RCURLY!
	;

tokenSpec
	:	TOKEN_REF ( ASSIGN^ (STRING_LITERAL|CHAR_LITERAL) )? SEMI!
	;

attrScopes
	:	(attrScope)*
	;

attrScope
	:	"scope"^ id ACTION
	;

rules
    :   (
			options {
				// limitation of appox LL(k) says ambig upon
				// DOC_COMMENT TOKEN_REF, but that's an impossible sequence
				warnWhenFollowAmbig=false;
			}
		:	//{g.type==PARSER}? (aliasLexerRule)=>aliasLexerRule |
			rule
		)+
    ;

rule!
{
GrammarAST modifier=null, blk=null, blkRoot=null, eob=null;
}
	:
	(	d:DOC_COMMENT	
	)?
	(	p1:"protected"	{modifier=#p1;}
	|	p2:"public"		{modifier=#p2;}
	|	p3:"private"    {modifier=#p3;}
	|	p4:"fragment"	{modifier=#p4;}
	)?
	ruleName:id
	{currentRuleName=#ruleName.getText();}
	( BANG  )?
	( aa:ARG_ACTION )?
	( "returns" rt:ARG_ACTION  )?
	( throwsSpec )?
	( opts:optionsSpec )?
	scopes:ruleScopeSpec
	(a:ruleActions)?
	colon:COLON
	{
	blkRoot = #[BLOCK,"BLOCK"];
	blkRoot.setLine(colon.getLine());
	blkRoot.setColumn(colon.getColumn());
	eob = #[EOB,"<end-of-block>"];
    }
	(	(setNoParens SEMI) => s:setNoParens // try to collapse sets
		{
		blk = #(blkRoot,#(#[ALT,"ALT"],#s,#[EOA,"<end-of-alt>"]),eob);
		}

	|	b:altList {blk = #b;}
	)
	semi:SEMI
	( exceptionGroup )?
    {
	eob.setLine(semi.getLine());
	eob.setColumn(semi.getColumn());
    GrammarAST eor = #[EOR,"<end-of-rule>"];
   	eor.setEnclosingRule(#ruleName.getText());
	eor.setLine(semi.getLine());
	eor.setColumn(semi.getColumn());
    #rule = #(#[RULE,"rule"],
              #ruleName,modifier,#(#[ARG,"ARG"],#aa),#(#[RET,"RET"],#rt),
              #opts,#scopes,#a,blk,eor);
    }
	;

ruleActions
	:	(ruleAction)+
	;

/** Match stuff like @init {int i;} */
ruleAction
	:	AMPERSAND^ id ACTION
	;

throwsSpec
	:	"throws" id ( COMMA id )*
		
	;

ruleScopeSpec
{
int line = LT(1).getLine();
int column = LT(1).getColumn();
}
	:!	( options {warnWhenFollowAmbig=false;} : "scope" a:ACTION )?
		( "scope" ids:idList SEMI! )*
		{
		GrammarAST scopeRoot = (GrammarAST)#[SCOPE,"scope"];
		scopeRoot.setLine(line);
		scopeRoot.setColumn(column);
		#ruleScopeSpec = #(scopeRoot, #a, #ids);
		}
	;

/** Build #(BLOCK ( #(ALT ...) EOB )+ ) */
block
    :   (set) => s:set  // special block like ('a'|'b'|'0'..'9')

    |	lp:LPAREN^ {#lp.setType(BLOCK); #lp.setText("BLOCK");}
		(
			// 2nd alt and optional branch ambig due to
			// linear approx LL(2) issue.  COLON ACTION
			// matched correctly in 2nd alt.
			options {
				warnWhenFollowAmbig = false;
			}
		:
			optionsSpec ( ruleActions )? COLON!
		|	ACTION COLON!
		)?

		a1:alternative rewrite ( OR! a2:alternative rewrite )*

        RPAREN!
        {
        GrammarAST eob = #[EOB,"<end-of-block>"];
        eob.setLine(lp.getLine());
        eob.setColumn(lp.getColumn());
        #block.addChild(eob);
        }
    ;

altList
{
	GrammarAST blkRoot = #[BLOCK,"BLOCK"];
	blkRoot.setLine(LT(1).getLine());
	blkRoot.setColumn(LT(1).getColumn());
}
    :   a1:alternative rewrite ( OR! a2:alternative rewrite )*
        {
        #altList = #(blkRoot,#altList,#[EOB,"<end-of-block>"]);
        }
    ;

alternative
{
    GrammarAST eoa = #[EOA, "<end-of-alt>"];
    GrammarAST altRoot = #[ALT,"ALT"];
    altRoot.setLine(LT(1).getLine());
    altRoot.setColumn(LT(1).getColumn());
}
    :   (BANG!)? ( el:element )+ ( exceptionSpecNoLabel! )?
        {
            if ( #alternative==null ) {
                #alternative = #(altRoot,#[EPSILON,"epsilon"],eoa);
            }
            else {
                #alternative = #(altRoot, #alternative,eoa);
            }
        }
    |   {#alternative = #(altRoot,#[EPSILON,"epsilon"],eoa);}
    ;

exceptionGroup
	:	( exceptionSpec )+
    ;

exceptionSpec
    :   "exception" ( ARG_ACTION  )?
        ( exceptionHandler )*
    ;

exceptionSpecNoLabel
    :   "exception" ( exceptionHandler )*
    ;

exceptionHandler
   :    "catch" ARG_ACTION ACTION
   ;

element
	:	elementNoOptionSpec //(elementOptionSpec!)?
	;

elementOptionSpec
	:	OPEN_ELEMENT_OPTION
		id ASSIGN optionValue
		(
			SEMI
			id ASSIGN optionValue
			
		)*
		CLOSE_ELEMENT_OPTION
	;

elementNoOptionSpec
{
    IntSet elements=null;
}
	:	(id ASSIGN^)?
		(   range
		|   terminal
		|	notSet
		|	ebnf
		)

    |   id PLUS_ASSIGN^ 
        (   terminal
		|	notSet
		|   ebnf
        )

	|   a:ACTION

	|   p:SEMPRED ( IMPLIES! {#p.setType(GATED_SEMPRED);} )?

	|   t3:tree
	;

notSet
{
    int line = LT(1).getLine();
    int col = LT(1).getColumn();
    GrammarAST subrule=null;
}
	:	n:NOT^
		(	notTerminal
        |   // special case: single element is not a set
            (LPAREN setElement RPAREN)=> LPAREN! setElement RPAREN! 
        |   set
		)
        ( subrule=ebnfSuffix[#n] {#notSet = subrule;} )?
        {#notSet.setLine(line); #notSet.setColumn(col);}
	;

/** Match two or more set elements */
set	:   LPAREN! s:setNoParens RPAREN!
    	( ast:ast_suffix! {#s.addChild(#ast);} )?
    ;

setNoParens
{Token startingToken = LT(1);}
    :   setElement (OR! setElement)+
        {
        GrammarAST ast = new GrammarAST();
		ast.initialize(new TokenWithIndex(SET, "SET"));
		((TokenWithIndex)ast.token)
			.setIndex(((TokenWithIndex)startingToken).getIndex());
        #setNoParens = #(ast, #setNoParens);
        }
    ;

setElement
    :   CHAR_LITERAL
    |   {gtype!=LEXER_GRAMMAR}? TOKEN_REF
    |   {gtype!=LEXER_GRAMMAR}? STRING_LITERAL
    |   range
    ;

tree :
	TREE_BEGIN^
        element ( element )+
    RPAREN!
	;

/** matches ENBF blocks (and sets via block rule) */
ebnf!
{
    int line = LT(1).getLine();
    int col = LT(1).getColumn();
}
	:	b:block
		(	(	QUESTION    {if (#b.getType()==SET) #b=setToBlockWithSet(#b);
							 #ebnf=#([OPTIONAL,"?"],#b);}
			|	STAR	    {if (#b.getType()==SET) #b=setToBlockWithSet(#b);
							 #ebnf=#([CLOSURE,"*"],#b);}
			|	PLUS	    {if (#b.getType()==SET) #b=setToBlockWithSet(#b);
							 #ebnf=#([POSITIVE_CLOSURE,"+"],#b);}
			)
			( BANG )?
		|   IMPLIES // syntactic predicate
			{
			// add grammar fragment to a list so we can make fake rules for them
			// later.
			String predName = grammar.defineSyntacticPredicate(#b,currentRuleName);
			// convert (alpha)=> into {synpredN}? where N is some pred count
			// during code gen we convert to function call with templates
			String synpredinvoke = predName;
			#ebnf = #[SYN_SEMPRED,synpredinvoke];
			}
        |   {#ebnf = #b;}
		)
		{#ebnf.setLine(line); #ebnf.setColumn(col);}
	;

range!
{
GrammarAST subrule=null, root=null;
}
	:	c1:CHAR_LITERAL RANGE c2:CHAR_LITERAL
		{
		GrammarAST r = #[CHAR_RANGE,".."];
		r.setLine(c1.getLine());
		r.setColumn(c1.getColumn());
		#range = #(r, #c1, #c2);
		root = #range;
		}
    	(subrule=ebnfSuffix[root] {#range=subrule;})?
	;

terminal
{
GrammarAST ebnfRoot=null, subrule=null;
}
    :   cl:CHAR_LITERAL^
    		(	subrule=ebnfSuffix[#cl] {#terminal=subrule;}
    		|	ast_suffix
    		)?

	|   tr:TOKEN_REF^
			( ARG_ACTION )?
			(	subrule=ebnfSuffix[#tr] {#terminal=subrule;}
			|	ast_suffix
			)?
			// Args are only valid for lexer rules

    |   rr:RULE_REF^
			( ARG_ACTION )?
			(	subrule=ebnfSuffix[#rr] {#terminal=subrule;}
			|	ast_suffix
			)?

	|   sl:STRING_LITERAL^
    		(	subrule=ebnfSuffix[#sl] {#terminal=subrule;}
			|	ast_suffix
			)?

	|   wi:WILDCARD^
    		(	subrule=ebnfSuffix[#wi] {#terminal=subrule;}
			|	ast_suffix
			)?
	;

ast_suffix
	:	ROOT
	|	RULEROOT
	|	BANG
	;

ebnfSuffix[GrammarAST elemAST] returns [GrammarAST subrule=null]
{
GrammarAST ebnfRoot=null;
}
	:!	(	QUESTION {ebnfRoot = #[OPTIONAL,"?"];}
   		|	STAR     {ebnfRoot = #[CLOSURE,"*"];}
   		|	PLUS     {ebnfRoot = #[POSITIVE_CLOSURE,"+"];}
   		)
    	{
       	ebnfRoot.setLine(elemAST.getLine());
       	ebnfRoot.setColumn(elemAST.getColumn());
    	GrammarAST blkRoot = #[BLOCK,"BLOCK"];
       	GrammarAST eob = #[EOB,"<end-of-block>"];
		eob.setLine(elemAST.getLine());
		eob.setColumn(elemAST.getColumn());
  		subrule =
  		     #(ebnfRoot,
  		       #(blkRoot,#(#[ALT,"ALT"],elemAST,#[EOA,"<end-of-alt>"]),
  		         eob)
  		      );
   		}
    ;

notTerminal
	:   cl:CHAR_LITERAL^ ( ast_suffix )? // bang could be "no char" in lexer

	|	tr:TOKEN_REF^ (ast_suffix)?

	|	STRING_LITERAL (ast_suffix)?
	;

idList
	:	(id)+
	;

id	:	TOKEN_REF {#id.setType(ID);}
	|	RULE_REF  {#id.setType(ID);}
	;

/** Match anything that looks like an ID and return tree as token type ID */
idToken
    :	TOKEN_REF {#idToken.setType(ID);}
	|	RULE_REF  {#idToken.setType(ID);}
	;

// R E W R I T E  S Y N T A X

rewrite
{
    GrammarAST root = new GrammarAST();
}
	:!	( options { warnWhenFollowAmbig=false;}
		: rew:REWRITE pred:SEMPRED alt:rewrite_alternative
	      {root.addChild( #(#rew, #pred, #alt) );}
	    )*
		rew2:REWRITE alt2:rewrite_alternative
        {
        root.addChild( #(#rew2, #alt2) );
        #rewrite = (GrammarAST)root.getFirstChild();
        }
	|
	;

// DOESNT DO SETS
rewrite_block
    :   lp:LPAREN^ {#lp.setType(BLOCK); #lp.setText("BLOCK");}
		rewrite_alternative
        RPAREN!
        {
        GrammarAST eob = #[EOB,"<end-of-block>"];
        eob.setLine(lp.getLine());
        eob.setColumn(lp.getColumn());
        #rewrite_block.addChild(eob);
        }
    ;

rewrite_alternative
{
    GrammarAST eoa = #[EOA, "<end-of-alt>"];
    GrammarAST altRoot = #[ALT,"ALT"];
    altRoot.setLine(LT(1).getLine());
    altRoot.setColumn(LT(1).getColumn());
}
    :	( rewrite_template )=> rewrite_template

    |	( rewrite_element )+
        {
            if ( #rewrite_alternative==null ) {
                #rewrite_alternative = #(altRoot,#[EPSILON,"epsilon"],eoa);
            }
            else {
                #rewrite_alternative = #(altRoot, #rewrite_alternative,eoa);
            }
        }

   	|   {#rewrite_alternative = #(altRoot,#[EPSILON,"epsilon"],eoa);}
    ;

rewrite_element
{
GrammarAST subrule=null;
}
	:	t:rewrite_terminal
    	( subrule=ebnfSuffix[#t] {#rewrite_element=subrule;} )?
	|   rewrite_ebnf
//	|   a:ACTION
	|   tr:rewrite_tree
    	( subrule=ebnfSuffix[#tr] {#rewrite_element=subrule;} )?
	;

rewrite_terminal
{
GrammarAST subrule=null;
}
    :   cl:CHAR_LITERAL
	|   tr:TOKEN_REF^ (ARG_ACTION)? // for imaginary nodes
    |   rr:RULE_REF
	|   sl:STRING_LITERAL
	|!  d:DOLLAR i:id // reference to a label in a rewrite rule
		{
		#rewrite_terminal = #[LABEL,i_AST.getText()];
		#rewrite_terminal.setLine(#d.getLine());
		#rewrite_terminal.setColumn(#d.getColumn());
		}
	|	ACTION
	;

rewrite_ebnf!
{
    int line = LT(1).getLine();
    int col = LT(1).getColumn();
}
	:	b:rewrite_block
		(	QUESTION    {if (#b.getType()==SET) #b=setToBlockWithSet(#b);
						 #rewrite_ebnf=#([OPTIONAL,"?"],#b);}
		|	STAR	    {if (#b.getType()==SET) #b=setToBlockWithSet(#b);
						 #rewrite_ebnf=#([CLOSURE,"*"],#b);}
		|	PLUS	    {if (#b.getType()==SET) #b=setToBlockWithSet(#b);
						 #rewrite_ebnf=#([POSITIVE_CLOSURE,"+"],#b);}
		)
		{#rewrite_ebnf.setLine(line); #rewrite_ebnf.setColumn(col);}
	;

rewrite_tree :
	TREE_BEGIN^
        rewrite_terminal ( rewrite_element )*
    RPAREN!
	;

/** Build a tree for a template rewrite:
      ^(TEMPLATE ID ^(ARGLIST ^(ARG ID ACTION) ...) )
    where ARGLIST is always there even if no args exist
 */
rewrite_template
{Token st=null;}
	:   {LT(1).getText().equals("template")}?
		rewrite_template_head {st=LT(1);}
		( DOUBLE_QUOTE_STRING_LITERAL! | DOUBLE_ANGLE_STRING_LITERAL! )
		{#rewrite_template.addChild(#[st]);}
	|	rewrite_template_head
	|	ACTION
	;

rewrite_template_head
	:	id lp:LPAREN^ {#lp.setType(TEMPLATE); #lp.setText("TEMPLATE");}
		rewrite_template_args
		RPAREN!
	;

rewrite_template_args
	:	rewrite_template_arg (COMMA! rewrite_template_arg)*
		{#rewrite_template_args = #(#[ARGLIST,"ARGLIST"], rewrite_template_args);}
	|	{#rewrite_template_args = #[ARGLIST,"ARGLIST"];}
	;

rewrite_template_arg
	:   id ASSIGN^ ACTION
	;

class ANTLRLexer extends Lexer;
options {
	k=2;
	exportVocab=ANTLR;
	testLiterals=false;
	interactive=true;
	charVocabulary='\003'..'\377';
}

WS	:	(	' '
		|	'\t'
		|	('\r')? '\n' {newline();}
		)
	;

COMMENT :
	( SL_COMMENT | t:ML_COMMENT {$setType(t.getType());} )
	;

protected
SL_COMMENT :
	"//"
	( options {greedy=false;} : . )* '\n'
	{ newline(); }
	;

protected
ML_COMMENT :
	"/*"
	(	{ LA(2)!='/' }? '*' {$setType(DOC_COMMENT);}
	|
	)
	(
		/*	'\r' '\n' can be matched in one alternative or by matching
			'\r' and then in the next token.  The language
			that allows both "\r\n" and "\r" and "\n" to all be valid
			newline is ambiguous.  Consequently, the resulting grammar
			must be ambiguous.  I'm shutting this warning off.
		 */
		options {
			greedy=false;  // make it exit upon "*/"
			generateAmbigWarnings=false; // shut off newline errors
		}
	:	'\r' '\n'	{newline();}
	|	'\r'		{newline();}
	|	'\n'		{newline();}
	|	~('\n'|'\r')
	)*
	"*/"
	;

OPEN_ELEMENT_OPTION
	:	'<'
	;

CLOSE_ELEMENT_OPTION
	:	'>'
	;

AMPERSAND : '@';

COMMA : ',';

QUESTION :	'?' ;

TREE_BEGIN : "^(" ;

LPAREN:	'(' ;

RPAREN:	')' ;

COLON :	':' ;

STAR:	'*' ;

PLUS:	'+' ;

ASSIGN : '=' ;

PLUS_ASSIGN : "+=" ;

IMPLIES : "=>" ;

REWRITE : "->" ;

SEMI:	';' ;

ROOT : '^' ;

RULEROOT : "^^" ;

BANG : '!' ;

OR	:	'|' ;

WILDCARD : '.' ;

RANGE : ".." ;

NOT :	'~' ;

RCURLY:	'}'	;

DOLLAR : '$' ;

CHAR_LITERAL
	:	'\'' (ESC|~'\'')* '\''
		{
		StringBuffer s = Grammar.getUnescapedStringFromGrammarStringLiteral($getText);
		if ( s.length()>1 ) {
			$setType(STRING_LITERAL);
		}
		}
	;

DOUBLE_QUOTE_STRING_LITERAL
	:	'"' (ESC|~'"')* '"'
	;

DOUBLE_ANGLE_STRING_LITERAL
	:	"<<" (options {greedy=false;}:ESC|.)* ">>"
	;

protected
ESC	:	'\\'
		(	'n'
		|	'r'
		|	't'
		|	'b'
		|	'f'
		|	'"'
		|	'\''
		|	'\\'
		|	'>'
		|	('0'..'3')
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:
			('0'..'9')
				(
					options {
						warnWhenFollowAmbig = false;
					}
				:
				'0'..'9'
				)?
			)?
		|	('4'..'7')
			(
				options {
					warnWhenFollowAmbig = false;
				}
			:
			('0'..'9')
			)?
		|	'u' XDIGIT XDIGIT XDIGIT XDIGIT
		|	. // unknown, leave as it is
		)
	;

protected
DIGIT
	:	'0'..'9'
	;

protected
XDIGIT :
		'0' .. '9'
	|	'a' .. 'f'
	|	'A' .. 'F'
	;

INT	:	('0'..'9')+
	;

ARG_ACTION
   :
	NESTED_ARG_ACTION
	;

protected
NESTED_ARG_ACTION :
	'['!
	(
		/*	'\r' '\n' can be matched in one alternative or by matching
			'\r' and then '\n' in the next iteration.
		 */
		options {
			generateAmbigWarnings=false; // shut off newline errors
		}
	:	NESTED_ARG_ACTION
	|	'\r' '\n'	{newline();}
	|	'\r'		{newline();}
	|	'\n'		{newline();}
	|	ACTION_STRING_LITERAL
	|	~']'
	)*
	']'!
	;

ACTION
{int actionLine=getLine(); int actionColumn = getColumn(); }
	:	NESTED_ACTION
		(	'?'! {_ttype = SEMPRED;} )?
		{
			Token t = makeToken(_ttype);
			String action = $getText;
			action = action.substring(1,action.length()-1);
			t.setText(action);
			t.setLine(actionLine);			// set action line to start
			t.setColumn(actionColumn);
			$setToken(t);
		}
	;

protected
NESTED_ACTION :
	'{'
	(
		options {
			greedy = false; // exit upon '}'
		}
	:
		(
			options {
				generateAmbigWarnings = false; // shut off newline warning
			}
		:	'\r' '\n'	{newline();}
		|	'\r' 		{newline();}
		|	'\n'		{newline();}
		)
	|	NESTED_ACTION
	|	ACTION_CHAR_LITERAL
	|	COMMENT
	|	ACTION_STRING_LITERAL
	|	.
	)*
	'}'
   ;

protected
ACTION_CHAR_LITERAL
	:	'\'' (ACTION_ESC|~'\'')* '\''
	;

protected
ACTION_STRING_LITERAL
	:	'"' (ACTION_ESC|~'"')* '"'
	;

protected
ACTION_ESC
	:	"\\'"
	|	"\\\""
	|	'\\' ~('\''|'"')
	;

TOKEN_REF
options { testLiterals = true; }
	:	'A'..'Z'
		(	// scarf as many letters/numbers as you can
			options {
				warnWhenFollowAmbig=false;
			}
		:
			'a'..'z'|'A'..'Z'|'_'|'0'..'9'
		)*
	;

// we get a warning here when looking for options '{', but it works right
RULE_REF
{
	int t=0;
}
	:	t=INTERNAL_RULE_REF {_ttype=t;}
		(	{t==OPTIONS}? WS_LOOP ('{' {_ttype = OPTIONS;})?
		|	{t==TOKENS}? WS_LOOP ('{' {_ttype = TOKENS;})?
		|
		)
	;

protected
WS_LOOP
	:	(	// grab as much WS as you can
			options {
				greedy=true;
			}
		:
			WS
		|	COMMENT
		)*
	;

protected
INTERNAL_RULE_REF returns [int t]
{
	t = RULE_REF;
}
	:	'a'..'z'
		(	// scarf as many letters/numbers as you can
			options {
				warnWhenFollowAmbig=false;
			}
		:
			'a'..'z'|'A'..'Z'|'_'|'0'..'9'
		)*
		{t = testLiteralsTable(t);}
	;

protected
WS_OPT
	:	(WS)?
	;

