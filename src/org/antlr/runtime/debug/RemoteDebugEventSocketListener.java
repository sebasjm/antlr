package org.antlr.runtime.debug;

import org.antlr.runtime.*;
import org.antlr.tool.ErrorManager;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.StringTokenizer;

public class RemoteDebugEventSocketListener implements Runnable {
	static final int MAX_EVENT_ELEMENTS = 7;
	DebugEventListener listener;
	String machine;
	int port;
	Socket channel = null;
	DataInputStream din = null;
	PrintStream out;

	public static class ProxyToken extends Token {
		int index;
		int type;
		int channel;
		int line;
		int charPos;
		String text;
		public ProxyToken(int index, int type, int channel,
						  int line, int charPos, String text)
		{
			this.index = index;
			this.type = type;
			this.channel = channel;
			this.line = line;
			this.charPos = charPos;
			this.text = text;
		}
		public String getText() {
			return text;
		}
		public int getType() {
			return type;
		}
		public int getLine() {
			return line;
		}
		public void setLine(int line) {
			this.line = line;
		}
		public int getCharPositionInLine() {
			return charPos;
		}
		public void setCharPositionInLine(int pos) {
			this.charPos = pos;
		}
		public int getChannel() {
			return channel;
		}
		public void setChannel(int channel) {
			this.channel = channel;
		}
		public int getTokenIndex() {
			return index;
		}
		public void setTokenIndex(int index) {
			this.index = index;
		}
	}

	public RemoteDebugEventSocketListener(DebugEventListener listener,
										  String machine,
										  int port)
	{
		this.listener = listener;
		this.machine = machine;
		this.port = port;
	}

	protected void eventHandler() {
		try {
			handshake();
			String line = din.readLine();
			while ( line!=null ) {
				System.out.println(line);
				dispatch(line);
				ack();
				line = din.readLine();
			}
			din.close(); din = null;
			channel.close(); channel=null;
		}
		catch (Exception e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		}
		finally {
			if ( din!=null ) {
				try {din.close();} catch (IOException ioe) {
					System.err.println(ioe);
				}
			}
			if ( channel!=null ) {
				try {channel.close();} catch (IOException ioe) {
					System.err.println(ioe);
				}
			}
		}
	}

	protected void handshake() throws IOException {
		channel = new Socket(machine, port);
		InputStream in = channel.getInputStream();
		din = new DataInputStream(in);
		out = new PrintStream(channel.getOutputStream());
		String line = din.readLine();
		if ( line!=null ) {
			System.out.println("handshake: "+line);
		}
	}

	protected void ack() {
        out.println("ack");
	}

	protected void dispatch(String line) {
		String[] elements = getEventElements(line);
		if ( elements[0].equals("enterRule") ) {
			listener.enterRule(elements[1]);
		}
		else if ( elements[0].equals("exitRule") ) {
			listener.exitRule(elements[1]);
		}
		else if ( elements[0].equals("enterAlt") ) {
			listener.enterAlt(Integer.parseInt(elements[1]));
		}
		else if ( elements[0].equals("enterSubRule") ) {
			listener.enterSubRule(Integer.parseInt(elements[1]));
		}
		else if ( elements[0].equals("exitSubRule") ) {
			listener.exitSubRule(Integer.parseInt(elements[1]));
		}
		else if ( elements[0].equals("location") ) {
			listener.location(Integer.parseInt(elements[1]),
							  Integer.parseInt(elements[2]));
		}
		else if ( elements[0].equals("consumeToken") ) {
			String indexS = elements[1];
			String typeS = elements[2];
			String channelS = elements[3];
			String lineS = elements[4];
			String posS = elements[5];
			String text = elements[6];
			ProxyToken t =
				new ProxyToken(Integer.parseInt(indexS),
							   Integer.parseInt(typeS),
							   Integer.parseInt(channelS),
							   Integer.parseInt(lineS),
							   Integer.parseInt(posS),
							   text);
			listener.consumeToken(t);
		}
		else if ( elements[0].equals("LT") ) {
			listener.LT(Integer.parseInt(elements[1]));
		}
		else if ( elements[0].equals("mark") ) {
			listener.LT(Integer.parseInt(elements[1]));
		}
		else if ( elements[0].equals("rewind") ) {
			listener.LT(Integer.parseInt(elements[1]));
		}
		else if ( elements[0].equals("exception") ) {
			RecognitionException e = null;
			if ( elements[1].equals("RecognitionException") ) {
				e = new RecognitionException(null);
			}
			listener.recognitionException(e);
		}
		else if ( elements[0].equals("recovered") ) {
			listener.recovered();
		}
		else {
			System.err.println("unknown debug event: "+line);
		}
	}

	/** Create a thread to listen to the remote running recognizer */
	public void start() {
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {
		eventHandler();
	}

	// M i s c

	public String[] getEventElements(String event) {
		String[] elements = new String[MAX_EVENT_ELEMENTS];
		String str = null; // a string element if present (must be last)
		try {
			int firstQuoteIndex = event.indexOf('"');
			if ( firstQuoteIndex>=0 ) {
				// treat specially; has a string argument like "a comment\n
				// Note that the string is terminated by \n not end quote.
				// Easier to parse that way.
				String eventWithoutString = event.substring(0,firstQuoteIndex);
				str = event.substring(firstQuoteIndex+1,event.length());
				event = eventWithoutString;
			}
			StringTokenizer st = new StringTokenizer(event, " \t", false);
			int i = 0;
			while ( st.hasMoreTokens() ) {
				elements[i] = st.nextToken();
				i++;
			}
			if ( str!=null ) {
				elements[i] = str;
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return elements;
	}

}

