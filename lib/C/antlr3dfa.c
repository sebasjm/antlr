/** \file 
 * Default implementation of the DFA and DFA_STATE classes.
 */
#include    <antlr3dfa.h>

static	ANTLR3_UINT32	    dfaStatePredict	    (pANTLR3_DFA dfastate, pANTLR3_INT_STREAM input, pANTLR3_DFA_STATE start);
static	pANTLR3_DFA_STATE   dfaStateTransition	    (pANTLR3_DFA dfastate, pANTLR3_INT_STREAM input);
static	void		    dfaStateFree	    (pANTLR3_DFA_STATE state);
static	void		    dfaFree		    (pANTLR3_DFA dfa);

ANTLR3_API pANTLR3_DFA
antlr3DFANew()
{
    pANTLR3_DFA	dfa;

    /* Allocate memory
     */
    dfa	= (pANTLR3_DFA)ANTLR3_MALLOC(sizeof(ANTLR3_DFA));

    dfa->me	= dfa;
    dfa->free	= dfaFree;

    return  dfa;
}
ANTLR3_API pANTLR3_DFA_STATE
antlr3DFAStateNew(pANTLR3_DFA dfa)
{
    pANTLR3_DFA_STATE	state;

    /* Allocate memory
     */
    state	= (pANTLR3_DFA_STATE)ANTLR3_MALLOC(sizeof(ANTLR3_DFA_STATE));

    state->dfa		= dfa;
    state->me		= state;
    state->predict	= dfaStatePredict;
    state->transition	= dfaStateTransition;
    state->alt		= 0;			/* Predict nothing, unless by random chance, like any psychic	*/
    return state;
}

static	void
dfaStateFree	    (pANTLR3_DFA_STATE state)
{
    ANTLR3_FREE(state);
}

static	void
dfaFree		    (pANTLR3_DFA dfa)
{
    ANTLR3_FREE(dfa);
}

#ifdef	WIN32
#pragma warning( disable : 4100 )
#endif

static	ANTLR3_UINT32	    
dfaStatePredict	    (pANTLR3_DFA dfastate, pANTLR3_INT_STREAM input, pANTLR3_DFA_STATE start)
{
    ANTLR3_UINT64	mark;
    
    /* State we are going to predict.
     */
    pANTLR3_DFA_STATE	s;

    /* Mark the input stream for rewind
     */
    mark    = input->mark(input->me);

    /* Seemingly inifinite loop, breaks when we predict an alt
     */
    s	    = start;
    for	(;;)
    {
	s   = s->transition(s->me, input);

	if  (s == NULL)
	{
	    /* We have an issue, here's a tissue! Choose alt 1 in 
	     * desperation.
	     */
	    return  (ANTLR3_UINT32)1;
	}

	if  (s->alt > 0)
	{
	    /* An alt has been predicted, break the loop
	     */
	    break;
	}

	/* Nothing was predicited yet, consume a token go for it again with current state
	 */
	input->consume(input->me);
    }

    /* OK - we have predicted a token, we can rewind from the lookahead and retrun the
     * alt we predicited.
     */
    input->rewind(input->me, mark);

    return  s->alt;
}

static	pANTLR3_DFA_STATE   
dfaStateTransition  (pANTLR3_DFA dfastate, pANTLR3_INT_STREAM input)
{
    /* Defaults to returning NULL if nothing else is installed by the generated
     * recognizer.
     */
    return  NULL;
}