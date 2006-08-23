/** \file
 *  Implementation of the tree parser and overrides for the base recognizer
 */

#include    <antlr3treeparser.h>

static void				SetTreeNodeStream   (pANTLR3_TREE_PARSER parser, pANTLR3_COMMON_TREE_NODE_STREAM input);
static pANTLR3_COMMON_TREE_NODE_STREAM	getTreeNodeStream   (pANTLR3_TREE_PARSER parser);
static void				mismatch	    (pANTLR3_TREE_PARSER parser);
    
    
/** Set the input stream and reset the parser
 */
static void
setTreeNodeStream	(pANTLR3_TREE_PARSER parser, pANTLR3_COMMON_TREE_NODE_STREAM input)
{
    parser->ctnstream = input;
    parser->ctnstream->reset(parser->ctnstream);
}

/** Return a pointer to the input stream
 */
static pANTLR3_COMMON_TREE_NODE_STREAM
getTreeNodeStream	(pANTLR3_TREE_PARSER parser)
{
    return  parser->ctnstream;
}