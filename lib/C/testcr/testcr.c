#include    <antlr3.h>

void myfree(void * p)
{
    free(p);
}

#define	TEST_HASH_COUNT	10000
#define	TEST_HASH_SIZE	3011

int main()
{
    pANTLR3_INPUT_STREAM input;
    int	i;
    ANTLR3_UINT8	    key[256];
    pANTLR3_HASH_TABLE	ht;

    pANTLR3_HASH_ENUM	en;

    ANTLR3_UINT32	c;

    unsigned char   * retkey;
    void	    * retdata;

    input   = antlr3NewAsciiFileStream("C:/iscsrc/users/5.1.mv/modules/Antlr/mvindex/src/mvindexcommands.g");

    while   ((c = input->LA(input, 1)) != ANTLR3_CHARSTREAM_EOF)
    {
	input->consume(input);
	printf("%c", c);
    }
    
    antlr3InputClose(input);

    ht	= antlr3NewHashTable(TEST_HASH_SIZE);

    if	(ht >= 0)
    {
	for (i=0; i<TEST_HASH_COUNT; i++)
	{
	    sprintf(key, "%d", i);
	    antlr3HashPut(ht, key, (void *)strdup(key), myfree);
	    if	((i % (TEST_HASH_COUNT/42)) == 0)
	    {
		printf("Key added: %s\n", key);
	    }
	}

	en  = antlr3EnumNew(ht);

	if  (en != NULL)
	{
	    i	= 0;
	    /* Enumerate all entries */
	    while   (antlr3EnumNext(en, &retkey, &retdata) == ANTLR3_SUCCESS)
	    {
		if  ((i % (TEST_HASH_COUNT/42)) == 0)
		{
		    printf("Enumerated key %s\n", retkey);
		}
		i++;
	    }
	}

	for (i=0; i<TEST_HASH_COUNT; i++)
	{
	    sprintf(key, "%d", i);
	    retkey = antlr3HashGet(ht, key);

	    if	((i % (TEST_HASH_COUNT/42)) == 0)
	    {
		printf("Key found: %s\n", retkey);
	    }
	}

	for (i=TEST_HASH_COUNT-1; i >= 0; i--)
	{
	    sprintf(key, "%d", i);
	    antlr3HashDelete(ht, key);
	    if	((i % (TEST_HASH_COUNT/42)) == 0)
	    {
		printf("Key deleted: %s\n", key);
	    }
	}
	
	for (i=0; i<TEST_HASH_COUNT; i++)
	{
	    sprintf(key, "%d", i);
	    antlr3HashPut(ht, key, (void *)strdup(key), myfree);
	    if	((i % (TEST_HASH_COUNT/42)) == 0)
	    {
		printf("Key added: %s\n", key);
	    }
	}

	antlr3HashFree(ht);
    }
    return 0;
}