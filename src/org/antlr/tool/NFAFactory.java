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
package org.antlr.tool;

import org.antlr.misc.IntervalSet;
import org.antlr.misc.IntSet;
import org.antlr.tool.Grammar;
import org.antlr.analysis.*;

import java.util.List;
import java.util.Iterator;

import antlr.collections.AST;

/** Routines to construct StateClusters from EBNF grammar constructs.
 *  No optimization is done to remove unnecessary epsilon edges.
 */
public class NFAFactory {
	/** This factory is attached to a specifc NFA that it is building.
     *  The NFA will be filled up with states and transitions.
     */
	NFA nfa = null;

    /** Used to assign state numbers */
    protected int stateCounter = 0;

	public NFAFactory(NFA nfa) {
        nfa.setFactory(this);
		this.nfa = nfa;
	}

    public NFAState newState() {
        NFAState n = new NFAState(nfa);
        int state = stateCounter;
        n.setStateNumber(state);
        stateCounter++;
        nfa.addState(n);
        return n;
    }

    public int getNumberOfStates() {
        return stateCounter;
    }


	/** From label A build Graph o-A->o */
	public StateCluster build_Atom(int label) {
		NFAState left = newState();
		NFAState right = newState();
		transitionBetweenStates(left, right, label);
		StateCluster g = new StateCluster(left, right);
		return g;
	}

    /** From set build single edge graph o->o-set->o.  To conform to
     *  what an alt block looks like, must have extra state on left.
     */
	public StateCluster build_Set(IntSet set) {
        //NFAState start = newState();
        NFAState left = newState();
        //transitionBetweenStates(start, left, Label.EPSILON);
        NFAState right = newState();
        Transition e = new Transition(new Label(set),right);
        left.addTransition(e);
        StateCluster g = new StateCluster(left, right);
        return g;
	}

    /** Can only complement block of simple alts; can complement build_Set()
     *  result, that is.  Get set and complement, replace old with complement.
     */
    public StateCluster build_AlternativeBlockComplement(StateCluster blk) {
        State s0 = blk.left();
        IntSet set = getCollapsedBlockAsSet(s0);
        if ( set!=null ) {
            // if set is available, then structure known and blk is a set
            set = set.complement();
            Label label = s0.transition(0).getTarget().transition(0).getLabel();
            label.setSet(set);
        }
        return blk;
    }

    public StateCluster build_Range(int a, int b) {
        NFAState left = newState();
        NFAState right = newState();
        Transition e = new Transition(new Label(IntervalSet.of(a,b)),right);
        left.addTransition(e);
        StateCluster g = new StateCluster(left, right);
        return g;
    }

	/** From char 'c' build StateCluster o-intValue(c)->o
	 *  can include unicode spec likes '\u0024' later.  Accepts
	 *  actual unicode 16-bit now, of course, by default.
	 */
	public StateCluster build_CharLiteralAtom(String charLiteral) {
        int c = Grammar.getCharValueFromLiteral(charLiteral);
        if ( charLiteral.charAt(1)=='\\' &&
             Character.toUpperCase(charLiteral.charAt(2))=='U' )
        {
            String unicodeChars = charLiteral.substring(3,charLiteral.length()-1);
            c = Integer.parseInt(unicodeChars, 16); // parse the unicode 16 bit
        }
		return build_Atom(c);
	}

	/** From char 'c' build StateCluster o-intValue(c)->o
	 *  can include unicode spec likes '\u0024' later.  Accepts
	 *  actual unicode 16-bit now, of course, by default.
     *  TODO not supplemental char clean!
	 */
	public StateCluster build_CharRange(String a, String b) {
		int from = Grammar.getCharValueFromLiteral(a);
		int to = Grammar.getCharValueFromLiteral(b);
		return build_Range(from, to);
	}

    /** For a non-lexer, just build a simple token reference atom.
     *  For a lexer, a string is a sequence of char to match.  That is,
     *  "fog" is treated as 'f' 'o' 'g' not as a single transition in
     *  the DFA.  Machine== o-'f'->o-'o'->o-'g'->o and has n+1 states
     *  for n characters.
     */
    public StateCluster build_StringLiteralAtom(String stringLiteral) {
        if ( nfa.getGrammar().getType()==Grammar.LEXER ) {
            // first remove the double-quotes
            stringLiteral = stringLiteral.substring(1,stringLiteral.length()-1);
            NFAState first = newState();
            NFAState last = null;
            NFAState prev = first;
            for (int i=0; i<stringLiteral.length(); i++) {
                // TODO: what about >16bit chars in strings?  UTF-16?
                int c = stringLiteral.charAt(i);
                NFAState next = newState();
                transitionBetweenStates(prev, next, c);
                prev = last = next;
            }
            return  new StateCluster(first, last);
        }

        // a simple token reference in non-Lexers
        int tokenType = nfa.getGrammar().getTokenType(stringLiteral);
        return build_Atom(tokenType);
    }

    /** For reference to rule r, build
     *
     *  o-e->(r)  o
     *
     *  where (r) is the start of rule r and the trailing o is not linked
     *  to from rule ref state directly (it's done thru the transition(0)
     *  RuleClosureTransition.
     *
     *  If the rule r is just a list of tokens, it's block will be just
     *  a set on an edge o->o->o-set->o->o->o, could inline it rather than doing
     *  the rule reference, but i'm not doing this yet as I'm not sure
     *  it would help much in the NFA->DFA construction.
     *
     *  TODO add to codegen: collapse alt blks that are sets into single matchSet
     */
    public StateCluster build_RuleRef(int ruleIndex, NFAState ruleStart) {
        /*
        System.out.println("building ref to rule "+ruleIndex+": "+
                nfa.getGrammar().getRuleName(ruleIndex));
        */
        NFAState left = newState();
        // left.setDescription("ref to "+ruleStart.getDescription());
        NFAState right = newState();
        // right.setDescription("NFAState following ref to "+ruleStart.getDescription());
        Transition e = new RuleClosureTransition(ruleIndex,ruleStart,right);
        left.addTransition(e);
        StateCluster g = new StateCluster(left, right);
        return g;
    }

    /** From an empty alternative build StateCluster o-e->o */
    public StateCluster build_Epsilon() {
        NFAState left = newState();
        NFAState right = newState();
        transitionBetweenStates(left, right, Label.EPSILON);
        StateCluster g = new StateCluster(left, right);
        return g;
    }

    /** Build what amounts to an epsilon transition with a semantic
     *  predicate action.  The pred is a pointer into the AST of
     *  the SEMPRED token.
     */
    public StateCluster build_SemanticPredicate(AST pred) {
        NFAState left = newState();
        NFAState right = newState();
        Transition e = new Transition(new Label(pred), right);
        left.addTransition(e);
        StateCluster g = new StateCluster(left, right);
        return g;
    }

    /** add an EOF transition to any rule end NFAState that points to nothing
     *  (i.e., for all those rules not invoked by another rule).  These
     *  are start symbols then.
     */
    public void build_EOFStates(List rules) {
        for (Iterator iterator = rules.iterator(); iterator.hasNext();) {
			Grammar.Rule r = (Grammar.Rule) iterator.next();
			String ruleName = r.getName();
			NFAState endNFAState = nfa.getGrammar().getRuleStopState(ruleName);
            // Is this rule a start symbol?  (no follow links)
            if ( endNFAState.transition(0)==null ) {
                // if so, then don't let algorithm fall off the end of
                // the rule, make it loop on EOF.
                build_EOFState(endNFAState);
            }
        }
    }

    /** set up an NFA NFAState that will yield eof tokens or,
     *  in the case of a lexer grammar, an EOT token when the conversion
     *  hits the end of a rule.
     */
    private void build_EOFState(NFAState endNFAState) {
        int label = Label.EOF;
        if ( nfa.getGrammar().getType()==Grammar.LEXER ) {
            label = Label.EOT;
        }
        // System.out.println("build "+nfa.getGrammar().getTokenName(label)+" loop on end of state "+endNFAState.getDescription());
        NFAState end = newState();
        end.setEOTState(true);
        Transition toEnd = new Transition(label, end);
        endNFAState.addTransition(toEnd);
    }

    /** From A B build A-e->B (that is, build an epsilon arc from right
     *  of A to left of B).
     *
     *  As a convenience, return B if A is null or return A if B is null.
     */
    public StateCluster build_AB(StateCluster A, StateCluster B) {
        if ( A==null ) {
            return B;
        }
        if ( B==null ) {
            return A;
        }
        transitionBetweenStates(A.right(), B.left(), Label.EPSILON);
        StateCluster g = new StateCluster(A.left(), B.right());
        return g;
    }

    /** From A|B|..|Z alternative block build
     *
     *  o->o-A->o->o (last NFAState is blockEndNFAState pointed to by all alts)
     *  |          ^
     *  o->o-B->o--|
     *  |          |
     *  ...        |
     *  |          |
     *  o->o-Z->o--|
     *
     *  So every alternative gets begin NFAState connected by epsilon
     *  and every alt right side points at a block end NFAState.  There is a
     *  new NFAState in the NFAState in the StateCluster for each alt plus one for the
     *  end NFAState.
     *
     *  Special case: only one alternative: don't make a block with alt
     *  begin/end.
     *
     *  Special case: if just a list of tokens/chars/sets, then collapse
     *  to a single edge'd o-set->o graph.
     *
     *  Set alt number (1..n) in the left-Transition NFAState.
     */
    public StateCluster build_AlternativeBlock(List alternativeStateClusters)
    {
        StateCluster result = null;
        if ( alternativeStateClusters==null || alternativeStateClusters.size()==0 ) {
            return null;
        }
        IntSet altsAsSet = null;

        // even if we can collapse for lookahead purposes, we will still
        // need to predict the alts of this subrule in case there are actions
        // etc...  This is the decision that is pointed to from the AST node
        // (always)
        NFAState prevAlternative = null; // tracks prev so we can link to next alt
        NFAState firstAlt = null;
        NFAState blockEndNFAState = newState();
        blockEndNFAState.setDescription("end block");
        int altNum = 1;
        for (Iterator iter = alternativeStateClusters.iterator(); iter.hasNext();) {
            StateCluster g = (StateCluster) iter.next();
            // add begin NFAState for this alt connected by epsilon
            NFAState left = newState();
            left.setDescription("alt "+altNum+" of ()");
            transitionBetweenStates(left, g.left(), Label.EPSILON);
            transitionBetweenStates(g.right(), blockEndNFAState, Label.EPSILON);
            // Are we the first alternative?
            if ( firstAlt==null ) {
                firstAlt = left; // track extreme left node of StateCluster
            }
            else {
                // if not first alternative, must link to this alt from previous
                transitionBetweenStates(prevAlternative, left, Label.EPSILON);
            }
            prevAlternative = left;
            altNum++;
        }

        if ( altsAsSet==null ) {
            // return StateCluster pointing representing entire block
            // Points to first alt NFAState on left, block end on right
            result = new StateCluster(firstAlt, blockEndNFAState);
        }

        return result;
    }

    /** From (A)? build either:
     *
	 *  o--A->o
	 *  |     ^
	 *  o---->|
     *
     *  or, if A is a block, just add an empty alt to the end of the block
     */
    public StateCluster build_Aoptional(StateCluster A) {
        StateCluster g = null;
        int n = nfa.getGrammar().getNumberOfAltsForDecisionNFA(A.left());
        if ( n==1 ) {
            // no decision, just wrap in an optional path
            NFAState emptyAlt = newState();
            NFAState realAlt = newState();
            realAlt.setDescription("only alt of ()? block");
            emptyAlt.setDescription("epsilon path of ()? block");
            NFAState blockEndNFAState = newState();
            blockEndNFAState.setDescription("end ()? block");
            transitionBetweenStates(realAlt, A.left(), Label.EPSILON);
            transitionBetweenStates(realAlt, emptyAlt, Label.EPSILON);
            transitionBetweenStates(emptyAlt, blockEndNFAState, Label.EPSILON);
            transitionBetweenStates(A.right(), blockEndNFAState, Label.EPSILON);
            g = new StateCluster(realAlt, blockEndNFAState);
        }
        else {
            // a decision block, add an empty alt
            NFAState lastRealAlt =
                    nfa.getGrammar().getNFAStateForAltOfDecision(A.left(), n);
            NFAState emptyAlt = newState();
            emptyAlt.setDescription("epsilon path of ()? block");
            transitionBetweenStates(lastRealAlt, emptyAlt, Label.EPSILON);
            transitionBetweenStates(emptyAlt, A.right(), Label.EPSILON);
            g = A; // return same block, but now with optional last path
        }

        return g;
    }

    /** From (A)+ build
	 *
     *     |---|    (Transition 2 from block end points at alt 1; follow is Transition 1)
	 *     v   |
     *  o->o-A-o->o
     *
     *  Meaning that the last NFAState in A points back to A's left Transition NFAState
     *  and we add a new begin/end NFAState.  A can be single alternative or
     *  multiple.
	 *
	 *  During analysis we'll call the follow link (transition 1) alt n+1 for
	 *  an n-alt A block.
     */
    public StateCluster build_Aplus(StateCluster A) {
        NFAState left = newState();
        NFAState blockEndNFAState = newState();
		// turn A's block end into a loopback (acts like alt 2)
		A.right().setDescription("()+ loopback");
        transitionBetweenStates(A.right(), blockEndNFAState, Label.EPSILON); // follow is Transition 1
		transitionBetweenStates(A.right(), A.left(), Label.EPSILON); // loop back Transition 2
		transitionBetweenStates(left, A.left(), Label.EPSILON);

        StateCluster g = new StateCluster(left, blockEndNFAState);
        return g;
    }

    /** From (A)* build
     *
	 *     |---|
	 *     v   |
	 *  o->o-A-o--o (Transition 2 from block end points at alt 1; follow is Transition 1)
     *  |         ^
     *  o---------| (optional branch is 2nd alt of optional block containing A+)
     *
     *  Meaning that the last (end) NFAState in A points back to A's
     *  left side NFAState and we add 3 new NFAStates (the
     *  optional branch is built just like an optional subrule).
     *  See the Aplus() method for more on the loop back Transition.
     *
     *  There are 2 or 3 decision points in a A*.  If A is not a block (i.e.,
     *  it only has one alt), then there are two decisions: the optional bypass
     *  and then loopback.  If A is a block of alts, then there are three
     *  decisions: bypass, loopback, and A's decision point.
     *
     *  Note that the optional bypass must be outside the loop as (A|B)* is
     *  not the same thing as (A|B|)+.
     *
     *  This is an accurate NFA representation of the meaning of (A)*, but
     *  for generating code, I don't need a DFA for the optional branch by
     *  virtue of how I generate code.  The exit-loopback-branch decision
     *  is sufficient to let me make an appropriate enter, exit, loop
     *  determination.  The antlr.codegen.g
     */
    public StateCluster build_Astar(StateCluster A) {
		NFAState realAlt = newState();
		realAlt.setDescription("enter loop path of ()* block");
        NFAState optionalAlt = newState();
        optionalAlt.setDescription("epsilon path of ()* block");
        NFAState blockEndNFAState = newState();
		// convert A's end block to loopback
		A.right().setDescription("()* loopback");
        // Transition 1 to actual block of stuff
        transitionBetweenStates(realAlt, A.left(), Label.EPSILON);
        // Transition 2 optional to bypass
        transitionBetweenStates(realAlt, optionalAlt, Label.EPSILON);
		transitionBetweenStates(optionalAlt, blockEndNFAState, Label.EPSILON);
        // Transition 1 of end block exits
        transitionBetweenStates(A.right(), blockEndNFAState, Label.EPSILON);
        // Transition 2 of end block loops
        transitionBetweenStates(A.right(), A.left(), Label.EPSILON);

        StateCluster g = new StateCluster(realAlt, blockEndNFAState);
        return g;
    }

    /** Build an NFA predictor for special rule called Tokens manually that
     *  predicts which token will succeed.  The refs to the rules are not
     *  RuleRefTransitions as I want DFA conversion to stop at the EOT
     *  transition on the end of each token, rather than return to Tokens rule.
     *  If I used normal build_alternativeBlock for this, the RuleRefTransitions
     *  would save return address went jumping away from Tokens rule.
     *
     *  All I do here is build n new states for n rules with an epsilon
     *  edge to the rule start states and then to the next state in the
     *  list:
     *
     *   o->(A)  (a state links to start of A and to next in list)
     *   |
     *   o->(B)
     *   |
     *   ...
     *   |
     *   o->(Z)
	 *
	 *  This is the NFA created for the artificial rule created in
	 *  Grammar.addArtificialMatchTokensRule().
     */
    public NFAState build_ArtificialMatchTokensRuleNFA() {
        int altNum = 1;
        NFAState firstAlt = null; // the start state for the "rule"
        NFAState prevAlternative = null;
        Iterator iter = nfa.getGrammar().getRules().iterator();
		// TODO: add a single decision node/state for good description
        while (iter.hasNext()) {
			Grammar.Rule r = (Grammar.Rule) iter.next();
            String ruleName = r.getName();
			String modifier = nfa.getGrammar().getRuleModifier(ruleName);
            if ( ruleName.equals(Grammar.TOKEN_RULENAME) ||
				 (modifier!=null &&
				  modifier.equals(Grammar.NONTOKEN_LEXER_RULE_MODIFIER)) )
			{
                continue; // don't loop to yourself or do nontoken rules
            }
            NFAState ruleStartState = nfa.getGrammar().getRuleStartState(ruleName);;
            NFAState left = newState();
            left.setDescription("alt "+altNum+" of artificial rule "+Grammar.TOKEN_RULENAME);
            transitionBetweenStates(left, ruleStartState, Label.EPSILON);
            // Are we the first alternative?
            if ( firstAlt==null ) {
                firstAlt = left; // track extreme top left node as rule start
            }
            else {
                // if not first alternative, must link to this alt from previous
                transitionBetweenStates(prevAlternative, left, Label.EPSILON);
            }
            prevAlternative = left;
            altNum++;
        }
        return firstAlt;
    }

    /** Build an atom with all unicode bits in its label */
    public StateCluster build_Wildcard() {
        NFAState left = newState();
        NFAState right = newState();
        Label label = null;
        if ( nfa.getGrammar().getType()==Grammar.LEXER ) {
            label = new Label(Label.ALLCHAR);
        }
        else {
            label = new Label(nfa.getGrammar().getTokenTypes()); 
        }
        Transition e = new Transition(label,right);
        left.addTransition(e);
        StateCluster g = new StateCluster(left, right);
        return g;
    }

    /** Given a list of StateClusters for a list of alts,
     *  determine if the block is just a list of tokens such as
     *  ("int" | "float" | "char"). (actions won't exist in NFA). In this case,
     *  return an IntSet.  This is analogous to char class compression
     *  for NFA->DFA conversion, but in grammars with rules such as
     *  what ANTLR accepts even for lexers, the programmer has implicitly
     *  told us what the char classes of interest are such as class "types"
     *  shown here.  Collapse what would normally be 3 edges in a DFA into
     *  a single DFA edge with a set.
     *
     *  If the alternative's single left->right edge is a set, this
     *  handles that too by including all elements of that set into this one.
     *  This should work recursively for subrules that ref subrules that are just
     *  sets. :)
     *
     *  Currently, you cannot have actions in the block.
     */
    public IntSet getBlockAsSet(List alts) {
        System.out.println("getBlockAsSet "+alts);
        // empty subrule?
        if ( alts==null || alts.size() <= 1) { // don't use set for 1 alt
            return null;
        }
        // The block must only contain alternatives with a single element,
        // where each element is a char, token, char range, or token range.
        // Look for o-A->o
        IntSet set = new IntervalSet();
        for (int i = 0; i < alts.size(); i++) {
            StateCluster alt = (StateCluster)alts.get(i);
            // if left doesn't point at right directly, can't be just a token
            if ( alt.left().transition(0).getTarget()!=alt.right() ) {
                return null;
            }
            Label label = alt.left().transition(0).getLabel();
            if ( label.isAtom() ) {
                set.add(label.getAtom());
            }
            else if ( label.isSet() ) {
                set.addAll(label.getSet());
            }
            else {
                return null;
            }
        }
        return set;
    }

    /** Given a collapsed block of alts (a set of atoms), pull out
     *  the set and return it.
     */
    protected IntSet getCollapsedBlockAsSet(State blk) {
        State s0 = blk;
        if ( s0!=null && s0.transition(0)!=null ) {
            State s1 = s0.transition(0).getTarget();
            if ( s1!=null && s1.transition(0)!=null ) {
                Label label = s1.transition(0).getLabel();
                if ( label.isSet() ) {
                    return label.getSet();
                }
            }
        }
        return null;
    }

    /** See if a rule is precisely a single set match:
     *  s0->s1->s2->set->s3->s4->s5
     *  If so, return the set, if not return null.
    protected IntSet ruleAsInlinedSetMatch(NFAState ruleStart) {
        State s0 = (NFAState)ruleStart;
        if ( s0!=null && s0.transition(0)!=null &&
             s0.transition(1)==null )
        {
            State s1 = s0.transition(0).getTarget();
            if ( s1!=null && s1.transition(0)!=null &&
                 s1.transition(1)==null )
            {
                State s2 = s1.transition(0).getTarget();
                if ( s2!=null && s2.transition(0)!=null &&
                     s2.transition(1)==null &&
                     s2.transition(0).getLabel().isSet() )
                {
                    State s3 = s2.transition(0).getTarget();
                    if ( s3!=null && s3.transition(0)!=null &&
                         s3.transition(1)==null )
                    {
                        State s4 = s3.transition(0).getTarget();
                        if ( s4!=null && s4.transition(0)!=null &&
                             s4.transition(1)==null )
                        {
                            State s5 = s4.transition(0).getTarget();
                            if ( s5!=null && s5.isAcceptState() ) {
                                // rule is definitely a simple single set match
                                return s2.transition(0).getLabel().getSet();
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
     */

    /** From an adajency matrix mapping state i to state j with a label
     *  (either epsilon, null, or a token/char), create a StateCluster.  This is
     *  really used for testing to see what the nfa.g grammar has built.
     *
     *  Returns a reference to the leftmost NFAState in the NFA.
     *
     *  For example, given a 4 state adjacency matrix:
     *
     *  StateCluster[0][1] = "e";  // use "e" to mean epsilon
     *  StateCluster[1][2] = "A";
     *  StateCluster[2][3] = "eof";
     *
     *  generate o--Ep-->o--A-->o--Ep-->o
     *
     *  Strings are looked up in the token table to convert to a integer
     *  label.
     *
     *  State 0 must the be start NFAState.
     *
     *  Note that there is either no Transition, 1 Transition (token or epsilon),
     *  or 2 Transitions (Transition 2 must be epsilon, Transition 1 can be token or epsilon).
     *  So, we know the Transition number from the label.
	 *
	 *  When there are two epsilon transfers, the lowest target state number makes
	 *  that transition Transition 1 (largest target then is Transition 2).
     */
    /*
    public NFAState build_NFAFromAdjacencyMatrix(String[][] adjacency) {
        int n = adjacency.length;
        NFAState states[] = new NFAState[n];

        // first, build all the states
        for (int i = 0; i < n; i++) {
            states[i] = newState();
        }

        // now add all the Transitions between the states
        for (int i = 0; i < n; i++) {
            String[] Transitions = adjacency[i];
            // find the (max of) two Transitions for state i
            Transition e1 = null;
            Transition e2 = null;
            for (int j = 0; j < n; j++) {
                // does an Transition exists from state i to state j
                if ( Transitions[j]==null ) {
                    continue;
                }


                // create Transition from i to j (just don't know if it's e1 or e2
                // emanating from i.
                Transition e = new Transition(getTokenType(Transitions[j]), states[j]);
                // System.out.println("found an Transition from "+i+" to "+j+": "+e);

                // if this is the first, make it e1
                if ( e1==null ) {
                    e1 = e;
                }
                else {
                    // ok, this is the second Transition.
                    // If existing e1 label is epsilon and this Transition
                    // is a real token, make this Transition e1
                    if ( e1.isEpsilon() && !e.isEpsilon() )
                    {
                        e2 = e1; // swap 'em
                        e1 = e;
                    }
                    else {
                        // e1 is a real label.  make this Transition e2
                        e2 = e;
                        if ( !e.isEpsilon() ) {
                            System.out.println("build_NFAFromAdjacencyMatrix: can't have more than one non-epsilon Transition");
                        }
                    }
                }
            }

            // now we have the properly ordered Transitions emanating from i
            // actually set them up
            states[i].addTransition(e1);
            states[i].addTransition(e2);
        }

        return states[0];
    }

    private int getTokenType(String transition) {
        int label;
        if ( transition.equals("eof") ) {
            return Label.EOF;
        }
        if ( transition.equals("e") ) {
			return Label.EPSILON;
		}
		if ( transition.startsWith("'") ) {
			// it's a character
			return transition.charAt(1);
		}

		// must be a token name, look it up to get int label
		int ttype = nfa.getGrammar().getTokenType(transition);
		return ttype;
    }
    */

	private void transitionBetweenStates(NFAState a, NFAState b, int label) {
		Transition e = new Transition(label,b);
		a.addTransition(e);
	}
}
