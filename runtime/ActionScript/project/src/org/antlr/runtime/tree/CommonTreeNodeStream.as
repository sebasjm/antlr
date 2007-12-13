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
package org.antlr.runtime.tree {

	import org.antlr.runtime.TokenConstants;
	import org.antlr.runtime.TokenStream;
	

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
		public static const DEFAULT_INITIAL_BUFFER_SIZE:int = 100;
		public static const INITIAL_CALL_STACK_SIZE:int = 10;
	/*
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
				return eof;
			}
	
			public void remove() {
				throw new RuntimeException("cannot remove nodes from stream");
			}
		}
	*/
		// all these navigation nodes are shared and hence they
		// cannot contain any line/column info
	
		protected var down:Object;
		protected var up:Object;
		protected var eof:Object;
	
		/** The complete mapping from stream index to tree node.
		 *  This buffer includes pointers to DOWN, UP, and EOF nodes.
		 *  It is built upon ctor invocation.  The elements are type
		 *  Object as we don't what the trees look like.
		 *
		 *  Load upon first need of the buffer so we can set token types
		 *  of interest for reverseIndexing.  Slows us down a wee bit to
		 *  do all of the if p==-1 testing everywhere though.
		 */
		protected var nodes:Array;
	
		/** Pull nodes from which tree? */
		protected var root:Object;
	
		/** IF this tree (root) was created from a token stream, track it. */
		protected var tokens:TokenStream;
	
		/** What tree adaptor was used to build these trees */
		internal var adaptor:TreeAdaptor;
	
		/** Reuse same DOWN, UP navigation nodes unless this is true */
		protected var uniqueNavigationNodes:Boolean = false;
	
		/** The index into the nodes list of the current node (next node
		 *  to consume).  If -1, nodes array not filled yet.
		 */
		protected var p:int = -1;
	
		/** Track the last mark() call result value for use in rewind(). */
		protected var lastMarker:int;
	
		/** Stack of indexes used for push/pop calls */
		protected var calls:Array;
	
		/** Stack pointer for stack of indexes; -1 indicates empty.  Points
		 *  at next location to push a value.
		 */
		protected var _sp:int = -1;
	
		/** During fillBuffer(), we can make a reverse index from a set
		 *  of token types of interest to the list of indexes into the
		 *  node stream.  This lets us convert a node pointer to a
		 *  stream index semi-efficiently for a list of interesting
		 *  nodes such as function definition nodes (you'll want to seek
		 *  to their bodies for an interpreter).  Also useful for doing
		 *  dynamic searches; i.e., go find me all PLUS nodes.
		 */
		protected var tokenTypeToStreamIndexesMap:Object;
	
		/** If tokenTypesToReverseIndex set to INDEX_ALL then indexing
		 *  occurs for all token types.
		 * 
		 * GMS -- was HashSet
		 */
		public static const INDEX_ALL:Object = new Object();
	
		/** A set of token types user would like to index for faster lookup.
		 *  If this is INDEX_ALL, then all token types are tracked.  If null,
		 *  then none are indexed.
		 * 
		 * GMS -- was a Set
		 */
		protected var tokenTypesToReverseIndex:Object = null;
	
		public function CommonTreeNodeStream(tree:Object, adaptor:TreeAdaptor = null, initialBufferSize:int = DEFAULT_INITIAL_BUFFER_SIZE) {
			this.root = tree;
			this.adaptor = adaptor == null ? new CommonTreeAdaptor() : adaptor;
			
			nodes = new Array();
			down = this.adaptor.createFromString(TokenConstants.DOWN, "DOWN");
			up = this.adaptor.createFromString(TokenConstants.UP, "UP");
			eof = this.adaptor.createFromString(TokenConstants.EOF, "EOF");
		}
	
		/** Walk tree with depth-first-search and fill nodes buffer.
		 *  Don't do DOWN, UP nodes if its a list (t is isNil).
		 */
		protected function fillBuffer():void {
			fillBufferTo(root);
			//System.out.println("revIndex="+tokenTypeToStreamIndexesMap);
			p = 0; // buffer of nodes intialized now
		}
	
		protected function fillBufferTo(t:Object):void {
			var nil:Boolean = adaptor.isNil(t);
			if ( !nil ) {
				nodes.push(t); // add this node
				fillReverseIndex(t, nodes.length-1);
			}
			// add DOWN node if t has children
			var n:int = adaptor.getChildCount(t);
			if ( !nil && n>0 ) {
				addNavigationNode(TokenConstants.DOWN);
			}
			// and now add all its children
			for (var c:int=0; c<n; c++) {
				var child:Object = adaptor.getChild(t,c);
				fillBufferTo(child);
			}
			// add UP node if t has children
			if ( !nil && n>0 ) {
				addNavigationNode(TokenConstants.UP);
			}
		}
	
		/** Given a node, add this to the reverse index tokenTypeToStreamIndexesMap.
		 *  You can override this method to alter how indexing occurs.  The
		 *  default is to create a
		 *
		 *    Map<Integer token type,ArrayList<Integer stream index>>
		 *
		 *  This data structure allows you to find all nodes with type INT in order.
		 *
		 *  If you really need to find a node of type, say, FUNC quickly then perhaps
		 *
		 *    Map<Integertoken type,Map<Object tree node,Integer stream index>>
		 *
		 *  would be better for you.  The interior maps map a tree node to
		 *  the index so you don't have to search linearly for a specific node.
		 *
		 *  If you change this method, you will likely need to change
		 *  getNodeIndex(), which extracts information.
		 */
		protected function fillReverseIndex(node:Object, streamIndex:int):void {
			//System.out.println("revIndex "+node+"@"+streamIndex);
			if ( tokenTypesToReverseIndex==null ) {
				return; // no indexing if this is empty (nothing of interest)
			}
			if ( tokenTypeToStreamIndexesMap==null ) {
				tokenTypeToStreamIndexesMap = new Object(); // first indexing op
			}
			var tokenType:int = adaptor.getType(node);
			var tokenTypeI:String = new String(tokenType);
			if ( !(tokenTypesToReverseIndex==INDEX_ALL ||
				   tokenTypesToReverseIndex.hasOwnProperty(tokenTypeI)) )
			{
				return; // tokenType not of interest
			}
			var streamIndexI:String = new String(streamIndex);
			var indexes:Array = tokenTypeToStreamIndexesMap[tokenTypeI];
			if ( indexes==null ) {
				indexes = new Array(); // no list yet for this token type
				indexes.push(streamIndexI); // not there yet, add
				tokenTypeToStreamIndexesMap.put(tokenTypeI, indexes);
			}
			else {
				// GMS - fix this.
				if ( !indexes.contains(streamIndexI) ) {
					indexes.add(streamIndexI); // not there yet, add
				}
			}
		}
	
		/** Track the indicated token type in the reverse index.  Call this
		 *  repeatedly for each type or use variant with Set argument to
		 *  set all at once.
		 * @param tokenType
		 */
		public function reverseIndex(tokenType:int):void {
			if ( tokenTypesToReverseIndex==null ) {
				tokenTypesToReverseIndex = new Object();
			}
			else if ( tokenTypesToReverseIndex==INDEX_ALL ) {
				return;
			}
			tokenTypesToReverseIndex[new String(tokenType)] = true;
		}
	
		/** Track the indicated token types in the reverse index. Set
		 *  to INDEX_ALL to track all token types.
		 * 
		 * GMS - tokenTypes was Set
		 */
		public function reverseIndexSet(tokenTypes:Object):void {
			tokenTypesToReverseIndex = tokenTypes;
		}
	
		/** Given a node pointer, return its index into the node stream.
		 *  This is not its Token stream index.  If there is no reverse map
		 *  from node to stream index or the map does not contain entries
		 *  for node's token type, a linear search of entire stream is used.
		 *
		 *  Return -1 if exact node pointer not in stream.
		 */
		public function getNodeIndex(node:Object):int {
			//System.out.println("get "+node);
			if ( tokenTypeToStreamIndexesMap==null ) {
				return getNodeIndexLinearly(node);
			}
			var tokenType:int = adaptor.getType(node);
			var tokenTypeI:String = new String(tokenType);
			var indexes:Array = tokenTypeToStreamIndexesMap[tokenTypeI];
			if ( indexes==null ) {
				//System.out.println("found linearly; stream index = "+getNodeIndexLinearly(node));
				return getNodeIndexLinearly(node);
			}
			for (var i:int = 0; i < indexes.size(); i++) {
				var streamIndexI:String = String(indexes[i]);
				var n:Object = getNode(int(streamIndexI));
				if ( n==node ) {
					//System.out.println("found in index; stream index = "+streamIndexI);
					return int(streamIndexI); // found it!
				}
			}
			return -1;
		}
	
		protected function getNodeIndexLinearly(node:Object):int {
			if ( p==-1 ) {
				fillBuffer();
			}
			for (var i:int = 0; i < nodes.length; i++) {
				var t:Object = nodes[i];
				if ( t==node ) {
					return i;
				}
			}
			return -1;
		}
	
		/** As we flatten the tree, we use UP, DOWN nodes to represent
		 *  the tree structure.  When debugging we need unique nodes
		 *  so instantiate new ones when uniqueNavigationNodes is true.
		 */
		protected function addNavigationNode(ttype:int):void {
			var navNode:Object = null;
			if ( ttype==TokenConstants.DOWN ) {
				if ( hasUniqueNavigationNodes) {
					navNode = adaptor.createFromString(TokenConstants.DOWN, "DOWN");
				}
				else {
					navNode = down;
				}
			}
			else {
				if ( hasUniqueNavigationNodes ) {
					navNode = adaptor.createFromString(TokenConstants.UP, "UP");
				}
				else {
					navNode = up;
				}
			}
			nodes.push(navNode);
		}
	
		public function getNode(i:int):Object {
			if ( p==-1 ) {
				fillBuffer();
			}
			return nodes[i];
		}
	
		public function LT(k:int):Object {
			if ( p==-1 ) {
				fillBuffer();
			}
			if ( k==0 ) {
				return null;
			}
			if ( k<0 ) {
				return LB(-k);
			}
			//System.out.print("LT(p="+p+","+k+")=");
			if ( (p+k-1) >= nodes.length ) {
				return eof;
			}
			return nodes[p+k-1];
		}
	
	/*
		public Object getLastTreeNode() {
			int i = index();
			if ( i>=size() ) {
				i--; // if at EOF, have to start one back
			}
			System.out.println("start last node: "+i+" size=="+nodes.size());
			while ( i>=0 &&
				(adaptor.getType(get(i))==Token.EOF ||
				 adaptor.getType(get(i))==Token.UP ||
				 adaptor.getType(get(i))==Token.DOWN) )
			{
				i--;
			}
			System.out.println("stop at node: "+i+" "+nodes.get(i));
			return nodes.get(i);
		}
	*/
		
		/** Look backwards k nodes */
		protected function LB(k:int):Object {
			if ( k==0 ) {
				return null;
			}
			if ( (p-k)<0 ) {
				return null;
			}
			return nodes[p-k];
		}
	
		public function get treeSource():Object {
			return root;
		}
	
		public function get tokenStream():TokenStream {
			return tokens;
		}
	
		public function set tokenStream(tokens:TokenStream):void {
			this.tokens = tokens;
		}
	
		public function get treeAdaptor():TreeAdaptor {
			return adaptor;
		}
	
		public function get hasUniqueNavigationNodes():Boolean {
			return uniqueNavigationNodes;
		}
	
		public function set hasUniqueNavigationNodes(uniqueNavigationNodes:Boolean):void {
			this.uniqueNavigationNodes = uniqueNavigationNodes;
		}
	
		public function consume():void {
			if ( p==-1 ) {
				fillBuffer();
			}
			p++;
		}
	
		public function LA(i:int):int {
			return adaptor.getType(LT(i));
		}
	
		public function mark():int {
			if ( p==-1 ) {
				fillBuffer();
			}
			lastMarker = index;
			return lastMarker;
		}
	
		public function release(marker:int):void {
			// no resources to release
		}
	
		public function get index():int {
			return p;
		}
	
		public function rewindTo(marker:int):void {
			seek(marker);
		}
	
		public function rewind():void {
			seek(lastMarker);
		}
	
		public function seek(index:int):void {
			if ( p==-1 ) {
				fillBuffer();
			}
			p = index;
		}
	
		/** Make stream jump to a new location, saving old location.
		 *  Switch back with pop().  I manage dyanmic array manually
		 *  to avoid creating Integer objects all over the place.
		 */
		public function push(index:int):void {
			if ( calls==null ) {
				calls = new Array(INITIAL_CALL_STACK_SIZE);
			}
			calls[++_sp] = p; // save current index
			seek(index);
		}
	
		/** Seek back to previous index saved during last push() call.
		 *  Return top of stack (return index).
		 */
		public function pop():int {
			var ret:int = calls[_sp--];
			seek(ret);
			return ret;
		}
	
		public function get size():int {
			if ( p==-1 ) {
				fillBuffer();
			}
			return nodes.size();
		}
	
	/*  GMS - need to see where used
		public Iterator iterator() {
			if ( p==-1 ) {
				fillBuffer();
			}
			return new StreamIterator();
		}
	*/
		/** Used for testing, just return the token type stream */
		public function toString():String {
			if ( p==-1 ) {
				fillBuffer();
			}
			var buf:String = "";
			for (var i:int = 0; i < nodes.length; i++) {
				var t:Object = nodes[i];
				buf += " ";
				buf += (adaptor.getType(t));
			}
			return buf.toString();
		}
	
		public function toStringRange(start:Object, stop:Object):String {
			if ( start==null || stop==null ) {
				return null;
			}
			if ( p==-1 ) {
				fillBuffer();
			}
			trace("stop: "+stop);
			if ( start is CommonTree )
				trace("toString: "+CommonTree(start).token+", ");
			else
				trace(start);
			if ( stop is CommonTree )
				trace(CommonTree(stop).token);
			else
				trace(stop);
			// if we have the token stream, use that to dump text in order
			if ( tokens!=null ) {
				var beginTokenIndex:int = adaptor.getTokenStartIndex(start);
				var endTokenIndex:int = adaptor.getTokenStopIndex(stop);
				// if it's a tree, use start/stop index from start node
				// else use token range from start/stop nodes
				if ( adaptor.getType(stop)==TokenConstants.UP ) {
					endTokenIndex = adaptor.getTokenStopIndex(start);
				}
				else if ( adaptor.getType(stop)==TokenConstants.EOF ) {
					endTokenIndex = size-2; // don't use EOF
				}
				return tokens.toStringRange(beginTokenIndex, endTokenIndex);
			}
			// walk nodes looking for start
			var t:Object = null;
			var i:int = 0;
			for (; i < nodes.size(); i++) {
				t = nodes.get(i);
				if ( t==start ) {
					break;
				}
			}
			// now walk until we see stop, filling string buffer with text
			 var buf:String = "";
			t = nodes.get(i);
			while ( t!=stop ) {
				var text:String = adaptor.getText(t);
				if ( text==null ) {
					text = " "+ adaptor.getType(t);
				}
				buf += text;
				i++;
				t = nodes[i];
			}
			// include stop node too
			text = adaptor.getText(stop);
			if ( text==null ) {
				text = " " + adaptor.getType(stop);
			}
			buf += text;
			return buf.toString();
		}
	}

}