namespace Antlr.Runtime
{
    using System;

    [Serializable]
    public class MismatchedRangeException : RecognitionException
    {
        public int a;
        public int b;

        public MismatchedRangeException(int a, int b, IIntStream input) : base(input)
        {
            this.a = a;
            this.b = b;
        }

        public override string ToString()
        {
            return string.Concat(new object[] { "MismatchedNotSetException(", this.UnexpectedType, " not in [", this.a, ",", this.b, "])" });
        }
    }
}

