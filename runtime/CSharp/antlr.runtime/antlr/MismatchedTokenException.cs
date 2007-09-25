namespace antlr
{
    using antlr.collections;
    using antlr.collections.impl;
    using System;
    using System.Text;

    [Serializable]
    public class MismatchedTokenException : RecognitionException
    {
        public BitSet bset;
        public int expecting;
        public TokenTypeEnum mismatchType;
        public AST node;
        public IToken token;
        internal string[] tokenNames;
        internal string tokenText;
        public int upper;

        public MismatchedTokenException() : base("Mismatched Token: expecting any AST node", "<AST>", -1, -1)
        {
            this.tokenText = null;
        }

        public MismatchedTokenException(string[] tokenNames_, AST node_, BitSet set_, bool matchNot) : base("Mismatched Token", "<AST>", -1, -1)
        {
            this.tokenText = null;
            this.tokenNames = tokenNames_;
            this.node = node_;
            if (node_ == null)
            {
                this.tokenText = "<empty tree>";
            }
            else
            {
                this.tokenText = node_.ToString();
            }
            this.mismatchType = matchNot ? TokenTypeEnum.NotSetType : TokenTypeEnum.SetType;
            this.bset = set_;
        }

        public MismatchedTokenException(string[] tokenNames_, AST node_, int expecting_, bool matchNot) : base("Mismatched Token", "<AST>", -1, -1)
        {
            this.tokenText = null;
            this.tokenNames = tokenNames_;
            this.node = node_;
            if (node_ == null)
            {
                this.tokenText = "<empty tree>";
            }
            else
            {
                this.tokenText = node_.ToString();
            }
            this.mismatchType = matchNot ? TokenTypeEnum.NotTokenType : TokenTypeEnum.TokenType;
            this.expecting = expecting_;
        }

        public MismatchedTokenException(string[] tokenNames_, AST node_, int lower, int upper_, bool matchNot) : base("Mismatched Token", "<AST>", -1, -1)
        {
            this.tokenText = null;
            this.tokenNames = tokenNames_;
            this.node = node_;
            if (node_ == null)
            {
                this.tokenText = "<empty tree>";
            }
            else
            {
                this.tokenText = node_.ToString();
            }
            this.mismatchType = matchNot ? TokenTypeEnum.NotRangeType : TokenTypeEnum.RangeType;
            this.expecting = lower;
            this.upper = upper_;
        }

        public MismatchedTokenException(string[] tokenNames_, IToken token_, BitSet set_, bool matchNot, string fileName_) : base("Mismatched Token", fileName_, token_.getLine(), token_.getColumn())
        {
            this.tokenText = null;
            this.tokenNames = tokenNames_;
            this.token = token_;
            this.tokenText = token_.getText();
            this.mismatchType = matchNot ? TokenTypeEnum.NotSetType : TokenTypeEnum.SetType;
            this.bset = set_;
        }

        public MismatchedTokenException(string[] tokenNames_, IToken token_, int expecting_, bool matchNot, string fileName_) : base("Mismatched Token", fileName_, token_.getLine(), token_.getColumn())
        {
            this.tokenText = null;
            this.tokenNames = tokenNames_;
            this.token = token_;
            this.tokenText = token_.getText();
            this.mismatchType = matchNot ? TokenTypeEnum.NotTokenType : TokenTypeEnum.TokenType;
            this.expecting = expecting_;
        }

        public MismatchedTokenException(string[] tokenNames_, IToken token_, int lower, int upper_, bool matchNot, string fileName_) : base("Mismatched Token", fileName_, token_.getLine(), token_.getColumn())
        {
            this.tokenText = null;
            this.tokenNames = tokenNames_;
            this.token = token_;
            this.tokenText = token_.getText();
            this.mismatchType = matchNot ? TokenTypeEnum.NotRangeType : TokenTypeEnum.RangeType;
            this.expecting = lower;
            this.upper = upper_;
        }

        private string tokenName(int tokenType)
        {
            if (tokenType == 0)
            {
                return "<Set of tokens>";
            }
            if ((tokenType < 0) || (tokenType >= this.tokenNames.Length))
            {
                return ("<" + tokenType.ToString() + ">");
            }
            return this.tokenNames[tokenType];
        }

        public override string Message
        {
            get
            {
                StringBuilder builder = new StringBuilder();
                switch (this.mismatchType)
                {
                    case TokenTypeEnum.TokenType:
                        builder.Append("expecting " + this.tokenName(this.expecting) + ", found '" + this.tokenText + "'");
                        break;

                    case TokenTypeEnum.NotTokenType:
                        builder.Append("expecting anything but " + this.tokenName(this.expecting) + "; got it anyway");
                        break;

                    case TokenTypeEnum.RangeType:
                        builder.Append("expecting token in range: " + this.tokenName(this.expecting) + ".." + this.tokenName(this.upper) + ", found '" + this.tokenText + "'");
                        break;

                    case TokenTypeEnum.NotRangeType:
                        builder.Append("expecting token NOT in range: " + this.tokenName(this.expecting) + ".." + this.tokenName(this.upper) + ", found '" + this.tokenText + "'");
                        break;

                    case TokenTypeEnum.SetType:
                    case TokenTypeEnum.NotSetType:
                    {
                        builder.Append("expecting " + ((this.mismatchType == TokenTypeEnum.NotSetType) ? "NOT " : "") + "one of (");
                        int[] numArray = this.bset.toArray();
                        for (int i = 0; i < numArray.Length; i++)
                        {
                            builder.Append(" ");
                            builder.Append(this.tokenName(numArray[i]));
                        }
                        builder.Append("), found '" + this.tokenText + "'");
                        break;
                    }
                    default:
                        builder.Append(base.Message);
                        break;
                }
                return builder.ToString();
            }
        }

        public enum TokenTypeEnum
        {
            NotRangeType = 4,
            NotSetType = 6,
            NotTokenType = 2,
            RangeType = 3,
            SetType = 5,
            TokenType = 1
        }
    }
}

