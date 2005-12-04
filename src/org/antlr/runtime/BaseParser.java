package org.antlr.runtime;

import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

/** A generic parser that can handle recognizers generated from
 *  both parser grammars and tree grammars.  This is all the parsing
 *  support code essentially; most of it is error recovery stuff.
 */
public abstract class BaseParser {
	/** Track the set of token types that can follow any rule invocation.
	 *  List<BitSet>.
	 */
	protected Stack following = new Stack();

	/** This is true when we see an error and before having successfully
	 *  matched a token.  Prevents generation of more than one error message
	 *  per error.
	 */
	protected boolean errorRecovery = false;

	/** The index into the input stream where the last error occurred.
	 * 	This is used to prevent infinite loops where an error is found
	 *  but no token is consumed during recovery...another error is found,
	 *  ad naseum.  This is a failsafe mechanism to guarantee that at least
	 *  one token/tree node is consumed for two errors.
	 */
	protected int lastErrorIndex = -1;

	/** In lieu of a return value, this indicates that a rule or token
	 *  has failed to match.  Reset to false upon valid token match.
	 */
	protected boolean failed = false;

	/** If 0, no backtracking is going on.  Safe to exec actions etc...
	 *  If >0 then it's the level of backtracking.
	 */
	protected int backtracking = 0;

	/** When backtracking, we need to know the start of the outermost
	 *  current backtracking.  The rule memoization uses this as an offset
	 *  in its memo array.
	protected int firstBacktrackingMarker = -1;
	 */

	/** reset the parser's state */
	public void reset() {
		following.setSize(0);
	}

	/** Match current input symbol against ttype.  Upon error, do one token
	 *  insertion or deletion if possible.  You can override to not recover
	 *  here and bail out of the current production to the normal error
	 *  exception catch (at the end of the method) by just throwing
	 *  MismatchedTokenException upon input.LA(1)!=ttype.
	 */
	public void match(IntStream input, int ttype, BitSet follow)
		throws MismatchedTokenException
	{
		if ( input.LA(1)==ttype ) {
			input.consume();
			errorRecovery = false;
			failed = false;
			return;
		}
		if ( backtracking>0 ) {
			failed = true;
			return;
		}
		MismatchedTokenException mte =
			new MismatchedTokenException(ttype, input);
		recoverFromMismatchedToken(input, mte, ttype, follow);
		return;
	}

	public void matchAny(IntStream input) {
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
			//System.err.print("[SPURIOUS] ");
			return;
		}
		errorRecovery = true;

		displayRecognitionError(this.getClass().getName(),
								this.getTokenNames(),
								e);
	}

	public static void displayRecognitionError(String name,
											   Object[] tokenNames,
											   RecognitionException e)
	{
		System.err.print(getRuleInvocationStack(e, name)+
						 ": line "+e.line+":"+e.charPositionInLine+" ");
		if ( e instanceof MismatchedTokenException ) {
			MismatchedTokenException mte = (MismatchedTokenException)e;
			System.err.println("mismatched token: "+
							   e.token+
							   "; expecting type "+
							   tokenNames[mte.expecting]);
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
		else if ( e instanceof FailedPredicateException ) {
			FailedPredicateException fpe = (FailedPredicateException)e;
			System.err.println("rule "+fpe.ruleName+" failed predicate: {"+
							   fpe.predicateText+"}?");
		}
	}

	/** Recover from an error found on the input stream.  Mostly this is
	 *  NoViableAlt exceptions, but could be a mismatched token that
	 *  the match() routine could not recover from.
	 */
	public void recover(IntStream input, RecognitionException re) {
		if ( lastErrorIndex==input.index() ) {
			// uh oh, another error at same token index; must be a case
			// where LT(1) is in the recovery token set so nothing is
			// consumed; consume a single token so at least to prevent
			// an infinite loop; this is a failsafe.
			input.consume();
		}
		lastErrorIndex = input.index();
		BitSet followSet = computeErrorRecoverySet();
		beginResync();
		consumeUntil(input, followSet);
		endResync();
	}

	/** A hook to listen in on the token consumption during error recovery.
	 *  The DebugParser subclasses this to fire events to the listenter.
	 */
	public void beginResync() {
	}

	public void endResync() {
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
	public void recoverFromMismatchedToken(IntStream input,
										   MismatchedTokenException e,
										   int ttype,
										   BitSet follow)
		throws MismatchedTokenException
	{
		//System.out.println("recoverFromMismatchedToken");
		// if next token is what we are looking for then "delete" this token
		if ( input.LA(2)==ttype ) {
			reportError(e);
			/*
			System.err.println("recoverFromMismatchedToken deleting "+input.LT(1)+
							   " since "+input.LT(2)+" is what we want");
			*/
			beginResync();
			input.consume(); // simply delete extra token
			endResync();
			input.consume(); // move past ttype token as if all were ok
			return;
		}
		if ( !recoverFromMismatchedElement(input,e,follow) ) {
			throw e;
		}
	}

	public void recoverFromMismatchedSet(IntStream input,
										 RecognitionException e,
										 BitSet follow)
		throws RecognitionException
	{
		// TODO do single token deletion like above for Token mismatch
		if ( !recoverFromMismatchedElement(input,e,follow) ) {
			throw e;
		}
	}

	/** This code is factored out from mismatched token and mismatched set
	 *  recovery.  It handles "single token insertion" error recovery for
	 *  both.  No tokens are consumed to recover from insertions.  Return
	 *  true if recovery was possible else return false.
	 */
	protected boolean recoverFromMismatchedElement(IntStream input,
												   RecognitionException e,
												   BitSet follow)
	{
		//System.out.println("recoverFromMismatchedElement");
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
			//System.out.println("LT(1)=="+input.LT(1)+" is consistent with what follows; inserting...");
			reportError(e);
			return true;
		}
		//System.err.println("nothing to do; throw exception");
		return false;
	}

	public void consumeUntil(IntStream input, int tokenType) {
		//System.out.println("consumeUntil "+tokenType);
		int ttype = input.LA(1);
		while (ttype != Token.EOF && ttype != tokenType) {
			input.consume();
			ttype = input.LA(1);
		}
	}

	/** Consume tokens until one matches the given token set */
	public void consumeUntil(IntStream input, BitSet set) {
		/*
		System.out.println("consumeUntil("+set.toString(getTokenNames())+")");
		System.out.println("LT(1)="+
						   input.LT(1).toString());
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

	/** Used to print out token names like ID during debugging and
	 *  error reporting.  The generated parsers implement a method
	 *  that overrides this to point to their String[] tokenNames.
	 */
	public String[] getTokenNames() {
		return null;
	}

	/** A convenience method for use most often with template rewrites.
	 *  Convert a List<Token> to List<String>
	 */
	public List toStrings(List tokens) {
		if ( tokens==null ) return null;
		List strings = new ArrayList(tokens.size());
		for (int i=0; i<tokens.size(); i++) {
			strings.add(((Token)tokens.get(i)).getText());
		}
		return strings;
	}

	/** Convert a List<RuleReturnScope> to List<StringTemplate> by copying
	 *  out the .st property.  Useful when converting from
	 *  list labels to template attributes:
	 *
	 *    a : ids+=ID -> foo(ids={toTemplates($ids)})
	 *      ;
	 */
	public List toTemplates(List retvals) {
		if ( retvals==null ) return null;
		List strings = new ArrayList(retvals.size());
		for (int i=0; i<retvals.size(); i++) {
			strings.add(((RuleReturnScope)retvals.get(i)).getTemplate());
		}
		return strings;
	}
}
