/** \file
 * Implementation of the base functionality for an ANTLR3 parser.
 */
#include    <antlr3parser.h>

static void			setTokenStream		    (pANTLR3_PARSER parser, pANTLR3_TOKEN_STREAM);
static pANTLR3_TOKEN_STREAM	getTokenStream		    (pANTLR3_PARSER parser);
static void			reset			    (pANTLR3_PARSER parser);
static void			reportError		    (pANTLR3_PARSER parser);
static void			displayRecognitionError	    (pANTLR3_PARSER parser, pANTLR3_UINT8 tokenNames);
static void			recover			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input);
static void			beginResync		    (pANTLR3_PARSER parser);
static void			endResync		    (pANTLR3_PARSER parser);
static pANTLR3_BITSET		computeErrorRecoverySet	    (pANTLR3_PARSER parser);
static pANTLR3_BITSET		computeCSRuleFollow	    (pANTLR3_PARSER parser);
static pANTLR3_BITSET		combineFollows		    (pANTLR3_PARSER parser, ANTLR3_BOOLEAN exact);
static void			recoverFromMismatchedToken  (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow);
static void		recoverFromMismatchedSet	    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_BITSET follow);
static ANTLR3_BOOLEAN	recoverFromMismatchedElement	    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_BITSET follow);
static void		consumeUntil			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 tokenType);
static void		consumeUntilSet			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_BITSET set);
static pANTLR3_STACK	getRuleInvocationStack		    (pANTLR3_PARSER parser);
static pANTLR3_STACK	getRuleInvocationStackNamed	    (pANTLR3_PARSER parser, pANTLR3_UINT8 name);
static pANTLR3_HASH_TABLE	
			toStrings			    (pANTLR3_PARSER parser, pANTLR3_HASH_TABLE);
static ANTLR3_UINT64	getRuleMemoization		    (pANTLR3_PARSER parser, ANTLR3_UINT32 ruleIndex, ANTLR3_UINT64 ruleParseStart);
static ANTLR3_BOOLEAN	alreadyParsedRule		    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ruleIndex);
static void		memoize				    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ruleIndex, ANTLR3_UINT64 ruleParseStart);
static ANTLR3_BOOLEAN	synpred				    (pANTLR3_PARSER parser, void * ctx, pANTLR3_INT_STREAM input, void (*predicate)(void * ctx));
static void		freeParser			    (pANTLR3_PARSER parser);



ANTLR3_API pANTLR3_PARSER
antlr3ParserNew		(ANTLR3_UINT32 sizeHint)
{
    pANTLR3_PARSER	parser;

    /* Allocate memory
     */
    parser	= (pANTLR3_PARSER) ANTLR3_MALLOC(sizeof(ANTLR3_PARSER));

    if	(parser == NULL)
    {
	return	(pANTLR3_PARSER) ANTLR3_ERR_NOMEM;
    }
    parser->me	= parser;

    /* Install a base parser
     */
    parser->rec =  antlr3BaseRecognizerNew(ANTLR3_TYPE_PARSER, sizeHint);

    if	(parser->rec == (pANTLR3_BASE_RECOGNIZER) ANTLR3_ERR_NOMEM)
    {
	parser->free(parser);
	return	(pANTLR3_PARSER) ANTLR3_ERR_NOMEM;
    }

    parser->rec->me	= parser;

    /* Install the API
     */
    parser->setTokenStream		= setTokenStream;
    parser->getTokenStream		= getTokenStream;
    parser->free			= freeParser;

    /* Install the base recognizer API
     */
    parser->reset				= reset;
    parser->rec->reportError			= reportError;
    parser->rec->displayRecognitionError	= displayRecognitionError;
    parser->rec->recover			= recover;
    parser->rec->beginResync			= beginResync;
    parser->rec->endResync			= endResync;
    parser->rec->computeErrorRecoverySet	= computeErrorRecoverySet;
    parser->rec->computeCSRuleFollow		= computeCSRuleFollow;
    parser->rec->combineFollows			= combineFollows;
    parser->rec->recoverFromMismatchedToken	= recoverFromMismatchedToken;
    parser->rec->recoverFromMismatchedSet	= recoverFromMismatchedSet;
    parser->rec->recoverFromMismatchedElement	= recoverFromMismatchedElement;
    parser->rec->consumeUntil			= consumeUntil;
    parser->rec->consumeUntilSet		= consumeUntilSet;
    parser->rec->getRuleInvocationStack		= getRuleInvocationStack;
    parser->rec->getRuleInvocationStackNamed	= getRuleInvocationStackNamed;
    parser->rec->toStrings			= toStrings;
    parser->rec->getRuleMemoization		= getRuleMemoization;
    parser->rec->alreadyParsedRule		= alreadyParsedRule;
    parser->rec->memoize			= memoize;
    parser->rec->synpred			= synpred;

    return parser;
}

ANTLR3_API pANTLR3_PARSER
antlr3ParserNewStream	(ANTLR3_UINT32 sizeHint, pANTLR3_TOKEN_STREAM tstream)
{
    pANTLR3_PARSER	parser;

    parser  = antlr3ParserNew(sizeHint);

    if	(parser == (pANTLR3_PARSER) ANTLR3_ERR_NOMEM)
    {
	return	(pANTLR3_PARSER) ANTLR3_ERR_NOMEM;
    }

    /* Everything seems to be hunky dory so we can install the 
     * token stream.
     */
    parser->setTokenStream(parser->me, tstream);

    return parser;
}

static void		
freeParser			    (pANTLR3_PARSER parser)
{
    parser->rec->free(parser->rec->me);
    parser->rec	= NULL;
    ANTLR3_FREE(parser);
}

static void			
setTokenStream		    (pANTLR3_PARSER parser, pANTLR3_TOKEN_STREAM tstream)
{
    parser->tstream = tstream;
    parser->reset(parser->rec->me);
}

static pANTLR3_TOKEN_STREAM	
getTokenStream		    (pANTLR3_PARSER parser)
{
    return  parser->tstream;
}

static void			
reset			    (pANTLR3_PARSER parser)
{
    if	(parser->rec->following != NULL)
    {
	parser->rec->following->free(parser->rec->following);
    }

    /* Install a new following set
     */
    parser->rec->following   = antlr3StackNew(64);
}

static void			
reportError		    (pANTLR3_PARSER parser)
{
    if	(parser->rec->errorRecovery == ANTLR3_TRUE)
    {
	/* In error recovery so don't display another error while doing so
	 */
	return;
    }

    /* Signal we are in error recovery now
     */
    parser->rec->errorRecovery = ANTLR3_TRUE;

    parser->rec->displayRecognitionError(parser->rec->me, parser->rec->tokenNames);
}

#ifdef	WIN32
#pragma warning( disable : 4100 )
#endif

static void			
displayRecognitionError	    (pANTLR3_PARSER parser, pANTLR3_UINT8 tokenNames)
{
    fprintf(stderr, "%s(", parser->tstream->istream->exception->streamName);
#ifdef WIN32
    /* shanzzle fraazzle Dick Dastardly */
    fprintf(stderr, "%I64d) ", parser->tstream->istream->exception->line);
#else
    fprintf(stderr, "%lld) ", parser->tstream->istream->exception->type);
#endif

    fprintf(stderr, ": error %d : %s at offset %d, near %s\n", 
					    parser->tstream->istream->exception->type,
		    (pANTLR3_UINT8)	   (parser->tstream->istream->exception->message),
					    parser->tstream->istream->exception->charPositionInLine,
		    ((pANTLR3_COMMON_TOKEN)(parser->tstream->istream->exception->token))->toString
		    );

    /* To DO: Handle the various exceptions we can get here
     */
}

/** Recover from an error found on the input stream.  Mostly this is
 *  NoViableAlt exceptions, but could be a mismatched token that
 *  the match() routine could not recover from.
 */
static void			
recover			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input)
{
    /* Used to compute the follow set of tokens
    */
    pANTLR3_BITSET	followSet;

    /* I know that all the indirction looks confusing, but you get used to it and it really isn't.
     * Don't be tempted to use macros like we do for the generated C code, you will never know
     * what is going on. The generated C code does this to hide implementation details.
     */
    if	(parser->rec->lastErrorIndex == parser->tstream->istream->index(parser->tstream->istream->me))
    {
	/* The last error was at the same token index point. This must be a case
	 * where LT(1) is in the recovery token set so nothing is
	 * consumed. Consume a single token so at least to prevent
	 * an infinite loop; this is a failsafe.
	 */
	parser->tstream->istream->consume(parser->tstream->istream->me);
    }

    /* Record error index position
     */
    parser->rec->lastErrorIndex	 = parser->tstream->istream->index(parser->tstream->istream->me);
    
    /* Work out the follows set for error recovery
     */
    followSet	= parser->rec->computeErrorRecoverySet(parser->rec->me);

    /* Call resync hook (for debuggeres and so on)
     */
    parser->rec->beginResync(parser->rec->me);

    /* Consume tokens until we have resynced to something in the follows set
     */
    parser->rec->consumeUntilSet(parser->rec->me, parser->tstream->istream, followSet);

    /* End resync hook 
     */
    parser->rec->endResync(parser->rec->me);

    /* Destoy the temporary bitset we produced.
     */
    followSet->free(followSet);

    /* Reset the in error bit so we don't re-report the exception
     */
    parser->tstream->istream->error	= ANTLR3_FALSE;
}

static void			
beginResync		    (pANTLR3_PARSER parser)
{
}

static void			
endResync		    (pANTLR3_PARSER parser)
{
}
/**
 * Documentation below is from teh Java implementation.
 *
 * Compute the error recovery set for the current rule.  During
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
static pANTLR3_BITSET		
computeErrorRecoverySet	    (pANTLR3_PARSER parser)
{
    return   parser->rec->combineFollows(parser->me, ANTLR3_FALSE);
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
static pANTLR3_BITSET		
computeCSRuleFollow	    (pANTLR3_PARSER parser)
{
    return   parser->rec->combineFollows(parser->me, ANTLR3_FALSE);
}

static pANTLR3_BITSET		
combineFollows		    (pANTLR3_PARSER parser, ANTLR3_BOOLEAN exact)
{
    pANTLR3_BITSET	followSet;
    pANTLR3_BITSET	localFollowSet;
    ANTLR3_UINT64	top;
    ANTLR3_UINT64	i;

    top	= parser->rec->following->size(parser->rec->following);

    followSet	    = antlr3BitsetNew(0);

    for (i = top; i>0; i--)
    {
	localFollowSet = (pANTLR3_BITSET) parser->rec->following->get(parser->rec->following, i);

	if  (localFollowSet != NULL)
	{
	    followSet->orInPlace(followSet, localFollowSet);
	}

	if	(      exact == ANTLR3_TRUE
		    && localFollowSet->isMember(localFollowSet, ANTLR3_EOR_TOKEN_TYPE) == ANTLR3_FALSE
		)
	{
	    break;
	}
    }

    followSet->remove(followSet, ANTLR3_EOR_TOKEN_TYPE);

    return  followSet;
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
 *
 * May need ot come back and look at the exception stuff here, I am assuming 
 * that the exception that was passed in in the java implementation is
 * sotred in the recognizer exception stack. To 'throw' it we set the
 * error flag and rules can cascade back when this is set.
 */
static void			
recoverFromMismatchedToken  (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow)
{
    /* If the next token after the one we are looking at in the input stream
     * is what we are looking for then we remove the one we have discovered
     * from the stream by consuming it, then consume this next one along too as
     * if nothing had happened.
     */
    if	( input->LA(input->me, 2) == ttype)
    {
	/* Print out the error
	 */
	parser->rec->reportError(parser->rec->me);

	/* Call resync hook (for debuggeres and so on)
	 */
	parser->rec->beginResync(parser->rec->me);

	/* "delete" the extra token
	 */
	input->consume(input->me);

	/* End resync hook 
	 */
	parser->rec->endResync(parser->rec->me);

	/* consume the token that the rule actually expected to get
	 */
	input->consume(input->me);

	parser->tstream->istream->error  = ANTLR3_FALSE;	/* Exception is not outstanding any more */

    }

    /* The next token (after the one that is current, is not the one
     * that we were expecting, so the input is in more of an error state
     * than we hoped. 
     * If we are able to recover from the error using the follow set, then
     * we are hunky dory again and can move on, if we cannot, then we resort
     * to throwing the exception.
     */
    if	(parser->rec->recoverFromMismatchedElement(parser->rec->me, input, follow) == ANTLR3_FALSE)
    {
	parser->tstream->istream->error  = ANTLR3_TRUE;
	parser->rec->failed		 = ANTLR3_TRUE;
	return;
    }

}

static void		
recoverFromMismatchedSet	    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_BITSET follow)
{
    /* TODO - Single token deletion like in recoverFromMismatchedToken()
     */
    if	(parser->rec->recoverFromMismatchedElement(parser->rec->me, input, follow) == ANTLR3_FALSE)
    {
	parser->tstream->istream->error  = ANTLR3_TRUE;
	parser->rec->failed		 = ANTLR3_TRUE;
	return;
    }
}

/** This code is factored out from mismatched token and mismatched set
 *  recovery.  It handles "single token insertion" error recovery for
 *  both.  No tokens are consumed to recover from insertions.  Return
 *  true if recovery was possible else return false.
 */
static ANTLR3_BOOLEAN	
recoverFromMismatchedElement	    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_BITSET follow)
{
    pANTLR3_BITSET  viableToksFollowingRule;

    pANTLR3_BITSET  newFollow;

    newFollow	= NULL;

    if	(follow == NULL)
    {
	/* The follow set is NULL, which means we don't know what can come 
	 * next, so we "hit and hope" by just signifying that we cannot
	 * recover, which will just cause the next token to be consumed,
	 * which might dig us out.
	 */
	return	ANTLR3_FALSE;
    }

    /* We have a bitmap for the follow set, hence we can compute 
     * what can follow this grammar element reference.
     */
    if	(follow->isMember(follow, ANTLR3_EOR_TOKEN_TYPE) == ANTLR3_TRUE)
    {
	/* First we need to know which of the available tokens are viable
	 * to follow this reference.
	 */
	viableToksFollowingRule	= parser->rec->computeCSRuleFollow(parser->rec->me);

	/* Knowing that, we can or in the follow set
	 */
	newFollow   = follow->or(follow, viableToksFollowingRule);

	/* Remove the EOR token, which we do not wish to compute with
	 */
	newFollow->remove(follow, ANTLR3_EOR_TOKEN_TYPE);

	/* We now have the computed set of what can follow the current token
	 */
	follow	= newFollow;
    }

    /* We can now see if the current token works with the set of tokens
     * that could follow the current grammar reference. If it looks like it
     * is consistent, then we can "insert" that token by not throwing
     * an exception and assumimng that we saw it. 
     */
    if	( follow->isMember(follow, input->LA(input->me, 1)) == ANTLR3_TRUE)
    {
	/* report the error, but don't cause any rules to abort and stuff
	 */
	parser->rec->reportError(parser->rec->me);
	parser->tstream->istream->error  = ANTLR3_FALSE;
	parser->rec->failed		 = ANTLR3_FALSE;
	return ANTLR3_TRUE;	/* Success in recovery	*/
    }

    /* We could not find anything viable to do, so this is going to 
     * cause an exception.
     */
    return  ANTLR3_FALSE;
}

/** Eat tokens from the input stream until we get one of JUST the right type
 */
static void		
consumeUntil			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 tokenType)
{
    ANTLR3_UINT32   ttype;

    /* What do have at the moment?
     */
    ttype	= input->LA(input->me, 1);

    /* Start eating tokens until we get to the one we want.
     */
    while   (ttype != ANTLR3_TOKEN_EOF && ttype != tokenType)
    {
	input->consume(input->me);
	ttype	= input->LA(input->me, 1);
    }
}

/** Eat tokens from the input stream until we find one that
 *  belongs to the supplied set.
 */
static void		
consumeUntilSet			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_BITSET set)
{
    ANTLR3_UINT32   ttype;

    /* What do have at the moment?
     */
    ttype	= input->LA(input->me, 1);

    /* Start eating tokens until we get to one we want.
     */
    while   (ttype != ANTLR3_TOKEN_EOF && set->isMember(set, ttype) == ANTLR3_FALSE)
    {
	input->consume(input->me);
	ttype	= input->LA(input->me, 1);
    }
}

/** Return the rule invokation stack (how we got here in the parse.
 *  In the java version Ter just asks the JVM for all the information
 *  but it C we don't get this information, so I am going to do nothing 
 *  right now, but when the genrated code is there I will look to see how much 
 *  overhead is involved in pushing and popping this informatino on rule entry
 *  and exit. It is only good for error reporting and error recovery, though
 *  I don;t see that we are using it in errory recovery yet anyway as the context
 *  sensitive recvoery just calls the normal recoery funtions.
 *  TODO: Consult with Ter on this one as to usefulness, it is easy but do I need it?
 */
static pANTLR3_STACK	
getRuleInvocationStack		    (pANTLR3_PARSER parser)
{
    return NULL;
}

static pANTLR3_STACK	
getRuleInvocationStackNamed	    (pANTLR3_PARSER parser, pANTLR3_UINT8 name)
{
    return NULL;
}

/** Convenience method for template rewrites - NYI.
 */
static pANTLR3_HASH_TABLE	
toStrings			    (pANTLR3_PARSER parser, pANTLR3_HASH_TABLE tokens)
{
    return NULL;
}

static	void
freeList    (void * list)
{
    ((pANTLR3_LIST)list)->free(list);
}
/** Pointer to a function to return whether the rule has parsed input starting at the supplied 
 *  start index before. If the rule has not parsed input starting from the supplied start index,
 *  then it will return ANTLR3_MEMO_RULE_UNKNOWN. If it has parsed from the suppled start point
 *  then it will return the point where it last stopped parsing after that start point.
 *
 * \remark
 * The rule memos are an ANTLR3_LIST of ANTLR3_LISTS, however if this becomes any kind of performance
 * issue (it probably won't teh has tables are pretty quick) then we could make a special int only
 * version of the table.
 */
static ANTLR3_UINT64	
getRuleMemoization		    (pANTLR3_PARSER parser, ANTLR3_UINT32 ruleIndex, ANTLR3_UINT64 ruleParseStart)
{
    /* The rule memos are an ANTLR3_LIST of ANTLR3_LIST.
     */
    pANTLR3_LIST    ruleList;

    ANTLR3_UINT64   stopIndex;

    /* See if we have a list in the ruleMemos for this rule, and if not, then create one
     * as we will need it eventually.
     */
    ruleList	= parser->rec->ruleMemo->get(parser->rec->ruleMemo, (ANTLR3_UINT64)ruleIndex);

    if	(ruleList == NULL)
    {
	/* Did not find it, so create a new one
	 */
	ruleList    = antlr3ListNew(31);
	parser->rec->ruleMemo->put(parser->rec->ruleMemo, (ANTLR3_UINT64)ruleIndex, (void *) ruleList, freeList);
    }

    /* See if there is a stop index associated with the supplied start index.
     * We index on the start position + 1, just in case there is ever a need to 
     * memoize the first token ever, at index 0.
     */
    stopIndex	= (ANTLR3_UINT64)ruleList->get(ruleList, ruleParseStart+1);

    if	(stopIndex == 0)
    {
	return MEMO_RULE_UNKNOWN;
    }

    return  stopIndex;
}
/** Has this rule already parsed input at the current index in the
 *  input stream?  Return the stop token index or MEMO_RULE_UNKNOWN.
 *  If we attempted but failed to parse properly before, return
 *  MEMO_RULE_FAILED.
 *
 *  This method has a side-effect: if we have seen this input for
 *  this rule and successfully parsed before, then seek ahead to
 *  1 past the stop token matched for this rule last time.
 */
static ANTLR3_BOOLEAN	
alreadyParsedRule		    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ruleIndex)
{
    ANTLR3_UINT64	stopIndex;

    /* See if we have a memo marker for this.
     */
    stopIndex	    = parser->rec->getRuleMemoization(parser->rec->me, ruleIndex, input->index(input->me));

    if	(stopIndex  == MEMO_RULE_UNKNOWN)
    {
	return ANTLR3_FALSE;
    }

    if	(stopIndex == MEMO_RULE_FAILED)
    {
	parser->rec->failed = ANTLR3_TRUE;
    }
    else
    {
	input->seek(input->me, stopIndex+1);
    }

    /* If here then the rule was executed for this input already
     */
    return  ANTLR3_TRUE;
}

/** Record whether or not this rule parsed the input at this position
 *  successfully.
 */
static void		
memoize				    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ruleIndex, ANTLR3_UINT64 ruleParseStart)
{
    /* The rule memos are an ANTLR3_LIST of ANTLR3_LIST.
     */
    pANTLR3_LIST    ruleList;
    ANTLR3_UINT64   stopIndex;
    
    stopIndex	= parser->rec->failed == ANTLR3_TRUE ? MEMO_RULE_FAILED : input->index(input->me) - 1;

    ruleList	= parser->rec->ruleMemo->get(parser->rec->ruleMemo, (ANTLR3_UINT64)ruleIndex);

    if	(ruleList != NULL)
    {
	/* Add one to key in case the start is 0 eveer
	 */
	ruleList->put(ruleList, ruleParseStart+1, (void *)(stopIndex), NULL);
    }
}
/** A syntactic predicate.  Returns true/false depending on whether
 *  the specified grammar fragment matches the current input stream.
 *  This resets the failed instance var afterwards.
 */
static ANTLR3_BOOLEAN	
synpred				    (pANTLR3_PARSER parser, void * ctx, pANTLR3_INT_STREAM input, void (*predicate)(void * ctx))
{
    ANTLR3_UINT64   start;

    /* Begin backtracking so we can get back to where we started after trying out
     * the syntactic predicate.
     */
    start   = input->mark(input->me);
    parser->rec->backtracking++;

    /* Try the syntactical predicate
     */
    predicate(ctx);

    /* Reset
     */
    input->rewind(input->me, start);
    parser->rec->backtracking--;

    if	(parser->rec->failed == ANTLR3_TRUE)
    {
	/* Predicate failed
	 */
	parser->rec->failed = ANTLR3_FALSE;
	return	ANTLR3_FALSE;
    }
    else
    {
	/* Predicate was succesful
	 */
	parser->rec->failed	= ANTLR3_FALSE;
	return	ANTLR3_TRUE;
    }
}

#ifdef	WIN32
#pragma warning( default : 4100 )
#endif

