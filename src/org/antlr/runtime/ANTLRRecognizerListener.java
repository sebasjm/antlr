package org.antlr.runtime;

public interface ANTLRRecognizerListener {
// TODO: how can we get the debugger to single step?  These all report stuff *after* the fact not before it's about to match
	public void enterRule(String ruleName);
	public void exitRule(String ruleName);
	public void matchElement(int type);

	public void mismatchedElement(String msg);
	public void noViableAlt(String msg);

/*
	public void consume(int type);
	public void LT(int type);
	public void LA(int type);

	public void location(int line, int pos);
*/
}
