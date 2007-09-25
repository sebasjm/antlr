namespace Antlr.Runtime
{
    using System;

    [Serializable]
    public class NoViableAltException : RecognitionException
    {
        public int decisionNumber;
        public string grammarDecisionDescription;
        public int stateNumber;

        public NoViableAltException()
        {
        }

        public NoViableAltException(string grammarDecisionDescription, int decisionNumber, int stateNumber, IIntStream input) : base(input)
        {
            this.grammarDecisionDescription = grammarDecisionDescription;
            this.decisionNumber = decisionNumber;
            this.stateNumber = stateNumber;
        }

        public override string ToString()
        {
            return string.Concat(new object[] { "NoViableAltException(", this.UnexpectedType, "!=[", this.grammarDecisionDescription, "])" });
        }
    }
}

