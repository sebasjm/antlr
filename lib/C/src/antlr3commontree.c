/** \file
 * Implementation of ANTLR3 CommonTree, wihch you can use as a
 * starting point for your own tree. Thoguh it is easier just to tag things on
 * to the user pointer in the tree to be honest.
 */
#include    <antlr3commontree.h>


static pANTLR3_COMMON_TOKEN (*getToken)			(pANTLR3_BASE_TREE base);
static pANTLR3_BASE_TREE    (*dupNode)			(pANTLR3_BASE_TREE tree);
static ANTLR3_BOOLEAN	    (*isNil)			(pANTLR3_BASE_TREE tree);
static ANTLR3_UINT32	    (*getType)			(pANTLR3_BASE_TREE tree);
static ANTLR3_UINT8	    (*getText)			(pANTLR3_BASE_TREE tree);
static ANTLR3_UINT64	    (*getLine)			(pANTLR3_BASE_TREE tree);
static ANTLR3_UINT32	    (*getCharPositionInLine)	(pANTLR3_BASE_TREE tree);
static pANTLR3_STRING	    (*toString)			(pANTLR3_BASE_TREE tree);
    
static void		freeTree	(pANTLR3_BASE_TREE tree);

ANTLR3_API pANTLR3_COMMON_TREE
antlr3CommonTreeNew()
{
    pANTLR3_COMMON_TREE	tree;

    tree    = ANTLR3_MALLOC(sizeof(ANTLR3_COMMON_TREE));

    /* Init base tree
     */
    antlr3BaseTreeNew(&(tree->baseTree));

    tree->baseTree.me	    = tree;

    tree->baseTree.free	    = freeTree;
    tree->baseTree.isNil    = isNil;
    tree->baseTree.toString = toString;
    tree->baseTree.dupNode  = dupNode;

    tree->getToken	    = getToken;
    tree->getLine	    = getLine;
    tree->getText	    = getText;

    return tree;
}

static void
freeTree(pANTLR3_BASE_TREE tree)
{
    ANTLR3_UINT64   i;

    /* Call free on all the nodes.
     * We installed all the nodes as base nodes with a pointer to a function that
     * knows how to free itself. A function that calls this function in fact. So if we just
     * delete the hash table, then this function will be called for all
     * child nodes, which will delete thier child nodes, and so on
     * recursively until they are all gone :-)
     */
    if	(tree->children != NULL)
    {
	tree->children->free(tree->children));
    }
    
    /* Now we can free this structure memory, which containts the base tree
     * structure also. LAter I wll expend this to call an public fuciton to release
     * the base node, so people overriding it will be able to use it more freely.
     */
    ANTLR3_FREE(tree->me);

    return;
}

static pANTLR3_COMMON_TOKEN (*getToken)			(pANTLR3_BASE_TREE base)
{
}

static pANTLR3_BASE_TREE    (*dupNode)			(pANTLR3_BASE_TREE tree)
{
}

static ANTLR3_BOOLEAN	    (*isNil)			(pANTLR3_BASE_TREE tree)
{
}

static ANTLR3_UINT32	    (*getType)			(pANTLR3_BASE_TREE tree)
{
}

static ANTLR3_UINT8	    (*getText)			(pANTLR3_BASE_TREE tree)
{
}

static ANTLR3_UINT64	    (*getLine)			(pANTLR3_BASE_TREE tree)
{
}

static ANTLR3_UINT32	    (*getCharPositionInLine)	(pANTLR3_BASE_TREE tree)
{
}

static pANTLR3_STRING	    (*toString)			(pANTLR3_BASE_TREE tree)
{
}
