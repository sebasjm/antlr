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



#import "ANTLRBaseRecognizer.h"
#import "ANTLRBitSet.h"
#import "ANTLRCommonToken.h"

@class ANTLRRuleReturnScope;
@implementation ANTLRBaseRecognizer

- (id) init
{
	if (nil != (self = [super init])) {
		following = [[NSMutableArray alloc] init];
		errorRecovery = NO;
		lastErrorIndex = -1;
		failed = NO;
		backtracking = 0;
		fsp = -1;
		ruleMemo = [[NSMutableArray alloc] initWithCapacity:ANTLR_INITIAL_FOLLOW_STACK_SIZE];
	}
	return self;
}

- (void) dealloc
{
	[following release];
	[ruleMemo release];
	[super dealloc];
}

- (BOOL) isFailed
{
	return failed;
}

- (void) setIsFailed: (BOOL) flag
{
	failed = flag;
}

- (BOOL) isBacktracking
{
	return backtracking > 0;
}

- (int) backtrackingLevel
{
	return backtracking;
}

- (void) reset
{
	errorRecovery = NO;
	lastErrorIndex = -1;
	failed = NO;
	backtracking = 0;
	fsp = -1;
	[following removeAllObjects]; 
	[ruleMemo removeAllObjects];
}

- (void) match:(id<ANTLRIntStream>)input 
	 tokenType:(ANTLRTokenType) ttype
		follow:(ANTLRBitSet *)follow
{
	if ([input LA:1] == ttype) {
		[input consume];
		errorRecovery = NO;
		failed = NO;
		return;
	}
	if (backtracking > 0) {
		failed = YES;
		return;
	}
	[self mismatch:input tokenType:ttype follow:follow];
}

- (void) mismatch:(id<ANTLRIntStream>)aStream tokenType:(int)aTType follow:(ANTLRBitSet *)aBitset
{
	ANTLRMismatchedTokenException *mte = [ANTLRMismatchedTokenException exceptionWithTokenType:aTType stream:aStream];
	[self recoverFromMismatchedToken:aStream exception:mte tokenType:aTType follow:aBitset];
}

- (void) matchAny:(id<ANTLRIntStream>)input
{
	errorRecovery = NO;
	failed = NO;
	[input consume];
}

- (void) reportError:(NSException *)e
{
	if (errorRecovery) {
		return;
	}
	errorRecovery = YES;
	[self displayRecognitionError:NSStringFromClass([self class]) tokenNames:[self tokenNames] exception:e];
}

- (void) displayRecognitionError:(NSString *)name tokenNames:(NSArray *)tokenNames exception:(NSException *)e
{
	NSLog(@"%@", [e description]);
}

- (void) recover:(id<ANTLRIntStream>)input exception:(NSException *)e
{
	if (lastErrorIndex == [input index]) {
		[input consume];
	}
	lastErrorIndex = [input index];
	ANTLRBitSet *followSet = [self computeErrorRecoverySet];
	[self beginResync];
	[self consumeUntil:input bitSet:followSet];
	[self endResync];
}

- (void) beginResync
{
}

- (void) endResync
{
}


- (ANTLRBitSet *)computeErrorRecoverySet
{
	return [self combineFollowsExact:NO];
}



- (ANTLRBitSet *)computeContectSensitiveRuleFOLLOW
{
	return [self combineFollowsExact:YES];
}

- (ANTLRBitSet *)combineFollowsExact:(BOOL)exact
{
	int top = fsp;
	ANTLRBitSet *followSet = [[[ANTLRBitSet alloc] init] autorelease];
	int i;
	for (i = top; i >= 0; i--) {
		ANTLRBitSet *localFollowSet = [following objectAtIndex:i];
		[followSet orInPlace:localFollowSet];
		if (exact && ![localFollowSet isMember:ANTLRTokenTypeEOR]) {
			break;
		}
	}
	[followSet remove:ANTLRTokenTypeEOR];
	return followSet;
}


- (void) recoverFromMismatchedToken:(id<ANTLRIntStream>)input 
						  exception:(NSException *)e 
						  tokenType:(ANTLRTokenType)ttype 
							 follow:(ANTLRBitSet *)follow
{
	if ([input LA:2] == ttype) {
		[self reportError:e];
		[self beginResync];
		[input consume];
		[self endResync];
		[input consume];
		return;
	}
	if (![self recoverFromMismatchedElement:input exception:e follow:follow]) {
		@throw e;
	}
}

- (void) recoverFromMismatchedSet:(id<ANTLRIntStream>)input
						exception:(NSException *)e
						   follow:(ANTLRBitSet *)follow
{
	// todo
	if (![self recoverFromMismatchedElement:input exception:e follow:follow]) {
		@throw e;
	}
}

- (BOOL) recoverFromMismatchedElement:(id<ANTLRIntStream>)input
							exception:(NSException *)e
							   follow:(ANTLRBitSet *)follow
{
	if (follow == nil) {
		return NO;
	}
	ANTLRBitSet *localFollow = follow;
	if ([follow isMember:ANTLRTokenTypeEOR]) {
		ANTLRBitSet *viableTokensFollowingThisRule = [self computeContectSensitiveRuleFOLLOW];
		ANTLRBitSet *localFollow = [follow or:viableTokensFollowingThisRule];
		[localFollow remove:ANTLRTokenTypeEOR];
	}
	if ([localFollow isMember:[input LA:1]]) {
		[self reportError:e];
		return YES;
	}
	return NO;
}

- (void) consumeUntil:(id<ANTLRIntStream>)input
			tokenType:(ANTLRTokenType)theTtype
{
	ANTLRTokenType ttype = [input LA:1];
	while (ttype != ANTLRTokenTypeEOF && ttype != theTtype) {
		[input consume];
		ttype = [input LA:1];
	}
}

- (void) consumeUntil:(id<ANTLRIntStream>)input
			   bitSet:(ANTLRBitSet *)bitSet
{
	ANTLRTokenType ttype = [input LA:1];
	while (ttype != ANTLRTokenTypeEOF && ![bitSet isMember:ttype]) {
		[input consume];
		ttype = [input LA:1];
	}
}

- (void) pushFollow:(ANTLRBitSet *)follow
{
	[following addObject:follow];
	fsp = [following count];
}


- (NSArray *) ruleInvocationStack
{
	return [self ruleInvocationStack:nil recognizer:[self class]];
}


- (NSArray *) ruleInvocationStack:(id) exception
					   recognizer:(Class) recognizerClass
{
	// todo
	return [NSArray arrayWithObject:[@"not implemented yet: " stringByAppendingString:NSStringFromClass(recognizerClass)]];
}


- (NSArray *) tokenNames
{
	return tokenNames;
}

- (NSString *) grammarFileName
{
	return grammarFileName;
}

- (NSArray *) toStrings:(NSArray *)tokens
{
	if (tokens == nil ) {
		return nil;
	}
	NSMutableArray *strings = [[[NSArray alloc] init] autorelease];
	NSEnumerator *tokensEnumerator = [tokens objectEnumerator];
	id value;
	while (nil != (value = [tokensEnumerator nextObject])) {
		[strings addObject:[(ANTLRToken *)value text]];
	}
	return strings;
}



- (NSArray *) toTemplates:(NSArray *)retvals
{
	return nil;
#warning Templates are not yet supported in ObjC!
/*	if (retvals == nil ) {
		return nil;
	}
	NSMutableArray *strings = [[[NSArray alloc] init] autorelease];
	NSEnumerator *retvalsEnumerator = [retvals objectEnumerator];
	id value;
	while (nil != (value = [retvalsEnumerator nextObject])) {
		[strings addObject:[(ANTLRRuleReturnScope *)value template]];
	}
	return strings;
*/
}


- (int) ruleMemoization:(int)ruleIndex startIndex:(int)ruleStartIndex
{
	if ([ruleMemo count] < ruleIndex) {
		[ruleMemo setObject:[NSMutableDictionary dictionary] forKey:[NSNumber numberWithInt:ruleIndex]];
	}
	NSNumber *stopIndexI = [ruleMemo objectForKey:[NSNumber numberWithInt:ruleIndex]];
	if (stopIndexI == nil) {
		return ANTLR_MEMO_RULE_UNKNOWN;
	} else {
		return [stopIndexI intValue];
	}
}

- (BOOL) alreadyParsedRule:(id<ANTLRIntStream>)input ruleIndex:(int)ruleIndex
{
	int stopIndex = [self ruleMemoization:ruleIndex startIndex:[input index]];
	if (stopIndex == ANTLR_MEMO_RULE_UNKNOWN) {
		return NO;
	}
	if (stopIndex == ANTLR_MEMO_RULE_FAILED) {
		failed = YES;
	} else {
		[input seek:stopIndex+1];
	}
	return YES;
}

- (void) memoize:(id<ANTLRIntStream>)input
	   ruleIndex:(int)ruleIndex
	  startIndex:(int)ruleStartIndex
{
	int stopTokenIndex = failed ? ANTLR_MEMO_RULE_FAILED : [input index]-1;
	if ([ruleMemo objectForKey:[NSNumber numberWithInt:ruleIndex]] == nil) {
		[ruleMemo setObject:[NSNumber numberWithInt:stopTokenIndex] forKey:[NSNumber numberWithInt:ruleStartIndex]];
	}
}

- (int) ruleMemoizationCacheSize
{
	int n = 0;
	
	NSEnumerator *ruleEnumerator = [ruleMemo objectEnumerator];
	id value;
	while ((value = [ruleEnumerator nextObject])) {
		n += [value count];
	}
	return n;
}

- (BOOL) evaluateSyntacticPredicate:(SEL)synpredFragment stream:(id<ANTLRIntStream>)input
{
    backtracking++;
	int start = [input mark];
    @try {
        [self performSelector:synpredFragment];
    }
    @catch (ANTLRRecognitionException *re) {
        NSLog(@"impossible synpred: %@", re);
    }
    BOOL success = !failed;
    [input rewind:start];
	backtracking--;
    failed = NO;
    return success;
}	

@end
