namespace antlr
{
    using System;

    [Serializable]
    public class RecognitionException : ANTLRException
    {
        public int column;
        public string fileName;
        public int line;

        public RecognitionException() : base("parsing error")
        {
            this.fileName = null;
            this.line = -1;
            this.column = -1;
        }

        public RecognitionException(string s) : base(s)
        {
            this.fileName = null;
            this.line = -1;
            this.column = -1;
        }

        public RecognitionException(string s, string fileName_, int line_, int column_) : base(s)
        {
            this.fileName = fileName_;
            this.line = line_;
            this.column = column_;
        }

        public virtual int getColumn()
        {
            return this.column;
        }

        [Obsolete("Replaced by Message property since version 2.7.0", true)]
        public virtual string getErrorMessage()
        {
            return this.Message;
        }

        public virtual string getFilename()
        {
            return this.fileName;
        }

        public virtual int getLine()
        {
            return this.line;
        }

        public override string ToString()
        {
            return (FileLineFormatter.getFormatter().getFormatString(this.fileName, this.line, this.column) + this.Message);
        }
    }
}

