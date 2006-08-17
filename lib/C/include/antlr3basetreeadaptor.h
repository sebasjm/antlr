/** \file
 * Definition of the ANTLR3 base tree adaptor.
 */

#ifndef	_ANTLR3_BASE_TREE_H
#define	_ANTLR3_BASE_TREE_H

#include    <antlr3defs.h>
#include    <antlr3collections.h>
#include    <antlr3string.h>

typedef	struct ANTLR3_BASE_TREE_ADAPTOR_struct
{
    /** POinter to any enclosing structure/interface that
     *  contains this structure.
     */
    void	* super;


    pANTLR3_BASE_TREE	(*nil)	    (void * adaptor);

    pANTLR3_BASE_TREE	(*dupTree)  (void * adaptor, pANTLR3_BASE_TREE tree);

    /** Add a child to the tree t.  If child is a flat tree (a list), make all
     *  in list children of t. Warning: if t has no children, but child does
     *  and child isNil then it is ok to move children to t via
     *  t.children = child.children; i.e., without copying the array.  This
     *  is for construction and I'm not sure it's completely general for
     *  a tree's addChild method to work this way.  Make sure you differentiate
     *  between your tree's addChild and this parser tree construction addChild
     *  if it's not ok to move children to t with a simple assignment.
     */
    void	(*addChild) (void * adaptor, pANTLR3_BASE_TREE t, pANTLR3_BASE_TREE child);
    
    /** If oldRoot is a nil root, just copy or move the children to newRoot.
     *  If not a nil root, make oldRoot a child of newRoot.
     *
     *    old=^(nil a b c), new=r yields ^(r a b c)
     *    old=^(a b c), new=r yields ^(r ^(a b c))
     *
     *  If newRoot is a nil-rooted single child tree, use the single
     *  child as the new root node.
     *
     *    old=^(nil a b c), new=^(nil r) yields ^(r a b c)
     *    old=^(a b c), new=^(nil r) yields ^(r ^(a b c))
     *
     *  If oldRoot was null, it's ok, just return newRoot (even if isNil).
     *
     *    old=null, new=r yields r
     *    old=null, new=^(nil r) yields ^(nil r)
     *
     *  Return newRoot.  Throw an exception if newRoot is not a
     *  simple node or nil root with a single child node--it must be a root
     *  node.  If newRoot is ^(nil x) return x as newRoot.
     *
     *  Be advised that it's ok for newRoot to point at oldRoot's
     *  children; i.e., you don't have to copy the list.  We are
     *  constructing these nodes so we should have this control for
     *  efficiency.
     */
    pANTLR3_BASE_TREE	(*becomeRoot)	(void * adaptor, pANTLR3_BASE_TREE newRoot, pANTLR3_BASE_TREE oldRoot);

    /** Transform ^(nil x) to x */
    pANTLR3_BASE_TREE	(*rulePostProcessing)	(void * adaptor, pANTLR3_BASE_TREE root);
    
    void		(*addChild)		(void * adaptor, pANTLR3_BASE_TREE t, pANTLR3_COMMON_TOKEN child);

    pANTLR3_BASE_TREE	(*becomeRootToken)	(void * adaptor, void * newRoot, pANTLR3_COMMON_TOKEN newRoot, pANTLR3_BASE_TREE oldRoot);

    pANTLR3_BASE_TREE	(*create)		(void * adpator, pANTLR3_COMMON_TOKEN payload);

    pANTLR3_BASE_TREE	(*createTypeToken)	(void * adaptor, ANTLR3_UINT32 tokenType, pANTLR3_COMMON_TOKEN fromToken);

    pANTLR3_BASE_TREE	(*createTypeTokenText)	(void * adaptor, ANTLR3_UINT32 tokenType, pANTLR3_COMMON_TOKEN fromToken, pANTLR3_UINT32 text);

    pANTLR3_BASE_TREE	(*createTypeText)	(void * adaptor, ANTLR3_UINT32 tokenType, pANTLR3_UINT32 text);

    pANTLR3_BASE_TREE	(*dupNode)		(void * adaptor, pANTLR3_BASE_TREE treeNode);

    ANTLR3_UINT32	(*getType)		(void * adaptor, pANTLR3_BASE_TREE t);

    void		(*setType)		(void * adaptor, pANTLR3_BASE_TREE t, ANTLR3_UINT32 type);
    
    pANTLR3_STRING	(*getText)		(void * adaptor, pANTLR3_BASE_TREE t);

    void		(*setText)		(void * adaptor, pANTLR3_STRING t);

    pANTLR3_BASE_TREE	(*getChild)		(void * adaptor, ANTLR3_UINT64 i);

    pANTLR3_UINT64	(*getChildCount)	(void * adaptor, pANTLR3_BASE_TREE);

    ANTLR3_UINT64	(*getUniqueID)		(void * adaptor, pANTLR3_BASE_TREE);

    /** Tell me how to create a token for use with imaginary token nodes.
     *  For example, there is probably no input symbol associated with imaginary
     *  token DECL, but you need to create it as a payload or whatever for
     *  the DECL node as in ^(DECL type ID).
     *
     *  If you care what the token payload objects' type is, you should
     *  override this method and any other createToken variant.
     */
    pANTLR3_COMMON_TOKEN
			(*createToken)		(void * adaptor, ANTLR3_UINT32 tokenType, pANTLR3_STRING text);
    
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
    pANTLR3_COMMON_TOKEN
			(*createTokenFromToken)	(void * adpator, pANTLR3_COMMON_TOKEN fromToken);

}
    ANTLR3_TREE_ADAPTOR;