package org.antlr.tool;

import org.antlr.stringtemplate.StringTemplate;

/** A generic message from the tool such as "file not found" type errors; there
 *  is no reason to create a special object for each error unlike the grammar
 *  errors, which may be rather complex.
 *
 *  Sometimes you need to pass in a filename or something to say it is "bad".
 *  Allow a generic object to be passed in and the string template can deal
 *  with just printing it or pulling a property out of it.
 *
 *  TODO what to do with exceptions?  Want stack trace for internal errors?
 */
public class ToolMessage extends Message {

	public ToolMessage(int msgID) {
		super(msgID, null, null);
	}
	public ToolMessage(int msgID, Object arg) {
		super(msgID, arg, null);
	}
	public ToolMessage(int msgID, Exception e) {
		super(msgID);
		this.e = e;
	}
	public ToolMessage(int msgID, Object arg, Object arg2) {
		super(msgID, arg, arg2);
	}
	public ToolMessage(int msgID, Object arg, Throwable e) {
		super(msgID,arg,null);
		this.e = e;
	}
	public String toString() {
		StringTemplate st = getMessageTemplate();
		if ( arg!=null ) {
			st.setAttribute("arg", arg);
		}
		if ( arg2!=null ) {
			st.setAttribute("arg2", arg2);
		}
		if ( e!=null ) {
			st.setAttribute("exception", e);
			st.setAttribute("stackTrace", e.getStackTrace());
		}
		return st.toString();
	}
}
