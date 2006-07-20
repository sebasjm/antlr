#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>
#import "SimpleCLexer.h"
#import "SimpleC.h"
#import <Foundation/NSDebug.h>

int main() {
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];

	NSString *string = [NSString stringWithContentsOfFile:@"input"];
	NSLog(@"input is: %@", string);
	ANTLRStringStream *stream = [[ANTLRStringStream alloc] initWithStringNoCopy:string];
	SimpleCLexer *lexer = [[SimpleCLexer alloc] initWithCharStream:stream];

//	ANTLRToken *currentToken;
//	while (currentToken = [lexer nextToken]) {
//		NSLog(@"%@", currentToken);
//	}
	
	ANTLRCommonTokenStream *tokenStream = [[ANTLRCommonTokenStream alloc] initWithTokenSource:lexer];
	SimpleC *parser = [[SimpleC alloc] initWithTokenStream:tokenStream];
	[parser program];
	[lexer release];
	[stream release];
	[tokenStream release];
	[parser release];

	[pool release];
	return 0;
}