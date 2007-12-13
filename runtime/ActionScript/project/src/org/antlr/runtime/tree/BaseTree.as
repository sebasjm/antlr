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
	/** A generic tree implementation with no payload.  You must subclass to
	 *  actually have any user data.  ANTLR v3 uses a list of children approach
	 *  instead of the child-sibling approach in v2.  A flat tree (a list) is
	 *  an empty node whose children represent the list.  An empty, but
	 *  non-null node is called "nil".
	 */
	public class BaseTree implements Tree {
		protected var children:Array;
	
		/** Create a new node from an existing node does nothing for BaseTree
		 *  as there are no fields other than the children list, which cannot
		 *  be copied as the children are not considered part of this node. 
		 */
		public function BaseTree(node:Tree = null) {
		}
	
		public function getChild(i:int):Tree {
			if ( children==null || i>=children.length ) {
				return null;
			}
			return BaseTree(children[i]);
		}
	
		public function getFirstChildWithType(type:int):Tree {
			for (var i:int = 0; children!=null && i < children.length; i++) {
				var t:Tree = Tree(children[i]);
				if ( t.type==type ) {
					return t;
				}
			}	
			return null;
		}
	
		public function get childCount():int {
			if ( children==null ) {
				return 0;
			}
			return children.length;
		}
	
		/** Add t as child of this node.
		 *
		 *  Warning: if t has no children, but child does
		 *  and child isNil then this routine moves children to t via
		 *  t.children = child.children; i.e., without copying the array.
		 */
		public function addChild(t:Tree):void {
			//System.out.println("add "+t.toStringTree()+" as child to "+this.toStringTree());
			if ( t==null ) {
				return; // do nothing upon addChild(null)
			}
			var childTree:BaseTree = BaseTree(t);
			if ( childTree.isNil() ) { // t is an empty node possibly with children
				if ( this.children!=null && this.children == childTree.children ) {
					throw new Error("attempt to add child list to itself");
				}
				// just add all of childTree's children to this
				if ( childTree.children!=null ) {
					if ( this.children!=null ) { // must copy, this has children already
						var n:int = childTree.children.length;
						for (var i:int = 0; i < n; i++) {
							this.children.push(childTree.children[i]);
						}
					}
					else {
						// no children for this but t has children; just set pointer
						this.children = childTree.children;
					}
				}
			}
			else { // t is not empty and might have children
				if ( children==null ) {
					children = createChildrenList(); // create children list on demand
				}
				children.push(t);
			}
		}
	
		/** Add all elements of kids list as children of this node */
		public function addChildren(kids:Array):void {
			for (var i:int = 0; i < kids.size(); i++) {
				var t:Tree = Tree(kids[i]);
				addChild(t);
			}
		}
	
		public function setChild(i:int, t:BaseTree):void {
			if ( children==null ) {
				children = createChildrenList();
			}
			children[i] =  t;
		}
	
		public function deleteChild(i:int):BaseTree {
			if ( children==null ) {
				return null;
			}
			return BaseTree(children.remove(i));
		}
	
		/** Override in a subclass to change the impl of children list */
		// GMS is is this needed?
		protected function createChildrenList():Array {
			return new Array();
		}
	
		public function isNil():Boolean {
			return false;
		}
	
		/** Recursively walk this tree, dup'ing nodes until you have copy of
		 *  this tree.  This method should work for all subclasses as long
		 *  as they override dupNode().
		 */
		public function dupTree():Tree {
			var newTree:Tree = this.dupNode();
			for (var i:int = 0; children!=null && i < children.length; i++) {
				var t:Tree = Tree(children[i]);
				var newSubTree:Tree = t.dupTree();
				newTree.addChild(newSubTree);
			}
			return newTree;
		}
	
		/** Print out a whole tree not just a node */
	    public function toStringTree():String {
			if ( children==null || children.length==0 ) {
				return this.toString();
			}
			var buf:String = "";
			if ( !isNil() ) {
				buf += "(";
				buf += this.toString();
				buf += ' ';
			}
			for (var i:int = 0; children!=null && i < children.length; i++) {
				var t:BaseTree = BaseTree(children[i]);
				if ( i>0 ) {
					buf += ' ';
				}
				buf += t.toStringTree();
			}
			if ( !isNil() ) {
				buf += ")";
			}
			return buf;
		}
	
	    public function get line():int {
			return 0;
		}
	
		public function get charPositionInLine():int {
			return 0;
		}

		/** :Abstract" functions - GMS
		 * Since no abstract classes in actionscript
		 *  */
		public function dupNode():Tree {
			return null;
		}
	
		public function get type():int {
			return 0;
		}
	
		public function get text():String {
			return toString();
		}
	
		public function get tokenStartIndex():int {
			return 0;
		}
	
		public function set tokenStartIndex(index:int):void {
		}
	
		public function get tokenStopIndex():int {
			return 0;
		}
	
		public function set tokenStopIndex(index:int):void {
		}
	
		public function toString():String {
			return "";
		}
	}
}