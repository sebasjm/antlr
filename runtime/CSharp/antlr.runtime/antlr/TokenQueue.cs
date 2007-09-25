namespace antlr
{
    using System;

    internal class TokenQueue
    {
        private IToken[] buffer;
        protected internal int nbrEntries;
        private int offset;
        private int sizeLessOne;

        public TokenQueue(int minSize)
        {
            if (minSize < 0)
            {
                this.init(0x10);
            }
            else if (minSize >= 0x3fffffff)
            {
                this.init(0x7fffffff);
            }
            else
            {
                int size = 2;
                while (size < minSize)
                {
                    size *= 2;
                }
                this.init(size);
            }
        }

        public void append(IToken tok)
        {
            if (this.nbrEntries == this.buffer.Length)
            {
                this.expand();
            }
            this.buffer[(this.offset + this.nbrEntries) & this.sizeLessOne] = tok;
            this.nbrEntries++;
        }

        public IToken elementAt(int idx)
        {
            return this.buffer[(this.offset + idx) & this.sizeLessOne];
        }

        private void expand()
        {
            IToken[] tokenArray = new IToken[this.buffer.Length * 2];
            for (int i = 0; i < this.buffer.Length; i++)
            {
                tokenArray[i] = this.elementAt(i);
            }
            this.buffer = tokenArray;
            this.sizeLessOne = this.buffer.Length - 1;
            this.offset = 0;
        }

        private void init(int size)
        {
            this.buffer = new IToken[size];
            this.sizeLessOne = size - 1;
            this.offset = 0;
            this.nbrEntries = 0;
        }

        public void removeFirst()
        {
            this.offset = (this.offset + 1) & this.sizeLessOne;
            this.nbrEntries--;
        }

        public void reset()
        {
            this.offset = 0;
            this.nbrEntries = 0;
        }
    }
}

