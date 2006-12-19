#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>
#import "SimpleCLexer.h"
#import "SimpleCParser.h"
#import "SimpleCTP.h"

int main() {
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	
	NSString *string = [NSString stringWithContentsOfFile:@"examples/simplecTreeParser/input"];
	NSLog(@"input is : %@", string);
	ANTLRStringStream *stream = [[ANTLRStringStream alloc] initWithStringNoCopy:string];
	SimpleCLexer *lexer = [[SimpleCLexer alloc] initWithCharStream:stream];
	
	//	ANTLRToken *currentToken;
	//	while (currentToken = [lexer nextToken]) {
	//		NSLog(@"%@", currentToken);
	//	}
	
	ANTLRCommonTokenStream *tokenStream = [[ANTLRCommonTokenStream alloc] initWithTokenSource:lexer];
	SimpleCParser *parser = [[SimpleCParser alloc] initWithTokenStream:tokenStream];
	ANTLRCommonTree *program_tree = [[parser program] tree];
	NSLog(@"%@", [program_tree treeDescription]);
	ANTLRCommonTreeNodeStream *treeStream = [[ANTLRCommonTreeNodeStream alloc] initWithTree:program_tree];
	SimpleCTP *walker = [[SimpleCTP alloc] initWithTreeNodeStream:treeStream];
	[walker program];
	[lexer release];
	[stream release];
	[tokenStream release];
	[parser release];
	[treeStream release];
	[walker release];
	
	[pool release];
	return 0;
}