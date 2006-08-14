/** \file
 * Implementation of ANTLR3 CommonTree, wihch you can use as a
 * starting point for your own tree. Thoguh it is easier just to tag things on
 * to the user pointer in the tree to be honest.
 */
#include    <antlr3commontree.h>


static pANTLR3_COMMON_TOKEN getToken			(pANTLR3_BASE_TREE tree);
static pANTLR3_BASE_TREE    dupNode			(pANTLR3_BASE_TREE tree);
static ANTLR3_BOOLEAN	    isNil			(pANTLR3_BASE_TREE tree);
static ANTLR3_UINT32	    getType			(pANTLR3_BASE_TREE tree);
static pANTLR3_UINT8	    getText			(pANTLR3_BASE_TREE tree);
static ANTLR3_UINT64	    getLine			(pANTLR3_BASE_TREE tree);
static ANTLR3_UINT32	    getCharPositionInLine	(pANTLR3_BASE_TREE tree);
static pANTLR3_STRING	    toString			(pANTLR3_BASE_TREE tree);
    
static void		freeTree	(pANTLR3_BASE_TREE tree);

ANTLR3_API pANTLR3_COMMON_TREE	    
antlr3CommonTreeNewFromTree(pANTLR3_COMMON_TREE tree)
{
    pANTLR3_COMMON_TREE	newTree;

    newTree = antlr3CommonTreeNew();

    if	(newTree == (pANTLR3_COMMON_TREE)ANTLR3_ERR_NOMEM)
    {
	return	(pANTLR3_COMMON_TREE)ANTLR3_ERR_NOMEM;
    }

    /* Pick up the payload we had in the supplied tree
     */
    newTree->token = tree->token;

    return  newTree;
}

ANTLR3_API pANTLR3_COMMON_TREE	    
antlr3CommonTreeNewFromToken(pANTLR3_COMMON_TOKEN token)
{
    pANTLR3_COMMON_TREE	newTree;

    newTree = antlr3CommonTreeNew();

    if	(newTree == (pANTLR3_COMMON_TREE)ANTLR3_ERR_NOMEM)
    {
	return	(pANTLR3_COMMON_TREE)ANTLR3_ERR_NOMEM;
    }

    /* Pick up the payload we had in the supplied tree
     */
    newTree->token = token;

    return newTree;
}

ANTLR3_API pANTLR3_COMMON_TREE
antlr3CommonTreeNew()
{
    pANTLR3_COMMON_TREE	tree;

    tree    = ANTLR3_MALLOC(sizeof(ANTLR3_COMMON_TREE));

    if	(tree == NULL)
    {
	return (pANTLR3_COMMON_TREE)ANTLR3_ERR_NOMEM;
    }
    /* Init base tree
     */
    antlr3BaseTreeNew(&(tree->baseTree));

    /* We need a pointer to ourselves for 
     * the payload and few functions that we
     * provide.
     */
    tree->baseTree.me	    = ANTLR3_API_FUNC tree;

    /* Common tree overrides */

    tree->baseTree.free	    = ANTLR3_API_FUNC freeTree;
    tree->baseTree.isNil    = ANTLR3_API_FUNC isNil;
    tree->baseTree.toString = ANTLR3_API_FUNC toString;
    tree->baseTree.dupNode  = ANTLR3_API_FUNC dupNode;
    tree->baseTree.getLine  = ANTLR3_API_FUNC getLine;
    tree->baseTree.getCharPositionInLine
			    = ANTLR3_API_FUNC getCharPositionInLine;
    tree->baseTree.toString = ANTLR3_API_FUNC toString;

    tree->getToken	    = ANTLR3_API_FUNC getToken;
    tree->getText	    = ANTLR3_API_FUNC getText;

    tree->token	= NULL;	/* No token as yet */
    tree->startIndex	= 0;
    tree->stopIndex	= 0;

    return tree;
}

static void
freeTree(pANTLR3_BASE_TREE tree)
{
    /* Call free on all the nodes.
     * We installed all the nodes as base nodes with a pointer to a function that
     * knows how to free itself. A function that calls this function in fact. So if we just
     * delete the hash table, then this function will be called for all
     * child nodes, which will delete thier child nodes, and so on
     * recursively until they are all gone :-)
     */
    if	(tree->children != NULL)
    {
	tree->children->free(tree->children);
    }
    
    /* Now we can free this structure memory, which contains the base tree
     * structure also. Later I wll expand this to call an public fuciton to release
     * the base node, so people overriding it will be able to use it more freely.
     */
    ANTLR3_FREE(tree->me);

    return;
}


static pANTLR3_COMMON_TOKEN 
getToken			(pANTLR3_BASE_TREE tree)
{
    /* The token is the payload of the common tree or other implementor
     * so it is stored within ourselves, which is the me pointer.
     */
    return  ((pANTLR3_COMMON_TREE)(tree->me))->token;
}

static pANTLR3_BASE_TREE    
dupNode			(pANTLR3_BASE_TREE tree)
{
    /* The node we are duplicating is in fact the common tree (that's why we are here)
     * so we use the me pointer to duplicate.
     */
    pANTLR3_COMMON_TREE	    theNew;
    
    theNew  = antlr3CommonTreeNewFromTree((pANTLR3_COMMON_TREE)(tree->me));

    /* The pointer we return is the base implementation of course
     */
    return &(theNew->baseTree);

}

static ANTLR3_BOOLEAN	    
isNil			(pANTLR3_BASE_TREE tree)
{
    /* This is a Nil tree if it has no payload (Token in our case)
     * This is C, and you should never return the result of a comparison
     * so we can't do the same as Java (no emails about this, I am correct and
     * you know it ;-)
     */
   if	(((pANTLR3_COMMON_TREE)(tree->me))->token == NULL)
   {
       return ANTLR3_TRUE;
   }
   else
   {
       return ANTLR3_FALSE;
   }
}

static ANTLR3_UINT32	    
getType			(pANTLR3_BASE_TREE tree)
{
    pANTLR3_COMMON_TREE    theTree;

    theTree = (pANTLR3_COMMON_TREE)(tree->me);

    if	(theTree->token == NULL)
    {
	return	0;
    }
    else
    {
	return	theTree->token->getType(theTree->token);
    }
}

static pANTLR3_UINT8	    
getText			(pANTLR3_BASE_TREE tree)
{
    return	tree->toString(tree)->text;
}

static ANTLR3_UINT64	    getLine			(pANTLR3_BASE_TREE tree)
{
    pANTLR3_COMMON_TREE	    cTree;
    pANTLR3_COMMON_TOKEN    token;

    cTree   = ((pANTLR3_COMMON_TREE)(tree->me))->me;

    token   = cTree->token;

    if	(token == NULL || token->getLine(token) == 0)
    {
	if  (tree->getChildCount(tree) > 0)
	{
	    return ((pANTLR3_BASE_TREE)tree->getChild(tree, 0))->getLine(tree);
	}
	return 0;
    }
    return  token->getLine(token);
}

static ANTLR3_UINT32	    getCharPositionInLine	(pANTLR3_BASE_TREE tree)
{
    pANTLR3_COMMON_TOKEN    token;

    token   = ((pANTLR3_COMMON_TREE)(tree->me))->token;

    if	(token == NULL || token->getCharPositionInLine(token) == -1)
    {
	if  (tree->getChildCount(tree) > 0)
	{
	    return ((pANTLR3_BASE_TREE)(tree->getChild(tree, 0)))->getCharPositionInLine(tree);
	}
	return 0;
    }
    return  token->getCharPositionInLine(token);
}

static pANTLR3_STRING	    toString			(pANTLR3_BASE_TREE tree)
{
	if  (tree->isNil(tree) == ANTLR3_TRUE)
	{
	    pANTLR3_STRING  nil;

	    nil	= tree->strFactory->newPtr(tree->strFactory, (pANTLR3_UINT8)"nil", 3);

	    return nil;
	}

	return	((pANTLR3_COMMON_TREE)(tree->me))->token->getText(((pANTLR3_COMMON_TREE)(tree->me))->token);
}

