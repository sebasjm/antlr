package org.antlr.analysis;

import org.antlr.misc.IntSet;
import org.antlr.misc.OrderedHashMap;
import org.antlr.tool.ANTLRParser;

import java.util.*;

/** Code that embodies the NFA conversion to DFA. */
public class NFAToDFAConverter {
	/** A list of DFA states we still need to process during NFA conversion */
	protected List work = new LinkedList();

	/** Have we found a condition that renders DFA useless?  If so, terminate */
	protected boolean terminate = false;

	/** While converting NFA, we must track states that
	 *  reference other rule's NFAs so we know what to do
	 *  at the end of a rule.  We need to know what context invoked
	 *  this rule so we can know where to continue looking for NFA
	 *  states.  I'm tracking a context tree (record of rule invocation
	 *  stack trace) for each alternative that could be predicted.
	 */
	protected NFAContext[] contextTrees;

	/** We are converting which DFA? */
	protected DFA dfa;

	public NFAToDFAConverter(DFA dfa) {
		this.dfa = dfa;
		NFAState nfaStartState = dfa.getNFADecisionStartState();
		int nAlts =
			dfa.getNFA().getGrammar().getNumberOfAltsForDecisionNFA(nfaStartState);
		initContextTrees(nAlts);
	}

	public void convert(NFAState blockStart) {
		// create the DFA start state
		DFAState startState = getStartState();
		dfa.setStartState(startState);

		// while more DFA states to check, process them
		while ( !terminate && work.size()>0 ) {
			DFAState d = (DFAState) work.get(0);
			/*
			System.out.println("convert DFA state "+d.getStateNumber()+
					" ("+d.getNFAConfigurations().size()+" nfa states)");
			*/
			findNewDFAStatesAndAddDFATransitions(d);
			work.remove(0); // done with it; remove from work list
		}
	}

	/** From this first alt NFA state of a decision, create a DFA.
	 *  Walk each alt in decision and compute closure from the start of that
	 *  rule, making sure that the closure does not include other alts within
	 *  that same decision.  The idea is to associate a specific alt number
	 *  with the starting closure so we can trace the alt number for all states
	 *  derived from this.  At a stop state in the DFA, we can return this alt
	 *  number, indicating which alt is predicted.
	 *
	 *  If this DFA is derived from an loop back NFA state, then the first
	 *  transition is actually the exit branch of the loop.  Rather than make
	 *  this alternative one, let's make this alt n+1 where n is the number of
	 *  alts in this block.  This is nice to keep the alts of the block 1..n;
	 *  helps with error messages.
	 *
	 *  I handle nongreedy in findNewDFAStatesAndAddDFATransitions
	 *  when nongreedy and EOT transition.  Make state with EOT emanating
	 *  from it the accept state.
	 */
	protected DFAState getStartState() {
		NFAState alt = dfa.getDecisionNFAStartState();
		DFAState startState = dfa.newState();
		int i = 0;
		int altNum = 1;
		while ( alt!=null ) {
			// find the set of NFA states reachable without consuming
			// any input symbols for each alt.  Keep adding to same
			// overall closure that will represent the DFA start state,
			// but track the alt number
			NFAContext initialContext = contextTrees[i];
			// if first alt is derived from exit branch of loop,
			// make alt=n+1 for n alts not 1
			if ( i==0 && dfa.getDecisionASTNode().getType()==ANTLRParser.EOB ) {
				int numAltsIncludingExitBranch = dfa.getNFA().getGrammar()
						.getNumberOfAltsForDecisionNFA(dfa.getDecisionNFAStartState());
				altNum = numAltsIncludingExitBranch;
				closure((NFAState)alt.transition(0).getTarget(),
						altNum,
						initialContext,
						SemanticContext.EMPTY_SEMANTIC_CONTEXT,
						startState,
						true);
				altNum = 1; // make next alt the first
			}
			else {
				closure((NFAState)alt.transition(0).getTarget(),
						altNum,
						initialContext,
						SemanticContext.EMPTY_SEMANTIC_CONTEXT,
						startState,
						true);
				altNum++;
			}
			i++;

			// move to next alternative
			if ( alt.transition(1)==null ) {
				break;
			}
			alt = (NFAState)alt.transition(1).getTarget();
		}

		// now DFA start state has the complete closure for the decision
		// but we have tracked which alt is associated with which
		// NFA states.
		work.add(startState);
		return startState;
	}

	/** From this node, add a d--a-->t transition for all
	 *  labels 'a' where t is a DFA node created
	 *  from the set of NFA states reachable from any NFA
	 *  state in DFA state d.
	 */
	protected void findNewDFAStatesAndAddDFATransitions(DFAState d) {
		OrderedHashMap labels = d.getReachableLabels();
		/*
		System.out.println("reachable="+labels.toString());
		System.out.println("|reachable|/|nfaconfigs|="+
				labels.size()+"/"+d.getNFAConfigurations().size()+"="+
				labels.size()/(float)d.getNFAConfigurations().size());
		*/

		// normally EOT is the "default" clause and decisions just
		// choose that last clause when nothing else matches.  DFA conversion
		// continues searching for a unique sequence that predicts the
		// various alts or until it finds EOT.  So this rule
		//
		// DUH : ('x'|'y')* "xy!";
		//
		// does not need a greedy indicator.  The following rule works fine too
		//
		// A : ('x')+ ;
		//
		// When the follow branch could match what is in the loop, by default,
		// the nondeterminism is resolved in favor of the loop.  You don't
		// get a warning because the only way to get this condition is if
		// the DFA conversion hits the end of the token.  In that case,
		// we're not *sure* what will happen next, but it could be anything.
		// Anyway, EOT is the default case which means it will never be matched
		// as resolution goes to the lowest alt number.  Exit branches are
		// always alt n+1 for n alts in a block.
		//
		// When a loop is nongreedy and we find an EOT transition, the DFA
		// state should become an accept state, predicting exit of loop.  It's
		// just reversing the resolution of ambiguity.
		// TODO: should this be done in the resolveAmbig method?
		if ( !dfa.isGreedy() && labels.containsKey(new Label(Label.EOT)) ) {
			convertToEOTAcceptState(d);
			return; // no more work to do on this accept state
		}

		// for each label that could possibly emanate from NFAStates of d
		int numberOfEdgesEmanating = 0;
		for (int i=0; i<labels.size(); i++) {
			Label label = (Label)labels.get(i);
			DFAState t = reach(d, label);
			/*
			System.out.println("DFA state after reach "+d+"-" +
					label.toString(nfa.getGrammar())+"->"+t);
			*/
			if ( t.getNFAConfigurations().size()==0 ) {
				// nothing was reached by label due to conflict resolution
				continue;
			}
			closure(t);  // add any NFA states reachable via epsilon
			/*
			System.out.println("DFA state after closure "+d+"-"+
					label.toString(nfa.getGrammar())+
					"->"+t);
			*/
			DFAState targetState = addDFAState(t); // add if not in DFA yet
			numberOfEdgesEmanating++;
			// make a transition from d to t upon 'a'
			d.addTransition(targetState, label);
		}

		if ( !d.isResolvedWithPredicates() && numberOfEdgesEmanating==0 ) {
			System.err.println("no emanating edges for state: "+d);
			terminate = true; // might as well stop now
		}

		// Check to see if we need to add any semantic predicate transitions
		if ( d.isResolvedWithPredicates() ) {
			addPredicateTransitions(d);
		}
	}

	/** For all NFA states (configurations) merged in d,
	 *  compute the epsilon closure; that is, find all NFA states reachable
	 *  from the NFA states in d via purely epsilon transitions.
	 */
	public void closure(DFAState d) {
		//System.out.println("closure("+d+")");
		Set configs = new HashSet();
		// Because we are adding to the configurations in closure
		// must clone initial list so we know when to stop doing closure
		configs.addAll(d.getNFAConfigurations());
		// for each NFA configuration in d
		Iterator iter = configs.iterator();
		while (iter.hasNext()) {
			NFAConfiguration c = (NFAConfiguration)iter.next();
			if ( c.singleAtomTransitionEmanating ) {
				continue; // ignore NFA states w/o epsilon transitions
			}
			// figure out reachable NFA states from each of d's nfa states
			// via epsilon transitions
			closure(dfa.getNFA().getState(c.state),
					c.alt,
					c.context,
					c.semanticContext,
					d,
					false);
		}
		d.closureBusy = null; // wack all that memory used during closure
	}

	/** Where can we get from NFA state p traversing only epsilon transitions?
	 *  Add new NFA states + context to DFA state d.  Also add semantic
	 *  predicates to semantic context if collectPredicates is set.  We only
	 *  collect predicates at hoisting depth 0, meaning before any token/char
	 *  have been recognized.  This corresponds, during analysis, to the
	 *  initial DFA start state construction closure() invocation.
	 *
	 *  There are four cases of interest (the last being the usual transition):
	 *
	 *   1. Traverse an edge that takes us to the start state of another
	 *      rule, r.  We must push this state so that if the DFA
	 *      conversion hits the end of rule r, then it knows to continue
	 *      the conversion at state following state that "invoked" r. By
	 *      construction, there is a single transition emanating from a rule
	 *      ref node.
	 *
	 *   2. Reach an NFA state associated with the end of a rule, r, in the
	 *      grammar from which it was built.  We must add an implicit (i.e.,
	 *      don't actually add an epsilon transition) epsilon transition
	 *      from r's end state to the NFA state following the NFA state
	 *      that transitioned to rule r's start state.  Because there are
	 *      many states that could reach r, the context for a rule invocation
	 *      is part of a call tree not a simple stack.  When we fall off end
	 *      of rule, "pop" a state off the call tree and add that state's
	 *      "following" node to d's NFA configuration list.  The context
	 *      for this new addition will be the new "stack top" in the call tree.
	 *
	 *   3. Like case 2, we reach an NFA state associated with the end of a
	 *      rule, r, in the grammar from which NFA was built.  In this case,
	 *      however, we realize that during this NFA->DFA conversion, no state
	 *      invoked the current rule's NFA.  There is no choice but to add
	 *      all NFA states that follow references to r's start state.  This is
	 *      analogous to computing the FOLLOW(r) in the LL(k) world.  By
	 *      construction, even rule stop state has a chain of nodes emanating
	 *      from it that points to every possible following node.  This case
	 *      is conveniently handled then by the 4th case.
	 *
	 *   4. Normal case.  If p can reach another NFA state q, then add
	 *      q to d's configuration list, copying p's context for q's context.
	 *      If there is a semantic predicate on the transition, then AND it
	 *      with any existing semantic context.
	 *
	 *   Current state p is always added to d's configuration list as it's part
	 *   of the closure as well.
	 */
	public void closure(NFAState p,
						int alt,
						NFAContext context,
						SemanticContext semanticContext,
						DFAState d,
						boolean collectPredicates)
	{
		//System.out.println("closure at NFA state "+p.getStateNumber()+"|"+alt+" filling DFA state "+d+" with context "+context);
		//System.out.println("closure at NFA state "+p.getStateNumber()+"|"+alt+"; context "+context);

		// Avoid infinite recursion
		// If we've seen this configuration before during closure, stop
		if ( d.closureIsBusy(p,alt,context,semanticContext) ) {
			/*
			System.out.println("avoid infinite closure computation emanating from "+
			p.getDescription()+":"+
			new NFAConfiguration(p.getStateNumber(),alt,context,semanticContext));
			System.out.println("state is "+d);
			*/
			return;
		}
		d.setClosureIsBusy(p,alt,context,semanticContext);

		// p itself is always in closure
		d.addNFAConfiguration(p, alt, context, semanticContext);

		// Case 1: are we a reference to another rule?
		Transition transition0 = p.transition(0);
		if ( transition0 instanceof RuleClosureTransition ) {
			RuleClosureTransition ref = (RuleClosureTransition)transition0;
			// first create a new context and push onto call tree,
			// recording the fact that we are invoking a rule and
			// from which state (case 2 below will get the following state
			// via the RuleClosureTransition emanating from the invoking state
			// pushed on the stack).
			// Reset the context to reflect the fact we invoked rule
			NFAContext newContext = new NFAContext(context, p);
			// System.out.print("invoking rule "+nfa.getGrammar().getRuleName(ref.getRuleIndex()));
			// System.out.println(" context="+context);
			// traverse epsilon edge to new rule
			NFAState ruleTarget = (NFAState)ref.getTarget();
			closure(ruleTarget, alt, newContext, semanticContext, d, collectPredicates);
		}
		// Case 2: end of rule state, context (i.e., an invoker) exists
		else if ( p.isAcceptState() && context.getParent()!=null ) {
			NFAState whichStateInvokedRule = context.getInvokingState();
			RuleClosureTransition edgeToRule =
				(RuleClosureTransition)whichStateInvokedRule.transition(0);
			NFAState continueState = edgeToRule.getFollowState();
			NFAContext newContext = context.getParent(); // "pop" invoking state
			closure(continueState, alt, newContext, semanticContext, d, collectPredicates);
		}
		// Case 3: end of rule state, nobody invoked this rule (no context)
		//    Fall thru to be handled by case 4 automagically.
		// Case 4: ordinary NFA->DFA conversion case: simple epsilon transition
		else {
			// recurse down any epsilon transitions
			if ( transition0!=null && transition0.isEpsilon() ) {
				closure((NFAState)transition0.getTarget(),
						alt,
						context,
						semanticContext,
						d,
						collectPredicates);
			}
			else if ( transition0!=null && transition0.isSemanticPredicate() ) {
				// continue closure here too, but add the sem pred to ctx
				SemanticContext newContext = semanticContext;
				if ( collectPredicates ) {
					// AND the previous semantic context with new pred
					SemanticContext labelContext =
						transition0.getLabel().getSemanticContext();
					newContext = SemanticContext.and(semanticContext,
													 labelContext);
				}
				closure((NFAState)transition0.getTarget(),
						alt,
						context,
						newContext,
						d,
						collectPredicates);
			}
			Transition transition1 = p.transition(1);
			if ( transition1!=null && transition1.isEpsilon() ) {
				closure((NFAState)transition1.getTarget(),
						alt,
						context,
						semanticContext,
						d,
						collectPredicates);
			}
		}

		// don't remove "busy" flag as we want to prevent all
		// references to same config of state|alt|ctx|semCtx even
		// if resulting from another NFA state
	}

	/** Given the set of NFA states in DFA state d, find all NFA states
	 *  reachable traversing label arcs.  By definition, there can be
	 *  only one DFA state reachable by an atom from DFA state d so we must
	 *  find and merge all NFA states reachable via label.  Return a new
	 *  DFAState that has all of those NFA states with their context (i.e.,
	 *  which alt do they predict and where to return to if they fall off
	 *  end of a rule).
	 *
	 *  Because we cannot jump to another rule nor fall off the end of a rule
	 *  via a non-epsilon transition, NFA states reachable from d have the
	 *  same configuration as the NFA state in d.  So if NFA state 7 in d's
	 *  configurations can reach NFA state 13 then 13 will be added to the
	 *  new DFAState (labelDFATarget) with the same configuration as state
	 *  7 had.
	 */
	public DFAState reach(DFAState d, Label label) {
		DFAState labelDFATarget = dfa.newState();
		// for each NFA state in d, add in target states for label
		int intLabel = label.getAtom();
		IntSet setLabel = label.getSet();
		Iterator iter = d.getNFAConfigurations().iterator();
		while ( iter.hasNext() ) {
			NFAConfiguration c = (NFAConfiguration)iter.next();
			if ( c.resolved || c.resolveWithPredicate ) {
				continue; // the conflict resolver indicates we must leave alone
			}
			NFAState p = dfa.getNFA().getState(c.state);
			// by design of the grammar->NFA conversion, only transition 0
			// may have a non-epsilon edge.
			Transition edge = p.transition(0);
			if ( edge==null || !c.singleAtomTransitionEmanating ) {
				continue;
			}
			// Labels not unique at this point (not until addReachableLabels)
			// so try simple int label match before general set intersection
			Label edgeLabel = edge.getLabel();
			//System.out.println("comparing "+edgeLabel+" with "+label);
			boolean matched =
				(!label.isSet()&&edgeLabel.getAtom()==intLabel)||
				(!edgeLabel.getSet().and(setLabel).isNil());
			if ( matched ) {
				// found a transition with label;
				// add NFA target to (potentially) new DFA state
				labelDFATarget.addNFAConfiguration(
					(NFAState)edge.getTarget(),
					c.alt,
					c.context,
					c.semanticContext);
			}
		}
		return labelDFATarget;
	}


	/** Walk the configurations of this DFA state d looking for the
	 *  configuration, c, that has a transition on EOT.  State d should
	 *  be converted to an accept state predicting the c.alt.  Blast
	 *  d's current configuration set and make it just have config c.
	 *
	 *  TODO: can there be more than one config with EOT transition?
	 *  That would mean that two NFA configurations could reach the
	 *  end of the token with possibly different predicted alts.
	 *  Seems like that would be rare or impossible.  Perhaps convert
	 *  this routine to find all such configs and give error if >1.
	 */
	protected void convertToEOTAcceptState(DFAState d) {
		Label eot = new Label(Label.EOT);
		Iterator iter = d.getNFAConfigurations().iterator();
		while ( iter.hasNext() ) {
			NFAConfiguration c =
					(NFAConfiguration)iter.next();
			if ( c.resolved || c.resolveWithPredicate ) {
				continue; // the conflict resolver indicates we must leave alone
			}
			NFAState p = dfa.nfa.getState(c.state);
			Transition edge = p.transition(0);
			Label edgeLabel = edge.getLabel();
			if ( edgeLabel.equals(eot) ) {
				System.out.println("config with EOT: "+c);
				d.setAcceptState(true);
				System.out.println("d goes from "+d);
				d.getNFAConfigurations().clear();
				d.addNFAConfiguration(p,c.alt,c.context,c.semanticContext);
				System.out.println("to "+d);
				return; // assume only one EOT transition
			}
		}
	}

	/** Add a new DFA state to this DFA if not already present.
     *  If the DFA state uniquely predicts a single alternative, it
     *  becomes a stop state; don't add to work list.  Further, if
     *  there exists an NFA state predicted by > 1 different alternatives
     *  and with the same syn and sem context, the DFA is nondeterministic for
     *  at least one input sequence reaching that NFA state.
     */
    protected DFAState addDFAState(DFAState d) {
        DFAState potentiallyExistingState = dfa.addState(d);
		if ( d != potentiallyExistingState ) {
			// already there...get the existing DFA state
			return potentiallyExistingState;
		}

		// if not there, then check new state.

        // resolve syntactic conflicts by choosing a single alt or
        // by using semantic predicates if present.
        d.resolveNonDeterminisms();

        // If deterministic, don't add this state; it's an accept state
        // for further processing--just return as a valid DFA state
        if ( d.getUniquelyPredictedAlt()!=NFA.INVALID_ALT_NUMBER ) {
            // everything is cool
            d.setAcceptState(true);
            /*
            System.out.println("state "+d.getStateNumber()+" uniquely predicts alt "+
                    d.getUniquelyPredictedAlt());
                    */
        }
        else {
            // unresolved, add to work list to continue NFA conversion
            work.add(d);
        }
        return d;
    }

	/** for each NFA config in d, look for "predicate required" sign set
	 *  during nondeterminism resolution.
	 */
	protected void addPredicateTransitions(DFAState d) {
		Iterator iter = d.getNFAConfigurations().iterator();
		while ( iter.hasNext() ) {
			NFAConfiguration c = (NFAConfiguration)iter.next();
			if ( !c.resolveWithPredicate ) {
				continue;
			}
			DFAState predDFATarget = dfa.newState();
			predDFATarget.addNFAConfiguration(dfa.nfa.getState(c.state),
					c.alt,
					c.context,
					c.semanticContext);
			predDFATarget.setAcceptState(true);
			d.addTransition(predDFATarget, new Label(c.semanticContext));
		}
	}

	protected void initContextTrees(int numberOfAlts) {
        contextTrees = new NFAContext[numberOfAlts];
        for (int i = 0; i < contextTrees.length; i++) {
            int alt = i+1;
            // add a dummy root node so that an NFA configuration can
            // always point at an NFAContext.  If a context refers to this
            // node then it implies there is no call stack for
            // that configuration
            contextTrees[i] = new NFAContext(null, null);
        }
    }
}
