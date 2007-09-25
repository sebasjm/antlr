namespace antlr.debug
{
    using System;

    public class NewLineEventArgs : ANTLREventArgs
    {
        private int line_;

        public NewLineEventArgs()
        {
        }

        public NewLineEventArgs(int line)
        {
            this.Line = line;
        }

        public override string ToString()
        {
            return ("NewLineEvent [" + this.line_ + "]");
        }

        public virtual int Line
        {
            get
            {
                return this.line_;
            }
            set
            {
                this.line_ = value;
            }
        }
    }
}

