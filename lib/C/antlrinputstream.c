/** \file
 * Base functions to initalize and manipulate any input stream
 */
#include    <antlr3.h>

/** \brief Reset a restartable input stream to the start
 *
 * \param input Input stream context pointer
 */
void
antlrInputReset(pANTLR3_INPUT_STREAM input)
{

    input->nextChar	= input->data;	/* Input at first character */
    input->line		= 1;		/* starts at line 1	    */
    input->markDepth	= 0;		/* Reset markers	    */
    
    /* Free up the markes table if it is there
     */
    antlr3HashFree(input->markers);

    /* Install a new markers table
     */
    input->markers  = antlr3NewHashTable(63);	/* May come back and revist the hash size with experience   */
}

/** \brief Return a pointer to the input stream source name, such as the file.
 *
 * \param input Input stream context pointer
 * \return Pointer to 8 bit ascii (assumed here at least) stream name
 */
pANTLR3_UINT8
antrl3InputFileName(pANTLR3_INPUT_STREAM input)
{
    return  input->fileName;
}

/** \brief Consume the next character in an 8 bit ASCII input stream
 *
 * \param input Input stream context pointer
 */
void
antlr3ConsumeAscii(pANTLR3_INPUT_STREAM input)
{
    if	((pANTLR3_UCHAR)(input->nextChar) < (((pANTLR3_UCHAR)input->data) + input->size))
    {	
	/* Indicate one more chracter in this line
	 */
	input->charPositionInLine++;
	
	if  ((ANTLR3_UCHAR)(*((pANTLR3_INT8)input->data)) == input->newlineChar)
	{
	    /* Reset for start of a new line of input
	     */
	    input->line++;
	    input->charPositionInLine	= 0;
	    input->currentLine		= (void *)(((pANTLR3_INT8)input->nextChar) + 1);
	}

	/* Increment to next character position
	 */
	input->nextChar = (void *)(((pANTLR3_UINT8)input->nextChar) + 1);
    }
}

/** \brief Return the input element assuming an 8 bit ascii iinput
 *
 * \param[in] input Input stream context pointer
 * \param[in] la 1 based offset of next input stream element
 *
 * \return Next input character in internal ANTLR3 encoding (UTF32)
 */
ANTLR3_UCHAR antlr3AsciiLA(pANTLR3_INPUT_STREAM input, ANTLR3_INT32 la)
{
    if	(( ((pANTLR3_UINT8)input->nextChar) + la - 1) >= (((pANTLR3_UINT8)input->data) + input->size))
    {
	return	ANTLR3_CHARSTREAM_EOF;
    }
    else
    {
	return	(ANTLR3_UCHAR)(*((pANTLR3_INT8)input->nextChar));
    }
}

/** \brief Return the input element assuming an 8 bit ascii iinput
 *
 * \param[in] input Input stream context pointer
 * \param[in] lt 1 based offset of next input stream element
 *
 * \return Next input character in internal ANTLR3 encoding (UTF32)
 */
ANTLR3_UCHAR antlr3AsciiLT(pANTLR3_INPUT_STREAM input, ANTLR3_INT32 lt)
{
    return input->LA(input, lt);
}

/** \brief Calculate the current index in the output stream.
 * \param[in] input Input stream context pointer
 */
ANTLR3_UINT32 antlr3AsciiIndex(pANTLR3_INPUT_STREAM input)
{
    return  (ANTLR3_UINT32)(((pANTLR3_INT8)input->nextChar) - ((pANTLR3_INT8)input->data));
}
