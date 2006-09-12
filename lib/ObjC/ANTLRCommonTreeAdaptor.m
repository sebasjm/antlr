//
//  ANTLRCommonTreeAdaptor.m
//  ANTLR
//
//  Created by Kay Röpke on 11.09.2006.
//  Copyright 2006 classDump. All rights reserved.
//

#import "ANTLRCommonTreeAdaptor.h"

@implementation ANTLRCommonTreeAdaptor

+ (id<ANTLRTree>) newTreeWithToken:(ANTLRToken *) payload
{
	// I simply don't get the warning here...gcc says:
	// warning: class 'ANTLRCommonToken' does not implement the 'ANTLRTree' protocol
	// IMHO that's FUBAR...
	return [[ANTLRCommonTree alloc] initWithToken:(ANTLRCommonToken *)payload];
}

+ (ANTLRToken *) newTokenWithToken:(ANTLRToken *)fromToken
{
	return [[ANTLRCommonToken alloc] initWithToken:(ANTLRCommonToken *)fromToken];
}

+ (ANTLRToken *) newTokenWithTokenType:(int)tokenType text:(NSString *)tokenText
{
	ANTLRCommonToken *newToken = [[ANTLRCommonToken alloc] init];
	[newToken setType:tokenType];
	[newToken setText:tokenText];
	return newToken;
}

+ (void) setBoundariesForTree:(id<ANTLRTree>)aTree fromToken:(ANTLRToken *)startToken toToken:(ANTLRToken *)stopToken
{
	ANTLRCommonTree *tmpTree = (ANTLRCommonTree *)aTree;
	[tmpTree setStartIndex:[startToken tokenIndex]];
	[tmpTree setStopIndex:[stopToken tokenIndex]];
		
}

+ (int) tokenStartIndexForTree:(id<ANTLRTree>)aTree
{
	return [(ANTLRCommonTree *)aTree startIndex];
}

+ (int) tokenStopIndexForTree:(id<ANTLRTree>)aTree
{
	return [(ANTLRCommonTree *)aTree stopIndex];
}

@end
