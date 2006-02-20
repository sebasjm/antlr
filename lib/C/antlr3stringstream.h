#ifndef	_ANTLR3_STRINGSTREAM_H
#define	_ANTLR3_STRINGSTREAM_H

#include    <antlr3defs.h>
#include    <antlr3input.h>

pANTLR3_INPUT_STREAM	antlr3NewAsciiStringInPlaceStream   (pANTLR3_UINT8 inString);
pANTLR3_INPUT_STREAM	antlr3NewAsciiStringCopyStream	    (pANTLR3_UINT8 inString);

#endif