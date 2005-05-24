package org.antlr.tool;

public class GrammarReporter {
	public Grammar grammar;
	public GrammarReporter(Grammar grammar) {
		this.grammar = grammar;
	}

	protected void computeStats() {
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
        buf.append("ANTLR Grammar Report");
		buf.append('\n');
		buf.append("Grammar: ");
		buf.append(grammar.name);
		buf.append('\n');
		buf.append("Type: ");
		buf.append(Grammar.grammarTypeToString[grammar.type]);
		buf.append('\n');
		buf.append("Rules: ");
		buf.append(grammar.getRules().size());
		buf.append('\n');
		buf.append("Decisions: ");
		buf.append(grammar.getNumberOfDecisions());
		buf.append('\n');
		buf.append("Cyclic DFA decisions: ");
		buf.append(grammar.getNumberOfCyclicDecisions());
		buf.append('\n');
		int numACyclicDecisions =
			grammar.getNumberOfDecisions()-grammar.getNumberOfCyclicDecisions();
		int[] depths = new int[numACyclicDecisions];
		int[] acyclicDFAStates = new int[numACyclicDecisions];
		int[] cyclicDFAStates = new int[grammar.getNumberOfCyclicDecisions()];
		int acyclicIndex = 0;
		int cyclicIndex = 0;
		for (int i=1; i<=grammar.getNumberOfDecisions(); i++) {
			Grammar.Decision d = grammar.getDecision(i);
			if ( !d.dfa.isCyclic() ) {
				int maxk = d.dfa.getMaxLookaheadDepth();
				depths[acyclicIndex] = maxk;
				acyclicDFAStates[acyclicIndex] = d.dfa.getNumberOfStates();
				acyclicIndex++;
			}
			else {
				cyclicDFAStates[cyclicIndex] = d.dfa.getNumberOfStates();
				cyclicIndex++;
			}
		}
		buf.append("Min fixed k: "); buf.append(min(depths));
		buf.append('\n');
		buf.append("Max fixed k: "); buf.append(max(depths));
		buf.append('\n');
		buf.append("Average fixed k: "); buf.append(avg(depths));
		buf.append('\n');
		buf.append("Standard deviation of fixed k: "); buf.append(stddev(depths));
		buf.append('\n');
		buf.append("Min acyclic DFA states: "); buf.append(min(acyclicDFAStates));
		buf.append('\n');
		buf.append("Max acyclic DFA states: "); buf.append(max(acyclicDFAStates));
		buf.append('\n');
		buf.append("Average acyclic DFA states: "); buf.append(avg(acyclicDFAStates));
		buf.append('\n');
		buf.append("Standard deviation of acyclic DFA states: "); buf.append(stddev(acyclicDFAStates));
		buf.append('\n');
		buf.append("Min cyclic DFA states: "); buf.append(min(cyclicDFAStates));
		buf.append('\n');
		buf.append("Max cyclic DFA states: "); buf.append(max(cyclicDFAStates));
		buf.append('\n');
		buf.append("Average cyclic DFA states: "); buf.append(avg(cyclicDFAStates));
		buf.append('\n');
		buf.append("Standard deviation of cyclic DFA states: "); buf.append(stddev(cyclicDFAStates));
		buf.append('\n');
		buf.append("Vocabulary size: ");
		buf.append(grammar.getTokenTypes().size());
		buf.append('\n');
		return buf.toString();
	}

	/** Compute the sample (unbiased estimator) standard deviation following:
	 *
	 *  Computing Deviations: Standard Accuracy
	 *  Tony F. Chan and John Gregg Lewis
	 *  Stanford University
	 *  Communications of ACM September 1979 of Volume 22 the ACM Number 9
	 *
	 *  The "two-pass" method from the paper; supposed to have better
	 *  numerical properties than the textbook summation/sqrt.  To me
	 *  this looks like the textbook method, but I ain't no numerical
	 *  methods guy.
	 */
	public static double stddev(int[] X) {
		int m = X.length;
		double xbar = avg(X);
		double s2 = 0.0;
		for (int i=0; i<m; i++){
			s2 += (X[i] - xbar)*(X[i] - xbar);
		}
		s2 = s2/(m-1);
		return Math.sqrt(s2);
	}

	/** Compute the sample mean */
	public static double avg(int[] X) {
		double xbar = 0.0;
		int m = X.length;
		for (int i=0; i<m; i++){
			xbar += X[i];
		}
		return xbar / m;
	}

	public static int min(int[] X) {
		int min = Integer.MAX_VALUE;
		int m = X.length;
		for (int i=0; i<m; i++){
			if ( X[i] < min ) {
				min = X[i];
			}
		}
		return min;
	}

	public static int max(int[] X) {
		int max = Integer.MIN_VALUE;
		int m = X.length;
		for (int i=0; i<m; i++){
			if ( X[i] > max ) {
				max = X[i];
			}
		}
		return max;
	}
}
