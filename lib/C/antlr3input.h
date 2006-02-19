/** \file
 * Defines the basic structures used to manipulate character
 * streams from any input source.
 */
#ifndef	_ANTLR3_INPUT_H
#define	_ANTLR3_INPUT_H

#include    <antlr3defs.h>

/** \brief Master context structure for an ANTLR3
 *   C runtime based input stream.
 */
typedef	struct	ANTLR3_INPUT_struct
{
    /** Pointer the start of the input string, chracters may be
     *  taken as offsets from here and in original input format encoding.
     */
    void	      *	data;

    /** Pointer to the next character to be consumed from the input data
     *  This is cast to point at the encoding of the original file that
     *  was read by the functions installed as pointer in this input stream
     *  context instance at file/string/whatever load time.
     */
    void	      * nextChar;

    /** The line number we are traversing in the input file. This gets incremented
     *  by a newline() call in the lexer grammer actions.
     */
    ANTLR3_UINT64	line;

    /** The offset within the current line of the current character
     */
    ANTLR3_UINT32	charPositionInLine;

    /** Tracks how deep mark() calls are nested
     */
    ANTLR3_UINT32	markDepth;

    /** List of mark() points in the input stream
    /* ASCII (assumed) file name string, set to pointer to memory if
     * you set it manually as it will be free()d
     */
    pANTLR3_INT8	fileName;

    /** Pointer to function that returns the source file name (if any)
     */
    pANTLR3_INT8	(getSourceName)(pANTLR3_INPUT_STREAM input);
}
    ANTLR3_INPUT_STREAM, *pANTLR3_INPUT_STREAM;

#endif	/* _ANTLR3_INPUT_H  */