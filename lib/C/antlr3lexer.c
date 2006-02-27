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

    /* Install our this pointer (can be overridden by caller of course)
     */
    lexer->me	= lexer;

    /* Now we need to create the base recognizer
     */
    lexer->rec	    = antlr3BaseRecognizerNew(ANTLR3_TYPE_LEXER, sizeHint);

    if	(lexer->rec == (pANTLR3_BASE_RECOGNIZER) ANTLR3_ERR_NOMEM)
    {
	lexer->free(lexer);
	return	(pANTLR3_LEXER) ANTLR3_ERR_NOMEM;
    }
    lexer->rec->me  = lexer;

    /* Now install the token source interface
     */
    lexer->tokSource	= (pANTLR3_TOKEN_SOURCE)ANTLR3_MALLOC(sizeof(ANTLR3_TOKEN_SOURCE));

    if	(lexer->tokSource == (pANTLR3_TOKEN_SOURCE) ANTLR3_ERR_NOMEM) 
    {
	lexer->rec->free(lexer->rec);
	lexer->free(lexer);

	return	(pANTLR3_LEXER) ANTLR3_ERR_NOMEM;
    }
    lexer->tokSource->me    = lexer;

    /* Install the lexer API
     */
    lexer->setCharStream	    = setCharStream;
    lexer->mTokens		    = mTokens;
    lexer->setCharStream	    = setCharStream;
    lexer->emit			    = emit;
    lexer->emitNew		    = emitNew;
    lexer->matchs		    = matchs;
    lexer->matchc		    = matchc;
    lexer->matchRange		    = matchRange;
    lexer->matchAny		    = matchAny;
    lexer->recover		    = recover;
    lexer->getLine		    = getLine;
    lexer->getCharIndex		    = getCharIndex;
    lexer->getCharPositionInLine    = getCharPositionInLine;
    lexer->getText		    = getText;
    lexer->free			    = freeLexer;
    
    return  lexer;
}

ANTLR3_API pANTLR3_LEXER
antlr3LexerNewStream(ANTLR3_UINT32 sizeHint, pANTLR3_INPUT_STREAM input)
{
    pANTLR3_LEXER   lexer;

    /* Create a basic lexer first
     */
    lexer   = antlr3LexerNew(sizeHint);

    /* Install the input stream and reset the lexer
     */
    setCharStream(lexer, input);

    return  lexer;
}

static void mTokens	    (pANTLR3_LEXER lexer)
{
    if	(lexer)	    /* Fool compiler, avoid pragmas */
    {
	fprintf(stderr, "lexer->mTokens(): Error: No lexer rules were added to the lexer yet!\n");
    }
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

    /* Set the current token to nothing
     */
    lexer->token		= NULL;
    lexer->tokenStartCharIndex	= -1;
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

    lexer->emit(lexer->me, token);

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
	if  (lexer->input->istream->LA(lexer->input->istream->me, 1) != (*string))
	{
	    if	(lexer->rec->backtracking > 0)
	    {
		lexer->rec->failed = ANTLR3_TRUE;
		return ANTLR3_FALSE;
	    }
	    
	    lexer->input->istream->exConstruct(lexer->input->istream->me);

	    /* TODO: IMplement exception creation more fully
	     */
	    lexer->recover(lexer->rec->me);
	    return  ANTLR3_FALSE;
	}

	/* Matched correctly, do consume it
	 */
	lexer->input->istream->consume(lexer->input->istream->me);

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
    if	(lexer->input->istream->LA(lexer->input->istream->me, 1) == c)
    {
	/* Matched correctly, do consume it
	 */
	lexer->input->istream->consume(lexer->input->istream->me);

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

    lexer->input->istream->exConstruct(lexer->input->istream->me);

    /* TODO: Implement exception creation more fully
     */
    lexer->recover(lexer->me);

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
    c	= lexer->input->istream->LA(lexer->input->istream->me, 1);
    if	( c >= low && c <= high)
    {
	/* Matched correctly, consume it
	 */
	lexer->input->istream->consume(lexer->input->istream->me);

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

    lexer->input->istream->exConstruct(lexer->input->istream->me);

    /* TODO: Implement exception creation more fully
     */
    lexer->recover(lexer->me);

    return  ANTLR3_FALSE;
}

static void
matchAny	    (pANTLR3_LEXER lexer)
{
    lexer->input->istream->consume(lexer->input->istream->me);
}

static void
recover	    (pANTLR3_LEXER lexer)
{
    lexer->input->istream->consume(lexer->input->istream->me);
}

static ANTLR3_UINT64
getLine	    (pANTLR3_LEXER lexer)
{
    return  lexer->input->getLine(lexer->input->me);
}

static ANTLR3_UINT32
getCharPositionInLine	(pANTLR3_LEXER lexer)
{
    return  lexer->input->getCharPositionInLine(lexer->input->me);
}

static ANTLR3_UINT64	getCharIndex	    (pANTLR3_LEXER lexer)
{
    return lexer->input->istream->index(lexer->input->istream->me);
}

static pANTLR3_STRING
getText	    (pANTLR3_LEXER lexer)
{
    return  lexer->input->substr(
			    lexer->input->me, 
			    lexer->tokenStartCharIndex,
			    lexer->getCharIndex(lexer->me)-1);

}