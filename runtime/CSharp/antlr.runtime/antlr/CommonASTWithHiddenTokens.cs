namespace antlr
{
    using antlr.collections;
    using System;

    public class CommonASTWithHiddenTokens : CommonAST
    {
        public static readonly CommonASTWithHiddenTokensCreator Creator = new CommonASTWithHiddenTokensCreator();
        protected internal IHiddenStreamToken hiddenAfter;
        protected internal IHiddenStreamToken hiddenBefore;

        public CommonASTWithHiddenTokens()
        {
        }

        [Obsolete("Deprecated since version 2.7.2. Use ASTFactory.dup() instead.", false)]
        protected CommonASTWithHiddenTokens(CommonASTWithHiddenTokens another) : base(another)
        {
            this.hiddenBefore = another.hiddenBefore;
            this.hiddenAfter = another.hiddenAfter;
        }

        public CommonASTWithHiddenTokens(IToken tok) : base(tok)
        {
        }

        [Obsolete("Deprecated since version 2.7.2. Use ASTFactory.dup() instead.", false)]
        public override object Clone()
        {
            return new CommonASTWithHiddenTokens(this);
        }

        public virtual IHiddenStreamToken getHiddenAfter()
        {
            return this.hiddenAfter;
        }

        public virtual IHiddenStreamToken getHiddenBefore()
        {
            return this.hiddenBefore;
        }

        public override void initialize(AST t)
        {
            this.hiddenBefore = ((CommonASTWithHiddenTokens) t).getHiddenBefore();
            this.hiddenAfter = ((CommonASTWithHiddenTokens) t).getHiddenAfter();
            base.initialize(t);
        }

        public override void initialize(IToken tok)
        {
            IHiddenStreamToken token = (IHiddenStreamToken) tok;
            base.initialize(token);
            this.hiddenBefore = token.getHiddenBefore();
            this.hiddenAfter = token.getHiddenAfter();
        }

        public class CommonASTWithHiddenTokensCreator : ASTNodeCreator
        {
            public override AST Create()
            {
                return new CommonASTWithHiddenTokens();
            }

            public override string ASTNodeTypeName
            {
                get
                {
                    return typeof(CommonASTWithHiddenTokens).FullName;
                }
            }
        }
    }
}

