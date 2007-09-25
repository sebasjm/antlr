namespace Antlr.Runtime.Tree
{
    using Antlr.Runtime;
    using System;

    public interface ITreeNodeStream : IIntStream
    {
        object Get(int i);
        object LT(int k);
        string ToString(object start, object stop);

        bool HasUniqueNavigationNodes { set; }

        ITokenStream TokenStream { get; }

        ITreeAdaptor TreeAdaptor { get; }

        object TreeSource { get; }
    }
}

