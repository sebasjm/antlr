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
        #( "grammar"
           (cmt:DOC_COMMENT {out(#cmt.getText()+"\n");} )?
           {out("grammar ");} atom (optionsSpec)? {out(";\n");}
           rules
         )
    ;

headerSpec
    :   #( "header" a:ACTION {out("header {"+#a.getText()+"}\n");} )
    ;

grammarType
    :   "lexer"  {out("lexer ");}
    |   "parser" {out("parser ");}
    |   "tree"   {out("tree ");}
    ;

optionsSpec
    :   #( OPTIONS (option {out(" ");} )+ )
    ;

option
    :   #( ASSIGN id {out("=");} optionValue )
    ;

optionValue
	:	id
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

rules
    :   ( rule )+
    ;

rule
    :   #( RULE (r:RULE_REF {out(r+" : ");}|t:TOKEN_REF {out(t+" : ");})
        block EOR {out(";\n");} )
    ;

block
    :   #(  BLOCK {out(" (");}
            {
            Map opts = #BLOCK.getOptions();
            if ( opts!=null ) {
                String os = opts.toString();
                os = os.substring(1,os.length()-1);
                out(os);
                out(" :");
            }
            }
            alternative ( {out(" | ");} alternative)*
            EOB   {out(")");}
         )
    ;

alternative
    :   #( ALT (element)+ EOA )
    ;

element
    :   atom
    |   #(NOT {out("~");} element) 
    |   #(RANGE atom {out("..");} atom)
    |   #(CHAR_RANGE atom {out("..");} atom)
    |   ebnf
    |   tree
    |   #( SYNPRED block ) {out("=>");}
    |   a:ACTION  {out("{"+a.getText()+"}");}
    |   SEMPRED
    |   EPSILON {out(" epsilon ");}
    ;

ebnf:   block {out(" ");}
    |   #( OPTIONAL block ) {out("? ");}
    |   #( CLOSURE block )  {out("* ");}
    |   #( POSITIVE_CLOSURE block ) {out("+ ");}
    ;

tree:   #(TREE_BEGIN {out(" #(");} atom (element)* {out(") ");} )
    ;

atom
{out(" "+#atom.toString()+" ");}
    :   RULE_REF
    |   TOKEN_REF
    |   CHAR_LITERAL
    |   STRING_LITERAL
    |   WILDCARD
    |   s:SET {out("="+s.getSetValue().toString()+" ");}
    ;

/** Match a.b.c.d qualified ids; WILDCARD here is overloaded as
 *  id separator; that is, I need a reference to the '.' token.
 */
qualifiedID
	:	id ( WILDCARD id )*
	;

id
{out(#id.toString());}
    :	TOKEN_REF
	|	RULE_REF
	;

