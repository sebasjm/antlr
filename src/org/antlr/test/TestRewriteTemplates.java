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

public class TestRewriteTemplates extends TestSuite {
	protected boolean debug = false;

	public void testDelete() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=template;}\n" +
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

	public void testAction() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=template;}\n" +
			"a : ID INT -> {new StringTemplate($ID.text)} ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34", debug);
		String expecting = "abc\n";
		assertEqual(found, expecting);
	}

	public void testInlineTemplate() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=template;}\n" +
			"a : ID INT -> template(x={$ID},y={$INT}) <<x:<x.text>, y:<y.text>;>> ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34", debug);
		String expecting = "x:abc, y:34;\n";
		assertEqual(found, expecting);
	}

	public void testNamedTemplate() throws Exception {
		// the support code adds template group in it's output Test.java
		// that defines template foo.
		String grammar =
			"grammar T;\n" +
			"options {output=template;}\n" +
			"a : ID INT -> foo(x={$ID.text},y={$INT.text}) ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34", debug);
		String expecting = "abc 34\n";
		assertEqual(found, expecting);
	}

	public void testInlineTemplateInvokingLib() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=template;}\n" +
			"a : ID INT -> template(x={$ID.text},y={$INT.text}) '<foo(...)>' ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34", debug);
		String expecting = "abc 34\n";
		assertEqual(found, expecting);
	}

	public void testPredicatedAlts() throws Exception {
		// the support code adds template group in it's output Test.java
		// that defines template foo.
		String grammar =
			"grammar T;\n" +
			"options {output=template;}\n" +
			"a : ID INT -> {false}? foo(x={$ID.text},y={$INT.text})\n" +
			"           -> foo(x={\"hi\"}, y={$ID.text})\n" +
			"  ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34", debug);
		String expecting = "hi abc\n";
		assertEqual(found, expecting);
	}

	public void testTemplateReturn() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=template;}\n" +
			"a : b {System.out.println($b.st);} ;\n" +
			"b : ID INT -> foo(x={$ID.text},y={$INT.text}) ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34", debug);
		String expecting = "abc 34\n";
		assertEqual(found, expecting);
	}

}
