package org.antlr.tool;

import org.antlr.runtime.CharStream;
import org.antlr.analysis.*;
import antlr.RecognitionException;

import java.util.Stack;
import java.util.HashMap;
import java.util.Map;

/** The recognition interpreter/engine for grammars.  Separated
 *  out of Grammar as it's related, but technically not a Grammar function.
 */
public class Interpreter {
	protected Grammar grammar;

	public Interpreter(Grammar grammar) {
		this.grammar = grammar;
	}

	/** For a given input char stream, try to match against the NFA
	 *  starting at startRule.  This is a deterministic parse even though
	 *  it is using an NFA because it uses DFAs at each decision point to
	 *  predict which alternative will succeed.  This is exactly what the
	 *  generated parser will do.
	 *
	 *  This only does lexer grammars.
	 *
	 *  Return the token type associated with the final rule end state.
	 */
	public int parse(String startRule, CharStream input)
		throws Exception
	{
		System.out.println("parse("+startRule+",'"+input.substring(input.index(),input.size()-1)+"')");
		// Build NFAs/DFAs from the grammar AST if NFAs haven't been built yet
		if ( grammar.getRuleStartState(startRule)==null ) {
			if ( grammar.getType()==Grammar.LEXER ) {
				grammar.addArtificialMatchTokensRule();
			}
			try {
				grammar.createNFAs();
			}
			catch (RecognitionException re) {
				System.err.println("problems creating NFAs from grammar AST for "+
								   grammar.getName());
				return 0;
			}
			// Create the DFA predictors for each decision
			grammar.createLookaheadDFAs();
		}

		// do the parse
		Stack ruleInvocationStack = new Stack();
		NFAState start = grammar.getRuleStartState(startRule);
		NFAState stop = grammar.getRuleStopState(startRule);
		return parseEngine(start, stop, input, ruleInvocationStack);
	}

	public void parse(String startRule, Grammar lexer)
		throws Exception
	{
		System.out.println("parse("+startRule+")");
		// Build NFAs/DFAs from the grammar AST if NFAs haven't been built yet
		if ( grammar.getRuleStartState(startRule)==null ) {
		}
		// do the parse
		Stack ruleInvocationStack = new Stack();
		NFAState start = grammar.getRuleStartState(startRule);
		NFAState stop = grammar.getRuleStopState(startRule);
		//parseEngine(start, stop, input, ruleInvocationStack);
	}

	protected int parseEngine(NFAState start,
							  NFAState stop,
							  CharStream input,
							  Stack ruleInvocationStack)
		throws Exception
	{
		NFAState s = start;
		int t = input.LA(1);
		while ( s!=stop ) {
			//System.out.println("parse state "+s.getStateNumber()+" input="+grammar.getTokenName(t));
			// CASE 1: decision state
			if ( s.getDecisionNumber()>0 ) {
				// decision point, must predict and jump to alt
				DFA dfa = grammar.getLookaheadDFA(s.getDecisionNumber());
				int m = input.mark();
				int predictedAlt = predict(dfa,input);
				if ( predictedAlt == NFA.INVALID_ALT_NUMBER ) {
					int position = input.index();
					throw new Exception("parsing error: no viable alternative at position="+position+
										" input symbol: "+grammar.getTokenName(t));
				}
				input.rewind(m);
				if ( s.getDecisionASTNode().getType()==ANTLRParser.EOB ) {
					if ( predictedAlt==grammar.getNumberOfAltsForDecisionNFA(s) )
					{
						// special case; loop end decisions have exit as
						// # block alts + 1; getNumberOfAltsForDecisionNFA() has
						// both block alts and exit branch.  So, any predicted alt
						// equal to number of alts is the exit alt.  The NFA
						// sees that as alt 1
						predictedAlt = 1;
					}
					else {
						// exit branch is really first transition, so skip
						predictedAlt = predictedAlt+1;
					}
				}
				NFAState alt = grammar.getNFAStateForAltOfDecision(s, predictedAlt);
				s = (NFAState)alt.transition(0).getTarget();
				continue;
			}

			// CASE 2: finished matching a rule
			if ( s.isAcceptState() ) { // end of rule node
				if ( ruleInvocationStack.empty() ) {
					// done parsing.  Hit the start state.
					System.out.println("stack empty in stop state for "+s.getEnclosingRule());
					return grammar.getTokenType(s.getEnclosingRule());
				}
				// pop invoking state off the stack to know where to return to
				NFAState invokingState = (NFAState)ruleInvocationStack.pop();
				RuleClosureTransition invokingTransition =
						(RuleClosureTransition)invokingState.transition(0);
				// move to node after state that invoked this rule
				s = invokingTransition.getFollowState();
				continue;
			}

			Transition trans = s.transition(0);
			Label label = trans.getLabel();
			// CASE 3: epsilon transition
			if ( label.isEpsilon() ) {
				// CASE 3a: rule invocation state
				if ( trans instanceof RuleClosureTransition ) {
					ruleInvocationStack.push(s);
				}
				// CASE 3b: plain old epsilon transition, just move
				s = (NFAState)trans.getTarget();
			}

			// CASE 4: match label on transition
			else if ( label.matches(t) ) {
				s = (NFAState)s.transition(0).getTarget();
				input.consume();
				t = input.LA(1);
			}

			// CASE 5: error condition; label is inconsistent with input
			else {
				int position = input.index();
				throw new Exception("parsing error at position="+position+
					" input symbol: "+grammar.getTokenName(t));
			}
		}
		System.out.println("hit stop state for "+stop.getEnclosingRule());
		return grammar.getTokenType(stop.getEnclosingRule());
	}

	/** Given an input stream, return the unique alternative predicted by
	 *  matching the input.  Upon error, return NFA.INVALID_ALT_NUMBER
	 *  The first symbol of lookahead is presumed to be primed; that is,
	 *  input.lookahead(1) must point at the input symbol you want to start
	 *  predicting with.
	 */
	public int predict(DFA dfa, CharStream input) {
		DFAState s = dfa.getStartState();
		int c = input.LA(1);
		Transition eotTransition = null;
	dfaLoop:
		while ( !s.isAcceptState() ) {
			/*
			System.out.println("DFA.predict("+s.getStateNumber()+", "+
					dfa.getNFA().getGrammar().getTokenName(c)+")");
			*/
			// for each edge of s, look for intersection with current char
			for (int i=0; i<s.getNumberOfTransitions(); i++) {
				Transition t = s.transition(i);
				// special case: EOT matches any char
				if ( t.getLabel().matches(c) ) {
					// take transition i
					s = (DFAState)t.getTarget();
					input.consume();
					c = input.LA(1);
					continue dfaLoop;
				}
				if ( t.getLabel().getAtom()==Label.EOT ) {
					eotTransition = t;
				}
			}
			if ( eotTransition!=null ) {
				s = (DFAState)eotTransition.getTarget();
				continue dfaLoop;
			}
			System.err.println("unexpected label '"+
					dfa.getNFA().getGrammar().getTokenName(c)+"' in dfa state "+s);
			return NFA.INVALID_ALT_NUMBER;
		}
		// woohoo!  We know which alt to predict
		// nothing emanates from a stop state; must terminate anyway
		/*
		System.out.println("DFA stop state "+s.getStateNumber()+" predicts "+
				s.getUniquelyPredictedAlt());
		*/
		return s.getUniquelyPredictedAlt();
	}

	/*
	Map busy = new HashMap();
	public boolean closureHasAcceptState(NFAState p) {
		if ( p.isAcceptState() )
		Transition transition0 = p.transition(0);

	}
	*/
}
