namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using Antlr.Runtime.Misc;
    using System;
    using System.Collections;
    using System.Text;

    public class Profiler : BlankDebugEventListener
    {
        protected internal int decisionLevel;
        public int[] decisionMaxCyclicLookaheads;
        public int[] decisionMaxFixedLookaheads;
        public IList decisionMaxSynPredLookaheads;
        protected internal CommonToken lastTokenConsumed;
        protected IList lookaheadStack;
        protected internal int maxLookaheadInCurrentDecision;
        public int maxRuleInvocationDepth;
        public const int NUM_RUNTIME_STATS = 0x1d;
        public int numBacktrackDecisions;
        protected int numberReportedErrors;
        public int numCharsMatched;
        public int numCyclicDecisions;
        public int numFixedDecisions;
        public int numGuessingRuleInvocations;
        public int numHiddenCharsMatched;
        public int numHiddenTokens;
        public int numMemoizationCacheEntries;
        public int numMemoizationCacheHits;
        public int numMemoizationCacheMisses;
        public int numRuleInvocations;
        public int numSemanticPredicates;
        public int numSyntacticPredicates;
        public DebugParser parser;
        protected internal int ruleLevel;
        public const string RUNTIME_STATS_FILENAME = "runtime.stats";
        public const string Version = "2";

        public Profiler()
        {
            this.parser = null;
            this.ruleLevel = 0;
            this.decisionLevel = 0;
            this.maxLookaheadInCurrentDecision = 0;
            this.lastTokenConsumed = null;
            this.lookaheadStack = new ArrayList();
            this.numRuleInvocations = 0;
            this.numGuessingRuleInvocations = 0;
            this.maxRuleInvocationDepth = 0;
            this.numFixedDecisions = 0;
            this.numCyclicDecisions = 0;
            this.numBacktrackDecisions = 0;
            this.decisionMaxFixedLookaheads = new int[200];
            this.decisionMaxCyclicLookaheads = new int[200];
            this.decisionMaxSynPredLookaheads = new ArrayList();
            this.numHiddenTokens = 0;
            this.numCharsMatched = 0;
            this.numHiddenCharsMatched = 0;
            this.numSemanticPredicates = 0;
            this.numSyntacticPredicates = 0;
            this.numberReportedErrors = 0;
            this.numMemoizationCacheMisses = 0;
            this.numMemoizationCacheHits = 0;
            this.numMemoizationCacheEntries = 0;
        }

        public Profiler(DebugParser parser)
        {
            this.parser = null;
            this.ruleLevel = 0;
            this.decisionLevel = 0;
            this.maxLookaheadInCurrentDecision = 0;
            this.lastTokenConsumed = null;
            this.lookaheadStack = new ArrayList();
            this.numRuleInvocations = 0;
            this.numGuessingRuleInvocations = 0;
            this.maxRuleInvocationDepth = 0;
            this.numFixedDecisions = 0;
            this.numCyclicDecisions = 0;
            this.numBacktrackDecisions = 0;
            this.decisionMaxFixedLookaheads = new int[200];
            this.decisionMaxCyclicLookaheads = new int[200];
            this.decisionMaxSynPredLookaheads = new ArrayList();
            this.numHiddenTokens = 0;
            this.numCharsMatched = 0;
            this.numHiddenCharsMatched = 0;
            this.numSemanticPredicates = 0;
            this.numSyntacticPredicates = 0;
            this.numberReportedErrors = 0;
            this.numMemoizationCacheMisses = 0;
            this.numMemoizationCacheHits = 0;
            this.numMemoizationCacheEntries = 0;
            this.parser = parser;
        }

        public override void BeginBacktrack(int level)
        {
            this.numBacktrackDecisions++;
        }

        public override void ConsumeHiddenToken(IToken token)
        {
            this.lastTokenConsumed = (CommonToken) token;
        }

        public override void ConsumeToken(IToken token)
        {
            this.lastTokenConsumed = (CommonToken) token;
        }

        protected static string[] DecodeReportData(string data)
        {
            string[] strArray = data.Split(new char[] { '\t' });
            if (strArray.Length != 0x1d)
            {
                return null;
            }
            return strArray;
        }

        public override void EndBacktrack(int level, bool successful)
        {
            this.decisionMaxSynPredLookaheads.Add(this.maxLookaheadInCurrentDecision);
        }

        public override void EnterDecision(int decisionNumber)
        {
            this.decisionLevel++;
            int num = this.parser.TokenStream.Index();
            this.lookaheadStack.Add(num);
        }

        public override void EnterRule(string ruleName)
        {
            this.ruleLevel++;
            this.numRuleInvocations++;
            if (this.ruleLevel > this.maxRuleInvocationDepth)
            {
                this.maxRuleInvocationDepth = this.ruleLevel;
            }
        }

        public void ExamineRuleMemoization(IIntStream input, int ruleIndex, string ruleName)
        {
            if (this.parser.GetRuleMemoization(ruleIndex, input.Index()) == -1)
            {
                this.numMemoizationCacheMisses++;
                this.numGuessingRuleInvocations++;
            }
            else
            {
                this.numMemoizationCacheHits++;
            }
        }

        public override void ExitDecision(int decisionNumber)
        {
            int[] numArray;
            if (this.parser.isCyclicDecision)
            {
                this.numCyclicDecisions++;
            }
            else
            {
                this.numFixedDecisions++;
            }
            this.lookaheadStack.Remove(this.lookaheadStack.Count - 1);
            this.decisionLevel--;
            if (this.parser.isCyclicDecision)
            {
                if (this.numCyclicDecisions >= this.decisionMaxCyclicLookaheads.Length)
                {
                    numArray = new int[this.decisionMaxCyclicLookaheads.Length * 2];
                    Array.Copy(this.decisionMaxCyclicLookaheads, 0, numArray, 0, this.decisionMaxCyclicLookaheads.Length);
                    this.decisionMaxCyclicLookaheads = numArray;
                }
                this.decisionMaxCyclicLookaheads[this.numCyclicDecisions - 1] = this.maxLookaheadInCurrentDecision;
            }
            else
            {
                if (this.numFixedDecisions >= this.decisionMaxFixedLookaheads.Length)
                {
                    numArray = new int[this.decisionMaxFixedLookaheads.Length * 2];
                    Array.Copy(this.decisionMaxFixedLookaheads, 0, numArray, 0, this.decisionMaxFixedLookaheads.Length);
                    this.decisionMaxFixedLookaheads = numArray;
                }
                this.decisionMaxFixedLookaheads[this.numFixedDecisions - 1] = this.maxLookaheadInCurrentDecision;
            }
            this.parser.isCyclicDecision = false;
            this.maxLookaheadInCurrentDecision = 0;
        }

        public override void ExitRule(string ruleName)
        {
            this.ruleLevel--;
        }

        public int GetNumberOfHiddenTokens(int i, int j)
        {
            int num = 0;
            ITokenStream tokenStream = this.parser.TokenStream;
            for (int k = i; (k < tokenStream.Size()) && (k <= j); k++)
            {
                if (tokenStream.Get(k).Channel != 0)
                {
                    num++;
                }
            }
            return num;
        }

        public bool InDecision()
        {
            return (this.decisionLevel > 0);
        }

        public override void LT(int i, IToken t)
        {
            if (this.InDecision())
            {
                int num = this.lookaheadStack.Count - 1;
                int num2 = (int) this.lookaheadStack[num];
                int j = this.parser.TokenStream.Index();
                int numberOfHiddenTokens = this.GetNumberOfHiddenTokens(num2, j);
                int num5 = ((i + j) - num2) - numberOfHiddenTokens;
                if (num5 > this.maxLookaheadInCurrentDecision)
                {
                    this.maxLookaheadInCurrentDecision = num5;
                }
            }
        }

        public void Memoize(IIntStream input, int ruleIndex, int ruleStartIndex, string ruleName)
        {
            this.numMemoizationCacheEntries++;
        }

        public override void RecognitionException(Antlr.Runtime.RecognitionException e)
        {
            this.numberReportedErrors++;
        }

        public override void SemanticPredicate(bool result, string predicate)
        {
            if (this.InDecision())
            {
                this.numSemanticPredicates++;
            }
        }

        public override void Terminate()
        {
            string data = this.ToNotifyString();
            Stats.WriteReport("runtime.stats", data);
            Console.Out.WriteLine(ToString(data));
        }

        protected int[] ToArray(IList a)
        {
            int[] array = new int[a.Count];
            a.CopyTo(array, 0);
            return array;
        }

        public virtual string ToNotifyString()
        {
            ITokenStream tokenStream = this.parser.TokenStream;
            for (int i = 0; ((i < tokenStream.Size()) && (this.lastTokenConsumed != null)) && (i <= this.lastTokenConsumed.TokenIndex); i++)
            {
                IToken token = tokenStream.Get(i);
                if (token.Channel != 0)
                {
                    this.numHiddenTokens++;
                    this.numHiddenCharsMatched += token.Text.Length;
                }
            }
            this.numCharsMatched = this.lastTokenConsumed.StopIndex + 1;
            this.decisionMaxFixedLookaheads = this.Trim(this.decisionMaxFixedLookaheads, this.numFixedDecisions);
            this.decisionMaxCyclicLookaheads = this.Trim(this.decisionMaxCyclicLookaheads, this.numCyclicDecisions);
            StringBuilder builder = new StringBuilder();
            builder.Append("2");
            builder.Append('\t');
            builder.Append(this.parser.GetType().FullName);
            builder.Append('\t');
            builder.Append(this.numRuleInvocations);
            builder.Append('\t');
            builder.Append(this.maxRuleInvocationDepth);
            builder.Append('\t');
            builder.Append(this.numFixedDecisions);
            builder.Append('\t');
            builder.Append(Stats.Min(this.decisionMaxFixedLookaheads));
            builder.Append('\t');
            builder.Append(Stats.Max(this.decisionMaxFixedLookaheads));
            builder.Append('\t');
            builder.Append(Stats.Avg(this.decisionMaxFixedLookaheads));
            builder.Append('\t');
            builder.Append(Stats.Stddev(this.decisionMaxFixedLookaheads));
            builder.Append('\t');
            builder.Append(this.numCyclicDecisions);
            builder.Append('\t');
            builder.Append(Stats.Min(this.decisionMaxCyclicLookaheads));
            builder.Append('\t');
            builder.Append(Stats.Max(this.decisionMaxCyclicLookaheads));
            builder.Append('\t');
            builder.Append(Stats.Avg(this.decisionMaxCyclicLookaheads));
            builder.Append('\t');
            builder.Append(Stats.Stddev(this.decisionMaxCyclicLookaheads));
            builder.Append('\t');
            builder.Append(this.numBacktrackDecisions);
            builder.Append('\t');
            builder.Append(Stats.Min(this.ToArray(this.decisionMaxSynPredLookaheads)));
            builder.Append('\t');
            builder.Append(Stats.Max(this.ToArray(this.decisionMaxSynPredLookaheads)));
            builder.Append('\t');
            builder.Append(Stats.Avg(this.ToArray(this.decisionMaxSynPredLookaheads)));
            builder.Append('\t');
            builder.Append(Stats.Stddev(this.ToArray(this.decisionMaxSynPredLookaheads)));
            builder.Append('\t');
            builder.Append(this.numSemanticPredicates);
            builder.Append('\t');
            builder.Append(this.parser.TokenStream.Size());
            builder.Append('\t');
            builder.Append(this.numHiddenTokens);
            builder.Append('\t');
            builder.Append(this.numCharsMatched);
            builder.Append('\t');
            builder.Append(this.numHiddenCharsMatched);
            builder.Append('\t');
            builder.Append(this.numberReportedErrors);
            builder.Append('\t');
            builder.Append(this.numMemoizationCacheHits);
            builder.Append('\t');
            builder.Append(this.numMemoizationCacheMisses);
            builder.Append('\t');
            builder.Append(this.numGuessingRuleInvocations);
            builder.Append('\t');
            builder.Append(this.numMemoizationCacheEntries);
            return builder.ToString();
        }

        public override string ToString()
        {
            return ToString(this.ToNotifyString());
        }

        public static string ToString(string notifyDataLine)
        {
            string[] strArray = DecodeReportData(notifyDataLine);
            if (strArray == null)
            {
                return null;
            }
            StringBuilder builder = new StringBuilder();
            builder.Append("ANTLR Runtime Report; Profile Version ");
            builder.Append(strArray[0]);
            builder.Append('\n');
            builder.Append("parser name ");
            builder.Append(strArray[1]);
            builder.Append('\n');
            builder.Append("Number of rule invocations ");
            builder.Append(strArray[2]);
            builder.Append('\n');
            builder.Append("Number of rule invocations in \"guessing\" mode ");
            builder.Append(strArray[0x1b]);
            builder.Append('\n');
            builder.Append("max rule invocation nesting depth ");
            builder.Append(strArray[3]);
            builder.Append('\n');
            builder.Append("number of fixed lookahead decisions ");
            builder.Append(strArray[4]);
            builder.Append('\n');
            builder.Append("min lookahead used in a fixed lookahead decision ");
            builder.Append(strArray[5]);
            builder.Append('\n');
            builder.Append("max lookahead used in a fixed lookahead decision ");
            builder.Append(strArray[6]);
            builder.Append('\n');
            builder.Append("average lookahead depth used in fixed lookahead decisions ");
            builder.Append(strArray[7]);
            builder.Append('\n');
            builder.Append("standard deviation of depth used in fixed lookahead decisions ");
            builder.Append(strArray[8]);
            builder.Append('\n');
            builder.Append("number of arbitrary lookahead decisions ");
            builder.Append(strArray[9]);
            builder.Append('\n');
            builder.Append("min lookahead used in an arbitrary lookahead decision ");
            builder.Append(strArray[10]);
            builder.Append('\n');
            builder.Append("max lookahead used in an arbitrary lookahead decision ");
            builder.Append(strArray[11]);
            builder.Append('\n');
            builder.Append("average lookahead depth used in arbitrary lookahead decisions ");
            builder.Append(strArray[12]);
            builder.Append('\n');
            builder.Append("standard deviation of depth used in arbitrary lookahead decisions ");
            builder.Append(strArray[13]);
            builder.Append('\n');
            builder.Append("number of evaluated syntactic predicates ");
            builder.Append(strArray[14]);
            builder.Append('\n');
            builder.Append("min lookahead used in a syntactic predicate ");
            builder.Append(strArray[15]);
            builder.Append('\n');
            builder.Append("max lookahead used in a syntactic predicate ");
            builder.Append(strArray[0x10]);
            builder.Append('\n');
            builder.Append("average lookahead depth used in syntactic predicates ");
            builder.Append(strArray[0x11]);
            builder.Append('\n');
            builder.Append("standard deviation of depth used in syntactic predicates ");
            builder.Append(strArray[0x12]);
            builder.Append('\n');
            builder.Append("rule memoization cache size ");
            builder.Append(strArray[0x1c]);
            builder.Append('\n');
            builder.Append("number of rule memoization cache hits ");
            builder.Append(strArray[0x19]);
            builder.Append('\n');
            builder.Append("number of rule memoization cache misses ");
            builder.Append(strArray[0x1a]);
            builder.Append('\n');
            builder.Append("number of evaluated semantic predicates ");
            builder.Append(strArray[0x13]);
            builder.Append('\n');
            builder.Append("number of tokens ");
            builder.Append(strArray[20]);
            builder.Append('\n');
            builder.Append("number of hidden tokens ");
            builder.Append(strArray[0x15]);
            builder.Append('\n');
            builder.Append("number of char ");
            builder.Append(strArray[0x16]);
            builder.Append('\n');
            builder.Append("number of hidden char ");
            builder.Append(strArray[0x17]);
            builder.Append('\n');
            builder.Append("number of syntax errors ");
            builder.Append(strArray[0x18]);
            builder.Append('\n');
            return builder.ToString();
        }

        protected int[] Trim(int[] X, int n)
        {
            if (n < X.Length)
            {
                int[] destinationArray = new int[n];
                Array.Copy(X, 0, destinationArray, 0, n);
                X = destinationArray;
            }
            return X;
        }

        public virtual DebugParser Parser
        {
            set
            {
                this.parser = value;
            }
        }
    }
}

