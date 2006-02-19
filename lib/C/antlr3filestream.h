#ifndef	_ANTLR3_FILESTREAM_H
#define	_ANTLR3_FILESTREAM_H

#include    <antlr3defs.h>
#include    <antlr3input.h>

pANTLR3_INPUT_STREAM	antlr3NewAsciiFileStream    (pANTLR3_INT8 fileName);
ANTLR3_FDSC		antlr3Fopen		    (const char * filename, const char * mode);
ANTLR3_UINT32		antlr3Fsize		    (pANTLR3_INT8 filename);
int			antlr3readAscii		    (pANTLR3_INPUT_STREAM    input)
#endif