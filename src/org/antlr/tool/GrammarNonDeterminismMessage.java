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
public class GrammarNonDeterminismMessage extends Message {
	public DecisionProbe probe;
    public DFAState problemState;

	public GrammarNonDeterminismMessage(DecisionProbe probe,
										DFAState problemState)
	{
		super(ErrorManager.MSG_GRAMMAR_NONDETERMINISM);
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

		// Now fill template with information about problemState
		List labels = probe.getSampleNonDeterministicInputSequence(problemState);
		String input = probe.getInputSequenceDisplay(labels);
		st.setAttribute("input", input);

		st.setAttribute("disabled", probe.getDisabledAlternatives(problemState));

		List nondetAlts = probe.getNonDeterministicAltsForState(problemState);
		NFAState nfaStart = probe.dfa.getNFADecisionStartState();
		// all state paths have to begin with same NFA state
		NFAState commonNondetAltsNFAStartState = null;
		int firstAlt = 0;
		for (Iterator iter = nondetAlts.iterator(); iter.hasNext();) {
			Integer displayAltI = (Integer) iter.next();
			if ( DecisionProbe.verbose ) {
				int tracePathAlt =
					nfaStart.translateDisplayAltToWalkAlt(displayAltI.intValue());
				if ( firstAlt == 0 ) {
					firstAlt = tracePathAlt;
				}
				List path =
					probe.getNFAPathStatesForAlt(firstAlt,
												 tracePathAlt,
												 labels);
				st.setAttribute("paths.{alt,states}",
								displayAltI, path);
			}
			else {
				st.setAttribute("conflictingAlts", displayAltI);
			}
		}
		return st.toString();
	}

}
