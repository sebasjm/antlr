/** \file
 * Definition of the ANTLR3 base tree adaptor.
 */

#ifndef	_ANTLR3_BASE_TREE_ADAPTOR_H
#define	_ANTLR3_BASE_TREE_ADAPTOR_H

#include    <antlr3defs.h>
#include    <antlr3collections.h>
#include    <antlr3string.h>
#include    <antlr3basetree.h>
#include    <antlr3commontoken.h>


typedef	struct ANTLR3_BASE_TREE_ADAPTOR_struct
{
    /** POinter to any enclosing structure/interface that
     *  contains this structure.
     */
    void	* super;


    pANTLR3_BASE_TREE	    (*nil)			(void * adaptor);

    pANTLR3_BASE_TREE	    (*dupTree)			(void * adaptor, pANTLR3_BASE_TREE tree);

    void		    (*addChild)			(void * adaptor, pANTLR3_BASE_TREE t, pANTLR3_BASE_TREE child);
    void		    (*addChildToken)		(void * adaptor, pANTLR3_BASE_TREE t, pANTLR3_COMMON_TOKEN child);

    pANTLR3_BASE_TREE	    (*becomeRoot)		(void * adaptor, pANTLR3_BASE_TREE newRoot, pANTLR3_BASE_TREE oldRoot);

    pANTLR3_BASE_TREE	    (*rulePostProcessing)	(void * adaptor, pANTLR3_BASE_TREE root);

    pANTLR3_BASE_TREE	    (*becomeRootToken)		(void * adaptor, pANTLR3_COMMON_TOKEN newRoot, pANTLR3_BASE_TREE oldRoot);

    pANTLR3_BASE_TREE	    (*create)			(void * adpator, pANTLR3_COMMON_TOKEN payload);
    pANTLR3_BASE_TREE	    (*createTypeToken)		(void * adaptor, ANTLR3_UINT32 tokenType, pANTLR3_COMMON_TOKEN fromToken);
    pANTLR3_BASE_TREE	    (*createTypeTokenText)	(void * adaptor, ANTLR3_UINT32 tokenType, pANTLR3_COMMON_TOKEN fromToken, pANTLR3_UINT8 text);
    pANTLR3_BASE_TREE	    (*createTypeText)		(void * adaptor, ANTLR3_UINT32 tokenType, pANTLR3_UINT8 text);

    pANTLR3_BASE_TREE	    (*dupNode)			(void * adaptor, pANTLR3_BASE_TREE treeNode);

    ANTLR3_UINT32	    (*getType)			(void * adaptor, pANTLR3_BASE_TREE t);

    void		    (*setType)			(void * adaptor, pANTLR3_BASE_TREE t, ANTLR3_UINT32 type);
    
    pANTLR3_UINT8	    (*getText)			(void * adaptor, pANTLR3_BASE_TREE t);

    void		    (*setText)			(void * adaptor, pANTLR3_UINT8 t);

    pANTLR3_BASE_TREE	    (*getChild)			(void * adaptor, ANTLR3_UINT64 i);

    pANTLR3_UINT64	    (*getChildCount)		(void * adaptor, pANTLR3_BASE_TREE);

    ANTLR3_UINT64	    (*getUniqueID)		(void * adaptor, pANTLR3_BASE_TREE);

    pANTLR3_COMMON_TOKEN    (*createToken)		(void * adaptor, ANTLR3_UINT32 tokenType, pANTLR3_UINT8 text);
    pANTLR3_COMMON_TOKEN    (*createTokenFromToken)	(void * adaptor, pANTLR3_COMMON_TOKEN fromToken);

    void		    (*setTokenBoundaries)	(void * adaptor, pANTLR3_BASE_TREE t, pANTLR3_COMMON_TOKEN startToken, pANTLR3_COMMON_TOKEN stopToken);

    ANTLR3_UINT64	    (*getTokenStartIndex)	(void * adaptor, pANTLR3_BASE_TREE t);

    ANTLR3_UINT64	    (*getTokenStopIndex)	(void * adaptor, pANTLR3_BASE_TREE t);
}
    ANTLR3_TREE_ADAPTOR;

#endif