/*
[The "BSD licence"]
Copyright (c) 2005-2007 Terence Parr
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
package org.antlr.tool;

import org.antlr.runtime.tree.BaseTree;
import org.antlr.runtime.tree.Tree;

import java.util.List;
import java.util.ArrayList;

/** A tree of grammars */
public class CompositeGrammarTree {
	protected List<CompositeGrammarTree> children;
	public Grammar grammar;

	/** Who is the parent node of this node; if null, implies node is root */
	public CompositeGrammarTree parent;

	public CompositeGrammarTree(Grammar g) {
		grammar = g;
	}

	public void addChild(CompositeGrammarTree t) {
		//System.out.println("add "+t.toStringTree()+" as child to "+this.toStringTree());
		if ( t==null ) {
			return; // do nothing upon addChild(null)
		}
		if ( children==null ) {
			children = new ArrayList();
		}
		children.add(t);
		t.parent = this;
	}

	public Rule getRule(String ruleName) {
		Rule r = grammar.getLocallyDefinedRule(ruleName);
		for (int i = 0; r==null && children!=null && i < children.size(); i++) {
			CompositeGrammarTree child = children.get(i);
			r = child.getRule(ruleName);
		}
		return r;
	}

	public CompositeGrammarTree findNode(Grammar g) {
		if ( g==null ) {
			return null;
		}
		if ( this.grammar == g ) {
			return this;
		}
		CompositeGrammarTree n = null;
		for (int i = 0; n==null && children!=null && i < children.size(); i++) {
			CompositeGrammarTree child = children.get(i);
			n = child.findNode(g);
		}
		return n;
	}

	public CompositeGrammarTree findNode(String grammarName) {
		if ( grammarName==null ) {
			return null;
		}
		if ( grammarName.equals(this.grammar.name) ) {
			return this;
		}
		CompositeGrammarTree n = null;
		for (int i = 0; n==null && children!=null && i < children.size(); i++) {
			CompositeGrammarTree child = children.get(i);
			n = child.findNode(grammarName);
		}
		return n;
	}

	/** Return a preorder list of grammars */
	public List<Grammar> collectAllGrammars() {
		List<Grammar> grammars = new ArrayList();
		_collectAllGrammars(grammars);
		return grammars;
	}

	/** work for collectAllGrammars */
	protected void _collectAllGrammars(List<Grammar> grammars) {
		grammars.add(this.grammar);
		CompositeGrammarTree n = null;
		for (int i = 0; n==null && children!=null && i < children.size(); i++) {
			CompositeGrammarTree child = children.get(i);
			child._collectAllGrammars(grammars);
		}
	}
}