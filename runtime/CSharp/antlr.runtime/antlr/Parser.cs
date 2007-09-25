namespace antlr
{
    using antlr.collections;
    using antlr.collections.impl;
    using antlr.debug;
    using System;
    using System.ComponentModel;

    public abstract class Parser : IParserDebugSubject, IDebugSubject
    {
        protected internal ASTFactory astFactory;
        internal static readonly object ConsumeEventKey = new object();
        internal static readonly object DoneEventKey = new object();
        internal static readonly object EnterRuleEventKey = new object();
        private EventHandlerList events_;
        internal static readonly object ExitRuleEventKey = new object();
        private bool ignoreInvalidDebugCalls;
        protected internal ParserSharedInputState inputState;
        internal static readonly object LAEventKey = new object();
        internal static readonly object MatchEventKey = new object();
        internal static readonly object MatchNotEventKey = new object();
        internal static readonly object MisMatchEventKey = new object();
        internal static readonly object MisMatchNotEventKey = new object();
        internal static readonly object NewLineEventKey = new object();
        internal static readonly object ReportErrorEventKey = new object();
        internal static readonly object ReportWarningEventKey = new object();
        protected internal AST returnAST;
        internal static readonly object SemPredEvaluatedEventKey = new object();
        internal static readonly object SynPredFailedEventKey = new object();
        internal static readonly object SynPredStartedEventKey = new object();
        internal static readonly object SynPredSucceededEventKey = new object();
        protected internal string[] tokenNames;
        protected internal int traceDepth;

        public event TokenEventHandler ConsumedToken
        {
            add
            {
                this.Events.AddHandler(ConsumeEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(ConsumeEventKey, value);
            }
        }

        public event TraceEventHandler Done
        {
            add
            {
                this.Events.AddHandler(DoneEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(DoneEventKey, value);
            }
        }

        public event TraceEventHandler EnterRule
        {
            add
            {
                this.Events.AddHandler(EnterRuleEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(EnterRuleEventKey, value);
            }
        }

        public event MessageEventHandler ErrorReported
        {
            add
            {
                this.Events.AddHandler(ReportErrorEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(ReportErrorEventKey, value);
            }
        }

        public event TraceEventHandler ExitRule
        {
            add
            {
                this.Events.AddHandler(ExitRuleEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(ExitRuleEventKey, value);
            }
        }

        public event MatchEventHandler MatchedNotToken
        {
            add
            {
                this.Events.AddHandler(MatchNotEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(MatchNotEventKey, value);
            }
        }

        public event MatchEventHandler MatchedToken
        {
            add
            {
                this.Events.AddHandler(MatchEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(MatchEventKey, value);
            }
        }

        public event MatchEventHandler MisMatchedNotToken
        {
            add
            {
                this.Events.AddHandler(MisMatchNotEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(MisMatchNotEventKey, value);
            }
        }

        public event MatchEventHandler MisMatchedToken
        {
            add
            {
                this.Events.AddHandler(MisMatchEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(MisMatchEventKey, value);
            }
        }

        public event SemanticPredicateEventHandler SemPredEvaluated
        {
            add
            {
                this.Events.AddHandler(SemPredEvaluatedEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(SemPredEvaluatedEventKey, value);
            }
        }

        public event SyntacticPredicateEventHandler SynPredFailed
        {
            add
            {
                this.Events.AddHandler(SynPredFailedEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(SynPredFailedEventKey, value);
            }
        }

        public event SyntacticPredicateEventHandler SynPredStarted
        {
            add
            {
                this.Events.AddHandler(SynPredStartedEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(SynPredStartedEventKey, value);
            }
        }

        public event SyntacticPredicateEventHandler SynPredSucceeded
        {
            add
            {
                this.Events.AddHandler(SynPredSucceededEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(SynPredSucceededEventKey, value);
            }
        }

        public event TokenEventHandler TokenLA
        {
            add
            {
                this.Events.AddHandler(LAEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(LAEventKey, value);
            }
        }

        public event MessageEventHandler WarningReported
        {
            add
            {
                this.Events.AddHandler(ReportWarningEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(ReportWarningEventKey, value);
            }
        }

        public Parser()
        {
            this.events_ = new EventHandlerList();
            this.astFactory = new ASTFactory();
            this.ignoreInvalidDebugCalls = false;
            this.traceDepth = 0;
            this.inputState = new ParserSharedInputState();
        }

        public Parser(ParserSharedInputState state)
        {
            this.events_ = new EventHandlerList();
            this.astFactory = new ASTFactory();
            this.ignoreInvalidDebugCalls = false;
            this.traceDepth = 0;
            this.inputState = state;
        }

        public virtual void addMessageListener(MessageListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new ArgumentException("addMessageListener() is only valid if parser built for debugging");
            }
        }

        public virtual void addParserListener(ParserListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new ArgumentException("addParserListener() is only valid if parser built for debugging");
            }
        }

        public virtual void addParserMatchListener(ParserMatchListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new ArgumentException("addParserMatchListener() is only valid if parser built for debugging");
            }
        }

        public virtual void addParserTokenListener(ParserTokenListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new ArgumentException("addParserTokenListener() is only valid if parser built for debugging");
            }
        }

        public virtual void addSemanticPredicateListener(SemanticPredicateListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new ArgumentException("addSemanticPredicateListener() is only valid if parser built for debugging");
            }
        }

        public virtual void addSyntacticPredicateListener(SyntacticPredicateListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new ArgumentException("addSyntacticPredicateListener() is only valid if parser built for debugging");
            }
        }

        public virtual void addTraceListener(TraceListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new ArgumentException("addTraceListener() is only valid if parser built for debugging");
            }
        }

        public abstract void consume();
        public virtual void consumeUntil(BitSet bset)
        {
            while ((this.LA(1) != 1) && !bset.member(this.LA(1)))
            {
                this.consume();
            }
        }

        public virtual void consumeUntil(int tokenType)
        {
            while ((this.LA(1) != 1) && (this.LA(1) != tokenType))
            {
                this.consume();
            }
        }

        protected internal virtual void defaultDebuggingSetup(TokenStream lexer, TokenBuffer tokBuf)
        {
        }

        public virtual AST getAST()
        {
            return this.returnAST;
        }

        public virtual ASTFactory getASTFactory()
        {
            return this.astFactory;
        }

        public virtual string getFilename()
        {
            return this.inputState.filename;
        }

        public virtual ParserSharedInputState getInputState()
        {
            return this.inputState;
        }

        public virtual string getTokenName(int num)
        {
            return this.tokenNames[num];
        }

        public virtual string[] getTokenNames()
        {
            return this.tokenNames;
        }

        public virtual bool isDebugMode()
        {
            return false;
        }

        public abstract int LA(int i);
        public abstract IToken LT(int i);
        public virtual int mark()
        {
            return this.inputState.input.mark();
        }

        public virtual void match(BitSet b)
        {
            if (!b.member(this.LA(1)))
            {
                throw new MismatchedTokenException(this.tokenNames, this.LT(1), b, false, this.getFilename());
            }
            this.consume();
        }

        public virtual void match(int t)
        {
            if (this.LA(1) != t)
            {
                throw new MismatchedTokenException(this.tokenNames, this.LT(1), t, false, this.getFilename());
            }
            this.consume();
        }

        public virtual void matchNot(int t)
        {
            if (this.LA(1) == t)
            {
                throw new MismatchedTokenException(this.tokenNames, this.LT(1), t, true, this.getFilename());
            }
            this.consume();
        }

        [Obsolete("De-activated since version 2.7.2.6 as it cannot be overidden.", true)]
        public static void panic()
        {
            Console.Error.WriteLine("Parser: panic");
            Environment.Exit(1);
        }

        public virtual void recover(RecognitionException ex, BitSet tokenSet)
        {
            this.consume();
            this.consumeUntil(tokenSet);
        }

        public virtual void removeMessageListener(MessageListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new SystemException("removeMessageListener() is only valid if parser built for debugging");
            }
        }

        public virtual void removeParserListener(ParserListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new SystemException("removeParserListener() is only valid if parser built for debugging");
            }
        }

        public virtual void removeParserMatchListener(ParserMatchListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new SystemException("removeParserMatchListener() is only valid if parser built for debugging");
            }
        }

        public virtual void removeParserTokenListener(ParserTokenListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new SystemException("removeParserTokenListener() is only valid if parser built for debugging");
            }
        }

        public virtual void removeSemanticPredicateListener(SemanticPredicateListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new ArgumentException("removeSemanticPredicateListener() is only valid if parser built for debugging");
            }
        }

        public virtual void removeSyntacticPredicateListener(SyntacticPredicateListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new ArgumentException("removeSyntacticPredicateListener() is only valid if parser built for debugging");
            }
        }

        public virtual void removeTraceListener(TraceListener l)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new SystemException("removeTraceListener() is only valid if parser built for debugging");
            }
        }

        public virtual void reportError(RecognitionException ex)
        {
            Console.Error.WriteLine(ex);
        }

        public virtual void reportError(string s)
        {
            if (this.getFilename() == null)
            {
                Console.Error.WriteLine("error: " + s);
            }
            else
            {
                Console.Error.WriteLine(this.getFilename() + ": error: " + s);
            }
        }

        public virtual void reportWarning(string s)
        {
            if (this.getFilename() == null)
            {
                Console.Error.WriteLine("warning: " + s);
            }
            else
            {
                Console.Error.WriteLine(this.getFilename() + ": warning: " + s);
            }
        }

        public virtual void resetState()
        {
            this.traceDepth = 0;
            this.inputState.reset();
        }

        public virtual void rewind(int pos)
        {
            this.inputState.input.rewind(pos);
        }

        public virtual void setASTFactory(ASTFactory f)
        {
            this.astFactory = f;
        }

        public virtual void setASTNodeClass(string cl)
        {
            this.astFactory.setASTNodeType(cl);
        }

        [Obsolete("Replaced by setASTNodeClass(string) since version 2.7.1", true)]
        public virtual void setASTNodeType(string nodeType)
        {
            this.setASTNodeClass(nodeType);
        }

        public virtual void setDebugMode(bool debugMode)
        {
            if (!this.ignoreInvalidDebugCalls)
            {
                throw new SystemException("setDebugMode() only valid if parser built for debugging");
            }
        }

        public virtual void setFilename(string f)
        {
            this.inputState.filename = f;
        }

        public virtual void setIgnoreInvalidDebugCalls(bool Value)
        {
            this.ignoreInvalidDebugCalls = Value;
        }

        public virtual void setInputState(ParserSharedInputState state)
        {
            this.inputState = state;
        }

        public virtual void setTokenBuffer(TokenBuffer t)
        {
            this.inputState.input = t;
        }

        public virtual void traceIn(string rname)
        {
            this.traceDepth++;
            this.traceIndent();
            Console.Out.WriteLine("> " + rname + "; LA(1)==" + this.LT(1).getText() + ((this.inputState.guessing > 0) ? " [guessing]" : ""));
        }

        public virtual void traceIndent()
        {
            for (int i = 0; i < this.traceDepth; i++)
            {
                Console.Out.Write(" ");
            }
        }

        public virtual void traceOut(string rname)
        {
            this.traceIndent();
            Console.Out.WriteLine("< " + rname + "; LA(1)==" + this.LT(1).getText() + ((this.inputState.guessing > 0) ? " [guessing]" : ""));
            this.traceDepth--;
        }

        protected internal EventHandlerList Events
        {
            get
            {
                return this.events_;
            }
        }
    }
}

