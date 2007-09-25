namespace antlr
{
    using System;

    public abstract class TokenCreator
    {
        protected TokenCreator()
        {
        }

        public abstract IToken Create();

        public abstract string TokenTypeName { get; }
    }
}

