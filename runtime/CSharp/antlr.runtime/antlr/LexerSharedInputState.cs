namespace antlr
{
    using System;
    using System.IO;

    public class LexerSharedInputState
    {
        protected internal int column;
        protected internal string filename;
        public int guessing;
        protected internal InputBuffer input;
        protected internal int line;
        protected internal int tokenStartColumn;
        protected internal int tokenStartLine;

        public LexerSharedInputState(InputBuffer inbuf)
        {
            this.initialize();
            this.input = inbuf;
        }

        public LexerSharedInputState(Stream inStream) : this(new ByteBuffer(inStream))
        {
        }

        public LexerSharedInputState(TextReader inReader) : this(new CharBuffer(inReader))
        {
        }

        private void initialize()
        {
            this.column = 1;
            this.line = 1;
            this.tokenStartColumn = 1;
            this.tokenStartLine = 1;
            this.guessing = 0;
            this.filename = null;
        }

        public virtual void reset()
        {
            this.initialize();
            this.input.reset();
        }

        public virtual void resetInput(InputBuffer ib)
        {
            this.reset();
            this.input = ib;
        }

        public virtual void resetInput(Stream s)
        {
            this.reset();
            this.input = new ByteBuffer(s);
        }

        public virtual void resetInput(TextReader tr)
        {
            this.reset();
            this.input = new CharBuffer(tr);
        }
    }
}

