namespace antlr.debug
{
    using System;

    public interface ParserController : ParserListener, SemanticPredicateListener, ParserMatchListener, MessageListener, ParserTokenListener, TraceListener, SyntacticPredicateListener, Listener
    {
        void checkBreak();

        antlr.debug.ParserEventSupport ParserEventSupport { set; }
    }
}

