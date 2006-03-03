#include    <antlr3.h>
#include    <cmqlLexer.h>
#include    <cmql.h>

int main()
{

    pANTLR3_INPUT_STREAM	    input;
    pANTLR3_COMMON_TOKEN_STREAM	    tstream;
    pcmqlLexer			    lxr;
    pcmql			    psr;

    int i;

    for (i=0; i<100; i++)
    {
	printf("i = %d\n", i);
	input	= antlr3AsciiFileStreamNew("inputtext.txt");

	lxr		= cmqlLexerNew(input);
        
	tstream	= antlr3CommonTokenStreamSourceNew(ANTLR3_SIZE_HINT, lxr->pLexer->tokSource);

	psr		= cmqlNew(tstream);
	
	psr->query(psr);

	psr->free(psr);
	tstream->free(tstream);
	lxr->free(lxr);
	input->close(input);
    }

}