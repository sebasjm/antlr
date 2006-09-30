//
//  BitSet.m
//  ANTLR
//
//  Created by Kay Röpke on 30.01.2006.
//  Copyright 2006 classDump. All rights reserved.
//

#import "TestBitSet.h"
#import "ANTLRBitSet.h"
#import <CoreFoundation/CoreFoundation.h>
#import <CoreFoundation/CFBitVector.h>

@implementation TestBitSet

- (void) testBitSetCreationFromLongs
{
	static const unsigned long long bitData[] = {3LL, 1LL};
	ANTLRBitSet *bitSet = [[ANTLRBitSet alloc] initWithBits:bitData count:2];
	CFMutableBitVectorRef bitVector = [bitSet _bitVector];
	NSLog(@"%@", [bitSet toString]);
	NSLog(@"countofbit %d getcount %d", CFBitVectorGetCountOfBit(bitVector,CFRangeMake(0,CFBitVectorGetCount(bitVector)),1), CFBitVectorGetCount(bitVector));
	STAssertEquals(CFBitVectorGetCountOfBit(bitVector,CFRangeMake(0,CFBitVectorGetCount(bitVector)),1), (CFIndex)3, @"There should be three bits set in bitvector. But I have %d",CFBitVectorGetCountOfBit(bitVector,CFRangeMake(0,CFBitVectorGetCount(bitVector)),1));
}

@end
