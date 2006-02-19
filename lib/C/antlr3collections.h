#ifndef	ANTLR3COLLECTIONS_H
#define	ANTLR3COLLECTIONS_H

ANTLR3_UINT32	antlr3Hash(void * key, ANTLR3_UINT32 keylen);

/** Internal structure representing an element in a hash bucket.
 *  Stores the original key so that duplicate keys can be rejected
 *  if necessary, and contains function can be suported. If the hash key
 *  could be unique I would have invented the perfect compression algorithm ;-)
 */
typedef	struct	ANTLR3_HASH_ENTRY_struct
{
    /** Key that created this particular entry
     */
    pANTLR3_UINT8   key;

    /** Pointer to the data for this particular entry
     */
    void	    * data;

    /** Pointer to the next entry in this bucket if there
     *  is one. Sometimes different keys will hash to the same bucket (especially
     *  if the number of buckets is small). We could implement dual hashing algorithms
     *  to minimize this, but that seesm over the top for what this is needed for.
     */
    struct	ANTLR3_HASH_ENTRY_struct * nextEntry;
}
    ANTLR3_HASH_ENTRY, *pANTLR3_HASH_ENTRY;

/** Internal structure of a hash table bucket, which tracks
 *  all keys that hash to the same bucket.
 */
typedef struct	ANTLR3_HASH_BUCKET_struct
{
    /** Pointer to the first entry in the bucket (if any, it
     *  may be NULL). Duplicate entries are chained from
     * here.
     */
    pANTLR3_HASH_ENTRY	entries;
    
}
    ANTLR3_HASH_BUCKET, pANTLR3_HASH_BUCKET;

/** Structure that tracks a hash table
 */
typedef	struct	ANTLR3_HASH_TABLE_struct
{
    /** Indicates whether the table allows duplicate keys
     */
    int			allowDups;

    /** Number of buckets available in this table
     */

    /* Points to the memory where the array of buckets
     * starts.
     */
    pANTLR3_HASH_BUCKET	buckets;

}
    ANTLR3_HASH_TABLE, * pANTLR3_HASH_TABLE;

#endif