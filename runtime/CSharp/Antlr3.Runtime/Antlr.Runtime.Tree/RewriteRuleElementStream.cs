namespace Antlr.Runtime.Tree
{
    using System;
    using System.Collections;

    public abstract class RewriteRuleElementStream
    {
        protected ITreeAdaptor adaptor;
        protected int cursor;
        protected string elementDescription;
        protected IList elements;
        protected object singleElement;

        public RewriteRuleElementStream(ITreeAdaptor adaptor, string elementDescription)
        {
            this.cursor = 0;
            this.elementDescription = elementDescription;
            this.adaptor = adaptor;
        }

        public RewriteRuleElementStream(ITreeAdaptor adaptor, string elementDescription, IList elements) : this(adaptor, elementDescription)
        {
            this.singleElement = null;
            this.elements = elements;
        }

        public RewriteRuleElementStream(ITreeAdaptor adaptor, string elementDescription, object oneElement) : this(adaptor, elementDescription)
        {
            this.Add(oneElement);
        }

        protected object _Next()
        {
            if (this.Size() == 0)
            {
                throw new RewriteEmptyStreamException(this.elementDescription);
            }
            if (this.cursor >= this.Size())
            {
                if (this.Size() != 1)
                {
                    throw new RewriteCardinalityException(this.elementDescription);
                }
                return this.singleElement;
            }
            if (this.singleElement != null)
            {
                this.cursor++;
                return this.ToTree(this.singleElement);
            }
            object obj2 = this.ToTree(this.elements[this.cursor]);
            this.cursor++;
            return obj2;
        }

        public void Add(object el)
        {
            if (el != null)
            {
                if (this.elements != null)
                {
                    this.elements.Add(el);
                }
                else if (this.singleElement == null)
                {
                    this.singleElement = el;
                }
                else
                {
                    this.elements = new ArrayList(5);
                    this.elements.Add(this.singleElement);
                    this.singleElement = null;
                    this.elements.Add(el);
                }
            }
        }

        protected abstract object Dup(object el);
        public bool HasNext()
        {
            return (((this.singleElement != null) && (this.cursor < 1)) || ((this.elements != null) && (this.cursor < this.elements.Count)));
        }

        public virtual object Next()
        {
            if ((this.cursor >= this.Size()) && (this.Size() == 1))
            {
                object el = this._Next();
                return this.Dup(el);
            }
            return this._Next();
        }

        public virtual void Reset()
        {
            this.cursor = 0;
        }

        public int Size()
        {
            int num = 0;
            if (this.singleElement != null)
            {
                num = 1;
            }
            if (this.elements != null)
            {
                return this.elements.Count;
            }
            return num;
        }

        protected virtual object ToTree(object el)
        {
            return el;
        }

        public string Description
        {
            get
            {
                return this.elementDescription;
            }
        }
    }
}

