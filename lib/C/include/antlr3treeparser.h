#ifndef	ANTLR3TREEPARSER_H
#define	ANTLR3TREEPARSER_H

#include    <antlr3defs.h>

/** Internal structure representing an element in a hash bucket.
 *  Stores the original key so that duplicate keys can be rejected
 *  if necessary, and contains function can be suported. If the hash key
 *  could be unique I would have invented the perfect compression algorithm ;-)
 */
typedef	struct	ANTLR3_TREE_PARSER_struct
{
    /** Pointer to any super class
     */
    void    * super;

    /** Pointer to the tree nod stream for the parser
     */
    pANTLR3_COMMON_TREE_NODE_STREAM input;

    /** A pointer to the base recognizer, where most of the parser functions actually
     *  live because they are shared between parser and tree parser and this is the
     *  easier way than copying the interface all over the place. Macros hide this
     *  for the generated code so it is easier on the eye (though not the debugger ;-).
     */
    pANTLR3_BASE_RECOGNIZER	rec;
    
}

    
#endif