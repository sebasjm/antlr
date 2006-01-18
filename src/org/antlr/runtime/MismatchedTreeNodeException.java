package org.antlr.runtime;

import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.Tree;

/**
 */
public class MismatchedTreeNodeException extends RecognitionException {
	public int expecting;
	public Object foundNode;

	public MismatchedTreeNodeException() {
	}

	public MismatchedTreeNodeException(int expecting, TreeNodeStream input) {
		super(input);
		if ( input.LT(1) instanceof Tree ) {
			Tree t = (Tree)input.LT(1);
			line = t.getLine();
			charPositionInLine = t.getCharPositionInLine();
			// TODO: if DOWN/UP, there is no line info currently
		}
		this.foundNode = input.LT(1);
		this.expecting = expecting;
	}

	public String toString() {
		return "MismatchedTreeNodeException("+getUnexpectedType()+"!="+expecting+")";
	}
}
