namespace antlr.debug
{
    using antlr;
    using antlr.collections.impl;
    using System;
    using System.Threading;

    public class LLkDebuggingParser : LLkParser, DebuggingParser
    {
        private bool _notDebugMode;
        protected internal ParserEventSupport parserEventSupport;
        protected internal string[] ruleNames;
        protected internal string[] semPredNames;

        public LLkDebuggingParser(int k_) : base(k_)
        {
            this._notDebugMode = false;
            this.InitBlock();
        }

        public LLkDebuggingParser(ParserSharedInputState state, int k_) : base(state, k_)
        {
            this._notDebugMode = false;
            this.InitBlock();
        }

        public LLkDebuggingParser(TokenBuffer tokenBuf, int k_) : base(tokenBuf, k_)
        {
            this._notDebugMode = false;
            this.InitBlock();
        }

        public LLkDebuggingParser(TokenStream lexer, int k_) : base(lexer, k_)
        {
            this._notDebugMode = false;
            this.InitBlock();
        }

        public override void addMessageListener(MessageListener l)
        {
            this.parserEventSupport.addMessageListener(l);
        }

        public override void addParserListener(ParserListener l)
        {
            this.parserEventSupport.addParserListener(l);
        }

        public override void addParserMatchListener(ParserMatchListener l)
        {
            this.parserEventSupport.addParserMatchListener(l);
        }

        public override void addParserTokenListener(ParserTokenListener l)
        {
            this.parserEventSupport.addParserTokenListener(l);
        }

        public override void addSemanticPredicateListener(SemanticPredicateListener l)
        {
            this.parserEventSupport.addSemanticPredicateListener(l);
        }

        public override void addSyntacticPredicateListener(SyntacticPredicateListener l)
        {
            this.parserEventSupport.addSyntacticPredicateListener(l);
        }

        public override void addTraceListener(TraceListener l)
        {
            this.parserEventSupport.addTraceListener(l);
        }

        public override void consume()
        {
            int c = -99;
            c = this.LA(1);
            base.consume();
            this.parserEventSupport.fireConsume(c);
        }

        protected internal virtual void fireEnterRule(int num, int data)
        {
            if (this.isDebugMode())
            {
                this.parserEventSupport.fireEnterRule(num, base.inputState.guessing, data);
            }
        }

        protected internal virtual void fireExitRule(int num, int data)
        {
            if (this.isDebugMode())
            {
                this.parserEventSupport.fireExitRule(num, base.inputState.guessing, data);
            }
        }

        protected internal virtual bool fireSemanticPredicateEvaluated(int type, int num, bool condition)
        {
            if (this.isDebugMode())
            {
                return this.parserEventSupport.fireSemanticPredicateEvaluated(type, num, condition, base.inputState.guessing);
            }
            return condition;
        }

        protected internal virtual void fireSyntacticPredicateFailed()
        {
            if (this.isDebugMode())
            {
                this.parserEventSupport.fireSyntacticPredicateFailed(base.inputState.guessing);
            }
        }

        protected internal virtual void fireSyntacticPredicateStarted()
        {
            if (this.isDebugMode())
            {
                this.parserEventSupport.fireSyntacticPredicateStarted(base.inputState.guessing);
            }
        }

        protected internal virtual void fireSyntacticPredicateSucceeded()
        {
            if (this.isDebugMode())
            {
                this.parserEventSupport.fireSyntacticPredicateSucceeded(base.inputState.guessing);
            }
        }

        public virtual string getRuleName(int num)
        {
            return this.ruleNames[num];
        }

        public virtual string getSemPredName(int num)
        {
            return this.semPredNames[num];
        }

        public virtual void goToSleep()
        {
            lock (this)
            {
                try
                {
                    Monitor.Wait(this);
                }
                catch (ThreadInterruptedException)
                {
                }
            }
        }

        private void InitBlock()
        {
            this.parserEventSupport = new ParserEventSupport(this);
        }

        public override bool isDebugMode()
        {
            return !this._notDebugMode;
        }

        public virtual bool isGuessing()
        {
            return (base.inputState.guessing > 0);
        }

        public override int LA(int i)
        {
            int la = base.LA(i);
            this.parserEventSupport.fireLA(i, la);
            return la;
        }

        public override void match(BitSet b)
        {
            string text = this.LT(1).getText();
            int c = this.LA(1);
            try
            {
                base.match(b);
                this.parserEventSupport.fireMatch(c, b, text, base.inputState.guessing);
            }
            catch (MismatchedTokenException exception)
            {
                if (base.inputState.guessing == 0)
                {
                    this.parserEventSupport.fireMismatch(c, b, text, base.inputState.guessing);
                }
                throw exception;
            }
        }

        public override void match(int t)
        {
            string text = this.LT(1).getText();
            int i = this.LA(1);
            try
            {
                base.match(t);
                this.parserEventSupport.fireMatch(t, text, base.inputState.guessing);
            }
            catch (MismatchedTokenException exception)
            {
                if (base.inputState.guessing == 0)
                {
                    this.parserEventSupport.fireMismatch(i, t, text, base.inputState.guessing);
                }
                throw exception;
            }
        }

        public override void matchNot(int t)
        {
            string text = this.LT(1).getText();
            int c = this.LA(1);
            try
            {
                base.matchNot(t);
                this.parserEventSupport.fireMatchNot(c, t, text, base.inputState.guessing);
            }
            catch (MismatchedTokenException exception)
            {
                if (base.inputState.guessing == 0)
                {
                    this.parserEventSupport.fireMismatchNot(c, t, text, base.inputState.guessing);
                }
                throw exception;
            }
        }

        public override void removeMessageListener(MessageListener l)
        {
            this.parserEventSupport.removeMessageListener(l);
        }

        public override void removeParserListener(ParserListener l)
        {
            this.parserEventSupport.removeParserListener(l);
        }

        public override void removeParserMatchListener(ParserMatchListener l)
        {
            this.parserEventSupport.removeParserMatchListener(l);
        }

        public override void removeParserTokenListener(ParserTokenListener l)
        {
            this.parserEventSupport.removeParserTokenListener(l);
        }

        public override void removeSemanticPredicateListener(SemanticPredicateListener l)
        {
            this.parserEventSupport.removeSemanticPredicateListener(l);
        }

        public override void removeSyntacticPredicateListener(SyntacticPredicateListener l)
        {
            this.parserEventSupport.removeSyntacticPredicateListener(l);
        }

        public override void removeTraceListener(TraceListener l)
        {
            this.parserEventSupport.removeTraceListener(l);
        }

        public override void reportError(RecognitionException ex)
        {
            this.parserEventSupport.fireReportError(ex);
            base.reportError(ex);
        }

        public override void reportError(string s)
        {
            this.parserEventSupport.fireReportError(s);
            base.reportError(s);
        }

        public override void reportWarning(string s)
        {
            this.parserEventSupport.fireReportWarning(s);
            base.reportWarning(s);
        }

        public override void setDebugMode(bool mode)
        {
            this._notDebugMode = !mode;
        }

        public virtual void setupDebugging(TokenBuffer tokenBuf)
        {
            this.setupDebugging(null, tokenBuf);
        }

        public virtual void setupDebugging(TokenStream lexer)
        {
            this.setupDebugging(lexer, null);
        }

        protected internal virtual void setupDebugging(TokenStream lexer, TokenBuffer tokenBuf)
        {
            this.setDebugMode(true);
            try
            {
                Type.GetType("antlr.parseview.ParseView").GetConstructor(new Type[] { typeof(LLkDebuggingParser), typeof(TokenStream), typeof(TokenBuffer) }).Invoke(new object[] { this, lexer, tokenBuf });
            }
            catch (Exception exception)
            {
                Console.Error.WriteLine("Error initializing ParseView: " + exception);
                Console.Error.WriteLine("Please report this to Scott Stanchfield, thetick@magelang.com");
                Environment.Exit(1);
            }
        }

        public virtual void wakeUp()
        {
            lock (this)
            {
                Monitor.Pulse(this);
            }
        }
    }
}

