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

public class TestSyntacticPredicateEvaluation extends TestSuite {
	public void testTwoPredsWithNakedAlt() throws Exception {
		String grammar =
			"grammar t;\n" +
			"s : (a ';')+ ;\n" +
			"a\n" +
			"options {\n" +
			"  k=1;\n" +
			"}\n" +
			"  : (b '.')=> b '.' {System.out.println(\"alt 1\");}\n" +
			"  | (b)=> b {System.out.println(\"alt 2\");}\n" +
			"  | c       {System.out.println(\"alt 3\");}\n" +
			"  ;\n" +
			"b\n" +
			"@init {System.out.println(\"enter b\");}\n" +
			"   : '(' 'x' ')' ;\n" +
			"c\n" +
			"@init {System.out.println(\"enter c\");}\n" +
			"   : '(' c ')' | 'x' ;\n" +
			"WS : (' '|'\\n')+ {channel=99;}\n" +
			"   ;\n" ;
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "t", "tLexer",
												 "a", "(x) ;", false);
		String expecting =
			"enter b\n" +
			"enter b\n" +
			"alt 2\n";
		assertEqual(found, expecting);

		found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "t", "tLexer",
												 "a", "(x). ;", false);
		expecting =
			"enter b\n" +
			"enter b\n" +
			"alt 1\n";
		assertEqual(found, expecting);

		found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "t", "tLexer",
												 "a", "((x)) ;", false);
		expecting =
			"enter b\n" +
			"enter c\n" +
			"enter c\n" +
			"enter c\n" +
			"alt 3\n";
		assertEqual(found, expecting);
	}

	public void testTwoPredsWithNakedAltNotLast() throws Exception {
		String grammar =
			"grammar t;\n" +
			"s : (a ';')+ ;\n" +
			"a\n" +
			"options {\n" +
			"  k=1;\n" +
			"}\n" +
			"  : (b '.')=> b '.' {System.out.println(\"alt 1\");}\n" +
			"  | c       {System.out.println(\"alt 2\");}\n" +
			"  | (b)=> b {System.out.println(\"alt 3\");}\n" +
			"  ;\n" +
			"b\n" +
			"@init {System.out.println(\"enter b\");}\n" +
			"   : '(' 'x' ')' ;\n" +
			"c\n" +
			"@init {System.out.println(\"enter c\");}\n" +
			"   : '(' c ')' | 'x' ;\n" +
			"WS : (' '|'\\n')+ {channel=99;}\n" +
			"   ;\n" ;
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "t", "tLexer",
												 "a", "(x) ;", false);
		String expecting =
			"enter b\n" +
			"enter c\n" +
			"enter c\n" +
			"alt 2\n";
		assertEqual(found, expecting);

		found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "t", "tLexer",
												 "a", "(x). ;", false);
		expecting =
			"enter b\n" +
			"enter b\n" +
			"alt 1\n";
		assertEqual(found, expecting);

		found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "t", "tLexer",
												 "a", "((x)) ;", false);
		expecting =
			"enter b\n" +
			"enter c\n" +
			"enter c\n" +
			"enter c\n" +
			"alt 2\n";
		assertEqual(found, expecting);
	}

	public void testLexerPred() throws Exception {
		// TODO: this one fails until I can change how I copy lexer rules
		String grammar =
			"grammar t;\n" +
			"s : A ;\n" +
			"A options {k=1;}\n" + // force backtracking
			"  : (B '.')=>B {System.out.println(\"alt1\");}\n" +
			"  | B {System.out.println(\"alt2\");}" +
			"  ;\n" +
			"B : 'x'+ ;\n" ;
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "t", "tLexer",
												 "s", "xxx", false);
		String expecting =
			"alt2\n";
		assertEqual(found, expecting);
	}

}
