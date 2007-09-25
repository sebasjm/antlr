namespace Antlr.Runtime.Tree
{
    using Antlr.Runtime;
    using System;

    public class CommonTree : BaseTree
    {
        public int startIndex;
        public int stopIndex;
        protected IToken token;

        public CommonTree()
        {
            this.startIndex = -1;
            this.stopIndex = -1;
        }

        public CommonTree(IToken t)
        {
            this.startIndex = -1;
            this.stopIndex = -1;
            this.token = t;
        }

        public CommonTree(CommonTree node) : base(node)
        {
            this.startIndex = -1;
            this.stopIndex = -1;
            this.token = node.token;
        }

        public override ITree DupNode()
        {
            return new CommonTree(this);
        }

        public override string ToString()
        {
            if (this.IsNil)
            {
                return "nil";
            }
            return this.token.Text;
        }

        public override int CharPositionInLine
        {
            get
            {
                if ((this.token == null) || (this.token.CharPositionInLine == -1))
                {
                    if (this.ChildCount > 0)
                    {
                        return this.GetChild(0).CharPositionInLine;
                    }
                    return 0;
                }
                return this.token.CharPositionInLine;
            }
        }

        public override bool IsNil
        {
            get
            {
                return (this.token == null);
            }
        }

        public override int Line
        {
            get
            {
                if ((this.token == null) || (this.token.Line == 0))
                {
                    if (this.ChildCount > 0)
                    {
                        return this.GetChild(0).Line;
                    }
                    return 0;
                }
                return this.token.Line;
            }
        }

        public override string Text
        {
            get
            {
                return this.ToString();
            }
        }

        public virtual IToken Token
        {
            get
            {
                return this.token;
            }
        }

        public override int TokenStartIndex
        {
            get
            {
                if ((this.startIndex == -1) && (this.token != null))
                {
                    return this.token.TokenIndex;
                }
                return this.startIndex;
            }
            set
            {
                this.startIndex = value;
            }
        }

        public override int TokenStopIndex
        {
            get
            {
                if ((this.stopIndex == -1) && (this.token != null))
                {
                    return this.token.TokenIndex;
                }
                return this.stopIndex;
            }
            set
            {
                this.stopIndex = value;
            }
        }

        public override int Type
        {
            get
            {
                if (this.token == null)
                {
                    return 0;
                }
                return this.token.Type;
            }
        }
    }
}

