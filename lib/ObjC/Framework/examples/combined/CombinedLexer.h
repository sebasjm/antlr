// $ANTLR 3.0b5 /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g 2006-11-12 21:51:45

#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>


#pragma mark Cyclic DFA start
#pragma mark Cyclic DFA end

#pragma mark Rule return scopes start
#pragma mark Rule return scopes end

#pragma mark Tokens
#define CombinedLexer_INT	5
#define CombinedLexer_EOF	-1
#define CombinedLexer_WS	6
#define CombinedLexer_Tokens	7
#define CombinedLexer_ID	4

@interface CombinedLexer : ANTLRLexer {
}


- (void) mID;
- (void) mINT;
- (void) mWS;
- (void) mTokens;



@end