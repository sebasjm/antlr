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

    /** A decent hash for a DFA state is the sum of the NFA state/alt pairs.
     *  This is used when we add DFAState objects to the DFA.states Map and
     *  when we compare DFA states.
     */
    public int hashCode() {
        return cachedHashCode;
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

	/** When more than one alternative can match the same input, the first
	 *  alternative is chosen to resolve the conflict.  The other alts
	 *  are "turned off" by setting the "resolved" flag in the NFA
	 *  configurations.  Return the set of disabled alternatives.  For
	 *
	 *  a : A | A | A ;
	 *
	 *  this method returns {2,3} as disabled.
	 */
	public Set getDisabledAlternatives() {
		Set disabled = new LinkedHashSet();
		Iterator iter = nfaConfigurations.iterator();
		NFAConfiguration configuration;
		while (iter.hasNext()) {
			configuration = (NFAConfiguration) iter.next();
			if ( configuration.resolved ) {
				disabled.add(new Integer(configuration.alt));
			}
		}
		return disabled;
	}

	/** */
	public int getNumberOfEOTNFAStates() {
		int n = 0;
		Iterator iter = nfaConfigurations.iterator();
		NFAConfiguration configuration;
		while (iter.hasNext()) {
			configuration = (NFAConfiguration) iter.next();
			NFAState s = dfa.getNFA().getState(configuration.state);
			if ( s.isEOTState() ) {
				n++;
			}
		}
		return n;
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
		Set nondeterministicAlts = null; // only create if have to

		// If only 1 NFA conf then no way it can be nondeterministic;
		// save the overhead.  There are many o-a->o NFA transitions
		// and so we save a hash map and iterator creation for each
		// state.
		if ( nfaConfigurations.size()<=1 ) {
			return null;
		}
        Iterator iter = nfaConfigurations.iterator();
        Map statePlusContextToAltMap = new HashMap();
        NFAConfiguration configuration;
        while (iter.hasNext()) {
            configuration = (NFAConfiguration) iter.next();
            String key = new StringBuffer().append(configuration.state)
				.append(configuration.context.toString()).toString();
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
					if ( nondeterministicAlts==null ) {
						nondeterministicAlts = new HashSet();
					}
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
	public Set getAltSet() {
		Set alts = new HashSet();
		Iterator iter = nfaConfigurations.iterator();
		NFAConfiguration configuration;
		while (iter.hasNext()) {
			configuration = (NFAConfiguration) iter.next();
			alts.add(new Integer(configuration.alt));
		}
		return alts;
	}

	/** Get the set of all states mentioned by all NFA configurations in this
	 *  DFA state associated with alt.
	 */
	public Set getNFAStatesForAlt(int alt) {
		Set alts = new HashSet();
		Iterator iter = nfaConfigurations.iterator();
		NFAConfiguration configuration;
		while (iter.hasNext()) {
			configuration = (NFAConfiguration) iter.next();
			if ( configuration.alt == alt ) {
				alts.add(new Integer(configuration.state));
			}
		}
		return alts;
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
