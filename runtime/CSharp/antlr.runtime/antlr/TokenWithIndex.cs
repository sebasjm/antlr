namespace antlr
{
    using System;

    public class TokenWithIndex : CommonToken
    {
        private int index;

        public TokenWithIndex()
        {
        }

        public TokenWithIndex(int i, string t) : base(i, t)
        {
        }

        public int getIndex()
        {
            return this.index;
        }

        public void setIndex(int i)
        {
            this.index = i;
        }

        public override string ToString()
        {
            return string.Concat(new object[] { "[", this.index, ":\"", this.getText(), "\",<", base.Type, ">,line=", base.line, ",col=", base.col, "]\n" });
        }
    }
}

