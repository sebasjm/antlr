namespace antlr
{
    using antlr.collections;
    using System;
    using System.Text;

    public abstract class ParseTree : BaseAST
    {
        protected ParseTree()
        {
        }

        public string getLeftmostDerivation(int maxSteps)
        {
            StringBuilder builder = new StringBuilder(0x7d0);
            builder.Append("    " + this.ToString());
            builder.Append("\n");
            for (int i = 1; i < maxSteps; i++)
            {
                builder.Append(" =>");
                builder.Append(this.getLeftmostDerivationStep(i));
                builder.Append("\n");
            }
            return builder.ToString();
        }

        protected internal abstract int getLeftmostDerivation(StringBuilder buf, int step);
        public string getLeftmostDerivationStep(int step)
        {
            if (step <= 0)
            {
                return this.ToString();
            }
            StringBuilder buf = new StringBuilder(0x7d0);
            this.getLeftmostDerivation(buf, step);
            return buf.ToString();
        }

        public override void initialize(AST ast)
        {
        }

        public override void initialize(IToken token)
        {
        }

        public override void initialize(int i, string s)
        {
        }
    }
}

