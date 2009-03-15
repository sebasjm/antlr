package org.antlr.misc;

import java.util.*;

public class Graph {

    public static class Node {
        Object payload;
        List<Node> edges; // depends on which nodes?

        public Node(Object payload) { this.payload = payload; }

        public void addEdge(Node n) {
            if ( edges==null ) edges = new ArrayList<Node>();
            if ( !edges.contains(n) ) edges.add(n);
        }

        public String toString() { return payload.toString(); }
    }

    /** Map from node payload to node containing it */
    protected Map<Object,Node> nodes = new HashMap<Object,Node>();

    public void addEdge(Object a, Object b) {
        //System.out.println("add edge "+a+" to "+b);
        Node a_node = getNode(a);
        Node b_node = getNode(b);
        a_node.addEdge(b_node);
    }

    protected Node getNode(Object a) {
        Node existing = nodes.get(a);
        if ( existing!=null ) return existing;
        Node n = new Node(a);
        nodes.put(a, n);
        return n;
    }

    /** DFS-based topological sort.  A valid sort is the reverse of
     *  the post-order DFA traversal.  Amazingly simple but true.
     *  For sorting, I'm not following convention here since ANTLR
     *  needs the opposite.  Here's what I assume for sorting:
     *
     *    If there exists an edge u -> v then u depends on v and v
     *    must happen before u.
     *
     *  So if this gives nonreversed postorder traversal, I get the order
     *  I want.
     */
    public List<Object> sort() {
        Set<Node> visited = new HashSet<Node>();
        ArrayList<Object> sorted = new ArrayList<Object>();
        while ( visited.size() < nodes.size() ) {
            // pick any unvisited node, n
            Node n = null;
            for (Iterator it = nodes.values().iterator(); it.hasNext();) {
                n = (Node)it.next();
                if ( !visited.contains(n) ) break;
            }
            DFS(n, visited, sorted);
        }
        return sorted;
    }

    public void DFS(Node n, Set<Node> visited, ArrayList<Object> sorted) {
        if ( visited.contains(n) ) return;
        visited.add(n);
        if ( n.edges!=null ) {
            for (Iterator it = n.edges.iterator(); it.hasNext();) {
                Node target = (Node) it.next();
                DFS(target, visited, sorted);
            }
        }
        sorted.add(n.payload);
    }
}