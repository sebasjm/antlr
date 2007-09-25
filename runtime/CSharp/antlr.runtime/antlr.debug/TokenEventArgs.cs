namespace antlr.debug
{
    using System;

    public class TokenEventArgs : ANTLREventArgs
    {
        private int amount;
        public static int CONSUME = 1;
        public static int LA = 0;
        private int value_;

        public TokenEventArgs()
        {
        }

        public TokenEventArgs(int type, int amount, int val)
        {
            this.setValues(type, amount, val);
        }

        internal void setValues(int type, int amount, int val)
        {
            base.setValues(type);
            this.Amount = amount;
            this.Value = val;
        }

        public override string ToString()
        {
            if (this.Type == LA)
            {
                return string.Concat(new object[] { "ParserTokenEvent [LA,", this.Amount, ",", this.Value, "]" });
            }
            return ("ParserTokenEvent [consume,1," + this.Value + "]");
        }

        public virtual int Amount
        {
            get
            {
                return this.amount;
            }
            set
            {
                this.amount = value;
            }
        }

        public virtual int Value
        {
            get
            {
                return this.value_;
            }
            set
            {
                this.value_ = value;
            }
        }
    }
}

