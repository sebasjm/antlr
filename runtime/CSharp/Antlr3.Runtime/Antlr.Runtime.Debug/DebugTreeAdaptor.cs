namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using Antlr.Runtime.Tree;
    using System;

    public class DebugTreeAdaptor : ITreeAdaptor
    {
        protected ITreeAdaptor adaptor;
        protected IDebugEventListener dbg;

        public DebugTreeAdaptor(IDebugEventListener dbg, ITreeAdaptor adaptor)
        {
            this.dbg = dbg;
            this.adaptor = adaptor;
        }

        public void AddChild(object t, IToken child)
        {
            object obj2 = this.Create(child);
            this.AddChild(t, obj2);
        }

        public void AddChild(object t, object child)
        {
            this.adaptor.AddChild(t, child);
            this.dbg.AddChild(this.adaptor.GetUniqueID(t), this.adaptor.GetUniqueID(child));
        }

        public object BecomeRoot(IToken newRoot, object oldRoot)
        {
            object obj2 = this.Create(newRoot);
            this.adaptor.BecomeRoot(obj2, oldRoot);
            this.dbg.BecomeRoot(this.adaptor.GetUniqueID(obj2), this.adaptor.GetUniqueID(oldRoot));
            return obj2;
        }

        public object BecomeRoot(object newRoot, object oldRoot)
        {
            object node = this.adaptor.BecomeRoot(newRoot, oldRoot);
            this.dbg.BecomeRoot(this.adaptor.GetUniqueID(node), this.adaptor.GetUniqueID(oldRoot));
            return node;
        }

        public object Create(IToken payload)
        {
            object node = this.adaptor.Create(payload);
            this.dbg.CreateNode(this.adaptor.GetUniqueID(node), payload.TokenIndex);
            return node;
        }

        public object Create(int tokenType, IToken fromToken)
        {
            object node = this.adaptor.Create(tokenType, fromToken);
            this.dbg.CreateNode(this.adaptor.GetUniqueID(node), fromToken.Text, tokenType);
            return node;
        }

        public object Create(int tokenType, string text)
        {
            object node = this.adaptor.Create(tokenType, text);
            this.dbg.CreateNode(this.adaptor.GetUniqueID(node), text, tokenType);
            return node;
        }

        public object Create(int tokenType, IToken fromToken, string text)
        {
            object node = this.adaptor.Create(tokenType, fromToken, text);
            this.dbg.CreateNode(this.adaptor.GetUniqueID(node), text, tokenType);
            return node;
        }

        public object DupNode(object treeNode)
        {
            return this.adaptor.DupNode(treeNode);
        }

        public object DupTree(object tree)
        {
            return this.adaptor.DupTree(tree);
        }

        public object GetChild(object t, int i)
        {
            return this.adaptor.GetChild(t, i);
        }

        public int GetChildCount(object t)
        {
            return this.adaptor.GetChildCount(t);
        }

        public object GetNilNode()
        {
            object nilNode = this.adaptor.GetNilNode();
            this.dbg.GetNilNode(this.adaptor.GetUniqueID(nilNode));
            return nilNode;
        }

        public string GetNodeText(object t)
        {
            return this.adaptor.GetNodeText(t);
        }

        public int GetNodeType(object t)
        {
            return this.adaptor.GetNodeType(t);
        }

        public IToken GetToken(object treeNode)
        {
            return this.adaptor.GetToken(treeNode);
        }

        public int GetTokenStartIndex(object t)
        {
            return this.adaptor.GetTokenStartIndex(t);
        }

        public int GetTokenStopIndex(object t)
        {
            return this.adaptor.GetTokenStopIndex(t);
        }

        public int GetUniqueID(object node)
        {
            return this.adaptor.GetUniqueID(node);
        }

        public bool IsNil(object tree)
        {
            return this.adaptor.IsNil(tree);
        }

        public object RulePostProcessing(object root)
        {
            return this.adaptor.RulePostProcessing(root);
        }

        public void SetNodeText(object t, string text)
        {
            this.adaptor.SetNodeText(t, text);
        }

        public void SetNodeType(object t, int type)
        {
            this.adaptor.SetNodeType(t, type);
        }

        public void SetTokenBoundaries(object t, IToken startToken, IToken stopToken)
        {
            this.adaptor.SetTokenBoundaries(t, startToken, stopToken);
            if (((t != null) && (startToken != null)) && (stopToken != null))
            {
                this.dbg.SetTokenBoundaries(this.adaptor.GetUniqueID(t), startToken.TokenIndex, stopToken.TokenIndex);
            }
        }

        public IDebugEventListener DebugEventListener
        {
            get
            {
                return this.dbg;
            }
        }

        public ITreeAdaptor TreeAdaptor
        {
            get
            {
                return this.adaptor;
            }
        }
    }
}

