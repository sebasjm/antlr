package org.antlr.runtime;

import org.antlr.stringtemplate.StringTemplate;

/** Rules can return start/stop info as well as possible trees and templates */
public class RuleReturnScope {
	/** Return the start token or tree */
	public Object getStart() { return null; }
	/** Return the stop token or tree */
	public Object getStop() { return null; }
	/** Has a value potentially if output=AST; */
	public Object getTree() { return null; }
	/** Has a value potentially if output=template; */
	public StringTemplate getTemplate() { return null; }
}
