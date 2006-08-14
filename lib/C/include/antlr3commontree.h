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
     * To this we add just a few functions that are specific to the 
     * payload. You can further abstract common treeso long
     * as you alwys have a baseTree pointer in the top structure
     * and copy it from the next one down. 
     * So, lets say we have a structure JIMS_TREE. 
     * It needs an ANTLR3_BASE_TREE that will support all the
     * general tree duplication stuff.
     * It needs a ANTLR3_COMMON_TREE structure embedded or completely
     * provides the equivalent interface.
     * It provides it's own methods and data.
     * To create a new one of these, the function provided to
     * the tree adaptor (see comments there) should allocate the
     * memory for a new JIMS_TREE sturcture, then call
     * antlr3InitCommonTree(<addressofembeddedCOMMON_TREE>)
     * antlr3BaseTreeNew(<addressofBASETREE>)
     * The interfaces for BASE_TREE and COMMON_TREE will then
     * be initialized. You then call and you can override them or just init
     * JIMS_TREE (note that the basetree in common tree will be ignored)
     * just the top level basetree is used). Codegen will take care of the rest.
     * 
     */
    ANTLR3_BASE_TREE	    baseTree;

    pANTLR3_COMMON_TOKEN    (*getToken)			(pANTLR3_BASE_TREE base);

    pANTLR3_UINT8	    (*getText)			(pANTLR3_BASE_TREE tree);
       
    /* Not used by ANTLR, but if a super structure is created above
     * this structure, it can be used to point to the start of the super
     * structure, where additional data and function pointers can be stored.
     */
}
    ANTLR3_COMMON_TREE;

#endif


