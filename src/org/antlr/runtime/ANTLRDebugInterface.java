package org.antlr.runtime;

public interface ANTLRDebugInterface {
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

	/** An input token was consumed; matched by any kind of element. */
	public void consumeToken(Token token);

	/** An input char was consumed; matched by any kind of element.
	 *  I separate this from consumeToken(Token) so that it's easier for the
	 *  debuggers to know what kind of thing they are debugging; though the
	 *  constructor for a debugger should probably make it clear what kind
	 *  of parser it is.
	 */
	public void consumeChar(int c);

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
}
