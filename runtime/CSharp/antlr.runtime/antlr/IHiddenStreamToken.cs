namespace antlr
{
    using System;

    public interface IHiddenStreamToken : IToken
    {
        IHiddenStreamToken getHiddenAfter();
        IHiddenStreamToken getHiddenBefore();
        void setHiddenAfter(IHiddenStreamToken t);
        void setHiddenBefore(IHiddenStreamToken t);
    }
}

