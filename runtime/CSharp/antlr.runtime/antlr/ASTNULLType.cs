namespace antlr
{
    using antlr.collections;
    using System;
    using System.Collections;

    public class ASTNULLType : AST, ICloneable
    {
        public virtual void addChild(AST c)
        {
        }

        public object Clone()
        {
            return base.MemberwiseClone();
        }

        public virtual bool Equals(AST t)
        {
            return false;
        }

        public virtual bool EqualsList(AST t)
        {
            return false;
        }

        public virtual bool EqualsListPartial(AST t)
        {
            return false;
        }

        public virtual bool EqualsTree(AST t)
        {
            return false;
        }

        public virtual bool EqualsTreePartial(AST t)
        {
            return false;
        }

        public virtual IEnumerator findAll(AST tree)
        {
            return null;
        }

        public virtual IEnumerator findAllPartial(AST subtree)
        {
            return null;
        }

        public virtual AST getFirstChild()
        {
            return this;
        }

        public virtual AST getNextSibling()
        {
            return this;
        }

        public int getNumberOfChildren()
        {
            return 0;
        }

        public virtual string getText()
        {
            return "<ASTNULL>";
        }

        public virtual void initialize(AST t)
        {
        }

        public virtual void initialize(IToken t)
        {
        }

        public virtual void initialize(int t, string txt)
        {
        }

        public virtual void setFirstChild(AST c)
        {
        }

        public virtual void setNextSibling(AST n)
        {
        }

        public virtual void setText(string text)
        {
        }

        public virtual void setType(int ttype)
        {
            this.Type = ttype;
        }

        public override string ToString()
        {
            return this.getText();
        }

        public virtual string ToStringList()
        {
            return this.getText();
        }

        public virtual string ToStringTree()
        {
            return this.getText();
        }

        public virtual int Type
        {
            get
            {
                return 3;
            }
            set
            {
            }
        }
    }
}

