package org.antlr.runtime.debug;

import org.antlr.runtime.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.DataInputStream;

public class DebugEventSocketProxy implements DebugEventListener {
	public static final int DEFAULT_DEBUGGER_PORT = 2005;
	protected int port = DEFAULT_DEBUGGER_PORT;
	protected ServerSocket serverSocket;
	protected Socket socket;
	PrintStream out;
	DataInputStream in;

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
			out = new PrintStream(socket.getOutputStream());
			in = new DataInputStream(socket.getInputStream());
			out.println("ANTLR 3.0 parser");
		}
	}

	public void terminate() {
		out.close();
		try {
			socket.close();
		}
		catch (IOException ioe) {
			System.err.println(ioe);
		}
	}

	protected void ack() {
		try {
			String ack = in.readLine();
		}
		catch (IOException ioe) {
			System.err.println("didn't receive ack");
		}

	}

	public void enterRule(String ruleName) {
		out.println("enterRule "+ruleName);
		ack();
	}

	public void enterAlt(int alt) {
		out.println("enterAlt "+alt);
		ack();
	}

	public void exitRule(String ruleName) {
		out.println("exitRule "+ruleName);
		ack();
	}

	public void enterSubRule(int decisionNumber) {
		out.println("enterSubRule "+decisionNumber);
		ack();
	}

	public void exitSubRule(int decisionNumber) {
		out.println("exitSubRule "+decisionNumber);
		ack();
	}

	public void consumeToken(Token t) {
		StringBuffer buf = new StringBuffer(50);
		buf.append(t.getTokenIndex()); buf.append(' ');
		buf.append(t.getType()); buf.append(' ');
		buf.append(t.getChannel()); buf.append(' ');
		buf.append(t.getLine()); buf.append(' ');
		buf.append(t.getCharPositionInLine()); buf.append(" \"");
		buf.append(t.getText());
		out.println("consumeToken "+buf);
		ack();
	}

	public void LT(int i) {
		out.println("LT "+i);
		ack();
	}

	public void mark(int i) {
		out.println("mark "+i);
		ack();
	}

	public void rewind(int i) {
		out.println("rewind "+i);
		ack();
	}

	public void location(int line, int pos) {
		out.println("location "+line+" "+pos);
		ack();
	}

	public void recognitionException(RecognitionException e) {
		out.println("exception");
		ack();
	}

	public void recovered() {
		out.println("recovered");
		ack();
	}
}

