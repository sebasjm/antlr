package org.antlr.analysis;

import org.antlr.tool.DOTGenerator;
import org.antlr.tool.ErrorManager;

import java.util.*;
import java.io.IOException;

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
	 *  Set<DFAState>
	 */
	protected Set stateToResolvedWithSemanticPredicatesMap = new HashSet();

	/** Track the predicates for each syntactically alt per DFA state;
	 *  more than one DFA state might have syntactically ambig alt prediction.
	 *  This is Map<DFAState, Map<int,SemanticContext>>; that is, it
	 *  maps DFA state
	 *  to another map, mapping alt number to a SemanticContext (pred(s) to
	 *  execute to resolve syntactic ambiguity).
	 */
	protected Map stateToAltSetWithSemanticPredicatesMap = new HashMap();

	protected Set danglingStates = new HashSet();

	/** Used to find paths through syntactically ambiguous DFA. */
	protected Map stateReachable;
	public static final Integer REACHABLE_BUSY = new Integer(-1);
	public static final Integer REACHABLE_NO = new Integer(0);
	public static final Integer REACHABLE_YES = new Integer(1);

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
		//System.out.println(d.getStateNumber()+".nondet alts="+nondeterministicAlts);
		stateToSyntacticallyAmbiguousAltsMap.put(d,nondeterministicAlts);
	}

	public void reportNondeterminismResolvedWithSemanticPredicate(DFAState d)
	{
		stateToResolvedWithSemanticPredicatesMap.add(d);
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

	/**   This situation " +
							   "occurs when you have gramars like 'a : A a X | A a Y;' "+
							   "that do not have finite-derivations or like "+

	 */
	public void reportErrors() {
		if ( danglingStates.size()>0 ) { // same as !isReduced()?
			//System.err.println("no emanating edges for states: "+danglingStates);
			for (Iterator it = danglingStates.iterator(); it.hasNext();) {
				DFAState d = (DFAState) it.next();
				stateReachable = new HashMap();
				List path = new LinkedList();
				boolean reaches = reachesState(dfa.getStartState(), d, path);
				if ( !reaches ) {
					System.err.println("whoa!  no path from start to "+d.getStateNumber());
				}
				//System.out.println("path="+path);
				Set nfaStates =
					getNFAStatesFromDFAStatesForAlt(path,0);
				System.err.println("The decision cannot distinguish between alternatives "+
								   "for at least one input sequence derived from these NFA states: "+
								   nfaStates);
				if ( getUnreachableAlts().size()>0 ) {
					System.err.println("As a result, some alternatives may be unreachable.");
				}
			}
		}
		if ( stateToSyntacticallyAmbiguousAltsMap.size()>0 ) {
			DOTGenerator dotGenerator = new DOTGenerator(dfa.getNFA().getGrammar());
			String dot = dotGenerator.getDOT( dfa.getStartState() );
			String dotFileName = "/tmp/dec-"+dfa.getDecisionNumber();
			try {
				dotGenerator.writeDOTFile(dotFileName, dot);
			}
			catch(IOException ioe) {
				ErrorManager.error(ErrorManager.MSG_CANNOT_GEN_DOT_FILE,
								   dotFileName,
								   ioe);
			}
			// GET DFA PATH
			Iterator it =
				stateToSyntacticallyAmbiguousAltsMap.keySet().iterator();
			while (	it.hasNext() ) {
				DFAState d = (DFAState) it.next();
				stateReachable = new HashMap();
				List path = new LinkedList();
				boolean reaches = reachesState(dfa.getStartState(), d, path);
				if ( !reaches ) {
					System.err.println("whoa!  no path from start to "+d.getStateNumber());
				}
				//System.out.println("path="+path);
				// NOW GET NFA STATES FROM DFA SET OF CONFIGS FOR NONDET ALTS
				Set nondetAlts = (Set)stateToSyntacticallyAmbiguousAltsMap.get(d);
				System.err.println("Decision can match the same input using multiple alternatives:");
				List sorted = new LinkedList();
				sorted.addAll(nondetAlts);
				Collections.sort(sorted); // make sure it's 1, 2, ...
				for (Iterator iter = sorted.iterator(); iter.hasNext();) {
					Integer altI = (Integer) iter.next();
					Set nfaStates =
						getNFAStatesFromDFAStatesForAlt(path,altI.intValue());
					System.err.println("  alt "+altI+" via NFA states "+nfaStates);
				}
				// syntactic nondet resolved?  If so, give unreachable warning
				Set disabled = d.getDisabledAlternatives();
				System.err.println("As a result, alternative(s) "+disabled+" were disabled for that input");
				/*
				List
				if ( getUnreachableAlts().size()>0 ) {
					Iterator iter = disabled.iterator();
					while (iter.hasNext()) {
						Integer I = (Integer) iter.next();
						if ( getUnreachableAlts().contains(I) ) {
							System.err.println("leaving " totally unreachable");
						}
					}
				}
				*/
			}
			/*
			System.err.println("nondeterministic alts for state(s) map: "+
							   stateToSyntacticallyAmbiguousAltsMap);
							   */
		}
		if ( getUnreachableAlts().size()>0 ) {
			System.err.println("The following alternatives are unreachable: "+
							   getUnreachableAlts());
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

	private void dumpPathToState(DFAState d) {
	}

	protected boolean reachesState(DFAState d, DFAState targetState, List statePath) {
		/*
		System.out.println("findPathToState processing DFA state "+
				d.getStateNumber()+" to target "+targetState.getStateNumber());
        */

		if ( d==targetState ) {
			statePath.add(0,targetState);
			//System.out.println("found target DFA state "+targetState.getStateNumber());
			stateReachable.put(d, REACHABLE_YES);
			return true;
		}

		// avoid infinite loops
		stateReachable.put(d, REACHABLE_BUSY);

		// look for a path to targetState among transitions for this state
		// stop when you find the first one; I'm pretty sure there is
		// at most one path to any DFA state with conflicting predictions
		for (int i=0; i<d.getNumberOfTransitions(); i++) {
			Transition t = d.transition(i);
			DFAState edgeTarget = (DFAState)t.getTarget();
			/*
			System.out.println(d.getStateNumber()+".target on "+
							   t.getLabel()+" is "+edgeTarget.getStateNumber());
			*/
			Integer targetStatus = (Integer)stateReachable.get(edgeTarget);
			if ( targetStatus==REACHABLE_BUSY ) { // avoid cycles; they say nothing
				continue;
			}
			if ( targetStatus==REACHABLE_YES ) { // return success!
				//System.out.println("state "+d.getStateNumber()+" reaches target (cached result)");
				stateReachable.put(d, REACHABLE_YES);
				return true;
			}
			if ( targetStatus==REACHABLE_NO ) { // try another transition
				continue;
			}
			// if null, target must be REACHABLE_UNKNOWN (i.e., unvisited)
			if ( reachesState(edgeTarget, targetState, statePath) ) {
				//System.out.println("state "+d.getStateNumber()+" reaches target");
				statePath.add(0,d);
				stateReachable.put(d, REACHABLE_YES);
				return true;
			}
		}

		stateReachable.put(d, REACHABLE_NO);
		return false; // no path to targetState found.
	}

    /** Given a list of DFA states, return a set of NFA states associated
	 *  with alt collected from all DFA states.  If alt==0 then collect
	 *  all NFA states regardless of alt.
	 */
	protected Set getNFAStatesFromDFAStatesForAlt(List path, int alt) {
		Set nfaStates = new LinkedHashSet();
		for (Iterator it = path.iterator(); it.hasNext();) {
			DFAState d = (DFAState) it.next();
			Set configs = d.getNFAConfigurations();
			for (Iterator configIter = configs.iterator(); configIter.hasNext();) {
				NFAConfiguration c = (NFAConfiguration) configIter.next();
				if ( alt==0 || c.alt==alt ) {
					nfaStates.add(new Integer(c.state));
				}
			}
		}
		return nfaStates;
	}
}
