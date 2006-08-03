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


#import "ANTLRStringStream.h"
#import "ANTLRStringStreamState.h"

@implementation ANTLRStringStream

- (id) init
{
	if (nil != (self = [super init])) {
		markers = [[NSMutableArray alloc] init];
		[self reset];
	}
	return self;
}

/** Copy data in string to a local char array */
- (id) initWithString:(NSString *) theString
{
	if (nil != (self = [self init])) {
		[self setData:[theString copy]];
	}
	return self;
}

/** This is the preferred constructor as no data is copied */
- (id) initWithStringNoCopy:(NSString *) theString
{
	if (nil != (self = [self init])) {
		[self setData:theString];
	}
	return self;
}

- (void) dealloc
{
	[markers release];
	markers = nil;
	[super dealloc];
}

/** Reset the stream so that it's in the same state it was
*  when the object was created *except* the data array is not
*  touched.
*/
- (void) reset
{
	p = 0;
	line = 1;
	charPositionInLine = 0;
	markDepth = 0;
	[markers removeAllObjects];
	[markers addObject:[NSNull null]];
}

- (void) consume 
{
	if ( p < [data length] ) {
		charPositionInLine++;
		if ( [data characterAtIndex:p] == '\n' ) {
			line++;
			charPositionInLine=0;
		}
		p++;
	}
}

- (int) LA:(int) i 
{
	if ( (p+i-1) >= [data length] ) {
		return ANTLRCharStreamEOF;
	}
	return (int)[data characterAtIndex:p+i-1];
}

/** Return the current input symbol index 0..n where n indicates the
*  last symbol has been read.
*/
- (unsigned int) index 
{
	return p;
}

- (unsigned int) count 
{
	return [data length];
}

- (unsigned int) mark 
{
	markDepth++;
	ANTLRStringStreamState *state = nil;
	if ( markDepth >= [markers count] ) {
		state = [[ANTLRStringStreamState alloc] init];
		[markers addObject:state];
		[state release];
	}
	else {
		state = (ANTLRStringStreamState *)[markers objectAtIndex:markDepth];
	}
	[state setIndex:p];
	[state setLine:line];
	[state setCharPositionInLine:charPositionInLine];
	return markDepth;
}

- (void) rewind:(unsigned int) marker 
{
	[self release:marker];
	ANTLRStringStreamState *state = (ANTLRStringStreamState *)[markers objectAtIndex:marker];
	// restore stream state
	[self seek:[state index]];
	line = [state line];
	charPositionInLine = [state charPositionInLine];
}

- (void) release:(unsigned int) marker 
{
#warning Leaking memory here?
	// unwind any other markers made after m and release m
	markDepth = marker;
	// release this marker
	markDepth--;
}

/** consume() ahead until p==index; can't just set p=index as we must
*  update line and charPositionInLine.
*/
- (void) seek:(unsigned int) index 
{
	if ( index<=p ) {
		p = index; // just jump; don't update stream state (line, ...)
		return;
	}
	// seek forward, consume until p hits index
	while ( p<index ) {
		[self consume];
	}
}

- (NSString *) substringWithRange:(NSRange) theRange 
{
	return [data substringWithRange:theRange];
}

- (unsigned int) line 
{
	return line;
}

- (unsigned int) charPositionInLine 
{
	return charPositionInLine;
}

- (void) setLine:(unsigned int) theLine 
{
	line = theLine;
}

- (void) setCharPositionInLine:(unsigned int) thePos 
{
	charPositionInLine = thePos;
}

//---------------------------------------------------------- 
//  data 
//---------------------------------------------------------- 
- (NSString *) data
{
    return data; 
}

- (void) setData: (NSString *) aData
{
    if (data != aData) {
        [aData retain];
        [data release];
        data = aData;
    }
}

@end
