namespace antlr.debug
{
    using System;

    public class Tracer : TraceListenerBase, TraceListener, Listener
    {
        protected string indentString = "";

        protected internal virtual void dedent()
        {
            if (this.indentString.Length < 2)
            {
                this.indentString = "";
            }
            else
            {
                this.indentString = this.indentString.Substring(2);
            }
        }

        public override void enterRule(object source, TraceEventArgs e)
        {
            Console.Out.WriteLine(this.indentString + e);
            this.indent();
        }

        public override void exitRule(object source, TraceEventArgs e)
        {
            this.dedent();
            Console.Out.WriteLine(this.indentString + e);
        }

        protected internal virtual void indent()
        {
            this.indentString = this.indentString + "  ";
        }
    }
}

