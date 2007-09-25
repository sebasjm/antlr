namespace antlr.collections
{
    using antlr;
    using System;
    using System.Collections;

    public interface AST : ICloneable
    {
        void addChild(AST c);
        bool Equals(AST t);
        bool EqualsList(AST t);
        bool EqualsListPartial(AST t);
        bool EqualsTree(AST t);
        bool EqualsTreePartial(AST t);
        IEnumerator findAll(AST tree);
        IEnumerator findAllPartial(AST subtree);
        AST getFirstChild();
        AST getNextSibling();
        int getNumberOfChildren();
        string getText();
        void initialize(AST t);
        void initialize(IToken t);
        void initialize(int t, string txt);
        void setFirstChild(AST c);
        void setNextSibling(AST n);
        void setText(string text);
        void setType(int ttype);
        string ToString();
        string ToStringList();
        string ToStringTree();

        int Type { get; set; }
    }
}

