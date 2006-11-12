// $ANTLR 3.0b5 /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/scopes/SymbolTable.g 2006-11-12 20:47:00

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