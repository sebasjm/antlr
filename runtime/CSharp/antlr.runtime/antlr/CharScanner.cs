namespace antlr
{
    using antlr.collections.impl;
    using antlr.debug;
    using System;
    using System.Collections;
    using System.ComponentModel;
    using System.Globalization;
    using System.IO;
    using System.Reflection;
    using System.Text;

    public abstract class CharScanner : TokenStream, ICharScannerDebugSubject, IDebugSubject
    {
        protected char cached_LA1;
        protected char cached_LA2;
        protected bool caseSensitive;
        protected bool caseSensitiveLiterals;
        protected internal bool commitToPath;
        internal static readonly object ConsumeEventKey = new object();
        internal static readonly object DoneEventKey = new object();
        internal static readonly object EnterRuleEventKey = new object();
        public static readonly char EOF_CHAR = 0xffff;
        private EventHandlerList events_;
        internal static readonly object ExitRuleEventKey = new object();
        protected internal LexerSharedInputState inputState;
        internal static readonly object LAEventKey = new object();
        protected Hashtable literals;
        internal static readonly object MatchEventKey = new object();
        internal static readonly object MatchNotEventKey = new object();
        internal static readonly object MisMatchEventKey = new object();
        internal static readonly object MisMatchNotEventKey = new object();
        internal static readonly object NewLineEventKey = new object();
        internal const char NO_CHAR = '\0';
        internal static readonly object ReportErrorEventKey = new object();
        internal static readonly object ReportWarningEventKey = new object();
        protected internal IToken returnToken_;
        protected bool saveConsumedInput;
        internal static readonly object SemPredEvaluatedEventKey = new object();
        internal static readonly object SynPredFailedEventKey = new object();
        internal static readonly object SynPredStartedEventKey = new object();
        internal static readonly object SynPredSucceededEventKey = new object();
        protected internal int tabsize;
        protected internal StringBuilder text;
        protected TokenCreator tokenCreator;
        protected internal int traceDepth;

        public event TokenEventHandler CharLA
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

        public event TokenEventHandler ConsumedChar
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

        public event NewLineEventHandler HitNewLine
        {
            add
            {
                this.Events.AddHandler(NewLineEventKey, value);
            }
            remove
            {
                this.Events.RemoveHandler(NewLineEventKey, value);
            }
        }

        public event MatchEventHandler MatchedChar
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

        public event MatchEventHandler MatchedNotChar
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

        public event MatchEventHandler MisMatchedChar
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

        public event MatchEventHandler MisMatchedNotChar
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

        public CharScanner()
        {
            this.events_ = new EventHandlerList();
            this.saveConsumedInput = true;
            this.caseSensitive = true;
            this.caseSensitiveLiterals = true;
            this.tabsize = 8;
            this.returnToken_ = null;
            this.commitToPath = false;
            this.traceDepth = 0;
            this.text = new StringBuilder();
            this.setTokenCreator(new CommonToken.CommonTokenCreator());
        }

        public CharScanner(InputBuffer cb) : this()
        {
            this.inputState = new LexerSharedInputState(cb);
            this.cached_LA2 = this.inputState.input.LA(2);
            this.cached_LA1 = this.inputState.input.LA(1);
        }

        public CharScanner(LexerSharedInputState sharedState) : this()
        {
            this.inputState = sharedState;
            if (this.inputState != null)
            {
                this.cached_LA2 = this.inputState.input.LA(2);
                this.cached_LA1 = this.inputState.input.LA(1);
            }
        }

        public virtual void append(char c)
        {
            if (this.saveConsumedInput)
            {
                this.text.Append(c);
            }
        }

        public virtual void append(string s)
        {
            if (this.saveConsumedInput)
            {
                this.text.Append(s);
            }
        }

        public virtual void commit()
        {
            this.inputState.input.commit();
        }

        public virtual void consume()
        {
            if (this.inputState.guessing == 0)
            {
                if (this.caseSensitive)
                {
                    this.append(this.cached_LA1);
                }
                else
                {
                    this.append(this.inputState.input.LA(1));
                }
                if (this.cached_LA1 == '\t')
                {
                    this.tab();
                }
                else
                {
                    this.inputState.column++;
                }
            }
            if (this.caseSensitive)
            {
                this.cached_LA1 = this.inputState.input.consume();
                this.cached_LA2 = this.inputState.input.LA(2);
            }
            else
            {
                this.cached_LA1 = this.toLower(this.inputState.input.consume());
                this.cached_LA2 = this.toLower(this.inputState.input.LA(2));
            }
        }

        public virtual void consumeUntil(BitSet bset)
        {
            while ((this.cached_LA1 != EOF_CHAR) && !bset.member(this.cached_LA1))
            {
                this.consume();
            }
        }

        public virtual void consumeUntil(int c)
        {
            while ((EOF_CHAR != this.cached_LA1) && (c != this.cached_LA1))
            {
                this.consume();
            }
        }

        public virtual bool getCaseSensitive()
        {
            return this.caseSensitive;
        }

        public bool getCaseSensitiveLiterals()
        {
            return this.caseSensitiveLiterals;
        }

        public virtual int getColumn()
        {
            return this.inputState.column;
        }

        public virtual bool getCommitToPath()
        {
            return this.commitToPath;
        }

        public virtual string getFilename()
        {
            return this.inputState.filename;
        }

        public virtual InputBuffer getInputBuffer()
        {
            return this.inputState.input;
        }

        public virtual LexerSharedInputState getInputState()
        {
            return this.inputState;
        }

        public virtual int getLine()
        {
            return this.inputState.line;
        }

        public virtual int getTabSize()
        {
            return this.tabsize;
        }

        public virtual string getText()
        {
            return this.text.ToString();
        }

        public virtual IToken getTokenObject()
        {
            return this.returnToken_;
        }

        public virtual char LA(int i)
        {
            if (i == 1)
            {
                return this.cached_LA1;
            }
            if (i == 2)
            {
                return this.cached_LA2;
            }
            if (this.caseSensitive)
            {
                return this.inputState.input.LA(i);
            }
            return this.toLower(this.inputState.input.LA(i));
        }

        protected internal virtual IToken makeToken(int t)
        {
            IToken badToken = null;
            bool flag;
            try
            {
                badToken = this.tokenCreator.Create();
                if (badToken != null)
                {
                    badToken.Type = t;
                    badToken.setColumn(this.inputState.tokenStartColumn);
                    badToken.setLine(this.inputState.tokenStartLine);
                    badToken.setFilename(this.inputState.filename);
                }
                flag = true;
            }
            catch
            {
                flag = false;
            }
            if (!flag)
            {
                this.panic("Can't create Token object '" + this.tokenCreator.TokenTypeName + "'");
                badToken = Token.badToken;
            }
            return badToken;
        }

        public virtual int mark()
        {
            return this.inputState.input.mark();
        }

        public virtual void match(BitSet b)
        {
            if (!b.member(this.cached_LA1))
            {
                throw new MismatchedCharException(this.cached_LA1, b, false, this);
            }
            this.consume();
        }

        public virtual void match(char c)
        {
            this.match((int) c);
        }

        public virtual void match(int c)
        {
            if (this.cached_LA1 != c)
            {
                throw new MismatchedCharException(this.cached_LA1, Convert.ToChar(c), false, this);
            }
            this.consume();
        }

        public virtual void match(string s)
        {
            int length = s.Length;
            for (int i = 0; i < length; i++)
            {
                if (this.cached_LA1 != s[i])
                {
                    throw new MismatchedCharException(this.cached_LA1, s[i], false, this);
                }
                this.consume();
            }
        }

        public virtual void matchNot(char c)
        {
            this.matchNot((int) c);
        }

        public virtual void matchNot(int c)
        {
            if (this.cached_LA1 == c)
            {
                throw new MismatchedCharException(this.cached_LA1, Convert.ToChar(c), true, this);
            }
            this.consume();
        }

        public virtual void matchRange(char c1, char c2)
        {
            this.matchRange((int) c1, (int) c2);
        }

        public virtual void matchRange(int c1, int c2)
        {
            if ((this.cached_LA1 < c1) || (this.cached_LA1 > c2))
            {
                throw new MismatchedCharException(this.cached_LA1, Convert.ToChar(c1), Convert.ToChar(c2), false, this);
            }
            this.consume();
        }

        public virtual void newline()
        {
            this.inputState.line++;
            this.inputState.column = 1;
        }

        public virtual IToken nextToken()
        {
            return null;
        }

        public virtual void panic()
        {
            this.panic("");
        }

        public virtual void panic(string s)
        {
            throw new ANTLRPanicException("CharScanner::panic: " + s);
        }

        public virtual void recover(RecognitionException ex, BitSet tokenSet)
        {
            this.consume();
            this.consumeUntil(tokenSet);
        }

        public virtual void refresh()
        {
            if (this.caseSensitive)
            {
                this.cached_LA2 = this.inputState.input.LA(2);
                this.cached_LA1 = this.inputState.input.LA(1);
            }
            else
            {
                this.cached_LA2 = this.toLower(this.inputState.input.LA(2));
                this.cached_LA1 = this.toLower(this.inputState.input.LA(1));
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

        public virtual void resetState(InputBuffer ib)
        {
            this.text.Length = 0;
            this.traceDepth = 0;
            this.inputState.resetInput(ib);
            this.refresh();
        }

        public void resetState(Stream s)
        {
            this.resetState(new ByteBuffer(s));
        }

        public void resetState(TextReader tr)
        {
            this.resetState(new CharBuffer(tr));
        }

        public virtual void resetText()
        {
            this.text.Length = 0;
            this.inputState.tokenStartColumn = this.inputState.column;
            this.inputState.tokenStartLine = this.inputState.line;
        }

        public virtual void rewind(int pos)
        {
            this.inputState.input.rewind(pos);
            if (this.caseSensitive)
            {
                this.cached_LA2 = this.inputState.input.LA(2);
                this.cached_LA1 = this.inputState.input.LA(1);
            }
            else
            {
                this.cached_LA2 = this.toLower(this.inputState.input.LA(2));
                this.cached_LA1 = this.toLower(this.inputState.input.LA(1));
            }
        }

        public virtual void setCaseSensitive(bool t)
        {
            this.caseSensitive = t;
            if (this.caseSensitive)
            {
                this.cached_LA2 = this.inputState.input.LA(2);
                this.cached_LA1 = this.inputState.input.LA(1);
            }
            else
            {
                this.cached_LA2 = this.toLower(this.inputState.input.LA(2));
                this.cached_LA1 = this.toLower(this.inputState.input.LA(1));
            }
        }

        public virtual void setColumn(int c)
        {
            this.inputState.column = c;
        }

        public virtual void setCommitToPath(bool commit)
        {
            this.commitToPath = commit;
        }

        public virtual void setFilename(string f)
        {
            this.inputState.filename = f;
        }

        public virtual void setInputState(LexerSharedInputState state)
        {
            this.inputState = state;
        }

        public virtual void setLine(int line)
        {
            this.inputState.line = line;
        }

        public virtual void setTabSize(int size)
        {
            this.tabsize = size;
        }

        public virtual void setText(string s)
        {
            this.resetText();
            this.text.Append(s);
        }

        public virtual void setTokenCreator(TokenCreator tokenCreator)
        {
            this.tokenCreator = tokenCreator;
        }

        public virtual void setTokenObjectClass(string cl)
        {
            this.tokenCreator = new ReflectionBasedTokenCreator(this, cl);
        }

        public virtual void tab()
        {
            int c = ((((this.getColumn() - 1) / this.tabsize) + 1) * this.tabsize) + 1;
            this.setColumn(c);
        }

        public virtual int testLiteralsTable(int ttype)
        {
            string str = this.text.ToString();
            switch (str)
            {
                case null:
                case string.Empty:
                    return ttype;
            }
            object obj2 = this.literals[str];
            return ((obj2 == null) ? ttype : ((int) obj2));
        }

        public virtual int testLiteralsTable(string someText, int ttype)
        {
            if ((someText == null) || (someText == string.Empty))
            {
                return ttype;
            }
            object obj2 = this.literals[someText];
            return ((obj2 == null) ? ttype : ((int) obj2));
        }

        public virtual char toLower(int c)
        {
            return char.ToLower(Convert.ToChar(c), CultureInfo.InvariantCulture);
        }

        public virtual void traceIn(string rname)
        {
            this.traceDepth++;
            this.traceIndent();
            Console.Out.WriteLine(string.Concat(new object[] { "> lexer ", rname, "; c==", this.LA(1) }));
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
            Console.Out.WriteLine(string.Concat(new object[] { "< lexer ", rname, "; c==", this.LA(1) }));
            this.traceDepth--;
        }

        public virtual void uponEOF()
        {
        }

        protected internal EventHandlerList Events
        {
            get
            {
                return this.events_;
            }
        }

        private class ReflectionBasedTokenCreator : TokenCreator
        {
            private CharScanner owner;
            private string tokenTypeName;
            private Type tokenTypeObject;

            protected ReflectionBasedTokenCreator()
            {
            }

            public ReflectionBasedTokenCreator(CharScanner owner, string tokenTypeName)
            {
                this.owner = owner;
                this.SetTokenType(tokenTypeName);
            }

            public override IToken Create()
            {
                IToken token = null;
                try
                {
                    token = (Token) Activator.CreateInstance(this.tokenTypeObject);
                }
                catch
                {
                }
                return token;
            }

            private void SetTokenType(string tokenTypeName)
            {
                this.tokenTypeName = tokenTypeName;
                foreach (Assembly assembly in AppDomain.CurrentDomain.GetAssemblies())
                {
                    try
                    {
                        this.tokenTypeObject = assembly.GetType(tokenTypeName);
                        if (this.tokenTypeObject != null)
                        {
                            break;
                        }
                    }
                    catch
                    {
                        throw new TypeLoadException("Unable to load Type for Token class '" + tokenTypeName + "'");
                    }
                }
                if (this.tokenTypeObject == null)
                {
                    throw new TypeLoadException("Unable to load Type for Token class '" + tokenTypeName + "'");
                }
            }

            public override string TokenTypeName
            {
                get
                {
                    return this.tokenTypeName;
                }
            }
        }
    }
}

