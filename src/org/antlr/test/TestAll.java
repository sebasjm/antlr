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
	}
}
