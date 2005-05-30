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

import java.util.List;
import java.util.ArrayList;

/** A generic tree. */
public class CommonTree implements Tree {
	protected Tree parent;
	protected List children;

	protected Object payload;

	public CommonTree() {;}

	public CommonTree(Object payload) {
		this.payload = payload;
	}

	public Object getPayload() {
		return payload;
	}

	public void setPayload(Object payload) {
		this.payload = payload;
	}

	public Tree getParent() {
		return parent;
	}

	public Tree getChild(int i) {
		if ( children==null || i>=children.size() ) {
			return null;
		}
		return (Tree)children.get(i);
	}

	public int getChildCount() {
		if ( children==null ) {
			return 0;
		}
		return children.size();
	}

	public void setParent(Tree t) {
		parent = t;
	}

	public void addChild(Tree t) {
		if ( children==null ) {
			createChildrenList();
		}
		children.add(t);
		t.setParent(this);
	}

	public void setChild(int i, Tree t) {
		if ( children==null ) {
			createChildrenList();
		}
		children.set(i, t);
		t.setParent(this);
	}

	public Tree deleteChild(int i) {
		if ( children==null ) {
			return null;
		}
		return (Tree)children.remove(i);
	}

	/** Override in a subclass to change the impl of children list */
	protected void createChildrenList() {
		children = new ArrayList();
	}

	public String toString() {
		if ( children==null || children.size()==0 ) {
			return payload.toString();
		}
		StringBuffer buf = new StringBuffer();
		buf.append("(");
		buf.append(payload.toString());
		for (int i = 0; i < children.size(); i++) {
			Tree t = (Tree) children.get(i);
			buf.append(' ');
			buf.append(t.toString());
		}
		buf.append(")");
		return buf.toString();
	}
}
