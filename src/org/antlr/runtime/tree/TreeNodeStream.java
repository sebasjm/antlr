/*
[The "BSD licence"]
Copyright (c) 2005 Terence Parr
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

/** A stream of tree nodes, accessing nodes from a tree of some kind */
public interface TreeNodeStream extends IntStream {
	/** Get tree node at current input pointer + i ahead where i=1 is next node.
	 *  i<0 indicates nodes in the past.  So -1 is previous node and -2 is
	 *  two nodes ago. LT(0) is undefined.  For i>=n, return null.
	 *  Return null for LT(0) and any index that results in an absolute address
	 *  that is negative.
	 *
	 *  This is analogus to the LT() method of the TokenStream, but this
	 *  returns a tree node instead of a token.  Makes code gen identical
	 *  for both parser and tree grammars. :)
	 */
	public Object LT(int k);

	/** Get a node at an absolute index i; 0..n-1. */
	public Object get(int i);

	/** Where is this stream pulling nodes from?  This is not the name, but
	 *  the object that provides node objects.
	 *
	 *  TODO: do we really need this?
	 */
	public Object getTreeSource();
}

