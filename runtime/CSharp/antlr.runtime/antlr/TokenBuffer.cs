namespace antlr
{
    using System;

    public class TokenBuffer
    {
        protected internal TokenStream input;
        protected internal int markerOffset = 0;
        protected internal int nMarkers = 0;
        protected internal int numToConsume = 0;
        internal TokenQueue queue;

        public TokenBuffer(TokenStream input_)
        {
            this.input = input_;
            this.queue = new TokenQueue(1);
        }

        public virtual void consume()
        {
            this.numToConsume++;
        }

        protected virtual void fill(int amount)
        {
            this.syncConsume();
            while (this.queue.nbrEntries < (amount + this.markerOffset))
            {
                this.queue.append(this.input.nextToken());
            }
        }

        public virtual TokenStream getInput()
        {
            return this.input;
        }

        public virtual int LA(int i)
        {
            this.fill(i);
            return this.queue.elementAt((this.markerOffset + i) - 1).Type;
        }

        public virtual IToken LT(int i)
        {
            this.fill(i);
            return this.queue.elementAt((this.markerOffset + i) - 1);
        }

        public virtual int mark()
        {
            this.syncConsume();
            this.nMarkers++;
            return this.markerOffset;
        }

        public virtual void reset()
        {
            this.nMarkers = 0;
            this.markerOffset = 0;
            this.numToConsume = 0;
            this.queue.reset();
        }

        public virtual void rewind(int mark)
        {
            this.syncConsume();
            this.markerOffset = mark;
            this.nMarkers--;
        }

        protected virtual void syncConsume()
        {
            while (this.numToConsume > 0)
            {
                if (this.nMarkers > 0)
                {
                    this.markerOffset++;
                }
                else
                {
                    this.queue.removeFirst();
                }
                this.numToConsume--;
            }
        }
    }
}

