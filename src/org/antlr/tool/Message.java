package org.antlr.tool;

import org.antlr.stringtemplate.StringTemplate;

/** The ANTLR code calls methods on ErrorManager to report errors etc...
 *  Rather than simply pass these arguments to the ANTLRErrorListener directly,
 *  create an object that encapsulates everything.  In this way, the error
 *  listener interface does not have to change when I add a new kind of
 *  error message.  I don't want to break a GUI for example every time
 *  I update the error system in ANTLR itself.
 *
 *  To get a printable error/warning message, call toString().
 */
public abstract class Message {
	public StringTemplate msgST;
	public int msgID;
	public Object arg;
	public Object arg2;
	public Throwable e;

	public Message() {
	}

	public Message(int msgID) {
		this(msgID, null, null);
	}

	public Message(int msgID, Object arg, Object arg2) {
		this.msgID = msgID;
		msgST = ErrorManager.getMessage(msgID);
		this.arg = arg;
		this.arg2 = arg2;
	}

	public StringTemplate getMessageTemplate() {
		return msgST;
	}
}
