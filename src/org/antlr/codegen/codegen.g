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
    import antlr.TokenWithIndex;
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
    protected String currentRuleName = null;

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

    protected StringTemplate outputFileST;
    protected StringTemplate headerFileST;

    /** DFAs might be in different file, have separate pointer */
    protected StringTemplate cyclicDFAST;

    protected void init(Grammar g) {
        this.grammar = g;
        this.generator = grammar.getCodeGenerator();
        this.templates = generator.getTemplates();
    }
}

/*
grammar[Grammar g,
        StringTemplate recognizerST,
        StringTemplate cyclicDFAST,  // in case DFAs go in separate file
        StringTemplate outputFileST,
        StringTemplate headerFileST]
{
    String name;
    init(g);
    this.recognizerST = recognizerST;
    this.outputFileST = outputFileST;
    this.headerFileST = headerFileST;
    this.cyclicDFAST = cyclicDFAST;
    if ( cyclicDFAST==null ) {
    	this.cyclicDFAST = recognizerST;
    }
}
    :   (headerSpec[outputFileST])*
        #(  . // don't care about grammar type
            name=id
            (cmt:DOC_COMMENT {outputFileST.setAttribute("docComment", #cmt.getText());} )?
            {
            recognizerST.setAttribute("name", name);
            outputFileST.setAttribute("name", name);
            headerFileST.setAttribute("name", name);
            }
            ( #(OPTIONS .) )?
            ( #(TOKENS .) )?
            rules[recognizerST]
         )
    ;
    */

grammar[Grammar g,
        StringTemplate recognizerST,
        StringTemplate cyclicDFAST,  // in case DFAs go in separate file
        StringTemplate outputFileST,
        StringTemplate headerFileST]
{
    init(g);
    this.recognizerST = recognizerST;
    this.outputFileST = outputFileST;
    this.headerFileST = headerFileST;
    this.cyclicDFAST = cyclicDFAST;
    if ( cyclicDFAST==null ) {
    	this.cyclicDFAST = recognizerST;
    }
}
    :   (headerSpec[outputFileST])*
	    ( #( LEXER_GRAMMAR grammarSpec )
	    | #( PARSER_GRAMMAR grammarSpec )
	    | #( TREE_GRAMMAR grammarSpec )
	    | #( COMBINED_GRAMMAR grammarSpec )
	    )
    ;

headerSpec[StringTemplate outputFileST]
    :   #( "header" (ID)? a:ACTION {outputFileST.setAttribute("headerAction", #a.getText());} )
    ;

attrScope
	:	#( "scope" ID ACTION )
	;

grammarSpec
	:   name:ID
		(cmt:DOC_COMMENT {outputFileST.setAttribute("docComment", #cmt.getText());} )?
		{
		recognizerST.setAttribute("name", #name.getText());
		if ( grammar.type!=Grammar.LEXER ) {
		    recognizerST.setAttribute("scopes", grammar.getScopes());
		}
		outputFileST.setAttribute("name", #name.getText());
		headerFileST.setAttribute("name", #name.getText());
		}
		( #(OPTIONS .) )?
		( #(TOKENS .) )?
        (attrScope)*
        ( act:ACTION
          {recognizerST.setAttribute("globalAction",
                           generator.translateAction(null,#act.getText()));}
        )?
		rules[recognizerST]
	;

rules[StringTemplate recognizerST]
{
StringTemplate rST;
}
    :   (	rST=rule
    		{
    		if ( rST!=null ) {
				recognizerST.setAttribute("rules", rST);
				outputFileST.setAttribute("rules", rST);
				headerFileST.setAttribute("rules", rST);
			}
    		}
   		)+
    ;

rule returns [StringTemplate code=null]
{
    String r;
    String initAction = null;
    StringTemplate b;
	// get the dfa for the BLOCK
    DFA dfa=#rule.getFirstChildWithType(BLOCK).getLookaheadDFA();
}
    :   #( RULE id:ID {r=#id.getText(); currentRuleName = r;}
		    (mod:modifier)?
            (ARG (ARG_ACTION)?)
            (RET (ARG_ACTION)?)
			( #(OPTIONS .) )?
			(ruleScopeSpec)?
            #( INITACTION
               (ia:ACTION {initAction=generator.translateAction(r,#ia.getText());})?
             )
	     	b=block["block", dfa] EOR
         )
        // do not generate lexer rules in combined grammar
        {
		if ( grammar.type==Grammar.LEXER ) {
			if ( r.equals(Grammar.TOKEN_RULENAME) ) {
				code = templates.getInstanceOf("tokensRule");
			}
			else {
				code = templates.getInstanceOf("lexerRule");
			}
		}
		else {
			if ( !(grammar.type==Grammar.COMBINED &&
				 Character.isUpperCase(r.charAt(0))) )
			{
				code = templates.getInstanceOf("rule");
				code.setAttribute("ruleDescriptor", grammar.getRule(r));
			}
		}
        if ( code!=null ) {
			if ( grammar.type==Grammar.LEXER ) {
		    	boolean naked =
		    		r.equals(Grammar.TOKEN_RULENAME) ||
		    	    (mod!=null&&mod.getText().equals(Grammar.FRAGMENT_RULE_MODIFIER));
		    	code.setAttribute("nakedBlock", new Boolean(naked));
			}
			String trace = (String)grammar.getOption("trace");
			if ( grammar.type!=Grammar.LEXER &&
				 trace!=null && trace.equals("true") )
			{ // TODO: use option for now not cmd-line; HIDEOUS, fix this with ST composition
				code.setAttribute("enterAction",
					"System.out.println(\"enter "+r+
					"; LT(1)==\"+input.LT(1).toString(input.getTokenSource().getCharStream()));");
				code.setAttribute("exitAction",
					"System.out.println(\"exit "+r+
					"; LT(1)==\"+input.LT(1).toString(input.getTokenSource().getCharStream()));");
			}
			code.setAttribute("ruleName", r);
			code.setAttribute("block", b);
			if ( initAction!=null ) {
				code.setAttribute("initAction", initAction);
			}
        }
        }
    ;

modifier
	:	"protected"
	|	"public"
	|	"private"
	|	"fragment"
	;

ruleScopeSpec
 	:	#( "scope" (ACTION)? ( ID )* )
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
        decision = generator.genLookaheadDecision(cyclicDFAST,dfa);
        code.setAttribute("decision", decision);
        code.setAttribute("decisionNumber", dfa.getDecisionNumber());
		code.setAttribute("maxK",generator.maxK);
		code.setAttribute("maxAlt",dfa.getNumberOfAlts());
    }
    else {
        code = templates.getInstanceOf(blockTemplateName+"SingleAlt");
    }
    StringTemplate a = null;
    List alts = null;
}
    :   #(  BLOCK
    	    ( OPTIONS )? // ignore
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
    :   code=atom[null]
    |   #(  n:NOT
            {code = templates.getInstanceOf("matchNotSet");}
            (  c:CHAR_LITERAL
	           {
	           int ttype = Grammar.getCharValueFromANTLRGrammarLiteral(c.getText());
	           elements = grammar.complement(ttype);
	           }
//          |  CHAR_RANGE
            |  t:TOKEN_REF
	           {
	           int ttype = grammar.getTokenType(t.getText());
	           elements = grammar.complement(ttype);
	           }
            |  st:SET
               {
               // do not complement elements; we're using matchNotSet
               elements = st.getSetValue();
               }
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

    |	#(ASSIGN label:ID code=atom[label.getText()])

    |   code=ebnf
    |   tree
    |   #( SYNPRED block["block",null] )

        // TODO: wrap in {...} for some targets?
    |   act:ACTION
        {
        String actText = #act.getText();

        code = new StringTemplate(templates,
                                  generator.translateAction(currentRuleName,#act.getText()));
        }

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

ebnf returns [StringTemplate code=null]
{
    DFA dfa=null;
    GrammarAST b = (GrammarAST)#ebnf.getFirstChild();
    GrammarAST eob = (GrammarAST)#b.getLastChild(); // loops will use EOB DFA
}
    :   {dfa = #ebnf.getLookaheadDFA();}
        code=block["block", dfa]
    |   {dfa = #ebnf.getLookaheadDFA();}
        #( OPTIONAL code=block["optionalBlock", dfa] )
    |   {dfa = #eob.getLookaheadDFA();}
        #( CLOSURE code=block["closureBlock", dfa] )
    |   {dfa = #eob.getLookaheadDFA();}
        #( POSITIVE_CLOSURE code=block["positiveClosureBlock", dfa] )
    ;

tree:   #(TREE_BEGIN atom[null] (element)*)
    ;

atom[String label] returns [StringTemplate code=null]
    :   r:RULE_REF     {code = templates.getInstanceOf("ruleRef");
                        code.setAttribute("rule", r.getText());
                        if ( label!=null ) {code.setAttribute("label", label);}
                        code.setAttribute("elementIndex", ((TokenWithIndex)r.getToken()).getIndex());
                        generator.generateLocalFOLLOW(#r,#r.getText(),currentRuleName);
                       }
    |   t:TOKEN_REF    {
                       if ( grammar.type==Grammar.LEXER ) {
                           code = templates.getInstanceOf("lexerRuleRef");
                           code.setAttribute("rule", t.getText());
                       }
                       else {
                           code = templates.getInstanceOf("tokenRef");
                           code.setAttribute("token", t.getText());
                           code.setAttribute("elementIndex", ((TokenWithIndex)#t.getToken()).getIndex());
                           generator.generateLocalFOLLOW(#t,#t.getText(),currentRuleName);
                       }
                       if ( label!=null ) {code.setAttribute("label", label);}
                       }

    |   c:CHAR_LITERAL  {
                            if ( grammar.type==Grammar.LEXER ) {
                                code = templates.getInstanceOf("charRef");
                                code.setAttribute("char",
                                   CodeGenerator.getJavaEscapedCharFromANTLRLiteral(c.getText()));
                            }
                            else { // else it's a token type reference
                                code = templates.getInstanceOf("tokenRef");
                                code.setAttribute("token",
                                                  new Integer(grammar.getTokenType(c.getText())));
                           		code.setAttribute("elementIndex",
                           		                  ((TokenWithIndex)#c.getToken()).getIndex());
	                            generator.generateLocalFOLLOW(#c,
	                                String.valueOf(grammar.getTokenType(#c.getText())),
	                                currentRuleName);
                            }
                            if ( label!=null ) {code.setAttribute("label", label);}
                        }

    |   s:STRING_LITERAL{
                        if ( grammar.type==Grammar.LEXER ) {
                            code = templates.getInstanceOf("lexerStringRef");
                            code.setAttribute("string",
                               CodeGenerator.getJavaEscapedCharFromANTLRLiteral(s.getText()));
                        }
                        else { // else it's a token type reference
                            code = templates.getInstanceOf("tokenRef");
                            code.setAttribute("token",
                                             new Integer(grammar.getTokenType(s.getText())));
                            code.setAttribute("elementIndex", ((TokenWithIndex)#s.getToken()).getIndex());
	                        generator.generateLocalFOLLOW(#s,
	                            String.valueOf(grammar.getTokenType(#s.getText())),
	                            currentRuleName);
                        }
                        if ( label!=null ) {code.setAttribute("label", label);}
                        }

    |   w:WILDCARD      {
                        if ( grammar.type==Grammar.LEXER ) {
                            code = templates.getInstanceOf("wildcardChar");
                        }
                        else { // else it's a token type reference
                            code = templates.getInstanceOf("wildcard");
                        }
                        if ( label!=null ) {code.setAttribute("label", label);}
                        }

    |	code=set[label]
    ;

set[String label] returns [StringTemplate code=null]
	:   s:SET
        {
        code = templates.getInstanceOf("matchSet");
        code.setAttribute("s", generator.genSetExpr(templates,#s.getSetValue(),1));
        if ( label!=null ) {code.setAttribute("label", label);}
        }
    ;
