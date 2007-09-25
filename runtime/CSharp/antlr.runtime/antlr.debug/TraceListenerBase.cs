namespace antlr.debug
{
    using System;

    public abstract class TraceListenerBase : TraceListener, Listener
    {
        protected TraceListenerBase()
        {
        }

        public virtual void doneParsing(object source, TraceEventArgs e)
        {
        }

        public virtual void enterRule(object source, TraceEventArgs e)
        {
        }

        public virtual void exitRule(object source, TraceEventArgs e)
        {
        }

        public virtual void refresh()
        {
        }
    }
}

