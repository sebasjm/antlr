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
import java.util.Map;
import java.util.HashMap;

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
	//protected List filteredTokens;

	/** Map<tokentype, channel> to override some Tokens' channel numbers */
	protected Map channelOverrideMap;

	/** Skip tokens on any channel but this one; this is how we skip whitespace... */
	protected int channel = Token.DEFAULT_CHANNEL;
    protected int p = 0;

	public CommonTokenStream(TokenSource tokenSource) {
		this.tokenSource = tokenSource;
		fillBuffer();
	}

	public CommonTokenStream(TokenSource tokenSource, int channel) {
		this.tokenSource = tokenSource;
		this.channel = channel;
		fillBuffer();
	}

	/** Load all tokens from the token source and put in tokens.
	 */
	protected void fillBuffer() {
		tokens = new ArrayList(500);
		int index = 0;
		Token t = tokenSource.nextToken();
		while ( t!=null && t.getType()!=CharStream.EOF ) {
			t.setTokenIndex(index);
			tokens.add(t);
			// is there a channel override for token type?
			if ( channelOverrideMap!=null ) {
				Integer channelI = (Integer)
					channelOverrideMap.get(new Integer(t.getType()));
				if ( channelI!=null ) {
					t.setChannel(channelI.intValue());
				}
			}
			t = tokenSource.nextToken();
			index++;
		}
		// leave p pointing at first token on channel
		p = 0;
		p = skipOffTokenChannels(p);
		/*
		filteredTokens = new ArrayList(500);
		int index = 0;
        // suck in all the input tokens
        Token t = tokenSource.nextToken();
        while ( t!=null && t.getType()!=CharStream.EOF ) {
			t.setTokenIndex(index);
            tokens.add(t);
			// is there a channel override for token type?
			if ( channelOverrideMap!=null ) {
				Integer channelI = (Integer)
					channelOverrideMap.get(new Integer(t.getType()));
				if ( channelI!=null ) {
					t.setChannel(channelI.intValue());
				}
			}
			// ignore tokens on different channel
			if ( t.getChannel()==channel ) {
				filteredTokens.add(t);
			}
            t = tokenSource.nextToken();
			index++;
        }
		*/
    }

	/** Move the input pointer to the next incoming token.  The stream
	 *  must become active with LT(1) available.  consume() simply
	 *  moves the input pointer so that LT(1) points at the next
	 *  input symbol. Consume at least one token.
	 *
	 *  Walk past any token not on the channel the parser is listening to.
	 */
	public void consume() {
		if ( p<tokens.size() ) {
            p++;
			p = skipOffTokenChannels(p); // leave p on valid token
        }
    }

	/** Given a starting index, return the index of the first on-channel
	 *  token.
	 */
	protected int skipOffTokenChannels(int i) {
		int n = tokens.size();
		while ( i<n && ((Token)tokens.get(i)).getChannel()!=channel ) {
			i++;
		}
		return i;
	}

	protected int skipOffTokenChannelsReverse(int i) {
		while ( i>=0 && ((Token)tokens.get(i)).getChannel()!=channel ) {
			i--;
		}
		return i;
	}

	/** A simple filter mechanism whereby you can tell this token stream
	 *  to force all tokens of type ttype to be on channel.  For example,
	 *  when interpreting, we cannot exec actions so we need to tell
	 *  the stream to force all WS and NEWLINE to be a different, ignored
	 *  channel.
	 */
	public void setTokenTypeChannel(int ttype, int channel) {
		if ( channelOverrideMap==null ) {
			channelOverrideMap = new HashMap();
		}
        channelOverrideMap.put(new Integer(ttype), new Integer(channel));
	}

	/** Get the ith token from the current position 1..n where k=1 is the
	 *  first symbol of lookahead.
	 */
	public Token LT(int k) {
		/*
		if ( tokens==null ) {
			fillBuffer();
		}
		*/
		if ( k==0 ) {
			return null;
		}
		if ( k<0 ) {
			return LB(-k);
		}
		//System.out.print("LT(p="+p+","+k+")=");
		if ( (p+k-1) >= tokens.size() ) {
			return Token.EOFToken;
		}
		//System.out.println(tokens.get(p+k-1));
		int i = p;
		int n = 1;
		// find k good tokens
		while ( n<k ) {
			// skip off-channel tokens
			i = skipOffTokenChannels(i+1); // leave p on valid token
			n++;
		}
        return (Token)tokens.get(i);
    }

	/** Look backwards k tokens on-channel tokens */
	protected Token LB(int k) {
		/*
		if ( tokens==null ) {
			fillBuffer();
		}
		*/
		if ( k==0 ) {
			return null;
		}
		if ( (p-k)<0 ) {
			return null;
		}

		int i = p;
		int n = 1;
		// find k good tokens looking backwards
		while ( n<=k ) {
			// skip off-channel tokens
			i = skipOffTokenChannelsReverse(i-1); // leave p on valid token
			n++;
		}
		if ( i<0 ) {
			return null;
		}
		return (Token)tokens.get(i);
	}

	/** Return absolute token i; ignore which channel the tokens are on;
	 *  that is, count all tokens not just on-channel tokens.
	 */
	public Token get(int i) {
		return (Token)tokens.get(i);
	}

	/** Get Token at current input marker + i ahead where i=1 is next Token.
	 *  This is primarily used for evaluating semantic predicates which
	 *  must evaluate relative to their original position not in the input
	 *  stream; predicate hoisting can make them execute much further along,
	 *  however, as we check syntax first and then semantic predicates.
	 *
	 *  int m = input.mark();
	 *  Token atom = input.LT(1);
	 *  input.consume();
	 *  input.consume();
	 *  ...
	 *  input.consume();
	 *  assertTrue(atom==input.LT(m, 1));
	 */
    /*
	public Token LT(int marker, int i) {
		if ( filteredTokens==null ) {
			fillBuffer();
		}
        if ( marker+i-1 >= filteredTokens.size() ) {
            return Token.EOFToken;
        }
        return (Token)filteredTokens.get(marker+i-1);
    }
	*/

    public int LA(int i) {
        return LT(i).getType();
    }

    /*
	public int LA(int marker, int i) {
        return LT(marker, i).getType();
    }
	*/

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

	public TokenSource getTokenSource() {
		return tokenSource;
	}

	public String toString() {
		/*
		if ( tokens==null ) {
			fillBuffer();
		}
		*/
 		StringBuffer buf = new StringBuffer();
		for (int i = 0; tokens!=null && i < tokens.size(); i++) {
			Token t = (Token)tokens.get(i);
			if ( i>0 ) {
				buf.append(' ');
			}
			buf.append(t.toString());
		}
		return buf.toString();
	}
}
