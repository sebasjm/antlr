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
import org.antlr.tool.FASerializer;
import org.antlr.analysis.State;
import org.antlr.analysis.DFA;
import org.antlr.codegen.CodeGenerator;

import java.util.List;

import antlr.RecognitionException;

public class TestCharDFAConversion extends TestSuite {

	/** Public default constructor used by TestRig */
	public TestCharDFAConversion() {
	}

	// R A N G E S  &  S E T S

	public void testSimpleRangeVersusChar() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a'..'z' '@' | 'k' '$' ;");
		g.createLookaheadDFAs();
		String expecting =
			".s0-'k'->.s1\n" +
			".s0-{'a'..'j', 'l'..'z'}->:s3=>1\n" +
			".s1-'$'->:s2=>2\n" +
			".s1-'@'->:s3=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testRangeWithDisjointSet() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a'..'z' '@'\n" +
			"  | ('k'|'9'|'p') '$'\n" +
			"  ;\n");
		g.createLookaheadDFAs();
		// must break up a..z into {'a'..'j', 'l'..'o', 'q'..'z'}
		String expecting =
			".s0-'9'->:s2=>2\n" +
			".s0-{'a'..'j', 'l'..'o', 'q'..'z'}->:s3=>1\n" +
			".s0-{'k', 'p'}->.s1\n" +
			".s1-'$'->:s2=>2\n" +
			".s1-'@'->:s3=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testDisjointSetCollidingWithTwoRanges() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : ('a'..'z'|'0'..'9') '@'\n" +
			"  | ('k'|'9'|'p') '$'\n" +
			"  ;\n");
		g.createLookaheadDFAs();
		// must break up a..z into {'a'..'j', 'l'..'o', 'q'..'z'} and 0..9
		// into 0..8
		String expecting =
			".s0-{'0'..'8', 'a'..'j', 'l'..'o', 'q'..'z'}->:s3=>1\n" +
			".s0-{'9', 'k', 'p'}->.s1\n" +
			".s1-'$'->:s2=>2\n" +
			".s1-'@'->:s3=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testDisjointSetCollidingWithTwoRangesCharsFirst() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : ('k'|'9'|'p') '$'\n" +
			"  | ('a'..'z'|'0'..'9') '@'\n" +
			"  ;\n");
		g.createLookaheadDFAs();
		// must break up a..z into {'a'..'j', 'l'..'o', 'q'..'z'} and 0..9
		// into 0..8
		String expecting =
			".s0-{'0'..'8', 'a'..'j', 'l'..'o', 'q'..'z'}->:s2=>2\n" +
			".s0-{'9', 'k', 'p'}->.s1\n" +
			".s1-'$'->:s3=>1\n" +
			".s1-'@'->:s2=>2\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testDisjointSetCollidingWithTwoRangesAsSeparateAlts() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a'..'z' '@'\n" +
			"  | 'k' '$'\n" +
			"  | '9' '$'\n" +
			"  | 'p' '$'\n" +
			"  | '0'..'9' '@'\n" +
			"  ;\n");
		g.createLookaheadDFAs();
		// must break up a..z into {'a'..'j', 'l'..'o', 'q'..'z'} and 0..9
		// into 0..8
		String expecting =
			".s0-'0'..'8'->:s7=>5\n" +
			".s0-'9'->.s6\n" +
			".s0-'k'->.s1\n" +
			".s0-'p'->.s4\n" +
			".s0-{'a'..'j', 'l'..'o', 'q'..'z'}->:s3=>1\n" +
			".s1-'$'->:s2=>2\n" +
			".s1-'@'->:s3=>1\n" +
			".s4-'$'->:s5=>4\n" +
			".s4-'@'->:s3=>1\n" +
			".s6-'$'->:s8=>3\n" +
			".s6-'@'->:s7=>5\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testKeywordVersusID() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"IF : \"if\" ;\n" +
			"ID : ('a'..'z')+ ;\n");
		String expecting =
			".s0-'a'..'z'->:s2=>1\n" +
			".s0-<EOT>->:s1=>2\n";
		checkDecision(g, 1, expecting, null);
		expecting =
			".s0-'i'->.s1\n" +
			".s0-{'a'..'h', 'j'..'z'}->:s4=>2\n" +
			".s1-'f'->.s2\n" +
			".s1-<EOT>->:s4=>2\n" +
			".s2-'a'..'z'->:s4=>2\n" +
			".s2-<EOT>->:s3=>1\n";
		checkDecision(g, 2, expecting, null);
	}

	// N O N G R E E D Y

	public void testNonGreedy() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"CMT : \"/*\" ( options {greedy=false;} : . )* \"*/\" ;");
		String expecting =
			".s0-'*'->.s1\n" +
			".s0-{'\\u0000'..')', '+'..'\\uFFFE'}->:s3=>1\n" +
			".s1-'/'->:s2=>2\n" +
			".s1-{'\\u0000'..'.', '0'..'\\uFFFE'}->:s3=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNonWildcardNonGreedy() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"DUH : (options {greedy=false;}:'x'|'y')* \"xy\" ;");
		String expecting =
			".s0-'x'->.s1\n" +
			".s0-'y'->:s4=>2\n" +
			".s1-'x'->:s3=>1\n" +
			".s1-'y'->:s2=>3\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNonGreedyLoopThatNeverLoops() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"DUH : (options {greedy=false;}:'x')+ ;");
		String expecting =
			":s0=>2\n";
		checkDecision(g, 1, expecting, new int[] {1});
	}

	// S U P P O R T

	public void _template() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : A | B;");
		g.createLookaheadDFAs();
		String expecting =
			"\n";
		checkDecision(g, 1, expecting, null);
	}

	protected void checkDecision(Grammar g,
								 int decision,
								 String expecting,
								 int[] expectingUnreachableAlts)
		throws FailedAssertionException
	{
		// mimic actions of org.antlr.Tool first time for grammar g
		if ( g.getCodeGenerator()==null ) {
			CodeGenerator generator = new CodeGenerator(null, g, "Java");
			g.setCodeGenerator(generator);
			if ( g.type==Grammar.LEXER ) {
				g.addArtificialMatchTokensRule();
			}
			g.createNFAs();
			g.createLookaheadDFAs();
		}

		DFA dfa = g.getLookaheadDFA(decision);
		assertTrue(dfa!=null, "unknown decision #"+decision);
		FASerializer serializer = new FASerializer(g);
		String result = serializer.serialize(dfa.startState);
		//System.out.print(result);
		List nonDetAlts = dfa.getUnreachableAlts();
		//System.out.println("alts w/o predict state="+nonDetAlts);

		// first make sure nondeterministic alts are as expected
		if ( expectingUnreachableAlts==null ) {
			assertTrue(nonDetAlts.size()==0, "unreachable alts mismatch");
		}
		else {
			for (int i=0; i<expectingUnreachableAlts.length; i++) {
				assertTrue(nonDetAlts.contains(new Integer(expectingUnreachableAlts[i])),
						   "unreachable alts mismatch");
			}
		}
		assertEqual(result, expecting);
	}

}
