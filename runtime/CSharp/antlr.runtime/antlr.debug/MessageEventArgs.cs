namespace antlr.debug
{
    using System;

    public class MessageEventArgs : ANTLREventArgs
    {
        public static int ERROR = 1;
        private string text_;
        public static int WARNING = 0;

        public MessageEventArgs()
        {
        }

        public MessageEventArgs(int type, string text)
        {
            this.setValues(type, text);
        }

        internal void setValues(int type, string text)
        {
            base.setValues(type);
            this.Text = text;
        }

        public override string ToString()
        {
            return ("ParserMessageEvent [" + ((this.Type == WARNING) ? "warning," : "error,") + this.Text + "]");
        }

        public virtual string Text
        {
            get
            {
                return this.text_;
            }
            set
            {
                this.text_ = value;
            }
        }
    }
}

