// $ANTLR 3.0b6 SymbolTable.g 2007-02-01 01:28:00

#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>


#pragma mark Cyclic DFA

#pragma mark Tokens
#define SymbolTableParser_INT	5
#define SymbolTableParser_WS	6
#define SymbolTableParser_EOF	-1
#define SymbolTableParser_ID	4

#pragma mark Dynamic Global Scopes
@interface SymbolTableParserSymbolsScope : NSObject {
	NSMutableArray *names;
}
// use KVC to access attributes!
@end

#pragma mark Dynamic Rule Scopes

#pragma mark Rule Return Scopes


@interface SymbolTableParser : ANTLRParser {

	NSMutableArray *SymbolTableParser_Symbols_stack;
							


	int level;

 }


- (void) prog;
- (void) globals;
- (void) method;
- (void) block;
- (void) stat;
- (void) decl;



@end