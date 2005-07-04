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
package org.antlr.test;

import org.antlr.test.unit.TestSuite;
import org.antlr.test.unit.FailedAssertionException;
import org.antlr.tool.*;
import org.antlr.analysis.Label;
import org.antlr.Tool;

import java.util.*;
import java.io.StringReader;

import antlr.Token;

public class TestSymbolDefinitions extends TestSuite {

    /** Public default constructor used by TestRig */
    public TestSymbolDefinitions() {
    }

	public void testParserSimpleTokens() throws Exception {
		Grammar g = new Grammar(
				"parser grammar t;\n"+
				"a : A | B;\n" +
				"b : C ;");
		String rules = "a, b";
		String tokenNames = "A, B, C";
		checkSymbols(g, rules, tokenNames);
	}

	public void testParserTokensSection() throws Exception {
		Grammar g = new Grammar(
				"parser grammar t;\n" +
				"tokens {\n" +
				"  C;\n" +
				"  D;" +
				"}\n"+
				"a : A | B;\n" +
				"b : C ;");
		String rules = "a, b";
		String tokenNames = "A, B, C, D";
		checkSymbols(g, rules, tokenNames);
	}

	public void testLexerTokensSection() throws Exception {
		Grammar g = new Grammar(
				"lexer grammar t;\n" +
				"tokens {\n" +
				"  C;\n" +
				"  D;" +
				"}\n"+
				"A : 'a';\n" +
				"C : 'c' ;");
		String rules = "A, C";
		String tokenNames = "A, C, D";
		checkSymbols(g, rules, tokenNames);
	}

	public void testCombinedGrammarLiterals() throws Exception {
		Grammar g = new Grammar(
				"grammar t;\n"+
				"a : \"begin\" b \"end\";\n" +
				"b : C ';' ;\n" +
				"ID : 'a' ;\n" +
				"FOO : \"foo\" ;\n" +  // "foo" is not a token name
				"C : 'c' ;\n");        // nor is 'c'
		String rules = "a, b";
		String tokenNames = "C, FOO, ID, \"begin\", \"end\", ';'";
		checkSymbols(g, rules, tokenNames);
	}

	public void testSimplePlusEqualLabel() throws Exception {
		Grammar g = new Grammar(
				"parser grammar t;\n"+
				"a : ids+=ID ( COMMA ids+=ID )* ;\n");
		String rule = "a";
		String tokenLabels = "ids";
		String ruleLabels = null;
		checkPlusEqualsLabels(g, rule, tokenLabels, ruleLabels);
	}

	public void testMixedPlusEqualLabel() throws Exception {
		Grammar g = new Grammar(
				"grammar t;\n"+
				"options {output=AST;}\n" +
				"a : id+=ID ( ',' e+=expr )* ;\n" +
				"expr : 'e';\n" +
				"ID : 'a';\n");
		String rule = "a";
		String tokenLabels = "id";
		String ruleLabels = "e";
		checkPlusEqualsLabels(g, rule, tokenLabels, ruleLabels);
	}

	// T E S T  L I T E R A L  E S C A P E S

	public void testParserCharLiteralWithEscape() throws Exception {
		Grammar g = new Grammar(
				"grammar t;\n"+
				"a : '\\n';\n");
		Set literals = g.getCharLiteralTokens();
		System.out.println("literals="+literals);
	}



	// T E S T  E R R O R S

	public void testParserStringLiterals() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
				"parser grammar t;\n"+
				"a : \"begin\" b ;\n" +
				"b : C ;");
		Object expectedArg = "\"begin\"";
		int expectedMsgID = ErrorManager.MSG_LITERAL_NOT_ASSOCIATED_WITH_LEXER_RULE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testParserCharLiterals() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
				"parser grammar t;\n"+
				"a : '(' b ;\n" +
				"b : C ;");
		Object expectedArg = "'('";
		int expectedMsgID = ErrorManager.MSG_LITERAL_NOT_ASSOCIATED_WITH_LEXER_RULE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testStringLiteralInParserTokensSection() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"parser grammar t;\n" +
				"tokens {\n" +
				"  B=\"begin\";\n" +
				"}\n"+
				"a : A B;\n" +
				"b : C ;");
		Object expectedArg = "\"begin\"";
		int expectedMsgID = ErrorManager.MSG_LITERAL_NOT_ASSOCIATED_WITH_LEXER_RULE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testCharLiteralInParserTokensSection() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"parser grammar t;\n" +
				"tokens {\n" +
				"  B='(';\n" +
				"}\n"+
				"a : A B;\n" +
				"b : C ;");
		Object expectedArg = "'('";
		int expectedMsgID = ErrorManager.MSG_LITERAL_NOT_ASSOCIATED_WITH_LEXER_RULE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testCharLiteralInLexerTokensSection() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"lexer grammar t;\n" +
				"tokens {\n" +
				"  B='(';\n" +
				"}\n"+
				"ID : 'a';\n");
		Object expectedArg = "'('";
		int expectedMsgID = ErrorManager.MSG_CANNOT_ALIAS_TOKENS_IN_LEXER;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testRuleRedefinition() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"parser grammar t;\n"+
				"a : A | B;\n" +
				"a : C ;");

		Object expectedArg = "a";
		int expectedMsgID = ErrorManager.MSG_RULE_REDEFINITION;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testLexerRuleRedefinition() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"lexer grammar t;\n"+
				"ID : 'a' ;\n" +
				"ID : 'd' ;");

		Object expectedArg = "ID";
		int expectedMsgID = ErrorManager.MSG_RULE_REDEFINITION;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testCombinedRuleRedefinition() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"grammar t;\n"+
				"x : ID ;\n" +
				"ID : 'a' ;\n" +
				"x : ID ID ;");

		Object expectedArg = "x";
		int expectedMsgID = ErrorManager.MSG_RULE_REDEFINITION;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testUndefinedToken() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"grammar t;\n"+
				"x : ID ;");

		Object expectedArg = "ID";
		int expectedMsgID = ErrorManager.MSG_NO_TOKEN_DEFINITION;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testUndefinedTokenOkInParser() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"parser grammar t;\n"+
				"x : ID ;");
        assertTrue(equeue.errors.size()==0, "should not be an error");
	}

	public void testUndefinedRule() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"grammar t;\n"+
				"x : r ;");

		Object expectedArg = "r";
		int expectedMsgID = ErrorManager.MSG_UNDEFINED_RULE_REF;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testLexerRuleInParser() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"parser grammar t;\n"+
				"X : ;");

		Object expectedArg = "X";
		int expectedMsgID = ErrorManager.MSG_LEXER_RULES_NOT_ALLOWED;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testParserRuleInLexer() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"lexer grammar t;\n"+
				"a : ;");

		Object expectedArg = "a";
		int expectedMsgID = ErrorManager.MSG_PARSER_RULES_NOT_ALLOWED;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testRuleScopeConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"grammar t;\n"+
			"scope a {\n" +
			"  int n;\n" +
			"}\n" +
			"a : \n" +
			"  ;\n");

		Object expectedArg = "a";
		int expectedMsgID = ErrorManager.MSG_SYMBOL_CONFLICTS_WITH_GLOBAL_SCOPE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testTokenRuleScopeConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"grammar t;\n"+
			"scope ID {\n" +
			"  int n;\n" +
			"}\n" +
			"ID : 'a'\n" +
			"  ;\n");

		Object expectedArg = "ID";
		int expectedMsgID = ErrorManager.MSG_SYMBOL_CONFLICTS_WITH_GLOBAL_SCOPE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testTokenScopeConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"grammar t;\n"+
			"tokens { ID; }\n"+
			"scope ID {\n" +
			"  int n;\n" +
			"}\n" +
			"a : \n" +
			"  ;\n");

		Object expectedArg = "ID";
		int expectedMsgID = ErrorManager.MSG_SYMBOL_CONFLICTS_WITH_GLOBAL_SCOPE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testTokenRuleScopeConflictInLexerGrammar() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"scope ID {\n" +
			"  int n;\n" +
			"}\n" +
			"ID : 'a'\n" +
			"  ;\n");

		Object expectedArg = "ID";
		int expectedMsgID = ErrorManager.MSG_SYMBOL_CONFLICTS_WITH_GLOBAL_SCOPE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testTokenLabelScopeConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"scope s {\n" +
			"  int n;\n" +
			"}\n" +
			"a : s=ID \n" +
			"  ;\n");

		Object expectedArg = "s";
		int expectedMsgID = ErrorManager.MSG_SYMBOL_CONFLICTS_WITH_GLOBAL_SCOPE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testRuleLabelScopeConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"scope s {\n" +
			"  int n;\n" +
			"}\n" +
			"a : s=b \n" +
			"  ;\n" +
			"b : ;\n");

		Object expectedArg = "s";
		int expectedMsgID = ErrorManager.MSG_SYMBOL_CONFLICTS_WITH_GLOBAL_SCOPE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testLabelAndRuleNameConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : c=b \n" +
			"  ;\n" +
			"b : ;\n" +
			"c : ;\n");

		Object expectedArg = "c";
		int expectedMsgID = ErrorManager.MSG_LABEL_CONFLICTS_WITH_RULE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testLabelAndTokenNameConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : ID=b \n" +
			"  ;\n" +
			"b : ID ;\n" +
			"c : ;\n");

		Object expectedArg = "ID";
		int expectedMsgID = ErrorManager.MSG_LABEL_CONFLICTS_WITH_TOKEN;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testLabelAndArgConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a[int i] returns [int x]: i=ID \n" +
			"  ;\n");

		Object expectedArg = "i";
		int expectedMsgID = ErrorManager.MSG_LABEL_CONFLICTS_WITH_RULE_ARG_RETVAL;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testLabelAndParameterConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a[int i] returns [int x]: x=ID \n" +
			"  ;\n");

		Object expectedArg = "x";
		int expectedMsgID = ErrorManager.MSG_LABEL_CONFLICTS_WITH_RULE_ARG_RETVAL;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testLabelRuleScopeConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a\n" +
			"scope {" +
			"  int n;" +
			"}\n" +
			"  : n=ID\n" +
			"  ;\n");

		Object expectedArg = "n";
		Object expectedArg2 = "a";
		int expectedMsgID = ErrorManager.MSG_LABEL_CONFLICTS_WITH_RULE_SCOPE_ATTRIBUTE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testRuleScopeArgConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a[int n]\n" +
			"scope {" +
			"  int n;" +
			"}\n" +
			"  : \n" +
			"  ;\n");

		Object expectedArg = "n";
		Object expectedArg2 = "a";
		int expectedMsgID = ErrorManager.MSG_ATTRIBUTE_CONFLICTS_WITH_RULE_ARG_RETVAL;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testRuleScopeReturnValueConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a returns [int n]\n" +
			"scope {" +
			"  int n;" +
			"}\n" +
			"  : \n" +
			"  ;\n");

		Object expectedArg = "n";
		Object expectedArg2 = "a";
		int expectedMsgID = ErrorManager.MSG_ATTRIBUTE_CONFLICTS_WITH_RULE_ARG_RETVAL;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testRuleScopeRuleNameConflict() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a\n" +
			"scope {" +
			"  int a;" +
			"}\n" +
			"  : \n" +
			"  ;\n");

		Object expectedArg = "a";
		Object expectedArg2 = null;
		int expectedMsgID = ErrorManager.MSG_ATTRIBUTE_CONFLICTS_WITH_RULE;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testBadGrammarOption() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Tool antlr = new Tool();
		Grammar g = new Grammar(antlr,
								"t",
								new StringReader(
									"grammar t;\n"+
									"options {foo=3; language=Java;}\n" +
									"a : 'a';\n"));

		Object expectedArg = "foo";
		int expectedMsgID = ErrorManager.MSG_ILLEGAL_OPTION;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testBadRuleOption() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"grammar t;\n"+
				"a\n"+
				"options {k=3; tokenVocab=blort;}\n" +
				"  : 'a';\n");

		Object expectedArg = "tokenVocab";
		int expectedMsgID = ErrorManager.MSG_ILLEGAL_OPTION;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testBadSubRuleOption() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue); // unique listener per thread
		Grammar g = new Grammar(
				"grammar t;\n"+
				"a : ( options {k=3; language=Java;}\n" +
				"    : 'a'\n" +
				"    | 'b'\n" +
				"    )\n" +
				"  ;\n");
		Object expectedArg = "language";
		int expectedMsgID = ErrorManager.MSG_ILLEGAL_OPTION;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	protected void checkError(ErrorQueue equeue,
							  GrammarSemanticsMessage expectedMessage)
		throws FailedAssertionException
	{
		/*
		System.out.println(equeue.infos);
		System.out.println(equeue.warnings);
		System.out.println(equeue.errors);
		assertTrue(equeue.errors.size()==n,
				   "number of errors mismatch; expecting "+n+"; found "+
				   equeue.errors.size());
				   */
		Message foundMsg = null;
		for (int i = 0; i < equeue.errors.size(); i++) {
			Message m = (Message)equeue.errors.get(i);
			if (m.msgID==expectedMessage.msgID ) {
				foundMsg = m;
			}
		}
		assertTrue(foundMsg!=null, "no error; "+expectedMessage.msgID+" expected");
		assertTrue(foundMsg instanceof GrammarSemanticsMessage,
				   "error is not a GrammarSemanticsMessage");
		assertEqual(foundMsg.arg, expectedMessage.arg);
	}

	protected void checkPlusEqualsLabels(Grammar g,
										 String ruleName,
										 String tokenLabelsStr,
										 String ruleLabelsStr)
		throws FailedAssertionException
	{
		// make sure expected += labels are there
		Rule r = g.getRule(ruleName);
		StringTokenizer st = new StringTokenizer(tokenLabelsStr, ", ");
		Set tokenLabels = null;
		while ( st.hasMoreTokens() ) {
			if ( tokenLabels==null ) {
				tokenLabels = new HashSet();
			}
			String labelName = st.nextToken();
			tokenLabels.add(labelName);
		}
		Set ruleLabels = null;
		if ( ruleLabelsStr!=null ) {
			st = new StringTokenizer(ruleLabelsStr, ", ");
			ruleLabels = new HashSet();
			while ( st.hasMoreTokens() ) {
				String labelName = st.nextToken();
				ruleLabels.add(labelName);
			}
		}
		assertTrue((tokenLabels!=null && r.tokenListLabels!=null) ||
				   (tokenLabels==null && r.tokenListLabels==null),
				   "token += labels mismatch; "+tokenLabels+"!="+r.tokenListLabels);
		assertTrue((ruleLabels!=null && r.ruleListLabels!=null) ||
				   (ruleLabels==null && r.ruleListLabels==null),
				   "rule += labels mismatch; "+ruleLabels+"!="+r.ruleListLabels);
		if ( tokenLabels!=null ) {
			assertEqual(tokenLabels, r.tokenListLabels.keySet());
		}
		if ( ruleLabels!=null ) {
			assertEqual(ruleLabels, r.ruleListLabels.keySet());
		}
	}

	protected void checkSymbols(Grammar g,
								String rulesStr,
								String tokensStr)
		throws FailedAssertionException
	{
		Set tokens = g.getTokenNames();

		// make sure expected tokens are there
		StringTokenizer st = new StringTokenizer(tokensStr, ", ");
		while ( st.hasMoreTokens() ) {
			String tokenName = st.nextToken();
			assertTrue(g.getTokenType(tokenName)!=Label.INVALID,
					   "token "+tokenName+" expected");
			tokens.remove(tokenName);
		}
		// make sure there are not any others (other than <EOF> etc...)
        for (Iterator iter = tokens.iterator(); iter.hasNext();) {
			String tokenName = (String) iter.next();
			assertTrue(g.getTokenType(tokenName)<Label.MIN_TOKEN_TYPE,
					   "unexpected token name "+tokenName);
		}

		// make sure all expected rules are there
		st = new StringTokenizer(rulesStr, ", ");
		int n = 0;
		while ( st.hasMoreTokens() ) {
			String ruleName = st.nextToken();
			assertTrue(g.getRule(ruleName)!=null, "rule "+ruleName+" expected");
			n++;
		}
		Collection rules = g.getRules();
		System.out.println("rules="+rules);
		// make sure there are no extra rules
		assertTrue(rules.size()==n,
				   "number of rules mismatch; expecting "+n+"; found "+rules.size());

	}

}
