package org.antlr.tool;

public interface InterpreterActions {
	public void enterRule(String ruleName);
	public void exitRule(String ruleName);
	public void matchElement(int type);
	public void mismatchedElement(String msg);
	public void noViableAlt(String msg);
}
