namespace Antlr.Runtime.Collections
{
    using System;
    using System.Collections;

    public class StackList : ArrayList
    {
        public object Peek()
        {
            return this[this.Count - 1];
        }

        public object Pop()
        {
            object obj2 = this[this.Count - 1];
            this.RemoveAt(this.Count - 1);
            return obj2;
        }

        public void Push(object item)
        {
            this.Add(item);
        }
    }
}

