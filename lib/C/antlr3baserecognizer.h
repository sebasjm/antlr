/** \file
 * Defines the basic structure to support recognizing by either a lexer,
 * parser, or tree parser.
 */

/** \brief Base tracking context structure for all types of
 * recognizers.
 */
typedef	struct ANTLR3_BASE_RECOGNIZER_struct
{
    /** Track the set of token types that can follow any rule invocation.
     *  Hashtable as place holder but will change to new Stack structure
     *  shortly, to support: List<BitSet>.
     */
    pANTLR3_HASH_TABLE	 following;

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
    ANTLR3_UINT64	lastErrorIndex;

    /** In lieu of a return value, this indicates that a rule or token
     *  has failed to match.  Reset to false upon valid token match.
     */
    ANTLR3_BOOLEAN	failed;

    /** If 0, no backtracking is going on.  Safe to exec actions etc...
     *  If >0 then it's the level of backtracking.
     */
    ANTLR3_INT32	backtracking;

    /** Pointer to function to reset the parser's state
     */
    void		(*reset)(void);

}
    ANTLR3_BASE_RECOGNIZER, *pANTLR3_BASE_RECOGNIZER;