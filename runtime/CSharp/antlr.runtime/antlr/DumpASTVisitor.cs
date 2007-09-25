namespace antlr
{
    using antlr.collections;
    using System;

    public class DumpASTVisitor : ASTVisitor
    {
        protected int level = 0;

        private void tabs()
        {
            for (int i = 0; i < this.level; i++)
            {
                Console.Out.Write("   ");
            }
        }

        public void visit(AST node)
        {
            AST ast;
            bool flag = false;
            for (ast = node; ast != null; ast = ast.getNextSibling())
            {
                if (ast.getFirstChild() != null)
                {
                    flag = false;
                    break;
                }
            }
            for (ast = node; ast != null; ast = ast.getNextSibling())
            {
                if (!(flag && (ast != node)))
                {
                    this.tabs();
                }
                if (ast.getText() == null)
                {
                    Console.Out.Write("nil");
                }
                else
                {
                    Console.Out.Write(ast.getText());
                }
                Console.Out.Write(" [" + ast.Type + "] ");
                if (flag)
                {
                    Console.Out.Write(" ");
                }
                else
                {
                    Console.Out.WriteLine("");
                }
                if (ast.getFirstChild() != null)
                {
                    this.level++;
                    this.visit(ast.getFirstChild());
                    this.level--;
                }
            }
            if (flag)
            {
                Console.Out.WriteLine("");
            }
        }
    }
}

