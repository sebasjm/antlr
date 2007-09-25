namespace antlr.debug
{
    using System;

    public class SemanticPredicateEventArgs : GuessingEventArgs
    {
        private int condition_;
        public const int PREDICTING = 1;
        private bool result_;
        public const int VALIDATING = 0;

        public SemanticPredicateEventArgs()
        {
        }

        public SemanticPredicateEventArgs(int type) : base(type)
        {
        }

        internal void setValues(int type, int condition, bool result, int guessing)
        {
            base.setValues(type, guessing);
            this.Condition = condition;
            this.Result = result;
        }

        public override string ToString()
        {
            return string.Concat(new object[] { "SemanticPredicateEvent [", this.Condition, ",", this.Result, ",", this.Guessing, "]" });
        }

        public virtual int Condition
        {
            get
            {
                return this.condition_;
            }
            set
            {
                this.condition_ = value;
            }
        }

        public virtual bool Result
        {
            get
            {
                return this.result_;
            }
            set
            {
                this.result_ = value;
            }
        }
    }
}

