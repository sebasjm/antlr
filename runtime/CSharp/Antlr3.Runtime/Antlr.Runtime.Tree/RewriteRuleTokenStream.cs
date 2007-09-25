namespace Antlr.Runtime.Tree
{
    using Antlr.Runtime;
    using System;
    using System.Collections;

    public class RewriteRuleTokenStream : RewriteRuleElementStream
    {
        public RewriteRuleTokenStream(ITreeAdaptor adaptor, string elementDescription) : base(adaptor, elementDescription)
        {
        }

        public RewriteRuleTokenStream(ITreeAdaptor adaptor, string elementDescription, IList elements) : base(adaptor, elementDescription, elements)
        {
        }

        public RewriteRuleTokenStream(ITreeAdaptor adaptor, string elementDescription, object oneElement) : base(adaptor, elementDescription, oneElement)
        {
        }

        protected override object Dup(object el)
        {
            return base.adaptor.Create((IToken) el);
        }

        protected override object ToTree(object el)
        {
            return base.adaptor.Create((IToken) el);
        }
    }
}

