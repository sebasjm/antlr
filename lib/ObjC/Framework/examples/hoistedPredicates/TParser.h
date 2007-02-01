// $ANTLR 3.0b6 T.g 2007-02-01 01:27:58

#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>


#pragma mark Cyclic DFA

#pragma mark Tokens
#define TParser_INT	5
#define TParser_WS	6
#define TParser_EOF	-1
#define TParser_ID	4

#pragma mark Dynamic Global Scopes

#pragma mark Dynamic Rule Scopes

#pragma mark Rule Return Scopes


@interface TParser : ANTLRParser {

					


	/** With this true, enum is seen as a keyword.  False, it's an identifier */
	BOOL enableEnum;

 }


- (void) stat;
- (void) identifier;
- (void) enumAsKeyword;
- (void) enumAsID;



@end