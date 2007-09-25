namespace antlr
{
    using System;

    public class LLkParser : Parser
    {
        internal int k;

        public LLkParser(int k_)
        {
            this.k = k_;
        }

        public LLkParser(ParserSharedInputState state, int k_)
        {
            this.k = k_;
            base.inputState = state;
        }

        public LLkParser(TokenBuffer tokenBuf, int k_)
        {
            this.k = k_;
            this.setTokenBuffer(tokenBuf);
        }

        public LLkParser(TokenStream lexer, int k_)
        {
            this.k = k_;
            TokenBuffer t = new TokenBuffer(lexer);
            this.setTokenBuffer(t);
        }

        public override void consume()
        {
            base.inputState.input.consume();
        }

        public override int LA(int i)
        {
            return base.inputState.input.LA(i);
        }

        public override IToken LT(int i)
        {
            return base.inputState.input.LT(i);
        }

        private void trace(string ee, string rname)
        {
            this.traceIndent();
            Console.Out.Write(ee + rname + ((base.inputState.guessing > 0) ? "; [guessing]" : "; "));
            for (int i = 1; i <= this.k; i++)
            {
                if (i != 1)
                {
                    Console.Out.Write(", ");
                }
                if (this.LT(i) != null)
                {
                    Console.Out.Write(string.Concat(new object[] { "LA(", i, ")==", this.LT(i).getText() }));
                }
                else
                {
                    Console.Out.Write("LA(" + i + ")==ull");
                }
            }
            Console.Out.WriteLine("");
        }

        public override void traceIn(string rname)
        {
            base.traceDepth++;
            this.trace("> ", rname);
        }

        public override void traceOut(string rname)
        {
            this.trace("< ", rname);
            base.traceDepth--;
        }
    }
}

