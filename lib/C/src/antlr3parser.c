/** \file
 * Implementation of the base functionality for an ANTLR3 parser.
 */
#include    <antlr3parser.h>

/* Parser API 
 */
static void			setTokenStream		    (pANTLR3_PARSER parser, pANTLR3_TOKEN_STREAM);
static pANTLR3_TOKEN_STREAM	getTokenStream		    (pANTLR3_PARSER parser);
static void			reset			    (pANTLR3_PARSER parser);
static void			freeParser		    (pANTLR3_PARSER parser);



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

    /* Install a base parser
     */
    parser->rec =  antlr3BaseRecognizerNew(ANTLR3_TYPE_PARSER, sizeHint);

    if	(parser->rec == (pANTLR3_BASE_RECOGNIZER) ANTLR3_ERR_NOMEM)
    {
	parser->free(parser);
	return	(pANTLR3_PARSER) ANTLR3_ERR_NOMEM;
    }

    parser->rec->super	= parser;

    /* Parser overrides
     */
    parser->rec->exConstruct	= ANTLR3_API_FUNC antlr3MTExceptionNew;

    /* Install the API
     */
    parser->setTokenStream		= ANTLR3_API_FUNC setTokenStream;
    parser->getTokenStream		= ANTLR3_API_FUNC getTokenStream;
    parser->free			= ANTLR3_API_FUNC freeParser;
    parser->reset			= ANTLR3_API_FUNC reset;

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
    parser->setTokenStream(parser, tstream);

    return parser;
}

static void		
freeParser			    (pANTLR3_PARSER parser)
{
    if	(parser->rec != NULL)
    {
	    if	(parser->rec->following != NULL)
	    {
		parser->rec->following->free(parser->rec->following);
		parser->rec->following = NULL;
	    }
	    parser->rec->free(parser->rec);
	    parser->rec	= NULL;

    }
    ANTLR3_FREE(parser);
}

static void			
setTokenStream		    (pANTLR3_PARSER parser, pANTLR3_TOKEN_STREAM tstream)
{
    parser->tstream = tstream;
    parser->reset(parser);
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












