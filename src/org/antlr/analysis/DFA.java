/*
 [The "BSD licence"]
 Copyright (c) 2004 Terence Parr
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
package org.antlr.analysis;

import org.antlr.runtime.IntegerStream;
import org.antlr.tool.GrammarAST;
import org.antlr.tool.ANTLRParser;
import org.antlr.tool.Grammar;
import org.antlr.misc.IntSet;
import org.antlr.misc.OrderedHashMap;
import org.antlr.misc.IntervalSet;
import org.antlr.misc.Interval;

import java.util.*;

/** A DFA (converted from a grammar's NFA).
 *  DFAs are used as prediction machine for alternative blocks in all kinds
 *  of recognizers (lexers, parsers, tree walkers).
 */
public class DFA {
    /** What's the start state for this DFA? */
    protected DFAState startState;

    /** From what NFAState did we create the DFA? */
    protected NFAState decisionNFAStartState;

    /** Unique state numbers */
    protected int stateCounter = 0;

    /** count only new states not states that were rejected as already present */
    protected int numberOfStates = 0;

    /** A set of all DFA states.  Maps hash of nfa configurations
     *  to the actual DFAState object.  We use this to detect
     *  existing DFA states.
     */
    protected Map states = new HashMap();

    /** A list of DFA states we still need to process during NFA conversion */
    protected List work = new LinkedList();

    public static final int REACHABLE_UNKNOWN = -2;
    public static final int REACHABLE_BUSY = -1; // in process of computing
    public static final int REACHABLE_NO = 0;
    public static final int REACHABLE_YES = 1;

    /** Is this DFA reduced?  I.e., can all states lead to an accept state? */
    protected boolean reduced = true;

    /** Are there any loops in this DFA? */
    protected boolean cyclic = false;

    /** Have we found a condition that renders DFA useless?  If so, terminate */
    protected boolean terminate = false;

    /** Each alt in an NFA derived from a grammar must have a DFA state that
     *  predicts it lest the parser not know what to do.  Nondeterminisms can
     *  lead to this situation (assuming no semantic predicates can resolve
     *  the problem) and when for some reason, I cannot compute the lookahead
     *  (which might arise from an error in the algorithm or from
     *  left-recursion etc...).  This list starts out with all alts contained
     *  and then in method doesStateReachAcceptState() I remove the alts I
     *  know to be uniquely predicted.
     */
    protected List unreachableAlts;

    /** While converting NFA, we must track states that
     *  reference other rule's NFAs so we know what to do
     *  at the end of a rule.  We need to know what context invoked
     *  this rule so we can know where to continue looking for NFA
     *  states.  I'm tracking a context tree (record of rule invocation
     *  stack trace) for each alternative that could be predicted.
     */
    protected NFAContext[] contextTrees;

    /** Which NFA are we converting (well, which piece of the NFA)? */
    protected NFA nfa;

    /** Given a choice in a nondeterministic situation, the decision will
     *  continue consuming tokens for the associated construct (usually
     *  a loop).  When nongreedy, the construct will exit the instant
     *  the lookahead is consistent with what follows the construct.  For
     *  example, to match Pascal comments, you want something like this:
     *
     *  "(*" ( greedy=false : . )* "*)"
     *
     *  Otherwise, greedy loop version would consume until EOF.
     *
     *  This is a cached value of what's in the options table.
     */
    protected boolean greedy = true;

    /** Subrules have options that will apply to any decisions built from
     *  that subrule.  This points to the options created during antlr.g
     *  parsing and stored in the BLOCK node.
     */
    protected Map options;

    public DFA(NFAState decisionStartState) {
        this.decisionNFAStartState = decisionStartState;
        nfa = decisionStartState.getNFA();
        int nAlts =
            nfa.getGrammar().getNumberOfAltsForDecisionNFA(decisionStartState);
        setOptions( nfa.getGrammar().getDecisionOptions(getDecisionNumber()) );
        initContextTrees(nAlts);
        initNondeterministicAlts(nAlts);

        performNFAToDFAConversion(decisionStartState);
    }

    protected void performNFAToDFAConversion(NFAState blockStart) {
        // create the DFA start state
        setStartState();

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
     *  alt is actually the exit branch of the loop.  Rather than make this
     *  alternative one, let's make this alt n+1 where n is the number of
     *  alts in this block.  Unless, that is, the DFA is greedy.  Then, we
     *  want exit alt to be 1 so that any nondeterminisms resolve in favor
     *  of the exit branch.  Cool!  Simple.  Actually, looking back on this
     *  I'm going to handle nongreedy in findNewDFAStatesAndAddDFATransitions
     *  when nongreedy and EOT transition.  Make state with EOT emanating
     *  from it the accept state.
     */
    protected void setStartState() {
        NFAState alt = decisionNFAStartState;
        startState = newState();
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
            if ( i==0 && getDecisionASTNode().getType()==ANTLRParser.EOB ) {
                int numAltsIncludingExitBranch = nfa.getGrammar()
                        .getNumberOfAltsForDecisionNFA(decisionNFAStartState);
                altNum = numAltsIncludingExitBranch;
                closure((NFAState)alt.transition(0).getTarget(),
                        altNum,
                        initialContext,
                        SemanticContext.EMPTY_SEMANTIC_CONTEXT,
                        startState,
                        true);
                altNum = 1;
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

        if ( !isGreedy() && labels.containsKey(new Label(Label.EOT)) ) {
            convertToEOTAcceptState(d);
            return; // no more work to do on this accept state
        }

        // for each label that could possibly emanate from NFAStates of d
        int numberOfEdgesEmanating = 0;
        for (int i=0; i<labels.size(); i++) {
            Label label = (Label)labels.get(i);
            /*
            if ( ignoreNonEOT && !label.equals(eot) ) {
                continue;
            }
            */
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

    /** Walk the configurations of this DFA state d looking for the
     *  configuration, c, that has a transition on EOT.  State d should
     *  be converted to an accept state predicting the c.alt.  Blast
     *  d's current configuration set and make it just have config c.
     *
     *  TODO: can there be more than one config with EOT transition?
     *  That would mean that two NFA configurations could reach the
     *  end of the token with possibly different predicted alts.
     *  Seems like that would rare or impossible.  Perhaps convert
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
            NFAState p = nfa.getState(c.state);
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
            DFAState predDFATarget = newState();
            predDFATarget.addNFAConfiguration(nfa.getState(c.state),
                    c.alt,
                    c.context,
                    c.semanticContext);
            predDFATarget.setAcceptState(true);
            d.addTransition(predDFATarget, new Label(c.semanticContext));
        }
    }

    /** EOT (end of token) is a label that indicates when the DFA conversion
     *  algorithm would "fall off the end of a lexer rule".  It normally
     *  means the default clause.  So for ('a'..'z')+ you would see a DFA
     *  with a state that has a..z and EOT emanating from it.  a..z would
     *  jump to a state predicting alt 1 and EOT would jump to a state
     *  predicting alt 2 (the exit loop branch).  EOF implies anything other
     *  than a..z.  If for some reason, the set is "all char" such as with
     *  the wildcard '.', then EOT cannot match anything.  For example,
     *
     *     BLOCK : '{' (.)* '}'
     *
     *  consumes all char until EOF when greedy=true.  When all edges are
     *  combined for the DFA state after matching '}', you will find that
     *  it is all char.  The EOT transition has nothing to match and is
     *  unreachable.  The findNewDFAStatesAndAddDFATransitions() method
     *  must know to ignore the EOT, so we simply remove it from the
     *  reachable labels.  Later analysis will find that the exit branch
     *  is not predicted by anything.  For greedy=false, we leave only
     *  the EOT label indicating that the DFA should stop immediately
     *  and predict the exit branch. The reachable labels are often a
     *  set of disjoint values like: [<EOT>, 42, {0..41, 43..65534}]
     *  due to DFA conversion so must construct a pure set to see if
     *  it is same as Label.ALLCHAR.
     *
     *  Only do this for Lexers.
     *
     *  If EOT coexists with ALLCHAR:
     *  1. If not greedy, modify the labels parameter to be EOT
     *  2. If greedy, remove EOT from the labels set
    protected boolean reachableLabelsEOTCoexistsWithAllChar(OrderedHashMap labels)
    {
        Label eot = new Label(Label.EOT);
        if ( !labels.containsKey(eot) ) {
            return false;
        }
        System.out.println("### contains EOT");
        boolean containsAllChar = false;
        IntervalSet completeVocab = new IntervalSet();
        int n = labels.size();
        for (int i=0; i<n; i++) {
            Label rl = (Label)labels.get(i);
            if ( !rl.equals(eot) ) {
                completeVocab.addAll(rl.getSet());
            }
        }
        System.out.println("completeVocab="+completeVocab);
        if ( completeVocab.equals(Label.ALLCHAR) ) {
            System.out.println("all char");
            containsAllChar = true;
        }
        return containsAllChar;
    }
     */

    /** For all NFA states (configurations) merged in d,
     *  compute the epsilon closure; that is, find all NFA states reachable
     *  from the NFA states in d via purely epsilon transitions.
     */
    protected void closure(DFAState d) {
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
            closure(nfa.getState(c.state),
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
    protected void closure(NFAState p,
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
    protected DFAState reach(DFAState d, Label label) {
        DFAState labelDFATarget = newState();
        // for each NFA state in d, add in target states for label
        int intLabel = label.getAtom();
        IntSet setLabel = label.getSet();
        Iterator iter = d.getNFAConfigurations().iterator();
        while ( iter.hasNext() ) {
            NFAConfiguration c =
                    (NFAConfiguration)iter.next();
            if ( c.resolved || c.resolveWithPredicate ) {
                continue; // the conflict resolver indicates we must leave alone
            }
            NFAState p = nfa.getState(c.state);
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

    /** Add a new DFA state to this DFA if not already present.
     *  If the DFA state uniquely predicts a single alternative, it
     *  becomes a stop state; don't add to work list.  Further, if
     *  there exists an NFA state predicted by > 1 different alternatives
     *  and with the same syn and sem context, the DFA is nondeterministic for
     *  at least one input sequence reaching that NFA state.
     */
    protected DFAState addDFAState(DFAState d) {
        DFAState existing = (DFAState)states.get(d);
        if ( existing != null ) {
            // already there...get the existing DFA state
            return existing;
        }

        // if not there, then check new state.
        states.put(d,d); // add to overall set of states
        numberOfStates++;
        
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

    /** Is the DFA reduced?  I.e., does every state have a path to an accept
     *  state?  If not, don't delete as we need to generate an error indicating
     *  which paths are "dead ends".  Also tracks list of alts with no accept
     *  state in the DFA.
     */
    public boolean isReduced() {
        doesStateReachAcceptState(getStartState());
        return reduced;
    }

    /** Is this DFA cyclic?  That is, are there any loops?  If not, then
     *  the DFA is essentially an LL(k) predictor for some fixed, max k value.
     *  We can build a series of nested IF statements to match this.  In the
     *  presence of cycles, we need to build a general DFA and interpret it
     *  to distinguish between alternatives.
     */
    public boolean isCyclic() {
        return cyclic;
    }

    /** Return a list of Integer alt numbers for which no lookahead could
     *  be computed or for which no single DFA accept state predicts those
     *  alts.
     */
    public List getUnreachableAlts() {
        isReduced(); // make sure computation is performed
        return unreachableAlts;
    }

    /** figure out if this state eventually reaches an accept state and
     *  modify the instance variable 'reduced' to indicate if we find
     *  at least one state that cannot reach an accept state.  This implies
     *  that the overall DFA is not reduced.  This algorithm should be
     *  linear in the number of DFA states.
     *
     *  The algorithm also tracks which alternatives have no accept state,
     *  indicating a nondeterminism.
     *  TODO: I call getUniquelyPredicatedAlt too much; cache predicted alt
     */
    protected boolean doesStateReachAcceptState(DFAState d) {
        /*
        System.out.println("doesStateReachAcceptState processing DFA state "+
                d.getStateNumber());
        */

        if ( d.isAcceptState() ) {
            // accept states have no edges emanating from them so we can return
            d.setAcceptStateReachable(REACHABLE_YES);
            // this alt is uniquely predicted, remove from nondeterministic list
            int predicts = d.getUniquelyPredictedAlt();
            unreachableAlts.remove(new Integer(predicts));
            return true;
        }

        // avoid infinite loops
        d.setAcceptStateReachable(REACHABLE_BUSY);

        boolean anEdgeReachesAcceptState = false;
        for (int i=0; i<d.getNumberOfTransitions(); i++) {
            Transition t = d.transition(i);
            DFAState edgeTarget = (DFAState)t.getTarget();
            int targetStatus = edgeTarget.getAcceptStateReachable();
            if ( targetStatus==REACHABLE_BUSY ) { // avoid cycles; they say nothing
                cyclic = true;
                continue;
            }
            if ( targetStatus==REACHABLE_YES ) { // avoid unnecessary work
                anEdgeReachesAcceptState = true;
                continue;
            }
            if ( targetStatus==REACHABLE_NO ) {  // avoid unnecessary work
                continue;
            }
            if ( doesStateReachAcceptState(edgeTarget) ) {
                anEdgeReachesAcceptState = true;
                // have to keep looking so don't break loop
                // must cover all states even if we find a path for this state
            }
        }
        if ( anEdgeReachesAcceptState ) {
            d.setAcceptStateReachable(REACHABLE_YES);
        }
        else {
            d.setAcceptStateReachable(REACHABLE_NO);
            reduced = false;
        }
        return anEdgeReachesAcceptState;
    }

    public DFAState getStartState() {
        return startState;
    }

    public NFAState getNFADecisionStartState() {
        return decisionNFAStartState;
    }

    public int getDecisionNumber() {
        return decisionNFAStartState.getDecisionNumber();
    }

    /** What GrammarAST node (derived from the grammar) is this DFA
     *  associated with?  It will point to the start of a block or
     *  the loop back of a (...)+ block etc...
     */
    public GrammarAST getDecisionASTNode() {
        return decisionNFAStartState.getDecisionASTNode();
    }

    public boolean isGreedy() {
        return greedy;
    }

    public void setOptions(Map options) {
        greedy=true; // reset options
        this.options = options;
        if ( options==null ) {
            return;
        }
        String v = (String)options.get("greedy");
        if ( v!=null && v.equals("false") ) {
            greedy=false;
        }
    }

    public DFAState newState() {
        DFAState n = new DFAState(this);
        int state = stateCounter;
        n.setStateNumber(state);
        stateCounter++;
        return n;
    }

    public int getNumberOfStates() {
        return numberOfStates;
    }

    public NFA getNFA() {
        return nfa;
    }

    /** Given an input stream, return the unique alternative predicted by
     *  matching the input.  Upon error, return NFA.INVALID_ALT_NUMBER
     *  The first symbol of lookahead is presumed to be primed; that is,
     *  input.lookahead(1) must point at the input symbol you want to start
     *  predicting with.
     */
    public int predict(IntegerStream input) {
        DFAState s = getStartState();
        int c = input.LA(1);
    dfaLoop:
        while ( !s.isAcceptState() ) {
            System.out.println("DFA.matches("+s.getStateNumber()+", "+
                    nfa.getGrammar().getTokenName(c)+")");
            // for each edge of s, look for intersection with current char
            for (int i=0; i<s.getNumberOfTransitions(); i++) {
                Transition t = s.transition(i);
                if ( t.getLabel().matches(c) ) {
                    // take transition i
                    s = (DFAState)t.getTarget();
                    input.consume();
                    c = input.LA(1);
                    continue dfaLoop;
                }
            }
            System.err.println("unexpected label '"+
                    nfa.getGrammar().getTokenName(c)+"' in dfa state "+s);
            return NFA.INVALID_ALT_NUMBER;
        }
        // woohoo!  We know which alt to predict
        // nothing emanates from a stop state; must terminate anyway
        System.out.println("DFA stop state "+s.getStateNumber()+" predicts "+
                s.getUniquelyPredictedAlt());
        return s.getUniquelyPredictedAlt();
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

    protected void initNondeterministicAlts(int numberOfAlts) {
        unreachableAlts = new LinkedList();
        for (int i = 1; i <= numberOfAlts; i++) {
            unreachableAlts.add(new Integer(i));
        }
    }
 }

