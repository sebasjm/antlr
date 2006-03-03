#include    <antlr3basetree.h>

static void    *	getChild	(pANTLR3_BASE_TREE tree, ANTLR3_UINT64 i);
static ANTLR3_UINT64	getChildCount	(pANTLR3_BASE_TREE tree);
static void		addChild	(pANTLR3_BASE_TREE tree, pANTLR3_BASE_TREE child);
static void		setChild	(pANTLR3_BASE_TREE tree, ANTLR3_UINT64 i, void * child);
static void    *	deleteChild	(pANTLR3_BASE_TREE tree, ANTLR3_UINT64 i);
static void    *	dupTree		(pANTLR3_BASE_TREE tree);
static pANTLR3_STRING	toStringTree	(pANTLR3_BASE_TREE tree);



ANTLR3_API pANTLR3_BASE_TREE
antlr3BaseTreeNew(pANTLR3_BASE_TREE  tree)
{
    /* api */
    tree->getChild	    = getChild;
    tree->getChildCount	    = getChildCount;
    tree->addChild	    = addChild;
    tree->setChild	    = setChild;
    tree->deleteChild	    = deleteChild;
    tree->dupTree	    = dupTree;
    tree->toStringTree	    = toStringTree;

    tree->children	    = NULL;
    
    /* Rest must be filled in by caller.
     */
    return  tree;
}




static void    *
getChild		(pANTLR3_BASE_TREE tree, ANTLR3_UINT64 i)
{
    if	(      tree->children == NULL
	    || i >= tree->children->size(tree->children))
    {
	return NULL;
    }
    return  tree->children->get(tree->children, i);
}

static ANTLR3_UINT64
getChildCount	(pANTLR3_BASE_TREE tree)
{
    if	(tree->children == NULL)
    {
	return 0;
    }
    else
    {
	return	tree->children->size(tree->children);
    }
}

void	    
addChild (pANTLR3_BASE_TREE tree, pANTLR3_BASE_TREE child)
{
    if	(child == NULL)
    {
	return;
    }

    if	(tree->children == NULL)
    {
	tree->children = antlr3ListNew(63);
    }

    if	(child->isNil(child->me) == ANTLR3_TRUE)
    {
	if  (child->children == tree->children)
	{
	    fprintf(stderr, "ANTLR3: Internal error, attempt to add child list to itself!\n");
	    return;
	}

	/* Add all of the childrens children to this list
	 */
	if  (child->children != NULL)
	{
	    ANTLR3_UINT64   n;
	    ANTLR3_UINT64   i;

	    n = tree->children->size(tree->children);

	    for (i = 0; i<n; i++)
	    {
		void	* entry;
		entry	= child->children->get(child->children, i);

		/* ANTLR3 lists can be sparse, unlike Array Lists
		 */
		if  (entry != NULL)
		{
		    tree->children->add(tree->children, entry, (void (*)(void *))child->free);
		}
	    }
	}
    }
    else
    {
	tree->children->add(tree->children, child, (void (*)(void *))child->free);
    }
}

static    void
setChild	(pANTLR3_BASE_TREE tree, ANTLR3_UINT64 i, void * child)
{
    if	(tree->children == NULL)
    {
	tree->children	= antlr3ListNew(63);
    }
    tree->children->put(tree->children, i, child, NULL);
}

static void    *
deleteChild	(pANTLR3_BASE_TREE tree, ANTLR3_UINT64 i)
{
    if	( tree->children == NULL)
    {
	return	NULL;
    }

    return  tree->children->remove(tree->children, i);
}

static void    *
dupTree		(pANTLR3_BASE_TREE tree)
{
    pANTLR3_BASE_TREE	newTree;
    ANTLR3_UINT64	i;

    newTree = tree->dupNode(tree);

    for	(i = 0; tree->children != NULL && i < tree->children->size(tree->children); i++)
    {
	pANTLR3_BASE_TREE    t;
	pANTLR3_BASE_TREE    newNode;

	t   = (pANTLR3_BASE_TREE) tree->children->get(tree->children, i);
	
	if  (t!= NULL)
	{
	    newNode	    = t->dupNode(t);
	    newTree->addChild(newTree, newNode);
	}
    }

    return newTree;
}

static pANTLR3_STRING
toStringTree	(pANTLR3_BASE_TREE tree)
{
    pANTLR3_STRING  string;
    ANTLR3_UINT64   i;

    if	(tree->children == NULL || tree->children->size(tree->children) == 0)
    {
	return	tree->toString(tree);
    }

    string	= tree->strFactory->newRaw(tree->strFactory);

    if	(tree->isNil(tree) == ANTLR3_FALSE)
    {
	string->append(string, "(");
	string->append(string, tree->toString(tree));
	string->append(string, " ");
    }
    for	(i = 0; tree->children != NULL && i < tree->children->size(tree->children); i++)
    {
	pANTLR3_BASE_TREE   t;

	t   = (pANTLR3_BASE_TREE) tree->children->get(tree->children, i);
	
	if  (i > 0)
	{
	    string->append(string, " ");
	}
	string->append(string, t->toStringTree(t)->text);
    }
    if	(tree->isNil(tree) == ANTLR3_FALSE)
    {
	string->append(string,")");
    }

    return  string;
}