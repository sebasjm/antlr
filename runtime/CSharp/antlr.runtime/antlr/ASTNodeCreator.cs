namespace antlr
{
    using antlr.collections;
    using System;

    public abstract class ASTNodeCreator
    {
        protected ASTNodeCreator()
        {
        }

        public abstract AST Create();

        public abstract string ASTNodeTypeName { get; }
    }
}

