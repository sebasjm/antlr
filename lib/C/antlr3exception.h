/** \file
 *  Contains the definition of a basic ANTLR3 exception structure created
 *  by a recognizer when errors are found/predicted.
 */
#ifndef	_ANTLR3_EXCEPTION_H
#define	_ANTLR3_EXCEPTION_H

#include    <antlr3defs.h>

/** Indicates that the recognizer received a token
 *  in the input that was not predicted.
 */
#define	ANTLR3_RECOGNITION_EXCEPTION	    1

/** Name of exception #ANTLR3_RECOGNITION_EXCEPTION
 */
#define	ANTLR3_RECOGNITION_EX_NAME  "Recognition Exception"

/** Inidicates that the recognizer was expecting one token and found a
 *  a different one.
 */
#define	ANTLR3_MISMATCHED_TOKEN_EXCEPTION   2

/** Name of #ANTLR3_MISMATCHED_TOKEN_EXCEPTION
 */
#define	ANTLR3_MISMATCHED_EX_NAME   "Mismatched Token Exception"

/** Base structure for an ANTLR3 exception tracker
 */
typedef	struct ANTLR3_EXCEPTION_struct
{
    /** Set to one of the exception type #defines above.
     */
    ANTLR3_UINT32   type;

    /** The string name of the exception
     */
    void    *	    name;

    /** The printable message that goes with this exception, in your preferred
     *  encoding format. ANTLR just uses ASCII by deafult but you cna ignore these
     *  messages or convert them to another ofrat or whatever of course. They are
     *  really internal messges that you then decide how to print out in a form that
     *  the users of your product will understand, as they are unlikely to know what
     *  to do with "Recognition exception at: [[TOK_GERUND..... " ;-)
     */
    void    *	    message;

    /** Name of the file/input source for reporting
     */
    void    *	    streamName;

    /** If set to ANTLR3_TRUE, this indicates that the message element of this structure
     *  should be freed by calling ANTLR3_FREE() when the exception is destroyed.
     */
    ANTLR3_BOOLEAN  freeMessage;

    /** Indicates the index of the 'token' we were looking at when the
     *  exception occurred.
     */
    ANTLR3_UINT64   index;

    /** Indicates what the current token was when the error occurred. Since not
     *  all input streams will be able to retrieve the nth token, we track it here
     *  instead. This is for parsers, and even tree parsers may set this.
     */
    void	* token;

    /** If this is a tree parser exception then the node is set to point ot the node
     * that caused the issue.
     */
    void	* node;

    /** The current character when an error ocurred - for lexers.
     */
    ANTLR3_UCHAR   c;

    /** Track the line at which the error occurred in case this is
     *  generated from a lexer.  We need to track this since the
     *  unexpected char doesn't carry the line info.
     */
    ANTLR3_UINT64   line;

    /** Character position in the line where the error occurred.
     */
    ANTLR3_INT32   charPositionInLine;

    /** Pointer to the next exception in the chain (if any)
     */
    struct ANTLR3_EXCEPTION_struct * nextException;

    /** Pointer to the input stream that this exception occurred in.
     */
    pANTLR3_INT_STREAM    input;

    /** Pointer for you, the programmer to add anything you like to an exception.
     */
    void    *	    custom;

    /** Pointer to a routine that is called to free the custom exception structure
     *  when the exception is destroyed. Set to NULL if nothing should be done.
     */
    void	    (*freeCustom)   (void * custom);
    void	    (*print)	    (struct ANTLR3_EXCEPTION_struct * ex);
    void	    (*freeEx)	    (struct ANTLR3_EXCEPTION_struct * ex);

}
    ANTLR3_EXCEPTION;



#endif