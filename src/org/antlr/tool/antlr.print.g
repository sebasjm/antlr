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

/** Print out a grammar (no pretty printing).
 *
 *  Terence Parr
 *  University of San Francisco
 *  August 19, 2003
 */
class ANTLRTreePrinter extends TreeParser;

options {
	importVocab = ANTLR;
	ASTLabelType = "GrammarAST";
    codeGenBitsetTestThreshold=999;
}

{
    StringBuffer buf = new StringBuffer(300);

    public void out(String s) {
        buf.append(s);
    }

    /** Parser error-reporting function can be overridden in subclass */
    public void reportError(RecognitionException ex) {
        System.out.println("print: "+ex.toString());
    }

    /** Parser error-reporting function can be overridden in subclass */
    public void reportError(String s) {
        System.out.println("print: error: " + s);
    }

	/** Normalize a grammar print out by removing all double spaces
	 *  and trailing/beginning stuff.  FOr example, convert
	 *
	 *  ( A  |  B  |  C )*
	 *
	 *  to
	 *
	 *  ( A | B | C )*
	 */
	public static String normalize(String g) {
	    StringTokenizer st = new StringTokenizer(g, " ", false);
		StringBuffer buf = new StringBuffer();
		while ( st.hasMoreTokens() ) {
			String w = st.nextToken();
			buf.append(w);
			buf.append(" ");
		}
		return buf.toString().trim();
	}
}

/** Call this to figure out how to print */
toString returns [String s=null]
    :   (   grammar
        |   rule
        |   alternative
        |   element
        |   EOR {s="EOR";}
        )
        {return normalize(buf.toString());}
    ;

// --------------

grammar
    :   (headerSpec)*
	    ( #( LEXER_GRAMMAR grammarSpec["lexer " ] )
	    | #( PARSER_GRAMMAR grammarSpec["parser"] )
	    | #( TREE_GRAMMAR grammarSpec["tree "] )
	    | #( COMBINED_GRAMMAR grammarSpec[""] )
	    )
     ;

headerSpec
    :   #( "header" a:ACTION {out("header {"+#a.getText()+"}\n");} )
    ;

grammarSpec[String gtype]
	:	 id:ID {out(gtype+"grammar "+#id.getText());}
        (cmt:DOC_COMMENT {out(#cmt.getText()+"\n");} )?
        (optionsSpec)? {out(";\n");}
        (tokensSpec)?
        rules
    ;

optionsSpec
    :   #( OPTIONS (option {out(" ");} )+ {out(" : ");} )
    ;

option
    :   #( ASSIGN id:ID {out(#id.getText()+"=");} optionValue )
    ;

optionValue
	:	id:ID            {out(#id.getText());}
	|   s:STRING_LITERAL {out(#s.getText());}
	|	c:CHAR_LITERAL   {out(#c.getText());}
	|	i:INT            {out(#i.getText());}
	|   charSet
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
	:	TOKEN_REF
	|	#( ASSIGN TOKEN_REF (STRING_LITERAL|CHAR_LITERAL) )
	;

rules
    :   ( rule )+
    ;

rule
    :   #( RULE id:ID
           (modifier)?
           {out(#id.getText()+" : ");}
           (optionsSpec)? b:block[false] EOR {out(";\n");}
         )
    ;

modifier
{out(#modifier.getText()); out(" ");}
	:	"protected"
	|	"public"
	|	"private"
	|	"fragment"
	;

block[boolean forceParens]
    :   #(  BLOCK {if ( forceParens||#block.getNumberOfChildren()>2 ) out(" (");}
            (optionsSpec)?
            alternative ( {out(" | ");} alternative)*
            EOB   {if ( forceParens||#block.getNumberOfChildren()>2 ) out(")");}
         )
    ;

alternative
    :   #( ALT (element)+ EOA )
    ;

element
    :   atom
    |   #(NOT {out("~");} atom) 
    |   #(RANGE atom {out("..");} atom)
    |   #(CHAR_RANGE atom {out("..");} atom)
    |   ebnf
    |   tree
    |   #( SYNPRED block[true] ) {out("=>");}
    |   a:ACTION  {out(a.getText());}
    |	lexer_action
    |   SEMPRED
    |   EPSILON {out(" epsilon ");}
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

ebnf:   block[false] {out(" ");}
    |   #( OPTIONAL block[true] ) {out("? ");}
    |   #( CLOSURE block[true] )  {out("* ");}
    |   #( POSITIVE_CLOSURE block[true] ) {out("+ ");}
    ;

tree:   #(TREE_BEGIN {out(" #(");} atom (element)* {out(") ");} )
    ;

atom

    :   (	{out(" "+#atom.toString()+" ");}
    	:	RULE_REF
		|   TOKEN_REF
		|   CHAR_LITERAL
		|   STRING_LITERAL
		|   WILDCARD
		)
    |   set
    ;

set
    :   #(SET {out("(");} setElement ({out("|");} setElement)* {out(")");} )
    ;

setElement
    :   (	{out(#setElement.getText());}
    	:	CHAR_LITERAL
		|   TOKEN_REF
		|   STRING_LITERAL
		)
    |	#(CHAR_RANGE c1:CHAR_LITERAL c2:CHAR_LITERAL)
    	{out(#c1.getText()+".."+#c2.getText());}
    ;
