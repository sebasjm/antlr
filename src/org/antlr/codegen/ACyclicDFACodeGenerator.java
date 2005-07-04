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
		// can be an if-then-else structure or a switch
		String dfaStateName = "dfaState";
		String dfaLoopbackStateName = "dfaLoopbackState";
		String dfaOptionalBlockStateName = "dfaOptionalBlockState";
		String dfaEdgeName = "dfaEdge";
		if ( parent.canGenerateSwitch(s) ) {
			dfaStateName = "dfaStateSwitch";
			dfaLoopbackStateName = "dfaLoopbackStateSwitch";
			dfaOptionalBlockStateName = "dfaOptionalBlockStateSwitch";
			dfaEdgeName = "dfaEdgeSwitch";
		}

		/*
		int oldMax = parent.decisionToMaxLookaheadDepth[dfa.getDecisionNumber()];
		if( k > oldMax ) {
			// track max (don't count the accept state)
			parent.decisionToMaxLookaheadDepth[dfa.getDecisionNumber()] = k;
		}
		*/
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
						parent.getTokenTypeAsTargetLabel(vI.intValue());
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
}

