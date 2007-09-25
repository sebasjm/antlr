namespace antlr
{
    using antlr.collections.impl;
    using System;
    using System.Text;

    [Serializable]
    public class MismatchedCharException : RecognitionException
    {
        public BitSet bset;
        public int expecting;
        public int foundChar;
        public CharTypeEnum mismatchType;
        public CharScanner scanner;
        public int upper;

        public MismatchedCharException() : base("Mismatched char")
        {
        }

        public MismatchedCharException(char c, BitSet set_, bool matchNot, CharScanner scanner_) : base("Mismatched char", scanner_.getFilename(), scanner_.getLine(), scanner_.getColumn())
        {
            this.mismatchType = matchNot ? CharTypeEnum.NotSetType : CharTypeEnum.SetType;
            this.foundChar = c;
            this.bset = set_;
            this.scanner = scanner_;
        }

        public MismatchedCharException(char c, char expecting_, bool matchNot, CharScanner scanner_) : base("Mismatched char", scanner_.getFilename(), scanner_.getLine(), scanner_.getColumn())
        {
            this.mismatchType = matchNot ? CharTypeEnum.NotCharType : CharTypeEnum.CharType;
            this.foundChar = c;
            this.expecting = expecting_;
            this.scanner = scanner_;
        }

        public MismatchedCharException(char c, char lower, char upper_, bool matchNot, CharScanner scanner_) : base("Mismatched char", scanner_.getFilename(), scanner_.getLine(), scanner_.getColumn())
        {
            this.mismatchType = matchNot ? CharTypeEnum.NotRangeType : CharTypeEnum.RangeType;
            this.foundChar = c;
            this.expecting = lower;
            this.upper = upper_;
            this.scanner = scanner_;
        }

        private void appendCharName(StringBuilder sb, int c)
        {
            switch (c)
            {
                case 9:
                    sb.Append(@"'\t'");
                    return;

                case 10:
                    sb.Append(@"'\n'");
                    return;

                case 13:
                    sb.Append(@"'\r'");
                    return;

                case 0xffff:
                    sb.Append("'<EOF>'");
                    return;
            }
            sb.Append('\'');
            sb.Append((char) c);
            sb.Append('\'');
        }

        public override string Message
        {
            get
            {
                StringBuilder sb = new StringBuilder();
                switch (this.mismatchType)
                {
                    case CharTypeEnum.CharType:
                        sb.Append("expecting ");
                        this.appendCharName(sb, this.expecting);
                        sb.Append(", found ");
                        this.appendCharName(sb, this.foundChar);
                        break;

                    case CharTypeEnum.NotCharType:
                        sb.Append("expecting anything but '");
                        this.appendCharName(sb, this.expecting);
                        sb.Append("'; got it anyway");
                        break;

                    case CharTypeEnum.RangeType:
                    case CharTypeEnum.NotRangeType:
                        sb.Append("expecting token ");
                        if (this.mismatchType == CharTypeEnum.NotRangeType)
                        {
                            sb.Append("NOT ");
                        }
                        sb.Append("in range: ");
                        this.appendCharName(sb, this.expecting);
                        sb.Append("..");
                        this.appendCharName(sb, this.upper);
                        sb.Append(", found ");
                        this.appendCharName(sb, this.foundChar);
                        break;

                    case CharTypeEnum.SetType:
                    case CharTypeEnum.NotSetType:
                    {
                        sb.Append("expecting " + ((this.mismatchType == CharTypeEnum.NotSetType) ? "NOT " : "") + "one of (");
                        int[] numArray = this.bset.toArray();
                        for (int i = 0; i < numArray.Length; i++)
                        {
                            this.appendCharName(sb, numArray[i]);
                        }
                        sb.Append("), found ");
                        this.appendCharName(sb, this.foundChar);
                        break;
                    }
                    default:
                        sb.Append(base.Message);
                        break;
                }
                return sb.ToString();
            }
        }

        public enum CharTypeEnum
        {
            CharType = 1,
            NotCharType = 2,
            NotRangeType = 4,
            NotSetType = 6,
            RangeType = 3,
            SetType = 5
        }
    }
}

