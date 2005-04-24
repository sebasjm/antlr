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

	/** This is true when we see an error and before having successfully
	 *  matched a token.  Prevents generation of more than one error message
	 *  per error.  
	 */
	protected boolean errorRecovery = false;

    public Parser(TokenStream input) {
        setTokenStream(input);
    }

	/** Set the token stream and reset the parser */
	public void setTokenStream(TokenStream input) {
		this.input = input;
		following.setSize(0);
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
		MismatchedTokenException mte =
			new MismatchedTokenException(ttype, input);
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
	 *
	 *  This method sets errorRecovery to indicate the parser is recovering
	 *  not parsing.  Once in recovery mode, no errors are generated.
	 *  To get out of recovery mode, the parser must successfully match
	 *  a token (after a resync).  So it will go:
	 *
	 * 		1. error occurs
	 * 		2. enter recovery mode, report error
	 * 		3. consume until token found in resynch set
	 * 		4. try to resume parsing
	 * 		5. next match() will reset errorRecovery mode
	 */
	public void reportError(RecognitionException e) {
		// if we've already reported an error and have not matched a token
		// yet successfully, don't report any errors.
		if ( errorRecovery ) {
			return;
		}
		errorRecovery = true;

		String parserClassName = getClass().getName();
		System.err.print(getRuleInvocationStack(e, parserClassName)+
						 ": line "+e.line+":"+e.charPositionInLine+" ");
		if ( e instanceof MismatchedTokenException ) {
			MismatchedTokenException mte = (MismatchedTokenException)e;
			System.err.println("mismatched token: "+
							   e.token+
							   "; expecting type "+getTokenNames()[mte.expecting]);
		}
		else if ( e instanceof NoViableAltException ) {
			NoViableAltException nvae = (NoViableAltException)e;
			System.err.println("decision=<<"+nvae.grammarDecisionDescription+">>"+
							   " state "+nvae.stateNumber+
							   " (decision="+nvae.decisionNumber+
							   ") no viable alt; token="+
							   e.token);
		}
		else if ( e instanceof EarlyExitException ) {
			EarlyExitException eee = (EarlyExitException)e;
			System.err.println("required (...)+ loop (decision="+
							   eee.decisionNumber+
							   ") did not match anything; token="+
							   e.token);
		}
		else if ( e instanceof MismatchedSetException ) {
			MismatchedSetException mse = (MismatchedSetException)e;
			System.err.println("mismatched token: "+
							   e.token+
							   "; expecting set "+mse.expecting);
		}
		else if ( e instanceof MismatchedNotSetException ) {
			MismatchedNotSetException mse = (MismatchedNotSetException)e;
			System.err.println("mismatched token: "+
							   e.token+
							   "; expecting set "+mse.expecting);
		}
	}

	public void recover(RecognitionException re, org.antlr.runtime.BitSet follow) {
		consumeUntil(follow);
	}

	/** Recover from an error found on the input stream.  Mostly this is
	 *  NoViableAlt exceptions, but could be a mismatched token that
	 *  the match() routine could not recover from.
	 *
	 *  Warning: if you override and you want to use -debug option,
	 *  you'll have to trigger dbg.recover() and recovered() yourself.
	 */
	public void recover(RecognitionException re) {
		BitSet followSet = computeErrorRecoverySet();
		recover(re, followSet);
	}

	/*  Compute the error recovery set for the current rule.  During
	 *  rule invocation, the parser pushes the set of tokens that can
	 *  follow that rule reference on the stack; this amounts to
	 *  computing FIRST of what follows the rule reference in the
	 *  enclosing rule. This local follow set only includes tokens
	 *  from within the rule; i.e., the FIRST computation done by
	 *  ANTLR stops at the end of a rule.
	 *
	 *  EXAMPLE
	 *
	 *  When you find a "no viable alt exception", the input is not
	 *  consistent with any of the alternatives for rule r.  The best
	 *  thing to do is to consume tokens until you see something that
	 *  can legally follow a call to r *or* any rule that called r.
	 *  You don't want the exact set of viable next tokens because the
	 *  input might just be missing a token--you might consume the
	 *  rest of the input looking for one of the missing tokens.
	 *
	 *  Consider grammar:
	 *
	 *  a : '[' b ']'
	 *    | '(' b ')'
	 *    ;
	 *  b : c '^' INT ;
	 *  c : ID
	 *    | INT
	 *    ;
	 *  
	 *  At each rule invocation, the set of tokens that could follow
	 *  that rule is pushed on a stack.  Here are the various "local"
	 *  follow sets:
	 *
	 *  FOLLOW(b1_in_a) = FIRST(']') = ']'
	 *  FOLLOW(b2_in_a) = FIRST(')') = ')'
	 *  FOLLOW(c_in_b) = FIRST('^') = '^'
	 *  
	 *  Upon erroneous input "[]", the call chain is
	 *
	 *  a -> b -> c
	 *
	 *  and, hence, the follow context stack is:
	 *
	 *  depth  local follow set     after call to rule
	 *    0         <EOF>                    a (from main())
	 *    1          ']'                     b
	 *    3          '^'                     c
	 *
	 *  Notice that ')' is not included, because b would have to have
	 *  been called from a different context in rule a for ')' to be
	 *  included. 
	 *
	 *  For error recovery, we cannot consider FOLLOW(c)
	 *  (context-sensitive or otherwise).  We need the combined set of
	 *  all context-sensitive FOLLOW sets--the set of all tokens that
	 *  could follow any reference in the call chain.  We need to
	 *  resync to one of those tokens.  Note that FOLLOW(c)='^' and if
	 *  we resync'd to that token, we'd consume until EOF.  We need to
	 *  sync to context-sensitive FOLLOWs for a, b, and c: {']','^'}.
	 *  In this case, for input "[]", LA(1) is in this set so we would
	 *  not consume anything and after printing an error rule c would
	 *  return normally.  It would not find the required '^' though.
	 *  At this point, it gets a mismatched token error and throws an
	 *  exception (since LA(1) is not in the viable following token
	 *  set).  The rule exception handler tries to recover, but finds
	 *  the same recovery set and doesn't consume anything.  Rule b
	 *  exits normally returning to rule a.  Now it finds the ']' (and
	 *  with the successful match exits errorRecovery mode).
	 *
	 *  So, you cna see that the parser walks up call chain looking
	 *  for the token that was a member of the recovery set.
	 *
	 *  Errors are not generated in errorRecovery mode.
	 *
	 *  ANTLR's error recovery mechanism is based upon original ideas:
	 *
	 *  "Algorithms + Data Structures = Programs" by Niklaus Wirth
	 *
	 *  and
	 *
	 *  "A note on error recovery in recursive descent parsers":
	 *  http://portal.acm.org/citation.cfm?id=947902.947905
	 *
	 *  Later, Josef Grosch had some good ideas:
	 *
	 *  "Efficient and Comfortable Error Recovery in Recursive Descent
	 *  Parsers":
	 *  ftp://www.cocolab.com/products/cocktail/doca4.ps/ell.ps.zip
	 *
	 *  Like Grosch I implemented local FOLLOW sets that are combined
	 *  at run-time upon error to avoid overhead during parsing.
	 */
	protected BitSet computeErrorRecoverySet() {
		return combineFollows(false);
	}

	/** Compute the context-sensitive FOLLOW set for current rule.
	 *  This is set of token types that can follow a specific rule
	 *  reference given a specific call chain.  You get the set of
	 *  viable tokens that can possibly come next (lookahead depth 1)
	 *  given the current call chain.  Contrast this with the
	 *  definition of plain FOLLOW for rule r:
	 *
	 *   FOLLOW(r)={x | S=>*alpha r beta in G and x in FIRST(beta)}
	 *  
	 *  where x in T* and alpha, beta in V*; T is set of terminals and
	 *  V is the set of terminals and nonterminals.  In other words,
	 *  FOLLOW(r) is the set of all tokens that can possibly follow
	 *  references to r in *any* sentential form (context).  At
	 *  runtime, however, we know precisely which context applies as
	 *  we have the call chain.  We may compute the exact (rather
	 *  than covering superset) set of following tokens.
	 *
	 *  For example, consider grammar:
	 *
	 *  stat : ID '=' expr ';'      // FOLLOW(stat)=={EOF}
	 *       | "return" expr '.'
	 *       ;
	 *  expr : atom ('+' atom)* ;   // FOLLOW(expr)=={';','.',')'}
	 *  atom : INT                  // FOLLOW(atom)=={'+',')',';','.'}
	 *       | '(' expr ')'
	 *       ;
	 *
	 *  The FOLLOW sets are all inclusive whereas context-sensitive
	 *  FOLLOW sets are precisely what could follow a rule reference.
	 *  For input input "i=(3);", here is the derivation:
	 *
	 *  stat => ID '=' expr ';'
	 *       => ID '=' atom ('+' atom)* ';'
	 *       => ID '=' '(' expr ')' ('+' atom)* ';'
	 *       => ID '=' '(' atom ')' ('+' atom)* ';'
	 *       => ID '=' '(' INT ')' ('+' atom)* ';'
	 *       => ID '=' '(' INT ')' ';'
	 *
	 *  At the "3" token, you'd have a call chain of
	 *
	 *    stat -> expr -> atom -> expr -> atom
	 *
	 *  What can follow that specific nested ref to atom?  Exactly ')'
	 *  as you can see by looking at the derivation of this specific
	 *  input.  Contrast this with the FOLLOW(atom)={'+',')',';','.'}.
	 *
	 *  You want the exact viable token set when recovering from a
	 *  token mismatch.  Upon token mismatch, if LA(1) is member of
	 *  the viable next token set, then you know there is most likely
	 *  a missing token in the input stream.  "Insert" one by just not
	 *  throwing an exception.
	 */
	protected BitSet computeContextSensitiveRuleFOLLOW() {
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

	/** Attempt to recover from a single missing or extra token.
	 *
	 *  EXTRA TOKEN
	 *
	 *  LA(1) is not what we are looking for.  If LA(2) has the right token,
	 *  however, then assume LA(1) is some extra spurious token.  Delete it
	 *  and LA(2) as if we were doing a normal match(), which advances the
	 *  input.
	 *
	 *  MISSING TOKEN
	 *
	 *  If current token is consistent with what could come after
	 *  ttype then it is ok to "insert" the missing token, else throw
	 *  exception For example, Input "i=(3;" is clearly missing the
	 *  ')'.  When the parser returns from the nested call to expr, it
	 *  will have call chain:
	 *
	 *    stat -> expr -> atom
	 *
	 *  and it will be trying to match the ')' at this point in the
	 *  derivation: 
	 *
	 *       => ID '=' '(' INT ')' ('+' atom)* ';'
	 *                          ^
	 *  match() will see that ';' doesn't match ')' and report a
	 *  mismatched token error.  To recover, it sees that LA(1)==';'
	 *  is in the set of tokens that can follow the ')' token
	 *  reference in rule atom.  It can assume that you forgot the ')'.
	 */
	public void recoverFromMismatchedToken(MismatchedTokenException e,
										   int ttype,
										   org.antlr.runtime.BitSet follow)
		throws MismatchedTokenException
	{
		// if next token is what we are looking for then "delete" this token
		if ( input.LA(2)==ttype ) {
			reportError(e);
			System.err.println("deleting "+input.LT(1));
			recoverFromExtraToken(e,ttype,follow);
			input.consume(); // move past ttype token as if all were ok
			return;
		}
		if ( !recoverFromMismatchedElement(e,follow) ) {
			throw e;
		}
	}

	/** How to recover when there is an extra, spurious token.  Mainly
	 *  I factored out this functionality so I can override it in the
	 *  DebugParser subclass.  It was the only way I could get the
	 *  recover/recovered debug events in the right spot.
	 */
	public void recoverFromExtraToken(MismatchedTokenException e,
									  int ttype,
									  org.antlr.runtime.BitSet follow)
		throws MismatchedTokenException
	{
		input.consume(); // simply delete extra token
	}

	public void recoverFromMismatchedSet(RecognitionException e,
										 org.antlr.runtime.BitSet follow)
		throws RecognitionException
	{
		// TODO do single token deletion like above for Token mismatch
		if ( !recoverFromMismatchedElement(e,follow) ) {
			throw e;
		}
	}

	protected boolean recoverFromMismatchedElement(RecognitionException e,
												   org.antlr.runtime.BitSet follow)
	{
		// compute what can follow this grammar element reference
		if ( follow.member(Token.EOR_TOKEN_TYPE) ) {
			BitSet viableTokensFollowingThisRule =
				computeContextSensitiveRuleFOLLOW();
			follow = follow.or(viableTokensFollowingThisRule);
			follow.remove(Token.EOR_TOKEN_TYPE);
		}
		// if current token is consistent with what could come after set
		// then it is ok to "insert" the missing token, else throw exception
		//System.out.println("viable tokens="+follow.toString(getTokenNames())+")");
		if ( follow.member(input.LA(1)) ) {
			reportError(e);
			return true;
		}
		System.err.println("nothing to do; throw exception");
		return false;
	}

	public void consumeUntil(int tokenType) {
		int ttype = input.LA(1);
		while (ttype != Token.EOF && ttype != tokenType) {
			input.consume();
			ttype = input.LA(1);
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
		int ttype = input.LA(1);
		while (ttype != Token.EOF && !set.member(ttype) ) {
			//System.out.println("LT(1)="+input.LT(1));
			input.consume();
			ttype = input.LA(1);
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
