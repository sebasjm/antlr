// $ANTLR 3.0b5 /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g 2006-11-12 21:51:44

#import "CombinedParser.h"

#pragma mark Cyclic DFA

#pragma mark Bitsets
const static unsigned long long FOLLOW_identifier_in_stat20_data[] = {0x0000000000000012LL};
static ANTLRBitSet *FOLLOW_identifier_in_stat20;
const static unsigned long long FOLLOW_CombinedParser_ID_in_identifier35_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_CombinedParser_ID_in_identifier35;


#pragma mark Dynamic Global Scopes

#pragma mark Dynamic Rule Scopes

#pragma mark Rule return scopes start

@implementation CombinedParser

+ (void) initialize
{
	FOLLOW_identifier_in_stat20 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_identifier_in_stat20_data count:1];
	FOLLOW_CombinedParser_ID_in_identifier35 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_CombinedParser_ID_in_identifier35_data count:1];

}

- (id) initWithTokenStream:(id<ANTLRTokenStream>)aStream
{
	if ((self = [super initWithTokenStream:aStream])) {

		tokenNames = [[NSArray alloc] initWithObjects:@"<invalid>", @"<EOR>", @"<DOWN>", @"<UP>",     @"ID",     @"INT",     @"WS", nil];

						
	}
	return self;
}

- (void) dealloc
{
	[tokenNames release];

	[super dealloc];
}


// $ANTLR start stat
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:7:1: stat : ( identifier )+ ;
- (void) stat
{
    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:7:7: ( ( identifier )+ ) // ruleBlockSingleAlt
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:7:7: ( identifier )+ // alt
        {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:7:7: ( identifier )+	// positiveClosureBlock
        int cnt1=0;

        do {
            int alt1=2;
            {
            	int LA1_0 = [input LA:1];
            	if ( LA1_0==CombinedParser_ID ) {
            		alt1 = 1;
            	}

            }
            switch (alt1) {
        	case 1 :
        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:7:7: identifier // alt
        	    {
        	    [following addObject:FOLLOW_identifier_in_stat20];
        	    [self identifier];
        	    [following removeLastObject];



        	    }
        	    break;

        	default :
        	    if ( cnt1 >= 1 )  goto loop1;
        			ANTLREarlyExitException *eee = [ANTLREarlyExitException exceptionWithStream:input decisionNumber:1];
        			@throw eee;
            }
            cnt1++;
        } while (YES); loop1: ;


        }

    }
	@catch (ANTLRRecognitionException *re) {
		[self reportError:re];
		[self recover:input exception:re];
	}
	@finally {
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	return ;
}
// $ANTLR end stat

// $ANTLR start identifier
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:9:1: identifier : ID ;
- (void) identifier
{
    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:10:7: ( ID ) // ruleBlockSingleAlt
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/combined/Combined.g:10:7: ID // alt
        {
        [self match:input tokenType:CombinedParser_ID follow:FOLLOW_CombinedParser_ID_in_identifier35]; 

        }

    }
	@catch (ANTLRRecognitionException *re) {
		[self reportError:re];
		[self recover:input exception:re];
	}
	@finally {
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	return ;
}
// $ANTLR end identifier



@end