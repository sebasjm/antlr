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
		out.println("terminate");
		ack();
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
		txt = txt.replaceAll("%","%25");   // escape all escape char ;)
		txt = txt.replaceAll("\n","%0A");  // escape \n
		txt = txt.replaceAll("\r","%0D");  // escape \r
		buf.append(txt);
		return buf.toString();
	}

}

