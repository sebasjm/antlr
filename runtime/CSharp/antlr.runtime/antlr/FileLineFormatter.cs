namespace antlr
{
    using System;

    public abstract class FileLineFormatter
    {
        private static FileLineFormatter formatter = new DefaultFileLineFormatter();

        protected FileLineFormatter()
        {
        }

        public abstract string getFormatString(string fileName, int line, int column);
        public static FileLineFormatter getFormatter()
        {
            return formatter;
        }

        public static void setFormatter(FileLineFormatter f)
        {
            formatter = f;
        }
    }
}

