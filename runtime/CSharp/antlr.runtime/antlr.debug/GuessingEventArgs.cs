namespace antlr.debug
{
    using System;

    public abstract class GuessingEventArgs : ANTLREventArgs
    {
        private int guessing_;

        public GuessingEventArgs()
        {
        }

        public GuessingEventArgs(int type) : base(type)
        {
        }

        public virtual void setValues(int type, int guessing)
        {
            base.setValues(type);
            this.Guessing = guessing;
        }

        public virtual int Guessing
        {
            get
            {
                return this.guessing_;
            }
            set
            {
                this.guessing_ = value;
            }
        }
    }
}

