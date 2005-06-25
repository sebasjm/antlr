/*
[The "BSD licence"]
Copyright (c) 2005 Terence Parr
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

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;

import java.util.Iterator;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

/** A stream of tree nodes, accessing nodes from a tree of some kind.
 *
 *  For tree rewriting during tree parsing, this must also be able
 *  to replace a set of children without "losing its place".
 *
 *  Because this class implements Iterator you can walk a tree with
 *  a for loop looking for nodes.  When using the Iterator
 *  interface methods you do not get DOWN and UP imaginary nodes that
 *  are used for parsing via TreeNodeStream interface.
 */
public class CommonTreeNodeStream implements TreeNodeStream, Iterator {
	public static final int INITIAL_LOOKAHEAD_BUFFER_SIZE = 20;

	protected static class DummyTree implements Tree {
		public Tree getChild(int i) { return null; }
		public int getChildCount() { return 0;}
		public void addChild(Tree t) {}
		public boolean isNil() {return false;}
		public Tree dupTree() {return null;}
		public Tree dupNode() {return null;}
		public int getType() {return 0;}
		public String toStringTree() {return null;}
	}

	public static final DummyTree DOWN =
		new DummyTree() {
			public int getType() {return Token.DOWN;}
		};

	public static final DummyTree UP =
		new DummyTree() {
			public int getType() {return Token.UP;}
		};

	/** Pull nodes from which tree? */
	protected Tree root;

	/** As we walk down the nodes, we must track parent nodes so we know
	 *  where to go after walking the last child of a node.  When visiting
	 *  a child, push current node and current index.
	 */
	protected Stack nodeStack = new Stack();

	/** Track which child index you are visiting for each node we push.
	 *  TODO: pretty inefficient...use int[] when you have time
	 */
	protected Stack indexStack = new Stack();

	/** Which node are we currently visiting? */
	protected Tree currentNode;

	/** Which child are we currently visiting?  If -1 we have not visited
	 *  this node yet; next consume() request will set currentIndex to 0.
	 */
	protected int currentChildIndex;

	/** Buffer tree node stream for use with LT(i).  This list grows
	 *  to fit new lookahead depths, but consume() wraps like a circular
	 *  buffer.
	 */
	protected Tree[] lookahead = new Tree[INITIAL_LOOKAHEAD_BUFFER_SIZE];

	/** lookahead[p] is the first symbol of lookahead, LT(1). */
	protected int head;

	/** Add new lookahead at lookahead[tail].  tail wraps around at the
	 *  end of the lookahead buffer so tail could be less than head.
 	 */
	protected int tail;

	/** What node index are we at?  i=0..n-1 for n node trees. */
	protected int absoluteNodeIndex = -1;


	public CommonTreeNodeStream(Tree tree) {
		this.root = tree;
		reset();
	}

	public void reset() {
		currentNode = root;
		currentChildIndex = -1;
		/*
		nodeStack.push(null);
		indexStack.push(new Integer(-1));
		*/
	}

	// Satisfy TreeNodeStream

	/** Get tree node at current input pointer + i ahead where i=1 is next node.
	 *  i<0 indicates nodes in the past.  So -1 is previous node and -2 is
	 *  two nodes ago. LT(0) is undefined.  For i>=n, return null.
	 *  Return null for LT(0) and any index that results in an absolute address
	 *  that is negative.
	 *
	 *  This is analogus to the LT() method of the TokenStream, but this
	 *  returns a tree node instead of a token.  Makes code gen identical
	 *  for both parser and tree grammars. :)
	 */
	public Object LT(int k) {
		fill(k);
		return lookahead[(head+k-1)%lookahead.length];
	}

	/** Where is this stream pulling nodes from?  This is not the name, but
	 *  the object that provides node objects.
	 */
	public Object getTreeSource() {
		return root;
	}

	/** Make sure we have at least k symbols in lookahead buffer */
	protected void fill(int k) {
		int n = tail<head?(lookahead.length-head+tail):(tail-head);
		// System.out.println("we have "+n+" nodes; need "+(k-n));
		for (int i=1; i<=k-n; i++) {
			next(); // get at least k-depth lookahead nodes
		}
	}

	/** Add a node to the lookahead buffer.  Add at lookahead[tail].
	 *  If you tail+1 == head, then we must create a bigger buffer
	 *  and copy all the nodes over plus reset head, tail.  After
	 *  this method, LT(1) will be lookahead[0].
	 */
	protected void addLookahead(Tree node) {
		lookahead[tail] = node;
		tail++;
		if ( tail==head ) { // buffer overflow: tail caught up with head
			// allocate a buffer 2x as big
			Tree[] bigger = new Tree[2*lookahead.length];
			// copy head to end of buffer to beginning of bigger buffer
			int remainderHeadToEnd = lookahead.length-head;
			System.arraycopy(lookahead, head, bigger, 0, remainderHeadToEnd);
			// copy 0..tail to after that
			System.arraycopy(lookahead, 0, bigger, remainderHeadToEnd, tail);
			head = 0;
			tail += remainderHeadToEnd;
		}
	}

	// Satisfy IntStream interface

	public void consume() {
		absoluteNodeIndex++;
	}

	public int LA(int i) {
		return ((Tree)LT(i)).getType();
	}

	public int mark() {
		return 0;
	}

	public int index() {
		return absoluteNodeIndex;
	}

	public void rewind(int marker) {
	}

	/** Expensive to compute so I won't bother doing the right thing.
	 *  This method only returns how much input has been seen so far.  So
	 *  after parsing it returns true size.
	 */
	public int size() {
		return absoluteNodeIndex+1;
	}

	// Satisfy Java's Iterator interface

	public boolean hasNext() {
		return currentNode!=null;
	}

	/** Return the next node found during a depth-first walk of root.
	 *  Also, add these nodes and DOWN/UP imaginary nodes into the lokoahead
	 *  buffer as a side-effect.  Normally side-effects are bad, but because
	 *  we can emit many tokens for ever next() call, it's pretty hard to
	 *  use a single return value for that.  We must add these tokens to
	 *  the lookahead buffer.
	 *
	 *  This does *not* return the DOWN/UP nodes; those are only returned
	 *  by the LT() method.
	 *
	 *  Ugh.  This mechanism is much more complicated than a recursive
	 *  solution, but it's the only way to provide nodes on-demand instead
	 *  of walking once completely through and buffering up the nodes. :(
	 */
	public Object next() {
		// already walked entire tree; nothing to return
		if ( currentNode==null ) {
			return null;
		}

		// initial condition (first time method is called)
		if ( currentChildIndex==-1 ) {
			return handleRootNode();
		}

		// index is in the child list?
		if ( currentChildIndex<currentNode.getChildCount() ) {
			return visitChild(currentChildIndex);
		}

		// hit end of child list, return to parent node or its parent ...
		walkBackToMostRecentNodeWithUnvisitedChildren();
		if ( currentNode!=null ) {
			return visitChild(currentChildIndex);
		}

		return null;
	}

	protected Tree handleRootNode() {
		Tree node;
		node = currentNode;
		// point to first child in prep for subsequent next()
		currentChildIndex = 0;
		if ( node.isNil() ) {
			// don't count this root nil node
			node = visitChild(currentChildIndex);
		}
		else {
			addLookahead(node);
			if ( currentNode.getChildCount()==0 ) {
				// single node case
				currentNode = null; // say we're done
			}
		}
		return node;
	}

	protected Tree visitChild(int child) {
		Tree node = null;
		// save state
		nodeStack.push(currentNode);
		indexStack.push(new Integer(child));
		if ( child==0 && !currentNode.isNil() ) {
			addLookahead(DOWN);
		}
		// visit child
		currentNode = (Tree)currentNode.getChild(child);
		currentChildIndex = 0;
		node = currentNode;  // record node to return
		addLookahead(node);
		walkBackToMostRecentNodeWithUnvisitedChildren();
		return node;
	}

	protected void walkBackToMostRecentNodeWithUnvisitedChildren() {
		while ( currentNode!=null &&
			    currentChildIndex>=currentNode.getChildCount() )
		{
			currentNode = (Tree)nodeStack.pop();
			currentChildIndex = ((Integer)indexStack.pop()).intValue();
			currentChildIndex++; // move to next child
			if ( currentChildIndex>=currentNode.getChildCount() ) {
				if ( !currentNode.isNil() ) {
					addLookahead(UP);
				}
				if ( currentNode==root ) { // we done yet?
					currentNode = null;
				}
			}
		}
	}

	public void remove() {
	}

	public String toNodesOnlyString() {
		StringBuffer buf = new StringBuffer();
		while (hasNext()) {
			CommonTree x = (CommonTree)next();
			buf.append(" ");
			buf.append(x.getType());
		}
		return buf.toString();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 1; i <= lookahead.length; i++) {
			Object node = LT(i);
			if ( node!=null ) {
				buf.append(" ");
				buf.append(((Tree)node).getType());
			}
		}
		return buf.toString();
	}
}

