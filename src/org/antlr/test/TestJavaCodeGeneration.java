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

import junit.framework.TestCase;

/** General code generation testing; compilation and/or execution.
 *  These tests are more about avoiding duplicate var definitions
 *  etc... than testing a particular ANTLR feature.
 */
public class TestJavaCodeGeneration extends BaseTest {
	public void testDupVarDefForPinchedState() {
		// so->s2 and s0->s3->s1 pinches back to s1
		// LA3_1, s1 state for DFA 3, was defined twice in similar scope
		// just wrapped in curlies and it's cool.
		String grammar =
			"grammar t;\n" +
			"a : (| A | B) X Y\n" +
			"  | (| A | B) X Z\n" +
			"  ;\n" ;
		boolean found =
			rawGenerateAndBuildRecognizer(
				"t.g", grammar, "tParser", null, false);
		boolean expecting = true; // should be ok
		assertEquals(expecting, found);
	}

	public void testLabeledSetsInLexer() {
		// c must be an int
		String grammar =
			"lexer grammar t;\n" +
			"A : c=('-'|'.')\n" +
			"  ; \n" ;
		boolean found =
			rawGenerateAndBuildRecognizer(
				"t.g", grammar, null, "tLexer", false);
		boolean expecting = true; // should be ok
		assertEquals(expecting, found);
	}

	public void testRepeatedLabelInLexer() {
		// currently fails; not sure it's worth fixing dup x var def
		String grammar =
			"lexer grammar t;\n" +
			"B : x='a' x='b' ;\n" ;
		boolean found =
			rawGenerateAndBuildRecognizer(
				"t.g", grammar, null, "tLexer", false);
		boolean expecting = true; // should be ok
		assertEquals(expecting, found);
	}

}
