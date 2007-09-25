namespace Antlr.Runtime.Tree
{
    using Antlr.Runtime;
    using System;

    public class ParseTree : BaseTree
    {
        public object payload;

        public ParseTree(object label)
        {
            this.payload = label;
        }

        public override ITree DupNode()
        {
            return null;
        }

        public override string ToString()
        {
            if (this.payload is IToken)
            {
                IToken payload = (IToken) this.payload;
                if (payload.Type == Token.EOF)
                {
                    return "<EOF>";
                }
                return payload.Text;
            }
            return this.payload.ToString();
        }

        public override string Text
        {
            get
            {
                return this.ToString();
            }
        }

        public override int TokenStartIndex
        {
            get
            {
                return 0;
            }
            set
            {
            }
        }

        public override int TokenStopIndex
        {
            get
            {
                return 0;
            }
            set
            {
            }
        }

        public override int Type
        {
            get
            {
                return 0;
            }
        }
    }
}

