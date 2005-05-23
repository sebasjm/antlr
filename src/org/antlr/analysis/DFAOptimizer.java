package org.antlr.analysis;

import org.antlr.tool.ANTLRParser;
import org.antlr.tool.Grammar;

import java.util.Set;
import java.util.HashSet;

/** A module to perform optimizations on DFAs.
 *
 *  For now only EBNF exit branches are removed.
 *
 *  I could more easily (and more quickly) do some optimizations (such as
 *  PRUNE_EBNF_EXIT_BRANCHES) during DFA construction, but then it
 *  messes up the determinism checking.  For example, it looks like
 *  loop exit branches are unreachable if you prune exit branches
 *  during DFA construction and before determinism checks.
 *
 *  In general, ANTLR's NFA->DFA->codegen pipeline seems very robust
 *  to me which I attribute to a uniform and consistent set of data
 *  structures.  Regardless of what I want to "say"/implement, I do so
 *  within the confines of, for example, a DFA.  The code generator
 *  can then just generate code--it doesn't have to do much thinking.
 *  Putting optimizations in the code gen code really starts to make
 *  it a spagetti factory (uh oh, now I'm hungry!).  The pipeline is
 *  very testable; each stage has well defined input/output pairs.
 *
 *  ### Optimization: PRUNE_EBNF_EXIT_BRANCHES
 *
 *  There is no need to test EBNF block exit branches.  Not only is it
 *  an unneeded computation, but counter-intuitively, you actually get
 *  better errors. You can report an error at the missing or extra
 *  token rather than as soon as you've figured out you will fail.
 *
 *  Imagine optional block "( DOT CLASS )? SEMI".  ANTLR generates:
 *
 *  int alt=0;
 *  if ( input.LA(1)==DOT ) {
 *      alt=1;
 *  }
 *  else if ( input.LA(1)==SEMI ) {
 *      alt=2;
 *  }
 *
 *  Clearly, since Parser.match() will ultimately find the error, we
 *  do not want to report an error nor do we want to bother testing
 *  lookahead against what follows the (...)?  We want to generate
 *  simply "should I enter the subrule?":
 *
 *  int alt=2;
 *  if ( input.LA(1)==DOT ) {
 *      alt=1;
 *  }
 *
 *  NOTE 1. Greedy loops cannot be optimized in this way.  For example,
 *  "(greedy=false:'x'|.)* '\n'".  You specifically need the exit branch
 *  to tell you when to terminate the loop as the same input actually
 *  predicts one of the alts (i.e., staying in the loop).
 *
 *  NOTE 2.  I do not optimize cyclic DFAs at the moment as it doesn't
 *  seem to work. ;)  I'll have to investigate later to see what work I
 *  can do on cyclic DFAs to make them have fewer edges.  Might have
 *  something to do with the EOT token.
 *
 *  ### Optimization: COLLAPSE_ALL_INCIDENT_EDGES
 *
 *  Done during DFA construction.  See method addTransition() in
 *  NFAToDFAConverter.
 *
 *  ### Optimization: MERGE_STOP_STATES
 *
 *  Done during DFA construction.  See addDFAState() in NFAToDFAConverter.
 */
public class DFAOptimizer {
	public static boolean PRUNE_EBNF_EXIT_BRANCHES = true;
	public static boolean COLLAPSE_ALL_PARALLEL_EDGES = true;
	public static boolean MERGE_STOP_STATES = true;

	/** Used by DFA state machine generator to avoid infinite recursion
	 *  resulting from cycles int the DFA.  This is a set of int state #s.
	 */
	protected Set visited = new HashSet();

    protected Grammar grammar;

    public DFAOptimizer(Grammar grammar) {
		this.grammar = grammar;
    }

	public void optimize() {
		// optimize each DFA in this grammar
		for (int decisionNumber=1;
			 decisionNumber<=grammar.getNumberOfDecisions();
			 decisionNumber++)
		{
			DFA dfa = grammar.getLookaheadDFA(decisionNumber);
			optimize(dfa);
		}
	}

	protected void optimize(DFA dfa) {
		/*
		System.out.println("Optimize DFA "+dfa.decisionNFAStartState.decisionNumber+
						   " num states="+dfa.getNumberOfStates());
		*/
		long start = System.currentTimeMillis();
		if ( PRUNE_EBNF_EXIT_BRANCHES ) {
			visited.clear();
			int decisionType =
				dfa.getNFADecisionStartState().getDecisionASTNode().getType();
			if ( dfa.isGreedy() && !dfa.isCyclic() &&
				 (decisionType==ANTLRParser.OPTIONAL ||
				 decisionType==ANTLRParser.EOB) )
			{
				optimizeExitBranches(dfa.startState);
			}
		}
		long stop = System.currentTimeMillis();
		//System.out.println("minimized in "+(int)(stop-start)+" ms");
    }

	protected void optimizeExitBranches(DFAState d) {
		Integer sI = new Integer(d.stateNumber);
		if ( visited.contains(sI) ) {
			return; // already visited
		}
		visited.add(sI);
		int nAlts = d.dfa.getNumberOfAlts();
		for (int i = 0; i < d.getNumberOfTransitions(); i++) {
			Transition edge = (Transition) d.transition(i);
			DFAState edgeTarget = ((DFAState)edge.target);
			/*
			System.out.println(d.stateNumber+"-"+
							   edge.label.toString(d.dfa.nfa.grammar)+"->"+
							   edgeTarget.stateNumber);
			*/
			if ( edgeTarget.isAcceptState() &&
				 edgeTarget.getUniquelyPredictedAlt()==nAlts)
			{
				/*
				System.out.println("ignoring transition "+i+" to max alt "+
								   d.dfa.getNumberOfAlts());
				*/
				d.removeTransition(i);
				i--; // back up one so that i++ of loop iteration stays at i
			}
			optimizeExitBranches(edgeTarget);
		}
	}
}
