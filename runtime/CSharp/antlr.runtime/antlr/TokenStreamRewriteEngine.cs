namespace antlr
{
    using antlr.collections.impl;
    using System;
    using System.Collections;
    using System.Text;

    public class TokenStreamRewriteEngine : TokenStream
    {
        public const string DEFAULT_PROGRAM_NAME = "default";
        protected BitSet discardMask;
        protected int index;
        protected IDictionary lastRewriteTokenIndexes;
        public const int MIN_TOKEN_INDEX = 0;
        public const int PROGRAM_INIT_SIZE = 100;
        protected IDictionary programs;
        protected TokenStream stream;
        protected IList tokens;

        public TokenStreamRewriteEngine(TokenStream upstream) : this(upstream, 0x3e8)
        {
        }

        public TokenStreamRewriteEngine(TokenStream upstream, int initialSize)
        {
            this.programs = null;
            this.lastRewriteTokenIndexes = null;
            this.index = 0;
            this.discardMask = new BitSet();
            this.stream = upstream;
            this.tokens = new ArrayList(initialSize);
            this.programs = new Hashtable();
            this.programs["default"] = new ArrayList(100);
            this.lastRewriteTokenIndexes = new Hashtable();
        }

        protected void addToSortedRewriteList(RewriteOperation op)
        {
            this.addToSortedRewriteList("default", op);
        }

        protected void addToSortedRewriteList(string programName, RewriteOperation op)
        {
            ArrayList list = (ArrayList) this.getProgram(programName);
            if (op.index >= this.getLastRewriteTokenIndex(programName))
            {
                list.Add(op);
                this.setLastRewriteTokenIndex(programName, op.index);
            }
            else
            {
                int num = list.BinarySearch(op, RewriteOperationComparer.Default);
                if (num < 0)
                {
                    list.Insert(-num - 1, op);
                }
            }
        }

        public void delete(IToken indexT)
        {
            this.delete("default", indexT, indexT);
        }

        public void delete(int index)
        {
            this.delete("default", index, index);
        }

        public void delete(IToken from, IToken to)
        {
            this.delete("default", from, to);
        }

        public void delete(int from, int to)
        {
            this.delete("default", from, to);
        }

        public void delete(string programName, IToken from, IToken to)
        {
            this.replace(programName, from, to, null);
        }

        public void delete(string programName, int from, int to)
        {
            this.replace(programName, from, to, null);
        }

        public void deleteProgram()
        {
            this.deleteProgram("default");
        }

        public void deleteProgram(string programName)
        {
            this.rollback(programName, 0);
        }

        public void discard(int ttype)
        {
            this.discardMask.add(ttype);
        }

        public int getLastRewriteTokenIndex()
        {
            return this.getLastRewriteTokenIndex("default");
        }

        protected int getLastRewriteTokenIndex(string programName)
        {
            object obj2 = this.lastRewriteTokenIndexes[programName];
            if (obj2 == null)
            {
                return -1;
            }
            return (int) obj2;
        }

        protected IList getProgram(string name)
        {
            IList list = (IList) this.programs[name];
            if (list == null)
            {
                list = this.initializeProgram(name);
            }
            return list;
        }

        public TokenWithIndex getToken(int i)
        {
            return (TokenWithIndex) this.tokens[i];
        }

        public int getTokenStreamSize()
        {
            return this.tokens.Count;
        }

        private IList initializeProgram(string name)
        {
            IList list = new ArrayList(100);
            this.programs[name] = list;
            return list;
        }

        public void insertAfter(IToken t, string text)
        {
            this.insertAfter("default", t, text);
        }

        public void insertAfter(int index, string text)
        {
            this.insertAfter("default", index, text);
        }

        public void insertAfter(string programName, IToken t, string text)
        {
            this.insertAfter(programName, ((TokenWithIndex) t).getIndex(), text);
        }

        public void insertAfter(string programName, int index, string text)
        {
            this.insertBefore(programName, (int) (index + 1), text);
        }

        public void insertBefore(IToken t, string text)
        {
            this.insertBefore("default", t, text);
        }

        public void insertBefore(int index, string text)
        {
            this.insertBefore("default", index, text);
        }

        public void insertBefore(string programName, IToken t, string text)
        {
            this.insertBefore(programName, ((TokenWithIndex) t).getIndex(), text);
        }

        public void insertBefore(string programName, int index, string text)
        {
            this.addToSortedRewriteList(programName, new InsertBeforeOp(index, text));
        }

        public IToken nextToken()
        {
            TokenWithIndex index;
            do
            {
                index = (TokenWithIndex) this.stream.nextToken();
                if (index != null)
                {
                    index.setIndex(this.index);
                    if (index.Type != 1)
                    {
                        this.tokens.Add(index);
                    }
                    this.index++;
                }
            }
            while ((index != null) && this.discardMask.member(index.Type));
            return index;
        }

        public void replace(IToken indexT, string text)
        {
            this.replace("default", indexT, indexT, text);
        }

        public void replace(int index, string text)
        {
            this.replace("default", index, index, text);
        }

        public void replace(IToken from, IToken to, string text)
        {
            this.replace("default", from, to, text);
        }

        public void replace(int from, int to, string text)
        {
            this.replace("default", from, to, text);
        }

        public void replace(string programName, IToken from, IToken to, string text)
        {
            this.replace(programName, ((TokenWithIndex) from).getIndex(), ((TokenWithIndex) to).getIndex(), text);
        }

        public void replace(string programName, int from, int to, string text)
        {
            this.addToSortedRewriteList(new ReplaceOp(from, to, text));
        }

        public void rollback(int instructionIndex)
        {
            this.rollback("default", instructionIndex);
        }

        public void rollback(string programName, int instructionIndex)
        {
            ArrayList list = (ArrayList) this.programs[programName];
            if (list != null)
            {
                this.programs[programName] = list.GetRange(0, instructionIndex);
            }
        }

        protected void setLastRewriteTokenIndex(string programName, int i)
        {
            this.lastRewriteTokenIndexes[programName] = i;
        }

        public string ToDebugString()
        {
            return this.ToDebugString(0, this.getTokenStreamSize());
        }

        public string ToDebugString(int start, int end)
        {
            StringBuilder builder = new StringBuilder();
            for (int i = start; ((i >= 0) && (i <= end)) && (i < this.tokens.Count); i++)
            {
                builder.Append(this.getToken(i));
            }
            return builder.ToString();
        }

        public string ToOriginalString()
        {
            return this.ToOriginalString(0, this.getTokenStreamSize() - 1);
        }

        public string ToOriginalString(int start, int end)
        {
            StringBuilder builder = new StringBuilder();
            for (int i = start; ((i >= 0) && (i <= end)) && (i < this.tokens.Count); i++)
            {
                builder.Append(this.getToken(i).getText());
            }
            return builder.ToString();
        }

        public override string ToString()
        {
            return this.ToString(0, this.getTokenStreamSize());
        }

        public string ToString(string programName)
        {
            return this.ToString(programName, 0, this.getTokenStreamSize());
        }

        public string ToString(int start, int end)
        {
            return this.ToString("default", start, end);
        }

        public string ToString(string programName, int start, int end)
        {
            IList list = (IList) this.programs[programName];
            if (list == null)
            {
                return null;
            }
            StringBuilder buf = new StringBuilder();
            int num = 0;
            int i = start;
            while (((i >= 0) && (i <= end)) && (i < this.tokens.Count))
            {
                if (num < list.Count)
                {
                    RewriteOperation operation = (RewriteOperation) list[num];
                    while ((i == operation.index) && (num < list.Count))
                    {
                        i = operation.execute(buf);
                        num++;
                        if (num < list.Count)
                        {
                            operation = (RewriteOperation) list[num];
                        }
                    }
                }
                if (i < end)
                {
                    buf.Append(this.getToken(i).getText());
                    i++;
                }
            }
            for (int j = num; j < list.Count; j++)
            {
                ((RewriteOperation) list[j]).execute(buf);
            }
            return buf.ToString();
        }

        protected class DeleteOp : TokenStreamRewriteEngine.ReplaceOp
        {
            public DeleteOp(int from, int to) : base(from, to, null)
            {
            }
        }

        protected class InsertBeforeOp : TokenStreamRewriteEngine.RewriteOperation
        {
            public InsertBeforeOp(int index, string text) : base(index, text)
            {
            }

            public override int execute(StringBuilder buf)
            {
                buf.Append(base.text);
                return base.index;
            }
        }

        protected class ReplaceOp : TokenStreamRewriteEngine.RewriteOperation
        {
            protected int lastIndex;

            public ReplaceOp(int from, int to, string text) : base(from, text)
            {
                this.lastIndex = to;
            }

            public override int execute(StringBuilder buf)
            {
                if (base.text != null)
                {
                    buf.Append(base.text);
                }
                return (this.lastIndex + 1);
            }
        }

        protected class RewriteOperation
        {
            protected internal int index;
            protected internal string text;

            protected RewriteOperation(int index, string text)
            {
                this.index = index;
                this.text = text;
            }

            public virtual int execute(StringBuilder buf)
            {
                return this.index;
            }
        }

        public class RewriteOperationComparer : IComparer
        {
            public static readonly TokenStreamRewriteEngine.RewriteOperationComparer Default = new TokenStreamRewriteEngine.RewriteOperationComparer();

            public virtual int Compare(object o1, object o2)
            {
                TokenStreamRewriteEngine.RewriteOperation operation = (TokenStreamRewriteEngine.RewriteOperation) o1;
                TokenStreamRewriteEngine.RewriteOperation operation2 = (TokenStreamRewriteEngine.RewriteOperation) o2;
                if (operation.index < operation2.index)
                {
                    return -1;
                }
                if (operation.index > operation2.index)
                {
                    return 1;
                }
                return 0;
            }
        }
    }
}

