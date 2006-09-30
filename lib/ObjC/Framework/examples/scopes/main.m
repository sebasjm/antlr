#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>
#import "SymtabTestParserLexer.h"
#import "SymtabTestParser.h"

int main() {
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	
	NSString *string = [NSString stringWithContentsOfFile:@"input"];
	NSLog(@"input is : %@", string);
	ANTLRStringStream *stream = [[ANTLRStringStream alloc] initWithStringNoCopy:string];
	SymtabTestParserLexer *lexer = [[SymtabTestParserLexer alloc] initWithCharStream:stream];
	
	//	ANTLRToken *currentToken;
	//	while (currentToken = [lexer nextToken]) {
	//		NSLog(@"%@", currentToken);
	//	}
	
	ANTLRCommonTokenStream *tokenStream = [[ANTLRCommonTokenStream alloc] initWithTokenSource:lexer];
	SymtabTestParser *parser = [[SymtabTestParser alloc] initWithTokenStream:tokenStream];
	[parser prog];
	[lexer release];
	[stream release];
	[tokenStream release];
	[parser release];
	
	[pool release];
	return 0;
}