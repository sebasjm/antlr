package org.antlr.tool;

import org.antlr.runtime.*;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.analysis.*;
import org.antlr.analysis.DFA;
import antlr.RecognitionException;

import java.util.Stack;

/** The recognition interpreter/engine for grammars.  Separated
 *  out of Grammar as it's related, but technically not a Grammar function.
 *  You create an interpreter for a grammar and an input stream.  This object
 *  can act as a TokenSource so that you can hook up two grammars (via
 *  a CommonTokenStream) to lex/parse.  Being a token source only makes sense
 *  for a lexer grammar of course.
 */
public class Interpreter implements TokenSource {
	protected Grammar grammar;
	protected IntStream input;

	class LexerActionGetTokenType implements InterpreterActions {
		public CommonToken token;
		Grammar g;
		public LexerActionGetTokenType(Grammar g) {
			this.g = g;
		}
		public void exitRule(String ruleName) {
			if ( !ruleName.equals(Grammar.TOKEN_RULENAME) ){
				int type = g.getTokenType(ruleName);
				int channel = Token.DEFAULT_CHANNEL;
				token = new CommonToken(type,channel,0,0);
				GrammarAST t = g.getLexerRuleAction(ruleName);
				if ( t!=null ) {
					executeLexerAction(token, t);
				}
			}
		}
		public void enterRule(String ruleName) {}
		public void matchElement(int type) {}
		public void mismatchedElement(String msg) {}
		public void noViableAlt(String msg) {}
	}

	static class BuildParseTree implements InterpreterActions {
		Grammar g;
		Stack callStack = new Stack();
		public BuildParseTree(Grammar g) {
			this.g = g;
			ParseTree root = new ParseTree("<grammar "+g.getName()+">");
			callStack.push(root);
		}
		public ParseTree getTree() {
			return (ParseTree)callStack.elementAt(0);
		}
		public void enterRule(String ruleName) {
			ParseTree parentRuleNode = (ParseTree)callStack.peek();
			ParseTree ruleNode = new ParseTree(ruleName);
			parentRuleNode.addChild(ruleNode);
			callStack.push(ruleNode);
		}
		public void exitRule(String ruleName) {
			callStack.pop();
		}
		public void matchElement(int type) {
			ParseTree ruleNode = (ParseTree)callStack.peek();
			ParseTree elementNode = new ParseTree(g.getTokenName(type));
			ruleNode.addChild(elementNode);
		}
		public void mismatchedElement(String msg) {}
		public void noViableAlt(String msg) {}
	}

	public Interpreter(Grammar grammar, IntStream input) {
		this.grammar = grammar;
		this.input = input;
	}

	public Token nextToken()
		throws TokenStreamException
	{
		if ( grammar.getType()!=Grammar.LEXER ) {
			return null;
		}
		if ( input.LA(1)==CharStream.EOF ) {
			return Token.EOFToken;
		}
		int start = input.index();
		int type = 0;
		CommonToken token = null;
		try {
			token = scan(Grammar.TOKEN_RULENAME);
		}
		catch (RecognitionException re) {
			throw new TokenStreamException(re);
		}
		// the scan can only set type and channel (if a ${...} action found)
		// we must set the line, and other junk here to make it a complete token
		int stop = input.index()-1;
		token.setLine(((CharStream)input).getLine());
		token.setStartIndex(start);
		token.setStopIndex(stop);
		token.setCharPositionInLine(((CharStream)input).getCharPositionInLine());
		return token;
	}

	public CharStream getCharStream() {
		return (CharStream)input;
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
	public void scan(String startRule, InterpreterActions actions)
		throws RecognitionException
	{
		if ( grammar.getType()!=Grammar.LEXER ) {
			return;
		}
		CharStream input = (CharStream)this.input;
		System.out.println("scan("+startRule+",'"+input.substring(input.index(),input.size()-1)+"')");
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
				return;
			}
			// Create the DFA predictors for each decision
			grammar.createLookaheadDFAs();
		}

		// do the parse
		Stack ruleInvocationStack = new Stack();
		NFAState start = grammar.getRuleStartState(startRule);
		NFAState stop = grammar.getRuleStopState(startRule);
		parseEngine(startRule, start, stop, input, ruleInvocationStack, actions);
	}

	public CommonToken scan(String startRule)
		throws RecognitionException
	{
		LexerActionGetTokenType actions = new LexerActionGetTokenType(grammar);
		scan(startRule, actions);
		return actions.token;
	}

	public void parse(String startRule, InterpreterActions actions)
		throws RecognitionException
	{
		System.out.println("parse("+startRule+")");
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
				return;
			}
			// Create the DFA predictors for each decision
			grammar.createLookaheadDFAs();
		}
		// do the parse
		Stack ruleInvocationStack = new Stack();
		NFAState start = grammar.getRuleStartState(startRule);
		NFAState stop = grammar.getRuleStopState(startRule);
		parseEngine(startRule, start, stop, input, ruleInvocationStack, actions);
	}

	public ParseTree parse(String startRule)
		throws RecognitionException
	{
		BuildParseTree actions = new BuildParseTree(grammar);
		parse(startRule, actions);
		return actions.getTree();
	}

	protected void parseEngine(String startRule,
							   NFAState start,
							   NFAState stop,
							   IntStream input,
							   Stack ruleInvocationStack,
							   InterpreterActions actions)
		throws RecognitionException
	{
		if ( actions!=null ) {
			actions.enterRule(start.getEnclosingRule());
		}
		NFAState s = start;
		int t = input.LA(1);
		while ( s!=stop ) {
			//System.out.println("parse state "+s.getStateNumber()+" input="+grammar.getTokenName(t));
			// CASE 1: decision state
			if ( s.getDecisionNumber()>0 ) {
				// decision point, must predict and jump to alt
				DFA dfa = grammar.getLookaheadDFA(s.getDecisionNumber());
				int m = input.mark();
				int predictedAlt = predict(dfa);
				if ( predictedAlt == NFA.INVALID_ALT_NUMBER ) {
					int position = input.index();
					if ( actions!=null ) {
						actions.noViableAlt("parsing error: no viable alternative at position="+position+
										" input symbol: "+grammar.getTokenName(t));
					}
					throw new RecognitionException("parsing error: no viable alternative at position="+position+
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
				if ( actions!=null ) {
					actions.exitRule(s.getEnclosingRule());
				}
				if ( ruleInvocationStack.empty() ) {
					// done parsing.  Hit the start state.
					//System.out.println("stack empty in stop state for "+s.getEnclosingRule());
					break;
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
					s = (NFAState)trans.getTarget();
					if ( actions!=null ) {
						actions.enterRule(s.getEnclosingRule());
					}
				}
				// CASE 3b: plain old epsilon transition, just move
				else {
					s = (NFAState)trans.getTarget();
				}
			}

			// CASE 4: match label on transition
			else if ( label.matches(t) ) {
				if ( actions!=null ) {
					actions.matchElement(t);
				}
				s = (NFAState)s.transition(0).getTarget();
				input.consume();
				t = input.LA(1);
			}

			// CASE 5: error condition; label is inconsistent with input
			else {
				int position = input.index();
				if ( actions!=null ) {
					actions.mismatchedElement("parsing error at position="+position+
					" input symbol: "+grammar.getTokenName(t));
				}
				throw new RecognitionException("parsing error at position="+position+
					" input symbol: "+grammar.getTokenName(t));
			}
		}
		//System.out.println("hit stop state for "+stop.getEnclosingRule());
		if ( actions!=null ) {
			actions.exitRule(stop.getEnclosingRule());
		}
	}

	/** Given an input stream, return the unique alternative predicted by
	 *  matching the input.  Upon error, return NFA.INVALID_ALT_NUMBER
	 *  The first symbol of lookahead is presumed to be primed; that is,
	 *  input.lookahead(1) must point at the input symbol you want to start
	 *  predicting with.
	 */
	public int predict(DFA dfa) {
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

	/** Exec an action limited to assignments like ( ${ ( = channel 99 ; ) ) */
	public void executeLexerAction(Token token, GrammarAST code) {
		System.out.println("action "+code.toString());
		ActionInterpreter interp = new ActionInterpreter();
		try {
			interp.lexer_action(code,token);
		}
		catch (RecognitionException re) {
			System.err.println("cannot exec action: "+code.toString());
		}
	}

}
