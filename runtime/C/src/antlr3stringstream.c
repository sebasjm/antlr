/** \file
 * Provides implementations of string (or memory) streams as input
 * for ANLTR3 lexers.
 */
#include    <antlr3.h>

/** \brief Create an inplace ASCII string stream as input to ANTLR 3.
 *
 * An inplace string steam is the preferred method of suplying strings to ANTLR as input 
 * for lexing and compiling. This is because we make no copies of the input string but
 * read from it right where it is.
 *
 * \param[in] inString	Pointer to the string to be used as the input stream
 * \param[in] size	Size (in 8 bit ASCII characters) of the input string
 * \param[in] name	NAme to attach the input stream (can be NULL pointer)
 *
 * \return
 *	- Pointer to new input stream context upon success
 *	- One of the ANTLR3_ERR_ defines on error.
 *
 * \remark
 *  - ANTLR does not alter the input string in any way.
 *  - String is slightly incorrect in that the passed in pointer can be to any
 *    memory in C version of ANTLR3 of course.
 */
ANTLR3_API pANTLR3_INPUT_STREAM	
antlr3NewAsciiStringInPlaceStream   (pANTLR3_UINT8 inString, ANTLR3_UINT64 size, pANTLR3_UINT8 name)
{
    /* Pointer to the input stream we are going to create
     */
    pANTLR3_INPUT_STREAM    input;

    /* Allocate memory for the input stream structure
     */
    input   = (pANTLR3_INPUT_STREAM)
		    ANTLR3_MALLOC(sizeof(ANTLR3_INPUT_STREAM));

    if	(input == NULL)
    {
	return	(pANTLR3_INPUT_STREAM) ANTLR3_ERR_NOMEM;
    }

    if	(name == NULL)
    {
	input->fileName	= ANTLR3_STRDUP((pANTLR3_UINT8)"-memory-");
    }
    else
    {
	input->fileName  = ANTLR3_STRDUP(name);
    }

    /* Structure was allocated correctly, now we can install the pointer.
     */
    input->data	    = inString;
    input->sizeBuf  = size;

    /* Call the common 8 bit ASCII input stream handler intializer.
     */
    antlr3AsciiSetupStream(input, ANTLR3_CHARSTREAM);

    return  input;
}

/** \brief Create an inplace UCS2 string stream as input to ANTLR 3.
 *
 * An inplace string steam is the preferred method of supplying strings to ANTLR as input 
 * for lexing and compiling. This is because we make no copies of the input string but
 * read from it right where it is.
 *
 * \param[in] inString	Pointer to the string to be used as the input stream
 * \param[in] size	Size (in 16 bit ASCII characters) of the input string
 * \param[in] name	Name to attach the input stream (can be NULL pointer)
 *
 * \return
 *	- Pointer to new input stream context upon success
 *	- One of the ANTLR3_ERR_ defines on error.
 *
 * \remark
 *  - ANTLR does not alter the input string in any way.
 *  - String is slightly incorrect in that the passed in pointer can be to any
 *    memory in C version of ANTLR3 of course.
 */
ANTLR3_API pANTLR3_INPUT_STREAM	
antlr3NewUCS2StringInPlaceStream   (pANTLR3_UINT16 inString, ANTLR3_UINT64 size, pANTLR3_UINT16 name)
{
    /* Pointer to the input stream we are going to create
     */
    pANTLR3_INPUT_STREAM    input;
    ANTLR3_UINT32	    count;

    /* Layout default file name string in correct encoding
     */
    ANTLR3_UINT16   defaultName[] = { '-', 'm', 'e', 'm', 'o', 'r', 'y', '-', '\0' };

    /* Allocate memory for the input stream structure
     */
    input   = (pANTLR3_INPUT_STREAM)
		    ANTLR3_MALLOC(sizeof(ANTLR3_INPUT_STREAM));

    if	(input == NULL)
    {
	return	(pANTLR3_INPUT_STREAM) ANTLR3_ERR_NOMEM;
    }

    if	(name == NULL)
    {
	input->fileName	= ANTLR3_MALLOC(sizeof(ANTLR3_UINT16) * 9);
	ANTLR3_MEMMOVE(input->fileName, (void *)defaultName, sizeof(ANTLR3_UINT16) * 9);
    }
    else
    {
	count = 0;
	while (*(name+count) != '\0') { count++;}
	input->fileName  = ANTLR3_MALLOC(sizeof(ANTLR3_UINT16) * (count +1));
	ANTLR3_MEMMOVE(input->fileName, (void *)name, sizeof(ANTLR3_UINT16) * (count+1));
	*(name+count+1) = '\0';
    }

    /* Structure was allocated correctly, now we can install the pointer.
     */
    input->data	    = inString;
    input->sizeBuf  = size;

    /* Call the common 18 bit input stream handler intializer.
     */
    antlr3UCS2SetupStream   (input, ANTLR3_CHARSTREAM);

    return  input;
}

/** \brief Create an ASCII string stream as input to ANTLR 3, copying the input string.
 *
 * This string stream first makes a copy of the string at the supplied pointer
 *
 * \param[in] inString	Pointer to the string to be copied as the input stream
 * \param[in] size	Size (in 8 bit ASCII characters) of the input string
 * \param[in] name	NAme to attach the input stream (can be NULL pointer)
 *
 * \return
 *	- Pointer to new input stream context upon success
 *	- One of the ANTLR3_ERR_ defines on error.
 *
 * \remark
 *  - ANTLR does not alter the input string in any way.
 *  - String is slightly incorrect in that the passed in pointer can be to any
 *    memory in C version of ANTLR3 of course.
 */
pANTLR3_INPUT_STREAM	antlr3NewAsciiStringCopyStream	    (pANTLR3_UINT8 inString, ANTLR3_UINT64 size, pANTLR3_UINT8 name)
{
    /* Pointer to the input stream we are going to create
     */
    pANTLR3_INPUT_STREAM    input;

    /* Allocate memory for the input stream structure
     */
    input   = (pANTLR3_INPUT_STREAM)
		    ANTLR3_MALLOC(sizeof(ANTLR3_INPUT_STREAM));

    if	(input == NULL)
    {
	return	(pANTLR3_INPUT_STREAM) ANTLR3_ERR_NOMEM;
    }

    if	(name == NULL)
    {
	input->fileName	= ANTLR3_STRDUP((pANTLR3_UINT8)"-memory-");
    }
    else
    {
	input->fileName	= ANTLR3_STRDUP(name);
    }

    /* Indicate that we allocated this input and allocate it
     */
    input->isAllocated	    = ANTLR3_TRUE;
    input->data		    = ANTLR3_MALLOC((size_t)size);

    if	(input->data == NULL)
    {
	return	    (pANTLR3_INPUT_STREAM) ANTLR3_ERR_NOMEM;
    }

    /* Structure was allocated correctly, now we can install the pointer and set the size.
     */
    ANTLR3_MEMMOVE(input->data, (const void *)inString, size);
    input->sizeBuf  = size;

    /* Call the common 8 bit ASCII input stream handler
     * intializer type thingy doobry function.
     */
    antlr3AsciiSetupStream(input, ANTLR3_CHARSTREAM);

    return  input;
}
