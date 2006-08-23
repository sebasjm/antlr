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
    recognizer->match		= ANTLR3_API_FUNC antlr3BRMatch;
    recognizer->mismatch	= ANTLR3_API_FUNC antlr3BRMismatch;
    recognizer->matchAny	= ANTLR3_API_FUNC antlr3BRMatchAny;
    recognizer->free		= ANTLR3_API_FUNC antlr3BRFree;

    recognizer->following	= NULL;
    recognizer->_fsp		= -1;
    recognizer->ruleMemo	= NULL;
    recognizer->tokenNames	= NULL;

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

    pANTLR3_INPUT_STREAM	    is;
    pANTLR3_COMMON_TOKEN_STREAM	    cts;
    pANTLR3_TREE_NODE_STREAM	    tns;

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

	is	= (pANTLR3_INPUT_STREAM) input->super;

	ex->c			= is->istream->LA			(is->istream, 1);	    /* Current input character			*/
	ex->line		= is->getLine				(is);		    /* Line number comes from stream		*/
	ex->charPositionInLine	= is->getCharPositionInLine		(is);		    /* Line offset also comes from the stream   */
	ex->index		= is->istream->index			(is->istream);
	ex->streamName		= is->getSourceName			(is);
	ex->message		= "Unexpected character";
	break;

    case    ANTLR3_TOKENSTREAM:

	cts	= (pANTLR3_COMMON_TOKEN_STREAM) input->super;

	ex->token	= cts->tstream->LT				(cts->tstream, 1);	    /* Current input token			    */
	ex->line	= ((pANTLR3_COMMON_TOKEN)(ex->token))->getLine	(ex->token);
	ex->index	= cts->tstream->istream->index			(cts->tstream->istream);
	ex->streamName	= "Token stream: fix this Jim, pick p name from input stream into token stream!";
	ex->message	= "Unexpected token";
	break;

    case    ANTLR3_COMMONTREENODE:

	tns	= (pANTLR3_TREE_NODE_STREAM) input->super;

	ex->token	= tns->LT					(tns, 1);	    /* Current input token			    */
	ex->line	= ((pANTLR3_COMMON_TOKEN)(ex->token))->getLine	(ex->token);
	ex->index	= tns->istream->index				(tns->istream);
	ex->streamName	= "Token stream: fix this Jim, pick p name from input stream into token stream!";
	ex->message	= "Unexpected node";
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
    if	(input->LA(input, 1) == ttype)
    {
	/* The token was the one we were told to expect
	 */
	input->consume(input);				 /* Consume that token from the stream	    */
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
    recognizer->mismatch(recognizer, input, ttype, follow);

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
    input->consume(input);

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
    recognizer->recoverFromMismatchedToken(recognizer, input, ttype, follow);

    return;

}
