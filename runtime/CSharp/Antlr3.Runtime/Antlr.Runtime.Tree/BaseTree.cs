namespace Antlr.Runtime.Tree
{
    using System;
    using System.Collections;
    using System.Text;

    public abstract class BaseTree : ITree
    {
        protected internal IList children;

        public BaseTree()
        {
        }

        public BaseTree(ITree node)
        {
        }

        public virtual void AddChild(ITree t)
        {
            if (t != null)
            {
                BaseTree tree = (BaseTree) t;
                if (tree.IsNil)
                {
                    if ((this.children != null) && (this.children == tree.children))
                    {
                        throw new InvalidOperationException("attempt to add child list to itself");
                    }
                    if (tree.children != null)
                    {
                        if (this.children != null)
                        {
                            int count = tree.children.Count;
                            for (int i = 0; i < count; i++)
                            {
                                this.children.Add(tree.children[i]);
                            }
                        }
                        else
                        {
                            this.children = tree.children;
                        }
                    }
                }
                else
                {
                    if (this.children == null)
                    {
                        this.children = this.CreateChildrenList();
                    }
                    this.children.Add(t);
                }
            }
        }

        public void AddChildren(IList kids)
        {
            for (int i = 0; i < kids.Count; i++)
            {
                ITree t = (ITree) kids[i];
                this.AddChild(t);
            }
        }

        protected internal virtual IList CreateChildrenList()
        {
            return new ArrayList();
        }

        public virtual BaseTree DeleteChild(int i)
        {
            if (this.children == null)
            {
                return null;
            }
            object obj2 = this.children[i];
            this.children.RemoveAt(i);
            return (BaseTree) obj2;
        }

        public abstract ITree DupNode();
        public virtual ITree DupTree()
        {
            ITree tree = this.DupNode();
            for (int i = 0; (this.children != null) && (i < this.children.Count); i++)
            {
                ITree t = ((ITree) this.children[i]).DupTree();
                tree.AddChild(t);
            }
            return tree;
        }

        public virtual ITree GetChild(int i)
        {
            if ((this.children == null) || (i >= this.children.Count))
            {
                return null;
            }
            return (BaseTree) this.children[i];
        }

        public virtual void SetChild(int i, BaseTree t)
        {
            if (this.children == null)
            {
                this.children = this.CreateChildrenList();
            }
            this.children[i] = t;
        }

        public abstract override string ToString();
        public virtual string ToStringTree()
        {
            if ((this.children == null) || (this.children.Count == 0))
            {
                return this.ToString();
            }
            StringBuilder builder = new StringBuilder();
            if (!this.IsNil)
            {
                builder.Append("(");
                builder.Append(this.ToString());
                builder.Append(' ');
            }
            for (int i = 0; (this.children != null) && (i < this.children.Count); i++)
            {
                BaseTree tree = (BaseTree) this.children[i];
                if (i > 0)
                {
                    builder.Append(' ');
                }
                builder.Append(tree.ToStringTree());
            }
            if (!this.IsNil)
            {
                builder.Append(")");
            }
            return builder.ToString();
        }

        public virtual int CharPositionInLine
        {
            get
            {
                return 0;
            }
        }

        public virtual int ChildCount
        {
            get
            {
                if (this.children == null)
                {
                    return 0;
                }
                return this.children.Count;
            }
        }

        public virtual bool IsNil
        {
            get
            {
                return false;
            }
        }

        public virtual int Line
        {
            get
            {
                return 0;
            }
        }

        public abstract string Text { get; }

        public abstract int TokenStartIndex { get; set; }

        public abstract int TokenStopIndex { get; set; }

        public abstract int Type { get; }
    }
}

