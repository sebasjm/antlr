package org.antlr.runtime.debug;

import org.antlr.runtime.Token;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.IntStream;
import org.antlr.runtime.TokenStream;

/** The default debugger mimics the traceParser behavior of ANTLR 2.x */
public class Tracer implements DebugEventListener {
	public IntStream input;
	protected int level = 0;

	public Tracer(IntStream input) {
		this.input = input;
	}

	public void enterRule(String ruleName) {
		for (int i=1; i<=level; i++) {System.out.print(" ");}
		System.out.println("> "+ruleName+" lookahead(1)="+getInputSymbol(1));
		level++;
	}

	public void exitRule(String ruleName) {
		level--;
		for (int i=1; i<=level; i++) {System.out.print(" ");}
		System.out.println("< "+ruleName+" lookahead(1)="+getInputSymbol(1));
	}

	public void enterAlt(int alt) {}
	public void enterSubRule(int decisionNumber) {}
	public void exitSubRule(int decisionNumber) {}
	public void enterDecision(int decisionNumber) {}
	public void exitDecision(int decisionNumber) {}
	public void location(int line, int pos) {}
	public void consumeToken(Token token) {}
	public void consumeHiddenToken(Token token) {}
	public void LT(int i, Token t) {}
	public void mark(int i) {}
	public void rewind(int i) {}
	public void recognitionException(RecognitionException e) {}
	public void beginResync() {}
	public void endResync() {}
	public void commence() {}
	public void terminate() {}

	public Object getInputSymbol(int k) {
		if ( input instanceof TokenStream ) {
			return ((TokenStream)input).LT(k);
		}
		return new Character((char)input.LA(k));
	}
}


