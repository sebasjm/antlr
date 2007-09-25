namespace Antlr.Runtime.Tree
{
    using System;

    public interface ITree
    {
        void AddChild(ITree t);
        ITree DupNode();
        ITree DupTree();
        ITree GetChild(int i);
        string ToString();
        string ToStringTree();

        int CharPositionInLine { get; }

        int ChildCount { get; }

        bool IsNil { get; }

        int Line { get; }

        string Text { get; }

        int TokenStartIndex { get; set; }

        int TokenStopIndex { get; set; }

        int Type { get; }
    }
}

