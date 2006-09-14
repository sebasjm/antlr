/** \file
 * \brief The ANTLR3 C filestream is used when the source character stream
 * is a filesystem based input set and all the chracters in the filestream
 * can be loaded at once into memory and away the lexer goes.
 *
 * A number of initializers are provided in order that various character
 * sets can be supported from input files. The ANTLR3 C runtime expects
 * to deal with UTF32 characters only (the reasons for this are to
 * do with the simplification of C code when using this form of Unicode 
 * encoding, though this is not a panacea. More information can be
 * found on this by consulting: 
 *   - http://www.unicode.org/versions/Unicode4.0.0/ch02.pdf#G11178
 * Where a well grounded discussino of the encoding formats available
 * may be found.
 *
 * At some future point, a UTF-16 version of the runtime may be produced
 * if enough peole feel that the memory overhead of UTF32 is a problem
 * for their applications. At that point I would overhaul the C.stg and
 * related string templates, which produce the code so that the character
 * encoding could be abstracted out and a common source produced where
 * possible. 
 *
 * At this point in time though, UTF32 is it, because memory tends not to
 * be a problem and mosts CPUs are 32 bit or above and process 32 bit
 * characters efficiently.
 */
#include    <antlr3.h>


/** \brief Use the contents of an operating system file as the input
 *         for an input stream.
 *
 * \param fileName Name of operating system file to read.
 * \return
 *	- Pointer to new input stream context upon success
 *	- One of the ANTLR3_ERR_ defines on error.
 */
ANTLR3_API pANTLR3_INPUT_STREAM
antlr3AsciiFileStreamNew(pANTLR3_UINT8 fileName)
{
    /* Pointer to the input stream we are going to create
     */
    pANTLR3_INPUT_STREAM    input;
    ANTLR3_UINT64	    status;

    /* Allocate memory for the input stream structure
     */
    input   = (pANTLR3_INPUT_STREAM)
		    ANTLR3_MALLOC(sizeof(ANTLR3_INPUT_STREAM));

    if	(input == NULL)
    {
	return	(pANTLR3_INPUT_STREAM) ANTLR3_ERR_NOMEM;
    }

    input->fileName  = ANTLR3_STRDUP(fileName);

    /* Structure was allocated correctly, now we can read the file.
     */
    status  = antlr3readAscii(input);

    if	(status != ANTLR3_SUCCESS)
    {
	ANTLR3_FREE(input->fileName);
	ANTLR3_FREE(input);

	return	ANTLR3_FUNC_PTR(status);
    }

    /* Call the common 8 bit ASCII input stream handler
     * intializer type thingy doobry function.
     */
    antlr3AsciiSetupStream(input, ANTLR3_CHARSTREAM);



    return  input;
}

ANTLR3_API ANTLR3_UINT64
antlr3readAscii(pANTLR3_INPUT_STREAM    input)
{
    ANTLR3_FDSC		    infile;
    ANTLR3_UINT64	    fSize;

    /* Open the OS file in read binary mode
     */
    infile  = antlr3Fopen(input->fileName, "rb");

    /* Check that it was there
     */
    if	(infile == NULL)
    {
	return	(ANTLR3_UINT64)ANTLR3_ERR_NOFILE;
    }

    /* It was there, so we can read the bytes now
     */
    fSize   = antlr3Fsize(input->fileName);	/* Size of input file	*/

    /* Allocate buffer for this input set   
     */
    input->data	    = ANTLR3_MALLOC((size_t)fSize);
    input->sizeBuf  = fSize;

    if	(input->data == NULL)
    {
	return	(ANTLR3_UINT64)ANTLR3_ERR_NOMEM;
    }
    
    input->isAllocated	= ANTLR3_TRUE;

    /* Now we read the file. Characters are not converted to
     * the internal ANTLR encoding until they are read from the buffer
     */
    antlr3Fread(infile, fSize, input->data);

    /* And close the file handle
     */
    antlr3Fclose(infile);

    return  ANTLR3_SUCCESS;
}

/** \brief Open an operating system file and return the descriptor
 * We just use the common open() and related functions here. 
 * Later we might find better ways on systems
 * such as Windows and OpenVMS for instance. But the idea is to read the 
 * while file at once anyway, so it may be irrelevant.
 */
ANTLR3_API ANTLR3_FDSC
antlr3Fopen(pANTLR3_UINT8 filename, const char * mode)
{
    return  (ANTLR3_FDSC)fopen((const char *)filename, mode);
}

/** \brief Close an operating system file and free any handles
 *  etc.
 */
ANTLR3_API void
antlr3Fclose(ANTLR3_FDSC fd)
{
    fclose(fd);
}
ANTLR3_API ANTLR3_UINT64
antlr3Fsize(pANTLR3_UINT8 fileName)
{   
    struct _stat	statbuf;

    _stat((const char *)fileName, &statbuf);

    return (ANTLR3_UINT64)statbuf.st_size;
}

ANTLR3_API ANTLR3_UINT64
antlr3Fread(ANTLR3_FDSC fdsc, ANTLR3_UINT64 count,  void * data)
{
    return  (ANTLR3_UINT64)fread(data, (size_t)count, 1, fdsc);
}
