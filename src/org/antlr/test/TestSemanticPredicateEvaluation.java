package org.antlr.test;

import org.antlr.test.unit.TestSuite;

public class TestSemanticPredicateEvaluation extends TestSuite {
	public void testSimpleCyclicDFAWithPredicate() throws Exception {
		String grammar =
			"grammar foo;\n" +
			"a :         'x'* 'y' {System.out.println(\"alt1\");}\n" +
			"  | {true}? 'x'* 'y' {System.out.println(\"alt2\");}\n" +
			"  ;\n" ;
		String found =
			TestCompileAndExecSupport.execParser("foo.g", grammar, "foo", "fooLexer",
												 "a", "xxxy");
		String expecting = "alt2\n";
		assertEqual(found, expecting);
	}

	public void testSimpleCyclicDFAWithInstanceVarPredicate() throws Exception {
		String grammar =
			"grammar foo;\n" +
			"{boolean v=true;}\n" +
			"a :      'x'* 'y' {System.out.println(\"alt1\");}\n" +
			"  | {v}? 'x'* 'y' {System.out.println(\"alt2\");}\n" +
			"  ;\n" ;
		String found =
			TestCompileAndExecSupport.execParser("foo.g", grammar, "foo", "fooLexer",
												 "a", "xxxy");
		String expecting = "alt2\n";
		assertEqual(found, expecting);
	}

	// S U P P O R T

	public void _test() throws Exception {
		String grammar =
			"grammar T;\n" +
			"options {output=AST;}\n" +
			"a :  ;\n" +
			"ID : 'a'..'z'+ ;\n" +
			"INT : '0'..'9'+;\n" +
			"WS : (' '|'\\n') {channel=99;} ;\n";
		String found =
			TestCompileAndExecSupport.execParser("t.g", grammar, "T", "TLexer",
												 "a", "abc 34");
		String expecting = "\n";
		assertEqual(found, expecting);
	}

}
