package org.antlr.tool;

import org.antlr.runtime.*;
import org.antlr.runtime.debug.*;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.analysis.*;
import org.antlr.analysis.DFA;

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

	/** A lexer listener that just creates token objects as they
	 *  are matched.  scan() use this listener to get a single object.
	 *  To get a stream of tokens, you must call scan() multiple times,
	 *  recording the token object result after each call.
	 */
	class LexerActionGetTokenType implements DebugEventListener {
		public CommonToken token;
		Grammar g;
		public LexerActionGetTokenType(Grammar g) {
			this.g = g;
		}
		public void exitRule(String ruleName) {
			if ( !ruleName.equals(Grammar.TOKEN_RULENAME) ){
				int type = g.getTokenType(ruleName);
				int channel = Token.DEFAULT_CHANNEL;
				token = new CommonToken((CharStream)input,type,channel,0,0);
			}
		}
		public void enterAlt(int alt) {}
		public void enterRule(String ruleName) {}
		public void enterSubRule(int decisionNumber) {}
		public void exitSubRule(int decisionNumber) {}
		public void location(int line, int pos) {}
		public void consumeToken(Token token) {}
		public void consumeHiddenToken(Token token) {}
		public void LT(int i, Token t) {}
		public void mark(int i) {}
		public void rewind(int i) {}
		public void recognitionException(RecognitionException e) {}
		public void recovered() {}
		public void commence() {}
		public void terminate() {}
	}

	/** This parser listener tracks rule entry/exit and token matches
	 *  to build a simple parse tree using the standard ANTLR Tree interface
	 */
	class BuildParseTree implements DebugEventListener {
		Grammar g;
		Stack callStack = new Stack();
		public BuildParseTree(Grammar g) {
			this.g = g;
			ParseTree root = new ParseTree("<grammar "+g.name+">");
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
		public void enterAlt(int alt) {}
		public void enterSubRule(int decisionNumber) {}
		public void exitSubRule(int decisionNumber) {}
		public void location(int line, int pos) {}
		public void exitRule(String ruleName) {
			callStack.pop();
		}
		public void consumeToken(Token token) {
			ParseTree ruleNode = (ParseTree)callStack.peek();
			ParseTree elementNode = new ParseTree(token);
			ruleNode.addChild(elementNode);
		}
		public void consumeHiddenToken(Token token) {}
		public void LT(int i, Token t) {}
		public void mark(int i) {}
		public void rewind(int i) {}
		public void recognitionException(RecognitionException e) {
			ParseTree ruleNode = (ParseTree)callStack.peek();
			ParseTree errorNode = new ParseTree(e);
			ruleNode.addChild(errorNode);
		}
		public void recovered() {}
		public void commence() {}
		public void terminate() {}
	}

	public Interpreter(Grammar grammar, IntStream input) {
		this.grammar = grammar;
		this.input = input;
	}

	public Token nextToken() {
		if ( grammar.type!=Grammar.LEXER ) {
			return null;
		}
		if ( input.LA(1)==CharStream.EOF ) {
			return Token.EOFToken;
		}
		int start = input.index();
		int charPos = ((CharStream)input).getCharPositionInLine();
		CommonToken token = null;
		loop:
		while (true) {
			try {
				token = scan(Grammar.TOKEN_RULENAME);
				break;
			}
			catch (RecognitionException re) {
				// report a problem and try for another
				reportScanError(re);
				continue loop;
			}
		}
		// the scan can only set type
		// we must set the line, and other junk here to make it a complete token
		int stop = input.index()-1;
		token.setLine(((CharStream)input).getLine());
		token.setStartIndex(start);
		token.setStopIndex(stop);
		token.setCharPositionInLine(charPos);
		return token;
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
	public void scan(String startRule, DebugEventListener actions)
		throws RecognitionException
	{
		if ( grammar.type!=Grammar.LEXER ) {
			return;
		}
		CharStream input = (CharStream)this.input;
		//System.out.println("scan("+startRule+",'"+input.substring(input.index(),input.size()-1)+"')");
		// Build NFAs/DFAs from the grammar AST if NFAs haven't been built yet
		if ( grammar.getRuleStartState(startRule)==null ) {
			if ( grammar.type==Grammar.LEXER ) {
				grammar.addArtificialMatchTokensRule();
			}
			grammar.createNFAs();
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

	public void parse(String startRule, DebugEventListener actions)
		throws RecognitionException
	{
		//System.out.println("parse("+startRule+")");
		// Build NFAs/DFAs from the grammar AST if NFAs haven't been built yet
		if ( grammar.getRuleStartState(startRule)==null ) {
			if ( grammar.type==Grammar.LEXER ) {
				grammar.addArtificialMatchTokensRule();
			}
			grammar.createNFAs();
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
		try {
			parse(startRule, actions);
		}
		catch (RecognitionException re) {
			// Errors are tracked via the ANTLRDebugInterface
			// Exceptions are used just to blast out of the parse engine
			// The error will be in the parse tree.
		}
		return actions.getTree();
	}

	protected void parseEngine(String startRule,
							   NFAState start,
							   NFAState stop,
							   IntStream input,
							   Stack ruleInvocationStack,
							   DebugEventListener actions)
		throws RecognitionException
	{
		if ( actions!=null ) {
			actions.enterRule(start.getEnclosingRule());
		}
		NFAState s = start;
		int t = input.LA(1);
		while ( s!=stop ) {
			//System.out.println("parse state "+s.stateNumber+" input="+grammar.getTokenName(t));
			// CASE 1: decision state
			if ( s.getDecisionNumber()>0 && grammar.getNumberOfAltsForDecisionNFA(s)>1 ) {
				// decision point, must predict and jump to alt
				DFA dfa = grammar.getLookaheadDFA(s.getDecisionNumber());
				/*
				System.out.println("decision: "+
								   dfa.getNFADecisionStartState().getDescription()+
								   " input="+grammar.getTokenName(t));
				*/
				int m = input.mark();
				int predictedAlt = predict(dfa);
				if ( predictedAlt == NFA.INVALID_ALT_NUMBER ) {
					String description = dfa.getNFADecisionStartState().getDescription();
					NoViableAltException nvae =
						new NoViableAltException(description,
												 dfa.getDecisionNumber(),
												 s.stateNumber,
												 input);
					if ( actions!=null ) {
						actions.recognitionException(nvae);
					}
					throw nvae;
				}
				input.rewind(m);
				int parseAlt =
					s.translateDisplayAltToWalkAlt(predictedAlt);
				/*
				System.out.println("predicted alt "+predictedAlt+", parseAlt "+
								   parseAlt);
				*/
				NFAState alt = grammar.getNFAStateForAltOfDecision(s, parseAlt);
				s = (NFAState)alt.transition(0).target;
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
			Label label = trans.label;
			// CASE 3: epsilon transition
			if ( label.isEpsilon() ) {
				// CASE 3a: rule invocation state
				if ( trans instanceof RuleClosureTransition ) {
					ruleInvocationStack.push(s);
					s = (NFAState)trans.target;
					if ( actions!=null ) {
						actions.enterRule(s.getEnclosingRule());
					}
				}
				// CASE 3b: plain old epsilon transition, just move
				else {
					s = (NFAState)trans.target;
				}
			}

			// CASE 4: match label on transition
			else if ( label.matches(t) ) {
				if ( actions!=null ) {
					if ( grammar.type == Grammar.PARSER ||
						 grammar.type == Grammar.COMBINED )
					{
						actions.consumeToken(((TokenStream)input).LT(1));
					}
				}
				s = (NFAState)s.transition(0).target;
				input.consume();
				t = input.LA(1);
			}

			// CASE 5: error condition; label is inconsistent with input
			else {
				if ( label.isAtom() ) {
					MismatchedTokenException mte =
						new MismatchedTokenException(label.getAtom(), input);
					if ( actions!=null ) {
						actions.recognitionException(mte);
					}
					throw mte;
				}
				else if ( label.isSet() ) {
					MismatchedSetException mse =
						new MismatchedSetException(label.getSet(), input);
					if ( actions!=null ) {
						actions.recognitionException(mse);
					}
					throw mse;
				}
				else {
					throw new RecognitionException(input); // unknown error
				}
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
		DFAState s = dfa.startState;
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
				if ( t.label.matches(c) ) {
					// take transition i
					s = (DFAState)t.target;
					input.consume();
					c = input.LA(1);
					continue dfaLoop;
				}
				if ( t.label.getAtom()==Label.EOT ) {
					eotTransition = t;
				}
			}
			if ( eotTransition!=null ) {
				s = (DFAState)eotTransition.target;
				continue dfaLoop;
			}
			/*
			ErrorManager.error(ErrorManager.MSG_NO_VIABLE_DFA_ALT,
							   s,
							   dfa.nfa.grammar.getTokenName(c));
			*/
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
	/*
	public void executeLexerAction(Token token, GrammarAST code) {
		System.out.println("action "+code.toStringTree());
		ActionInterpreter interp = new ActionInterpreter();
		try {
			interp.lexer_action(code,token);
		}
		catch (RecognitionException re) {
			ErrorManager.error(ErrorManager.MSG_BAD_ACTION_AST_STRUCTURE,
							   code,
							   re);
		}
	}
	*/

	public void reportScanError(RecognitionException re) {
		CharStream cs = (CharStream)input;
		// don't report to ANTLR tool itself; make people override to redirect
		System.err.println("problem matching token at "+
						   cs.getLine()+":"+cs.getCharPositionInLine());
	}
}
