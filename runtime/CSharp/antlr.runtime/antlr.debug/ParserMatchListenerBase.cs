namespace antlr.debug
{
    using System;

    public abstract class ParserMatchListenerBase : ParserMatchListener, Listener
    {
        protected ParserMatchListenerBase()
        {
        }

        public virtual void doneParsing(object source, TraceEventArgs e)
        {
        }

        public virtual void parserMatch(object source, MatchEventArgs e)
        {
        }

        public virtual void parserMatchNot(object source, MatchEventArgs e)
        {
        }

        public virtual void parserMismatch(object source, MatchEventArgs e)
        {
        }

        public virtual void parserMismatchNot(object source, MatchEventArgs e)
        {
        }

        public virtual void refresh()
        {
        }
    }
}

