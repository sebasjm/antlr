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
package org.antlr.runtime.debug;

import org.antlr.runtime.*;

public class DebugTokenStream implements TokenStream {
	protected DebugEventListener dbg;
	protected TokenStream input;
	protected boolean firstConsume = true;

	public DebugTokenStream(TokenStream input, DebugEventListener dbg) {
		this.input = input;
		setDebugListener(dbg);
	}

	public void setDebugListener(DebugEventListener dbg) {
		this.dbg = dbg;
	}

	public void consume() {
		if ( firstConsume ) {
			// consume all initial off channel tokens
			int firstOnChannelTokenIndex = input.index();
			for (int i=0; i<firstOnChannelTokenIndex; i++) {
				dbg.consumeHiddenToken(input.get(i));
			}
			firstConsume = false;
		}
		int a = input.index();
		Token t = input.LT(1);
		input.consume();
		int b = input.index();
		dbg.consumeToken(t);
		if ( b>a+1 ) {
			// then we consumed more than one token; must be off channel tokens
			for (int i=a+1; i<b; i++) {
				dbg.consumeHiddenToken(input.get(i));
			}
		}
	}

	public int LA(int i) {
		dbg.LT(i, input.LT(i));
		return input.LA(i);
	}

	public Token get(int i) {
		return input.get(i);
	}

	public int mark() {
		int m = input.index();
		dbg.mark(m);
		return input.mark();
	}

	public int index() {
		return input.index();
	}

	public void rewind(int marker) {
		dbg.rewind(marker);
		input.rewind(marker);
	}

	public int size() {
		return input.size();
	}

	public Token LT(int i) {
		dbg.LT(i, input.LT(i));
		return input.LT(i);
	}

	public TokenSource getTokenSource() {
		return input.getTokenSource();
	}
}
