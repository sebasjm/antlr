package org.antlr.runtime.tree
{
	import flash.utils.Dictionary;
	
	import org.antlr.runtime.Token;
	
	public class BaseTreeAdaptor implements TreeAdaptor {
		/** System.identityHashCode() is not always unique due to GC; we have to
		 *  track ourselves.  That's ok, it's only for debugging, though it's
		 *  expensive: we have to create a hashtable with all tree nodes in it.
		 */
		protected var treeToUniqueIDMap:Dictionary;
		protected var uniqueNodeID:int = 1;
	
		public function nil():Object {
			return create(null);
		}
	
		public function isNil(tree:Object):Boolean {
			return Tree(tree).isNil();
		}
	
		public function dupTree(tree:Object):Object {
			return Tree(tree).dupTree();
		}
	
		/** Add a child to the tree t.  If child is a flat tree (a list), make all
		 *  in list children of t.  Warning: if t has no children, but child does
		 *  and child isNil then you can decide it is ok to move children to t via
		 *  t.children = child.children; i.e., without copying the array.  Just
		 *  make sure that this is consistent with have the user will build
		 *  ASTs.
		 */
		public function addChild(t:Object, child:Object):void {
			if ( t!=null && child!=null ) {
				Tree(t).addChild(Tree(child));
			}
		}
	
		/** If oldRoot is a nil root, just copy or move the children to newRoot.
		 *  If not a nil root, make oldRoot a child of newRoot.
		 *
		 *    old=^(nil a b c), new=r yields ^(r a b c)
		 *    old=^(a b c), new=r yields ^(r ^(a b c))
		 *
		 *  If newRoot is a nil-rooted single child tree, use the single
		 *  child as the new root node.
		 *
		 *    old=^(nil a b c), new=^(nil r) yields ^(r a b c)
		 *    old=^(a b c), new=^(nil r) yields ^(r ^(a b c))
		 *
		 *  If oldRoot was null, it's ok, just return newRoot (even if isNil).
		 *
		 *    old=null, new=r yields r
		 *    old=null, new=^(nil r) yields ^(nil r)
		 *
		 *  Return newRoot.  Throw an exception if newRoot is not a
		 *  simple node or nil root with a single child node--it must be a root
		 *  node.  If newRoot is ^(nil x) return x as newRoot.
		 *
		 *  Be advised that it's ok for newRoot to point at oldRoot's
		 *  children; i.e., you don't have to copy the list.  We are
		 *  constructing these nodes so we should have this control for
		 *  efficiency.
		 */
		public function becomeRoot(newRoot:Object, oldRoot:Object):Object {
			var newRootTree:Tree = Tree(newRoot);
			var oldRootTree:Tree = Tree(oldRoot);
			if ( oldRoot==null ) {
				return newRoot;
			}
			// handle ^(nil real-node)
			if ( newRootTree.isNil() ) {
				if ( newRootTree.childCount > 1 ) {
					// TODO: make tree run time exceptions hierarchy
					throw new Error("more than one node as root (TODO: make exception hierarchy)");
				}
				newRootTree = newRootTree.getChild(0);
			}
			// add oldRoot to newRoot; addChild takes care of case where oldRoot
			// is a flat list (i.e., nil-rooted tree).  All children of oldRoot
			// are added to newRoot.
			newRootTree.addChild(oldRootTree);
			return newRootTree;
		}
	
		/** Transform ^(nil x) to x */
		public function rulePostProcessing(root:Object):Object {
			var r:Tree = Tree(root);
			if ( r!=null && r.isNil() && r.childCount==1 ) {
				r = r.getChild(0);
			}
			return r;
		}
	
		public function becomeRootToken(newRoot:Token, oldRoot:Object):Object {
			return becomeRoot(create(newRoot), oldRoot);
		}
	
		public function createFromToken(tokenType:int, fromToken:Token):Object {
			fromToken = createTokenFromToken(fromToken);
			//((ClassicToken)fromToken).setType(tokenType);
			fromToken.type = tokenType;
			return create(fromToken);
		}
	
		public function createFromTokenString(tokenType:int, fromToken:Token, text:String):Object {
			fromToken = createTokenFromToken(fromToken);
			fromToken.type = tokenType;
			fromToken.text = text;
			return create(fromToken);
		}
	
		public function createFromString(tokenType:int, text:String):Object {
			var fromToken:Token = createToken(tokenType, text);
			return create(fromToken);
		}
	
		public function getType(t:Object):int {
			Tree(t).type;
			return 0;
		}
	
		public function setType(t:Object, type:int):void {
			throw new Error("don't know enough about Tree node");
		}
	
		public function getText(t:Object):String {
			return Tree(t).text;
		}
	
		public function setText(t:Object, text:String):void {
			throw new Error("don't know enough about Tree node");
		}
	
		public function getChild(t:Object, i:int):Object {
			return Tree(t).getChild(i);
		}
	
		public function getChildCount(t:Object):int {
			return Tree(t).childCount;
		}
	
		public function getUniqueID(node:Object):int {
			if ( treeToUniqueIDMap==null ) {
				 treeToUniqueIDMap = new Dictionary();
			}
			// GMS - check this behavior
			if (treeToUniqueIDMap.hasOwnProperty(node)) {
				return treeToUniqueIDMap[node];
			}

			var ID:int = uniqueNodeID;
			treeToUniqueIDMap.put(node, ID);
			uniqueNodeID++;
			return ID;

		}
	
		/** Tell me how to create a token for use with imaginary token nodes.
		 *  For example, there is probably no input symbol associated with imaginary
		 *  token DECL, but you need to create it as a payload or whatever for
		 *  the DECL node as in ^(DECL type ID).
		 *
		 *  If you care what the token payload objects' type is, you should
		 *  override this method and any other createToken variant.
		 * 
		 * GMS - was abstract
		 */
		public function createToken(tokenType:int, text:String):Token {
			throw new Error("Not implemented - abstract function");
		}
	
		/** Tell me how to create a token for use with imaginary token nodes.
		 *  For example, there is probably no input symbol associated with imaginary
		 *  token DECL, but you need to create it as a payload or whatever for
		 *  the DECL node as in ^(DECL type ID).
		 *
		 *  This is a variant of createToken where the new token is derived from
		 *  an actual real input token.  Typically this is for converting '{'
		 *  tokens to BLOCK etc...  You'll see
		 *
		 *    r : lc='{' ID+ '}' -> ^(BLOCK[$lc] ID+) ;
		 *
		 *  If you care what the token payload objects' type is, you should
		 *  override this method and any other createToken variant.
		 * 
		 * GMS - was abstract
		 */
		public function createTokenFromToken(fromToken:Token):Token {
			throw new Error("Not implemented - abstract function");
		}
		
		public function create(payload:Token):Object {
			throw new Error("Not implemented - abstract function");
		}
		
		public function dupNode(t:Object):Object {
			throw new Error("Not implemented - abstract function");
		}
	
		public function getToken(t:Object):Token {
			throw new Error("Not implemented - abstract function");
		}
	
		public function setTokenBoundaries(t:Object, startToken:Token, stopToken:Token):void {
			throw new Error("Not implemented - abstract function");
		}
	
		public function getTokenStartIndex(t:Object):int {
			throw new Error("Not implemented - abstract function");
		}
	
		public function getTokenStopIndex(t:Object):int {
			throw new Error("Not implemented - abstract function");
		}
	
	}


}