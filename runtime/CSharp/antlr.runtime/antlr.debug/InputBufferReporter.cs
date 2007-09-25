namespace antlr.debug
{
    using System;

    public class InputBufferReporter : InputBufferListenerBase, InputBufferListener, Listener
    {
        public virtual void inputBufferChanged(object source, InputBufferEventArgs e)
        {
            Console.Out.WriteLine(e);
        }

        public override void inputBufferConsume(object source, InputBufferEventArgs e)
        {
            Console.Out.WriteLine(e);
        }

        public override void inputBufferLA(object source, InputBufferEventArgs e)
        {
            Console.Out.WriteLine(e);
        }

        public override void inputBufferMark(object source, InputBufferEventArgs e)
        {
            Console.Out.WriteLine(e);
        }

        public override void inputBufferRewind(object source, InputBufferEventArgs e)
        {
            Console.Out.WriteLine(e);
        }
    }
}

