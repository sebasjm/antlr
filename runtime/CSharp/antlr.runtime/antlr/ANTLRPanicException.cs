namespace antlr
{
    using System;

    [Serializable]
    public class ANTLRPanicException : ANTLRException
    {
        public ANTLRPanicException()
        {
        }

        public ANTLRPanicException(string s) : base(s)
        {
        }

        public ANTLRPanicException(string s, Exception inner) : base(s, inner)
        {
        }
    }
}

