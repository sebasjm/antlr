// [The "BSD licence"]
// Copyright (c) 2005-2006 Terence Parr
// Copyright (c) 2006 Kay Roepke (Objective-C runtime)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. The name of the author may not be used to endorse or promote products
//    derived from this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
// OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
// IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
// INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
// NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
// DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
// THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
// THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


#import "ANTLRCommonToken.h"


@implementation ANTLRCommonToken

- (ANTLRCommonToken *) initWithInput:(id<ANTLRCharStream>)anInput tokenType:(int)aTType channel:(int)aChannel start:(int)theStart stop:(int)theStop
{
	if (nil != (self = [super init])) {
		[self setInput:anInput];
		type = aTType;
		channel = aChannel;
		start = theStart;
		stop = theStop;
	}
	return self;
}


// inherited from ANTLRToken
//---------------------------------------------------------- 
//  text 
//---------------------------------------------------------- 
- (NSString *) text
{
	if (text != nil) {
		return text;
	}
	if (input == nil) {
		return nil;
	}
	return [input substringWithRange:NSMakeRange(start,stop-start)];
}

- (void) setText: (NSString *) aText
{
    if (text != aText) {
        [aText retain];
        [text release];
        text = aText;
    }
}

//---------------------------------------------------------- 
//  type 
//---------------------------------------------------------- 
- (int) type
{
    return type;
}

- (void) setType: (int) aType
{
    type = aType;
}

//---------------------------------------------------------- 
//  line 
//---------------------------------------------------------- 
- (int) line
{
    return line;
}

- (void) setLine: (int) aLine
{
    line = aLine;
}

//---------------------------------------------------------- 
//  charPositionInLine 
//---------------------------------------------------------- 
- (int) charPositionInLine
{
    return charPositionInLine;
}

- (void) setCharPositionInLine: (int) aCharPositionInLine
{
    charPositionInLine = aCharPositionInLine;
}

//---------------------------------------------------------- 
//  channel 
//---------------------------------------------------------- 
- (int) channel
{
    return channel;
}

- (void) setChannel: (int) aChannel
{
    channel = aChannel;
}



// end inherited

//---------------------------------------------------------- 
//  input 
//---------------------------------------------------------- 
- (id<ANTLRCharStream>) input
{
    return input; 
}

- (void) setInput: (id<ANTLRCharStream>) anInput
{
    if (input != anInput) {
        [anInput retain];
        [input release];
        input = anInput;
    }
}


//---------------------------------------------------------- 
//  start 
//---------------------------------------------------------- 
- (int) start
{
    return start;
}

- (void) setStart: (int) aStart
{
    start = aStart;
}

//---------------------------------------------------------- 
//  stop 
//---------------------------------------------------------- 
- (int) stop
{
    return stop;
}

- (void) setStop: (int) aStop
{
    stop = aStop;
}

//---------------------------------------------------------- 
//  index 
//---------------------------------------------------------- 
- (int) index
{
    return index;
}

- (void) setIndex: (int) anIndex
{
    index = anIndex;
}


- (NSString *) description
{
	NSString *channelString = [[NSString alloc] initWithFormat:@"channel=%d", channel];
	NSMutableString *txtString;
	if ([self text] != nil) {
		txtString = [NSMutableString stringWithString:[self text]];
		[txtString replaceOccurrencesOfString:@"\n" withString:@"\\\n" options:NSAnchoredSearch range:NSMakeRange(0, [txtString length])];
		[txtString replaceOccurrencesOfString:@"\r" withString:@"\\\r" options:NSAnchoredSearch range:NSMakeRange(0, [txtString length])];
		[txtString replaceOccurrencesOfString:@"\t" withString:@"\\\t" options:NSAnchoredSearch range:NSMakeRange(0, [txtString length])];
	} else {
		txtString = [NSMutableString stringWithString:@"<no text>"];
	}
	return [@"[@" stringByAppendingFormat:@"%d, %d, %d=%@,<%d>,%@,%d:%d]", index, start, stop, txtString, type, channelString, line, charPositionInLine];
}


@end
