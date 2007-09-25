namespace antlr
{
    using System;

    public class CommonHiddenStreamToken : CommonToken, IHiddenStreamToken, IToken
    {
        public static readonly CommonHiddenStreamTokenCreator Creator = new CommonHiddenStreamTokenCreator();
        protected internal IHiddenStreamToken hiddenAfter;
        protected internal IHiddenStreamToken hiddenBefore;

        public CommonHiddenStreamToken()
        {
        }

        public CommonHiddenStreamToken(string s) : base(s)
        {
        }

        public CommonHiddenStreamToken(int t, string txt) : base(t, txt)
        {
        }

        public virtual IHiddenStreamToken getHiddenAfter()
        {
            return this.hiddenAfter;
        }

        public virtual IHiddenStreamToken getHiddenBefore()
        {
            return this.hiddenBefore;
        }

        public virtual void setHiddenAfter(IHiddenStreamToken t)
        {
            this.hiddenAfter = t;
        }

        public virtual void setHiddenBefore(IHiddenStreamToken t)
        {
            this.hiddenBefore = t;
        }

        public class CommonHiddenStreamTokenCreator : TokenCreator
        {
            public override IToken Create()
            {
                return new CommonHiddenStreamToken();
            }

            public override string TokenTypeName
            {
                get
                {
                    return typeof(CommonHiddenStreamToken).FullName;
                }
            }
        }
    }
}

