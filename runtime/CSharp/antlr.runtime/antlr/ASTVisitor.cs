namespace antlr
{
    using antlr.collections;
    using System;

    public interface ASTVisitor
    {
        void visit(AST node);
    }
}

