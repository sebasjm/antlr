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
import org.antlr.tool.*;
import org.antlr.analysis.State;
import org.antlr.analysis.DFA;
import org.antlr.analysis.DecisionProbe;
import org.antlr.codegen.CodeGenerator;
import org.antlr.misc.BitSet;

import java.util.List;

import antlr.RecognitionException;

public class TestDFAConversion extends TestSuite {

	/** Public default constructor used by TestRig */
	public TestDFAConversion() {
	}

	public void testA() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : A C | B;");
		String expecting =
			".s0-A->:s1=>1\n" +
			".s0-B->:s2=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testAB_or_AC() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : A B | A C;");
		String expecting =
			".s0-A->.s1\n" +
			".s1-B->:s2=>1\n" +
			".s1-C->:s3=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testselfRecurseNonDet() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : A a X | A a Y;");
		// nondeterministic from left edge; no stop state
		String expecting =
			".s0-A->.s1\n" +
			".s1-A->.s2\n" +
			".s2-A->.s3\n";
		int[] unreachableAlts = new int[] {1,2};
		int[] nonDetAlts = null;
		String ambigInput = null;
		int[] danglingAlts = new int[] {1,2};
		int numWarnings = 2;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	public void testselfRecurseNonDet2() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : P a P | P;");
		// nondeterministic from left edge; no stop state
		String expecting =
			".s0-P->.s1\n" +
			".s1-P->:s2=>1\n";
		int[] unreachableAlts = new int[] {2};
		int[] nonDetAlts = new int[] {1,2};
		String ambigInput = "P P";
		int[] danglingAlts = null;
		int numWarnings = 2;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	public void testifThenElse() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"s : IF s (E s)? | B;\n" +
			"slist: s SEMI ;");
		String expecting =
			".s0-E->:s1=>1\n" +
			".s0-SEMI->:s2=>2\n";
		int[] unreachableAlts = null;
		int[] nonDetAlts = new int[] {1,2};
		String ambigInput = "E";
		int[] danglingAlts = null;
		int numWarnings = 1;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
		expecting =
			".s0-B->:s2=>2\n" +
			".s0-IF->:s1=>1\n";
		checkDecision(g, 2, expecting, null, null, null, null, 0);
	}

	public void testinvokeRule() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : b A\n" +
			"  | b B\n" +
			"  | C\n" +
			"  ;\n" +
			"b : X\n" +
			"  ;\n");
		String expecting =
			".s0-C->:s4=>3\n" +
			".s0-X->.s1\n" +
			".s1-A->:s3=>1\n" +
			".s1-B->:s2=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testDoubleInvokeRuleLeftEdge() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : b X\n" +
			"  | b Y\n" +
			"  ;\n" +
			"b : c B\n" +
			"  | c\n" +
			"  ;\n" +
			"c : C ;\n");
		String expecting =
			".s0-C->.s1\n" +
			".s1-B->.s4\n" +
			".s1-X->:s2=>1\n" +
			".s1-Y->:s3=>2\n" +
			".s4-X->:s2=>1\n" +
			".s4-Y->:s3=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
		expecting =
			".s0-C->.s1\n" +
			".s1-B->:s3=>1\n" +
			".s1-X..Y->:s2=>2\n";
		checkDecision(g, 2, expecting, null, null, null, null, 0);
	}

	public void testimmediateTailRecursion() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : A a | A B;");
		String expecting =
			".s0-A->.s1\n" +
			".s1-A->:s2=>1\n" +
			".s1-B->:s3=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testAStar_immediateTailRecursion() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : A a | ;");
		String expecting =
			".s0-A->:s1=>1\n";
		int[] unreachableAlts = new int[] {2};
		int[] nonDetAlts = null;
		String ambigInput = null;
		int[] danglingAlts = null;
		int numWarnings = 1;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	public void testimmediateLeftRecursion() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : a A | B;");
		String expecting =
			".s0-B->:s1=>1\n";
		int[] unreachableAlts = new int[] {2};
		int[] nonDetAlts = new int[] {1,2};
		String ambigInput = "B";
		int[] danglingAlts = null;
		int numWarnings = 2;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	// L O O P S

	public void testAStar() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : ( A )* ;");
		String expecting =
			".s0-<EOF>->:s1=>2\n" +
			".s0-A->:s2=>1\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testAorBorCStar() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : ( A | B | C )* ;");
		String expecting =
			".s0-<EOF>->:s1=>2\n" +
			".s0-A..C->:s2=>1\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testAPlus() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : ( A )+ ;");
		String expecting =
			".s0-<EOF>->:s1=>2\n" +
			".s0-A->:s2=>1\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0); // loopback decision
	}

	public void testAPlusNonGreedyWhenDeterministic() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : (options {greedy=false;}:A)+ ;\n");
		// should look the same as A+ since no ambiguity
		String expecting =
			".s0-<EOF>->:s1=>2\n" +
			".s0-A->:s2=>1\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testAorBorCPlus() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : ( A | B | C )+ ;");
		String expecting =
			".s0-<EOF>->:s1=>2\n" +
			".s0-A..C->:s2=>1\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testAOptional() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : ( A )? B ;");
		String expecting =
			".s0-A->:s1=>1\n" +
			".s0-B->:s2=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0); // loopback decision
	}

	public void testAorBorCOptional() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : ( A | B | C )? Z ;");
		String expecting =
			".s0-A..C->:s1=>1\n" +
			".s0-Z->:s2=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0); // loopback decision
	}

	// A R B I T R A R Y  L O O K A H E A D

	public void testAStarBOrAStarC() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : (A)* B | (A)* C;");
		String expecting =
			".s0-A->:s2=>1\n" +
			".s0-B->:s1=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0); // loopback
		expecting =
			".s0-A->:s2=>1\n" +
			".s0-C->:s1=>2\n";
		checkDecision(g, 2, expecting, null, null, null, null, 0); // loopback
		expecting =
			".s0-A->.s1\n" +
			".s0-B->:s2=>1\n" +
			".s0-C->:s3=>2\n" +
			".s1-A->.s1\n" +
			".s1-B->:s2=>1\n" +
			".s1-C->:s3=>2\n";
		checkDecision(g, 3, expecting, null, null, null, null, 0); // rule block
	}


	public void testAStarBOrAPlusC() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : (A)* B | (A)+ C;");
		String expecting =
			".s0-A->:s2=>1\n" +
			".s0-B->:s1=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0); // loopback
		expecting =
			".s0-A->:s2=>1\n" +
			".s0-C->:s1=>2\n";
		checkDecision(g, 2, expecting, null, null, null, null, 0); // loopback
		expecting =
			".s0-A->.s1\n" +
			".s0-B->:s2=>1\n" +
			".s1-A->.s1\n" +
			".s1-B->:s2=>1\n" +
			".s1-C->:s3=>2\n";
		checkDecision(g, 3, expecting, null, null, null, null, 0); // rule block
	}


	public void testAOrBPlusOrAPlus() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : (A|B)* X | (A)+ Y;");
		String expecting =
			".s0-A..B->:s2=>1\n" +
			".s0-X->:s1=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0); // loopback (A|B)*
		expecting =
			".s0-A->:s2=>1\n" +
			".s0-Y->:s1=>2\n";
		checkDecision(g, 2, expecting, null, null, null, null, 0); // loopback (A)+
		expecting =
			".s0-A->.s1\n" +
			".s0-B..X->:s2=>1\n" +
			".s1-A->.s1\n" +
			".s1-B..X->:s2=>1\n" +
			".s1-Y->:s3=>2\n";
		checkDecision(g, 3, expecting, null, null, null, null, 0); // rule
	}

	public void testLoopbackAndExit() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : (A|B)+ B;");
		String expecting =
			".s0-A->:s2=>1\n" +
			".s0-B->.s1\n" +
			".s1-<EOF>->:s3=>2\n" +
			".s1-A..B->:s2=>1\n"; // sees A|B as a set
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testOptionalAltAndBypass() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : (A|B)? B;");
		String expecting =
			".s0-A->:s2=>1\n" +
			".s0-B->.s1\n" +
			".s1-<EOF>->:s3=>2\n" +
			".s1-B->:s2=>1\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	// R E S O L V E  S Y N  C O N F L I C T S

	public void testResolveLL1ByChoosingFirst() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : A C | A C;");
		String expecting =
			".s0-A->.s1\n" +
			".s1-C->:s2=>1\n";
		int[] unreachableAlts = new int[] {2};
		int[] nonDetAlts = new int[] {1,2};
		String ambigInput = "A C";
		int[] danglingAlts = null;
		int numWarnings = 2;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	public void testResolveLL2ByChoosingFirst() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : A B | A B;");
		String expecting =
			".s0-A->.s1\n" +
			".s1-B->:s2=>1\n";
		int[] unreachableAlts = new int[] {2};
		int[] nonDetAlts = new int[] {1,2};
		String ambigInput = "A B";
		int[] danglingAlts = null;
		int numWarnings = 2;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	public void testResolveLL2MixAlt() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : A B | A C | A B | Z;");
		String expecting =
			".s0-A->.s1\n" +
			".s0-Z->:s4=>4\n" +
			".s1-B->:s2=>1\n" +
			".s1-C->:s3=>2\n";
		int[] unreachableAlts = new int[] {3};
		int[] nonDetAlts = new int[] {1,3};
		String ambigInput = "A B";
		int[] danglingAlts = null;
		int numWarnings = 2;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	public void testIndirectIFThenElseStyleAmbig() throws Exception {
		// the (c)+ loopback is ambig because it could match "CASE E"
		// by entering the loop or by falling out and ignoring (s)*
		// back falling back into (cg)* loop which stats over and
		// calls cg again.  Either choice allows it to get back to
		// the same node.  The software catches it as:
		// "avoid infinite closure computation emanating from alt 1 of ():27|2|[8 $]"
		// where state 27 is the first alt of (c)+ and 8 is the first alt
		// of the (cg)* loop.
		Grammar g = new Grammar(
			"parser grammar t;\n" +
			"s : LCURLY ( cg )* RCURLY | E SEMI  ;\n" +
			"cg : (c)+ (s)* ;\n" +
			"c : CASE E ;\n");
		String expecting =
			".s0-CASE->.s2\n" +
			".s0-LCURLY..E->:s1=>2\n" +
			".s2-E->:s3=>1\n";
		int[] unreachableAlts = null;
		int[] nonDetAlts = new int[] {1,2};
		String ambigInput = "CASE E";
		int[] danglingAlts = null;
		int numWarnings = 1;
		checkDecision(g, 3, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	// S E T S

	public void testComplement() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : ~(A | B | C) | C;\n" +
			"b : X Y Z ;");
		String expecting =
			".s0-C->:s2=>2\n" +
			".s0-X..Z->:s1=>1\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testComplementToken() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : ~C | C;\n" +
			"b : X Y Z ;");
		String expecting =
			".s0-C->:s2=>2\n" +
			".s0-X..Z->:s1=>1\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testComplementChar() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : ~'x' | 'x';\n");
		String expecting =
			".s0-'x'->:s2=>2\n" +
			".s0-{'\\u0000'..'w', 'y'..'\\uFFFE'}->:s1=>1\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testComplementCharSet() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : ~(' '|'\t'|'x') | 'x';\n");
		String expecting =
			".s0-'x'->:s2=>2\n" +
			".s0-{'\\u0000'..'\\b', '\\n'..'\\u001F', '!'..'w', 'y'..'\\uFFFE'}->:s1=>1\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testNoSetCollapseWithActions() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : (A | B {foo}) | C;");
		String expecting =
			".s0-A->:s1=>1\n" +
			".s0-B->:s2=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testRuleAltsSetCollapse() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : A | B | C ;"
		);
		String expecting =
			" ( grammar t ( rule a ARG RET INITACTION ( BLOCK ( ALT ( SET A B C ) EOA ) EOB ) <end-of-rule> ) )";
		assertEqual(g.getGrammarTree().toStringTree(),
					expecting);
	}

	public void testTokensRuleAltsDoNotCollapse() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : 'a';" +
			"B : 'b';\n"
		);
		String expecting =
			".s0-'a'->:s1=>1\n" +
			".s0-'b'->:s2=>2\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	public void testMultipleSequenceCollision() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n" +
			"a : (A{;}|B)\n" +
			"  | (A{;}|B)\n" +
			"  | A\n" +
			"  ;");
		String expecting =
			".s0-A->:s1=>1\n" +
			".s0-B->:s2=>1\n"; // not optimized because states are nondet
		int[] unreachableAlts = new int[] {2,3};
		int[] nonDetAlts = new int[] {1,2,3};
		String ambigInput = "A";
		int[] danglingAlts = null;
		int numWarnings = 3;
		checkDecision(g, 3, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
		/* There are 2 nondet errors, but the checkDecision only checks first one :(
		   The "B" conflicting input is not checked except by virtue of the
		   result DFA.
<string>:2:5: Decision can match input such as "A" using multiple alternatives:
  alt 1 via NFA path 7,2,3
  alt 2 via NFA path 14,9,10
  alt 3 via NFA path 16,17
As a result, alternative(s) 2,3 were disabled for that input,
<string>:2:5: Decision can match input such as "B" using multiple alternatives:
  alt 1 via NFA path 7,8,4,5
  alt 2 via NFA path 14,15,11,12
As a result, alternative(s) 2 were disabled for that input
<string>:2:5: The following alternatives are unreachable: 2,3
		*/
	}

	public void testMultipleAltsSameSequenceCollision() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n" +
			"a : type ID \n" +
			"  | type ID\n" +
			"  | type ID\n" +
			"  | type ID\n" +
			"  ;\n" +
			"\n" +
			"type : I | F;");
		// nondeterministic from left edge; no stop state
		String expecting =
			".s0-I..F->.s1\n" +
			".s1-ID->:s2=>1\n";
		int[] unreachableAlts = new int[] {2,3,4};
		int[] nonDetAlts = new int[] {1,2,3,4};
		String ambigInput = "I..F ID";
		int[] danglingAlts = null;
		int numWarnings = 2;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	public void testFollowReturnsToLoopReenteringSameRule() throws Exception {
		// D07 can be matched in the (...)? or fall out of esc back into (..)*
		// loop in sl.  Note that D07 is matched by ~(R|SLASH).  No good
		// way to write that grammar I guess
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"sl : L ( esc | ~(R|SLASH) )* R ;\n" +
			"\n" +
			"esc : SLASH ( N | D03 (D07)? ) ;");
		String expecting =
			".s0-R->:s1=>3\n" +
			".s0-SLASH->:s2=>1\n" +
			".s0-{L, N..D07}->:s3=>2\n";
		int[] unreachableAlts = null;
		int[] nonDetAlts = new int[] {1,2};
		String ambigInput = "D07";
		int[] danglingAlts = null;
		int numWarnings = 1;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}


	public void testSelfRecursionAmbigAlts() throws Exception {
		// ambiguous grammar for "L ID R" (alts 1,2 of a)
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a   :   L ID R\n" +
			"    |   L a R\n" + // disabled for L ID R
			"    |   b\n" +
			"    ;\n" +
			"\n" +
			"b   :   ID\n" +
			"    ;\n");
		String expecting =
			".s0-ID->:s5=>3\n" +
			".s0-L->.s1\n" +
			".s1-ID->.s3\n" +
			".s1-L->:s2=>2\n" +
			".s3-R->:s4=>1\n";
		int[] unreachableAlts = null;
		int[] nonDetAlts = new int[] {1,2};
		String ambigInput = "L ID R";
		int[] danglingAlts = null;
		int numWarnings = 1;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	public void testIndirectRecursionAmbigAlts() throws Exception {
		// ambiguous grammar for "L ID R" (alts 1,2 of a)
		// This was derived from the java grammar 12/4/2004 when it
		// was not handling a unaryExpression properly.  I traced it
		// to incorrect closure-busy condition.  It thought that the trace
		// of a->b->a->b again for "L ID" was an infinite loop, but actually
		// the repeat call to b only happens *after* an L has been matched.
		// I added a check to see what the initial stack looks like and it
		// seems to work now.
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a   :   L ID R\n" +
			"    |   b\n" +
			"    ;\n" +
			"\n" +
			"b   :   ID\n" +
			"    |   L a R\n" +
			"    ;");
		String expecting =
			".s0-ID->:s2=>2\n" +
			".s0-L->.s1\n" +
			".s1-ID->.s3\n" +
			".s1-L->:s2=>2\n" +
			".s3-R->:s4=>1\n";
		int[] unreachableAlts = null;
		int[] nonDetAlts = new int[] {1,2};
		String ambigInput = "L ID R";
		int[] danglingAlts = null;
		int numWarnings = 1;
		checkDecision(g, 1, expecting, unreachableAlts,
					  nonDetAlts, ambigInput, danglingAlts, numWarnings);
	}

	public void testNoSetForTokenRefsInLexer() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar P;\n"+
			"A : (B | C) ;\n"+
			"fragment B : 'b' ;\n" +
			"fragment C : 'c' ;\n"
		);
		String expecting =
			".s0-'b'->:s1=>1\n" +  // must not collapse set!
			".s0-'c'->:s2=>2\n";
		// no decision if (B|C) collapses; must not collapse
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	// S U P P O R T

	public void _template() throws Exception {
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : A | B;");
		String expecting =
			"\n";
		checkDecision(g, 1, expecting, null, null, null, null, 0);
	}

	protected void checkDecision(Grammar g,
								 int decision,
								 String expecting,
								 int[] expectingUnreachableAlts,
								 int[] expectingNonDetAlts,
								 String expectingAmbigInput,
								 int[] expectingDanglingAlts,
								 int expectingNumWarnings)
		throws FailedAssertionException
	{
		DecisionProbe.verbose=true; // make sure we get all error info
		TestSemanticPredicates.ErrorQueue equeue = new TestSemanticPredicates.ErrorQueue();
		ErrorManager.setErrorListener(equeue);

		// mimic actions of org.antlr.Tool first time for grammar g
		if ( g.getNumberOfDecisions()==0 ) {
			if ( g.type==Grammar.LEXER ) {
				g.addArtificialMatchTokensRule();
			}
			g.createNFAs();
			g.createLookaheadDFAs();
		}

		if ( equeue.size()!=expectingNumWarnings ) {
			System.err.println("Warnings issued: "+equeue.warnings);
		}

		assertTrue(equeue.size()==expectingNumWarnings,
				   "unexpected number of expected problems: "+equeue.size()+
				   "; expecting "+expectingNumWarnings);

		DFA dfa = g.getLookaheadDFA(decision);
		assertTrue(dfa!=null, "no DFA for decision "+decision);
		FASerializer serializer = new FASerializer(g);
		String result = serializer.serialize(dfa.startState);

		List unreachableAlts = dfa.getUnreachableAlts();

		// make sure unreachable alts are as expected
		if ( expectingUnreachableAlts!=null ) {
			BitSet s = new BitSet();
			s.addAll(expectingUnreachableAlts);
			BitSet s2 = new BitSet();
			s2.addAll(unreachableAlts);
			assertTrue(s.equals(s2), "unreachable alts mismatch; expecting "+s+
									 " found "+s2);
		}
		else {
			assertTrue(unreachableAlts.size()==0,
					"unreachable alts mismatch; expecting none found "+
					unreachableAlts);
		}

		// check conflicting input
		if ( expectingAmbigInput!=null ) {
			// first, find nondet message
			Message msg = (Message)equeue.warnings.get(0);
			assertTrue(msg instanceof GrammarNonDeterminismMessage,
					   "expecting nondeterminism; found "+msg.getClass().getName());
			GrammarNonDeterminismMessage nondetMsg =
				getNonDeterminismMessage(equeue.warnings);
			List labels =
				nondetMsg.probe.getSampleNonDeterministicInputSequence(nondetMsg.problemState);
			String input = nondetMsg.probe.getInputSequenceDisplay(labels);
			assertEqual(input, expectingAmbigInput);
		}

		// check nondet alts
		if ( expectingNonDetAlts!=null ) {
			GrammarNonDeterminismMessage nondetMsg =
				getNonDeterminismMessage(equeue.warnings);
			assertTrue(nondetMsg!=null, "found no nondet alts; expecting: "+
										str(expectingNonDetAlts));
			List nonDetAlts =
				nondetMsg.probe.getNonDeterministicAltsForState(nondetMsg.problemState);
            // compare nonDetAlts with expectingNonDetAlts
			BitSet s = new BitSet();
			s.addAll(expectingNonDetAlts);
			BitSet s2 = new BitSet();
			s2.addAll(nonDetAlts);
			assertTrue(s.equals(s2), "nondet alts mismatch; expecting "+s+" found "+s2);
		}
		else {
			// not expecting any nondet alts, make sure there are none
			GrammarNonDeterminismMessage nondetMsg =
				getNonDeterminismMessage(equeue.warnings);
			assertTrue(nondetMsg==null, "found nondet alts, but expecting none");
		}

		assertEqual(result, expecting);
	}

	protected GrammarNonDeterminismMessage getNonDeterminismMessage(List warnings) {
		for (int i = 0; i < warnings.size(); i++) {
			Message m = (Message) warnings.get(i);
			if ( m instanceof GrammarNonDeterminismMessage ) {
				return (GrammarNonDeterminismMessage)m;
			}
		}
		return null;
	}

	protected GrammarDanglingStateMessage getDanglingStateMessage(List warnings) {
		for (int i = 0; i < warnings.size(); i++) {
			Message m = (Message) warnings.get(i);
			if ( m instanceof GrammarDanglingStateMessage ) {
				return (GrammarDanglingStateMessage)m;
			}
		}
		return null;
	}

	protected String str(int[] elements) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < elements.length; i++) {
			if ( i>0 ) {
				buf.append(", ");
			}
			int element = elements[i];
			buf.append(element);
		}
		return buf.toString();
	}

}
