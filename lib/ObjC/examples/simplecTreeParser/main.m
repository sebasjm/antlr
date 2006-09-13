#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>
#import "SimpleCLexer.h"
#import "SimpleC.h"

int main() {
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	
	NSString *string = [NSString stringWithContentsOfFile:@"input"];
	NSLog(@"input is : %@", string);
	ANTLRStringStream *stream = [[ANTLRStringStream alloc] initWithStringNoCopy:string];
	SimpleCLexer *lexer = [[SimpleCLexer alloc] initWithCharStream:stream];
	
	//	ANTLRToken *currentToken;
	//	while (currentToken = [lexer nextToken]) {
	//		NSLog(@"%@", currentToken);
	//	}
	
	ANTLRCommonTokenStream *tokenStream = [[ANTLRCommonTokenStream alloc] initWithTokenSource:lexer];
	SimpleC *parser = [[SimpleC alloc] initWithTokenStream:tokenStream];
	ANTLRCommonTree *program_tree = [[parser program] tree];
	NSLog(@"%@", [program_tree treeDescription]);
	[lexer release];
	[stream release];
	[tokenStream release];
	[parser release];
	
	[pool release];
	return 0;
}