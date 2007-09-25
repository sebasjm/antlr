namespace Antlr.Runtime
{
    using System;

    public class Parser : BaseRecognizer
    {
        protected internal ITokenStream input;

        public Parser(ITokenStream input)
        {
            this.TokenStream = input;
        }

        public override void Reset()
        {
            base.Reset();
            if (this.input != null)
            {
                this.input.Seek(0);
            }
        }

        public virtual void TraceIn(string ruleName, int ruleIndex)
        {
            base.TraceIn(ruleName, ruleIndex, this.input.LT(1));
        }

        public virtual void TraceOut(string ruleName, int ruleIndex)
        {
            base.TraceOut(ruleName, ruleIndex, this.input.LT(1));
        }

        public override IIntStream Input
        {
            get
            {
                return this.input;
            }
        }

        public virtual ITokenStream TokenStream
        {
            get
            {
                return this.input;
            }
            set
            {
                this.input = null;
                this.Reset();
                this.input = value;
            }
        }
    }
}

