package org.antlr.analysis;

import org.antlr.tool.GrammarAST;
import org.antlr.tool.Grammar;

public class ActionLabel extends Label {
	public GrammarAST actionAST;
	
	public ActionLabel(GrammarAST actionAST) {
		super(ACTION);
		this.actionAST = actionAST;
	}

	public boolean isEpsilon() {
		return true; // we are to be ignored by analysis 'cept for predicates
	}

	public boolean isAction() {
		return true;
	}

	public String toString() {
		return "{"+actionAST+"}";
	}

	public String toString(Grammar g) {
		return toString();
	}
}
