/** Interface for an ANTLR3 common tree which is what gets
 *  passed around by the AST producing parser.
 */

#ifndef	_ANTLR3_COMMON_TREE_H
#define	_ANTLR3_COMMON_TREE_H

#include    <antlr3defs.h>
#include    <antlr3basetree.h>
#include    <antlr3commontoken.h>

typedef struct ANTLR3_COMMON_TREE_struct
{

    /** Other things can sub class this if they like, and can carry
     *  round a pointer to theirselves. Not used by antlr as this is the
     * top of the inhertience tree :-)
     */
    void	* me;

    /** Start token index that encases this tree
     */
    ANTLR3_UINT64   startIndex;

    /** End token that encases this tree
     */
    ANTLR3_UINT64   stopIndex;

    /** A single token, this is the payload for the tree
     */
    pANTLR3_COMMON_TOKEN    token;

    /* An encapsulated BASE TREE strcuture (NOT a pointer)
     * that perfoms a lot of the dirty work of node management
     */
    ANTLR3_BASE_TREE	    baseTree;

    pANTLR3_COMMON_TOKEN    (*getToken)			(pANTLR3_BASE_TREE base);

    pANTLR3_BASE_TREE	    (*dupNode)			(pANTLR3_BASE_TREE tree);

    ANTLR3_BOOLEAN	    (*isNil)			(pANTLR3_BASE_TREE tree);

    ANTLR3_UINT32	    (*getType)			(pANTLR3_BASE_TREE tree);

    pANTLR3_UINT8	    (*getText)			(pANTLR3_BASE_TREE tree);

    ANTLR3_UINT64	    (*getLine)			(pANTLR3_BASE_TREE tree);
    
    ANTLR3_UINT32	    (*getCharPositionInLine)	(pANTLR3_BASE_TREE tree);

    pANTLR3_STRING	    (*toString)			(pANTLR3_BASE_TREE tree);




}
    ANTLR3_COMMON_TREE;

#endif


