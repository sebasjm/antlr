package org.antlr.test;

import org.junit.Test;
import org.antlr.misc.Graph;

import java.util.List;

/** Test topo sort in GraphNode. */
public class TestTopologicalSort extends BaseTest {
    @Test
    public void testFairlyLargeGraph() throws Exception {
        Graph g = new Graph();
        g.addEdge("C", "F");
        g.addEdge("C", "G");
        g.addEdge("C", "A");
        g.addEdge("C", "B");
        g.addEdge("A", "D");
        g.addEdge("A", "E");
        g.addEdge("B", "E");
        g.addEdge("D", "E");
        g.addEdge("D", "F");
        g.addEdge("F", "H");
        g.addEdge("E", "F");

        String expecting = "[H, F, E, D, A, G, B, C]";
        List nodes = g.sort();
        String result = nodes.toString();
        assertEquals(expecting, result);
    }

    @Test
    public void testCyclicGraph() throws Exception {
        Graph g = new Graph();
        g.addEdge("A", "B");
        g.addEdge("B", "C");
        g.addEdge("C", "A");
        g.addEdge("C", "D");

        String expecting = "[D, C, B, A]";
        List nodes = g.sort();
        String result = nodes.toString();
        assertEquals(expecting, result);
    }

    @Test
    public void testRepeatedEdges() throws Exception {
        Graph g = new Graph();
        g.addEdge("A", "B");
        g.addEdge("B", "C");
        g.addEdge("A", "B"); // dup
        g.addEdge("C", "D");

        String expecting = "[D, C, B, A]";
        List nodes = g.sort();
        String result = nodes.toString();
        assertEquals(expecting, result);
    }

    @Test
    public void testSimpleTokenDependence() throws Exception {
        Graph g = new Graph();
        g.addEdge("Java.g", "MyJava.tokens"); // Java feeds off manual token file
        g.addEdge("Java.tokens", "Java.g");        
        g.addEdge("Def.g", "Java.tokens");    // walkers feed off generated tokens
        g.addEdge("Ref.g", "Java.tokens");

        String expecting = "[MyJava.tokens, Java.g, Java.tokens, Def.g, Ref.g]";
        List nodes = g.sort();
        String result = nodes.toString();
        assertEquals(expecting, result);
    }

    @Test
    public void testParserLexerCombo() throws Exception {
        Graph g = new Graph();
        g.addEdge("JavaLexer.tokens", "JavaLexer.g");
        g.addEdge("JavaParser.g", "JavaLexer.tokens");
        g.addEdge("Def.g", "JavaLexer.tokens");
        g.addEdge("Ref.g", "JavaLexer.tokens");

        String expecting = "[JavaLexer.g, JavaLexer.tokens, JavaParser.g, Def.g, Ref.g]";
        List nodes = g.sort();
        String result = nodes.toString();
        assertEquals(expecting, result);
    }
}
