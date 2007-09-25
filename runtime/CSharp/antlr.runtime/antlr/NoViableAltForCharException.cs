namespace antlr
{
    using System;
    using System.Text;

    [Serializable]
    public class NoViableAltForCharException : RecognitionException
    {
        public char foundChar;

        public NoViableAltForCharException(char c, CharScanner scanner) : base("NoViableAlt", scanner.getFilename(), scanner.getLine(), scanner.getColumn())
        {
            this.foundChar = c;
        }

        public NoViableAltForCharException(char c, string fileName, int line, int column) : base("NoViableAlt", fileName, line, column)
        {
            this.foundChar = c;
        }

        public override string Message
        {
            get
            {
                StringBuilder builder = new StringBuilder("unexpected char: ");
                if ((this.foundChar >= ' ') && (this.foundChar <= '~'))
                {
                    builder.Append('\'');
                    builder.Append(this.foundChar);
                    builder.Append('\'');
                }
                else
                {
                    builder.Append("0x");
                    builder.Append(((int) this.foundChar).ToString("X"));
                }
                return builder.ToString();
            }
        }
    }
}

