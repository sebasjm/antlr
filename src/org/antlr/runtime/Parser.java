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
import org.antlr.misc.IntervalSet;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

public class Parser {
    protected TokenStream input;

	/** Track the set of token types that can follow any rule invocation.
	 *  List<BitSet>.
	 */
	protected Stack following = new Stack();

	protected boolean errorRecovery = false;


    public Parser(TokenStream input) {
        this.input = input;
    }

	public CharStream getCharStream() {
		return input.getTokenSource().getCharStream();
	}

    public TokenStream getTokenStream() {
		return input;
	}

	/** Match current input symbol against ttype.  Upon error, do one token
	 *  insertion or deletion if possible.  You can override to not recover
	 *  here and bail out of the current production to the normal error
	 *  exception catch (at the end of the method) by just throwing
	 *  MismatchedTokenException upon input.LA(1)!=ttype.
	 */
	public void match(int ttype, org.antlr.runtime.BitSet follow)
		throws MismatchedTokenException
	{
		if ( input.LA(1)==ttype ) {
			input.consume();
			errorRecovery = false;
			return;
		}
		errorRecovery = true;
		MismatchedTokenException mte = new MismatchedTokenException(ttype);
		recoverFromMismatchedToken(mte, ttype, follow);
		return;
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
		String parserClassName = getClass().getName();
		System.err.print(getRuleInvocationStack(e, parserClassName)+
						 ": line "+input.LT(1).getLine()+" ");
		if ( e instanceof MismatchedTokenException ) {
			MismatchedTokenException mte = (MismatchedTokenException)e;
			System.err.println("mismatched token: "+
							   input.LT(1).toString(getCharStream())+
							   "; expecting type "+getTokenNames()[mte.expecting]);
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

	public void recover(RecognitionException re, org.antlr.runtime.BitSet follow) {
		consumeUntil(follow);
	}

	public void recover(RecognitionException re) {
		BitSet followSet = computeRuleFollow();
		consumeUntil(followSet);
	}

	/*  Compute the context-sensitive FOLLOW set for current rule.
	 *  During rule invocation, the parser pushes the set of tokens
	 *  that can follow that rule reference on the stack; this amounts
	 *  to computing FIRST of what follows the rule reference in the
	 *  rule.  We can consider this the local follow.  The local
	 *  follow set only includes tokens from within the enclosing
	 *  rule; i.e., the FIRST computation done by ANTLR stops at the
	 *  end of a rule.
	 *
	 *  This computation returns the union of all of these local
	 *  follow sets.  It is important to note that the set is not
	 *  limited to any particular lookahead depth.
	 *
	 *  For SLL(k) parsers, such as those built by ANTLR, you can only
	 *  compute the "global" FOLLOW statically.  The global FOLLOW is
	 *  the set of tokens that can follow a rule reference in *any*
	 *  context.  In our case, since we are computing the combined set
	 *  at run time, we know which particular single context and can
	 *  compute an exact FOLLOW.
	 *
	 *  EXAMPLE
	 *
	 *  When you find a "no viable alt exception", the input is not
	 *  consistent with any of the alternatives for rule r.  The best
	 *  thing to do is to consume tokens until you see something that
	 *  can legally follow a call to r or any rule that called r.  You
	 *  don't want the exact set of viable next tokens because the
	 *  input might just be missing a token--you could consume the
	 *  rest of the input looking for one of those tokens.
	 *
	 *  For example, consider grammar:
	 *
	 *  stat : ID '=' expr ';'
	 *       | "return" expr '.'
	 *       ;
	 *  expr : atom ('+' atom)* ;
	 *  atom : INT
	 *       | '(' expr ')'
	 *       ;
	 *
	 *  For input "i = (;" the parser will enter
	 *
	 *  stat -> expr -> atom -> expr -> atom
	 *
	 *  and then discover ';' doesn't start an atom.  The FOLLOW of
	 *  atom is set {'+',')',';'}.  Notice that it does not include
	 *  '.'  because that token is contributed from a different
	 *  context (that of having called expr from the 2nd alt of stat
	 *  rather than the first as in our case).  The FOLLOW set is
	 *  computed by walking back up the call chain and combining sets
	 *  from the local follow for each rule.
	 *
	 *  Anyway, the parser should not consume anything as LA(1)==';'.
	 *  Consider the difference between FOLLOW(atom) and the set of
	 *  viable tokens for what follows a reference to atom: {'+',')'}.
	 *  If you used this set instead of the FOLLOW, you'd consume the
	 *  ';' and probably far into the future looking for a + or ')'.
	 */
	protected BitSet computeRuleFollow() {
		return combineFollows(false);
	}

	/** Compute the set of token types that can come next after a
	 *  token reference.  You get the set of viable tokens that can
	 *  possibly come next at lookahead depth 1.  You want the exact
	 *  viable token set when recovering from a token mismatch.  If
	 *  LA(1) is member of exact set, then you know there is most
	 *  likely a missing token in the input stream.  "Insert" one by
	 *  just not throwing an exception.
	 */
	protected BitSet computeViableTokens() {
		return combineFollows(true);
	}

	protected BitSet combineFollows(boolean exact) {
		int top = following.size()-1;
		BitSet followSet = new BitSet();
		for (int i=top; i>=0; i--) {
			BitSet localFollowSet = (BitSet) following.get(i);
			/*
			System.out.println("local follow depth "+i+"="+
							   localFollowSet.toString(getTokenNames())+")");
			*/
			followSet.orInPlace(localFollowSet);
			if ( exact && !localFollowSet.member(Token.EOR_TOKEN_TYPE) ) {
				break;
			}
		}
		followSet.remove(Token.EOR_TOKEN_TYPE);
		return followSet;
	}

	public void recoverFromMismatchedToken(MismatchedTokenException mte,
										   int ttype,
										   org.antlr.runtime.BitSet follow)
		throws MismatchedTokenException
	{
		// if next token is what we are looking for then "delete" this token
		if ( input.LA(2)==ttype ) {
			reportError(mte);
			System.err.println("deleting "+input.LT(1).toString(getCharStream()));
			input.consume(); // delete extra token
			input.consume(); // move past ttype token as if all were ok
			return;
		}
		// compute what can follow this token reference
		if ( follow.member(Token.EOR_TOKEN_TYPE) ) {
			BitSet viableTokensFollowingThisRule = computeViableTokens();
			follow = follow.or(viableTokensFollowingThisRule);
			follow.remove(Token.EOR_TOKEN_TYPE);
		}
		// if current token is consistent with what could come after ttype
		// then it is ok to "insert" the missing token, else throw exception
		//System.out.println("viable tokens="+follow.toString(getTokenNames())+")");
		if ( follow.member(input.LA(1)) ) {
			if ( !errorRecovery ) {
				reportError(mte);
			}
			System.err.println("inserting "+getTokenNames()[ttype]);
			return;
		}
		System.err.println("nothing to do; throw exception");
		throw mte;
	}

	public void consumeUntil(int tokenType) {
		while (input.LA(1) != Token.EOF && input.LA(1) != tokenType) {
			input.consume();
		}
	}

	/** Consume tokens until one matches the given token set */
	public void consumeUntil(BitSet set) {
		System.out.println("consumeUntil("+set.toString(getTokenNames())+")");
		/*
		System.out.println("LT(1)="+
						   input.LT(1).toString(input.getTokenSource().getCharStream()));
		input.consume(); // always consume at least one token; inoptimal but safe
		*/
		while (input.LA(1) != Token.EOF && !set.member(input.LA(1)) ) {
			System.out.println("LT(1)="+
							   input.LT(1).toString(input.getTokenSource().getCharStream()));
			input.consume();
		}
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

	public String[] getTokenNames() {
		return null;
	}
}
