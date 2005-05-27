package org.antlr.tool;

import org.antlr.stringtemplate.StringTemplate;
import antlr.Token;

/** A problem with the syntax of your antlr grammar such as
 *  "The '{' came as a complete surprise to me at this point in your program"
 */
public class GrammarSyntaxMessage extends Message {
	public Grammar g;
	/** Most of the time, we'll have a token and so this will be set. */
	public Token offendingToken;
	public antlr.RecognitionException exception;

	public GrammarSyntaxMessage(int msgID,
								Token offendingToken,
								antlr.RecognitionException exception)
	{
		this(msgID,offendingToken,null,exception);
	}

	public GrammarSyntaxMessage(int msgID,
								Token offendingToken,
								Object arg,
								antlr.RecognitionException exception)
	{
		super(msgID, arg, null);
		this.offendingToken = offendingToken;
		this.exception = exception;
	}

	public String toString() {
		int line = 0;
		int col = 0;
		if ( offendingToken!=null ) {
			line = offendingToken.getLine();
			col = offendingToken.getColumn();
		}
		String fileName = g.getFileName();
		StringTemplate st = getMessageTemplate();
		if ( arg!=null ) {
			st.setAttribute("arg", arg);
		}
		if ( arg2!=null ) {
			st.setAttribute("arg2", arg2);
		}
		if ( fileName!=null ) {
			st.setAttribute("file", fileName);
		}
		st.setAttribute("line", new Integer(line));
		st.setAttribute("col", new Integer(col));
		return st.toString();
	}
}
