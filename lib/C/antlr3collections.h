#ifndef	ANTLR3COLLECTIONS_H
#define	ANTLR3COLLECTIONS_H

#include    <antlr3defs.h>



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

    /** Pointer to routine that knows how to release the memory
     *  structure pointed at by data. If this is NULL then we assume
     *  that the data pointer does not need to be freed when the entry
     *  is deleted from the table.
     */
    void	    (*free)(void * data);

    /** Pointer to the next entry in this bucket if there
     *  is one. Sometimes different keys will hash to the same bucket (especially
     *  if the number of buckets is small). We could implement dual hashing algorithms
     *  to minimize this, but that seems over the top for what this is needed for.
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
    ANTLR3_HASH_BUCKET, *pANTLR3_HASH_BUCKET;

/** Structure that tracks a hash table
 */
typedef	struct	ANTLR3_HASH_TABLE_struct
{
    /** Indicates whether the table allows duplicate keys
     */
    int			allowDups;

    /** Number of buckets available in this table
     */
    ANTLR3_UINT32	modulo;

    /** Points to the memory where the array of buckets
     * starts.
     */
    pANTLR3_HASH_BUCKET	buckets;

    /** How many elements currently exist in the table.
     */
    ANTLR3_UINT64	count;

}
    ANTLR3_HASH_TABLE, * pANTLR3_HASH_TABLE;


/** Internal structure representing an enumeration of a table.
 *  This is returned by antlr3Enumeration()
 *  Allows the programmer to traverse the table in hash order without 
 *  knowing what is in the actual table.
 *
 *  Note that it is up to the caller to ensure that the table
 *  structure does not change in the hash bucket that is currently being
 *  enumerated as this structure just tracks the next pointers in the
 *  bucket series.
 */
typedef struct	ANTLR3_HASH_ENUM_struct
{
    /* Pointer to the table we are enumerating
     */
    pANTLR3_HASH_TABLE	table;

    /* Bucket we are currently enumerating (if NULL then we are done)
     */
    ANTLR3_UINT32	bucket;

    /* Next entry to return, if NULL, then move to next bucket if any
     */
    pANTLR3_HASH_ENTRY	entry;
}
    ANTLR3_HASH_ENUM, *pANTLR3_HASH_ENUM;

ANTLR3_API  pANTLR3_HASH_TABLE	      antlr3NewHashTable(ANTLR3_UINT32 sizeHint);
ANTLR3_API  ANTLR3_UINT32	      antlr3Hash	(void * key, ANTLR3_UINT32 keylen);
ANTLR3_API  void		    * antlr3HashGet	(pANTLR3_HASH_TABLE table, void * key);
ANTLR3_API  int			      antlr3HashPut	(pANTLR3_HASH_TABLE table, void * key, void * element, void (*free)(void *));
ANTLR3_API  void		      antlr3HashFree	(pANTLR3_HASH_TABLE table);
ANTLR3_API  void		      antlr3HashDelete	(pANTLR3_HASH_TABLE table, void * key);
ANTLR3_API  pANTLR3_HASH_ENUM	      antlr3EnumNew	(pANTLR3_HASH_TABLE table);
ANTLR3_API  int			      antlr3EnumNext	(pANTLR3_HASH_ENUM en, void ** key, void ** data);
ANTLR3_API  void		      antlr3EnumFree	(pANTLR3_HASH_ENUM en);

#endif