/** \file Default implementaation of CommonTokenStream
 */
#include    <antlr3tokenstream.h>

ANTLR3_API pANTLR3_TOKEN_STREAM
antlr3CommonTokenStreamNew(ANTLR3_UINT32 hint)
{
    pANTLR3_TOKEN_STREAM stream;

    stream  = (pANTLR3_TOKEN_STREAM) ANTLR3_MALLOC(sizeof(ANTLR3_TOKEN_STREAM));

    if	(stream == NULL)
    {
	return	(pANTLR3_TOKEN_STREAM) ANTLR3_ERR_NOMEM;
    }

    stream->tokens  = antlr3ListNew(hint);

    return  stream;
}