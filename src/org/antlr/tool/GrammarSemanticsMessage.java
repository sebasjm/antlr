package org.antlr.tool;

import org.antlr.stringtemplate.StringTemplate;
import antlr.Token;

/** A problem with the symbols and/or meaning of a grammar such as rule
 *  redefinition.
 */
public class GrammarSemanticsMessage extends Message {
	public Grammar g;
	/** Most of the time, we'll have a token such as an undefined rule ref
	 *  and so this will be set.
	 */
	public Token offendingToken;

	public GrammarSemanticsMessage(int msgID,
						  Grammar g,
						  Token offendingToken)
	{
		this(msgID,g,offendingToken,null,null);
	}

	public GrammarSemanticsMessage(int msgID,
						  Grammar g,
						  Token offendingToken,
						  Object arg)
	{
		this(msgID,g,offendingToken,arg,null);
	}

	public GrammarSemanticsMessage(int msgID,
						  Grammar g,
						  Token offendingToken,
						  Object arg,
						  Object arg2)
	{
		super(msgID,arg,arg2);
		this.g = g;
		this.offendingToken = offendingToken;
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
