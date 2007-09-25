namespace antlr.debug
{
    using System;
    using System.Runtime.CompilerServices;

    public interface IParserDebugSubject : IDebugSubject
    {
        event TokenEventHandler ConsumedToken;

        event MatchEventHandler MatchedNotToken;

        event MatchEventHandler MatchedToken;

        event MatchEventHandler MisMatchedNotToken;

        event MatchEventHandler MisMatchedToken;

        event TokenEventHandler TokenLA;
    }
}

