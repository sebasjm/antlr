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
			".method <init> ()V 1 1\n" +
			"\taload 0\n" +
			"\tinvokespecial java/lang/Object.<init>()V\n" +
			"\treturn\n" +
			"; public static int DFA3(IntStream); // maxstack=6, locals+param=2\n" +
			".method DFA3 (Lorg/antlr/runtime/IntStream;)I 6 2\n" +
			";    getstatic java/lang/System.err Ljava/io/PrintStream;\n" +
			";    ldc \"enter DFA \"\n" +
			";    invokevirtual java/io/PrintStream.print(Ljava/lang/String;)V\n" +
			";    getstatic java/lang/System.err Ljava/io/PrintStream;\n" +
			";    iconst 3 \n" +
			";    invokevirtual java/io/PrintStream.println(I)V\n" +
			"    goto s0\n" +
			"errorState:\n" +
			"; first print error\n" +
			"    getstatic java/lang/System.err Ljava/io/PrintStream;\n" +
			"    ldc \"DFA match error input=\"\n" +
			"    invokevirtual java/io/PrintStream.print(Ljava/lang/String;)V\n" +
			"    getstatic java/lang/System.err Ljava/io/PrintStream;\n" +
			"    aload 0\n" +
			"    iconst 1\n" +
			"    invokeinterface org/antlr/runtime/IntStream.LA(I)I 2\n" +
			"    invokevirtual java/io/PrintStream.println(I)V\n" +
			"; throw NoViableAltException\n" +
			"    new org/antlr/runtime/NoViableAltException\n" +
			"    dup\n" +
			"    ldc \"\"\n" +
			"    iconst 3\n" +
			"; don't know error state at this point; use -1\n" +
			"    iconst -1\n" +
			"    aload 0\n" +
			"    invokespecial org/antlr/runtime/NoViableAltException.<init>(Ljava/lang/String;IILorg/antlr/runtime/IntStream;)V\n" +
			"    athrow\n" +
			"s10:\n" +
			"    iconst 1\n" +
			"    ireturn\n" +
			"s4:\n" +
			"    iconst 2\n" +
			"    ireturn\n" +
			"s6:\n" +
			"    iconst 3\n" +
			"    ireturn\n" +
			"s2:\n" +
			"; edges have to come first so labels are defined; jump over to switch\n" +
			"    goto s2_switch\n" +
			"s2e2_go:\n" +
			"    aload 0\n" +
			"    invokeinterface org/antlr/runtime/IntStream.consume()V 1\n" +
			"    goto s2\n" +
			"s2e3_go:\n" +
			"    aload 0\n" +
			"    invokeinterface org/antlr/runtime/IntStream.consume()V 1\n" +
			"    goto s6\n" +
			"\n" +
			"s2_default:\n" +
			"; when EOT is an edge label, can't have an error.  This is the default clause\n" +
			"    goto s4\n" +
			"s2_switch:\n" +
			"; jump to one of the previous edge labels based upon input.LA(1)\n" +
			"    aload 0\n" +
			"    iconst 1\n" +
			"    invokeinterface org/antlr/runtime/IntStream.LA(I)I 2\n" +
			"    lookupswitch s2_default 64/s2e3_go 97/s2e2_go 98/s2e2_go 99/s2e2_go 100/s2e2_go 101/s2e2_go 102/s2e2_go 103/s2e2_go 104/s2e2_go 105/s2e2_go 106/s2e2_go 107/s2e2_go 108/s2e2_go 109/s2e2_go 110/s2e2_go 111/s2e2_go 112/s2e2_go 113/s2e2_go 114/s2e2_go 115/s2e2_go 116/s2e2_go 117/s2e2_go 118/s2e2_go 119/s2e2_go 120/s2e2_go 121/s2e2_go 122/s2e2_go\n" +
			"s3:\n" +
			"; edges have to come first so labels are defined; jump over to switch\n" +
			"    goto s3_switch\n" +
			"s3e2_go:\n" +
			"    aload 0\n" +
			"    invokeinterface org/antlr/runtime/IntStream.consume()V 1\n" +
			"    goto s2\n" +
			"s3e3_go:\n" +
			"    aload 0\n" +
			"    invokeinterface org/antlr/runtime/IntStream.consume()V 1\n" +
			"    goto s6\n" +
			"\n" +
			"s3_default:\n" +
			"; when EOT is an edge label, can't have an error.  This is the default clause\n" +
			"    goto s10\n" +
			"s3_switch:\n" +
			"; jump to one of the previous edge labels based upon input.LA(1)\n" +
			"    aload 0\n" +
			"    iconst 1\n" +
			"    invokeinterface org/antlr/runtime/IntStream.LA(I)I 2\n" +
			"    lookupswitch s3_default 64/s3e3_go 97/s3e2_go 98/s3e2_go 99/s3e2_go 100/s3e2_go 101/s3e2_go 102/s3e2_go 103/s3e2_go 104/s3e2_go 105/s3e2_go 106/s3e2_go 107/s3e2_go 108/s3e2_go 109/s3e2_go 110/s3e2_go 111/s3e2_go 112/s3e2_go 113/s3e2_go 114/s3e2_go 115/s3e2_go 116/s3e2_go 117/s3e2_go 118/s3e2_go 119/s3e2_go 120/s3e2_go 121/s3e2_go 122/s3e2_go\n" +
			"s1:\n" +
			"; edges have to come first so labels are defined; jump over to switch\n" +
			"    goto s1_switch\n" +
			"s1e1_go:\n" +
			"    aload 0\n" +
			"    invokeinterface org/antlr/runtime/IntStream.consume()V 1\n" +
			"    goto s3\n" +
			"s1e3_go:\n" +
			"    aload 0\n" +
			"    invokeinterface org/antlr/runtime/IntStream.consume()V 1\n" +
			"    goto s2\n" +
			"s1e4_go:\n" +
			"    aload 0\n" +
			"    invokeinterface org/antlr/runtime/IntStream.consume()V 1\n" +
			"    goto s6\n" +
			"\n" +
			"s1_default:\n" +
			"; when EOT is an edge label, can't have an error.  This is the default clause\n" +
			"    goto s4\n" +
			"s1_switch:\n" +
			"; jump to one of the previous edge labels based upon input.LA(1)\n" +
			"    aload 0\n" +
			"    iconst 1\n" +
			"    invokeinterface org/antlr/runtime/IntStream.LA(I)I 2\n" +
			"    lookupswitch s1_default 64/s1e4_go 97/s1e3_go 98/s1e3_go 99/s1e3_go 100/s1e3_go 101/s1e3_go 102/s1e1_go 103/s1e3_go 104/s1e3_go 105/s1e3_go 106/s1e3_go 107/s1e3_go 108/s1e3_go 109/s1e3_go 110/s1e3_go 111/s1e3_go 112/s1e3_go 113/s1e3_go 114/s1e3_go 115/s1e3_go 116/s1e3_go 117/s1e3_go 118/s1e3_go 119/s1e3_go 120/s1e3_go 121/s1e3_go 122/s1e3_go\n" +
			"s0:\n" +
			"; edges have to come first so labels are defined; jump over to switch\n" +
			"    goto s0_switch\n" +
			"s0e1_go:\n" +
			"    aload 0\n" +
			"    invokeinterface org/antlr/runtime/IntStream.consume()V 1\n" +
			"    goto s1\n" +
			"s0e2_go:\n" +
			"    aload 0\n" +
			"    invokeinterface org/antlr/runtime/IntStream.consume()V 1\n" +
			"    goto s2\n" +
			"\n" +
			"s0_default:\n" +
			"    goto errorState\n" +
			"s0_switch:\n" +
			"; jump to one of the previous edge labels based upon input.LA(1)\n" +
			"    aload 0\n" +
			"    iconst 1\n" +
			"    invokeinterface org/antlr/runtime/IntStream.LA(I)I 2\n" +
			"    lookupswitch s0_default 97/s0e2_go 98/s0e2_go 99/s0e2_go 100/s0e2_go 101/s0e2_go 102/s0e2_go 103/s0e2_go 104/s0e2_go 105/s0e1_go 106/s0e2_go 107/s0e2_go 108/s0e2_go 109/s0e2_go 110/s0e2_go 111/s0e2_go 112/s0e2_go 113/s0e2_go 114/s0e2_go 115/s0e2_go 116/s0e2_go 117/s0e2_go 118/s0e2_go 119/s0e2_go 120/s0e2_go 121/s0e2_go 122/s0e2_go\n";
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
		if ( g.type==Grammar.LEXER ) {
			g.addArtificialMatchTokensRule();
		}
		generator.genRecognizer();
		String result = generator.getCyclicDFAByteCodeST().toString();
		assertEqual(result, expecting);
	}
}
