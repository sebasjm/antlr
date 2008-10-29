package org.antlr.test;

import org.antlr.runtime.tree.*;

import java.util.Map;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/** Test the TreeParser.inContext() method */
public class TestTreeContext extends BaseTest {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "VEC", "ASSIGN", "PRINT",
        "PLUS", "MULT", "DOT", "ID", "INT", "WS", "'['", "','", "']'"
    };

    @Test public void testSimpleParent() {
        String tree = "(nil (ASSIGN ID[x] INT[3]) (PRINT (MULT ID[x] (VEC INT[1] INT[2] INT[3]))))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(nil (ASSIGN ID[x] INT[3]) (PRINT (MULT ID (VEC INT %x:INT INT))))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = true;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "VEC");
        assertEquals(expecting, found);
    }

    @Test public void testNoParent() {
        String tree = "(PRINT (MULT ID[x] (VEC INT[1] INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(%x:PRINT (MULT ID (VEC INT INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = false;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "VEC");
        assertEquals(expecting, found);
    }

    @Test public void testParentWithWildcard() {
        String tree = "(nil (ASSIGN ID[x] INT[3]) (PRINT (MULT ID[x] (VEC INT[1] INT[2] INT[3]))))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(nil (ASSIGN ID[x] INT[3]) (PRINT (MULT ID (VEC INT %x:INT INT))))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = true;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "VEC ...");
        assertEquals(expecting, found);
    }

    @Test public void testWildcardAtStartIgnored() {
        String tree = "(nil (ASSIGN ID[x] INT[3]) (PRINT (MULT ID[x] (VEC INT[1] INT[2] INT[3]))))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(nil (ASSIGN ID[x] INT[3]) (PRINT (MULT ID (VEC INT %x:INT INT))))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = true;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "...VEC");
        assertEquals(expecting, found);
    }

    @Test public void testWildcardInBetween() {
        String tree = "(nil (ASSIGN ID[x] INT[3]) (PRINT (MULT ID[x] (VEC INT[1] INT[2] INT[3]))))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(nil (ASSIGN ID[x] INT[3]) (PRINT (MULT ID (VEC INT %x:INT INT))))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = true;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "PRINT...VEC");
        assertEquals(expecting, found);
    }

    @Test public void testLotsOfWildcards() {
        String tree = "(nil (ASSIGN ID[x] INT[3]) (PRINT (MULT ID[x] (VEC INT[1] INT[2] INT[3]))))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(nil (ASSIGN ID[x] INT[3]) (PRINT (MULT ID (VEC INT %x:INT INT))))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = true;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "... PRINT ... VEC ...");
        assertEquals(expecting, found);
    }

    @Test public void testDeep() {
        String tree = "(PRINT (MULT ID[x] (VEC (MULT INT[9] INT[1]) INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(PRINT (MULT ID (VEC (MULT INT %x:INT) INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = true;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "VEC ...");
        assertEquals(expecting, found);
    }

    @Test public void testDeepAndFindRoot() {
        String tree = "(PRINT (MULT ID[x] (VEC (MULT INT[9] INT[1]) INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(PRINT (MULT ID (VEC (MULT INT %x:INT) INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = true;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "PRINT ...");
        assertEquals(expecting, found);
    }

    @Test public void testDeepAndFindRoot2() {
        String tree = "(PRINT (MULT ID[x] (VEC (MULT INT[9] INT[1]) INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(PRINT (MULT ID (VEC (MULT INT %x:INT) INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = true;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "PRINT ... VEC ...");
        assertEquals(expecting, found);
    }

    @Test public void testChain() {
        String tree = "(PRINT (MULT ID[x] (VEC (MULT INT[9] INT[1]) INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(PRINT (MULT ID (VEC (MULT INT %x:INT) INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = true;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "PRINT MULT VEC MULT");
        assertEquals(expecting, found);
    }

    // TEST INVALID CONTEXTS

    @Test public void testNotParent() {
        String tree = "(PRINT (MULT ID[x] (VEC (MULT INT[9] INT[1]) INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(PRINT (MULT ID (VEC (MULT INT %x:INT) INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = false;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "VEC");
        assertEquals(expecting, found);
    }

    @Test public void testMismatch() {
        String tree = "(PRINT (MULT ID[x] (VEC (MULT INT[9] INT[1]) INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(PRINT (MULT ID (VEC (MULT INT %x:INT) INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = false;
        // missing MULT
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "PRINT VEC MULT");
        assertEquals(expecting, found);
    }

    @Test public void testMismatch2() {
        String tree = "(PRINT (MULT ID[x] (VEC (MULT INT[9] INT[1]) INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(PRINT (MULT ID (VEC (MULT INT %x:INT) INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = false;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "PRINT VEC ...");
        assertEquals(expecting, found);
    }

    @Test public void testMismatch3() {
        String tree = "(PRINT (MULT ID[x] (VEC (MULT INT[9] INT[1]) INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(PRINT (MULT ID (VEC (MULT INT %x:INT) INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        boolean expecting = false;
        boolean found = TreeParser.inContext(adaptor, tokenNames, node, "VEC ... VEC MULT");
        assertEquals(expecting, found);
    }

    @Test public void testDoubleEtc() {
        String tree = "(PRINT (MULT ID[x] (VEC (MULT INT[9] INT[1]) INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(PRINT (MULT ID (VEC (MULT INT %x:INT) INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        String expecting = "invalid syntax: ... ...";
        String found = null;
        try {
            TreeParser.inContext(adaptor, tokenNames, node, "PRINT ... ... VEC");
        }
        catch (IllegalArgumentException iae) {
            found = iae.getMessage();
        }
        assertEquals(expecting, found);
    }

    @Test public void testDotDot() {
        String tree = "(PRINT (MULT ID[x] (VEC (MULT INT[9] INT[1]) INT[2] INT[3])))";
        TreeAdaptor adaptor = new CommonTreeAdaptor();
        TreeWizard wiz = new TreeWizard(adaptor, tokenNames);
        CommonTree t = (CommonTree)wiz.create(tree);

        Map labels = new HashMap();
        boolean valid =
            wiz.parse(t,
                      "(PRINT (MULT ID (VEC (MULT INT %x:INT) INT INT)))",
                      labels);
        assertTrue(valid);
        CommonTree node = (CommonTree)labels.get("x");

        String expecting = "invalid syntax: ..";
        String found = null;
        try {
            TreeParser.inContext(adaptor, tokenNames, node, "PRINT .. VEC");
        }
        catch (IllegalArgumentException iae) {
            found = iae.getMessage();
        }
        assertEquals(expecting, found);
    }
}
