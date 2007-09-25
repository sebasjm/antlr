namespace Antlr.Utility.Tree
{
    using Antlr.Runtime.Tree;
    using Antlr.StringTemplate;
    using System;
    using System.Collections;

    public class DOTTreeGenerator
    {
        public static Antlr.StringTemplate.StringTemplate _edgeST = new Antlr.StringTemplate.StringTemplate("$parent$ -> $child$ // \"$parentText$\" -> \"$childText$\"\n");
        public static Antlr.StringTemplate.StringTemplate _nodeST = new Antlr.StringTemplate.StringTemplate("$name$ [label=\"$text$\"];\n");
        public static Antlr.StringTemplate.StringTemplate _treeST = new Antlr.StringTemplate.StringTemplate("digraph {\n  ordering=out;\n  ranksep=.4;\n  node [shape=plaintext, fixedsize=true, fontsize=11, fontname=\"Courier\",\n        width=.25, height=.25];\n  edge [arrowsize=.5]\n  $nodes$\n  $edges$\n}\n");
        private int nodeNumber;
        private IDictionary nodeToNumberMap = new Hashtable();

        protected int GetNodeNumber(object t)
        {
            object obj2 = this.nodeToNumberMap[t];
            if (obj2 != null)
            {
                return (int) obj2;
            }
            this.nodeToNumberMap[t] = this.nodeNumber;
            this.nodeNumber++;
            return (this.nodeNumber - 1);
        }

        protected Antlr.StringTemplate.StringTemplate GetNodeST(ITreeAdaptor adaptor, object t)
        {
            string nodeText = adaptor.GetNodeText(t);
            Antlr.StringTemplate.StringTemplate instanceOf = _nodeST.GetInstanceOf();
            string val = "n" + this.GetNodeNumber(t);
            instanceOf.SetAttribute("name", val);
            instanceOf.SetAttribute("text", nodeText);
            return instanceOf;
        }

        public Antlr.StringTemplate.StringTemplate ToDOT(ITree tree)
        {
            return this.ToDOT(tree, new CommonTreeAdaptor());
        }

        public Antlr.StringTemplate.StringTemplate ToDOT(object tree, ITreeAdaptor adaptor)
        {
            return this.ToDOT(tree, adaptor, _treeST, _edgeST);
        }

        public Antlr.StringTemplate.StringTemplate ToDOT(object tree, ITreeAdaptor adaptor, Antlr.StringTemplate.StringTemplate _treeST, Antlr.StringTemplate.StringTemplate _edgeST)
        {
            Antlr.StringTemplate.StringTemplate instanceOf = _treeST.GetInstanceOf();
            this.nodeNumber = 0;
            this.ToDOTDefineNodes(tree, adaptor, instanceOf);
            this.nodeNumber = 0;
            this.ToDOTDefineEdges(tree, adaptor, instanceOf);
            return instanceOf;
        }

        protected void ToDOTDefineEdges(object tree, ITreeAdaptor adaptor, Antlr.StringTemplate.StringTemplate treeST)
        {
            if (tree != null)
            {
                int childCount = adaptor.GetChildCount(tree);
                if (childCount != 0)
                {
                    string val = "n" + this.GetNodeNumber(tree);
                    string nodeText = adaptor.GetNodeText(tree);
                    for (int i = 0; i < childCount; i++)
                    {
                        object child = adaptor.GetChild(tree, i);
                        string str3 = adaptor.GetNodeText(child);
                        string str4 = "n" + this.GetNodeNumber(child);
                        Antlr.StringTemplate.StringTemplate instanceOf = _edgeST.GetInstanceOf();
                        instanceOf.SetAttribute("parent", val);
                        instanceOf.SetAttribute("child", str4);
                        instanceOf.SetAttribute("parentText", nodeText);
                        instanceOf.SetAttribute("childText", str3);
                        treeST.SetAttribute("edges", instanceOf);
                        this.ToDOTDefineEdges(child, adaptor, treeST);
                    }
                }
            }
        }

        protected void ToDOTDefineNodes(object tree, ITreeAdaptor adaptor, Antlr.StringTemplate.StringTemplate treeST)
        {
            if (tree != null)
            {
                int childCount = adaptor.GetChildCount(tree);
                if (childCount != 0)
                {
                    Antlr.StringTemplate.StringTemplate nodeST = this.GetNodeST(adaptor, tree);
                    treeST.SetAttribute("nodes", nodeST);
                    for (int i = 0; i < childCount; i++)
                    {
                        object child = adaptor.GetChild(tree, i);
                        Antlr.StringTemplate.StringTemplate val = this.GetNodeST(adaptor, child);
                        treeST.SetAttribute("nodes", val);
                        this.ToDOTDefineNodes(child, adaptor, treeST);
                    }
                }
            }
        }
    }
}

