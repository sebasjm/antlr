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

    public void testdoubleInvokeRuleLeftEdge() throws Exception {
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
                ".s1-B->:s4=>1\n" +
                ".s1-X->:s2=>2\n" +
                ".s1-Y->:s3=>2\n";
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
                ".s0-B->:s4=>1\n" +
                ".s0-X->:s2=>1\n" +
                ".s1-A->.s1\n" +
                ".s1-B->:s4=>1\n" +
                ".s1-X->:s2=>1\n" +
                ".s1-Y->:s3=>2\n";
        checkDecision(g, 3, expecting, null); // rule
    }

    public void testLoopbackAndExit() throws Exception {
        Grammar g = new Grammar(
                "grammar t;\n"+
                "a : (A|B)+ B;");
        String expecting =
                ".s0-A->:s3=>1\n" +
                ".s0-B->.s1\n" +
                ".s1-<EOF>->:s4=>2\n" +
                ".s1-A->:s3=>1\n" +
                ".s1-B->:s2=>1\n"; // sees A|B as a set
        checkDecision(g, 1, expecting, null);
    }

    public void testOptionalAltAndBypass() throws Exception {
        Grammar g = new Grammar(
                "grammar t;\n"+
                "a : (A|B)? B;");
        String expecting =
                ".s0-A->:s4=>1\n" +
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
                ".s0-CASE->.s4\n" +
                ".s0-E->:s2=>2\n" +
                ".s0-LCURLY->:s1=>2\n" +
                ".s0-RCURLY->:s3=>2\n" +
                ".s4-E->:s5=>1\n";
        checkDecision(g, 3, expecting, null);
    }


    // S E M  P R E D S

    public void testPredsButSyntaxResolves() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : {p1}? A | {p2}? B ;");
        String expecting =
                ".s0-A->:s1=>1\n" +
                ".s0-B->:s2=>2\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testLL_1_Pred() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : {p1}? A | {p2}? A ;");
        String expecting =
                ".s0-A->.s1\n" +
                ".s1-{p1}?->:s2=>1\n" +
                ".s1-{p2}?->:s3=>2\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testLL_2_Pred() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : {p1}? A B | {p2}? A B ;");
        String expecting =
                ".s0-A->.s1\n" +
                ".s1-B->.s2\n" +
                ".s2-{p1}?->:s3=>1\n" +
                ".s2-{p2}?->:s4=>2\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testPredicatedLoop() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : ( {p1}? A | {p2}? A )+;");
        String expecting =                   // loop back
                ".s0-<EOF>->:s1=>3\n" +
                ".s0-A->.s2\n" +
                ".s2-{p1}?->:s3=>1\n" +
                ".s2-{p2}?->:s4=>2\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testPredicatedToStayInLoop() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : ( {p1}? A )+ (A)+;");
        String expecting =
                ".s0-A->.s1\n" +
                ".s1-{!(p1)}?->:s2=>1\n" +
                ".s1-{p1}?->:s3=>2\n";       // loop back
    }

    public void testAndPredicates() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : {p1}? {p1a}? A | {p2}? A ;");
        String expecting =
                ".s0-A->.s1\n" +
                ".s1-{(p1&&p1a)}?->:s2=>1\n" +
                ".s1-{p2}?->:s3=>2\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testOrPredicates() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : b | {p2}? A ;\n" +
            "b : {p1}? A | {p1a}? A ;");
        String expecting =
                ".s0-A->.s1\n" +
                ".s1-{(p1||p1a)}?->:s2=>1\n" +
                ".s1-{p2}?->:s3=>2\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testIgnoresHoistingDepthGreaterThanZero() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : A {p1}? | A {p2}?;");
        String expecting =
                ".s0-A->:s1=>1\n";
        checkDecision(g, 1, expecting, new int[] {2});
    }

    public void testHoist2() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : b | c ;\n" +
            "b : {p1}? A ;\n" +
            "c : {p2}? A ;\n");
        String expecting =
                ".s0-A->.s1\n" +
                ".s1-{p1}?->:s3=>1\n" +
                ".s1-{p2}?->:s2=>2\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testHoistCorrectContext() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : b | {p2}? ID ;\n" +
            "b : {p1}? ID | INT ;\n");
        String expecting =  // only tests after ID, not INT :)
                ".s0-ID->.s1\n" +
                ".s0-INT->:s4=>1\n" +
                ".s1-{p1}?->:s2=>1\n" +
                ".s1-{p2}?->:s3=>2\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testDefaultPred() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : b | ID ;\n" +
            "b : {p1}? ID | INT ;\n");
        String expecting =
                ".s0-ID->.s1\n" +
                ".s0-INT->:s4=>1\n" +
                ".s1-{!(p1)}?->:s3=>2\n" +
                ".s1-{p1}?->:s2=>1\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testLeftRecursivePred() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : {p1}? a | ID ;\n");
        String expecting =
                ".s0-ID->.s1\n" +
                ".s1-{!(p1)}?->:s3=>2\n" +
                ".s1-{p1}?->:s2=>1\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testIgnorePredFromLL2Alt() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : {p1}? A B | A C | {p2}? A | {p3}? A | A ;\n");
        // two situations of note:
        // 1. A B syntax is enough to predict that alt, so p1 is not used
        //    to distinguish it from alts 2..5
        // 2. Alts 3, 4, 5 are nondeterministic with upon A.  p2, p3 and the
        //    complement of p2||p3 is sufficient to resolve the conflict. Do
        //    not include alt 1's p1 pred in the "complement of other alts"
        //    because it is not considered nondeterministic with alts 3..5
        String expecting =
                ".s0-A->.s1\n" +
                ".s1-B->:s3=>1\n" +
                ".s1-C->:s2=>2\n" +
                ".s1-{!((p3||p2))}?->:s5=>5\n" +
                ".s1-{p2}?->:s4=>3\n" +
                ".s1-{p3}?->:s6=>4\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testPredGets2SymbolSyntacticContext() throws Exception {
        Grammar g = new Grammar(
            "grammar P;\n"+
            "a : b | A B | C ;\n" +
            "b : {p1}? A B ;\n");
        String expecting =
                ".s0-A->.s1\n" +
                ".s0-C->:s5=>3\n" +
                ".s1-B->.s2\n" +
                ".s2-{!(p1)}?->:s4=>2\n" +
                ".s2-{p1}?->:s3=>1\n";
        checkDecision(g, 1, expecting, null);
    }

    /** The following grammar should yield an error that rule 'a' has
     *  insufficient semantic info pulled from 'b'.
     */
    public void testIncompleteSemanticHoistedContext() throws Exception {
        Grammar g = new Grammar(
                "grammar t;\n"+
                "a : b | B;\n" +
                "b : {p1}? B | B ;");
        String expecting =
                ".s0-B->:s1=>1\n";
        checkDecision(g, 1, expecting, new int[] {2});
    }

    /** The following grammar should yield an error that rule 'a' has
     *  insufficient semantic info pulled from 'b'.  This is the same
     *  as the previous case except that the D prevents the B path from
     *  "pinching" together into a single NFA state.
     *
     *  This test also demonstrates that just because B D could predict
     *  alt 1 in rule 'a', it is unnecessary to continue NFA->DFA
     *  conversion to include an edge for D.  Alt 1 is the only possible
     *  prediction because we resolve the ambiguity by choosing alt 1.
     */
    public void testIncompleteSemanticHoistedContext2() throws Exception {
        Grammar g = new Grammar(
                "grammar t;\n"+
                "a : b | B;\n" +
                "b : {p1}? B | B D ;");
        String expecting =
                ".s0-B->:s1=>1\n";
        checkDecision(g, 1, expecting, new int[] {2});
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

    // N O N G R E E D Y

    public void testNonGreedy() throws Exception {
        Grammar g = new Grammar(
                "lexer grammar t;\n"+
                "CMT : \"/*\" ( greedy=false : . )* \"*/\" ;");
        String expecting =
                ".s0-'*'->.s1\n" +
                ".s0-{'\\u0000'..')', '+'..'\\uFFFE'}->:s4=>1\n" +
                ".s1-'*'->:s3=>1\n" +
                ".s1-'/'->:s2=>2\n" +
                ".s1-{'\\u0000'..')', '+'..'.', '0'..'\\uFFFE'}->:s4=>1\n";
        checkDecision(g, 1, expecting, null);
    }

    public void testNonWildcardNonGreedy() throws Exception {
        Grammar g = new Grammar(
                "lexer grammar t;\n"+
                "DUH : (greedy=false:'x'|'y')* \"xy\" ;");
        String expecting =
                ".s0-'x'->.s1\n" +
                ".s0-'y'->:s4=>2\n" +
                ".s1-'x'->:s3=>1\n" +
                ".s1-'y'->:s2=>3\n";
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
                ".s1-<EOT>->:s5=>2\n" +
                ".s1-{'a'..'e', 'g'..'z'}->:s4=>2\n" +
                ".s2-'a'..'z'->:s4=>2\n" +
                ".s2-<EOT>->:s3=>1\n";
        checkDecision(g, 2, expecting, null);
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
        if ( g.getCodeGenerator()==null ) {
            CodeGenerator generator = new CodeGenerator(null, g, "Java");
            g.setCodeGenerator(generator);
            if ( g.getType()==Grammar.LEXER ) {
                g.addArtificialMatchTokensRule();
            }
            try {
                g.createNFAs();
            }
            catch (RecognitionException re) {
                throw new FailedAssertionException("problem building nfas: "+
                        re.toString());
            }
            g.createLookaheadDFAs();
        }

        DFA dfa = g.getLookaheadDFA(decision);
        FASerializer serializer = new FASerializer(g);
        String result = serializer.serialize(dfa.getStartState());
        System.out.print(result);
        List nonDetAlts = dfa.getUnreachableAlts();

        // first make sure nondeterministic alts are as expected
        if ( expectingUnreachableAlts==null ) {
            BitSet s = new BitSet();
            s.addAll(expectingUnreachableAlts);
            assertTrue(nonDetAlts.size()==0,
                    "unreachable alts mismatch; expecting="+
                    s.toString()+" found "+nonDetAlts);
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
