namespace antlr.debug
{
    using antlr;
    using System;
    using System.Collections;

    public class DebuggingInputBuffer : InputBuffer
    {
        private InputBuffer buffer;
        private bool debugMode = true;
        private InputBufferEventSupport inputBufferEventSupport;

        public DebuggingInputBuffer(InputBuffer buffer)
        {
            this.buffer = buffer;
            this.inputBufferEventSupport = new InputBufferEventSupport(this);
        }

        public virtual void addInputBufferListener(InputBufferListener l)
        {
            this.inputBufferEventSupport.addInputBufferListener(l);
        }

        public override char consume()
        {
            char c = ' ';
            try
            {
                c = this.buffer.LA(1);
            }
            catch (CharStreamException)
            {
            }
            this.buffer.consume();
            if (this.debugMode)
            {
                this.inputBufferEventSupport.fireConsume(c);
            }
            return c;
        }

        public override void fill(int a)
        {
            this.buffer.fill(a);
        }

        public virtual bool isDebugMode()
        {
            return this.debugMode;
        }

        public override bool isMarked()
        {
            return this.buffer.isMarked();
        }

        public override char LA(int i)
        {
            char c = this.buffer.LA(i);
            if (this.debugMode)
            {
                this.inputBufferEventSupport.fireLA(c, i);
            }
            return c;
        }

        public override int mark()
        {
            int pos = this.buffer.mark();
            this.inputBufferEventSupport.fireMark(pos);
            return pos;
        }

        public virtual void removeInputBufferListener(InputBufferListener l)
        {
            if (this.inputBufferEventSupport != null)
            {
                this.inputBufferEventSupport.removeInputBufferListener(l);
            }
        }

        public override void rewind(int mark)
        {
            this.buffer.rewind(mark);
            this.inputBufferEventSupport.fireRewind(mark);
        }

        public virtual bool DebugMode
        {
            set
            {
                this.debugMode = value;
            }
        }

        public virtual ArrayList InputBufferListeners
        {
            get
            {
                return this.inputBufferEventSupport.InputBufferListeners;
            }
        }
    }
}

