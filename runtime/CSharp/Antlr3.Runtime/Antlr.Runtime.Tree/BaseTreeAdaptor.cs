namespace Antlr.Runtime.Tree
{
    using Antlr.Runtime;
    using System;
    using System.Runtime.CompilerServices;

    public abstract class BaseTreeAdaptor : ITreeAdaptor
    {
        protected BaseTreeAdaptor()
        {
        }

        public virtual void AddChild(object t, object child)
        {
            if (t != null)
            {
                ((ITree) t).AddChild((ITree) child);
            }
        }

        public virtual object BecomeRoot(IToken newRoot, object oldRoot)
        {
            return this.BecomeRoot(this.Create(newRoot), oldRoot);
        }

        public virtual object BecomeRoot(object newRoot, object oldRoot)
        {
            ITree child = (ITree) newRoot;
            ITree t = (ITree) oldRoot;
            if (oldRoot == null)
            {
                return newRoot;
            }
            if (child.IsNil)
            {
                if (child.ChildCount > 1)
                {
                    throw new SystemException("more than one node as root (TODO: make exception hierarchy)");
                }
                child = child.GetChild(0);
            }
            child.AddChild(t);
            return child;
        }

        public abstract object Create(IToken param1);
        public virtual object Create(int tokenType, IToken fromToken)
        {
            fromToken = this.CreateToken(fromToken);
            fromToken.Type = tokenType;
            return (ITree) this.Create(fromToken);
        }

        public virtual object Create(int tokenType, string text)
        {
            IToken token = this.CreateToken(tokenType, text);
            return (ITree) this.Create(token);
        }

        public virtual object Create(int tokenType, IToken fromToken, string text)
        {
            fromToken = this.CreateToken(fromToken);
            fromToken.Type = tokenType;
            fromToken.Text = text;
            return (ITree) this.Create(fromToken);
        }

        public abstract IToken CreateToken(IToken fromToken);
        public abstract IToken CreateToken(int tokenType, string text);
        public abstract object DupNode(object param1);
        public virtual object DupTree(object tree)
        {
            return ((ITree) tree).DupTree();
        }

        public virtual object GetChild(object t, int i)
        {
            return ((ITree) t).GetChild(i);
        }

        public virtual int GetChildCount(object t)
        {
            return ((ITree) t).ChildCount;
        }

        public virtual object GetNilNode()
        {
            return this.Create(null);
        }

        public virtual string GetNodeText(object t)
        {
            return ((ITree) t).Text;
        }

        public virtual int GetNodeType(object t)
        {
            int type = ((ITree) t).Type;
            return 0;
        }

        public abstract IToken GetToken(object treeNode);
        public abstract int GetTokenStartIndex(object t);
        public abstract int GetTokenStopIndex(object t);
        public int GetUniqueID(object node)
        {
            return RuntimeHelpers.GetHashCode(node);
        }

        public virtual bool IsNil(object tree)
        {
            return ((ITree) tree).IsNil;
        }

        public virtual object RulePostProcessing(object root)
        {
            ITree child = (ITree) root;
            if (((child != null) && child.IsNil) && (child.ChildCount == 1))
            {
                child = child.GetChild(0);
            }
            return child;
        }

        public virtual void SetNodeText(object t, string text)
        {
            throw new NotImplementedException("don't know enough about Tree node");
        }

        public virtual void SetNodeType(object t, int type)
        {
            throw new NotImplementedException("don't know enough about Tree node");
        }

        public abstract void SetTokenBoundaries(object param1, IToken param2, IToken param3);
    }
}

