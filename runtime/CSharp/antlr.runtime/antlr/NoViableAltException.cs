namespace antlr
{
    using antlr.collections;
    using System;

    [Serializable]
    public class NoViableAltException : RecognitionException
    {
        public AST node;
        public IToken token;

        public NoViableAltException(AST t) : base("NoViableAlt", "<AST>", -1, -1)
        {
            this.node = t;
        }

        public NoViableAltException(IToken t, string fileName_) : base("NoViableAlt", fileName_, t.getLine(), t.getColumn())
        {
            this.token = t;
        }

        public override string Message
        {
            get
            {
                if (this.token != null)
                {
                    return ("unexpected token: " + this.token.ToString());
                }
                if ((this.node == null) || (this.node == TreeParser.ASTNULL))
                {
                    return "unexpected end of subtree";
                }
                return ("unexpected AST node: " + this.node.ToString());
            }
        }
    }
}

