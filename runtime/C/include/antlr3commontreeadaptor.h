/** \file
 * Definition of the ANTLR3 common tree adaptor.
 */

#ifndef	_ANTLR3_COMMON_TREE_ADAPTOR_H
#define	_ANTLR3_COMMON_TREE_ADAPTOR_H

#include    <antlr3defs.h>
#include    <antlr3collections.h>
#include    <antlr3string.h>
#include    <antlr3basetreeadaptor.h>
#include    <antlr3commontree.h>
#include	<antlr3debugeventlistener.h>

typedef	struct ANTLR3_COMMON_TREE_ADAPTOR_struct
{
    /** Any enclosing structure/class can use this pointer to point to its own interface.
     */
    void    * super;

    /** Base interface implementation, embedded structure
     */
    ANTLR3_TREE_ADAPTOR	baseAdaptor;

    /** Tree factory for producing new nodes as required without needing to track
     *  memory allocation per node.
     */
    pANTLR3_ARBORETUM	arboretum;

	/// Replace from start to stop child index of parent with t, which might
	/// be a list.  Number of children may be different
	/// after this call.  
	///
	/// If parent is null, don't do anything; must be at root of overall tree.
	/// Can't replace whatever points to the parent externally.  Do nothing.
	///
	void						(*replaceChildren)				(struct ANTLR3_BASE_TREE_ADAPTOR_struct * adaptor, pANTLR3_BASE_TREE parent, ANTLR3_UINT32 startChildIndex, ANTLR3_UINT32 stopChildIndex, pANTLR3_BASE_TREE t);
}
    ANTLR3_COMMON_TREE_ADAPTOR;

#endif
