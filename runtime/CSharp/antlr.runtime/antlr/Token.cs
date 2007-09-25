namespace antlr
{
    using System;

    public class Token : IToken
    {
        public static Token badToken = new Token(0, "<no text>");
        public const int EOF_TYPE = 1;
        public const int INVALID_TYPE = 0;
        public const int MIN_USER_TYPE = 4;
        public const int NULL_TREE_LOOKAHEAD = 3;
        public static readonly int SKIP = -1;
        protected int type_;

        public Token()
        {
            this.type_ = 0;
        }

        public Token(int t)
        {
            this.type_ = t;
        }

        public Token(int t, string txt)
        {
            this.type_ = t;
            this.setText(txt);
        }

        public virtual int getColumn()
        {
            return 0;
        }

        public virtual string getFilename()
        {
            return null;
        }

        public virtual int getLine()
        {
            return 0;
        }

        public virtual string getText()
        {
            return "<no text>";
        }

        public virtual void setColumn(int c)
        {
        }

        public virtual void setFilename(string name)
        {
        }

        public virtual void setLine(int l)
        {
        }

        public virtual void setText(string t)
        {
        }

        public virtual void setType(int newType)
        {
            this.Type = newType;
        }

        public override string ToString()
        {
            return string.Concat(new object[] { "[\"", this.getText(), "\",<", this.type_, ">]" });
        }

        public int Type
        {
            get
            {
                return this.type_;
            }
            set
            {
                this.type_ = value;
            }
        }
    }
}

