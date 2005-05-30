/*
 [The "BSD licence"]
 Copyright (c) 2005 Terence Parr
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

import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class DebugEventSocketProxy implements DebugEventListener {
	public static final int DEFAULT_DEBUGGER_PORT = 2005;
	protected int port = DEFAULT_DEBUGGER_PORT;
	protected ServerSocket serverSocket;
	protected Socket socket;
	PrintWriter out;
	BufferedReader in;

	public DebugEventSocketProxy() {
		this(DEFAULT_DEBUGGER_PORT);
	}

	public DebugEventSocketProxy(int port) {
		this.port = port;
	}

	public void handshake() throws IOException {
		if ( serverSocket==null ) {
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			socket.setTcpNoDelay(true);
			OutputStream os = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os, "UTF8");
			out = new PrintWriter(new BufferedWriter(osw));
			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is, "UTF8");
			in = new BufferedReader(isr);
			transmit("ANTLR 3.0ea1");
		}
	}

	public void commence() {
		// don't bother sending event; listener will trigger upon connection
	}

	public void terminate() {
		transmit("terminate");
		out.close();
		try {
			socket.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.err);
		}
	}

	protected void ack() {
		try {
			String ack = in.readLine();
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.err);
		}

	}

	protected void transmit(String event) {
		out.println(event);
		out.flush();
		ack();
	}

	public void enterRule(String ruleName) {
		transmit("enterRule "+ruleName);
	}

	public void enterAlt(int alt) {
		transmit("enterAlt "+alt);
	}

	public void exitRule(String ruleName) {
		transmit("exitRule "+ruleName);
	}

	public void enterSubRule(int decisionNumber) {
		transmit("enterSubRule "+decisionNumber);
	}

	public void exitSubRule(int decisionNumber) {
		transmit("exitSubRule "+decisionNumber);
	}

	public void enterDecision(int decisionNumber) {
		transmit("enterDecision "+decisionNumber);
	}

	public void exitDecision(int decisionNumber) {
		transmit("exitDecision "+decisionNumber);
	}

	public void consumeToken(Token t) {
		String buf = serializeToken(t);
		transmit("consumeToken "+buf);
	}

	public void consumeHiddenToken(Token t) {
		String buf = serializeToken(t);
		transmit("consumeHiddenToken "+buf);
	}

	public void LT(int i, Token t) {
        if(t != null)
            transmit("LT "+i+" "+serializeToken(t));
	}

	public void mark(int i) {
		transmit("mark "+i);
	}

	public void rewind(int i) {
		transmit("rewind "+i);
	}

	public void location(int line, int pos) {
		transmit("location "+line+" "+pos);
	}

	public void recognitionException(RecognitionException e) {
		StringBuffer buf = new StringBuffer(50);
		buf.append("exception ");
		buf.append(e.getClass().getName());
		// dump only the data common to all exceptions for now
		buf.append(" ");
		buf.append(e.index);
		buf.append(" ");
		buf.append(e.line);
		buf.append(" ");
		buf.append(e.charPositionInLine);
		transmit(buf.toString());
	}

	public void beginResync() {
		transmit("beginResync");
	}

	public void endResync() {
		transmit("endResync");
	}

	public void semanticPredicate(boolean result, String predicate) {
		predicate = escapeNewlines(predicate);
		StringBuffer buf = new StringBuffer(50);
		buf.append("semanticPredicate ");
		buf.append(result);
		buf.append(" ");
		buf.append(predicate);
		transmit(buf.toString());
	}

	protected String serializeToken(Token t) {
		StringBuffer buf = new StringBuffer(50);
		buf.append(t.getTokenIndex()); buf.append(' ');
		buf.append(t.getType()); buf.append(' ');
		buf.append(t.getChannel()); buf.append(' ');
		buf.append(t.getLine()); buf.append(' ');
		buf.append(t.getCharPositionInLine()); buf.append(" \"");
		String txt = t.getText();
		if ( txt==null ) {
			txt = "";
		}
		// escape \n and \r all text for token appears to exist on one line
		// this escape is slow but easy to understand
		txt = escapeNewlines(txt);
		buf.append(txt);
		return buf.toString();
	}

	protected String escapeNewlines(String txt) {
		txt = txt.replaceAll("%","%25");   // escape all escape char ;)
		txt = txt.replaceAll("\n","%0A");  // escape \n
		txt = txt.replaceAll("\r","%0D");  // escape \r
		return txt;
	}
}

