package org.antlr.tool;

import org.antlr.stringtemplate.StringTemplate;

public class GrammarMessage extends Message {
	protected Object arg;
	protected Object arg2;
	protected Throwable e;
	protected Grammar g;
	protected int line;
	protected int col;
	protected String fileName;

	public GrammarMessage(int msgID,
						  Grammar g,
						  String fileName,
						  int line,
						  int col)
	{
		this(msgID, g, fileName, line, col, null, null);
	}

	public GrammarMessage(int msgID,
						  Grammar g,
						  String fileName,
						  int line,
						  int col,
						  Object arg)
	{
		this(msgID, g, fileName, line, col, arg, null);
	}

	public GrammarMessage(int msgID,
						  Grammar g,
						  String fileName,
						  int line,
						  int col,
						  Object arg,
						  Object arg2)
	{
		super(msgID);
		this.g = g;
		this.fileName = fileName;
		this.line = line;
		this.col = col;
		this.arg = arg;
		this.arg2 = arg2;
	}

	public String toString() {
		StringTemplate st = getMessage();
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
