namespace antlr.collections.impl
{
    using antlr.collections;
    using System;

    public class ASTArray
    {
        public AST[] array;
        public int size = 0;

        public ASTArray(int capacity)
        {
            this.array = new AST[capacity];
        }

        public virtual ASTArray add(AST node)
        {
            this.array[this.size++] = node;
            return this;
        }
    }
}

