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

import org.antlr.runtime.Token;

/** How to create and navigate trees.  Rather than have a separate factory
 *  and adaptor, I've merged them.  Makes sense to encapsulate.
 *
 *  This takes the place of the tree construction code generated in the
 *  generated code in 2.x and the ASTFactory.
 *
 *  I do not need to know the type of a tree at all so they are all
 *  generic Objects.  This may increase the amount of typecasting needed. :(
 */
public interface TreeAdaptor {
	// C o n s t r u c t i o n

	/** Create a tree node from Token object; for CommonTree type trees,
	 *  then the token just becomes the payload.
     */
	public Object create(Token payload);

	/** Duplicate tree recursively, using dupNode() for each node */
	public Object dupTree(Object tree);

	/** Duplicate a single tree node */
	public Object dupNode(Object treeNode);

	/** Return a nil node (an empty but non-null node) that can hold
	 *  a list of element as the children.  If you want a flat tree (a list)
	 *  use "t=adaptor.nil(); t.addChild(x); t.addChild(y);"
	 */
	public Object nil();

	/** Add a child to the tree t.  If child is a flat tree (a list), make all
	 *  in list children of t.
	 */
	public void addChild(Object t, Object child);

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
	 *
	 *  Be advised that it's ok for newRoot to point at oldRoot's
	 *  children; i.e., you don't have to copy the list.  We are
	 *  constructing these nodes to we should have this control for
	 *  efficiency.
	 */
	public Object becomeRoot(Object newRoot, Object oldRoot);

	/** Given the root of the subtree created for this rule, post process
	 *  it to do any simplifications or whatever you want.  A required
	 *  behavior is to convert ^(nil singleSubtree) to singleSubtree
	 *  as the setting of start/stop indexes relies on a single non-nil root
	 *  for non-flat trees (such as for lists like "idlist : ID+ ;").
	 *
	 *  This method is executed after all rule tree construction and right
	 *  before setTokenBoundaries().
	 */
	public Object rulePostProcessing(Object root);


	// R e w r i t e  R u l e s

	/** Create a node for child and add as a child to root t.  Rather
	 *  than invoke create directly in the generated code for child,
	 *  the coder has more flexibility if the token itself is passed in.
	 *  You might want to do something different during rewrite construction
	 *  than you do during auto-AST construction (which has create() calls
	 *  generated in the output).
	 */
	public void addChild(Object t, Token child);

	/** Create a node for newRoot make it the root of oldRoot.
	 *  If oldRoot is a nil root, just copy or move the children to newRoot.
	 *  If not a nil root, make oldRoot a child of newRoot.
	 *
	 *  Return node created for newRoot.
	 */
	public Object becomeRoot(Token newRoot, Object oldRoot);

	/** Tell me how to create a token for use with imaginary token nodes.
	 *  For example, there is probably no input symbol associated with imaginary
	 *  token DECL, but you need to create it as a payload or whatever for
	 *  the DECL node as in ^(DECL type ID).
	 *
	 *  If you care what the token payload objects' type is, you should
	 *  override this method and any other createToken variant.
	 */
	public Token createToken(int tokenType, String text);

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
	 */
	public Token createToken(Token fromToken);

	/** Create a new node derived from a token, with a new token type.
	 *  This is invoked from an imaginary node ref on right side of a
	 *  rewrite rule as IMAG[$tokenLabel].
	 *
	 *  This should invoke createToken(Token).
	 */
	public Object create(int tokenType, Token fromToken);

	/** Same as create(tokenType,fromToken) except set the text too.
	 *  This is invoked from an imaginary node ref on right side of a
	 *  rewrite rule as IMAG[$tokenLabel, "IMAG"].
	 *
	 *  This should invoke createToken(Token).
	 */
	public Object create(int tokenType, Token fromToken, String text);

	/** Create a new node derived from a token, with a new token type.
	 *  This is invoked from an imaginary node ref on right side of a
	 *  rewrite rule as IMAG["IMAG"].
	 *
	 *  This should invoke createToken(int,String).
	 */
	public Object create(int tokenType, String text);


	// C o n t e n t

	/** For tree parsing, I need to know the token type of a node */
	public int getType(Object t);

	/** Node constructors can set the type of a node */
	public void setType(Object t, int type);

	/** Node constructors can set the text of a node */
	public void setText(Object t, String text);

	/** Where are the bounds in the input token stream for this node and
	 *  all children?  Each rule that creates AST nodes will call this
	 *  method right before returning.  Flat trees (i.e., lists) will
	 *  still usually have a nil root node just to hold the children list.
	 *  That node would contain the start/stop indexes then.
	 */
	public void setTokenBoundaries(Object t, Token startToken, Token stopToken);

	// N a v i g a t i o n  /  T r e e  P a r s i n g

	/** Get a child 0..n-1 node */
	public Object getChild(int i);

	/** How many children?  If 0, then this is a leaf node */
	public int getChildCount();
}
