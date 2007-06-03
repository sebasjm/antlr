// $ANTLR 3.0 Test.gl 2007-06-02 22:06:43

#import "TestLexer.h"
#pragma mark Cyclic DFAs

/** As per Terence: No returns for lexer rules!
#pragma mark Rule return scopes start
#pragma mark Rule return scopes end
*/
@implementation TestLexer


- (id) initWithCharStream:(id<ANTLRCharStream>)anInput
{
	if (nil!=(self = [super initWithCharStream:anInput])) {
	}
	return self;
}

- (void) dealloc
{
	[super dealloc];
}

- (NSString *) grammarFileName
{
	return @"Test.gl";
}


- (void) mID
{
	@try {
		ruleNestingLevel++;
		int _type = TestLexer_ID;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// Test.gl:8:6: ( LETTER ( LETTER | DIGIT )* ) // ruleBlockSingleAlt
		// Test.gl:8:6: LETTER ( LETTER | DIGIT )* // alt
		{
		[self mLETTER];


		do {
		    int alt1=2;
		    {
		    	int LA1_0 = [input LA:1];
		    	if ( (LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||(LA1_0>='a' && LA1_0<='z') ) {
		    		alt1 = 1;
		    	}

		    }
		    switch (alt1) {
			case 1 :
			    // Test.gl: // alt
			    {
			    if (([input LA:1]>='0' && [input LA:1]<='9')||([input LA:1]>='A' && [input LA:1]<='Z')||([input LA:1]>='a' && [input LA:1]<='z')) {
			    	[input consume];

			    } else {
			    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
			    	[self recover:mse];	@throw mse;
			    }


			    }
			    break;

			default :
			    goto loop1;
		    }
		} while (YES); loop1: ;


		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	return;
}
// $ANTLR end ID


- (void) mDIGIT
{
	@try {
		ruleNestingLevel++;
		// Test.gl:11:18: ( '0' .. '9' ) // ruleBlockSingleAlt
		// Test.gl:11:18: '0' .. '9' // alt
		{
		[self matchRangeFromChar:'0' to:'9'];

		}

	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	return;
}
// $ANTLR end DIGIT


- (void) mLETTER
{
	@try {
		ruleNestingLevel++;
		// Test.gl:15:4: ( 'a' .. 'z' | 'A' .. 'Z' ) // ruleBlockSingleAlt
		// Test.gl: // alt
		{
		if (([input LA:1]>='A' && [input LA:1]<='Z')||([input LA:1]>='a' && [input LA:1]<='z')) {
			[input consume];

		} else {
			ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
			[self recover:mse];	@throw mse;
		}


		}

	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	return;
}
// $ANTLR end LETTER

- (void) mTokens
{
    // Test.gl:1:10: ( ID ) // ruleBlockSingleAlt
    // Test.gl:1:10: ID // alt
    {
    [self mID];



    }


}

@end