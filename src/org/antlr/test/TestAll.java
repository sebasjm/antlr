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

import org.antlr.test.unit.TestSuite;
import org.antlr.test.unit.TestRig;

public class TestAll extends TestSuite {
	public void runTests() {
		TestSuite test = null;
		test = TestRig.runAllTests(TestCharDFAConversion.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestInterpretedParsing.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestIntervalSet.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestDFAConversion.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestNFAConstruction.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestDFAMatching.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestSemanticPredicates.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestInterpretedLexing.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestSymbolDefinitions.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestAttributes.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestAutoAST.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());

		test = TestRig.runAllTests(TestRewriteAST.class);
		System.out.println("successes: "+test.getSuccesses());
		System.out.println("failures: "+test.getFailures());
	}
}
