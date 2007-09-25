namespace Antlr.Runtime
{
    using System;
    using System.Runtime.CompilerServices;

    public abstract class DFA
    {
        protected short[] accept;
        public const bool debug = false;
        protected int decisionNumber;
        protected short[] eof;
        protected short[] eot;
        protected int[] max;
        protected int[] min;
        protected BaseRecognizer recognizer;
        protected short[] special;
        public SpecialStateTransitionHandler specialStateTransitionHandler;
        protected short[][] transition;

        protected DFA()
        {
        }

        public virtual void Error(NoViableAltException nvae)
        {
        }

        protected void NoViableAlt(int s, IIntStream input)
        {
            if (this.recognizer.backtracking > 0)
            {
                this.recognizer.failed = true;
            }
            else
            {
                NoViableAltException nvae = new NoViableAltException(this.Description, this.decisionNumber, s, input);
                this.Error(nvae);
                throw nvae;
            }
        }

        public int Predict(IIntStream input)
        {
            int num6;
            int marker = input.Mark();
            int index = 0;
            try
            {
                bool flag;
                goto Label_0178;
            Label_0010:
                flag = true;
                int s = this.special[index];
                if (s >= 0)
                {
                    flag = true;
                    index = this.specialStateTransitionHandler(this, s, input);
                    input.Consume();
                }
                else
                {
                    if (this.accept[index] >= 1)
                    {
                        flag = true;
                        return this.accept[index];
                    }
                    char ch = (char) input.LA(1);
                    if ((ch >= this.min[index]) && (ch <= this.max[index]))
                    {
                        int num4 = this.transition[index][ch - this.min[index]];
                        if (num4 < 0)
                        {
                            if (this.eot[index] < 0)
                            {
                                this.NoViableAlt(index, input);
                                return 0;
                            }
                            flag = true;
                            index = this.eot[index];
                            input.Consume();
                        }
                        else
                        {
                            index = num4;
                            input.Consume();
                        }
                    }
                    else if (this.eot[index] >= 0)
                    {
                        flag = true;
                        index = this.eot[index];
                        input.Consume();
                    }
                    else
                    {
                        if ((ch == ((char) Token.EOF)) && (this.eof[index] >= 0))
                        {
                            flag = true;
                            return this.accept[this.eof[index]];
                        }
                        flag = true;
                        this.NoViableAlt(index, input);
                        return 0;
                    }
                }
            Label_0178:
                flag = true;
                goto Label_0010;
            }
            finally
            {
                input.Rewind(marker);
            }
            return num6;
        }

        public virtual int SpecialStateTransition(int s, IIntStream input)
        {
            return -1;
        }

        public int SpecialTransition(int state, int symbol)
        {
            return 0;
        }

        public virtual string Description
        {
            get
            {
                return "n/a";
            }
        }

        public delegate int SpecialStateTransitionHandler(DFA dfa, int s, IIntStream input);
    }
}

