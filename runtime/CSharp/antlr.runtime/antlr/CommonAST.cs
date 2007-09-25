namespace antlr
{
    using antlr.collections;
    using System;

    public class CommonAST : BaseAST
    {
        public static readonly CommonASTCreator Creator = new CommonASTCreator();
        internal string text;
        internal int ttype;

        public CommonAST()
        {
            this.ttype = 0;
        }

        [Obsolete("Deprecated since version 2.7.2. Use ASTFactory.dup() instead.", false)]
        protected CommonAST(CommonAST another)
        {
            this.ttype = 0;
            this.ttype = another.ttype;
            this.text = (another.text == null) ? null : string.Copy(another.text);
        }

        public CommonAST(IToken tok)
        {
            this.ttype = 0;
            this.initialize(tok);
        }

        [Obsolete("Deprecated since version 2.7.2. Use ASTFactory.dup() instead.", false)]
        public override object Clone()
        {
            return new CommonAST(this);
        }

        public override string getText()
        {
            return this.text;
        }

        public override void initialize(AST t)
        {
            this.setText(t.getText());
            this.Type = t.Type;
        }

        public override void initialize(IToken tok)
        {
            this.setText(tok.getText());
            this.Type = tok.Type;
        }

        public override void initialize(int t, string txt)
        {
            this.Type = t;
            this.setText(txt);
        }

        public override void setText(string text_)
        {
            this.text = text_;
        }

        public override void setType(int ttype_)
        {
            this.Type = ttype_;
        }

        public override int Type
        {
            get
            {
                return this.ttype;
            }
            set
            {
                this.ttype = value;
            }
        }

        public class CommonASTCreator : ASTNodeCreator
        {
            public override AST Create()
            {
                return new CommonAST();
            }

            public override string ASTNodeTypeName
            {
                get
                {
                    return typeof(CommonAST).FullName;
                }
            }
        }
    }
}

