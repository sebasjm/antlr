namespace antlr
{
    using System;
    using System.Text;

    public class ParseTreeToken : ParseTree
    {
        protected IToken token;

        public ParseTreeToken(IToken token)
        {
            this.token = token;
        }

        protected internal override int getLeftmostDerivation(StringBuilder buf, int step)
        {
            buf.Append(' ');
            buf.Append(this.ToString());
            return step;
        }

        public override string ToString()
        {
            if (this.token != null)
            {
                return this.token.getText();
            }
            return "<missing token>";
        }
    }
}

