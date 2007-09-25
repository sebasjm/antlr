namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using System;
    using System.IO;
    using System.Net.Sockets;
    using System.Text;
    using System.Threading;

    public class RemoteDebugEventSocketListener
    {
        internal TcpClient channel = null;
        internal string eventLabel;
        public string grammarFileName;
        internal string hostName;
        internal IDebugEventListener listener;
        internal const int MAX_EVENT_ELEMENTS = 8;
        internal int port;
        private int previousTokenIndex = -1;
        internal StreamReader reader;
        private bool tokenIndexesInvalid = false;
        public string version;
        internal StreamWriter writer;

        public RemoteDebugEventSocketListener(IDebugEventListener listener, string hostName, int port)
        {
            this.listener = listener;
            this.hostName = hostName;
            this.port = port;
            if (!this.OpenConnection())
            {
                throw new Exception();
            }
        }

        protected virtual void Ack()
        {
            this.writer.WriteLine("Ack");
            this.writer.Flush();
        }

        protected virtual void CloseConnection()
        {
            try
            {
                try
                {
                    this.reader.Close();
                    this.reader = null;
                    this.writer.Close();
                    this.writer = null;
                    this.channel.Close();
                    this.channel = null;
                }
                catch (Exception exception)
                {
                    Console.Error.WriteLine(exception);
                    Console.Error.WriteLine(exception.StackTrace);
                }
            }
            finally
            {
                IOException exception2;
                if (this.reader != null)
                {
                    try
                    {
                        this.reader.Close();
                    }
                    catch (IOException exception3)
                    {
                        exception2 = exception3;
                        Console.Error.WriteLine(exception2);
                    }
                }
                if (this.writer != null)
                {
                    this.writer.Close();
                }
                if (this.channel != null)
                {
                    try
                    {
                        this.channel.Close();
                    }
                    catch (IOException exception4)
                    {
                        exception2 = exception4;
                        Console.Error.WriteLine(exception2);
                    }
                }
            }
        }

        protected internal virtual ProxyToken DeserializeToken(string[] elements, int offset)
        {
            string s = elements[offset];
            string str2 = elements[offset + 1];
            string str3 = elements[offset + 2];
            string str4 = elements[offset + 3];
            string str5 = elements[offset + 4];
            string txt = elements[offset + 5];
            return new ProxyToken(int.Parse(s), int.Parse(str2), int.Parse(str3), int.Parse(str4), int.Parse(str5), this.UnEscapeNewlines(txt));
        }

        protected virtual void Dispatch(string line)
        {
            string[] eventElements = this.GetEventElements(line);
            if ((eventElements == null) || (eventElements[0] == null))
            {
                Console.Error.WriteLine("unknown debug event: " + line);
            }
            else if (eventElements[0].Equals("enterRule"))
            {
                this.listener.EnterRule(eventElements[1]);
            }
            else if (eventElements[0].Equals("exitRule"))
            {
                this.listener.ExitRule(eventElements[1]);
            }
            else if (eventElements[0].Equals("enterAlt"))
            {
                this.listener.EnterAlt(int.Parse(eventElements[1]));
            }
            else if (eventElements[0].Equals("enterSubRule"))
            {
                this.listener.EnterSubRule(int.Parse(eventElements[1]));
            }
            else if (eventElements[0].Equals("exitSubRule"))
            {
                this.listener.ExitSubRule(int.Parse(eventElements[1]));
            }
            else if (eventElements[0].Equals("enterDecision"))
            {
                this.listener.EnterDecision(int.Parse(eventElements[1]));
            }
            else if (eventElements[0].Equals("exitDecision"))
            {
                this.listener.ExitDecision(int.Parse(eventElements[1]));
            }
            else if (eventElements[0].Equals("location"))
            {
                this.listener.Location(int.Parse(eventElements[1]), int.Parse(eventElements[2]));
            }
            else
            {
                IToken token;
                if (eventElements[0].Equals("consumeToken"))
                {
                    token = this.DeserializeToken(eventElements, 1);
                    if (token.TokenIndex == this.previousTokenIndex)
                    {
                        this.tokenIndexesInvalid = true;
                    }
                    this.previousTokenIndex = token.TokenIndex;
                    this.listener.ConsumeToken(token);
                }
                else if (eventElements[0].Equals("consumeHiddenToken"))
                {
                    token = this.DeserializeToken(eventElements, 1);
                    if (token.TokenIndex == this.previousTokenIndex)
                    {
                        this.tokenIndexesInvalid = true;
                    }
                    this.previousTokenIndex = token.TokenIndex;
                    this.listener.ConsumeHiddenToken(token);
                }
                else if (eventElements[0].Equals("LT"))
                {
                    token = this.DeserializeToken(eventElements, 2);
                    this.listener.LT(int.Parse(eventElements[1]), token);
                }
                else if (eventElements[0].Equals("mark"))
                {
                    this.listener.Mark(int.Parse(eventElements[1]));
                }
                else if (eventElements[0].Equals("rewind"))
                {
                    if (eventElements[1] != null)
                    {
                        this.listener.Rewind(int.Parse(eventElements[1]));
                    }
                    else
                    {
                        this.listener.Rewind();
                    }
                }
                else if (eventElements[0].Equals("beginBacktrack"))
                {
                    this.listener.BeginBacktrack(int.Parse(eventElements[1]));
                }
                else if (eventElements[0].Equals("endBacktrack"))
                {
                    int level = int.Parse(eventElements[1]);
                    int num2 = int.Parse(eventElements[2]);
                    this.listener.EndBacktrack(level, num2 == 1);
                }
                else if (eventElements[0].Equals("exception"))
                {
                    string typeName = eventElements[1];
                    string s = eventElements[2];
                    string str3 = eventElements[3];
                    string str4 = eventElements[4];
                    try
                    {
                        RecognitionException e = (RecognitionException) Activator.CreateInstance(Type.GetType(typeName));
                        e.Index = int.Parse(s);
                        e.Line = int.Parse(str3);
                        e.CharPositionInLine = int.Parse(str4);
                        this.listener.RecognitionException(e);
                    }
                    catch (UnauthorizedAccessException exception2)
                    {
                        Console.Error.WriteLine("can't access class " + exception2);
                        Console.Error.WriteLine(exception2.StackTrace);
                    }
                }
                else if (eventElements[0].Equals("beginResync"))
                {
                    this.listener.BeginResync();
                }
                else if (eventElements[0].Equals("endResync"))
                {
                    this.listener.EndResync();
                }
                else if (eventElements[0].Equals("terminate"))
                {
                    this.listener.Terminate();
                }
                else if (eventElements[0].Equals("semanticPredicate"))
                {
                    bool result = bool.Parse(eventElements[1]);
                    string txt = eventElements[2];
                    txt = this.UnEscapeNewlines(txt);
                    this.listener.SemanticPredicate(result, txt);
                }
                else
                {
                    string str6;
                    if (eventElements[0].Equals("consumeNode"))
                    {
                        str6 = eventElements[3];
                        str6 = this.UnEscapeNewlines(str6);
                        this.listener.ConsumeNode(int.Parse(eventElements[1]), str6, int.Parse(eventElements[2]));
                    }
                    else if (eventElements[0].Equals("LN"))
                    {
                        str6 = eventElements[4];
                        str6 = this.UnEscapeNewlines(str6);
                        this.listener.LT(int.Parse(eventElements[1]), int.Parse(eventElements[2]), str6, int.Parse(eventElements[3]));
                    }
                    else if (eventElements[0].Equals("createNodeFromToken"))
                    {
                        str6 = eventElements[3];
                        str6 = this.UnEscapeNewlines(str6);
                        this.listener.CreateNode(int.Parse(eventElements[1]), str6, int.Parse(eventElements[2]));
                    }
                    else if (eventElements[0].Equals("createNode"))
                    {
                        this.listener.CreateNode(int.Parse(eventElements[1]), int.Parse(eventElements[2]));
                    }
                    else if (eventElements[0].Equals("nilNode"))
                    {
                        this.listener.GetNilNode(int.Parse(eventElements[1]));
                    }
                    else if (eventElements[0].Equals("becomeRoot"))
                    {
                        this.listener.BecomeRoot(int.Parse(eventElements[1]), int.Parse(eventElements[2]));
                    }
                    else if (eventElements[0].Equals("addChild"))
                    {
                        this.listener.AddChild(int.Parse(eventElements[1]), int.Parse(eventElements[2]));
                    }
                    else if (eventElements[0].Equals("setTokenBoundaries"))
                    {
                        this.listener.SetTokenBoundaries(int.Parse(eventElements[1]), int.Parse(eventElements[2]), int.Parse(eventElements[3]));
                    }
                    else
                    {
                        Console.Error.WriteLine("unknown debug event: " + line);
                    }
                }
            }
        }

        protected virtual void EventHandler()
        {
            try
            {
                try
                {
                    this.Handshake();
                    this.eventLabel = this.reader.ReadLine();
                    while (this.eventLabel != null)
                    {
                        this.Dispatch(this.eventLabel);
                        this.Ack();
                        this.eventLabel = this.reader.ReadLine();
                    }
                }
                catch (Exception exception)
                {
                    Console.Error.WriteLine(exception);
                    Console.Error.WriteLine(exception.StackTrace);
                }
            }
            finally
            {
                this.CloseConnection();
            }
        }

        public virtual string[] GetEventElements(string eventLabel)
        {
            if (eventLabel == null)
            {
                return null;
            }
            string[] strArray = new string[8];
            string str = null;
            try
            {
                int index = eventLabel.IndexOf('"');
                if (index >= 0)
                {
                    string str2 = eventLabel.Substring(0, index);
                    str = eventLabel.Substring(index + 1, eventLabel.Length - (index + 1));
                    eventLabel = str2;
                }
                string[] strArray2 = eventLabel.Split(new char[] { ' ', '\t' });
                int num2 = 0;
                while (num2 < strArray2.Length)
                {
                    strArray[num2] = strArray2[num2];
                    num2++;
                }
                if (str != null)
                {
                    strArray[num2] = str;
                }
            }
            catch (Exception exception)
            {
                Console.Error.WriteLine(exception.StackTrace);
            }
            return strArray;
        }

        protected virtual void Handshake()
        {
            string eventLabel = this.reader.ReadLine();
            string[] eventElements = this.GetEventElements(eventLabel);
            this.version = eventElements[1];
            string str2 = this.reader.ReadLine();
            string[] strArray2 = this.GetEventElements(str2);
            this.grammarFileName = strArray2[1];
            this.Ack();
            this.listener.Commence();
        }

        protected virtual bool OpenConnection()
        {
            bool flag = false;
            try
            {
                this.channel = new TcpClient(this.hostName, this.port);
                this.channel.NoDelay = true;
                this.writer = new StreamWriter(this.channel.GetStream(), Encoding.UTF8);
                this.reader = new StreamReader(this.channel.GetStream(), Encoding.UTF8);
                flag = true;
            }
            catch (Exception exception)
            {
                Console.Error.WriteLine(exception);
            }
            return flag;
        }

        public virtual void Run()
        {
            this.EventHandler();
        }

        public virtual void start()
        {
            new Thread(new ThreadStart(this.Run)).Start();
        }

        protected string UnEscapeNewlines(string txt)
        {
            txt = txt.Replace("%0A", "\n");
            txt = txt.Replace("%0D", "\r");
            txt = txt.Replace("%25", "%");
            return txt;
        }

        public bool TokenIndexesAreInvalid
        {
            get
            {
                return false;
            }
        }

        public class ProxyToken : IToken
        {
            internal int channel;
            internal int charPos;
            internal int index;
            internal int line;
            internal string text;
            internal int type;

            public ProxyToken(int index, int type, int channel, int line, int charPos, string text)
            {
                this.index = index;
                this.type = type;
                this.channel = channel;
                this.line = line;
                this.charPos = charPos;
                this.text = text;
            }

            public override string ToString()
            {
                string str = "";
                if (this.channel > 0)
                {
                    str = ",channel=" + this.channel;
                }
                return string.Concat(new object[] { "[", this.Text, "/<", this.type, ">", str, ",", this.line, ":", this.CharPositionInLine, "]" });
            }

            public int Channel
            {
                get
                {
                    return this.channel;
                }
                set
                {
                    this.channel = value;
                }
            }

            public int CharPositionInLine
            {
                get
                {
                    return this.charPos;
                }
                set
                {
                    this.charPos = value;
                }
            }

            public int Line
            {
                get
                {
                    return this.line;
                }
                set
                {
                    this.line = value;
                }
            }

            public string Text
            {
                get
                {
                    return this.text;
                }
                set
                {
                    this.text = value;
                }
            }

            public int TokenIndex
            {
                get
                {
                    return this.index;
                }
                set
                {
                    this.index = value;
                }
            }

            public int Type
            {
                get
                {
                    return this.type;
                }
                set
                {
                    this.type = value;
                }
            }
        }
    }
}

