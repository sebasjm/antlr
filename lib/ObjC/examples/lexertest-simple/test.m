// $ANTLR 3.0ea9 lexer.g 2006-05-07 14:00:52

#import "Test.h"
#pragma mark Cyclic DFA start
#pragma mark Cyclic DFA end

@implementation TestLexer


- (id) initWithCharStream:(id<ANTLRCharStream>)anInput
{
	if (nil!=(self = [super initWithCharStream:anInput])) {
	}
	return self;
}

- (void) mID
{
    int type = Test_ID;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // lexer.g:8:17: ( LETTER ( LETTER | DIGIT )* )
    // lexer.g:8:17: LETTER ( LETTER | DIGIT )*
    {
    [self mLETTER];


    do {
        int alt1=3;
        int LA1_0 = [input LA:1];
        if ( (LA1_0>='A' && LA1_0<='Z')
        ||(LA1_0>='a' && LA1_0<='z')
         ) {
        	alt1 = 1;
        }
        else if ( (LA1_0>='0' && LA1_0<='9')
         ) {
        	alt1 = 2;
        }


        switch (alt1) {
    	case 1 :
    	    // lexer.g:8:25: LETTER
    	    {
    	    [self mLETTER];



    	    }
    	    break;
    	case 2 :
    	    // lexer.g:8:34: DIGIT
    	    {
    	    [self mDIGIT];



    	    }
    	    break;

    	default :
    	    goto loop1;
        }
    } while (true); loop1: ;


    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end ID

- (void) mDIGIT
{
    // lexer.g:11:25: ( '0' .. '9' )
    // lexer.g:11:25: '0' .. '9'
    {
    [self matchRangeFromChar:'0' to:'9'];

    }

}
// $ANTLR end DIGIT

- (void) mLETTER
{
    // lexer.g:15:9: ( ('a'..'z'|'A'..'Z'))
    // lexer.g:15:17: ('a'..'z'|'A'..'Z')
    {
    if (([input LA:1]>='A' && [input LA:1]<='Z')||([input LA:1]>='a' && [input LA:1]<='z')) {
    	[input consume];
    	errorRecovery = NO;
    } else {
    #warning Think about exceptions!
    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
    	[self recover:mse];	@throw mse;
    }


    }

}
// $ANTLR end LETTER

- (void) mTokens
{
    // lexer.g:1:10: ( ID )
    // lexer.g:1:10: ID
    {
    [self mID];



    }


}


@end