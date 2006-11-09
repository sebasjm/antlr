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

import java.util.Iterator;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

/** A stream of tree nodes, accessing nodes from a tree of some kind.
 *  The stream can be accessed as an Iterator or via LT(1)/consume or
 *  LT(i).  No new nodes should be created during the walk.  A small buffer
 *  of tokens is kept to efficiently and easily handle LT(i) calls, though
 *  the lookahead mechanism is fairly complicated.
 *
 *  For tree rewriting during tree parsing, this must also be able
 *  to replace a set of children without "losing its place".
 *  That part is not yet implemented.  Will permit a rule to return
 *  a different tree and have it stitched into the output tree probably.
 *
 *  Because this class implements Iterator you can walk a tree with
 *  a for loop looking for nodes.  When using the Iterator
 *  interface methods you do not get DOWN and UP imaginary nodes that
 *  are used for parsing via TreeNodeStream interface.
 */
public class CommonTreeNodeStream implements TreeNodeStream, Iterator {
	public static final int INITIAL_LOOKAHEAD_BUFFER_SIZE = 5;

	protected static abstract class DummyTree extends BaseTree {
		public Tree dupNode() {return null;}
	}

	// all these navigation nodes are shared and hence they
	// cannot contain any line/column info

	public static class NavDownNode extends DummyTree {
		public int getType() {return Token.DOWN;}
		public String getText() {return "DOWN";}
		public String toString() {return "DOWN";}
	};

	public static class NavUpNode extends DummyTree {
		public int getType() {return Token.UP;}
		public String getText() {return "UP";}
		public String toString() {return "UP";}
	};

	public static class EOFNode extends DummyTree {
		public int getType() {return Token.EOF;}
		public String getText() {return "EOF";}
		public String toString() {return "EOF";}
	};

	public static final DummyTree DOWN = new NavDownNode();

	public static final DummyTree UP = new NavUpNode();

	public static final DummyTree EOF_NODE = new EOFNode();

	/** Reuse same DOWN, UP navigation nodes unless this is true */
	protected boolean uniqueNavigationNodes = false;

	/** Pull nodes from which tree? */
	protected Tree root;

	/** What tree adaptor was used to build these trees */
	TreeAdaptor adaptor;

	/** As we walk down the nodes, we must track parent nodes so we know
	 *  where to go after walking the last child of a node.  When visiting
	 *  a child, push current node and current index.
	 */
	protected Stack nodeStack = new Stack();

	/** Track which child index you are visiting for each node we push.
	 *  TODO: pretty inefficient...use int[] when you have time
	 */
	protected Stack indexStack = new Stack();

	/** Track the last mark() call result value for use in rewind(). */
	protected int lastMarker;

	/** Which node are we currently visiting? */
	protected Tree currentNode;

	/** Which node did we visit last?  Used for LT(-1) calls. */
	protected Tree previousNode;

	/** Which child are we currently visiting?  If -1 we have not visited
	 *  this node yet; next consume() request will set currentIndex to 0.
	 */
	protected int currentChildIndex;

	/** What node index did we just consume?  i=0..n-1 for n node trees.
	 *  IntStream.next is hence 1 + this value.  Size will be same.
	 */
	protected int absoluteNodeIndex;

	/** Buffer tree node stream for use with LT(i).  This list grows
	 *  to fit new lookahead depths, but consume() wraps like a circular
	 *  buffer.
	 */
	protected Tree[] lookahead = new Tree[INITIAL_LOOKAHEAD_BUFFER_SIZE];

	/** lookahead[head] is the first symbol of lookahead, LT(1). */
	protected int head;

	/** Add new lookahead at lookahead[tail].  tail wraps around at the
	 *  end of the lookahead buffer so tail could be less than head.
	  */
	protected int tail;

	/** When walking ahead with cyclic DFA or for syntactic predicates,
	  *  we need to record the state of the tree node stream.  This
	 *  class wraps up the current state of the CommonTreeNodeStream.
	 *  Calling mark() will push another of these on the markers stack.
	 */
	protected class TreeWalkState {
		int currentChildIndex;
		int absoluteNodeIndex;
		Tree currentNode;
		Tree previousNode;
		/** Record state of the nodeStack */
		int nodeStackSize;
		/** Record state of the indexStack */
		int indexStackSize;
		Tree[] lookahead;
	}

	/** Calls to mark() may be nested so we have to track a stack of
	 *  them.  The marker is an index into this stack.  Index 0 is
	 *  the first marker.  This is a List<TreeWalkState>
	 */
	protected List markers;

	public CommonTreeNodeStream(Tree tree) {
		this(new CommonTreeAdaptor(), tree);
	}

	public CommonTreeNodeStream(TreeAdaptor adaptor, Tree tree) {
		this.root = tree;
		this.adaptor = adaptor;
		reset();
	}

	public void reset() {
		currentNode = root;
		previousNode = null;
		currentChildIndex = -1;
		absoluteNodeIndex = -1;
		head = tail = 0;
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
		//System.out.println("LT("+k+"); head="+head+", tail="+tail);
		if ( k==-1 ) {
			return previousNode;
		}
		if ( k<0 ) {
			throw new IllegalArgumentException("tree node streams cannot look backwards more than 1 node");
		}
		if ( k==0 ) {
			return Tree.INVALID_NODE;
		}
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
		int n = getLookaheadSize();
		//System.out.println("we have "+n+" nodes; need "+(k-n));
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
		//System.out.println("addLookahead head="+head+", tail="+tail);
		lookahead[tail] = node;
		tail = (tail+1)%lookahead.length;
		if ( tail==head ) {
			// buffer overflow: tail caught up with head
			// allocate a buffer 2x as big
			Tree[] bigger = new Tree[2*lookahead.length];
			// copy head to end of buffer to beginning of bigger buffer
			int remainderHeadToEnd = lookahead.length-head;
			System.arraycopy(lookahead, head, bigger, 0, remainderHeadToEnd);
			// copy 0..tail to after that
			System.arraycopy(lookahead, 0, bigger, remainderHeadToEnd, tail);
			lookahead = bigger; // reset to bigger buffer
			head = 0;
			tail += remainderHeadToEnd;
		}
	}

	// Satisfy IntStream interface

	public void consume() {
		/*
		System.out.println("consume: currentNode="+currentNode.getType()+
						   " childIndex="+currentChildIndex+
						   " nodeIndex="+absoluteNodeIndex);
						   */
		// make sure there is something in lookahead buf, which might call next()
		fill(1);
		absoluteNodeIndex++;
		previousNode = lookahead[head]; // track previous node before moving on
		head = (head+1) % lookahead.length;
	}

	public int LA(int i) {
		Tree t = (Tree)LT(i);
		if ( t==null ) {
			return Token.INVALID_TOKEN_TYPE;
		}
		return t.getType();
	}

	/** Record the current state of the tree walk which includes
	 *  the current node and stack state.
	 */
	public int mark() {
		if ( markers==null ) {
			markers = new ArrayList();
		}
		TreeWalkState state = new TreeWalkState();
		state.absoluteNodeIndex = absoluteNodeIndex;
		state.currentChildIndex = currentChildIndex;
		state.currentNode = currentNode;
		state.previousNode = previousNode;
		state.nodeStackSize = nodeStack.size();
		state.indexStackSize = indexStack.size();
		// take snapshot of lookahead buffer
		int n = getLookaheadSize();
		int i=0;
		state.lookahead = new Tree[n];
		for (int k=1; k<=n; k++,i++) {
			state.lookahead[i] = (Tree)LT(k);
		}
		markers.add(state);
		return markers.size(); // markers go 1..depth
	}

	public void release(int marker) {
		throw new NoSuchMethodError("can't release tree parse; email parrt@antlr.org");
	}

	/** Rewind the current state of the tree walk to the state it
	 *  was in when mark() was called and it returned marker.  Also,
	 *  wipe out the lookahead which will force reloading a few nodes
	 *  but it is better than making a copy of the lookahead buffer
	 *  upon mark().
	 */
	public void rewind(int marker) {
		if ( markers==null || markers.size()<marker ) {
			return; // do nothing upon error; perhaps this should throw exception?
		}
		TreeWalkState state = (TreeWalkState)markers.get(marker-1);
		markers.remove(marker-1); // "pop" state from stack
		absoluteNodeIndex = state.absoluteNodeIndex;
		currentChildIndex = state.currentChildIndex;
		currentNode = state.currentNode;
		previousNode = state.previousNode;
		// drop node and index stacks back to old size
		nodeStack.setSize(state.nodeStackSize);
		indexStack.setSize(state.indexStackSize);
		head = tail = 0; // wack lookahead buffer and then refill
		for (; tail<state.lookahead.length; tail++) {
			lookahead[tail] = state.lookahead[tail];
		}
	}

	public void rewind() {
		rewind(lastMarker);
	}

	/** consume() ahead until we hit index.  Can't just jump ahead--must
	 *  spit out the navigation nodes.
	 */
	public void seek(int index) {
		if ( index<this.index() ) {
			throw new IllegalArgumentException("can't seek backwards in node stream");
		}
		// seek forward, consume until we hit index
		while ( this.index()<index ) {
			consume();
		}
	}

	public int index() {
		return absoluteNodeIndex+1;
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
	 *  we can emit many tokens for every next() call, it's pretty hard to
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
			addLookahead(EOF_NODE);
			// this is infinite stream returning EOF at end forever
			// so don't throw NoSuchElementException
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
			addNavigationNode(Token.DOWN);
		}
		// visit child
		currentNode = (Tree)currentNode.getChild(child);
		currentChildIndex = 0;
		node = currentNode;  // record node to return
		addLookahead(node);
		walkBackToMostRecentNodeWithUnvisitedChildren();
		return node;
	}

	/** As we flatten the tree, we use UP, DOWN nodes to represent
	 *  the tree structure.  When debugging we need unique nodes
	 *  so instantiate new ones when uniqueNavigationNodes is true.
	 */
	protected void addNavigationNode(final int ttype) {
		Tree node = null;
		if ( ttype==Token.DOWN ) {
			if ( hasUniqueNavigationNodes() ) node = new NavDownNode();
			else node = DOWN;
		}
		else {
			if ( hasUniqueNavigationNodes() ) node = new NavUpNode();
			else node = UP;
		}
		addLookahead(node);
	}

	/** Walk upwards looking for a node with more children to walk. */
	protected void walkBackToMostRecentNodeWithUnvisitedChildren() {
		while ( currentNode!=null &&
				currentChildIndex>=currentNode.getChildCount() )
		{
			currentNode = (Tree)nodeStack.pop();
			currentChildIndex = ((Integer)indexStack.pop()).intValue();
			currentChildIndex++; // move to next child
			if ( currentChildIndex>=currentNode.getChildCount() ) {
				if ( !currentNode.isNil() ) {
					addNavigationNode(Token.UP);
				}
				if ( currentNode==root ) { // we done yet?
					currentNode = null;
				}
			}
		}
	}

	/** Just here to satisfy Iterator interface */
	public void remove() {
		throw new NoSuchMethodError();
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

	/** Using the Iterator interface, return a list of all the token types
	 *  as text.  Used for testing.
	 */
	public String toNodesOnlyString() {
		StringBuffer buf = new StringBuffer();
		while (hasNext()) {
			CommonTree x = (CommonTree)next();
			buf.append(" ");
			buf.append(x.getType());
		}
		return buf.toString();
	}

	/** Print out the entire tree including DOWN/UP nodes.  Uses
	 *  a recursive walk.  Mostly useful for testing as it yields
	 *  the token types not text.
	 */
	public String toString() {
		return toString(root, null);
	}

	protected int getLookaheadSize() {
		return tail<head?(lookahead.length-head+tail):(tail-head);
	}

	public String toString(Object start, Object stop) {
		StringBuffer buf = new StringBuffer();
		toStringWork((Tree)start, (Tree)stop, buf);
		return buf.toString();
	}

	protected void toStringWork(Tree p, Tree stop, StringBuffer buf) {
		if ( !p.isNil() ) {
			String text = p.toString();
			if ( text==null ) {
				text = " "+String.valueOf(p.getType());
			}
			buf.append(text); // ask the node to go to string
		}
		if ( p==stop ) {
			return;
		}
		int n = p.getChildCount();
		if ( n>0 && !p.isNil() ) {
			buf.append(" ");
			buf.append(Token.DOWN);
		}
		for (int c=0; c<n; c++) {
			Tree child = p.getChild(c);
			toStringWork(child, stop, buf);
		}
		if ( n>0 && !p.isNil() ) {
			buf.append(" ");
			buf.append(Token.UP);
		}
	}
}

