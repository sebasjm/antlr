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

/** A simple stream of integers used when all I care about is the char
 *  or token type sequence (such as interpretation).
 */
public interface IntStream {
	void consume();

	/** Get int at current input pointer + i ahead where i=1 is next int */
	int LA(int i);

	/** Tell the stream to start buffering if it hasn't already.  Return
     *  current input position, index().
     */
	int mark();

	/** Return the current input symbol index 0..n where n indicates the
     *  last symbol has been read.
     */
	int index();

	/** Reset the stream so that next call to index would return marker. */
	void rewind(int marker);

	/** Only makes sense for streams that buffer everything up probably, but
	 *  might be useful to display the entire stream or for testing.
	 */
	int size();
}
