namespace antlr.debug
{
    using System;
    using System.Runtime.CompilerServices;

    public interface ICharScannerDebugSubject : IDebugSubject
    {
        event TokenEventHandler CharLA;

        event TokenEventHandler ConsumedChar;

        event NewLineEventHandler HitNewLine;

        event MatchEventHandler MatchedChar;

        event MatchEventHandler MatchedNotChar;

        event MatchEventHandler MisMatchedChar;

        event MatchEventHandler MisMatchedNotChar;
    }
}

