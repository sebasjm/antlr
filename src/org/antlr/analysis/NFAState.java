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

import org.antlr.tool.GrammarAST;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/** A state within an NFA. At most 2 transitions emanate from any NFA state. */
public class NFAState extends State {
    public static final int MAX_TRANSITIONS = 2;

    /** How many transitions; 0, 1, or 2 transitions */
    int numTransitions = 0;
    Transition[] transition = new Transition[MAX_TRANSITIONS];

    /** Which NFA are we in? */
    NFA nfa = null;

    /** What's its decision number from 1..n? */
    protected int decisionNumber = 0;

	/** Subrules (...)* and (...)+ have more than one decision point in
	 *  the NFA created for them.  They both have a loop-exit-or-stay-in
	 *  decision node (the loop back node).  They both have a normal
	 *  alternative block decision node at the left edge.  The (...)* is
	 *  worst as it even has a bypass decision (2 alts: stay in or bypass)
	 *  node at the extreme left edge.  This is not how they get generated
	 *  in code as a while-loop or whatever deals nicely with either.  For
	 *  error messages (where I need to print the nondeterministic alts)
	 *  and for interpretation, I need to use the single DFA that is created
	 *  (for efficiency) but interpret the results differently depending
	 *  on which of the 2 or 3 decision states uses the DFA.  For example,
	 *  the DFA will always report alt 1 as the exit branch so I need to
	 *  translate that depending on the decision state.
	 *
	 *  For the actual loop-back NFA state, the altTranslationMap is
	 *  an identity translation.
	 *
	 *  If decisionNumber>0 then this map tells you how to translate the
	 *  alt number coming from the DFA.  Alt n for n alts is returned as
	 *  the exit branch.
	 */
	//protected int[] altTranslationMap;

	/** What rule do we live in?  I currently only set on rule start/stop states */
	protected String enclosingRule;

    /** During debugging and for nondeterminism warnings, it's useful
     *  to know what relationship this node has to the original grammar.
     *  For example, "start of alt 1 of rule a".
     */
    protected String description;

    /** Associate this NFAState with the corresponding GrammarAST node
     *  from which this node was created.  This is useful not only for
     *  associating the eventual lookahead DFA with the associated
     *  Grammar position, but also for providing users with
     *  nondeterminism warnings.
     *
     *  Only decision states (such as block start or loop back states)
     *  have this value set.
     */
    protected GrammarAST decisionASTNode;

    /** Is this state the sole target of an EOT transition? */
    protected boolean EOTState = false;

	/** Jean Bovet needs in the GUI to know which state pairs correspond
	 *  to the start/stop of a block.
 	 */
	public int endOfBlockStateNumber = State.INVALID_STATE_NUMBER;

    public NFAState(NFA nfa) {
        this.nfa = nfa;
    }

    public int getNumberOfTransitions() {
        return numTransitions;
    }

    public void addTransition(Transition e) {
        if ( numTransitions>transition.length ) {
            throw new IllegalArgumentException("You can only have "+transition.length+" transitions");
        }
        if ( e!=null ) {
            transition[numTransitions] = e;
            numTransitions++;
        }
    }

	/** Used during optimization to reset a state to have the (single)
	 *  transition another state has.
	 */
	public void setTransition0(Transition e) {
		transition[0] = e;
		transition[1] = null;
		numTransitions = 1;
	}

    public Transition transition(int i) {
        return transition[i];
    }

    /*
	public List getTransitions() {
        List t = new ArrayList();
        t.add(transition[0]);
        t.add(transition[1]);
        return t;
    }
	*/

    // Setter/Getters

    public GrammarAST getDecisionASTNode() {
        return decisionASTNode;
    }

    /** What AST node is associated with this NFAState?  When you
	 *  set the AST node, I set the node to point back to this NFA state.
	 */
	public void setDecisionASTNode(GrammarAST decisionASTNode) {
		/*
		System.out.println("setting ast "+
						   nfa.grammar.grammarTreeToString(decisionASTNode)+
						   " AST="+decisionASTNode.toStringTree()+
						   " for "+this.toString());
		*/
		decisionASTNode.setNFAStartState(this);
		this.decisionASTNode = decisionASTNode;
	}

	public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDecisionNumber() {
        return decisionNumber;
    }

    public void setDecisionNumber(int decisionNumber) {
        this.decisionNumber = decisionNumber;
    }

	public void setEnclosingRuleName(String rule) {
		this.enclosingRule = rule;
	}

	public String getEnclosingRule() {
		return enclosingRule;
	}

    public boolean isEOTState() {
        return EOTState;
    }

    public void setEOTState(boolean eot) {
        EOTState = eot;
    }

	public String toString() {
		return String.valueOf(stateNumber);
	}

}

