namespace Antlr.Runtime
{
    using Antlr.Runtime.Tree;
    using System;

    [Serializable]
    public class MismatchedTreeNodeException : RecognitionException
    {
        public int expecting;

        public MismatchedTreeNodeException()
        {
        }

        public MismatchedTreeNodeException(int expecting, ITreeNodeStream input) : base(input)
        {
            ITree tree = (ITree) input.LT(1);
            if (input.LT(1) is ITree)
            {
                base.Line = tree.Line;
                base.CharPositionInLine = tree.CharPositionInLine;
            }
            this.expecting = expecting;
        }

        public override string ToString()
        {
            return string.Concat(new object[] { "MismatchedTreeNodeException(", this.UnexpectedType, "!=", this.expecting, ")" });
        }
    }
}

