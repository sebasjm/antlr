package org.antlr.runtime;

public interface ANTLRDebugInterface {
	public void enterRule(String ruleName);
	public void exitRule(String ruleName);
	public void matchElement(int type);

	public void mismatchedElement(MismatchedTokenException e);
	public void mismatchedSet(MismatchedSetException e);
	public void noViableAlt(NoViableAltException e);

/*
	public void consume(int type);
	public void LT(int type);
	public void LA(int type);

	public void location(int file, int line, int pos);
*/
}
