namespace antlr
{
    using System;

    public class TreeParserSharedInputState
    {
        public int guessing = 0;

        public virtual void reset()
        {
            this.guessing = 0;
        }
    }
}

