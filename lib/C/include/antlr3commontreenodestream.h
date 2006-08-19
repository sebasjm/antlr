/** \file
 * Definition of the ANTLR3 common tree adaptor.
 */

#ifndef	_ANTLR3_COMMON_TREE_NODE_STREAM__H
#define	_ANTLR3_COMMON_TREE_NODE_STREAM__H

#include    <antlr3defs.h>
#include    <antlr3collections.h>
#include    <antlr3string.h>
#include    <antlr3commontree.h>

/** As tokens are cached in the stream for lookahead
 *  we start with a bufer of a certain size, defined here
 *  and increase the size if it overflows.
 */
#define	INITIAL_LOOKAHEAD_BUFFER_SIZE  5

typedef	struct ANTLR3_COMMON_TREE_NODE_STREAM_struct
{
    /** Any interface that implements this interface (is a 
     *  super class of this structure), may store teh pointer
     *  to itself here in the super pointer, which is not used by 
     *  the tree node stream.
     */
    void		* super;

    /* String factory for use by anything that wishes to create strings
     * such as a tree representation or some copy of the text etc.
     */
    pANTLR3_STRING_FACTORY  stringFactory;

    /** Dummy tree node that indicates a descent into a child
     *  tree. Initialized by a call to create a new interface.
     */
    ANTLR3_BASE_TREE	  DOWN;

    /** Dummy tree node that indicates a descent up to a parent
     *  tree. Initialized by a call to create a new interface.
     */
    ANTLR3_BASE_TREE	  UP;

    /** Dummy tree node that indicates the termination point of the
     *  tree. Initialized by a call to create a new interface.
     */
    ANTLR3_BASE_TREE	  EOF_NODE;

    /** Dummy node that is returned if we need to indicate an invlaid node
     *  for any reason.
     */
    ANTLR3_BASE_TREE	  INVALID_NODE;

    /** If set to ANTLR3_TRUE then the navigation nodes UP, DOWN are
     *  duplicated rather than reused within the tree.
     */
    ANTLR3_BOOLEAN	  uniqueNavigationNodes;

    /** Which tree are we navigating ?
     */
    pANTLR3_BASE_TREE	  root;

    /** Pointer to tree adaptor interface that maniplates/builds
     *  the tree.
     */
    pANTLR3_BASE_TREE_ADAPTOR	adaptor;

    /** As we walk down the nodes, we must track parent nodes so we know
     *  where to go after walking the last child of a node.  When visiting
     *  a child, push current node and current index (current index
     *  is first stored in the tree node structure to avoid two stacks.
     */
    pANTLR3_STACK	  nodeStack;

    /** Track the last mark() call result value for use in rewind(). 
     */
    ANTLR3_UINT64	  lastMarker;

    /** Which node are we currently visiting?
     */
    pANTLR3_BASE_TREE	  currentNode;

    /** Which node did we last visit? Used for LT(-1)
     */
    pANTLR3_BASE_TREE	  previousNode;

    /** Which child are we currently visiting?  If -1 we have not visited
     *  this node yet; next consume() request will set currentIndex to 0.
     */
    ANTLR3_INT64	  currentChildIndex;

    /** What node index did we just consume?  i=0..n-1 for n node trees.
     *  IntStream.next is hence 1 + this value.  Size will be same.
     */
    ANTLR3_INT64	  absoluteNodeIndex;

    /** Buffer tree node stream for use with LT(i).  This list grows
     *  to fit new lookahead depths, but consume() wraps like a circular
     *  buffer.
     */
    pANTLR3_BASE_TREE	  * lookAhead;

    /** NUmber of elements available in the lookahead buffer at any point in
     *  time. This is the current size of the array.
     */
    ANTLR3_UINT32	  lookAheadLength;

    /** lookAhead[head] is the first symbol of lookahead, LT(1). 
     */
    ANTLR3_UINT32	  head;

    /** Add new lookahead at lookahead[tail].  tail wraps around at the
     *  end of the lookahead buffer so tail could be less than head.
     */
    ANTLR3_UINT32	  tail;

    /** Calls to mark() may be nested so we have to track a stack of
     *  them.  The marker is an index into this stack.  Index 0 is
     *  the first marker.  This is a List<TreeWalkState>
     */
    pANTLR3_LIST	  markers;

    /* INTERFACE    */

    void		(*reset)    (void * ctns);

    pANTLR3_BASE_TREE	    (*LT)		(void * ctns, ANTLR3_UINT64 k);

    pANTLR3_BASE_TREE	    (*getTreeSource)	(void * ctns);

    void		    (*fill)		(void * ctns, ANTLR3_UINT64 k);

    void		    (*addLookahead)	(void * ctns, pANTLR3_BASE_TREE node);

    void		    (*consume)		(void * ctns);

    ANTLR3_UINT32	    (*LA)		(void * ctns, ANTLR3_UINT64 i);

    ANTLR3_UINT64	    (*mark)		(void * ctns);

    void		    (*release)		(void * ctns, ANTLR3_UINT64 marker);

    void		    (*rewindMark)	(void * ctns, ANTLR3_UINT64 marker);

    void		    (*rewind)		(void * ctns);

    void		    (*seek)		(void * ctns, ANTLR3_UINT64 index);

    ANTLR3_UINT64	    (*index)		(void * ctns);

    void		    (*size)		(void * ctns);

    ANTLR3_BOOLEAN	    (*hasNext)		(void * ctns);

    pANTLR3_BASE_TREE	    (*next)		(void * ctns);

    pANTLR3_BASE_TREE	    (*handleRootnode)	(void * ctns);

    pANTLR3_BASE_TREE	    (*visitChild)	(void * ctns, ANTLR3_UINT64 child);

    void		    (*addNavigationNode)    (void * ctns, ANTLR3_UINT32 ttype);

    pANTLR3_BASE_TREE	    (*newDownNode)	(void * ctns);

    pANTLR3_BASE_TREE	    (*newUpNode)	(void * ctns);

    void		    (*walkBackToMostRecentNodeWithUnvisitedChildren)

				    (void * ctns);

    pANTLR3_BASE_TREE_ADAPTOR
			    (*getTreeAdaptor)	( void *ctns);

    ANTLR3_BOOLEAN	    (*hasUniqueNavigationNodes)	(void * ctns);

    void		    (*setUniqueNavigationNodes)	(void * ctns, ANTLR3_BOOLEAN uniqueNavigationNodes);

    pANTLR3_STRING	    (*toNodesOnlyString)	(void * ctns);

    pANTLR3_STRING	    (*toString)			(void * ctns);

    pANTLR3_STRING	    (*toStringSS)		(void * ctns, pANTLR3_BASE_TREE start, pANTLR3_BASE_TREE stop);

    void		    (*toStringWork)		(void * ctns, pANTLR3_BASE_TREE start, pANTLR3_BASE_TREE stop, pANTLR3_STRING buf);

    ANTLR3_UINT32	    (*getLookaheadSize)		(void * ctns);

}
    ANTLR3_COMMON_TREE_NODE_STREAM;

/** This structure is used to save the state information in the treenodestream
 *  when walking ahead with cyclic DFA or for syntactic predicates,
 *  we need to record the state of the tree node stream.  This
 *  class wraps up the current state of the CommonTreeNodeStream.
 *  Calling mark() will push another of these on the markers stack.
 */
typedef struct ANTLR3_TREE_WALK_STATE_struct
{
    ANTLR3_UINT64	      currentChildIndex;
    ANTLR3_UINT64	      absoluteNodeIndex;
    pANTLR3_BASE_TREE	      currentNode;
    pANTLR3_BASE_TREE	      previousNode;
    ANTLR3_UINT64	      nodeStackSize;
    pANTLR3_BASE_TREE	    * lookAhead;
    ANTLR3_UINT32	      lookAheadLength;
    ANTLR3_UINT32	      tail;
    ANTLR3_UINT32	      head;
}
    ANTLR3_TREE_WALK_STATE;

#endif