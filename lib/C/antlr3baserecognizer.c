/** \file
 * Contains the base functions that all recognizers start out with.
 * Any function can be overridden by a lexer/parser/tree parser or by the
 * ANTLR3 programmer.
 * 
 */
#include    <antlr3.h>

/* Interface functions
 */
static void	antlr3BRReset	    (pANTLR3_BASE_RECOGNIZER recognizer);
static void	antlr3BRMatch	    (pANTLR3_BASE_RECOGNIZER recognizer, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow);
static void	antlr3BRMismatch    (pANTLR3_BASE_RECOGNIZER recognizer, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow);
static void	antlr3BRMatchAny    (pANTLR3_BASE_RECOGNIZER recognizer);

ANTLR3_API pANTLR3_BASE_RECOGNIZER
antlrBaseRecognizerNew(ANTLR3_UINT32 type)
{
    pANTLR3_BASE_RECOGNIZER recognizer;

    /* Allocate memory for the structure
     */
    recognizer	= (pANTLR3_BASE_RECOGNIZER) ANTLR3_MALLOC((size_t)sizeof(ANTLR3_BASE_RECOGNIZER));

    if	(recognizer == NULL)
    {
	/* Allocation failed
	 */
	return	(pANTLR3_BASE_RECOGNIZER) ANTLR3_ERR_NOMEM;
    }



    /* Initialize variables
     */
    recognizer->type		= type;
    recognizer->errorRecovery	= ANTLR3_FALSE;
    recognizer->lastErrorIndex	= -1;
    recognizer->failed		= ANTLR3_FALSE;
    recognizer->backtracking	= 0;

    /* Install the API
     */
    recognizer->reset	    = antlr3BRReset;
    recognizer->match	    = antlr3BRMatch;
    recognizer->mismatch    = antlr3BRMismatch;
    recognizer->matchAny    = antlr3BRMatchAny;

    if	(recognizer->following == NULL)
    {
	/* Could not allocate memory for reset
	 */
	ANTLR3_FREE(recognizer);
	return	(pANTLR3_BASE_RECOGNIZER) ANTLR3_ERR_NOMEM;
    }

    return  recognizer;
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
antlr3MTExceptionNew(pANTLR3_BASE_RECOGNIZER recognizer)
{
    /* Create a basic recognition exception strucuture
     */
    antlr3RecognitionExceptionNew(recognizer);

    /* Now update it to indicate this is a Mismatched token exception
     */
    recognizer->exception->name		= ANTLR3_MISMATCHED_EX_NAME;
    recognizer->exception->type		= ANTLR3_MISMATCHED_TOKEN_EXCEPTION;

    return;
}

ANTLR3_API	void
antlr3RecognitionExceptionNew(pANTLR3_BASE_RECOGNIZER recognizer)
{
    /* Create a basic exception strucuture
     */
    pANTLR3_EXCEPTION	ex = antlr3ExceptionNew(ANTLR3_RECOGNITION_EXCEPTION,
						ANTLR3_RECOGNITION_EX_NAME,
						NULL,
						ANTLR3_FALSE);

    /* Record the input and recognizer state variables that make sense
     */
    ex->input	= recognizer->input;
    ex->index	= recognizer->input->index(recognizer->input);

    /* Rest of information depends on the base type of the 
     * input stream.
     */
    switch  (recognizer->input->type & ANTLR3_INPUT_MASK)
    {
    case    ANTLR3_CHARSTREAM:

	ex->c	    = recognizer->input->LA			    (recognizer->input, 1);	/* Current input character		    */
	ex->line    = recognizer->input->getLine		    (recognizer->input);	/* Line number comes from stream	    */
	ex->charPositionInLine
		    = recognizer->input->getCharPositionInLine	    (recognizer->input);	/* Line offset also comes from the stream   */
	break;

    case    ANTLR3_TOKENSTREAM:

	ex->token   = recognizer->input->LT			    (recognizer->input, 1);	/* Current input token			    */
	ex->line    = ((pANTLR3_COMMON_TOKEN)(ex->token))->getLine(ex->token);
    }

    return;
}
static void
antlr3BRReset(pANTLR3_BASE_RECOGNIZER recognizer)
{
    if	(recognizer->following != NULL)
    {
	recognizer->following->free(recognizer->following);
    }

    /* Install a new folowing set
     */
    recognizer->following   = antlr3StackNew(64);
}

/** Match current input symbol against ttype.  Upon error, do one token
 *  insertion or deletion if possible.  You can override to not recover
 *  here and bail out of the current production to the normal error
 *  exception catch (at the end of the method) by just throwing
 *  MismatchedTokenException upon input.LA(1)!=ttype.
 */
static void
antlr3BRMatch(pANTLR3_BASE_RECOGNIZER recognizer, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow)
{
    if	(recognizer->input->LA(recognizer->input, 1) == ttype)
    {
	/* The token was the one we were told to expect
	 */
	recognizer->input->consume(recognizer->input);	/* Consume that token from the stream	    */
	recognizer->errorRecovery   = ANTLR3_FALSE;	/* Not in error recovery now (if we were)   */
	recognizer->failed	    = ANTLR3_FALSE;	/* The match was a success		    */
	return;						/* We are done				    */
    }

    /* We did not find the expectd token type, if we are backtracking then
     * we just set the failed flag and return.
     */
    if	(recognizer->backtracking > 0)
    {
	/* Backtracking is going on
	 */
	recognizer->failed  = ANTLR3_TRUE;
	return;
    }

    /* We did not find the expected token and there is no backtracking
     * going on, so we mismatch, which creates an exception in the recognizer exception
     * stack.
     */
    recognizer->mismatch(recognizer, ttype, follow);

    return;
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
antlr3BRMatchAny(pANTLR3_BASE_RECOGNIZER recognizer)
{
    recognizer->errorRecovery	    = ANTLR3_FALSE;
    recognizer->failed		    = ANTLR3_FALSE;
    recognizer->input->consume(recognizer->input);

    return;
}

static	void
antlr3BRMismatch(pANTLR3_BASE_RECOGNIZER recognizer, ANTLR3_UINT32 ttype, pANTLR3_BITSET follow)
{
    /* Install a mismtached token exception in the exception stack
     */
    antlr3MTExceptionNew(recognizer);

    /* Enter error recovery mode
     */
    recognizer->recoverFromMismatchedToken(recognizer, ttype, follow);

    return;

}