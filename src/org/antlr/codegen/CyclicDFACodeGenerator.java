package org.antlr.codegen;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.analysis.*;
import org.antlr.misc.BitSet;
import org.antlr.misc.IntSet;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

public class CyclicDFACodeGenerator {
	protected CodeGenerator parent;

	/** Used by cyclic DFA state machine generator to avoid infinite recursion
	 *  resulting from cycles int the DFA.  This is a set of int state #s.
	 */
	protected IntSet visited;
	protected DFA dfa;

	public CyclicDFACodeGenerator(CodeGenerator parent) {
		this.parent = parent;
	}

	public StringTemplate genCyclicLookaheadDecision(StringTemplateGroup templates,
													 DFA dfa)
	{
		this.dfa = dfa;
		StringTemplate dfaST = templates.getInstanceOf("cyclicDFA");
		int d = dfa.getDecisionNumber();
		dfaST.setAttribute("decisionNumber", new Integer(d));
		dfaST.setAttribute("className", parent.getClassName());
		visited = new BitSet(dfa.getNumberOfStates());
		walkCyclicDFAGeneratingStateMachine(templates, dfaST, dfa.startState);
		parent.decisionToMaxLookaheadDepth[dfa.getDecisionNumber()]
			= Integer.MAX_VALUE;
		return dfaST;
	}

	protected void walkCyclicDFAGeneratingStateMachine(
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
			if ( parent.canGenerateSwitch(s) ) {
				stateST = templates.getInstanceOf("cyclicDFAStateSwitch");
			}
			else {
				stateST = templates.getInstanceOf("cyclicDFAState");
			}
			stateST.setAttribute("needErrorClause", new Boolean(true));
		}
		stateST.setAttribute("stateNumber", new Integer(s.stateNumber));
		if ( parent.canGenerateSwitch(s) ) {
			walkEdgesGeneratingComputedGoto(s, templates, stateST, dfaST);
		}
		else {
			walkEdgesGeneratingIfThenElse(s, templates, stateST, dfaST);
		}
		dfaST.setAttribute("states", stateST);
	}

	public static class LabelEdgeNumberPair implements Comparable {
		public int value;
		public int edgeNumber;
		public LabelEdgeNumberPair(int value, int edgeNumber) {
			this.value = value;
			this.edgeNumber = edgeNumber;
		}
		public int compareTo(Object o) {
			LabelEdgeNumberPair other = (LabelEdgeNumberPair)o;
			return this.value-other.value;
		}
		public boolean equals(Object o) {
			LabelEdgeNumberPair other = (LabelEdgeNumberPair)o;
			return this.value==other.value;
		}
	}

	protected void walkEdgesGeneratingComputedGoto(DFAState s,
												   StringTemplateGroup templates,
												   StringTemplate stateST,
												   StringTemplate dfaST)
	{
		for (int i = 0; i < s.getNumberOfTransitions(); i++) {
			Transition edge = (Transition) s.transition(i);
			int edgeNumber = i+1;
			Integer edgeNumberI = new Integer(edgeNumber);
			StringTemplate edgeST;
			if ( edge.label.getAtom()==Label.EOT ) {
				stateST.removeAttribute("needErrorClause");
				stateST.setAttribute("EOTTargetStateNumber",
									 new Integer(edge.target.stateNumber));
			}
			else {
				edgeST = templates.getInstanceOf("cyclicDFAEdgeSwitch");
				edgeST.setAttribute("edgeNumber", edgeNumberI);
				edgeST.setAttribute("targetStateNumber",
									new Integer(edge.target.stateNumber));
				List labels = edge.label.getSet().toList();
				for (int j = 0; j < labels.size(); j++) {
					Integer vI = (Integer) labels.get(j);
					String label =
						parent.grammar.getTokenTypeAsLabel(vI.intValue());
					labels.set(j, label); // rewrite List element to be name
				}
				edgeST.setAttribute("labels", labels);
				stateST.setAttribute("edges", edgeST);
			}
			/*
			List edgeLabels = edge.label.getSet().toList();
			for (int j = 0; j < edgeLabels.size(); j++) {
				Integer vI = (Integer) edgeLabels.get(j);
				if ( vI.intValue()!=Label.EOT ) {
	                labels.add(new LabelEdgeNumberPair(vI.intValue(), edgeNumber));
				}
			}
			*/
			// now gen code for other states
			walkCyclicDFAGeneratingStateMachine(templates,
											   dfaST,
											   (DFAState)edge.target);
		}
		// now sort the edge case values
		if ( !s.isAcceptState() ) {
			/*
			Collections.sort( labels );
			*/
		}
	}

	protected void walkEdgesGeneratingIfThenElse(DFAState s,
												 StringTemplateGroup templates,
												 StringTemplate stateST,
												 StringTemplate dfaST)
	{
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
				StringTemplate exprST =
					parent.genLabelExpr(templates,edge.label,1);
				// If this is a predicate edge and the code gen templates have
				// a template called singlePredicateMethod, it indicates
				// they want to build a method for predicates
				if ( edge.label.isSemanticPredicate() &&
					 templates.isDefinedInThisGroup("singlePredicateMethod") )
				{
					SemanticContext semCtx = edge.label.getSemanticContext();
					StringTemplate predMethodST =
						templates.getInstanceOf("singlePredicateMethod");
					predMethodST.setAttribute("decision", dfa.getDecisionNumber());
					predMethodST.setAttribute("stateNumber", s.stateNumber);
					predMethodST.setAttribute("edgeNumber", i+1);
					StringTemplate predEvalST = semCtx.genExpr(templates);
					predMethodST.setAttribute("pred", predEvalST);
					// we've computed a method to eval pred, now dump bytecodes
					// to invoke method
					exprST = templates.getInstanceOf("invokePredicate");
					exprST.setAttribute("pred", predEvalST);
				}
				edgeST.setAttribute("labelExpr", exprST);
			}
			edgeST.setAttribute("edgeNumber", new Integer(i+1));
			edgeST.setAttribute("targetStateNumber",
								 new Integer(edge.target.stateNumber));
			if ( edge.label.getAtom()!=Label.EOT ) {
				stateST.setAttribute("edges", edgeST);
			}
			// now check other states
			walkCyclicDFAGeneratingStateMachine(templates,
											   dfaST,
											   (DFAState)edge.target);
		}
		if ( eotST!=null ) {
			stateST.setAttribute("edges", eotST);
		}
	}

}

