package org.antlr.analysis;

import java.util.*;

/** Collection of information about what is wrong with a decision as
 *  discovered while building the DFA predictor.
 *
 *  The information is collected during NFA->DFA conversion and, while
 *  some of this is available elsewhere, it is nice to have it all tracked
 *  in one spot so a great error message can be easily had.  I also like
 *  the fact that this object tracks it all for later perusing to make an
 *  excellent error message instead of lots of imprecise on-the-fly warnings
 *  (during conversion).
 */
public class DecisionProbe {
	protected DFA dfa;

	/** Track for each DFA state the set of nondeterministic alternatives.
	 *  By reaching the same DFA state, a path through the NFA for some input
	 *  is able to reach the same NFA state by starting at more than one
	 *  alternative's left edge.  Though, later, we may find that predicates
	 *  resolve the issue, but track info anyway.
	 *  Map<DFAState, Set<int>>
	 */
	protected Map stateToSyntacticallyAmbiguousAltsMap = new HashMap();

	/** Was a syntactic ambiguity resolved with predicates?  Any DFA
	 *  state that predicts more than one alternative, must be resolved
	 *  with predicates or it should be reported to the user.
	 *  Map<DFAState, boolean>
	 */
	protected Map stateToResolvedWithSemanticPredicatesMap = new HashMap();

	/** Track the predicates for each syntactically alt per DFA state;
	 *  more than one DFA state might have syntactically ambig alt prediction.
	 *  This is Map<DFAState, Map<int,SemanticContext>>; that is, it
	 *  maps DFA state
	 *  to another map, mapping alt number to a SemanticContext (pred(s) to
	 *  execute to resolve syntactic ambiguity).
	 */
	protected Map stateToAltSetWithSemanticPredicatesMap = new HashMap();

	protected List danglingStates = new ArrayList();

	/** Map<DFAState,Set<in>> Tracks alts insufficiently covered.
	 *  For example, p1||true gets reduced to true so leaves whole alt uncovered
	 *  TODO handles both incompletely covered and just plain missing it seems
	 */
	//protected Map stateToSetOfIncompletelyCoveredAlts = new HashMap();

	public DecisionProbe(DFA dfa) {
		this.dfa = dfa;
	}

	public boolean isReduced() {
		return dfa.isReduced();
	}

	public boolean isCyclic() {
		return dfa.isCyclic();
	}

	public List getUnreachableAlts() {
		return dfa.getUnreachableAlts();
	}

    /** Report the fact that DFA state d is not a state resolved with
     *  predicates and yet it has no emanating edges.  Usually this
     *  is a result of the closure/reach operations being unable to proceed
     */
	public void reportDanglingState(DFAState d) {
		danglingStates.add(d);
	}

	public void reportNondeterminism(DFAState d, Set nondeterministicAlts) {
		stateToSyntacticallyAmbiguousAltsMap.put(d,nondeterministicAlts);
	}

	public void reportNondeterminismResolvedWithSemanticPredicate(DFAState d,
																  boolean resolved)
	{
		stateToResolvedWithSemanticPredicatesMap.put(d,new Boolean(resolved));
	}

	/** Report the list of predicates found for each alternative; copy
	 *  the list because this set gets altered later by the method
	 *  tryToResolveWithSemanticPredicates() while flagging NFA configurations
	 *  in d as resolved.
	 */
	public void reportAltPredicateContext(DFAState d, Map altPredicateContext) {
		Map copy = new HashMap();
		copy.putAll(altPredicateContext);
		stateToAltSetWithSemanticPredicatesMap.put(d,copy);
	}

	/** Report the predicates available for each alt.  This is the list
	 *  of predicates after the incomplete ones have been removed.  I.e.,
	 *  an alt might lead to two paths for token A, one with a pred and one
	 *  w/o.  In that case, the whole path must be considered uncovered.
	 */
	public void reportIncompletelyCoveredAlts(DFAState d,
											  int alt,
											  SemanticContext pred)
	{
		System.err.println("predicate for alt "+alt+" insufficient: "+pred);
		//stateToSetOfIncompletelyCoveredAlts.put(d,incompletelyCoveredAlts);
	}

	public void reportDisabledAlternatives(DFAState d,
										   Set nondeterministicAlts)
	{
		// don't track for now; might be too much detail
	}

	public boolean healthy() {
		return true;
	}

	public void reportErrors() {
		if ( !isReduced() ) {
			System.err.println("nonreduced DFA for "+
					dfa.decisionNFAStartState.getDescription());
		}
		if ( getUnreachableAlts().size()>0 ) {
			System.err.println("alts w/o predict state="+
							   getUnreachableAlts());
		}
		if ( danglingStates.size()>0 ) {
			System.err.println("no emanating edges for states: "+
							   danglingStates);
		}
		if ( stateToSyntacticallyAmbiguousAltsMap.size()>0 ) {
			System.err.println("nondeterministic alts for state(s) map: "+
							   stateToSyntacticallyAmbiguousAltsMap);
		}
		if ( stateToAltSetWithSemanticPredicatesMap.size()>0 ) {
			System.err.println("state to alts-with-predicate: "+
							   stateToAltSetWithSemanticPredicatesMap);
		}
		/*
		if ( stateToSetOfIncompletelyCoveredAlts.size()>0 ) {
			System.err.println("state to alts-with-insufficient predicates: "+
							   stateToSetOfIncompletelyCoveredAlts);
		}
		*/
		if ( stateToResolvedWithSemanticPredicatesMap.size()>0 ) {
			System.err.println("state to nondet alts resolved with sem pred map: "+
							   stateToResolvedWithSemanticPredicatesMap);
			//System.err.println("nondeterminism NOT resolved with sem preds");
		}
	}
}
