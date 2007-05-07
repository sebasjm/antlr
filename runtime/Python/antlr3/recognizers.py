"""ANTLR3 runtime package"""

# [The "BSD licence"]
# Copyright (c) 2005-2006 Terence Parr
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 3. The name of the author may not be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
# IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
# OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
# NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
# THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

import sys

from antlr3.constants import DEFAULT_CHANNEL, HIDDEN_CHANNEL, EOF, \
     EOR_TOKEN_TYPE
from antlr3.exceptions import RecognitionException, MismatchedTokenException, \
     MismatchedRangeException
from antlr3.tokens import CommonToken, EOF_TOKEN, SKIP_TOKEN

# compatibility stuff
try:
    set = set
    frozenset = frozenset
except NameError:
    from sets import Set as set, ImmutableSet as frozenset


try:
    reversed = reversed
except NameError:
    def reversed(l):
        l = l[:]
        l.reverse()
        return l


class BaseRecognizer(object):
    """
    A generic recognizer that can handle recognizers generated from
    lexer, parser, and tree grammars.  This is all the parsing
    support code essentially; most of it is error recovery stuff and
    backtracking.
    """

    MEMO_RULE_FAILED = -2
    MEMO_RULE_UNKNOWN = -1

    # copies from Token object for convenience in actions
    DEFAULT_TOKEN_CHANNEL = DEFAULT_CHANNEL

    # for convenience in actions
    HIDDEN = HIDDEN_CHANNEL

    # overridden by generated subclasses
    tokenNames = None
    
    def __init__(self):
        # Input stream of the recognizer. Must be initialized by a subclass.
        self.input = None
        
        # Track the set of token types that can follow any rule invocation.
        # Stack grows upwards.  When it hits the max, it grows 2x in size
        # and keeps going.
        self.following = []

        # This is true when we see an error and before having successfully
        # matched a token.  Prevents generation of more than one error message
        # per error.
        self.errorRecovery = False

        # The index into the input stream where the last error occurred.
        # This is used to prevent infinite loops where an error is found
        # but no token is consumed during recovery...another error is found,
        # ad naseum.  This is a failsafe mechanism to guarantee that at least
        # one token/tree node is consumed for two errors.
        self.lastErrorIndex = -1

        # In lieu of a return value, this indicates that a rule or token
        # has failed to match.  Reset to false upon valid token match.
        self.failed = False

        # If 0, no backtracking is going on.  Safe to exec actions etc...
        # If >0 then it's the level of backtracking.
        self.backtracking = 0

        # An array[size num rules] of Map<Integer,Integer> that tracks
        # the stop token index for each rule.  ruleMemo[ruleIndex] is
        # the memoization table for ruleIndex.  For key ruleStartIndex, you
        # get back the stop token for associated rule or MEMO_RULE_FAILED.
        #
        #  This is only used if rule memoization is on (which it is by default).
        self.ruleMemo = None


    # this one only exists to shut up pylint :(
    def setInput(self, input):
        self.input = input

        
    def reset(self):
        """
        reset the parser's state; subclasses must rewinds the input stream
        """
        
        # wack everything related to error recovery
        self._fsp = -1
        self.errorRecovery = False
        self.lastErrorIndex = -1
        self.failed = False
        # wack everything related to backtracking and memoization
        self.backtracking = 0
        if self.ruleMemo is not None:
            self.ruleMemo = {}


    def match(self, input, ttype, follow):
        """
        Match current input symbol against ttype.  Upon error, do one token
        insertion or deletion if possible.  You can override to not recover
	here and bail out of the current production to the normal error
	exception catch (at the end of the method) by just throwing
	MismatchedTokenException upon input.LA(1)!=ttype.
        """
        
        if self.input.LA(1) == ttype:
            self.input.consume()
            self.errorRecovery = False
            self.failed = False
            return

        if self.backtracking > 0:
            self.failed = True
            return

        self.mismatch(input, ttype, follow)
        return


    def matchAny(self, input):
        self.errorRecovery = False
        self.failed = False
        self.input.consume()


    def mismatch(self, input, ttype, follow):
        """
        factor out what to do upon token mismatch so tree parsers can behave
        differently.  Override this method in your parser to do things
	like bailing out after the first error; just throw the mte object
	instead of calling the recovery method.
        """
        
        mte = MismatchedTokenException(ttype, input)
        self.recoverFromMismatchedToken(input, mte, ttype, follow)


    def reportError(self, e):
        """Report a recognition problem.
            
        This method sets errorRecovery to indicate the parser is recovering
        not parsing.  Once in recovery mode, no errors are generated.
        To get out of recovery mode, the parser must successfully match
        a token (after a resync).  So it will go:

        1. error occurs
        2. enter recovery mode, report error
        3. consume until token found in resynch set
        4. try to resume parsing
        5. next match() will reset errorRecovery mode
        """
        
        # if we've already reported an error and have not matched a token
        # yet successfully, don't report any errors.
        if self.errorRecovery:
            return

        self.errorRecovery = True

        self.displayRecognitionError(self.tokenNames, e)


    def displayRecognitionError(self, tokenNames, e):
        hdr = self.getErrorHeader(e)
        msg = self.getErrorMessage(e, tokenNames)
        self.emitErrorMessage(hdr+" "+msg)


    def getErrorMessage(self, e, tokenNames):
        """
        What error message should be generated for the various
        exception types?
        
        Not very object-oriented code, but I like having all error message
        generation within one method rather than spread among all of the
	exception classes. This also makes it much easier for the exception
	handling because the exception classes do not have to have pointers back
	to this object to access utility routines and so on. Also, changing
	the message for an exception type would be difficult because you
	would have to subclassing exception, but then somehow get ANTLR
	to make those kinds of exception objects instead of the default.
	This looks weird, but trust me--it makes the most sense in terms
	of flexibility.

        For grammar debugging, you will want to override this to add
	more information such as the stack frame with
	getRuleInvocationStack(e, this.getClass().getName()) and,
	for no viable alts, the decision description and state etc...

        Override this to change the message generated for one or more
	exception types.
        """

        # FIXME: correct implentation
        return str(e)
    

    def getErrorHeader(self, e):
        """
        What is the error header, normally line/character position information?
        """
        
        return "line %d:%d" % (e.line, e.charPositionInLine)


    def getTokenErrorDisplay(self, t):
        """
        How should a token be displayed in an error message? The default
        is to display just the text, but during development you might
        want to have a lot of information spit out.  Override in that case
        to use t.toString() (which, for CommonToken, dumps everything about
        the token). This is better than forcing you to override a method in
        your token objects because you don't have to go modify your lexer
        so that it creates a new Java type.
        """
        
        raise NotImplementedError
    

    def emitErrorMessage(self, msg):
        """Override this method to change where error messages go"""
        sys.stderr.write(msg + '\n')


    def recover(self, input, re):
        """
        Recover from an error found on the input stream.  Mostly this is
        NoViableAlt exceptions, but could be a mismatched token that
        the match() routine could not recover from.
        """
        
        # PROBLEM? what if input stream is not the same as last time
        # perhaps make lastErrorIndex a member of input
        if self.lastErrorIndex == input.index():
            # uh oh, another error at same token index; must be a case
            # where LT(1) is in the recovery token set so nothing is
            # consumed; consume a single token so at least to prevent
            # an infinite loop; this is a failsafe.
            input.consume()

        self.lastErrorIndex = input.index()
        followSet = self.computeErrorRecoverySet()
        
        self.beginResync()
        self.consumeUntil(input, followSet)
        self.endResync()


    def beginResync(self):
        """
        A hook to listen in on the token consumption during error recovery.
        The DebugParser subclasses this to fire events to the listenter.
        """

        pass


    def endResync(self):
        """
        A hook to listen in on the token consumption during error recovery.
        The DebugParser subclasses this to fire events to the listenter.
        """

        pass


    def computeErrorRecoverySet(self):
        """
        Compute the error recovery set for the current rule.  During
        rule invocation, the parser pushes the set of tokens that can
        follow that rule reference on the stack; this amounts to
        computing FIRST of what follows the rule reference in the
        enclosing rule. This local follow set only includes tokens
        from within the rule; i.e., the FIRST computation done by
        ANTLR stops at the end of a rule.

        EXAMPLE

        When you find a "no viable alt exception", the input is not
        consistent with any of the alternatives for rule r.  The best
        thing to do is to consume tokens until you see something that
        can legally follow a call to r *or* any rule that called r.
        You don't want the exact set of viable next tokens because the
        input might just be missing a token--you might consume the
        rest of the input looking for one of the missing tokens.

        Consider grammar:

        a : '[' b ']'
          | '(' b ')'
          ;
        b : c '^' INT ;
        c : ID
          | INT
          ;

        At each rule invocation, the set of tokens that could follow
        that rule is pushed on a stack.  Here are the various "local"
        follow sets:

        FOLLOW(b1_in_a) = FIRST(']') = ']'
        FOLLOW(b2_in_a) = FIRST(')') = ')'
        FOLLOW(c_in_b) = FIRST('^') = '^'

        Upon erroneous input "[]", the call chain is

        a -> b -> c

        and, hence, the follow context stack is:

        depth  local follow set     after call to rule
          0         <EOF>                    a (from main())
          1          ']'                     b
          3          '^'                     c

        Notice that ')' is not included, because b would have to have
        been called from a different context in rule a for ')' to be
        included.

        For error recovery, we cannot consider FOLLOW(c)
        (context-sensitive or otherwise).  We need the combined set of
        all context-sensitive FOLLOW sets--the set of all tokens that
        could follow any reference in the call chain.  We need to
        resync to one of those tokens.  Note that FOLLOW(c)='^' and if
        we resync'd to that token, we'd consume until EOF.  We need to
        sync to context-sensitive FOLLOWs for a, b, and c: {']','^'}.
        In this case, for input "[]", LA(1) is in this set so we would
        not consume anything and after printing an error rule c would
        return normally.  It would not find the required '^' though.
        At this point, it gets a mismatched token error and throws an
        exception (since LA(1) is not in the viable following token
        set).  The rule exception handler tries to recover, but finds
        the same recovery set and doesn't consume anything.  Rule b
        exits normally returning to rule a.  Now it finds the ']' (and
        with the successful match exits errorRecovery mode).

        So, you cna see that the parser walks up call chain looking
        for the token that was a member of the recovery set.

        Errors are not generated in errorRecovery mode.

        ANTLR's error recovery mechanism is based upon original ideas:

        "Algorithms + Data Structures = Programs" by Niklaus Wirth

        and

        "A note on error recovery in recursive descent parsers":
        http://portal.acm.org/citation.cfm?id=947902.947905

        Later, Josef Grosch had some good ideas:

        "Efficient and Comfortable Error Recovery in Recursive Descent
        Parsers":
        ftp://www.cocolab.com/products/cocktail/doca4.ps/ell.ps.zip

        Like Grosch I implemented local FOLLOW sets that are combined
        at run-time upon error to avoid overhead during parsing.
        """
        
        return self.combineFollows(False)

        
    def computeContextSensitiveRuleFOLLOW(self):
        """
        Compute the context-sensitive FOLLOW set for current rule.
        This is set of token types that can follow a specific rule
        reference given a specific call chain.  You get the set of
        viable tokens that can possibly come next (lookahead depth 1)
        given the current call chain.  Contrast this with the
        definition of plain FOLLOW for rule r:

         FOLLOW(r)={x | S=>*alpha r beta in G and x in FIRST(beta)}

        where x in T* and alpha, beta in V*; T is set of terminals and
        V is the set of terminals and nonterminals.  In other words,
        FOLLOW(r) is the set of all tokens that can possibly follow
        references to r in *any* sentential form (context).  At
        runtime, however, we know precisely which context applies as
        we have the call chain.  We may compute the exact (rather
        than covering superset) set of following tokens.

        For example, consider grammar:

        stat : ID '=' expr ';'      // FOLLOW(stat)=={EOF}
             | "return" expr '.'
             ;
        expr : atom ('+' atom)* ;   // FOLLOW(expr)=={';','.',')'}
        atom : INT                  // FOLLOW(atom)=={'+',')',';','.'}
             | '(' expr ')'
             ;

        The FOLLOW sets are all inclusive whereas context-sensitive
        FOLLOW sets are precisely what could follow a rule reference.
        For input input "i=(3);", here is the derivation:

        stat => ID '=' expr ';'
             => ID '=' atom ('+' atom)* ';'
             => ID '=' '(' expr ')' ('+' atom)* ';'
             => ID '=' '(' atom ')' ('+' atom)* ';'
             => ID '=' '(' INT ')' ('+' atom)* ';'
             => ID '=' '(' INT ')' ';'

        At the "3" token, you'd have a call chain of

          stat -> expr -> atom -> expr -> atom

        What can follow that specific nested ref to atom?  Exactly ')'
        as you can see by looking at the derivation of this specific
        input.  Contrast this with the FOLLOW(atom)={'+',')',';','.'}.

        You want the exact viable token set when recovering from a
        token mismatch.  Upon token mismatch, if LA(1) is member of
        the viable next token set, then you know there is most likely
        a missing token in the input stream.  "Insert" one by just not
        throwing an exception.
        """

        return self.combineFollows(True)


    def combineFollows(self, exact):
        followSet = set()
        for localFollowSet in reversed(self.following):
            followSet |= localFollowSet
            if exact and EOR_TOKEN_TYPE not in localFollowSet:
                break

        followSet -= set([EOR_TOKEN_TYPE])
        return followSet


    def recoverFromMismatchedToken(self, input, e, ttype, follow):
        """Attempt to recover from a single missing or extra token.

        EXTRA TOKEN

        LA(1) is not what we are looking for.  If LA(2) has the right token,
        however, then assume LA(1) is some extra spurious token.  Delete it
        and LA(2) as if we were doing a normal match(), which advances the
        input.

        MISSING TOKEN

        If current token is consistent with what could come after
        ttype then it is ok to "insert" the missing token, else throw
        exception For example, Input "i=(3;" is clearly missing the
        ')'.  When the parser returns from the nested call to expr, it
        will have call chain:

          stat -> expr -> atom

        and it will be trying to match the ')' at this point in the
        derivation:

             => ID '=' '(' INT ')' ('+' atom)* ';'
                                ^
        match() will see that ';' doesn't match ')' and report a
        mismatched token error.  To recover, it sees that LA(1)==';'
        is in the set of tokens that can follow the ')' token
        reference in rule atom.  It can assume that you forgot the ')'.
        """
                                         
        # if next token is what we are looking for then "delete" this token
        if input.LA(2) == ttype:
            self.reportError(e)

            self.beginResync()
            input.consume() # simply delete extra token
            self.endResync()
            input.consume()  # move past ttype token as if all were ok
            return

        if not self.recoverFromMismatchedElement(input, e, follow):
            raise e



    def recoverFromMismatchedSet(self, input, e, follow):
        raise NotImplementedError


    def recoverFromMismatchedElement(self, input, e, follow):
        """
        This code is factored out from mismatched token and mismatched set
        recovery.  It handles "single token insertion" error recovery for
        both.  No tokens are consumed to recover from insertions.  Return
        true if recovery was possible else return false.
        """
        
        if follow is None:
            # we have no information about the follow; we can only consume
            # a single token and hope for the best
            return False

        # compute what can follow this grammar element reference
        if EOR_TOKEN_TYPE in follow:
            viableTokensFollowingThisRule = \
                self.computeContextSensitiveRuleFOLLOW()
            
            follow = (follow | viableTokensFollowingThisRule) \
                     - set([EOR_TOKEN_TYPE])

        # if current token is consistent with what could come after set
        # then it is ok to "insert" the missing token, else throw exception
        if input.LA(1) in follow:
            self.reportError(e)
            return True

        # nothing to do; throw exception
        return False


    def consumeUntil(self, input, tokenTypes):
        """
        Consume tokens until one matches the given token or token set

        tokenTypes can be a single token type or a set of token types
        
        """
        
        if not isinstance(tokenTypes, (set, frozenset)):
            tokenTypes = frozenset([tokenTypes])

        ttype = input.LA(1)
        while ttype != EOF and ttype not in tokenTypes:
            input.consume()
            ttype = input.LA(1)


## 	/** Return List<String> of the rules in your parser instance
## 	 *  leading up to a call to this method.  You could override if
## 	 *  you want more details such as the file/line info of where
## 	 *  in the parser java code a rule is invoked.
## 	 *
## 	 *  This is very useful for error messages and for context-sensitive
## 	 *  error recovery.
## 	 */
## 	public List getRuleInvocationStack() {
## 		String parserClassName = getClass().getName();
## 		return getRuleInvocationStack(new Throwable(), parserClassName);
## 	}

## 	/** A more general version of getRuleInvocationStack where you can
## 	 *  pass in, for example, a RecognitionException to get it's rule
## 	 *  stack trace.  This routine is shared with all recognizers, hence,
## 	 *  static.
## 	 *
## 	 *  TODO: move to a utility class or something; weird having lexer call this
## 	 */
## 	public static List getRuleInvocationStack(Throwable e,
## 											  String recognizerClassName)
## 	{
## 		List rules = new ArrayList();
## 		StackTraceElement[] stack = e.getStackTrace();
## 		int i = 0;
## 		for (i=stack.length-1; i>=0; i--) {
## 			StackTraceElement t = stack[i];
## 			if ( t.getClassName().startsWith("org.antlr.runtime.") ) {
## 				continue; // skip support code such as this method
## 			}
## 			if ( t.getMethodName().equals("nextToken") ) {
## 				continue;
## 			}
## 			if ( !t.getClassName().equals(recognizerClassName) ) {
## 				continue; // must not be part of this parser
## 			}
##             rules.add(t.getMethodName());
## 		}
## 		return rules;
## 	}

## 	public int getBacktrackingLevel() {
## 		return backtracking;
## 	}

## 	/** For debugging and other purposes, might want the grammar name.
## 	 *  Have ANTLR generate an implementation for this method.
## 	 */
## 	public String getGrammarFileName() {
## 		return null;
## 	}

## 	/** A convenience method for use most often with template rewrites.
## 	 *  Convert a List<Token> to List<String>
## 	 */
## 	public List toStrings(List tokens) {
## 		if ( tokens==null ) return null;
## 		List strings = new ArrayList(tokens.size());
## 		for (int i=0; i<tokens.size(); i++) {
## 			strings.add(((Token)tokens.get(i)).getText());
## 		}
## 		return strings;
## 	}

## 	/** Convert a List<RuleReturnScope> to List<StringTemplate> by copying
## 	 *  out the .st property.  Useful when converting from
## 	 *  list labels to template attributes:
## 	 *
## 	 *    a : ids+=rule -> foo(ids={toTemplates($ids)})
## 	 *      ;
## 	 */
## 	public List toTemplates(List retvals) {
## 		if ( retvals==null ) return null;
## 		List strings = new ArrayList(retvals.size());
## 		for (int i=0; i<retvals.size(); i++) {
## 			strings.add(((RuleReturnScope)retvals.get(i)).getTemplate());
## 		}
## 		return strings;
## 	}

    def getRuleMemoization(self, ruleIndex, ruleStartIndex):
        """
        Given a rule number and a start token index number, return
        MEMO_RULE_UNKNOWN if the rule has not parsed input starting from
        start index.  If this rule has parsed input starting from the
        start index before, then return where the rule stopped parsing.
        It returns the index of the last token matched by the rule.

        For now we use a hashtable and just the slow Object-based one.
        Later, we can make a special one for ints and also one that
        tosses out data after we commit past input position i.
	"""
        
        if ruleIndex not in self.ruleMemo:
            self.ruleMemo[ruleIndex] = {}
		
        stopIndex = self.ruleMemo[ruleIndex].get(ruleStartIndex, None)
        if stopIndex is None:
            return self.MEMO_RULE_UNKNOWN

        return stopIndex


    def alreadyParsedRule(self, input, ruleIndex):
        """
        Has this rule already parsed input at the current index in the
        input stream?  Return the stop token index or MEMO_RULE_UNKNOWN.
        If we attempted but failed to parse properly before, return
        MEMO_RULE_FAILED.

        This method has a side-effect: if we have seen this input for
        this rule and successfully parsed before, then seek ahead to
        1 past the stop token matched for this rule last time.
        """
        
        stopIndex = self.getRuleMemoization(ruleIndex, input.index())
        if stopIndex == self.MEMO_RULE_UNKNOWN:
            return False

        if stopIndex == self.MEMO_RULE_FAILED:
            self.failed = True

        else:
            input.seek(stopIndex + 1)

        return True


    def memoize(self, input, ruleIndex, ruleStartIndex):
        """
        Record whether or not this rule parsed the input at this position
	successfully.
	"""

        if self.failed:
            stopTokenIndex = self.MEMO_RULE_FAILED
        else:
            stopTokenIndex = input.index() - 1
        
        if ruleIndex in self.ruleMemo:
            self.ruleMemo[ruleIndex][ruleStartIndex] = stopTokenIndex


## 	/** return how many rule/input-index pairs there are in total.
## 	 *  TODO: this includes synpreds. :(
## 	 */
## 	public int getRuleMemoizationCacheSize() {
## 		int n = 0;
## 		for (int i = 0; ruleMemo!=null && i < ruleMemo.length; i++) {
## 			Map ruleMap = ruleMemo[i];
## 			if ( ruleMap!=null ) {
## 				n += ruleMap.size(); // how many input indexes are recorded?
## 			}
## 		}
## 		return n;
## 	}

## 	public void traceIn(String ruleName, int ruleIndex, Object inputSymbol)  {
## 		System.out.print("enter "+ruleName+" "+inputSymbol);
## 		if ( failed ) {
## 			System.out.println(" failed="+failed);
## 		}
## 		if ( backtracking>0 ) {
## 			System.out.print(" backtracking="+backtracking);
## 		}
## 		System.out.println();
## 	}

## 	public void traceOut(String ruleName,
## 						 int ruleIndex,
## 						 Object inputSymbol)
## 	{
## 		System.out.print("exit "+ruleName+" "+inputSymbol);
## 		if ( failed ) {
## 			System.out.println(" failed="+failed);
## 		}
## 		if ( backtracking>0 ) {
## 			System.out.print(" backtracking="+backtracking);
## 		}
## 		System.out.println();
## 	}


    def synpred(self, input, fragment):
        """
        A syntactic predicate.  Returns true/false depending on whether
        the specified grammar fragment matches the current input stream.
        This resets the failed instance var afterwards.
        """

        raise NotImplementedError


class Lexer(BaseRecognizer):
    """
    A lexer is recognizer that draws input symbols from a character stream.
    lexer grammars result in a subclass of this object. A Lexer object
    uses simplified match() and error recovery mechanisms in the interest
    of speed.
    """

    def __init__(self, input):
        BaseRecognizer.__init__(self)
        
        # Where is the lexer drawing characters from?
        self.input = input

        # The goal of all lexer rules/methods is to create a token object.
	# This is an instance variable as multiple rules may collaborate to
	# create a single token.  nextToken will return this object after
	# matching lexer rule(s).  If you subclass to allow multiple token
	# emissions, then set this to the last token to be matched or
	# something nonnull so that the auto token emit mechanism will not
	# emit another token.
        self.token = None

	# What character index in the stream did the current token start at?
	# Needed, for example, to get the text for current token.  Set at
	# the start of nextToken.
        self.tokenStartCharIndex = -1

        # You can set the text for the current token to override what is in
	# the input char buffer.  Use setText() or can set this instance var.
        self.text = None

	# We must track the token rule nesting level as we only want to
	# emit a token automatically at the outermost level so we don't get
	# two if FLOAT calls INT.  To save code space and time, do not
	# inc/dec this in fragment rules.
        self.ruleNestingLevel = 0


    def reset(self):
        BaseRecognizer.reset(self) # reset all recognizer state variables

        # wack Lexer state variables
        self.token = None
        self.tokenStartCharIndex = -1
        self.text = None
        self.ruleNestingLevel = 0
        if self.input is not None:
            self.input.seek(0) # rewind the input


    def nextToken(self):
        """
        Return a token from this source; i.e., match a token on the char
	stream.
	"""
        
        while 1:
            self.token = None
            self.tokenStartCharIndex = self.getCharIndex()
            self.text = None
            if self.input.LA(1) == EOF:
                return EOF_TOKEN

            try:
                self.mTokens()
                if self.token != SKIP_TOKEN:
                    return self.token

            except RecognitionException, re:
                raise # no error reporting/recovery
                #self.reportError(re)
                #self.recover(re)


    def skip(self):
        """
	Instruct the lexer to skip creating a token for current lexer rule
	and look for another token.  nextToken() knows to keep looking when
	a lexer rule finishes with token set to SKIP_TOKEN.  Recall that
	if token==null at end of any token rule, it creates one for you
	and emits it.
	"""
        
        self.token = SKIP_TOKEN


    def mTokens(self):
        """This is the lexer entry point that sets instance var 'token'"""

        # abstract method
        raise NotImplementedError
    

    def setCharStream(self, input):
        """Set the char stream and reset the lexer"""
        self.input = input
        self.token = None
        self.tokenStartCharIndex = -1
        self.ruleNestingLevel = 0


##     def emit(self, token):
## 	"""
##         Currently does not support multiple emits per nextToken invocation
## 	for efficiency reasons.  Subclass and override this method and
## 	nextToken (to push tokens into a list and pull from that list rather
## 	than a single variable as this implementation does).
## 	"""
        
##         self.token = token


    def emit(self, tokenType, line, charPosition, channel, start, stop):
        """
        The standard method called to automatically emit a token at the
	outermost lexical rule.  The token object should point into the
	char buffer start..stop.  If there is a text override in 'text',
	use that to set the token's text.
	"""

        t = CommonToken()
        t.input = self.input
        t.type = tokenType
        t.channel = channel
        t.start = start
        t.stop = stop
        t.line = line
        t.text = self.text
        t.charPositionInLine = charPosition

        self.token = t
        
        return t


    def match(self, s):
        if isinstance(s, basestring):
            i = 0
            while i < len(s):
                if self.input.LA(1) != s[i]:
                    if self.backtracking > 0:
                        self.failed = True
                        return

                    mte = MismatchedTokenException(s[i], self.input)
                    self.recover(mte)
                    raise mte

                i += 1
                self.input.consume()
                self.failed = False

        else:
            if self.input.LA(1) != s:
                if self.backtracking > 0:
                    self.failed = True
                    return

                mte = MismatchedTokenException(s, self.input)
                self.recover(mte)
                raise mte
        
            self.input.consume()
            self.failed = False
            

    def matchAny(self):
        self.input.consume()


    def matchRange(self, a, b):
        if self.input.LA(1) < a or self.input.LA(1) > b:
            if self.backtracking > 0:
                self.failed = True
                return

            mre = MismatchedRangeException(a, b, self.input)
            self.recover(mre)
            raise mre

        self.input.consume()
        self.failed = False


    def getLine(self):
        return self.input.line


    def getCharPositionInLine(self):
        return self.input.charPositionInLine


    def getCharIndex(self):
        """What is the index of the current character of lookahead?"""
        
        return self.input.index()


    def getText(self):
        """
        Return the text matched so far for the current token or any
        text override.
        """
        if self.text is not None:
            return self.text
        
        return self.input.substring(
            self.tokenStartCharIndex,
            self.getCharIndex()-1
            )


    def setText(self, text):
        """
        Set the complete text of this token; it wipes any previous
	changes to the text.
	"""
        self.text = text


    def reportError(self, e):
        ## TODO: not thought about recovery in lexer yet.

        ## # if we've already reported an error and have not matched a token
        ## # yet successfully, don't report any errors.
        ## if self.errorRecovery:
        ##     #System.err.print("[SPURIOUS] ");
        ##     return;
        ## 
        ## self.errorRecovery = True

        self.displayRecognitionError(self.tokenNames, e)


    def getErrorMessage(self, e, tokenNames):
        raise NotImplementedError


    def getCharErrorDisplay(self, c):
        raise NotImplementedError


    def recover(self, re):
        """
        Lexers can normally match any char in it's vocabulary after matching
	a token, so do the easy thing and just kill a character and hope
	it all works out.  You can instead use the rule invocation stack
	to do sophisticated error recovery if you are in a fragment rule.
	"""

        self.input.consume()


    def traceIn(self, ruleName, ruleIndex):
        raise NotImplementedError


    def traceOut(self, ruleName, ruleIndex):
        raise NotImplementedError



class Parser(BaseRecognizer):
    def __init__(self, lexer):
        BaseRecognizer.__init__(self)

        self.input = lexer

