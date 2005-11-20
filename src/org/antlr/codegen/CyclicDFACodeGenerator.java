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
package org.antlr.codegen;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.analysis.*;
import org.antlr.misc.BitSet;
import org.antlr.misc.IntSet;

import java.util.List;

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
						parent.getTokenTypeAsTargetLabel(vI.intValue());
					labels.set(j, label); // rewrite List element to be name
				}
				edgeST.setAttribute("labels", labels);
				stateST.setAttribute("edges", edgeST);
			}
			// now gen code for other states
			walkCyclicDFAGeneratingStateMachine(templates,
											   dfaST,
											   (DFAState)edge.target);
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

