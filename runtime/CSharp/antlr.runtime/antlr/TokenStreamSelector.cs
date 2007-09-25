namespace antlr
{
    using System;
    using System.Collections;

    public class TokenStreamSelector : TokenStream
    {
        protected internal TokenStream input;
        protected internal Hashtable inputStreamNames = new Hashtable();
        protected internal Stack streamStack = new Stack();

        public virtual void addInputStream(TokenStream stream, string key)
        {
            this.inputStreamNames[key] = stream;
        }

        public virtual TokenStream getCurrentStream()
        {
            return this.input;
        }

        public virtual TokenStream getStream(string sname)
        {
            TokenStream stream = (TokenStream) this.inputStreamNames[sname];
            if (stream == null)
            {
                throw new ArgumentException("TokenStream " + sname + " not found");
            }
            return stream;
        }

        public virtual IToken nextToken()
        {
            while (true)
            {
                try
                {
                    return this.input.nextToken();
                }
                catch (TokenStreamRetryException)
                {
                }
            }
        }

        public virtual TokenStream pop()
        {
            TokenStream stream = (TokenStream) this.streamStack.Pop();
            this.select(stream);
            return stream;
        }

        public virtual void push(TokenStream stream)
        {
            this.streamStack.Push(this.input);
            this.select(stream);
        }

        public virtual void push(string sname)
        {
            this.streamStack.Push(this.input);
            this.select(sname);
        }

        public virtual void retry()
        {
            throw new TokenStreamRetryException();
        }

        public virtual void select(TokenStream stream)
        {
            this.input = stream;
            if (this.input is CharScanner)
            {
                ((CharScanner) this.input).refresh();
            }
        }

        public virtual void select(string sname)
        {
            this.input = this.getStream(sname);
            if (this.input is CharScanner)
            {
                ((CharScanner) this.input).refresh();
            }
        }
    }
}

