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

#import "ANTLRBitSet.h"

@implementation ANTLRBitSet

// initializer
#pragma mark Initializer

- (ANTLRBitSet *) init
{
	if (nil != (self = [super init])) {
		bitVector = CFBitVectorCreateMutable(kCFAllocatorDefault,0);
	}
	return self;
}

- (ANTLRBitSet *) initWithBitVector:(CFMutableBitVectorRef)theBitVector
{
	if (nil != (self = [super init])) {
		bitVector = theBitVector;
	}
	return self;
}

- (ANTLRBitSet *) initWithBits:(const unsigned long long *)theBits count:(unsigned int)longCount
{
	if (nil != (self = [self init])) {
		int longNo;
		CFIndex bitIdx;
		CFBitVectorSetCount(bitVector,sizeof(unsigned long long)*8*longCount);

		for (longNo = 0; longNo < longCount; longNo++) {
			for (bitIdx = 0; bitIdx < sizeof(unsigned long long)*8; bitIdx++) { 
				if (theBits[longNo] & (1LL << bitIdx)) {
					CFBitVectorSetBitAtIndex(bitVector,bitIdx+(longNo*sizeof(unsigned long long)*8),1);
				}
			}
		}
	}
	return self;
}

- (ANTLRBitSet *) initWithArrayOfBits:(NSArray *)theArray
{
	if (nil != (self = [self init])) {
		NSEnumerator *enumerator = [theArray objectEnumerator];
		id value;
		int bit = 0;
		while (value = [enumerator nextObject]) {
			if ([value boolValue] == YES) {
				CFBitVectorSetBitAtIndex(bitVector,bit,1);
			}
			bit++;
		}
	}
	return self;
}

- (void) dealloc
{
	CFRelease(bitVector);
	[super dealloc];
}

	// operations
#pragma mark Operations
- (ANTLRBitSet *) or:(ANTLRBitSet *) aBitSet
{
	ANTLRBitSet *bitsetCopy = [self copy];
	[bitsetCopy orInPlace:aBitSet];
	return bitsetCopy;
}

- (void) orInPlace:(ANTLRBitSet *) aBitSet
{
	CFIndex selfCnt = CFBitVectorGetCount(bitVector);
	CFMutableBitVectorRef otherBitVector = [aBitSet _bitVector];
	CFIndex otherCnt = CFBitVectorGetCount(otherBitVector);
	CFIndex maxBitCnt = selfCnt > otherCnt ? selfCnt : otherCnt;
	CFBitVectorSetCount(bitVector,maxBitCnt);
	
	CFIndex currIdx;
	for (currIdx = 0; currIdx < maxBitCnt; currIdx++) {
		if (CFBitVectorGetBitAtIndex(bitVector,currIdx) | CFBitVectorGetBitAtIndex(otherBitVector,currIdx)) {
			CFBitVectorSetBitAtIndex(bitVector,currIdx,1);
		}
	}
}

- (void) add:(unsigned int) bit
{
	if (bit > CFBitVectorGetCount(bitVector))
		CFBitVectorSetCount(bitVector,bit);
	CFBitVectorSetBitAtIndex(bitVector,bit,1);
}

- (void) remove:(unsigned int) bit
{
	CFBitVectorSetBitAtIndex(bitVector,bit,0);
}

- (unsigned int) size
{
	return CFBitVectorGetCount(bitVector);
}

- (void) setSize:(unsigned int) noOfWords
{
	// not supported - not needed :)
}

#pragma mark Informational
- (unsigned long long) bitMask:(unsigned int) bitNumber
{
	return 1LL << bitNumber;
}

- (BOOL) isMember:(unsigned int) bitNumber
{
	return CFBitVectorGetBitAtIndex(bitVector,bitNumber) ? YES : NO;
}

- (BOOL) isNil
{
	return CFBitVectorGetCountOfBit(bitVector,CFRangeMake(0,CFBitVectorGetCount(bitVector)),1)==0 ? YES : NO;
}

- (NSString *) toString
{
	CFIndex length = CFBitVectorGetCount(bitVector);
	CFIndex currBit;
	NSMutableString *descString = [[NSMutableString alloc] initWithString:@"{"];
	BOOL haveInsertedBit = false;
	for (currBit = 0; currBit < length; currBit++) {
		if (CFBitVectorGetBitAtIndex(bitVector,currBit)) {
			if (haveInsertedBit) {
				[descString appendString:@","];
			}
			[descString appendString:[NSString stringWithFormat:@"%d", currBit]];
			haveInsertedBit = YES;
		}
	}
	[descString appendString:@"}"];
	return descString;
}

	// NSCopying
#pragma mark NSCopying support

- (id) copyWithZone:(NSZone *) theZone
{
	ANTLRBitSet *newBitSet = [[ANTLRBitSet alloc] initWithBitVector:CFBitVectorCreateMutableCopy(kCFAllocatorDefault,0,bitVector)];
	return newBitSet;
}

- (CFMutableBitVectorRef) _bitVector
{
	return bitVector;
}


@end
