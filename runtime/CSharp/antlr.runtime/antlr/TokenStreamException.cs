namespace antlr
{
    using System;

    [Serializable]
    public class TokenStreamException : ANTLRException
    {
        public TokenStreamException()
        {
        }

        public TokenStreamException(string s) : base(s)
        {
        }
    }
}

