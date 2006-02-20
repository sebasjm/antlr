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

    /** Indicates if the data pointer was allocated by us, and so should be freed
     *  when the stream dies.
     */
    int			isAllocated;

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
    ANTLR3_UINT64	sizeBuf;

    /** The line number we are traversing in the input file. This gets incremented
     *  by a newline() call in the lexer grammer actions.
     */
    ANTLR3_UINT64	line;

    /** Pointer into the input buffer where the current line
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

    /** Pointer to function to return the total size of the input buffer. For streams
     *  this may be just the total we have available so far. This means of course that
     *  the input stream must be careful to accumulate enough input so that any backtracking
     *  can be satisfied.
     */
    ANTLR3_UINT64	(*size)		(struct ANTLR3_INPUT_struct * input);

    /** Pointer to function mark the current point in the input stream. Records
     *  the state of teh inptu stream so taht it can be reset with rewind()
     */
    ANTLR3_UINT32	(*mark)		(struct ANTLR3_INPUT_struct * input);

    /** Pointer to function that resets the lexer to marker state n
     */
    void		(*rewind)	(struct ANTLR3_INPUT_struct * input, ANTLR3_INT32 mark);

    /** Pointer to function that cleans up any mark states after state n
     */
    void		(*release)	(struct ANTLR3_INPUT_struct * input, ANTLR3_INT32 mark);

    /** Pointer to function that seeks the input pointer to a particular character
     *  offset in the input stream, taking care of any pointer updates, such as line etc.
     */
    void		(*seek)		(struct ANTLR3_INPUT_struct * input, void * seekPoint);

    /** Pointer to function to return a substring of the input stream. String is returned in allocated
     *  memory and is in same encoding as the input stream itself, NOT internal ANTLR3_UCHAR form.
     */
    void	  *	(*substr)	(struct ANTLR3_INPUT_struct * input, ANTLR3_INT32 start, ANTLR3_INT32 stop);

    /** Pointer to function to return the current line number in the innput stream
     */
    ANTLR3_UINT64	(*getLine)	(struct ANTLR3_INPUT_struct * input);

    /** Pointer to function to return the current line buffer in the input stream
     *  The pointer returned is directly into the input stream so you must copy
     *  it if you wish to manipulate it without damaging the input stream. Encoding
     *  is obviously in the same form as the input stream.
     *  \remark
     *    - Note taht this function wil lbe inaccurate if setLine is called as there
     *      is no way at the moment to position the input stream at a particular line 
     *	    number offset.
     */
    void	  *	(*getLineBuf)	(struct ANTLR3_INPUT_struct * input);

    /** Pointer to function to return the current offset in the current input stream line
     */
    ANTLR3_UINT32	(*getCharPositionInLine)  (struct ANTLR3_INPUT_struct * input);

    /** Pointer to function to set the current line number in the input stream
     */
    void		(*setLine)		  (struct ANTLR3_INPUT_struct * input, ANTLR3_UINT32 line);

    /** Pointer to function to set the current position in the current line.
     */
    void		(*setCharPositionInLine)  (struct ANTLR3_INPUT_struct * input, ANTLR3_UINT32 position);

    /** Pointer to function to override the default newline character that the input stream
     *  looks for to trigger the line and offset and line buffer recording information.
     *  \remark
     *   - By default the chracter '\n' will be instaleldas tehe newline trigger character. When this
     *     character is seen by the consume() function then the current line number is incremented and the
     *     current line offset is reset to 0. The Pointer for the line of input we are consuming
     *     is updated to point to the next character after this one in the input stream (which means it
     *     may become invlaid if the last newline character in the file is seen (so watch out).
     *   - If for some reason you do not want teh counters and pointesr to be restee, yu can set the 
     *     chracter to some impossible charater such as '\0' or whatever.
     *   - This is a single character only, so choose the last chracter in a sequence of two or more.
     *   - This is only a simple aid to error reporting - if you have a complicated binary inptu structure
     *     it may not be adequate, but you can always override every function in the input stream with your
     *     own of course, and can even write your own complete input stream set if you like.
     *   - It is your responsiblity to set a valid cahracter for the input stream type. Ther is no point 
     *     setting this to 0xFFFFFFFF if the input stream is 8 bit ASCII as this will just be truncated and never
     *	   trigger as the comparison will be (INT32)0xFF == (INT32)0xFFFFFFFF
     */
    void		(*SetNewLineChar)	    (struct ANTLR3_INPUT_struct * input, ANTLR3_UINT32 newlineChar);
}

    ANTLR3_INPUT_STREAM, *pANTLR3_INPUT_STREAM;


/** \brief Structure for track lex input states as part of mark()
 *  and rewind() of lexer.
 */
typedef	struct	ANTLR3_LEX_STATE_struct
{
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

    /** Pointer into the input buffer where the current line
     *  started.
     */
    void	      * currentLine;

    /** The offset within the current line of the current character
     */
    ANTLR3_UINT32	charPositionInLine;

}
    ANTLR3_LEX_STATE, *pANTLR3_LEX_STATE;

    /* Prototypes 
     */
    void	    antlr3InputClose		(pANTLR3_INPUT_STREAM input);
    void	    antlr3InputReset		(pANTLR3_INPUT_STREAM input);
    pANTLR3_UINT8   antrl3InputFileName		(pANTLR3_INPUT_STREAM input);
    void	    antlr3AsciiConsume		(pANTLR3_INPUT_STREAM input);
    ANTLR3_UCHAR    antlr3AsciiLA		(pANTLR3_INPUT_STREAM input, ANTLR3_INT32 la);
    ANTLR3_UCHAR    antlr3AsciiLT		(pANTLR3_INPUT_STREAM input, ANTLR3_INT32 lt);
    ANTLR3_UINT32   antlr3AsciiIndex		(pANTLR3_INPUT_STREAM input);
    ANTLR3_UINT64   antrl3AsciiSize		(pANTLR3_INPUT_STREAM input);
    ANTLR3_UINT32   antlr3AsciiMark		(pANTLR3_INPUT_STREAM input);
    void	    antlr3AsciiRewind		(pANTLR3_INPUT_STREAM input, ANTLR3_INT32 mark);
    void	    antlr3AsciiRelease		(pANTLR3_INPUT_STREAM input, ANTLR3_INT32 mark);
    void	    antlr3AsciiSeek		(pANTLR3_INPUT_STREAM input, void * seekPoint);
    void	  * antlr3AsciiSubstr		(pANTLR3_INPUT_STREAM input, ANTLR3_INT32 start, ANTLR3_INT32 stop);
    ANTLR3_UINT64   antlr3AsciiGetLine		(pANTLR3_INPUT_STREAM input);
    void	  * antlr3AsciiGetLineBuf	(pANTLR3_INPUT_STREAM input);
    ANTLR3_UINT32   antlr3AsciiGetCharPosition	(pANTLR3_INPUT_STREAM input);
    void	    antlr3AsciiSetLine		(pANTLR3_INPUT_STREAM input, ANTLR3_UINT32 line);
    void	    antlr3AsciiSetCharPosition	(pANTLR3_INPUT_STREAM input, ANTLR3_UINT32 position);
    void	    antlr3AsciiSetNewLineChar	(pANTLR3_INPUT_STREAM input, ANTLR3_UINT32 newlineChar);
    void	    antlr3AsciiSetupStream	(pANTLR3_INPUT_STREAM input);
#endif	/* _ANTLR3_INPUT_H  */