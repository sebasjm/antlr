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

public class TestInterpretedParsing extends TestSuite {

    /** Public default constructor used by TestRig */
    public TestInterpretedParsing() {
    }

	public void testSimpleParse() throws Exception {
		Grammar pg = new Grammar(
			"parser grammar p;\n"+
			"prog : WHILE ID LCURLY (assign)* RCURLY;\n" +
			"assign : ID ASSIGN expr SEMI ;\n" +
			"expr : INT | FLOAT | ID ;\n");
		Grammar g = new Grammar();
		g.importTokenVocabulary(pg);
		g.setGrammarContent(
			"lexer grammar t;\n"+
			"WHILE : \"while\";\n"+
			"LCURLY : '{';\n"+
			"RCURLY : '}';\n"+
			"ASSIGN : '=';\n"+
			"SEMI : ';';\n"+
			"ID : ('a'..'z')+ ;\n"+
			"INT : (DIGIT)+ ;\n"+
			"FLOAT : (DIGIT)+ '.' (DIGIT)* ;\n"+
			"fragment DIGIT : '0'..'9';\n" +
			"WS : (' ')+ ;\n");
		CharStream input = new ANTLRStringStream("while x { i=1; y=3.42; z=y; }");
		Interpreter lexEngine = new Interpreter(g, input);

		CommonTokenStream tokens = new CommonTokenStream(lexEngine);
		tokens.setTokenTypeChannel(g.getTokenType("WS"), 99);
		//System.out.println("tokens="+tokens.toString());
		Interpreter parseEngine = new Interpreter(pg, tokens);
		ParseTree t = parseEngine.parse("prog");
		String result = t.toString();
		String expecting =
			"(<grammar p> (prog while x { (assign i = (expr 1) ;) (assign y = (expr 3.42) ;) (assign z = (expr y) ;) }))";
		assertEqual(result, expecting);
	}

	public void testMismatchedTokenError() throws Exception {
		Grammar pg = new Grammar(
			"parser grammar p;\n"+
			"prog : WHILE ID LCURLY (assign)* RCURLY;\n" +
			"assign : ID ASSIGN expr SEMI ;\n" +
			"expr : INT | FLOAT | ID ;\n");
		Grammar g = new Grammar();
		g.importTokenVocabulary(pg);
		g.setGrammarContent(
			"lexer grammar t;\n"+
			"WHILE : \"while\";\n"+
			"LCURLY : '{';\n"+
			"RCURLY : '}';\n"+
			"ASSIGN : '=';\n"+
			"SEMI : ';';\n"+
			"ID : ('a'..'z')+ ;\n"+
			"INT : (DIGIT)+ ;\n"+
			"FLOAT : (DIGIT)+ '.' (DIGIT)* ;\n"+
			"fragment DIGIT : '0'..'9';\n" +
			"WS : (' ')+ ;\n");
		CharStream input = new ANTLRStringStream("while x { i=1 y=3.42; z=y; }");
		Interpreter lexEngine = new Interpreter(g, input);

		CommonTokenStream tokens = new CommonTokenStream(lexEngine);
		tokens.setTokenTypeChannel(g.getTokenType("WS"), 99);
		//System.out.println("tokens="+tokens.toString());
		Interpreter parseEngine = new Interpreter(pg, tokens);
		ParseTree t = parseEngine.parse("prog");
		String result = t.toString();
		String expecting =
			"(<grammar p> (prog while x { (assign i = (expr 1) MismatchedTokenException(3!=7))))";
		assertEqual(result, expecting);
	}

	public void testMismatchedSetError() throws Exception {
		Grammar pg = new Grammar(
			"parser grammar p;\n"+
			"prog : WHILE ID LCURLY (assign)* RCURLY;\n" +
			"assign : ID ASSIGN expr SEMI ;\n" +
			"expr : INT | FLOAT | ID ;\n");
		Grammar g = new Grammar();
		g.importTokenVocabulary(pg);
		g.setGrammarContent(
			"lexer grammar t;\n"+
			"WHILE : \"while\";\n"+
			"LCURLY : '{';\n"+
			"RCURLY : '}';\n"+
			"ASSIGN : '=';\n"+
			"SEMI : ';';\n"+
			"ID : ('a'..'z')+ ;\n"+
			"INT : (DIGIT)+ ;\n"+
			"FLOAT : (DIGIT)+ '.' (DIGIT)* ;\n"+
			"fragment DIGIT : '0'..'9';\n" +
			"WS : (' ')+ ;\n");
		CharStream input = new ANTLRStringStream("while x { i=; y=3.42; z=y; }");
		Interpreter lexEngine = new Interpreter(g, input);

		CommonTokenStream tokens = new CommonTokenStream(lexEngine);
		tokens.setTokenTypeChannel(g.getTokenType("WS"), 99);
		//System.out.println("tokens="+tokens.toString());
		Interpreter parseEngine = new Interpreter(pg, tokens);
		ParseTree t = parseEngine.parse("prog");
		String result = t.toString();
		String expecting =
			"(<grammar p> (prog while x { (assign i = (expr MismatchedSetException(7!={3, 8..9})))))";
		assertEqual(result, expecting);
	}

	public void testNoViableAltError() throws Exception {
		Grammar pg = new Grammar(
			"parser grammar p;\n"+
			"prog : WHILE ID LCURLY (assign)* RCURLY;\n" +
			"assign : ID ASSIGN expr SEMI ;\n" +
			"expr : {;}INT | FLOAT | ID ;\n");
		Grammar g = new Grammar();
		g.importTokenVocabulary(pg);
		g.setGrammarContent(
			"lexer grammar t;\n"+
			"WHILE : \"while\";\n"+
			"LCURLY : '{';\n"+
			"RCURLY : '}';\n"+
			"ASSIGN : '=';\n"+
			"SEMI : ';';\n"+
			"ID : ('a'..'z')+ ;\n"+
			"INT : (DIGIT)+ ;\n"+
			"FLOAT : (DIGIT)+ '.' (DIGIT)* ;\n"+
			"fragment DIGIT : '0'..'9';\n" +
			"WS : (' ')+ ;\n");
		CharStream input = new ANTLRStringStream("while x { i=; y=3.42; z=y; }");
		Interpreter lexEngine = new Interpreter(g, input);

		CommonTokenStream tokens = new CommonTokenStream(lexEngine);
		tokens.setTokenTypeChannel(g.getTokenType("WS"), 99);
		//System.out.println("tokens="+tokens.toString());
		Interpreter parseEngine = new Interpreter(pg, tokens);
		ParseTree t = parseEngine.parse("prog");
		String result = t.toString();
		String expecting =
			"(<grammar p> (prog while x { (assign i = (expr NoViableAltException(7!=[4:1: expr : ({;} INT | FLOAT | ID );])))))";
		assertEqual(result, expecting);
	}

}
