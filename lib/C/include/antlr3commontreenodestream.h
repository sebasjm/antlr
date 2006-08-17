/** \file
 * Definition of the ANTLR3 common tree adaptor.
 */

#ifndef	_ANTLR3_TREE_NODE_H
#define	_ANTLR3_TREE_NODE_H

#include    <antlr3defs.h>
#include    <antlr3collections.h>
#include    <antlr3string.h>

/** As tokens are cached in the stream for lookahead
 *  we start with a bufer of a certain size, defined here
 *  and increase the size if it overflows.
 */
#define	INITIAL_LOOKAHEAD_BUFFER_SIZE  5;

typedef	struct ANTLR3_COMMON_TREE_NODE_STREAM_struct
{
    /** Any interface that implements this interface (is a 
     *  super class of this structure), may store teh pointer
     *  to itself here in the super pointer, which is not used by 
     *  the tree node stream.
     */
    void		* super;

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
    ANTLR3_UINT32	  lastMarker;

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
    pANTLR3_BASE_TREE	  lookAhead;

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

    /** Get tree node at current input pointer + i ahead where i=1 is next node.
     *  i<0 indicates nodes in the past.  So -1 is previous node and -2 is
     *  two nodes ago. LT(0) is undefined.  For i>=n, return null.
     *  Return null for LT(0) and any index that results in an absolute address
     *  that is negative.
     *
     *  This is analogus to the LT() method of the TokenStream, but this
     *  returns a tree node instead of a token.  Makes code gen identical
     *  for both parser and tree grammars. :)
     */
    pANTLR3_BASE_TREE	    (*LT)		(void * ctns, ANTLR3_UINT64 k);

    /** Where is this stream pulling nodes from?  This is not the name, but
     *  the object that provides node objects.
     */
    pANTLR3_BASE_TREE	    (*getTreeSource)	(void * ctns);

    /** Make sure we have at least k symbols in lookahead buffer 
     */
    void		    (*fill)		(void * ctns, ANTLR3_UINT64 k);

    /** Add a node to the lookahead buffer.  Add at lookahead[tail].
     *  If you tail+1 == head, then we must create a bigger buffer
     *  and copy all the nodes over plus reset head, tail.  After
     *  this method, LT(1) will be lookahead[0].
     */
    void		    (*addLookahead)	(void * ctns, pANTLR3_BASE_TREE node);

    void		    (*consume)		(void * ctns);

    ANTLR3_UINT32	    (*LA)		(void * ctns, ANTLR3_UINT64 i);

    ANTLR3_UINT64	    (*mark)		(void * ctns);

    void		    (*release)		(void * ctns, ANTLR3_UINT64 marker);

    void		    (*rewindMark)	(void * ctns, ANTLR3_UINT64 marker);

    void		    (*rewind)		(void * ctns);

    void		    (*seek)		(void * ctns, ANTLR3_UINT64 index);

    void		    (*index)		(void * ctns);

    void		    (*size)		(void * ctns);

    ANTLR3_BOOLEAN	    (*hasNext)		(void * ctns);

    /** Return the next node found during a depth-first walk of root.
     *  Also, add these nodes and DOWN/UP imaginary nodes into the lokoahead
     *  buffer as a side-effect.  Normally side-effects are bad, but because
     *  we can emit many tokens for every next() call, it's pretty hard to
     *  use a single return value for that.  We must add these tokens to
     *  the lookahead buffer.
     *
     *  This does *not* return the DOWN/UP nodes; those are only returned
     *  by the LT() method.
     *
     *  Ugh.  This mechanism is much more complicated than a recursive
     *  solution, but it's the only way to provide nodes on-demand instead
     *  of walking once completely through and buffering up the nodes. :(
     */
    pANTLR3_BASE_TREE	    (*next)		(void * ctns);

    pANTLR3_BASE_TREE	    (*handleRootnode)	(void * ctns);

    pANTLR3_BASE_TREE	    (*visitChild)	(void * ctns, ANTLR3_UINT64 child);

    /** As we flatten the tree, we use UP, DOWN nodes to represent
     *  the tree structure.  When debugging we need unique nodes
     *  so instantiate new ones when uniqueNavigationNodes is true.
     */
    void		    (*addNavigationNode)    (void * ctns, ANTLR3_UINT32 ttype);

    /** Walk upwards looking for a node with more children to walk
     *  using a function with a name almost as long as this sentence
     */
    void		    (*walkBackToMostRecentNodeWithUnvisitedChildren)

				    (void * ctns);

    pANTLR3_BASE_TREE_ADAPTOR
			    (*getTreeAdaptor)	( void *ctns);

    ANTLR3_BOOLEAN	    (*hasUniqueNavigationNodes)	(void * ctns);

    void		    (*setUniqueNavigationNodes)	(void * ctns, ANTLR3_BOOLEAN uniqueNavigationNodes);

    /** Using the Iterator interface, return a list of all the token types
     *  as text.  Used for testing.
     */
    pANTLR3_STRING	    (*toNodesOnlyString)	(void * ctns);

    /** Print out the entire tree including DOWN/UP nodes.  Uses
     *  a recursive walk.  Mostly useful for testing as it yields
     *  the token types not text.
     */
    pANTLR3_STRING	    (*toString)			(void * ctns);

    pANTLR3_STRING	    (*toStringSS)		(void * ctns, pANTLR3_BASE_TREE start, pANTLR3_BASE_TREE stop);

    pANTLR3_STRING	    (*toStringWork)		(void * ctns, pANTLR3_BASE_TREE start, pANTLR3_BASE_TREE stop, pANTLR3_STRING buf);

    ANTLR3_UINT64	    (*getLookaheadSize)		(void * ctns);

}
    ANTLR3_COMMON_TREE_NODE_STREAM;

    /** This structure is used to save the state information in the treenodestream
     *  when walking ahead with cyclic DFA or for syntactic predicates,
     *  we need to record the state of the tree node stream.  This
     *  class wraps up the current state of the CommonTreeNodeStream.
     *  Calling mark() will push another of these on the markers stack.
     */
    typedef ANTLR3_TREE_WALK_STATE_struct
    {
	ANTLR3_UINT64	    currentChildIndex;
	ANTLR3_UINT64	    absoluteNodeIndex;
	pANTLR3_BASE_TREE   currentNode;
	pANTLR3_BASE_TREE   previousNode;
	ANTLR3_UINT64	    nodeStackSize;
	pANTLR3_BASE_TREE   lookahead;
    }
       ANTLR3_TREE_WALK_STATE;

#endif