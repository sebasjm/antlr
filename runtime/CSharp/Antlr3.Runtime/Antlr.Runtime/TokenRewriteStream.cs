namespace Antlr.Runtime
{
    using System;
    using System.Collections;
    using System.Text;

    public class TokenRewriteStream : CommonTokenStream
    {
        public const string DEFAULT_PROGRAM_NAME = "default";
        protected IDictionary lastRewriteTokenIndexes;
        public const int MIN_TOKEN_INDEX = 0;
        public const int PROGRAM_INIT_SIZE = 100;
        protected IDictionary programs;

        public TokenRewriteStream()
        {
            this.programs = null;
            this.lastRewriteTokenIndexes = null;
            this.Init();
        }

        public TokenRewriteStream(ITokenSource tokenSource) : base(tokenSource)
        {
            this.programs = null;
            this.lastRewriteTokenIndexes = null;
            this.Init();
        }

        public TokenRewriteStream(ITokenSource tokenSource, int channel) : base(tokenSource, channel)
        {
            this.programs = null;
            this.lastRewriteTokenIndexes = null;
            this.Init();
        }

        protected virtual void AddToSortedRewriteList(RewriteOperation op)
        {
            this.AddToSortedRewriteList("default", op);
        }

        protected virtual void AddToSortedRewriteList(string programName, RewriteOperation op)
        {
            IList program = this.GetProgram(programName);
            int index = ((ArrayList) program).BinarySearch(op, new RewriteOpComparer());
            if (index < 0)
            {
                program.Insert(-index - 1, op);
            }
            else
            {
                RewriteOperation operation;
                while (index >= 0)
                {
                    operation = (RewriteOperation) program[index];
                    if (operation.index < op.index)
                    {
                        break;
                    }
                    index--;
                }
                index++;
                if (!(op is ReplaceOp))
                {
                    program.Insert(index, op);
                }
                else
                {
                    bool flag = false;
                    int num2 = index;
                    while (num2 < program.Count)
                    {
                        operation = (RewriteOperation) program[index];
                        if (operation.index != op.index)
                        {
                            break;
                        }
                        if (operation is ReplaceOp)
                        {
                            program[index] = op;
                            flag = true;
                            break;
                        }
                        num2++;
                    }
                    if (!flag)
                    {
                        program.Insert(num2, op);
                    }
                }
            }
        }

        public virtual void Delete(IToken indexT)
        {
            this.Delete("default", indexT, indexT);
        }

        public virtual void Delete(int index)
        {
            this.Delete("default", index, index);
        }

        public virtual void Delete(IToken from, IToken to)
        {
            this.Delete("default", from, to);
        }

        public virtual void Delete(int from, int to)
        {
            this.Delete("default", from, to);
        }

        public virtual void Delete(string programName, IToken from, IToken to)
        {
            this.Replace(programName, from, to, null);
        }

        public virtual void Delete(string programName, int from, int to)
        {
            this.Replace(programName, from, to, null);
        }

        public virtual void DeleteProgram()
        {
            this.DeleteProgram("default");
        }

        public virtual void DeleteProgram(string programName)
        {
            this.Rollback(programName, 0);
        }

        public virtual int GetLastRewriteTokenIndex()
        {
            return this.GetLastRewriteTokenIndex("default");
        }

        protected virtual int GetLastRewriteTokenIndex(string programName)
        {
            object obj2 = this.lastRewriteTokenIndexes[programName];
            if (obj2 == null)
            {
                return -1;
            }
            return (int) obj2;
        }

        protected virtual IList GetProgram(string name)
        {
            IList list = (IList) this.programs[name];
            if (list == null)
            {
                list = this.InitializeProgram(name);
            }
            return list;
        }

        protected internal virtual void Init()
        {
            this.programs = new Hashtable();
            this.programs["default"] = new ArrayList(100);
            this.lastRewriteTokenIndexes = new Hashtable();
        }

        private IList InitializeProgram(string name)
        {
            IList list = new ArrayList(100);
            this.programs[name] = list;
            return list;
        }

        public virtual void InsertAfter(IToken t, object text)
        {
            this.InsertAfter("default", t, text);
        }

        public virtual void InsertAfter(int index, object text)
        {
            this.InsertAfter("default", index, text);
        }

        public virtual void InsertAfter(string programName, IToken t, object text)
        {
            this.InsertAfter(programName, t.TokenIndex, text);
        }

        public virtual void InsertAfter(string programName, int index, object text)
        {
            this.InsertBefore(programName, (int) (index + 1), text);
        }

        public virtual void InsertBefore(IToken t, object text)
        {
            this.InsertBefore("default", t, text);
        }

        public virtual void InsertBefore(int index, object text)
        {
            this.InsertBefore("default", index, text);
        }

        public virtual void InsertBefore(string programName, IToken t, object text)
        {
            this.InsertBefore(programName, t.TokenIndex, text);
        }

        public virtual void InsertBefore(string programName, int index, object text)
        {
            this.AddToSortedRewriteList(programName, new InsertBeforeOp(index, text));
        }

        public virtual void Replace(IToken indexT, object text)
        {
            this.Replace("default", indexT, indexT, text);
        }

        public virtual void Replace(int index, object text)
        {
            this.Replace("default", index, index, text);
        }

        public virtual void Replace(IToken from, IToken to, object text)
        {
            this.Replace("default", from, to, text);
        }

        public virtual void Replace(int from, int to, object text)
        {
            this.Replace("default", from, to, text);
        }

        public virtual void Replace(string programName, IToken from, IToken to, object text)
        {
            this.Replace(programName, from.TokenIndex, to.TokenIndex, text);
        }

        public virtual void Replace(string programName, int from, int to, object text)
        {
            if (((from <= to) && (from >= 0)) && (to >= 0))
            {
                this.AddToSortedRewriteList(programName, new ReplaceOp(from, to, text));
            }
        }

        public virtual void Rollback(int instructionIndex)
        {
            this.Rollback("default", instructionIndex);
        }

        public virtual void Rollback(string programName, int instructionIndex)
        {
            IList list = (IList) this.programs[programName];
            if (list != null)
            {
                this.programs[programName] = ((ArrayList) list).GetRange(0, instructionIndex);
            }
        }

        protected virtual void SetLastRewriteTokenIndex(string programName, int i)
        {
            this.lastRewriteTokenIndexes[programName] = i;
        }

        public virtual string ToDebugString()
        {
            return this.ToDebugString(0, this.Size() - 1);
        }

        public virtual string ToDebugString(int start, int end)
        {
            StringBuilder builder = new StringBuilder();
            for (int i = start; ((i >= 0) && (i <= end)) && (i < base.tokens.Count); i++)
            {
                builder.Append(this.Get(i));
            }
            return builder.ToString();
        }

        public virtual string ToOriginalString()
        {
            return this.ToOriginalString(0, this.Size() - 1);
        }

        public virtual string ToOriginalString(int start, int end)
        {
            StringBuilder builder = new StringBuilder();
            for (int i = start; ((i >= 0) && (i <= end)) && (i < base.tokens.Count); i++)
            {
                builder.Append(this.Get(i).Text);
            }
            return builder.ToString();
        }

        public override string ToString()
        {
            return this.ToString(0, this.Size() - 1);
        }

        public virtual string ToString(string programName)
        {
            return this.ToString(programName, 0, this.Size() - 1);
        }

        public override string ToString(int start, int end)
        {
            return this.ToString("default", start, end);
        }

        public virtual string ToString(string programName, int start, int end)
        {
            RewriteOperation operation;
            IList list = (IList) this.programs[programName];
            if ((list == null) || (list.Count == 0))
            {
                return this.ToOriginalString(start, end);
            }
            StringBuilder buf = new StringBuilder();
            int num = 0;
            int i = start;
            while (((i >= 0) && (i <= end)) && (i < base.tokens.Count))
            {
                if (num < list.Count)
                {
                    operation = (RewriteOperation) list[num];
                    while ((operation.index < i) && (num < list.Count))
                    {
                        num++;
                        if (num < list.Count)
                        {
                            operation = (RewriteOperation) list[num];
                        }
                    }
                    while ((i == operation.index) && (num < list.Count))
                    {
                        i = operation.Execute(buf);
                        num++;
                        if (num < list.Count)
                        {
                            operation = (RewriteOperation) list[num];
                        }
                    }
                }
                if (i <= end)
                {
                    buf.Append(this.Get(i).Text);
                    i++;
                }
            }
            for (int j = num; j < list.Count; j++)
            {
                operation = (RewriteOperation) list[j];
                if (operation.index >= this.Size())
                {
                    operation.Execute(buf);
                }
            }
            return buf.ToString();
        }

        internal protected class DeleteOp : TokenRewriteStream.ReplaceOp
        {
            public DeleteOp(int from, int to) : base(from, to, null)
            {
            }
        }

        internal protected class InsertBeforeOp : TokenRewriteStream.RewriteOperation
        {
            public InsertBeforeOp(int index, object text) : base(index, text)
            {
            }

            public override int Execute(StringBuilder buf)
            {
                buf.Append(base.text);
                return base.index;
            }
        }

        internal protected class ReplaceOp : TokenRewriteStream.RewriteOperation
        {
            private int lastIndex;

            public ReplaceOp(int from, int to, object text) : base(from, text)
            {
                this.lastIndex = to;
            }

            public override int Execute(StringBuilder buf)
            {
                if (base.text != null)
                {
                    buf.Append(base.text);
                }
                return (this.lastIndex + 1);
            }
        }

        private class RewriteOpComparer : IComparer
        {
            public virtual int Compare(object o1, object o2)
            {
                TokenRewriteStream.RewriteOperation operation = (TokenRewriteStream.RewriteOperation) o1;
                TokenRewriteStream.RewriteOperation operation2 = (TokenRewriteStream.RewriteOperation) o2;
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

        internal protected class RewriteOperation
        {
            protected internal int index;
            protected internal object text;

            protected internal RewriteOperation(int index, object text)
            {
                this.index = index;
                this.text = text;
            }

            public virtual int Execute(StringBuilder buf)
            {
                return this.index;
            }

            public override string ToString()
            {
                string fullName = base.GetType().FullName;
                int index = fullName.IndexOf('$');
                fullName = fullName.Substring(index + 1, fullName.Length - (index + 1));
                return string.Concat(new object[] { fullName, "@", this.index, '"', this.text, '"' });
            }
        }
    }
}

