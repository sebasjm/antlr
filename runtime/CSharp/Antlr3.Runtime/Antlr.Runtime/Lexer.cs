namespace Antlr.Runtime
{
    using System;

    public abstract class Lexer : BaseRecognizer, ITokenSource
    {
        protected int channel;
        protected internal ICharStream input;
        protected string text;
        protected internal IToken token;
        private const int TOKEN_dot_EOF = -1;
        protected internal int tokenStartCharIndex;
        protected int tokenStartCharPositionInLine;
        protected int tokenStartLine;
        protected int type;

        public Lexer()
        {
            this.tokenStartCharIndex = -1;
        }

        public Lexer(ICharStream input)
        {
            this.tokenStartCharIndex = -1;
            this.input = input;
        }

        public virtual IToken Emit()
        {
            IToken token = new CommonToken(this.input, this.type, this.channel, this.tokenStartCharIndex, this.CharIndex - 1);
            token.Line = this.tokenStartLine;
            token.Text = this.text;
            token.CharPositionInLine = this.tokenStartCharPositionInLine;
            this.Emit(token);
            return token;
        }

        public virtual void Emit(IToken token)
        {
            this.token = token;
        }

        public string GetCharErrorDisplay(int c)
        {
            string str;
            switch (c)
            {
                case 9:
                    str = @"\t";
                    break;

                case 10:
                    str = @"\n";
                    break;

                case 13:
                    str = @"\r";
                    break;

                case -1:
                    str = "<EOF>";
                    break;

                default:
                    str = Convert.ToString((char) c);
                    break;
            }
            return ("'" + str + "'");
        }

        public override string GetErrorMessage(RecognitionException e, string[] tokenNames)
        {
            MismatchedSetException exception4;
            if (e is MismatchedTokenException)
            {
                MismatchedTokenException exception = (MismatchedTokenException) e;
                return ("mismatched character " + this.GetCharErrorDisplay(e.Char) + " expecting " + this.GetCharErrorDisplay(exception.expecting));
            }
            if (e is NoViableAltException)
            {
                NoViableAltException exception2 = (NoViableAltException) e;
                return ("no viable alternative at character " + this.GetCharErrorDisplay(exception2.Char));
            }
            if (e is EarlyExitException)
            {
                EarlyExitException exception3 = (EarlyExitException) e;
                return ("required (...)+ loop did not match anything at character " + this.GetCharErrorDisplay(exception3.Char));
            }
            if (e is MismatchedSetException)
            {
                exception4 = (MismatchedSetException) e;
                return string.Concat(new object[] { "mismatched character ", this.GetCharErrorDisplay(exception4.Char), " expecting set ", exception4.expecting });
            }
            if (e is MismatchedNotSetException)
            {
                exception4 = (MismatchedSetException) e;
                return string.Concat(new object[] { "mismatched character ", this.GetCharErrorDisplay(exception4.Char), " expecting set ", exception4.expecting });
            }
            if (e is MismatchedRangeException)
            {
                MismatchedRangeException exception5 = (MismatchedRangeException) e;
                return ("mismatched character " + this.GetCharErrorDisplay(exception5.Char) + " expecting set " + this.GetCharErrorDisplay(exception5.a) + ".." + this.GetCharErrorDisplay(exception5.b));
            }
            return base.GetErrorMessage(e, tokenNames);
        }

        public virtual void Match(int c)
        {
            if (this.input.LA(1) != c)
            {
                if (base.backtracking <= 0)
                {
                    MismatchedTokenException re = new MismatchedTokenException(c, this.input);
                    this.Recover(re);
                    throw re;
                }
                base.failed = true;
            }
            else
            {
                this.input.Consume();
                base.failed = false;
            }
        }

        public virtual void Match(string s)
        {
            int num = 0;
            while (num < s.Length)
            {
                if (this.input.LA(1) != s[num])
                {
                    if (base.backtracking <= 0)
                    {
                        MismatchedTokenException re = new MismatchedTokenException(s[num], this.input);
                        this.Recover(re);
                        throw re;
                    }
                    base.failed = true;
                    break;
                }
                num++;
                this.input.Consume();
                base.failed = false;
            }
        }

        public virtual void MatchAny()
        {
            this.input.Consume();
        }

        public virtual void MatchRange(int a, int b)
        {
            if ((this.input.LA(1) < a) || (this.input.LA(1) > b))
            {
                if (base.backtracking <= 0)
                {
                    MismatchedRangeException re = new MismatchedRangeException(a, b, this.input);
                    this.Recover(re);
                    throw re;
                }
                base.failed = true;
            }
            else
            {
                this.input.Consume();
                base.failed = false;
            }
        }

        public abstract void mTokens();
        public virtual IToken NextToken()
        {
            bool flag;
        Label_00C8:
            flag = true;
            this.token = null;
            this.channel = 0;
            this.tokenStartCharIndex = this.input.Index();
            this.tokenStartCharPositionInLine = this.input.CharPositionInLine;
            this.tokenStartLine = this.input.Line;
            this.text = null;
            if (this.input.LA(1) == -1)
            {
                return Token.EOF_TOKEN;
            }
            try
            {
                this.mTokens();
                if (this.token == null)
                {
                    this.Emit();
                }
                else if (this.token == Token.SKIP_TOKEN)
                {
                    goto Label_00C8;
                }
                return this.token;
            }
            catch (RecognitionException exception)
            {
                this.ReportError(exception);
                this.Recover(exception);
            }
            goto Label_00C8;
        }

        public virtual void Recover(RecognitionException re)
        {
            this.input.Consume();
        }

        public override void ReportError(RecognitionException e)
        {
            this.DisplayRecognitionError(this.TokenNames, e);
        }

        public override void Reset()
        {
            base.Reset();
            this.token = null;
            this.type = 0;
            this.channel = 0;
            this.tokenStartCharIndex = -1;
            this.tokenStartCharPositionInLine = -1;
            this.tokenStartLine = -1;
            this.text = null;
            if (this.input != null)
            {
                this.input.Seek(0);
            }
        }

        public void Skip()
        {
            this.token = Token.SKIP_TOKEN;
        }

        public virtual void TraceIn(string ruleName, int ruleIndex)
        {
            string inputSymbol = string.Concat(new object[] { (char) this.input.LT(1), " line=", this.Line, ":", this.CharPositionInLine });
            base.TraceIn(ruleName, ruleIndex, inputSymbol);
        }

        public virtual void TraceOut(string ruleName, int ruleIndex)
        {
            string inputSymbol = string.Concat(new object[] { (char) this.input.LT(1), " line=", this.Line, ":", this.CharPositionInLine });
            base.TraceOut(ruleName, ruleIndex, inputSymbol);
        }

        public virtual int CharIndex
        {
            get
            {
                return this.input.Index();
            }
        }

        public virtual int CharPositionInLine
        {
            get
            {
                return this.input.CharPositionInLine;
            }
        }

        public virtual ICharStream CharStream
        {
            set
            {
                this.input = null;
                this.Reset();
                this.input = value;
            }
        }

        public override IIntStream Input
        {
            get
            {
                return this.input;
            }
        }

        public virtual int Line
        {
            get
            {
                return this.input.Line;
            }
        }

        public virtual string Text
        {
            get
            {
                if (this.text != null)
                {
                    return this.text;
                }
                return this.input.Substring(this.tokenStartCharIndex, this.CharIndex - 1);
            }
            set
            {
                this.text = value;
            }
        }
    }
}

