/** \file
 * This is the standard tree daptor used by the C runtime unless the grammar
 * source file says to use anything different. It embeds a BASE_TREE to which
 * it adds its own implementaion of anything that the abase tree is not 
 * good enough for, plus a number of methods that any other adaptor type
 * needs to implement too.
 */

#include    <antlr3commontreeadaptor.h>

#ifdef	WIN32
#pragma warning( disable : 4100 )
#endif

/* BASE_TREE_ADAPTOR overrides... */
static	pANTLR3_BASE_TREE	dupNode		(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE treeNode);
static	pANTLR3_BASE_TREE	create		(pANTLR3_BASE_TREE_ADAPTOR adpator, pANTLR3_COMMON_TOKEN payload);
static	pANTLR3_COMMON_TOKEN	createToken	(pANTLR3_BASE_TREE_ADAPTOR adaptor, ANTLR3_UINT32 tokenType, pANTLR3_UINT8 text);
static	pANTLR3_COMMON_TOKEN	createTokenFromToken	(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_COMMON_TOKEN fromToken);
static	pANTLR3_UINT8		getText		(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE t);
static	ANTLR3_UINT32		getType		(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE t);

/* Methods specific to each tree adaptor
 */
static	void		setTokenBoundaries	(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE t, pANTLR3_COMMON_TOKEN startToken, pANTLR3_COMMON_TOKEN stopToken);
static	ANTLR3_UINT64   getTokenStartIndex	(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE t);
static  ANTLR3_UINT64   getTokenStopIndex	(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE t);

/** Create a new tree adaptor. Note that despite the fact that this is
 *  creating a new COMMON_TREE adaptor, we return the address of the
 *  BASE_TREE interface, as should any other adaptor that wishes to be 
 *  used as the tree element of a tree parse/build.
 */
ANTLR3_API pANTLR3_BASE_TREE_ADAPTOR
ANTLR3_TREE_ADAPTORNew()
{
    pANTLR3_COMMON_TREE_ADAPTOR	cta;

    /* First job is to create the memory we need for the tree adaptor interface.
     */
    cta	= (pANTLR3_COMMON_TREE_ADAPTOR) ANTLR3_MALLOC((size_t)(sizeof(ANTLR3_COMMON_TREE_ADAPTOR)));

    if	(cta == NULL)
    {
	return	(pANTLR3_BASE_TREE_ADAPTOR)(ANTLR3_ERR_NOMEM);
    }

    /* Memory is initialized, so initialize the base tree adaptor
     */
    antlr3BaseTreeAdaptorInit(&(cta->baseAdaptor));

    /* Install our interface overrides.
     */
    cta->baseAdaptor.dupNode		    = ANTLR3_API_FUNC dupNode;
    cta->baseAdaptor.create		    = ANTLR3_API_FUNC create;
    cta->baseAdaptor.createToken	    = ANTLR3_API_FUNC createToken;
    cta->baseAdaptor.createTokenFromToken   = ANTLR3_API_FUNC createTokenFromToken;
    cta->baseAdaptor.setTokenBoundaries	    = ANTLR3_API_FUNC setTokenBoundaries;
    cta->baseAdaptor.getTokenStartIndex	    = ANTLR3_API_FUNC getTokenStartIndex;
    cta->baseAdaptor.getTokenStopIndex	    = ANTLR3_API_FUNC getTokenStopIndex;
    cta->baseAdaptor.getText		    = ANTLR3_API_FUNC getText;
    cta->baseAdaptor.getType		    = ANTLR3_API_FUNC getType;

    /* Install the super class pointer
     */
    cta->baseAdaptor.super	    = cta;

    /* Install a tree factory for creating new tree nodes
     */
    cta->arboretum  = antlr3ArboretumNew();

    /* Allow the base tree adaptor to share the tree factory's string factory.
     */
    cta->baseAdaptor.strFactory	= cta->arboretum->unTruc.baseTree.strFactory;

    /* Return the address of the base adaptor interface.
     */
    return  &(cta->baseAdaptor);
}


/* BASE_TREE_ADAPTOR overrides */

/** Duplicate the supplied node.
 */
static	pANTLR3_BASE_TREE
dupNode		(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE treeNode)
{
    return  treeNode->dupNode(treeNode);
}

static	pANTLR3_BASE_TREE
create		(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_COMMON_TOKEN payload)
{
    pANTLR3_BASE_TREE	ct;
    
    /* Create a new common tree as this is what this adaptor dels with
     */
    ct = ((pANTLR3_COMMON_TREE_ADAPTOR)(adaptor->super))->arboretum->newFromToken(((pANTLR3_COMMON_TREE_ADAPTOR)(adaptor->super))->arboretum, payload);

    /* But all adaptors return the pointer to the base interface.
     */
    return  ct;
}


/** Tell me how to create a token for use with imaginary token nodes.
 *  For example, there is probably no input symbol associated with imaginary
 *  token DECL, but you need to create it as a payload or whatever for
 *  the DECL node as in ^(DECL type ID).
 *
 *  If you care what the token payload objects' type is, you should
 *  override this method and any other createToken variant.
 */
static	pANTLR3_COMMON_TOKEN
createToken		(pANTLR3_BASE_TREE_ADAPTOR adaptor, ANTLR3_UINT32 tokenType, pANTLR3_UINT8 text)
{
    pANTLR3_COMMON_TOKEN    newToken;

    newToken	= antlr3CommonTokenNew(tokenType);

    if	(newToken != (pANTLR3_COMMON_TOKEN)(ANTLR3_ERR_NOMEM))
    {
	/* Create the text using our own string factory to avoid complicating
	 * commontoken.
	 */
	newToken->text	= adaptor->strFactory->newPtr(adaptor->strFactory, text, (ANTLR3_UINT32)strlen((const char *)text));
    }

    return  newToken;
}

/** Tell me how to create a token for use with imaginary token nodes.
 *  For example, there is probably no input symbol associated with imaginary
 *  token DECL, but you need to create it as a payload or whatever for
 *  the DECL node as in ^(DECL type ID).
 *
 *  This is a variant of createToken where the new token is derived from
 *  an actual real input token.  Typically this is for converting '{'
 *  tokens to BLOCK etc...  You'll see
 *
 *    r : lc='{' ID+ '}' -> ^(BLOCK[$lc] ID+) ;
 *
 *  If you care what the token payload objects' type is, you should
 *  override this method and any other createToken variant.
 *
 * NB: this being C it is not so easy to extend the types of creaeteToken.
 *     We will have to see if anyone needs to do this and add any variants to
 *     this interface.
 */
static	pANTLR3_COMMON_TOKEN
createTokenFromToken	(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_COMMON_TOKEN fromToken)
{
    pANTLR3_COMMON_TOKEN    newToken;

    newToken	= antlr3CommonTokenNew(fromToken->getType(fromToken));

    if	(newToken != (pANTLR3_COMMON_TOKEN)(ANTLR3_ERR_NOMEM))
    {
	/* Create the text using our own string factory to avoid complicating
	 * commontoken.
	 */
	pANTLR3_STRING	text;
	text		= fromToken->getText(fromToken);
	newToken->text	= adaptor->strFactory->newPtr(adaptor->strFactory, text->text, text->len);

	newToken->setLine		(newToken, fromToken->getLine(fromToken));
	newToken->setTokenIndex		(newToken, fromToken->getTokenIndex(fromToken));
	newToken->setCharPositionInLine	(newToken, fromToken->getCharPositionInLine(fromToken));
	newToken->setChannel		(newToken, fromToken->getChannel(fromToken));
	newToken->toString		= fromToken->toString;
    }

    return  newToken;
}

/* Specific methods for a TreeAdaptor */

/** Track start/stop token for subtree root created for a rule.
 *  Only works with CommonTree nodes.  For rules that match nothing,
 *  seems like this will yield start=i and stop=i-1 in a nil node.
 *  Might be useful info so I'll not force to be i..i.
 */
static	void
setTokenBoundaries	(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE t, pANTLR3_COMMON_TOKEN startToken, pANTLR3_COMMON_TOKEN stopToken)
{
    ANTLR3_UINT64   start;
    ANTLR3_UINT64   stop;

    pANTLR3_COMMON_TREE	    ct;

    if	(t == NULL)
    {
	return;
    }

    if	( startToken != NULL)
    {
	start = startToken->getTokenIndex(startToken);
    }
    else
    {
	start = 0;
    }

    if	( stopToken != NULL)
    {
	stop = stopToken->getTokenIndex(stopToken);
    }
    else
    {
	stop = 0;
    }

    ct	= (pANTLR3_COMMON_TREE)(t->super);

    ct->startIndex  = start;
    ct->stopIndex   = stop;

}

static	ANTLR3_UINT64   
getTokenStartIndex	(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE t)
{
    return  ((pANTLR3_COMMON_TREE)(t->super))->startIndex;
}

static	ANTLR3_UINT64   
getTokenStopIndex	(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE t)
{
    return  ((pANTLR3_COMMON_TREE)(t->super))->stopIndex;
}

static	pANTLR3_UINT8
getText		(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE t)
{
    return  t->getText(t);
}

static	ANTLR3_UINT32
getType		(pANTLR3_BASE_TREE_ADAPTOR adaptor, pANTLR3_BASE_TREE t)
{
    return  t->getType(t);
}