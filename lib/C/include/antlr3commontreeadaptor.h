/** \file
 * Definition of the ANTLR3 common tree adaptor.
 */

#ifndef	_ANTLR3_COMMON_TREE_H
#define	_ANTLR3_COMMON_TREE_H

#include    <antlr3defs.h>
#include    <antlr3collections.h>
#include    <antlr3string.h>

typedef	struct ANTLR3_BASE_TREE_ADAPTOR_struct
{
    void    * super;


    /** Track start/stop token for subtree root created for a rule.
     *  Only works with CommonTree nodes.  For rules that match nothing,
     *  seems like this will yield start=i and stop=i-1 in a nil node.
     *  Might be useful info so I'll not force to be i..i.
     */
    void	    (*setTokenBoundaries)	(void * adaptor, pANTLR3_BASE_TREE t, pANTLR3_COMMON_TOKEN startToken, pANTLR3_COMMON_TOKEN stopToken);

    ANTLR3_UINT64   (*getTokenStartIndex)	(void * adaptor, pANTLR3_BASE_TREE t);

    ANTLR3_UINT64   (*getTokenStopIndex)	(void * adaptor, pANTLR3_BASE_TREE t);

}
    ANTLR3_COMMON_TREE_ADAPTOR;