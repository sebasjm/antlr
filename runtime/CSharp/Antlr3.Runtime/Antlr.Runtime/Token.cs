namespace Antlr.Runtime
{
    using System;

    [Serializable]
    public abstract class Token : IToken
    {
        public const int DEFAULT_CHANNEL = 0;
        public const int DOWN = 2;
        public static readonly int EOF = -1;
        public static readonly Token EOF_TOKEN = new CommonToken(EOF);
        public const int EOR_TOKEN_TYPE = 1;
        public const int HIDDEN_CHANNEL = 0x63;
        public static readonly Token INVALID_TOKEN = new CommonToken(0);
        public const int INVALID_TOKEN_TYPE = 0;
        public static readonly int MIN_TOKEN_TYPE = 4;
        public static readonly Token SKIP_TOKEN = new CommonToken(0);
        public const int UP = 3;

        protected Token()
        {
        }

        public abstract int Channel { get; set; }

        public abstract int CharPositionInLine { get; set; }

        public abstract int Line { get; set; }

        public virtual string Text
        {
            get
            {
                return null;
            }
            set
            {
            }
        }

        public abstract int TokenIndex { get; set; }

        public abstract int Type { get; set; }
    }
}

