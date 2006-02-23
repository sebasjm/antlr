/** \file
 * Implementation of the ANTLR3 string and string factory classes
 */
#include    <antlr3string.h>

/* Factory API
 */
static    pANTLR3_STRING    newRaw	(pANTLR3_STRING_FACTORY factory);
static    pANTLR3_STRING    newSize	(pANTLR3_STRING_FACTORY factory, ANTLR3_UINT32 size);
static    pANTLR3_STRING    newPtr	(pANTLR3_STRING_FACTORY factory, pANTLR3_UINT8 string, ANTLR3_UINT32 size);
static    void		    destroy	(pANTLR3_STRING_FACTORY factory, pANTLR3_STRING string);
static    pANTLR3_STRING    printable	(pANTLR3_STRING_FACTORY factory, pANTLR3_STRING string);
static    void		    close	(pANTLR3_STRING_FACTORY factory);

/* String API
 */
static    pANTLR3_UINT8	    append	(pANTLR3_STRING string, void * newbit);
static    pANTLR3_UINT8	    set		(pANTLR3_STRING string, void * chars);
static    pANTLR3_UINT8	    addc	(pANTLR3_STRING string, ANTLR3_UINT8 c);
static    pANTLR3_UINT8	    addi	(pANTLR3_STRING string, ANTLR3_INT32 i);
static	  pANTLR3_UINT8	    insert	(pANTLR3_STRING string, ANTLR3_UINT32 point, void * newbit);
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
    factory->newRaw	= newRaw;
    factory->newPtr	= newPtr;
    factory->newSize	= newSize;
    factory->destroy	= destroy;
    factory->printable	= printable;
    factory->destroy	= destroy;
    factory->close	= close;

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
    factory->strings->put(factory->strings, factory->index, (void *) string, stringFree);
    string->index   = factory->index++;

    return string;
}

static	void	stringFree  (pANTLR3_STRING string)
{
    /* First free the string itself if there was anything in it
     */
    if	(string->text)
    {
	ANTLR3_FREE(string->text);
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
    string->text    = NULL;

    string->addc    = addc;
    string->addi    = addi;
    string->append  = append;
    string->set	    = set;
    string->insert  = insert;
    string->inserti = inserti;
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
    string->text    = (pANTLR3_UINT8) ANTLR3_MALLOC((size_t)size+1);
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
	ANTLR3_MEMMOVE(string->text, (const void *)ptr, size);
	*(string->text + size) = '\0';	    /* Terminate, these strings are usually used for Token streams and printing etc.	*/
	string->len = size;
    }

    return  string;
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
    scannedText = string->text;

    for	(i = 0; i < instr->len; i++)
    {
	if (*(instr->text + i) == '\n')
	{
	    *scannedText++ = '\\';
	    *scannedText++ = 'n';
	}
	else if (*(instr->text + i) == '\r')
	{
	    *scannedText++ = '\\';
	    *scannedText++ = 'n';
	}
	else if (*(instr->text + i) == '\r')
	{
	    *scannedText++ = '\\';
	    *scannedText++ = 'n';
	}
	else if	(*(instr->text + i) < 0x32 || *(instr->text +i) > 0x7F)
	{
	    *scannedText++ = '?';
	}
	i++;
    }
    *scannedText++  = '\0';

    string->len	= (ANTLR3_UINT32)((ANTLR3_UINT64)scannedText - string->len);
    
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
append	(pANTLR3_STRING string, void * newbit)
{
    ANTLR3_UINT32 len;

    len	= (ANTLR3_UINT32)strlen(newbit);

    if	(string->size < (string->len + len + 1))
    {
	string->text	= (pANTLR3_UINT8) ANTLR3_REALLOC((void *)string->text, (ANTLR3_UINT64)(string->len + len + 1));
	string->size	= string->len + len + 1;
    }

    /* Note we copy one more byte than the strlen in order to get the trailing
     */
    ANTLR3_MEMMOVE((void *)(string->text + string->len), newbit, (ANTLR3_UINT64)(len+1));
    string->len	+= len;

    return string->text;
}

static    pANTLR3_UINT8   
set	(pANTLR3_STRING string, void * chars)
{
    ANTLR3_UINT32	len;

    len = (ANTLR3_UINT32)strlen(chars);
    if	(string->size < len + 1)
    {
	string->text	= (pANTLR3_UINT8) ANTLR3_REALLOC((void *)string->text, (ANTLR3_UINT64)(len + 1));
	string->size	= len + 1;
    }

    /* Note we copy one more byte than the strlen in order to get the trailing '\0'
     */
    ANTLR3_MEMMOVE((void *)(string->text), chars, (ANTLR3_UINT64)(len));

    return  string->text;

}

static    pANTLR3_UINT8   
addc	(pANTLR3_STRING string, ANTLR3_UINT8 c)
{
    if	(string->size < string->len + 2)
    {
	string->text	= (pANTLR3_UINT8) ANTLR3_REALLOC((void *)string->text, (ANTLR3_UINT64)(string->len + 2));
	string->size	= string->len + 2;
    }
    *(string->text + string->len)	= c;
    *(string->text + string->len + 1)	= '\0';
    string->len++;

    return  string->text;
}

static    pANTLR3_UINT8   
addi	(pANTLR3_STRING string, ANTLR3_INT32 i)
{
    ANTLR3_UINT8	    newbit[32];

    sprintf((char *)newbit, "%d", i);

    return  string->append(string, newbit);
}

static	  pANTLR3_UINT8
inserti	(pANTLR3_STRING string, ANTLR3_UINT32 point, ANTLR3_INT32 i)
{
    ANTLR3_UINT8	    newbit[32];

    sprintf((char *)newbit, "%d", i);
    return  string->insert(string, point, newbit);
}

static	pANTLR3_UINT8
insert	(pANTLR3_STRING string, ANTLR3_UINT32 point, void * newbit)
{
    ANTLR3_UINT32	len;

    if	(point >= string->len)
    {
	return	string->append(string, newbit);
    }
 
    len	= (ANTLR3_UINT32)strlen(newbit);

    if	(len == 0)
    {
	return	string->text;
    }

    if	(string->size < (string->len + len + 1))
    {
	string->text	= (pANTLR3_UINT8) ANTLR3_REALLOC((void *)string->text, (ANTLR3_UINT64)(string->len + len + 1));
	string->size	= string->len + len + 1;
    }

    /* Move the characters we are inserting before, including the delimiter
     */
    ANTLR3_MEMMOVE((void *)(string->text + point + len), (void *)(string->text + point), (ANTLR3_UINT64)(string->len - point));

    /* Note we copy the exact number of bytes
     */
    ANTLR3_MEMMOVE((void *)(string->text + point), newbit, (ANTLR3_UINT64)(len));
    
    string->len += len;

    return  string->text;
}
