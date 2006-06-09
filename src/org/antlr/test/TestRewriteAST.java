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
import org.antlr.tool.ErrorManager;
import org.antlr.tool.GrammarSemanticsMessage;
import org.antlr.tool.Message;
import org.antlr.tool.Grammar;
import org.antlr.Tool;
import org.antlr.codegen.CodeGenerator;

public class TestRewriteAST extends TestSuite {
	protected boolean debug = false;

	public void testDelete() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34", debug);
		String expecting = "";
		assertEqual(found, expecting);
	}

	public void testSingleToken() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID -> ID;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc", debug);
		String expecting = "abc\n";
		assertEqual(found, expecting);
	}

	public void testSingleCharLiteral() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : 'c' -> 'c';\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "c", debug);
		String expecting = "c\n";
		assertEqual(found, expecting);
	}

	public void testSingleStringLiteral() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : 'ick' -> 'ick';\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "ick", debug);
		String expecting = "ick\n";
		assertEqual(found, expecting);
	}

	public void testSingleRule() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : b -> b;\n" +
			"b : ID ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc", debug);
		String expecting = "abc\n";
		assertEqual(found, expecting);
	}

	public void testReorderTokens() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> INT ID;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34", debug);
		String expecting = "34 abc\n";
		assertEqual(found, expecting);
	}

	public void testReorderTokenAndRule() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : b INT -> INT b;\n" +
			"b : ID ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34", debug);
		String expecting = "34 abc\n";
		assertEqual(found, expecting);
	}

	public void testTokenTree() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> ^(INT ID);\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34", debug);
		String expecting = "(34 abc)\n";
		assertEqual(found, expecting);
	}

	public void testTokenTreeAfterOtherStuff() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : 'void' ID INT -> 'void' ^(INT ID);\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "void abc 34", debug);
		String expecting = "void (34 abc)\n";
		assertEqual(found, expecting);
	}

	public void testNestedTokenTreeWithOuterLoop() throws Exception {
		// verify that ID and INT both iterate over outer index variable
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {DUH;}\n" +
			"a : ID INT ID INT -> ^( DUH ID ^( DUH INT) )+ ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a 1 b 2", debug);
		String expecting = "(DUH a (DUH 1)) (DUH b (DUH 2))\n";
		assertEqual(found, expecting);
	}

	public void testOptionalSingleToken() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID -> ID? ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc", debug);
		String expecting = "abc\n";
		assertEqual(found, expecting);
	}

	public void testClosureSingleToken() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID ID -> ID* ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a b", debug);
		String expecting = "a b\n";
		assertEqual(found, expecting);
	}

	public void testPositiveClosureSingleToken() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID ID -> ID+ ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a b", debug);
		String expecting = "a b\n";
		assertEqual(found, expecting);
	}

	public void testOptionalSingleRule() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : b -> b?;\n" +
			"b : ID ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc", debug);
		String expecting = "abc\n";
		assertEqual(found, expecting);
	}

	public void testClosureSingleRule() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : b b -> b*;\n" +
			"b : ID ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a b", debug);
		String expecting = "a b\n";
		assertEqual(found, expecting);
	}

	public void testClosureOfLabel() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : x+=b x+=b -> $x*;\n" +
			"b : ID ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a b", debug);
		String expecting = "a b\n";
		assertEqual(found, expecting);
	}

	public void testOptionalLabelNoListLabel() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : (x=ID)? -> $x?;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a", debug);
		String expecting = "a\n";
		assertEqual(found, expecting);
	}

	public void testPositiveClosureSingleRule() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : b b -> b+;\n" +
			"b : ID ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a b", debug);
		String expecting = "a b\n";
		assertEqual(found, expecting);
	}

	public void testSinglePredicateT() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID -> {true}? ID -> ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc", debug);
		String expecting = "abc\n";
		assertEqual(found, expecting);
	}

	public void testSinglePredicateF() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID -> {false}? ID -> ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc", debug);
		String expecting = "";
		assertEqual(found, expecting);
	}

	public void testMultiplePredicate() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> {false}? ID\n" +
			"           -> {true}? INT\n" +
			"           -> \n" +
			"  ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a 2", debug);
		String expecting = "2\n";
		assertEqual(found, expecting);
	}

	public void testMultiplePredicateTrees() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : ID INT -> {false}? ^(ID INT)\n" +
			"           -> {true}? ^(INT ID)\n" +
			"           -> ID\n" +
			"  ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a 2", debug);
		String expecting = "(2 a)\n";
		assertEqual(found, expecting);
	}

	public void testSimpleTree() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : op INT -> ^(op INT);\n" +
			"op : '+'|'-' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "-34", debug);
		String expecting = "(- 34)\n";
		assertEqual(found, expecting);
	}

	public void testSimpleTree2() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : op INT -> ^(INT op);\n" +
			"op : '+'|'-' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "+ 34", debug);
		String expecting = "(34 +)\n";
		assertEqual(found, expecting);
	}

	public void testQueueingOfTokens() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : 'int' ID (',' ID)* ';' -> ^('int' ID+) ;\n" +
			"op : '+'|'-' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "int a,b,c;", debug);
		String expecting = "(int a b c)\n";
		assertEqual(found, expecting);
	}

	public void testNestedTrees() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : 'var' (ID ':' type ';')+ -> ^('var' ^(':' ID type)+) ;\n" +
			"type : 'int' | 'float' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "var a:int; b:float;", debug);
		String expecting = "(var (: a int) (: b float))\n";
		assertEqual(found, expecting);
	}

	public void testImaginaryTokenCopy() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {VAR;}\n" +
			"a : ID (',' ID)*-> ^(VAR ID)+ ;\n" +
			"type : 'int' | 'float' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a,b,c", debug);
		String expecting = "(VAR a) (VAR b) (VAR c)\n";
		assertEqual(found, expecting);
	}

	public void testImaginaryTokenCopySetText() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {VAR;}\n" +
			"a : ID (',' ID)*-> ^(VAR[\"var\"] ID)+ ;\n" +
			"type : 'int' | 'float' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a,b,c", debug);
		String expecting = "(var a) (var b) (var c)\n";
		assertEqual(found, expecting);
	}

	public void testImaginaryTokenNoCopyFromToken() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {BLOCK;}\n" +
			"a : lc='{' ID+ '}' -> ^(BLOCK[$lc] ID+) ;\n" +
			"type : 'int' | 'float' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "{a b c}", debug);
		String expecting = "({ a b c)\n";
		assertEqual(found, expecting);
	}

	public void testImaginaryTokenNoCopyFromTokenSetText() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {BLOCK;}\n" +
			"a : lc='{' ID+ '}' -> ^(BLOCK[$lc,\"block\"] ID+) ;\n" +
			"type : 'int' | 'float' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "{a b c}", debug);
		String expecting = "(block a b c)\n";
		assertEqual(found, expecting);
	}

	public void testMixedRewriteAndAutoAST() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {BLOCK;}\n" +
			"a : b b^ ;\n" + // 2nd b matches only an INT; can make it root
			"b : ID INT -> INT ID\n" +
			"  | INT\n" +
			"  ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a 1 2", debug);
		String expecting = "(2 1 a)\n";
		assertEqual(found, expecting);
	}

	public void testSubruleWithRewrite() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {BLOCK;}\n" +
			"a : b b ;\n" +
			"b : (ID INT -> INT ID | INT INT -> INT+ )\n" +
			"  ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a 1 2 3", debug);
		String expecting = "1 a 2 3\n";
		assertEqual(found, expecting);
	}

	public void testSubruleWithRewriteReferencingPreviousElement() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {TYPE;}\n" +
			"a : b b ;\n" +
			"b : 'int'\n" +
			"    ( ID -> ^(TYPE 'int' ID)\n" +
			"    | ID '=' INT -> ^(TYPE 'int' ID INT)\n" +
			"    )\n" +
			"    ';'\n" +
			"  ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "int a; int b=3;", debug);
		String expecting = "(TYPE int a) (TYPE int b 3)\n";
		assertEqual(found, expecting);
	}

	public void testNestedRewriteShutsOffAutoAST() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {BLOCK;}\n" +
			"a : b b ;\n" +
			"b : ID ( ID (last=ID -> $last)+ ) ';'\n" + // get last ID
			"  | INT\n" + // should still get auto AST construction
			"  ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a b c d; 42", debug);
		String expecting = "d 42\n";
		assertEqual(found, expecting);
	}

	public void testRewriteActions() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : atom -> ^({adaptor.create(INT,\"9\")} atom) ;\n" +
			"atom : INT ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "3", debug);
		String expecting = "(9 3)\n";
		assertEqual(found, expecting);
	}

	public void testRewriteActions2() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : atom -> {adaptor.create(INT,\"9\")} atom ;\n" +
			"atom : INT ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "3", debug);
		String expecting = "9 3\n";
		assertEqual(found, expecting);
	}

	public void testRefToOldValue() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {BLOCK;}\n" +
			"a : (atom -> atom) (op='+' r=atom -> ^($op $a $r) )* ;\n" +
			"atom : INT ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "3+4+5", debug);
		String expecting = "(+ (+ 3 4) 5)\n";
		assertEqual(found, expecting);
	}

	public void testCopySemanticsForRules() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {BLOCK;}\n" +
			"a : atom -> ^(atom atom) ;\n" + // NOT CYCLE! (dup atom)
			"atom : INT ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "3", debug);
		String expecting = "(3 3)\n";
		assertEqual(found, expecting);
	}

	public void testComplicatedMelange() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {BLOCK;}\n" +
			"a : A A b=B B b=B c+=C C c+=C D {$D.text;} -> A+ B+ C+ D ;\n" +
			"type : 'int' | 'float' ;\n" +
			"A : 'a' ;\n" +
			"B : 'b' ;\n" +
			"C : 'c' ;\n" +
			"D : 'd' ;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a a b b b c c c d", debug);
		String expecting = "a a b b b c c c d\n";
		assertEqual(found, expecting);
	}

	public void testRuleListLabel() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {BLOCK;}\n" +
			"a : x+=b x+=b -> $x+;\n"+
			"b : ID ;\n"+
			"ID : 'a'..'z'+ ;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a b", debug);
		String expecting = "a b\n";
		assertEqual(found, expecting);
	}

	public void testArbitraryExprType() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"tokens {BLOCK;}\n" +
			"a : x+=b x+=b -> {new Object()};\n"+
			"b : ID ;\n"+
			"ID : 'a'..'z'+ ;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "a b", debug);
		String expecting = "[not sure what this should be!]\n";
		assertEqual(found, expecting);
	}

	// E R R O R S

	public void testUnknownRule() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);

		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : INT -> ugh ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";

		Grammar g = new Grammar(grammar);
		Tool antlr = new Tool();
		antlr.setOutputDirectory(null); // write to /dev/null
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer();

		int expectedMsgID = ErrorManager.MSG_UNDEFINED_RULE_REF;
		Object expectedArg = "ugh";
		Object expectedArg2 = null;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);

		checkError(equeue, expectedMessage);
	}

	public void testKnownRuleButNotInLHS() throws Exception {
		// THIS WORKS BY ITSELF BUT NOT WHEN I RUN WHOLE FILE!!!!!!
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);

		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : INT -> b ;\n" +
			"b : 'b' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";

		Grammar g = new Grammar(grammar);
		Tool antlr = new Tool();
		antlr.setOutputDirectory(null); // write to /dev/null
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer();

		int expectedMsgID = ErrorManager.MSG_REWRITE_ELEMENT_NOT_PRESENT_ON_LHS;
		Object expectedArg = "b";
		Object expectedArg2 = null;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);

		checkError(equeue, expectedMessage);
	}

	public void testUnknownToken() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);

		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : INT -> ICK ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";

		Grammar g = new Grammar(grammar);
		Tool antlr = new Tool();
		antlr.setOutputDirectory(null); // write to /dev/null
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer();

		int expectedMsgID = ErrorManager.MSG_UNDEFINED_TOKEN_REF_IN_REWRITE;
		Object expectedArg = "ICK";
		Object expectedArg2 = null;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);

		checkError(equeue, expectedMessage);
	}

	public void testUnknownLabel() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);

		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : INT -> $foo ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";

		Grammar g = new Grammar(grammar);
		Tool antlr = new Tool();
		antlr.setOutputDirectory(null); // write to /dev/null
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer();

		int expectedMsgID = ErrorManager.MSG_UNDEFINED_LABEL_REF_IN_REWRITE;
		Object expectedArg = "foo";
		Object expectedArg2 = null;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);

		checkError(equeue, expectedMessage);
	}

	public void testUnknownCharLiteralToken() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);

		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : INT -> 'a' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";

		Grammar g = new Grammar(grammar);
		Tool antlr = new Tool();
		antlr.setOutputDirectory(null); // write to /dev/null
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer();

		int expectedMsgID = ErrorManager.MSG_UNDEFINED_TOKEN_REF_IN_REWRITE;
		Object expectedArg = "'a'";
		Object expectedArg2 = null;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);

		checkError(equeue, expectedMessage);
	}

	public void testUnknownStringLiteralToken() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);

		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a : INT -> 'foo' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";

		Grammar g = new Grammar(grammar);
		Tool antlr = new Tool();
		antlr.setOutputDirectory(null); // write to /dev/null
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer();

		int expectedMsgID = ErrorManager.MSG_UNDEFINED_TOKEN_REF_IN_REWRITE;
		Object expectedArg = "'foo'";
		Object expectedArg2 = null;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);

		checkError(equeue, expectedMessage);
	}

	// S U P P O R T

	protected void checkError(ErrorQueue equeue,
							  GrammarSemanticsMessage expectedMessage)
		throws FailedAssertionException
	{
		//System.out.println("errors="+equeue);
		Message foundMsg = null;
		for (int i = 0; i < equeue.errors.size(); i++) {
			Message m = (Message)equeue.errors.get(i);
			if (m.msgID==expectedMessage.msgID ) {
				foundMsg = m;
			}
		}
		assertTrue(equeue.errors.size()>0, "no error; "+expectedMessage.msgID+" expected");
		assertTrue(equeue.errors.size()<=1, "too many errors; "+equeue.errors);
		assertTrue(foundMsg!=null, "couldn't find expected error: "+expectedMessage.msgID);
		assertTrue(foundMsg instanceof GrammarSemanticsMessage,
				   "error is not a GrammarSemanticsMessage");
		assertEqual(foundMsg.arg, expectedMessage.arg);
		assertEqual(foundMsg.arg2, expectedMessage.arg2);
	}

}
