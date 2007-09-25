namespace antlr.debug
{
    using System;

    public class MatchEventArgs : GuessingEventArgs
    {
        public static int BITSET = 1;
        public static int CHAR = 2;
        public static int CHAR_BITSET = 3;
        public static int CHAR_RANGE = 5;
        private bool inverse_;
        private bool matched_;
        public static int STRING = 4;
        private object target_;
        private string text_;
        public static int TOKEN = 0;
        private int val_;

        public MatchEventArgs()
        {
        }

        public MatchEventArgs(int type, int val, object target, string text, int guessing, bool inverse, bool matched)
        {
            this.setValues(type, val, target, text, guessing, inverse, matched);
        }

        public virtual bool isInverse()
        {
            return this.inverse_;
        }

        public virtual bool isMatched()
        {
            return this.matched_;
        }

        internal void setValues(int type, int val, object target, string text, int guessing, bool inverse, bool matched)
        {
            base.setValues(type, guessing);
            this.Value = val;
            this.Target = target;
            this.Inverse = inverse;
            this.Matched = matched;
            this.Text = text;
        }

        public override string ToString()
        {
            return string.Concat(new object[] { "ParserMatchEvent [", this.isMatched() ? "ok," : "bad,", this.isInverse() ? "NOT " : "", (this.Type == TOKEN) ? "token," : "bitset,", this.Value, ",", this.Target, ",", this.Guessing, "]" });
        }

        internal bool Inverse
        {
            set
            {
                this.inverse_ = value;
            }
        }

        internal bool Matched
        {
            set
            {
                this.matched_ = value;
            }
        }

        public virtual object Target
        {
            get
            {
                return this.target_;
            }
            set
            {
                this.target_ = value;
            }
        }

        public virtual string Text
        {
            get
            {
                return this.text_;
            }
            set
            {
                this.text_ = value;
            }
        }

        public virtual int Value
        {
            get
            {
                return this.val_;
            }
            set
            {
                this.val_ = value;
            }
        }

        public enum ParserMatchEnums
        {
            TOKEN,
            BITSET,
            CHAR,
            CHAR_BITSET,
            STRING,
            CHAR_RANGE
        }
    }
}

