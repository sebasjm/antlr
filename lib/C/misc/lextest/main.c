#include    <antlr3.h>
#include    <cmql.h>
#include    <cmqlLexer.h>
#include    <cmqlTree.h>

int main( int argc, char *argv[ ])
{

	pANTLR3_INPUT_STREAM		    input;
	pANTLR3_COMMON_TOKEN_STREAM	    tstream;
	pANTLR3_COMMON_TREE_NODE_STREAM	    nodes;
	pcmql				    psr;
	pcmqlLexer			    lxr;
	pcmqlTree			    walker;
	pANTLR3_STRING			    jobno;
	
	cmql_query_return		    synError;

	pANTLR3_STRING		    treestr;

	if	(argc < 2)
	{
	    fprintf(stderr, "Usage: cmqltest filename\n");
	    exit(7);
	}

	input	= antlr3AsciiFileStreamNew(argv[1]);

	if  (	(input == (pANTLR3_INPUT_STREAM)ANTLR3_ERR_NOFILE))
	{
	    fprintf(stderr, "Unable to open input file!\n");
	    exit(6);
	}
	if  (	(input == (pANTLR3_INPUT_STREAM)ANTLR3_ERR_NOMEM))
	{
	    fprintf(stderr, "Unable to allocate memory for input stream!\n");
	    exit(1);
	}
	lxr		= cmqlLexerNew(input);

	if  (lxr == (pcmqlLexer)ANTLR3_ERR_NOMEM)
	{
	    fprintf(stderr, "Unable to allocate memory for lexer!\n");
	    exit(2);
	}

	tstream	= antlr3CommonTokenStreamSourceNew(ANTLR3_SIZE_HINT, lxr->pLexer->tokSource);

	if  (tstream == (pANTLR3_COMMON_TOKEN_STREAM)ANTLR3_ERR_NOMEM)
	{
	    fprintf(stderr, "Unable to allocate memory for token stream!\n");
	    exit(3);
	}

	psr		= cmqlNew(tstream);

	if  (psr == (pcmql)ANTLR3_ERR_NOMEM)
	{
	    fprintf(stderr, "Unable to allocate memory for parser!\n");
	    exit(4);
	}

	printf("parsing to tree...\n");
	synError	= psr->query(psr);

	if  (synError.error == ANTLR3_TRUE)
	{
	    fprintf(stderr, "Syntax error parsing the query\n");
	}

	treestr = synError.tree->toStringTree(synError.tree);

	printf("%s\n", treestr->text);

	nodes	    = antlr3CommonTreeNodeStreamNewTree(synError.tree, ANTLR3_SIZE_HINT);
        walker	    = cmqlTreeNew(nodes);

	walker->query(walker, jobno);

	tstream->free(tstream);
	psr->free(psr);
	lxr->free(lxr);
	input->close(input);

	ANTLR3_MEM_REPORT(ANTLR3_TRUE);
 
}