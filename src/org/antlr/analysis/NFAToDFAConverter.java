package org.antlr.analysis;

import org.antlr.misc.*;
import org.antlr.tool.ANTLRParser;
import org.antlr.tool.Grammar;

import java.util.*;
import java.util.BitSet;

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

	private static boolean debug = false;

	public NFAToDFAConverter(DFA dfa) {
		this.dfa = dfa;
		NFAState nfaStartState = dfa.getNFADecisionStartState();
		int nAlts =
			dfa.nfa.getGrammar().getNumberOfAltsForDecisionNFA(nfaStartState);
		initContextTrees(nAlts);
	}

	public void convert(NFAState blockStart) {
		// create the DFA start state
		DFAState startState = getStartState();
		dfa.setStartState(startState);

		// while more DFA states to check, process them
		while ( !terminate && work.size()>0 ) {
			DFAState d = (DFAState) work.get(0);
			if ( debug ) {
				System.out.println("convert DFA state "+d.getStateNumber()+
								   " ("+d.getNFAConfigurations().size()+" nfa states)");
			}
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
				int numAltsIncludingExitBranch = dfa.nfa.getGrammar()
						.getNumberOfAltsForDecisionNFA(dfa.getDecisionNFAStartState());
				altNum = numAltsIncludingExitBranch;
				closure((NFAState)alt.transition(0).getTarget(),
						altNum,
						initialContext,
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
			if ( debug ) {
				System.out.println("DFA state after reach "+d+"-" +
								   label.toString(dfa.nfa.getGrammar())+"->"+t);
			}
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
			dfa.probe.reportDanglingState(d);
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
		if ( debug ) {
			System.out.println("closure("+d+")");
		}
		Set configs = new HashSet();
		// Because we are adding to the configurations in closure
		// must clone initial list so we know when to stop doing closure
		// TODO: expensive, try to get around this alloc / copy
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
			closure(dfa.nfa.getState(c.state),
					c.alt,
					c.context,
					c.context, // initialContext = c.context (stack top)
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
						NFAContext initialContext,
						SemanticContext semanticContext,
						DFAState d,
						boolean collectPredicates)
	{
		if ( debug ) {
			System.out.println("closure at NFA state "+p.getStateNumber()+"|"+
							   alt+" filling DFA state "+d+" with context "+context+
							   "(initial context="+initialContext+")");
		}

		// Avoid infinite recursion
		// If we've seen this configuration before during closure, stop
		if ( closureIsBusy(d,p,alt,context,initialContext,semanticContext) ) {
			if ( debug ) {
				System.out.println("avoid infinite closure computation to state "+p.getStateNumber()+
								   " from context "+context+" alt="+alt+" semctx="+semanticContext+
								   " (initial context="+initialContext+")");
				System.out.println("state is "+d);
			}
			return;
		}
		setClosureIsBusy(d,p,alt,context,semanticContext);

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
			closure(ruleTarget, alt, newContext, initialContext, semanticContext, d, collectPredicates);
		}
		// Case 2: end of rule state, context (i.e., an invoker) exists
		else if ( p.isAcceptState() && context.getParent()!=null ) {
			NFAState whichStateInvokedRule = context.getInvokingState();
			RuleClosureTransition edgeToRule =
				(RuleClosureTransition)whichStateInvokedRule.transition(0);
			NFAState continueState = edgeToRule.getFollowState();
			NFAContext newContext = context.getParent(); // "pop" invoking state
			// we must move the initial context for this overall closure
			initialContext = newContext; // mv stack top
			closure(continueState, alt, newContext, initialContext, semanticContext, d, collectPredicates);
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
						initialContext,
						semanticContext,
						d,
						collectPredicates);
			}
			else if ( transition0!=null && transition0.isSemanticPredicate() ) {
				// continue closure here too, but add the sem pred to ctx
				SemanticContext newSemanticContext = semanticContext;
				if ( collectPredicates ) {
					// AND the previous semantic context with new pred
					SemanticContext labelContext =
						transition0.getLabel().getSemanticContext();
					newSemanticContext = SemanticContext.and(semanticContext,
													 labelContext);
				}
				closure((NFAState)transition0.getTarget(),
						alt,
						context,
						initialContext,
						newSemanticContext,
						d,
						collectPredicates);
			}
			Transition transition1 = p.transition(1);
			if ( transition1!=null && transition1.isEpsilon() ) {
				closure((NFAState)transition1.getTarget(),
						alt,
						context,
						initialContext,
						semanticContext,
						d,
						collectPredicates);
			}
		}

		// don't remove "busy" flag as we want to prevent all
		// references to same config of state|alt|ctx|semCtx even
		// if resulting from another NFA state
	}

	/** When is a closure operation in a cycle condition?  While it is
	 *  very possible to have the same NFA state mentioned twice
	 *  within the same DFA state, there are two situations (when
	 *  grammar is nondeterministic) that lead to nontermination of
	 *  closure operation:
	 *
	 *  (1) Whenever closure reaches a configuration where the same state
	 *      with same context already exists.  This catches
	 *      the IF-THEN-ELSE tail recursion cycle and things like
	 *
	 *      a : A a | B ;
	 *
	 *      the context will be $ (empty stack).  We have to check
	 *      larger context stacks because of (...)+ loops.  For
	 *      example, the context of a (...)+ can be nonempty if the
	 *      surrounding rule is invoked by another rule:
	 *
	 *      a : b A | X ;
	 *      b : (B|)+ ;  // nondeterministic by the way
	 *
	 *      The context of the (B|)+ loop is "invoked from item a :
	 *      . b A ;" and then the empty alt of the loop can reach back
	 *      to itself.  The context stack will have one "return
	 *      address" element and so we must check for same state, same
	 *      context for arbitrary context stacks.
	 *
	 *      A simple equality between the state and context stack
	 *      strings satisfies this condition.  This condition catches
	 *      cycles derived from tail recursion explicitly or
	 *      implicitly with (...)+ loops.
	 *
	 *  (2) Whenever closure reaches a configuration where the state
	 *      is present in its own context stack.  This means that this
	 *      closure operation has come full circle back to the same
	 *      NFA state; i.e., there is a "back pointer" to an earlier
	 *      NFA configuration that would force us to loop endlessly.
	 *      This situation arises only in direct and indirect
	 *      recursion such as "a : a A | B;" (direct here).
	 *
	 *      One way to test for this condition is to look for two
	 *      configurations that have the same state and where one
	 *      context is a right suffix of another such as 5|2$, 5|262$.
	 *      In this case, the closure got got to state 5 from context
	 *      2$ and then, after visiting another rule via state
	 *      6, the algorithm returns to state 2 which again jumps to state 5.
	 *      Could be infinite recursion.  More formally, if the
	 *      state of the NFA may proceed from
	 *
	 *      p|alpha$ ->+ p|beta p alpha$
	 *
	 *      then the closure has detected an epsilon cycle in the NFA
	 *      derived from left-recursion.
	 *
	 * 		TODO: ack! You can get back to same state with from same context but after consuming a token!
	 * 		TODO: fix this comment to indicate that we need to check for cycles only within a single closure op
	 *      TODO: also this comment reflects old context where it was "return addr" not jumping off point
	 *
	 *      A quick way to check for this cycle is to focus on a
	 *      single configuration of p|alpha$ rather than searching
	 *      other NFA configurations in this DFA state looking for a
	 *      subset.  To detect a cycle (specifically cycles due to
	 *      left recursion), one can simply look for the presence of
	 *      p's followState in alpha.  If p has been visited before
	 *      during closure, we'll visit again in the future ad
	 *      infinitum.
	 *
	 *  The DFA simulates the possible configurations of the NFA and,
	 *  hence, a closure that returns to the same state implies that
	 *  the NFA returns to the same configuration without having
	 *  consumed any input. [ONLY IF IN SAME CLOSURE!] A clear example is an NFA state that
	 *  loops to itself on epsilon.  This loop should be ignored in
	 *  the final DFA as it does not contribute to the language
	 *  generated by the NFA (nor the DFA consequently).  We must
	 *  avoid any situation where the closure returns to process an
	 *  NFA state where the context (1) is identical or (2) indicates
	 *  that the NFA has already been to this state.
	 *
	 *  Termination
	 *
	 *  This guarantees termination of the addition of configurations
	 *  to a DFA state because there is a finite number of
	 *  configurations added due to transitions from the prior DFA
	 *  state (from which this state was created).  Any NFA states
	 *  reachable from the prior DFA state NFA subset labeled with the
	 *  same, nonepsilon, label will lead to this state.  There is a
	 *  finite number of NFA states and hence a finite number of
	 *  nonepsilon edges leading to other NFA states thus there will
	 *  be a finite number of initial NFA states in this new state
	 *  before closure.  Closure terminates because there is a finite
	 *  number of NFA configurations that may be added to a DFA state.
	 *  First, there are a finite number of NFA states and so only
	 *  repeated NFA states could lead to an infinite number.  NFA
	 *  state | context pairs must be unique and so only pairs of
	 *  states with dissimilar contexts must be considered.  The only
	 *  way for two configurations to have the same state and
	 *  different context stacks is when there is a cycle to that
	 *  state emulating a rule invocation (i.e., left-recursion).  We
	 *  detect this case by asking if the state has been visited
	 *  before in it's current context.
	 *
	 *  What about the alts?  Do they affect termination of the
	 *  closure?  No.  This information is merely carried along to
	 *  decide when to terminate the overall algorithm early (i.e., as
	 *  soon as we find that the decision is deterministic==all NFA
	 *  states within a DFA state predict same alt).  Since we do not
	 *  split states or alter the algorithm in anyway (except early
	 *  termination upon determinism or nondetermism), the alts are
	 *  not technically considered part of the "closure busy" signal.
	 *  HOWEVER, we would like to know that two alts got to the same
	 *  NFA configuration since we can give a good error message
	 *  later.  So, we do not collapse configurations p:i|alpha and
	 *  p:j|alpha into the same configuration for "busy signal"
	 *  purposes.  Allow both of those configurations in the DFA
	 *  state.
	 */
	public boolean closureIsBusy(DFAState d,
								 NFAState p,
								 int alt,
								 NFAContext context,
								 NFAContext initialContext,
								 SemanticContext semContext)
	{
		// case (1) : epsilon cycle (same state, same context)
		NFAConfiguration c =
				new NFAConfiguration(p.getStateNumber(),
						alt,
						context,
						semContext);
		if ( d.closureBusy.contains(c) ) {
			return true;
		}

		// case (2) : recursive (same state, state visited before)
		if ( context.contains(p.getStateNumber(), initialContext) ) {
			return true;
		}
		return false;
	}

	public void setClosureIsBusy(DFAState d,
								 NFAState p,
								 int alt,
								 NFAContext context,
								 SemanticContext semContext)
	{
		NFAConfiguration c =
				new NFAConfiguration(p.getStateNumber(),
									 alt,
									 context,
									 semContext);
		d.closureBusy.add(c);
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
			NFAState p = dfa.nfa.getState(c.state);
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

	/** Add a new DFA state to the DFA if not already present.
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
        resolveNonDeterminisms(d);

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

	/** If > 1 NFA configurations within this DFA state have identical
	 *  NFA state and context, but differ in their predicted
	 *  alternative then a single input sequence predicts multiple alts.
	 *  The NFA decision is therefore syntactically indistinguishable
	 *  from the left edge upon at least one input sequence.  We may
	 *  terminate the NFA to DFA conversion for these paths since no
	 *  paths emanating from those NFA states can possibly separate
	 *  these conjoined twins once interwined to make things
	 *  deterministic (unless there are semantic predicates; see below).
	 *
	 *  Upon a nondeterministic set of NFA configurations, we should
	 *  report a problem to the grammar designer and resolve the issue
	 *  by aribitrarily picking the first alternative (this usually
	 *  ends up producing the most natural behavior).  Pick the lowest
	 *  alt number and just turn off all NFA configurations
	 *  associated with the other alts. Rather than remove conflicting
	 *  NFA configurations, I set the "resolved" bit so that future
	 *  computations will ignore them.  In this way, we maintain the
	 *  complete DFA state with all its configurations, but prevent
	 *  future DFA conversion operations from pursuing undesirable
	 *  paths.  Remember that we want to terminate DFA conversion as
	 *  soon as we know the decision is deterministic *or*
	 *  nondeterministic.
	 *
	 *  [BTW, I have convinced myself that there can be at most one
	 *  set of nondeterministic configurations in a DFA state.  Only NFA
	 *  configurations arising from the same input sequence can appear
	 *  in a DFA state.  There is no way to have another complete set
	 *  of nondeterministic NFA configurations without another input
	 *  sequence, which would reach a different DFA state.  Therefore,
	 *  the two nondeterministic NFA configuration sets cannot collide
	 *  in the same DFA state.]
	 *
	 *  Consider DFA state {(s|1),(s|2),(s|3),(t|3),(v|4)} where (s|a)
	 *  is state 's' and alternative 'a'.  Here, configuration set
	 *  {(s|1),(s|2),(s|3)} predicts 3 different alts.  Configurations
	 *  (s|2) and (s|3) are "resolved", leaving {(s|1),(t|3),(v|4)} as
	 *  items that must still be considered by the DFA conversion
	 *  algorithm in DFA.findNewDFAStatesAndAddDFATransitions().
	 *
	 *  Consider the following grammar where alts 1 and 2 are no
	 *  problem because of the 2nd lookahead symbol.  Alts 3 and 4 are
	 *  identical and will therefore reach the rule end NFA state but
	 *  predicting 2 different alts (no amount of future lookahead
	 *  will render them deterministic/separable):
	 *
	 *  a : A B
	 *    | A C
	 *    | A
	 *    | A
	 *    ;
	 *
	 *  Here is a (slightly reduced) NFA of this grammar:
	 *
	 *  (1)-A->(2)-B->(end)-EOF->(8)
	 *   |              ^
	 *  (2)-A->(3)-C----|
	 *   |              ^
	 *  (4)-A->(5)------|
	 *   |              ^
	 *  (6)-A->(7)------|
	 *
	 *  where (n) is NFA state n.  To begin DFA conversion, the start
	 *  state is created:
	 *
	 *  {(1|1),(2|2),(4|3),(6|4)}
	 *
	 *  Upon A, all NFA configurations lead to new NFA states yielding
	 *  new DFA state:
	 *
	 *  {(2|1),(3|2),(5|3),(7|4),(end|3),(end|4)}
	 *
	 *  where the configurations with state end in them are added
	 *  during the epsilon closure operation.  State end predicts both
	 *  alts 3 and 4.  An error is reported, the latter configuration is
	 *  flagged as resolved leaving the DFA state as:
	 *
	 *  {(2|1),(3|2),(5|3),(7|4|resolved),(end|3),(end|4|resolved)}
	 *
	 *  As NFA configurations are added to a DFA state during its
	 *  construction, the reachable set of labels is computed.  Here
	 *  reachable is {B,C,EOF} because there is at least one NFA state
	 *  in the DFA state that can transition upon those symbols.
	 *
	 *  The final DFA looks like:
	 *
	 *  {(1|1),(2|2),(4|3),(6|4)}
	 *              |
	 *              v
	 *  {(2|1),(3|2),(5|3),(7|4),(end|3),(end|4)} -B-> (end|1)
	 *              |                        |
	 *              C                        ----EOF-> (8,3)
	 *              |
	 *              v
	 *           (end|2)
	 *
	 *  Upon AB, alt 1 is predicted.  Upon AC, alt 2 is predicted.
	 *  Upon A EOF, alt 3 is predicted.  Alt 4 is not a viable
	 *  alternative.
	 *
	 *  The algorithm is essentially to walk all the configurations
	 *  looking for a conflict of the form (s|i) and (s|j) for i!=j.
	 *  Use a hash table to track state+context pairs for collisions
	 *  so that we have O(n) to walk the n configurations looking for
	 *  a conflict.  Upon every conflict, track the alt number so
	 *  we have a list of all nondeterministically predicted alts. Also
	 *  track the minimum alt.  Next go back over the configurations, setting
	 *  the "resolved" bit for any that have an alt that is a member of
	 *  the nondeterministic set.  This will effectively remove any alts
	 *  but the one we want from future consideration.
	 *
	 *  See resolveWithSemanticPredicates()
	 *
	 *  AMBIGUOUS TOKENS
	 *
	 *  If all NFA states in this DFA state are targets of EOT transitions,
	 *  (and there is more than one state plus no unique alt is predicted)
	 *  then DFA conversion will leave this state as a dead state as nothing
	 *  can be reached from this state.  To resolve the ambiguity, just do
	 *  what flex and friends do: pick the first rule (alt in this case) to
	 *  win.  This means you should put keywords before the ID rule (unless
	 *  you're using the literals table).  If the DFA state has only one NFA
	 *  state then there is no issue: it uniquely predicts one alt. :)  Problem
	 *  states will look like this during conversion:
	 *
	 *  DFA 1:{9|1, 19|2, 14|3, 20|2, 23|2, 24|2, ...}-<EOT>->5:{41|3, 42|2}
	 *
	 *  Worse, when you have two identical literal rules, you will see 3 alts
	 *  in the EOF state (one for ID and one each for the identical rules).
	 */
	public void resolveNonDeterminisms(DFAState d) {
		boolean conflictingLexerRules = false;
		Set nondeterministicAlts = d.getNondeterministicAlts();

		// CHECK FOR AMBIGUOUS EOT (if |allAlts|>1 and EOT state, resolve)

		// grab any config to see if EOT state; any other configs must
		// transition on EOT to get to this DFA state as well
		NFAConfiguration anyConfig;
		Iterator itr = d.nfaConfigurations.iterator();
		anyConfig = (NFAConfiguration)itr.next();
		NFAState anyState = dfa.nfa.getState(anyConfig.state);
		// if d is target of EOT and more than one predicted alt
		// indicate that d is nondeterministic on all alts otherwise
		// it looks like state has no problem
		if ( anyState.isEOTState() ) {
			Set allAlts = d.getAltSet();
			if ( allAlts.size()>1 ) {
				nondeterministicAlts = allAlts;
				int decision = d.dfa.getDecisionNumber();
				NFAState tokensRuleStartState =
					dfa.nfa.getGrammar().getRuleStartState(Grammar.TOKEN_RULENAME);
				NFAState decisionState =
					(NFAState)tokensRuleStartState.transition(0).getTarget();
				// track lexer rule issues differently than other decisions
				if ( decisionState.getDecisionNumber() == decision ) {
					dfa.probe.reportLexerRuleNondeterminism(d,allAlts);
					conflictingLexerRules = true;
				}
			}
		}

		if ( nondeterministicAlts==null ) {
			return; // no problems, return
		}

		if ( !conflictingLexerRules ) {
			dfa.probe.reportNondeterminism(d,nondeterministicAlts);
		}

		// ATTEMPT TO RESOLVE WITH SEMANTIC PREDICATES

		boolean resolved =
			tryToResolveWithSemanticPredicates(d, nondeterministicAlts);
		if ( resolved ) {
			d.resolvedWithPredicates = true;
			dfa.probe.reportNondeterminismResolvedWithSemanticPredicate(d);
			return;
		}

		// RESOLVE SYNTACTIC CONFLICT BY REMOVING ALL BUT MIN ALT

        resolveByPickingMinAlt(d,nondeterministicAlts);
	}

	protected void resolveByPickingMinAlt(DFAState d, Set nondeterministicAlts) {
		Iterator iter = nondeterministicAlts.iterator();
		int min = Integer.MAX_VALUE;
		while (iter.hasNext()) {
			Integer altI = (Integer) iter.next();
			int alt = altI.intValue();
			if ( alt < min ) {
				min = alt;
			}
		}

		// turn off all states associated with alts other than the good one
		// (as long as they are one of the nondeterministic ones)
		iter = d.nfaConfigurations.iterator();
		NFAConfiguration configuration;
		while (iter.hasNext()) {
			configuration = (NFAConfiguration) iter.next();
			if ( configuration.alt!=min &&
				 nondeterministicAlts.contains(new Integer(configuration.alt)) ) {
				configuration.resolved = true;
			}
		}
	}

	/** See if a set of nondeterministic alternatives can be disambiguated
	 *  with the semantic predicate contexts of the alternatives.
	 *
	 *  Without semantic predicates, syntactic conflicts are resolved
	 *  by simply choosing the first viable alternative.  In the
	 *  presence of semantic predicates, you can resolve the issue by
	 *  evaluating boolean expressions at run time.  During analysis,
	 *  this amounts to suppressing grammar error messages to the
	 *  developer.  NFA configurations are always marked as "to be
	 *  resolved with predicates" so that
	 *  DFA.findNewDFAStatesAndAddDFATransitions() will know to ignore
	 *  these configurations and add predicate transitions to the DFA
	 *  after adding token/char labels.
	 *
	 *  During analysis, we can simply make sure that for n
	 *  ambiguously predicted alternatives there are at least n-1
	 *  unique predicate sets.  The nth alternative can be predicted
	 *  with "not" the "or" of all other predicates.  NFA configurations without
	 *  predicates are assumed to have the default predicate of
	 *  "true" from a user point of view.  When true is combined via || with
	 *  another predicate, the predicate is a tautology and must be removed
	 *  from consideration for disambiguation:
	 *
	 *  a : b | B ; // hoisting p1||true out of rule b, yields no predicate
	 *  b : {p1}? B | B ;
	 *
	 *  This is done down in getPredicatesPerNonDeterministicAlt().
	 */
	protected boolean tryToResolveWithSemanticPredicates(DFAState d,
														 Set nondeterministicAlts)
	{
		Map altToPredMap =
				getPredicatesPerNonDeterministicAlt(d, nondeterministicAlts);

		if ( altToPredMap.size()==0 ) {
			return false;
		}

		System.out.println("nondeterministic alts with predicates: "+altToPredMap);
		dfa.probe.reportAltPredicateContext(d, altToPredMap);

		if ( nondeterministicAlts.size()-altToPredMap.size()>1 ) {
			// too few predicates to resolve; just return
			// TODO: actually do we need to gen error here?
			return false;
		}

		// Handle case where 1 predicate is missing
		if ( altToPredMap.size()==nondeterministicAlts.size()-1 ) {
			// if there are n-1 predicates for n nondeterministic alts, can fix
			org.antlr.misc.BitSet ndSet = org.antlr.misc.BitSet.of(nondeterministicAlts);
			org.antlr.misc.BitSet predSet = org.antlr.misc.BitSet.of(altToPredMap);
			int nakedAlt = ndSet.subtract(predSet).getSingleElement();
			// pretend naked alternative is covered with !(union other preds)
			SemanticContext unionOfPredicatesFromAllAlts =
					getUnionOfPredicates(altToPredMap);
			//System.out.println("all predicates "+unionOfPredicatesFromAllAlts);
			SemanticContext notOtherPreds =
					SemanticContext.not(unionOfPredicatesFromAllAlts);
			System.out.println("covering naked alt="+nakedAlt+" with "+notOtherPreds);
			altToPredMap.put(new Integer(nakedAlt), notOtherPreds);
			// set all config with alt=nakedAlt to have NOT of all
			// predicates on other alts
			Iterator iter = d.nfaConfigurations.iterator();
			NFAConfiguration configuration;
			while (iter.hasNext()) {
				configuration = (NFAConfiguration) iter.next();
				if ( configuration.alt == nakedAlt ) {
					configuration.semanticContext = notOtherPreds;
				}
			}
		}

		if ( altToPredMap.size()==nondeterministicAlts.size() ) {
			// RESOLVE CONFLICT by picking one NFA configuration for each alt
			// and setting its resolvedWithPredicate flag
			Iterator iter = d.nfaConfigurations.iterator();
			NFAConfiguration configuration;
			while (iter.hasNext()) {
				configuration = (NFAConfiguration) iter.next();
				SemanticContext semCtx = (SemanticContext)
						altToPredMap.get(new Integer(configuration.alt));
				if ( semCtx!=null ) {
					// resolve (first found) with pred
					// and remove alt from problem list
					configuration.resolveWithPredicate = true;
					configuration.semanticContext = semCtx; // reset to combined
					altToPredMap.remove(new Integer(configuration.alt));
				}
				else if ( nondeterministicAlts.contains(new Integer(configuration.alt)) ) {
					// resolve all configurations for nondeterministic alts
					// for which there is no predicate context by turning it off
					configuration.resolved = true;
				}
			}
			return true;
		}

		return false;  // couldn't fix the problem with predicates
	}

	/** Return a mapping from nondeterministc alt to combined list of predicates.
	 *  If both (s|i|semCtx1) and (t|i|semCtx2) exist, then the proper predicate
	 *  for alt i is semCtx1||semCtx2 because you have arrived at this single
	 *  DFA state via two NFA paths, both of which have semantic predicates.
	 *  We ignore deterministic alts because syntax alone is sufficient
	 *  to predict those.  Do not include their predicates.
	 *
	 *  Alts with no predicate are assumed to have {true}? pred.
	 *
	 *  When combining via || with "true", all predicates are removed from
	 *  consideration since the expression will always be true and hence
	 *  not tell us how to resolve anything.  So, if any NFA configuration
	 *  in this DFA state does not have a semantic context, the alt cannot
	 *  be resolved with a predicate.
	 */
	protected Map getPredicatesPerNonDeterministicAlt(DFAState d,
													  Set nondeterministicAlts)
	{
		// map alt to combined SemanticContext
		Map altToPredicateContextMap = new HashMap();
		// track tautologies like p1||true
		Set altToIncompletePredicateContextMap = new HashSet();
		Iterator iter = d.nfaConfigurations.iterator();
		NFAConfiguration configuration;
		while (iter.hasNext()) {
			configuration = (NFAConfiguration) iter.next();
			// if alt is nondeterministic, combine its predicates
			if ( nondeterministicAlts.contains(new Integer(configuration.alt)) ) {
				// if there is a predicate for this NFA configuration, OR in
				if ( configuration.semanticContext !=
					 SemanticContext.EMPTY_SEMANTIC_CONTEXT )
				{
					SemanticContext altsExistingPred =(SemanticContext)
							altToPredicateContextMap.get(new Integer(configuration.alt));
					if ( altsExistingPred!=null ) {
						// must merge all predicates from configs with same alt
						SemanticContext combinedContext =
								SemanticContext.or(
										altsExistingPred,
										configuration.semanticContext);
						/*
						System.out.println(altsExistingPred+" OR "+
										   configuration.semanticContext+
										   "="+combinedContext);
						*/
						altToPredicateContextMap.put(
								new Integer(configuration.alt),
								combinedContext
						);
					}
					else {
						// not seen before, just add it
						altToPredicateContextMap.put(
								new Integer(configuration.alt),
								configuration.semanticContext
						);
					}
				}
				else {
					// if no predicate, but it's part of nondeterministic alt
					// then at least one path exists not covered by a predicate.
					// must remove predicate for this alt; track incomplete alts
					altToIncompletePredicateContextMap.add(
							new Integer(configuration.alt)
					);
				}
			}
		}

		// remove any predicates from incompletely covered alts
		iter = altToIncompletePredicateContextMap.iterator();
		while (iter.hasNext()) {
			Integer alt = (Integer) iter.next();
			SemanticContext insufficientPred =(SemanticContext)
					altToPredicateContextMap.get(alt);
			if ( insufficientPred!=null ) {
				dfa.probe.reportIncompletelyCoveredAlts(d,
														alt.intValue(),
														insufficientPred);
			}
			altToPredicateContextMap.remove(alt);
		}

		return altToPredicateContextMap;
	}

	/** OR together all predicates from the alts.  Note that the predicate
	 *  for an alt could itself be a combination of predicates.
	 */
	protected SemanticContext getUnionOfPredicates(Map altToPredMap) {
		Iterator iter;
		SemanticContext unionOfPredicatesFromAllAlts = null;
		iter = altToPredMap.values().iterator();
		while ( iter.hasNext() ) {
			SemanticContext semCtx = (SemanticContext)iter.next();
			if ( unionOfPredicatesFromAllAlts==null ) {
				unionOfPredicatesFromAllAlts = semCtx;
			}
			else {
				unionOfPredicatesFromAllAlts =
						SemanticContext.or(unionOfPredicatesFromAllAlts,semCtx);
			}
		}
		return unionOfPredicatesFromAllAlts;
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
