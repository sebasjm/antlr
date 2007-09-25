namespace antlr
{
    using antlr.collections.impl;
    using System;

    public class TokenStreamHiddenTokenFilter : TokenStreamBasicFilter, TokenStream
    {
        protected internal IHiddenStreamToken firstHidden;
        protected internal BitSet hideMask;
        protected internal IHiddenStreamToken lastHiddenToken;
        private IHiddenStreamToken nextMonitoredToken;

        public TokenStreamHiddenTokenFilter(TokenStream input) : base(input)
        {
            this.firstHidden = null;
            this.hideMask = new BitSet();
        }

        protected internal virtual void consume()
        {
            this.nextMonitoredToken = (IHiddenStreamToken) base.input.nextToken();
        }

        private void consumeFirst()
        {
            this.consume();
            IHiddenStreamToken t = null;
            while (this.hideMask.member(this.LA(1).Type) || base.discardMask.member(this.LA(1).Type))
            {
                if (this.hideMask.member(this.LA(1).Type))
                {
                    if (t == null)
                    {
                        t = this.LA(1);
                    }
                    else
                    {
                        t.setHiddenAfter(this.LA(1));
                        this.LA(1).setHiddenBefore(t);
                        t = this.LA(1);
                    }
                    this.lastHiddenToken = t;
                    if (this.firstHidden == null)
                    {
                        this.firstHidden = t;
                    }
                }
                this.consume();
            }
        }

        public virtual BitSet getDiscardMask()
        {
            return base.discardMask;
        }

        public virtual IHiddenStreamToken getHiddenAfter(IHiddenStreamToken t)
        {
            return t.getHiddenAfter();
        }

        public virtual IHiddenStreamToken getHiddenBefore(IHiddenStreamToken t)
        {
            return t.getHiddenBefore();
        }

        public virtual BitSet getHideMask()
        {
            return this.hideMask;
        }

        public virtual IHiddenStreamToken getInitialHiddenToken()
        {
            return this.firstHidden;
        }

        public virtual void hide(BitSet mask)
        {
            this.hideMask = mask;
        }

        public virtual void hide(int m)
        {
            this.hideMask.add(m);
        }

        protected internal virtual IHiddenStreamToken LA(int i)
        {
            return this.nextMonitoredToken;
        }

        public override IToken nextToken()
        {
            if (this.LA(1) == null)
            {
                this.consumeFirst();
            }
            IHiddenStreamToken token = this.LA(1);
            token.setHiddenBefore(this.lastHiddenToken);
            this.lastHiddenToken = null;
            this.consume();
            IHiddenStreamToken t = token;
            while (this.hideMask.member(this.LA(1).Type) || base.discardMask.member(this.LA(1).Type))
            {
                if (this.hideMask.member(this.LA(1).Type))
                {
                    t.setHiddenAfter(this.LA(1));
                    if (t != token)
                    {
                        this.LA(1).setHiddenBefore(t);
                    }
                    t = this.lastHiddenToken = this.LA(1);
                }
                this.consume();
            }
            return token;
        }

        public virtual void resetState()
        {
            this.firstHidden = null;
            this.lastHiddenToken = null;
            this.nextMonitoredToken = null;
        }
    }
}

