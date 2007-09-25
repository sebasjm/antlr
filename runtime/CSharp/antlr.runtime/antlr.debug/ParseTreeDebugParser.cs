namespace antlr.debug
{
    using antlr;
    using antlr.collections.impl;
    using System;
    using System.Collections;

    public class ParseTreeDebugParser : LLkParser
    {
        protected Stack currentParseTreeRoot;
        protected ParseTreeRule mostRecentParseTreeRoot;
        protected int numberOfDerivationSteps;

        public ParseTreeDebugParser(int k_) : base(k_)
        {
            this.currentParseTreeRoot = new Stack();
            this.mostRecentParseTreeRoot = null;
            this.numberOfDerivationSteps = 1;
        }

        public ParseTreeDebugParser(ParserSharedInputState state, int k_) : base(state, k_)
        {
            this.currentParseTreeRoot = new Stack();
            this.mostRecentParseTreeRoot = null;
            this.numberOfDerivationSteps = 1;
        }

        public ParseTreeDebugParser(TokenBuffer tokenBuf, int k_) : base(tokenBuf, k_)
        {
            this.currentParseTreeRoot = new Stack();
            this.mostRecentParseTreeRoot = null;
            this.numberOfDerivationSteps = 1;
        }

        public ParseTreeDebugParser(TokenStream lexer, int k_) : base(lexer, k_)
        {
            this.currentParseTreeRoot = new Stack();
            this.mostRecentParseTreeRoot = null;
            this.numberOfDerivationSteps = 1;
        }

        protected void addCurrentTokenToParseTree()
        {
            if (base.inputState.guessing <= 0)
            {
                ParseTreeRule rule = (ParseTreeRule) this.currentParseTreeRoot.Peek();
                ParseTreeToken node = null;
                if (this.LA(1) == 1)
                {
                    node = new ParseTreeToken(new CommonToken("EOF"));
                }
                else
                {
                    node = new ParseTreeToken(this.LT(1));
                }
                rule.addChild(node);
            }
        }

        public int getNumberOfDerivationSteps()
        {
            return this.numberOfDerivationSteps;
        }

        public ParseTree getParseTree()
        {
            return this.mostRecentParseTreeRoot;
        }

        public override void match(BitSet bitSet)
        {
            this.addCurrentTokenToParseTree();
            base.match(bitSet);
        }

        public override void match(int i)
        {
            this.addCurrentTokenToParseTree();
            base.match(i);
        }

        public override void matchNot(int i)
        {
            this.addCurrentTokenToParseTree();
            base.matchNot(i);
        }

        public override void traceIn(string s)
        {
            if (base.inputState.guessing <= 0)
            {
                ParseTreeRule node = new ParseTreeRule(s);
                if (this.currentParseTreeRoot.Count > 0)
                {
                    ((ParseTreeRule) this.currentParseTreeRoot.Peek()).addChild(node);
                }
                this.currentParseTreeRoot.Push(node);
                this.numberOfDerivationSteps++;
            }
        }

        public override void traceOut(string s)
        {
            if (base.inputState.guessing <= 0)
            {
                this.mostRecentParseTreeRoot = (ParseTreeRule) this.currentParseTreeRoot.Pop();
            }
        }
    }
}

