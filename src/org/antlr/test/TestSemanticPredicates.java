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

public class TestSemanticPredicates extends TestSuite {

    /** Public default constructor used by TestRig */
    public TestSemanticPredicates() {
    }

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

	public void testTooFewSemanticPredicates() throws Exception {
		Grammar g = new Grammar(
				"grammar t;\n"+
				"a : {p1}? A | A | A ;");
		String expecting =
				".s0-A->:s1=>1\n";
		checkDecision(g, 1, expecting, new int[] {2,3});
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
        FASerializer serializer = new FASerializer(g);
        String result = serializer.serialize(dfa.startState);
        //System.out.print(result);
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
