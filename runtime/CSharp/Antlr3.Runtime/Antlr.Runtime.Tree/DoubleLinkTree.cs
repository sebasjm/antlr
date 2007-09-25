namespace Antlr.Runtime.Tree
{
    using System;

    public abstract class DoubleLinkTree : BaseTree
    {
        protected internal DoubleLinkTree parent;

        protected DoubleLinkTree()
        {
        }

        public virtual void AddChild(BaseTree t)
        {
            base.AddChild(t);
            ((DoubleLinkTree) t).Parent = this;
        }

        public override void SetChild(int i, BaseTree t)
        {
            base.SetChild(i, t);
            ((DoubleLinkTree) t).Parent = this;
        }

        public virtual DoubleLinkTree Parent
        {
            get
            {
                return this.parent;
            }
            set
            {
                this.parent = value;
            }
        }
    }
}

