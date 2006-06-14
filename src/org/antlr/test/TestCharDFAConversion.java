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

import org.antlr.analysis.DFA;
import org.antlr.analysis.DFAOptimizer;
import org.antlr.codegen.CodeGenerator;
import org.antlr.test.unit.FailedAssertionException;
import org.antlr.test.unit.TestSuite;
import org.antlr.tool.*;

import java.util.List;

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
			".s0-'0'..'8'->:s8=>5\n" +
			".s0-'9'->.s6\n" +
			".s0-'k'->.s1\n" +
			".s0-'p'->.s4\n" +
			".s0-{'a'..'j', 'l'..'o', 'q'..'z'}->:s3=>1\n" +
			".s1-'$'->:s2=>2\n" +
			".s1-'@'->:s3=>1\n" +
			".s4-'$'->:s5=>4\n" +
			".s4-'@'->:s3=>1\n" +
			".s6-'$'->:s7=>3\n" +
			".s6-'@'->:s8=>5\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testKeywordVersusID() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"IF : 'if' ;\n" + // choose this over ID
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

	public void testIdenticalRules() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a' ;\n" +
			"B : 'a' ;\n"); // can't reach this
		String expecting =
			".s0-'a'->.s1\n" +
			".s1-<EOT>->:s2=>1\n";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);

		checkDecision(g, 1, expecting, new int[] {2});

		assertTrue(equeue.size()==1,
				   "unexpected number of expected problems: "+equeue.size()+
				   "; expecting "+1);
		Message msg = (Message)equeue.warnings.get(0);
		assertTrue(msg instanceof GrammarUnreachableAltsMessage,
				   "warning must be an unreachable alt");
		GrammarUnreachableAltsMessage u = (GrammarUnreachableAltsMessage)msg;
		assertEqual(u.alts.toString(), "[2]");
	}

	public void testAdjacentNotCharLoops() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : (~'r')+ ;\n" +
			"B : (~'s')+ ;\n");
		String expecting =
			".s0-'r'->:s3=>2\n" +
			".s0-'s'->:s2=>1\n" +
			".s0-{'\\u0000'..'q', 't'..'\\uFFFE'}->.s1\n" +
			".s1-'r'->:s3=>2\n" +
			".s1-<EOT>->:s2=>1\n" +
			".s1-{'\\u0000'..'q', 't'..'\\uFFFE'}->.s1\n";
		checkDecision(g, 3, expecting, null);
	}

	public void testNonAdjacentNotCharLoops() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : (~'r')+ ;\n" +
			"B : (~'t')+ ;\n");
		String expecting =
			".s0-'r'->:s3=>2\n" +
			".s0-'t'->:s2=>1\n" +
			".s0-{'\\u0000'..'q', 's', 'u'..'\\uFFFE'}->.s1\n" +
			".s1-'r'->:s3=>2\n" +
			".s1-<EOT>->:s2=>1\n" +
			".s1-{'\\u0000'..'q', 's', 'u'..'\\uFFFE'}->.s1\n";
		checkDecision(g, 3, expecting, null);
	}

	public void testLoopsWithOptimizedOutExitBranches() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'x'* ~'x'+ ;\n");
		String expecting =
			".s0-'x'->:s2=>1\n" +
			".s0-{'\\u0000'..'w', 'y'..'\\uFFFE'}->:s1=>2\n";
		checkDecision(g, 1, expecting, null);

		// The optimizer yanks out all exit branches from EBNF blocks
		// This is ok because we've already verified there are no problems
		// with the enter/exit decision
		DFAOptimizer optimizer = new DFAOptimizer(g);
		optimizer.optimize();
		FASerializer serializer = new FASerializer(g);
		DFA dfa = g.getLookaheadDFA(1);
		String result = serializer.serialize(dfa.startState);
		expecting = ".s0-'x'->:s1=>1\n";
		assertEqual(result, expecting);
	}

	// N O N G R E E D Y

	public void testNonGreedy() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"CMT : '/*' ( options {greedy=false;} : . )* '*/' ;");
		String expecting =
			".s0-'*'->.s1\n" +
			".s0-{'\\u0000'..')', '+'..'\\uFFFE'}->:s3=>1\n" +
			".s1-'/'->:s2=>2\n" +
			".s1-{'\\u0000'..'.', '0'..'\\uFFFE'}->:s3=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNonGreedyWildcardStar() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"SLCMT : '//' ( options {greedy=false;} : . )* '\n' ;");
		String expecting =
			".s0-'\\n'->:s1=>2\n" +
			".s0-{'\\u0000'..'\\t', '\\u000B'..'\\uFFFE'}->:s2=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNonGreedyByDefaultWildcardStar() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"SLCMT : '//' .* '\n' ;");
		String expecting =
			".s0-'\\n'->:s1=>2\n" +
			".s0-{'\\u0000'..'\\t', '\\u000B'..'\\uFFFE'}->:s2=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNonGreedyWildcardPlus() throws Exception {
		// same DFA as nongreedy .* but code gen checks number of
		// iterations at runtime
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"SLCMT : '//' ( options {greedy=false;} : . )+ '\n' ;");
		String expecting =
			".s0-'\\n'->:s1=>2\n" +
			".s0-{'\\u0000'..'\\t', '\\u000B'..'\\uFFFE'}->:s2=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNonGreedyByDefaultWildcardPlus() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"SLCMT : '//' .+ '\n' ;");
		String expecting =
			".s0-'\\n'->:s1=>2\n" +
			".s0-{'\\u0000'..'\\t', '\\u000B'..'\\uFFFE'}->:s2=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNonGreedyByDefaultWildcardPlusWithParens() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"SLCMT : '//' (.)+ '\n' ;");
		String expecting =
			".s0-'\\n'->:s1=>2\n" +
			".s0-{'\\u0000'..'\\t', '\\u000B'..'\\uFFFE'}->:s2=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNonWildcardNonGreedy() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"DUH : (options {greedy=false;}:'x'|'y')* 'xy' ;");
		String expecting =
			".s0-'x'->.s1\n" +
			".s0-'y'->:s4=>2\n" +
			".s1-'x'->:s3=>1\n" +
			".s1-'y'->:s2=>3\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNonWildcardEOTMakesItWorkWithoutNonGreedyOption() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"DUH : ('x'|'y')* 'xy' ;");
		String expecting =
			".s0-'x'->.s1\n" +
				".s0-'y'->:s3=>1\n" +
				".s1-'x'->:s3=>1\n" +
				".s1-'y'->.s2\n" +
				".s2-'x'..'y'->:s3=>1\n" +
				".s2-<EOT>->:s4=>2\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testAltConflictsWithLoopThenExit() throws Exception {
		// \" predicts alt 1, but wildcard then " can predict exit also
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"STRING : '\"' (options {greedy=false;}: '\\\\\"' | .)* '\"' ;\n"
		);
		String expecting =
			".s0-'\"'->:s1=>3\n" +
				".s0-'\\\\'->.s2\n" +
				".s0-{'\\u0000'..'!', '#'..'[', ']'..'\\uFFFE'}->:s4=>2\n" +
				".s2-'\"'->:s3=>1\n" +
				".s2-{'\\u0000'..'!', '#'..'\\uFFFE'}->:s4=>2\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNonGreedyLoopThatNeverLoops() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"DUH : (options {greedy=false;}:'x')+ ;"); // loop never matched
		String expecting =
			":s0=>2\n";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);

		checkDecision(g, 1, expecting, new int[] {1});

		assertTrue(equeue.size()==1,
				   "unexpected number of expected problems: "+equeue.size()+
				   "; expecting "+1);
		Message msg = (Message)equeue.warnings.get(0);
		assertTrue(msg instanceof GrammarUnreachableAltsMessage,
				   "warning must be an unreachable alt");
		GrammarUnreachableAltsMessage u = (GrammarUnreachableAltsMessage)msg;
		assertEqual(u.alts.toString(), "[1]");
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
			assertTrue(nonDetAlts.size()==0, "unreachable alts mismatch; should be empty: "+nonDetAlts);
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
