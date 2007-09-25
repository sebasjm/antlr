namespace antlr
{
    using System;
    using System.IO;

    public class CharBuffer : InputBuffer
    {
        private char[] buf = new char[0x10];
        private const int BUF_SIZE = 0x10;
        [NonSerialized]
        internal TextReader input;

        public CharBuffer(TextReader input_)
        {
            this.input = input_;
        }

        public override void fill(int amount)
        {
            try
            {
                int num2;
                this.syncConsume();
                for (int i = (amount + base.markerOffset) - base.queue.Count; i > 0; i -= num2)
                {
                    num2 = this.input.Read(this.buf, 0, 0x10);
                    for (int j = 0; j < num2; j++)
                    {
                        base.queue.Add(this.buf[j]);
                    }
                    if (num2 < 0x10)
                    {
                        while ((i-- > 0) && (base.queue.Count < 0x10))
                        {
                            base.queue.Add(CharScanner.EOF_CHAR);
                        }
                        return;
                    }
                }
            }
            catch (IOException exception)
            {
                throw new CharStreamIOException(exception);
            }
        }
    }
}

