package org.antlr.runtime.debug;

import org.antlr.runtime.*;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.Socket;

public class RemoteDebugEventSocketListener implements Runnable {
	DebugEventListener listener;
	String machine;
	int port;
	public RemoteDebugEventSocketListener(DebugEventListener listener,
										  String machine,
										  int port)
	{
		this.listener = listener;
		this.machine = machine;
		this.port = port;
	}

	protected void eventHandler() {
		Socket channel = null;
		DataInputStream din = null;
		try {
			channel = new Socket(machine, port);
			InputStream in = channel.getInputStream();
			din = new DataInputStream(in);
			String line = din.readLine();
			while ( line!=null ) {
				System.out.println("client said: "+line);
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

	/** Create a thread to listen to the remote running recognizer */
	public void start() {
		Thread t = new Thread(this);
		t.start();
	}

	public void run() {
		eventHandler();
	}
}

