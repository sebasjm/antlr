package org.antlr.test;

import org.antlr.test.unit.TestSuite;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.CommonToken;

public class TestTreeNodeStream extends TestSuite {
	public void testSingleNode() throws Exception {
		Tree t = new CommonTree(new CommonToken(101));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(t);
		String expecting = " 101";
		String found = stream.toNodesOnlyString();
		assertEqual(found, expecting);

		expecting = " 101";
		found = stream.toString();
		assertEqual(found, expecting);
	}

	public void test4Nodes() throws Exception {
		Tree t = new CommonTree(new CommonToken(101));
		t.addChild(new CommonTree(new CommonToken(102)));
		t.getChild(0).addChild(new CommonTree(new CommonToken(103)));
		t.addChild(new CommonTree(new CommonToken(104)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(t);
		String expecting = " 101 102 103 104";
		String found = stream.toNodesOnlyString();
		assertEqual(found, expecting);

		expecting = " 101 2 102 2 103 3 104 3";
		found = stream.toString();
		assertEqual(found, expecting);
	}

	public void testList() throws Exception {
		Tree root = new CommonTree((Token)null);

		Tree t = new CommonTree(new CommonToken(101));
		t.addChild(new CommonTree(new CommonToken(102)));
		t.getChild(0).addChild(new CommonTree(new CommonToken(103)));
		t.addChild(new CommonTree(new CommonToken(104)));

		Tree u = new CommonTree(new CommonToken(105));

		root.addChild(t);
		root.addChild(u);

		CommonTreeNodeStream stream = new CommonTreeNodeStream(root);
		String expecting = " 101 102 103 104 105";
		String found = stream.toNodesOnlyString();
		assertEqual(found, expecting);

		expecting = " 101 2 102 2 103 3 104 3 105";
		found = stream.toString();
		assertEqual(found, expecting);
	}

	public void testFlatList() throws Exception {
		Tree root = new CommonTree((Token)null);

		root.addChild(new CommonTree(new CommonToken(101)));
		root.addChild(new CommonTree(new CommonToken(102)));
		root.addChild(new CommonTree(new CommonToken(103)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(root);
		String expecting = " 101 102 103";
		String found = stream.toNodesOnlyString();
		assertEqual(found, expecting);

		expecting = " 101 102 103";
		found = stream.toString();
		assertEqual(found, expecting);
	}

	public void testListWithOneNode() throws Exception {
		Tree root = new CommonTree((Token)null);

		root.addChild(new CommonTree(new CommonToken(101)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(root);
		String expecting = " 101";
		String found = stream.toNodesOnlyString();
		assertEqual(found, expecting);

		expecting = " 101";
		found = stream.toString();
		assertEqual(found, expecting);
	}

	public void testAoverB() throws Exception {
		Tree t = new CommonTree(new CommonToken(101));
		t.addChild(new CommonTree(new CommonToken(102)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(t);
		String expecting = " 101 102";
		String found = stream.toNodesOnlyString();
		assertEqual(found, expecting);

		expecting = " 101 2 102 3";
		found = stream.toString();
		assertEqual(found, expecting);
	}

}
