package org.antlr.analysis;

import org.antlr.tool.GrammarAST;

public class PredicateLabel extends Label {
	/** Make a semantic predicate label */
	public PredicateLabel(GrammarAST predicateASTNode) {
		super(SEMPRED);
		this.semanticContext = new SemanticContext.Predicate(predicateASTNode);
	}

	/** Make a semantic predicates label */
	public PredicateLabel(SemanticContext semCtx) {
		super(SEMPRED);
		this.semanticContext = semCtx;
	}

	public boolean isSemanticPredicate() {
		return true;
	}
}
