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
import org.antlr.analysis.Label;
import org.antlr.analysis.DFA;

import java.util.*;

import antlr.Token;

/** Test the view of nondeterminisms through DecisionProbe objects.  A gui
 *  will need to display errors that way.  Also, text messages to the screen
 *  will be derived from DecisionProbe objects.
 */
public class TestNonDeterminism extends TestSuite {

	static class ErrorQueue implements ANTLRErrorListener {
		List infos = new LinkedList();
		List errors = new LinkedList();
		List warnings = new LinkedList();

		public void info(String msg) {
			infos.add(msg);
		}

		public void error(Message msg) {
			errors.add(msg);
		}

		public void warning(Message msg) {
			warnings.add(msg);
		}

		public void error(ToolMessage msg) {
			errors.add(msg);
		}
	};

    /** Public default constructor used by TestRig */
    public TestNonDeterminism() {
    }

	public void testSimpleLL1Ambiguity() throws Exception {
		Grammar g = new Grammar(
				"parser grammar t;\n"+
				"a : A | {;}A;\n"); // add action to avoid set collapse
		checkDecision(g, 1);
	}

	public void testSimpleLL2Ambiguity() throws Exception {
		Grammar g = new Grammar(
				"parser grammar t;\n"+
				"a : A B | A B;\n"); // add action to avoid set collapse
		checkDecision(g, 1);
	}

	protected void checkDecision(Grammar g,
								 int decision)
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
	}
}
