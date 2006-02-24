/** \file
 * Basic type and constant definitions for ANTLR3 Runtime.
 */
#ifndef	_ANTLR3DEFS_H
#define	_ANTLR3DEFS_H

#define	ANTLR3_MEM_DEBUG


/* Common definitions come first
 */
#include    <antlr3errors.h>

#define	ANTLR3_WINDOWS
#define	ANTLR3_WIN32

#ifdef	ANTLR3_WINDOWS 

#define	WINDOWS_LEAN_AND_MEAN

#include    <windows.h>
#include    <stdio.h>
#include    <sys/stat.h>
#include    <stdarg.h>

#define	ANTLR3_API  __declspec(dllexport)

typedef	INT32	ANTLR3_CHAR,	*pANTLR3_CHAR;
typedef	UINT32	ANTLR3_UCHAR,	*pANTLR3_UCHAR;

typedef	INT8	ANTLR3_INT8,	*pANTLR3_INT8;
typedef	INT16	ANTLR3_INT16,	*pANTLR3_INT16;
typedef	INT32	ANTLR3_INT32,	*pANTLR3_INT32;
typedef	INT64	ANTLR3_INT64,	*pANTLR3_INT64;

typedef	UINT8	ANTLR3_UINT8,	*pANTLR3_UINT8;
typedef	UINT16	ANTLR3_UINT16,	*pANTLR3_UINT16;
typedef	UINT32	ANTLR3_UINT32,	*pANTLR3_UINT32;
typedef	UINT64	ANTLR3_UINT64,	*pANTLR3_UINT64;

typedef	UINT32	ANTLR3_BOOLEAN, *pANTLR3_BOOLEAN;

#define	ANTLR3_INLINE	inline

typedef FILE *	    ANTLR3_FDSC;
typedef	struct stat ANTLR3_FSTAT_STRUCT;

#ifdef	ANTLR3_WIN32
#endif

#ifdef	ANTLR3_WIN64
#endif

#endif

#ifdef	ANTLR3_UNIX

#define	_stat	stat

#define	ANTLR3_API

#endif

#ifdef	ANTLR3_VMS

#define	_stat	stat

#define	ANTLR3_API

#endif

/* Predeclare the typedefs for all the interfaces, then 
 * they can be inter-dependant and we will let the linker
 * sort it out for us.
 */
#include    <antlr3interfaces.h>

// Prototypes
//
ANTLR3_API pANTLR3_BITSET	    antlr3BitsetNew		    (ANTLR3_UINT32 numBits);
ANTLR3_API pANTLR3_BITSET	    antlr3BitsetOf		    (ANTLR3_INT32 bit, ...);
ANTLR3_API pANTLR3_BITSET	    antlr3BitsetList		    (pANTLR3_HASH_TABLE list);
ANTLR3_API pANTLR3_BITSET	    antlr3BitsetCopy		    (pANTLR3_UINT64 inSet, ANTLR3_UINT32 numElements);

ANTLR3_API pANTLR3_BASE_RECOGNIZER  antlr3BaseRecognizerNew	    (ANTLR3_UINT32 type);
ANTLR3_API void			    antlr3RecognitionExceptionNew   (pANTLR3_BASE_RECOGNIZER recognizer);
ANTLR3_API void			    antlr3MTExceptionNew	    (pANTLR3_BASE_RECOGNIZER recognizer);

ANTLR3_API pANTLR3_HASH_TABLE	    antlr3HashTableNew		    (ANTLR3_UINT32 sizeHint);
ANTLR3_API ANTLR3_UINT32	    antlr3Hash			    (void * key, ANTLR3_UINT32 keylen);
ANTLR3_API pANTLR3_HASH_ENUM	    antlr3EnumNew		    (pANTLR3_HASH_TABLE table);
ANTLR3_API pANTLR3_LIST		    antlr3ListNew		    (ANTLR3_UINT32 sizeHint);
ANTLR3_API pANTLR3_STACK	    antlr3StackNew		    (ANTLR3_UINT32 sizeHint);

ANTLR3_API ANTLR3_UCHAR		    antlr3c8toAntlrc		    (ANTLR3_INT8 inc);

ANTLR3_API pANTLR3_EXCEPTION	    antlr3ExceptionNew		    (ANTLR3_UINT32 exception, void * name, void * message, ANTLR3_BOOLEAN freeMessage);

ANTLR3_API pANTLR3_INPUT_STREAM	    antlr3AsciiFileStreamNew	    (pANTLR3_UINT8 fileName);



#ifdef	ANTLR3_MEM_DEBUG

ANTLR3_API void			  * ANTLR3_MALLOC_DBG		    (pANTLR3_UINT8 file, ANTLR3_UINT32 line, size_t request);
ANTLR3_API void			  * ANTLR3_REALLOC_DBG		    (pANTLR3_UINT8 file, ANTLR3_UINT32 line, void * current, ANTLR3_UINT64 request);
ANTLR3_API void			    ANTLR3_FREE_DBG		    (void * ptr);
ANTLR3_API pANTLR3_UINT8	    ANTLR3_STRDUP_DBG		    (pANTLR3_UINT8 file, ANTLR3_UINT32 line, pANTLR3_UINT8 instr);
ANTLR3_API void			    ANTLR3_MEM_REPORT		    (ANTLR3_BOOLEAN);

#define	ANTLR3_MALLOC( s )	ANTLR3_MALLOC_DBG   ((pANTLR3_UINT8) __FILE__ , (ANTLR3_UINT32)__LINE__ , (s))
#define	ANTLR3_REALLOC( c, s)	ANTLR3_REALLOC_DBG  ((pANTLR3_UINT8) __FILE__ , (ANTLR3_UINT32)__LINE__ , c, s)
#define	ANTLR3_FREE( p )	ANTLR3_FREE_DBG	    (p)
#define	ANTLR3_FREE_FUNC	ANTLR3_FREE_DBG	
#define	ANTLR3_STRDUP( p )	ANTLR3_STRDUP_DBG   ((pANTLR3_UINT8) __FILE__ , (ANTLR3_UINT32)__LINE__ , p)

#else

#define	ANTLR3_MEM_REPORT()
ANTLR3_API void			  * ANTLR3_MALLOC		    (size_t request);
ANTLR3_API void			  * ANTLR3_REALLOC		    (void * current, ANTLR3_UINT64 request);
ANTLR3_API void			    ANTLR3_FREE			    (void * ptr);
#define	ANTLR3_FREE_FUNC	    ANTLR3_FREE
ANTLR3_API pANTLR3_UINT8	    ANTLR3_STRDUP		    (pANTLR3_UINT8 instr);

#endif

ANTLR3_API void			  * ANTLR3_MEMMOVE		    (void * target, const void * source, ANTLR3_UINT64 size);
ANTLR3_API void			  * ANTLR3_MEMSET		    (void * target, ANTLR3_UINT8 byte, ANTLR3_UINT64 size);


ANTLR3_API pANTLR3_INPUT_STREAM	    antlr3NewAsciiStringInPlaceStream   (pANTLR3_UINT8 inString, ANTLR3_UINT64 size, pANTLR3_UINT8 name);
ANTLR3_API pANTLR3_INPUT_STREAM	    antlr3NewAsciiStringCopyStream	(pANTLR3_UINT8 inString, ANTLR3_UINT64 size, pANTLR3_UINT8 name);

ANTLR3_API pANTLR3_STRING_FACTORY   antlr3StringFactoryNew	    ();

ANTLR3_API pANTLR3_COMMON_TOKEN	    antlr3CommonTokenNew	    (ANTLR3_UINT32 ttype);
ANTLR3_API pANTLR3_TOKEN_FACTORY    antlr3TokenFactoryNew	    (pANTLR3_INPUT_STREAM input);

#endif	/* _ANTLR3DEFS_H	*/