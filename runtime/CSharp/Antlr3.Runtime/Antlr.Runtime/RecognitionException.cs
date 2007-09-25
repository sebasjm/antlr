namespace Antlr.Runtime
{
    using Antlr.Runtime.Tree;
    using System;

    [Serializable]
    public class RecognitionException : Exception
    {
        protected int c;
        protected int charPositionInLine;
        protected int index;
        [NonSerialized]
        protected IIntStream input;
        protected int line;
        protected object node;
        protected IToken token;

        public RecognitionException() : this(null, null, null)
        {
        }

        public RecognitionException(IIntStream input) : this(null, null, input)
        {
        }

        public RecognitionException(string message) : this(message, null, null)
        {
        }

        public RecognitionException(string message, IIntStream input) : this(message, null, input)
        {
        }

        public RecognitionException(string message, Exception inner) : this(message, inner, null)
        {
        }

        public RecognitionException(string message, Exception inner, IIntStream input) : base(message, inner)
        {
            this.input = input;
            this.index = input.Index();
            if (input is ITokenStream)
            {
                this.token = ((ITokenStream) input).LT(1);
                this.line = this.token.Line;
                this.charPositionInLine = this.token.CharPositionInLine;
            }
            if (input is CommonTreeNodeStream)
            {
                this.node = ((CommonTreeNodeStream) input).LT(1);
                if (this.node is CommonTree)
                {
                    this.token = ((CommonTree) this.node).Token;
                    this.line = this.token.Line;
                    this.charPositionInLine = this.token.CharPositionInLine;
                }
            }
            else if (input is ICharStream)
            {
                this.c = input.LA(1);
                this.line = ((ICharStream) input).Line;
                this.charPositionInLine = ((ICharStream) input).CharPositionInLine;
            }
            else
            {
                this.c = input.LA(1);
            }
        }

        public int Char
        {
            get
            {
                return this.c;
            }
            set
            {
                this.c = value;
            }
        }

        public int CharPositionInLine
        {
            get
            {
                return this.charPositionInLine;
            }
            set
            {
                this.charPositionInLine = value;
            }
        }

        public int Index
        {
            get
            {
                return this.index;
            }
            set
            {
                this.index = value;
            }
        }

        public IIntStream Input
        {
            get
            {
                return this.input;
            }
            set
            {
                this.input = value;
            }
        }

        public int Line
        {
            get
            {
                return this.line;
            }
            set
            {
                this.line = value;
            }
        }

        public object Node
        {
            get
            {
                return this.node;
            }
            set
            {
                this.node = value;
            }
        }

        public IToken Token
        {
            get
            {
                return this.token;
            }
            set
            {
                this.token = value;
            }
        }

        public virtual int UnexpectedType
        {
            get
            {
                if (this.input is ITokenStream)
                {
                    return this.token.Type;
                }
                return this.c;
            }
        }
    }
}

