#include    <antlr3.h>
#include    <lextestLexer.h>
#include    <lextest.h>

int main()
{

    pANTLR3_INPUT_STREAM	    input;
    pANTLR3_COMMON_TOKEN_STREAM	    tstream;
    plextestLexer		    lxr;
    plextest			    psr;

    input	= antlr3AsciiFileStreamNew("inputtext.txt");

    lxr		= lextestLexerNew(input);

    tstream	= antlr3CommonTokenStreamSourceNew(ANTLR3_SIZE_HINT, lxr->pLexer->tokSource);

    psr		= lextestNew(tstream);

    psr->creates(psr);

}