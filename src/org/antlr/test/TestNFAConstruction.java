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
import antlr.RecognitionException;

public class TestNFAConstruction extends TestSuite {

    /** Public default constructor used by TestRig */
    public TestNFAConstruction() {
    }

    public void testA() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : A;");
        String expecting =
                ".s0->.s1\n" +
                ".s1->.s2\n" +
                ".s2-A->.s3\n" +
                ".s3->.s4\n" +
                ".s4->:s5\n" +
                ":s5-<EOF>->.s6\n";
        checkRule(g, "a", expecting);
    }

    public void testAB() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : A B ;");
        String expecting =
                ".s0->.s1\n" +
                ".s1->.s2\n" +
                ".s2-A->.s3\n" +
                ".s3->.s4\n" +
                ".s4-B->.s5\n" +
                ".s5->.s6\n" +
                ".s6->:s7\n" +
                ":s7-<EOF>->.s8\n";
        checkRule(g, "a", expecting);
    }

    public void testAorB() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : A | B {;} ;");
        /* expecting (0)--Ep-->(1)--Ep-->(2)--A-->(3)--Ep-->(4)--Ep-->(5,end)
                                |                            ^
                               (6)--Ep-->(7)--B-->(8)--------|
         */
        String expecting =
                ".s0->.s1\n" +
			".s1->.s2\n" +
			".s1->.s7\n" +
			".s2-A->.s3\n" +
			".s3->.s4\n" +
			".s4->:s5\n" +
			".s7->.s8\n" +
			".s8-B->.s9\n" +
			".s9->.s4\n" +
			":s5-<EOF>->.s6\n";
        checkRule(g, "a", expecting);
    }

	public void testRangeOrRange() throws Exception {
		Grammar g = new Grammar(
				"lexer grammar P;\n"+
				"A : ('a'..'c' 'h' | 'q' 'j'..'l') ;"
		);
		/* expecting
		(0)--Ep-->(1)-->(2)-->(3)--'a'..'c'-->(4)-->(5)--'h'-->(6)-->(7)-->(8)-->(9,end)
				         |                                             ^
				         |		                                       |                         |
				        (10)-->(11)--'q'->(12)--Ep-->(13)--'j'..'l'-->(14)
		 */
        String expecting =
                ".s0->.s1\n" +
			".s1->.s2\n" +
			".s11->.s12\n" +
			".s12-'q'->.s13\n" +
			".s13->.s14\n" +
			".s14-'j'..'l'->.s15\n" +
			".s15->.s7\n" +
			".s2->.s11\n" +
			".s2->.s3\n" +
			".s3-'a'..'c'->.s4\n" +
			".s4->.s5\n" +
			".s5-'h'->.s6\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOT>->.s10\n";
        checkRule(g, "A", expecting);
	}

	public void testRange() throws Exception {
		Grammar g = new Grammar(
				"lexer grammar P;\n"+
				"A : 'a'..'c' ;"
		);
		/* expecting
		          (0)--Ep-->(1)--Ep-->(2)--'a'..'c'-->(3)--Ep--->(5,end)
		 */
        String expecting =
                ".s0->.s1\n" +
                ".s1->.s2\n" +
                ".s2-'a'..'c'->.s3\n" +
                ".s3->.s4\n" +
                ".s4->:s5\n" +
                ":s5-<EOT>->.s6\n";
        checkRule(g, "A", expecting);
	}

	public void testABorCD() throws Exception {
			Grammar g = new Grammar(
					"grammar P;\n"+
					"a : A B | C D;");
			/* expecting
			(0)--Ep-->(1)--Ep-->(2)--A-->(3)--Ep-->(4)--B-->(5)--Ep-->(6)--Ep-->(7,end)
					   |                                               ^
					  (8)--Ep-->(9)--C-->(10)--Ep-->(11)--D-->(12)-----|
			 */
        String expecting =
                ".s0->.s1\n" +
			".s1->.s2\n" +
			".s1->.s9\n" +
			".s10-C->.s11\n" +
			".s11->.s12\n" +
			".s12-D->.s13\n" +
			".s13->.s6\n" +
			".s2-A->.s3\n" +
			".s3->.s4\n" +
			".s4-B->.s5\n" +
			".s5->.s6\n" +
			".s6->:s7\n" +
			".s9->.s10\n" +
			":s7-<EOF>->.s8\n";
        checkRule(g, "a", expecting);
    }

    public void testbA() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : b A ;\n"+
                "b : B ;");
        /* expecting
        a: (0)--Ep-->(1)--Ep--|  (2)--Ep-->(3)--A-->(4)--Ep-->(5,end)
               /--------------|   ^
              /                   |-------------------
        b: (6)--Ep-->(7)--B-->(8)--Ep-->(9,end)--Ep--|
        */
		String expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s10-A->.s11\n" +
			".s11->.s12\n" +
			".s12->:s13\n" +
			".s2->.s3\n" +
			".s3->.s4\n" +
			".s4->.s5\n" +
			".s5-B->.s6\n" +
			".s6->.s7\n" +
			".s7->:s8\n" +
			".s9->.s10\n" +
			":s13-<EOF>->.s14\n" +
			":s8->.s9\n";
        checkRule(g, "a", expecting);
    }

    public void testbA_bC() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : b A ;\n"+
                "b : B ;\n"+
                "c : b C;");
        /* expecting
        a: (0)--Ep-->(1)--Ep--|  (2)--Ep-->(3)--A-->(4)--Ep-->(5,end)
            |  /--------------|   ^
            | /                   |-------------------
        b: (6)--Ep-->(7)--B-->(8)--Ep-->(9,end)--Ep--|   1st follow link
            |\                              |
            | \                            (10)--Ep--|   2nd follow link
            |  -----------------|   |----------------|
            |                   |   v
        a: (11)--Ep-->(12)--Ep--|  (13)--Ep-->(14)--C-->(15)--Ep-->(16,end)

        where I hook rule a to rule b and b to c so it's one NFA.
        */
        String expecting =
                ".s0->.s1\n" +
			".s1->.s2\n" +
			".s10-A->.s11\n" +
			".s11->.s12\n" +
			".s12->:s13\n" +
			".s15->.s16\n" +
			".s16->.s17\n" +
			".s17-C->.s18\n" +
			".s18->.s19\n" +
			".s19->:s20\n" +
			".s2->.s3\n" +
			".s3->.s4\n" +
			".s4->.s5\n" +
			".s5-B->.s6\n" +
			".s6->.s7\n" +
			".s7->:s8\n" +
			".s9->.s10\n" +
			":s13-<EOF>->.s14\n" +
			":s20-<EOF>->.s21\n" +
			":s8->.s15\n" +
			":s8->.s9\n";
        checkRule(g, "a", expecting);
    }

    public void testAorEpsilon() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : A | ;");
        /* expecting (0)--Ep-->(1)--Ep-->(2)--A-->(3)--Ep-->(4)--Ep-->(5,end)
                                |                            ^
                               (6)--Ep-->(7)--Ep-->(8)-------|
         */
		String expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s1->.s7\n" +
			".s2-A->.s3\n" +
			".s3->.s4\n" +
			".s4->:s5\n" +
			".s7->.s8\n" +
			".s8->.s9\n" +
			".s9->.s4\n" +
			":s5-<EOF>->.s6\n";
        checkRule(g, "a", expecting);
    }

    public void testAoptional() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : (A)?;");
        String expecting =
            ".s0->.s1\n" +
			".s1->.s2\n" +
			".s11->.s7\n" +
			".s2->.s11\n" +
			".s2->.s3\n" +
			".s3->.s4\n" +
			".s4-A->.s5\n" +
			".s5->.s6\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
        checkRule(g, "a", expecting);
    }

    public void testAorBthenC() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : (A | B) C;");
        /* expecting

        (0)--Ep-->(1)--Ep-->(2)--A-->(3)--Ep-->(4)--Ep-->(5)--C-->(6)--Ep-->(7,end)
                   |                            ^
                  (8)--Ep-->(9)--B-->(10)-------|
         */
    }

    public void testAplus() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : (A)+;");
        /* expecting
		                     |--------|
		                     v        |
        (0)--Ep-->(1)--Ep-->(2)--A-->(3)--Ep-->(4)--Ep-->(5,end)
         */
		String expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s2->.s3\n" +
			".s3->.s4\n" +
			".s4-A->.s5\n" +
			".s5->.s6\n" +
			".s6->.s3\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
        checkRule(g, "a", expecting);
    }

	public void testAplusNonGreedy() throws Exception {
		Grammar g = new Grammar(
				"lexer grammar t;\n"+
				"A : (options {greedy=false;}:'0'..'9')+ ;\n");
		String expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s2->.s3\n" +
			".s3->.s4\n" +
			".s4-'0'..'9'->.s5\n" +
			".s5->.s6\n" +
			".s6->.s3\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOT>->.s10\n";
		checkRule(g, "A", expecting);
	}

    public void testAorBplus() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : (A | B{action})+ ;");
        /* expecting
                             |----------------------------|
                             v                            |
        (0)--Ep-->(1)--Ep-->(2)--Ep-->(3)--A-->(4)--Ep-->(5)--Ep-->(6)--Ep-->(7,end)
                             |                            ^
                            (8)--Ep-->(9)--B-->(10)-------|
         */
		String expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s11->.s12\n" +
			".s12-B->.s13\n" +
			".s13->.s6\n" +
			".s2->.s3\n" +
			".s3->.s11\n" +
			".s3->.s4\n" +
			".s4-A->.s5\n" +
			".s5->.s6\n" +
			".s6->.s3\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
        checkRule(g, "a", expecting);
    }

    public void testAorBorEmptyPlus() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : (A | B | )+ ;");
        /* expecting
                             |----------------------------|
                             v                            |
        (0)--Ep-->(1)--Ep-->(2)--Ep-->(3)--A-->(4)--Ep-->(5)--Ep-->(6)--Ep-->(7,end)
                             |                            ^
                            (8)--Ep-->(9)--B-->(10)-------|
                             |                            |
                            (11)--Ep->(12)--Ep-->(13)-----|
         */
        String expecting =
            ".s0->.s1\n" +
			".s1->.s2\n" +
			".s11->.s12\n" +
			".s11->.s14\n" +
			".s12-B->.s13\n" +
			".s13->.s6\n" +
			".s14->.s15\n" +
			".s15->.s16\n" +
			".s16->.s6\n" +
			".s2->.s3\n" +
			".s3->.s11\n" +
			".s3->.s4\n" +
			".s4-A->.s5\n" +
			".s5->.s6\n" +
			".s6->.s3\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
        checkRule(g, "a", expecting);
    }

    public void testAstar() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : (A)*;");
		String expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s11->.s7\n" +
			".s2->.s11\n" +
			".s2->.s3\n" +
			".s3->.s4\n" +
			".s4-A->.s5\n" +
			".s5->.s6\n" +
			".s6->.s3\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
        checkRule(g, "a", expecting);
    }

    public void testAorBstar() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : (A | B{action})* ;");
		String expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s11->.s12\n" +
			".s12-B->.s13\n" +
			".s13->.s6\n" +
			".s14->.s7\n" +
			".s2->.s14\n" +
			".s2->.s3\n" +
			".s3->.s11\n" +
			".s3->.s4\n" +
			".s4-A->.s5\n" +
			".s5->.s6\n" +
			".s6->.s3\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
        checkRule(g, "a", expecting);
    }

    public void testAorBOptionalSubrule() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : ( A | B )? ;");
        String expecting =
            ".s0->.s1\n" +
			".s1->.s2\n" +
			".s11->.s7\n" +
			".s2->.s11\n" +
			".s2->.s3\n" +
			".s3->.s4\n" +
			".s4-A..B->.s5\n" +
			".s5->.s6\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
        checkRule(g, "a", expecting);
    }

    public void testPredicatedAorB() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : {p1}? A | {p2}? B ;");
		String expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s1->.s9\n" +
			".s10-{p2}?->.s11\n" +
			".s11->.s12\n" +
			".s12-B->.s13\n" +
			".s13->.s6\n" +
			".s2-{p1}?->.s3\n" +
			".s3->.s4\n" +
			".s4-A->.s5\n" +
			".s5->.s6\n" +
			".s6->:s7\n" +
			".s9->.s10\n" +
			":s7-<EOF>->.s8\n";
        checkRule(g, "a", expecting);
    }

    public void testMultiplePredicates() throws Exception {
        Grammar g = new Grammar(
                "grammar P;\n"+
                "a : {p1}? {p1a}? A | {p2}? B | {p3} b;\n" +
                "b : {p4}? B ;");
		String expecting =
			".s0->.s1\n" +
			".s1->.s11\n" +
			".s1->.s2\n" +
			".s11->.s12\n" +
			".s11->.s16\n" +
			".s12-{p2}?->.s13\n" +
			".s13->.s14\n" +
			".s14-B->.s15\n" +
			".s15->.s8\n" +
			".s16->.s17\n" +
			".s17->.s18\n" +
			".s18->.s19\n" +
			".s19->.s20\n" +
			".s2-{p1}?->.s3\n" +
			".s20-{p4}?->.s21\n" +
			".s21->.s22\n" +
			".s22-B->.s23\n" +
			".s23->.s24\n" +
			".s24->:s25\n" +
			".s26->.s8\n" +
			".s3->.s4\n" +
			".s4-{p1a}?->.s5\n" +
			".s5->.s6\n" +
			".s6-A->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s25->.s26\n" +
			":s9-<EOF>->.s10\n";
        checkRule(g, "a", expecting);
	}

	public void testSets() throws Exception {
		Grammar g = new Grammar(
			"grammar P;\n"+
			"a : ( A | B )+ ;\n" +
			"b : ( A | B{;} )+ ;\n" +
			"c : (A|B) (A|B) ;\n" +
			"d : ( A | B )* ;\n" +
			"e : ( A | B )? ;");
		String expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s2->.s3\n" +
			".s3->.s4\n" +
			".s4-A..B->.s5\n" +
			".s5->.s6\n" +
			".s6->.s3\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
		checkRule(g, "a", expecting);
		expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s11->.s12\n" +
			".s12-B->.s13\n" +
			".s13->.s6\n" +
			".s2->.s3\n" +
			".s3->.s11\n" +
			".s3->.s4\n" +
			".s4-A->.s5\n" +
			".s5->.s6\n" +
			".s6->.s3\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
		checkRule(g, "b", expecting);
		expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s2-A..B->.s3\n" +
			".s3->.s4\n" +
			".s4-A..B->.s5\n" +
			".s5->.s6\n" +
			".s6->:s7\n" +
			":s7-<EOF>->.s8\n";
		checkRule(g, "c", expecting);
		expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s11->.s7\n" +
			".s2->.s11\n" +
			".s2->.s3\n" +
			".s3->.s4\n" +
			".s4-A..B->.s5\n" +
			".s5->.s6\n" +
			".s6->.s3\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
		checkRule(g, "d", expecting);
		expecting =
			".s0->.s1\n" +
			".s1->.s2\n" +
			".s11->.s7\n" +
			".s2->.s11\n" +
			".s2->.s3\n" +
			".s3->.s4\n" +
			".s4-A..B->.s5\n" +
			".s5->.s6\n" +
			".s6->.s7\n" +
			".s7->.s8\n" +
			".s8->:s9\n" +
			":s9-<EOF>->.s10\n";
		checkRule(g, "e", expecting);
	}


	private void checkRule(Grammar g, String rule, String expecting)
            throws FailedAssertionException
    {
        g.createNFAs();
        State startState = g.getRuleStartState(rule);
        FASerializer serializer = new FASerializer(g);
        String result = serializer.serialize(startState);

        //System.out.print(result);
        assertEqual(result, expecting);
    }

}
