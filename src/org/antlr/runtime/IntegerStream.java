/*
 [The "BSD licence"]
 Copyright (c) 2004 Terence Parr
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
package org.antlr.runtime;

/** A source of characters or token types for an ANTLR recognizer */
public interface IntegerStream {
    public static final int EOF = -1;

    public void consume();

    /** Get int at current input pointer + i ahead where i=1 is next int */
    public int LA(int i);

    /** Get int at current input marker + i ahead where i=1 is next int.
     *  This is primarily used for evaluating semantic predicates which
     *  must evaluate relative to their original position not in the input
     *  stream; predicate hoisting can make them execute much further along,
     *  however, as we check syntax first and then semantic predicates.
     *
     *  int m = input.mark();
     *  int atom = input.LA(1);
     *  input.next();
     *  input.next();
     *  ...
     *  input.next();
     *  assertTrue(atom==input.LA(m, 1));
     */
    public int LA(int marker, int i);

    /** Tell the stream to start buffering if it hasn't already.  Return
     *  current input position, index().
     */
    public int mark();

    /** Return the current input symbol index 0..n where n indicates the
     *  last symbol has been read.
     */
    public int index();

    public void rewind(int marker);
}
