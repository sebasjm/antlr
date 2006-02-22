/** \brief ANTLR3 C rutime memory management abstractions
 *
 * Abstracts routines such as malloc into our own fucntion calls
 * such that we can look at faster ways to allocate memory on one
 * system vs another in the future. For now, this just uses
 * malloc()
 */
#include    <antlr3defs.h>

ANTLR3_API void *
ANTLR3_MALLOC(size_t request)
{
    return  malloc(request);
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