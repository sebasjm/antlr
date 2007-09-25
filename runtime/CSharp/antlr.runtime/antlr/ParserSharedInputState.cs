namespace antlr
{
    using System;

    public class ParserSharedInputState
    {
        protected internal string filename;
        public int guessing = 0;
        protected internal TokenBuffer input;

        public virtual void reset()
        {
            this.guessing = 0;
            this.filename = null;
            this.input.reset();
        }
    }
}

