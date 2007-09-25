namespace antlr
{
    using System;
    using System.IO;

    [Serializable]
    public class TokenStreamIOException : TokenStreamException
    {
        public IOException io;

        public TokenStreamIOException(IOException io) : base(io.Message)
        {
            this.io = io;
        }
    }
}

