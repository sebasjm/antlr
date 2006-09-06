/** \file
 *
 * Base implementation of an antlr 3 lexer.
 *
 * An ANTLR3 lexer implements a base recongizer, a token source and
 * a lexer interface. It constructs a base recognizer with default
 * functions, then overrides any of these that are parser specific (usual
 * default implementation of base recognizer.
 */
#include    <antlr3lexer.h>

static void		    mTokens	    (pANTLR3_LEXER lexer);
static void		    setCharStream   (pANTLR3_LEXER lexer,  pANTLR3_INPUT_STREAM input);
static void		    emit	    (pANTLR3_LEXER lexer,  pANTLR3_COMMON_TOKEN token);
static void		    emitNew	    (pANTLR3_LEXER lexer,  ANTLR3_UINT32 ttype,
						ANTLR3_UINT64 line,	    ANTLR3_UINT32 charPosition, 
						ANTLR3_UINT32 channel, 
						ANTLR3_UINT64 start,    ANTLR3_UINT64 stop
							);
static ANTLR3_BOOLEAN	    matchs	    (pANTLR3_LEXER lexer, ANTLR3_UCHAR * string);
static ANTLR3_BOOLEAN	    matchc	    (pANTLR3_LEXER lexer, ANTLR3_UCHAR c);
static ANTLR3_BOOLEAN	    matchRange	    (pANTLR3_LEXER lexer, ANTLR3_UCHAR low, ANTLR3_UCHAR high);
static void		    matchAny	    (pANTLR3_LEXER lexer);
static void		    recover	    (pANTLR3_LEXER lexer);
static ANTLR3_UINT64	    getLine	    (pANTLR3_LEXER lexer);
static ANTLR3_UINT64	    getCharIndex    (pANTLR3_LEXER lexer);
static ANTLR3_UINT32	    getCharPositionInLine
					    (pANTLR3_LEXER lexer);
static pANTLR3_STRING	    getText	    (pANTLR3_LEXER lexer);
static pANTLR3_COMMON_TOKEN nextToken	    (pANTLR3_TOKEN_SOURCE toksource);

static void		    displayRecognitionError	    (pANTLR3_BASE_RECOGNIZER rec, pANTLR3_UINT8 * tokenNames);
static void		    reportError			    (pANTLR3_BASE_RECOGNIZER rec);

static void		    freeLexer	    (pANTLR3_LEXER lexer);


ANTLR3_API pANTLR3_LEXER
antlr3LexerNew(ANTLR3_UINT32 sizeHint)
{
    pANTLR3_LEXER   lexer;

    /* Allocate memory
     */
    lexer   = (pANTLR3_LEXER) ANTLR3_MALLOC(sizeof(ANTLR3_LEXER));

    if	(lexer == NULL)
    {
	return	(pANTLR3_LEXER) ANTLR3_ERR_NOMEM;
    }

    /* Now we need to create the base recognizer
     */
    lexer->rec	    = ANTLR3_API_FUNC antlr3BaseRecognizerNew(ANTLR3_TYPE_LEXER, sizeHint);

    if	(lexer->rec == (pANTLR3_BASE_RECOGNIZER) ANTLR3_ERR_NOMEM)
    {
	lexer->free(lexer);
	return	(pANTLR3_LEXER) ANTLR3_ERR_NOMEM;
    }
    lexer->rec->super  = ANTLR3_API_FUNC lexer;

    lexer->rec->displayRecognitionError	    = ANTLR3_API_FUNC displayRecognitionError;
    lexer->rec->reportError		    = ANTLR3_API_FUNC reportError;

    /* Now install the token source interface
     */
    lexer->tokSource	= (pANTLR3_TOKEN_SOURCE)ANTLR3_MALLOC(sizeof(ANTLR3_TOKEN_SOURCE));

    if	(lexer->tokSource == (pANTLR3_TOKEN_SOURCE) ANTLR3_ERR_NOMEM) 
    {
	lexer->rec->free(lexer->rec);
	lexer->free(lexer);

	return	(pANTLR3_LEXER) ANTLR3_ERR_NOMEM;
    }
    lexer->tokSource->super    = ANTLR3_API_FUNC lexer;

    /* Install the default nextToken() method, which may be overridden
     * by generated code, or by anything else in fact.
     */
    lexer->tokSource->nextToken	    = ANTLR3_API_FUNC nextToken;
    lexer->tokSource->strFactory    = NULL;

    lexer->tokFactory		    = NULL;

    /* Install the lexer API
     */
    lexer->setCharStream	    = ANTLR3_API_FUNC setCharStream;
    lexer->mTokens		    = ANTLR3_API_FUNC mTokens;
    lexer->setCharStream	    = ANTLR3_API_FUNC setCharStream;
    lexer->emit			    = ANTLR3_API_FUNC emit;
    lexer->emitNew		    = ANTLR3_API_FUNC emitNew;
    lexer->matchs		    = ANTLR3_API_FUNC matchs;
    lexer->matchc		    = ANTLR3_API_FUNC matchc;
    lexer->matchRange		    = ANTLR3_API_FUNC matchRange;
    lexer->matchAny		    = ANTLR3_API_FUNC matchAny;
    lexer->recover		    = ANTLR3_API_FUNC recover;
    lexer->getLine		    = ANTLR3_API_FUNC getLine;
    lexer->getCharIndex		    = ANTLR3_API_FUNC getCharIndex;
    lexer->getCharPositionInLine    = ANTLR3_API_FUNC getCharPositionInLine;
    lexer->getText		    = ANTLR3_API_FUNC getText;
    lexer->free			    = ANTLR3_API_FUNC freeLexer;
    
    return  lexer;
}


/**
 * \brief
 * Default implementation of the nextToken() call for a lexer.
 * 
 * \param toksouirce
 * Points to the implementation of a token source. The lexer is 
 * addressed by the super structure pointer.
 * 
 * \returns
 * Write description of return value here.
 * 
 * \throws <exception class>
 * Description of criteria for throwing this exception.
 * 
 * Write detailed description for nextToken here.
 * 
 * \remarks
 * Write remarks for nextToken here.
 * 
 * \see
 */
static pANTLR3_COMMON_TOKEN nextToken	    (pANTLR3_TOKEN_SOURCE toksource)
{
    pANTLR3_LEXER   lexer;

    lexer   = (pANTLR3_LEXER)(toksource->super);

    /* Get rid of any previous token (token factory takes care of
     * any deallocation when this token is finally used up.
     */
    lexer->token		    = NULL;
    lexer->rec->error		    = ANTLR3_FALSE;	    /* Start out without an exception	*/
    lexer->rec->failed		    = ANTLR3_FALSE;

    /* Record the start of the token in our input stream.
     */
    lexer->tokenStartCharIndex	= lexer->getCharIndex(lexer);   

    /* Now call the matching rules and see if we can generate a new token
     */
    for	(;;)
    {
	if  (lexer->input->istream->LA(lexer->input->istream, 1) == ANTLR3_CHARSTREAM_EOF)
	{
	    /* Reached the end of the stream, nothing more to do.
	     */
	    pANTLR3_COMMON_TOKEN    teof = lexer->input->istream->eofToken;

	    teof->setStartIndex (teof, lexer->getCharIndex(lexer));
	    teof->setStopIndex  (teof, lexer->getCharIndex(lexer));
	    teof->setLine	(teof, lexer->getLine(lexer));
	    return  teof;
	}
	
	lexer->token			= NULL;
	lexer->rec->error		= ANTLR3_FALSE;	    /* Start out without an exception	*/
	lexer->rec->failed		= ANTLR3_FALSE;

	/* Call the generated lexer, see if it can get a new token together.
	 */
	lexer->mTokens(lexer->ctx);

	if  (lexer->rec->error  == ANTLR3_TRUE)
	{
	    /* Recongition exception, report it and try to recover.
	     */
	    lexer->rec->failed	    = ANTLR3_TRUE;
	    lexer->rec->reportError(lexer->rec);
	    lexer->recover(lexer);
	}
	else
	{
	    return  lexer->token;
	}
    }
}

ANTLR3_API pANTLR3_LEXER
antlr3LexerNewStream(ANTLR3_UINT32 sizeHint, pANTLR3_INPUT_STREAM input)
{
    pANTLR3_LEXER   lexer;

    /* Create a basic lexer first
     */
    lexer   = antlr3LexerNew(sizeHint);

    if	(lexer != (pANTLR3_LEXER)ANTLR3_ERR_NOMEM) 
    {
	/* Install the input stream and reset the lexer
	 */
	setCharStream(lexer, input);
    }

    return  lexer;
}

static void mTokens	    (pANTLR3_LEXER lexer)
{
    if	(lexer)	    /* Fool compiler, avoid pragmas */
    {
	fprintf(stderr, "lexer->mTokens(): Error: No lexer rules were added to the lexer yet!\n");
    }
}

static void			
reportError		    (pANTLR3_BASE_RECOGNIZER rec)
{
    rec->displayRecognitionError(rec, rec->tokenNames);
}

#ifdef	WIN32
#pragma warning( disable : 4100 )
#endif

static void			
displayRecognitionError	    (pANTLR3_BASE_RECOGNIZER rec, pANTLR3_UINT8 * tokenNames)
{
    char    buf[64];
    pANTLR3_LEXER   lexer;

    lexer   = (pANTLR3_LEXER)(rec->super);

    fprintf(stderr, "%s(", lexer->rec->exception->streamName);

#ifdef WIN32
    /* shanzzle fraazzle Dick Dastardly */
    fprintf(stderr, "%I64d) ", lexer->rec->exception->line);
#else
    fprintf(stderr, "%lld) ", lexer->rec->exception->line);
#endif

    fprintf(stderr, ": error %d : %s at offset %d, near ", 
					    lexer->rec->exception->type,
		    (pANTLR3_UINT8)	   (lexer->rec->exception->message),
					    lexer->rec->exception->charPositionInLine+1
		    );

    if	(isprint(lexer->rec->exception->c))
    {
	fprintf(stderr, "'%c'\n", lexer->rec->exception->c);
    }
    else
    {
	sprintf(buf, "char(%04x)", lexer->rec->exception->c);
	fprintf(stderr, "%s\n", buf);
    }
    

    /* To DO: Handle the various exceptions we can get here
     */
}

static void setCharStream   (pANTLR3_LEXER lexer,  pANTLR3_INPUT_STREAM input)
{
    /* Install the input interface
     */
    lexer->input	= input;

    /* We may need a token factory for the lexer; we don't destory any existing factory
     * until the lexer is destroyed, as people may still be using the tokens it produced.
     * Later I will provide a dup() method for a token so that it can extract itself
     * out of the factory. 
     */
    if	(lexer->tokFactory == NULL)
    {
	lexer->tokFactory	= antlr3TokenFactoryNew(input);
    }

    /* Need to create the EOF token
     */
    input->istream->eofToken	= lexer->tokFactory->newToken(lexer->tokFactory);
    input->istream->eofToken->setType(input->istream->eofToken, ANTLR3_TOKEN_EOF);

    /* This is a lexer, install the appropriate exception creator
     */
    lexer->rec->exConstruct = antlr3RecognitionExceptionNew;

    /* Set the current token to nothing
     */
    lexer->token		= NULL;
    lexer->tokenStartCharIndex	= -1;
    lexer->ruleNestingLevel	= 0;
}

static void emit	    (pANTLR3_LEXER lexer,  pANTLR3_COMMON_TOKEN token)
{
    lexer->token    = token;	/* Voila!   */
}

static void emitNew	    (pANTLR3_LEXER lexer,
					    ANTLR3_UINT32 ttype,
					    ANTLR3_UINT64 line,	    ANTLR3_UINT32 charPosition, 
					    ANTLR3_UINT32 channel, 
					    ANTLR3_UINT64 start,    ANTLR3_UINT64 stop
							)
{
    pANTLR3_COMMON_TOKEN	token;

    /* We could check pointers to token factories and so on, but
     * we are not in code tha twe want to run as fast as possible
     * so we are not checking any errors. I will come round again and 
     * creates some ANTLR3_DEBUG defs at a later date, but for now, be happy
     * that I made my fingers hurt almost as badly as Ter's to get this all
     * done in a week! So make sure you hae installed an input stream befire
     * trying to emit a new token.
     */
    token   = lexer->tokFactory->newToken(lexer->tokFactory);

    /* Install the supplied information, and some other bits we already know
     * get added automatically, such as the input stream it is assoicated with
     * (though it can all be overridden of course)
     */
    token->setType		(token, ttype);
    token->setChannel		(token, channel);
    token->setStartIndex        (token, start);
    token->setStopIndex		(token, stop);
    token->setLine		(token, line);
    token->setCharPositionInLine(token, charPosition);

    lexer->emit(lexer, token);

}

/**
 * Free the resources allocated by a lexer
 */
static void 
freeLexer    (pANTLR3_LEXER lexer)
{
    if	(lexer->tokFactory != NULL)
    {
	lexer->tokFactory->close(lexer->tokFactory);
	lexer->tokFactory = NULL;
    }
    if	(lexer->tokSource != NULL)
    {
	ANTLR3_FREE(lexer->tokSource);
	lexer->tokSource = NULL;
    }
    if	(lexer->rec != NULL)
    {
	lexer->rec->free(lexer->rec);
	lexer->rec = NULL;
    }
    ANTLR3_FREE(lexer);
}

/** Implementation of matchs for the lexer, overrides any
 *  base implementation in the base recognizer. 
 *
 *  \remark
 *  Note that the generated code lays down arrays of ints for constant
 *  strings so that they are int UTF32 form!
 */
static ANTLR3_BOOLEAN
matchs(pANTLR3_LEXER lexer, ANTLR3_UCHAR * string)
{
    while   (*string != ANTLR3_STRING_TERMINATOR)
    {
	if  (lexer->input->istream->LA(lexer->input->istream, 1) != (*string))
	{
	    if	(lexer->rec->backtracking > 0)
	    {
		lexer->rec->failed = ANTLR3_TRUE;
		return ANTLR3_FALSE;
	    }
	    
	    lexer->rec->exConstruct(lexer->rec);
	    lexer->rec->failed	 = ANTLR3_TRUE;

	    /* TODO: Implement exception creation more fully
	     */
	    lexer->recover(lexer);
	    return  ANTLR3_FALSE;
	}

	/* Matched correctly, do consume it
	 */
	lexer->input->istream->consume(lexer->input->istream);
	string++;

	/* Reset any failed indicator
	 */
	lexer->rec->failed = ANTLR3_FALSE;
    }
	    

    return  ANTLR3_TRUE;
}

/** Implementation of matchc for the lexer, overrides any
 *  base implementation in the base recognizer. 
 *
 *  \remark
 *  Note that the generated code lays down arrays of ints for constant
 *  strings so that they are int UTF32 form!
 */
static ANTLR3_BOOLEAN
matchc(pANTLR3_LEXER lexer, ANTLR3_UCHAR c)
{
    if	(lexer->input->istream->LA(lexer->input->istream, 1) == c)
    {
	/* Matched correctly, do consume it
	 */
	lexer->input->istream->consume(lexer->input->istream);

	/* Reset any failed indicator
	 */
	lexer->rec->failed = ANTLR3_FALSE;

	return	ANTLR3_TRUE;
    }
    
    /* Failed to match, exception and recovery time.
     */
    if	(lexer->rec->backtracking > 0)
    {
	lexer->rec->failed  = ANTLR3_TRUE;
	return	ANTLR3_FALSE;
    }

    lexer->rec->exConstruct(lexer->rec);

    /* TODO: Implement exception creation more fully
     */
    lexer->recover(lexer);

    return  ANTLR3_FALSE;
}

/** Implementation of matchc for the lexer, overrides any
 *  base implementation in the base recognizer. 
 *
 *  \remark
 *  Note that the generated code lays down arrays of ints for constant
 *  strings so that they are int UTF32 form!
 */
static ANTLR3_BOOLEAN
matchRange(pANTLR3_LEXER lexer, ANTLR3_UCHAR low, ANTLR3_UCHAR high)
{
    ANTLR3_UCHAR    c;

    /* What is in the stream at the moment?
     */
    c	= lexer->input->istream->LA(lexer->input->istream, 1);
    if	( c >= low && c <= high)
    {
	/* Matched correctly, consume it
	 */
	lexer->input->istream->consume(lexer->input->istream);

	/* Reset any failed indicator
	 */
	lexer->rec->failed = ANTLR3_FALSE;

	return	ANTLR3_TRUE;
    }
    
    /* Failed to match, execption and recovery time.
     */

    if	(lexer->rec->backtracking > 0)
    {
	lexer->rec->failed  = ANTLR3_TRUE;
	return	ANTLR3_FALSE;
    }

    lexer->rec->exConstruct(lexer->rec);

    /* TODO: Implement exception creation more fully
     */
    lexer->recover(lexer);

    return  ANTLR3_FALSE;
}

static void
matchAny	    (pANTLR3_LEXER lexer)
{
    lexer->input->istream->consume(lexer->input->istream);
}

static void
recover	    (pANTLR3_LEXER lexer)
{
    lexer->input->istream->consume(lexer->input->istream);
}

static ANTLR3_UINT64
getLine	    (pANTLR3_LEXER lexer)
{
    return  lexer->input->getLine(lexer->input);
}

static ANTLR3_UINT32
getCharPositionInLine	(pANTLR3_LEXER lexer)
{
    return  lexer->input->getCharPositionInLine(lexer->input);
}

static ANTLR3_UINT64	getCharIndex	    (pANTLR3_LEXER lexer)
{
    return lexer->input->istream->index(lexer->input->istream);
}

static pANTLR3_STRING
getText	    (pANTLR3_LEXER lexer)
{
    return  lexer->input->substr(
			    lexer->input, 
			    lexer->tokenStartCharIndex,
			    lexer->getCharIndex(lexer)-1);

}

