/*
[The "BSD licence"]
Copyright (c) 2005-2006 Terence Parr
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.antlr.runtime.tree;

import org.antlr.runtime.Token;

import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/** A buffered stream of tree nodes.  Nodes can be from a tree of ANY kind.
 *
 *  This node stream sucks all nodes out of the tree specified in
 *  the constructor during construction and makes pointers into
 *  the tree using an array of Object pointers. The stream necessarily
 *  includes pointers to DOWN and UP and EOF nodes.
 *
 *  This stream knows how to mark/release for backtracking.
 *
 *  This stream is most suitable for tree interpreters that need to
 *  jump around a lot or for tree parsers requiring speed (at cost of memory).
 *  There is some duplicated functionality here with UnBufferedTreeNodeStream
 *  but just in bookkeeping, not tree walking etc...
 *
 *  @see UnBufferedTreeNodeStream
 */
public class CommonTreeNodeStream implements TreeNodeStream {
	public static final int DEFAULT_INITIAL_BUFFER_SIZE = 100;
	public static final int INITIAL_CALL_STACK_SIZE = 10;

	protected static abstract class DummyTree extends BaseTree {
		public Tree dupNode() {return null;}
		public int getTokenStartIndex() { return 0; }
		public void setTokenStartIndex(int index) {}
		public int getTokenStopIndex() { return 0; }
		public void setTokenStopIndex(int index) {}
	}

	public static class NavDownNode extends DummyTree {
		public int getType() {return Token.DOWN;}
		public String getText() {return "DOWN";}
		public String toString() {return "DOWN";}
	}

	public static class NavUpNode extends DummyTree {
		public int getType() {return Token.UP;}
		public String getText() {return "UP";}
		public String toString() {return "UP";}
	}

	public static class EOFNode extends DummyTree {
		public int getType() {return Token.EOF;}
		public String getText() {return "EOF";}
		public String toString() {return "EOF";}
	}

	protected class StreamIterator implements Iterator {
		int i = 0;
		public boolean hasNext() {
			return i<nodes.size();
		}

		public Object next() {
			int current = i;
			i++;
			if ( current < nodes.size() ) {
				return nodes.get(current);
			}
			return EOF_NODE;
		}

		public void remove() {
			throw new RuntimeException("cannot remove nodes from stream");
		}
	}

	// all these navigation nodes are shared and hence they
	// cannot contain any line/column info

	public static final DummyTree DOWN = new NavDownNode();
	public static final DummyTree UP = new NavUpNode();
	public static final DummyTree EOF_NODE = new EOFNode();

	/** The complete mapping from stream index to tree node.
	 *  This buffer includes pointers to DOWN, UP, and EOF nodes.
	 *  It is built upon ctor invocation.  The elements are type
	 *  Object as we don't what the trees look like.
	 */
	protected List nodes;

	/** Pull nodes from which tree? */
	protected Object root;

	/** What tree adaptor was used to build these trees */
	TreeAdaptor adaptor;

	/** Reuse same DOWN, UP navigation nodes unless this is true */
	protected boolean uniqueNavigationNodes = false;

	/** The index into the nodes list of the current node (next node
	 *  to consume).
	 */
	protected int p = 0;

	/** Track the last mark() call result value for use in rewind(). */
	protected int lastMarker;

	/** Stack of indexes used for push/pop calls */
	protected int[] calls;

	/** Stack pointer for stack of indexes; -1 indicates empty.  Points
	 *  at next location to push a value.
	 */
	protected int _sp = -1;

	public CommonTreeNodeStream(Object tree) {
		this(new CommonTreeAdaptor(), tree);
	}

	public CommonTreeNodeStream(TreeAdaptor adaptor, Object tree) {
		this(adaptor, tree, DEFAULT_INITIAL_BUFFER_SIZE);
	}

	public CommonTreeNodeStream(TreeAdaptor adaptor, Object tree, int initialBufferSize) {
		this.root = tree;
		this.adaptor = adaptor;
		nodes = new ArrayList(initialBufferSize);
		fillBuffer(root); // starting at root, fill nodes buffer
	}

	/** Walk tree with depth-first-search and fill nodes buffer.
	 *  Don't do DOWN, UP nodes if its a list (p is isNil).
	 */
	protected void fillBuffer(Object p) {
		boolean nil = adaptor.isNil(p);
		if ( !nil ) {
			nodes.add(p); // add this node
		}
		// add DOWN node if p has children
		int n = adaptor.getChildCount(p);
		if ( !nil && n>0 ) {
			addNavigationNode(Token.DOWN);
		}
		// and now add all its children
		for (int c=0; c<n; c++) {
			Object child = adaptor.getChild(p,c);
			fillBuffer(child);
		}
		// add UP node if p has children
		if ( !nil && n>0 ) {
			addNavigationNode(Token.UP);
		}
	}

	/** As we flatten the tree, we use UP, DOWN nodes to represent
	 *  the tree structure.  When debugging we need unique nodes
	 *  so instantiate new ones when uniqueNavigationNodes is true.
	 */
	protected void addNavigationNode(final int ttype) {
		Object navNode = null;
		if ( ttype== Token.DOWN ) {
			if ( hasUniqueNavigationNodes() ) navNode = new CommonTreeNodeStream.NavDownNode();
			else navNode = CommonTreeNodeStream.DOWN;
		}
		else {
			if ( hasUniqueNavigationNodes() ) navNode = new CommonTreeNodeStream.NavUpNode();
			else navNode = CommonTreeNodeStream.UP;
		}
		nodes.add(navNode);
	}

	public Object LT(int k) {
		if ( k==0 ) {
			return null;
		}
		if ( k<0 ) {
			return LB(-k);
		}
		//System.out.print("LT(p="+p+","+k+")=");
		if ( (p+k-1) >= nodes.size() ) {
			return CommonTreeNodeStream.EOF_NODE;
		}
		return nodes.get(p+k-1);
	}

	/** Look backwards k nodes */
	protected Object LB(int k) {
		if ( k==0 ) {
			return null;
		}
		if ( (p-k)<0 ) {
			return null;
		}
		return nodes.get(p-k);
	}

	public Object getTreeSource() {
		return root;
	}

	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}

	public boolean hasUniqueNavigationNodes() {
		return uniqueNavigationNodes;
	}

	public void setUniqueNavigationNodes(boolean uniqueNavigationNodes) {
		this.uniqueNavigationNodes = uniqueNavigationNodes;
	}

	public void consume() {
		p++;
	}

	public int LA(int i) {
		return adaptor.getType(LT(i));
	}

	public int mark() {
		lastMarker = index();
		return lastMarker;
	}

	public void release(int marker) {
		// no resources to release
	}

	public int index() {
		return p;
	}

	public void rewind(int marker) {
		seek(marker);
	}

	public void rewind() {
		seek(lastMarker);
	}

	public void seek(int index) {
		p = index;
	}

	/** Make stream jump to a new location, saving old location.
	 *  Switch back with pop().  I manage dyanmic array manually
	 *  to avoid creating Integer objects all over the place.
	 */
	public void push(int index) {
		if ( calls==null ) {
			calls = new int[INITIAL_CALL_STACK_SIZE];
		}
		else if ( (_sp+1)>=calls.length ) {
			int[] newStack = new int[calls.length*2];
			System.arraycopy(calls, 0, newStack, 0, calls.length-1);
			calls = newStack;
		}
		calls[++_sp] = p; // save current index
		seek(index);
	}

	/** Seek back to previous index saved during last push() call.
	 *  Return top of stack (return index).
	 */
	public int pop() {
		int ret = calls[_sp--];
		seek(ret);
		return ret;
	}

	public int size() {
		return nodes.size();
	}

	public Iterator iterator() {
		return new StreamIterator();
	}

	/** Used for testing, just return the token type stream */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < nodes.size(); i++) {
			Object t = (Object) nodes.get(i);
			buf.append(" ");
			buf.append(adaptor.getType(t));
		}
		return buf.toString();
	}

	public String toString(Object start, Object stop) {
		if ( start==null || stop==null ) {
			return null;
		}
		// walk nodes looking for start
		Object t = null;
		int i = 0;
		for (; i < nodes.size(); i++) {
			t = nodes.get(i);
			if ( t==start ) {
				break;
			}
		}
		// now walk until we see stop, filling string buffer with text
		 StringBuffer buf = new StringBuffer();
		t = nodes.get(i);
		while ( t!=stop ) {
			String text = adaptor.getText(t);
			if ( text==null ) {
				text = " "+String.valueOf(adaptor.getType(t));
			}
			buf.append(text);
			i++;
			t = nodes.get(i);
		}
		// include stop node too
		String text = adaptor.getText(stop);
		if ( text==null ) {
			text = " "+String.valueOf(adaptor.getType(stop));
		}
		buf.append(text);
		return buf.toString();
	}
}
