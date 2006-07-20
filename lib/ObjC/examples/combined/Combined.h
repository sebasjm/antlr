// $ANTLR 3.0b2 Combined.g 2006-07-08 11:59:53

#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>


#pragma mark Tokens
#define Combined_INT	5
#define Combined_WS	6
#define Combined_EOF	-1
#define Combined_ID	4

@interface Combined : ANTLRParser {
	NSArray *tokenNames;
	NSString *grammarFileName;


 }


  - (void) stat;
   - (void) identifier;
  


@end