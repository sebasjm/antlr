package org.antlr.runtime.tree;

import org.antlr.runtime.Token;
import org.antlr.runtime.CommonToken;

/** What does a tree look like?  ANTLR has a number of support classes
 *  such as CommonTreeNodeStream that work on these kinds of trees.  You
 *  don't have to make your trees implement this interface, but if you do,
 *  you'll be able to use more support code.
 *
 *  NOTE: When constructing trees, ANTLR can build any kind of tree; it can
 *  even use Token objects as trees if you add a child list to your tokens.
 *
 *  This is a tree node without any payload; just navigation and factory stuff.
 */
public interface Tree {
	public static final Tree INVALID_NODE = new CommonTree(Token.INVALID_TOKEN);

	Tree getChild(int i);

	int getChildCount();

	void addChild(Tree t);

	boolean isNil();

	Tree dupTree();

	Tree dupNode();

	/** Return a token type; needed for tree parsing */
	int getType();

	String toStringTree();

	String toString();
}
