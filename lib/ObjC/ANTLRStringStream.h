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


#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLRCharStream.h>

@interface ANTLRStringStream : NSObject < ANTLRCharStream > {
	NSMutableArray *markers;
	NSString *data;
	
	unsigned  p;
	unsigned  line;
	unsigned  charPositionInLine;
	unsigned  markDepth;
}

- (id) init;

/** Copy data in string to a local char array */
- (id) initWithString:(NSString *) theString;

/** This is the preferred constructor as no data is copied */
- (id) initWithStringNoCopy:(NSString *) theString;

- (void) dealloc;

/** Reset the stream so that it's in the same state it was
*  when the object was created *except* the data array is not
*  touched.
*/
- (void) reset;
- (void) consume;

- (int) LA:(int) i;

/** Return the current input symbol index 0..n where n indicates the
*  last symbol has been read.
*/
- (unsigned int) index;
- (unsigned int) count;

- (unsigned int) mark;

- (void) rewind:(unsigned int) marker;

- (void) release:(unsigned int) marker;

/** consume ahead until p==index; can't just set p=index as we must
*  update line and charPositionInLine.
*/
- (void) seek:(unsigned int) index;

- (NSString *) substringWithRange:(NSRange) theRange;

- (unsigned int) line;
- (void) setLine:(unsigned int) theLine;
- (unsigned int) charPositionInLine;
- (void) setCharPositionInLine:(unsigned int) thePos;

- (NSString *) data;
- (void) setData: (NSString *) aData;


@end
