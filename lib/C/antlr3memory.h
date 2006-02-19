#ifndef	_ANTLR3MEMORY_H
#define	_ANTLR3MEMORY_H

#include    <antlr3defs.h>

ANTLR3_API void		* ANTLR3_MALLOC(size_t request);
ANTLR3_API void		  ANTLR3_FREE(void * ptr);
ANTLR3_API pANTLR3_INT8	  ANTLR3_STRDUP(pANTLR3_INT8 instr)

#endif	/* _ANTLR3MEMORY_H */