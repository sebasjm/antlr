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

import org.antlr.misc.BitSet;
import org.antlr.misc.IntSet;
import org.antlr.misc.OrderedHashMap;

import java.util.*;

/** A DFA state represents a set of possible NFA configurations.
 *  As Aho, Sethi, Ullman p. 117 says "The DFA uses its state
 *  to keep track of all possible states the NFA can be in after
 *  reading each input symbol.  That is to say, after reading
 *  input a1a2..an, the DFA is in a state that represents the
 *  subset T of the states of the NFA that are reachable from the
 *  NFA's start state along some path labeled a1a2..an."
 *  In conventional NFA->DFA conversion, therefore, the subset T
 *  would be a bitset representing the set of states the
 *  NFA could be in.  We need to track the alt predicted by each
 *  state as well, however.  More importantly, we need to maintain
 *  a stack of states, tracking the closure operations as they
 *  jump from rule to rule, emulating rule invocations (method calls).
 *  Recall that NFAs do not normally have a stack like a pushdown-machine
 *  so I have to add one to simulate the proper lookahead sequences for
 *  the underlying LL grammar from which the NFA was derived.
 *
 *  I use a list of NFAConfiguration objects.  An NFAConfiguration
 *  is both a state (ala normal conversion) and an NFAContext describing
 *  the chain of rules (if any) followed to arrive at that state.  There
 *  is also the semantic context, which is the "set" of predicates found
 *  on the path to this configuration.
 *
 *  A DFA state may have multiple references to a particular state,
 *  but with different NFAContexts (with same or different alts)
 *  meaning that state was reached via a different set of rule invocations.
 */
public class DFAState extends State {
    public static final int INITIAL_NUM_TRANSITIONS = 8;

    /** We are part of what DFA?  Use this ref to get access to the
     *  context trees for an alt.
     */
    protected DFA dfa;

    /** Track the transitions emanating from this DFA state.  The List
     *  elements are Transition objects.
     */
    protected List transitions = new ArrayList(INITIAL_NUM_TRANSITIONS);

    /** Does this DFA state represent a stop state that predicts which
     *  alternative to choose?
    protected boolean acceptState = false;
     */

    /** The NFA->DFA algorithm may terminate leaving some states
     *  without a path to an accept state, implying that upon certain
     *  input, the decision is not deterministic--no decision about
     *  predicting a unique alternative can be made.  Recall that an
     *  accept state is one in which a unique alternative is predicted.
     */
    protected int acceptStateReachable = DFA.REACHABLE_UNKNOWN;

    /** Rather than recheck every NFA configuration in a DFA state (after
     *  resolving) in findNewDFAStatesAndAddDFATransitions just check
     *  this boolean.  Saves a linear walk perhaps DFA state creation.
     *  Every little bit helps.
     */
    protected boolean resolvedWithPredicates = false;

    /** Build up the hash code for this state as NFA configurations
     *  are added as it's monotonically increasing list of configurations.
     */
    protected int cachedHashCode;

    /** The set of NFA configurations (state,alt,context) for this DFA state */
    protected Set nfaConfigurations = new HashSet();

    /** Used to prevent the closure operation from looping to itself and
     *  hence looping forever.  Sensitive to the NFA state, the alt, and
     *  the context.
     */
    protected Set closureBusy = new HashSet();

    /** As this state is constructed (i.e., as NFA states are added), we
     *  can easily check for non-epsilon transitions because the only
     *  transition that could be a valid label is transition(0).  When we
     *  process this node eventually, we'll have to walk all states looking
     *  for all possible transitions.  That is of the order: size(label space)
     *  times size(nfa states), which can be pretty damn big.  It's better
     *  to simply track possible labels.
     *  This is type List<Label>.
     */
    protected OrderedHashMap reachableLabels = new OrderedHashMap();

    public DFAState(DFA dfa) {
        this.dfa = dfa;
    }

    public Transition transition(int i) {
        return (Transition)transitions.get(i);
    }

    public int getNumberOfTransitions() {
        return transitions.size();
    }

    public void addTransition(Transition t) {
        transitions.add(t);
    }

    public void addTransition(DFAState target, Label label) {
        transitions.add( new Transition(label, target) );
    }

    public List getTransitions() {
        return transitions;
    }

    /** Add an NFA configuration to this DFA node.  Add uniquely
     *  an NFA state/alt/syntactic&semantic context (chain of invoking state(s)
     *  and semantic predicate contexts).
     *
     *  I don't see how there could be two configurations with same
     *  state|alt|synCtx and different semantic contexts because the
     *  semantic contexts are computed along the path to a particular state
     *  so those two configurations would have to have the same predicate.
     *  Nonetheless, the addition of configurations is unique on all
     *  configuration info.  I guess I'm saying that syntactic context
     *  implies semantic context as the latter is computed according to the
     *  former.
     *
     *  As we add configurations to this DFA state, track the set of all possible
     *  transition labels so we can simply walk it later rather than doing a
     *  loop over all possible labels in the NFA.
     */
    public void addNFAConfiguration(NFAState state,
                                    int alt,
                                    NFAContext context,
                                    SemanticContext semanticContext) {
        NFAConfiguration c = new NFAConfiguration(state.getStateNumber(),
                                                  alt,
                                                  context,
                                                  semanticContext);
        if ( nfaConfigurations.contains(c) ) {
            return;
        }

        nfaConfigurations.add(c);

        // update hashCode; for some reason using context.hashCode() also
        // makes the GC take like 70% of the CPU and this is slow!
        cachedHashCode += c.state + c.alt;

        // update reachableLabels
        if ( state.transition(0)!=null ) {
            Label label = state.transition(0).getLabel();
            if ( !(label.isEpsilon()||label.isSemanticPredicate()) ) {
                if ( state.transition(1)==null ) {
                    c.singleAtomTransitionEmanating = true;
                }
                addReachableLabel(label);
            }
        }
    }

    /** Add label uniquely and disjointly; intersection with
     *  another set or int/char forces breaking up the set(s).
     *
     *  Example, if reachable list of labels is [a..z, {k,9}, 0..9],
     *  the disjoint list will be [{a..j,l..z}, k, 9, 0..8].
     *
     *  As we add NFA configurations to a DFA state, we might as well track
     *  the set of all possible transition labels to make the DFA conversion
     *  more efficient.  W/o the reachable labels, we'd need to check the
     *  whole vocabulary space (could be 0..\uFFFF)!  The problem is that
     *  labels can be sets, which may overlap with int labels or other sets.
     *  As we need a deterministic set of transitions from any
     *  state in the DFA, we must make the reachable labels set disjoint.
     *  This operation amounts to finding the character classes for this
     *  DFA state whereas with tools like flex, that need to generate a
     *  homogeneous DFA, must compute char classes across all states.
     *  We are going to generate DFAs with heterogeneous states so we
     *  only care that the set of transitions out of a single state are
     *  unique. :)
     *
     *  The idea for adding a new set, t, is to look for overlap with the
     *  elements of existing list s.  Upon overlap, replace
     *  existing set s[i] with two new disjoint sets, s[i]-t and s[i]&t.
     *  (if s[i]-t is nil, don't add).  The remainder is t-s[i], which is
     *  what you want to add to the set minus what was already there.  The
     *  remainder must then be compared against the i+1..n elements in s
     *  looking for another collision.  Each collision results in a smaller
     *  and smaller remainder.  Stop when you run out of s elements or
     *  remainder goes to nil.  If remainder is non nil when you run out of
     *  s elements, then add remainder to the end.
     *
     *  Single element labels are treated as sets to make the code uniform.
     */
    protected void addReachableLabel(Label label) {
        //System.out.println("addReachableLabel: "+label.getSet().toString(dfa.getNFA().getGrammar()));
        if ( reachableLabels.containsKey(label) ) { // exact label present
            return;
        }
        IntSet t = label.getSet();
        IntSet remainder = t; // remainder starts out as whole set to add
        int n = reachableLabels.size(); // only look at initial elements
        // walk the existing list looking for the collision
        for (int i=0; i<n; i++) {
            Label rl = (Label)reachableLabels.get(i);
            /*
            if ( label.equals(rl) ) {
                // OPTIMIZATION:
                // exact label already here, just return; previous addition
                // would have made everything unique/disjoint
                return;
            }
            */
            IntSet s_i = rl.getSet();
            IntSet intersection = s_i.and(t);
            /*
            System.out.println(label.toString(dfa.getNFA().getGrammar())+" & "+
                    rl.toString(dfa.getNFA().getGrammar())+"="+
                    intersection.toString(dfa.getNFA().getGrammar()));
            */
            if ( intersection.isNil() ) {
                continue;
            }

            // For any (s_i, t) with s_i&t!=nil replace with (s_i-t, s_i&t)
            // (ignoring s_i-t if nil; don't put in list)

            // Replace existing s_i with intersection since we
            // know that will always be a non nil character class
            reachableLabels.set(i, new Label(intersection));

            // Compute s_i-t to see what is in current set and not in incoming
            IntSet existingMinusNewElements = s_i.subtract(t);
            if ( !existingMinusNewElements.isNil() ) {
                // found a new character class, add to the end (doesn't affect
                // outer loop duration due to n computation a priori.
                Label newLabel = new Label(existingMinusNewElements);
                reachableLabels.add(newLabel);
            }

            /*
            System.out.println("after collision, " +
                    "reachableLabels="+reachableLabels.toString());
            */

            // anything left to add to the reachableLabels?
            remainder = t.subtract(s_i);
            if ( remainder.isNil() ) {
                break; // nothing left to add to set.  done!
            }

            t = remainder;
        }
        if ( !remainder.isNil() ) {
            Label newLabel = new Label(remainder);
            reachableLabels.add(newLabel);
        }
    }

    public OrderedHashMap getReachableLabels() {
        return reachableLabels;
    }

    public Set getNFAConfigurations() {
        return this.nfaConfigurations;
    }

    public void setNFAConfigurations(Set configs) {
        this.nfaConfigurations = configs;
    }

    /*
    public IntSet getNFAStates() {
        int numTokens = dfa.getNFA().getGrammar().getNumberOfTokens();
        IntSet nfaStates = new org.antlr.misc.BitSet(numTokens+1);
        for (int i = 0; i < nfaConfigurations.size(); i++) {
            NFAConfiguration c = (NFAConfiguration)nfaConfigurations.get(i);
            nfaStates.add(c.state);
        }
        return nfaStates;
    }
    */

    /** A decent hash for a DFA state is the sum of the NFA state/alt pairs.
     *  This is used when we add DFAState objects to the DFA.states Map and
     *  when we compare DFA states.
     */
    public int hashCode() {
        return cachedHashCode;
        /*
        int hash = 0;
        Iterator iter = this.nfaConfigurations.iterator();
        while (iter.hasNext()) {
            NFAConfiguration myConfig = (NFAConfiguration) iter.next();
            hash = hash + myConfig.state + myConfig.alt;
        }
        return hash;
        */
    }

    /** Two DFAStates are equal if their NFA state/alt pairs are the same.
     *  This method is used to see if a DFA state already exists.  At first
     *  I was only checking that their NFA state subsets were the same, but
     *  test "a : (A|B)+ B;" resulting in a loopback DFA with two states
     *  containing the same NFA subsets but with different alts.  The DFA
     *  erroneously treated the states as identical.
     *
     *  Walk this list, adding state+alt to a hashset.  Walk other list
     *  and check against hashset.
     *
     *  Because the number of alternatives and number of NFA states are
     *  finite, there is a finite number of DFA states that can be processed.
     *  This is necessary to show that the algorithm terminates.
     */
    public boolean equals(Object o) {
        DFAState other = (DFAState)o;
        if ( o==null ) {
            return false;
        }
        if ( this.hashCode()!=other.hashCode() ) {
            return false;
        }
        Set myPairs = new HashSet();
        Iterator iter = this.nfaConfigurations.iterator();
        while (iter.hasNext()) {
            NFAConfiguration myConfig = (NFAConfiguration) iter.next();
            myPairs.add(myConfig.state+"|"+myConfig.alt);
        }
        iter = other.nfaConfigurations.iterator();
        while (iter.hasNext()) {
            NFAConfiguration theirConfig = (NFAConfiguration) iter.next();
            if ( !myPairs.contains(theirConfig.state+"|"+theirConfig.alt) ) {
                return false;
            }
        }
        return true;
    }

    /** Walk each configuration and if they are all the same alt, return
     *  that alt else return NFA.INVALID_ALT_NUMBER.  Ignore resolved
     *  configurations, but don't ignore resolveWithPredicate configs
     *  because this state should not be an accept state.  We need to add
     *  this to the work list and then have semantic predicate edges
     *  emanating from it.
     */
    public int getUniquelyPredictedAlt() {
        int alt = NFA.INVALID_ALT_NUMBER;
        Iterator iter = nfaConfigurations.iterator();
        NFAConfiguration configuration;
        while (iter.hasNext()) {
            configuration = (NFAConfiguration) iter.next();
            // ignore anything we resolved; predicates will still result
            // in transitions out of this state, so must count those
            // configurations; i.e., don't ignore resolveWithPredicate configs
            if ( configuration.resolved ) {
                continue;
            }
            if ( alt==NFA.INVALID_ALT_NUMBER ) {
                alt = configuration.alt; // found first nonresolved alt
            }
            else if ( configuration.alt!=alt ) {
                return NFA.INVALID_ALT_NUMBER;
            }
        }
        return alt;
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
    public void resolveNonDeterminisms() {
        Set nondeterministicAlts;

        // AMBIGUOUS EOT (if |alts|>1 and EOT state, resolve)
        // TODO: not sure if this grab first element is right?  why not all?
        Set alts = getAltSet();
        NFAConfiguration anyConfig;
        Iterator itr = nfaConfigurations.iterator(); // grab any config
        anyConfig = (NFAConfiguration)itr.next();
        NFAState anyState = dfa.getNFA().getState(anyConfig.state);
        if ( alts.size()>1 && anyState.isEOTState() ) {
            nondeterministicAlts = alts;
        }
        else {
            nondeterministicAlts = getNondeterministicAlts();
        }

        if ( nondeterministicAlts.size()==0 ) {
            return; // no problems, return
        }
        System.err.println("alts "+nondeterministicAlts+" nondeterministic for (d="+
                dfa.getDecisionNumber()+"): "+
                dfa.getNFADecisionStartState().getDescription());

        // ATTEMPT TO RESOLVE WITH SEMANTIC PREDICATES

        boolean done = resolveWithSemanticPredicates(nondeterministicAlts);
        if ( done ) {
            resolvedWithPredicates = true;
            return;
        }
        System.err.println("nondeterminism NOT resolved with sem preds");
        
        // RESOLVE SYNTACTIC CONFLICT BY REMOVING ALL BUT MIN ALT

        Iterator iter = nondeterministicAlts.iterator();
        int min = Integer.MAX_VALUE;
        while (iter.hasNext()) {
            Integer altI = (Integer) iter.next();
            int alt = altI.intValue();
            if ( alt < min ) {
                min = alt;
            }
        }
        // remove the one we pick to resolve conflict yielding list to turn off
        nondeterministicAlts.remove(new Integer(min));

        // turn off all states associated with alts other than the good one
        iter = nfaConfigurations.iterator();
        NFAConfiguration configuration;
        while (iter.hasNext()) {
            configuration = (NFAConfiguration) iter.next();
            if ( nondeterministicAlts.contains(new Integer(configuration.alt)) ) {
                configuration.resolved = true;
            }
        }
        //System.out.println("after resolution: "+this.toString());
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
    protected boolean resolveWithSemanticPredicates(Set nondeterministicAlts) {
        Map altToPredMap =
                getPredicatesPerNonDeterministicAlt(nondeterministicAlts);

        if ( altToPredMap.size()==0 ) {
            return false;
        }

        System.out.println("nondeterministic alts with predicates: "+altToPredMap);

        if ( nondeterministicAlts.size()-altToPredMap.size()>1 ) {
            System.err.println("alts with predicates insufficient to resolve");
            System.out.println("alts with predicates insufficient to resolve");
            return false;
        }

        // Handle case where 1 predicate is missing
        if ( altToPredMap.size()==nondeterministicAlts.size()-1 ) {
            // if there are n-1 predicates for n nondeterministic alts, can fix
            BitSet ndSet = BitSet.of(nondeterministicAlts);
            BitSet predSet = BitSet.of(altToPredMap);
            int nakedAlt = ndSet.subtract(predSet).getSingleElement();
            System.out.println("nake alt="+nakedAlt);
            // pretend naked alternative is covered with a predicate too
            SemanticContext unionOfPredicatesFromAllAlts =
                    getUnionOfPredicates(altToPredMap);
            System.out.println("all predicates "+unionOfPredicatesFromAllAlts);
            SemanticContext notOtherPreds =
                    SemanticContext.not(unionOfPredicatesFromAllAlts);
            altToPredMap.put(new Integer(nakedAlt), notOtherPreds);
            // set all config with alt=nakedAlt to have NOT of all
            // predicates on other alts
            Iterator iter = nfaConfigurations.iterator();
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
            Iterator iter = nfaConfigurations.iterator();
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

    /** Walk each NFA configuration in this DFA state looking for a conflict
     *  where (s|i|ctx) and (s|j|ctx) exist, indicating that state s with
     *  context ctx predicts alts i and j.  Return an Integer set of the
     *  alternative numbers that conflict.
     *
     *  Use a hash table to record state/ctx pairs as they are encountered.
     *  When the same pair is seen again, the alt number must be the same
     *  to avoid a conflict.
     */
    protected Set getNondeterministicAlts() {
        Set nondeterministicAlts = new HashSet();

        Iterator iter = nfaConfigurations.iterator();
        Map statePlusContextToAltMap = new HashMap();
        NFAConfiguration configuration;
        while (iter.hasNext()) {
            configuration = (NFAConfiguration) iter.next();
            String key = configuration.state + configuration.context.toString();
            NFAConfiguration previous =
                    (NFAConfiguration)statePlusContextToAltMap.get(key);
            if ( previous==null ) {
                // new state + context pair; no previousAlt defined; define it
                statePlusContextToAltMap.put(key,configuration);
            }
            else {
                // state + context pair exists, check to ensure previousAlt is same
                if ( configuration.alt!=previous.alt ) {
                    /*
                    System.out.println("config "+key+" ambig for alts "+
                            configuration.alt+","+previous.alt);
                    */
                    // these alts are in conflict; record this fact
                    nondeterministicAlts.add(new Integer(previous.alt));
                    nondeterministicAlts.add(new Integer(configuration.alt));
                }
            }
        }
        return nondeterministicAlts;
    }

    /** Get the set of all alts mentioned by all NFA configurations in this
     *  DFA state.
     */
    protected Set getAltSet() {
        Set alts = new HashSet();
        Iterator iter = nfaConfigurations.iterator();
        NFAConfiguration configuration;
        while (iter.hasNext()) {
            configuration = (NFAConfiguration) iter.next();
            alts.add(new Integer(configuration.alt));
        }
        return alts;
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
    protected Map getPredicatesPerNonDeterministicAlt(Set nondeterministicAlts) {
        // map alt to combined SemanticContext
        Map altToPredicateContextMap = new HashMap();
        // track tautologies like p1||true
        Set altToIncompletePredicateContextMap = new HashSet();
        Iterator iter = nfaConfigurations.iterator();
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
        if ( altToIncompletePredicateContextMap.size() == 0 ) {
            System.err.println("alts incompletely covered with predicate(s): "+
                altToIncompletePredicateContextMap);
        }

        // remove any predicates from incompletely covered alts
        iter = altToIncompletePredicateContextMap.iterator();
        while (iter.hasNext()) {
            Integer alt = (Integer) iter.next();
            altToPredicateContextMap.remove(alt);
        }

        return altToPredicateContextMap;
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
     *      2$ and then, after visiting a state, q, whose followState
     *      is 6, return to state 5 which we already know can get to q
     *      and back to 5.  Infinite recursion.  More formally, if the
     *      state of the NFA may proceed from
     *
     *      p|alpha$ ->+ p|beta p alpha$
     *
     *      then the closure has detected an epsilon cycle in the NFA
     *      derived from left-recursion.
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
     *  consumed any input.  A clear example is an NFA state that
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
    public boolean closureIsBusy(NFAState p,
                                 int alt,
                                 NFAContext context,
                                 SemanticContext semContext)
    {
        // case (1) : epsilon cycle (same state, same context)
        NFAConfiguration c =
                new NFAConfiguration(p.getStateNumber(),
                        alt,
                        context,
                        semContext);
        if ( closureBusy.contains(c) ) {
            return true;
        }

        // case (2) : left recursion (same state, state visited before)
        if ( context.contains(p.getStateNumber()) ) {
            return true;
        }
        return false;
    }

    public void setClosureIsBusy(NFAState p,
                                 int alt,
                                 NFAContext context,
                                 SemanticContext semContext)
    {
        NFAConfiguration c =
                new NFAConfiguration(p.getStateNumber(),
                                     alt,
                                     context,
                                     semContext);
        closureBusy.add(c);
    }

    /** Is an accept state reachable from this state? */
    public int getAcceptStateReachable() {
        return acceptStateReachable;
    }

    public void setAcceptStateReachable(int acceptStateReachable) {
        this.acceptStateReachable = acceptStateReachable;
    }

    public boolean isResolvedWithPredicates() {
        return resolvedWithPredicates;
    }

    /** Print all NFA states plus what alts they predict */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getStateNumber()+":{");
        Iterator iter = nfaConfigurations.iterator();
        int i = 1;
        while (iter.hasNext()) {
            NFAConfiguration configuration = (NFAConfiguration) iter.next();
            if ( i>1 ) {
                buf.append(", ");
            }
            buf.append(configuration);
            i++;
        }
        buf.append("}");
        return buf.toString();
    }
}
