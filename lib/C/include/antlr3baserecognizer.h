/** \file
 * Defines the basic structure to support recognizing by either a lexer,
 * parser, or tree parser.
 */
#ifndef	_ANTLR3_BASERECOGNIZER_H
#define	_ANTLR3_BASERECOGNIZER_H

#include    <antlr3defs.h>
#include    <antlr3exception.h>
#include    <antlr3input.h>
#include    <antlr3tokenstream.h>
#include    <antlr3commontoken.h>

/** Type indicator for a lexer recognizer
 */
#define	    ANTLR3_TYPE_LEXER		0x0001

/** Type inficator for a parser recognizer
 */
#define	    ANTLR3_TYPE_PARSER		0x0002

/** Type indicator for a tree parser recognizer
 */
#define	    ANTLR3_TYPE_TREEPARSER	0x0004

/** \brief Base tracking context structure for all types of
 * recognizers.
 */
typedef	struct ANTLR3_BASE_RECOGNIZER_struct
{
    /** Whatever is providing this interface needs a pointer to itself
     *  so that this can be passed back to it whenever the api functions
     *  are called.
     */
    void	      * me;

    /** Indicates the type of recognizer that we are an instance of.
     *  The programmer may set this to anything of course, but the default 
     *  implementations of the interface only really understand the built in
     *  types, so new error handlers etc woudl proably be required too.
     *
     *  Valid types are:
     *
     *    - #ANTLR3_LEXER  
     *	  - #ANTLR3_PARSER
     *    - #ANTLR3_TREEPARSER
     */
    ANTLR3_UINT32	type;

    /** Track around a hint from the creator of the recognizer as to how big this
     *  thing is going to get, as the actress said to the bishop. This allows us
     *  to tune hash tables accordingly. This might not be the best place for this
     *  in the end but we will see.
     */
    ANTLR3_UINT32	sizeHint;

    /** Track the set of token types that can follow any rule invocation.
     *  Stack structure, to support: List<BitSet>.
     */
    pANTLR3_STACK	following;

    /** This is true when we see an error and before having successfully
     *  matched a token.  Prevents generation of more than one error message
     *  per error.
     */
    ANTLR3_BOOLEAN	errorRecovery;
    
    /** The index into the input stream where the last error occurred.
     * 	This is used to prevent infinite loops where an error is found
     *  but no token is consumed during recovery...another error is found,
     *  ad naseum.  This is a failsafe mechanism to guarantee that at least
     *  one token/tree node is consumed for two errors.
     */
    ANTLR3_INT64	lastErrorIndex;

    /** In lieu of a return value, this indicates that a rule or token
     *  has failed to match.  Reset to false upon valid token match.
     */
    ANTLR3_BOOLEAN	failed;

    /** If 0, no backtracking is going on.  Safe to exec actions etc...
     *  If >0 then it's the level of backtracking.
     */
    ANTLR3_INT32	backtracking;

    /** ANTLR3_LIST of ANTLR3_LIST for rule memoizing.
     * tracks
     *  the stop token index for each rule.  ruleMemo[ruleIndex] is
     *  the memoization table for ruleIndex.  For key ruleStartIndex, you
     *  get back the stop token for associated rule or MEMO_RULE_FAILED.
     *
     *  This is only used if rule memoization is on (which it is by default).
     */
    pANTLR3_LIST	ruleMemo;

    /** Pointer to an array of token names
     *  that are generally useful in error reporting. The generated parsers install
     *  this pointer. The table it points to is statically allocated as 8 bit ascii
     *  at parser compile time - grammar token names are thus restricted in chracter
     *  sets, which does not seem to terrible.
     */
    pANTLR3_UINT8	tokenNames;

    /** Pointer to a function that matches the current input symbol
     *  against the supplied type. the function causes an error if a
     *  match is not found and the default implementation will also
     *  attempt to perform one token insertion or deletion if that is
     *  possible with the input stream. You can override the default
     *  implementation by installing a pointer to your own function
     *  in this interface after the recognizer has initialized. This can
     *  perform differnt recovery options or not recover at all and so on.
     *  To ignore recovery altogether, see the comments in the default
     *  implementation of this function in antlr3baserecognizer.c
     *
     *  Note that errors are signalled by setting the error flag below
     *  and creating a new exception structure and installing it in the
     *  exception pointer below (you can chain these if you like and handle them
     *  in some customized way).
     *  TODO: See if we end up using error or just used failed...
     */
    ANTLR3_BOOLEAN	(*match)	(void * recognizer, pANTLR3_INT_STREAM	input,
					    ANTLR3_UINT32 ttype, pANTLR3_BITSET follow);

    /** Pointer to a function that matches the next token/char in the input stream
     *  regardless of what it actaully is.
     */
    void		(*matchAny)	(void * recognizer, pANTLR3_INT_STREAM	input);
    
    /** Pointer to a function that works out what to do when a token mismatch
     *  occurs, so that Tree parsers can behave differently to other recognizers.
     */
    void		(*mismatch)	(void * recognizer,  pANTLR3_INT_STREAM	input,
					    ANTLR3_UINT32 ttype, pANTLR3_BITSET follow);

    /** Pointer to a function to call to report a recognition problem. You may override
     *  this funciton with your own function, but refer to the standard implementation
     *  in antlr3baserecognizer.c for guidance. The function should recognize whether 
     *  error recovery is in force, so that it does not prinnt out more than one error messages
     *  for the same error. From the java comments in BaseRecognizer.java:
     *
     *  This method sets errorRecovery to indicate the parser is recovering
     *  not parsing.  Once in recovery mode, no errors are generated.
     *  To get out of recovery mode, the parser must successfully match
     *  a token (after a resync).  So it will go:
     *
     * 		1. error occurs
     * 		2. enter recovery mode, report error
     * 		3. consume until token found in resynch set
     * 		4. try to resume parsing
     * 		5. next match() will reset errorRecovery mode
     */
    void		(*reportError)		    (void * recognizer);

    /** Pointer to a function that is called to display a recognition error message. You may
     *  overrdide this function independently of (*reportError)() above as that function calls
     *  this one to do the actual exception printing.
     */
    void		(*displayRecognitionError)  (void * recognizer, pANTLR3_UINT8 tokenNames);

    /** Pointer to a function that recovers from an error found in the input stream.
     *  Generally, this will be a #ANTLR3_EXCEPTION_NOVIABLE_ALT but it could also
     *  be from a mismatched token that the (*match)() could not recover from.
     */
    void		(*recover)		    (void * recognizer,  pANTLR3_INT_STREAM	input);

    /** Pointer to a function that is a hook to listen to token consumption during error recovery.
     *  This is mainly used by the debug parser to send events to the listener.
     */
    void		(*beginResync)		    (void * recognizer);

    /** Pointer to a function that is a hook to listen to token consumption during error recovery.
     *  This is mainly used by the debug parser to send events to the listener.
     */
    void		(*endResync)		    (void * recognizer);

    /** Pointer to a function to computer the error recovery set for the current rule.
     *  \see antlr3ComputeErrorRecoverySet() for details.
     */
    pANTLR3_BITSET	(*computeErrorRecoverySet)  (void * recognizer);

    /** Pointer to a function that computes the context-sensitive FOLLOW set for the 
     *  current rule.
     * \see antlr3ComputeCSRuleFollow() for details.
     */
    pANTLR3_BITSET	(*computeCSRuleFollow)	    (void * recognizer);

    /** Pointer to a function to combine follow bitsets.
     * \see antlr3CombineFollows() for details.
     */
    pANTLR3_BITSET	(*combineFollows)	    (void * recognizer, 
							    ANTLR3_BOOLEAN exact);
 
    /** Pointer to a function that recovers from a mismatched token in the input stream.
     * \see antlr3RecoverMismatch() for details.
     */
    void		(*recoverFromMismatchedToken)
						    (void * recognizer,  pANTLR3_INT_STREAM	input,
							    ANTLR3_UINT32	ttype,
							    pANTLR3_BITSET	follow);

    /** Pointer to a function that recoverers from a mismatched set in the token stream, in a similar manner
     *  to (*recoverFromMismatchedToken)
     */
    void		(*recoverFromMismatchedSet) (void * recognizer,  pANTLR3_INT_STREAM	input,
							    pANTLR3_BITSET	follow);

    /** Pointer to common routine to handle single token insertion for recovery functions.
     */
    ANTLR3_BOOLEAN	(*recoverFromMismatchedElement)
						    (void * recognizer,  pANTLR3_INT_STREAM	input,
							    pANTLR3_BITSET	follow);
    
    /** Pointer to function that consumes input until the next token matches
     *  the given token.
     */
    void		(*consumeUntil)		    (void * recognizer,  pANTLR3_INT_STREAM	input,
							    ANTLR3_UINT32   tokenType);

    /** Pointer to function that consumes input until the next token matches
     *  one in the given set.
     */
    void		(*consumeUntilSet)	    (void * recognizer,  pANTLR3_INT_STREAM	input,
							    pANTLR3_BITSET	set);

    /** Pointer to function that returns an ANTLR3_LIST of the strings that identify
     *  the rules in the parser that got you to this point. Can be overridden by installing your
     *	own function set.
     *
     * \todo Document how to override invocation stack functions.
     */
    pANTLR3_STACK	(*getRuleInvocationStack)	(void * recognizer);
    pANTLR3_STACK	(*getRuleInvocationStackNamed)  (void * recognizer,
								pANTLR3_UINT8	    name);

    /** Pointer to a function that converts an ANLR3_LIST of tokens to an ANTLR3_LIST of
     *  string token names. As this is mostly used in string template processing it may not be useful
     *  in the C runtime.
     */
    pANTLR3_HASH_TABLE	(*toStrings)			(void * recognizer,
								pANTLR3_HASH_TABLE);

    /** Pointer to a function to return whether the rule has parsed input starting at the supplied 
     *  start index before. If the rule has not parsed input starting from the supplied start index,
     *  then it will return ANTLR3_MEMO_RULE_UNKNOWN. If it has parsed from the suppled start point
     *  then it will return the point where it last stopped parsing after that start point.
     */
    ANTLR3_UINT64	(*getRuleMemoization)		(void * recognizer,
								ANTLR3_UINT32	ruleIndex,
								ANTLR3_UINT64	ruleParseStart);

    /** Pointer to function that determines whether the rule has parsed input at the current index
     *  in the input stream
     */
    ANTLR3_BOOLEAN	(*alreadyParsedRule)		(void * recognizer,  pANTLR3_INT_STREAM	input,
								ANTLR3_UINT32	ruleIndex);

    /** Pointer to function that records whether the rule has parsed the input at a 
     *  current position successfully or not.
     */
    void		(*memoize)			(void * recognizer,  pANTLR3_INT_STREAM	input,
								ANTLR3_UINT32	ruleIndex,
								ANTLR3_UINT64	ruleParseStart);

    /** Pointer to a function that returns whether the supplied grammar function
     *  will parse the current input stream otr not. This is the way that syntactic
     *  predicates are evaluated. Unlike java, C is pefectly happy to invoke code
     *  via a pointer to a function (hence that's what all teh ANTLR3 C interfaces 
     *  do.
     */
    ANTLR3_BOOLEAN	(*synpred)			(void * recognizer,  void * ctx, pANTLR3_INT_STREAM	input,
								void (*predicate)(void * ctx));

    /** Pointer to a function that knows how to free the resources of a base recongizer.
     */
    void		(*free)				(void * recognizer);

}
    ANTLR3_BASE_RECOGNIZER;



#endif	    /* _ANTLR3_BASERECOGNIZER_H	*/

