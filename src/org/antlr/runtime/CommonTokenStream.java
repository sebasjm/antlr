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

import org.antlr.tool.Grammar;

import java.util.List;
import java.util.ArrayList;

/** The most common stream of tokens is one where every token is buffered up
 *  and tokens are prefiltered for a certain channel (the parser will only
 *  see these tokens and cannot change the filter channel number during the
 *  parse).
 *
 *  TODO: how to access the full token stream?  How to track all tokens matched per rule?
 */
public class CommonTokenStream implements TokenStream {
    protected TokenSource tokenSource;

	/** Record every single token pulled from the source so we can reproduce
	 *  chunks of it later.
	 */
	protected List tokens;

	/** For efficiently, prefilter the list of tokens to get only those on a
	 *  specific channel.  (seems to cost a list to track this extra array;
	 *  like 10% tokenizing a 5000 line java program w/o parsing).
	 */
	protected List filteredTokens;

	/** Skip tokens on any channel but this one; this is how we skip whitespace... */
	protected int channel = Token.DEFAULT_CHANNEL;
    protected int p = 0;

    public CommonTokenStream(TokenSource tokenSource) {
		tokens = new ArrayList(500);
		filteredTokens = new ArrayList(500);
        this.tokenSource = tokenSource;
		int index = 0;
        // suck in all the input tokens
        Token t = tokenSource.nextToken();
        while ( t!=null && t.getType()!=CharStream.EOF ) {
			t.setTokenIndex(index);
            tokens.add(t);
			if ( t.getChannel()==channel ) {
				filteredTokens.add(t);
			}
            t = tokenSource.nextToken();
			index++;
        }
    }

	/** Move the input pointer to the next incoming token.  The stream
	 *  must become active with lookahead(1) available.  consume() simply
	 *  moves the input pointer so that lookahead(1) points at the next
	 *  input symbol. Consume at least one token.
	 *
	 *  Walk past any token not on the channel the parser is listening to.
	 */
	public void consume() {
		if ( p<filteredTokens.size() ) {
            p++;
        }
    }

	public void tuneToChannel(int channel) {
		this.channel = channel;
	}

	/** Get the ith token from the current position 1..n where i=1 is the
	 *  first symbol of lookahead.
	 */
	public Token LT(int i) {
        //System.out.println("LT("+i+")="+LT(p, i));
        return LT(p, i);
    }

	/** Get Token at current input marker + i ahead where i=1 is next Token.
	 *  This is primarily used for evaluating semantic predicates which
	 *  must evaluate relative to their original position not in the input
	 *  stream; predicate hoisting can make them execute much further along,
	 *  however, as we check syntax first and then semantic predicates.
	 *
	 *  int m = input.mark();
	 *  Token atom = input.lookahead(1);
	 *  input.next();
	 *  input.next();
	 *  ...
	 *  input.next();
	 *  assertTrue(atom==input.lookahead(m, 1));
	 */
    public Token LT(int marker, int i) {
        if ( marker+i-1 >= filteredTokens.size() ) {
            return Token.EOFToken;
        }
        return (Token)filteredTokens.get(marker+i-1);
    }

    public int LA(int i) {
        return LT(i).getType();
    }

    public int LA(int marker, int i) {
        return LT(marker, i).getType();
    }

    public int mark() {
        return index();
    }

	public int size() {
		return filteredTokens.size();
	}

	public int getFilteredSize() {
		return filteredTokens.size();
	}

    public int index() {
        return p;
    }

    public void rewind(int marker) {
        p = marker;
    }

	public TokenSource getTokenSource() {
		return tokenSource;
	}

	public String getSourceName() {
		return tokenSource.getCharStream().getSourceName();
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < tokens.size(); i++) {
			Token t = (Token) tokens.get(i);
			if ( i>0 ) {
				buf.append(' ');
			}
			buf.append(t.toString(tokenSource.getCharStream()));
		}
		return buf.toString();
	}
}
