package org.antlr.runtime.debug;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.TreeAdaptor;

/** A TreeAdaptor proxy that fires debugging events to a DebugEventListener
 *  delegate and uses the TreeAdaptor delegate to do the actual work.  All
 *  AST events are triggered by this adaptor; no code gen changes are needed
 *  in generated rules.  Debugging events are triggered *after* invoking
 *  tree adaptor routines.
 *
 *  Trees created with actions in rewrite actions like "-> ^(ADD {foo} {bar})"
 *  cannot be tracked as they might not use the adaptor to create foo, bar.
 *  The debug listener has to deal with tree node IDs for which it did
 *  not see a createNode event.  A single <unknown> node is sufficient even
 *  if it represents a whole tree.
 */
public class DebugTreeAdaptor implements TreeAdaptor {
	protected DebugEventListener dbg;
	protected TreeAdaptor adaptor;

	public DebugTreeAdaptor(DebugEventListener dbg, TreeAdaptor adaptor) {
		this.dbg = dbg;
		this.adaptor = adaptor;
	}

	public Object create(Token payload) {
		Object n = adaptor.create(payload);
		dbg.createNode(getUniqueID(n), payload.getTokenIndex());
		return n;
	}

	public Object dupTree(Object tree) {
		// TODO: do these need to be sent to dbg?
		return adaptor.dupTree(tree);
	}

	public Object dupNode(Object treeNode) {
		// TODO: do these need to be sent to dbg?
		return adaptor.dupNode(treeNode);
	}

	public Object nil() {
		Object n = adaptor.nil();
		dbg.nilNode(getUniqueID(n));
		return n;
	}

	public void addChild(Object t, Object child) {
		adaptor.addChild(t,child);
		dbg.addChild(getUniqueID(t), getUniqueID(child));
	}

	public Object becomeRoot(Object newRoot, Object oldRoot) {
		Object n = adaptor.becomeRoot(newRoot, oldRoot);
		dbg.becomeRoot(getUniqueID(n), getUniqueID(oldRoot));
		return n;
	}

	public Object rulePostProcessing(Object root) {
		return adaptor.rulePostProcessing(root);
	}

	public void addChild(Object t, Token child) {
		Object n = adaptor.create(child);
		this.addChild(t, n);
	}

	public Object becomeRoot(Token newRoot, Object oldRoot) {
		Object n = adaptor.create(newRoot);
		adaptor.becomeRoot(n, oldRoot);
		dbg.becomeRoot(getUniqueID(n), getUniqueID(oldRoot));
		return n;
	}

	public Object create(int tokenType, Token fromToken) {
		Object n = adaptor.create(tokenType, fromToken);
		dbg.createNode(getUniqueID(n), fromToken.getText(), tokenType);
		return n;
	}

	public Object create(int tokenType, Token fromToken, String text) {
		Object n = adaptor.create(tokenType, fromToken, text);
		dbg.createNode(getUniqueID(n), text, tokenType);
		return n;
	}

	public Object create(int tokenType, String text) {
		Object n = adaptor.create(tokenType, text);
		dbg.createNode(getUniqueID(n), text, tokenType);
		return n;
	}

	public int getType(Object t) {
		return adaptor.getType(t);
	}

	public void setType(Object t, int type) {
		adaptor.setType(t, type);
	}

	public String getText(Object t) {
		return adaptor.getText(t);
	}

	public void setText(Object t, String text) {
		adaptor.setText(t, text);
	}

	public void setTokenBoundaries(Object t, Token startToken, Token stopToken) {
		adaptor.setTokenBoundaries(t, startToken, stopToken);
		dbg.setTokenBoundaries(getUniqueID(t),
							   startToken.getTokenIndex(),
							   stopToken.getTokenIndex());
	}

	public int getTokenStartIndex(Object t) {
		return adaptor.getTokenStartIndex(t);
	}

	public int getTokenStopIndex(Object t) {
		return adaptor.getTokenStopIndex(t);
	}

	public Object getChild(int i) {
		return adaptor.getChild(i);
	}

	public int getChildCount() {
		return adaptor.getChildCount();
	}

	// support

	/** For identifying trees.
	 *
	 *  How to identify nodes so we can say "add node to a prior node"?
	 *  Even becomeRoot is an issue. Ok, number the nodes as they are created?
	 *  Use a Map<Tree,Integer> or can we get away with a node's hashCode?
	 *  Two identical nodes could be in tree and hashCode would be same
	 *  if they implement that method. Damn...no way to get address. I wonder
	 *  if we can check to see if they implement hashCode and if so use the
	 *  super's hashCode. Nope...node.super.hashCode() doesn't parse.
	 *  Let's assume hashCode for now. Equals will have to be implemented,
	 *  but hashCode must remain unimplemented against Java doc. No biggie as
	 *  it's unlikely that people will add AST nodes to HashMaps.
	 *
	 *  TODO:  put a check to see if they have hashCode defined; that would break this
	 *  Method m = node.getClass().getDeclaredMethod("hashCode", null);
	 */
	public int getUniqueID(Object node) {
		return node.hashCode();
	}

	public DebugEventListener getDebugEventListener() {
		return dbg;
	}

	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}

}
