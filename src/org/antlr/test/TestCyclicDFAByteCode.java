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
import org.antlr.test.unit.FailedAssertionException;
import org.antlr.tool.Grammar;
import org.antlr.tool.Interpreter;
import org.antlr.tool.InterpreterActions;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.codegen.CodeGenerator;
import org.antlr.Tool;

public class TestCyclicDFAByteCode extends TestSuite {

    /** Public default constructor used by TestRig */
    public TestCyclicDFAByteCode() {
    }

	public void testSimpleParse() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar lex;\n"+
			"IF : \"if\" ;\n" +
			"ID : ('a'..'z')+ ;\n"+
			"ID2 : ('a'..'z')+ '@';\n");
		String expecting =
			"";
		checkByteCodes(g, 2, expecting);
	}

	protected void checkByteCodes(Grammar g, int decision, String expecting)
		throws FailedAssertionException
	{
		// Generate code
		String language = "Java";
		Tool antlr = new Tool();
		antlr.processArgs(new String[] {"-o",System.getProperty("java.io.tmpdir")});
		CodeGenerator generator = new CodeGenerator(antlr, g, language);
		g.setCodeGenerator(generator);
		if ( g.getType()==Grammar.LEXER ) {
			g.addArtificialMatchTokensRule();
		}
		generator.genRecognizer();
		String result = generator.getCyclicDFAByteCodeST().toString();
		assertEqual(result, expecting);
	}
}
