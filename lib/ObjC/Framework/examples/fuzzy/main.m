#import <Cocoa/Cocoa.h>
#import "FuzzyJava.h"
#import <ANTLR/ANTLR.h>

int main(int argc, const char * argv[])
{
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	NSString *string = [NSString stringWithContentsOfFile:@"input"];
	NSLog(@"%@", string);
	ANTLRStringStream *stream = [[ANTLRStringStream alloc] initWithStringNoCopy:string];
	FuzzyJava *lexer = [[FuzzyJava alloc] initWithCharStream:stream];
	ANTLRToken *currentToken;
	while (currentToken = [lexer nextToken]) {
//		NSLog(@"%@", currentToken);
	}
	[lexer release];
	[stream release];
	
	[pool release];
	return 0;
}