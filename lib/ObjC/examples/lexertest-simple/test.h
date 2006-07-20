// $ANTLR 3.0ea9 lexer.g 2006-05-07 14:00:52

#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>

#pragma mark Cyclic DFA start
// Cyclic DFA state subclasses
#pragma mark C y c l i c   D F A   S t a t e s


// Cyclic DFAs newstyle
#pragma mark C y c l i c   D F A

#pragma mark Cyclic DFA end

#pragma mark Tokens
#define Test_LETTER	4
#define Test_EOF	-1
#define Test_Tokens	7
#define Test_DIGIT	5
#define Test_ID	6

@interface TestLexer : ANTLRLexer {
}

- (void) mID; // TODO: parameterScope
- (void) mDIGIT; // TODO: parameterScope
- (void) mLETTER; // TODO: parameterScope
- (void) mTokens; // TODO: parameterScope



@end