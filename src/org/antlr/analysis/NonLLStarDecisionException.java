package org.antlr.analysis;

/** Used to abort DFA construction when we find non-LL(*) decision; i.e.,
 *  a decision that has recursion in more than a single alt.
 */
public class NonLLStarDecisionException extends RuntimeException {
	public DFA abortedDFA;
	public NonLLStarDecisionException(DFA abortedDFA) {
		this.abortedDFA = abortedDFA;
	}
}
