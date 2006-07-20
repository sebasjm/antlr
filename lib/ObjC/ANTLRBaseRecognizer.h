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
#import <Foundation/Foundation.h>

#import <ANTLR/ANTLRIntStream.h>

// This is an abstract superclass for lexers and parsers.

#define ANTLR_MEMO_RULE_FAILED -2
#define ANTLR_MEMO_RULE_UNKNOWN -1
#define ANTLR_INITIAL_FOLLOW_STACK_SIZE 100

@class ANTLRBitSet;
@class ANTLRGrammarFragment;
#import <ANTLR/ANTLRRecognitionException.h>
#import <ANTLR/ANTLRMismatchedTokenException.h>
#import <ANTLR/ANTLRToken.h>

@interface ANTLRBaseRecognizer : NSObject {
	NSMutableArray *following;
	int fsp;
	BOOL errorRecovery;
	int lastErrorIndex;
	BOOL failed;
	int backtracking;
	NSMutableDictionary *ruleMemo;
	
	NSArray *tokenNames;
	NSString *grammarFileName;
	
}

- (BOOL) isFailed;
- (void) setIsFailed: (BOOL) flag;

- (BOOL) isBacktracking;
- (int) backtrackingLevel;

- (id) init;

- (void) reset;

- (void) match:(id<ANTLRIntStream>)input tokenType:(ANTLRTokenType) ttype follow:(ANTLRBitSet *)follow;
- (void) matchAny:(id<ANTLRIntStream>)input;

- (void) reportError:(NSException *)e;
- (void) displayRecognitionError:(NSString *)name tokenNames:(NSArray *)tokenNames exception:(NSException *)e;

- (void) recover:(id<ANTLRIntStream>)input exception:(NSException *)e;

- (void) beginResync;
- (void) endResync;

- (ANTLRBitSet *)computeContectSensitiveRuleFOLLOW;
- (ANTLRBitSet *)combineFollowsExact:(BOOL) exact;
- (ANTLRBitSet *)computeErrorRecoverySet;

- (void) mismatch:(id<ANTLRIntStream>)aStream tokenType:(int)aTType follow:(ANTLRBitSet *)aBitset;
- (void) recoverFromMismatchedToken:(id<ANTLRIntStream>)input 
						  exception:(NSException *)e 
						  tokenType:(ANTLRTokenType)ttype 
							 follow:(ANTLRBitSet *)follow;

- (void) recoverFromMismatchedSet:(id<ANTLRIntStream>)input
						exception:(NSException *)e
						   follow:(ANTLRBitSet *)follow;

- (BOOL) recoverFromMismatchedElement:(id<ANTLRIntStream>)input
							exception:(NSException *)e
							   follow:(ANTLRBitSet *)follow;

- (void) consumeUntil:(id<ANTLRIntStream>)input
			tokenType:(ANTLRTokenType)ttype;
- (void) consumeUntil:(id<ANTLRIntStream>)input
			   bitSet:(ANTLRBitSet *)bitSet;


- (NSArray *) ruleInvocationStack;
- (NSArray *) ruleInvocationStack:(id) exception
					   recognizer:(Class) recognizerClass;

- (NSArray *) tokenNames;
- (NSString *) grammarFileName;

- (NSArray *) toStrings:(NSArray *)tokens;
- (NSArray *) toTemplates:(NSArray *)retvals;


- (int) ruleMemoization:(int)ruleIndex startIndex:(int)ruleStartIndex;

- (BOOL) alreadyParsedRule:(id<ANTLRIntStream>)input ruleIndex:(int)ruleIndex;

- (void) memoize:(id<ANTLRIntStream>)input
	   ruleIndex:(int)ruleIndex
	  startIndex:(int)ruleStartIndex;

- (int) ruleMemoizationCacheSize;

@end
