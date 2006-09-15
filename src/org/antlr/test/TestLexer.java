/*
 [The "BSD licence"]
 Copyright (c) 2005-2006 Terence Parr
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

public class TestLexer extends BaseTest {
	protected boolean debug = false;

	/** Public default constructor used by TestRig */
	public TestLexer() {
	}

	public void testSetText() throws Exception {
		// this must return A not I to the parser; calling a nonfragment rule
		// from a nonfragment rule does not set the overall token.
		String grammar =
			"grammar P;\n"+
			"a : A {System.out.println(input);} ;\n"+
			"A : '\\\\' 't' {setText(\"\t\");} ;\n" +
			"WS : (' '|'\\n') {channel=99;} ;";
		String found = execParser("P.g", grammar, "PParser", "PLexer",
				    "a", "\\t", debug);
		assertEquals("\t\n", found);
	}

	public void testRefToRuleDoesNotSetTokenNorEmitAnother() throws Exception {
		// this must return A not I to the parser; calling a nonfragment rule
		// from a nonfragment rule does not set the overall token.
		String grammar =
			"grammar P;\n"+
			"a : A EOF {System.out.println(input);} ;\n"+
			"A : '-' I ;\n" +
			"I : '0'..'9'+ ;\n"+
			"WS : (' '|'\\n') {channel=99;} ;";
		String found = execParser("P.g", grammar, "PParser", "PLexer",
				    "a", "-34", debug);
		assertEquals("-34\n", found);
	}

	public void testRefToFragment() throws Exception {
		// this must return A not I to the parser; calling a nonfragment rule
		// from a nonfragment rule does not set the overall token.
		String grammar =
			"grammar P;\n"+
			"a : A {System.out.println(input);} ;\n"+
			"A : '-' I ;\n" +
			"fragment I : '0'..'9'+ ;\n"+
			"WS : (' '|'\\n') {channel=99;} ;";
		String found = execParser("P.g", grammar, "PParser", "PLexer",
				    "a", "-34", debug);
		assertEquals("-34\n", found);
	}

	public void testMultipleRefToFragment() throws Exception {
		// this must return A not I to the parser; calling a nonfragment rule
		// from a nonfragment rule does not set the overall token.
		String grammar =
			"grammar P;\n"+
			"a : A EOF {System.out.println(input);} ;\n"+
			"A : I '.' I ;\n" +
			"fragment I : '0'..'9'+ ;\n"+
			"WS : (' '|'\\n') {channel=99;} ;";
		String found = execParser("P.g", grammar, "PParser", "PLexer",
				    "a", "3.14159", debug);
		assertEquals("3.14159\n", found);
	}

}
