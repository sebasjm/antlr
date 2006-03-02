#include    <antlr3.h>
#include    <cmqlLexer.h>
#include    <cmql.h>

int main()
{

    pANTLR3_INPUT_STREAM	    input;
    pANTLR3_COMMON_TOKEN_STREAM	    tstream;
    pcmqlLexer			    lxr;
    pcmql			    psr;

    input	= antlr3AsciiFileStreamNew("inputtext.txt");

    lxr		= cmqlLexerNew(input);
    
    tstream	= antlr3CommonTokenStreamSourceNew(ANTLR3_SIZE_HINT, lxr->pLexer->tokSource);

    psr		= cmqlNew(tstream);

    psr->query(psr);

}