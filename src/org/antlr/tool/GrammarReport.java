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
package org.antlr.tool;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

public class GrammarReport {
	/** Because I may change the stats, I need to track that for later
	 *  computations to be consistent.
	 */
	public static final String Version = "1";
	public static final String GRAMMAR_STATS_FILENAME = "grammar.stats";
	public static final String ANTLRWORKS_DIR = "antlrworks";
	public static final int NUM_GRAMMAR_STATS = 32;

	public Grammar grammar;

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
			if( d.dfa==null ) {
				continue;
			}
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
		buf.append('\t');
		buf.append(grammar.numberOfSemanticPredicates);
		buf.append('\t');
		buf.append(grammar.numberOfManualLookaheadOptions);
		buf.append('\t');
		buf.append(grammar.setOfNondeterministicDecisionNumbers.size());
		buf.append('\t');
		buf.append(grammar.setOfNondeterministicDecisionNumbersResolvedWithPredicates.size());
		buf.append('\t');
		buf.append(grammar.numberOfDFAConversionsTerminatedEarly);
		buf.append('\t');
		buf.append(ErrorManager.getErrorState().errors);
		buf.append('\t');
		buf.append(ErrorManager.getErrorState().warnings);
		buf.append('\t');
		buf.append(ErrorManager.getErrorState().infos);
		return buf.toString();
	}

	/** Given a stats line suitable for sending to the antlr.org site,
	 *  return a human-readable version.  Return null if there is a
	 *  problem with the data.
	 */
	public String toString() {
        return toString(toNotifyString());
	}

	protected static String[] decodeReportData(String data) {
		String[] fields = new String[NUM_GRAMMAR_STATS];
		StringTokenizer st = new StringTokenizer(data, "\t");
		int i = 0;
		while ( st.hasMoreTokens() ) {
			fields[i] = st.nextToken();
			i++;
		}
		if ( i!=NUM_GRAMMAR_STATS ) {
			return null;
		}
		return fields;
	}

	public static String toString(String notifyDataLine) {
		String[] fields = decodeReportData(notifyDataLine);
		if ( fields==null ) {
			return null;
		}
		StringBuffer buf = new StringBuffer();
        buf.append("ANTLR Grammar Report; Stats Version ");
		buf.append(fields[0]);
		buf.append('\n');
		buf.append("Grammar: ");
		buf.append(fields[1]);
		buf.append('\n');
		buf.append("Type: ");
		buf.append(fields[2]);
		buf.append('\n');
		buf.append("Target language: ");
		buf.append(fields[3]);
		buf.append('\n');
		buf.append("Rules: ");
		buf.append(fields[4]);
		buf.append('\n');
		buf.append("Productions: ");
		buf.append(fields[5]);
		buf.append('\n');
		buf.append("Decisions: ");
		buf.append(fields[6]);
		buf.append('\n');
		buf.append("Cyclic DFA decisions: ");
		buf.append(fields[7]);
		buf.append('\n');
		buf.append("Min fixed k: "); buf.append(fields[8]);
		buf.append('\n');
		buf.append("Max fixed k: "); buf.append(fields[9]);
		buf.append('\n');
		buf.append("Average fixed k: "); buf.append(fields[10]);
		buf.append('\n');
		buf.append("Standard deviation of fixed k: "); buf.append(fields[11]);
		buf.append('\n');
		buf.append("Min acyclic DFA states: "); buf.append(fields[12]);
		buf.append('\n');
		buf.append("Max acyclic DFA states: "); buf.append(fields[13]);
		buf.append('\n');
		buf.append("Average acyclic DFA states: "); buf.append(fields[14]);
		buf.append('\n');
		buf.append("Standard deviation of acyclic DFA states: "); buf.append(fields[15]);
		buf.append('\n');
		buf.append("Total acyclic DFA states: "); buf.append(fields[16]);
		buf.append('\n');
		buf.append("Min cyclic DFA states: "); buf.append(fields[17]);
		buf.append('\n');
		buf.append("Max cyclic DFA states: "); buf.append(fields[18]);
		buf.append('\n');
		buf.append("Average cyclic DFA states: "); buf.append(fields[19]);
		buf.append('\n');
		buf.append("Standard deviation of cyclic DFA states: "); buf.append(fields[20]);
		buf.append('\n');
		buf.append("Total cyclic DFA states: "); buf.append(fields[21]);
		buf.append('\n');
		buf.append("Vocabulary size: ");
		buf.append(fields[22]);
		buf.append('\n');
		buf.append("DFA creation time in ms: ");
		buf.append(fields[23]);
		buf.append('\n');
		buf.append("Number of semantic predicates: ");
		buf.append(fields[24]);
		buf.append('\n');
		buf.append("Number of manual fixed lookahead k=value options: ");
		buf.append(fields[25]);
		buf.append('\n');
		buf.append("Number of nondeterministic decisions: ");
		buf.append(fields[26]);
		buf.append('\n');
		buf.append("Number of nondeterministic decisions resolved with predicates: ");
		buf.append(fields[27]);
		buf.append('\n');
		buf.append("Number of DFA conversions terminated early: ");
		buf.append(fields[28]);
		buf.append('\n');
		buf.append("Number of errors: ");
		buf.append(fields[29]);
		buf.append('\n');
		buf.append("Number of warnings: ");
		buf.append(fields[30]);
		buf.append('\n');
		buf.append("Number of infos: ");
		buf.append(fields[31]);
		buf.append('\n');
		return buf.toString();
	}

	public static void writeReport(String filename, String data) {
		String absoluteFilename = getAbsoluteFileName(filename);
		File f = new File(absoluteFilename);
		File parent = f.getParentFile();
		parent.mkdirs(); // ensure parent dir exists
		// write file
		try {
			FileOutputStream fos = new FileOutputStream(f, true); // append
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			PrintStream ps = new PrintStream(bos);
			ps.println(data);
			ps.close();
			bos.close();
			fos.close();
		}
		catch (IOException ioe) {
			ErrorManager.internalError("can't write stats to "+absoluteFilename,
									   ioe);
		}
	}

	public static String getAbsoluteFileName(String filename) {
		return System.getProperty("user.home")+File.separator+
					ANTLRWORKS_DIR+File.separator+
					filename;
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
		if ( m<=1 ) {
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
