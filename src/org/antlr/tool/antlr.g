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
import java.io.*;
import org.antlr.analysis.*;
import org.antlr.misc.*;
import antlr.*;
}

/** Read in an ANTLR grammar and build an AST.  Try not to do
 *  any actions, just build the tree.
 *
 *  Terence Parr
 *  University of San Francisco
 *  2004
 */
class ANTLRParser extends Parser;
options {
    buildAST = true;
	exportVocab=ANTLR;
    ASTLabelType="GrammarAST";
	k=2;
}

tokens {
	"tokens";
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
    PARSER="parser";
    OPTIONS;
    CHARSET;
    SET;
    ID;
    ARG;
    RET;
    LEXER_GRAMMAR;
    PARSER_GRAMMAR;
    TREE_GRAMMAR;
    COMBINED_GRAMMAR;
    INITACTION;
}

{
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

    public void reportError(RecognitionException ex) {
        System.out.println("buildast: "+ex.toString());
        ex.printStackTrace(System.out);
    }

    public void reportError(String s) {
        System.out.println("buildast: error: " + s);
    }
}

grammar!
   :    hdr:headerSpec
        ( ACTION )?
	    ( cmt:DOC_COMMENT  )?
        gr:grammarType gid:id SEMI
		    (opt:optionsSpec)?
		    (ts:tokensSpec!)?
        	scopes:attrScopes
		    ( a:ACTION! )?
	        r:rules
        EOF
        {
        #grammar = #(null, #hdr, #(#gr, #gid, #cmt, #opt, #ts, #scopes, #a, #r));
        }
	;

headerSpec
    :   ( 	"header"^ (id)?
	 	    ACTION
	    )*
	;

grammarType
    :   (	"lexer"!  {gtype=LEXER_GRAMMAR;}    // pure lexer
    	|   "parser"! {gtype=PARSER_GRAMMAR;}   // pure parser
    	|   "tree"!   {gtype=TREE_GRAMMAR;}     // a tree parser
    	|			  {gtype=COMBINED_GRAMMAR;} // merged parser/lexer
    	)
    	gr:"grammar" {#gr.setType(gtype);}
    ;

/*
grammarOptionsSpec
    :   LPAREN!
            optionList
        RPAREN!
    ;

optionList
    :   option (COMMA! option)*
    	{#optionList = #(#[OPTIONS,"OPTIONS"], #optionList);}
    ;

    */

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

charSet
	:   LPAREN^ {#LPAREN.setType(CHARSET);}
	        charSetElement ( OR^ charSetElement )*
	    RPAREN!
	;

charSetElement
	:   c1:CHAR_LITERAL
	|   c2:CHAR_LITERAL RANGE^ c3:CHAR_LITERAL
	;

tokensSpec
	:	TOKENS^
			( tokenSpec	)+
		RCURLY!
	;

tokenSpec
	:	TOKEN_REF ( ASSIGN^ (STRING_LITERAL|CHAR_LITERAL) )? SEMI!
	;

/*
tokensSpecOptions
	:	OPEN_ELEMENT_OPTION
		id ASSIGN optionValue
		(
			SEMI id ASSIGN optionValue
		)*
		CLOSE_ELEMENT_OPTION
	;
*/

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
	( scopes:ruleScopeSpec )?
	( "init" init:ACTION )?
	colon:COLON
	{
	blkRoot = #[BLOCK,"BLOCK"];
	blkRoot.setLine(colon.getLine());
	blkRoot.setColumn(colon.getColumn());
	eob = #[EOB,"<end-of-block>"];
    }
	(	(setNoParens SEMI) => s:setNoParens
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
              #opts,#scopes,#(#[INITACTION,"INITACTION"],#init),blk,eor);
    }
	;

throwsSpec
	:	"throws" id ( COMMA id )*
		
	;

ruleScopeSpec
	:	"scope"^ ACTION ( ( COMMA! id )* SEMI! )?
	|	"scope"^ id ( COMMA! id )* SEMI!
	;

/** Build #(BLOCK ( #(ALT ...) EOB )+ ) */
block
    :   (set) => set  // special block like ('a'|'b'|'0'..'9')

    |	lp:LPAREN^ {#lp.setType(BLOCK); #lp.setText("BLOCK");}
		(
			// 2nd alt and optional branch ambig due to
			// linear approx LL(2) issue.  COLON ACTION
			// matched correctly in 2nd alt.
			options {
				warnWhenFollowAmbig = false;
			}
		:
			optionsSpec ( "init" ACTION )? COLON!
		|	ACTION COLON!
		)?

		a1:alternative ( OR! a2:alternative )*

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
    :   a1:alternative ( OR! a2:alternative )*
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

	|   p:SEMPRED

	|   t3:tree
	;

notSet
	:	NOT^
		(	notTerminal
        |   ebnf
		)
	;

/** Match two or more set elements */
set	:   LPAREN! setNoParens RPAREN!
    ;

setNoParens
{Token startingToken = LT(1);}
    :   {!currentRuleName.equals(Grammar.TOKEN_RULENAME)}?
    	setElement (OR! setElement)+
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
        rootNode
        ( element )+
    RPAREN!
	;

rootNode
	:   (id! COLON!)?
		terminal
	;

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
//		|   IMPLIES	        {#b.setType(SYNPRED); #ebnf=#b;}
        |                   {#ebnf = #b;}
		)
		{#ebnf.setLine(line); #ebnf.setColumn(col);}
	;

ast_type_spec
	:	(	CARET
		|	BANG 
		)?
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
    :   cl:CHAR_LITERAL ast_type_spec! (subrule=ebnfSuffix[#cl] {#terminal=subrule;})?

	|   tr:TOKEN_REF^ ast_type_spec! (subrule=ebnfSuffix[#tr] {#terminal=subrule;})?
		// Args are only valid for lexer rules
		( targ:ARG_ACTION )?

    |   rr:RULE_REF^ ast_type_spec! ( rarg:ARG_ACTION )?
    	(subrule=ebnfSuffix[#rr] {#terminal=subrule;})?

	|   sl:STRING_LITERAL
		ast_type_spec!
    	(subrule=ebnfSuffix[#sl] {#terminal=subrule;})?

	|   wi:WILDCARD ast_type_spec!
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
	:   cl:CHAR_LITERAL

		( BANG! )?
	|
		tr:TOKEN_REF ast_type_spec!
	;

id	:	TOKEN_REF {#id.setType(ID);}
	|	RULE_REF  {#id.setType(ID);}
	;

/** Match anything that looks like an ID and return tree as token type ID */
idToken
    :	TOKEN_REF {#idToken.setType(ID);}
	|	RULE_REF  {#idToken.setType(ID);}
	;

class ANTLRLexer extends Lexer;
options {
	k=2;
	exportVocab=ANTLR;
	testLiterals=false;
	interactive=true;
	charVocabulary='\003'..'\377';
}

tokens {
	"options";
}

{
	/**Convert 'c' to an integer char value. */
	// TODO move this to better location
	public static int escapeCharValue(String cs) {
		//System.out.println("escapeCharValue("+cs+")");
		if ( cs.charAt(1)!='\\' ) return 0;
		switch ( cs.charAt(2) ) {
		case 'b' : return '\b';
		case 'r' : return '\r';
		case 't' : return '\t';
		case 'n' : return '\n';
		case 'f' : return '\f';
		case '"' : return '\"';
		case '\'' :return '\'';
		case '\\' :return '\\';

		case 'u' :
			// Unicode char
			if (cs.length() != 8) {
				return 0;
			}
			else {
				return
					Character.digit(cs.charAt(3), 16) * 16 * 16 * 16 +
					Character.digit(cs.charAt(4), 16) * 16 * 16 +
					Character.digit(cs.charAt(5), 16) * 16 +
					Character.digit(cs.charAt(6), 16);
			}

		case '0' :
		case '1' :
		case '2' :
		case '3' :
			if ( cs.length()>5 && Character.isDigit(cs.charAt(4)) ) {
				return (cs.charAt(2)-'0')*8*8 + (cs.charAt(3)-'0')*8 + (cs.charAt(4)-'0');
			}
			if ( cs.length()>4 && Character.isDigit(cs.charAt(3)) ) {
				return (cs.charAt(2)-'0')*8 + (cs.charAt(3)-'0');
			}
			return cs.charAt(2)-'0';

		case '4' :
		case '5' :
		case '6' :
		case '7' :
			if ( cs.length()>4 && Character.isDigit(cs.charAt(3)) ) {
				return (cs.charAt(2)-'0')*8 + (cs.charAt(3)-'0');
			}
			return cs.charAt(2)-'0';

		default :
			return 0;
		}
	}

	public static int tokenTypeForCharLiteral(String lit) {
		if ( lit.length()>3 ) {  // does char contain escape?
			return escapeCharValue(lit);
		}
		else {
			return lit.charAt(1);
		}
	}
}

WS	:	(	/*	'\r' '\n' can be matched in one alternative or by matching
				'\r' in one iteration and '\n' in another.  I am trying to
				handle any flavor of newline that comes in, but the language
				that allows both "\r\n" and "\r" and "\n" to all be valid
				newline is ambiguous.  Consequently, the resulting grammar
				must be ambiguous.  I'm shutting this warning off.
			 */
			options {
				generateAmbigWarnings=false;
			}
		:	' '
		|	'\t'
		|	'\r' '\n'	{newline();}
		|	'\r'		{newline();}
		|	'\n'		{newline();}
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

COMMA : ',';

QUESTION :	'?' ;

TREE_BEGIN : "#(" ;

LPAREN:	'(' ;

RPAREN:	')' ;

COLON :	':' ;

STAR:	'*' ;

PLUS:	'+' ;

ASSIGN : '=' ;

PLUS_ASSIGN : "+=" ;

IMPLIES : "=>" ;

SEMI:	';' ;

CARET : '^' ;

BANG : '!' ;

OR	:	'|' ;

WILDCARD : '.' ;

RANGE : ".." ;

NOT :	'~' ;

RCURLY:	'}'	;

CHAR_LITERAL
	:	'\'' (ESC|~'\'') '\''
	;

STRING_LITERAL
	:	'"' (ESC|~'"')* '"'
	;

protected
ESC	:	'\\'
		(	'n' {$setText('\n');}
		|	'r' {$setText('\r');}
		|	't' {$setText('\t');}
		|	'b' {$setText('\b');}
		|	'f' {$setText('\f');}
		|	'"' {$setText('\"');}
		|	'\'' {$setText('\'');}
		|	'\\' {$setText('\\');}
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

/** ${...} is a lexer action */
LEXER_ACTION
	:   "${"
	;

/** ^{...} is a tree action */
TREE_ACTION
	:   "^{"
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
	|	CHAR_LITERAL
	|	STRING_LITERAL
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
	:	'\'' (ACTION_ESC|~'\'') '\''
	;

protected
ACTION_STRING_LITERAL
	:	'"' (ACTION_ESC|~'"')* '"'
	;

protected
ACTION_ESC
	:	"\'"
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
		(	{t==LITERAL_options}? WS_LOOP ('{' {_ttype = OPTIONS;})?
		|	{t==LITERAL_tokens}? WS_LOOP ('{' {_ttype = TOKENS;})?
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

