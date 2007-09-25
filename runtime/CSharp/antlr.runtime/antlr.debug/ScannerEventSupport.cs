namespace antlr.debug
{
    using antlr;
    using antlr.collections.impl;
    using System;
    using System.Collections;

    public class ScannerEventSupport
    {
        private Hashtable listeners = new Hashtable();
        private MatchEventArgs matchEvent = new MatchEventArgs();
        private MessageEventArgs messageEvent = new MessageEventArgs();
        private NewLineEventArgs newLineEvent = new NewLineEventArgs();
        private int ruleDepth = 0;
        private SemanticPredicateEventArgs semPredEvent = new SemanticPredicateEventArgs();
        private object source;
        private SyntacticPredicateEventArgs synPredEvent = new SyntacticPredicateEventArgs();
        private TokenEventArgs tokenEvent = new TokenEventArgs();
        private TraceEventArgs traceEvent = new TraceEventArgs();

        public ScannerEventSupport(object source)
        {
            this.source = source;
        }

        public virtual void addDoneListener(Listener l)
        {
            ((CharScanner) this.source).Done += new TraceEventHandler(l.doneParsing);
            this.listeners[l] = l;
        }

        public virtual void addMessageListener(MessageListener l)
        {
            ((CharScanner) this.source).ErrorReported += new MessageEventHandler(l.reportError);
            ((CharScanner) this.source).WarningReported += new MessageEventHandler(l.reportWarning);
            this.addDoneListener(l);
        }

        public virtual void addNewLineListener(NewLineListener l)
        {
            ((CharScanner) this.source).HitNewLine += new NewLineEventHandler(l.hitNewLine);
            this.addDoneListener(l);
        }

        public virtual void addParserListener(ParserListener l)
        {
            if (l is ParserController)
            {
            }
            this.addParserMatchListener(l);
            this.addParserTokenListener(l);
            this.addMessageListener(l);
            this.addTraceListener(l);
            this.addSemanticPredicateListener(l);
            this.addSyntacticPredicateListener(l);
        }

        public virtual void addParserMatchListener(ParserMatchListener l)
        {
            ((CharScanner) this.source).MatchedChar += new MatchEventHandler(l.parserMatch);
            ((CharScanner) this.source).MatchedNotChar += new MatchEventHandler(l.parserMatchNot);
            ((CharScanner) this.source).MisMatchedChar += new MatchEventHandler(l.parserMismatch);
            ((CharScanner) this.source).MisMatchedNotChar += new MatchEventHandler(l.parserMismatchNot);
            this.addDoneListener(l);
        }

        public virtual void addParserTokenListener(ParserTokenListener l)
        {
            ((CharScanner) this.source).ConsumedChar += new TokenEventHandler(l.parserConsume);
            ((CharScanner) this.source).CharLA += new TokenEventHandler(l.parserLA);
            this.addDoneListener(l);
        }

        public virtual void addSemanticPredicateListener(SemanticPredicateListener l)
        {
            ((CharScanner) this.source).SemPredEvaluated += new SemanticPredicateEventHandler(l.semanticPredicateEvaluated);
            this.addDoneListener(l);
        }

        public virtual void addSyntacticPredicateListener(SyntacticPredicateListener l)
        {
            ((CharScanner) this.source).SynPredStarted += new SyntacticPredicateEventHandler(l.syntacticPredicateStarted);
            ((CharScanner) this.source).SynPredFailed += new SyntacticPredicateEventHandler(l.syntacticPredicateFailed);
            ((CharScanner) this.source).SynPredSucceeded += new SyntacticPredicateEventHandler(l.syntacticPredicateSucceeded);
            this.addDoneListener(l);
        }

        public virtual void addTraceListener(TraceListener l)
        {
            ((CharScanner) this.source).EnterRule += new TraceEventHandler(l.enterRule);
            ((CharScanner) this.source).ExitRule += new TraceEventHandler(l.exitRule);
            this.addDoneListener(l);
        }

        public virtual void checkController()
        {
        }

        public virtual void fireConsume(int c)
        {
            TokenEventHandler handler = (TokenEventHandler) ((CharScanner) this.source).Events[Parser.LAEventKey];
            if (handler != null)
            {
                this.tokenEvent.setValues(TokenEventArgs.CONSUME, 1, c);
                handler(this.source, this.tokenEvent);
            }
            this.checkController();
        }

        public virtual void fireDoneParsing()
        {
            TraceEventHandler handler = (TraceEventHandler) ((CharScanner) this.source).Events[Parser.DoneEventKey];
            if (handler != null)
            {
                this.traceEvent.setValues(TraceEventArgs.DONE_PARSING, 0, 0, 0);
                handler(this.source, this.traceEvent);
            }
            this.checkController();
        }

        public virtual void fireEnterRule(int ruleNum, int guessing, int data)
        {
            this.ruleDepth++;
            TraceEventHandler handler = (TraceEventHandler) ((CharScanner) this.source).Events[Parser.EnterRuleEventKey];
            if (handler != null)
            {
                this.traceEvent.setValues(TraceEventArgs.ENTER, ruleNum, guessing, data);
                handler(this.source, this.traceEvent);
            }
            this.checkController();
        }

        public virtual void fireExitRule(int ruleNum, int guessing, int data)
        {
            TraceEventHandler handler = (TraceEventHandler) ((CharScanner) this.source).Events[Parser.ExitRuleEventKey];
            if (handler != null)
            {
                this.traceEvent.setValues(TraceEventArgs.EXIT, ruleNum, guessing, data);
                handler(this.source, this.traceEvent);
            }
            this.checkController();
            this.ruleDepth--;
            if (this.ruleDepth == 0)
            {
                this.fireDoneParsing();
            }
        }

        public virtual void fireLA(int k, int la)
        {
            TokenEventHandler handler = (TokenEventHandler) ((CharScanner) this.source).Events[Parser.LAEventKey];
            if (handler != null)
            {
                this.tokenEvent.setValues(TokenEventArgs.LA, k, la);
                handler(this.source, this.tokenEvent);
            }
            this.checkController();
        }

        public virtual void fireMatch(char c, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.CHAR, c, c, null, guessing, false, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMatch(string s, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.STRING, 0, s, null, guessing, false, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMatch(char c, BitSet b, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.CHAR_BITSET, c, b, null, guessing, false, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMatch(char c, string target, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.CHAR_RANGE, c, target, null, guessing, false, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMatch(int n, string text, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.TOKEN, n, n, text, guessing, false, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMatch(int c, BitSet b, string text, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.BITSET, c, b, text, guessing, false, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMatchNot(char c, char n, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MatchNotEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.CHAR, c, n, null, guessing, true, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMatchNot(int c, int n, string text, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MatchNotEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.TOKEN, c, n, text, guessing, true, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMismatch(char c, BitSet b, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MisMatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.CHAR_BITSET, c, b, null, guessing, false, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMismatch(char c, char n, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MisMatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.CHAR, c, n, null, guessing, false, false);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMismatch(char c, string target, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MisMatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.CHAR_RANGE, c, target, null, guessing, false, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMismatch(string s, string text, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MisMatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.STRING, 0, text, s, guessing, false, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMismatch(int i, BitSet b, string text, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MisMatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.BITSET, i, b, text, guessing, false, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMismatch(int i, int n, string text, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MisMatchEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.TOKEN, i, n, text, guessing, false, false);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMismatchNot(char v, char c, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MisMatchNotEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.CHAR, v, c, null, guessing, true, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireMismatchNot(int i, int n, string text, int guessing)
        {
            MatchEventHandler handler = (MatchEventHandler) ((CharScanner) this.source).Events[Parser.MisMatchNotEventKey];
            if (handler != null)
            {
                this.matchEvent.setValues(MatchEventArgs.TOKEN, i, n, text, guessing, true, true);
                handler(this.source, this.matchEvent);
            }
            this.checkController();
        }

        public virtual void fireNewLine(int line)
        {
            NewLineEventHandler handler = (NewLineEventHandler) ((CharScanner) this.source).Events[Parser.NewLineEventKey];
            if (handler != null)
            {
                this.newLineEvent.Line = line;
                handler(this.source, this.newLineEvent);
            }
            this.checkController();
        }

        public virtual void fireReportError(Exception e)
        {
            MessageEventHandler handler = (MessageEventHandler) ((CharScanner) this.source).Events[Parser.ReportErrorEventKey];
            if (handler != null)
            {
                this.messageEvent.setValues(MessageEventArgs.ERROR, e.ToString());
                handler(this.source, this.messageEvent);
            }
            this.checkController();
        }

        public virtual void fireReportError(string s)
        {
            MessageEventHandler handler = (MessageEventHandler) ((CharScanner) this.source).Events[Parser.ReportErrorEventKey];
            if (handler != null)
            {
                this.messageEvent.setValues(MessageEventArgs.ERROR, s);
                handler(this.source, this.messageEvent);
            }
            this.checkController();
        }

        public virtual void fireReportWarning(string s)
        {
            MessageEventHandler handler = (MessageEventHandler) ((CharScanner) this.source).Events[Parser.ReportWarningEventKey];
            if (handler != null)
            {
                this.messageEvent.setValues(MessageEventArgs.WARNING, s);
                handler(this.source, this.messageEvent);
            }
            this.checkController();
        }

        public virtual bool fireSemanticPredicateEvaluated(int type, int condition, bool result, int guessing)
        {
            SemanticPredicateEventHandler handler = (SemanticPredicateEventHandler) ((CharScanner) this.source).Events[Parser.SemPredEvaluatedEventKey];
            if (handler != null)
            {
                this.semPredEvent.setValues(type, condition, result, guessing);
                handler(this.source, this.semPredEvent);
            }
            this.checkController();
            return result;
        }

        public virtual void fireSyntacticPredicateFailed(int guessing)
        {
            SyntacticPredicateEventHandler handler = (SyntacticPredicateEventHandler) ((CharScanner) this.source).Events[Parser.SynPredFailedEventKey];
            if (handler != null)
            {
                this.synPredEvent.setValues(0, guessing);
                handler(this.source, this.synPredEvent);
            }
            this.checkController();
        }

        public virtual void fireSyntacticPredicateStarted(int guessing)
        {
            SyntacticPredicateEventHandler handler = (SyntacticPredicateEventHandler) ((CharScanner) this.source).Events[Parser.SynPredStartedEventKey];
            if (handler != null)
            {
                this.synPredEvent.setValues(0, guessing);
                handler(this.source, this.synPredEvent);
            }
            this.checkController();
        }

        public virtual void fireSyntacticPredicateSucceeded(int guessing)
        {
            SyntacticPredicateEventHandler handler = (SyntacticPredicateEventHandler) ((CharScanner) this.source).Events[Parser.SynPredSucceededEventKey];
            if (handler != null)
            {
                this.synPredEvent.setValues(0, guessing);
                handler(this.source, this.synPredEvent);
            }
            this.checkController();
        }

        public virtual void refreshListeners()
        {
            Hashtable hashtable;
            lock (this.listeners.SyncRoot)
            {
                hashtable = (Hashtable) this.listeners.Clone();
            }
            foreach (Listener listener in hashtable)
            {
                listener.refresh();
            }
        }

        public virtual void removeDoneListener(Listener l)
        {
            ((CharScanner) this.source).Done -= new TraceEventHandler(l.doneParsing);
            this.listeners.Remove(l);
        }

        public virtual void removeMessageListener(MessageListener l)
        {
            ((CharScanner) this.source).ErrorReported -= new MessageEventHandler(l.reportError);
            ((CharScanner) this.source).WarningReported -= new MessageEventHandler(l.reportWarning);
            this.removeDoneListener(l);
        }

        public virtual void removeNewLineListener(NewLineListener l)
        {
            ((CharScanner) this.source).HitNewLine -= new NewLineEventHandler(l.hitNewLine);
            this.removeDoneListener(l);
        }

        public virtual void removeParserListener(ParserListener l)
        {
            this.removeParserMatchListener(l);
            this.removeMessageListener(l);
            this.removeParserTokenListener(l);
            this.removeTraceListener(l);
            this.removeSemanticPredicateListener(l);
            this.removeSyntacticPredicateListener(l);
        }

        public virtual void removeParserMatchListener(ParserMatchListener l)
        {
            ((CharScanner) this.source).MatchedChar -= new MatchEventHandler(l.parserMatch);
            ((CharScanner) this.source).MatchedNotChar -= new MatchEventHandler(l.parserMatchNot);
            ((CharScanner) this.source).MisMatchedChar -= new MatchEventHandler(l.parserMismatch);
            ((CharScanner) this.source).MisMatchedNotChar -= new MatchEventHandler(l.parserMismatchNot);
            this.removeDoneListener(l);
        }

        public virtual void removeParserTokenListener(ParserTokenListener l)
        {
            ((CharScanner) this.source).ConsumedChar -= new TokenEventHandler(l.parserConsume);
            ((CharScanner) this.source).CharLA -= new TokenEventHandler(l.parserLA);
            this.removeDoneListener(l);
        }

        public virtual void removeSemanticPredicateListener(SemanticPredicateListener l)
        {
            ((CharScanner) this.source).SemPredEvaluated -= new SemanticPredicateEventHandler(l.semanticPredicateEvaluated);
            this.removeDoneListener(l);
        }

        public virtual void removeSyntacticPredicateListener(SyntacticPredicateListener l)
        {
            ((CharScanner) this.source).SynPredStarted -= new SyntacticPredicateEventHandler(l.syntacticPredicateStarted);
            ((CharScanner) this.source).SynPredFailed -= new SyntacticPredicateEventHandler(l.syntacticPredicateFailed);
            ((CharScanner) this.source).SynPredSucceeded -= new SyntacticPredicateEventHandler(l.syntacticPredicateSucceeded);
            this.removeDoneListener(l);
        }

        public virtual void removeTraceListener(TraceListener l)
        {
            ((CharScanner) this.source).EnterRule -= new TraceEventHandler(l.enterRule);
            ((CharScanner) this.source).ExitRule -= new TraceEventHandler(l.exitRule);
            this.removeDoneListener(l);
        }
    }
}

