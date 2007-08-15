package org.antlr.runtime.tree;

import org.antlr.runtime.Token;

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

	// Tree tracks parent and child index now > 3.0

	public Tree getParent();

	public void setParent(Tree t);

	/** This node is what child index? 0..n-1 */
	public int getChildIndex();

	public void setChildIndex(int index);

	/** Set the parent and child index values for all children */
	public void freshenParentAndChildIndexes();

	/** Add t as a child to this node.  If t is null, do nothing.  If t
	 *  is nil, add all children of t to this' children.
	 */
	void addChild(Tree t);

	/** Set ith child (0..n-1) to t; t must be non-null and non-nil node */
	public void setChild(int i, Tree t);

	public Object deleteChild(int i);

	/** Delete children from start to stop and replace with t even if t is
	 *  a list (nil-root tree).  num of children can increase or decrease.
	 *  For huge child lists, inserting children can force walking rest of
	 *  children to set their childindex; could be slow.
	 */
	public void replaceChildren(int startChildIndex, int stopChildIndex, Object t);	

	/** Indicates the node is a nil node but may still have children, meaning
	 *  the tree is a flat list.
	 */
	boolean isNil();

	/**  What is the smallest token index (indexing from 0) for this node
	 *   and its children?
	 */
	int getTokenStartIndex();

	void setTokenStartIndex(int index);

	/**  What is the largest token index (indexing from 0) for this node
	 *   and its children?
	 */
	int getTokenStopIndex();

	void setTokenStopIndex(int index);

	Tree dupNode();

	/** Return a token type; needed for tree parsing */
	int getType();

	String getText();

	/** In case we don't have a token payload, what is the line for errors? */
	int getLine();

	int getCharPositionInLine();

	String toStringTree();

	String toString();
}
