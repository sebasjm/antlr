package org.antlr.tool;

import org.antlr.stringtemplate.StringTemplate;

/** The ANTLR code calls methods on ErrorManager to report errors etc...
 *  Rather than simply pass these arguments to the ANTLRErrorListener directly,
 *  create an object that encapsulates everything.  In this way, the error
 *  listener interface does not have to change when I add a new kind of
 *  error message.  I don't want to break a GUI for example every time
 *  I update the error system in ANTLR itself.
 */
public abstract class Message {
	StringTemplate msgST;
	public Message() {
	}
	public Message(int msgID) {
		msgST = ErrorManager.getMessage(msgID);
	}
	public StringTemplate getMessage() {
		return msgST;
	}
}
