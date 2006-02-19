/** \file
 * Defines the basic structures used to manipulate character
 * streams from any input source.
 */
#ifndef	_ANTLR3_INPUT_H
#define	_ANTLR3_INPUT_H

#include    <antlr3defs.h>
#include    <antlr3collections.h>

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

    /** Number of characters that can be consumed at this point in time.
     *  Mostly this is just what is left in the pre-read buffer, but if the
     *  input source is a stream such as a socket or something then we may
     *  call special read code to wait for more input.
     */
    ANTLR3_UINT64	size;

    /** The line number we are traversing in the input file. This gets incremented
     *  by a newline() call in the lexer grammer actions.
     */
    ANTLR3_UINT64	line;

    /** Pointer into the input buffer where he current line
     *  started.
     */
    void	      * currentLine;

    /** The offset within the current line of the current character
     */
    ANTLR3_UINT32	charPositionInLine;

    /** Tracks how deep mark() calls are nested
     */
    ANTLR3_UINT32	markDepth;

    /** List of mark() points in the input stream
     */
    pANTLR3_HASH_TABLE	markers;

    /** ASCII (assumed) file name string, set to pointer to memory if
     * you set it manually as it will be free()d
     */
    pANTLR3_UINT8	fileName;

    /** Character that automatically causes an internal line count
     *  increment.
     */
    ANTLR3_UCHAR	newlineChar;

    /** Pointer to function that returns the source file name (if any)
     */
    pANTLR3_UINT8	(*getSourceName)(struct	ANTLR3_INPUT_struct * input);

    /** Pointer to function that resets the input stream
     */
    void		(*reset)	(struct	ANTLR3_INPUT_struct * input);

    /** Pointer to function to consume the next element in the input stream
     */
    void		(*consume)	(struct ANTLR3_INPUT_struct * input);

    /** Pointer to function to return input stream element at 1 based
     *  offset from nextChar.
     */
    ANTLR3_UCHAR	(*LA)		(struct	ANTLR3_INPUT_struct * input, ANTLR3_INT32 la);

    /** Pointer to function to return input stream element at 1 based
     *  offset from nextChar. Same as LA for file stream, but overrides for token
     *  streams etc.
     */
    ANTLR3_UCHAR	(*LT)		(struct	ANTLR3_INPUT_struct * input, ANTLR3_INT32 lt);

    /** Pointer to function to return the current index in the output stream.
     */
    ANTLR3_UINT32	(*index)	(struct ANTLR3_INPUT_struct * input);
}
    ANTLR3_INPUT_STREAM, *pANTLR3_INPUT_STREAM;

    /* Prototypes 
     */
    void	    antlrInputReset	(pANTLR3_INPUT_STREAM input);
    pANTLR3_UINT8   antrl3InputFileName	(pANTLR3_INPUT_STREAM input);
    void	    antlr3ConsumeAscii	(pANTLR3_INPUT_STREAM input);
    ANTLR3_UCHAR    antlr3AsciiLA	(pANTLR3_INPUT_STREAM input, ANTLR3_INT32 la);
    ANTLR3_UCHAR    antlr3AsciiLT	(pANTLR3_INPUT_STREAM input, ANTLR3_INT32 lt);
    ANTLR3_UINT32   antlr3AsciiIndex	(pANTLR3_INPUT_STREAM input);

#endif	/* _ANTLR3_INPUT_H  */