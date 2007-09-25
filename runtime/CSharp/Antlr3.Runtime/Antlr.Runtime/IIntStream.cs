namespace Antlr.Runtime
{
    using System;

    public interface IIntStream
    {
        void Consume();
        int Index();
        int LA(int i);
        int Mark();
        void Release(int marker);
        void Rewind();
        void Rewind(int marker);
        void Seek(int index);
        int Size();
    }
}

