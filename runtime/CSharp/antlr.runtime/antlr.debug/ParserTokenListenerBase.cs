namespace antlr.debug
{
    using System;

    public abstract class ParserTokenListenerBase : ParserTokenListener, Listener
    {
        protected ParserTokenListenerBase()
        {
        }

        public virtual void doneParsing(object source, TraceEventArgs e)
        {
        }

        public virtual void parserConsume(object source, TokenEventArgs e)
        {
        }

        public virtual void parserLA(object source, TokenEventArgs e)
        {
        }

        public virtual void refresh()
        {
        }
    }
}

