// $ANTLR 3.0b2 Combined.g 2006-07-08 11:59:53

#import "Combined.h"

#pragma mark Cyclic DFA start
#pragma mark Cyclic DFA end

@implementation Combined

const static unsigned long long FOLLOW_identifier_in_stat20_data[] = {0x0000000000000012L};
static ANTLRBitSet *FOLLOW_identifier_in_stat20;
const static unsigned long long FOLLOW_ID_in_identifier43_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_ID_in_identifier43;

+ (void) initialize
{
	FOLLOW_identifier_in_stat20 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_identifier_in_stat20_data count:1];
	FOLLOW_ID_in_identifier43 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_ID_in_identifier43_data count:1];

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
// Combined.g:7:1: stat : ( identifier )+ ;
- (void) stat
{
    @try {
        // Combined.g:7:7: ( ( identifier )+ ) // ruleBlockSingleAlt
        // Combined.g:7:7: ( identifier )+ // alt
        {
        // Combined.g:7:7: ( identifier )+	// positiveClosureBlock
        int cnt1=0;

        do {
            int alt1=2;
            int LA1_0 = [input LA:1];
            if ( LA1_0==Combined_ID ) {
            	alt1 = 1;
            }


            switch (alt1) {
        	case 1 :
        	    // Combined.g:7:7: identifier // alt
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

   //     System.out.println("enum is an ID");

        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        //test token labels
        //test token labels
        //test rule labels
        //test rule labels

    }
    return ;
}
// $ANTLR end stat


// $ANTLR start identifier
// Combined.g:10:1: identifier : ID ;
- (void) identifier
{
    @try {
        // Combined.g:11:7: ( ID ) // ruleBlockSingleAlt
        // Combined.g:11:7: ID // alt
        {
        [self match:input tokenType:Combined_ID follow:FOLLOW_ID_in_identifier43]; 

        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        //test token labels
        //test token labels
        //test rule labels
        //test rule labels

    }
    return ;
}
// $ANTLR end identifier


@end