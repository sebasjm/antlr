namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using Antlr.Runtime.Misc;
    using Antlr.Runtime.Tree;
    using System;
    using System.IO;

    public class DebugTreeParser : TreeParser
    {
        protected IDebugEventListener dbg;
        public bool isCyclicDecision;

        public DebugTreeParser(ITreeNodeStream input) : this(input, 0xc001)
        {
        }

        public DebugTreeParser(ITreeNodeStream input, IDebugEventListener dbg) : base(new DebugTreeNodeStream(input, dbg))
        {
            this.dbg = null;
            this.isCyclicDecision = false;
            this.DebugListener = dbg;
        }

        public DebugTreeParser(ITreeNodeStream input, int port) : base(new DebugTreeNodeStream(input, null))
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
                this.reportError(exception);
            }
        }

        public override void BeginBacktrack(int level)
        {
            this.dbg.BeginBacktrack(level);
        }

        public override void BeginResync()
        {
            this.dbg.BeginResync();
        }

        public override void EndBacktrack(int level, bool successful)
        {
            this.dbg.EndBacktrack(level, successful);
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

        public virtual void RecoverFromMismatchedToken(IIntStream input, MismatchedTokenException mte, int ttype, BitSet follow)
        {
            this.dbg.RecognitionException(mte);
            base.RecoverFromMismatchedToken(input, mte, ttype, follow);
        }

        public void reportError(IOException e)
        {
            ErrorManager.InternalError(e);
        }

        public IDebugEventListener DebugListener
        {
            get
            {
                return this.dbg;
            }
            set
            {
                if (base.input is DebugTreeNodeStream)
                {
                    ((DebugTreeNodeStream) base.input).SetDebugListener(this.dbg);
                }
                this.dbg = value;
            }
        }
    }
}

