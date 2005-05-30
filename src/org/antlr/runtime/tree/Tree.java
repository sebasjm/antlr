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

/** What does a generic tree look like?  AST and parse trees derive from
 *  this interface.  This is called Tree instead of node or whatever
 *  because node is just a single-node tree.
 *
 *  A tree node is really just a list of children and a payload pointer.
 *  ANTLR 2.x used child-first-sibling trees, but I believe this more
 *  traditional node is better.
 *
 *  This is the minimal definition of a tree usable by all ANTLR concepts such
 *  as parse tree and AST building plus tree parsing; technically we don't even
 *  need the parent, but that can just return null if your implementation
 *  doesn't need it.
 *
 *  With child-sibling trees from 2.x, it was easy to have a flat tree (a list
 *  of nodes).  How do you do that with this Tree definition when there is no
 *  "next" pointer?  You must have an "empty" root node whose children are the
 *  list elements.  So, flat tree A B C, perhaps created from rule
 *
 *   a : A B C ;
 *
 *  would actually have a root Tree node with a payload of null and three
 *  children.  Tree syntax: ^(nil A B C).
 */
public interface Tree {
	/** What user data is to be associated with this node? */
	public Object getPayload();
	public void setPayload(Object payload);

	/** Get a child 0..n-1 node */
	public Tree getChild(int i);

	/** How many children?  If 0, then this is a leaf node */
	public int getChildCount();

	/** I am a child of what node?  Not required but I want it available. */
	public Tree getParent();

	// routines to construct/alter trees

	public void setParent(Tree t);
	public void addChild(Tree t);
	public void setChild(int i, Tree t);
	public Tree deleteChild(int i);
}
