#import <Cocoa/Cocoa.h>
#import "TestLexer.h"
#import <ANTLR/ANTLR.h>

int main(int argc, const char * argv[])
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	
	ANTLRStringStream *stream = [[ANTLRStringStream alloc] initWithStringNoCopy:@"abB9Cdd44"];
	TestLexer *lexer = [[TestLexer alloc] initWithCharStream:stream];
	ANTLRToken *currentToken;
	while (currentToken = [lexer nextToken]) {
		NSLog(@"%@", currentToken);
	}
	[lexer release];
	[stream release];
	
	[pool release];
	return 0;
}