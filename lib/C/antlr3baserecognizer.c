/** \file
 * Contains the base functions that all recognizers start out with.
 * Any function can be overridden by a lexer/parser/tree parser or by the
 * ANTLR3 programmer.
 * 
 */
#include    <antlr3baserecognizer.h>

/* Interface functions
 */
static ANTLR3_BOOLEAN
		antlr3BRMatch	    (pANTLR3_BASE_RECOGNIZER recognizer, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow);
static void	antlr3BRMatchAny    (pANTLR3_BASE_RECOGNIZER recognizer, pANTLR3_INT_STREAM input);
static void	antlr3BRMismatch    (pANTLR3_BASE_RECOGNIZER recognizer, pANTLR3_INT_STREAM input, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow);

static void	antlr3BRFree	    (pANTLR3_BASE_RECOGNIZER recognizer);

ANTLR3_API pANTLR3_BASE_RECOGNIZER
antlr3BaseRecognizerNew(ANTLR3_UINT32 type, ANTLR3_UINT32 sizeHint)
{
    pANTLR3_BASE_RECOGNIZER recognizer;

    /* Allocate memory for the structure
     */
    recognizer	    = (pANTLR3_BASE_RECOGNIZER) ANTLR3_MALLOC((size_t)sizeof(ANTLR3_BASE_RECOGNIZER));

    if	(recognizer == NULL)
    {
	/* Allocation failed
	 */
	return	(pANTLR3_BASE_RECOGNIZER) ANTLR3_ERR_NOMEM;
    }
    recognizer->me	    = recognizer;
    recognizer->sizeHint    = sizeHint;

    /* Initialize variables
     */
    recognizer->type		= type;
    recognizer->errorRecovery	= ANTLR3_FALSE;
    recognizer->lastErrorIndex	= -1;
    recognizer->failed		= ANTLR3_FALSE;
    recognizer->backtracking	= 0;

    /* Install the API
     */
    recognizer->match		= antlr3BRMatch;
    recognizer->mismatch	= antlr3BRMismatch;
    recognizer->matchAny	= antlr3BRMatchAny;
    recognizer->free		= antlr3BRFree;

    if	(recognizer->following == NULL)
    {
	/* Could not allocate memory for reset
	 */
	ANTLR3_FREE(recognizer);
	return	(pANTLR3_BASE_RECOGNIZER) ANTLR3_ERR_NOMEM;
    }

    return  recognizer;
}
static void	
antlr3BRFree	    (pANTLR3_BASE_RECOGNIZER recognizer)
{
    if	(recognizer->ruleMemo != NULL)
    {
	recognizer->ruleMemo->free(recognizer->ruleMemo);
    }

    ANTLR3_FREE(recognizer);
}

/**
 * \brief
 * Creates a new Mismatched Token Exception and inserts in the recognizer
 * exception stack.
 * 
 * \param recognizer
 * Context pointer for this recognizer
 * 
 */
ANTLR3_API	void
antlr3MTExceptionNew(pANTLR3_INT_STREAM input)
{
    /* Create a basic recognition exception strucuture
     */
    antlr3RecognitionExceptionNew(input);

    /* Now update it to indicate this is a Mismatched token exception
     */
    input->exception->name		= ANTLR3_MISMATCHED_EX_NAME;
    input->exception->type		= ANTLR3_MISMATCHED_TOKEN_EXCEPTION;

    return;
}

ANTLR3_API	void
antlr3RecognitionExceptionNew(pANTLR3_INT_STREAM input)
{

    pANTLR3_INPUT_STREAM	    cs;
    pANTLR3_COMMON_TOKEN_STREAM	    ts;

    /* Create a basic exception strucuture
     */
    pANTLR3_EXCEPTION	ex = antlr3ExceptionNew(ANTLR3_RECOGNITION_EXCEPTION,
						(void *)ANTLR3_RECOGNITION_EX_NAME,
						NULL,
						ANTLR3_FALSE);

    /* Rest of information depends on the base type of the 
     * input stream.
     */
    switch  (input->type & ANTLR3_INPUT_MASK)
    {
    case    ANTLR3_CHARSTREAM:

	cs	= (pANTLR3_INPUT_STREAM) input->me;

	ex->c			= cs->istream->LA		    (cs->istream->me, 1);   /* Current input character			*/
	ex->line		= cs->getLine			    (cs->me);		    /* Line number comes from stream		*/
	ex->charPositionInLine	= cs->getCharPositionInLine	    (cs->me);		    /* Line offset also comes from the stream   */
	ex->index		= cs->istream->index		    (cs->istream->me);
	ex->streamName		= cs->getSourceName		    (cs->me);
	ex->message		= "Unexpected character";
	break;

    case    ANTLR3_TOKENSTREAM:

	ts	= (pANTLR3_COMMON_TOKEN_STREAM) input->me;

	ex->token   = ts->tstream->LT(ts->tstream->me, 1);			/* Current input token			    */
	ex->line    = ((pANTLR3_COMMON_TOKEN)(ex->token))->getLine(ex->token);
	ex->index   = ts->tstream->istream->index		  (ts->tstream->istream->me);
	ex->streamName	= "Token stream: fix this Jim, pick p name from input stream into token stream!";
	ex->message	= "Unexpected token";
	break;
    }

    ex->nextException	= NULL;
    ex->input		= input;

    input->exception	= ex;
    input->error	= ANTLR3_TRUE;	    /* Exception is outstanding	*/

    return;
}


/** Match current input symbol against ttype.  Upon error, do one token
 *  insertion or deletion if possible.  You can override to not recover
 *  here and bail out of the current production to the normal error
 *  exception catch (at the end of the method) by just throwing
 *  MismatchedTokenException upon input.LA(1)!=ttype.
 */
static ANTLR3_BOOLEAN
antlr3BRMatch(	pANTLR3_BASE_RECOGNIZER recognizer, pANTLR3_INT_STREAM	input,
		ANTLR3_UINT32 ttype, pANTLR3_BITSET follow)
{
    if	(input->LA(input->me, 1) == ttype)
    {
	/* The token was the one we were told to expect
	 */
	input->consume(input->me);	/* Consume that token from the stream	    */
	recognizer->errorRecovery   = ANTLR3_FALSE;	/* Not in error recovery now (if we were)   */
	recognizer->failed	    = ANTLR3_FALSE;	/* The match was a success		    */
	return ANTLR3_TRUE;				/* We are done				    */
    }

    /* We did not find the expectd token type, if we are backtracking then
     * we just set the failed flag and return.
     */
    if	(recognizer->backtracking > 0)
    {
	/* Backtracking is going on
	 */
	recognizer->failed  = ANTLR3_TRUE;
	return ANTLR3_FALSE;
    }

    /* We did not find the expected token and there is no backtracking
     * going on, so we mismatch, which creates an exception in the recognizer exception
     * stack.
     */
    recognizer->mismatch(recognizer->me, input, ttype, follow);

    return ANTLR3_FALSE;
}

/**
 * \brief
 * Consumes the next token whatever it is and resets the recognizer state
 * so that it is not in error.
 * 
 * \param recognizer
 * Recognizer context pointer
 */
static void
antlr3BRMatchAny(pANTLR3_BASE_RECOGNIZER recognizer, pANTLR3_INT_STREAM	input)
{
    recognizer->errorRecovery	    = ANTLR3_FALSE;
    recognizer->failed		    = ANTLR3_FALSE;
    input->consume(input->me);

    return;
}

static	void
antlr3BRMismatch(pANTLR3_BASE_RECOGNIZER recognizer, pANTLR3_INT_STREAM	input, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow)
{
    /* Install a mismtached token exception in the exception stack
     */
    antlr3MTExceptionNew(input);

    /* Enter error recovery mode
     */
    recognizer->recoverFromMismatchedToken(recognizer->me, input, ttype, follow);

    return;

}