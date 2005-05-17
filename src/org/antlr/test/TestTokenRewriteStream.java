/*
 [The "BSD licence"]
 Copyright (c) 2004 Terence Parr
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
import org.antlr.tool.Grammar;
import org.antlr.tool.Interpreter;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.ParseTree;

public class TestTokenRewriteStream extends TestSuite {

    /** Public default constructor used by TestRig */
    public TestTokenRewriteStream() {
    }

	public void testInsertBeforeIndex0() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.insertBefore(0, "0");
		String result = tokens.toString();
		String expecting = "0abc";
		assertEqual(result, expecting);
	}

	public void testInsertAfterLastIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.insertAfter(2, "x");
		String result = tokens.toString();
		String expecting = "abcx";
		assertEqual(result, expecting);
	}

	public void test2InsertBeforeAfterMiddleIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.insertBefore(1, "x");
		tokens.insertAfter(1, "x");
		String result = tokens.toString();
		String expecting = "axbxc";
		assertEqual(result, expecting);
	}

	public void testReplaceIndex0() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(0, "x");
		String result = tokens.toString();
		String expecting = "xbc";
		assertEqual(result, expecting);
	}

	public void testReplaceLastIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(2, "x");
		String result = tokens.toString();
		String expecting = "abx";
		assertEqual(result, expecting);
	}

	public void testReplaceMiddleIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(1, "x");
		String result = tokens.toString();
		String expecting = "axc";
		assertEqual(result, expecting);
	}

	public void test2ReplaceMiddleIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(1, "x");
		tokens.replace(1, "y");
		String result = tokens.toString();
		String expecting = "ayc";
		assertEqual(result, expecting);
	}

	public void testReplaceThenDeleteMiddleIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(1, "x");
		tokens.delete(1);
		String result = tokens.toString();
		String expecting = "ac";
		assertEqual(result, expecting);
	}

	public void testReplaceThenInsertSameIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(0, "x");
		tokens.insertBefore(0, "0");
		String result = tokens.toString();
		String expecting = "0xbc";
		assertEqual(result, expecting);
	}

	public void testReplaceThen2InsertSameIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(0, "x");
		tokens.insertBefore(0, "y");
		tokens.insertBefore(0, "z");
		String result = tokens.toString();
		String expecting = "zyxbc";
		assertEqual(result, expecting);
	}

	public void testInsertThenReplaceSameIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.insertBefore(0, "0");
		tokens.replace(0, "x");
		String result = tokens.toString();
		String expecting = "0xbc";
		assertEqual(result, expecting);
	}

	public void test2InsertMiddleIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.insertBefore(1, "x");
		tokens.insertBefore(1, "y");
		String result = tokens.toString();
		String expecting = "ayxbc";
		assertEqual(result, expecting);
	}

	public void test2InsertThenReplaceIndex0() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.insertBefore(0, "x");
		tokens.insertBefore(0, "y");
		tokens.replace(0, "z");
		String result = tokens.toString();
		String expecting = "yxzbc";
		assertEqual(result, expecting);
	}

	public void testReplaceThenInsertBeforeLastIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(2, "x");
		tokens.insertBefore(2, "y");
		String result = tokens.toString();
		String expecting = "abyx";
		assertEqual(result, expecting);
	}

	public void testInsertThenReplaceLastIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.insertBefore(2, "y");
		tokens.replace(2, "x");
		String result = tokens.toString();
		String expecting = "abyx";
		assertEqual(result, expecting);
	}

	public void testReplaceThenInsertAfterLastIndex() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(2, "x");
		tokens.insertAfter(2, "y");
		String result = tokens.toString();
		String expecting = "abxy";
		assertEqual(result, expecting);
	}

	public void testReplaceRangeThenInsertInMiddle() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abcccba");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(2, 4, "x");
		tokens.insertBefore(3, "y");
		String result = tokens.toString();
		String expecting = "abyxba";
		assertEqual(result, expecting);
	}

	public void testReplaceRangeThenInsertAtLeftEdge() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abcccba");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(2, 4, "x");
		tokens.insertBefore(2, "y");
		String result = tokens.toString();
		String expecting = "abyxba";
		assertEqual(result, expecting);
	}

	public void testReplaceRangeThenInsertAtRightEdge() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abcccba");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(2, 4, "x");
		tokens.insertBefore(4, "y");
		String result = tokens.toString();
		String expecting = "abyxba";
		assertEqual(result, expecting);
	}

	public void testReplaceRangeThenInsertAfterRightEdge() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abcccba");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(2, 4, "x");
		tokens.insertAfter(4, "y");
		String result = tokens.toString();
		String expecting = "abxyba";
		assertEqual(result, expecting);
	}

	public void testReplaceAll() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abcccba");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		tokens.replace(0, 6, "x");
		String result = tokens.toString();
		String expecting = "x";
		assertEqual(result, expecting);
	}

}
