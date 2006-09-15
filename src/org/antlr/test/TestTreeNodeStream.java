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
package org.antlr.test;

import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.CommonToken;

/** Test the tree node stream. */
public class TestTreeNodeStream extends BaseTest {
	public void testSingleNode() throws Exception {
		Tree t = new CommonTree(new CommonToken(101));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(t);
		String expecting = " 101";
		String found = stream.toNodesOnlyString();
		assertEquals(expecting, found);

		expecting = " 101";
		found = stream.toString();
		assertEquals(expecting, found);
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
		assertEquals(expecting, found);

		expecting = " 101 2 102 2 103 3 104 3";
		found = stream.toString();
		assertEquals(expecting, found);
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
		assertEquals(expecting, found);

		expecting = " 101 2 102 2 103 3 104 3 105";
		found = stream.toString();
		assertEquals(expecting, found);
	}

	public void testFlatList() throws Exception {
		Tree root = new CommonTree((Token)null);

		root.addChild(new CommonTree(new CommonToken(101)));
		root.addChild(new CommonTree(new CommonToken(102)));
		root.addChild(new CommonTree(new CommonToken(103)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(root);
		String expecting = " 101 102 103";
		String found = stream.toNodesOnlyString();
		assertEquals(expecting, found);

		expecting = " 101 102 103";
		found = stream.toString();
		assertEquals(expecting, found);
	}

	public void testListWithOneNode() throws Exception {
		Tree root = new CommonTree((Token)null);

		root.addChild(new CommonTree(new CommonToken(101)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(root);
		String expecting = " 101";
		String found = stream.toNodesOnlyString();
		assertEquals(expecting, found);

		expecting = " 101";
		found = stream.toString();
		assertEquals(expecting, found);
	}

	public void testAoverB() throws Exception {
		Tree t = new CommonTree(new CommonToken(101));
		t.addChild(new CommonTree(new CommonToken(102)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(t);
		String expecting = " 101 102";
		String found = stream.toNodesOnlyString();
		assertEquals(expecting, found);

		expecting = " 101 2 102 3";
		found = stream.toString();
		assertEquals(expecting, found);
	}

	public void testLT() throws Exception {
		// ^(101 ^(102 103) 104)
		Tree t = new CommonTree(new CommonToken(101));
		t.addChild(new CommonTree(new CommonToken(102)));
		t.getChild(0).addChild(new CommonTree(new CommonToken(103)));
		t.addChild(new CommonTree(new CommonToken(104)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(t);
		assertEquals(101, ((Tree)stream.LT(1)).getType());
		assertEquals(Token.DOWN, ((Tree)stream.LT(2)).getType());
		assertEquals(102, ((Tree)stream.LT(3)).getType());
		assertEquals(Token.DOWN, ((Tree)stream.LT(4)).getType());
		assertEquals(103, ((Tree)stream.LT(5)).getType());
		assertEquals(Token.UP, ((Tree)stream.LT(6)).getType());
		assertEquals(104, ((Tree)stream.LT(7)).getType());
		assertEquals(Token.UP, ((Tree)stream.LT(8)).getType());
		assertEquals(Token.EOF, ((Tree)stream.LT(9)).getType());
		// check way ahead
		assertEquals(Token.EOF, ((Tree)stream.LT(100)).getType());
	}

	public void testMarkRewindEntire() throws Exception {
		// ^(101 ^(102 103 ^(106 107) ) 104 105)
		// stream has 7 real + 6 nav nodes
		// Sequence of types: 101 DN 102 DN 103 106 DN 107 UP UP 104 105 UP EOF
		Tree r0 = new CommonTree(new CommonToken(101));
		Tree r1 = new CommonTree(new CommonToken(102));
		r0.addChild(r1);
		r1.addChild(new CommonTree(new CommonToken(103)));
		Tree r2 = new CommonTree(new CommonToken(106));
		r2.addChild(new CommonTree(new CommonToken(107)));
		r1.addChild(r2);
		r0.addChild(new CommonTree(new CommonToken(104)));
		r0.addChild(new CommonTree(new CommonToken(105)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(r0);
		int m = stream.mark(); // MARK
		for (int k=1; k<=13; k++) { // consume til end
			stream.LT(1);
			stream.consume();
		}
		assertEquals(Token.EOF, ((Tree)stream.LT(1)).getType());
		assertEquals(Token.UP, ((Tree)stream.LT(-1)).getType());
		stream.rewind(m);      // REWIND

		// consume til end again :)
		for (int k=1; k<=13; k++) { // consume til end
			stream.LT(1);
			stream.consume();
		}
		assertEquals(Token.EOF, ((Tree)stream.LT(1)).getType());
		assertEquals(Token.UP, ((Tree)stream.LT(-1)).getType());
	}

	public void testMarkRewindInMiddle() throws Exception {
		// ^(101 ^(102 103 ^(106 107) ) 104 105)
		// stream has 7 real + 6 nav nodes
		// Sequence of types: 101 DN 102 DN 103 106 DN 107 UP UP 104 105 UP EOF
		Tree r0 = new CommonTree(new CommonToken(101));
		Tree r1 = new CommonTree(new CommonToken(102));
		r0.addChild(r1);
		r1.addChild(new CommonTree(new CommonToken(103)));
		Tree r2 = new CommonTree(new CommonToken(106));
		r2.addChild(new CommonTree(new CommonToken(107)));
		r1.addChild(r2);
		r0.addChild(new CommonTree(new CommonToken(104)));
		r0.addChild(new CommonTree(new CommonToken(105)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(r0);
		for (int k=1; k<=7; k++) { // consume til middle
			//System.out.println(((Tree)stream.LT(1)).getType());
			stream.consume();
		}
		assertEquals(107, ((Tree)stream.LT(1)).getType());
		int m = stream.mark(); // MARK
		stream.consume(); // consume 107
		stream.consume(); // consume UP
		stream.consume(); // consume UP
		stream.consume(); // consume 104
		stream.rewind(m);      // REWIND

		assertEquals(107, ((Tree)stream.LT(1)).getType());
		stream.consume();
		assertEquals(Token.UP, ((Tree)stream.LT(1)).getType());
		stream.consume();
		assertEquals(Token.UP, ((Tree)stream.LT(1)).getType());
		stream.consume();
		assertEquals(104, ((Tree)stream.LT(1)).getType());
		stream.consume();
		// now we're past rewind position
		assertEquals(105, ((Tree)stream.LT(1)).getType());
		stream.consume();
		assertEquals(Token.UP, ((Tree)stream.LT(1)).getType());
		stream.consume();
		assertEquals(Token.EOF, ((Tree)stream.LT(1)).getType());
		assertEquals(Token.UP, ((Tree)stream.LT(-1)).getType());
	}

	public void testMarkRewindNested() throws Exception {
		// ^(101 ^(102 103 ^(106 107) ) 104 105)
		// stream has 7 real + 6 nav nodes
		// Sequence of types: 101 DN 102 DN 103 106 DN 107 UP UP 104 105 UP EOF
		Tree r0 = new CommonTree(new CommonToken(101));
		Tree r1 = new CommonTree(new CommonToken(102));
		r0.addChild(r1);
		r1.addChild(new CommonTree(new CommonToken(103)));
		Tree r2 = new CommonTree(new CommonToken(106));
		r2.addChild(new CommonTree(new CommonToken(107)));
		r1.addChild(r2);
		r0.addChild(new CommonTree(new CommonToken(104)));
		r0.addChild(new CommonTree(new CommonToken(105)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(r0);
		int m = stream.mark(); // MARK at start
		stream.consume(); // consume 101
		stream.consume(); // consume DN
		int m2 = stream.mark(); // MARK on 102
		stream.consume(); // consume 102
		stream.consume(); // consume DN
		stream.consume(); // consume 103
		stream.consume(); // consume 106
		stream.rewind(m2);      // REWIND to 102
		assertEquals(102, ((Tree)stream.LT(1)).getType());
		stream.consume();
		assertEquals(Token.DOWN, ((Tree)stream.LT(1)).getType());
		stream.consume();
		// stop at 103 and rewind to start
		stream.rewind(m); // REWIND to 101
		assertEquals(101, ((Tree)stream.LT(1)).getType());
		stream.consume();
		assertEquals(Token.DOWN, ((Tree)stream.LT(1)).getType());
		stream.consume();
		assertEquals(102, ((Tree)stream.LT(1)).getType());
		stream.consume();
		assertEquals(Token.DOWN, ((Tree)stream.LT(1)).getType());
	}

	public void testSeek() throws Exception {
		// ^(101 ^(102 103 ^(106 107) ) 104 105)
		// stream has 7 real + 6 nav nodes
		// Sequence of types: 101 DN 102 DN 103 106 DN 107 UP UP 104 105 UP EOF
		Tree r0 = new CommonTree(new CommonToken(101));
		Tree r1 = new CommonTree(new CommonToken(102));
		r0.addChild(r1);
		r1.addChild(new CommonTree(new CommonToken(103)));
		Tree r2 = new CommonTree(new CommonToken(106));
		r2.addChild(new CommonTree(new CommonToken(107)));
		r1.addChild(r2);
		r0.addChild(new CommonTree(new CommonToken(104)));
		r0.addChild(new CommonTree(new CommonToken(105)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(r0);
		stream.consume(); // consume 101
		stream.consume(); // consume DN
		stream.consume(); // consume 102
		stream.seek(7);   // seek to 107
		assertEquals(107, ((Tree)stream.LT(1)).getType());
		stream.consume(); // consume 107
		stream.consume(); // consume UP
		stream.consume(); // consume UP
		assertEquals(104, ((Tree)stream.LT(1)).getType());
	}

	public void testSeekFromStart() throws Exception {
		// ^(101 ^(102 103 ^(106 107) ) 104 105)
		// stream has 7 real + 6 nav nodes
		// Sequence of types: 101 DN 102 DN 103 106 DN 107 UP UP 104 105 UP EOF
		Tree r0 = new CommonTree(new CommonToken(101));
		Tree r1 = new CommonTree(new CommonToken(102));
		r0.addChild(r1);
		r1.addChild(new CommonTree(new CommonToken(103)));
		Tree r2 = new CommonTree(new CommonToken(106));
		r2.addChild(new CommonTree(new CommonToken(107)));
		r1.addChild(r2);
		r0.addChild(new CommonTree(new CommonToken(104)));
		r0.addChild(new CommonTree(new CommonToken(105)));

		CommonTreeNodeStream stream = new CommonTreeNodeStream(r0);
		stream.seek(7);   // seek to 107
		assertEquals(107, ((Tree)stream.LT(1)).getType());
		stream.consume(); // consume 107
		stream.consume(); // consume UP
		stream.consume(); // consume UP
		assertEquals(104, ((Tree)stream.LT(1)).getType());
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
		assertEquals(expecting, found);

		expecting = buf2.toString();
		found = stream.toString();
		assertEquals(expecting, found);
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
			assertEquals(i, node.getType());
			stream.consume();
		}

		// now use LT to lookahead past end of buffer
		int remaining = CommonTreeNodeStream.INITIAL_LOOKAHEAD_BUFFER_SIZE-N;
		int wrapBy = 4; // wrap around by 4 nodes
		assertTrue("bad test code; wrapBy must be less than N", wrapBy<N);
		for (int i=1; i<=remaining+wrapBy; i++) { // wrap past end of buffer
			Tree node = (Tree)stream.LT(i); // look ahead to ith token
			assertEquals(N + i, node.getType());
		}
	}

}
