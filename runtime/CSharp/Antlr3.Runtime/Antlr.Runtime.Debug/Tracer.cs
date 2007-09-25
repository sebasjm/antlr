namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using System;

    public class Tracer : BlankDebugEventListener
    {
        public IIntStream input;
        protected int level = 0;

        public Tracer(IIntStream input)
        {
            this.input = input;
        }

        public override void EnterRule(string ruleName)
        {
            for (int i = 1; i <= this.level; i++)
            {
                Console.Out.Write(" ");
            }
            Console.Out.WriteLine(string.Concat(new object[] { "> ", ruleName, " lookahead(1)=", this.GetInputSymbol(1) }));
            this.level++;
        }

        public override void ExitRule(string ruleName)
        {
            this.level--;
            for (int i = 1; i <= this.level; i++)
            {
                Console.Out.Write(" ");
            }
            Console.Out.WriteLine(string.Concat(new object[] { "< ", ruleName, " lookahead(1)=", this.GetInputSymbol(1) }));
        }

        public virtual object GetInputSymbol(int k)
        {
            if (this.input is ITokenStream)
            {
                return ((ITokenStream) this.input).LT(k);
            }
            return (char) this.input.LA(k);
        }
    }
}

