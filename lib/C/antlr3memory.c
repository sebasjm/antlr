/** \brief ANTLR3 C rutime memory management abstractions
 *
 * Abstracts routines such as malloc into our own fucntion calls
 * such that we can look at faster ways to allocate memory on one
 * system vs another in the future. For now, this just uses
 * malloc()
 */
#include    <antlr3.h>

ANTLR3_API void *
ANTLR3_MALLOC(size_t request)
{
    return  malloc(request);
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