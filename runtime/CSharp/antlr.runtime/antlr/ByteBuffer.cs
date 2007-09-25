namespace antlr
{
    using System;
    using System.IO;

    public class ByteBuffer : InputBuffer
    {
        private byte[] buf = new byte[0x10];
        private const int BUF_SIZE = 0x10;
        [NonSerialized]
        internal Stream input;

        public ByteBuffer(Stream input_)
        {
            this.input = input_;
        }

        public override void fill(int amount)
        {
            int num2;
            this.syncConsume();
            for (int i = (amount + base.markerOffset) - base.queue.Count; i > 0; i -= num2)
            {
                num2 = this.input.Read(this.buf, 0, 0x10);
                for (int j = 0; j < num2; j++)
                {
                    base.queue.Add((char) this.buf[j]);
                }
                if (num2 < 0x10)
                {
                    while ((i-- > 0) && (base.queue.Count < 0x10))
                    {
                        base.queue.Add(CharScanner.EOF_CHAR);
                    }
                    break;
                }
            }
        }
    }
}

