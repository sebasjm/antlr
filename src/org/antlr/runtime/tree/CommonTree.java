package org.antlr.runtime.tree;

import java.util.List;
import java.util.ArrayList;

/** A generic tree with no data inside.  Subclass to add data fields. */
public class CommonTree implements Tree {
	protected Tree parent;
	protected List children;

	public CommonTree() {;}

	public Tree getParent() {
		return parent;
	}

	public Tree getChild(int i) {
		if ( children==null || i>=children.size() ) {
			return null;
		}
		return (Tree)children.get(i);
	}

	public Tree getDown() {
		return getChild(0);
	}

	public Tree getRight() {
		Tree p = getParent();
		if ( p==null ) {
			return null; // no sibling if you have no parent; you're root
		}
		int i = p.getIndexOfChild(this);
		return p.getChild(i+1);
	}

	public Tree getLeft() {
		Tree p = getParent();
		if ( p==null ) {
			return null; // no sibling if you have no parent; you're root
		}
		int i = p.getIndexOfChild(this);
		return p.getChild(i-1);
	}

	public Tree getUp() {
		return getParent();
	}

	public int getNumberOfChildren() {
		if ( children==null ) {
			return 0;
		}
		return children.size();
	}

	/** Return an index 0..n-1 of t's index within its parents children.
	 *  Return -1 if no such tree or t==null.
	 */
	public int getIndexOfChild(Tree t) {
		if ( t==null || children==null ) {
			return -1;
		}
		return children.indexOf(t);
	}

	public void setParent(Tree t) {
		parent = t;
	}

	public void addChild(Tree t) {
		if ( children==null ) {
			createChildrenList();
		}
		children.add(t);
		t.setParent(this);
	}

	public void setChild(int i, Tree t) {
		if ( children==null ) {
			createChildrenList();
		}
		children.set(i, t);
		t.setParent(this);
	}

	public Tree deleteChild(int i) {
		if ( children==null ) {
			return null;
		}
		return (Tree)children.remove(i);
	}

	/** Override in a subclass to change the impl of children list */
	protected void createChildrenList() {
		children = new ArrayList();
	}
}