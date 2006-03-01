#include    <antlr3.h>
#include    <lex1.h>

int main()
{

    pANTLR3_INPUT_STREAM	    input;
    pANTLR3_COMMON_TOKEN_STREAM	    tstream;
    plex1			    lxr;

    pANTLR3_COMMON_TOKEN	    token;

    input	= antlr3AsciiFileStreamNew((pANTLR3_UINT8)"c:/antlrsrc/code/antlr/main/lib/C/lextest/inputtext.txt");

    if	(input == (pANTLR3_INPUT_STREAM)ANTLR3_ERR_NOMEM)
    {
	fprintf(stderr, "Unable to allocate memory for the input stream");
	exit(1);
    }

    if	(input == (pANTLR3_INPUT_STREAM)ANTLR3_ERR_NOFILE)
    {
	fprintf(stderr, "File not found");
	exit(2);
    }

    lxr		= lex1New(input);

    tstream	= antlr3CommonTokenStreamSourceNew(ANTLR3_SIZE_HINT, lxr->pLexer->tokSource);

    while   ((token = tstream->tstream->LT(tstream->tstream->me, 1))->getType(token) != ANTLR3_TOKEN_EOF)
    {
	printf("Token is %s\n", token->toString(token)->text);
	tstream->tstream->istream->consume(tstream->tstream->istream->me);
    }
}