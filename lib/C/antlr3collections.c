/** \file
 * Provides a number of useful functions that are roughly equivalent
 * to java HashTable and List for the purposes of Antlr 3 C runtime.
 * Also useable by the C programmer for things like symbol tables pointers
 * and so on.
 *
 */
#include    <antlr3.h>

void
antlr3HashFree(pANTLR3_HASH_TABLE table)
{
    ANTLR3_UINT32	bucket;	/* Used to traverse the buckets	*/

    pANTLR3_HASH_BUCKET	thisBucket;
    pANTLR3_HASH_ENTRY	entry;
    pANTLR3_HASH_ENTRY	nextEntry;

    /* Free the table, all buckets and all entries, and all the
     * keys and data (if the table exists)
     */
    if	(table	!= NULL)
    {
	for	(bucket = 0; bucket < table->modulo; bucket++)
	{
	    thisBucket	= &table->buckets[bucket];

	    /* Allow sparse tables, though we don't create them as such at present
	     */
	    if	( thisBucket != NULL)
	    {
		entry	= thisBucket->entries;

		/* Search all entries in the bucket and free them up
		 */
		while	(entry != NULL)
		{
		    /* Save next entry - we do not want to access memory in entry after we
		     * have freed it.
		     */
		    nextEntry	= entry->nextEntry;

		    /* Free any data pointer, this means that anythign placed in the
		     * table must be freeable directly, and not contain any pointes that 
		     * could be orphaned. Perhaps, if this becomes an issue later, I could make the data pointers 
		     * actually be an abstract structure pointer, within which there is a pointer
		     * to a function that knows how to free the real data within it.
		     */
		    if	(entry->data != NULL)
		    {
			ANTLR3_FREE(entry->data);
		    }

		    /* Free the key memory - we know that we allocated this
		     */
		    if	(entry->key != NULL)
		    {
			ANTLR3_FREE(entry->key);
		    }

		    /* Free this entry
		     */
		    ANTLR3_FREE(entry);
		    entry   = nextEntry;    /* Load next pointer to see if we shoud free it */
		}

		/* Now we can free this bucket's memory
		 */
		ANTLR3_FREE(thisBucket);
		thisBucket = NULL;
	    }
	}
    }
}
pANTLR3_HASH_TABLE
antlr3NewHashTable(ANTLR3_UINT32 sizeHint)
{
    /* All we have to do is create the hashtable tracking structure
     * and allocate memory for the requested number of buckets.
     */
    pANTLR3_HASH_TABLE	table;
    
    ANTLR3_UINT32	bucket;	/* Used to traverse the buckets	*/

    table   = ANTLR3_MALLOC(sizeof(ANTLR3_HASH_TABLE));

    /* Error out if no memory left */
    if	(table	== NULL)
    {
	return	(pANTLR3_HASH_TABLE) ANTLR3_ERR_NOMEM;
    }

    /* Allocate memory for the buckets
     */
    table->buckets = (pANTLR3_HASH_BUCKET) ANTLR3_MALLOC((size_t) (sizeof(ANTLR3_HASH_BUCKET) * sizeHint)); 

    if	(table->buckets == NULL)
    {
	ANTLR3_FREE((void *)table);
	return	(pANTLR3_HASH_TABLE) ANTLR3_ERR_NOMEM;
    }

    table->modulo   = sizeHint;

    /* Initialize the buckets to empty
     */
    for	(bucket = 0; bucket < sizeHint; bucket++)
    {
	table->buckets[bucket].entries = NULL;
    }

    /* Exclude duplicate entries by default
     */
    table->allowDups	= ANTLR3_FALSE;

    return  table;
}

/** Return the element pointer in the hash table for a particular
 *  key value, or NULL if it don't exist (or was itself NULL).
 */
void *
antlr3HashGet(pANTLR3_HASH_TABLE table, void * key)
{
    ANTLR3_UINT32	    hash;
    pANTLR3_HASH_BUCKET	    bucket;
    pANTLR3_HASH_ENTRY	    entry;

    /* First we need to know the hash of the provided key
     */
    hash    = antlr3Hash(key, (ANTLR3_UINT32)strlen((const char *)key));

    /* Knowing the hash, we can find the bucket
     */
    bucket  = table->buckets + (hash % table->modulo);

    /* Now we can inspect the key at each entry in the bucket
     * and see if we have a match.
     */
    entry   = bucket->entries;

    while   (entry != NULL)
    {
	if  (strcmp((const char *)key, (const char *)entry->key) == 0)
	{
	    /* Match was found, return the data pointer for this entry
	     */
	    return  entry->data;
	}
    }

    /* If we got here, then we did no find the key
     */
    return  NULL;
}

/** Add the element pointer in to the table, based upon the 
 *  hash of the provided key.
 */
int
antlr3HashPut(pANTLR3_HASH_TABLE table, void * key, void * element)
{
    ANTLR3_UINT32	    hash;
    pANTLR3_HASH_BUCKET	    bucket;
    pANTLR3_HASH_ENTRY	    entry;
    pANTLR3_HASH_ENTRY	    * newPointer;

    /* First we need to know the hash of the provided key
     */
    hash    = antlr3Hash(key, (ANTLR3_UINT32)strlen((const char *)key));

    /* Knowing the hash, we can find the bucket
     */
    bucket  = table->buckets + (hash % table->modulo);

    /* Knowign the bucket, we can traverse the entries until we
     * we find a NULL pointer ofr we find that this is already 
     * in the table and duplicates were not allowed.
     */
    newPointer	= &bucket->entries;

    while   (*newPointer !=  NULL)
    {
	/* The value at new pointer is pointing to an existing entry.
	 * If duplicates are allowed then we don't care what it is, but
	 * must reject this add if the key is the same as the one we are
	 * supplied with.
	 */
	if  (table->allowDups == ANTLR3_FALSE)
	{
	    if	(strcmp((const char*) key, (const char *)(*newPointer)->key) == 0)
	    {
		return	ANTLR3_ERR_HASHDUP;
	    }
	}

	/* Point to the next entry pointer of the current entry we
	 * are traversing, if it is NULL we will create our new
	 * structure and point this to it.
	 */
	newPointer = &((*newPointer)->nextEntry);
    }

    /* newPointer is now poiting at the pointer where we need to
     * add our new entry, so let's crate the entry and add it in.
     */
    entry   = ANTLR3_MALLOC((size_t)sizeof(ANTLR3_HASH_ENTRY));

    if	(entry == NULL)
    {
	return	ANTLR3_ERR_NOMEM;
    }
	
    entry->data		= element;		/* Install the data element supplied		    */
    entry->key		= ANTLR3_STRDUP(key);	/* Record the key value				    */
    entry->nextEntry	= NULL;			/* Ensure that the forward pointer ends the chain   */

    *newPointer	= entry;    /* Install the next entry in this bucket	*/

    return  ANTLR3_SUCCESS;
}

/** Given an input key of arbitrary length, return a hash value of
 *  it. This can then be used (with suitable modulo) to index other
 *  structures.
 */
ANTLR3_UINT32
antlr3Hash(void * key, ANTLR3_UINT32 keylen)
{
    /* Accumulate the hash value of the key
     */
    ANTLR3_UINT32   hash;
    pANTLR3_UINT8   keyPtr;
    ANTLR3_UINT32   i1;

    hash    = 0;
    keyPtr  = (pANTLR3_UINT8) key;

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

    return  hash;
}