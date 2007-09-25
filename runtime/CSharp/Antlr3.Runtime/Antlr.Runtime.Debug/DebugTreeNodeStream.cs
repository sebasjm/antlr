namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using Antlr.Runtime.Tree;
    using System;

    public class DebugTreeNodeStream : ITreeNodeStream, IIntStream
    {
        protected ITreeAdaptor adaptor;
        protected IDebugEventListener dbg;
        protected bool initialStreamState = true;
        protected ITreeNodeStream input;
        protected int lastMarker;

        public DebugTreeNodeStream(ITreeNodeStream input, IDebugEventListener dbg)
        {
            this.input = input;
            this.adaptor = input.TreeAdaptor;
            this.input.HasUniqueNavigationNodes = true;
            this.SetDebugListener(dbg);
        }

        public void Consume()
        {
            object node = this.input.LT(1);
            this.input.Consume();
            int uniqueID = this.adaptor.GetUniqueID(node);
            string nodeText = this.adaptor.GetNodeText(node);
            int nodeType = this.adaptor.GetNodeType(node);
            this.dbg.ConsumeNode(uniqueID, nodeText, nodeType);
        }

        public object Get(int i)
        {
            return this.input.Get(i);
        }

        public int Index()
        {
            return this.input.Index();
        }

        public int LA(int i)
        {
            object node = this.input.LT(i);
            int uniqueID = this.adaptor.GetUniqueID(node);
            string nodeText = this.adaptor.GetNodeText(node);
            int nodeType = this.adaptor.GetNodeType(node);
            this.dbg.LT(i, uniqueID, nodeText, nodeType);
            return nodeType;
        }

        public object LT(int i)
        {
            object node = this.input.LT(i);
            int uniqueID = this.adaptor.GetUniqueID(node);
            string nodeText = this.adaptor.GetNodeText(node);
            int nodeType = this.adaptor.GetNodeType(node);
            this.dbg.LT(i, uniqueID, nodeText, nodeType);
            return node;
        }

        public int Mark()
        {
            this.lastMarker = this.input.Mark();
            this.dbg.Mark(this.lastMarker);
            return this.lastMarker;
        }

        public void Release(int marker)
        {
        }

        public void Rewind()
        {
            this.dbg.Rewind();
            this.input.Rewind(this.lastMarker);
        }

        public void Rewind(int marker)
        {
            this.dbg.Rewind(marker);
            this.input.Rewind(marker);
        }

        public void Seek(int index)
        {
            this.input.Seek(index);
        }

        public void SetDebugListener(IDebugEventListener dbg)
        {
            this.dbg = dbg;
        }

        public int Size()
        {
            return this.input.Size();
        }

        public string ToString(object start, object stop)
        {
            return this.input.ToString(start, stop);
        }

        public virtual bool HasUniqueNavigationNodes
        {
            set
            {
                this.input.HasUniqueNavigationNodes = value;
            }
        }

        public ITokenStream TokenStream
        {
            get
            {
                return this.input.TokenStream;
            }
        }

        public ITreeAdaptor TreeAdaptor
        {
            get
            {
                return this.adaptor;
            }
        }

        public object TreeSource
        {
            get
            {
                return this.input;
            }
        }
    }
}

