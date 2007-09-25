namespace antlr
{
    using antlr.collections;
    using System;
    using System.Text;

    public class ParseTreeRule : ParseTree
    {
        protected int altNumber;
        public const int INVALID_ALT = -1;
        protected string ruleName;

        public ParseTreeRule(string ruleName) : this(ruleName, -1)
        {
        }

        public ParseTreeRule(string ruleName, int altNumber)
        {
            this.ruleName = ruleName;
            this.altNumber = altNumber;
        }

        protected internal override int getLeftmostDerivation(StringBuilder buf, int step)
        {
            int num = 0;
            if (step <= 0)
            {
                buf.Append(' ');
                buf.Append(this.ToString());
                return num;
            }
            AST ast = this.getFirstChild();
            num = 1;
            while (ast != null)
            {
                if ((num >= step) || (ast is ParseTreeToken))
                {
                    buf.Append(' ');
                    buf.Append(ast.ToString());
                }
                else
                {
                    int num2 = step - num;
                    int num3 = ((ParseTree) ast).getLeftmostDerivation(buf, num2);
                    num += num3;
                }
                ast = ast.getNextSibling();
            }
            return num;
        }

        public string getRuleName()
        {
            return this.ruleName;
        }

        public override string ToString()
        {
            if (this.altNumber == -1)
            {
                return ('<' + this.ruleName + '>');
            }
            return string.Concat(new object[] { '<', this.ruleName, "[", this.altNumber, "]>" });
        }
    }
}

