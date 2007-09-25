namespace Antlr.Runtime.Tree
{
    using Antlr.Runtime;
    using System;

    public interface ITreeAdaptor
    {
        void AddChild(object t, object child);
        object BecomeRoot(IToken newRoot, object oldRoot);
        object BecomeRoot(object newRoot, object oldRoot);
        object Create(IToken payload);
        object Create(int tokenType, IToken fromToken);
        object Create(int tokenType, string text);
        object Create(int tokenType, IToken fromToken, string text);
        object DupNode(object treeNode);
        object DupTree(object tree);
        object GetChild(object t, int i);
        int GetChildCount(object t);
        object GetNilNode();
        string GetNodeText(object t);
        int GetNodeType(object t);
        IToken GetToken(object treeNode);
        int GetTokenStartIndex(object t);
        int GetTokenStopIndex(object t);
        int GetUniqueID(object node);
        bool IsNil(object tree);
        object RulePostProcessing(object root);
        void SetNodeText(object t, string text);
        void SetNodeType(object t, int type);
        void SetTokenBoundaries(object t, IToken startToken, IToken stopToken);
    }
}

