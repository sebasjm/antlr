package org.antlr.runtime.debug;

import org.antlr.runtime.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.PrintStream;

public class DebugEventSocketProxy implements DebugEventListener {
	protected int port;
	protected ServerSocket serverSocket;
	protected Socket socket;
	PrintStream out;


	public DebugEventSocketProxy(int port) {
		this.port = port;
	}

	public void handshake() throws IOException {
		if ( serverSocket==null ) {
			serverSocket = new ServerSocket(port);
			socket = serverSocket.accept();
			out = new PrintStream(socket.getOutputStream());
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

	public void enterRule(String ruleName) {
	}

	public void enterAlt(int alt) {
	}

	public void exitRule(String ruleName) {
	}

	public void enterSubRule() {
	}

	public void exitSubRule() {
	}

	public void consumeToken(Token t) {
	}

	public void LT(int i) {
	}

	public void location(int line, int pos) {
	}

	public void recognitionException(RecognitionException e) {
	}

	public void recovered(Token t) {
	}
}

