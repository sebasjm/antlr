/** \file
 * Implementation of the ANTLR3 string and string factory classes
 */
#include    <antlr3string.h>

/* Factory API
 */
static    pANTLR3_STRING    newRaw	(pANTLR3_STRING_FACTORY factory);
static    pANTLR3_STRING    newSize	(pANTLR3_STRING_FACTORY factory, ANTLR3_UINT32 size);
static    pANTLR3_STRING    newPtr	(pANTLR3_STRING_FACTORY factory, pANTLR3_UINT8 string, ANTLR3_UINT32 size);
static    pANTLR3_STRING    newStr	(pANTLR3_STRING_FACTORY factory, pANTLR3_UINT8 string);
static    void		    destroy	(pANTLR3_STRING_FACTORY factory, pANTLR3_STRING string);
static    pANTLR3_STRING    printable	(pANTLR3_STRING_FACTORY factory, pANTLR3_STRING string);
static    void		    close	(pANTLR3_STRING_FACTORY factory);

/* String API
 */
static    pANTLR3_UINT8	    set		(pANTLR3_STRING string, const char * chars);
static    pANTLR3_UINT8	    append	(pANTLR3_STRING string, const char * newbit);
static	  pANTLR3_UINT8	    insert	(pANTLR3_STRING string, ANTLR3_UINT32 point, const char * newbit);

static    pANTLR3_UINT8	    setS	(pANTLR3_STRING string, pANTLR3_STRING chars);
static    pANTLR3_UINT8	    appendS	(pANTLR3_STRING string, pANTLR3_STRING newbit);
static	  pANTLR3_UINT8	    insertS	(pANTLR3_STRING string, ANTLR3_UINT32 point, pANTLR3_STRING newbit);

static    pANTLR3_UINT8	    addc	(pANTLR3_STRING string, ANTLR3_UINT32 c);
static    pANTLR3_UINT8	    addi	(pANTLR3_STRING string, ANTLR3_INT32 i);
static	  pANTLR3_UINT8	    inserti	(pANTLR3_STRING string, ANTLR3_UINT32 point, ANTLR3_INT32 i);

/* Local helpers
 */
static	void	stringInit  (pANTLR3_STRING string);
static	void	stringFree  (pANTLR3_STRING string);

ANTLR3_API pANTLR3_STRING_FACTORY 
antlr3StringFactoryNew()
{
    pANTLR3_STRING_FACTORY  factory;

    /* Allocate memory
     */
    factory	= (pANTLR3_STRING_FACTORY) ANTLR3_MALLOC(sizeof(ANTLR3_STRING_FACTORY));

    if	(factory == NULL)
    {
	return	(pANTLR3_STRING_FACTORY)(ANTLR3_ERR_NOMEM);
    }

    /* Now we make a new list to track the strings, 256 will allow thousands
     * before there is any real performance degradation (until free() ;-).
     */
    factory->strings	= antlr3ListNew(256);
    factory->index	= 0;

    if	(factory->strings == (pANTLR3_LIST)(ANTLR3_ERR_NOMEM))
    {
	ANTLR3_FREE(factory);
	return	(pANTLR3_STRING_FACTORY)(ANTLR3_ERR_NOMEM);
    }

    /* Install the API
     */
    factory->newRaw	= ANTLR3_API_FUNC newRaw;
    factory->newPtr	= ANTLR3_API_FUNC newPtr;
    factory->newStr	= ANTLR3_API_FUNC newStr;
    factory->newSize	= ANTLR3_API_FUNC newSize;
    factory->destroy	= ANTLR3_API_FUNC destroy;
    factory->printable	= ANTLR3_API_FUNC printable;
    factory->destroy	= ANTLR3_API_FUNC destroy;
    factory->close	= ANTLR3_API_FUNC close;

    return  factory;
}
 
/**
 *
 * \param factory 
 * \return 
 */
static    pANTLR3_STRING    
newRaw	(pANTLR3_STRING_FACTORY factory)
{
    pANTLR3_STRING  string;

    string  = (pANTLR3_STRING) ANTLR3_MALLOC(sizeof(ANTLR3_STRING));

    if	(string == NULL)
    {
	return	(pANTLR3_STRING)(ANTLR3_ERR_NOMEM);
    }

    /* Structure is allocated, now fill in the API etc.
     */
    stringInit(string);
    string->factory = factory;

    /* Add the string into the allocated list
     */
    factory->strings->put(factory->strings, factory->index, (void *) string, ANTLR3_API_FUNC stringFree);
    string->index   = factory->index++;

    return string;
}

static	void	stringFree  (pANTLR3_STRING string)
{
    /* First free the string itself if there was anything in it
     */
    if	(string->chars)
    {
	ANTLR3_FREE(string->chars);
    }

    /* Now free the space for this string
     */
    ANTLR3_FREE(string);

    return;
}
/**
 *
 * \param string 
 * \return 
 */
static	void
stringInit  (pANTLR3_STRING string)
{
    string->len	    = 0;
    string->size    = 0;
    string->chars   = NULL;

    /* API */

    string->set	    = set;
    string->append  = append;
    string->insert  = insert;

    string->setS    = setS;
    string->appendS = appendS;
    string->insertS = insertS;

    string->addi    = addi;
    string->inserti = inserti;

    string->addc    = addc;

}

/**
 *
 * \param factory 
 * \param size 
 * \return 
 */
static    pANTLR3_STRING    
newSize	(pANTLR3_STRING_FACTORY factory, ANTLR3_UINT32 size)
{
    pANTLR3_STRING  string;

    string  = factory->newRaw(factory);

    if	(string == (pANTLR3_STRING)(ANTLR3_ERR_NOMEM))
    {
	return	string;
    }

    /* Always add one more byte for a terminator ;-)
     */
    string->chars   = (pANTLR3_UINT8) ANTLR3_MALLOC((size_t)size+1);
    string->size    = size + 1;

    return string;
}

static    pANTLR3_STRING    
newPtr	(pANTLR3_STRING_FACTORY factory, pANTLR3_UINT8 ptr, ANTLR3_UINT32 size)
{
    pANTLR3_STRING  string;

    string  = factory->newSize(factory, size);

    if	(string == (pANTLR3_STRING)(ANTLR3_ERR_NOMEM))
    {
	return	string;
    }

    if	(size <= 0)
    {
	return	string;
    }

    if	(ptr != NULL)
    {
	ANTLR3_MEMMOVE(string->chars, (const void *)ptr, size);
	*(string->chars + size) = '\0';	    /* Terminate, these strings are usually used for Token streams and printing etc.	*/
	string->len = size;
    }

    return  string;
}

static    pANTLR3_STRING    
newStr	(pANTLR3_STRING_FACTORY factory, pANTLR3_UINT8 ptr)
{
    return factory->newPtr(factory, ptr, (ANTLR3_UINT32)strlen((const char *)ptr));
}

static    void		    
destroy	(pANTLR3_STRING_FACTORY factory, pANTLR3_STRING string)
{
    factory->strings->del(factory->strings, string->index);
}

static    pANTLR3_STRING    
printable(pANTLR3_STRING_FACTORY factory, pANTLR3_STRING instr)
{
    pANTLR3_STRING  string;
    
    /* We don't need to be too efficient here, this is mostly for error messages and so on.
     */
    pANTLR3_UINT8   scannedText;
    ANTLR3_UINT32   i;

    /* Assume we need as much as twice as much space to parse out the control characters
     */
    string  = factory->newSize(factory, instr->len *2 + 1);

    /* Scan through and replace unprintable (in terms of this routine)
     * characters
     */
    scannedText = string->chars;

    for	(i = 0; i < instr->len; i++)
    {
	if (*(instr->chars + i) == '\n')
	{
	    *scannedText++ = '\\';
	    *scannedText++ = 'n';
	}
	else if (*(instr->chars + i) == '\r')
	{
	    *scannedText++ = '\\';
	    *scannedText++ = 'n';
	}
	else if (*(instr->chars + i) == '\r')
	{
	    *scannedText++ = '\\';
	    *scannedText++ = 'n';
	}
	else if	(!isprint(*(instr->chars +i)))
	{
	    *scannedText++ = '?';
	}
	else
	{
	    *scannedText++ = *(instr->chars + i);
	}
    }
    *scannedText++  = '\0';

    string->len	= (ANTLR3_UINT32)(scannedText - string->chars);
    
    return  string;
}

static    void		    
close	(pANTLR3_STRING_FACTORY factory)
{
    /* Delete the hash table we were tracking teh strings with,this will
     * causes all the allocated strings to be deallocated too
     */
    factory->strings->free(factory->strings);

    /* Delete the space for the factory itself
     */
    ANTLR3_FREE((void *)factory);
}

static    pANTLR3_UINT8   
append	(pANTLR3_STRING string, const char * newbit)
{
    ANTLR3_UINT32 len;

    len	= (ANTLR3_UINT32)strlen(newbit);

    if	(string->size < (string->len + len + 1))
    {
	string->chars	= (pANTLR3_UINT8) ANTLR3_REALLOC((void *)string->chars, (ANTLR3_UINT64)(string->len + len + 1));
	string->size	= string->len + len + 1;
    }

    /* Note we copy one more byte than the strlen in order to get the trailing
     */
    ANTLR3_MEMMOVE((void *)(string->chars + string->len), newbit, (ANTLR3_UINT64)(len+1));
    string->len	+= len;

    return string->chars;
}

static    pANTLR3_UINT8   
set	(pANTLR3_STRING string, const char * chars)
{
    ANTLR3_UINT32	len;

    len = (ANTLR3_UINT32)strlen(chars);
    if	(string->size < len + 1)
    {
	string->chars	= (pANTLR3_UINT8) ANTLR3_REALLOC((void *)string->chars, (ANTLR3_UINT64)(len + 1));
	string->size	= len + 1;
    }

    /* Note we copy one more byte than the strlen in order to get the trailing '\0'
     */
    ANTLR3_MEMMOVE((void *)(string->chars), chars, (ANTLR3_UINT64)(len));
    string->len	    = len;

    return  string->chars;

}

static    pANTLR3_UINT8   
addc	(pANTLR3_STRING string, ANTLR3_UINT32 c)
{
    if	(string->size < string->len + 2)
    {
	string->chars	= (pANTLR3_UINT8) ANTLR3_REALLOC((void *)string->chars, (ANTLR3_UINT64)(string->len + 2));
	string->size	= string->len + 2;
    }
    *(string->chars + string->len)	= (ANTLR3_UINT8)c;
    *(string->chars + string->len + 1)	= '\0';
    string->len++;

    return  string->chars;
}

static    pANTLR3_UINT8   
addi	(pANTLR3_STRING string, ANTLR3_INT32 i)
{
    ANTLR3_UINT8	    newbit[32];

    sprintf((char *)newbit, "%d", i);

    return  string->append(string, (const char *)newbit);
}

static	  pANTLR3_UINT8
inserti	(pANTLR3_STRING string, ANTLR3_UINT32 point, ANTLR3_INT32 i)
{
    ANTLR3_UINT8	    newbit[32];

    sprintf((char *)newbit, "%d", i);
    return  string->insert(string, point, (const char *)newbit);
}

static	pANTLR3_UINT8
insert	(pANTLR3_STRING string, ANTLR3_UINT32 point, const char * newbit)
{
    ANTLR3_UINT32	len;

    if	(point >= string->len)
    {
	return	string->append(string, newbit);
    }
 
    len	= (ANTLR3_UINT32)strlen(newbit);

    if	(len == 0)
    {
	return	string->chars;
    }

    if	(string->size < (string->len + len + 1))
    {
	string->chars	= (pANTLR3_UINT8) ANTLR3_REALLOC((void *)string->chars, (ANTLR3_UINT64)(string->len + len + 1));
	string->size	= string->len + len + 1;
    }

    /* Move the characters we are inserting before, including the delimiter
     */
    ANTLR3_MEMMOVE((void *)(string->chars + point + len), (void *)(string->chars + point), (ANTLR3_UINT64)(string->len - point));

    /* Note we copy the exact number of bytes
     */
    ANTLR3_MEMMOVE((void *)(string->chars + point), newbit, (ANTLR3_UINT64)(len));
    
    string->len += len;

    return  string->chars;
}

static    pANTLR3_UINT8	    setS	(pANTLR3_STRING string, pANTLR3_STRING chars)
{
    return  set(string, (const char *)(chars->chars));
}
static    pANTLR3_UINT8	    appendS	(pANTLR3_STRING string, pANTLR3_STRING newbit)
{
    // We may be passed an empty string, in which case we just return the current pointer
    //
    if	(newbit->len == 0 || newbit->size == 0 || newbit->chars == NULL)
    {
	return	string->chars;
    }
    else
    {
	return  append(string, (const char *)(newbit->chars));
    }
}
static	  pANTLR3_UINT8	    insertS	(pANTLR3_STRING string, ANTLR3_UINT32 point, pANTLR3_STRING newbit)
{
    return  insert(string, point, (const char *)(newbit->chars));
}