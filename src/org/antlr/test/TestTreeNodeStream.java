package org.antlr.test;

import org.antlr.test.unit.TestSuite;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.CommonToken;

/** Test the tree node stream. */
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
		// ^(101 ^(102 103) 104)
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

	public void testBufferOverflow() throws Exception {
		StringBuffer buf = new StringBuffer();
		StringBuffer buf2 = new StringBuffer();
		// make ^(101 102 ... n)
		Tree t = new CommonTree(new CommonToken(101));
		buf.append(" 101");
		buf2.append(" 101");
		buf2.append(" ");
		buf2.append(Token.DOWN);
		for (int i=0; i<=CommonTreeNodeStream.INITIAL_LOOKAHEAD_BUFFER_SIZE+10; i++) {
			t.addChild(new CommonTree(new CommonToken(102+i)));
			buf.append(" ");
			buf.append(102+i);
			buf2.append(" ");
			buf2.append(102+i);
		}
		buf2.append(" ");
		buf2.append(Token.UP);

		CommonTreeNodeStream stream = new CommonTreeNodeStream(t);
		String expecting = buf.toString();
		String found = stream.toNodesOnlyString();
		assertEqual(found, expecting);

		expecting = buf2.toString();
		found = stream.toString();
		assertEqual(found, expecting);
	}

	/** Test what happens when tail hits the end of the buffer, but there
	 *  is more room left.  Specifically that would mean that head is not
	 *  at 0 but has advanced somewhere to the middle of the lookahead
	 *  buffer.
	 *
	 *  Use consume() to advance N nodes into lookahead.  Then use LT()
	 *  to load at least INITIAL_LOOKAHEAD_BUFFER_SIZE-N nodes so the
	 *  buffer has to wrap.
	 */
	public void testBufferWrap() throws Exception {
		int N = 10;
		// make tree with types: 1 2 ... INITIAL_LOOKAHEAD_BUFFER_SIZE+N
		Tree t = new CommonTree((Token)null);
		for (int i=0; i<CommonTreeNodeStream.INITIAL_LOOKAHEAD_BUFFER_SIZE+N; i++) {
			t.addChild(new CommonTree(new CommonToken(i+1)));
		}

		// move head to index N
		CommonTreeNodeStream stream = new CommonTreeNodeStream(t);
		for (int i=1; i<=N; i++) { // consume N
			Tree node = (Tree)stream.LT(1);
			assertEqual(node.getType(), i);
			stream.consume();
		}

		// now use LT to lookahead past end of buffer
		int remaining = CommonTreeNodeStream.INITIAL_LOOKAHEAD_BUFFER_SIZE-N;
		int wrapBy = 4; // wrap around by 4 nodes
		assertTrue(wrapBy<N, "bad test code; wrapBy must be less than N");
		for (int i=1; i<=remaining+wrapBy; i++) { // wrap past end of buffer
			Tree node = (Tree)stream.LT(i); // look ahead to ith token
			assertEqual(node.getType(), N+i);
		}
	}

}
