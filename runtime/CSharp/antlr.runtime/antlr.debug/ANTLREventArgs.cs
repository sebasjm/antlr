namespace antlr.debug
{
    using System;

    public abstract class ANTLREventArgs : EventArgs
    {
        private int type_;

        public ANTLREventArgs()
        {
        }

        public ANTLREventArgs(int type)
        {
            this.Type = type;
        }

        internal void setValues(int type)
        {
            this.Type = type;
        }

        public virtual int Type
        {
            get
            {
                return this.type_;
            }
            set
            {
                this.type_ = value;
            }
        }
    }
}

