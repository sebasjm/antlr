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
	package org.antlr.codegen;
    import org.antlr.tool.*;
    import org.antlr.analysis.*;
    import org.antlr.misc.*;
	import java.util.*;
	import org.antlr.stringtemplate.*;
    import java.io.InputStream;
    import java.io.BufferedReader;
    import java.io.InputStreamReader;
}

/** Walk a grammar and generate code by gradually building up
 *  a bigger and bigger StringTemplate.
 *
 *  Terence Parr
 *  University of San Francisco
 *  June 15, 2004
 */
class CodeGenTreeWalker extends TreeParser;

options {
    // warning! ANTLR cannot see another directory to get vocabs, so I had
    // to copy the ANTLRTokenTypes.txt file into this dir from ../tools!
    // Yuck!  If you modify ../tools/antlr.g, make sure to copy the vocab here.
	importVocab = ANTLR;
    codeGenBitsetTestThreshold=999;
    ASTLabelType=GrammarAST;
}

{
    public void reportError(RecognitionException ex) {
        System.out.println("codegen: "+ex.toString());
    }

    public void reportError(String s) {
        System.out.println("codegen: error: " + s);
    }

    protected CodeGenerator generator;
    protected Grammar grammar;
    protected StringTemplateGroup templates;

    /** The overall lexer/parser template; simulate dynamically scoped
     *  attributes by making this an instance var of the walker.
     */
    protected StringTemplate recognizerST;

    /** DFAs might be in different file, have separate pointer */
    protected StringTemplate dfaST;

    protected void init(Grammar g) {
        this.grammar = g;
        this.generator = grammar.getCodeGenerator();
        this.templates = generator.getTemplates();
    }
}

grammar[Grammar g,
        StringTemplate recognizerST,
        StringTemplate dfaST,  // in case DFAs go in separate file
        StringTemplate outputFileST]
{
    String name;
    init(g);
    this.recognizerST = recognizerST;
    this.dfaST = dfaST;
}
    :   (headerSpec[outputFileST])*
        #( "grammar"
            (cmt:DOC_COMMENT {outputFileST.setAttribute("docComment", #cmt.getText());} )?
            name=id {recognizerST.setAttribute("name", name);}
            ( #(OPTIONS .) )?
            rules[recognizerST]
         )
        {
        generator.genTokenTypeDefinitions(recognizerST);
        }
    ;

headerSpec[StringTemplate outputFileST]
    :   #( "header" a:ACTION {outputFileST.setAttribute("headerAction", #a.getText());} )
    ;

rules[StringTemplate recognizerST]
{
StringTemplate rST;
}
    :   ( rST=rule {recognizerST.setAttribute("rules", rST);} )+
    ;

rule returns [StringTemplate code=null]
{
    String r;
    StringTemplate b;
    if ( grammar.getType()==Grammar.LEXER ) {
        code = templates.getInstanceOf("lexerRule");
    }
    else {
        code = templates.getInstanceOf("rule");
    }
    // get the dfa for the BLOCK
    DFA dfa = #rule.getChild(1).getLookaheadDFA();
}
    :   #( RULE r=id b=block["block", dfa] EOR )
        {
        if ( grammar.getType()==Grammar.LEXER &&
             !r.equals(Grammar.TOKEN_RULENAME) )
        {
            StringTemplate setTypeST = templates.getInstanceOf("setType");
            setTypeST.setAttribute("type", r);
            b.setAttribute("preamble", setTypeST);
        }
        String trace = (String)grammar.getOption("trace");
        if ( grammar.getType()!=Grammar.LEXER &&
             trace!=null && trace.equals("true") )
        { // use option for now not cmd-line
            code.setAttribute("enterAction", "System.out.println(\"enter "+r+"; LT(1)==\"+input.LT(1));");
            code.setAttribute("exitAction", "System.out.println(\"exit "+r+"; LT(1)==\"+input.LT(1));");
        }
        code.setAttribute("name", r);
        code.setAttribute("block", b);
        }
    ;

block[String blockTemplateName, DFA dfa]
     returns [StringTemplate code=null]
{
    StringTemplate decision = null;
    /*
    if ( #block.getNumberOfChildren()==2 ) { // 1 alt + 1 EOB node => single alt
        code = templates.getInstanceOf(blockTemplateName+"SingleAlt");
    }
    */
    if ( dfa!=null ) {
        code = templates.getInstanceOf(blockTemplateName);
        decision = generator.genLookaheadDecision(dfaST,dfa);
        code.setAttribute("decision", decision);
        code.setAttribute("decisionNumber", dfa.getDecisionNumber());
		code.setAttribute("maxK",generator.maxK);
    }
    else {
        code = templates.getInstanceOf(blockTemplateName+"SingleAlt");
    }
    StringTemplate a = null;
    List alts = null;
}
    :   #(  BLOCK
            a=alternative {code.setAttribute("alts",a);}
            ( a=alternative {code.setAttribute("alts",a);} )*
            EOB
         )
    ;

alternative returns [StringTemplate code=templates.getInstanceOf("alt")]
{
StringTemplate e;
}
    :   #( ALT (e=element {code.setAttribute("elements", e);})+ EOA )
    ;

element returns [StringTemplate code=null]
{
    IntSet elements=null;
}
    :   code=atom
    |   #(  n:NOT
            (  c:CHAR_LITERAL
	           {
	           int ttype = Grammar.getCharValueFromLiteral(c.getText());
	           elements = grammar.complement(ttype);
	           }
//          |  CHAR_RANGE
            |  t:TOKEN_REF
	           {
	           int ttype = grammar.getTokenType(t.getText());
	           elements = grammar.complement(ttype);
	           }
            |  st:SET {elements = st.getSetValue();}
            )
            {
            code = templates.getInstanceOf("matchNotSet");
            code.setAttribute("s", generator.genSetExpr(templates,elements,1));
            }
         )
    |   #(CHAR_RANGE a:CHAR_LITERAL b:CHAR_LITERAL)
        {code = templates.getInstanceOf("charRangeRef");
         code.setAttribute("a", a.getText());
         code.setAttribute("b", b.getText());
        }
    |   code=ebnf
    |   tree
    |   #( SYNPRED block["block",null] )

        // TODO: wrap in {...} for some targets?
    |   act:ACTION {code = new StringTemplate(templates, #act.getText());}
    |   SEMPRED
    |   s:SET
        {
        code = templates.getInstanceOf("matchSet");
        code.setAttribute("s", generator.genSetExpr(templates,s.getSetValue(),1));
        }
    |   EPSILON 
    ;

ebnf returns [StringTemplate code=null]
{
    DFA dfa=null;
    GrammarAST b = (GrammarAST)#ebnf.getFirstChild();
    GrammarAST eob = (GrammarAST)#b.getLastChild(); // loops will use EOB DFA
}
    :   {dfa = #ebnf.getLookaheadDFA();}
        code=block["block", dfa]
    |   {dfa = #b.getLookaheadDFA();}
        #( OPTIONAL code=block["optionalBlock", dfa] )
    |   {dfa = #eob.getLookaheadDFA();}
        #( CLOSURE code=block["closureBlock", dfa] )
    |   {dfa = #eob.getLookaheadDFA();}
        #( POSITIVE_CLOSURE code=block["positiveClosureBlock", dfa] )
    ;

tree:   #(TREE_BEGIN atom (element)*)
    ;

atom returns [StringTemplate code=null]
    :   r:RULE_REF     {code = templates.getInstanceOf("ruleRef");
                        code.setAttribute("rule", r.getText());}
    |   t:TOKEN_REF    {
                       if ( grammar.getType()==Grammar.LEXER ) {
                           code = templates.getInstanceOf("lexerRuleRef");
                           code.setAttribute("rule", t.getText());
                       }
                       else {
                           code = templates.getInstanceOf("tokenRef");
                           code.setAttribute("token", t.getText());
                       }
                       }

    |   c:CHAR_LITERAL  {code = templates.getInstanceOf("charRef");
                         code.setAttribute("char", c.getText());}

    |   s:STRING_LITERAL{
                        if ( grammar.getType()==Grammar.LEXER ) {
                            code = templates.getInstanceOf("lexerStringRef");
                            code.setAttribute("string", s.getText());
                        }
                        else { // else it's a token type reference
                            code = templates.getInstanceOf("tokenRef");
                            code.setAttribute("token",
                                             new Integer(grammar.getTokenType(s.getText())));
                        }
                        }

    |   w:WILDCARD      {
                        if ( grammar.getType()==Grammar.LEXER ) {
                            code = templates.getInstanceOf("wildcardChar");
                        }
                        else { // else it's a token type reference
                            code = templates.getInstanceOf("wildcard");
                        }
                        }
    ;

id returns [String r]
{r=#id.getText();}
    :	TOKEN_REF
	|	RULE_REF
	;
