package org.antlr.runtime.tree;

/** What does a generic tree look like?  AST and parse trees extend
 *  this interface.  This is called Tree instead of node or whatever
 *  because node is just a single-node tree.
 *
 *  This is the minimal definition of a tree usable by all ANTLR concepts such
 *  as parse tree and AST building plus tree parsing; technically we don't even
 *  need the parent, but that can just return null if your implementation
 *  doesn't need it.
 *
 *  Some behavior requirements: null and duplicate children pointers are not
 *  allowed if you expect getRight() and getLeft() to work.  A default
 *  implementation must search the sibling list to find the pointer and then
 *  get the left or right node.
 */
public interface Tree {
	/** I am a child of what node? */
	public Tree getParent();

	/** Get a child 0..n-1 */
	public Tree getChild(int i);

	/** Get the first child or null if none */
	public Tree getDown();

	/** Get the next sibling or null if none */
	public Tree getRight();

	/** Get the previous sibling or null if none */
	public Tree getLeft();

	/** Synonymous with getParent(); do we need this? */
	public Tree getUp();

	public int getNumberOfChildren();

	/** Return an index 0..n-1 of t's index within its parents children.
	 *  Return -1 if no such tree or t==null.
	 */
	public int getIndexOfChild(Tree t);

	// routines to construct/alter trees

	public void setParent(Tree t);
	public void addChild(Tree t);
	public void setChild(int i, Tree t);
	public Tree deleteChild(int i);
}
