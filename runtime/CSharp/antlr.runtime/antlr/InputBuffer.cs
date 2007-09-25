namespace antlr
{
    using System;
    using System.Collections;
    using System.Text;

    public abstract class InputBuffer
    {
        protected internal int markerOffset = 0;
        protected internal int nMarkers = 0;
        protected internal int numToConsume = 0;
        protected ArrayList queue = new ArrayList();

        public virtual void commit()
        {
            this.nMarkers--;
        }

        public virtual char consume()
        {
            this.numToConsume++;
            return this.LA(1);
        }

        public abstract void fill(int amount);
        public virtual string getLAChars()
        {
            StringBuilder builder = new StringBuilder();
            char[] array = new char[this.queue.Count - this.markerOffset];
            this.queue.CopyTo(array, this.markerOffset);
            builder.Append(array);
            return builder.ToString();
        }

        public virtual string getMarkedChars()
        {
            StringBuilder builder = new StringBuilder();
            char[] array = new char[this.queue.Count - this.markerOffset];
            this.queue.CopyTo(array, this.markerOffset);
            builder.Append(array);
            return builder.ToString();
        }

        public virtual bool isMarked()
        {
            return (this.nMarkers != 0);
        }

        public virtual char LA(int i)
        {
            this.fill(i);
            return (char) this.queue[(this.markerOffset + i) - 1];
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
            this.queue.Clear();
        }

        public virtual void rewind(int mark)
        {
            this.syncConsume();
            this.markerOffset = mark;
            this.nMarkers--;
        }

        protected internal virtual void syncConsume()
        {
            if (this.numToConsume > 0)
            {
                if (this.nMarkers > 0)
                {
                    this.markerOffset += this.numToConsume;
                }
                else
                {
                    this.queue.RemoveRange(0, this.numToConsume);
                }
                this.numToConsume = 0;
            }
        }
    }
}

