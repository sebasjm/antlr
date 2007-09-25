namespace antlr.debug
{
    using System;

    public interface DebuggingParser
    {
        string getRuleName(int n);
        string getSemPredName(int n);
    }
}

