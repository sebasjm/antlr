"""ANTLR3 runtime module"""

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

import sys
from cStringIO import StringIO

# compatibility stuff
try:
    set = set
    frozenset = frozenset
except NameError:
    from sets import Set as set, ImmutableSet as frozenset


try:
    reversed = reversed
except NameError:
    def reversed(l):
        l = l[:]
        l.reverse()
        return l


##############################################################################
#
# Exceptions
#
##############################################################################

class RecognitionException(Exception):
    """The root of the ANTLR exception hierarchy.

    To avoid English-only error messages and to generally make things
    as flexible as possible, these exceptions are not created with strings,
    but rather the information necessary to generate an error.  Then
    the various reporting methods in Parser and Lexer can be overridden
    to generate a localized error message.  For example, MismatchedToken
    exceptions are built with the expected token type.
    So, don't expect getMessage() to return anything.

    Note that as of Java 1.4, you can access the stack trace, which means
    that you can compute the complete trace of rules from the start symbol.
    This gives you considerable context information with which to generate
    useful error messages.

    ANTLR generates code that throws exceptions upon recognition error and
    also generates code to catch these exceptions in each rule.  If you
    want to quit upon first error, you can turn off the automatic error
    handling mechanism using rulecatch action, but you still need to
    override methods mismatch and recoverFromMismatchSet.
    
    In general, the recognition exceptions can track where in a grammar a
    problem occurred and/or what was the expected input.  While the parser
    knows its state (such as current input symbol and line info) that
    state can change before the exception is reported so current token index
    is computed and stored at exception time.  From this info, you can
    perhaps print an entire line of input not just a single token, for example.
    Better to just say the recognizer had a problem and then let the parser
    figure out a fancy report.
    
    """

    def __init__(self, input=None):
        Exception.__init__(self)

	# What input stream did the error occur in?
	self.input = None

	# What is index of token/char were we looking at when the error occurred?
	self.index = None

	# The current Token when an error occurred.  Since not all streams
	# can retrieve the ith Token, we have to track the Token object.
	# For parsers.  Even when it's a tree parser, token might be set.
	self.token = None

	# If this is a tree parser exception, node is set to the node with
	# the problem.
	self.node = None

	# The current char when an error occurred. For lexers.
	self.c = None

	# Track the line at which the error occurred in case this is
	# generated from a lexer.  We need to track this since the
        # unexpected char doesn't carry the line info.
	self.line = None

	self.charPositionInLine = None


        if input is not None:
            self.input = input
            self.index = input.index()

            # Get current lookahead, where the error occured.
            # This may be a token or character.
            lt = input.LT(1)           
            try:
                lt.text

            except AttributeError:
                # we have not a token, so we assume it's a character
                # once we have tree parsers, we also have to handle nodes

                self.c = lt
                self.line = input.line
                self.charPositionInLine = input.charPositionInLine
                
            else:
                # it's a token
                
                self.token = lt
                self.line = lt.line
                self.charPositionInLine = lt.charPositionInLine

##             if isinstance(input, CommonTreeNodeStream):
##                 self.node = input.LT(1)
##                 if instanceof(self.node, CommonTree):
##                     self.token = this.node.token
##                     self.line = token.line
##                     self.charPositionInLine = token.charPositionInLine

##             elif instanceof(input, CharStream):
##                 self.c = input.LA(1);
##                 self.line = input.line
##                 self.charPositionInLine = input.charPositionInLine

##             else:
##                 self.c = input.LA(1)


    def getUnexpectedType(self):
	"""Return the token type or char of the unexpected input element"""

        try:
            return self.token.type
        except AttributeError:
            return self.c

    unexpectedType = property(getUnexpectedType)
    

class MismatchedTokenException(RecognitionException):
    def __init__(self, expecting, input):
        RecognitionException.__init__(self, input)
        self.expecting = expecting
        

    def __str__(self):
        #return "MismatchedTokenException("+self.expecting+")"
        return "MismatchedTokenException(%r!=%r)" % (self.getUnexpectedType(), self.expecting)
    __repr__ = __str__
    

class MismatchedRangeException(RecognitionException):
    def __init__(self, a, b, input):
        RecognitionException.__init__(self, input)

        self.a = a
        self.b = b
        

    def __str__(self):
        return "MismatchedRangeException(%r not in [%r..%r])" % (self.getUnexpectedType(), self.a, self.b)
    __repr__ = __str__
    

class MismatchedSetException(RecognitionException):
    def __init__(self, expecting, input):
        RecognitionException.__init__(self, input)

        self.expecting = expecting
        

    def __str__(self):
        return "MismatchedSetException(%r not in %r)" % (self.getUnexpectedType(), self.expecting)
    __repr__ = __str__


class NoViableAltException(RecognitionException):
    def __init__(self, grammarDecisionDescription, decisionNumber, stateNumber, input):
        RecognitionException.__init__(self, input)

	self.grammarDecisionDescription = grammarDecisionDescription
	self.decisionNumber = decisionNumber
	self.stateNumber = stateNumber


    def __str__(self):
        return "NoViableAltException(%r!=[%r])" % (
            self.unexpectedType, self.grammarDecisionDescription
            )
    __repr__ = __str__
    

class EarlyExitException(RecognitionException):
    """The recognizer did not match anything for a (..)+ loop."""

    def __init__(self, decisionNumber, input):
        RecognitionException.__init__(self, input)

        self.decisionNumber = decisionNumber


##############################################################################
#
# Streams
#
##############################################################################

EOF = -1

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

 	# The index of the character relative to the beginning of the line 0..n-1
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

        
##############################################################################
#
# Tokens
#
##############################################################################

# All tokens go to the parser (unless skip() is called in that rule)
# on a particular "channel".  The parser tunes to a particular channel
# so that whitespace etc... can go to the parser on a "hidden" channel.
DEFAULT_CHANNEL = 0

# Anything on different channel than DEFAULT_CHANNEL is not parsed
# by parser.
HIDDEN_CHANNEL = 99

# Predefined token types
EOR_TOKEN_TYPE = 1

# imaginary tree navigation type; traverse "get child" link
DOWN = 2
# imaginary tree navigation type; finish with a child list
UP = 3

MIN_TOKEN_TYPE = UP+1
	

## 	/** Get the text of the token */
## 	public abstract String getText();
## 	public abstract void setText(String text);

## 	public abstract int getType();
## 	public abstract void setType(int ttype);
## 	/**  The line number on which this token was matched; line=1..n */
## 	public abstract int getLine();
##     public abstract void setLine(int line);

## 	/** The index of the first character relative to the beginning of the line 0..n-1 */
## 	public abstract int getCharPositionInLine();
## 	public abstract void setCharPositionInLine(int pos);

## 	public abstract int getChannel();
## 	public abstract void setChannel(int channel);

## 	/** An index from 0..n-1 of the token object in the input stream.
## 	 *  This must be valid in order to use the ANTLRWorks debugger.
## 	 */
## 	public abstract int getTokenIndex();
## 	public abstract void setTokenIndex(int index);


class CommonToken(object):
    def __init__(self, type=None, channel=DEFAULT_CHANNEL, text=None, input=None, start=None, stop=None, oldToken=None):
        if oldToken is not None:
            self.type = oldToken.type
            self.line = oldToken.line
            self.charPositionInLine = oldToken.charPositionInLine
            self.channel = oldToken.channel
            self.index = oldToken.index
            self._text = oldToken._text

        else:
            self.type = type
            self.input = input
            self.charPositionInLine = -1 # set to invalid position
            self.line = 0
            self.channel = channel
            
	    #What token number is this from 0..n-1 tokens; < 0 implies invalid index
            self.index = -1
            
            # We need to be able to change the text once in a while.  If
            # this is non-null, then getText should return this.  Note that
            # start/stop are not affected by changing this.
            self._text = text

        # The char position into the input buffer where this token starts
	self.start = start

        # The char position into the input buffer where this token stops
        # This is the index of the last char, *not* the index after it!
	self.stop = stop


    def getText(self):
        if self._text is not None:
            return self._text

        if self.input is None:
            return None
        
        return self.input.substring(self.start, self.stop)


    def setText(self, text):
	"""
        Override the text for this token.  getText() will return this text
        rather than pulling from the buffer.  Note that this does not mean
        that start/stop indexes are not valid.  It means that that input
        was converted to a new string in the token object.
	"""
        self._text = text

    text = property(getText, setText)


    def __str__(self):
        channelStr = "";
        if self.channel > 0:
            channelStr = ",channel=" + str(self.channel)

        txt = self.text
        if txt is not None:
            txt = txt.replace("\n","\\\\n")
            txt = txt.replace("\r","\\\\r")
            txt = txt.replace("\t","\\\\t")
        else:
            txt = "<no text>"

        return "[@%s,%s:%s=%r,<%s>%s,%s:%s]" % (
            self.index,
            self.start, self.stop,
            txt,
            self.type, channelStr,
            self.line, self.charPositionInLine
            )
    


EOF_TOKEN = CommonToken(type=EOF)
	
INVALID_TOKEN_TYPE = 0
INVALID_TOKEN = CommonToken(type=INVALID_TOKEN_TYPE)

# In an action, a lexer rule can set token to this SKIP_TOKEN and ANTLR
# will avoid creating a token for this symbol and try to fetch another.
SKIP_TOKEN = CommonToken(type=INVALID_TOKEN_TYPE)


##############################################################################
#
# Token streams
#
##############################################################################


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

        return i;


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
            self.fillBuffer();

        if k==0:
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

    

##############################################################################
#
# Recognizers
#
##############################################################################


class BaseRecognizer(object):
    """
    A generic recognizer that can handle recognizers generated from
    lexer, parser, and tree grammars.  This is all the parsing
    support code essentially; most of it is error recovery stuff and
    backtracking.
    """

    MEMO_RULE_FAILED = -2
    MEMO_RULE_UNKNOWN = -1

    # copies from Token object for convenience in actions
    DEFAULT_TOKEN_CHANNEL = DEFAULT_CHANNEL

    # for convenience in actions
    HIDDEN = HIDDEN_CHANNEL

    def __init__(self):
        # Track the set of token types that can follow any rule invocation.
        # Stack grows upwards.  When it hits the max, it grows 2x in size
        # and keeps going.
        self.following = []

        # This is true when we see an error and before having successfully
        # matched a token.  Prevents generation of more than one error message
        # per error.
        self.errorRecovery = False

        # The index into the input stream where the last error occurred.
        # This is used to prevent infinite loops where an error is found
        # but no token is consumed during recovery...another error is found,
        # ad naseum.  This is a failsafe mechanism to guarantee that at least
        # one token/tree node is consumed for two errors.
        self.lastErrorIndex = -1

        # In lieu of a return value, this indicates that a rule or token
        # has failed to match.  Reset to false upon valid token match.
        self.failed = False

        # If 0, no backtracking is going on.  Safe to exec actions etc...
        # If >0 then it's the level of backtracking.
        self.backtracking = 0

        # An array[size num rules] of Map<Integer,Integer> that tracks
        # the stop token index for each rule.  ruleMemo[ruleIndex] is
        # the memoization table for ruleIndex.  For key ruleStartIndex, you
        # get back the stop token for associated rule or MEMO_RULE_FAILED.
        #
        #  This is only used if rule memoization is on (which it is by default).
        self.ruleMemo = None


    def reset():
        """
        reset the parser's state; subclasses must rewinds the input stream
        """
        
        # wack everything related to error recovery
        self._fsp = -1
        self.errorRecovery = False
        self.lastErrorIndex = -1
        self.failed = False
        # wack everything related to backtracking and memoization
        self.backtracking = 0
        if self.ruleMemo is not None:
            self.ruleMemo = {}


    def match(self, input, ttype, follow):
        """
        Match current input symbol against ttype.  Upon error, do one token
        insertion or deletion if possible.  You can override to not recover
	here and bail out of the current production to the normal error
	exception catch (at the end of the method) by just throwing
	MismatchedTokenException upon input.LA(1)!=ttype.
        """
        
        if self.input.LA(1) == ttype:
            self.input.consume()
            self.errorRecovery = False
            self.failed = False
            return

        if self.backtracking > 0:
            self.failed = True
            return

        self.mismatch(input, ttype, follow)
        return


    def matchAny(self, input):
        self.errorRecovery = False
        self.failed = False
        self.input.consume()


    def mismatch(self, input, ttype, follow):
        """
        factor out what to do upon token mismatch so tree parsers can behave
        differently.  Override this method in your parser to do things
	like bailing out after the first error; just throw the mte object
	instead of calling the recovery method.
        """
        
        mte = MismatchedTokenException(ttype, input)
        self.recoverFromMismatchedToken(input, mte, ttype, follow)


    def reportError(self, e):
        """Report a recognition problem.
            
        This method sets errorRecovery to indicate the parser is recovering
        not parsing.  Once in recovery mode, no errors are generated.
        To get out of recovery mode, the parser must successfully match
        a token (after a resync).  So it will go:

        1. error occurs
        2. enter recovery mode, report error
        3. consume until token found in resynch set
        4. try to resume parsing
        5. next match() will reset errorRecovery mode
        """
        
        # if we've already reported an error and have not matched a token
        # yet successfully, don't report any errors.
        if self.errorRecovery:
            return

        self.errorRecovery = True

        self.displayRecognitionError(self.tokenNames, e)


    def displayRecognitionError(self, tokenNames, e):
        hdr = self.getErrorHeader(e)
        msg = self.getErrorMessage(e, tokenNames)
        self.emitErrorMessage(hdr+" "+msg)


    def getErrorMessage(self, e, tokenNames):
        """
        What error message should be generated for the various
        exception types?
        
        Not very object-oriented code, but I like having all error message
        generation within one method rather than spread among all of the
	exception classes. This also makes it much easier for the exception
	handling because the exception classes do not have to have pointers back
	to this object to access utility routines and so on. Also, changing
	the message for an exception type would be difficult because you
	would have to subclassing exception, but then somehow get ANTLR
	to make those kinds of exception objects instead of the default.
	This looks weird, but trust me--it makes the most sense in terms
	of flexibility.

        For grammar debugging, you will want to override this to add
	more information such as the stack frame with
	getRuleInvocationStack(e, this.getClass().getName()) and,
	for no viable alts, the decision description and state etc...

        Override this to change the message generated for one or more
	exception types.
        """

        # FIXME: correct implentation
        return str(e)
    
## 		String msg = null;
## 		if ( e instanceof MismatchedTokenException ) {
## 			MismatchedTokenException mte = (MismatchedTokenException)e;
## 			String tokenName="<unknown>";
## 			if ( mte.expecting== Token.EOF ) {
## 				tokenName = "EOF";
## 			}
## 			else {
## 				tokenName = tokenNames[mte.expecting];
## 			}
## 			msg = "mismatched input "+getTokenErrorDisplay(e.token)+
## 				" expecting "+tokenName;
## 		}
## 		else if ( e instanceof MismatchedTreeNodeException ) {
## 			MismatchedTreeNodeException mtne = (MismatchedTreeNodeException)e;
## 			String tokenName="<unknown>";
## 			if ( mtne.expecting==Token.EOF ) {
## 				tokenName = "EOF";
## 			}
## 			else {
## 				tokenName = tokenNames[mtne.expecting];
## 			}
## 			msg = "mismatched tree node: "+mtne.node+
## 				" expecting "+tokenName;
## 		}
## 		else if ( e instanceof NoViableAltException ) {
## 			NoViableAltException nvae = (NoViableAltException)e;
## 			// for development, can add "decision=<<"+nvae.grammarDecisionDescription+">>"
## 			// and "(decision="+nvae.decisionNumber+") and
## 			// "state "+nvae.stateNumber
## 			msg = "no viable alternative at input "+getTokenErrorDisplay(e.token);
## 		}
## 		else if ( e instanceof EarlyExitException ) {
## 			EarlyExitException eee = (EarlyExitException)e;
## 			// for development, can add "(decision="+eee.decisionNumber+")"
## 			msg = "required (...)+ loop did not match anything at input "+
## 				getTokenErrorDisplay(e.token);
## 		}
## 		else if ( e instanceof MismatchedSetException ) {
## 			MismatchedSetException mse = (MismatchedSetException)e;
## 			msg = "mismatched input "+getTokenErrorDisplay(e.token)+
## 				" expecting set "+mse.expecting;
## 		}
## 		else if ( e instanceof MismatchedNotSetException ) {
## 			MismatchedNotSetException mse = (MismatchedNotSetException)e;
## 			msg = "mismatched input "+getTokenErrorDisplay(e.token)+
## 				" expecting set "+mse.expecting;
## 		}
## 		else if ( e instanceof FailedPredicateException ) {
## 			FailedPredicateException fpe = (FailedPredicateException)e;
## 			msg = "rule "+fpe.ruleName+" failed predicate: {"+
## 				fpe.predicateText+"}?";
## 		}
## 		return msg;
## 	}


    def getErrorHeader(self, e):
        """
        What is the error header, normally line/character position information?
        """
        
        return "line %d:%d" % (e.line, e.charPositionInLine)


## 	/** How should a token be displayed in an error message? The default
## 	 *  is to display just the text, but during development you might
## 	 *  want to have a lot of information spit out.  Override in that case
## 	 *  to use t.toString() (which, for CommonToken, dumps everything about
## 	 *  the token). This is better than forcing you to override a method in
## 	 *  your token objects because you don't have to go modify your lexer
## 	 *  so that it creates a new Java type.
## 	 */
## 	public String getTokenErrorDisplay(Token t) {
## 		String s = t.getText();
## 		if ( s==null ) {
## 			if ( t.getType()==Token.EOF ) {
## 				s = "<EOF>";
## 			}
## 			else {
## 				s = "<"+t.getType()+">";
## 			}
## 		}
## 		s = s.replaceAll("\n","\\\\n");
## 		s = s.replaceAll("\r","\\\\r");
## 		s = s.replaceAll("\t","\\\\t");
## 		return "'"+s+"'";
## 	}

    def emitErrorMessage(self, msg):
	"""Override this method to change where error messages go"""
        sys.stderr.write(msg + '\n')


    def recover(self, input, re):
        """
        Recover from an error found on the input stream.  Mostly this is
        NoViableAlt exceptions, but could be a mismatched token that
        the match() routine could not recover from.
        """
        
        # PROBLEM? what if input stream is not the same as last time
        # perhaps make lastErrorIndex a member of input
        if self.lastErrorIndex == input.index():
            # uh oh, another error at same token index; must be a case
            # where LT(1) is in the recovery token set so nothing is
            # consumed; consume a single token so at least to prevent
            # an infinite loop; this is a failsafe.
            input.consume()

        self.lastErrorIndex = input.index()
        followSet = self.computeErrorRecoverySet()
        
        self.beginResync()
        self.consumeUntil(input, followSet)
        self.endResync()


    def beginResync(self):
	"""
        A hook to listen in on the token consumption during error recovery.
        The DebugParser subclasses this to fire events to the listenter.
        """

        pass


    def endResync(self):
	"""
        A hook to listen in on the token consumption during error recovery.
        The DebugParser subclasses this to fire events to the listenter.
        """

        pass


    def computeErrorRecoverySet(self):
        """
        Compute the error recovery set for the current rule.  During
        rule invocation, the parser pushes the set of tokens that can
        follow that rule reference on the stack; this amounts to
        computing FIRST of what follows the rule reference in the
        enclosing rule. This local follow set only includes tokens
        from within the rule; i.e., the FIRST computation done by
        ANTLR stops at the end of a rule.

        EXAMPLE

        When you find a "no viable alt exception", the input is not
        consistent with any of the alternatives for rule r.  The best
        thing to do is to consume tokens until you see something that
        can legally follow a call to r *or* any rule that called r.
        You don't want the exact set of viable next tokens because the
        input might just be missing a token--you might consume the
        rest of the input looking for one of the missing tokens.

        Consider grammar:

        a : '[' b ']'
          | '(' b ')'
          ;
        b : c '^' INT ;
        c : ID
          | INT
          ;

        At each rule invocation, the set of tokens that could follow
        that rule is pushed on a stack.  Here are the various "local"
        follow sets:

        FOLLOW(b1_in_a) = FIRST(']') = ']'
        FOLLOW(b2_in_a) = FIRST(')') = ')'
        FOLLOW(c_in_b) = FIRST('^') = '^'

        Upon erroneous input "[]", the call chain is

        a -> b -> c

        and, hence, the follow context stack is:

        depth  local follow set     after call to rule
          0         <EOF>                    a (from main())
          1          ']'                     b
          3          '^'                     c

        Notice that ')' is not included, because b would have to have
        been called from a different context in rule a for ')' to be
        included.

        For error recovery, we cannot consider FOLLOW(c)
        (context-sensitive or otherwise).  We need the combined set of
        all context-sensitive FOLLOW sets--the set of all tokens that
        could follow any reference in the call chain.  We need to
        resync to one of those tokens.  Note that FOLLOW(c)='^' and if
        we resync'd to that token, we'd consume until EOF.  We need to
        sync to context-sensitive FOLLOWs for a, b, and c: {']','^'}.
        In this case, for input "[]", LA(1) is in this set so we would
        not consume anything and after printing an error rule c would
        return normally.  It would not find the required '^' though.
        At this point, it gets a mismatched token error and throws an
        exception (since LA(1) is not in the viable following token
        set).  The rule exception handler tries to recover, but finds
        the same recovery set and doesn't consume anything.  Rule b
        exits normally returning to rule a.  Now it finds the ']' (and
        with the successful match exits errorRecovery mode).

        So, you cna see that the parser walks up call chain looking
        for the token that was a member of the recovery set.

        Errors are not generated in errorRecovery mode.

        ANTLR's error recovery mechanism is based upon original ideas:

        "Algorithms + Data Structures = Programs" by Niklaus Wirth

        and

        "A note on error recovery in recursive descent parsers":
        http://portal.acm.org/citation.cfm?id=947902.947905

        Later, Josef Grosch had some good ideas:

        "Efficient and Comfortable Error Recovery in Recursive Descent
        Parsers":
        ftp://www.cocolab.com/products/cocktail/doca4.ps/ell.ps.zip

        Like Grosch I implemented local FOLLOW sets that are combined
        at run-time upon error to avoid overhead during parsing.
        """
        
        return self.combineFollows(False)

        
    def computeContextSensitiveRuleFOLLOW(self):
        """
        Compute the context-sensitive FOLLOW set for current rule.
        This is set of token types that can follow a specific rule
        reference given a specific call chain.  You get the set of
        viable tokens that can possibly come next (lookahead depth 1)
        given the current call chain.  Contrast this with the
        definition of plain FOLLOW for rule r:

         FOLLOW(r)={x | S=>*alpha r beta in G and x in FIRST(beta)}

        where x in T* and alpha, beta in V*; T is set of terminals and
        V is the set of terminals and nonterminals.  In other words,
        FOLLOW(r) is the set of all tokens that can possibly follow
        references to r in *any* sentential form (context).  At
        runtime, however, we know precisely which context applies as
        we have the call chain.  We may compute the exact (rather
        than covering superset) set of following tokens.

        For example, consider grammar:

        stat : ID '=' expr ';'      // FOLLOW(stat)=={EOF}
             | "return" expr '.'
             ;
        expr : atom ('+' atom)* ;   // FOLLOW(expr)=={';','.',')'}
        atom : INT                  // FOLLOW(atom)=={'+',')',';','.'}
             | '(' expr ')'
             ;

        The FOLLOW sets are all inclusive whereas context-sensitive
        FOLLOW sets are precisely what could follow a rule reference.
        For input input "i=(3);", here is the derivation:

        stat => ID '=' expr ';'
             => ID '=' atom ('+' atom)* ';'
             => ID '=' '(' expr ')' ('+' atom)* ';'
             => ID '=' '(' atom ')' ('+' atom)* ';'
             => ID '=' '(' INT ')' ('+' atom)* ';'
             => ID '=' '(' INT ')' ';'

        At the "3" token, you'd have a call chain of

          stat -> expr -> atom -> expr -> atom

        What can follow that specific nested ref to atom?  Exactly ')'
        as you can see by looking at the derivation of this specific
        input.  Contrast this with the FOLLOW(atom)={'+',')',';','.'}.

        You want the exact viable token set when recovering from a
        token mismatch.  Upon token mismatch, if LA(1) is member of
        the viable next token set, then you know there is most likely
        a missing token in the input stream.  "Insert" one by just not
        throwing an exception.
        """

        return self.combineFollows(True)


    def combineFollows(self, exact):
        followSet = set()
        for localFollowSet in reversed(self.following):
            followSet |= localFollowSet
            if exact and EOR_TOKEN_TYPE not in localFollowSet:
                break

        followSet -= set([EOR_TOKEN_TYPE])
        return followSet


    def recoverFromMismatchedToken(self, input, e, ttype, follow):
        """Attempt to recover from a single missing or extra token.

        EXTRA TOKEN

        LA(1) is not what we are looking for.  If LA(2) has the right token,
        however, then assume LA(1) is some extra spurious token.  Delete it
        and LA(2) as if we were doing a normal match(), which advances the
        input.

        MISSING TOKEN

        If current token is consistent with what could come after
        ttype then it is ok to "insert" the missing token, else throw
        exception For example, Input "i=(3;" is clearly missing the
        ')'.  When the parser returns from the nested call to expr, it
        will have call chain:

          stat -> expr -> atom

        and it will be trying to match the ')' at this point in the
        derivation:

             => ID '=' '(' INT ')' ('+' atom)* ';'
                                ^
        match() will see that ';' doesn't match ')' and report a
        mismatched token error.  To recover, it sees that LA(1)==';'
        is in the set of tokens that can follow the ')' token
        reference in rule atom.  It can assume that you forgot the ')'.
        """
                                         
        # if next token is what we are looking for then "delete" this token
        if input.LA(2) == ttype:
            self.reportError(e)

            self.beginResync()
            input.consume() # simply delete extra token
            self.endResync()
            input.consume()  # move past ttype token as if all were ok
            return

        if not self.recoverFromMismatchedElement(input, e, follow):
            raise e



## 	public void recoverFromMismatchedSet(IntStream input,
## 										 RecognitionException e,
## 										 BitSet follow)
## 		throws RecognitionException
## 	{
## 		// TODO do single token deletion like above for Token mismatch
## 		if ( !recoverFromMismatchedElement(input,e,follow) ) {
## 			throw e;
## 		}
## 	}

## 	/** This code is factored out from mismatched token and mismatched set
## 	 *  recovery.  It handles "single token insertion" error recovery for
## 	 *  both.  No tokens are consumed to recover from insertions.  Return
## 	 *  true if recovery was possible else return false.
## 	 */
    def recoverFromMismatchedElement(self, input, e, follow):
        if follow is None:
            # we have no information about the follow; we can only consume
            # a single token and hope for the best
            return False

        # compute what can follow this grammar element reference
        if EOR_TOKEN_TYPE in follow:
            viableTokensFollowingThisRule = \
                self.computeContextSensitiveRuleFOLLOW()
            
            follow = (follow | viableTokensFollowingThisRule) - set([EOR_TOKEN_TYPE])

        # if current token is consistent with what could come after set
        # then it is ok to "insert" the missing token, else throw exception
        if input.LA(1) in follow:
 	    self.reportError(e)
            return True

        # nothing to do; throw exception
        return False


    def consumeUntil(self, input, tokenTypes):
        """
        Consume tokens until one matches the given token or token set

        tokenTypes can be a single token type or a set of token types
        
        """
        
        if not isinstance(tokenTypes, (set, frozenset)):
            tokenTypes = frozenset([tokenTypes])

        ttype = input.LA(1)
        while ttype != EOF and ttype not in tokenTypes:
            input.consume()
            ttype = input.LA(1)


## 	/** Return List<String> of the rules in your parser instance
## 	 *  leading up to a call to this method.  You could override if
## 	 *  you want more details such as the file/line info of where
## 	 *  in the parser java code a rule is invoked.
## 	 *
## 	 *  This is very useful for error messages and for context-sensitive
## 	 *  error recovery.
## 	 */
## 	public List getRuleInvocationStack() {
## 		String parserClassName = getClass().getName();
## 		return getRuleInvocationStack(new Throwable(), parserClassName);
## 	}

## 	/** A more general version of getRuleInvocationStack where you can
## 	 *  pass in, for example, a RecognitionException to get it's rule
## 	 *  stack trace.  This routine is shared with all recognizers, hence,
## 	 *  static.
## 	 *
## 	 *  TODO: move to a utility class or something; weird having lexer call this
## 	 */
## 	public static List getRuleInvocationStack(Throwable e,
## 											  String recognizerClassName)
## 	{
## 		List rules = new ArrayList();
## 		StackTraceElement[] stack = e.getStackTrace();
## 		int i = 0;
## 		for (i=stack.length-1; i>=0; i--) {
## 			StackTraceElement t = stack[i];
## 			if ( t.getClassName().startsWith("org.antlr.runtime.") ) {
## 				continue; // skip support code such as this method
## 			}
## 			if ( t.getMethodName().equals("nextToken") ) {
## 				continue;
## 			}
## 			if ( !t.getClassName().equals(recognizerClassName) ) {
## 				continue; // must not be part of this parser
## 			}
##             rules.add(t.getMethodName());
## 		}
## 		return rules;
## 	}

## 	public int getBacktrackingLevel() {
## 		return backtracking;
## 	}

## 	/** For debugging and other purposes, might want the grammar name.
## 	 *  Have ANTLR generate an implementation for this method.
## 	 */
## 	public String getGrammarFileName() {
## 		return null;
## 	}

## 	/** A convenience method for use most often with template rewrites.
## 	 *  Convert a List<Token> to List<String>
## 	 */
## 	public List toStrings(List tokens) {
## 		if ( tokens==null ) return null;
## 		List strings = new ArrayList(tokens.size());
## 		for (int i=0; i<tokens.size(); i++) {
## 			strings.add(((Token)tokens.get(i)).getText());
## 		}
## 		return strings;
## 	}

## 	/** Convert a List<RuleReturnScope> to List<StringTemplate> by copying
## 	 *  out the .st property.  Useful when converting from
## 	 *  list labels to template attributes:
## 	 *
## 	 *    a : ids+=rule -> foo(ids={toTemplates($ids)})
## 	 *      ;
## 	 */
## 	public List toTemplates(List retvals) {
## 		if ( retvals==null ) return null;
## 		List strings = new ArrayList(retvals.size());
## 		for (int i=0; i<retvals.size(); i++) {
## 			strings.add(((RuleReturnScope)retvals.get(i)).getTemplate());
## 		}
## 		return strings;
## 	}

    def getRuleMemoization(self, ruleIndex, ruleStartIndex):
	"""
        Given a rule number and a start token index number, return
        MEMO_RULE_UNKNOWN if the rule has not parsed input starting from
        start index.  If this rule has parsed input starting from the
        start index before, then return where the rule stopped parsing.
        It returns the index of the last token matched by the rule.

        For now we use a hashtable and just the slow Object-based one.
        Later, we can make a special one for ints and also one that
        tosses out data after we commit past input position i.
	"""
        
        if ruleIndex not in self.ruleMemo:
            self.ruleMemo[ruleIndex] = {}
		
        stopIndex = self.ruleMemo[ruleIndex].get(ruleStartIndex, None)
        if stopIndex is None:
            return self.MEMO_RULE_UNKNOWN

        return stopIndex


    def alreadyParsedRule(self, input, ruleIndex):
	"""
        Has this rule already parsed input at the current index in the
        input stream?  Return the stop token index or MEMO_RULE_UNKNOWN.
        If we attempted but failed to parse properly before, return
        MEMO_RULE_FAILED.

        This method has a side-effect: if we have seen this input for
        this rule and successfully parsed before, then seek ahead to
        1 past the stop token matched for this rule last time.
        """
        
        stopIndex = self.getRuleMemoization(ruleIndex, input.index())
        if stopIndex == self.MEMO_RULE_UNKNOWN:
            return False

        if stopIndex == self.MEMO_RULE_FAILED:
            self.failed = True

        else:
            input.seek(stopIndex + 1)

        return True;


    def memoize(self, input, ruleIndex, ruleStartIndex):
	"""
        Record whether or not this rule parsed the input at this position
	successfully.
	"""

        if self.failed:
            stopTokenIndex = self.MEMO_RULE_FAILED
        else:
            stopTokenIndex = input.index() - 1
        
        if ruleIndex in self.ruleMemo:
            self.ruleMemo[ruleIndex][ruleStartIndex] = stopTokenIndex


## 	/** return how many rule/input-index pairs there are in total.
## 	 *  TODO: this includes synpreds. :(
## 	 */
## 	public int getRuleMemoizationCacheSize() {
## 		int n = 0;
## 		for (int i = 0; ruleMemo!=null && i < ruleMemo.length; i++) {
## 			Map ruleMap = ruleMemo[i];
## 			if ( ruleMap!=null ) {
## 				n += ruleMap.size(); // how many input indexes are recorded?
## 			}
## 		}
## 		return n;
## 	}

## 	public void traceIn(String ruleName, int ruleIndex, Object inputSymbol)  {
## 		System.out.print("enter "+ruleName+" "+inputSymbol);
## 		if ( failed ) {
## 			System.out.println(" failed="+failed);
## 		}
## 		if ( backtracking>0 ) {
## 			System.out.print(" backtracking="+backtracking);
## 		}
## 		System.out.println();
## 	}

## 	public void traceOut(String ruleName,
## 						 int ruleIndex,
## 						 Object inputSymbol)
## 	{
## 		System.out.print("exit "+ruleName+" "+inputSymbol);
## 		if ( failed ) {
## 			System.out.println(" failed="+failed);
## 		}
## 		if ( backtracking>0 ) {
## 			System.out.print(" backtracking="+backtracking);
## 		}
## 		System.out.println();
## 	}

## 	/** A syntactic predicate.  Returns true/false depending on whether
## 	 *  the specified grammar fragment matches the current input stream.
## 	 *  This resets the failed instance var afterwards.
## 	public boolean synpred(IntStream input, GrammarFragmentPtr fragment) {
## 		//int i = input.index();
## 		//System.out.println("begin backtracking="+backtracking+" @"+i+"="+((CommonTokenStream)input).LT(1));
## 		backtracking++;
## 		beginBacktrack(backtracking);
## 		int start = input.mark();
## 		try {fragment.invoke();}
## 		catch (RecognitionException re) {
## 			System.err.println("impossible: "+re);
## 		}
## 		boolean success = !failed;
## 		input.rewind(start);
## 		endBacktrack(backtracking, success);
## 		backtracking--;
## 		//System.out.println("end backtracking="+backtracking+": "+(failed?"FAILED":"SUCCEEDED")+" @"+input.index()+" should be "+i);
## 		failed=false;
## 		return success;
## 	}
## 	 */


class Lexer(BaseRecognizer):
    """
    A lexer is recognizer that draws input symbols from a character stream.
    lexer grammars result in a subclass of this object. A Lexer object
    uses simplified match() and error recovery mechanisms in the interest
    of speed.
    """

    def __init__(self, input):
        BaseRecognizer.__init__(self)
        
        # Where is the lexer drawing characters from?
        self.input = input

        # The goal of all lexer rules/methods is to create a token object.
	# This is an instance variable as multiple rules may collaborate to
	# create a single token.  nextToken will return this object after
	# matching lexer rule(s).  If you subclass to allow multiple token
	# emissions, then set this to the last token to be matched or
	# something nonnull so that the auto token emit mechanism will not
	# emit another token.
        self.token = None

	# What character index in the stream did the current token start at?
	# Needed, for example, to get the text for current token.  Set at
	# the start of nextToken.
	self.tokenStartCharIndex = -1

        # You can set the text for the current token to override what is in
	# the input char buffer.  Use setText() or can set this instance var.
        self.text = None

	# We must track the token rule nesting level as we only want to
	# emit a token automatically at the outermost level so we don't get
	# two if FLOAT calls INT.  To save code space and time, do not
	# inc/dec this in fragment rules.
	self.ruleNestingLevel = 0


    def reset(self):
        BaseRecognizer.reset(self) # reset all recognizer state variables

        # wack Lexer state variables
        self.token = None
        self.tokenStartCharIndex = -1
        self.text = None
        self.ruleNestingLevel = 0
        if self.input is not None:
            self.input.seek(0) # rewind the input


    def nextToken(self):
	"""
        Return a token from this source; i.e., match a token on the char
	stream.
	"""
        
        while 1:
            self.token = None
            self.tokenStartCharIndex = self.getCharIndex()
            self.text = None
            if self.input.LA(1) == EOF:
                return EOF_TOKEN

            try:
                self.mTokens()
                if self.token != SKIP_TOKEN:
                    return self.token

            except RecognitionException, re:
                raise # no error reporting/recovery
                #self.reportError(re)
                #self.recover(re)


    def skip(self):
        """
	Instruct the lexer to skip creating a token for current lexer rule
	and look for another token.  nextToken() knows to keep looking when
	a lexer rule finishes with token set to SKIP_TOKEN.  Recall that
	if token==null at end of any token rule, it creates one for you
	and emits it.
	"""
        
        self.token = Token.SKIP_TOKEN


    def mTokens(self):
	"""This is the lexer entry point that sets instance var 'token'"""

        # abstract method
        raise NotImplementedError
    

    def setCharStream(self, input):
        """Set the char stream and reset the lexer"""
        self.input = input
        self.token = None
        self.tokenStartCharIndex = -1
        self.ruleNestingLevel = 0


##     def emit(self, token):
## 	"""
##         Currently does not support multiple emits per nextToken invocation
## 	for efficiency reasons.  Subclass and override this method and
## 	nextToken (to push tokens into a list and pull from that list rather
## 	than a single variable as this implementation does).
## 	"""
        
##         self.token = token


    def emit(self, tokenType, line, charPosition, channel, start, stop):
	"""
        The standard method called to automatically emit a token at the
	outermost lexical rule.  The token object should point into the
	char buffer start..stop.  If there is a text override in 'text',
	use that to set the token's text.
	"""

        t = CommonToken()
        t.input = self.input
        t.type = tokenType
        t.channel = channel
        t.start = start
        t.stop = stop
        t.line = line
        t.text = self.text
        t.charPositionInLine = charPosition

        self.token = t
        
        return t


    def match(self, s):
        if isinstance(s, basestring):
            i = 0;
            while i < len(s):
                if self.input.LA(1) != s[i]:
                    if self.backtracking > 0:
                        self.failed = True
                        return

                    mte = MismatchedTokenException(s[i], self.input)
                    self.recover(mte)
                    raise mte

                i += 1
                self.input.consume()
                self.failed = False

        else:
            if self.input.LA(1) != s:
                if self.backtracking > 0:
                    self.failed = True
                    return

                mte = MismatchedTokenException(s, self.input)
                self.recover(mte);
                raise mte
        
            self.input.consume()
            self.failed = False
            

    def matchAny(self):
        self.input.consume()


    def matchRange(self, a, b):
        if self.input.LA(1) < a or self.input.LA(1) > b:
            if self.backtracking > 0:
                self.failed = True
                return

            mre = MismatchedRangeException(a, b, self.input)
            self.recover(mre)
            raise mre

        self.input.consume()
        self.failed = False


    def getLine(self):
        return self.input.line


    def getCharPositionInLine(self):
        return self.input.charPositionInLine


    def getCharIndex(self):
        """What is the index of the current character of lookahead?"""
        
        return self.input.index()


    def getText(self):
        """
        Return the text matched so far for the current token or any
        text override.
        """
        if self.text is not None:
            return self.text
        
        return self.input.substring(self.tokenStartCharIndex, self.getCharIndex()-1)


    def setText(self, text):
	"""
        Set the complete text of this token; it wipes any previous
	changes to the text.
	"""
        self.text = text


    def reportError(self, e):
        ## TODO: not thought about recovery in lexer yet.

        ## # if we've already reported an error and have not matched a token
        ## # yet successfully, don't report any errors.
        ## if self.errorRecovery:
        ##     #System.err.print("[SPURIOUS] ");
        ##     return;
        ## 
        ## self.errorRecovery = True

        self.displayRecognitionError(self.tokenNames, e)


    def getErrorMessage(self, e, tokenNames):
        raise NotImplementedError
## 		String msg = null;
## 		if ( e instanceof MismatchedTokenException ) {
## 			MismatchedTokenException mte = (MismatchedTokenException)e;
## 			msg = "mismatched character "+getCharErrorDisplay(e.c)+" expecting "+getCharErrorDisplay(mte.expecting);
## 		}
## 		else if ( e instanceof NoViableAltException ) {
## 			NoViableAltException nvae = (NoViableAltException)e;
## 			// for development, can add "decision=<<"+nvae.grammarDecisionDescription+">>"
## 			// and "(decision="+nvae.decisionNumber+") and
## 			// "state "+nvae.stateNumber
## 			msg = "no viable alternative at character "+getCharErrorDisplay(e.c);
## 		}
## 		else if ( e instanceof EarlyExitException ) {
## 			EarlyExitException eee = (EarlyExitException)e;
## 			// for development, can add "(decision="+eee.decisionNumber+")"
## 			msg = "required (...)+ loop did not match anything at character "+getCharErrorDisplay(e.c);
## 		}
## 		else if ( e instanceof MismatchedSetException ) {
## 			MismatchedSetException mse = (MismatchedSetException)e;
## 			msg = "mismatched character "+getCharErrorDisplay(e.c)+" expecting set "+mse.expecting;
## 		}
## 		else if ( e instanceof MismatchedNotSetException ) {
## 			MismatchedNotSetException mse = (MismatchedNotSetException)e;
## 			msg = "mismatched character "+getCharErrorDisplay(e.c)+" expecting set "+mse.expecting;
## 		}
## 		else if ( e instanceof MismatchedRangeException ) {
## 			MismatchedRangeException mre = (MismatchedRangeException)e;
## 			msg = "mismatched character "+getCharErrorDisplay(e.c)+" expecting set "+
## 				getCharErrorDisplay(mre.a)+".."+getCharErrorDisplay(mre.b);
## 		}
## 		else {
## 			msg = super.getErrorMessage(e, tokenNames);
## 		}
## 		return msg;
## 	}

    def getCharErrorDisplay(self, c):
        raise NotImplementedError
## 	public String getCharErrorDisplay(int c) {
## 		String s = String.valueOf((char)c);
## 		switch ( c ) {
## 			case Token.EOF :
## 				s = "<EOF>";
## 				break;
## 			case '\n' :
## 				s = "\\n";
## 				break;
## 			case '\t' :
## 				s = "\\t";
## 				break;
## 			case '\r' :
## 				s = "\\r";
## 				break;
## 		}
## 		return "'"+s+"'";
## 	}

    def recover(self, re):
	"""
        Lexers can normally match any char in it's vocabulary after matching
	a token, so do the easy thing and just kill a character and hope
	it all works out.  You can instead use the rule invocation stack
	to do sophisticated error recovery if you are in a fragment rule.
	"""
        #System.out.println("consuming char "+(char)input.LA(1)+" during recovery");
        #re.printStackTrace();
        self.input.consume()


    def traceIn(self, ruleName, ruleIndex):
        raise NotImplementedError
##             String inputSymbol = ((char)input.LT(1))+" line="+getLine()+":"+getCharPositionInLine();
## 		super.traceIn(ruleName, ruleIndex, inputSymbol);
##             }


    def traceOut(self, ruleName, ruleIndex):
        raise NotImplementedError
## 		String inputSymbol = ((char)input.LT(1))+" line="+getLine()+":"+getCharPositionInLine();
## 		super.traceOut(ruleName, ruleIndex, inputSymbol);
## 	}



class Parser(BaseRecognizer):
    def __init__(self, lexer):
        BaseRecognizer.__init__(self)

        self.input = lexer


##############################################################################
#
# DFAs
#
##############################################################################


class DFA(object):
    """A DFA implemented as a set of transition tables.

    Any state that has a semantic predicate edge is special; those states
    are generated with if-then-else structures in a specialStateTransition()
    which is generated by cyclicDFA template.
  
    There are at most 32767 states (16-bit signed short).
    Could get away with byte sometimes but would have to generate different
    types and the simulation code too.  For a point of reference, the Java
    lexer's Tokens rule DFA has 326 states roughly.
    
    """
    
    def __init__(
        self,
        recognizer, decisionNumber,
        eot, eof, min, max, accept, special, transition
        ):
        # Which recognizer encloses this DFA?  Needed to check backtracking
        self.recognizer = recognizer
        
        self.decisionNumber = decisionNumber
        self.eot = eot
        self.eof = eof
        self.min = min
        self.max = max
        self.accept = accept
        self.special = special
        self.transition = transition


    def predict(self, input):
	"""
        From the input stream, predict what alternative will succeed
	using this DFA (representing the covering regular approximation
	to the underlying CFL).  Return an alternative number 1..n.  Throw
	 an exception upon error.
	"""
        mark = input.mark()
        s = 0 # we always start at s0
        try:
            while True:
                #print "***Current state = %d" % s
                
                specialState = self.special[s]
                if specialState >= 0:
                    #print "is special"
                    s = self.specialStateTransition(specialState, input)
                    input.consume()
                    continue

                if self.accept[s] >= 1:
                    #print "accept state for alt %d" % self.accept[s]
                    return self.accept[s]

                # look for a normal char transition
                LA = input.LA(1)
                #print LA, repr(input.LT(1)), input.LT(1).text
                if LA == EOF:
                    c = -1 #0xffff
                else:
                    try:
                        c = ord(LA)
                    except TypeError:
                        # LA is a token type (int), not a char
                        c = LA
                        
                #print "LA = %d (%r)" % (c, unichr(c))
                #print "range = %d..%d" % (self.min[s], self.max[s])

                if c >= self.min[s] and c <= self.max[s]:
                    snext = self.transition[s][c-self.min[s]] # move to next state
                    #print "in range, next state = %d" % snext
                    
                    if snext < 0:
                        #print "not a normal transition"
                        # was in range but not a normal transition
                        # must check EOT, which is like the else clause.
                        # eot[s]>=0 indicates that an EOT edge goes to another
                        # state.
                        if self.eot[s] >= 0: # EOT Transition to accept state?
                            #print "EOT trans to accept state %d" % self.eot[s]
                            
                            s = self.eot[s]
                            input.consume()
                            # TODO: I had this as return accept[eot[s]]
                            # which assumed here that the EOT edge always
                            # went to an accept...faster to do this, but
                            # what about predicated edges coming from EOT
                            # target?
                            continue

                        #print "no viable alt"
                        self.noViableAlt(s, input)
                        return 0

                    s = snext
                    input.consume()
                    continue

                if self.eot[s]>=0:
                    #print "EOT to %d" % self.eot[s]
                    
                    s = self.eot[s]
                    input.consume()
                    continue

                # EOF Transition to accept state?
                if c == EOF and self.eof[s] >= 0:
                    #print "EOF Transition to accept state %d" % self.accept[self.eof[s]]
                    return self.accept[self.eof[s]]

                # not in range and not EOF/EOT, must be invalid symbol
                self.noViableAlt(s, input)
                return 0
            
        finally:
            input.rewind(mark)


    def noViableAlt(self, s, input):
        if self.recognizer.backtracking > 0:
            self.recognizer.failed = True
            return

        nvae = NoViableAltException(
            self.getDescription(),
            self.decisionNumber,
            s,
            input
            )

        self.error(nvae)
        raise nvae


    def error(self, nvae):
        """A hook for debugging interface"""
        pass


    def specialStateTransition(self, s, input):
        return -1


    def getDescription(self):
        return "n/a"


    def specialTransition(self, state, symbol):
        return 0
