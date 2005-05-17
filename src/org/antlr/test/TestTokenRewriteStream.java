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

	public void testSimpleParse() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';\n" +
			"B : 'b';\n" +
			"C : 'c';\n");
		CharStream input = new ANTLRStringStream("abc");
		Interpreter lexEngine = new Interpreter(g, input);
		TokenRewriteStream tokens = new TokenRewriteStream(lexEngine);
		tokens.LT(1); // fill buffer
		System.out.println("tokens="+tokens.toString());
		tokens.replace(0, "x");
		tokens.insertBefore(0, "0");
		System.out.println("tokens="+tokens.toString());
		String result = tokens.toString();
		String expecting = "0xbc";
		assertEqual(result, expecting);
	}

}
