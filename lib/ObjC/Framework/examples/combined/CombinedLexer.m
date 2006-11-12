// $ANTLR 3.0b5 /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g 2006-11-12 21:51:45

#import "CombinedLexer.h"
#pragma mark Cyclic DFAs

/** As per Terence: No returns for lexer rules!
#pragma mark Rule return scopes start
#pragma mark Rule return scopes end
*/
@implementation CombinedLexer


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


- (void) mID
{
	@try {
		int _type = CombinedLexer_ID;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:14:9: ( ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'0'..'9'|'_'))* ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:14:9: ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'0'..'9'|'_'))* // alt
		{
		if (([input LA:1]>='A' && [input LA:1]<='Z')||[input LA:1]=='_'||([input LA:1]>='a' && [input LA:1]<='z')) {
			[input consume];

		} else {
			ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
			[self recover:mse];	@throw mse;
		}

		do {
		    int alt1=2;
		    {
		    	int LA1_0 = [input LA:1];
		    	if ( (LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z') ) {
		    		alt1 = 1;
		    	}

		    }
		    switch (alt1) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:14:34: ('a'..'z'|'A'..'Z'|'0'..'9'|'_') // alt
			    {
			    if (([input LA:1]>='0' && [input LA:1]<='9')||([input LA:1]>='A' && [input LA:1]<='Z')||[input LA:1]=='_'||([input LA:1]>='a' && [input LA:1]<='z')) {
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

		if ( token == nil ) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	return;
}
// $ANTLR end ID


- (void) mINT
{
	@try {
		int _type = CombinedLexer_INT;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:17:9: ( ( '0' .. '9' )+ ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:17:9: ( '0' .. '9' )+ // alt
		{
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:17:9: ( '0' .. '9' )+	// positiveClosureBlock
		int cnt2=0;

		do {
		    int alt2=2;
		    {
		    	int LA2_0 = [input LA:1];
		    	if ( (LA2_0>='0' && LA2_0<='9') ) {
		    		alt2 = 1;
		    	}

		    }
		    switch (alt2) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:17:10: '0' .. '9' // alt
			    {
			    [self matchRangeFromChar:'0' to:'9'];

			    }
			    break;

			default :
			    if ( cnt2 >= 1 )  goto loop2;
					ANTLREarlyExitException *eee = [ANTLREarlyExitException exceptionWithStream:input decisionNumber:2];
					@throw eee;
		    }
		    cnt2++;
		} while (YES); loop2: ;


		}

		if ( token == nil ) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	return;
}
// $ANTLR end INT


- (void) mWS
{
	@try {
		int _type = CombinedLexer_WS;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:20:9: ( ( (' '|'\\t'|'\\r'|'\\n'))+ ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:20:9: ( (' '|'\\t'|'\\r'|'\\n'))+ // alt
		{
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:20:9: ( (' '|'\\t'|'\\r'|'\\n'))+	// positiveClosureBlock
		int cnt3=0;

		do {
		    int alt3=2;
		    {
		    	int LA3_0 = [input LA:1];
		    	if ( (LA3_0>='\t' && LA3_0<='\n')||LA3_0=='\r'||LA3_0==' ' ) {
		    		alt3 = 1;
		    	}

		    }
		    switch (alt3) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:20:13: (' '|'\\t'|'\\r'|'\\n') // alt
			    {
			    if (([input LA:1]>='\t' && [input LA:1]<='\n')||[input LA:1]=='\r'||[input LA:1]==' ') {
			    	[input consume];

			    } else {
			    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
			    	[self recover:mse];	@throw mse;
			    }


			    }
			    break;

			default :
			    if ( cnt3 >= 1 )  goto loop3;
					ANTLREarlyExitException *eee = [ANTLREarlyExitException exceptionWithStream:input decisionNumber:3];
					@throw eee;
		    }
		    cnt3++;
		} while (YES); loop3: ;

		 _channel=99; 

		}

		if ( token == nil ) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	return;
}
// $ANTLR end WS

- (void) mTokens
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:1:10: ( ID | INT | WS ) //ruleblock
    int alt4=3;
    switch ([input LA:1]) {
    	case 'A':
    	case 'B':
    	case 'C':
    	case 'D':
    	case 'E':
    	case 'F':
    	case 'G':
    	case 'H':
    	case 'I':
    	case 'J':
    	case 'K':
    	case 'L':
    	case 'M':
    	case 'N':
    	case 'O':
    	case 'P':
    	case 'Q':
    	case 'R':
    	case 'S':
    	case 'T':
    	case 'U':
    	case 'V':
    	case 'W':
    	case 'X':
    	case 'Y':
    	case 'Z':
    	case '_':
    	case 'a':
    	case 'b':
    	case 'c':
    	case 'd':
    	case 'e':
    	case 'f':
    	case 'g':
    	case 'h':
    	case 'i':
    	case 'j':
    	case 'k':
    	case 'l':
    	case 'm':
    	case 'n':
    	case 'o':
    	case 'p':
    	case 'q':
    	case 'r':
    	case 's':
    	case 't':
    	case 'u':
    	case 'v':
    	case 'w':
    	case 'x':
    	case 'y':
    	case 'z':
    		alt4 = 1;
    		break;
    	case '0':
    	case '1':
    	case '2':
    	case '3':
    	case '4':
    	case '5':
    	case '6':
    	case '7':
    	case '8':
    	case '9':
    		alt4 = 2;
    		break;
    	case '\t':
    	case '\n':
    	case '\r':
    	case ' ':
    		alt4 = 3;
    		break;
    default:
     {
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:4 state:0 stream:input];
    	@throw nvae;

    	}}
    switch (alt4) {
    	case 1 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:1:10: ID // alt
    	    {
    	    [self mID];



    	    }
    	    break;
    	case 2 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:1:13: INT // alt
    	    {
    	    [self mINT];



    	    }
    	    break;
    	case 3 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:1:17: WS // alt
    	    {
    	    [self mWS];



    	    }
    	    break;

    }

}

@end