namespace antlr
{
    using System;

    [Serializable]
    public class TokenStreamRecognitionException : TokenStreamException
    {
        public RecognitionException recog;

        public TokenStreamRecognitionException(RecognitionException re) : base(re.Message)
        {
            this.recog = re;
        }

        public override string ToString()
        {
            return this.recog.ToString();
        }
    }
}

