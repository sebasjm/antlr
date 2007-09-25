namespace antlr.debug
{
    using System;

    public class TraceEventArgs : GuessingEventArgs
    {
        private int data_;
        public static int DONE_PARSING = 2;
        public static int ENTER = 0;
        public static int EXIT = 1;
        private int ruleNum_;

        public TraceEventArgs()
        {
        }

        public TraceEventArgs(int type, int ruleNum, int guessing, int data)
        {
            this.setValues(type, ruleNum, guessing, data);
        }

        internal void setValues(int type, int ruleNum, int guessing, int data)
        {
            base.setValues(type, guessing);
            this.RuleNum = ruleNum;
            this.Data = data;
        }

        public override string ToString()
        {
            return string.Concat(new object[] { "ParserTraceEvent [", (this.Type == ENTER) ? "enter," : "exit,", this.RuleNum, ",", this.Guessing, "]" });
        }

        public virtual int Data
        {
            get
            {
                return this.data_;
            }
            set
            {
                this.data_ = value;
            }
        }

        public virtual int RuleNum
        {
            get
            {
                return this.ruleNum_;
            }
            set
            {
                this.ruleNum_ = value;
            }
        }
    }
}

