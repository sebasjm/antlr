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
    ANTLR3_HASH_ENTRY;

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
    ANTLR3_HASH_BUCKET;

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

    /** Pointer to function to completely delete this table
     */
    void		(*free)	    (struct ANTLR3_HASH_TABLE_struct * table);
    void		(*del)	    (struct ANTLR3_HASH_TABLE_struct * table, void * key);
    pANTLR3_HASH_ENTRY	(*remove)   (struct ANTLR3_HASH_TABLE_struct * table, void * key);
    void *		(*get)	    (struct ANTLR3_HASH_TABLE_struct * table, void * key);
    ANTLR3_INT32	(*put)	    (struct ANTLR3_HASH_TABLE_struct * table, void * key, void * element, void (*freeptr)(void *));
    ANTLR3_UINT64	(*size)	    (struct ANTLR3_HASH_TABLE_struct * table);
}
    ANTLR3_HASH_TABLE;


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

    /* Interface
     */
    int		(*next)	    (struct ANTLR3_HASH_ENUM_struct * en, void ** key, void ** data);
    void	(*free)	    (struct ANTLR3_HASH_ENUM_struct * table);
}
    ANTLR3_HASH_ENUM;

/** Structure that represents a LIST collection
 */
typedef	struct	ANTLR3_LIST_struct
{
    /** Hash table that is storing the list elements
     */
    pANTLR3_HASH_TABLE	table;

    void	    (*free)	(struct ANTLR3_LIST_struct * list);
    void	    (*del)	(struct ANTLR3_LIST_struct * list, ANTLR3_UINT64 key);
    void *	    (*get)	(struct ANTLR3_LIST_struct * list, ANTLR3_UINT64 key);
    void *	    (*remove)	(struct ANTLR3_LIST_struct * list, ANTLR3_UINT64 key);
    ANTLR3_INT32    (*add)	(struct ANTLR3_LIST_struct * list, void * element, void (*freeptr)(void *));
    ANTLR3_INT32    (*put)	(struct ANTLR3_LIST_struct * list, ANTLR3_UINT64 key, void * element, void (*freeptr)(void *));
    ANTLR3_UINT64   (*size)	(struct ANTLR3_LIST_struct * list);
    
}
    ANTLR3_LIST;

/** Structure that represents a Stack collection
 */
typedef	struct	ANTLR3_STACK_struct
{
    /** List that supports the stack structure
     */
    pANTLR3_LIST    list;

    /** Used for quick access to the top of the stack
     */
    void *	    top;
    void	    (*free)	(struct ANTLR3_STACK_struct * stack);
    void *	    (*pop)	(struct ANTLR3_STACK_struct * stack);
    void *	    (*get)	(struct ANTLR3_STACK_struct * stack, ANTLR3_UINT64 key);
    ANTLR3_BOOLEAN  (*push)	(struct ANTLR3_STACK_struct * stack, void * element, void (*freeptr)(void *));
    ANTLR3_UINT64   (*size)	(struct ANTLR3_STACK_struct * stack);
    void *	    (*peek)	(struct ANTLR3_STACK_struct * stack);

}
    ANTLR3_STACK;

/* Structure that represents a vector element
 */
typedef struct ANTLR3_VECTOR_ELEMENT_struct
{
    void    * element;
    void (*freeptr)(void *);
}
    ANTLR3_VECTOR_ELEMENT, *pANTLR3_VECTOR_ELEMENT;

/* Structure that represents a vector collection. A vector is a simple list
 * that contains a pointer to the element and a pointer to a function that
 * that can free the element if it is removed. It auto resizes but does not
 * use hash techniques as it is referenced by a simple numeric index. It is not a 
 * sparse list, so if any element is deleted, then the ones following are moved
 * down in memory and the count is adjusted.
 */
typedef struct ANTLR3_VECTOR_struct
{
    /** Array of pointers to vector elements
     */
    pANTLR3_VECTOR_ELEMENT  elements;

    /** Number of entries currently in the list;
     */
    ANTLR3_UINT64   count;

    /** Indicates if the structure was made by a factory, in which
     *  case only the factory can free the memory for the actual vector,
     *  though the vector free function is called and wil recurse through its
     *  entries calling any free pointers for each entry.
     */
    ANTLR3_BOOLEAN  factoryMade;

    /** Total number of entires in elements at any point in time
     */
    ANTLR3_UINT64   elementsSize;

    void	    (*free)	(struct ANTLR3_VECTOR_struct * vector);
    void	    (*del)	(struct ANTLR3_VECTOR_struct * vector, ANTLR3_UINT64 entry);
    void *	    (*get)	(struct ANTLR3_VECTOR_struct * vector, ANTLR3_UINT64 entry);
    void *	    (*remove)	(struct ANTLR3_VECTOR_struct * vector, ANTLR3_UINT64 entry);
    ANTLR3_INT32    (*add)	(struct ANTLR3_VECTOR_struct * vector, void * element, void (*freeptr)(void *));
    ANTLR3_INT32    (*put)	(struct ANTLR3_VECTOR_struct * vector, ANTLR3_UINT64 entry, void * element, void (*freeptr)(void *));
    ANTLR3_UINT64   (*size)	(struct ANTLR3_VECTOR_struct * vector);
}
    ANTLR3_VECTOR;

/** Structure that tracks vectors in a vector and auto deletes the vectors
 *  in the vector factory when closed.
 */
typedef struct ANTLR3_VECTOR_FACTORY_struct
{
    /** Vector of all the vectors created so far
     */
    pANTLR3_VECTOR  vectors;

    /** Function to close the vector factory
     */
    void	    (*close)	    (struct ANTLR3_VECTOR_FACTORY_struct * factory);

    /** Function to supply a new vector
     */
    pANTLR3_VECTOR  (*newVector)    (struct ANTLR3_VECTOR_FACTORY_struct * factory);

}
    ANTLR3_VECTOR_FACTORY; 

#endif

