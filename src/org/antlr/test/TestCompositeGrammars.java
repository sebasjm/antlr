/*
 [The "BSD licence"]
 Copyright (c) 2005-2007 Terence Parr
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

import org.antlr.tool.ErrorManager;
import org.antlr.tool.Grammar;

public class TestCompositeGrammars extends BaseTest {
	public void testWildcardStillWorks() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		String grammar =
			"parser grammar S;\n" +
			"a : B . C ;\n"; // not qualified ID
		Grammar g = new Grammar(grammar);
		assertEquals("unexpected errors: "+equeue, 0, equeue.errors.size());
	}

	public void testDelegatorInvokesDelegateRule() throws Exception {
		String slave =
			"parser grammar S;\n" +
			"a : B {System.out.println(\"S.a\");} ;\n";
		mkdir(tmpdir);
		writeFile(tmpdir, "S.g", slave);
		String master =
			"grammar M;\n" +
			"import S;\n" +
			"s : a ;\n" +
			"B : 'b' ;" + // defines B from inherited token space
			"WS : (' '|'\\n') {skip();} ;\n" ;
		String found = execParser("M.g", master, "MParser", "MLexer",
								  "s", "b", false);
		assertEquals("S.a\n", found);
	}

	public void testDelegatorInvokesDelegateRuleWithArgs() throws Exception {
		// must generate something like:
		// public int a(int x) throws RecognitionException { return gS.a(x); }
		// in M.
		String slave =
			"parser grammar S;\n" +
			"a[int x] returns [int y] : B {System.out.print(\"S.a\"); $y=1000;} ;\n";
		mkdir(tmpdir);
		writeFile(tmpdir, "S.g", slave);
		String master =
			"grammar M;\n" +
			"import S;\n" +
			"s : label=a[3] {System.out.println($label.y);} ;\n" +
			"B : 'b' ;" + // defines B from inherited token space
			"WS : (' '|'\\n') {skip();} ;\n" ;
		String found = execParser("M.g", master, "MParser", "MLexer",
								  "s", "b", false);
		assertEquals("S.a1000\n", found);
	}

	public void testDelegatorAccessesDelegateMembers() throws Exception {
		String slave =
			"parser grammar S;\n" +
			"@members {\n" +
			"  public void foo() {System.out.println(\"foo\");}\n" +
			"}\n" +
			"a : B ;\n";
		mkdir(tmpdir);
		writeFile(tmpdir, "S.g", slave);
		String master =
			"grammar M;\n" +		// uses no rules from the import
			"import S;\n" +
			"s : 'b' {gS.foo();} ;\n" + // gS is import pointer
			"WS : (' '|'\\n') {skip();} ;\n" ;
		String found = execParser("M.g", master, "MParser", "MLexer",
								  "s", "b", false);
		assertEquals("foo\n", found);
	}

	public void testDelegatorInvokesFirstVersionOfDelegateRule() throws Exception {
		String slave =
			"parser grammar S;\n" +
			"a : b {System.out.println(\"S.a\");} ;\n" +
			"b : B ;\n" ;
		mkdir(tmpdir);
		writeFile(tmpdir, "S.g", slave);
		String slave2 =
			"parser grammar T;\n" +
			"a : B {System.out.println(\"T.a\");} ;\n"; // hidden by S.a
		mkdir(tmpdir);
		writeFile(tmpdir, "T.g", slave2);
		String master =
			"grammar M;\n" +
			"import S,T;\n" +
			"s : a ;\n" +
			"B : 'b' ;\n" +
			"WS : (' '|'\\n') {skip();} ;\n" ;
		String found = execParser("M.g", master, "MParser", "MLexer",
								  "s", "b", false);
		assertEquals("S.a\n", found);
	}

	public void testDelegatesSeeSameTokenType() throws Exception {
		String slave =
			"parser grammar S;\n" + // A, B, C token type order
			"tokens { A; B; C; }\n" +
			"x : A {System.out.println(\"S.x\");} ;\n";
		mkdir(tmpdir);
		writeFile(tmpdir, "S.g", slave);
		String slave2 =
			"parser grammar T;\n" +
			"tokens { C; B; A; }\n" + // reverse order
			"y : A {System.out.println(\"T.y\");} ;\n";
		mkdir(tmpdir);
		writeFile(tmpdir, "T.g", slave2);
		// The lexer will create rules to match letters a, b, c.
		// The associated token types A, B, C must have the same value
		// and all importd parsers.  Since ANTLR regenerates all imports
		// for use with the delegator M, it can generate the same token type
		// mapping in each parser:
		// public static final int C=6;
		// public static final int EOF=-1;
		// public static final int B=5;
		// public static final int WS=7;
		// public static final int A=4;

		String master =
			"grammar M;\n" +
			"import S,T;\n" +
			"s : x y ;\n" + // matches AA, which should be "aa"
			"B : 'b' ;\n" + // another order: B, A, C
			"A : 'a' ;\n" +
			"C : 'c' ;\n" +
			"WS : (' '|'\\n') {skip();} ;\n" ;
		String found = execParser("M.g", master, "MParser", "MLexer",
								  "s", "aa", false);
		assertEquals("S.x\n" +
					 "T.y\n", found);
	}

	public void testDelegatorRuleOverridesDelegate() throws Exception {
		String slave =
			"parser grammar S;\n" +
			"a : b {System.out.println(\"S.a\");} ;\n" +
			"b : B ;\n" ;
		mkdir(tmpdir);
		writeFile(tmpdir, "S.g", slave);
		String master =
			"grammar M;\n" +
			"import S;\n" +
			"b : 'b'|'c' ;\n" +
			"WS : (' '|'\\n') {skip();} ;\n" ;
		String found = execParser("M.g", master, "MParser", "MLexer",
								  "a", "c", false);
		assertEquals("S.a\n", found);
	}

	// LEXER INHERITANCE

	public void testLexerDelegatorInvokesDelegateRule() throws Exception {
		String slave =
			"lexer grammar S;\n" +
			"A : 'a' {System.out.println(\"S.A\");} ;\n" +
			"C : 'c' ;\n";
		mkdir(tmpdir);
		writeFile(tmpdir, "S.g", slave);
		String master =
			"lexer grammar M;\n" +
			"import S;\n" +
			"B : 'b' ;\n" +
			"WS : (' '|'\\n') {skip();} ;\n" ;
		String found = execLexer("M.g", master, "M", "abc", false);
		assertEquals("S.A\nabc\n", found);
	}

	public void testLexerDelegatorRuleOverridesDelegate() throws Exception {
		// hmm...generates A in S still but Tokens calls gM.A() properly.
		// I get an error:
		// warning(209): M.g:1:1: Multiple token rules can match input such as "'a'": A, Tokens
		// As a result, tokens(s) Tokens were disabled for that input
		// warning(208): M.g:1:1: The following token definitions are unreachable: Tokens
		String slave =
			"lexer grammar S;\n" +
			"A : 'a' {System.out.println(\"S.A\");} ;\n";
		mkdir(tmpdir);
		writeFile(tmpdir, "S.g", slave);
		String master =
			"lexer grammar M;\n" +
			"import S;\n" +
			"A : 'a' {System.out.println(\"M.A\");} ;\n" +
			"WS : (' '|'\\n') {skip();} ;\n" ;
		String found = execLexer("M.g", master, "M", "a", false);
		assertEquals("M.A\na\n", found);
	}

	// TODO: test -lib import path
}