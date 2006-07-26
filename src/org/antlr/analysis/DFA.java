/*
 [The "BSD licence"]
 Copyright (c) 2005-2006 Terence Parr
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

import org.antlr.Tool;
import org.antlr.codegen.CodeGenerator;
import org.antlr.misc.IntervalSet;
import org.antlr.misc.IntSet;
import org.antlr.runtime.IntStream;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.*;

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
	public static final int MAX_STATES_PER_ALT_IN_DFA = 450;

	/** Set to 0 to not terminate early */
	public static int MAX_TIME_PER_DFA_CREATION = 1*1000;

	/** How many edges can each DFA state have before a "special" state
	 *  is created that uses IF expressions instead of a table?
	 */
	public static int MAX_STATE_TRANSITIONS_FOR_TABLE = 65534;

	/** What's the start state for this DFA? */
    public DFAState startState;

	/** This DFA is being built for which decision? */
	public int decisionNumber = 0;

    /** From what NFAState did we create the DFA? */
    public NFAState decisionNFAStartState;

	/** The printable grammar fragment associated with this DFA */
	public String description;

	/** A set of all uniquely-numbered DFA states.  Maps hash of DFAState
     *  to the actual DFAState object.  We use this to detect
     *  existing DFA states.  Map<DFAState,DFAState>.  Use Map so
	 *  we can get old state back (Set only allows you to see if it's there).
	 *  Not used during fixed k lookahead as it's a waste to fill it with
	 *  a dup of states array.
     */
    protected Map uniqueStates = new HashMap();

	/** Maps the state number to the actual DFAState.  Use a Vector as it
	 *  grows automatically when I set the ith element.  This contains all
	 *  states, but the states are not unique.  s3 might be same as s1 so
	 *  s3 -> s1 in this table.  This is how cycles occur.  If fixed k,
	 *  then these states will all be unique as states[i] always points
	 *  at state i when no cycles exist.
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

    /** Are there any loops in this DFA?
	 *  Computed by doesStateReachAcceptState()
	 */
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

	protected int nAlts = 0;

	/** We only want one accept state per predicted alt; track here */
	protected DFAState[] altToAcceptState;

	/** Track whether an alt discovers recursion for each alt during
	 *  NFA to DFA conversion; >1 alt with recursion implies nonregular.
	 */
	protected IntSet recursiveAltSet = new IntervalSet();

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

	/** Map an edge transition table to a unique set number; ordered so
	 *  we can push into the output template as an ordered list of sets
	 *  and then ref them from within the transition[][] table.  Like this
	 *  for C# target:
	 *     public static readonly DFA30_transition0 =
	 *     	new short[] { 46, 46, -1, 46, 46, -1, -1, -1, -1, -1, -1, -1,...};
	 *         public static readonly DFA30_transition1 =
	 *     	new short[] { 21 };
	 *      public static readonly short[][] DFA30_transition = {
	 *     	  DFA30_transition0,
	 *     	  DFA30_transition0,
	 *     	  DFA30_transition1,
	 *     	  ...
	 *      };
	 */
	public Map edgeTransitionClassMap = new LinkedHashMap();

	/** The unique edge transition class number; every time we see a new
	 *  set of edges emanating from a state, we number it so we can reuse
	 *  if it's every seen again for another state.  For Java grammar,
	 *  some of the big edge transition tables are seen about 57 times.
	 */
	protected int edgeTransitionClass =0;

	/* This DFA can be converted to a transition[state][char] table and
	 * the following tables are filled by createStateTables upon request.
	 * These are injected into the templates for code generation.
	 * See March 25, 2006 entry for description:
	 *   http://www.antlr.org/blog/antlr3/codegen.tml
	 * Often using Vector as can't set ith position in a List and have
	 * it extend list size; bizarre.
	 */

	/** List of special DFAState objects */
	public List specialStates;
	/** List of ST for special states. */
	public List specialStateSTs;
	public Vector accept;
	public Vector eot;
	public Vector eof;
	public Vector min;
	public Vector max;
	public Vector special;
	public Vector transition;
	/** just the Vector<Integer> indicating which unique edge table is at
	 *  position i.
	 */
	public Vector transitionEdgeTables; // not used by java yet
	protected int uniqueCompressedSpecialStateNum = 0;

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

		// must be after verify as it computes cyclic, needed by this routine
		resetStateNumbersToBeContiguous();

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

	/** Walk all states and reset their numbers to be a contiguous sequence
	 *  of integers starting from 0.  Only cyclic DFA can have unused positions
	 *  in states list.  State i might be identical to a previous state j and
	 *  will result in states[i] == states[j].  We don't want to waste a state
	 *  number on this.  Useful mostly for code generation in tables.
	 */
	public void resetStateNumbersToBeContiguous() {
		/*
		if ( !isCyclic() ) {
			return;
		}
		*/
		if ( getUserMaxLookahead()>0 ) {
			// all numbers are unique already; no states are thrown out.
			return;
		}
		int snum=0;
		/*
		if ( decisionNumber==30 ) {
			System.out.println("DFA :"+decisionNumber+" "+this);
			System.out.println("unique states: "+getUniqueStates());
			System.out.println("states: "+states);
			System.out.println("stateCounter="+stateCounter);
		}
		*/
		Map states = getUniqueStates();
		for (int i = 0; i <= getMaxStateNumber(); i++) {
			DFAState s = getState(i);
			// if valid and it's not already been renumbered
			if ( states.containsValue(s) && s.stateNumber>=i ) {
				// state i is a valid state, reset it's state number
				s.stateNumber = snum++; // rewrite state numbers to be 0..n-1
			}
		}
		if ( snum!=getNumberOfStates() ) {
			ErrorManager.internalError("DFA "+decisionNumber+": "+decisionNFAStartState.getDescription()+" max state num "+getNumberOfStates()+
				"!= max renumbered state "+snum);
		}
	}

	// JAVA-SPECIFIC Accessors!!!!!  It is so impossible to get arrays
	// or even consistently formatted strings acceptable to java that
	// I am forced to build the individual char elements here

	public List getJavaCompressedAccept() { return getRunLengthEncoding(accept); }
	public List getJavaCompressedEOT() { return getRunLengthEncoding(eot); }
	public List getJavaCompressedEOF() { return getRunLengthEncoding(eof); }
	public List getJavaCompressedMin() { return getRunLengthEncoding(min); }
	public List getJavaCompressedMax() { return getRunLengthEncoding(max); }
	public List getJavaCompressedSpecial() { return getRunLengthEncoding(special); }
	public List getJavaCompressedTransition() {
		if ( transition==null || transition.size()==0 ) {
			return null;
		}
		List encoded = new ArrayList(transition.size());
		// walk Vector<Vector<FormattedInteger>> which is the transition[][] table
		for (int i = 0; i < transition.size(); i++) {
			Vector transitionsForState = (Vector) transition.elementAt(i);
			encoded.add(getRunLengthEncoding(transitionsForState));
		}
		return encoded;
	}

	/** Compress the incoming data list so that runs of same number are
	 *  encoded as number,value pair sequences.  3 -1 -1 -1 28 is encoded
	 *  as 1 3 3 -1 1 28.  I am pretty sure this is the lossless compression
	 *  that GIF files use.  Transition tables are heavily compressed by
	 *  this technique.  I got the idea from JFlex http://jflex.de/
	 *
	 *  Return List<String> where each string is either \xyz for 8bit char
	 *  and \uFFFF for 16bit.  Hideous and specific to Java, but it is the
	 *  only target bad enough to need it.
	 */
	public static List getRunLengthEncoding(List data) {
		if ( data==null || data.size()==0 ) {
			// for states with no transitions we want an empty string ""
			// to hold its place in the transitions array.
			List empty = new ArrayList();
			empty.add("");
			return empty;
		}
		Integer negOneI = new Integer(-1);
		int size = Math.max(2,data.size()/2);
		List encoded = new ArrayList(size); // guess at size
		// scan values looking for runs
		int i = 0;
		while ( i < data.size() ) {
			Integer I = (Integer)data.get(i);
			if ( I==null ) {
				I = negOneI; // empty values become -1
			}
			// count how many v there are?
			int n = 0;
			for (int j = i; j < data.size(); j++) {
				Integer v = (Integer)data.get(j);
				if ( v==null ) {
					v = negOneI; // empty values become -1
				}
				if ( I.equals(v) ) {
					n++;
				}
				else {
					break;
				}
			}
			encoded.add(encodeIntAsCharEscape((char)n));
			encoded.add(encodeIntAsCharEscape((char)I.intValue()));
			i+=n;
		}
		return encoded;
	}

	public void createStateTables(CodeGenerator generator) {
		//System.out.println("createTables:\n"+this);

		description = getNFADecisionStartState().getDescription();
		description =
			generator.target.getTargetStringLiteralFromString(description);

		// create all the tables
		special = new Vector(this.getNumberOfStates()); // Vector<short>
		special.setSize(this.getNumberOfStates());
		specialStates = new ArrayList();				// List<DFAState>
		specialStateSTs = new ArrayList();				// List<ST>
		accept = new Vector(this.getNumberOfStates()); // Vector<int>
		accept.setSize(this.getNumberOfStates());
		eot = new Vector(this.getNumberOfStates()); // Vector<int>
		eot.setSize(this.getNumberOfStates());
		eof = new Vector(this.getNumberOfStates()); // Vector<int>
		eof.setSize(this.getNumberOfStates());
		min = new Vector(this.getNumberOfStates()); // Vector<int>
		min.setSize(this.getNumberOfStates());
		max = new Vector(this.getNumberOfStates()); // Vector<int>
		max.setSize(this.getNumberOfStates());
		transition = new Vector(this.getNumberOfStates()); // Vector<Vector<int>>
		transition.setSize(this.getNumberOfStates());
		transitionEdgeTables = new Vector(this.getNumberOfStates()); // Vector<Vector<int>>
		transitionEdgeTables.setSize(this.getNumberOfStates());

		// for each state in the DFA, fill relevant tables.
		Iterator it = null;
		if ( getUserMaxLookahead()>0 ) {
			it = states.iterator();
		}
		else {
			it = getUniqueStates().values().iterator();
		}
		while ( it.hasNext() ) {
			DFAState s = (DFAState)it.next();
			// init EOT/EOF tables; need to be -1
			/*
			eot.set(s.stateNumber, new Integer(-1));
			eof.set(s.stateNumber, new Integer(-1));
			*/
			if ( s.isAcceptState() ) {
				// can't compute min,max,special,transition on accepts
				accept.set(s.stateNumber,
						   new Integer(s.getUniquelyPredictedAlt()));
				/*
				special.set(s.stateNumber, new Integer(-1)); // not special
				min.set(s.stateNumber, new Integer(0));
				max.set(s.stateNumber, new Integer(0));
				*/
			}
			else {
				//accept.set(s.stateNumber, new Integer(0)); // doesn't predict
				createMinMaxTables(s);
				createTransitionTableEntryForState(s);
				createSpecialTable(generator, s);
				createEOTTable(s);
			}
		}

		// now that we have computed list of specialStates, gen code for 'em
		for (int i = 0; i < specialStates.size(); i++) {
			DFAState ss = (DFAState) specialStates.get(i);
			StringTemplate stateST =
				generator.generateSpecialState(ss);
			specialStateSTs.add(stateST);
		}

		// check that the tables are not messed up by encode/decode
		/*
		testEncodeDecode(min);
		testEncodeDecode(max);
		testEncodeDecode(accept);
		testEncodeDecode(special);
		System.out.println("min="+min);
		System.out.println("max="+max);
		System.out.println("accept="+accept);
		System.out.println("special="+special);
		System.out.println("transition="+transition);
		*/
	}

	/*
	private void testEncodeDecode(List data) {
		//System.out.println("data="+data);
		List encoded = getRunLengthEncoding(data);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < encoded.size(); i++) {
			String I = (String)encoded.get(i);
			int v = 0;
			if ( I.startsWith("\\u") ) {
				v = Integer.parseInt(I.substring(2,I.length()), 16);
			}
			else {
				v = Integer.parseInt(I.substring(1,I.length()), 8);
			}
			buf.append((char)v);
		}
		String encodedS = buf.toString();
		short[] decoded = DFA_.unpackEncodedString(encodedS);
		//System.out.println("decoded:");
		for (int i = 0; i < decoded.length; i++) {
			short x = decoded[i];
			if ( x!=((Integer)data.get(i)).intValue() ) {
				System.err.println("problem with encoding");
			}
			//System.out.print(", "+x);
		}
		//System.out.println();
	}
*/

	protected void createMinMaxTables(DFAState s) {
		int smin = Label.MAX_CHAR_VALUE + 1;
		int smax = Label.MIN_ATOM_VALUE - 1;
		for (int j = 0; j < s.getNumberOfTransitions(); j++) {
			Transition edge = (Transition) s.transition(j);
			Label label = edge.label;
			if ( label.isAtom() ) {
				if ( label.getAtom()>=Label.MIN_CHAR_VALUE ) {
					if ( label.getAtom()<smin ) {
						smin = label.getAtom();
					}
					if ( label.getAtom()>smax ) {
						smax = label.getAtom();
					}
				}
			}
			else if ( label.isSet() ) {
				IntervalSet labels = (IntervalSet)label.getSet();
				if ( labels.getMinElement()<smin ) {
					smin = labels.getMinElement();
				}
				if ( labels.getMaxElement()>smax ) {
					smax = labels.getMaxElement();
				}
			}
		}

		if ( smax<0 ) {
			// must be predicates or pure EOT transition; just zero out min, max
			smin = Label.MIN_CHAR_VALUE;
			smax = Label.MIN_CHAR_VALUE;
		}

		min.set(s.stateNumber, new Integer(smin));
		max.set(s.stateNumber, new Integer(smax));

		if ( smax<0 || smin>Label.MAX_CHAR_VALUE ) {
			ErrorManager.internalError("messed up: min="+min+", max="+max);
		}
	}

	protected void createTransitionTableEntryForState(DFAState s) {
		/*
		System.out.println("createTransitionTableEntryForState s"+s.stateNumber+
			" dec "+s.dfa.decisionNumber+" cyclic="+s.dfa.isCyclic());
		*/
		int smax = ((Integer)max.get(s.stateNumber)).intValue();
		int smin = ((Integer)min.get(s.stateNumber)).intValue();
		Vector stateTransitions = new Vector(smax-smin+1);
		stateTransitions.setSize(smax-smin+1);
		transition.set(s.stateNumber, stateTransitions);
		for (int j = 0; j < s.getNumberOfTransitions(); j++) {
			Transition edge = (Transition) s.transition(j);
			Label label = edge.label;
			if ( label.isAtom() && label.getAtom()>=Label.MIN_CHAR_VALUE ) {
				int labelIndex = label.getAtom()-smin; // offset from 0
				stateTransitions.set(labelIndex,
									 new Integer(edge.target.stateNumber));
			}
			else if ( label.isSet() ) {
				IntervalSet labels = (IntervalSet)label.getSet();
				int[] atoms = labels.toArray();
				for (int a = 0; a < atoms.length; a++) {
					int labelIndex = atoms[a]-smin; // offset from 0
					stateTransitions.set(labelIndex,
										 new Integer(edge.target.stateNumber));
				}
			}
		}
		// track unique state transition tables so we can reuse
		Integer edgeClass = (Integer)edgeTransitionClassMap.get(stateTransitions);
		if ( edgeClass!=null ) {
			//System.out.println("we've seen this array before; size="+stateTransitions.size());
			transitionEdgeTables.set(s.stateNumber, edgeClass);
		}
		else {
			/*
			if ( stateTransitions.size()>255 ) {
				System.out.println("edge edgeTable "+stateTransitions.size()+" s"+s.stateNumber+": "+new Integer(edgeTransitionClass));
			}
			else {
				System.out.println("stateTransitions="+stateTransitions);
			}
			*/
			edgeClass = new Integer(edgeTransitionClass);
			transitionEdgeTables.set(s.stateNumber, edgeClass);
			edgeTransitionClassMap.put(stateTransitions, edgeClass);
			edgeTransitionClass++;
		}
	}

	protected void createEOTTable(DFAState s) {
		for (int j = 0; j < s.getNumberOfTransitions(); j++) {
			Transition edge = (Transition) s.transition(j);
			Label label = edge.label;
			if ( label.isAtom() ) {
				if ( label.getAtom()==Label.EOT ) {
					// eot[s] points to accept state
					eot.set(s.stateNumber, new Integer(edge.target.stateNumber));
				}
				else if ( label.getAtom()==Label.EOF ) {
					// eof[s] points to accept state
					eof.set(s.stateNumber, new Integer(edge.target.stateNumber));
				}
			}
		}
	}

	protected void createSpecialTable(CodeGenerator generator, DFAState s) {
		// number all special states from 0...n-1 instead of their usual numbers
		boolean hasSemPred = false;

		// TODO this code is very similar to canGenerateSwitch.  Refactor to share
		for (int j = 0; j < s.getNumberOfTransitions(); j++) {
			Transition edge = (Transition) s.transition(j);
			Label label = edge.label;
			// can't do a switch if the edges have preds or are going to
			// require gated predicates
			if ( label.isSemanticPredicate() ||
				 ((DFAState)edge.target).getGatedPredicatesInNFAConfigurations()!=null)
			{
				hasSemPred = true;
				break;
			}
		}
		// if has pred or too big for table, make it special
		int smax = ((Integer)max.get(s.stateNumber)).intValue();
		int smin = ((Integer)min.get(s.stateNumber)).intValue();
		if ( hasSemPred || smax-smin>MAX_STATE_TRANSITIONS_FOR_TABLE ) {
			special.set(s.stateNumber,
						new Integer(uniqueCompressedSpecialStateNum));
			uniqueCompressedSpecialStateNum++;
			specialStates.add(s);
		}
		else {
			special.set(s.stateNumber, new Integer(-1)); // not special
		}
	}

	public static String encodeIntAsCharEscape(int v) {
		if ( v<=127 ) {
			return "\\"+Integer.toOctalString(v);
		}
		String hex = Integer.toHexString(v|0x10000).substring(1,5);
		return "\\u"+hex;
	}

	public int predict(IntStream input) {
		Interpreter interp = new Interpreter(nfa.grammar, input);
		return interp.predict(this);
	}

	/** Add a new DFA state to this DFA if not already present.
     *  To force an acyclic, fixed maximum depth DFA, just always
	 *  return the incoming state.  By not reusing old states,
	 *  no cycles can be created.  If we're doing fixed k lookahead
	 *  don't updated uniqueStates, just return incoming state, which
	 *  indicates it's a new state.
     */
    protected DFAState addState(DFAState d) {
		//System.out.println("addState: "+d);
		if ( getUserMaxLookahead()>0 ) {
			return d;
		}
		// does a DFA state exist already with everything the same
		// except its state number?
		DFAState existing = (DFAState)uniqueStates.get(d);
		if ( existing != null ) {
			// already there...get the existing DFA state
			return existing;
		}

		// if not there, then add new state.
        uniqueStates.put(d,d);
        numberOfStates++;
		return d;
	}

	public void removeState(DFAState d) {
		DFAState it = (DFAState)uniqueStates.remove(d);
		if ( it!=null ) {
			numberOfStates--;
		}
	}

	public Map getUniqueStates() {
		return uniqueStates;
	}

	/** What is the max state number ever created?  This may be beyond
	 *  getNumberOfStates().
	 */
	public int getMaxStateNumber() {
		return states.size()-1;
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
	 *  If this decision has no k lookahead specified, then try the grammar.
	 */
	public int getUserMaxLookahead() {
		if ( user_k>=0 ) { // cache for speed
			return user_k;
		}
		GrammarAST blockAST = nfa.grammar.getDecisionBlockAST(decisionNumber);
		Object k = blockAST.getOption("k");
		if ( k==null ) {
			user_k = nfa.grammar.getGrammarMaxLookahead();
			return user_k;
		}
		if (k instanceof Integer) {
			Integer kI = (Integer)k;
			user_k = kI.intValue();
		}
		else {
			// must be String "*"
			if ( k.equals("*") ) {
				user_k = 0;
			}
		}
		return user_k;
	}

	public boolean getAutoBacktrackMode() {
		String autoBacktrack =
			(String)decisionNFAStartState.getAssociatedASTNode().getOption("backtrack");
		if ( autoBacktrack==null ) {
			autoBacktrack = (String)nfa.grammar.getOption("backtrack");
		}
		return autoBacktrack!=null&&autoBacktrack.equals("true");
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
		if ( !probe.nonRegularDecision ) {
			doesStateReachAcceptState(startState);
		}
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

	public String getDescription() {
		return description;
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
		if ( getUserMaxLookahead()>0 ) {
			// if using fixed lookahead then uniqueSets not set
			return states.size();
		}
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
		if ( startState==null ) {
			return "";
		}
		return serializer.serialize(startState);
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

