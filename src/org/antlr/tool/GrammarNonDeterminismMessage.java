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
		for (Iterator iter = nondetAlts.iterator(); iter.hasNext();) {
			Integer altI = (Integer) iter.next();
			NFAState nfaStart = probe.dfa.getNFADecisionStartState();
			int displayAlt = altI.intValue();
			if ( nfaStart.getDecisionASTNode().getType()==ANTLRParser.EOB ) {
				if ( displayAlt==probe.dfa.nfa.grammar.getNumberOfAltsForDecisionNFA(nfaStart) ) {
					// special case; loop end decisions have exit as
					// # block alts + 1; getNumberOfAltsForDecisionNFA() has
					// both block alts and exit branch.  So, any predicted displayAlt
					// equal to number of alts is the exit displayAlt.  The NFA
					// sees that as displayAlt 1.  Yes, this is gross, but
					// I have searched for months for a better solution
					// without success. :(
					displayAlt = 1;
				}
				else {
					// exit branch is really first transition, so skip
					displayAlt = displayAlt+1;
				}
			}
			if ( DecisionProbe.verbose ) {
				List path =
					probe.getNFAPathStatesForAlt(problemState,altI.intValue(),labels);
				st.setAttribute("paths.{alt,states}", new Integer(displayAlt), path);
			}
			else {
				st.setAttribute("conflictingAlts", displayAlt);
			}
		}
		return st.toString();
	}

	public void computeErrors() {
		System.out.println("--------------------\nnondeterministic decision (d="
				+probe.dfa.getDecisionNumber()+") for "+
				probe.dfa.getNFADecisionStartState().getDescription());

		Set resolvedStates = probe.getNondeterministicStatesResolvedWithSemanticPredicate();
		Set problemStates = probe.getDFAStatesWithSyntacticallyAmbiguousAlts();
		if ( problemStates.size()>0 ) {
			Iterator it =
				problemStates.iterator();
			while (	it.hasNext() ) {
				DFAState d = (DFAState) it.next();
				if ( resolvedStates!=null && resolvedStates.contains(d) ) {
					// don't report problem if resolved
					continue;
				}
				List nondetAlts = probe.getNonDeterministicAltsForState(d);
				if ( !DecisionProbe.verbose ) {
					System.err.println("decision predicts multiple alternatives: "+
									   nondetAlts+" for the same lookahead");
					break;
				}
				List labels = probe.getSampleNonDeterministicInputSequence(d);
				String input = probe.getInputSequenceDisplay(labels);
				System.err.println("Decision can match input such as \""+input+"\" using multiple alternatives:");
				// For each nondet alt, compute path of NFA states
				for (Iterator iter = nondetAlts.iterator(); iter.hasNext();) {
					Integer altI = (Integer) iter.next();
					// now get path take for an input sequence limited to
					// those states associated with this nondeterminism
					NFAState nfaStart = probe.dfa.getNFADecisionStartState();
					Grammar g = probe.dfa.nfa.grammar;
					// convert all possible NFA states list for this displayAlt into
					// an exact path for input 'labels'; more useful.
					List path = probe.getNFAPathStatesForAlt(d,altI.intValue(),labels);
					// compute the proper displayable alt number (ick)
					int displayAlt = altI.intValue();
					if ( nfaStart.getDecisionASTNode().getType()==ANTLRParser.EOB ) {
						if ( displayAlt==g.getNumberOfAltsForDecisionNFA(nfaStart) ) {
							// special case; loop end decisions have exit as
							// # block alts + 1; getNumberOfAltsForDecisionNFA() has
							// both block alts and exit branch.  So, any predicted displayAlt
							// equal to number of alts is the exit displayAlt.  The NFA
							// sees that as displayAlt 1.  Yes, this is gross, but
							// I have searched for months for a better solution
							// without success. :(
							displayAlt = 1;
						}
						else {
							// exit branch is really first transition, so skip
							displayAlt = displayAlt+1;
						}
					}
					System.err.println("  alt "+altI+" via NFA path "+path);
					SemanticContext altSemCtx =
						probe.getSemanticContextForAlt(d,altI.intValue());
					if ( altSemCtx!=null ) {
						System.err.println("pred for "+displayAlt+": "+altSemCtx);
					}
				}
				if ( DecisionProbe.verbose ) {
					Set disabled = d.getDisabledAlternatives();
					System.err.println("As a result, alternative(s) "+disabled+" were disabled for that input");
				}
				List incompleteAlts = probe.getIncompletelyCoveredAlts(d);
				if ( incompleteAlts!=null && incompleteAlts.size()>0 ) {
					System.err.println("alts with insufficient predicates: "+
									   incompleteAlts);
				}
			}
		}
		List unreachableAlts = probe.dfa.getUnreachableAlts();
		if ( unreachableAlts!=null && unreachableAlts.size()>0 ) {
			System.err.println("The following alternatives are unreachable: "+
							   unreachableAlts);
		}
		if ( probe.getNondeterministicStatesResolvedWithSemanticPredicate()!=null &&
			 probe.getNondeterministicStatesResolvedWithSemanticPredicate().size()>0 )
		{
			System.err.println("states resolved with sem pred map: "+
							   probe.getNondeterministicStatesResolvedWithSemanticPredicate());
			//System.err.println("nondeterminism NOT resolved with sem preds");
		}
	}

}
