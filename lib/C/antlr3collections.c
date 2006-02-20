/** \file
 * Provides a number of useful functions that are roughly equivalent
 * to java HashTable and List for the purposes of Antlr 3 C runtime.
 * Also useable by the C programmer for things like symbol tables pointers
 * and so on.
 *
 */
#include    <antlr3.h>

/* Local function to advance enumeration structure pointers
 */
static void antlr3EnumNextEntry(pANTLR3_HASH_ENUM en);

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
	    thisBucket	= &(table->buckets[bucket]);

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

		    /* Free any data pointer, this only happens if the user supplied
		     * a pointer to a routine that knwos how to free the structure they
		     * added to the table.
		     */
		    if	(entry->free != NULL)
		    {
			entry->free(entry->data);
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
		/* Invalidate the current pointer
		 */
		thisBucket->entries = NULL;
	    }
	}

	/* Now we can free the bucket memory
	 */
	ANTLR3_FREE(table->buckets);
    }

    /* Now we free teh memory for the table itself
     */
    ANTLR3_FREE(table);
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

    /* Modulo of the table, (bucket count).
     */
    table->modulo   = sizeHint;

    table->count    = 0;	    /* Nothing in there yet ( I hope)	*/

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

/** Remove the element in the hash table for a particular
 *  key value, if it exists - no error if it does not.
 */
void 
antlr3HashDelete(pANTLR3_HASH_TABLE table, void * key)
{
    ANTLR3_UINT32	    hash;
    pANTLR3_HASH_BUCKET	    bucket;
    pANTLR3_HASH_ENTRY	    entry;
    pANTLR3_HASH_ENTRY	    * nextPointer;

    /* First we need to know the hash of the provided key
     */
    hash    = antlr3Hash(key, (ANTLR3_UINT32)strlen((const char *)key));

    /* Knowing the hash, we can find the bucket
     */
    bucket  = table->buckets + (hash % table->modulo);

    /* Now, we traverse the entries in the bucket until
     * we find the key or the end of the entires in the bucket. 
     * We track the element prior to the one we are exmaining
     * as we need to set its next pointer to the next pointer
     * of the entry we are deleting (if we find it).
     */
    entry	    =   bucket->entries;    /* Entry to examine					    */
    nextPointer	    = & bucket->entries;    /* Where to put the next pointer of the deleted entry   */

    while   (entry != NULL)
    {
	/* See if this is the entry we wish to delete
	 */
	if  (strcmp((const char *)key, (const char *)entry->key) == 0)
	{
	    /* It was the correct entry, so we set the next pointer
	     * of the previous entry to the next pointer of this
	     * located one, which takes it out of the chain.
	     */
	    (*nextPointer)		= entry->nextEntry;

	    /* Now we can free the elements and the entry in order
	     */
	    if	(entry->free != NULL)
	    {
		/* Call programmer supplied function to release this entry
		 */
		entry->free(entry->data);
		entry->data = NULL;
	    }

	    /* Release the key - we allocated that
	     */
	    ANTLR3_FREE(entry->key);
	    entry->key	= NULL;

	    /* Finally release the space for this entry block.
	     */
	    ANTLR3_FREE(entry);

	    /* Signal the end	*/
	    entry   = NULL;

	    table->count--;
	}
	else
	{
	    /* We found an entry but it wasn't the one that was wanted, so
	     * move to the next one, if any.
	     */
	    nextPointer	= & (entry->nextEntry);	    /* Address of the next pointer in the current entry	    */
	    entry	= entry->nextEntry;	    /* Address of the next element in the bucket (if any)   */
	}
    }
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
	entry = entry->nextEntry;
    }

    /* If we got here, then we did no find the key
     */
    return  NULL;
}

/** Add the element pointer in to the table, based upon the 
 *  hash of the provided key.
 */
int
antlr3HashPut(pANTLR3_HASH_TABLE table, void * key, void * element, void (*free)(void *))
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
    entry->free		= free;			/* Function that knows how to release the entry	    */
    entry->key		= ANTLR3_STRDUP(key);	/* Record the key value				    */
    entry->nextEntry	= NULL;			/* Ensure that the forward pointer ends the chain   */

    *newPointer	= entry;    /* Install the next entry in this bucket	*/

    table->count++;

    return  ANTLR3_SUCCESS;
}

/** \brief Creates an enumeration structure to traverse the hash table.
 *
 * \param table Table to enumerate
 * \return Pointer to enumeration structure.
 */
pANTLR3_HASH_ENUM
antlr3EnumNew	(pANTLR3_HASH_TABLE table)
{
    pANTLR3_HASH_ENUM	en;

    /* Allocate structure memory
     */
    en    = (pANTLR3_HASH_ENUM) ANTLR3_MALLOC((size_t)sizeof(ANTLR3_HASH_ENUM));

    /* Check that the allocation was good 
     */
    if	(en == NULL)
    {
	return	(pANTLR3_HASH_ENUM) ANTLR3_ERR_NOMEM;
    }
    
    /* Initialize the start pointers
    */
    en->table	= table;
    en->bucket	= 0;				/* First bucket		    */
    en->entry	= en->table->buckets->entries;	/* First entry to return    */

    /* Special case in that the first bucket may not have anything in it
     * but the antlr3EnumNext() function expects that the en->entry is
     * set to the next valid pointer. Hence if it is not a valid element
     * pointer, attempt to find the next one that is, (table may be empty
     * of course.
     */
    if	(en->entry == NULL)
    {
	antlr3EnumNextEntry(en);
    }

    /* All is good
     */
    return  en;
}

/** \brief Return the next entry in the hashtable ebign traversed by the supplied
 *         enumeration.
 *
 * \param[in] en Pointer to the enumeration tracking structure
 * \param key	 Pointer to void pointer, where the key pointer is returned.
 * \param data	 Pointer to void pointer where the data poitner is returned.
 * \return 
 *	- ANTLR3_SUCCESS if there was a next key
 *	- ANTLR3_FAIL	 if there were no more keys
 *
 * \remark
 *  No checking of input structure is performed!
 */
int
antlr3EnumNext	(pANTLR3_HASH_ENUM en, void ** key, void ** data)
{
    /* If the current entry is valid, then use it
     */
    if  (en->bucket >= en->table->modulo)
    {
        /* Already exhausted the table
         */
        return	ANTLR3_FAIL;
    }

    /* Pointers are already set to the current entry to return, or
     * we would not be at this point in the logic flow.
     */
    *key	= en->entry->key;
    *data	= en->entry->data;

    /* Return pointers are set up, so now we move the element
     * pointer to the next in the table (if any).
     */
    antlr3EnumNextEntry(en);

    return	ANTLR3_SUCCESS;
}

/** \brief Local function to avance the entry pointer of an enumeration 
 * structure to the next vlaid entry (if there is one).
 *
 * \param[in] enum Pointer to ANTLR3 enumeratio structure returned by antlr3EnumNew()
 *
 * \remark
 *   - The function always leaves the pointers pointing at a valid enrty if there
 *     is one, so if the entry pointer is NULL when this function exits, there were
 *     no more entries in the table.
 */
static void
antlr3EnumNextEntry(pANTLR3_HASH_ENUM en)
{
    pANTLR3_HASH_BUCKET	bucket;

    /* See if the current entry pointer is valid fisrt of all
     */
    if	(en->entry != NULL)
    {
	/* Current entry was a vlaid point, see if ther eis another
	 * one in the chain.
	 */
	if  (en->entry->nextEntry != NULL)
	{
	    /* Next entry in the enumeration is just the next entry
	     * in the chain.
	     */
	    en->entry = en->entry->nextEntry;
	    return;
	}
    }

    /* There were no more entries in the current bucket, if there are
     * more buckets then chase them until we find an entry.
     */
    en->bucket++;

    while   (en->bucket < en->table->modulo)
    {
	/* There was one more bucket, see if it has any elements in it
	 */
	bucket	= en->table->buckets + en->bucket;

	if  (bucket->entries != NULL)
	{
	    /* There was an entry in this bucket, so we can use it
	     * for the next entry in the enumeration.
	     */
	    en->entry	= bucket->entries;
	    return;
	}

	/* There was nothing in the bucket we just examined, move to the
	 * next one.
	 */
	en->bucket++;
    }

    /* Here we have exhausted all buckets and the enumeration pointer will 
     * have its bucket count = table->modulo whicih signifies that we are done.
     */
}

/** \brief Frees up the memory structures that represent a hash table
 *  enumeration.
 * \param[in] enum Pointer to ANTLR3 enumeratio structure returned by antlr3EnumNew()
 */
void
antlr3EnumFree	(pANTLR3_HASH_ENUM en)
{
    /* Nothing to check, we just free it.
     */
    ANTLR3_FREE(en);
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