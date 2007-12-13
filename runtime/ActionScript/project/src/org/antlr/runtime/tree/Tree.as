package org.antlr.runtime.tree {
	/** What does a tree look like?  ANTLR has a number of support classes
	 *  such as CommonTreeNodeStream that work on these kinds of trees.  You
	 *  don't have to make your trees implement this interface, but if you do,
	 *  you'll be able to use more support code.
	 *
	 *  NOTE: When constructing trees, ANTLR can build any kind of tree; it can
	 *  even use Token objects as trees if you add a child list to your tokens.
	 *
	 *  This is a tree node without any payload; just navigation and factory stuff.
	 */
	public interface Tree {
	
		function getChild(i:int):Tree;
	
		function get childCount():int;
	
		/** Add t as a child to this node.  If t is null, do nothing.  If t
		 *  is nil, add all children of t to this' children.
		 * @param t
		 */
		function addChild(t:Tree):void;
	
		/** Indicates the node is a nil node but may still have children, meaning
		 *  the tree is a flat list.
		 */
		function isNil():Boolean;
	
		/**  What is the smallest token index (indexing from 0) for this node
		 *   and its children?
		 */
		function get tokenStartIndex():int;
	
		function set tokenStartIndex(index:int):void;
	
		/**  What is the largest token index (indexing from 0) for this node
		 *   and its children?
		 */
		function get tokenStopIndex():int;
	
		function set tokenStopIndex(index:int):void;
	
		function dupTree():Tree;
	
		function dupNode():Tree;
	
		/** Return a token type; needed for tree parsing */
		function get type():int;
	
		function get text():String;
	
		/** In case we don't have a token payload, what is the line for errors? */
		function get line():int;
	
		function get charPositionInLine():int;
	
		function toStringTree():String;
	
		function toString():String;
	}

}