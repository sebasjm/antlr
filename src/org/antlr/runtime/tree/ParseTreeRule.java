package org.antlr.runtime.tree;

/** An interior node of a parse tree. */
public class ParseTreeRule extends ParseTree {
	protected String ruleName;
	public ParseTreeRule(String ruleName) {
		this.ruleName = ruleName;
	}
}
