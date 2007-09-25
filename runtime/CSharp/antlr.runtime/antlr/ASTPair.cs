namespace antlr
{
    using antlr.collections;
    using System;
    using System.Runtime.InteropServices;

    [StructLayout(LayoutKind.Sequential)]
    public struct ASTPair
    {
        public AST root;
        public AST child;
        public void advanceChildToEnd()
        {
            if (this.child != null)
            {
                while (this.child.getNextSibling() != null)
                {
                    this.child = this.child.getNextSibling();
                }
            }
        }

        public ASTPair copy()
        {
            ASTPair pair = new ASTPair();
            pair.root = this.root;
            pair.child = this.child;
            return pair;
        }

        private void reset()
        {
            this.root = null;
            this.child = null;
        }

        public override string ToString()
        {
            string str = (this.root == null) ? "null" : this.root.getText();
            string str2 = (this.child == null) ? "null" : this.child.getText();
            return ("[" + str + "," + str2 + "]");
        }
    }
}

