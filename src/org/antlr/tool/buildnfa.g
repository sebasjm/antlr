header {
/*
 [The "BSD licence"]
 Copyright (c) 2005-2006 Terence Parr
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
import org.antlr.analysis.*;
import org.antlr.misc.*;
}

/** Build an NFA from a tree representing an ANTLR grammar. */
class TreeToNFAConverter extends TreeParser;

options {
	importVocab = ANTLR;
	ASTLabelType = "GrammarAST";
}

{
/** Factory used to create nodes and submachines */
protected NFAFactory factory = null;

/** Which NFA object are we filling in? */
protected NFA nfa = null;

/** Which grammar are we converting an NFA for? */
protected Grammar grammar = null;

protected String currentRuleName = null;

public TreeToNFAConverter(Grammar g, NFA nfa, NFAFactory factory) {
	this();
	this.grammar = g;
	this.nfa = nfa;
	this.factory = factory;
}

protected void init() {
    // define all the rule begin/end NFAStates to solve forward reference issues
    Collection rules = grammar.getRules();
    for (Iterator itr = rules.iterator(); itr.hasNext();) {
		Rule r = (Rule) itr.next();
        String ruleName = r.name;
        NFAState ruleBeginState = factory.newState();
        ruleBeginState.setDescription("rule "+ruleName+" start");
		ruleBeginState.setEnclosingRuleName(ruleName);
        grammar.setRuleStartState(ruleName, ruleBeginState);
        NFAState ruleEndState = factory.newState();
        ruleEndState.setDescription("rule "+ruleName+" end");
        ruleEndState.setAcceptState(true);
		ruleEndState.setEnclosingRuleName(ruleName);
        grammar.setRuleStopState(ruleName, ruleEndState);
    }
}

protected void addFollowTransition(String ruleName, NFAState following) {
     //System.out.println("adding follow link to rule "+ruleName);
     // find last link in FOLLOW chain emanating from rule
     NFAState end = grammar.getRuleStopState(ruleName);
     while ( end.transition(1)!=null ) {
         end = (NFAState)end.transition(1).target;
     }
     if ( end.transition(0)!=null ) {
         // already points to a following node
         // gotta add another node to keep edges to a max of 2
         NFAState n = factory.newState();
         Transition e = new Transition(Label.EPSILON, n);
         end.addTransition(e);
         end = n;
     }
     Transition followEdge = new Transition(Label.EPSILON, following);
     end.addTransition(followEdge);
}

protected void finish() {
    List rules = new LinkedList();
    rules.addAll(grammar.getRules());
    int numEntryPoints = factory.build_EOFStates(rules);
    if ( numEntryPoints==0 ) {
        ErrorManager.grammarError(ErrorManager.MSG_NO_GRAMMAR_START_RULE,
                                  grammar,
                                  null,
                                  grammar.name);
    }
}

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
            grammar,
            token,
            "buildnfa: "+ex.toString(),
            ex);
    }
}

grammar
    :   {init();}
        ( #( LEXER_GRAMMAR grammarSpec )
	    | #( PARSER_GRAMMAR grammarSpec )
	    | #( TREE_GRAMMAR grammarSpec )
	    | #( COMBINED_GRAMMAR grammarSpec )
	    )
        {finish();}
    ;

attrScope
	:	#( "scope" ID ACTION )
	;

grammarSpec
	:	ID
		(cmt:DOC_COMMENT)?
        ( #(OPTIONS .) )?
        ( #(TOKENS .) )?
        (attrScope)*
        (AMPERSAND)* // skip actions
        rules
	;

rules
    :   ( rule )+
    ;

rule
{
    StateCluster g=null;
    StateCluster b = null;
    String r=null;
}
    :   #( RULE id:ID {r=#id.getText();}
		{currentRuleName = r; factory.currentRuleName = r;}
		(modifier)?
        (ARG (ARG_ACTION)?)
        (RET (ARG_ACTION)?)
		( OPTIONS )?
		( ruleScopeSpec )?
		   (AMPERSAND)*
           #(BLOCK b=block EOB)
           (exceptionGroup)?
           EOR
           {
           /* 11/28/2005: removed to treat Tokens rule like any other
           if ( r.equals(Grammar.ARTIFICIAL_TOKENS_RULENAME) ) {
                NFAState ruleState = factory.build_ArtificialMatchTokensRuleNFA();
                if ( grammar.getNumberOfAltsForDecisionNFA(ruleState)>1 ) {
	                ruleState.setDecisionASTNode(#BLOCK); // always track ast node
                    int d = grammar.assignDecisionNumber( ruleState );
                    grammar.setDecisionNFA( d, ruleState );
                    grammar.setDecisionBlockAST(d, #BLOCK);
                }
                // hook rule start state for Tokens to its manually-created start
                NFAState start = grammar.getRuleStartState(r);
		        start.addTransition(new Transition(Label.EPSILON, ruleState));
           }
           else */
           {
				if ( Character.isLowerCase(r.charAt(0)) ||
					 grammar.type==Grammar.LEXER )
				{
					// attach start node to block for this rule
					NFAState start = grammar.getRuleStartState(r);
					start.setAssociatedASTNode(#id);
					start.addTransition(new Transition(Label.EPSILON, b.left));

					// track decision if > 1 alts
					if ( grammar.getNumberOfAltsForDecisionNFA(b.left)>1 ) {
						b.left.setDescription(grammar.grammarTreeToString(#rule,false));
						b.left.setDecisionASTNode(#BLOCK);
						int d = grammar.assignDecisionNumber( b.left );
						grammar.setDecisionNFA( d, b.left );
                    	grammar.setDecisionBlockAST(d, #BLOCK);
					}

					// hook to end of rule node
					NFAState end = grammar.getRuleStopState(r);
					b.right.addTransition(new Transition(Label.EPSILON,end));
				}
           }
           }
         )
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

block returns [StateCluster g = null]
{
    StateCluster a = null;
    List alts = new LinkedList();
}
    :   ( OPTIONS )? // ignore
		( a=alternative rewrite {alts.add(a);} )+
        {
        g = factory.build_AlternativeBlock(alts);
        }
    ;

alternative returns [StateCluster g=null]
{
    StateCluster e = null;
}
    :   #( ALT (e=element {g = factory.build_AB(g,e);} )+ )
        {
        if (g==null) { // if alt was a list of actions or whatever
            g = factory.build_Epsilon();
        }
        else {
        	factory.optimizeAlternative(g);
        }
        }
    ;

exceptionGroup
	:	( exceptionSpec )+
    ;

exceptionSpec
    :   #("exception" ( ARG_ACTION )? ( exceptionHandler )*)
    ;

exceptionHandler
    :    #("catch" ARG_ACTION ACTION)
    ;

rewrite
	:	(
			{
			if ( grammar.getOption("output")==null ) {
				ErrorManager.grammarError(ErrorManager.MSG_REWRITE_OR_OP_WITH_NO_OUTPUT_OPTION,
										  grammar, #rewrite.token, currentRuleName);
			}
			}
			#( REWRITE (SEMPRED)? (ALT|TEMPLATE|ACTION) )
		)*
	;

element returns [StateCluster g=null]
    :   g=atom
    |   #(  n:NOT
            (  #( c:CHAR_LITERAL (ast1:ast_suffix)? )
	           {
	            int ttype=0;
     			if ( grammar.type==Grammar.LEXER ) {
        			ttype = Grammar.getCharValueFromGrammarCharLiteral(#c.getText());
     			}
     			else {
        			ttype = grammar.getTokenType(#c.getText());
        		}
                IntSet notAtom = grammar.complement(ttype);
                if ( notAtom.isNil() ) {
                    ErrorManager.grammarError(ErrorManager.MSG_EMPTY_COMPLEMENT,
					  			              grammar,
								              #c.token,
									          #c.getText());
                }
	            g=factory.build_Set(notAtom);
	           }
            |  #( t:TOKEN_REF (ast3:ast_suffix)? )
	           {
	           int ttype = grammar.getTokenType(t.getText());
               IntSet notAtom = grammar.complement(ttype);
               if ( notAtom.isNil() ) {
                  ErrorManager.grammarError(ErrorManager.MSG_EMPTY_COMPLEMENT,
				  			              grammar,
							              #t.token,
								          #t.getText());
               }
	           g=factory.build_Set(notAtom);
	           }
            |  g=set
	           {
	           GrammarAST stNode = (GrammarAST)n.getFirstChild();
               IntSet notSet = grammar.complement(stNode.getSetValue());
               stNode.setSetValue(notSet);
               if ( notSet.isNil() ) {
                  ErrorManager.grammarError(ErrorManager.MSG_EMPTY_COMPLEMENT,
				  			              grammar,
							              #n.token);
               }
	           g=factory.build_Set(notSet);
	           }
            )
        	{#n.followingNFAState = g.right;}
         )
    |   #(RANGE a:atom b:atom)
        {g = factory.build_Range(grammar.getTokenType(#a.getText()),
                                 grammar.getTokenType(#b.getText()));}
    |   #(CHAR_RANGE c1:CHAR_LITERAL c2:CHAR_LITERAL)
        {
        if ( grammar.type==Grammar.LEXER ) {
        	g = factory.build_CharRange(#c1.getText(), #c2.getText());
        }
        }
    |	#(ASSIGN ID g=atom_or_notatom)
    |	#(PLUS_ASSIGN ID g=atom)
    |   g=ebnf
    |   g=tree
    |   #( SYNPRED block )
    |   ACTION
    |   pred:SEMPRED {g = factory.build_SemanticPredicate(#pred);}
    |   spred:SYN_SEMPRED {g = factory.build_SemanticPredicate(#spred);}
    |   gpred:GATED_SEMPRED {g = factory.build_SemanticPredicate(#gpred);}
    |   EPSILON {g = factory.build_Epsilon();}
    ;

ebnf returns [StateCluster g=null]
{
    StateCluster b = null;
}
    :   #( BLOCK b=block EOB )
        {
        // track decision if > 1 alts
        if ( grammar.getNumberOfAltsForDecisionNFA(b.left)>1 ) {
            b.left.setDescription(grammar.grammarTreeToString(#BLOCK,false));
            b.left.setDecisionASTNode(#BLOCK);
            int d = grammar.assignDecisionNumber( b.left );
            grammar.setDecisionNFA( d, b.left );
            grammar.setDecisionBlockAST(d, #BLOCK);
        }
        g = b;
        }
    |   #( OPTIONAL #( blk:BLOCK b=block EOB ) )
        {
        g = factory.build_Aoptional(b);
    	g.left.setDescription(grammar.grammarTreeToString(#ebnf,false));
        // there is always at least one alt even if block has just 1 alt
        int d = grammar.assignDecisionNumber( g.left );
		grammar.setDecisionNFA(d, g.left);
        grammar.setDecisionBlockAST(d, #blk);
        g.left.setDecisionASTNode(#ebnf);
    	}
    |   #( CLOSURE #( BLOCK b=block eob:EOB ) )
        {
        g = factory.build_Astar(b);
		// track the loop back / exit decision point
    	b.right.setDescription("()* loopback of "+grammar.grammarTreeToString(#ebnf,false));
        int d = grammar.assignDecisionNumber( b.right );
		grammar.setDecisionNFA(d, b.right);
        grammar.setDecisionBlockAST(d, #BLOCK);
        b.right.setDecisionASTNode(#eob);
        // make block entry state also have same decision for interpreting grammar
        NFAState altBlockState = (NFAState)g.left.transition(0).target;
        altBlockState.setDecisionASTNode(#ebnf);
        altBlockState.setDecisionNumber(d);
        g.left.setDecisionNumber(d); // this is the bypass decision (2 alts)
        g.left.setDecisionASTNode(#ebnf);
    	}
    |   #( POSITIVE_CLOSURE #( blk2:BLOCK b=block eob3:EOB ) )
        {
        g = factory.build_Aplus(b);
        // don't make a decision on left edge, can reuse loop end decision
		// track the loop back / exit decision point
    	b.right.setDescription("()+ loopback of "+grammar.grammarTreeToString(#ebnf,false));
        int d = grammar.assignDecisionNumber( b.right );
		grammar.setDecisionNFA(d, b.right);
        grammar.setDecisionBlockAST(d, #blk2);
        b.right.setDecisionASTNode(#eob3);
        // make block entry state also have same decision for interpreting grammar
        NFAState altBlockState = (NFAState)g.left.transition(0).target;
        altBlockState.setDecisionASTNode(#ebnf);
        altBlockState.setDecisionNumber(d);
        }
    ;

tree returns [StateCluster g=null]
{
StateCluster e=null;
}
	:   #( TREE_BEGIN
		   {GrammarAST el=(GrammarAST)_t;}
		   g=element
		   {
           StateCluster down = factory.build_Atom(Label.DOWN);
           // TODO set following states for imaginary nodes?
           //el.followingNFAState = down.right;
		   g = factory.build_AB(g,down);
		   }
		   ( {el=(GrammarAST)_t;} e=element {g = factory.build_AB(g,e);} )*
		   {
           StateCluster up = factory.build_Atom(Label.UP);
           //el.followingNFAState = up.right;
		   g = factory.build_AB(g,up);
		   // tree roots point at right edge of DOWN for LOOK computation later
		   #tree.NFATreeDownState = down.left;
		   }
		 )
    ;

atom_or_notatom returns [StateCluster g=null]
	:	g=atom
	|	#(  n:NOT
            (  c:CHAR_LITERAL (ast1:ast_suffix)?
	           {
	            int ttype=0;
     			if ( grammar.type==Grammar.LEXER ) {
        			ttype = Grammar.getCharValueFromGrammarCharLiteral(#c.getText());
     			}
     			else {
        			ttype = grammar.getTokenType(#c.getText());
        		}
                IntSet notAtom = grammar.complement(ttype);
                if ( notAtom.isNil() ) {
                    ErrorManager.grammarError(ErrorManager.MSG_EMPTY_COMPLEMENT,
					  			              grammar,
								              #c.token,
									          #c.getText());
                }
	            g=factory.build_Set(notAtom);
	           }
            |  t:TOKEN_REF (ast3:ast_suffix)?
	           {
	           int ttype = grammar.getTokenType(t.getText());
               IntSet notAtom = grammar.complement(ttype);
               if ( notAtom.isNil() ) {
                  ErrorManager.grammarError(ErrorManager.MSG_EMPTY_COMPLEMENT,
				  			              grammar,
							              #t.token,
								          #t.getText());
               }
	           g=factory.build_Set(notAtom);
	           }
            |  g=set
	           {
	           GrammarAST stNode = (GrammarAST)n.getFirstChild();
               IntSet notSet = grammar.complement(stNode.getSetValue());
               stNode.setSetValue(notSet);
               if ( notSet.isNil() ) {
                  ErrorManager.grammarError(ErrorManager.MSG_EMPTY_COMPLEMENT,
				  			              grammar,
							              #n.token);
               }
	           g=factory.build_Set(notSet);
	           }
            )
        	{#n.followingNFAState = g.right;}
         )
	;

atom returns [StateCluster g=null]
    :   #( r:RULE_REF (rarg:ARG_ACTION)? (as1:ast_suffix)? )
        {
        NFAState start = grammar.getRuleStartState(r.getText());
        if ( start!=null ) {
            int ruleIndex = grammar.getRuleIndex(r.getText());
            g = factory.build_RuleRef(ruleIndex, start);
            r.followingNFAState = g.right;
            if ( g.left.transition(0) instanceof RuleClosureTransition
            	 && grammar.type!=Grammar.LEXER )
            {
                addFollowTransition(r.getText(), g.right);
            }
            // else rule ref got inlined to a set
        }
        }

    |   #( t:TOKEN_REF (targ:ARG_ACTION)? (as2:ast_suffix)? )
        {
        if ( grammar.type==Grammar.LEXER ) {
            NFAState start = grammar.getRuleStartState(t.getText());
            if ( start!=null ) {
                int ruleIndex = grammar.getRuleIndex(t.getText());
                g = factory.build_RuleRef(ruleIndex, start);
                // don't add FOLLOW transitions in the lexer;
                // only exact context should be used.
            }
        }
        else {
            int tokenType = grammar.getTokenType(t.getText());
            g = factory.build_Atom(tokenType);
            t.followingNFAState = g.right;
        }
        }

    |   #( c:CHAR_LITERAL (as3:ast_suffix)? )
    	{
    	if ( grammar.type==Grammar.LEXER ) {
    		g = factory.build_CharLiteralAtom(c.getText());
    	}
    	else {
            int tokenType = grammar.getTokenType(c.getText());
            g = factory.build_Atom(tokenType);
            c.followingNFAState = g.right;
    	}
    	}

    |   #( s:STRING_LITERAL (as4:ast_suffix)? )
    	{
     	if ( grammar.type==Grammar.LEXER ) {
     		g = factory.build_StringLiteralAtom(s.getText());
     	}
     	else {
             int tokenType = grammar.getTokenType(s.getText());
             g = factory.build_Atom(tokenType);
             s.followingNFAState = g.right;
     	}
     	}

    |   #( w:WILDCARD (as5:ast_suffix)? )    {g = factory.build_Wildcard();}

	|	g=set
	;

ast_suffix
{
if ( grammar.getOption("output")==null ) {
	ErrorManager.grammarError(ErrorManager.MSG_REWRITE_OR_OP_WITH_NO_OUTPUT_OPTION,
							  grammar, #ast_suffix.token, currentRuleName);
}
}
	:	ROOT
	|	RULEROOT
	|	BANG
	;

set returns [StateCluster g=null]
{
IntSet elements=new IntervalSet();
#set.setSetValue(elements); // track set for use by code gen
}
	:	#( s:SET (setElement[elements])+ ( ast:ast_suffix )? )
        {
        g = factory.build_Set(elements);
        #s.followingNFAState = g.right;
        }
		//{System.out.println("set elements="+elements.toString(grammar));}
    ;

setElement[IntSet elements]
{
    int ttype;
}
    :   c:CHAR_LITERAL
        {
     	if ( grammar.type==Grammar.LEXER ) {
        	ttype = Grammar.getCharValueFromGrammarCharLiteral(c.getText());
     	}
     	else {
        	ttype = grammar.getTokenType(c.getText());
        }
        if ( elements.member(ttype) ) {
			ErrorManager.grammarError(ErrorManager.MSG_DUPLICATE_SET_ENTRY,
									  grammar,
									  #c.token,
									  #c.getText());
        }
        elements.add(ttype);
        }
    |   t:TOKEN_REF
        {
        ttype = grammar.getTokenType(t.getText());
        if ( elements.member(ttype) ) {
			ErrorManager.grammarError(ErrorManager.MSG_DUPLICATE_SET_ENTRY,
									  grammar,
									  #t.token,
									  #t.getText());
        }
        elements.add(ttype);
        }
    |   s:STRING_LITERAL
        {
        ttype = grammar.getTokenType(s.getText());
        if ( elements.member(ttype) ) {
			ErrorManager.grammarError(ErrorManager.MSG_DUPLICATE_SET_ENTRY,
									  grammar,
									  #s.token,
									  #s.getText());
        }
        elements.add(ttype);
        }
    |	#(CHAR_RANGE c1:CHAR_LITERAL c2:CHAR_LITERAL)
    	{
     	if ( grammar.type==Grammar.LEXER ) {
	        int a = Grammar.getCharValueFromGrammarCharLiteral(c1.getText());
    	    int b = Grammar.getCharValueFromGrammarCharLiteral(c2.getText());
    		elements.addAll(IntervalSet.of(a,b));
     	}
    	}
    ;
