namespace Antlr.Runtime
{
    using System;
    using System.Collections;
    using System.Diagnostics;

    public abstract class BaseRecognizer
    {
        protected internal int backtracking = 0;
        public const int DEFAULT_TOKEN_CHANNEL = 0;
        protected internal bool errorRecovery = false;
        protected internal bool failed = false;
        protected BitSet[] following = new BitSet[100];
        protected int followingStackPointer_ = -1;
        public const int HIDDEN = 0x63;
        public const int INITIAL_FOLLOW_STACK_SIZE = 100;
        protected internal int lastErrorIndex = -1;
        public const int MEMO_RULE_FAILED = -2;
        public const int MEMO_RULE_UNKNOWN = -1;
        public static readonly string NEXT_TOKEN_RULE_NAME = "nextToken";
        protected internal IDictionary[] ruleMemo;

        protected BaseRecognizer()
        {
        }

        public virtual bool AlreadyParsedRule(IIntStream input, int ruleIndex)
        {
            int ruleMemoization = this.GetRuleMemoization(ruleIndex, input.Index());
            if (ruleMemoization == -1)
            {
                return false;
            }
            if (ruleMemoization == -2)
            {
                this.failed = true;
            }
            else
            {
                input.Seek(ruleMemoization + 1);
            }
            return true;
        }

        public virtual void BeginBacktrack(int level)
        {
        }

        public virtual void BeginResync()
        {
        }

        protected internal virtual BitSet CombineFollows(bool exact)
        {
            int num = this.followingStackPointer_;
            BitSet set = new BitSet();
            for (int i = num; i >= 0; i--)
            {
                BitSet a = this.following[i];
                set.OrInPlace(a);
                if (!(!exact || a.Member(1)))
                {
                    break;
                }
            }
            set.Remove(1);
            return set;
        }

        protected internal virtual BitSet ComputeContextSensitiveRuleFOLLOW()
        {
            return this.CombineFollows(true);
        }

        protected internal virtual BitSet ComputeErrorRecoverySet()
        {
            return this.CombineFollows(false);
        }

        public virtual void ConsumeUntil(IIntStream input, BitSet set)
        {
            for (int i = input.LA(1); (i != Token.EOF) && !set.Member(i); i = input.LA(1))
            {
                input.Consume();
            }
        }

        public virtual void ConsumeUntil(IIntStream input, int tokenType)
        {
            for (int i = input.LA(1); (i != Token.EOF) && (i != tokenType); i = input.LA(1))
            {
                input.Consume();
            }
        }

        public virtual void DisplayRecognitionError(string[] tokenNames, RecognitionException e)
        {
            this.EmitErrorMessage(this.GetErrorHeader(e) + " " + this.GetErrorMessage(e, tokenNames));
        }

        public virtual void EmitErrorMessage(string msg)
        {
            Console.Error.WriteLine(msg);
        }

        public virtual void EndBacktrack(int level, bool successful)
        {
        }

        public virtual void EndResync()
        {
        }

        public virtual string GetErrorHeader(RecognitionException e)
        {
            return string.Concat(new object[] { "line ", e.Line, ":", e.CharPositionInLine });
        }

        public virtual string GetErrorMessage(RecognitionException e, string[] tokenNames)
        {
            string str = null;
            string str2;
            if (e is MismatchedTokenException)
            {
                MismatchedTokenException exception = (MismatchedTokenException) e;
                str2 = "<unknown>";
                if (exception.expecting == Token.EOF)
                {
                    str2 = "EOF";
                }
                else
                {
                    str2 = tokenNames[exception.expecting];
                }
                return ("mismatched input " + this.GetTokenErrorDisplay(e.Token) + " expecting " + str2);
            }
            if (e is MismatchedTreeNodeException)
            {
                MismatchedTreeNodeException exception2 = (MismatchedTreeNodeException) e;
                str2 = "<unknown>";
                if (exception2.expecting == Token.EOF)
                {
                    str2 = "EOF";
                }
                else
                {
                    str2 = tokenNames[exception2.expecting];
                }
                return string.Concat(new object[] { "mismatched tree node: ", exception2.Node, " expecting ", str2 });
            }
            if (e is NoViableAltException)
            {
                NoViableAltException exception3 = (NoViableAltException) e;
                return ("no viable alternative at input " + this.GetTokenErrorDisplay(e.Token));
            }
            if (e is EarlyExitException)
            {
                EarlyExitException exception4 = (EarlyExitException) e;
                return ("required (...)+ loop did not match anything at input " + this.GetTokenErrorDisplay(e.Token));
            }
            if (e is MismatchedSetException)
            {
                MismatchedSetException exception5 = (MismatchedSetException) e;
                return string.Concat(new object[] { "mismatched input ", this.GetTokenErrorDisplay(e.Token), " expecting set ", exception5.expecting });
            }
            if (e is MismatchedNotSetException)
            {
                MismatchedNotSetException exception6 = (MismatchedNotSetException) e;
                return string.Concat(new object[] { "mismatched input ", this.GetTokenErrorDisplay(e.Token), " expecting set ", exception6.expecting });
            }
            if (e is FailedPredicateException)
            {
                FailedPredicateException exception7 = (FailedPredicateException) e;
                str = "rule " + exception7.ruleName + " failed predicate: {" + exception7.predicateText + "}?";
            }
            return str;
        }

        public virtual IList GetRuleInvocationStack()
        {
            string fullName = base.GetType().FullName;
            return GetRuleInvocationStack(new Exception(), fullName);
        }

        public static IList GetRuleInvocationStack(Exception e, string recognizerClassName)
        {
            IList list = new ArrayList();
            StackTrace trace = new StackTrace(e);
            int index = 0;
            for (index = trace.FrameCount - 1; index >= 0; index--)
            {
                StackFrame frame = trace.GetFrame(index);
                if ((!frame.GetMethod().DeclaringType.FullName.StartsWith("Antlr.Runtime.") && !frame.GetMethod().Name.Equals(NEXT_TOKEN_RULE_NAME)) && frame.GetMethod().DeclaringType.FullName.Equals(recognizerClassName))
                {
                    list.Add(frame.GetMethod().Name);
                }
            }
            return list;
        }

        public virtual int GetRuleMemoization(int ruleIndex, int ruleStartIndex)
        {
            if (this.ruleMemo[ruleIndex] == null)
            {
                this.ruleMemo[ruleIndex] = new Hashtable();
            }
            object obj2 = this.ruleMemo[ruleIndex][ruleStartIndex];
            if (obj2 == null)
            {
                return -1;
            }
            return (int) obj2;
        }

        public int GetRuleMemoizationCacheSize()
        {
            int num = 0;
            for (int i = 0; (this.ruleMemo != null) && (i < this.ruleMemo.Length); i++)
            {
                IDictionary dictionary = this.ruleMemo[i];
                if (dictionary != null)
                {
                    num += dictionary.Count;
                }
            }
            return num;
        }

        public virtual string GetTokenErrorDisplay(IToken t)
        {
            string text = t.Text;
            if (text == null)
            {
                if (t.Type == Token.EOF)
                {
                    text = "<EOF>";
                }
                else
                {
                    text = "<" + t.Type + ">";
                }
            }
            text = text.Replace("\n", @"\\n").Replace("\r", @"\\r").Replace("\t", @"\\t");
            return ("'" + text + "'");
        }

        public virtual void Match(IIntStream input, int ttype, BitSet follow)
        {
            if (input.LA(1) == ttype)
            {
                input.Consume();
                this.errorRecovery = false;
                this.failed = false;
            }
            else if (this.backtracking > 0)
            {
                this.failed = true;
            }
            else
            {
                this.Mismatch(input, ttype, follow);
            }
        }

        public virtual void MatchAny(IIntStream input)
        {
            this.errorRecovery = false;
            this.failed = false;
            input.Consume();
        }

        public virtual void Memoize(IIntStream input, int ruleIndex, int ruleStartIndex)
        {
            int num = this.failed ? -2 : (input.Index() - 1);
            if (this.ruleMemo[ruleIndex] != null)
            {
                this.ruleMemo[ruleIndex][ruleStartIndex] = num;
            }
        }

        protected internal virtual void Mismatch(IIntStream input, int ttype, BitSet follow)
        {
            MismatchedTokenException e = new MismatchedTokenException(ttype, input);
            this.RecoverFromMismatchedToken(input, e, ttype, follow);
        }

        protected void PushFollow(BitSet fset)
        {
            if ((this.followingStackPointer_ + 1) >= this.following.Length)
            {
                BitSet[] destinationArray = new BitSet[this.following.Length * 2];
                Array.Copy(this.following, 0, destinationArray, 0, this.following.Length - 1);
                this.following = destinationArray;
            }
            this.following[++this.followingStackPointer_] = fset;
        }

        public virtual void Recover(IIntStream input, RecognitionException re)
        {
            if (this.lastErrorIndex == input.Index())
            {
                input.Consume();
            }
            this.lastErrorIndex = input.Index();
            BitSet set = this.ComputeErrorRecoverySet();
            this.BeginResync();
            this.ConsumeUntil(input, set);
            this.EndResync();
        }

        protected internal virtual bool RecoverFromMismatchedElement(IIntStream input, RecognitionException e, BitSet follow)
        {
            if (follow != null)
            {
                if (follow.Member(1))
                {
                    BitSet a = this.ComputeContextSensitiveRuleFOLLOW();
                    follow = follow.Or(a);
                    follow.Remove(1);
                }
                if (follow.Member(input.LA(1)))
                {
                    this.ReportError(e);
                    return true;
                }
            }
            return false;
        }

        public virtual void RecoverFromMismatchedSet(IIntStream input, RecognitionException e, BitSet follow)
        {
            if (!this.RecoverFromMismatchedElement(input, e, follow))
            {
                throw e;
            }
        }

        public virtual void RecoverFromMismatchedToken(IIntStream input, RecognitionException e, int ttype, BitSet follow)
        {
            if (input.LA(2) == ttype)
            {
                this.ReportError(e);
                this.BeginResync();
                input.Consume();
                this.EndResync();
                input.Consume();
            }
            else if (!this.RecoverFromMismatchedElement(input, e, follow))
            {
                throw e;
            }
        }

        public virtual void ReportError(RecognitionException e)
        {
            if (!this.errorRecovery)
            {
                this.errorRecovery = true;
                this.DisplayRecognitionError(this.TokenNames, e);
            }
        }

        public virtual void Reset()
        {
            this.followingStackPointer_ = -1;
            this.errorRecovery = false;
            this.lastErrorIndex = -1;
            this.failed = false;
            this.backtracking = 0;
            for (int i = 0; (this.ruleMemo != null) && (i < this.ruleMemo.Length); i++)
            {
                this.ruleMemo[i] = null;
            }
        }

        public virtual IList ToStrings(IList tokens)
        {
            if (tokens == null)
            {
                return null;
            }
            IList list = new ArrayList(tokens.Count);
            for (int i = 0; i < tokens.Count; i++)
            {
                list.Add(((IToken) tokens[i]).Text);
            }
            return list;
        }

        public virtual void TraceIn(string ruleName, int ruleIndex, object inputSymbol)
        {
            Console.Out.Write(string.Concat(new object[] { "enter ", ruleName, " ", inputSymbol }));
            if (this.failed)
            {
                Console.Out.WriteLine(" failed=" + this.failed);
            }
            if (this.backtracking > 0)
            {
                Console.Out.Write(" backtracking=" + this.backtracking);
            }
            Console.Out.WriteLine();
        }

        public virtual void TraceOut(string ruleName, int ruleIndex, object inputSymbol)
        {
            Console.Out.Write(string.Concat(new object[] { "exit ", ruleName, " ", inputSymbol }));
            if (this.failed)
            {
                Console.Out.WriteLine(" failed=" + this.failed);
            }
            if (this.backtracking > 0)
            {
                Console.Out.Write(" backtracking=" + this.backtracking);
            }
            Console.Out.WriteLine();
        }

        public int BacktrackingLevel
        {
            get
            {
                return this.backtracking;
            }
        }

        public virtual string GrammarFileName
        {
            get
            {
                return null;
            }
        }

        public abstract IIntStream Input { get; }

        public virtual string[] TokenNames
        {
            get
            {
                return null;
            }
        }
    }
}

