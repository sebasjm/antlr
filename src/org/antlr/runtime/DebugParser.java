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
package org.antlr.runtime;

public class DebugParser extends Parser {

	static class DummyDebugger implements ANTLRDebugInterface {
		public void enterRule(String ruleName) {}
		public void enterAlt(int alt) {}
		public void exitRule(String ruleName) {}
		public void enterSubRule() {}
		public void exitSubRule() {}
		public void location(int line, int pos) {}
		public void consumeToken(Token token) {}
		public void recognitionException(RecognitionException e) {}
		public void recovered(Token t) {}
	}

	/** Who to notify when events in the parser occur. */
	protected ANTLRDebugInterface dbg = null;

	/** Create a normal parser except wrap the token stream in a debug
	 *  proxy that fires consume events.
	 */
	public DebugParser(TokenStream input, ANTLRDebugInterface dbg) {
		super(new DebugTokenStream(dbg,input));
		setDebugListener(dbg);
	}

	public DebugParser(TokenStream input) {
		this(input, new DummyDebugger());
	}

	public void setDebugListener(ANTLRDebugInterface dbg) {
		this.dbg = dbg;
	}

	public void match(int ttype, BitSet follow) throws MismatchedTokenException {
		boolean before = this.errorRecovery;
		Token t = input.LT(1);
		super.match(ttype, follow);
		boolean after = this.errorRecovery;
		// if was in recovery and is not now, trigger recovered event
		if ( before && !after ) {
			dbg.recovered(t);
		}
	}

	public void reportError(RecognitionException e) {
		super.reportError(e);
		dbg.recognitionException(e);
	}

}
