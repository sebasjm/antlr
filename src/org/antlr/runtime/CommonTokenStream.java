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

public class CommonTokenStream implements TokenStream {
    protected TokenSource input;
    protected List tokens;
	/** Skip tokens on any channel but this one; this is how we skip whitespace... */
	protected int channel = Lexer.DEFAULT_CHANNEL;
    protected int p = 0;

    public CommonTokenStream(TokenSource input) {
        tokens = new ArrayList();
        this.input = input;
        // suck in all the input tokens
        Token t = input.nextToken();
        while ( t!=null && t.getType()!=CharStream.EOF ) {
            tokens.add(t);
            t = input.nextToken();
        }
    }

    /** Move to the next token on our channel; consume at least one token. */
	public void consume() {
		if ( p<tokens.size() ) {
            p++;
			while ( p<tokens.size() &&
					((Token)tokens.get(p)).getChannel()!=channel )
			{
				p++;
			}
        }
    }

	public void tuneToChannel(int channel) {
		this.channel = channel;
	}

    public Token LT(int i) {
        //System.out.println("LT("+i+")="+LT(p, i));
        return LT(p, i);
    }

    public Token LT(int marker, int i) {
        if ( marker+i-1 >= tokens.size() ) {
            return Token.EOFToken;
        }
        return (Token)tokens.get(marker+i-1);
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
		return tokens.size();
	}
	
    public int index() {
        return p;
    }

    public void rewind(int marker) {
        p = marker;
    }
}
