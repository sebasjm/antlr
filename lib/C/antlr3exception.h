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
#define	ANTRL3_RECOGNITION_EXCEPTION	1

/** Name of exception #ANTLR3_RECOGNITION_EXCEPTION
 */
#define	ANTLR3_RECOGNITION_EX_NAME  "Recognition Exception"

/** Base structure for an ANTLR3 excpetion tracker
 */
typedef	struct ANTLR3_EXCEPTION_struct
{
    /** Set to one of the exception type #defines above.
     */
    ANTLR3_UINT32   exception;

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

    /** If set to ANTLR_TRUE, this indicates that the message element of this structure
     *  should be freed by calling ANTLR3_FREE() when the exception is destroyed.
     */
    ANTLR3_BOOLEAN  freeMessage;

    /** Pointer for you, the programmer to add anything you like to an exception.
     */
    void    *	    custom;

    /** Pointer to a routine that is called to free the custom exception structure
     *  when the exception is destroyed. Set to NULL if nothing should be done.
     */
    void	    (*freeCustom)(void * custom);

    /** Pointer to the next exception in the chain (if any)
     */
    struct ANTLR3_EXCEPTION_struct * nextException;
}
    ANTLR3_EXCEPTION, *pANTLR3_EXCEPTION;

    pANTLR3_EXCEPTION	antlr3ExceptionNew(ANTLR3_UINT32 exception, void * name, void * message, ANTLR3_BOOLEAN freeMessage);
    void		antlr3ExceptionPrint(pANTLR3_EXCEPTION ex);
    void		antlr3ExceptionFree(pANTLR3_EXCEPTION ex);

#endif