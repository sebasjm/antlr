package org.antlr.analysis;

import org.antlr.tool.DOTGenerator;
import org.antlr.tool.ErrorManager;
import org.antlr.tool.Grammar;
import org.antlr.tool.ANTLRParser;

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
 *
 *  This class is not thread safe due to shared use of visited maps etc...
 *  Only one thread should really need to access one DecisionProbe anyway.
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

	/** Track just like stateToSyntacticallyAmbiguousAltsMap, but only
	 *  for nondeterminisms that arise in the Tokens rule such as keyword vs
	 *  ID rule.  The state maps to the list of Tokens rule alts that are
	 *  in conflict.
	 *  Map<DFAState, Set<int>>
	 */
	protected Map stateToSyntacticallyAmbiguousTokensRuleAltsMap = new HashMap();

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

	/** Used while finding edge labels in various recursive routines such
	 *  as getSampleInputSequenceUsingStateSet().  Tracks the input position
	 *  we were at the last time at this node.  If same input position, then
	 *  we'd have reached same state without consuming input...probably an
	 *  infinite loop.  Stop.  Map<Integer>.
	 */
	protected Map statesVisited;

	protected Set statesVisitedDuringSampleSequence;

	/** Map<DFAState,Set<in>> Tracks alts insufficiently covered.
	 *  For example, p1||true gets reduced to true so leaves whole alt uncovered
	 */
	protected Set setOfIncompletelyCoveredAlts = new HashSet();

	protected boolean verbose = true;

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

	public void reportLexerRuleNondeterminism(DFAState d, Set nondeterministicAlts) {
		stateToSyntacticallyAmbiguousTokensRuleAltsMap.put(d,nondeterministicAlts);
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
		//System.err.println("predicate for alt "+alt+" insufficient: "+pred);
		setOfIncompletelyCoveredAlts.add(new Integer(alt));
	}

	/** If no states are dead-ends, no alts are unreachable, there are
	 *  no nondeterminisms unresolved by syn preds, all is ok with decision.
	 */
	public boolean isDeterministic() {
		return
			danglingStates.size()==0 &&
			stateToSyntacticallyAmbiguousAltsMap.size()==0 &&
			dfa.getUnreachableAlts().size()==0;
	}

	public void reportErrors() {
		System.out.println("--------------------\nnondeterministic decision (d="
				+dfa.getDecisionNumber()+") for "+
				dfa.getNFADecisionStartState().getDescription());
		/*
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
        */

		List unreachableAlts = dfa.getUnreachableAlts();
		/*
		Set unreachableAlts = new HashSet();
		unreachableAlts.addAll(dfa.getUnreachableAlts());
		*/

		if ( danglingStates.size()>0 ) { // same as !isReduced()?
			//System.err.println("no emanating edges for states: "+danglingStates);
			for (Iterator it = danglingStates.iterator(); it.hasNext();) {
				DFAState d = (DFAState) it.next();
				stateReachable = new HashMap();
				Set dfaStates = new HashSet();
				boolean reaches = reachesState(dfa.getStartState(), d, dfaStates);
				if ( !reaches ) {
					System.err.println("whoa!  no path from start to "+d.getStateNumber());
				}
				System.err.println("the decision cannot distinguish between alternatives "+
								   d.getAltSet()+" for at least one input sequence");
				/*
				//System.out.println("path="+path);
				Set nfaStates =
					getNFAStatesFromDFAStatesForAlt(dfaStates,0);
				System.err.println("The decision cannot distinguish between alternatives "+
								   d.getAltSet()+" for at least one input sequence derived from these NFA states: "+
								   nfaStates);
								   */
			}
		}
		if ( stateToSyntacticallyAmbiguousAltsMap.size()>0 )
		{
			// GET DFA PATH
			Iterator it =
				stateToSyntacticallyAmbiguousAltsMap.keySet().iterator();
			while (	it.hasNext() ) {
				DFAState d = (DFAState) it.next();
				stateReachable = new HashMap();
				Set dfaStates = new HashSet();
				boolean reaches = reachesState(dfa.getStartState(), d, dfaStates);
				if ( !reaches ) {
					System.err.println("whoa!  no path from start to "+d.getStateNumber());
				}
				//System.out.println("dfa states="+dfaStates);
				// NOW GET NFA STATES FROM DFA SET OF CONFIGS FOR NONDET ALTS
				Set nondetAlts = (Set)stateToSyntacticallyAmbiguousAltsMap.get(d);
				List sorted = new LinkedList();
				sorted.addAll(nondetAlts);
				Collections.sort(sorted); // make sure it's 1, 2, ...
				if ( !verbose ) {
					System.err.println("decision predicts multiple alternatives: "+
									   sorted+" for the same lookahead");
					break;
				}
				statesVisitedDuringSampleSequence = new HashSet();
				List labels = new ArrayList(); // may access ith element; use array
				getSampleInputSequenceUsingStateSet(dfa.getStartState(),
													d,
													dfaStates,
													labels);
				String input = getInputSequenceDisplay(labels);
				System.err.println("Decision can match input such as \""+input+"\" using multiple alternatives:");
				for (Iterator iter = sorted.iterator(); iter.hasNext();) {
					Integer altI = (Integer) iter.next();
					int alt = altI.intValue();
					// now get path take for an input sequence limited to
					// those states associated with this nondeterminism
					List path = new LinkedList();
					NFAState nfaStart = dfa.getNFADecisionStartState();
					Grammar g = dfa.getNFA().getGrammar();
					if ( nfaStart.getDecisionASTNode().getType()==ANTLRParser.EOB ) {
						if ( alt==g.getNumberOfAltsForDecisionNFA(nfaStart) ) {
							// TODO ugh: fix this weirdness!
							// special case; loop end decisions have exit as
							// # block alts + 1; getNumberOfAltsForDecisionNFA() has
							// both block alts and exit branch.  So, any predicted alt
							// equal to number of alts is the exit alt.  The NFA
							// sees that as alt 1
							alt = 1;
						}
						else {
							// exit branch is really first transition, so skip
							alt = alt+1;
						}
					}
					// convert all possible NFA states list for this alt into
					// an exact path for input 'labels'; more useful.
					NFAState altStart = g.getNFAStateForAltOfDecision(nfaStart,alt);
					altStart = (NFAState)altStart.transition(0).getTarget();
					statesVisited = new HashMap();
					Set nfaStates =
						getNFAStatesFromDFAStatesForAlt(dfaStates,altI.intValue());
					/*
					List sortedNFAStates = new LinkedList();
					sortedNFAStates.addAll(nfaStates);
					Collections.sort(sortedNFAStates); // make sure it's 1, 2, ...
                    */
					path.add(altStart);
					getNFAPath(altStart,
							   0,
							   nfaStates,
							   labels,
							   path);
					System.err.println("  alt "+altI+" via NFA path "+path);
				}
				if ( verbose ) {
					Set disabled = d.getDisabledAlternatives();
					System.err.println("As a result, alternative(s) "+disabled+" were disabled for that input");
				}
				// syntactic nondet resolved?  If so, give unreachable warning
				/*
				List totallyDisabledDueToLookahead = new LinkedList();
				Set disabled = d.getDisabledAlternatives();
				System.err.println("As a result, alternative(s) "+disabled+" were disabled for that input");
				if ( unreachableAlts.size()>0 ) {
					Iterator iter = disabled.iterator();
					while (iter.hasNext()) {
						Integer I = (Integer) iter.next();
						if ( unreachableAlts.contains(I) ) {
							totallyDisabledDueToLookahead.add(I);
							unreachableAlts.remove(I); // don't report this one later
						}
					}
				}
				if ( totallyDisabledDueToLookahead.size()>0 ) {
					System.err.println("leaving alternative(s) "+totallyDisabledDueToLookahead+" totally unreachable");
				}
				*/
			}
		}
		if ( unreachableAlts.size()>0 ) {
			System.err.println("The following alternatives are unreachable: "+
							   unreachableAlts);
		}
		if ( stateToAltSetWithSemanticPredicatesMap.size()>0 ) {
			/*
			System.err.println("state to alts-with-predicate: "+
							   stateToAltSetWithSemanticPredicatesMap);
							   */
		}
		if ( setOfIncompletelyCoveredAlts.size()>0 ) {
			System.err.println("alts with insufficient predicates: "+
							   setOfIncompletelyCoveredAlts);
		}
		if ( stateToResolvedWithSemanticPredicatesMap.size()>0 ) {
			/*
			System.err.println("state to nondet alts resolved with sem pred map: "+
							   stateToResolvedWithSemanticPredicatesMap);
			//System.err.println("nondeterminism NOT resolved with sem preds");
			*/
		}
	}

	/** Given a start state and a target state, return the set of DFA states
	 *  that reach from start to target.
	 */
	protected boolean reachesState(DFAState startState,
								   DFAState targetState,
								   Set states) {
		if ( startState==targetState ) {
			states.add(targetState);
			//System.out.println("found target DFA state "+targetState.getStateNumber());
			stateReachable.put(startState, REACHABLE_YES);
			return true;
		}

		// avoid infinite loops
		stateReachable.put(startState, REACHABLE_BUSY);

		// look for a path to targetState among transitions for this state
		// stop when you find the first one; I'm pretty sure there is
		// at most one path to any DFA state with conflicting predictions
		for (int i=0; i<startState.getNumberOfTransitions(); i++) {
			Transition t = startState.transition(i);
			DFAState edgeTarget = (DFAState)t.getTarget();
			Integer targetStatus = (Integer)stateReachable.get(edgeTarget);
			if ( targetStatus==REACHABLE_BUSY ) { // avoid cycles; they say nothing
				continue;
			}
			if ( targetStatus==REACHABLE_YES ) { // return success!
				stateReachable.put(startState, REACHABLE_YES);
				return true;
			}
			if ( targetStatus==REACHABLE_NO ) { // try another transition
				continue;
			}
			// if null, target must be REACHABLE_UNKNOWN (i.e., unvisited)
			if ( reachesState(edgeTarget, targetState, states) ) {
				states.add(startState);
				stateReachable.put(startState, REACHABLE_YES);
				return true;
			}
		}

		stateReachable.put(startState, REACHABLE_NO);
		return false; // no path to targetState found.
	}

    /** Given a set of DFA states, return a set of NFA states associated
	 *  with alt collected from all DFA states.  If alt==0 then collect
	 *  all NFA states regardless of alt.
	 */
	protected Set getNFAStatesFromDFAStatesForAlt(Set dfaStates, int alt) {
		Set nfaStates = new LinkedHashSet();
		for (Iterator it = dfaStates.iterator(); it.hasNext();) {
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

	/** Given a start state and a final state, find a list of edge labels
	 *  between the two ignoring epsilon.  Limit your scan to a set of states
	 *  passed in.  This is used to show a sample input sequence that is
	 *  nondeterministic with respect to this decision.  Return List<Label> as
	 *  a parameter.  The incoming states set must be all states that lead
	 *  from startState to targetState and no others so this algorithm doesn't
	 *  take a path that eventually leads to a state other than targetState.
	 *  Don't follow loops, leading to short (possibly shortest) path.
	 */
	public void getSampleInputSequenceUsingStateSet(State startState,
													State targetState,
													Set states,
													List labels)
	{
		statesVisitedDuringSampleSequence.add(startState);

		// pick the first edge in states as the one to traverse
		for (int i=0; i<startState.getNumberOfTransitions(); i++) {
			Transition t = startState.transition(i);
			DFAState edgeTarget = (DFAState)t.getTarget();
			if ( states.contains(edgeTarget) &&
				 !statesVisitedDuringSampleSequence.contains(edgeTarget) )
			{
				labels.add(t.getLabel()); // traverse edge and track label
				if ( edgeTarget!=targetState ) {
					// get more labels if not at target
					getSampleInputSequenceUsingStateSet(edgeTarget,
														targetState,
														states,
														labels);
				}
				// done with this DFA state as we've found a good path to target
				return;
			}
		}
		// hmm...no path available?
		ErrorManager.error(ErrorManager.MSG_CANNOT_COMPUTE_SAMPLE_INPUT_SEQ);
	}

	/** Given a sample input sequence, you usually would like to know the
	 *  path taken through the NFA.  Return the list of NFA states visited
	 *  while matching a list of labels.  This cannot use the usual
	 *  interpreter, which does a deterministic walk.  We need to be able to
	 *  take paths that are turned off during nondeterminism resolution. So,
	 *  just do a depth-first walk restricting yourself to a set of states and
	 *  only traversing edges labeled with the current label.  Return true
	 *  if a path was found emanating from state s.
	 */
	public boolean getNFAPath(NFAState s,     // starting where?
							  int labelIndex, // 0..labels.size()-1
							  Set states,     // legal NFA states; Set<Integer>
							  List labels,    // input sequence
							  List path)      // output list of NFA states
	{
		//System.out.println("getNFAPath start "+s.getStateNumber());
		statesVisited.put(s, new Integer(labelIndex));

		// pick the first edge in states and with label as the one to traverse
		for (int i=0; i<s.getNumberOfTransitions(); i++) {
			Transition t = s.transition(i);
			NFAState edgeTarget = (NFAState)t.getTarget();
			Integer targetStateNumI = new Integer(edgeTarget.getStateNumber());
			Integer previousLabelIndexAtThisState =
				(Integer)statesVisited.get(edgeTarget);
			if ( states.contains(targetStateNumI) &&
				 (previousLabelIndexAtThisState==null||
				  !previousLabelIndexAtThisState.equals(targetStateNumI)) )
			{
				Label label = (Label)labels.get(labelIndex);
				/*
				System.out.println(s.getStateNumber()+"-"+
								   label.toString(dfa.getNFA().getGrammar())+"->"+
								   edgeTarget.getStateNumber());
				*/
				if ( t.getLabel().isEpsilon() ) {
					// nondeterministically backtrack down epsilon edges
					path.add(edgeTarget);
					boolean found =
						getNFAPath(edgeTarget, labelIndex, states, labels, path);
					if ( found ) {
						return true; // return to "calling" state
					}
					path.remove(path.size()-1); // remove; didn't work out
					continue; // look at the next edge
				}
				if ( t.getLabel().matches(label) ) {
					path.add(edgeTarget);
					/*
					System.out.println("found label "+
									   t.getLabel().toString(dfa.getNFA().getGrammar())+
									   " at state "+s.getStateNumber());
					*/
					if ( labelIndex==labels.size()-1 ) {
						// found last label; done!
						return true;
					}
					// otherwise try to match remaining input
					boolean found =
						getNFAPath(edgeTarget, labelIndex+1, states, labels, path);
					if ( found ) {
						return true;
					}
					path.remove(path.size()-1); // remove; didn't work out
					continue; // keep looking for a path for labels
				}
			}
		}
		// no edge was found matching label; is ok, some state will have it
		return false;
	}

	/** Return k if decision is LL(k) for some k else return max int */
	public int getFixedLookaheadDepth() {
		if ( isCyclic() ) {
			return Integer.MAX_VALUE;
		}
		// find max k value as their might be multiple depths for different alts
		// TODO: add functionality
		return Integer.MAX_VALUE;
	}

	// TODO: probably need a method for optimization later than returns fixed lookahead

	/** Given List<Label>, return a String with a useful representation
	 *  of the associated input string.  One could show something different
	 *  for lexers and parsers, for example.
	 */
	public String getInputSequenceDisplay(List labels) {
        Grammar g = dfa.getNFA().getGrammar();
		StringBuffer buf = new StringBuffer();
		for (Iterator it = labels.iterator(); it.hasNext();) {
			Label label = (Label) it.next();
			buf.append(label.toString(g));
			if ( it.hasNext() && g.getType()!=Grammar.LEXER ) {
				buf.append(' ');
			}
		}
		return buf.toString();
	}

	/** TODO: */
	public int getNumberOfStates() {
		return dfa.getNumberOfStates();
	}
}
