package org.antlr.runtime.tree;

import java.util.List;
import java.util.ArrayList;

/** A generic tree. */
public class CommonTree implements Tree {
	protected Tree parent;
	protected List children;

	protected Object payload;

	public CommonTree() {;}

	public CommonTree(Object payload) {
		this.payload = payload;
	}

	public Object getPayload() {
		return payload;
	}

	public Tree getParent() {
		return parent;
	}

	public Tree getChild(int i) {
		if ( children==null || i>=children.size() ) {
			return null;
		}
		return (Tree)children.get(i);
	}

	public int getChildCount() {
		if ( children==null ) {
			return 0;
		}
		return children.size();
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

	public String toString() {
		if ( children==null || children.size()==0 ) {
			return payload.toString();
		}
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		buf.append(payload.toString());
		for (int i = 0; i < children.size(); i++) {
			Tree t = (Tree) children.get(i);
			buf.append(' ');
			buf.append(t.toString());
		}
		buf.append(")");
		return buf.toString();
	}
}