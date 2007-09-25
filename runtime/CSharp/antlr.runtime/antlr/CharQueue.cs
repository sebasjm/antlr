namespace antlr
{
    using System;

    public class CharQueue
    {
        protected internal char[] buffer;
        protected internal int nbrEntries;
        private int offset;
        private int sizeLessOne;

        public CharQueue(int minSize)
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

        public void append(char tok)
        {
            if (this.nbrEntries == this.buffer.Length)
            {
                this.expand();
            }
            this.buffer[(this.offset + this.nbrEntries) & this.sizeLessOne] = tok;
            this.nbrEntries++;
        }

        public char elementAt(int idx)
        {
            return this.buffer[(this.offset + idx) & this.sizeLessOne];
        }

        private void expand()
        {
            char[] chArray = new char[this.buffer.Length * 2];
            for (int i = 0; i < this.buffer.Length; i++)
            {
                chArray[i] = this.elementAt(i);
            }
            this.buffer = chArray;
            this.sizeLessOne = this.buffer.Length - 1;
            this.offset = 0;
        }

        public virtual void init(int size)
        {
            this.buffer = new char[size];
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

