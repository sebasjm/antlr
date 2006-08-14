/** \file 
 * Base interface for any ANTLR3 lexer.
 *
 * An ANLTR3 lexer builds from two sets of components:
 *
 *  - The runtime components that provide common functionality such as
 *    traversing character streams, building tokens for output and so on.
 *  - The generated rules and struutre of the actual lexer, which call upon the
 *    runtime components.
 *
 * A lexer class contains  a character input stream, a base recognizer interface 
 * (which it will normally implement) and a token source interface (which it also
 * implements. The Tokensource interface is called by a token consumer (such as
 * a parser, but in theory it can be anything that wants a set of abstract
 * tokens in place of a raw character stream.
 *
 * So then, we set up a lexer in a sequence akin to:
 *
 *  - Create a chracter stream (somethign which implements ANTLR3_INPUT_STREAM)
 *    and initialize it.
 *  - Create a lexer interface and tell it wher it its input stream is.
 *    This will cause the creation of a base recognizer class, which it will 
 *    override with its own implementations of some methods. The lexer creator
 *    can also then in turn override anything it likes. 
 *  - The lexer token source interface is then passed to some interface that
 *    knows how to use it, byt calling for a next token. 
 *  - When a next token is called, let ze lexing begin.
 *
 */
#ifndef	_ANTLR3_LEXER
#define	_ANTLR3_LEXER

/* Definitions
 */
#define	ANTLR3_STRING_TERMINATOR	0xFFFFFFFF

#include    <antlr3defs.h>
#include    <antlr3input.h>
#include    <antlr3commontoken.h>
#include    <antlr3tokenstream.h>
#include    <antlr3baserecognizer.h>

typedef	struct ANTLR3_LEXER_struct
{
    /** An implementor of a lexer receives a pointer to itself
     *  when its API functions are called.
     */
    void	* me;

    /** A generated lexer has an mTokens() function, which needs
     *  the context pointer of the generated lexr, not the base lexer interface
     *  this is stored here and initialized by the generated code (or manually
     *  if this is a maunlly built lexer.
     */
    void	* ctx;

    /** A pointer to the character stream whence this lexer is receiving
     *  characters. I may come back to this and implement charstream outside
     *  the input stream as per the java implementatio.
     */
    pANTLR3_INPUT_STREAM	input;

    /** Pointer to the implementation of a base recognizer, which the lexer
     *  creates and then overrides with its own lexer oriented functions (the 
     *  default implementation is parser oriented). This also contains a
     *  token source interface, which the lexer instance will provide to anything 
     *  that needs it, which is anything else that implements a base recognizer,
     *  such as a parser.
     */
    pANTLR3_BASE_RECOGNIZER	rec;

    /** The goal of all lexer rules/methods is to create a token object.
     *  This is an instance variable as multiple rules may collaborate to
     *  create a single token.  For example, NUM : INT | FLOAT ;
     *  In this case, you want the INT or FLOAT rule to set token and not
     *  have it reset to a NUM token in rule NUM.
     */
    pANTLR3_COMMON_TOKEN	token;

    /** The goal of all lexer rules being to create a token, then a lexer
     *  needs to build a token factory to create them.
     */
    pANTLR3_TOKEN_FACTORY	tokFactory;

    /** A lexer is a source of tokens, produced by all the generated (or
     *  hand crafted if you like) matching rules. As such it needs to provide
     *  a token source interface implementation.
     */
    pANTLR3_TOKEN_SOURCE	tokSource;

    /** What character index in the stream did the current token start at?
     *  Needed, for example, to get the text for current token.  Set at
     *  the start of nextToken.
     */
    ANTLR3_INT64		tokenStartCharIndex;

    /** Pointer to a function that sets the charstream source for the lexer and
     *  causes it to  be reset.
     */
    void			(*setCharStream)    (void * lexer, pANTLR3_INPUT_STREAM input);
    
    /** Pointer to a function that emits the supplied token as the next token in
     *  the stream.
     */
    void			(*emit)		    (void * lexer, pANTLR3_COMMON_TOKEN token);

    /** Pointer to a function that constructs a new token from the supplied information 
     */
    void			(*emitNew)	    (void * lexer, 
							ANTLR3_UINT32 ttype,
							ANTLR3_UINT64 line,	ANTLR3_UINT32 charPosition,
							ANTLR3_UINT32 channel,
							ANTLR3_UINT64 start,	ANTLR3_UINT64 stop
							);

    /** Pointer to the user provided (either manually or through code generation
     *  function that causes the lexer rules to run the lexing rules and produce 
     *  the next token if there iss one. This is called from nextToken() in the
     *  pANTLR3_TOKEN_SOURCE. Note that the input parameter for this funciton is 
     *  the generated lexer context (stored in ctx in this interface) it is a generated
     *  function and expects the context to be the generated lexer. 
     */
    void	        (*mTokens)		    (void * ctx);

    /** Pointer to a function that attempts to match and consume the specified string from the input
     *  stream. Note that strings muse be passed as terminated arrays of ANTLR3_UCHAR. Strings are terminated
     *  with 0xFFFFFFFF, which is an invalid UTF32 character
     */
    ANTLR3_BOOLEAN	(*matchs)	    (void * lexer, ANTLR3_UCHAR * string);

    /** Pointer to a function that matches and consumes the specified character from the input stream.
     *  As the input stream is required to provide characters via LA() as UTF32 characters it does not 
     *  need to provide an implementation if it is not sourced from 8 bit ASCII. The default lexer
     *  implementation is source encoding agnostic, unless for some reason it takes two 32 bit characters
     *  to specify a single character, in which case the input stream and the lexer rules would have to match
     *  in encoding and then it would work 'by accident' anyway.
     */
    ANTLR3_BOOLEAN	(*matchc)	    (void * lexer, ANTLR3_UCHAR c);

    /** Pointer to a function that matches any character in the supplied range (I suppose it could be a token range too
     *  but this would only be useful if the tokens were in tsome guaranteed order which is
     *  only going to happen with a hand crafted token set).
     */
    ANTLR3_BOOLEAN	(*matchRange)	    (void * lexer, ANTLR3_UCHAR low, ANTLR3_UCHAR high);

    /** Pointer to a function that matches the next token/char in the input stream
     *  regardless of what it actaully is.
     */
    void		(*matchAny)	    (void * lexer);

    /** Pointer to a function that recovers from an error found in the input stream.
     *  Generally, this will be a #ANTLR3_EXCEPTION_NOVIABLE_ALT but it could also
     *  be from a mismatched token that the (*match)() could not recover from.
     */
    void		(*recover)	    (void * lexer);

    /** Pointer to function to return the current line number in the input stream
     */
    ANTLR3_UINT64	(*getLine)		(void * lexer);
    ANTLR3_UINT64	(*getCharIndex)		(void * lexer);
    ANTLR3_UINT32	(*getCharPositionInLine)(void * lexer);

    /** Pointer to function to return the text so far for the current token being generated
     */
    pANTLR3_STRING	(*getText)	    (void * lexer);

    /** Pointer to a function that knows how to free the resources of a lexer
     */
    void		(*free)		    (void * lexer);

    /** We must track the token rule nesting level as we only want to
     *  emit a token automatically at the outermost level so we don't get
     *  two if FLOAT calls INT.  To save code space and time, do not
     *  inc/dec this in fragment rules.
     */
    ANTLR3_INT32	ruleNestingLevel;
}
    ANTLR3_LEXER;


#endif
