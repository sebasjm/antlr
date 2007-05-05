"""ANTLR3 runtime package"""

# [The "BSD licence"]
# Copyright (c) 2005-2006 Terence Parr
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
# 1. Redistributions of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 3. The name of the author may not be used to endorse or promote products
#    derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
# IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
# OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
# IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
# INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
# NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
# THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

from antlr3.constants import DEFAULT_CHANNEL, EOF
from antlr3.tokens import EOF_TOKEN


class StringStream(object):
    def __init__(self, data):
  	# The data being scanned
        self.data = data

	# How many characters are actually in the buffer
        self.n = len(data)

 	# 0..n-1 index into string of next char
        self.p = 0

	# line number 1..n within the input
        self.line = 1

 	# The index of the character relative to the beginning of the
        # line 0..n-1
        self.charPositionInLine = 0

	# A list of CharStreamState objects that tracks the stream state
        # values line, charPositionInLine, and p that can change as you
        # move through the input stream.  Indexed from 0..markDepth-1.
        self._markers = [ ]


    def reset(self):
        """
        Reset the stream so that it's in the same state it was
        when the object was created *except* the data array is not
        touched.
        """
        
        self.p = 0
        self.line = 1
        self.charPositionInLine = 0
        self._markers = [ ]


    def consume(self):
        if self.p < self.n:
            self.charPositionInLine += 1
            if self.data[self.p] == '\n':
                self.line += 1
                self.charPositionInLine = 0

            self.p += 1


    def LA(self, i):
        if i == 0:
            return 0 # undefined

        if i < 0:
            i += 1 # e.g., translate LA(-1) to use offset i=0; then data[p+0-1]
            if self.p+i-1 < 0:
                return EOF # invalid; no char before first char

        if self.p+i-1 >= self.n:
            return EOF

        return self.data[self.p+i-1]

    LT = LA

    def index(self):
        """
        Return the current input symbol index 0..n where n indicates the
        last symbol has been read.  The index is the index of char to
        be returned from LA(1).
        """
        
        return self.p


    def size(self):
        return self.n


    def markDepth(self):
        return len(self._markers)
    markDepth = property(markDepth)


    def mark(self):
        state = (self.p, self.line, self.charPositionInLine)
        self._markers.append(state)

        return self.markDepth


    def rewind(self, marker=None):
        if marker is None:
            marker = self.markDepth
            
        p, line, charPositionInLine = self._markers[marker-1]

        self.seek(p)
        self.line = line
        self.charPositionInLine = charPositionInLine
        self.release(marker)


    def release(self, marker=None):
        if marker is None:
            marker = self.markDepth
            
        self._markers = self._markers[:marker-1]


    def seek(self, index):
        """
        consume() ahead until p==index; can't just set p=index as we must
        update line and charPositionInLine.
        """
        
        if index <= self.p:
            self.p = index # just jump; don't update stream state (line, ...)
            return

        # seek forward, consume until p hits index
        while self.p < index:
            self.consume()


    def substring(self, start, stop):
        return self.data[start:stop+1]


## class FileStream(object):
##     def __init__(self, file):
##         self.file = file
        
##         self.line = 0
##         self.charPositionInLine = 0

    
##     def consume(self):
##         raise NotImplementedError


##     def LA(self, i):
##         """
##         Get int at current input pointer + i ahead where i=1 is next int.
## 	Negative indexes are allowed.  LA(-1) is previous token (token
## 	just matched).  LA(-i) where i is before first token should
##         yield -1, invalid char / EOF.
##         """

##         raise NotImplementedError


##     def mark(self):
##         """
##         Tell the stream to start buffering if it hasn't already.  Return
##         current input position, index(), or some other marker so that
##         when passed to rewind() you get back to the same spot.
##         rewind(mark()) should not affect the input cursor.  The Lexer
##         track line/col info as well as input index so its markers are
##         not pure input indexes.  Same for tree node streams.
##         """
        
##         raise NotImplementedError
    
    
##     def index(self):
##         """
##         Return the current input symbol index 0..n where n indicates the
##         last symbol has been read.  The index is the symbol about to be
##         read not the most recently read symbol.
##         """
        
## 	raise NotImplementedError


##     def rewind(self, marker=None):
##         """
##         Reset the stream so that next call to index would return marker.
##         The marker will usually be index() but it doesn't have to be.  It's
##         just a marker to indicate what state the stream was in.  This is
##         essentially calling release() and seek().  If there are markers
##         created after this marker argument, this routine must unroll them
##         like a stack.  Assume the state the stream was in when this marker
##         was created.
##         """
        
##         raise NotImplementedError


##     def release(self, marker):
##         """
##         You may want to commit to a backtrack but don't want to force the
##         stream to keep bookkeeping objects around for a marker that is
##         no longer necessary.  This will have the same behavior as
##         rewind() except it releases resources without the backward seek.
##         This must throw away resources for all markers back to the marker
##         argument.  So if you're nested 5 levels of mark(), and then release(2)
##         you have to release resources for depths 2..5.
## 	"""

##         raise NotImplementedError


##     def seek(self, index):
## 	"""
##         Set the input cursor to the position indicated by index.  This is
##         normally used to seek ahead in the input stream.  No buffering is
##         required to do this unless you know your stream will use seek to
##         move backwards such as when backtracking.
	
##         This is different from rewind in its multi-directional
##         requirement and in that its argument is strictly an input cursor (index).
	
##         For char streams, seeking forward must update the stream state such
##         as line number.  For seeking backwards, you will be presumably
##         backtracking using the mark/rewind mechanism that restores state and
##         so this method does not need to update state when seeking backwards.
	
##         Currently, this method is only used for efficient backtracking using
##         memoization, but in the future it may be used for incremental parsing.
	
##         The index is 0..n-1.
##         """

##         raise NotImplementedError


##     def size(self):
## 	"""
##         Only makes sense for streams that buffer everything up probably, but
##         might be useful to display the entire stream or for testing.  This
##         value includes a single EOF.
##         """

##         raise NotImplementedError
    

##     def substring(self, start, stop):
##         """
##         For infinite streams, you don't need this; primarily I'm providing
##         a useful interface for action code.  Just make sure actions don't
##         use this on streams that don't support it.
##         """
        
##         raise NotImplementedError


##     def LT(self, i):
##         """
##         Get the ith character of lookahead.  This is the same usually as
##         LA(i).  This will be used for labels in the generated
##         lexer code.  I'd prefer to return a char here type-wise, but it's
##         probably better to be 32-bit clean and be consistent with LA.
##         """

##         raise NotImplementedError
        

## class StringStream(FileStream):
##     def __init__(self, text):
##         FileStream.__init__(self, StringIO(text))

        
class CommonTokenStream(object):
    """
    The most common stream of tokens is one where every token is buffered up
    and tokens are prefiltered for a certain channel (the parser will only
    see these tokens and cannot change the filter channel number during the
    parse).

    TODO: how to access the full token stream?  How to track all tokens matched per rule?
    """

    def __init__(self, tokenSource, channel=DEFAULT_CHANNEL):
        self.tokenSource = tokenSource

	# Record every single token pulled from the source so we can reproduce
        # chunks of it later.
        self.tokens = []

	# Map<tokentype, channel> to override some Tokens' channel numbers
        self.channelOverrideMap = {}

	# Set<tokentype>; discard any tokens with this type
        self.discardSet = set()

	# Skip tokens on any channel but this one; this is how we skip whitespace...
        self.channel = channel

	# By default, track all incoming tokens
        self.discardOffChannelTokens = False

	# The index into the tokens list of the current token (next token
        # to consume).  p==-1 indicates that the tokens list is empty
        self.p = -1

        # Remember last marked position
        self.lastMarker = None
        

    def setTokenSource(self, tokenSource):
        """Reset this token stream by setting its token source."""
        
        self.tokenSource = tokenSource
        self.p = -1
        self.channel = DEFAULT_CHANNEL


    def fillBuffer(self):
        """
        Load all tokens from the token source and put in tokens.
	This is done upon first LT request because you might want to
        set some token type / channel overrides before filling buffer.
        """
        

        index = 0
        t = self.tokenSource.nextToken()
        while t is not None and t.type != EOF:
            discard = False
            
            if self.discardSet is not None and t.type in self.discardSet:
                discard = True

            elif self.discardOffChannelTokens and t.channel != self.channel:
                discard = True

            # is there a channel override for token type?
            try:
                overrideChannel = self.channelOverrideMap[t.type]
                
            except KeyError:
                # no override for this type
                pass
            
            else:
                if overrideChannel == self.channel:
                    t.channel = overrideChannel
                else:
                    discard = True
            
            if not discard:
                t.index = index
                self.tokens.append(t)
                index += 1

            t = self.tokenSource.nextToken()
       
        # leave p pointing at first token on channel
        self.p = 0
        self.p = self.skipOffTokenChannels(self.p)


    def consume(self):
        """
        Move the input pointer to the next incoming token.  The stream
        must become active with LT(1) available.  consume() simply
        moves the input pointer so that LT(1) points at the next
        input symbol. Consume at least one token.

        Walk past any token not on the channel the parser is listening to.
        """
        
        if self.p < len(self.tokens):
            self.p += 1

            self.p = self.skipOffTokenChannels(self.p) # leave p on valid token


    def skipOffTokenChannels(self, i):
        """
        Given a starting index, return the index of the first on-channel
        token.
        """

        n = len(self.tokens)
        while i < n and self.tokens[i].channel != self.channel:
            i += 1

        return i


    def skipOffTokenChannelsReverse(self, i):
        while i >= 0 and self.tokens[i].channel != self.channel:
            i -= 1

        return i


## 	/** A simple filter mechanism whereby you can tell this token stream
## 	 *  to force all tokens of type ttype to be on channel.  For example,
## 	 *  when interpreting, we cannot exec actions so we need to tell
## 	 *  the stream to force all WS and NEWLINE to be a different, ignored
## 	 *  channel.
## 	 */
## 	public void setTokenTypeChannel(int ttype, int channel) {
## 		if ( channelOverrideMap==null ) {
## 			channelOverrideMap = new HashMap();
## 		}
##         channelOverrideMap.put(new Integer(ttype), new Integer(channel));
## 	}

    def discardTokenType(self, ttype):
        self.discardSet.add(ttype)


## 	public void discardOffChannelTokens(boolean discardOffChannelTokens) {
## 		this.discardOffChannelTokens = discardOffChannelTokens;
## 	}

## 	public List getTokens() {
## 		if ( p == -1 ) {
## 			fillBuffer();
## 		}
## 		return tokens;
## 	}

## 	public List getTokens(int start, int stop) {
## 		return getTokens(start, stop, (BitSet)null);
## 	}

## 	/** Given a start and stop index, return a List of all tokens in
## 	 *  the token type BitSet.  Return null if no tokens were found.  This
## 	 *  method looks at both on and off channel tokens.
## 	 */
## 	public List getTokens(int start, int stop, BitSet types) {
## 		if ( p == -1 ) {
## 			fillBuffer();
## 		}
## 		if ( stop>=tokens.size() ) {
## 			stop=tokens.size()-1;
## 		}
## 		if ( start<0 ) {
## 			start=0;
## 		}
## 		if ( start>stop ) {
## 			return null;
## 		}

## 		// list = tokens[start:stop]:{Token t, t.getType() in types}
## 		List filteredTokens = new ArrayList();
## 		for (int i=start; i<=stop; i++) {
## 			Token t = (Token)tokens.get(i);
## 			if ( types==null || types.member(t.getType()) ) {
## 				filteredTokens.add(t);
## 			}
## 		}
## 		if ( filteredTokens.size()==0 ) {
## 			filteredTokens = null;
## 		}
## 		return filteredTokens;
## 	}

## 	public List getTokens(int start, int stop, List types) {
## 		return getTokens(start,stop,new BitSet(types));
## 	}

## 	public List getTokens(int start, int stop, int ttype) {
## 		return getTokens(start,stop,BitSet.of(ttype));
## 	}

    def LT(self, k):
        """
        Get the ith token from the current position 1..n where k=1 is the
        first symbol of lookahead.
        """

        if self.p == -1:
            self.fillBuffer()

        if k == 0:
            return None

        if k < 0:
            return self.LB(-k)
                
        if self.p + k - 1 >= len(self.tokens):
            return EOF_TOKEN

        i = self.p
        n = 1
        # find k good tokens
        while n < k:
            # skip off-channel tokens
            i = self.skipOffTokenChannels(i+1) # leave p on valid token
            n += 1
        
        if i >= len(self.tokens):
            return EOF_TOKEN

        return self.tokens[i]


    def LB(self, k):
        """Look backwards k tokens on-channel tokens"""

        if self.p == -1:
            self.fillBuffer()

        if k == 0:
            return None

        if self.p - k < 0:
            return None

        i = self.p
        n = 1
        # find k good tokens looking backwards
        while n <= k:
            # skip off-channel tokens
            i = self.skipOffTokenChannelsReverse(i-1) # leave p on valid token
            n += 1

        if i < 0:
            return None
            
        return self.tokens[i]


## 	/** Return absolute token i; ignore which channel the tokens are on;
## 	 *  that is, count all tokens not just on-channel tokens.
## 	 */
## 	public Token get(int i) {
## 		return (Token)tokens.get(i);
## 	}

    def LA(self, i):
        return self.LT(i).type


    def mark(self):
        self.lastMarker = self.index()
        return self.lastMarker
    

    def release(self, marker):
        # no resources to release
        pass
    

## 	public int size() {
## 		return tokens.size();
## 	}

    def index(self):
        return self.p


    def rewind(self, marker=None):
        if marker is None:
            marker = self.lastMarker
            
        self.seek(marker)


    def seek(self, index):
        self.p = index


## 	public TokenSource getTokenSource() {
## 		return tokenSource;
## 	}

## 	public String toString() {
## 		if ( p == -1 ) {
## 			fillBuffer();
## 		}
## 		return toString(0, tokens.size()-1);
## 	}

    def toString(self, start=None, stop=None):
        if self.p == -1:
            self.fillBuffer()

        if start is None:
            start = 0
        elif not isinstance(start, int):
            start = start.index

        if stop is None:
            stop = len(self.tokens) - 1
        elif not isinstance(stop, int):
            stop = stop.index
        
        if stop >= len(self.tokens):
            stop = len(self.tokens) - 1

        return ''.join([t.text for t in self.tokens[start:stop+1]])

    

