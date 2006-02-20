#ifndef	_ANTLR3MEMORY_H
#define	_ANTLR3MEMORY_H

#include    <antlr3defs.h>

ANTLR3_API void		  * ANTLR3_MALLOC(size_t request);
ANTLR3_API void		    ANTLR3_FREE(void * ptr);
ANTLR3_API pANTLR3_UINT8    ANTLR3_STRDUP(pANTLR3_UINT8 instr);
ANTLR3_API void		  * ANTLR3_MEMMOVE(void * target, const void * source, ANTLR3_UINT64 size);

#endif	/* _ANTLR3MEMORY_H */