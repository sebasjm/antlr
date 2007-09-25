namespace Antlr.Runtime.Tree
{
    using Antlr.Runtime;
    using System;

    public class CommonTreeAdaptor : BaseTreeAdaptor
    {
        public override object Create(IToken payload)
        {
            return new CommonTree(payload);
        }

        public override IToken CreateToken(IToken fromToken)
        {
            return new CommonToken(fromToken);
        }

        public override IToken CreateToken(int tokenType, string text)
        {
            return new CommonToken(tokenType, text);
        }

        public override object DupNode(object treeNode)
        {
            return ((ITree) treeNode).DupNode();
        }

        public override object GetChild(object t, int i)
        {
            return ((ITree) t).GetChild(i);
        }

        public override int GetChildCount(object t)
        {
            return ((ITree) t).ChildCount;
        }

        public override string GetNodeText(object t)
        {
            return ((ITree) t).Text;
        }

        public override int GetNodeType(object t)
        {
            if (t == null)
            {
                return 0;
            }
            return ((ITree) t).Type;
        }

        public override IToken GetToken(object treeNode)
        {
            if (treeNode is CommonTree)
            {
                return ((CommonTree) treeNode).Token;
            }
            return null;
        }

        public override int GetTokenStartIndex(object t)
        {
            return ((ITree) t).TokenStartIndex;
        }

        public override int GetTokenStopIndex(object t)
        {
            return ((ITree) t).TokenStopIndex;
        }

        public override void SetTokenBoundaries(object t, IToken startToken, IToken stopToken)
        {
            if (t != null)
            {
                int tokenIndex = 0;
                int num2 = 0;
                if (startToken != null)
                {
                    tokenIndex = startToken.TokenIndex;
                }
                if (stopToken != null)
                {
                    num2 = stopToken.TokenIndex;
                }
                ((ITree) t).TokenStartIndex = tokenIndex;
                ((ITree) t).TokenStopIndex = num2;
            }
        }
    }
}

