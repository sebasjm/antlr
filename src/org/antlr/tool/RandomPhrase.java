package org.antlr.tool;

import org.antlr.analysis.NFAState;
import org.antlr.analysis.RuleClosureTransition;
import org.antlr.analysis.Transition;
import org.antlr.analysis.Label;
import org.antlr.misc.IntSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class RandomPhrase {

	/** an experimental method to generate random phrases for a given
	 *  grammar given a start rule.  Return a list of token types.
	 */
	protected static void randomPhrase(Grammar g, List tokenTypes, String startRule) {
		// Build NFAs from the grammar AST
		g.createNFAs();

		NFAState state = g.getRuleStartState(startRule);
		NFAState stopState = g.getRuleStopState(startRule);

		Stack ruleInvocationStack = new Stack();
		while ( true ) {
			if ( state==stopState && ruleInvocationStack.size()==0 ) {
				break;
			}
			// System.out.println("state "+state);
			if ( state.getNumberOfTransitions()==0 ) {
				//System.out.println("dangling state: "+state);
				return;
			}
			// end of rule node
			if ( state.isAcceptState() ) {
				NFAState invokingState = (NFAState)ruleInvocationStack.pop();
				//System.out.println("pop invoking state "+invokingState);
				RuleClosureTransition invokingTransition =
					(RuleClosureTransition)invokingState.transition(0);
				// move to node after state that invoked this rule
				state = invokingTransition.getFollowState();
				continue;
			}
			if ( state.getNumberOfTransitions()==1 ) {
				// no branching, just take this path
				Transition t0 = state.transition(0);
				if ( t0 instanceof RuleClosureTransition ) {
					ruleInvocationStack.push(state);
					// System.out.println("push state "+state);
				}
				else if ( !t0.label.isEpsilon() ) {
					tokenTypes.add( getTokenType(t0.label) );
				}
				state = (NFAState)t0.target;
				continue;
			}

			int decisionNumber = state.getDecisionNumber();
			if ( decisionNumber==0 ) {
				System.out.println("weird: no decision number but a choice node");
				continue;
			}
			// decision point, pick ith alternative randomly
			int n = g.getNumberOfAltsForDecisionNFA(state);
			int randomAlt = (int)Math.floor(Math.random()*n + 1);
			if ( randomAlt>n ) {
				randomAlt = n;
			}
			//System.out.println("alt = "+randomAlt);
			NFAState altStartState =
				g.getNFAStateForAltOfDecision(state, randomAlt);
			Transition t = altStartState.transition(0);
			if ( !t.label.isEpsilon() ) {
				tokenTypes.add( getTokenType(t.label) );
			}
			state = (NFAState)t.target;
		}
	}

	protected static Integer getTokenType(Label label) {
		if ( label.isSet() ) {
			// pick random element of set
			IntSet typeSet = label.getSet();
			List typeList = typeSet.toList();
			double r = Math.random()*typeList.size();
			int randomIndex = (int)Math.floor(r);
			if ( randomIndex==typeList.size() ) {
				// rare case, but check anyway.  If r==1.0*size,
				// index would be out of range.
				randomIndex--;
			}
			return (Integer)typeList.get(randomIndex);
		}
		else {
			return new Integer(label.getAtom());
		}
		//System.out.println(t0.label.toString(g));
	}

	/** Used to generate random strings */
	public static void main(String[] args) throws Exception {
		String grammarFileName = args[0];
		String startRule = args[1];

		Grammar parser =
			new Grammar(null,
						grammarFileName,
						new BufferedReader(new FileReader(grammarFileName)));

		String lexerGrammarText = parser.getLexerGrammar();
		Grammar lexer = new Grammar();
		lexer.importTokenVocabulary(parser);
		lexer.setGrammarContent(lexerGrammarText);

		List tokenTypes = new ArrayList(100);
		randomPhrase(parser, tokenTypes, startRule);
		//System.out.println("token types="+tokenTypes);
		for (int i = 0; i < tokenTypes.size(); i++) {
			Integer ttypeI = (Integer) tokenTypes.get(i);
			int ttype = ttypeI.intValue();
			String ttypeDisplayName = parser.getTokenDisplayName(ttype);
			if ( Character.isUpperCase(ttypeDisplayName.charAt(0)) ) {
				List charsInToken = new ArrayList(10);
				randomPhrase(lexer, charsInToken, ttypeDisplayName);
				System.out.print(" ");
				for (int j = 0; j < charsInToken.size(); j++) {
					java.lang.Integer cI = (java.lang.Integer) charsInToken.get(j);
					System.out.print((char)cI.intValue());
				}
			}
			else { // it's a literal
				System.out.print(" "+ttypeDisplayName);
			}
		}
		System.out.println();
	}

}
