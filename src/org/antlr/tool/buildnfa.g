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
    Set ruleSet = grammar.getRules();
    for (Iterator itr = ruleSet.iterator(); itr.hasNext();) {
        String ruleName = (String) itr.next();
        NFAState ruleBeginState = factory.newState();
        ruleBeginState.setDescription("rule "+ruleName+" start");
        grammar.setRuleStartState(ruleName, ruleBeginState);
        NFAState ruleEndState = factory.newState();
        ruleEndState.setDescription("rule "+ruleName+" end");
        ruleEndState.setAcceptState(true);
        grammar.setRuleStopState(ruleName, ruleEndState);
    }
}

protected void addFollowTransition(String ruleName, NFAState following) {
     //System.out.println("adding follow link to rule "+ruleName);
     // find last link in FOLLOW chain emanating from rule
     NFAState end = grammar.getRuleStopState(ruleName);
     while ( end.transition(1)!=null ) {
         end = (NFAState)end.transition(1).getTarget();
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
    factory.build_EOFStates(rules);
}

    public void reportError(RecognitionException ex) {
        System.out.println("buildnfa: "+ex.toString());
    }

    public void reportError(String s) {
        System.out.println("buildnfa: error: " + s);
    }
}

grammar
    :   {init();}
        (headerSpec)*
        #( "grammar"
           (DOC_COMMENT)?
           ( t:TOKEN_REF {grammar.setName(#t.getText());}
           | r:RULE_REF  {grammar.setName(#r.getText());}
           )
           ( #(OPTIONS .) )?
           rules
         )
        {finish();}
    ;

headerSpec
    :   #( "header" ACTION )
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
    :   #( RULE r=id {currentRuleName = r;}
           #(BLOCK b=block EOB) EOR
           {
           if ( r.equals(Grammar.TOKEN_RULENAME) ) {
                NFAState ruleState = factory.build_ArtificialMatchTokensRuleNFA();
                if ( grammar.getNumberOfAltsForDecisionNFA(ruleState)>1 ) {
	                ruleState.setDecisionASTNode(#BLOCK); // always track ast node
                    int d = grammar.assignDecisionNumber( ruleState );
                    grammar.setDecisionNFA( d, ruleState );
                    grammar.setDecisionOptions(d, #BLOCK.getOptions());
                }
                // hook rule start state for Tokens to its manually-created start
                NFAState start = grammar.getRuleStartState(r);
		        start.addTransition(new Transition(Label.EPSILON, ruleState));
           }
           else {
                // attach start node to block for this rule
                NFAState start = grammar.getRuleStartState(r);
		        start.addTransition(new Transition(Label.EPSILON, b.left()));

                // track decision if > 1 alts
                if ( grammar.getNumberOfAltsForDecisionNFA(b.left())>1 ) {
                    b.left().setDescription(grammar.grammarTreeToString(#rule));
                    b.left().setDecisionASTNode(#BLOCK);
                    int d = grammar.assignDecisionNumber( b.left() );
                    grammar.setDecisionNFA( d, b.left() );
                    grammar.setDecisionOptions(d, #BLOCK.getOptions());
                }

                // hook to end of rule node
                NFAState end = grammar.getRuleStopState(r);
                b.right().addTransition(new Transition(Label.EPSILON,end));
           }
           }
         )
    ;

block returns [StateCluster g = null]
{
    StateCluster a = null;
    List alts = new LinkedList();
}
    :   ( a=alternative {alts.add(a);} )+
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
        }
    ;

element returns [StateCluster g=null]
    :   g=atom
    |   #(  n:NOT
            (  c:CHAR_LITERAL
	           {
	           int ttype = Grammar.getCharValueFromLiteral(c.getText());
	           g=factory.build_Set(grammar.complement(ttype));
	           }
//          |  CHAR_RANGE
            |  t:TOKEN_REF
	           {
	           int ttype = grammar.getTokenType(t.getText());
	           g=factory.build_Set(grammar.complement(ttype));
	           }
            |  st:SET
	           {g=factory.build_Set(grammar.complement(st.getSetValue()));}
            )
         )
    |   #(RANGE a:atom b:atom)
        {g = factory.build_Range(grammar.getTokenType(#a.getText()),
                                 grammar.getTokenType(#b.getText()));}
    |   #(CHAR_RANGE c1:CHAR_LITERAL c2:CHAR_LITERAL)
        {g = factory.build_CharRange(#c1.getText(), #c2.getText());}
    |   g=ebnf
    |   tree
    |   #( SYNPRED block )
    |   ACTION
    |   pred:SEMPRED {g = factory.build_SemanticPredicate(#pred);}
    |   s:SET {g = factory.build_Set(s.getSetValue());}
    |   EPSILON {g = factory.build_Epsilon();}
    ;

ebnf returns [StateCluster g=null]
{
    StateCluster b = null;
}
    :   #( BLOCK b=block EOB )
        {
        // track decision if > 1 alts
        if ( grammar.getNumberOfAltsForDecisionNFA(b.left())>1 ) {
            b.left().setDescription(grammar.grammarTreeToString(#BLOCK));
            b.left().setDecisionASTNode(#BLOCK);
            int d = grammar.assignDecisionNumber( b.left() );
            grammar.setDecisionNFA( d, b.left() );
            grammar.setDecisionOptions(d, #BLOCK.getOptions());
        }
        g = b;
        }
    |   #( OPTIONAL #( BLOCK b=block EOB ) )
        {
        g = factory.build_Aoptional(b);
    	g.left().setDescription(grammar.grammarTreeToString(#ebnf));
        // there is always at least one alt even if block has just 1 alt
        int d = grammar.assignDecisionNumber( g.left() );
		grammar.setDecisionNFA(d, g.left());
        grammar.setDecisionOptions(d, #BLOCK.getOptions());
        g.left().setDecisionASTNode(#BLOCK);
    	}
    |   #( CLOSURE #( BLOCK b=block eob:EOB ) )
        {
        g = factory.build_Astar(b);
		// track the loop back / exit decision point
    	b.right().setDescription("()* loopback of "+grammar.grammarTreeToString(#ebnf));
        int d = grammar.assignDecisionNumber( b.right() );
		grammar.setDecisionNFA(d, b.right());
        grammar.setDecisionOptions(d, #BLOCK.getOptions());
        b.right().setDecisionASTNode(#eob);
    	}
    |   #( POSITIVE_CLOSURE #( BLOCK b=block eob3:EOB ) )
        {
        g = factory.build_Aplus(b);
        // don't make a decision on left edge, can reuse loop end decision
		// track the loop back / exit decision point
    	b.right().setDescription("()+ loopback of "+grammar.grammarTreeToString(#ebnf));
        int d = grammar.assignDecisionNumber( b.right() );
		grammar.setDecisionNFA(d, b.right());
        grammar.setDecisionOptions(d, #BLOCK.getOptions());
        b.right().setDecisionASTNode(#eob3);
        }
    ;

tree:   #(TREE_BEGIN atom (element)*)
    ;

atom returns [StateCluster g=null]
    :   r:RULE_REF
        {
        NFAState start = grammar.getRuleStartState(r.getText());
        if ( start!=null ) {
            int ruleIndex = grammar.getRuleIndex(r.getText());
            g = factory.build_RuleRef(ruleIndex, start);
            if ( g.left().transition(0) instanceof RuleClosureTransition ) {
                addFollowTransition(r.getText(), g.right());
            }
            // else rule ref got inlined to a set
        }
        }

    |   t:TOKEN_REF
        {
        if ( grammar.getType()==Grammar.LEXER ) {
            NFAState start = grammar.getRuleStartState(t.getText());
            if ( start!=null ) {
                int ruleIndex = grammar.getRuleIndex(t.getText());
                g = factory.build_RuleRef(ruleIndex, start);
                //addFollowTransition(t.getText(), g.right());
                // don't hook up follow links back into Tokens rule
                // we need to see EOT on ends of token rules
                if ( !currentRuleName.equals(Grammar.TOKEN_RULENAME) ) {
                    addFollowTransition(t.getText(), g.right());
                }
            }
        }
        else {
            int tokenType = grammar.getTokenType(t.getText());
            g = factory.build_Atom(tokenType);
        }
        }

    |   c:CHAR_LITERAL   {g = factory.build_CharLiteralAtom(c.getText());}

    |   s:STRING_LITERAL {g = factory.build_StringLiteralAtom(s.getText());}

    |   WILDCARD         {g = factory.build_Wildcard();}
    ;

id returns [String r]
{r=#id.getText();}
    :	TOKEN_REF
	|	RULE_REF
	;

