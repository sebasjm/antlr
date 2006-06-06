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

import org.antlr.analysis.DFA;
import org.antlr.analysis.DFAState;
import org.antlr.analysis.Label;
import org.antlr.analysis.Transition;
import org.antlr.misc.IntSet;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

public class CyclicDFACodeGenerator {
	protected CodeGenerator parentGenerator;

	/** Used by cyclic DFA state machine generator to avoid infinite recursion
	 *  resulting from cycles int the DFA.  This is a set of int state #s.
	 */
	protected IntSet visited;
	protected DFA dfa;

	public CyclicDFACodeGenerator(CodeGenerator parent) {
		this.parentGenerator = parent;
	}

	/** A special state is huge (too big for state tables) or has a predicated
	 *  edge.  Generate a simple if-then-else.  Cannot be an accept state as
	 *  they have no emanating edges.  Don't worry about switch vs if-then-else
	 *  because if you get here, the state is super complicated and needs an
	 *  if-then-else.  This is used by the new DFA scheme created June 2006.
	 */
	public StringTemplate generateSpecialState(DFAState s) {
		StringTemplateGroup templates = parentGenerator.templates;
		StringTemplate stateST;
		stateST = templates.getInstanceOf("cyclicDFAState");
		stateST.setAttribute("needErrorClause", new Boolean(true));
		stateST.setAttribute("semPredState",
							 new Boolean(s.isResolvedWithPredicates()));
		stateST.setAttribute("stateNumber", s.stateNumber);
		stateST.setAttribute("decisionNumber", s.dfa.decisionNumber);

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
					parentGenerator.genLabelExpr(templates,edge,1);
				edgeST.setAttribute("labelExpr", exprST);
			}
			edgeST.setAttribute("edgeNumber", new Integer(i+1));
			edgeST.setAttribute("targetStateNumber",
								 new Integer(edge.target.stateNumber));
			// stick in any gated predicates for any edge if not already a pred
			if ( !edge.label.isSemanticPredicate() ) {
				DFAState target = (DFAState)edge.target;
				edgeST.setAttribute("predicates",
									target.getGatedPredicatesInNFAConfigurations());
			}
			if ( edge.label.getAtom()!=Label.EOT ) {
				stateST.setAttribute("edges", edgeST);
			}
		}
		if ( eotST!=null ) {
			stateST.setAttribute("edges", eotST);
		}
		return stateST;
	}

}

