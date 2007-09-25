namespace antlr
{
    using antlr.collections;
    using antlr.collections.impl;
    using System;

    public class TreeParser
    {
        protected internal ASTFactory astFactory = new ASTFactory();
        public static ASTNULLType ASTNULL = new ASTNULLType();
        protected internal TreeParserSharedInputState inputState = new TreeParserSharedInputState();
        protected internal AST retTree_;
        protected internal AST returnAST;
        protected internal string[] tokenNames;
        protected internal int traceDepth = 0;

        public virtual AST getAST()
        {
            return this.returnAST;
        }

        public virtual ASTFactory getASTFactory()
        {
            return this.astFactory;
        }

        public virtual string getTokenName(int num)
        {
            return this.tokenNames[num];
        }

        public virtual string[] getTokenNames()
        {
            return this.tokenNames;
        }

        public virtual void match(AST t, BitSet b)
        {
            if (!(((t != null) && (t != ASTNULL)) && b.member(t.Type)))
            {
                throw new MismatchedTokenException(this.getTokenNames(), t, b, false);
            }
        }

        protected internal virtual void match(AST t, int ttype)
        {
            if (((t == null) || (t == ASTNULL)) || (t.Type != ttype))
            {
                throw new MismatchedTokenException(this.getTokenNames(), t, ttype, false);
            }
        }

        protected internal virtual void matchNot(AST t, int ttype)
        {
            if (((t == null) || (t == ASTNULL)) || (t.Type == ttype))
            {
                throw new MismatchedTokenException(this.getTokenNames(), t, ttype, true);
            }
        }

        [Obsolete("De-activated since version 2.7.2.6 as it cannot be overidden.", true)]
        public static void panic()
        {
            Console.Error.WriteLine("TreeWalker: panic");
            Environment.Exit(1);
        }

        public virtual void reportError(RecognitionException ex)
        {
            Console.Error.WriteLine(ex.ToString());
        }

        public virtual void reportError(string s)
        {
            Console.Error.WriteLine("error: " + s);
        }

        public virtual void reportWarning(string s)
        {
            Console.Error.WriteLine("warning: " + s);
        }

        public virtual void resetState()
        {
            this.traceDepth = 0;
            this.returnAST = null;
            this.retTree_ = null;
            this.inputState.reset();
        }

        public virtual void setASTFactory(ASTFactory f)
        {
            this.astFactory = f;
        }

        public virtual void setASTNodeClass(string nodeType)
        {
            this.astFactory.setASTNodeType(nodeType);
        }

        public virtual void setASTNodeType(string nodeType)
        {
            this.setASTNodeClass(nodeType);
        }

        public virtual void traceIn(string rname, AST t)
        {
            this.traceDepth++;
            this.traceIndent();
            Console.Out.WriteLine("> " + rname + "(" + ((t != null) ? t.ToString() : "null") + ")" + ((this.inputState.guessing > 0) ? " [guessing]" : ""));
        }

        public virtual void traceIndent()
        {
            for (int i = 0; i < this.traceDepth; i++)
            {
                Console.Out.Write(" ");
            }
        }

        public virtual void traceOut(string rname, AST t)
        {
            this.traceIndent();
            Console.Out.WriteLine("< " + rname + "(" + ((t != null) ? t.ToString() : "null") + ")" + ((this.inputState.guessing > 0) ? " [guessing]" : ""));
            this.traceDepth--;
        }
    }
}

