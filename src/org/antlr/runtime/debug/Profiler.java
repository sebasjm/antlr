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
package org.antlr.runtime.debug;

import org.antlr.runtime.*;
import org.antlr.tool.GrammarReport;

import java.util.StringTokenizer;

/** Using the debug event interface, track what is happening in the parser
 *  and record statistics about the runtime.
 */
public class Profiler implements DebugEventListener {
	/** Because I may change the stats, I need to track that for later
	 *  computations to be consistent.
	 */
	public static final String Version = "1";
	public static final String RUNTIME_STATS_FILENAME = "runtime.stats";
	public static final int NUM_RUNTIME_STATS = 20;

	//public IntStream input;

	public DebugParser parser = null;

	// working variables

	protected int level = 0;
	protected boolean inDecision = false;
	protected int maxLookaheadInDecision = 0;
	protected CommonToken lastTokenConsumed=null;

	// stats variables

	public int numRuleInvocations = 0;
	public int maxRuleInvocationDepth = 0;
	public int numFixedDecisions = 0;
	public int numCyclicDecisions = 0;
	public int[] decisionMaxFixedLookaheads = new int[200];
	public int[] decisionMaxCyclicLookaheads = new int[200];
	public int numHiddenTokens = 0;
	public int numCharsMatched = 0;
	public int numHiddenCharsMatched = 0;
	public int numSemanticPredicates = 0;
	protected int numberReportedErrors = 0;
	
	public Profiler() {
	}

	public Profiler(DebugParser parser) {
		this.parser = parser;
	}

	public void enterRule(String ruleName) {
		level++;
		numRuleInvocations++;
		if ( level>maxRuleInvocationDepth ) {
			maxRuleInvocationDepth = level;
		}
	}

	public void exitRule(String ruleName) {
		level--;
	}

	public void enterAlt(int alt) {;}
	public void enterSubRule(int d) {;}
	public void exitSubRule(int d) {;}

	public void enterDecision(int decisionNumber) {
		inDecision=true;
	}

	public void exitDecision(int decisionNumber) {
		if ( parser.isCyclicDecision ) {
			numCyclicDecisions++;
		}
		else {
			numFixedDecisions++;
		}
		inDecision=false;
		if ( parser.isCyclicDecision ) {
			// cyclic decisions use number of consumes + 1; we track consumes
			// so add one to the max lookahead.  For example, if it's LL(1)
			// it will use LA(1) but will not consume.
			maxLookaheadInDecision++;
			if ( numCyclicDecisions>=decisionMaxCyclicLookaheads.length ) {
				int[] bigger = new int[decisionMaxCyclicLookaheads.length*2];
				System.arraycopy(decisionMaxCyclicLookaheads,0,bigger,0,decisionMaxCyclicLookaheads.length);
				decisionMaxCyclicLookaheads = bigger;
			}
			decisionMaxCyclicLookaheads[numCyclicDecisions-1] = maxLookaheadInDecision;
		}
		else {
			if ( numFixedDecisions>=decisionMaxFixedLookaheads.length ) {
				int[] bigger = new int[decisionMaxFixedLookaheads.length*2];
				System.arraycopy(decisionMaxFixedLookaheads,0,bigger,0,decisionMaxFixedLookaheads.length);
				decisionMaxFixedLookaheads = bigger;
			}
			decisionMaxFixedLookaheads[numFixedDecisions-1] = maxLookaheadInDecision;
		}
		parser.isCyclicDecision = false;
		maxLookaheadInDecision = 0;
	}

	public void location(int line, int pos) {;}

	public void consumeToken(Token token) {
		if ( inDecision ) {
			if ( parser.isCyclicDecision ) {
				maxLookaheadInDecision++;
			}
		}
		lastTokenConsumed = (CommonToken)token;
	}

	public void consumeHiddenToken(Token token) {
		lastTokenConsumed = (CommonToken)token;
	}

	public void LT(int i, Token t) {
		if ( inDecision && !parser.isCyclicDecision ) {
			if ( i>maxLookaheadInDecision ) {
				maxLookaheadInDecision = i;
			}
		}
	}

	public void mark(int i) {;}
	public void rewind(int i) {;}

	public void beginBacktrack(int level) {
		// TODO: implement
	}

	public void endBacktrack(int level, boolean successful) {
		// TODO: implement
	}

	public void recognitionException(RecognitionException e) {
		numberReportedErrors++;
	}

	public void beginResync() {;}
	public void endResync() {;}

	public void semanticPredicate(boolean result, String predicate) {
		numSemanticPredicates++;
	}

	public void commence() {;}

	public void terminate() {
		GrammarReport.writeReport(RUNTIME_STATS_FILENAME,toNotifyString());
	}

	public void setParser(DebugParser parser) {
		this.parser = parser;
	}

	// R E P O R T I N G

	public String toNotifyString() {
		TokenStream input = parser.getTokenStream();
		for (int i=0; i<input.size()&&i<=lastTokenConsumed.getTokenIndex(); i++) {
			Token t = input.get(i);
			if ( t.getChannel()!=Token.DEFAULT_CHANNEL ) {
				numHiddenTokens++;
				numHiddenCharsMatched += t.getText().length();
			}
		}
		numCharsMatched = lastTokenConsumed.getStopIndex() + 1;
		decisionMaxFixedLookaheads = trim(decisionMaxFixedLookaheads, numFixedDecisions);
		decisionMaxCyclicLookaheads = trim(decisionMaxCyclicLookaheads, numCyclicDecisions);
		StringBuffer buf = new StringBuffer();
		buf.append(Version);
		buf.append('\t');
		buf.append(parser.getClass().getName());
		buf.append('\t');
		buf.append(numRuleInvocations);
		buf.append('\t');
		buf.append(maxRuleInvocationDepth);
		buf.append('\t');
		buf.append(numFixedDecisions);
		buf.append('\t');
		buf.append(GrammarReport.min(decisionMaxFixedLookaheads));
		buf.append('\t');
		buf.append(GrammarReport.max(decisionMaxFixedLookaheads));
		buf.append('\t');
		buf.append(GrammarReport.avg(decisionMaxFixedLookaheads));
		buf.append('\t');
		buf.append(GrammarReport.stddev(decisionMaxFixedLookaheads));
		buf.append('\t');
		buf.append(numCyclicDecisions);
		buf.append('\t');
		buf.append(GrammarReport.min(decisionMaxCyclicLookaheads));
		buf.append('\t');
		buf.append(GrammarReport.max(decisionMaxCyclicLookaheads));
		buf.append('\t');
		buf.append(GrammarReport.avg(decisionMaxCyclicLookaheads));
		buf.append('\t');
		buf.append(GrammarReport.stddev(decisionMaxCyclicLookaheads));
		buf.append('\t');
		buf.append(numSemanticPredicates);
		buf.append('\t');
		buf.append(parser.getTokenStream().size());
		buf.append('\t');
		buf.append(numHiddenTokens);
		buf.append('\t');
		buf.append(numCharsMatched);
		buf.append('\t');
		buf.append(numHiddenCharsMatched);
		buf.append('\t');
		buf.append(numberReportedErrors);
		return buf.toString();
	}

	public String toString() {
        return toString(toNotifyString());
	}

	protected static String[] decodeReportData(String data) {
		String[] fields = new String[NUM_RUNTIME_STATS];
		StringTokenizer st = new StringTokenizer(data, "\t");
		int i = 0;
		while ( st.hasMoreTokens() ) {
			fields[i] = st.nextToken();
			i++;
		}
		if ( i!=NUM_RUNTIME_STATS ) {
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
        buf.append("ANTLR Runtime Report; Profile Version ");
		buf.append(fields[0]);
		buf.append('\n');
		buf.append("parser name ");
		buf.append(fields[1]);
		buf.append('\n');
		buf.append("Number of rule invocations ");
		buf.append(fields[2]);
		buf.append('\n');
		buf.append("max rule invocation nesting depth ");
		buf.append(fields[3]);
		buf.append('\n');
		buf.append("number of fixed lookahead decisions ");
		buf.append(fields[4]);
		buf.append('\n');
		buf.append("min lookahead used in a fixed lookahead decision ");
		buf.append(fields[5]);
		buf.append('\n');
		buf.append("max lookahead used in a fixed lookahead decision ");
		buf.append(fields[6]);
		buf.append('\n');
		buf.append("average lookahead depth used in fixed lookahead decisions ");
		buf.append(fields[7]);
		buf.append('\n');
		buf.append("standard deviation of depth used in fixed lookahead decisions ");
		buf.append(fields[8]);
		buf.append('\n');
		buf.append("number of arbitrary lookahead decisions ");
		buf.append(fields[9]);
		buf.append('\n');
		buf.append("min lookahead used in an arbitrary lookahead decision ");
		buf.append(fields[10]);
		buf.append('\n');
		buf.append("max lookahead used in an arbitrary lookahead decision ");
		buf.append(fields[11]);
		buf.append('\n');
		buf.append("average lookahead depth used in arbitrary lookahead decisions ");
		buf.append(fields[12]);
		buf.append('\n');
		buf.append("standard deviation of depth used in arbitrary lookahead decisions ");
		buf.append(fields[13]);
		buf.append('\n');
		buf.append("number of evaluated semantic predicates ");
		buf.append(fields[14]);
		buf.append('\n');
		buf.append("number of tokens ");
		buf.append(fields[15]);
		buf.append('\n');
		buf.append("number of hidden tokens ");
		buf.append(fields[16]);
		buf.append('\n');
		buf.append("number of char ");
		buf.append(fields[17]);
		buf.append('\n');
		buf.append("number of hidden char ");
		buf.append(fields[18]);
		buf.append('\n');
		buf.append("number of syntax errors ");
		buf.append(fields[19]);
		buf.append('\n');
		return buf.toString();
	}

	protected int[] trim(int[] X, int n) {
		if ( n<X.length ) {
			int[] trimmed = new int[n];
			System.arraycopy(X,0,trimmed,0,n);
			X = trimmed;
		}
		return X;
	}

}
