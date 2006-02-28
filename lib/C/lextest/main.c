#include    <antlr3.h>
#include    <lextest.h>

int main()
{

    pANTLR3_INPUT_STREAM	    input;
    pANTLR3_COMMON_TOKEN_STREAM	    tstream;
    plextest			    lxr;

    pANTLR3_COMMON_TOKEN	    token;

    input	= antlr3AsciiFileStreamNew("inputtext.txt");

    lxr		= lextestNew(input);

    tstream	= antlr3CommonTokenStreamSourceNew(ANTLR3_SIZE_HINT, lxr->pLexer->tokSource);

    while   ((token = tstream->tstream->LT(tstream->tstream->me, 1))->getType(token) != ANTLR3_TOKEN_EOF)
    {
	printf("Token is %s\n", token->toString(token)->text);
	tstream->tstream->istream->consume(tstream->tstream->istream->me);
    }
}