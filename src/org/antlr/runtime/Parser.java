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
package org.antlr.runtime;

import org.antlr.misc.IntSet;

import java.util.List;
import java.util.ArrayList;

public class Parser {
    protected TokenStream input;

    public Parser(TokenStream input) {
        this.input = input;
    }

	public CharStream getCharStream() {
		return input.getTokenSource().getCharStream();
	}

    public TokenStream getTokenStream() {
		return input;
	}

	public void match(int ttype) throws MismatchedTokenException {
		if ( input.LA(1)!=ttype ) {
			throw new MismatchedTokenException(ttype);
		}
		input.consume();
	}

	public void matchAny() {
		input.consume();
	}

	/** Report a recognition problem.  Java is not polymorphic on the
	 *  argument types so you have to check the type of exception yourself.
	 *  That's not very clean but it's better than generating a bunch of
	 *  catch clauses in each rule and makes it easy to extend with
	 *  more exceptions w/o breaking old code.
	 */
	public void reportError(RecognitionException e) {
		System.err.print(getRuleInvocationStack()+
						 ": line "+input.getTokenSource().getCharStream().getLine()+" ");
		if ( e instanceof MismatchedTokenException ) {
			MismatchedTokenException mte = (MismatchedTokenException)e;
			System.err.println("mismatched token: "+
							   input.LT(1).toString(getCharStream())+
							   "; expecting type "+mte.expecting);
		}
		else if ( e instanceof NoViableAltException ) {
			NoViableAltException nvae = (NoViableAltException)e;
			System.err.println(nvae.grammarDecisionDescription+
							   " state "+nvae.stateNumber+
							   " (decision="+nvae.decisionNumber+
							   ") no viable alt; token="+
							   input.LT(1).toString(getCharStream()));
		}
		else if ( e instanceof EarlyExitException ) {
			EarlyExitException eee = (EarlyExitException)e;
			System.err.println("required (...)+ loop (decision="+
							   eee.decisionNumber+
							   ") did not match anything; token="+
							   input.LT(1).toString(getCharStream()));
		}
		else if ( e instanceof MismatchedSetException ) {
			MismatchedSetException mse = (MismatchedSetException)e;
			System.err.println("mismatched token: "+
							   input.LT(1).toString(getCharStream())+
							   "; expecting set "+mse.expecting);
		}
		else if ( e instanceof MismatchedNotSetException ) {
			MismatchedSetException mse = (MismatchedSetException)e;
			System.err.println("mismatched token: "+
							   input.LT(1).toString(getCharStream())+
							   "; expecting set "+mse.expecting);
		}
	}

	/** TODO: make this accept the FOLLOW(enclosing-Rule) */
	public void recover() {
		input.consume();
	}

	/** Return List<String> of the rules in your parser instance
	 *  leading up to a call to this method.  You could override if
	 *  you want more details such as the file/line info of where
	 *  in the parser java code a rule is invoked.
	 *
	 *  This is very useful for error messages and for context-sensitive
	 *  error recovery.
	 */
	public List getRuleInvocationStack() {
		String parserClassName = getClass().getName();
		return getRuleInvocationStack(new Throwable(), parserClassName);
	}

	/** A more general version of getRuleInvocationStack where you can
	 *  pass in, for example, a RecognitionException to get it's rule
	 *  stack trace.  This routine is shared with all recognizers, hence,
	 *  static.
	 *
	 *  TODO: move to a utility class or something; weird having lexer call this
	 */
	public static List getRuleInvocationStack(Throwable e,
											  String recognizerClassName)
	{
		List rules = new ArrayList();
		StackTraceElement[] stack = e.getStackTrace();
		int i = 0;
		for (i=stack.length-1; i>=0; i--) {
			StackTraceElement t = stack[i];
			if ( t.getClassName().startsWith("org.antlr.runtime.") ) {
				continue; // skip support code such as this method
			}
			if ( t.getMethodName().equals("nextToken") ) {
				continue;
			}
			if ( !t.getClassName().equals(recognizerClassName) ) {
				continue; // must not be part of this parser
			}
            rules.add(t.getMethodName());
		}
		return rules;
	}
}
