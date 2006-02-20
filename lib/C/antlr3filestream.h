#ifndef	_ANTLR3_FILESTREAM_H
#define	_ANTLR3_FILESTREAM_H

#include    <antlr3defs.h>
#include    <antlr3input.h>

ANTLR3_API  pANTLR3_INPUT_STREAM    antlr3NewAsciiFileStream    (pANTLR3_UINT8 fileName);
	    ANTLR3_FDSC		    antlr3Fopen			(pANTLR3_UINT8 filename, const char * mode);
	    size_t		    antlr3Fsize			(pANTLR3_UINT8 filename);
	    int			    antlr3readAscii		(pANTLR3_INPUT_STREAM input);
	    size_t		    antlr3Fread			(ANTLR3_FDSC fdsc, size_t count,  void * data);

#endif