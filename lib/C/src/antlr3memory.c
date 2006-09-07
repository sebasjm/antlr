/** \brief ANTLR3 C rutime memory management abstractions
 *
 * Abstracts routines such as malloc into our own fucntion calls
 * such that we can look at faster ways to allocate memory on one
 * system vs another in the future. For now, this just uses
 * malloc()
 */
#include    <antlr3defs.h>

ANTLR3_API void *
ANTLR3_MEMMOVE(void * target, const void * source, ANTLR3_UINT64 size)
{
    return  memmove(target, source, (size_t)size);
}

ANTLR3_API void *
ANTLR3_MEMSET(void * target, ANTLR3_UINT8 byte, ANTLR3_UINT64 size)
{
    return memset(target, (int)byte, (size_t)size);
}

#ifndef	ANTLR3_MEM_DEBUG

ANTLR3_API void *
ANTLR3_MALLOC(size_t request)
{
    return  calloc(1, request);
}

ANTLR3_API void *
ANTLR3_REALLOC(void * current, ANTLR3_UINT64 request)
{
    return  realloc(current, (size_t)request);
}

ANTLR3_API void
ANTLR3_FREE(void * ptr)
{
    free(ptr);
}

ANTLR3_API pANTLR3_UINT8
ANTLR3_STRDUP(pANTLR3_UINT8 instr)
{
    return  (pANTLR3_UINT8)strdup((const char *)instr);
}

#else

#include    <antlr3collections.h>
#ifdef	WIN32
#pragma warning( disable : 4100 )
#endif


static	pANTLR3_LIST  memtrace;

static	int initialized	= 0;
static	int record		= 0;
static	int reporting	= 0;

static void	init()
{
    initialized	    = 1;    /* Only call this once	    */
    record		    = 0;    /* Don't record while initializing */
    memtrace	    = antlr3ListNew(32767);    /* Nice big one for trace - just debugging - coudl even be biggger I guess	*/
    record		    = 1;
}

struct	memrec
{
    ANTLR3_UINT8    file[256];
    ANTLR3_UINT32   line;
    ANTLR3_UINT64   size;
    ANTLR3_UINT8    type;
};

ANTLR3_API void *
ANTLR3_MALLOC_DBG(pANTLR3_UINT8 file, ANTLR3_UINT32 line, size_t request)
{
    void    * m;
    struct  memrec * tr;

    if	(!initialized)
    {
	init();
    }

    m =  calloc(1, request);

    if	(record && !reporting)
    {
	record = 0;
	tr  = (struct  memrec *)malloc(sizeof(struct memrec));
	tr->line    = line;
	tr->size    = request;
	tr->type    = 0;
	sprintf((char *)tr->file, "%.256s", file);
	memtrace->put(memtrace, (ANTLR3_UINT64)m, (void *)tr, free);
	record = 1;
    }
    
    return  m;
}

ANTLR3_API void *
ANTLR3_REALLOC_DBG(pANTLR3_UINT8 file, ANTLR3_UINT32 line, void * current, ANTLR3_UINT64 request)
{
    void    * m;
    struct  memrec * tr;

    if	(!initialized)
    {
	init();
    }

    m =   realloc(current, (size_t)request);

    if	(record && !reporting && m != current)
    {
	record = 0;
	memtrace->del(memtrace, (ANTLR3_UINT64)current);
	tr  = (struct  memrec *)malloc(sizeof(struct memrec));
	tr->line    = line;
	tr->size    = request;
	tr->type    = 1;
	sprintf((char *)tr->file, "%.256s", file);
	memtrace->put(memtrace, (ANTLR3_UINT64)m, (void *)tr, free);
	record = 1;
    }
    
    return  m;

}

ANTLR3_API void
ANTLR3_FREE_DBG(void * ptr)
{
    if	(record && !reporting)
    {
	record = 0;
	memtrace->del(memtrace, (ANTLR3_UINT64)ptr);
	record = 1;
    }
    free(ptr);
}

ANTLR3_API pANTLR3_UINT8
ANTLR3_STRDUP_DBG(pANTLR3_UINT8 file, ANTLR3_UINT32 line, pANTLR3_UINT8 instr)
{
    void    * m;
    struct  memrec * tr;

    if	(!initialized)
    {
	init();
    }

    m =   (pANTLR3_UINT8)strdup((const char *)instr);
    
    if	(record && ! reporting)
    {
	record = 0;
	tr  = (struct  memrec *)malloc(sizeof(struct memrec));
	tr->line    = line;
	tr->size    = strlen((const char *)instr);
	tr->type    = 2;
	sprintf((char *)tr->file, "%.256s", file);
	memtrace->put(memtrace, (ANTLR3_UINT64)m, (void *)tr, free);
	record = 1;
    }
    return m;
}

ANTLR3_API void
ANTLR3_MEM_REPORT(ANTLR3_BOOLEAN cleanup)
{
    pANTLR3_HASH_ENUM    en;

    char    *addr;
    struct  memrec * tr;
    int	    first;

    reporting	= 1;
    first	= 1;
    en  = antlr3EnumNew(memtrace->table);

    if  (en != NULL)
    {
	first	= 2;

	/* Enumerate all entries */
	while   (en->next(en, &addr, &tr) == ANTLR3_SUCCESS)
	{
	    if	(first == 2)
	    {
		printf("ANTLR3 Found unreleased memory\n");
		printf("==============================\n\n");
		first	= 0;
	    }
	    switch  (tr->type)
	    {
	    case	0:
		printf("    malloc : ");
		break;
	    case	1:
		printf("   realloc : ");
		break;
	    case	2:
		printf("    strdup : ");
		break;
	    default:
		printf(" !UNKNOWN! : ");
		break;
	    }

	    printf("%8I64d bytes at %08I64X  - line %8d of file %s\n",
		    tr->size, addr, tr->line, tr->file);
	}
    }

    en->free(en);

    if	(first == 2)
    {
	printf("ANTLR3 : Congrats - No unreleased memory\n");
	printf("========================================\n\n");
    }
    if	(cleanup)
    {
	memtrace->free(memtrace);
    }
    reporting = 0;
}

#endif
