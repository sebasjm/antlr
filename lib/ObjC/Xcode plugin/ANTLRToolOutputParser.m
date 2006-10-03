//
//  ANLTRToolOutputParser.m
//  Xcode plugin
//
//  Created by Kay Röpke on 02.10.2006.
//  Copyright 2006 classDump. All rights reserved.
//

#import "ANTLRToolOutputParser.h"
#import "RegularExpressions.h"

TSRegularExpression *loc_of_something = nil;
TSRegularExpression *syntax_error = nil;

@implementation ANTLRToolOutputParser

+ (void)initialize
{
	loc_of_something = [[TSRegularExpression alloc] initWithExpressionString:@"^(.+?):([0-9]+):[0-9]+:(.+)$"];
	//11:8: syntax error: antlr: /Users/kroepke/Projects/scratch/antlrplugintest/TestT.g:11:8: unexpected token: +
	syntax_error = [[TSRegularExpression alloc] initWithExpressionString:@"^([0-9]+):([0-9]+): syntax error: antlr: (.*?):[0-9]+:[0-9]+: (.*)$"];
}


- (id)initWithNextOutputStream:(id)nextOutputStream
{
	if ((self = [super initWithNextOutputStream:nextOutputStream])) {
	}
	return self;
}

- (void) setDelegate:(id)delegate
{
	NSLog(@"my delegate is: %@", delegate);
	[super setDelegate:delegate];
}

- (void)writeBytes:(const char *)string length:(unsigned int)length
{
	NSString* outputLine = [[[NSString alloc] initWithBytes:string length:length encoding:NSASCIIStringEncoding] autorelease];
	NSArray *captures;
	NSLog(@"examining line: %@", outputLine);
	if (nil != (captures = [syntax_error subexpressionsForString:outputLine])) {
		// we found a syntax error from ANTLR
		NSString *fileName = [captures objectAtIndex:3];
		int line = [[captures objectAtIndex:1] intValue];
		NSString *message = [captures objectAtIndex:4];
		NSLog(@"found syntax error: %@", captures);
		if (line != 0)
			[[self delegate] parser:self foundMessageOfType:1	// errors
							  title:[message UTF8String]
					  forFileAtPath:[fileName UTF8String]
						 lineNumber:line];
	} else if (nil != (captures = [loc_of_something subexpressionsForString:outputLine])) {
		// we found a message from ANTLR saying something about a filename, line, col
		NSString *fileName = [captures objectAtIndex:1];
		int line = [[captures objectAtIndex:2] intValue];
		NSString *message = [captures objectAtIndex:3];
		NSLog(@"found something: %@", captures);
		if (line != 0)
			[[self delegate] parser:self foundMessageOfType:2	// warnings
							  title:[message UTF8String]
					  forFileAtPath:[fileName UTF8String]
						 lineNumber:line];
	}
	[super writeBytes:string length:length];
}

@end
