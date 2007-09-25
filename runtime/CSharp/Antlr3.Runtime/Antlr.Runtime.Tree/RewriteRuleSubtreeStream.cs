namespace Antlr.Runtime.Tree
{
    using System;
    using System.Collections;

    public class RewriteRuleSubtreeStream : RewriteRuleElementStream
    {
        public RewriteRuleSubtreeStream(ITreeAdaptor adaptor, string elementDescription) : base(adaptor, elementDescription)
        {
        }

        public RewriteRuleSubtreeStream(ITreeAdaptor adaptor, string elementDescription, IList elements) : base(adaptor, elementDescription, elements)
        {
        }

        public RewriteRuleSubtreeStream(ITreeAdaptor adaptor, string elementDescription, object oneElement) : base(adaptor, elementDescription, oneElement)
        {
        }

        protected override object Dup(object el)
        {
            return base.adaptor.DupTree(el);
        }

        public object NextNode()
        {
            object treeNode = base._Next();
            if ((base.cursor >= base.Size()) && (base.Size() == 1))
            {
                return base.adaptor.DupNode(treeNode);
            }
            return treeNode;
        }
    }
}

