namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using System;

    public class DebugTokenStream : ITokenStream, IIntStream
    {
        protected internal IDebugEventListener dbg;
        protected internal bool initialStreamState = true;
        public ITokenStream input;
        protected int lastMarker;

        public DebugTokenStream(ITokenStream input, IDebugEventListener dbg)
        {
            this.input = input;
            this.DebugListener = dbg;
            input.LT(1);
        }

        public virtual void Consume()
        {
            if (this.initialStreamState)
            {
                this.ConsumeInitialHiddenTokens();
            }
            int num = this.input.Index();
            IToken t = this.input.LT(1);
            this.input.Consume();
            int num2 = this.input.Index();
            this.dbg.ConsumeToken(t);
            if (num2 > (num + 1))
            {
                for (int i = num + 1; i < num2; i++)
                {
                    this.dbg.ConsumeHiddenToken(this.input.Get(i));
                }
            }
        }

        protected internal virtual void ConsumeInitialHiddenTokens()
        {
            int num = this.input.Index();
            for (int i = 0; i < num; i++)
            {
                this.dbg.ConsumeHiddenToken(this.input.Get(i));
            }
            this.initialStreamState = false;
        }

        public virtual IToken Get(int i)
        {
            return this.input.Get(i);
        }

        public virtual int Index()
        {
            return this.input.Index();
        }

        public virtual int LA(int i)
        {
            if (this.initialStreamState)
            {
                this.ConsumeInitialHiddenTokens();
            }
            this.dbg.LT(i, this.input.LT(i));
            return this.input.LA(i);
        }

        public virtual IToken LT(int i)
        {
            if (this.initialStreamState)
            {
                this.ConsumeInitialHiddenTokens();
            }
            this.dbg.LT(i, this.input.LT(i));
            return this.input.LT(i);
        }

        public virtual int Mark()
        {
            this.lastMarker = this.input.Mark();
            this.dbg.Mark(this.lastMarker);
            return this.lastMarker;
        }

        public virtual void Release(int marker)
        {
        }

        public virtual void Rewind()
        {
            this.dbg.Rewind();
            this.input.Rewind(this.lastMarker);
        }

        public virtual void Rewind(int marker)
        {
            this.dbg.Rewind(marker);
            this.input.Rewind(marker);
        }

        public virtual void Seek(int index)
        {
            this.input.Seek(index);
        }

        public virtual int Size()
        {
            return this.input.Size();
        }

        public override string ToString()
        {
            return this.input.ToString();
        }

        public virtual string ToString(IToken start, IToken stop)
        {
            return this.input.ToString(start, stop);
        }

        public virtual string ToString(int start, int stop)
        {
            return this.input.ToString(start, stop);
        }

        public virtual IDebugEventListener DebugListener
        {
            set
            {
                this.dbg = value;
            }
        }

        public virtual ITokenSource TokenSource
        {
            get
            {
                return this.input.TokenSource;
            }
        }
    }
}

