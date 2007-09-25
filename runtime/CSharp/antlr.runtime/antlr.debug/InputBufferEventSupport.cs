namespace antlr.debug
{
    using System;
    using System.Collections;

    public class InputBufferEventSupport
    {
        protected internal const int CONSUME = 0;
        private InputBufferEventArgs inputBufferEvent = new InputBufferEventArgs();
        private ArrayList inputBufferListeners;
        protected internal const int LA = 1;
        protected internal const int MARK = 2;
        protected internal const int REWIND = 3;
        private object source;

        public InputBufferEventSupport(object source)
        {
            this.source = source;
        }

        public virtual void addInputBufferListener(InputBufferListener l)
        {
            if (this.inputBufferListeners == null)
            {
                this.inputBufferListeners = new ArrayList();
            }
            this.inputBufferListeners.Add(l);
        }

        public virtual void fireConsume(char c)
        {
            this.inputBufferEvent.setValues(0, c, 0);
            this.fireEvents(0, this.inputBufferListeners);
        }

        public virtual void fireEvent(int type, Listener l)
        {
            switch (type)
            {
                case 0:
                    ((InputBufferListener) l).inputBufferConsume(this.source, this.inputBufferEvent);
                    break;

                case 1:
                    ((InputBufferListener) l).inputBufferLA(this.source, this.inputBufferEvent);
                    break;

                case 2:
                    ((InputBufferListener) l).inputBufferMark(this.source, this.inputBufferEvent);
                    break;

                case 3:
                    ((InputBufferListener) l).inputBufferRewind(this.source, this.inputBufferEvent);
                    break;

                default:
                    throw new ArgumentException("bad type " + type + " for fireEvent()");
            }
        }

        public virtual void fireEvents(int type, ArrayList listeners)
        {
            ArrayList list = null;
            Listener l = null;
            lock (this)
            {
                if (listeners == null)
                {
                    return;
                }
                list = (ArrayList) listeners.Clone();
            }
            if (list != null)
            {
                for (int i = 0; i < list.Count; i++)
                {
                    l = (Listener) list[i];
                    this.fireEvent(type, l);
                }
            }
        }

        public virtual void fireLA(char c, int la)
        {
            this.inputBufferEvent.setValues(1, c, la);
            this.fireEvents(1, this.inputBufferListeners);
        }

        public virtual void fireMark(int pos)
        {
            this.inputBufferEvent.setValues(2, ' ', pos);
            this.fireEvents(2, this.inputBufferListeners);
        }

        public virtual void fireRewind(int pos)
        {
            this.inputBufferEvent.setValues(3, ' ', pos);
            this.fireEvents(3, this.inputBufferListeners);
        }

        protected internal virtual void refresh(ArrayList listeners)
        {
            ArrayList list;
            lock (listeners)
            {
                list = (ArrayList) listeners.Clone();
            }
            if (list != null)
            {
                for (int i = 0; i < list.Count; i++)
                {
                    ((Listener) list[i]).refresh();
                }
            }
        }

        public virtual void refreshListeners()
        {
            this.refresh(this.inputBufferListeners);
        }

        public virtual void removeInputBufferListener(InputBufferListener l)
        {
            if (this.inputBufferListeners != null)
            {
                ArrayList inputBufferListeners = this.inputBufferListeners;
                object item = l;
                inputBufferListeners.Contains(item);
                inputBufferListeners.Remove(item);
            }
        }

        public virtual ArrayList InputBufferListeners
        {
            get
            {
                return this.inputBufferListeners;
            }
        }
    }
}

