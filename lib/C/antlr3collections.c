/** \file
 * Provides a number of useful functions that are roughly equivalent
 * to java HashTable and List for the purposes of Antlr 3 C runtime.
 * Also useable by the C programmer for things like symbol tables pointers
 * and so on.
 *
 */
#include    <antlr3.h>

pANTLR3_HASHTABLE
antlr3NewHashTable(ANTLR3_UINT32 sizeHint)
{
    /* All we have to do is create the hashtable tracking structure
     * and allocate memory for the requested number of buckets.
     */
    pANTLR3_HASHTABLE	table;

    table   = ANTLR3_MALLOC(sizeof(ANTLR3_HASHTABLE));

    /* Error out if no memory left */
    if	(table	== NULL)
    {
	return	(pANTLR3_HASHTABLE) ANTLR3_ERR_NOMEM;
    }

    /* Allocate memory for the buckets
     */
    table->

}

/** Given an input key of arbitrary length, return a hash value
 *  it. This can then be used (with suitable modulo) to index other
 *  structures.
 */
ANTLR3_UINT32
antlr3Hash(void * key, ANTLR3_UINT32 keylen)
{
    /* Accumulate the hash value of the key
     */
    ANTLR3_UINT32   hash;
    pANTLR3_UNINT8  keyPtr;

    hash    = 0;
    keyPtr  = (ANTLR3_UNINT8) key;

    /* Iterate the key and accumulate the hash
     */
    while(keylen > 0)
    {
	hash = (hash << 4) + (*(keyPtr++));

	if ((i1=hash&0xf0000000) != 0)
	{
		hash = hash ^ (i1 >> 24);
		hash = hash ^ i1;
	}
	keylen--;
    }
}