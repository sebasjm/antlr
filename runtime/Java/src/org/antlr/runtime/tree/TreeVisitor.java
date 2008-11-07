package org.antlr.runtime.tree;

/** */
public class TreeVisitor {
    protected TreeAdaptor adaptor;
    
    public TreeVisitor(TreeAdaptor adaptor) {
        this.adaptor = adaptor;
    }
    public TreeVisitor() { this(new CommonTreeAdaptor()); }
    
    /** Visit every node in tree t and trigger an action for each node
     *  before/after having visited all of its children.  Bottom up walk.
     *  Execute both actions even if t has no children.
     */
    public Object visit(Object t, TreeVisitorAction action) {
        int n = adaptor.getChildCount(t);
        boolean isNil = adaptor.isNil(t);
        if ( action!=null && !isNil ) {
            t = action.pre(t); // if rewritten, walk children of new t
        }
        for (int i=0; i<n; i++) {
            Object child = adaptor.getChild(t, i);
            visit(child, action);
        }
        if ( action!=null && !isNil ) t = action.post(t);
        return t;
    }
}
