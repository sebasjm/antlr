namespace Antlr.Runtime.Tree
{
    using Antlr.Runtime;
    using System;

    public class TreeParser : BaseRecognizer
    {
        public const int DOWN = 2;
        protected internal ITreeNodeStream input;
        public const int UP = 3;

        public TreeParser(ITreeNodeStream input)
        {
            this.TreeNodeStream = input;
        }

        public override string GetErrorHeader(RecognitionException e)
        {
            return string.Concat(new object[] { this.GrammarFileName, ": node from line ", e.Line, ":", e.CharPositionInLine });
        }

        public override string GetErrorMessage(RecognitionException e, string[] tokenNames)
        {
            if (this != null)
            {
                ITreeAdaptor treeAdaptor = ((ITreeNodeStream) e.Input).TreeAdaptor;
                e.Token = treeAdaptor.GetToken(e.Node);
                if (e.Token == null)
                {
                    e.Token = new CommonToken(treeAdaptor.GetNodeType(e.Node), treeAdaptor.GetNodeText(e.Node));
                }
            }
            return base.GetErrorMessage(e, tokenNames);
        }

        public override void MatchAny(IIntStream ignore)
        {
            base.errorRecovery = false;
            base.failed = false;
            object t = this.input.LT(1);
            if (this.input.TreeAdaptor.GetChildCount(t) == 0)
            {
                this.input.Consume();
            }
            else
            {
                int num = 0;
                int nodeType = this.input.TreeAdaptor.GetNodeType(t);
                while ((nodeType != Token.EOF) && ((nodeType != 3) || (num != 0)))
                {
                    this.input.Consume();
                    t = this.input.LT(1);
                    switch (this.input.TreeAdaptor.GetNodeType(t))
                    {
                        case 2:
                            num++;
                            break;

                        case 3:
                            num--;
                            break;
                    }
                }
                this.input.Consume();
            }
        }

        protected internal override void Mismatch(IIntStream input, int ttype, BitSet follow)
        {
            MismatchedTreeNodeException e = new MismatchedTreeNodeException(ttype, (ITreeNodeStream) input);
            this.RecoverFromMismatchedToken(input, e, ttype, follow);
        }

        public override void Reset()
        {
            base.Reset();
            if (this.input != null)
            {
                this.input.Seek(0);
            }
        }

        public virtual void TraceIn(string ruleName, int ruleIndex)
        {
            base.TraceIn(ruleName, ruleIndex, this.input.LT(1));
        }

        public virtual void TraceOut(string ruleName, int ruleIndex)
        {
            base.TraceOut(ruleName, ruleIndex, this.input.LT(1));
        }

        public override IIntStream Input
        {
            get
            {
                return this.input;
            }
        }

        public virtual ITreeNodeStream TreeNodeStream
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
    }
}

