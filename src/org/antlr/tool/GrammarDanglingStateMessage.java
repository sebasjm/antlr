package org.antlr.tool;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.analysis.DecisionProbe;
import org.antlr.analysis.DFAState;
import org.antlr.analysis.NFAState;
import org.antlr.analysis.SemanticContext;
import antlr.Token;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Reports a potential parsing issue with a decision; the decision is
 *  nondeterministic in some way.
 */
public class GrammarDanglingStateMessage extends Message {
	public DecisionProbe probe;
    public DFAState problemState;

	public GrammarDanglingStateMessage(DecisionProbe probe,
									   DFAState problemState)
	{
		super(ErrorManager.MSG_DANGLING_STATE);
		this.probe = probe;
		this.problemState = problemState;
	}

	public String toString() {
		GrammarAST decisionASTNode = probe.dfa.getDecisionASTNode();
		int line = decisionASTNode.getLine();
		int col = decisionASTNode.getColumn();
		String fileName = probe.dfa.nfa.grammar.getFileName();
		StringTemplate st = getMessageTemplate();
		if ( fileName!=null ) {
			st.setAttribute("file", fileName);
		}
		st.setAttribute("line", new Integer(line));
		st.setAttribute("col", new Integer(col));

		st.setAttribute("danglingAlts", problemState.getAltSet());

		return st.toString();
	}

}
