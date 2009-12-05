package org.antlr.runtime;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;

/** Buffer all input tokens but do on-demand fetching of new tokens from
 *  lexer. Useful when the parser or lexer has to set context/mode info before
 *  proper lexing of future tokens. The ST template parser needs this,
 *  for example, because it has to constantly flip back and forth between
 *  inside/output templates. E.g., <names:{hi, <it>}> has to parse names
 *  as part of an expression but "hi, <it>" as a nested template.
 *
 *  You can't use this stream if you pass whitespace or other off-channel
 *  tokens to the parser. The stream can't ignore off-channel tokens.
 *  (UnbufferedTokenStream is the same way.)
 *
 *  This is not a subclass of UnbufferedTokenStream because I don't want
 *  to confuse small moving window of tokens it uses for the full buffer.
 */
public class BufferedTokenStream implements TokenStream {
    protected TokenSource tokenSource;

    /** Record every single token pulled from the source so we can reproduce
     *  chunks of it later.  The buffer in LookaheadStream overlaps sometimes
     *  as its moving window moves through the input.  This list captures
     *  everything so we can access complete input text.
     */
    protected List<Token> tokens = new ArrayList<Token>(100);

    /** Track the last mark() call result value for use in rewind(). */
    protected int lastMarker;

    /** The index into the tokens list of the current token (next token
     *  to consume).  tokens[p] should be LT(1).  p=-1 indicates need
     *  to initialize with first token.  The ctor doesn't get a token.
     *  First call to LT(1) or whatever gets the first token and sets p=0;
     */
    protected int p = -1;

    public BufferedTokenStream() {;}

    public BufferedTokenStream(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }

    public TokenSource getTokenSource() { return tokenSource; }

    public int index() { return p; }

    public int mark() {
        if ( p == -1 ) setup();
		lastMarker = index();
		return lastMarker;
	}

	public void release(int marker) {
		// no resources to release
	}

    public void rewind(int marker) {
        seek(marker);
    }

    public void rewind() {
        seek(lastMarker);
    }

    public void reset() {
        p = 0;
        lastMarker = 0;
    }

    public void seek(int index) { p = index; }

    public int size() { return tokens.size(); }

    /** Move the input pointer to the next incoming token.  The stream
     *  must become active with LT(1) available.  consume() simply
     *  moves the input pointer so that LT(1) points at the next
     *  input symbol. Consume at least one token.
     *
     *  Walk past any token not on the channel the parser is listening to.
     */
    public void consume() {
        if ( p == -1 ) setup();
        p++;
        sync(p);
    }

    /** Make sure index i in tokens has a token. */
    protected void sync(int i) {
        int n = i - tokens.size() + 1; // how many more elements we need?
        if ( n > 0 ) fetch(n);
    }

    /** add n elements to buffer */
    protected void fetch(int n) {
        for (int i=1; i<=n; i++) {
            Token t = tokenSource.nextToken();
            t.setTokenIndex(tokens.size());
            tokens.add(t);
            if ( t.getType()==Token.EOF ) break;
        }
    }    

    public Token get(int i) {
        if ( i < 0 || i >= tokens.size() ) {
            throw new NoSuchElementException("token index "+i+" out of range 0.."+(tokens.size()-1));
        }
        return tokens.get(i);
    }

    public int LA(int i) { return LT(i).getType(); }

    protected Token LB(int k) {
        if ( (p-k)<0 ) return null;
        return tokens.get(p-k);
    }

    public Token LT(int k) {
        if ( p == -1 ) setup();
        if ( k==0 ) return null;
        if ( k < 0 ) return LB(-k);

        sync(p+k-1);
        if ( (p+k-1) >= tokens.size() ) {
            return Token.EOF_TOKEN;
        }
        return tokens.get(p+k-1);
    }

    protected void setup() { sync(0); p = 0; }

    /** Reset this token stream by setting its token source. */
    public void setTokenSource(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
        tokens.clear();
        p = -1;
    }
    
    public List getTokens() { return tokens; }

    public List getTokens(int start, int stop) {
        return getTokens(start, stop, (BitSet)null);
    }

    /** Given a start and stop index, return a List of all tokens in
     *  the token type BitSet.  Return null if no tokens were found.  This
     *  method looks at both on and off channel tokens.
     */
    public List getTokens(int start, int stop, BitSet types) {
        if ( p == -1 ) setup();
        if ( stop>=tokens.size() ) stop=tokens.size()-1;
        if ( start<0 ) start=0;
        if ( start>stop ) return null;

        // list = tokens[start:stop]:{Token t, t.getType() in types}
        List<Token> filteredTokens = new ArrayList<Token>();
        for (int i=start; i<=stop; i++) {
            Token t = tokens.get(i);
            if ( types==null || types.member(t.getType()) ) {
                filteredTokens.add(t);
            }
        }
        if ( filteredTokens.size()==0 ) {
            filteredTokens = null;
        }
        return filteredTokens;
    }

    public List getTokens(int start, int stop, List types) {
        return getTokens(start,stop,new BitSet(types));
    }

    public List getTokens(int start, int stop, int ttype) {
        return getTokens(start,stop,BitSet.of(ttype));
    }

    public String getSourceName() {	return tokenSource.getSourceName();	}

    public String toString() {
        if ( p == -1 ) setup();
        return toString(0, tokens.size()-1);
    }

    public String toString(int start, int stop) {
        if ( start<0 || stop<0 ) return null;
        if ( p == -1 ) setup();
        if ( stop>=tokens.size() ) stop = tokens.size()-1;
        StringBuffer buf = new StringBuffer();
        for (int i = start; i <= stop; i++) {
            Token t = tokens.get(i);
            if ( t.getType()==Token.EOF ) break;
            buf.append(t.getText());
        }
        return buf.toString();
    }

    public String toString(Token start, Token stop) {
        if ( start!=null && stop!=null ) {
            return toString(start.getTokenIndex(), stop.getTokenIndex());
        }
        return null;
    }
}
