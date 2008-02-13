/// \file
/// Definition of the ANTLR3 common tree node stream.
///

#ifndef	_ANTLR3_UNBUF_TREE_NODE_STREAM__H
#define	_ANTLR3_UNBUF_TREE_NODE_STREAM__H

#include    <antlr3defs.h>
#include    <antlr3commontreeadaptor.h>
#include    <antlr3commontree.h>
#include    <antlr3collections.h>
#include    <antlr3intstream.h>
#include    <antlr3string.h>
#include	<antlr3commontreenodestream.h>

/// As tokens are cached in the stream for lookahead
///  we start with a buffer of a certain size, defined here
///  and increase the size if it overflows.
///
#define	INITIAL_LOOKAHEAD_BUFFER_SIZE  5

#endif
