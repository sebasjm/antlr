package org.antlr.codegen;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.analysis.*;
import org.antlr.tool.GrammarAST;
import org.antlr.tool.ANTLRParser;
import org.antlr.misc.Utils;

import java.util.List;

public class ACyclicDFACodeGenerator {
	protected CodeGenerator parent;

	public ACyclicDFACodeGenerator(CodeGenerator parent) {
		this.parent = parent;
	}

	public StringTemplate genFixedLookaheadDecision(StringTemplateGroup templates,
													DFA dfa)
	{
		return walkFixedDFAGeneratingStateMachine(templates, dfa, dfa.startState, 1);
	}

	protected StringTemplate walkFixedDFAGeneratingStateMachine(
			StringTemplateGroup templates,
			DFA dfa,
			DFAState s,
			int k)
	{
		if ( s.isAcceptState() ) {
			StringTemplate dfaST = templates.getInstanceOf("dfaAcceptState");
			dfaST.setAttribute("alt", new Integer(s.getUniquelyPredictedAlt()));
			return dfaST;
		}

		// the default templates for generating a state and its edges
		// usually this is an if-then-else structure
		String dfaStateName = "dfaState";
		String dfaLoopbackStateName = "dfaLoopbackState";
		String dfaOptionalBlockStateName = "dfaOptionalBlockState";
		String dfaEdgeName = "dfaEdge";
		if ( canGenerateSwitch(s) ) {
			dfaStateName = "dfaStateSwitch";
			dfaLoopbackStateName = "dfaLoopbackStateSwitch";
			dfaOptionalBlockStateName = "dfaOptionalBlockStateSwitch";
			dfaEdgeName = "dfaEdgeSwitch";
		}

		int oldMax = parent.decisionToMaxLookaheadDepth[dfa.getDecisionNumber()];
		if( k > oldMax ) {
			// track max (don't count the accept state)
			parent.decisionToMaxLookaheadDepth[dfa.getDecisionNumber()] = k;
		}
		GrammarAST decisionASTNode =
			dfa.getNFADecisionStartState().getDecisionASTNode();
		StringTemplate dfaST = templates.getInstanceOf(dfaStateName);
		if ( decisionASTNode.getType()==ANTLRParser.EOB ) {
			dfaST = templates.getInstanceOf(dfaLoopbackStateName);
		}
		else if ( decisionASTNode.getType()==ANTLRParser.OPTIONAL ) {
			dfaST = templates.getInstanceOf(dfaOptionalBlockStateName);
		}
		dfaST.setAttribute("k", new Integer(k));
		dfaST.setAttribute("stateNumber", new Integer(s.stateNumber));
		String description = dfa.getNFADecisionStartState().getDescription();
		//System.out.println("DFA: "+description+" associated with AST "+decisionASTNode);
		if ( description!=null ) {
			description = Utils.replace(description,"\"", "\\\"");
			dfaST.setAttribute("description", description);
		}
		int EOTPredicts = NFA.INVALID_ALT_NUMBER;
		for (int i = 0; i < s.getNumberOfTransitions(); i++) {
			Transition edge = (Transition) s.transition(i);
			if ( edge.label.getAtom()==Label.EOT ) {
				// don't generate a real edge for EOT; track what EOT predicts
				DFAState target = (DFAState)edge.target;
				EOTPredicts = target.getUniquelyPredictedAlt();
				continue;
			}
			StringTemplate edgeST = templates.getInstanceOf(dfaEdgeName);
			// If the template wants all the label values delineated, do that
			if ( edgeST.getFormalArgument("labels")!=null ) {
				List labels = edge.label.getSet().toList();
				for (int j = 0; j < labels.size(); j++) {
					Integer vI = (Integer) labels.get(j);
					String label =
						parent.grammar.getTokenTypeAsLabel(vI.intValue());
					labels.set(j, label); // rewrite List element to be name
				}
				edgeST.setAttribute("labels", labels);
			}
			else { // else create an expression to evaluate (the general case)
				edgeST.setAttribute("labelExpr",
								parent.genLabelExpr(templates,edge.label,k));
			}
			StringTemplate targetST =
				walkFixedDFAGeneratingStateMachine(templates,
												   dfa,
												   (DFAState)edge.target,
												   k+1);
			edgeST.setAttribute("targetState", targetST);
			dfaST.setAttribute("edges", edgeST);
		}
		if ( EOTPredicts!=NFA.INVALID_ALT_NUMBER ) {
			dfaST.setAttribute("eotPredictsAlt", new Integer(EOTPredicts));
		}
		return dfaST;
	}

	/** You can generate a switch rather than if-then-else for a state
	 *  if there are no semantic predicates and the number of edge label
	 *  values is small enough; e.g., don't generate a switch for a state
	 *  containing an edge label such as 20..52330 (the resulting byte codes
	 *  would overflow the method 65k limit probably).
	 */
	protected boolean canGenerateSwitch(DFAState s) {
		int size = 0;
		for (int i = 0; i < s.getNumberOfTransitions(); i++) {
			Transition edge = (Transition) s.transition(i);
			if ( edge.label.isSemanticPredicate() ) {
				return false;
			}
			size += edge.label.getSet().size();
		}
		if ( size>=parent.MAX_SWITCH_CASE_LABELS ) {
			return false;
		}
        return true;
	}
}

