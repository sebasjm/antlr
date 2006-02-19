#ifndef	_ANTLR3ERRORS_H
#define	_ANTLR3ERRORS_H

#define	ANTLR3_SUCCESS	0

/** Indicates end of chrater stream and is an invalid Unicode code point. */
#define ANTLR3_CHARSTREAM_EOF	0xFFFFFFFFFFFFFFFF

#define	ANTLR3_ERR_BASE	    -1
#define	ANTLR3_ERR_NOMEM    ANTLR3_ERR_BASE - 0
#define	ANTLR3_ERR_NOFILE   ANTLR3_ERR_BASE - 1
#define	ANTLR3_ERR_HASHDUP  ANTLR3_ERR_BASE - 2

#endif	/* _ANTLR3ERRORS_H */