package org.antlr.codegen;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.analysis.DFA;
import org.antlr.analysis.DFAState;
import org.antlr.analysis.Transition;
import org.antlr.analysis.Label;
import org.antlr.misc.BitSet;
import org.antlr.misc.IntSet;

public class CyclicDFACodeGenerator {
	protected CodeGenerator parent;

	/** Used by cyclic DFA state machine generator to avoid infinite recursion
	 *  resulting from cycles int the DFA.  This is a set of int state #s.
	 */
	protected IntSet visited;

	public CyclicDFACodeGenerator(CodeGenerator parent) {
		this.parent = parent;
	}

	public StringTemplate genCyclicLookaheadDecision(StringTemplateGroup templates,
													 DFA dfa)
	{
		StringTemplate dfaST = templates.getInstanceOf("cyclicDFA");
		int d = dfa.getDecisionNumber();
		dfaST.setAttribute("decision", new Integer(d));
		visited = new BitSet(dfa.getNumberOfStates());
		walkFixedDFAGeneratingStateMachine(templates, dfaST, dfa.startState);
		parent.decisionToMaxLookaheadDepth[dfa.getDecisionNumber()]
			= Integer.MAX_VALUE;
		return dfaST;
	}

	protected void walkFixedDFAGeneratingStateMachine(
			StringTemplateGroup templates,
			StringTemplate dfaST,
			DFAState s)
	{
		if ( visited.member(s.stateNumber) ) {
			return; // already visited
		}
		visited.add(s.stateNumber);

		StringTemplate stateST;
		if ( s.isAcceptState() ) {
			stateST = templates.getInstanceOf("cyclicDFAAcceptState");
			stateST.setAttribute("predictAlt",
								 new Integer(s.getUniquelyPredictedAlt()));
		}
		else {
			stateST = templates.getInstanceOf("cyclicDFAState");
			stateST.setAttribute("needErrorClause", new Boolean(true));
		}
		stateST.setAttribute("stateNumber", new Integer(s.stateNumber));
		StringTemplate eotST = null;
		for (int i = 0; i < s.getNumberOfTransitions(); i++) {
			Transition edge = (Transition) s.transition(i);
			StringTemplate edgeST;
			if ( edge.label.getAtom()==Label.EOT ) {
				// this is the default clause; has to held until last
				edgeST = templates.getInstanceOf("eotDFAEdge");
				stateST.removeAttribute("needErrorClause");
				eotST = edgeST;
			}
			else {
				edgeST = templates.getInstanceOf("cyclicDFAEdge");
				edgeST.setAttribute("labelExpr",
						parent.genLabelExpr(templates,edge.label,1));
			}
			edgeST.setAttribute("edgeNumber", new Integer(i+1));
			edgeST.setAttribute("targetStateNumber",
								 new Integer(edge.target.stateNumber));
			if ( edge.label.getAtom()!=Label.EOT ) {
				stateST.setAttribute("edges", edgeST);
			}
			// now check other states
			walkFixedDFAGeneratingStateMachine(templates,
											   dfaST,
											   (DFAState)edge.target);
		}
		if ( eotST!=null ) {
			stateST.setAttribute("edges", eotST);
		}
		dfaST.setAttribute("states", stateST);
	}

}

