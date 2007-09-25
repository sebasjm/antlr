namespace antlr
{
    using System;
    using System.Text;

    public class DefaultFileLineFormatter : FileLineFormatter
    {
        public override string getFormatString(string fileName, int line, int column)
        {
            StringBuilder builder = new StringBuilder();
            if (fileName != null)
            {
                builder.Append(fileName + ":");
            }
            if (line != -1)
            {
                if (fileName == null)
                {
                    builder.Append("line ");
                }
                builder.Append(line);
                if (column != -1)
                {
                    builder.Append(":" + column);
                }
                builder.Append(":");
            }
            builder.Append(" ");
            return builder.ToString();
        }
    }
}

