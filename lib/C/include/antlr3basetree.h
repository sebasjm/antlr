/** \file
 * Definition of the ANTLR3 base tree.
 */

#ifndef	_ANTLR3_BASE_TREE_H
#define	_ANTLR3_BASE_TREE_H

#include    <antlr3defs.h>
#include    <antlr3collections.h>
#include    <antlr3string.h>

/** A generic tree implementation with no payload.  You must subclass to
 *  actually have any user data.  ANTLR v3 uses a list of children approach
 *  instead of the child-sibling approach in v2.  A flat tree (a list) is
 *  an empty node whose children represent the list.  An empty, but
 *  non-null node is called "nil".
 */
typedef	struct ANTLR3_BASE_TREE_struct
{

    /** Implementors of this interface sometimes require a pointer to theirselves.
     */
    void    *	    me;

    /** The list of all the children that belong to this node. They are not part of the node
     *  as they belong to the common tree node that implements this.
     */
    pANTLR3_LIST    children;

    /** A string factory to produce strings for toString etc
     */
    pANTLR3_STRING_FACTORY strFactory;

    void	    (*addChild)		(void * tree, void * child);

    void	    (*addChildren)	(void * tree, pANTLR3_LIST kids);

    void    	    (*createChildrenList)
					(void * tree);

    void    *	    (*deleteChild)	(void * tree, ANTLR3_UINT64 i);

    void    *	    (*dupNode)		(void * dupNode);

    void    *	    (*dupTree)		(void * tree);

    ANTLR3_UINT32   (*getCharPositionInLine)
					(void * tree);

    void    *	    (*getChild)		(void * tree, ANTLR3_UINT64 i);

    ANTLR3_UINT64   (*getChildCount)	(void * tree);

    ANTLR3_UINT32   (*getType)		(void * tree);

    void    *	    (*getFirstChildWithType)
					(void * tree, ANTLR3_UINT32 type);

    ANTLR3_UINT64   (*getLine)		(void * tree);

    ANTLR3_BOOLEAN  (*isNil)		(void * tree);

    void	    (*setChild)		(void * tree, ANTLR3_UINT64 i, void * child);

    pANTLR3_STRING  (*toStringTree)	(void * tree);

    pANTLR3_STRING  (*toString)		(void * tree);

    void    	    (*free)		(struct ANTLR3_BASE_TREE_struct * tree);

}
    ANTLR3_BASE_TREE;

#endif
