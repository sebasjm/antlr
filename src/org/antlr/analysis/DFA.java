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

import org.antlr.runtime.IntStream;
import org.antlr.tool.*;

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

    public static final int REACHABLE_UNKNOWN = -2;
    public static final int REACHABLE_BUSY = -1; // in process of computing
    public static final int REACHABLE_NO = 0;
    public static final int REACHABLE_YES = 1;

    /** Is this DFA reduced?  I.e., can all states lead to an accept state? */
    protected boolean reduced = true;

    /** Are there any loops in this DFA? */
    protected boolean cyclic = false;

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
	 *  Loops will always just do the right thing I believe.  The DFA
	 *  conversion will continue until it finds either a unique char sequence
	 *  that predicts exiting the loop or it will hit EOT, which acts like
	 *  a unique char.  The greedy=false just lets the DFA be smaller as it
	 *  can stop when it finds a DFA state with an EOT transition.
	 *
     *  This is a cached value of what's in the options table.
     */
    protected boolean greedy = true;

    /** Subrules have options that will apply to any decisions built from
     *  that subrule.  This points to the options created during antlr.g
     *  parsing and stored in the BLOCK node.
     */
    protected Map options;

	protected NFAToDFAConverter nfaConverter;

	/** This probe tells you a lot about a decision and is useful even
	 *  when there is no error such as when a syntactic nondeterminism
	 *  is solved via semantic predicates.  Perhaps a GUI would want
	 *  the ability to show that.
	 */
	protected DecisionProbe probe = new DecisionProbe(this);

    public DFA(NFAState decisionStartState) {
        this.decisionNFAStartState = decisionStartState;
        nfa = decisionStartState.getNFA();
        int nAlts =
            nfa.getGrammar().getNumberOfAltsForDecisionNFA(decisionStartState);
        setOptions( nfa.getGrammar().getDecisionOptions(getDecisionNumber()) );
        initUnreachableAlts(nAlts);

        nfaConverter = new NFAToDFAConverter(this);
		nfaConverter.convert(decisionStartState);

		// figure out if there are problems with decision
		verify();
		
		probe.reportErrors();
    }

	public int predict(IntStream input) {
		Interpreter interp = new Interpreter(nfa.getGrammar(), input);
		return interp.predict(this);
	}

	public void setStartState(DFAState startState) {
		this.startState = startState;
	}

	/** Add a new DFA state to this DFA if not already present.
     *  If the DFA state uniquely predicts a single alternative, it
     *  becomes a stop state; don't add to work list.  Further, if
     *  there exists an NFA state predicted by > 1 different alternatives
     *  and with the same syn and sem context, the DFA is nondeterministic for
     *  at least one input sequence reaching that NFA state.
     */
    protected DFAState addState(DFAState d) {
		DFAState existing = (DFAState)states.get(d);
		if ( existing != null ) {
			// already there...get the existing DFA state
			return existing;
		}

		// if not there, then add new state.
        states.put(d,d);
        numberOfStates++;
		return d;
	}

	/** Is the DFA reduced?  I.e., does every state have a path to an accept
     *  state?  If not, don't delete as we need to generate an error indicating
     *  which paths are "dead ends".  Also tracks list of alts with no accept
     *  state in the DFA.  Must call verify() first before this makes sense.
     */
    public boolean isReduced() {
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
     *  alts.  Must call verify() first before this makes sense.
     */
    public List getUnreachableAlts() {
        return unreachableAlts;
    }

	/** Once this DFA has been built, need to verify that:
	 *
	 *  1. it's reduced
	 *  2. all alts have an accept state
	 *
	 *  Elsewhere, in the NFA converter, we need to verify that:
	 *
	 *  3. alts i and j have disjoint lookahead if no sem preds
	 *  4. if sem preds, nondeterministic alts must be sufficiently covered
	 */
	public void verify() {
		doesStateReachAcceptState(getStartState());
	}

    /** figure out if this state eventually reaches an accept state and
     *  modify the instance variable 'reduced' to indicate if we find
     *  at least one state that cannot reach an accept state.  This implies
     *  that the overall DFA is not reduced.  This algorithm should be
     *  linear in the number of DFA states.
     *
     *  The algorithm also tracks which alternatives have no accept state,
     *  indicating a nondeterminism.
	 *
	 *  Also computes whether the DFA is cyclic.
	 *
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
        // Visit every transition, track if at least one edge reaches stop state
		// Cannot terminate when we know this state reaches stop state since
		// all transitions must be traversed to set status of each DFA state.
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
			// target must be REACHABLE_UNKNOWN (i.e., unvisited)
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
			if ( d.getNumberOfTransitions()==0 ) {
				probe.reportDanglingState(d);
			}
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

	public NFAState getDecisionNFAStartState() {
		return decisionNFAStartState;
	}

    protected void initUnreachableAlts(int numberOfAlts) {
        unreachableAlts = new LinkedList();
        for (int i = 1; i <= numberOfAlts; i++) {
            unreachableAlts.add(new Integer(i));
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
 }

