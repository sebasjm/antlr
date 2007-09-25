namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using System;

    public interface IDebugEventListener
    {
        void AddChild(int rootID, int childID);
        void BecomeRoot(int newRootID, int oldRootID);
        void BeginBacktrack(int level);
        void BeginResync();
        void Commence();
        void ConsumeHiddenToken(IToken t);
        void ConsumeNode(int ID, string text, int type);
        void ConsumeToken(IToken t);
        void CreateNode(int ID, int tokenIndex);
        void CreateNode(int ID, string text, int type);
        void EndBacktrack(int level, bool successful);
        void EndResync();
        void EnterAlt(int alt);
        void EnterDecision(int decisionNumber);
        void EnterRule(string ruleName);
        void EnterSubRule(int decisionNumber);
        void ExitDecision(int decisionNumber);
        void ExitRule(string ruleName);
        void ExitSubRule(int decisionNumber);
        void GetNilNode(int ID);
        void Location(int line, int pos);
        void LT(int i, IToken t);
        void LT(int i, int ID, string text, int type);
        void Mark(int marker);
        void RecognitionException(Antlr.Runtime.RecognitionException e);
        void Rewind();
        void Rewind(int marker);
        void SemanticPredicate(bool result, string predicate);
        void SetTokenBoundaries(int ID, int tokenStartIndex, int tokenStopIndex);
        void Terminate();
    }
}

