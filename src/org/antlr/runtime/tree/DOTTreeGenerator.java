/*
 [The "BSD licence"]
 Copyright (c) 2005-2006 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.antlr.runtime.tree;

import org.antlr.stringtemplate.StringTemplate;

/** A utility class to generate DOT diagrams (graphviz) from
 *  arbitrary trees.  You can pass in your own templates and
 *  can pass in any kind of tree or use Tree interface method.
 *  I wanted this separator so that you don't have to include
 *  ST just to use the org.antlr.runtime.tree.* package.
 *  This is a set of non-static methods so you can subclass
 *  to override.  For example, here is an invocation:
 *
 *      CharStream input = new ANTLRInputStream(System.in);
 *      TLexer lex = new TLexer(input);
 *      CommonTokenStream tokens = new CommonTokenStream(lex);
 *      TParser parser = new TParser(tokens);
 *      TParser.e_return r = parser.e();
 *      Tree t = (Tree)r.tree;
 *      System.out.println(t.toStringTree());
 *      DOTTreeGenerator gen = new DOTTreeGenerator();
 *      StringTemplate st = gen.toDOT(t);
 *      System.out.println(st);
 */
public class DOTTreeGenerator {

    public static StringTemplate _treeST =
        new StringTemplate(
            "digraph {\n" +
            "  node [shape=plaintext, fixedsize=true, fontsize=11, fontname=\"Courier\",\n" +
            "        width=.4, height=.2];\n" +
            "  edge [arrowsize=.7]\n" +
            "  $nodes$\n" + // only used when edges empty; single node tree
            "  $edges$\n" +
            "}\n");

    public static StringTemplate _edgeST =
            new StringTemplate("\"$parent$\" -> \"$child$\"\n");

    public StringTemplate toDOT(Object tree,
                                TreeAdaptor adaptor,
                                StringTemplate _treeST,
                                StringTemplate _edgeST)
    {
        StringTemplate treeST = _treeST.getInstanceOf();
        toDOTTreeWork(tree, adaptor, treeST);
        if ( adaptor.getChildCount(tree)==0 ) {
            // single node, don't do edge.
            treeST.setAttribute("nodes", adaptor.getText(tree));
        }
        return treeST;
    }

    public StringTemplate toDOT(Object tree,
                                TreeAdaptor adaptor)
    {
        return toDOT(tree, adaptor, _treeST, _edgeST);
    }

    /** Generate DOT (graphviz) for a whole tree not just a node.
     *  For example, 3+4*5 should generate:
     *
     * digraph {
     *   node [shape=plaintext, fixedsize=true, fontsize=11, fontname="Courier",
     *         width=.4, height=.2];
     *   edge [arrowsize=.7]
     *   "+"->3
     *   "+"->"*"
     *   "*"->4
     *   "*"->5
     * }
     *
     * Return the ST not a string in case people want to alter.
     *
     * Takes a Tree interface object.
     */
    public StringTemplate toDOT(Tree tree) {
        return toDOT(tree, new CommonTreeAdaptor());
    }

    protected void toDOTTreeWork(Object tree,
                                 TreeAdaptor adaptor,
                                 StringTemplate treeST)
    {
        if ( tree==null ) {
            return;
        }
        int n = adaptor.getChildCount(tree);
        if ( n==0 ) {
            // must have already dumped as child from previous
            // invocation; do nothing
            return;
        }

        // for each child, do a parent -> child edge
        String parentText = adaptor.getText(tree);
        for (int i = 0; i < n; i++) {
            Object child = adaptor.getChild(tree, i);
            String childText = adaptor.getText(child);
            StringTemplate edgeST = _edgeST.getInstanceOf();
            edgeST.setAttribute("parent", parentText);
            edgeST.setAttribute("child", childText);
            treeST.setAttribute("edges", edgeST);
            toDOTTreeWork(child, adaptor, treeST);
        }
    }

}
