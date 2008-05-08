package org.antlr.analysis;

import org.antlr.tool.GrammarAST;
import org.antlr.tool.Grammar;

public class PredicateLabel extends Label {
	/** A tree of semantic predicates from the grammar AST if label==SEMPRED.
	 *  In the NFA, labels will always be exactly one predicate, but the DFA
	 *  may have to combine a bunch of them as it collects predicates from
	 *  multiple NFA configurations into a single DFA state.
	 */
	protected SemanticContext semanticContext;
	
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

	public int hashCode() {
		return semanticContext.hashCode();
	}

	public boolean equals(Object o) {
		if ( o==null ) {
			return false;
		}
		if ( this == o ) {
			return true; // equals if same object
		}
		return semanticContext.equals(((PredicateLabel)o).semanticContext);
	}

	public boolean isSemanticPredicate() {
		return true;
	}

	public SemanticContext getSemanticContext() {
		return semanticContext;
	}

	public String toString() {
		return "{"+semanticContext+"}?";
	}

	public String toString(Grammar g) {
		return toString();
	}
}
