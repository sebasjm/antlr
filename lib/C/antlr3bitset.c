/**
 * \file
 * Contains the C implementation of ANTLR3 bitsets as adapted from Terence Parr's
 * Java implementation.
 */

#include    <antlr3.h>

// External interface
//

static	pANTLR3_BITSET  antlr3BitsetClone	(pANTLR3_BITSET inSet);
static	pANTLR3_BITSET  antlr3BitsetOR		(pANTLR3_BITSET bitset1, pANTLR3_BITSET bitset2);
static	void		antlr3BitsetORInPlace	(pANTLR3_BITSET bitset, pANTLR3_BITSET bitset2);
static	ANTLR3_UINT32	antlr3BitsetSize	(pANTLR3_BITSET bitset);
static	void		antlr3BitsetAdd		(pANTLR3_BITSET bitset, ANTLR3_INT32 bit);
static	ANTLR3_BOOLEAN	antlr3BitsetEquals	(pANTLR3_BITSET bitset1, pANTLR3_BITSET bitset2);
static	ANTLR3_BOOLEAN	antlr3BitsetMember	(pANTLR3_BITSET bitset, ANTLR3_UINT32 bit);
static	ANTLR3_UINT32	antlr3BitsetNumBits	(pANTLR3_BITSET bitset);
static	void		antlr3BitsetRemove	(pANTLR3_BITSET bitset, ANTLR3_UINT32 bit);
static	ANTLR3_BOOLEAN	antrl3BitsetIsNil	(pANTLR3_BITSET bitset);

// Local functions
//
static	void		growToInclude	(pANTLR3_BITSET bitset, ANTLR3_INT32 bit);
static	ANTLR3_UINT64	bitMask		(ANTLR3_UINT32 bitNumber);
static	ANTLR3_UINT32	numWordsToHold	(ANTLR3_UINT32 bit);
static	ANTLR3_UINT32	wordNumber	(ANTLR3_UINT32 bit);

ANTLR3_API void
antlr3BitsetFree(pANTLR3_BITSET bitset)
{
    if	(bitset->bits != NULL)
    {
	ANTLR3_FREE(bitset->bits);
	bitset->bits = NULL;
    }
    ANTLR3_FREE(bitset);

    return;
}

ANTLR3_API pANTLR3_BITSET
antlr3BitsetNew(ANTLR3_UINT32 numBits)
{
    pANTLR3_BITSET  bitset;

    ANTLR3_UINT32   numelements;

    /* Allocate memory for the bitset structure itself
     */
    bitset  = (pANTLR3_BITSET) ANTLR3_MALLOC((size_t)sizeof(ANTLR3_BITSET));

    if	(bitset == NULL)
    {
	return	(pANTLR3_BITSET) ANTLR3_ERR_NOMEM;
    }

    /* Avoid memory thrashing at teh upfron expense of a few bytes
     */
    if	(numBits < (8 * ANTLR3_BITSET_BITS))
    {
	numBits = 8 * ANTLR3_BITSET_BITS;
    }

    /* No we need to allocate the memory for the number of bits asked for
     * in multiples of ANTLR3_UINT64.
     */
    numelements	= ((numBits -1) >> ANTLR3_BITSET_LOG_BITS) + 1;

    bitset->bits    = (pANTLR3_UINT64) ANTLR3_MALLOC((size_t)(numelements * sizeof(ANTLR3_UINT64)));
    bitset->length  = numelements;

    if	(bitset->bits	== NULL)
    {
	ANTLR3_FREE(bitset);
	return	(pANTLR3_BITSET) ANTLR3_ERR_NOMEM;
    }

    ANTLR3_MEMSET(bitset->bits, 0x00, (ANTLR3_UINT64)(numelements * sizeof(ANTLR3_UINT64)));

    bitset->clone	=   antlr3BitsetClone;
    bitset->or		=   antlr3BitsetOR;
    bitset->orInPlace	=   antlr3BitsetORInPlace;
    bitset->size	=   antlr3BitsetSize;
    bitset->add		=   antlr3BitsetAdd;
    bitset->equals	=   antlr3BitsetEquals;
    bitset->isMember	=   antlr3BitsetMember;
    bitset->numBits	=   antlr3BitsetNumBits;
    bitset->remove	=   antlr3BitsetRemove;
    bitset->isNil	=   antrl3BitsetIsNil;

    /* All seems good
     */
    return  bitset;
}

ANTLR3_API pANTLR3_BITSET
antlr3BitsetCopy(pANTLR3_UINT64 inSet, ANTLR3_UINT32 numElements)
{
    pANTLR3_BITSET  bitset;

    /* Allocate memory for the bitset structure itself
     */
    bitset  = (pANTLR3_BITSET) ANTLR3_MALLOC((size_t)sizeof(ANTLR3_BITSET));

    if	(bitset == NULL)
    {
	return	(pANTLR3_BITSET) ANTLR3_ERR_NOMEM;
    }

    /* Avoid memory thrashing at the expense of a few more bytes
     */
    if	(numElements < 8)
    {
	numElements = 8;
    }

    /* Install the length in ANTLR3_UINT64 units
     */
    bitset->length  = numElements;

    bitset->bits    = (pANTLR3_UINT64)ANTLR3_MALLOC((size_t)(numElements * sizeof(ANTLR3_UINT64)));

    if	(bitset->bits == NULL)
    {
	ANTLR3_FREE(bitset);
	return	(pANTLR3_BITSET) ANTLR3_ERR_NOMEM;
    }

    ANTLR3_MEMMOVE(bitset->bits, inSet, (ANTLR3_UINT64)(numElements * sizeof(ANTLR3_UINT64)));

    /* All seems good
     */
    return  bitset;
}

static pANTLR3_BITSET
antlr3BitsetClone(pANTLR3_BITSET inSet)
{
    pANTLR3_BITSET  bitset;

    /* Allocate memory for the bitset structure itself
     */
    bitset  = antlr3BitsetNew(8 * inSet->length);

    if	(bitset == NULL)
    {
	return	(pANTLR3_BITSET) ANTLR3_ERR_NOMEM;
    }

    /* Install the actual bits in the source set
     */
    ANTLR3_MEMMOVE(bitset->bits, inSet->bits, (ANTLR3_UINT64)(inSet->length * sizeof(ANTLR3_UINT64)));

    /* All seems good
     */
    return  bitset;
}


ANTLR3_API pANTLR3_BITSET
antlr3BitsetList()
{
    /* TODO: Place holder until I need to code it and have written List and Stack */
    return NULL;
}


/*!
 * \brief
 * Creates a new bitset with at least one element, but as
 * many elements are required.
 * 
 * \param[in] bit
 * A variable number of bits to add to the set, ending in -1 (impossible bit).
 * 
 * \returns
 * A new bit set with all of the specified elements added into it.
 * 
 * Call as:
 *  - pANTLR3_BITSET = antlrBitsetOf(n, n1, n2, -1);
 *  - pANTLR3_BITSET = antlrBitsetOf(-1);  Create empty bitset 
 *
 * \remarks
 * Stdargs function - must supply -1 as last paremeter, which is NOT
 * added to the set.
 * 
 */
ANTLR3_API pANTLR3_BITSET
antlr3BitsetOf(ANTLR3_INT32 bit, ...)
{
    pANTLR3_BITSET  bitset;

    va_list ap;

    /* Allocate memory for the bitset structure itself
     * the input parameter is the bit number (0 based)
     * to include in the bitset, so we need at at least
     * bit + 1 bits. If any arguments indicate a 
     * a bit higher than the default number of bits (0 menas default size)
     * then Add() will take care
     * of it.
     */
    bitset  = antlr3BitsetNew(0);

    if	(bitset == NULL)
    {
	return	(pANTLR3_BITSET) ANTLR3_ERR_NOMEM;
    }

    /* Now we can add the element bits into the set
     */
    va_start(ap, bit);
    while   (bit != -1)
    {
	antlr3BitsetAdd(bitset, bit);
	bit = va_arg(ap, ANTLR3_UINT32);
    }
    va_end(ap);

    /* return the new bitset
     */
    return  bitset;
}

static pANTLR3_BITSET
antlr3BitsetOR(pANTLR3_BITSET bitset1, pANTLR3_BITSET bitset2)
{
    pANTLR3_BITSET  bitset;

    if	(bitset1 == NULL)
    {
	return antlr3BitsetClone(bitset2);
    }

    if	(bitset2 == NULL)
    {
	return	antlr3BitsetClone(bitset1);
    }

    /* Allocate memory for the newly ordered bitset structure itself.
     */
    bitset  = antlr3BitsetClone(bitset1);
    
    antlr3BitsetORInPlace(bitset, bitset2);

    return  bitset;

}

static void
antlr3BitsetAdd(pANTLR3_BITSET bitset, ANTLR3_INT32 bit)
{
    ANTLR3_UINT32   word;

    word    = wordNumber(bit);

    if	(word	> bitset->length)
    {
	growToInclude(bitset, bit);
    }

    bitset->bits[word] |= bitMask(bit);

}

static void
growToInclude(pANTLR3_BITSET bitset, ANTLR3_INT32 bit)
{
    ANTLR3_UINT32   newSize;
    pANTLR3_UINT64   newBits;

    newSize = max((bitset->length << 1), numWordsToHold(bit));

    /* Space for newly sized bitset - come back to this and iuse realloc, it may
     * be more efficient...
     */
    newBits = (pANTLR3_UINT64) ANTLR3_MALLOC((size_t)(newSize * sizeof(ANTLR3_UINT64)));

    /* Blank them out
     */
    ANTLR3_MEMSET(newBits, 0x00, (size_t)(newSize * sizeof(ANTLR3_UINT64)));

    /* Copy existing bits
     */
    ANTLR3_MEMMOVE((void *)newBits, (const void *)bitset->bits, (size_t)(bitset->length * sizeof(ANTLR3_UINT64)));

    /* Out with the old bits
     */
    ANTLR3_FREE(bitset->bits);

    /* In with the new bits... der der der der.
     */
    bitset->bits    = newBits;

}

static void
antlr3BitsetORInPlace(pANTLR3_BITSET bitset, pANTLR3_BITSET bitset2)
{
    ANTLR3_UINT32   minimum;
    ANTLR3_UINT32   i;

    if	(bitset2 == NULL)
    {
	return;
    }


    /* First make sure that the target bitset is big enough
     * for the new bits to be ored in.
     */
    if	(bitset->length < bitset2->length)
    {
	growToInclude(bitset, (bitset2->length * sizeof(ANTLR3_UINT64)));
    }
    
    /* Or the miniimum number of bits after any resizing went on
     */
    minimum = min(bitset->length, bitset2->length);

    for	(i = minimum - 1; i >= 0; i--)
    {
	bitset->bits[i] |= bitset2->bits[i];
    }
}

static ANTLR3_UINT64
bitMask(ANTLR3_UINT32 bitNumber)
{
    return  ((ANTLR3_UINT64)1) << (bitNumber & ANTLR3_BITSET_MOD_MASK);
}

static ANTLR3_UINT32
antlr3BitsetSize(pANTLR3_BITSET bitset)
{
    ANTLR3_UINT32   degree;
    ANTLR3_UINT32   i;
    ANTLR3_UINT8    bit;
    
    /* Come back to this, it may be faster to & with 0x01
     * then shift right a copy of the 4 bits, than shift left a constant of 1.
     * But then again, the optimizer might just work this out
     * anyway.
     */
    degree  = 0;
    for	(i = bitset->length - 1; i>= 0; i--)
    {
	if  (bitset->bits[i] != 0)
	{
	    for	(bit = ANTLR3_BITSET_BITS - 1; bit >= 0; bit--)
	    {
		if  ((bitset->bits[i] & (((ANTLR3_UINT64)1) << bit)) != 0)
		{
		    degree++;
		}
	    }
	}
    }
    return degree;
}

static ANTLR3_BOOLEAN
antlr3BitsetEquals(pANTLR3_BITSET bitset1, pANTLR3_BITSET bitset2)
{
    ANTLR3_UINT32   minimum;
    ANTLR3_UINT32   i;

    if	(bitset1 == NULL || bitset2 == NULL)
    {
	return	ANTLR3_FALSE;
    }

    /* Work out the minimum comparison set
     */
    minimum = min(bitset1->length, bitset2->length);

    /* Make sure explict in common bits are equal
     */
    for	(i = minimum - 1; i >=0 ; i--)
    {
	if  (bitset1->bits[i] != bitset2->bits[i])
	{
	    return  ANTLR3_FALSE;
	}
    }

    /* Now make sure the bits of the larger set are all turned
     * off.
     */
    if	(bitset1->length > minimum)
    {
	for (i = minimum ; i < bitset1->length; i++)
	{
	    if	(bitset1->bits[i] != 0)
	    {
		return	ANTLR3_FALSE;
	    }
	}
    }
    else if (bitset2->length > minimum)
    {
	for (i = minimum; i < bitset2->length; i++)
	{
	    if	(bitset2->bits[i] != 0)
	    {
		return	ANTLR3_FALSE;
	    }
	}
    }

    return  ANTLR3_TRUE;
}

static ANTLR3_BOOLEAN
antlr3BitsetMember(pANTLR3_BITSET bitset, ANTLR3_UINT32 bit)
{
    ANTLR3_UINT32    wordNo;

    wordNo  = wordNumber(bit);

    if	(wordNo > bitset->length)
    {
	return	ANTLR3_FALSE;
    }
    
    return  (ANTLR3_BOOLEAN)(bitset->bits[wordNo] & bitMask(bit));
}

static void
antlr3BitsetRemove(pANTLR3_BITSET bitset, ANTLR3_UINT32 bit)
{
    ANTLR3_UINT32    wordNo;

    wordNo  = wordNumber(bit);

    if	(wordNo < bitset->length)
    {
	bitset->bits[wordNo] &= ~(bitMask(bit));
    }
}
static ANTLR3_BOOLEAN
antrl3BitsetIsNil(pANTLR3_BITSET bitset)
{
   ANTLR3_UINT32    i;

   for	(i = bitset->length -1; i>= 0; i--)
   {
       if   (bitset->bits[i] != 0)
       {
	   return ANTLR3_FALSE;
       }
   }
   
   return   ANTLR3_TRUE;
}

static ANTLR3_UINT32
numWordsToHold(ANTLR3_UINT32 bit)
{
    return  (bit >> ANTLR3_BITSET_LOG_BITS) + 1;
}

static	ANTLR3_UINT32
wordNumber(ANTLR3_UINT32 bit)
{
    return  bit >> ANTLR3_BITSET_LOG_BITS;
}

static ANTLR3_UINT32
antlr3BitsetNumBits(pANTLR3_BITSET bitset)
{
    return  bitset->length << ANTLR3_BITSET_LOG_BITS;
}

