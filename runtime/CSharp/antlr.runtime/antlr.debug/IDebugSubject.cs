namespace antlr.debug
{
    using System;
    using System.Runtime.CompilerServices;

    public interface IDebugSubject
    {
        event TraceEventHandler Done;

        event TraceEventHandler EnterRule;

        event MessageEventHandler ErrorReported;

        event TraceEventHandler ExitRule;

        event SemanticPredicateEventHandler SemPredEvaluated;

        event SyntacticPredicateEventHandler SynPredFailed;

        event SyntacticPredicateEventHandler SynPredStarted;

        event SyntacticPredicateEventHandler SynPredSucceeded;

        event MessageEventHandler WarningReported;
    }
}

