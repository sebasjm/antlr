namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using System;

    public class BlankDebugEventListener : IDebugEventListener
    {
        public virtual void AddChild(int rootID, int childID)
        {
        }

        public virtual void BecomeRoot(int newRootID, int oldRootID)
        {
        }

        public virtual void BeginBacktrack(int level)
        {
        }

        public virtual void BeginResync()
        {
        }

        public virtual void Commence()
        {
        }

        public virtual void ConsumeHiddenToken(IToken token)
        {
        }

        public virtual void ConsumeNode(int ID, string text, int type)
        {
        }

        public virtual void ConsumeToken(IToken token)
        {
        }

        public virtual void CreateNode(int ID, int tokenIndex)
        {
        }

        public virtual void CreateNode(int ID, string text, int type)
        {
        }

        public virtual void EndBacktrack(int level, bool successful)
        {
        }

        public virtual void EndResync()
        {
        }

        public virtual void EnterAlt(int alt)
        {
        }

        public virtual void EnterDecision(int decisionNumber)
        {
        }

        public virtual void EnterRule(string ruleName)
        {
        }

        public virtual void EnterSubRule(int decisionNumber)
        {
        }

        public virtual void ExitDecision(int decisionNumber)
        {
        }

        public virtual void ExitRule(string ruleName)
        {
        }

        public virtual void ExitSubRule(int decisionNumber)
        {
        }

        public virtual void GetNilNode(int ID)
        {
        }

        public virtual void Location(int line, int pos)
        {
        }

        public virtual void LT(int i, IToken t)
        {
        }

        public virtual void LT(int i, int ID, string text, int type)
        {
        }

        public virtual void Mark(int i)
        {
        }

        public virtual void RecognitionException(Antlr.Runtime.RecognitionException e)
        {
        }

        public virtual void Rewind()
        {
        }

        public virtual void Rewind(int i)
        {
        }

        public virtual void SemanticPredicate(bool result, string predicate)
        {
        }

        public virtual void SetTokenBoundaries(int ID, int tokenStartIndex, int tokenStopIndex)
        {
        }

        public virtual void Terminate()
        {
        }
    }
}

