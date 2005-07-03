package org.antlr.runtime.tree;

import org.antlr.runtime.Token;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.ClassicToken;

import java.util.List;
import java.lang.reflect.Method;

public abstract class BaseTreeAdaptor implements TreeAdaptor {
	public Object nil() {
		return create(null);
	}

	public Object dupTree(Object tree) {
		return ((Tree)tree).dupTree();
	}

	/** Add a child to the tree t.  If child is a flat tree (a list), make all
	 *  in list children of t.
	 */
	public void addChild(Object t, Object child) {
		((Tree)t).addChild((Tree)child);
	}

	/** If oldRoot is a nil root, just copy or move the children to newRoot.
	 *  If not a nil root, make oldRoot a child of newRoot.
	 *
	 *  If newRoot is a nil-rooted single child tree, use the single
	 *  child as the new root node.
	 *
	 *  If oldRoot was null, it's ok, just return newRoot (even if isNil).
	 *
	 *  Return newRoot.  Throw an exception if newRoot is not a
	 *  simple node or nil root with a single child.
	 */
	public Object becomeRoot(Object newRoot, Object oldRoot) {
		Tree newRootTree = (Tree)newRoot;
		Tree oldRootTree = (Tree)oldRoot;
		if ( oldRoot==null ) {
			return newRoot;
		}
		// handle ^(nil real-node)
		if ( newRootTree.isNil() ) {
			if ( newRootTree.getChildCount()>1 ) {
				// TODO: make tree run time exceptions hierarchy
				throw new RuntimeException("more than one node as root (TODO: make exception hierarchy)");
			}
			newRootTree = (Tree)newRootTree.getChild(0);
		}
		newRootTree.addChild(oldRootTree);
		return newRootTree;
	}

	public void addChild(Object t, Token child) {
		addChild(t, create(child));
	}

	public Object becomeRoot(Token newRoot, Object oldRoot) {
		return becomeRoot(create(newRoot), oldRoot);
	}

	public Object create(int tokenType, Token fromToken) {
		fromToken = createToken(fromToken);
		//((ClassicToken)fromToken).setType(tokenType);
		fromToken.setType(tokenType);
		Tree t = (Tree)create(fromToken);
		return t;
	}

	public Object create(int tokenType, Token fromToken, String text) {
		fromToken = createToken(fromToken);
		fromToken.setType(tokenType);
		fromToken.setText(text);
		Tree t = (Tree)create(fromToken);
		return t;
	}

	public Object create(int tokenType, String text) {
		Token fromToken = createToken(tokenType, text);
		Tree t = (Tree)create(fromToken);
		return t;
	}

	public int getType(Object t) {
		((Tree)t).getType();
		return 0;
	}

	public void setType(Object t, int type) {
	}

	public void setText(Object t, String text) {
	}

	public Object getChild(int i) {
		return null;
	}

	public int getChildCount() {
		return 0;
	}
}

