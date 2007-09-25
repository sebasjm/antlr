namespace antlr
{
    using System;

    public interface IToken
    {
        int getColumn();
        string getFilename();
        int getLine();
        string getText();
        void setColumn(int c);
        void setFilename(string name);
        void setLine(int l);
        void setText(string t);

        int Type { get; set; }
    }
}

