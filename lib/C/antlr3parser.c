/** \file
 * Implementation of the base functionality for an ANTLR3 parser.
 */
#include    <antlr3parser.h>

static void			setTokenStream		    (pANTLR3_PARSER parser, pANTLR3_TOKEN_STREAM);
static pANTLR3_TOKEN_STREAM	getTokenStream		    (pANTLR3_PARSER parser);
static void			reset			    (pANTLR3_PARSER parser);
static void			reportError		    (pANTLR3_PARSER parser, pANTLR3_EXCEPTION ex);
static void			displayRecognitionError	    (pANTLR3_PARSER parser, pANTLR3_EXCEPTION ex, pANTLR3_UINT8 tokenNames);
static void			recover			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_EXCEPTION ex);
static void			beginResync		    (pANTLR3_PARSER parser);
static void			endResync		    (pANTLR3_PARSER parser);
static pANTLR3_BITSET		computeErrorRecoverySet	    (pANTLR3_PARSER parser);
static pANTLR3_BITSET		computeCSRuleFollow	    (pANTLR3_PARSER parser);
static pANTLR3_BITSET		combineFollows		    (pANTLR3_PARSER parser, ANTLR3_BOOLEAN exact);
static void			recoverFromMismatchedToken  (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow);
static void		recoverFromMismatchedSet	    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_EXCEPTION ex, pANTLR3_BITSET follow);
static ANTLR3_BOOLEAN	recoverFromMismatchedElement	    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_EXCEPTION ex, pANTLR3_BITSET follow);
static void		consumeUntil			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 tokenType);
static void		consumeUntilSet			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_BITSET set);
static pANTLR3_STACK	getRuleInvocationStack		    (pANTLR3_PARSER parser);
static pANTLR3_STACK	getRuleInvocationStackNamed	    (pANTLR3_PARSER parser, pANTLR3_EXCEPTION ex, pANTLR3_UINT8 name);
static pANTLR3_HASH_TABLE	
			toStrings			    (pANTLR3_PARSER parser, pANTLR3_HASH_TABLE);
static pANTLR3_UINT64	getRuleMemoization		    (pANTLR3_PARSER parser, ANTLR3_UINT32 ruleIndex, ANTLR3_UINT64 ruleParseStart);
static ANTLR3_BOOLEAN	alreadyParsedRule		    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ruleIndex);
static void		memoize				    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ruleIndex, ANTLR3_UINT64 ruleParseStart);
static ANTLR3_BOOLEAN	synpred				    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, void (*predicate)());
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
reportError		    (pANTLR3_PARSER parser, pANTLR3_EXCEPTION ex)
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

    parser->rec->displayRecognitionError(parser->rec->me, ex, parser->rec->tokenNames);
}

#ifdef	WIN32
#pragma warning( disable : 4100 )
#endif

static void			
displayRecognitionError	    (pANTLR3_PARSER parser, pANTLR3_EXCEPTION ex, pANTLR3_UINT8 tokenNames)
{
    fprintf(stderr, "%s(%ld) : error %d : %s at offset %d, near %s\n", 
		    ex->streamName, 
		    ex->line,
		    ex->type,
		    (pANTLR3_UINT8)(ex->message),
		    ex->charPositionInLine,
		    ((pANTLR3_COMMON_TOKEN)(ex->token))->toString
		    );

    /* To DO: Handle the various exceptions we can get here
     */
}

/** Recover from an error found on the input stream.  Mostly this is
 *  NoViableAlt exceptions, but could be a mismatched token that
 *  the match() routine could not recover from.
 */
static void			
recover			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_EXCEPTION ex)
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
}



static void			
beginResync		    (pANTLR3_PARSER parser)
{
}

static void			
endResync		    (pANTLR3_PARSER parser)
{
}

static pANTLR3_BITSET		
computeErrorRecoverySet	    (pANTLR3_PARSER parser)
{
    pANTLR3_BITSET errSet;


    return  errSet = NULL;
}

static pANTLR3_BITSET		
computeCSRuleFollow	    (pANTLR3_PARSER parser)
{
    pANTLR3_BITSET followSet;


    return  followSet = NULL;
}

static pANTLR3_BITSET		
combineFollows		    (pANTLR3_PARSER parser, ANTLR3_BOOLEAN exact)
{
        pANTLR3_BITSET followSet;


    return  followSet = NULL;
}

static void			
recoverFromMismatchedToken  (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow)
{
}

static void		
recoverFromMismatchedSet	    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_EXCEPTION ex, pANTLR3_BITSET follow)
{
}

static ANTLR3_BOOLEAN	
recoverFromMismatchedElement	    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_EXCEPTION ex, pANTLR3_BITSET follow)
{
    return  ANTLR3_TRUE;
}

static void		
consumeUntil			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 tokenType)
{
}

static void		
consumeUntilSet			    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, pANTLR3_BITSET set)
{
}

static pANTLR3_STACK	
getRuleInvocationStack		    (pANTLR3_PARSER parser)
{
    return NULL;
}

static pANTLR3_STACK	
getRuleInvocationStackNamed	    (pANTLR3_PARSER parser, pANTLR3_EXCEPTION ex, pANTLR3_UINT8 name)
{
    return NULL;
}

static pANTLR3_HASH_TABLE	
toStrings			    (pANTLR3_PARSER parser, pANTLR3_HASH_TABLE tokens)
{
    return NULL;
}

static pANTLR3_UINT64	
getRuleMemoization		    (pANTLR3_PARSER parser, ANTLR3_UINT32 ruleIndex, ANTLR3_UINT64 ruleParseStart)
{
    return 0;
}

static ANTLR3_BOOLEAN	
alreadyParsedRule		    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ruleIndex)
{
    return  ANTLR3_TRUE;
}

static void		
memoize				    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ruleIndex, ANTLR3_UINT64 ruleParseStart)
{
}

static ANTLR3_BOOLEAN	
synpred				    (pANTLR3_PARSER parser, pANTLR3_INT_STREAM input, void (*predicate)())
{
    return  ANTLR3_TRUE;
}

#ifdef	WIN32
#pragma warning( default : 4100 )
#endif

