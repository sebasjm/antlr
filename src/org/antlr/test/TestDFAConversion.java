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
import org.antlr.misc.BitSet;

import java.util.List;

import antlr.RecognitionException;

public class TestDFAConversion extends TestSuite {

	/** Public default constructor used by TestRig */
	public TestDFAConversion() {
	}

	public void testA() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : A C | B;");
		String expecting =
			".s0-A->:s1=>1\n" +
			".s0-B->:s2=>2\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testAB_or_AC() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : A B | A C;");
		String expecting =
			".s0-A->.s1\n" +
			".s1-B->:s2=>1\n" +
			".s1-C->:s3=>2\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testselfRecurseNonDet() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : A a X | A a Y;");
		// nondeterministic from left edge; no stop state
		String expecting =
			".s0-A->.s1\n" +
			".s1-A->.s2\n" +
			".s2-A->.s3\n";
		checkDecision(g, 1, expecting, new int[] {1,2});
	}

	public void testselfRecurseNonDet2() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : P a P | P;");
		// nondeterministic from left edge; no stop state
		String expecting =
			".s0-P->.s1\n" +
			".s1-P->:s2=>1\n";
		checkDecision(g, 1, expecting, new int[] {2});
	}

	public void testifThenElse() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"s : IF s (E s)? | B;\n" +
			"slist: s SEMI ;");
		String expecting =
			".s0-E->:s1=>1\n" +
			".s0-SEMI->:s2=>2\n";
		checkDecision(g, 1, expecting, null);
		expecting =
			".s0-B->:s2=>2\n" +
			".s0-IF->:s1=>1\n";
		checkDecision(g, 2, expecting, null);
	}

	public void testinvokeRule() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
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
		checkDecision(g, 1, expecting, null);
	}

	public void testDoubleInvokeRuleLeftEdge() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
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
		checkDecision(g, 1, expecting, null);
		expecting =
			".s0-C->.s1\n" +
			".s1-B->:s3=>1\n" +
			".s1-X..Y->:s2=>2\n";
		checkDecision(g, 2, expecting, null);
	}

	public void testimmediateTailRecursion() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : A a | A B;");
		String expecting =
			".s0-A->.s1\n" +
			".s1-A->:s2=>1\n" +
			".s1-B->:s3=>2\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testAStar_immediateTailRecursion() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : A a | ;");
		String expecting =
			".s0-A->:s1=>1\n";
		checkDecision(g, 1, expecting, new int[] {2});
	}

	public void testimmediateLeftRecursion() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : a A | B;");
		String expecting =
			".s0-B->:s1=>1\n";
		checkDecision(g, 1, expecting, new int[] {2});
	}

	// L O O P S

	public void testAStar() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : ( A )* ;");
		String expecting =
			".s0-<EOF>->:s1=>2\n" +
			".s0-A->:s2=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testAorBorCStar() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : ( A | B | C )* ;");
		String expecting =
			".s0-<EOF>->:s1=>2\n" +
			".s0-A..C->:s2=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testAPlus() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : ( A )+ ;");
		String expecting =
			".s0-<EOF>->:s1=>2\n" +
			".s0-A->:s2=>1\n";
		checkDecision(g, 1, expecting, null); // loopback decision
	}

	public void testAPlusNonGreedyWhenDeterministic() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : (options {greedy=false;}:A)+ ;\n");
		// should look the same as A+ since no ambiguity
		String expecting =
			".s0-<EOF>->:s1=>2\n" +
			".s0-A->:s2=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testAorBorCPlus() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : ( A | B | C )+ ;");
		String expecting =
			".s0-<EOF>->:s1=>2\n" +
			".s0-A..C->:s2=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testAOptional() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : ( A )? B ;");
		String expecting =
			".s0-A->:s1=>1\n" +
			".s0-B->:s2=>2\n";
		checkDecision(g, 1, expecting, null); // loopback decision
	}

	public void testAorBorCOptional() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : ( A | B | C )? Z ;");
		String expecting =
			".s0-A..C->:s1=>1\n" +
			".s0-Z->:s2=>2\n";
		checkDecision(g, 1, expecting, null); // loopback decision
	}

	// A R B I T R A R Y  L O O K A H E A D

	public void testAStarBOrAStarC() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : (A)* B | (A)* C;");
		String expecting =
			".s0-A->:s2=>1\n" +
			".s0-B->:s1=>2\n";
		checkDecision(g, 1, expecting, null); // loopback
		expecting =
			".s0-A->:s2=>1\n" +
			".s0-C->:s1=>2\n";
		checkDecision(g, 2, expecting, null); // loopback
		expecting =
			".s0-A->.s1\n" +
			".s0-B->:s2=>1\n" +
			".s0-C->:s3=>2\n" +
			".s1-A->.s1\n" +
			".s1-B->:s2=>1\n" +
			".s1-C->:s3=>2\n";
		checkDecision(g, 3, expecting, null); // rule block
	}


	public void testAStarBOrAPlusC() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : (A)* B | (A)+ C;");
		String expecting =
			".s0-A->:s2=>1\n" +
			".s0-B->:s1=>2\n";
		checkDecision(g, 1, expecting, null); // loopback
		expecting =
			".s0-A->:s2=>1\n" +
			".s0-C->:s1=>2\n";
		checkDecision(g, 2, expecting, null); // loopback
		expecting =
			".s0-A->.s1\n" +
			".s0-B->:s2=>1\n" +
			".s1-A->.s1\n" +
			".s1-B->:s2=>1\n" +
			".s1-C->:s3=>2\n";
		checkDecision(g, 3, expecting, null); // rule block
	}


	public void testAOrBPlusOrAPlus() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : (A|B)* X | (A)+ Y;");
		String expecting =
			".s0-A..B->:s2=>1\n" +
			".s0-X->:s1=>2\n";
		checkDecision(g, 1, expecting, null); // loopback (A|B)*
		expecting =
			".s0-A->:s2=>1\n" +
			".s0-Y->:s1=>2\n";
		checkDecision(g, 2, expecting, null); // loopback (A)+
		expecting =
			".s0-A->.s1\n" +
			".s0-B..X->:s2=>1\n" +
			".s1-A->.s1\n" +
			".s1-B..X->:s2=>1\n" +
			".s1-Y->:s3=>2\n";
		checkDecision(g, 3, expecting, null); // rule
	}

	public void testLoopbackAndExit() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : (A|B)+ B;");
		String expecting =
			".s0-A->:s2=>1\n" +
			".s0-B->.s1\n" +
			".s1-<EOF>->:s3=>2\n" +
			".s1-A..B->:s2=>1\n"; // sees A|B as a set
		checkDecision(g, 1, expecting, null);
	}

	public void testOptionalAltAndBypass() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : (A|B)? B;");
		String expecting =
			".s0-A->:s2=>1\n" +
			".s0-B->.s1\n" +
			".s1-<EOF>->:s3=>2\n" +
			".s1-B->:s2=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	// R E S O L V E  S Y N  C O N F L I C T S

	public void testResolveLL1ByChoosingFirst() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : A C | A C;");
		String expecting =
			".s0-A->.s1\n" +
			".s1-C->:s2=>1\n";
		checkDecision(g, 1, expecting, new int[] {2});
	}

	public void testResolveLL2ByChoosingFirst() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : A B | A B;");
		String expecting =
			".s0-A->.s1\n" +
			".s1-B->:s2=>1\n";
		checkDecision(g, 1, expecting, new int[] {2});
	}

	public void testResolveLL2MixAlt() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : A B | A C | A B | Z;");
		String expecting =
			".s0-A->.s1\n" +
			".s0-Z->:s4=>4\n" +
			".s1-B->:s2=>1\n" +
			".s1-C->:s3=>2\n";
		checkDecision(g, 1, expecting, new int[] {3});
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
			"grammar t;\n" +
			"s : LCURLY ( cg )* RCURLY | E SEMI  ;\n" +
			"cg : (c)+ (s)* ;\n" +
			"c : CASE E ;\n");
		String expecting =
			".s0-CASE->.s2\n" +
			".s0-LCURLY..E->:s1=>2\n" +
			".s2-E->:s3=>1\n";
		checkDecision(g, 3, expecting, null);
	}

	// S E T S

	public void testComplement() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : ~(A | B | C) | C;\n" +
			"b : X Y Z ;");
		String expecting =
			".s0-C->:s2=>2\n" +
			".s0-X..Z->:s1=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testComplementToken() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : ~C | C;\n" +
			"b : X Y Z ;");
		String expecting =
			".s0-C->:s2=>2\n" +
			".s0-X..Z->:s1=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testComplementChar() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : ~'x' | 'x';\n");
		String expecting =
			".s0-'x'->:s2=>2\n" +
			".s0-{'\\u0000'..'w', 'y'..'\\uFFFE'}->:s1=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testComplementCharSet() throws Exception {
		Grammar g = new Grammar(
			"lexer grammar t;\n"+
			"A : ~(' '|'\t'|'x') | 'x';\n");
		String expecting =
			".s0-'x'->:s2=>2\n" +
			".s0-{'\\u0000'..'\\b', '\\n'..'\\u001F', '!'..'w', 'y'..'\\uFFFE'}->:s1=>1\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testNoSetCollapseWithActions() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : (A | B {foo}) | C;");
		String expecting =
			".s0-A->:s1=>1\n" +
			".s0-B->:s2=>2\n";
		checkDecision(g, 1, expecting, null);
	}

	public void testRuleAltsSetCollapse() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
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
		checkDecision(g, 1, expecting, null);
	}

	public void testMultipleSequenceCollision() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n" +
			"a : (A{;}|B)\n" +
			"  | (A{;}|B)\n" +
			"  | A\n" +
			"  ;");
		String expecting =
			".s0-A..B->:s1=>1\n"; // after optimization
		checkDecision(g, 3, expecting, new int[] {2,3});
	}

	public void testMultipleAltsSameSequenceCollision() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n" +
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
		checkDecision(g, 1, expecting, new int[] {2,3,4});
	}

	public void testFollowReturnsToLoopReenteringSameRule() throws Exception {
		// D07 can be matched in the (...)? or fall out of esc back into (..)*
		// loop in sl.  Note that D07 is matched by ~(R|SLASH).  No good
		// way to write that grammar I guess
		Grammar g = new Grammar(
			"grammar t;\n"+
			"sl : L ( esc | ~(R|SLASH) )* R ;\n" +
			"\n" +
			"esc : SLASH ( N | D03 (D07)? ) ;");
		String expecting =
			".s0-R->:s1=>3\n" +
			".s0-SLASH->:s2=>1\n" +
			".s0-{L, N..D07}->:s3=>2\n";
		checkDecision(g, 1, expecting, null);
	}


	public void testSelfRecursionAmbigAlts() throws Exception {
		// ambiguous grammar for "L ID R" (alts 1,2 of a)
		Grammar g = new Grammar(
			"grammar t;\n"+
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
		checkDecision(g, 1, expecting, null);
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
		checkDecision(g, 1, expecting, null);
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
		checkDecision(g, 1, expecting, null);
	}

	// S U P P O R T

	public void _template() throws Exception {
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : A | B;");
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
		if ( g.getNumberOfDecisions()==0 ) {
			if ( g.type==Grammar.LEXER ) {
				g.addArtificialMatchTokensRule();
			}
			g.createNFAs();
			g.createLookaheadDFAs();
		}

		DFA dfa = g.getLookaheadDFA(decision);
		assertTrue(dfa!=null, "no DFA for decision "+decision);
		FASerializer serializer = new FASerializer(g);
		String result = serializer.serialize(dfa.startState);
		//System.out.print(result);
		List nonDetAlts = dfa.getUnreachableAlts();

		// first make sure nondeterministic alts are as expected
		BitSet s = new BitSet();
		s.addAll(expectingUnreachableAlts);
		if ( expectingUnreachableAlts==null ) {
			assertTrue(nonDetAlts.size()==0,
					   "unreachable alts mismatch; expecting="+
					   s.toString()+" found "+nonDetAlts);
		}
		else {
			for (int i=0; i<expectingUnreachableAlts.length; i++) {
				assertTrue(nonDetAlts.contains(new Integer(expectingUnreachableAlts[i])),
						   "unreachable alts mismatch; expecting "+s.toString()+" found "+nonDetAlts);
			}
		}
		assertEqual(result, expecting);
	}

}
