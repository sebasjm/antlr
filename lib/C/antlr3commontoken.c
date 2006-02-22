/**
 * Contains the default implementation of the common token used within
 * java. Custom tokens should create this structure and then append to it using the 
 * custom pointer to install their own strcuture and API.
 */
#include    <antlr3.h>

static  pANTLR3_UINT8   getText			(pANTLR3_COMMON_TOKEN token);
static  void		setText			(pANTLR3_COMMON_TOKEN token);
static	ANTLR3_UINT32   getType			(pANTLR3_COMMON_TOKEN token);
static  void		setType			(pANTLR3_COMMON_TOKEN token);
static  ANTLR3_UINT64   getLine			(pANTLR3_COMMON_TOKEN token);
static  void		setLine			(pANTLR3_COMMON_TOKEN token, ANTLR3_UINT64 line);
static  ANTLR3_UINT32   getCharPositionInLine	(pANTLR3_COMMON_TOKEN token);
static  void		setCharPositionInLine	(pANTLR3_COMMON_TOKEN token, ANTLR3_UINT32 pos);
static  ANTLR3_UINT32   getChannel		(pANTLR3_COMMON_TOKEN token);
static  void		setChannel		(pANTLR3_COMMON_TOKEN token, ANTLR3_UINT32 channel);
static  ANTLR3_UINT64   getTokenIndex		(pANTLR3_COMMON_TOKEN token);
static  void		setTokenIndex		(pANTLR3_COMMON_TOKEN token, ANTLR3_UINT64);
static  ANTLR3_UINT64   getStartIndex		(pANTLR3_COMMON_TOKEN token);
static  void		setStartIndex		(pANTLR3_COMMON_TOKEN token, ANTLR3_UINT64 index);
static  ANTLR3_UINT64   getStopIndex		(pANTLR3_COMMON_TOKEN token);
static  void		setStopIndex		(pANTLR3_COMMON_TOKEN token, ANTLR3_UINT64 index);
static  pANTLR3_INT8    toString		(pANTLR3_COMMON_TOKEN token);

/* Internal management functions
 */
static	void			setAPI	    (pANTLR3_COMMON_TOKEN token);
static	pANTLR3_COMMON_TOKEN	newToken    (void);
static	void			newPool	    (pANTLR3_TOKEN_FACTORY factory);

ANTLR3_COMMON_TOKEN ANTLR3_EOF_TOKEN	= { ANTLR3_CHARSTREAM_EOF , ANTLR3_FALSE };


ANTLR3_API pANTLR3_COMMON_TOKEN
antlr3NewCommonTokenType(ANTLR3_UINT32 ttype)
{
    pANTLR3_COMMON_TOKEN    token;
    
    /* Create a raw token with the interface installed
     */
    token   = newToken();

    if	(token != (pANTLR3_COMMON_TOKEN)(ANTLR3_ERR_NOMEM))
    {
	token->setType(token, ttype);
    }

    /* All good
     */
    return  token;
}



ANTLR3_API pANTLR3_TOKEN_FACTORY
antlr3NewTokenFactory()
{
    pANTLR3_TOKEN_FACTORY   factory;

    /* allocate memory
     */
    factory	= (pANTLR3_TOKEN_FACTORY) ANTLR3_MALLOC((size_t)sizeof(ANTLR3_TOKEN_FACTORY));

    if	(factory == NULL)
    {
	return	(pANTLR3_TOKEN_FACTORY)(ANTLR3_ERR_NOMEM);
    }

    /* Allocate the initial pool
     */
    factory->thisPool	= -1;
    factory->pools	= NULL;
    newPool(factory);

    /* Factory space is good, we now want to initialize our cheating token
     * which one it is initialized is the model for all tokens we manufacture
     */
    setAPI(&factory->unTruc);

    /* Set some initial variables for future copying
     */
    factory->unTruc.setCharPositionInLine(&factory->unTruc, -1);
    factory->unTruc.factoryMade	= ANTLR3_TRUE;
    factory->unTruc.custom	= NULL;
    factory->unTruc.freeCustom	= NULL;
    factory->unTruc.type	= ANTLR3_TOKEN_INVALID;

    return  factory;

}

static void
newPool(pANTLR3_TOKEN_FACTORY factory)
{
    /* Increment factory count
     */
    factory->thisPool++;

    factory->pools  = (pANTLR3_COMMON_TOKEN)
			ANTLR3_REALLOC(	(void *)factory->pools,	    /* Current pools pointer (starts at NULL)	*/
					(ANTLR3_UINT64)((factory->thisPool +1) * sizeof(pANTLR3_COMMON_TOKEN))	/* Memory for new pool pointers */
					);

    /* Allocate a new pool for the factory
     */
    factory->pools[thisPool]	=
			    (pANTLR3_COMMON_TOKEN) 
				ANTLR3_MALLOC((size_t)(sizeof(ANTLR3_COMMON_TOKEN) * ANTLR3_FACTORY_POOL_SIZE));

    /* Reset the counters
     */
    factory->nextToken	= 0;
  
    /* Done
     */
    return;
}

static	pANTLR3_COMMON_TOKEN    
newPoolToken	    (pANTLR3_TOKEN_FACTORY factory)
{
    pANTLR3_COMMON_TOKEN    token;

    /* See if we need a new token pool before allocating a new
     * one
     */
    if	(factory->nextToken >= ANTLR3_FACTORY_POOL_SIZE)
    {
	/* We ran out of tokens in the current pool, so we need a new pool
	 */
	newPool(factory);
    }

    /* Assuming everything went well (we are trying for performance here so doing minimal
     * error checking - we might introduce a DEBUG flag set that turns on tracing and things
     * later, but I have typed this entire runtime in in 3 days so far :-(), <breath>, then
     * we can work out what the pointer is to the next token.
     */
    token   = factory->pools[factory->thisPool] + factory->nextToken;
    factory->nextToken++;

    /* We have our token pointer now, so we can initialize it to the predefined model.
     */
    ANTLR3_MEMMOVE((void *)token, (const void *)&factory->unTruc, (ANTLR3_UINT64)sizeof(ANTLR3_COMMON_TOKEN));

    /* And we are done
     */
    return  token;
}

static	void
factoryClose	    (pANTLR3_TOKEN_FACTORY factory)
{
}


static	pANTLR3_COMMON_TOKEN	
newToken(void)
{
    pANTLR3_COMMON_TOKEN    token;

    /* Allocate memory for this
     */
    token   = (pANTLR3_COMMON_TOKEN) ANTLR3_MALLOC((size_t)(sizeof(ANTLR3_COMMON_TOKEN)));

    if	(token == NULL)
    {
	return	(pANTLR3_COMMON_TOKEN)(ANTLR3_ERR_NOMEM);
    }

    /* Install the API
     */
    setAPI(token);

    return  token;
}

static void 
setAPI(pANTLR3_COMMON_TOKEN token)
{
    return;
}