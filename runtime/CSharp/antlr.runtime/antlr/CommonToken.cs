namespace antlr
{
    using System;

    public class CommonToken : Token
    {
        protected internal int col;
        public static readonly CommonTokenCreator Creator = new CommonTokenCreator();
        protected internal int line;
        protected internal string text;

        public CommonToken()
        {
            this.text = null;
        }

        public CommonToken(string s)
        {
            this.text = null;
            this.text = s;
        }

        public CommonToken(int t, string txt)
        {
            this.text = null;
            base.type_ = t;
            this.setText(txt);
        }

        public override int getColumn()
        {
            return this.col;
        }

        public override int getLine()
        {
            return this.line;
        }

        public override string getText()
        {
            return this.text;
        }

        public override void setColumn(int c)
        {
            this.col = c;
        }

        public override void setLine(int l)
        {
            this.line = l;
        }

        public override void setText(string s)
        {
            this.text = s;
        }

        public override string ToString()
        {
            return string.Concat(new object[] { "[\"", this.getText(), "\",<", base.type_, ">,line=", this.line, ",col=", this.col, "]" });
        }

        public class CommonTokenCreator : TokenCreator
        {
            public override IToken Create()
            {
                return new CommonToken();
            }

            public override string TokenTypeName
            {
                get
                {
                    return typeof(CommonToken).FullName;
                }
            }
        }
    }
}

