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

import java.io.IOException;

public class DebugParser extends Parser {

	/** The default debugger mimics the traceParser behavior of ANTLR 2.x */
	public class TraceDebugger implements DebugEventListener {
		protected int level = 0;
		public void enterRule(String ruleName) {
			for (int i=1; i<=level; i++) {System.out.print(" ");}
			System.out.println("> "+ruleName+" LT(1)="+input.LT(1).toString());
			level++;
		}
		public void exitRule(String ruleName) {
			level--;
			for (int i=1; i<=level; i++) {System.out.print(" ");}
			System.out.println("< "+ruleName+" LT(1)="+input.LT(1).toString());
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
		public void recover() {}
		public void recovered() {}
		public void commence() {}
		public void terminate() {}
	}

	/** Who to notify when events in the parser occur. */
	protected DebugEventListener dbg = null;

	/** Create a normal parser except wrap the token stream in a debug
	 *  proxy that fires consume events.
	 */
	public DebugParser(TokenStream input, DebugEventListener dbg) {
		super(new DebugTokenStream(input,dbg));
		setDebugListener(dbg);
	}

	public DebugParser(TokenStream input) {
		this(input, DebugEventSocketProxy.DEFAULT_DEBUGGER_PORT);
	}

	/** Create a proxy to marshall events across socket to another
	 *  listener.  This constructor returns after handshaking with
	 *  debugger so programmer does not have to manually invoke handshake.
	 */
	public DebugParser(TokenStream input, int port) {
		super(new DebugTokenStream(input,null));
		DebugEventSocketProxy proxy = new DebugEventSocketProxy(port);
		setDebugListener(proxy);
		try {
			proxy.handshake();
		}
		catch (IOException ioe) {
			reportError(ioe);
		}
	}

	/** Provide a new debug event listener for this parser.  Notify the
	 *  input stream too that it should send events to this listener.
	 */
	public void setDebugListener(DebugEventListener dbg) {
		if ( input instanceof DebugTokenStream ) {
			((DebugTokenStream)input).setDebugListener(dbg);
		}
		this.dbg = dbg;
	}

	public void reportError(IOException e) {
		System.err.println("problem with debugger: "+e);
		e.printStackTrace(System.err);
	}

	public void recover(RecognitionException re, org.antlr.runtime.BitSet follow) {
		dbg.recover();
		try {
			super.recover(re, follow);
		}
		finally {
			dbg.recovered();
		}
	}

	public void recoverFromMismatchedToken(MismatchedTokenException mte,
										   int ttype,
										   org.antlr.runtime.BitSet follow)
		throws MismatchedTokenException
	{
		dbg.recognitionException(mte);
		dbg.recover();
		try {
			super.recoverFromMismatchedToken(mte,ttype,follow);
		}
		finally {
			dbg.recovered();
		}
	}

	public void recoverFromExtraToken(MismatchedTokenException mte,
									  int ttype,
									  org.antlr.runtime.BitSet follow)
		throws MismatchedTokenException
	{
		dbg.recognitionException(mte);
		dbg.recover();
		try {
			super.recoverFromExtraToken(mte,ttype,follow);
		}
		finally {
			dbg.recovered();
		}
	}

	public void recoverFromMismatchedSet(RecognitionException mte,
										 org.antlr.runtime.BitSet follow)
		throws RecognitionException
	{
		dbg.recognitionException(mte);
		dbg.recover();
		try {
			super.recoverFromMismatchedSet(mte,follow);
		}
		finally {
			dbg.recovered();
		}
	}

}
