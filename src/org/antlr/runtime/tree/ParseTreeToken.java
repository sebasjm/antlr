package org.antlr.runtime.tree;

import org.antlr.runtime.Token;

/** A leaf node of a parse tree. */
public class ParseTreeToken extends ParseTree {
	public ParseTreeToken(Token token) {
		super(token);
	}
}
