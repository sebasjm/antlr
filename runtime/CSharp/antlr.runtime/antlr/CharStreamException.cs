namespace antlr
{
    using System;

    [Serializable]
    public class CharStreamException : ANTLRException
    {
        public CharStreamException(string s) : base(s)
        {
        }
    }
}

