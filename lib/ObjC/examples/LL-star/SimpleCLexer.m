// $ANTLR 3.0b3 simplec.g 2006-07-20 00:49:55

#import "SimpleCLexer.h"
#pragma mark Cyclic DFA start
#pragma mark Cyclic DFA end



#pragma mark Rule return scopes start
#pragma mark Rule return scopes end
@implementation SimpleCLexer


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



- (void) mT7
{
	@try {
		int type = SimpleCLexer_T7;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:7:6: ( ';' ) // ruleBlockSingleAlt
		// simplec.g:7:6: ';' // alt
		{
		[self matchChar:';'];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T7



- (void) mT8
{
	@try {
		int type = SimpleCLexer_T8;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:8:6: ( '(' ) // ruleBlockSingleAlt
		// simplec.g:8:6: '(' // alt
		{
		[self matchChar:'('];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T8



- (void) mT9
{
	@try {
		int type = SimpleCLexer_T9;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:9:6: ( ',' ) // ruleBlockSingleAlt
		// simplec.g:9:6: ',' // alt
		{
		[self matchChar:','];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T9



- (void) mT10
{
	@try {
		int type = SimpleCLexer_T10;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:10:7: ( ')' ) // ruleBlockSingleAlt
		// simplec.g:10:7: ')' // alt
		{
		[self matchChar:')'];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T10



- (void) mT11
{
	@try {
		int type = SimpleCLexer_T11;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:11:7: ( 'int' ) // ruleBlockSingleAlt
		// simplec.g:11:7: 'int' // alt
		{
		[self matchString:@"int"];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T11



- (void) mT12
{
	@try {
		int type = SimpleCLexer_T12;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:12:7: ( 'char' ) // ruleBlockSingleAlt
		// simplec.g:12:7: 'char' // alt
		{
		[self matchString:@"char"];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T12



- (void) mT13
{
	@try {
		int type = SimpleCLexer_T13;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:13:7: ( 'void' ) // ruleBlockSingleAlt
		// simplec.g:13:7: 'void' // alt
		{
		[self matchString:@"void"];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T13



- (void) mT14
{
	@try {
		int type = SimpleCLexer_T14;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:14:7: ( '{' ) // ruleBlockSingleAlt
		// simplec.g:14:7: '{' // alt
		{
		[self matchChar:'{'];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T14



- (void) mT15
{
	@try {
		int type = SimpleCLexer_T15;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:15:7: ( '}' ) // ruleBlockSingleAlt
		// simplec.g:15:7: '}' // alt
		{
		[self matchChar:'}'];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T15



- (void) mT16
{
	@try {
		int type = SimpleCLexer_T16;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:16:7: ( 'for' ) // ruleBlockSingleAlt
		// simplec.g:16:7: 'for' // alt
		{
		[self matchString:@"for"];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T16



- (void) mT17
{
	@try {
		int type = SimpleCLexer_T17;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:17:7: ( '=' ) // ruleBlockSingleAlt
		// simplec.g:17:7: '=' // alt
		{
		[self matchChar:'='];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T17



- (void) mT18
{
	@try {
		int type = SimpleCLexer_T18;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:18:7: ( '==' ) // ruleBlockSingleAlt
		// simplec.g:18:7: '==' // alt
		{
		[self matchString:@"=="];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T18



- (void) mT19
{
	@try {
		int type = SimpleCLexer_T19;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:19:7: ( '<' ) // ruleBlockSingleAlt
		// simplec.g:19:7: '<' // alt
		{
		[self matchChar:'<'];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T19



- (void) mT20
{
	@try {
		int type = SimpleCLexer_T20;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:20:7: ( '+' ) // ruleBlockSingleAlt
		// simplec.g:20:7: '+' // alt
		{
		[self matchChar:'+'];



		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end T20



- (void) mID
{
	@try {
		int type = SimpleCLexer_ID;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:95:9: ( ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'0'..'9'|'_'))* ) // ruleBlockSingleAlt
		// simplec.g:95:9: ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'0'..'9'|'_'))* // alt
		{
		if (([input LA:1]>='A'/*lower*/ && [input LA:1]<='Z'/*upper/isorange*/)||[input LA:1]=='_'/*atom/iso*/||([input LA:1]>='a'/*lower*/ && [input LA:1]<='z'/*upper/isorange*/)) {
			[input consume];
			errorRecovery = NO;
		} else {
			ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
			[self recover:mse];	@throw mse;
		}

		do {
		    int alt1=2;
		    int LA1_0 = [input LA:1];
		    if ( (LA1_0>='0'/*lower*/ && LA1_0<='9'/*upper/range*/)||(LA1_0>='A'/*lower*/ && LA1_0<='Z'/*upper/range*/)||LA1_0=='_'/*atom/Test*/||(LA1_0>='a'/*lower*/ && LA1_0<='z'/*upper/range*/) ) {
		    	alt1 = 1;
		    }


		    switch (alt1) {
			case 1 :
			    // simplec.g:95:34: ('a'..'z'|'A'..'Z'|'0'..'9'|'_') // alt
			    {
			    if (([input LA:1]>='0'/*lower*/ && [input LA:1]<='9'/*upper/isorange*/)||([input LA:1]>='A'/*lower*/ && [input LA:1]<='Z'/*upper/isorange*/)||[input LA:1]=='_'/*atom/iso*/||([input LA:1]>='a'/*lower*/ && [input LA:1]<='z'/*upper/isorange*/)) {
			    	[input consume];
			    	errorRecovery = NO;
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

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end ID



- (void) mINT
{
	@try {
		int type = SimpleCLexer_INT;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:98:7: ( ( '0' .. '9' )+ ) // ruleBlockSingleAlt
		// simplec.g:98:7: ( '0' .. '9' )+ // alt
		{
		// simplec.g:98:7: ( '0' .. '9' )+	// positiveClosureBlock
		int cnt2=0;

		do {
		    int alt2=2;
		    int LA2_0 = [input LA:1];
		    if ( (LA2_0>='0'/*lower*/ && LA2_0<='9'/*upper/range*/) ) {
		    	alt2 = 1;
		    }


		    switch (alt2) {
			case 1 :
			    // simplec.g:98:8: '0' .. '9' // alt
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

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end INT



- (void) mWS
{
	@try {
		int type = SimpleCLexer_WS;
		int start = [self charIndex];
		int line = [self line];
		int charPosition = [self charPositionInLine];
		int channel = [ANTLRToken defaultChannel];
		// simplec.g:101:9: ( ( (' '|'\\t'|'\\r'|'\\n'))+ ) // ruleBlockSingleAlt
		// simplec.g:101:9: ( (' '|'\\t'|'\\r'|'\\n'))+ // alt
		{
		// simplec.g:101:9: ( (' '|'\\t'|'\\r'|'\\n'))+	// positiveClosureBlock
		int cnt3=0;

		do {
		    int alt3=2;
		    int LA3_0 = [input LA:1];
		    if ( (LA3_0>='\t'/*lower*/ && LA3_0<='\n'/*upper/range*/)||LA3_0=='\r'/*atom/Test*/||LA3_0==' '/*atom/Test*/ ) {
		    	alt3 = 1;
		    }


		    switch (alt3) {
			case 1 :
			    // simplec.g:101:13: (' '|'\\t'|'\\r'|'\\n') // alt
			    {
			    if (([input LA:1]>='\t'/*lower*/ && [input LA:1]<='\n'/*upper/isorange*/)||[input LA:1]=='\r'/*atom/iso*/||[input LA:1]==' '/*atom/iso*/) {
			    	[input consume];
			    	errorRecovery = NO;
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

		 channel=99; 

		}

		if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
	}
	@finally {
        // rule cleanup
		//test token labels
		//test token labels
		//test rule labels
		//test rule labels

	}
	return;
}
// $ANTLR end WS

- (void) mTokens
{
    // simplec.g:1:10: ( T7 | T8 | T9 | T10 | T11 | T12 | T13 | T14 | T15 | T16 | T17 | T18 | T19 | T20 | ID | INT | WS ) //ruleblock
    int alt4=17;
    switch ([input LA:1]) {
    	case ';':
    		alt4 = 1;
    		break;
    	case '(':
    		alt4 = 2;
    		break;
    	case ',':
    		alt4 = 3;
    		break;
    	case ')':
    		alt4 = 4;
    		break;
    	case 'i':
    		{
    			int LA4_5 = [input LA:2];
    			if ( LA4_5=='n'/*atom/Test*/ ) {
    				{
    					int LA4_17 = [input LA:3];
    					if ( LA4_17=='t'/*atom/Test*/ ) {
    						{
    							int LA4_23 = [input LA:4];
    							if ( (LA4_23>='0'/*lower*/ && LA4_23<='9'/*upper/range*/)||(LA4_23>='A'/*lower*/ && LA4_23<='Z'/*upper/range*/)||LA4_23=='_'/*atom/Test*/||(LA4_23>='a'/*lower*/ && LA4_23<='z'/*upper/range*/) ) {
    								alt4 = 15;
    							}
    						else {
    							alt4 = 5;	}
    						}
    					}
    				else {
    					alt4 = 15;	}
    				}
    			}
    		else {
    			alt4 = 15;	}
    		}
    		break;
    	case 'c':
    		{
    			int LA4_6 = [input LA:2];
    			if ( LA4_6=='h'/*atom/Test*/ ) {
    				{
    					int LA4_18 = [input LA:3];
    					if ( LA4_18=='a'/*atom/Test*/ ) {
    						{
    							int LA4_24 = [input LA:4];
    							if ( LA4_24=='r'/*atom/Test*/ ) {
    								{
    									int LA4_28 = [input LA:5];
    									if ( (LA4_28>='0'/*lower*/ && LA4_28<='9'/*upper/range*/)||(LA4_28>='A'/*lower*/ && LA4_28<='Z'/*upper/range*/)||LA4_28=='_'/*atom/Test*/||(LA4_28>='a'/*lower*/ && LA4_28<='z'/*upper/range*/) ) {
    										alt4 = 15;
    									}
    								else {
    									alt4 = 6;	}
    								}
    							}
    						else {
    							alt4 = 15;	}
    						}
    					}
    				else {
    					alt4 = 15;	}
    				}
    			}
    		else {
    			alt4 = 15;	}
    		}
    		break;
    	case 'v':
    		{
    			int LA4_7 = [input LA:2];
    			if ( LA4_7=='o'/*atom/Test*/ ) {
    				{
    					int LA4_19 = [input LA:3];
    					if ( LA4_19=='i'/*atom/Test*/ ) {
    						{
    							int LA4_25 = [input LA:4];
    							if ( LA4_25=='d'/*atom/Test*/ ) {
    								{
    									int LA4_29 = [input LA:5];
    									if ( (LA4_29>='0'/*lower*/ && LA4_29<='9'/*upper/range*/)||(LA4_29>='A'/*lower*/ && LA4_29<='Z'/*upper/range*/)||LA4_29=='_'/*atom/Test*/||(LA4_29>='a'/*lower*/ && LA4_29<='z'/*upper/range*/) ) {
    										alt4 = 15;
    									}
    								else {
    									alt4 = 7;	}
    								}
    							}
    						else {
    							alt4 = 15;	}
    						}
    					}
    				else {
    					alt4 = 15;	}
    				}
    			}
    		else {
    			alt4 = 15;	}
    		}
    		break;
    	case '{':
    		alt4 = 8;
    		break;
    	case '}':
    		alt4 = 9;
    		break;
    	case 'f':
    		{
    			int LA4_10 = [input LA:2];
    			if ( LA4_10=='o'/*atom/Test*/ ) {
    				{
    					int LA4_20 = [input LA:3];
    					if ( LA4_20=='r'/*atom/Test*/ ) {
    						{
    							int LA4_26 = [input LA:4];
    							if ( (LA4_26>='0'/*lower*/ && LA4_26<='9'/*upper/range*/)||(LA4_26>='A'/*lower*/ && LA4_26<='Z'/*upper/range*/)||LA4_26=='_'/*atom/Test*/||(LA4_26>='a'/*lower*/ && LA4_26<='z'/*upper/range*/) ) {
    								alt4 = 15;
    							}
    						else {
    							alt4 = 10;	}
    						}
    					}
    				else {
    					alt4 = 15;	}
    				}
    			}
    		else {
    			alt4 = 15;	}
    		}
    		break;
    	case '=':
    		{
    			int LA4_11 = [input LA:2];
    			if ( LA4_11=='='/*atom/Test*/ ) {
    				alt4 = 12;
    			}
    		else {
    			alt4 = 11;	}
    		}
    		break;
    	case '<':
    		alt4 = 13;
    		break;
    	case '+':
    		alt4 = 14;
    		break;
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
    	case 'd':
    	case 'e':
    	case 'g':
    	case 'h':
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
    	case 'w':
    	case 'x':
    	case 'y':
    	case 'z':
    		alt4 = 15;
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
    		alt4 = 16;
    		break;
    	case '\t':
    	case '\n':
    	case '\r':
    	case ' ':
    		alt4 = 17;
    		break;
    default:
     {
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:4 state:0 stream:input];
    	@throw nvae;

    	}}
    switch (alt4) {
    	case 1 :
    	    // simplec.g:1:10: T7 // alt
    	    {
    	    [self mT7];



    	    }
    	    break;
    	case 2 :
    	    // simplec.g:1:13: T8 // alt
    	    {
    	    [self mT8];



    	    }
    	    break;
    	case 3 :
    	    // simplec.g:1:16: T9 // alt
    	    {
    	    [self mT9];



    	    }
    	    break;
    	case 4 :
    	    // simplec.g:1:19: T10 // alt
    	    {
    	    [self mT10];



    	    }
    	    break;
    	case 5 :
    	    // simplec.g:1:23: T11 // alt
    	    {
    	    [self mT11];



    	    }
    	    break;
    	case 6 :
    	    // simplec.g:1:27: T12 // alt
    	    {
    	    [self mT12];



    	    }
    	    break;
    	case 7 :
    	    // simplec.g:1:31: T13 // alt
    	    {
    	    [self mT13];



    	    }
    	    break;
    	case 8 :
    	    // simplec.g:1:35: T14 // alt
    	    {
    	    [self mT14];



    	    }
    	    break;
    	case 9 :
    	    // simplec.g:1:39: T15 // alt
    	    {
    	    [self mT15];



    	    }
    	    break;
    	case 10 :
    	    // simplec.g:1:43: T16 // alt
    	    {
    	    [self mT16];



    	    }
    	    break;
    	case 11 :
    	    // simplec.g:1:47: T17 // alt
    	    {
    	    [self mT17];



    	    }
    	    break;
    	case 12 :
    	    // simplec.g:1:51: T18 // alt
    	    {
    	    [self mT18];



    	    }
    	    break;
    	case 13 :
    	    // simplec.g:1:55: T19 // alt
    	    {
    	    [self mT19];



    	    }
    	    break;
    	case 14 :
    	    // simplec.g:1:59: T20 // alt
    	    {
    	    [self mT20];



    	    }
    	    break;
    	case 15 :
    	    // simplec.g:1:63: ID // alt
    	    {
    	    [self mID];



    	    }
    	    break;
    	case 16 :
    	    // simplec.g:1:66: INT // alt
    	    {
    	    [self mINT];



    	    }
    	    break;
    	case 17 :
    	    // simplec.g:1:70: WS // alt
    	    {
    	    [self mWS];



    	    }
    	    break;

    }

}

@end