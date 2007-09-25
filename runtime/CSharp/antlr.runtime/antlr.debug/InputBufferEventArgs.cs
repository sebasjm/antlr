namespace antlr.debug
{
    using System;

    public class InputBufferEventArgs : ANTLREventArgs
    {
        internal char c_;
        public const int CONSUME = 0;
        public const int LA = 1;
        internal int lookaheadAmount_;
        public const int MARK = 2;
        public const int REWIND = 3;

        public InputBufferEventArgs()
        {
        }

        public InputBufferEventArgs(int type, char c, int lookaheadAmount)
        {
            this.setValues(type, c, lookaheadAmount);
        }

        internal void setValues(int type, char c, int la)
        {
            base.setValues(type);
            this.Char = c;
            this.LookaheadAmount = la;
        }

        public override string ToString()
        {
            return string.Concat(new object[] { "CharBufferEvent [", (this.Type == 0) ? "CONSUME, " : "LA, ", this.Char, ",", this.LookaheadAmount, "]" });
        }

        public virtual char Char
        {
            get
            {
                return this.c_;
            }
            set
            {
                this.c_ = value;
            }
        }

        public virtual int LookaheadAmount
        {
            get
            {
                return this.lookaheadAmount_;
            }
            set
            {
                this.lookaheadAmount_ = value;
            }
        }
    }
}

