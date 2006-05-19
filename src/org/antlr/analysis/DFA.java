/*
 [The "BSD licence"]
 Copyright (c) 2005 Terence Parr
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

import org.antlr.tool.FASerializer;
import org.antlr.tool.Grammar;
import org.antlr.tool.GrammarAST;
import org.antlr.tool.Interpreter;
import org.antlr.Tool;
import org.antlr.runtime.IntStream;

import java.util.*;

/** A DFA (converted from a grammar's NFA).
 *  DFAs are used as prediction machine for alternative blocks in all kinds
 *  of recognizers (lexers, parsers, tree walkers).
 */
public class DFA {
	public static final int REACHABLE_UNKNOWN = -2;
	public static final int REACHABLE_BUSY = -1; // in process of computing
	public static final int REACHABLE_NO = 0;
	public static final int REACHABLE_YES = 1;

	/** Prevent explosion of DFA states during conversion. The max number
	 *  of states per alt in a single decision's DFA.
	 */
	public static final int MAX_STATES_PER_ALT_IN_DFA = 400;

	public static int MAX_TIME_PER_DFA_CREATION = 2*1000;

	/** What's the start state for this DFA? */
    public DFAState startState;

	/** This DFA is being built for which decision? */
	public int decisionNumber = 0;

    /** From what NFAState did we create the DFA? */
    public NFAState decisionNFAStartState;

    /** A set of all DFA states.  Maps hash of nfa configurations
     *  to the actual DFAState object.  We use this to detect
     *  existing DFA states.
     */
    protected Map configurationsToDFAStateMap = new HashMap();

	/** Maps the state number to the actual DFAState.  Use a Vector as it
	 *  grows automatically when I set the ith element.
	 */
	protected Vector states = new Vector();

	/** Unique state numbers */
	protected int stateCounter = 0;

	/** count only new states not states that were rejected as already present */
	protected int numberOfStates = 0;

	/** User specified max fixed lookahead.  If 0, nothing specified.  -1
	 *  implies we have not looked at the options table yet to set k.
	 */
	protected int user_k = -1;

	/** While building the DFA, track max lookahead depth if not cyclic */
	protected int max_k = -1;

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

	/** We only want one accept state per predicted alt; track here */
	protected DFAState[] altToAcceptState;

	protected int nAlts = 0;

    /** Which NFA are we converting (well, which piece of the NFA)? */
    public NFA nfa;

	protected NFAToDFAConverter nfaConverter;

	/** This probe tells you a lot about a decision and is useful even
	 *  when there is no error such as when a syntactic nondeterminism
	 *  is solved via semantic predicates.  Perhaps a GUI would want
	 *  the ability to show that.
	 */
	public DecisionProbe probe = new DecisionProbe(this);

	/** Track absolute time of the conversion so we can have a failsafe:
	 *  if it takes too long, then terminate.  Assume bugs are in the
	 *  analysis engine.
	 */
	protected long conversionStartTime;

	public DFA(int decisionNumber, NFAState decisionStartState) {
		this.decisionNumber = decisionNumber;
        this.decisionNFAStartState = decisionStartState;
        nfa = decisionStartState.nfa;
        nAlts = nfa.grammar.getNumberOfAltsForDecisionNFA(decisionStartState);
        //setOptions( nfa.grammar.getDecisionOptions(getDecisionNumber()) );
        initAltRelatedInfo();

		//long start = System.currentTimeMillis();
        nfaConverter = new NFAToDFAConverter(this);
		nfaConverter.convert(decisionStartState);

		// figure out if there are problems with decision
		verify();

		if ( !probe.isDeterministic() ||
			 probe.analysisAborted() ||
			 probe.analysisOverflowed() )
		{
			probe.issueWarnings();
		}
		//long stop = System.currentTimeMillis();
		//System.out.println("verify cost: "+(int)(stop-start)+" ms");

		if ( Tool.internalOption_PrintDFA ) {
			System.out.println("DFA d="+decisionNumber);
			FASerializer serializer = new FASerializer(nfa.grammar);
			String result = serializer.serialize(startState);
			System.out.println(result);
		}
    }

	public int predict(IntStream input) {
		Interpreter interp = new Interpreter(nfa.grammar, input);
		return interp.predict(this);
	}

	/** Add a new DFA state to this DFA if not already present.
     *  To force an acyclic, fixed maximum depth DFA, just always
	 *  return the incoming state.  By not reusing old states,
	 *  no cycles can be created.
     */
    protected DFAState addState(DFAState d) {
		if ( getUserMaxLookahead()>0 ) {
			configurationsToDFAStateMap.put(d,d);
			numberOfStates++;
			return d;
		}
		DFAState existing = (DFAState)configurationsToDFAStateMap.get(d);
		if ( existing != null ) {
			// already there...get the existing DFA state
			return existing;
		}

		// if not there, then add new state.
        configurationsToDFAStateMap.put(d,d);
        numberOfStates++;
		return d;
	}

	public void removeState(DFAState d) {
		DFAState it = (DFAState)configurationsToDFAStateMap.remove(d);
		// states.set(d.stateNumber, null);
		if ( it!=null ) {
			numberOfStates--;
		}
	}

	public Map getConfigurationsToDFAStateMap() {
		return configurationsToDFAStateMap;
	}

	public DFAState getState(int stateNumber) {
		return (DFAState)states.get(stateNumber);
	}

	public void setState(int stateNumber, DFAState d) {
		states.set(stateNumber, d);
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
        return cyclic && getUserMaxLookahead()==0;
    }

	/** Is this DFA derived from the NFA for the Tokens rule? */
	public boolean isTokensRuleDecision() {
		if ( nfa.grammar.type!=Grammar.LEXER ) {
			return false;
		}
		NFAState nfaStart = getNFADecisionStartState();
		NFAState TokensRuleStart =
			nfa.grammar.getRuleStartState(Grammar.ARTIFICIAL_TOKENS_RULENAME);
		NFAState TokensDecisionStart =
			(NFAState)TokensRuleStart.transition(0).target;
		return nfaStart == TokensDecisionStart;
	}

	/** The user may specify a max, acyclic lookahead for any decision.  No
	 *  DFA cycles are created when this value, k, is greater than 0.
	 */
	public int getUserMaxLookahead() {
		if ( user_k>=0 ) { // cache for speed
			return user_k;
		}
		GrammarAST blockAST = nfa.grammar.getDecisionBlockAST(decisionNumber);
		Integer kI = (Integer)blockAST.getOption("k");
		if ( kI==null ) {
			user_k=0;
			return 0;
		}
		user_k = kI.intValue();
		return user_k;
	}

	public void setUserMaxLookahead(int k) {
		this.user_k = k;
	}

	/** Return k if decision is LL(k) for some k else return max int */
	public int getMaxLookaheadDepth() {
		if ( isCyclic() ) {
			return Integer.MAX_VALUE;
		}
		return max_k;
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
		doesStateReachAcceptState(startState);
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
            DFAState edgeTarget = (DFAState)t.target;
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
			/*
			if ( d.getNumberOfTransitions()==0 ) {
				probe.reportDanglingState(d);
			}
			*/
            d.setAcceptStateReachable(REACHABLE_NO);
			reduced = false;
        }
        return anEdgeReachesAcceptState;
    }

    public NFAState getNFADecisionStartState() {
        return decisionNFAStartState;
    }

	public DFAState getAcceptState(int alt) {
		return altToAcceptState[alt];
	}

	public void setAcceptState(int alt, DFAState acceptState) {
		altToAcceptState[alt] = acceptState;
	}

	public int getDecisionNumber() {
        return decisionNFAStartState.getDecisionNumber();
    }

    /** What GrammarAST node (derived from the grammar) is this DFA
     *  associated with?  It will point to the start of a block or
     *  the loop back of a (...)+ block etc...
     */
    public GrammarAST getDecisionASTNode() {
        return decisionNFAStartState.getAssociatedASTNode();
    }

    public boolean isGreedy() {
		GrammarAST blockAST = nfa.grammar.getDecisionBlockAST(decisionNumber);
		String v = (String)blockAST.getOption("greedy");
		if ( v!=null && v.equals("false") ) {
			return false;
		}
        return true;
    }

    public DFAState newState() {
        DFAState n = new DFAState(this);
        n.stateNumber = stateCounter;
        stateCounter++;
		states.setSize(n.stateNumber+1);
		states.set(n.stateNumber, n); // track state num to state
        return n;
    }

	public int getNumberOfStates() {
		return numberOfStates;
	}

	public int getNumberOfAlts() {
		return nAlts;
	}

	public boolean analysisAborted() {
		return probe.analysisAborted();
	}

    protected void initAltRelatedInfo() {
        unreachableAlts = new LinkedList();
        for (int i = 1; i <= nAlts; i++) {
            unreachableAlts.add(new Integer(i));
        }
		altToAcceptState = new DFAState[nAlts+1];
    }

	public String toString() {
		FASerializer serializer = new FASerializer(nfa.grammar);
		String result = serializer.serialize(startState);
		return result;
	}

	/** EOT (end of token) is a label that indicates when the DFA conversion
	 *  algorithm would "fall off the end of a lexer rule".  It normally
	 *  means the default clause.  So for ('a'..'z')+ you would see a DFA
	 *  with a state that has a..z and EOT emanating from it.  a..z would
	 *  jump to a state predicting alt 1 and EOT would jump to a state
	 *  predicting alt 2 (the exit loop branch).  EOT implies anything other
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
	protected boolean reachableLabelsEOTCoexistsWithAllChar(OrderedHashSet labels)
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

