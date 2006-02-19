/** \file
 * Basic type and constant definitions for ANTLR3 Runtime.
 */
#ifndef	_ANTLR3DEFS_H
#define	_ANTLR3DEFS_H

/* Common definitions come first
 */

#define	ANTLR3_WINDOWS
#define	ANTLR3_WIN32

#ifdef	ANTLR3_WINDOWS 

#define	WINDOWS_LEAN_AND_MEAN

#include    <windows.h>
#include    <stdio.h>
#include    <sys/stat.h>


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

#endif	/* _ANTLR3DEFS_H	*/