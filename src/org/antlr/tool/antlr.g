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
}

/** Read in an ANTLR grammar and build an AST.  For now, I'm ignoring
 *  anything except the rules.
 *
 *  Terence Parr
 *  University of San Francisco
 *  August 19, 2003
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
    NOT;
    EPSILON;
    ALT;
    EOR;
    EOB;
    EOA; // end of alt
    PARSER="parser";
    OPTIONS;
    CHARSET;
    SET;
}

{
Grammar g = null;

    public void reportError(RecognitionException ex) {
        System.out.println("buildast: "+ex.toString());
    }

    public void reportError(String s) {
        System.out.println("buildast: error: " + s);
    }

public ANTLRParser(TokenStream in, Grammar g) {
    this(in);
    this.g = g;
}
}

grammar!
{
GrammarAST c=null;
}
   :    hdr:headerSpec
        ( a:ACTION )?
	    ( cmt:DOC_COMMENT  )?
        ( grammarType | {g.setType(Grammar.PARSER);} )
        gr:"grammar" gid:id (opt:grammarOptionsSpec)? SEMI
		    (tokensSpec!)?
		    ( ACTION! )?
	        r:rules
        EOF
        {#grammar = #(null, #hdr, #(#gr, #cmt, #gid, #a, #opt, #r));}
	;

headerSpec
    :   ( 	"header"^
	 	    (n:STRING_LITERAL)?
	 	    ACTION
	    )*
	;

grammarType
    :   "lexer"          {g.setType(Grammar.LEXER);}
    |   "parser"         {g.setType(Grammar.PARSER);}
    |   "tree"           {g.setType(Grammar.TREE_PARSER);}
    ;

grammarOptionsSpec
{
    HashMap opts = null;
}
    :   LPAREN^ {#LPAREN.setType(OPTIONS);}
            opts=optionList {g.setOptions(opts);}
        RPAREN!
    ;

optionList returns [HashMap opts=new HashMap()]
    :   option[opts] (COMMA! option[opts])*
    ;

option[HashMap opts]
{
    String key=null;
    Object value=null;
}
    :   key=id ASSIGN^ value=optionValue {opts.put(key,value);}
    ;

optionValue returns [Object value=null]
	:	value=id
	|   s:STRING_LITERAL {String vs = s.getText();
	                      value=vs.substring(1,vs.length()-1);}
	|	c:CHAR_LITERAL   {String vs = c.getText();
	                      value=vs.substring(1,vs.length()-1);}
	|	i:INT            {value = new Integer(i.getText());}
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
	:	TOKENS
			(	(	t1:TOKEN_REF
					( ASSIGN s1:STRING_LITERAL )? // (tokensSpecOptions)?
                    {
                    int ttype = g.defineToken(t1.getText());
                    g.defineToken(s1.getText(), ttype);
                    }
				|	s3:STRING_LITERAL // (tokensSpecOptions)?
                    {g.defineToken(s3.getText());}
				)
				SEMI
			)+
		RCURLY
	;

tokensSpecOptions
	:	OPEN_ELEMENT_OPTION
		id ASSIGN optionValue
		(
			SEMI id ASSIGN optionValue
		)*
		CLOSE_ELEMENT_OPTION
	;

rules
    :   (
			options {
				// limitation of appox LL(k) says ambig upon
				// DOC_COMMENT TOKEN_REF, but that's an impossible sequence
				warnWhenFollowAmbig=false;
			}
		:	rule
		)+
    ;

rule!
{
	String modifier=null;
    Map opts = null;
}
	:
	(	d:DOC_COMMENT	
	)?
	(	{modifier=LT(1).getText();}
	:	p1:"protected"
	|	p2:"public"		
	|	p3:"private"
	|	p4:"local"
	|	{modifier=null;}
	)
	ruleName:id
	( BANG  )?
	( aa:ARG_ACTION )?
	( "returns" rt:ARG_ACTION  )?
	( throwsSpec )?
	( opts=optionList )?
	(a:ACTION )?
	COLON b:altList SEMI
	( exceptionGroup )?
    {
   	int ruleIndex = g.defineRule(#ruleName.getText(), modifier, opts);
    if ( ruleIndex!=Grammar.INVALID_RULE_INDEX ) {
        GrammarAST eor = #[EOR,"<end-of-rule>"];
        eor.setEnclosingRule(#ruleName.getText());
        #rule = #(#[RULE,"rule"],#ruleName,#b,eor);
        g.setRuleAST(#ruleName.getText(), #rule);
    }
    }
	;

throwsSpec
	:	"throws" id 
		( COMMA id  )*
		
	;

/** Build #(BLOCK ( #(ALT ...) EOB )+ ) */
block
{
    IntSet elements = null;
    HashMap opts = null;
}
    :   (set)=> elements=set
        {
        GrammarAST s = #[SET,"SET"];
        s.setSetValue(elements);
        GrammarAST alt = #(#[ALT,"ALT"], s, #[EOA, "<end-of-alt>"]);
        #block = #(#[BLOCK, "BLOCK"],alt,#[EOB, "<end-of-block>"]);
        }

    |!  lp:LPAREN
		(
			// 2nd alt and optional branch ambig due to
			// linear approx LL(2) issue.  COLON ACTION
			// matched correctly in 2nd alt.
			options {
				warnWhenFollowAmbig = false;
			}
		:
			opts=optionList ( ACTION )? COLON
		|	ACTION COLON
		)?

		b:altList {#block = #b; #block.setOptions(opts);}
        RPAREN
    ;

altList
    :   a1:alternative ( OR! a2:alternative )*
        {
        #altList = #(#[BLOCK,"BLOCK"],#altList,#[EOB,"<end-of-block>"]);
        }
    ;

/** Match two or more set elements and return a single "match set" tree */
set returns [IntSet elements=new IntervalSet()]
    :   LPAREN
        setElement[elements] (OR! setElement[elements])+
        RPAREN
    ;

setElement[IntSet elements]
{
    int ttype;
}
    :   c:CHAR_LITERAL
        {
        ttype = Grammar.getCharValueFromLiteral(c.getText());
        elements.add(ttype);
        }
    |   t:TOKEN_REF
        {
        ttype = g.defineToken(t.getText());
        elements.add(ttype);
        }
    |   s:STRING_LITERAL
        {
        ttype = g.defineToken(s.getText());
        elements.add(ttype);
        }
//	|   CHAR_LITERAL RANGE^ CHAR_LITERAL
    ;

alternative // [GrammarAST enclosingBlock]
{
    GrammarAST eoa = #[EOA, "<end-of-alt>"];
}
    :   (BANG!)? ( element )+ ( exceptionSpecNoLabel! )?
        {
            if ( #alternative==null ) {
                #alternative = #(#[ALT,"ALT"],#[EPSILON,"epsilon"],eoa);
            }
            else {
                #alternative = #(#[ALT,"ALT"], #alternative,eoa);
            }
        }
    |   {#alternative = #(#[ALT,"ALT"],#[EPSILON,"epsilon"],eoa);}
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
	:	elementNoOptionSpec (elementOptionSpec!)?
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

elementNoOptionSpec!
{
    IntSet elements=null;
}
	:	id
		ASSIGN
		( id COLON  )?
		(	rr:RULE_REF {#elementNoOptionSpec = #rr;}
			( ARG_ACTION  )?
			( BANG  )?
		|	// this syntax only valid for lexer
			tr:TOKEN_REF {#elementNoOptionSpec = #tr;}
			( ARG_ACTION  )?
		)
	|
		(id COLON  )?
		(	r2:RULE_REF {#elementNoOptionSpec = #r2;}
			( ARG_ACTION  )?
			( BANG  )?
		|   r:range {#elementNoOptionSpec = #r;}
		|   t:terminal  {#elementNoOptionSpec = #t;}
		|	NOT_OP
			(	nt:notTerminal  {#elementNoOptionSpec = #(#[NOT,"~"],#nt);}
			|	elements=set
	            {
	            // must not after entire grammar has been read so
	            // we can see entire token space, make NOT SET tree
	            GrammarAST s = #[SET,"SET"];
	            s.setSetValue(elements);
	            #elementNoOptionSpec = #(#[NOT,"~"],s);
	            }
			)
		|	e2:ebnf             {#elementNoOptionSpec = #e2;}
		)

	|   a:ACTION  	            {#elementNoOptionSpec = #a;}

	|   p:SEMPRED 	            {#elementNoOptionSpec = #p;}

	|   t3:tree                 {#elementNoOptionSpec = #t3;}
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
		(	(	QUESTION    {#ebnf=#([OPTIONAL,"?"],b);}
			|	STAR	    {#ebnf=#([CLOSURE,"*"],b);}
			|	PLUS	    {#ebnf=#([POSITIVE_CLOSURE,"+"],b);}
			)
			( BANG )?
		|   IMPLIES	        {#b.setType(SYNPRED); #ebnf=#b;}
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
	:	c1:CHAR_LITERAL RANGE c2:CHAR_LITERAL
		{#range = #(#[CHAR_RANGE,".."], #c1, #c2);}
	;

terminal
    :   cl:CHAR_LITERAL ( BANG! )?

	|   tr:TOKEN_REF {g.defineToken(tr.getText());}
		ast_type_spec!
		// Args are only valid for lexer
		( ARG_ACTION! )?

	|   sl:STRING_LITERAL {if (g.getType()!=Grammar.LEXER) {g.defineToken(sl.getText());}}
		ast_type_spec!

	|   wi:WILDCARD ast_type_spec!
	;

notTerminal
	:   cl:CHAR_LITERAL

		( BANG! )?
	|
		tr:TOKEN_REF {g.defineToken(tr.getText());}
		 ast_type_spec!
	;

/** Match a.b.c.d qualified ids; WILDCARD here is overloaded as
 *  id separator; that is, I need a reference to the '.' token.
 */
qualifiedID
	:	id ( WILDCARD id )*
	;

id returns [String r]
{r=LT(1).getText();}
    :	TOKEN_REF
	|	RULE_REF
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
		{ $setType(Token.SKIP); }
	;

COMMENT :
	( SL_COMMENT | t:ML_COMMENT {$setType(t.getType());} )
	{if ( _ttype != DOC_COMMENT ) $setType(Token.SKIP);}
	;

protected
SL_COMMENT :
	"//"
	( ~('\n'|'\r') )*
	(
		/*	'\r' '\n' can be matched in one alternative or by matching
			'\r' and then in the next token.  The language
			that allows both "\r\n" and "\r" and "\n" to all be valid
			newline is ambiguous.  Consequently, the resulting grammar
			must be ambiguous.  I'm shutting this warning off.
		 */
			options {
				generateAmbigWarnings=false;
			}
		:	'\r' '\n'
		|	'\r'
		|	'\n'
	)
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

IMPLIES : "=>" ;

SEMI:	';' ;

CARET : '^' ;

BANG : '!' ;

OR	:	'|' ;

WILDCARD : '.' ;

RANGE : ".." ;

NOT_OP :	'~' ;

RCURLY:	'}'	;

CHAR_LITERAL
	:	'\'' (ESC|~'\'') '\''
	;

STRING_LITERAL
	:	'"' (ESC|~'"')* '"'
	;

protected
ESC	:	'\\'
		(	'n'
		|	'r'
		|	't'
		|	'b'
		|	'f'
		|	'w'
		|	'a'
		|	'"'
		|	'\''
		|	'\\'
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
	'['
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
	']'
	;

ACTION
{int actionLine=getLine(); int actionColumn = getColumn(); }
	:	NESTED_ACTION
		(	'?'!	{_ttype = SEMPRED;} )?
		{
			CommonToken t = new CommonToken(_ttype,$getText);
			t.setLine(actionLine);			// set action line to start
			t.setColumn(actionColumn);
			$setToken(t);
		}
	;

protected
NESTED_ACTION :
	'{'!
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
	|	CHAR_LITERAL
	|	COMMENT
	|	STRING_LITERAL
	|	.
	)*
	'}'!
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
WS_OPT :
	(WS)?
	;

