package org.antlr.runtime.debug;

import org.antlr.runtime.*;

public interface DebugEventListener {
	/** The parser has just entered a rule.  No decision has been made about
	 *  which alt is predicted.  This is fired AFTER init actions have been
	 *  executed.  Attributes are defined and available etc...
	 */
	public void enterRule(String ruleName);

	/** Because rules can have lots of alternatives, it is very useful to
	 *  know which alt you are entering.  This is 1..n for n alts.
	 */
	public void enterAlt(int alt);

	/** This is the last thing executed before leaving a rule.  It is
	 *  executed even if an exception is thrown.  This is triggered after
	 *  error reporting and recovery have occurred.  This implies an
	 *  "exitAlt" event.
	 */
	public void exitRule(String ruleName);

	/** Track entry into any (...) subrule other EBNF construct */
	public void enterSubRule(int decisionNumber);

	public void exitSubRule(int decisionNumber);

	/** Every decision, fixed k or arbitrary, has an enter/exit event
	 *  so that a GUI can easily track what LT/consume events are
	 *  associated with prediction.  You will see a single enter/exit
	 *  subrule but multiple enter/exit decision events, one for each
	 *  loop iteration.
	 */ 
	public void enterDecision(int decisionNumber);

	public void exitDecision(int decisionNumber);

	/** An input token was consumed; matched by any kind of element.
	 *  Trigger after the token was matched by things like match(), matchAny().
	 */
	public void consumeToken(Token t);

	/** An off-channel input token was consumed.
	 *  Trigger after the token was matched by things like match(), matchAny().
	 *  (unless of course the hidden token is first stuff in the input stream).
	 */
	public void consumeHiddenToken(Token t);

	/** Somebody (anybody) looked ahead.  Note that this actually gets
	 *  triggered by both LA and LT calls.  The debugger will want to know
	 *  which Token object was examined.  Like consumeToken, this indicates
	 *  what token was seen at that depth.  A remote debugger cannot look
	 *  ahead into a file it doesn't have so LT events must pass the token
	 *  even if the info is redundant.
	 */
	public void LT(int i, Token t);

	/** The parser is going to look arbitrarily ahead starting with token i. */
	public void mark(int i);

	/** After an arbitrairly long lookahead as with a cyclic DFA (or with
	 *  any backtrack), this informs the debugger that the current token
	 *  is now rewound to index i.
	 */
	public void rewind(int i);

	/** To watch a parser move through the grammar, the parser needs to
	 *  inform the debugger what line/charPos it is passing in the grammar.
	 *  For now, this does not know how to switch from one grammar to the
	 *  other and back for island grammars etc...
	 *
	 *  This should also allow breakpoints because the debugger can stop
	 *  the parser whenever it hits this line/pos.
	 */
	public void location(int line, int pos);

	/** A recognition exception occurred such as NoViableAltException.  I made
	 *  this a generic event so that I can alter the exception hierachy later
	 *  without having to alter all the debug objects.
	 */
	public void recognitionException(RecognitionException e);

	/** Indicates that the parser was in an error state and has now recovered
	 *  in that a token, t, was successfully matched.  This will be useful
	 *  in a gui where you want to probably grey out tokens that are consumed
	 *  but not matched to anything in grammar.  Anything between an exception
	 *  and recovered() was tossed out by the parser.
	 */
	public void recovered();

	/** Announce that parsing has begun.  Not strictly useful except for
	 *  sending events over a socket.  A GUI for example will launch a thread
	 *  to connect and communicate with a remote parser.  The thread will want
	 *  to notify the GUI when a connection is made.
	 */
	public void commence();

	/** Parsing is over; successfully or not.  Mostly useful for telling
	 *  remote debugging listeners that it's time to quit.  The invoker
	 *  of the parser must call this not the parser because any rule
	 *  can be a start symbol.
	 */
	public void terminate();
}
