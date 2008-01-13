package org.antlr.runtime;

/** An extra token while parsing a TokenStream */
public class UnwantedTokenException extends MismatchedTokenException {
	public UnwantedTokenException(int expecting, IntStream input) {
		super(expecting, input);
	}

	public Token getUnexpectedToken() {
		return token;
	}

	public String toString() {
		return "UnwantedTokenException(found="+token.getText()+", expected "+
			   expecting+")";
	}
}
