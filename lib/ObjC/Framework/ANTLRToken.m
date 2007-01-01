// [The "BSD licence"]
// Copyright (c) 2006-2007 Kay Roepke
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


#import "ANTLRToken.h"
#import <Foundation/NSDebug.h>

@implementation ANTLRToken

// return the singleton EOF Token 
+ (ANTLRToken *) eofToken
{
	static ANTLRToken *eofToken = nil;
	if (eofToken != nil) {
		return eofToken;
	}
	eofToken = [[ANTLRToken alloc] init];
	if (eofToken) {
		[eofToken setType:ANTLRTokenTypeEOF];
		return eofToken;
	}
	return nil;
}


// the default channel for this class of Tokens
+ (ANTLRTokenChannel) defaultChannel
{
	return ANTLRTokenChannelDefault;
}


// provide dummy implementations of the accessor methods.
- (NSString *) text
{
	return nil;
}
- (void) setText:(NSString *) theText
{
}

- (int) type
{
	return type;
}
- (void) setType: (int) aType
{
	type = aType;
}

- (unsigned int) line
{
	return 0;
}
- (void) setLine: (unsigned int) aLine
{
}

- (unsigned int) charPositionInLine
{
	return 0;
}
- (void) setCharPositionInLine: (unsigned int) aCharPositionInLine
{
}

- (unsigned int) channel
{
	return 0;
}
- (void) setChannel: (unsigned int) aChannel
{
}

- (unsigned int) tokenIndex
{
	return 0;
}
- (void) setTokenIndex: (unsigned int) aTokenIndex
{
}

#pragma mark NSCopying conformance

- (id) copyWithZone:(NSZone *)theZone
{
	ANTLRToken *copy = [[[self class] allocWithZone:theZone] init];
	if (copy) {
		[copy setType:type];
	}
	return copy;
}

- (NSString *) description
{
	if (type == ANTLRTokenTypeEOF) {
		return @"EOFToken";
	}
	return @"unknown token - something is foul";
}

@end
