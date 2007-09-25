namespace Antlr.Runtime.Debug
{
    using Antlr.Runtime;
    using System;
    using System.IO;
    using System.Net.Sockets;
    using System.Text;

    public class DebugEventSocketProxy : BlankDebugEventListener
    {
        public const int DEFAULT_DEBUGGER_PORT = 0xc001;
        protected string grammarFileName;
        protected int port;
        protected StreamReader reader;
        protected TcpListener serverSocket;
        protected TcpClient socket;
        protected StreamWriter writer;

        public DebugEventSocketProxy() : this(null, 0xc001)
        {
        }

        public DebugEventSocketProxy(string grammarFileName, int port)
        {
            this.port = 0xc001;
            this.grammarFileName = grammarFileName;
            this.port = port;
        }

        protected internal virtual void Ack()
        {
            try
            {
                this.reader.ReadLine();
            }
            catch (IOException exception)
            {
                Console.Error.WriteLine(exception.StackTrace);
            }
        }

        public override void AddChild(int rootID, int childID)
        {
            this.Transmit(string.Concat(new object[] { "AddChild ", rootID, " ", childID }));
        }

        public override void BecomeRoot(int newRootID, int oldRootID)
        {
            this.Transmit(string.Concat(new object[] { "BecomeRoot ", newRootID, " ", oldRootID }));
        }

        public override void BeginBacktrack(int level)
        {
            this.Transmit("BeginBacktrack " + level);
        }

        public override void BeginResync()
        {
            this.Transmit("BeginResync");
        }

        public override void Commence()
        {
        }

        public override void ConsumeHiddenToken(IToken t)
        {
            this.Transmit("ConsumeHiddenToken " + this.SerializeToken(t));
        }

        public override void ConsumeNode(int ID, string text, int type)
        {
            text = this.EscapeNewlines(text);
            StringBuilder builder = new StringBuilder(50);
            builder.Append("ConsumeNode ");
            builder.Append(ID);
            builder.Append(" ");
            builder.Append(type);
            builder.Append(" ");
            builder.Append(text);
            this.Transmit(builder.ToString());
        }

        public override void ConsumeToken(IToken t)
        {
            this.Transmit("ConsumeToken " + this.SerializeToken(t));
        }

        public override void CreateNode(int ID, int tokenIndex)
        {
            this.Transmit(string.Concat(new object[] { "CreateNode ", ID, " ", tokenIndex }));
        }

        public override void CreateNode(int ID, string text, int type)
        {
            text = this.EscapeNewlines(text);
            StringBuilder builder = new StringBuilder(50);
            builder.Append("CreateNodeFromToken ");
            builder.Append(ID);
            builder.Append(" ");
            builder.Append(type);
            builder.Append(" ");
            builder.Append(text);
            this.Transmit(builder.ToString());
        }

        public override void EndBacktrack(int level, bool successful)
        {
            bool flag;
            this.Transmit(string.Concat(new object[] { "EndBacktrack ", level, " ", successful ? (flag = true).ToString() : (flag = false).ToString() }));
        }

        public override void EndResync()
        {
            this.Transmit("EndResync");
        }

        public override void EnterAlt(int alt)
        {
            this.Transmit("EnterAlt " + alt);
        }

        public override void EnterDecision(int decisionNumber)
        {
            this.Transmit("EnterDecision " + decisionNumber);
        }

        public override void EnterRule(string ruleName)
        {
            this.Transmit("EnterRule " + ruleName);
        }

        public override void EnterSubRule(int decisionNumber)
        {
            this.Transmit("EnterSubRule " + decisionNumber);
        }

        protected internal virtual string EscapeNewlines(string txt)
        {
            txt = txt.Replace("%", "%25");
            txt = txt.Replace("\n", "%0A");
            txt = txt.Replace("\r", "%0D");
            return txt;
        }

        public override void ExitDecision(int decisionNumber)
        {
            this.Transmit("ExitDecision " + decisionNumber);
        }

        public override void ExitRule(string ruleName)
        {
            this.Transmit("ExitRule " + ruleName);
        }

        public override void ExitSubRule(int decisionNumber)
        {
            this.Transmit("ExitSubRule " + decisionNumber);
        }

        public override void GetNilNode(int ID)
        {
            this.Transmit("GetNilNode " + ID);
        }

        public virtual void Handshake()
        {
            if (this.serverSocket == null)
            {
                this.serverSocket = new TcpListener(this.port);
                this.serverSocket.Start();
                this.socket = this.serverSocket.AcceptTcpClient();
                this.socket.NoDelay = true;
                this.reader = new StreamReader(this.socket.GetStream(), Encoding.UTF8);
                this.writer = new StreamWriter(this.socket.GetStream(), Encoding.UTF8);
                this.writer.WriteLine("ANTLR " + Constants.DEBUG_PROTOCOL_VERSION);
                this.writer.WriteLine("grammar \"" + this.grammarFileName);
                this.writer.Flush();
            }
        }

        public override void Location(int line, int pos)
        {
            this.Transmit(string.Concat(new object[] { "Location ", line, " ", pos }));
        }

        public override void LT(int i, IToken t)
        {
            if (t != null)
            {
                this.Transmit(string.Concat(new object[] { "LT ", i, " ", this.SerializeToken(t) }));
            }
        }

        public override void LT(int i, int ID, string text, int type)
        {
            text = this.EscapeNewlines(text);
            StringBuilder builder = new StringBuilder(50);
            builder.Append("LN ");
            builder.Append(i);
            builder.Append(" ");
            builder.Append(ID);
            builder.Append(" ");
            builder.Append(type);
            builder.Append(" ");
            builder.Append(text);
            this.Transmit(builder.ToString());
        }

        public override void Mark(int i)
        {
            this.Transmit("Mark " + i);
        }

        public override void RecognitionException(Antlr.Runtime.RecognitionException e)
        {
            StringBuilder builder = new StringBuilder(50);
            builder.Append("Exception ");
            builder.Append(e.GetType().FullName);
            builder.Append(" ");
            builder.Append(e.Index);
            builder.Append(" ");
            builder.Append(e.Line);
            builder.Append(" ");
            builder.Append(e.CharPositionInLine);
            this.Transmit(builder.ToString());
        }

        public override void Rewind()
        {
            this.Transmit("Rewind");
        }

        public override void Rewind(int i)
        {
            this.Transmit("Rewind " + i);
        }

        public override void SemanticPredicate(bool result, string predicate)
        {
            predicate = this.EscapeNewlines(predicate);
            StringBuilder builder = new StringBuilder(50);
            builder.Append("SemanticPredicate ");
            builder.Append(result);
            builder.Append(" ");
            builder.Append(predicate);
            this.Transmit(builder.ToString());
        }

        protected internal virtual string SerializeToken(IToken t)
        {
            StringBuilder builder = new StringBuilder(50);
            builder.Append(t.TokenIndex);
            builder.Append(' ');
            builder.Append(t.Type);
            builder.Append(' ');
            builder.Append(t.Channel);
            builder.Append(' ');
            builder.Append(t.Line);
            builder.Append(' ');
            builder.Append(t.CharPositionInLine);
            builder.Append(" \"");
            string text = t.Text;
            if (text == null)
            {
                text = "";
            }
            text = this.EscapeNewlines(text);
            builder.Append(text);
            return builder.ToString();
        }

        public override void SetTokenBoundaries(int ID, int tokenStartIndex, int tokenStopIndex)
        {
            this.Transmit(string.Concat(new object[] { "SetTokenBoundaries ", ID, " ", tokenStartIndex, " ", tokenStopIndex }));
        }

        public override void Terminate()
        {
            this.Transmit("Terminate");
            this.writer.Close();
            try
            {
                this.socket.Close();
            }
            catch (IOException exception)
            {
                Console.Error.WriteLine(exception.StackTrace);
            }
        }

        protected internal virtual void Transmit(string eventLabel)
        {
            this.writer.WriteLine(eventLabel);
            this.writer.Flush();
            this.Ack();
        }
    }
}

