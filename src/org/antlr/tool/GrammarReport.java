package org.antlr.tool;

import java.util.Collection;
import java.util.Iterator;
import java.io.*;
import java.nio.channels.FileLock;
import java.nio.channels.FileChannel;

public class GrammarReport {
	/** Because I may change the stats, I need to track that for later
	 *  computations to be consistent.
	 */
	public static final String Version = "1";
	public static final String GRAMMAR_STATS_FILENAME = "grammar.stats";
	public static final String RUNTIME_STATS_FILENAME = "runtime.stats";

	public Grammar grammar;
	public static final String ANTLRWORKS_DIR = "antlrworks";

	public GrammarReport(Grammar grammar) {
		this.grammar = grammar;
	}

	/** Create a single-line stats report about this grammar suitable to
	 *  send to the notify page at antlr.org
	 */
	public String toNotifyString() {
		StringBuffer buf = new StringBuffer();
		buf.append(Version);
		buf.append('\t');
		buf.append(grammar.name);
		buf.append('\t');
		buf.append(Grammar.grammarTypeToString[grammar.type]);
		buf.append('\t');
		buf.append(grammar.getOption("language"));
		buf.append('\t');
		buf.append(grammar.getRules().size());
		buf.append('\t');
		int totalProductions = 0;
		Collection rules = grammar.getRules();
		for (Iterator it = rules.iterator(); it.hasNext();) {
			Rule r = (Rule) it.next();
			totalProductions += r.numberOfAlts;
		}
		buf.append(totalProductions);
		buf.append('\t');
		buf.append(grammar.getNumberOfDecisions());
		buf.append('\t');
		buf.append(grammar.getNumberOfCyclicDecisions());
		buf.append('\t');
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
		buf.append(min(depths));
		buf.append('\t');
		buf.append(max(depths));
		buf.append('\t');
		buf.append(avg(depths));
		buf.append('\t');
		buf.append(stddev(depths));
		buf.append('\t');
		buf.append(min(acyclicDFAStates));
		buf.append('\t');
		buf.append(max(acyclicDFAStates));
		buf.append('\t');
		buf.append(avg(acyclicDFAStates));
		buf.append('\t');
		buf.append(stddev(acyclicDFAStates));
		buf.append('\t');
		buf.append(sum(acyclicDFAStates));
		buf.append('\t');
		buf.append(min(cyclicDFAStates));
		buf.append('\t');
		buf.append(max(cyclicDFAStates));
		buf.append('\t');
		buf.append(avg(cyclicDFAStates));
		buf.append('\t');
		buf.append(stddev(cyclicDFAStates));
		buf.append('\t');
		buf.append(sum(cyclicDFAStates));
		buf.append('\t');
		buf.append(grammar.getTokenTypes().size());
		buf.append('\t');
		buf.append(grammar.DFACreationWallClockTimeInMS);
		return buf.toString();
	}

	/** Given a stats line suitable for sending to the antlr.org site,
	 *  return a human-readable version.  Return null if there is a
	 *  problem with the data.
	 */
	public String toString(String notifyStatsLine) {
        return null;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
        buf.append("ANTLR Grammar Report; Stats Version ");
		buf.append(Version);
		buf.append('\n');
		buf.append("Grammar: ");
		buf.append(grammar.name);
		buf.append('\n');
		buf.append("Type: ");
		buf.append(Grammar.grammarTypeToString[grammar.type]);
		buf.append('\n');
		buf.append("Target language: ");
		buf.append(grammar.getOption("language"));
		buf.append('\n');
		buf.append("Rules: ");
		buf.append(grammar.getRules().size());
		buf.append('\n');
		int totalProductions = 0;
		Collection rules = grammar.getRules();
		for (Iterator it = rules.iterator(); it.hasNext();) {
			Rule r = (Rule) it.next();
			totalProductions += r.numberOfAlts;
		}
		buf.append("Productions: ");
		buf.append(totalProductions);
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
		buf.append("Total acyclic DFA states: "); buf.append(sum(acyclicDFAStates));
		buf.append('\n');
		buf.append("Min cyclic DFA states: "); buf.append(min(cyclicDFAStates));
		buf.append('\n');
		buf.append("Max cyclic DFA states: "); buf.append(max(cyclicDFAStates));
		buf.append('\n');
		buf.append("Average cyclic DFA states: "); buf.append(avg(cyclicDFAStates));
		buf.append('\n');
		buf.append("Standard deviation of cyclic DFA states: "); buf.append(stddev(cyclicDFAStates));
		buf.append('\n');
		buf.append("Total cyclic DFA states: "); buf.append(sum(cyclicDFAStates));
		buf.append('\n');
		buf.append("Vocabulary size: ");
		buf.append(grammar.getTokenTypes().size());
		buf.append('\n');
		buf.append("DFA creation time in ms: ");
		buf.append(grammar.DFACreationWallClockTimeInMS);
		buf.append('\n');
		return buf.toString();
	}


	public void writeStats() {
		String data = this.toNotifyString(); // compute data line to write
		String absoluteFilename =
			System.getProperty("user.home")+File.separator+
			ANTLRWORKS_DIR+File.separator+
			GRAMMAR_STATS_FILENAME;
		File f = new File(absoluteFilename);
		File parent = f.getParentFile();
		parent.mkdirs(); // ensure parent dir exists
		// write file
		try {
			FileOutputStream fos = new FileOutputStream(f, true); // append
			// First, try to get a lock
			FileChannel channel = fos.getChannel();
			FileLock lock = channel.tryLock();
			if ( lock==null ) {
				// oops, somebody has the lock
				try {
					Thread.sleep(1000); // sleep 1 sec and try again
				}
				catch (InterruptedException ie) {
					; // who cares if we are interrupted
				}
				lock = channel.tryLock(); // try again
			}
			if ( lock!=null ) {
				BufferedOutputStream bos = new BufferedOutputStream(fos);
				PrintStream ps = new PrintStream(bos);
				ps.println(data);
				ps.close();
				// release the lock
				lock.release();
				channel.close();
			}
			else {
				ErrorManager.internalError("can't acquire lock on "+
										   absoluteFilename);
			}
			fos.close();
		}
		catch (IOException ioe) {
			ErrorManager.internalError("can't write stats to "+absoluteFilename,
									   ioe);
		}
	}

	// M I S C

	// note that these routines return 0.0 if no values exist in the X[]
	// which is not "correct", but it is useful so I don't generate NaN
	// in my output

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
		if ( m==0 ) {
			return 0;
		}
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
		if ( m==0 ) {
			return 0;
		}
		for (int i=0; i<m; i++){
			xbar += X[i];
		}
		if ( xbar>=0.0 ) {
			return xbar / m;
		}
		return 0.0;
	}

	public static int min(int[] X) {
		int min = Integer.MAX_VALUE;
		int m = X.length;
		if ( m==0 ) {
			return 0;
		}
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
		if ( m==0 ) {
			return 0;
		}
		for (int i=0; i<m; i++){
			if ( X[i] > max ) {
				max = X[i];
			}
		}
		return max;
	}

	public static int sum(int[] X) {
		int s = 0;
		int m = X.length;
		if ( m==0 ) {
			return 0;
		}
		for (int i=0; i<m; i++){
			s += X[i];
		}
		return s;
	}
}
