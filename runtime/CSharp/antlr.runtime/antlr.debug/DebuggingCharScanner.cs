namespace antlr.debug
{
    using antlr;
    using antlr.collections.impl;
    using System;
    using System.Text;
    using System.Threading;

    public abstract class DebuggingCharScanner : CharScanner, DebuggingParser
    {
        private bool _notDebugMode;
        private ScannerEventSupport eventSupport;
        protected internal string[] ruleNames;
        protected internal string[] semPredNames;

        public DebuggingCharScanner(InputBuffer cb) : base(cb)
        {
            this._notDebugMode = false;
            this.InitBlock();
        }

        public DebuggingCharScanner(LexerSharedInputState state) : base(state)
        {
            this._notDebugMode = false;
            this.InitBlock();
        }

        public virtual void addMessageListener(MessageListener l)
        {
            this.eventSupport.addMessageListener(l);
        }

        public virtual void addNewLineListener(NewLineListener l)
        {
            this.eventSupport.addNewLineListener(l);
        }

        public virtual void addParserListener(ParserListener l)
        {
            this.eventSupport.addParserListener(l);
        }

        public virtual void addParserMatchListener(ParserMatchListener l)
        {
            this.eventSupport.addParserMatchListener(l);
        }

        public virtual void addParserTokenListener(ParserTokenListener l)
        {
            this.eventSupport.addParserTokenListener(l);
        }

        public virtual void addSemanticPredicateListener(SemanticPredicateListener l)
        {
            this.eventSupport.addSemanticPredicateListener(l);
        }

        public virtual void addSyntacticPredicateListener(SyntacticPredicateListener l)
        {
            this.eventSupport.addSyntacticPredicateListener(l);
        }

        public virtual void addTraceListener(TraceListener l)
        {
            this.eventSupport.addTraceListener(l);
        }

        public override void consume()
        {
            int c = -99;
            try
            {
                c = this.LA(1);
            }
            catch (CharStreamException)
            {
            }
            base.consume();
            this.eventSupport.fireConsume(c);
        }

        protected internal virtual void fireEnterRule(int num, int data)
        {
            if (this.isDebugMode())
            {
                this.eventSupport.fireEnterRule(num, base.inputState.guessing, data);
            }
        }

        protected internal virtual void fireExitRule(int num, int ttype)
        {
            if (this.isDebugMode())
            {
                this.eventSupport.fireExitRule(num, base.inputState.guessing, ttype);
            }
        }

        protected internal virtual bool fireSemanticPredicateEvaluated(int type, int num, bool condition)
        {
            if (this.isDebugMode())
            {
                return this.eventSupport.fireSemanticPredicateEvaluated(type, num, condition, base.inputState.guessing);
            }
            return condition;
        }

        protected internal virtual void fireSyntacticPredicateFailed()
        {
            if (this.isDebugMode())
            {
                this.eventSupport.fireSyntacticPredicateFailed(base.inputState.guessing);
            }
        }

        protected internal virtual void fireSyntacticPredicateStarted()
        {
            if (this.isDebugMode())
            {
                this.eventSupport.fireSyntacticPredicateStarted(base.inputState.guessing);
            }
        }

        protected internal virtual void fireSyntacticPredicateSucceeded()
        {
            if (this.isDebugMode())
            {
                this.eventSupport.fireSyntacticPredicateSucceeded(base.inputState.guessing);
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
            this.eventSupport = new ScannerEventSupport(this);
        }

        public virtual bool isDebugMode()
        {
            return !this._notDebugMode;
        }

        public override char LA(int i)
        {
            char la = base.LA(i);
            this.eventSupport.fireLA(i, la);
            return la;
        }

        protected internal override IToken makeToken(int t)
        {
            return base.makeToken(t);
        }

        public override void match(BitSet b)
        {
            string text = base.text.ToString();
            char c = this.LA(1);
            try
            {
                base.match(b);
                this.eventSupport.fireMatch(c, b, text, base.inputState.guessing);
            }
            catch (MismatchedCharException exception)
            {
                if (base.inputState.guessing == 0)
                {
                    this.eventSupport.fireMismatch(c, b, text, base.inputState.guessing);
                }
                throw exception;
            }
        }

        public override void match(int c)
        {
            char ch = this.LA(1);
            try
            {
                base.match(c);
                this.eventSupport.fireMatch(Convert.ToChar(c), base.inputState.guessing);
            }
            catch (MismatchedCharException exception)
            {
                if (base.inputState.guessing == 0)
                {
                    this.eventSupport.fireMismatch(ch, Convert.ToChar(c), base.inputState.guessing);
                }
                throw exception;
            }
        }

        public override void match(string s)
        {
            StringBuilder builder = new StringBuilder("");
            int length = s.Length;
            try
            {
                for (int i = 1; i <= length; i++)
                {
                    builder.Append(base.LA(i));
                }
            }
            catch (Exception)
            {
            }
            try
            {
                base.match(s);
                this.eventSupport.fireMatch(s, base.inputState.guessing);
            }
            catch (MismatchedCharException exception)
            {
                if (base.inputState.guessing == 0)
                {
                    this.eventSupport.fireMismatch(builder.ToString(), s, base.inputState.guessing);
                }
                throw exception;
            }
        }

        public override void matchNot(int c)
        {
            char ch = this.LA(1);
            try
            {
                base.matchNot(c);
                this.eventSupport.fireMatchNot(ch, Convert.ToChar(c), base.inputState.guessing);
            }
            catch (MismatchedCharException exception)
            {
                if (base.inputState.guessing == 0)
                {
                    this.eventSupport.fireMismatchNot(ch, Convert.ToChar(c), base.inputState.guessing);
                }
                throw exception;
            }
        }

        public override void matchRange(int c1, int c2)
        {
            char c = this.LA(1);
            try
            {
                base.matchRange(c1, c2);
                this.eventSupport.fireMatch(c, "" + c1 + c2, base.inputState.guessing);
            }
            catch (MismatchedCharException exception)
            {
                if (base.inputState.guessing == 0)
                {
                    this.eventSupport.fireMismatch(c, "" + c1 + c2, base.inputState.guessing);
                }
                throw exception;
            }
        }

        public override void newline()
        {
            base.newline();
            this.eventSupport.fireNewLine(this.getLine());
        }

        public virtual void removeMessageListener(MessageListener l)
        {
            this.eventSupport.removeMessageListener(l);
        }

        public virtual void removeNewLineListener(NewLineListener l)
        {
            this.eventSupport.removeNewLineListener(l);
        }

        public virtual void removeParserListener(ParserListener l)
        {
            this.eventSupport.removeParserListener(l);
        }

        public virtual void removeParserMatchListener(ParserMatchListener l)
        {
            this.eventSupport.removeParserMatchListener(l);
        }

        public virtual void removeParserTokenListener(ParserTokenListener l)
        {
            this.eventSupport.removeParserTokenListener(l);
        }

        public virtual void removeSemanticPredicateListener(SemanticPredicateListener l)
        {
            this.eventSupport.removeSemanticPredicateListener(l);
        }

        public virtual void removeSyntacticPredicateListener(SyntacticPredicateListener l)
        {
            this.eventSupport.removeSyntacticPredicateListener(l);
        }

        public virtual void removeTraceListener(TraceListener l)
        {
            this.eventSupport.removeTraceListener(l);
        }

        public virtual void reportError(MismatchedCharException e)
        {
            this.eventSupport.fireReportError(e);
            base.reportError(e);
        }

        public override void reportError(string s)
        {
            this.eventSupport.fireReportError(s);
            base.reportError(s);
        }

        public override void reportWarning(string s)
        {
            this.eventSupport.fireReportWarning(s);
            base.reportWarning(s);
        }

        public virtual void setDebugMode(bool mode)
        {
            this._notDebugMode = !mode;
        }

        public virtual void setupDebugging()
        {
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

