namespace Antlr.Runtime.Debug
{
    using System;

    public class TraceDebugEventListener : BlankDebugEventListener
    {
        public override void AddChild(int rootID, int childID)
        {
            Console.Out.WriteLine(string.Concat(new object[] { "AddChild ", rootID, ", ", childID }));
        }

        public override void BecomeRoot(int newRootID, int oldRootID)
        {
            Console.Out.WriteLine(string.Concat(new object[] { "BecomeRoot ", newRootID, ", ", oldRootID }));
        }

        public override void ConsumeNode(int ID, string text, int type)
        {
            Console.Out.WriteLine(string.Concat(new object[] { "ConsumeNode ", ID, " ", text, " ", type }));
        }

        public override void CreateNode(int ID, int tokenIndex)
        {
            Console.Out.WriteLine(string.Concat(new object[] { "Create ", ID, ": ", tokenIndex }));
        }

        public override void CreateNode(int ID, string text, int type)
        {
            Console.Out.WriteLine(string.Concat(new object[] { "Create ", ID, ": ", text, ", ", type }));
        }

        public override void EnterRule(string ruleName)
        {
            Console.Out.WriteLine("EnterRule " + ruleName);
        }

        public override void EnterSubRule(int decisionNumber)
        {
            Console.Out.WriteLine("EnterSubRule");
        }

        public override void ExitRule(string ruleName)
        {
            Console.Out.WriteLine("ExitRule " + ruleName);
        }

        public override void ExitSubRule(int decisionNumber)
        {
            Console.Out.WriteLine("ExitSubRule");
        }

        public override void GetNilNode(int ID)
        {
            Console.Out.WriteLine("GetNilNode " + ID);
        }

        public override void Location(int line, int pos)
        {
            Console.Out.WriteLine(string.Concat(new object[] { "Location ", line, ":", pos }));
        }

        public override void LT(int i, int ID, string text, int type)
        {
            Console.Out.WriteLine(string.Concat(new object[] { "LT ", i, " ", ID, " ", text, " ", type }));
        }

        public override void SetTokenBoundaries(int ID, int tokenStartIndex, int tokenStopIndex)
        {
            Console.Out.WriteLine(string.Concat(new object[] { "SetTokenBoundaries ", ID, ", ", tokenStartIndex, ", ", tokenStopIndex }));
        }
    }
}

