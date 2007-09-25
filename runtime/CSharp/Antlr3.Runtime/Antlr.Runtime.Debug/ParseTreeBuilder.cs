namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using Antlr.Runtime.Tree;
    using System;
    using System.Collections;

    public class ParseTreeBuilder : BlankDebugEventListener
    {
        private Stack callStack = new Stack();

        public ParseTreeBuilder(string grammarName)
        {
            ParseTree tree = this.Create("<grammar " + grammarName + ">");
            this.callStack.Push(tree);
        }

        public override void ConsumeToken(IToken token)
        {
            ParseTree tree = (ParseTree) this.callStack.Peek();
            ParseTree t = this.Create(token);
            tree.AddChild(t);
        }

        public ParseTree Create(object payload)
        {
            return new ParseTree(payload);
        }

        public override void EnterRule(string ruleName)
        {
            ParseTree tree = (ParseTree) this.callStack.Peek();
            ParseTree t = this.Create(ruleName);
            tree.AddChild(t);
            this.callStack.Push(t);
        }

        public override void ExitRule(string ruleName)
        {
            this.callStack.Pop();
        }

        public ParseTree GetTree()
        {
            return (ParseTree) this.callStack.Peek();
        }

        public override void RecognitionException(Antlr.Runtime.RecognitionException e)
        {
            ParseTree tree = (ParseTree) this.callStack.Peek();
            ParseTree t = this.Create(e);
            tree.AddChild(t);
        }
    }
}

