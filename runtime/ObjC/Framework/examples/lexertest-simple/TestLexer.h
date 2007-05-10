// $ANTLR 3.0b5 /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/lexertest-simple/Test.g 2006-11-12 22:57:12

#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>


#pragma mark Cyclic DFA start
#pragma mark Cyclic DFA end

#pragma mark Rule return scopes start
#pragma mark Rule return scopes end

#pragma mark Tokens
#define TestLexer_LETTER	4
#define TestLexer_EOF	-1
#define TestLexer_Tokens	7
#define TestLexer_DIGIT	5
#define TestLexer_ID	6

@interface TestLexer : ANTLRLexer {
}


- (void) mID;
- (void) mDIGIT;
- (void) mLETTER;
- (void) mTokens;



@end