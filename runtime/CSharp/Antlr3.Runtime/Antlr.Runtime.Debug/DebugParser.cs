namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using Antlr.Runtime.Misc;
    using System;
    using System.IO;

    public class DebugParser : Parser
    {
        protected internal IDebugEventListener dbg;
        public bool isCyclicDecision;

        public DebugParser(ITokenStream input) : this(input, 0xc001)
        {
        }

        public DebugParser(ITokenStream input, IDebugEventListener dbg) : base(new DebugTokenStream(input, dbg))
        {
            this.dbg = null;
            this.isCyclicDecision = false;
            this.DebugListener = dbg;
        }

        public DebugParser(ITokenStream input, int port) : base(new DebugTokenStream(input, null))
        {
            this.dbg = null;
            this.isCyclicDecision = false;
            DebugEventSocketProxy proxy = new DebugEventSocketProxy(this.GrammarFileName, port);
            this.DebugListener = proxy;
            try
            {
                proxy.Handshake();
            }
            catch (IOException exception)
            {
                this.ReportError(exception);
            }
        }

        public override void BeginResync()
        {
            this.dbg.BeginResync();
        }

        public override void EndResync()
        {
            this.dbg.EndResync();
        }

        public override void RecoverFromMismatchedSet(IIntStream input, RecognitionException mte, BitSet follow)
        {
            this.dbg.RecognitionException(mte);
            base.RecoverFromMismatchedSet(input, mte, follow);
        }

        public override void RecoverFromMismatchedToken(IIntStream input, RecognitionException mte, int ttype, BitSet follow)
        {
            this.dbg.RecognitionException(mte);
            base.RecoverFromMismatchedToken(input, mte, ttype, follow);
        }

        public virtual void ReportError(IOException e)
        {
            ErrorManager.InternalError(e);
        }

        public virtual IDebugEventListener DebugListener
        {
            get
            {
                return this.dbg;
            }
            set
            {
                if (base.input is DebugTokenStream)
                {
                    ((DebugTokenStream) base.input).DebugListener = value;
                }
                this.dbg = value;
            }
        }
    }
}

