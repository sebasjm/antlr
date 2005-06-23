package org.antlr.runtime.tree;

import org.antlr.runtime.Token;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.ClassicToken;

import java.util.List;
import java.lang.reflect.Method;

public class CommonTreeAdaptor implements TreeAdaptor {
	public Object create(Object payload) {
		return new CommonTree((Token)payload);
	}

	public Object nil() {
		return create(null);
	}

	public Object dupTree(Object tree) {
		return ((CommonTree)tree).dupTree();
	}

	public Object dupNode(Object treeNode) {
		return new CommonTree((CommonTree)treeNode);
	}

	/** Add a child to the tree t.  If child is a flat tree (a list), make all
	 *  in list children of t.
	 */
	public void addChild(Object t, Object child) {
		((CommonTree)t).addChild((CommonTree)child);
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
		if ( oldRoot==null ) {
			return newRoot;
		}
		if ( ((CommonTree)newRoot).isNil() ) {
			List kids = ((CommonTree)newRoot).children;
			if ( kids.size()>1 ) {
				// TODO: make tree run time exceptions hierarchy
				throw new IllegalArgumentException("more than one node as root (TODO: make exception hierarchy)");
			}
			newRoot = (CommonTree)((CommonTree)newRoot).children.get(0);
		}
		if ( ((CommonTree)oldRoot).isNil() ) {
			((CommonTree)newRoot).children = ((CommonTree)oldRoot).children;
		}
		else {
			((CommonTree)newRoot).addChild((CommonTree)oldRoot);
		}
		return newRoot;
	}

	public void addChild(Object t, Token child) {
		addChild(t, create(child));
	}

	public Object becomeRoot(Token newRoot, Object oldRoot) {
		return becomeRoot(create(newRoot), oldRoot);
	}

	public Token createToken(int tokenType, String text) {
		return new ClassicToken(tokenType, text);
	}

	public Token createToken(Token fromToken) {
		return new ClassicToken(fromToken);
	}

	public Object create(int tokenType, Token fromToken) {
		fromToken = createToken(fromToken);
		//((ClassicToken)fromToken).setType(tokenType);
		fromToken.setType(tokenType);
		CommonTree t = (CommonTree)create(fromToken);
		return t;
	}

	public Object create(int tokenType, Token fromToken, String text) {
		fromToken = createToken(fromToken);
		fromToken.setType(tokenType);
		fromToken.setText(text);
		CommonTree t = (CommonTree)create(fromToken);
		return t;
	}

	public Object create(int tokenType, String text) {
		Token fromToken = createToken(tokenType, text);
		CommonTree t = (CommonTree)create(fromToken);
		return t;
	}

	public int getType(Object t) {
		((CommonTree)t).token.getType();
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
