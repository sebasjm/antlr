package org.antlr.runtime;

/** We were expecting a token but it's not found.  The current token
 *  is actually what we wanted next.
 */
public class MissingTokenException extends MismatchedTokenException {
	public MissingTokenException(int expecting, IntStream input) {
		super(expecting, input);
	}

	public int getMissingType() {
		return expecting;
	}

	public String toString() {
		return "MissingTokenException(expected "+expecting+")";
	}
}
