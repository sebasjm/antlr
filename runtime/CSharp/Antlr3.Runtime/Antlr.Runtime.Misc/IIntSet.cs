namespace Antlr.Runtime.Misc
{
    using System;
    using System.Collections;

    public interface IIntSet
    {
        void Add(int el);
        void AddAll(IIntSet set);
        IIntSet And(IIntSet a);
        IIntSet Complement(IIntSet elements);
        bool Equals(object obj);
        int GetSingleElement();
        bool IsNil();
        bool Member(int el);
        IIntSet Or(IIntSet a);
        void Remove(int el);
        int Size();
        IIntSet Subtract(IIntSet a);
        IList ToList();
        string ToString();
    }
}

