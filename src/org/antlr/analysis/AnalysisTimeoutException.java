package org.antlr.analysis;

/** Analysis took too long; bail out of entire DFA construction. */
public class AnalysisTimeoutException extends RuntimeException {
	public DFA abortedDFA;
	public AnalysisTimeoutException(DFA abortedDFA) {
		this.abortedDFA = abortedDFA;
	}
}
