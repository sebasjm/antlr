package org.antlr.runtime.tree;

/** What does a generic tree look like?  AST and parse trees extend
 *  this interface.  This is called Tree instead of node or whatever
 *  because node is just a single-node tree.
 *
 *  This is the minimal definition of a tree usable by all ANTLR concepts such
 *  as parse tree and AST building plus tree parsing; technically we don't even
 *  need the parent, but that can just return null if your implementation
 *  doesn't need it.
 */
public interface Tree {
	/** What user data is to be associated with this node? */
	public Object getPayload();

	/** I am a child of what node? */
	public Tree getParent();

	/** Get a child 0..n-1 */
	public Tree getChild(int i);

	public int getChildCount();

	// routines to construct/alter trees

	public void setParent(Tree t);
	public void addChild(Tree t);
	public void setChild(int i, Tree t);
	public Tree deleteChild(int i);
}
