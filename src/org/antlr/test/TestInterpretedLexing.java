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
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;

public class TestInterpretedLexing extends TestSuite {

    /** Public default constructor used by TestRig */
    public TestInterpretedLexing() {
    }

    /*
	public void testSimpleAltCharTest() throws Exception {
        Grammar g = new Grammar(
                "lexer grammar t;\n"+
                "A : 'a' | 'b' | 'c';");
        g.parse("A", new ANTLRStringStream("a"));
        g.parse("A", new ANTLRStringStream("b"));
        g.parse("A", new ANTLRStringStream("c"));
    }

    public void testSingleRuleRef() throws Exception {
        Grammar g = new Grammar(
                "lexer grammar t;\n"+
                "A : 'a' B 'c' ;\n" +
                "B : 'b' ;\n");
        g.parse("A", new ANTLRStringStream("abc"));
    }

    public void testSimpleLoop() throws Exception {
        Grammar g = new Grammar(
                "lexer grammar t;\n"+
                "INT : (DIGIT)+ ;\n"+
				"fragment DIGIT : '0'..'9';\n");
		g.parse("INT", new ANTLRStringStream("12x")); // should ignore the x
		g.parse("INT", new ANTLRStringStream("1234"));
    }

    public void testMultAltLoop() throws Exception {
        Grammar g = new Grammar(
                "lexer grammar t;\n"+
                "A : ('0'..'9'|'a'|'b')+ ;\n");
        g.parse("A", new ANTLRStringStream("a"));
		g.parse("A", new ANTLRStringStream("1234"));
        g.parse("A", new ANTLRStringStream("aaa"));
        g.parse("A", new ANTLRStringStream("aaaa9"));
        g.parse("A", new ANTLRStringStream("b"));
        g.parse("A", new ANTLRStringStream("baa"));
    }
    */
	public void testSimpleLoops() throws Exception {
		Grammar g = new Grammar(
				"lexer grammar t;\n"+
				"A : ('0'..'9')+ '.' ('0'..'9')* | ('0'..'9')+ ;\n");
		CharStream input = new ANTLRStringStream("1234.5");
		Interpreter engine = new Interpreter(g, input);
		int result = engine.scan("A");
	}

	public void testTokensRules() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"INT : (DIGIT)+ ;\n"+
			"FLOAT : (DIGIT)+ '.' (DIGIT)* ;\n"+
			"fragment DIGIT : '0'..'9';\n" +
			"WS : (' ')+ ;\n");
		CharStream input = new ANTLRStringStream("123 139.52");
		Interpreter lexEngine = new Interpreter(g, input);

		CommonTokenStream tokens = new CommonTokenStream(lexEngine);
		// TODO: doesn't work yet.  it sees:
		// unexpected label '4' in dfa state 0:{13|4, 6|3, 4|2, 14|4, 2|1, 1|4}
		// probably 4 is the token type and can't match against the dfa?
		Grammar pg = new Grammar("grammar p; a : (INT|FLOAT|WS)+;\n");
		Interpreter parseEngine = new Interpreter(pg, tokens);
		parseEngine.parse("a");
		//pg.parse("a", g);

		/*
		Token t = lexEngine.nextToken();
		while ( t.getType()!=Token.EOF ) {
			System.out.println(t.toString(input));
			t = lexEngine.nextToken();
		}
		*/
	}

}
