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
	package org.antlr.codegen;
    import org.antlr.tool.*;
    import org.antlr.analysis.*;
    import org.antlr.misc.*;
	import java.util.*;
	import org.antlr.stringtemplate.*;
    import antlr.TokenWithIndex;
    import antlr.CommonToken;
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
	protected static final int RULE_BLOCK_NESTING_LEVEL = 0;
	protected static final int OUTER_REWRITE_NESTING_LEVEL = 0;
	//protected static final String PREV_RULE_ROOT_LABEL = "old";

    protected String currentRuleName = null;
    protected int blockNestingLevel = 0;
    protected int rewriteBlockNestingLevel = 0;
	protected int outerAltNum = 0;
    protected StringTemplate currentBlockST = null;
    protected boolean currentAltHasRewrite = false;
    protected int rewriteTreeNestingLevel = 0;
    protected String firstReferencedElement = null;
    protected Set rewriteRuleRefs = null;

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
            "codegen: "+ex.toString(),
            ex);
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

	protected StringTemplate getWildcardST(GrammarAST elementAST, GrammarAST ast_suffix, String label) {
		String name = "wildcard";
		if ( grammar.type!=Grammar.LEXER ) {
			return getTokenElementST("wildcard", "wildcard", elementAST, ast_suffix, label);
		}
		return templates.getInstanceOf("wildcardChar");
	}

	protected StringTemplate getRuleElementST(String name,
										      String elementName,
											  GrammarAST elementAST,
    										  GrammarAST ast_suffix,
    										  String label)
	{
		String suffix = getSTSuffix(ast_suffix,label);
		name += suffix;
		if ( suffix.length()>0 && label==null ) {
			// we will need a label to do the AST or tracking, make one
			label = generator.createUniqueLabel(elementName);
			CommonToken labelTok = new CommonToken(ANTLRParser.ID, label);
			grammar.defineRuleRefLabel(currentRuleName, labelTok, elementAST);
		}
		StringTemplate elementST = templates.getInstanceOf(name);
		if ( label!=null ) {
			elementST.setAttribute("label", label);
		}
		return elementST;
	}

	protected StringTemplate getTokenElementST(String name,
											   String elementName,
											   GrammarAST elementAST,
											   GrammarAST ast_suffix,
											   String label)
	{
		String suffix = getSTSuffix(ast_suffix,label);
		name += suffix;
		if ( suffix.length()>0 && label==null ) {
			label = generator.createUniqueLabel(elementName);
			CommonToken labelTok = new CommonToken(ANTLRParser.ID, label);
			grammar.defineTokenRefLabel(currentRuleName, labelTok, elementAST);
		}
		StringTemplate elementST = templates.getInstanceOf(name);
		if ( label!=null ) {
			elementST.setAttribute("label", label);
		}
		return elementST;
	}

	/** Return a non-empty template name suffix if the token is to be
	 *  tracked, added to a tree, or both.
	 */
	protected String getSTSuffix(GrammarAST ast_suffix, String label) {
		if ( grammar.type==Grammar.LEXER ) {
			return "";
		}
		// handle list label stuff; make element use "Track"
		boolean hasListLabel=false;
		if ( label!=null ) {
			Rule r = grammar.getRule(currentRuleName);
			String stName = null;
			if ( r!=null ) {
				Grammar.LabelElementPair pair = r.getLabel(label);
				if ( pair!=null &&
					 (pair.type==Grammar.TOKEN_LIST_LABEL||
					  pair.type==Grammar.RULE_LIST_LABEL) )
				{
					hasListLabel=true;
				}
			}
		}

		String astPart = "";
		String operatorPart = "";
		String rewritePart = "";
		String listLabelPart = "";
		if ( grammar.buildAST() ) {
			astPart = "AST";
		}
		if ( ast_suffix!=null ) {
			if ( ast_suffix.getType()==ANTLRParser.ROOT ) {
    			operatorPart = "Root";
    		}
    		else if ( ast_suffix.getType()==ANTLRParser.RULEROOT ) {
    			operatorPart = "RuleRoot";
    		}
    		else if ( ast_suffix.getType()==ANTLRParser.BANG ) {
    			operatorPart = "Bang";
    		}
   		}
		if ( currentAltHasRewrite ) {
			rewritePart = "Track";
		}
		if ( hasListLabel ) {
			listLabelPart = "AndListLabel";
		}
		String STsuffix = astPart+operatorPart+rewritePart+listLabelPart;
		//System.out.println("suffix = "+STsuffix);

    	return STsuffix;
	}

    protected void init(Grammar g) {
        this.grammar = g;
        this.generator = grammar.getCodeGenerator();
        this.templates = generator.getTemplates();
    }
}

grammar[Grammar g,
        StringTemplate recognizerST,
        StringTemplate outputFileST,
        StringTemplate headerFileST]
{
    init(g);
    this.recognizerST = recognizerST;
    this.outputFileST = outputFileST;
    this.headerFileST = headerFileST;
    String superClass = (String)g.getOption("superClass");
    recognizerST.setAttribute("superClass", superClass);
    if ( g.type!=Grammar.LEXER ) {
		recognizerST.setAttribute("ASTLabelType", g.getOption("ASTLabelType"));
	}
}
    :   (headerSpec[outputFileST])*
	    ( #( LEXER_GRAMMAR grammarSpec )
	    | #( PARSER_GRAMMAR grammarSpec )
	    | #( TREE_GRAMMAR grammarSpec
	       )
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
		    recognizerST.setAttribute("scopes", grammar.getGlobalScopes());
		}
		outputFileST.setAttribute("name", #name.getText());
		headerFileST.setAttribute("name", #name.getText());
		}
		( #(OPTIONS .) )?
		( #(TOKENS .) )?
        (attrScope)*
        ( act:ACTION
          {recognizerST.setAttribute("globalAction",
                           generator.translateAction(null,#act));}
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
	// init blockNestingLevel so it's block level RULE_BLOCK_NESTING_LEVEL
	// for alts of rule
	blockNestingLevel = RULE_BLOCK_NESTING_LEVEL-1;
}
    :   #( RULE id:ID {r=#id.getText(); currentRuleName = r;}
		    (mod:modifier)?
            (ARG (ARG_ACTION)?)
            (RET (ARG_ACTION)?)
			( #(OPTIONS .) )?
			(ruleScopeSpec)?
            #( INITACTION
               (ia:ACTION {initAction=generator.translateAction(r,#ia);})?
             )
	     	b=block["ruleBlock", dfa] EOR
         )
        {
		String description =
		    grammar.grammarTreeToString(#rule.getFirstChildWithType(BLOCK), false);
    	b.setAttribute("description", description);
		/*
		System.out.println("rule "+r+" tokens="+
						   grammar.getRule(r).getAllTokenRefsInAltsWithRewrites());
		System.out.println("rule "+r+" rules="+
						   grammar.getRule(r).getAllRuleRefsInAltsWithRewrites());
		*/
        // do not generate lexer rules in combined grammar
		if ( grammar.type==Grammar.LEXER ) {
			if ( r.equals(Grammar.TOKEN_RULENAME) ) {
				code = templates.getInstanceOf("tokensRule");
			}
			else {
				code = templates.getInstanceOf("lexerRule");
				code.setAttribute("ruleDescriptor", grammar.getRule(r));
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
			else {
				description =
					grammar.grammarTreeToString(#rule,false);
				code.setAttribute("description", description);
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
    if ( dfa!=null ) {
        code = templates.getInstanceOf(blockTemplateName);
        decision = generator.genLookaheadDecision(recognizerST,dfa);
        code.setAttribute("decision", decision);
        code.setAttribute("decisionNumber", dfa.getDecisionNumber());
		code.setAttribute("maxK",dfa.getMaxLookaheadDepth());
		code.setAttribute("maxAlt",dfa.getNumberOfAlts());
    }
    else {
        code = templates.getInstanceOf(blockTemplateName+"SingleAlt");
    }
    blockNestingLevel++;
    code.setAttribute("blockLevel", blockNestingLevel);
    code.setAttribute("enclosingBlockLevel", blockNestingLevel-1);
    StringTemplate alt = null;
    StringTemplate rew = null;
    GrammarAST r = null;
    int altNum = 1;
	if ( this.blockNestingLevel==RULE_BLOCK_NESTING_LEVEL ) {this.outerAltNum=1;}
}
    :   #(  BLOCK
    	    ( OPTIONS )? // ignore
            ( alt=alternative {r=(GrammarAST)_t;} rew=rewrite
              {
              if ( this.blockNestingLevel==RULE_BLOCK_NESTING_LEVEL ) {
              	this.outerAltNum++;
              }
              // add the rewrite code as just another element in the alt :)
    		  if ( rew!=null ) {
    		  	alt.setAttribute("elements.{el,line,pos}",
    		  		rew, new Integer(r.getLine()), new Integer(r.getColumn()));
    		  }
    		  // add this alt to the list of alts for this block
              code.setAttribute("alts",alt);
              alt.setAttribute("altNum", new Integer(altNum));
              alt.setAttribute("outerAlt",
                  new Boolean(blockNestingLevel==RULE_BLOCK_NESTING_LEVEL));
              altNum++;
              }
            )+
            EOB
         )
    	{blockNestingLevel--;}
    ;

alternative returns [StringTemplate code=templates.getInstanceOf("alt")]
{
if ( blockNestingLevel==RULE_BLOCK_NESTING_LEVEL ) {
	GrammarAST aRewriteNode = #alternative.findFirstType(REWRITE);
	if ( grammar.buildAST() &&
		 (aRewriteNode!=null||
		 (#alternative.getNextSibling()!=null &&
		  #alternative.getNextSibling().getType()==REWRITE)) )
	{
		currentAltHasRewrite = true;
	}
	else {
		currentAltHasRewrite = false;
	}
}
String description = grammar.grammarTreeToString(#alternative, false);
code.setAttribute("description", description);
if ( !currentAltHasRewrite && grammar.buildAST() ) {
	code.setAttribute("autoAST", new Boolean(true));
}
StringTemplate e;
}
    :   #(	a:ALT
    		(	{GrammarAST elAST=(GrammarAST)_t;}
    			e=element
    			{
    			code.setAttribute("elements.{el,line,pos}",
    							  e,
    							  new Integer(elAST.getLine()),
    							  new Integer(elAST.getColumn())
    							 );
    			}
    		)+
    		EOA
    	 )
    ;

element returns [StringTemplate code=null]
{
    IntSet elements=null;
    GrammarAST ast = null;
}
    :   code=atom[null]
    |   #(  n:NOT
            {code = getTokenElementST("matchSet", "set", (GrammarAST)#n.getFirstChild(), #ast, null);}

            (  #( c:CHAR_LITERAL (ast1:ast_suffix)? )
	           {
	            int ttype=0;
     			if ( grammar.type==Grammar.LEXER ) {
        			ttype = Grammar.getCharValueFromGrammarCharLiteral(c.getText());
     			}
     			else {
        			ttype = grammar.getTokenType(c.getText());
        		}
	            elements = grammar.complement(ttype);
	            ast = #ast1;
	           }
            |  #( s:STRING_LITERAL (ast2:ast_suffix)? )
	           {
	            int ttype=0;
     			if ( grammar.type==Grammar.LEXER ) {
        			// TODO: error!
     			}
     			else {
        			ttype = grammar.getTokenType(s.getText());
        		}
	            elements = grammar.complement(ttype);
	            ast = #ast2;
	           }
            |  #( t:TOKEN_REF (ast3:ast_suffix)? )
	           {
	           int ttype = grammar.getTokenType(t.getText());
	           elements = grammar.complement(ttype);
	           ast = #ast3;
	           }
            |  #( st:SET (setElement)+ (ast4:ast_suffix)? )
               {
               // SETs are not precomplemented by buildnfa.g like
               // simple elements.
               elements = st.getSetValue();
	           ast = #ast4;
               }
            )
            {
            //code.setAttribute("not", "Not");
            code.setAttribute("s", generator.genSetExpr(templates,elements,1,false));
		 	code.setAttribute("elementIndex", ((TokenWithIndex)#n.getToken()).getIndex());
			if ( grammar.type!=Grammar.LEXER ) {
		 		generator.generateLocalFOLLOW(#n,"set",currentRuleName);
        	}
            }
         )

    |   #(CHAR_RANGE a:CHAR_LITERAL b:CHAR_LITERAL)
        {code = templates.getInstanceOf("charRangeRef");
         code.setAttribute("a", a.getText());
         code.setAttribute("b", b.getText());
        }

    |	#(ASSIGN label:ID code=atom[#label.getText()])

    |	#(	PLUS_ASSIGN label2:ID code=atom[#label2.getText()]
         )

    |   code=ebnf
    |   code=tree
    |   #( SYNPRED block["block",null] )

    |   act:ACTION
        {
        String actText = #act.getText();
        code = new StringTemplate(templates,
                                  generator.translateAction(currentRuleName,#act));
        }

    |   SEMPRED

    |   EPSILON
    ;

ebnf returns [StringTemplate code=null]
{
    DFA dfa=null;
    GrammarAST b = (GrammarAST)#ebnf.getFirstChild();
    GrammarAST eob = (GrammarAST)#b.getLastChild(); // loops will use EOB DFA
}
    :   (	{dfa = #ebnf.getLookaheadDFA();}
			code=block["block", dfa]
		|   {dfa = #ebnf.getLookaheadDFA();}
			#( OPTIONAL code=block["optionalBlock", dfa] )
		|   {dfa = #eob.getLookaheadDFA();}
			#( CLOSURE code=block["closureBlock", dfa] )
		|   {dfa = #eob.getLookaheadDFA();}
			#( POSITIVE_CLOSURE code=block["positiveClosureBlock", dfa] )
		)
		{
		String description = grammar.grammarTreeToString(#ebnf, false);
    	code.setAttribute("description", description);
    	}
    ;

tree returns [StringTemplate code=templates.getInstanceOf("tree")]
{
StringTemplate el=null;
GrammarAST elAST=null;
}
    :   #( TREE_BEGIN {elAST=(GrammarAST)_t;}
    	   el=element
           {
           code.setAttribute("root.{el,line,pos}",
							  el,
							  new Integer(elAST.getLine()),
							  new Integer(elAST.getColumn())
							  );
           }
           ( {elAST=(GrammarAST)_t;}
    		 el=element
           	 {
			 code.setAttribute("children.{el,line,pos}",
							  el,
							  new Integer(elAST.getLine()),
							  new Integer(elAST.getColumn())
							  );
			 }
           )*
         )
    ;

atom[String label] returns [StringTemplate code=null]
    :   #( r:RULE_REF (rarg:ARG_ACTION)? (as1:ast_suffix)? )
        {
        grammar.checkRuleReference(#r, #rarg, currentRuleName);
        code = getRuleElementST("ruleRef", #r.getText(), #r, #as1, label);
		code.setAttribute("rule", r.getText());

		//if ( label!=null ) {code.setAttribute("label", label);}
		if ( #rarg!=null ) {code.setAttribute("args", #rarg.getText());}
		code.setAttribute("elementIndex", ((TokenWithIndex)r.getToken()).getIndex());
		generator.generateLocalFOLLOW(#r,#r.getText(),currentRuleName);
		#r.code = code;
        }

    |   #( t:TOKEN_REF (targ:ARG_ACTION)? (as2:ast_suffix)? )
        {
           grammar.checkRuleReference(#t, #targ, currentRuleName);
		   if ( grammar.type==Grammar.LEXER ) {
			   code = templates.getInstanceOf("lexerRuleRef");
			   code.setAttribute("rule", t.getText());
			   if ( #targ!=null ) {code.setAttribute("args", #targ.getText());}
			   if ( label!=null ) code.setAttribute("label", label);
		   }
		   else {
			   code = getTokenElementST("tokenRef", #t.getText(), #t, #as2, label);
			   code.setAttribute("token", t.getText());
			   code.setAttribute("elementIndex", ((TokenWithIndex)#t.getToken()).getIndex());
			   generator.generateLocalFOLLOW(#t,#t.getText(),currentRuleName);
		   }
		   #t.code = code;
		}

    |   #( c:CHAR_LITERAL (as3:ast_suffix)? )
        {
		if ( grammar.type==Grammar.LEXER ) {
			code = templates.getInstanceOf("charRef");
			code.setAttribute("char",
			   generator.target.getTargetCharLiteralFromANTLRCharLiteral(c.getText()));
			if ( label!=null ) {
				code.setAttribute("label", label);
			}
		}
		else { // else it's a token type reference
			code = getTokenElementST("tokenRef", "char_literal", #c, #as3, label);
			code.setAttribute("token",
							  new Integer(grammar.getTokenType(c.getText())));
			code.setAttribute("elementIndex",
							  ((TokenWithIndex)#c.getToken()).getIndex());
			generator.generateLocalFOLLOW(#c,
				String.valueOf(grammar.getTokenType(#c.getText())),
				currentRuleName);
		}
        }

    |   #( s:STRING_LITERAL (as4:ast_suffix)? )
        {
		if ( grammar.type==Grammar.LEXER ) {
			code = templates.getInstanceOf("lexerStringRef");
			code.setAttribute("string",
			   generator.target.getTargetStringLiteralFromANTLRStringLiteral(s.getText()));
			if ( label!=null ) {
				code.setAttribute("label", label);
			}
		}
		else { // else it's a token type reference
			code = getTokenElementST("tokenRef", "string_literal", #s, #as4, label);
			code.setAttribute("token",
							 new Integer(grammar.getTokenType(s.getText())));
			code.setAttribute("elementIndex", ((TokenWithIndex)#s.getToken()).getIndex());
			generator.generateLocalFOLLOW(#s,
				String.valueOf(grammar.getTokenType(#s.getText())),
				currentRuleName);
		}
		}

    |   #( w:WILDCARD (as5:ast_suffix)? )
        {
		code = getWildcardST(#w,#as5,label);
		/*
		if ( label!=null ) {
		    code.setAttribute("label", label);
		}
		*/
		code.setAttribute("elementIndex", ((TokenWithIndex)#w.getToken()).getIndex());
		}

    |	code=set[label]
    ;

ast_suffix
	:	ROOT
	|	RULEROOT
	|	BANG
	;

set[String label] returns [StringTemplate code=null]
	:   #( s:SET (setElement)+ ( ast:ast_suffix )? )
        {
        // TODO: make this work with ast_suffix
        code = getTokenElementST("matchSet", "set", #s, #ast, label);
		code.setAttribute("elementIndex", ((TokenWithIndex)#s.getToken()).getIndex());
		if ( grammar.type!=Grammar.LEXER ) {
			generator.generateLocalFOLLOW(#s,"set",currentRuleName);
        }
        code.setAttribute("s", generator.genSetExpr(templates,#s.getSetValue(),1,false));
		/*
		if ( label!=null ) {
		    code.setAttribute("label", label);
		}
		*/
        }
    ;

setElement
    :   c:CHAR_LITERAL
    |   t:TOKEN_REF
    |   s:STRING_LITERAL
    |	#(CHAR_RANGE c1:CHAR_LITERAL c2:CHAR_LITERAL)
    ;

// REWRITE stuff

rewrite returns [StringTemplate code=null]
{
StringTemplate alt;
if ( #rewrite.getType()==REWRITE ) {
	code = templates.getInstanceOf("rewriteCode");
	code.setAttribute("treeLevel", new Integer(OUTER_REWRITE_NESTING_LEVEL));
	code.setAttribute("rewriteBlockLevel", new Integer(OUTER_REWRITE_NESTING_LEVEL));
	currentBlockST = code;
	firstReferencedElement = null;
}
}
	:	(
			{rewriteRuleRefs = new HashSet();}
			#( r:REWRITE (pred:SEMPRED)? alt=rewrite_alternative )
			{
            rewriteBlockNestingLevel = OUTER_REWRITE_NESTING_LEVEL;
			String predText = null;
			if ( #pred!=null ) {
				predText = #pred.getText();
			}
			String altDescr =
			    grammar.grammarTreeToString(#r);
			code.setAttribute("alts.{pred,alt,description}",
							  predText,
							  alt,
							  altDescr);
			pred=null;
			}
		)*
		{
		if ( code!=null ) {
			code.setAttribute("firstReferencedElement", firstReferencedElement);
		}
		}
	;

rewrite_block[String blockTemplateName] returns [StringTemplate code=null]
{
rewriteBlockNestingLevel++;
code = templates.getInstanceOf(blockTemplateName);
StringTemplate save_currentBlockST = currentBlockST;
currentBlockST = code;
String save_firstReferencedElement = firstReferencedElement;
firstReferencedElement = null;
code.setAttribute("rewriteBlockLevel", rewriteBlockNestingLevel);
StringTemplate alt=null;
}
    :   #(  BLOCK
            alt=rewrite_alternative
            EOB
         )
    	{
    	code.setAttribute("alt", alt);
		code.setAttribute("firstReferencedElement", firstReferencedElement);
    	rewriteBlockNestingLevel--;
    	currentBlockST = save_currentBlockST;
    	firstReferencedElement = save_firstReferencedElement;
    	}
    ;

rewrite_alternative
	returns [StringTemplate code=templates.getInstanceOf("rewriteElementList")]
{
StringTemplate el;
}
    :   #(	a:ALT
			(	( el=rewrite_element {code.setAttribute("elements", el);} )+
    		|	EPSILON
    			{code.setAttribute("elements",
    							   templates.getInstanceOf("rewriteEmptyAlt"));}
    		)
    		EOA
    	 )
    ;

rewrite_element returns [StringTemplate code=null]
{
    IntSet elements=null;
    GrammarAST ast = null;
}
    :   code=rewrite_atom[false]

    |   #(  n:NOT
            (  c:CHAR_LITERAL
            |  s:STRING_LITERAL
            |  t:TOKEN_REF
            |  #( st:SET (rewrite_setElement)+ )
            )
         )

    |   code=rewrite_ebnf

    |   code=rewrite_tree
    ;

rewrite_ebnf returns [StringTemplate code=null]
    :   #( OPTIONAL code=rewrite_block["rewriteOptionalBlock"] )
		{
		String description = grammar.grammarTreeToString(#rewrite_ebnf, false);
		code.setAttribute("description", description);
		}
    |   #( CLOSURE code=rewrite_block["rewriteClosureBlock"] )
		{
		String description = grammar.grammarTreeToString(#rewrite_ebnf, false);
		code.setAttribute("description", description);
		}
    |   #( POSITIVE_CLOSURE code=rewrite_block["rewritePositiveClosureBlock"] )
		{
		String description = grammar.grammarTreeToString(#rewrite_ebnf, false);
		code.setAttribute("description", description);
		}
    ;

rewrite_tree returns [StringTemplate code=templates.getInstanceOf("rewriteTree")]
{
rewriteTreeNestingLevel++;
code.setAttribute("treeLevel", rewriteTreeNestingLevel);
code.setAttribute("enclosingTreeLevel", rewriteTreeNestingLevel-1);
StringTemplate r, el;
}
	:   #(	TREE_BEGIN
			r=rewrite_atom[true] {code.setAttribute("root",r);}
			(el=rewrite_element {code.setAttribute("children",el);})*
		)
		{
		String description = grammar.grammarTreeToString(#rewrite_tree, false);
		code.setAttribute("description", description);
    	rewriteTreeNestingLevel--;
		}
    ;

rewrite_atom[boolean isRoot] returns [StringTemplate code=null]
    :   r:RULE_REF
    	{
    	String ruleRefName = #r.getText();
    	String stName = "rewriteRuleRef";
    	if ( isRoot ) {
    		stName += "Root";
    	}
    	code = templates.getInstanceOf(stName);
    	code.setAttribute("rule", ruleRefName);
    	if ( grammar.getRule(ruleRefName)==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_UNDEFINED_RULE_REF,
									  grammar,
									  ((GrammarAST)(#r)).getToken(),
									  ruleRefName);
    		code = new StringTemplate(); // blank; no code gen
    	}
    	else if ( grammar.getRule(currentRuleName)
    			     .getRuleRefsInAlt(ruleRefName,outerAltNum)==null )
		{
			ErrorManager.grammarError(ErrorManager.MSG_REWRITE_ELEMENT_NOT_PRESENT_ON_LHS,
									  grammar,
									  ((GrammarAST)(#r)).getToken(),
									  ruleRefName);
    		code = new StringTemplate(); // blank; no code gen
    	}
    	else {
    		// track all rule refs as we must copy 2nd ref to rule and beyond
    		if ( !rewriteRuleRefs.contains(ruleRefName) ) {
	    		rewriteRuleRefs.add(ruleRefName);
    		}
    		else {
    			// we found a ref to the same rule in the same -> rewrite
    			code.setAttribute("dup", new Boolean(true));
    		}
			if ( firstReferencedElement==null ) {
				firstReferencedElement = ruleRefName;
			}
			else {
				currentBlockST.setAttribute("referencedRules", ruleRefName);
			}
		}
    	}

    |   ( #(TOKEN_REF (arg:ARG_ACTION)?) | CHAR_LITERAL | STRING_LITERAL )
    	{
    	String tokenName = #rewrite_atom.getText();
    	String stName = "rewriteTokenRef";
    	Rule rule = grammar.getRule(currentRuleName);
    	Set tokenRefsInAlt = rule.getTokenRefsInAlt(outerAltNum);
    	boolean imaginary = !tokenRefsInAlt.contains(tokenName);
    	if ( imaginary ) {
    		stName = "rewriteImaginaryTokenRef";
    	}
    	if ( isRoot ) {
    		stName += "Root";
    	}
    	code = templates.getInstanceOf(stName);
    	if ( #arg!=null ) {
    		code.setAttribute("args", generator.translateAction(currentRuleName,#arg));
    	}
		code.setAttribute("elementIndex", ((TokenWithIndex)#rewrite_atom.getToken()).getIndex());
		int ttype = grammar.getTokenType(tokenName);
		String tok = generator.getTokenTypeAsTargetLabel(ttype);
    	code.setAttribute("token", tok);
    	if ( grammar.getTokenType(tokenName)==Label.INVALID ) {
			ErrorManager.grammarError(ErrorManager.MSG_UNDEFINED_TOKEN_REF_IN_REWRITE,
									  grammar,
									  ((GrammarAST)(#rewrite_atom)).getToken(),
									  tokenName);
    		code = new StringTemplate(); // blank; no code gen
    	}
    	else {
    		// only track this reference if it's valid
			if ( !imaginary ) {
				if ( firstReferencedElement==null ) {
					firstReferencedElement = tok;
				}
				else {
					currentBlockST.setAttribute("referencedTokens", tok);
				}
			}
		}
    	}

    |	code=rewrite_set

    |	LABEL
    	{
    	String labelName = #LABEL.getText();
    	Rule rule = grammar.getRule(currentRuleName);
    	Grammar.LabelElementPair pair = rule.getLabel(labelName);
    	if ( labelName.equals(currentRuleName) ) {
    		// special case; ref to old value via $rule
    		StringTemplate labelST = templates.getInstanceOf("prevRuleRootRef");
    		code = templates.getInstanceOf("rewriteRuleLabelRef"+(isRoot?"Root":""));
    		code.setAttribute("label", labelST);
    	}
    	else if ( pair==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_UNDEFINED_LABEL_REF_IN_REWRITE,
									  grammar,
									  ((GrammarAST)(#LABEL)).getToken(),
									  labelName);
			code = new StringTemplate();
    	}
    	else {
			String stName = null;
			String refListAttrName = null;
			switch ( pair.type ) {
				case Grammar.TOKEN_LABEL :
					stName = "rewriteTokenLabelRef";
					break;
				case Grammar.RULE_LABEL :
					stName = "rewriteRuleLabelRef";
					break;
				case Grammar.TOKEN_LIST_LABEL :
					stName = "rewriteTokenListLabelRef";
					refListAttrName = "referencedListLabels";
					break;
				case Grammar.RULE_LIST_LABEL :
					stName = "rewriteRuleListLabelRef";
					refListAttrName = "referencedListLabels";
					break;
			}
			if ( isRoot ) {
				stName += "Root";
			}
			code = templates.getInstanceOf(stName);
			code.setAttribute("label", labelName);
			if ( refListAttrName!=null ) {
				if ( firstReferencedElement==null ) {
					firstReferencedElement = #LABEL.getText();
				}
				else {
					currentBlockST.setAttribute(refListAttrName, #LABEL.getText());
				}
			}
		}
    	}

    |	ACTION
        {
        // actions in rewrite rules yield a tree object
        String actText = #ACTION.getText();
        String action = generator.translateAction(currentRuleName,#ACTION);
		code = templates.getInstanceOf("rewriteAction"+(isRoot?"Root":""));
		code.setAttribute("action", action);
        }
    ;

rewrite_set returns [StringTemplate code=null]
	:   #( s:SET (rewrite_setElement)+ )
    ;

rewrite_setElement
    :   c:CHAR_LITERAL
    |   t:TOKEN_REF
    |   s:STRING_LITERAL
    ;
