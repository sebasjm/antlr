// $ANTLR 3.0b3 simplec.g 2006-07-20 00:49:52

#import "SimpleC.h"

#pragma mark Cyclic DFA start
@implementation SimpleCDFA2
const static int SimpleCdfa2_eot[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static int SimpleCdfa2_eof[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static unichar SimpleCdfa2_min[13] =
    {4,4,7,4,0,4,7,9,0,0,4,4,9};
const static unichar SimpleCdfa2_max[13] =
    {13,4,8,13,0,4,14,10,0,0,13,4,10};
const static int SimpleCdfa2_accept[13] =
    {0,0,0,0,1,0,0,0,3,2,0,0,0};
const static int SimpleCdfa2_special[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static int SimpleCdfa2_transition0[] = {1,-1,-1,-1,-1,-1,-1,1,1,1};
const static int SimpleCdfa2_transition1[] = {2};
const static int SimpleCdfa2_transition2[] = {4,3};
const static int SimpleCdfa2_transition3[] = {5,-1,-1,-1,-1,-1,6,5,5,5};
const static int SimpleCdfa2_transition4[] = {};
const static int SimpleCdfa2_transition5[] = {7};
const static int SimpleCdfa2_transition6[] = {9,-1,-1,-1,-1,-1,-1,8};
const static int SimpleCdfa2_transition7[] = {10,6};
const static int SimpleCdfa2_transition8[] = {};
const static int SimpleCdfa2_transition9[] = {};
const static int SimpleCdfa2_transition10[] = {11,-1,-1,-1,-1,-1,-1,11,11,
    11};
const static int SimpleCdfa2_transition11[] = {12};
const static int SimpleCdfa2_transition12[] = {10,6};


- (id) init
{
	if ((self = [super init]) != nil) {
		eot = SimpleCdfa2_eot;
		eof = SimpleCdfa2_eof;
		min = SimpleCdfa2_min;
		max = SimpleCdfa2_max;
		accept = SimpleCdfa2_accept;
		special = SimpleCdfa2_special;
		if (!(transition = calloc(13, sizeof(void*)))) {
			[self release];
			return nil;
		}
		transition[0] = SimpleCdfa2_transition0;
		transition[1] = SimpleCdfa2_transition1;
		transition[2] = SimpleCdfa2_transition2;
		transition[3] = SimpleCdfa2_transition3;
		transition[4] = SimpleCdfa2_transition4;
		transition[5] = SimpleCdfa2_transition5;
		transition[6] = SimpleCdfa2_transition6;
		transition[7] = SimpleCdfa2_transition7;
		transition[8] = SimpleCdfa2_transition8;
		transition[9] = SimpleCdfa2_transition9;
		transition[10] = SimpleCdfa2_transition10;
		transition[11] = SimpleCdfa2_transition11;
		transition[12] = SimpleCdfa2_transition12;
	}
	return self;
}

- (void) release
{
	free(transition);
	[super release];
}

- (NSString *) description
{
	return @"11:1: declaration : ( variable | functionHeader ';' | functionHeader block );";
}

@end


#pragma mark Cyclic DFA end

const static unsigned long long FOLLOW_declaration_in_program27_data[] = {0x0000000000003812L};
static ANTLRBitSet *FOLLOW_declaration_in_program27;
const static unsigned long long FOLLOW_variable_in_declaration49_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_variable_in_declaration49;
const static unsigned long long FOLLOW_functionHeader_in_declaration59_data[] = {0x0000000000000080L};
static ANTLRBitSet *FOLLOW_functionHeader_in_declaration59;
const static unsigned long long FOLLOW_7_in_declaration61_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_7_in_declaration61;
const static unsigned long long FOLLOW_functionHeader_in_declaration74_data[] = {0x0000000000004000L};
static ANTLRBitSet *FOLLOW_functionHeader_in_declaration74;
const static unsigned long long FOLLOW_block_in_declaration76_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_block_in_declaration76;
const static unsigned long long FOLLOW_type_in_variable98_data[] = {0x0000000000000010L};
static ANTLRBitSet *FOLLOW_type_in_variable98;
const static unsigned long long FOLLOW_declarator_in_variable100_data[] = {0x0000000000000080L};
static ANTLRBitSet *FOLLOW_declarator_in_variable100;
const static unsigned long long FOLLOW_7_in_variable102_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_7_in_variable102;
const static unsigned long long FOLLOW_ID_in_declarator121_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_ID_in_declarator121;
const static unsigned long long FOLLOW_type_in_functionHeader150_data[] = {0x0000000000000010L};
static ANTLRBitSet *FOLLOW_type_in_functionHeader150;
const static unsigned long long FOLLOW_ID_in_functionHeader152_data[] = {0x0000000000000100L};
static ANTLRBitSet *FOLLOW_ID_in_functionHeader152;
const static unsigned long long FOLLOW_8_in_functionHeader154_data[] = {0x0000000000003C10L};
static ANTLRBitSet *FOLLOW_8_in_functionHeader154;
const static unsigned long long FOLLOW_formalParameter_in_functionHeader158_data[] = {0x0000000000000600L};
static ANTLRBitSet *FOLLOW_formalParameter_in_functionHeader158;
const static unsigned long long FOLLOW_9_in_functionHeader162_data[] = {0x0000000000003810L};
static ANTLRBitSet *FOLLOW_9_in_functionHeader162;
const static unsigned long long FOLLOW_formalParameter_in_functionHeader164_data[] = {0x0000000000000600L};
static ANTLRBitSet *FOLLOW_formalParameter_in_functionHeader164;
const static unsigned long long FOLLOW_10_in_functionHeader172_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_10_in_functionHeader172;
const static unsigned long long FOLLOW_type_in_formalParameter194_data[] = {0x0000000000000010L};
static ANTLRBitSet *FOLLOW_type_in_formalParameter194;
const static unsigned long long FOLLOW_declarator_in_formalParameter196_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_declarator_in_formalParameter196;
const static unsigned long long FOLLOW_set_in_type223_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_set_in_type223;
const static unsigned long long FOLLOW_14_in_block285_data[] = {0x000000000001F9B0L};
static ANTLRBitSet *FOLLOW_14_in_block285;
const static unsigned long long FOLLOW_variable_in_block299_data[] = {0x000000000001F9B0L};
static ANTLRBitSet *FOLLOW_variable_in_block299;
const static unsigned long long FOLLOW_stat_in_block314_data[] = {0x000000000001C1B0L};
static ANTLRBitSet *FOLLOW_stat_in_block314;
const static unsigned long long FOLLOW_15_in_block325_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_15_in_block325;
const static unsigned long long FOLLOW_forStat_in_stat337_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_forStat_in_stat337;
const static unsigned long long FOLLOW_expr_in_stat345_data[] = {0x0000000000000080L};
static ANTLRBitSet *FOLLOW_expr_in_stat345;
const static unsigned long long FOLLOW_7_in_stat347_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_7_in_stat347;
const static unsigned long long FOLLOW_block_in_stat361_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_block_in_stat361;
const static unsigned long long FOLLOW_assignStat_in_stat369_data[] = {0x0000000000000080L};
static ANTLRBitSet *FOLLOW_assignStat_in_stat369;
const static unsigned long long FOLLOW_7_in_stat371_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_7_in_stat371;
const static unsigned long long FOLLOW_7_in_stat379_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_7_in_stat379;
const static unsigned long long FOLLOW_16_in_forStat398_data[] = {0x0000000000000100L};
static ANTLRBitSet *FOLLOW_16_in_forStat398;
const static unsigned long long FOLLOW_8_in_forStat400_data[] = {0x0000000000000010L};
static ANTLRBitSet *FOLLOW_8_in_forStat400;
const static unsigned long long FOLLOW_assignStat_in_forStat402_data[] = {0x0000000000000080L};
static ANTLRBitSet *FOLLOW_assignStat_in_forStat402;
const static unsigned long long FOLLOW_7_in_forStat404_data[] = {0x0000000000000130L};
static ANTLRBitSet *FOLLOW_7_in_forStat404;
const static unsigned long long FOLLOW_expr_in_forStat406_data[] = {0x0000000000000080L};
static ANTLRBitSet *FOLLOW_expr_in_forStat406;
const static unsigned long long FOLLOW_7_in_forStat408_data[] = {0x0000000000000010L};
static ANTLRBitSet *FOLLOW_7_in_forStat408;
const static unsigned long long FOLLOW_assignStat_in_forStat410_data[] = {0x0000000000000400L};
static ANTLRBitSet *FOLLOW_assignStat_in_forStat410;
const static unsigned long long FOLLOW_10_in_forStat412_data[] = {0x0000000000004000L};
static ANTLRBitSet *FOLLOW_10_in_forStat412;
const static unsigned long long FOLLOW_block_in_forStat414_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_block_in_forStat414;
const static unsigned long long FOLLOW_ID_in_assignStat441_data[] = {0x0000000000020000L};
static ANTLRBitSet *FOLLOW_ID_in_assignStat441;
const static unsigned long long FOLLOW_17_in_assignStat443_data[] = {0x0000000000000130L};
static ANTLRBitSet *FOLLOW_17_in_assignStat443;
const static unsigned long long FOLLOW_expr_in_assignStat445_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_expr_in_assignStat445;
const static unsigned long long FOLLOW_condExpr_in_expr467_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_condExpr_in_expr467;
const static unsigned long long FOLLOW_aexpr_in_condExpr486_data[] = {0x00000000000C0002L};
static ANTLRBitSet *FOLLOW_aexpr_in_condExpr486;
const static unsigned long long FOLLOW_set_in_condExpr491_data[] = {0x0000000000000130L};
static ANTLRBitSet *FOLLOW_set_in_condExpr491;
const static unsigned long long FOLLOW_aexpr_in_condExpr498_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_aexpr_in_condExpr498;
const static unsigned long long FOLLOW_atom_in_aexpr520_data[] = {0x0000000000100002L};
static ANTLRBitSet *FOLLOW_atom_in_aexpr520;
const static unsigned long long FOLLOW_20_in_aexpr524_data[] = {0x0000000000000130L};
static ANTLRBitSet *FOLLOW_20_in_aexpr524;
const static unsigned long long FOLLOW_atom_in_aexpr526_data[] = {0x0000000000100002L};
static ANTLRBitSet *FOLLOW_atom_in_aexpr526;
const static unsigned long long FOLLOW_ID_in_atom546_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_ID_in_atom546;
const static unsigned long long FOLLOW_INT_in_atom560_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_INT_in_atom560;
const static unsigned long long FOLLOW_8_in_atom574_data[] = {0x0000000000000130L};
static ANTLRBitSet *FOLLOW_8_in_atom574;
const static unsigned long long FOLLOW_expr_in_atom576_data[] = {0x0000000000000400L};
static ANTLRBitSet *FOLLOW_expr_in_atom576;
const static unsigned long long FOLLOW_10_in_atom578_data[] = {0x0000000000000002L};
static ANTLRBitSet *FOLLOW_10_in_atom578;


@implementation SimpleC

+ (void) initialize
{
	FOLLOW_declaration_in_program27 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_declaration_in_program27_data count:1];
	FOLLOW_variable_in_declaration49 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_variable_in_declaration49_data count:1];
	FOLLOW_functionHeader_in_declaration59 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_functionHeader_in_declaration59_data count:1];
	FOLLOW_7_in_declaration61 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_7_in_declaration61_data count:1];
	FOLLOW_functionHeader_in_declaration74 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_functionHeader_in_declaration74_data count:1];
	FOLLOW_block_in_declaration76 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_block_in_declaration76_data count:1];
	FOLLOW_type_in_variable98 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_variable98_data count:1];
	FOLLOW_declarator_in_variable100 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_declarator_in_variable100_data count:1];
	FOLLOW_7_in_variable102 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_7_in_variable102_data count:1];
	FOLLOW_ID_in_declarator121 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_ID_in_declarator121_data count:1];
	FOLLOW_type_in_functionHeader150 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_functionHeader150_data count:1];
	FOLLOW_ID_in_functionHeader152 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_ID_in_functionHeader152_data count:1];
	FOLLOW_8_in_functionHeader154 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_8_in_functionHeader154_data count:1];
	FOLLOW_formalParameter_in_functionHeader158 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_formalParameter_in_functionHeader158_data count:1];
	FOLLOW_9_in_functionHeader162 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_9_in_functionHeader162_data count:1];
	FOLLOW_formalParameter_in_functionHeader164 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_formalParameter_in_functionHeader164_data count:1];
	FOLLOW_10_in_functionHeader172 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_10_in_functionHeader172_data count:1];
	FOLLOW_type_in_formalParameter194 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_formalParameter194_data count:1];
	FOLLOW_declarator_in_formalParameter196 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_declarator_in_formalParameter196_data count:1];
	FOLLOW_set_in_type223 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_set_in_type223_data count:1];
	FOLLOW_14_in_block285 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_14_in_block285_data count:1];
	FOLLOW_variable_in_block299 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_variable_in_block299_data count:1];
	FOLLOW_stat_in_block314 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_stat_in_block314_data count:1];
	FOLLOW_15_in_block325 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_15_in_block325_data count:1];
	FOLLOW_forStat_in_stat337 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_forStat_in_stat337_data count:1];
	FOLLOW_expr_in_stat345 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_stat345_data count:1];
	FOLLOW_7_in_stat347 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_7_in_stat347_data count:1];
	FOLLOW_block_in_stat361 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_block_in_stat361_data count:1];
	FOLLOW_assignStat_in_stat369 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_assignStat_in_stat369_data count:1];
	FOLLOW_7_in_stat371 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_7_in_stat371_data count:1];
	FOLLOW_7_in_stat379 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_7_in_stat379_data count:1];
	FOLLOW_16_in_forStat398 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_16_in_forStat398_data count:1];
	FOLLOW_8_in_forStat400 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_8_in_forStat400_data count:1];
	FOLLOW_assignStat_in_forStat402 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_assignStat_in_forStat402_data count:1];
	FOLLOW_7_in_forStat404 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_7_in_forStat404_data count:1];
	FOLLOW_expr_in_forStat406 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_forStat406_data count:1];
	FOLLOW_7_in_forStat408 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_7_in_forStat408_data count:1];
	FOLLOW_assignStat_in_forStat410 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_assignStat_in_forStat410_data count:1];
	FOLLOW_10_in_forStat412 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_10_in_forStat412_data count:1];
	FOLLOW_block_in_forStat414 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_block_in_forStat414_data count:1];
	FOLLOW_ID_in_assignStat441 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_ID_in_assignStat441_data count:1];
	FOLLOW_17_in_assignStat443 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_17_in_assignStat443_data count:1];
	FOLLOW_expr_in_assignStat445 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_assignStat445_data count:1];
	FOLLOW_condExpr_in_expr467 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_condExpr_in_expr467_data count:1];
	FOLLOW_aexpr_in_condExpr486 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_aexpr_in_condExpr486_data count:1];
	FOLLOW_set_in_condExpr491 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_set_in_condExpr491_data count:1];
	FOLLOW_aexpr_in_condExpr498 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_aexpr_in_condExpr498_data count:1];
	FOLLOW_atom_in_aexpr520 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_atom_in_aexpr520_data count:1];
	FOLLOW_20_in_aexpr524 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_20_in_aexpr524_data count:1];
	FOLLOW_atom_in_aexpr526 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_atom_in_aexpr526_data count:1];
	FOLLOW_ID_in_atom546 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_ID_in_atom546_data count:1];
	FOLLOW_INT_in_atom560 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_INT_in_atom560_data count:1];
	FOLLOW_8_in_atom574 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_8_in_atom574_data count:1];
	FOLLOW_expr_in_atom576 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_atom576_data count:1];
	FOLLOW_10_in_atom578 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_10_in_atom578_data count:1];

}

- (id) initWithTokenStream:(id<ANTLRTokenStream>)aStream
{
	if ((self = [super initWithTokenStream:aStream])) {
		tokenNames = [[NSArray alloc] initWithObjects:@"<invalid>", @"<EOR>", @"<DOWN>", @"<UP>",     @"ID",     @"INT",     @"WS",     @"';'",     @"'('",     @"','",     @"')'",     @"'int'",     @"'char'",     @"'void'",     @"'{'",     @"'}'",     @"'for'",     @"'='",     @"'=='",     @"'<'",     @"'+'", nil];
		dfa2 = [[SimpleCDFA2 alloc] init];
	}
	return self;
}

- (void) dealloc
{
	[tokenNames release];
	[dfa2 release];
	[super dealloc];
}



// $ANTLR start program
// simplec.g:7:1: program : ( declaration )+ ;
- (void) program
{
    @try {
        // simplec.g:8:9: ( ( declaration )+ ) // ruleBlockSingleAlt
        // simplec.g:8:9: ( declaration )+ // alt
        {
        // simplec.g:8:9: ( declaration )+	// positiveClosureBlock
        int cnt1=0;

        do {
            int alt1=2;
            int LA1_0 = [input LA:1];
            if ( LA1_0==SimpleC_ID/*atom/Test*/||(LA1_0>=11/*lower*/ && LA1_0<=13/*upper/range*/) ) {
            	alt1 = 1;
            }


            switch (alt1) {
        	case 1 :
        	    // simplec.g:8:9: declaration // alt
        	    {
        	    [following addObject:FOLLOW_declaration_in_program27];
        	    [self declaration];
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
        //test token labels
        //test token labels
        //test rule labels
        //test rule labels

    }
    return ;
}
// $ANTLR end program


// $ANTLR start declaration
// simplec.g:11:1: declaration : ( variable | functionHeader ';' | functionHeader block );
- (void) declaration
{
    NSString* functionHeader1 = nil;

    NSString* functionHeader2 = nil;


    @try {
        // simplec.g:21:9: ( variable | functionHeader ';' | functionHeader block ) //ruleblock
        int alt2=3;
        alt2 = [dfa2 predict:input];
        switch (alt2) {
        	case 1 :
        	    // simplec.g:21:9: variable // alt
        	    {
        	    [following addObject:FOLLOW_variable_in_declaration49];
        	    [self variable];
        	    [following removeLastObject];



        	    }
        	    break;
        	case 2 :
        	    // simplec.g:22:9: functionHeader ';' // alt
        	    {
        	    [following addObject:FOLLOW_functionHeader_in_declaration59];
        	    functionHeader1 = [self functionHeader];
        	    [following removeLastObject];


        	    [self match:input tokenType:7 follow:FOLLOW_7_in_declaration61]; 
        	    NSLog(@"%@ is a declaration", functionHeader1);

        	    }
        	    break;
        	case 3 :
        	    // simplec.g:24:9: functionHeader block // alt
        	    {
        	    [following addObject:FOLLOW_functionHeader_in_declaration74];
        	    functionHeader2 = [self functionHeader];
        	    [following removeLastObject];


        	    [following addObject:FOLLOW_block_in_declaration76];
        	    [self block];
        	    [following removeLastObject];


        	    NSLog(@"%@ is a definition", functionHeader2);

        	    }
        	    break;

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
        //[functionHeader1 release];
        //[functionHeader2 release];
        //test rule labels

    }
    return ;
}
// $ANTLR end declaration


// $ANTLR start variable
// simplec.g:28:1: variable : type declarator ';' ;
- (void) variable
{
    @try {
        // simplec.g:29:9: ( type declarator ';' ) // ruleBlockSingleAlt
        // simplec.g:29:9: type declarator ';' // alt
        {
        [following addObject:FOLLOW_type_in_variable98];
        [self type];
        [following removeLastObject];


        [following addObject:FOLLOW_declarator_in_variable100];
        [self declarator];
        [following removeLastObject];


        [self match:input tokenType:7 follow:FOLLOW_7_in_variable102]; 

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
// $ANTLR end variable


// $ANTLR start declarator
// simplec.g:32:1: declarator : ID ;
- (void) declarator
{
    @try {
        // simplec.g:33:9: ( ID ) // ruleBlockSingleAlt
        // simplec.g:33:9: ID // alt
        {
        [self match:input tokenType:SimpleC_ID follow:FOLLOW_ID_in_declarator121]; 

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
// $ANTLR end declarator


// $ANTLR start functionHeader
// simplec.g:36:1: functionHeader returns [NSString* name] : type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' ;
- (NSString*) functionHeader
{
    NSString* name;
    ANTLRToken * ID3 = nil;


        name=nil; // for now you must init here rather than in 'returns'

    @try {
        // simplec.g:40:9: ( type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' ) // ruleBlockSingleAlt
        // simplec.g:40:9: type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' // alt
        {
        [following addObject:FOLLOW_type_in_functionHeader150];
        [self type];
        [following removeLastObject];


        ID3=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:SimpleC_ID follow:FOLLOW_ID_in_functionHeader152]; 
        [self match:input tokenType:8 follow:FOLLOW_8_in_functionHeader154]; 
        // simplec.g:40:21: ( formalParameter ( ',' formalParameter )* )? // block
        int alt4=2;
        int LA4_0 = [input LA:1];
        if ( LA4_0==SimpleC_ID/*atom/Test*/||(LA4_0>=11/*lower*/ && LA4_0<=13/*upper/range*/) ) {
        	alt4 = 1;
        }
        switch (alt4) {
        	case 1 :
        	    // simplec.g:40:23: formalParameter ( ',' formalParameter )* // alt
        	    {
        	    [following addObject:FOLLOW_formalParameter_in_functionHeader158];
        	    [self formalParameter];
        	    [following removeLastObject];


        	    do {
        	        int alt3=2;
        	        int LA3_0 = [input LA:1];
        	        if ( LA3_0==9/*atom/Test*/ ) {
        	        	alt3 = 1;
        	        }


        	        switch (alt3) {
        	    	case 1 :
        	    	    // simplec.g:40:41: ',' formalParameter // alt
        	    	    {
        	    	    [self match:input tokenType:9 follow:FOLLOW_9_in_functionHeader162]; 
        	    	    [following addObject:FOLLOW_formalParameter_in_functionHeader164];
        	    	    [self formalParameter];
        	    	    [following removeLastObject];



        	    	    }
        	    	    break;

        	    	default :
        	    	    goto loop3;
        	        }
        	    } while (YES); loop3: ;


        	    }
        	    break;

        }

        [self match:input tokenType:10 follow:FOLLOW_10_in_functionHeader172]; 
        name = [ID3 text]; 

        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        //test token labels
        [ID3 release];
        //test token labels
        //test rule labels
        //test rule labels

    }
    return name;
}
// $ANTLR end functionHeader


// $ANTLR start formalParameter
// simplec.g:44:1: formalParameter : type declarator ;
- (void) formalParameter
{
    @try {
        // simplec.g:45:9: ( type declarator ) // ruleBlockSingleAlt
        // simplec.g:45:9: type declarator // alt
        {
        [following addObject:FOLLOW_type_in_formalParameter194];
        [self type];
        [following removeLastObject];


        [following addObject:FOLLOW_declarator_in_formalParameter196];
        [self declarator];
        [following removeLastObject];



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
// $ANTLR end formalParameter


// $ANTLR start type
// simplec.g:48:1: type : ('int'|'char'|'void'|ID);
- (void) type
{
    @try {
        // simplec.g:49:5: ( ('int'|'char'|'void'|ID)) // ruleBlockSingleAlt
        // simplec.g:49:9: ('int'|'char'|'void'|ID) // alt
        {
        if ([input LA:1]==SimpleC_ID/*atom/iso*/||([input LA:1]>=11/*lower*/ && [input LA:1]<=13/*upper/isorange*/)) {
        	[input consume];
        	errorRecovery = NO;
        } else {
        	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
        	[self recoverFromMismatchedSet:input exception:mse follow:FOLLOW_set_in_type223];	@throw mse;
        }


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
// $ANTLR end type


// $ANTLR start block
// simplec.g:55:1: block : '{' ( variable )* ( stat )* '}' ;
- (void) block
{
    @try {
        // simplec.g:56:9: ( '{' ( variable )* ( stat )* '}' ) // ruleBlockSingleAlt
        // simplec.g:56:9: '{' ( variable )* ( stat )* '}' // alt
        {
        [self match:input tokenType:14 follow:FOLLOW_14_in_block285]; 
        do {
            int alt5=2;
            int LA5_0 = [input LA:1];
            if ( LA5_0==SimpleC_ID/*atom/Test*/ ) {
            	int LA5_2 = [input LA:2];
            	if ( LA5_2==SimpleC_ID/*atom/Test*/ ) {
            		alt5 = 1;
            	}


            }
            else if ( (LA5_0>=11/*lower*/ && LA5_0<=13/*upper/range*/) ) {
            	alt5 = 1;
            }


            switch (alt5) {
        	case 1 :
        	    // simplec.g:57:13: variable // alt
        	    {
        	    [following addObject:FOLLOW_variable_in_block299];
        	    [self variable];
        	    [following removeLastObject];



        	    }
        	    break;

        	default :
        	    goto loop5;
            }
        } while (YES); loop5: ;

        do {
            int alt6=2;
            int LA6_0 = [input LA:1];
            if ( (LA6_0>=SimpleC_ID/*lower*/ && LA6_0<=SimpleC_INT/*upper/range*/)||(LA6_0>=7/*lower*/ && LA6_0<=8/*upper/range*/)||LA6_0==14/*atom/Test*/||LA6_0==16/*atom/Test*/ ) {
            	alt6 = 1;
            }


            switch (alt6) {
        	case 1 :
        	    // simplec.g:58:13: stat // alt
        	    {
        	    [following addObject:FOLLOW_stat_in_block314];
        	    [self stat];
        	    [following removeLastObject];



        	    }
        	    break;

        	default :
        	    goto loop6;
            }
        } while (YES); loop6: ;

        [self match:input tokenType:15 follow:FOLLOW_15_in_block325]; 

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
// $ANTLR end block


// $ANTLR start stat
// simplec.g:62:1: stat : ( forStat | expr ';' | block | assignStat ';' | ';' );
- (void) stat
{
    @try {
        // simplec.g:62:7: ( forStat | expr ';' | block | assignStat ';' | ';' ) //ruleblock
        int alt7=5;
        switch ([input LA:1]) {
        	case 16:
        		alt7 = 1;
        		break;
        	case SimpleC_ID:
        		{
        			int LA7_2 = [input LA:2];
        			if ( LA7_2==17/*atom/Test*/ ) {
        				alt7 = 4;
        			}
        			else if ( LA7_2==7/*atom/Test*/||(LA7_2>=18/*lower*/ && LA7_2<=20/*upper/range*/) ) {
        				alt7 = 2;
        			}
        		else {
        		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:7 state:2 stream:input];
        			@throw nvae;
        			}
        		}
        		break;
        	case SimpleC_INT:
        	case 8:
        		alt7 = 2;
        		break;
        	case 14:
        		alt7 = 3;
        		break;
        	case 7:
        		alt7 = 5;
        		break;
        default:
         {
            ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:7 state:0 stream:input];
        	@throw nvae;

        	}}
        switch (alt7) {
        	case 1 :
        	    // simplec.g:62:7: forStat // alt
        	    {
        	    [following addObject:FOLLOW_forStat_in_stat337];
        	    [self forStat];
        	    [following removeLastObject];



        	    }
        	    break;
        	case 2 :
        	    // simplec.g:63:7: expr ';' // alt
        	    {
        	    [following addObject:FOLLOW_expr_in_stat345];
        	    [self expr];
        	    [following removeLastObject];


        	    [self match:input tokenType:7 follow:FOLLOW_7_in_stat347]; 

        	    }
        	    break;
        	case 3 :
        	    // simplec.g:64:7: block // alt
        	    {
        	    [following addObject:FOLLOW_block_in_stat361];
        	    [self block];
        	    [following removeLastObject];



        	    }
        	    break;
        	case 4 :
        	    // simplec.g:65:7: assignStat ';' // alt
        	    {
        	    [following addObject:FOLLOW_assignStat_in_stat369];
        	    [self assignStat];
        	    [following removeLastObject];


        	    [self match:input tokenType:7 follow:FOLLOW_7_in_stat371]; 

        	    }
        	    break;
        	case 5 :
        	    // simplec.g:66:7: ';' // alt
        	    {
        	    [self match:input tokenType:7 follow:FOLLOW_7_in_stat379]; 

        	    }
        	    break;

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


// $ANTLR start forStat
// simplec.g:69:1: forStat : 'for' '(' assignStat ';' expr ';' assignStat ')' block ;
- (void) forStat
{
    @try {
        // simplec.g:70:9: ( 'for' '(' assignStat ';' expr ';' assignStat ')' block ) // ruleBlockSingleAlt
        // simplec.g:70:9: 'for' '(' assignStat ';' expr ';' assignStat ')' block // alt
        {
        [self match:input tokenType:16 follow:FOLLOW_16_in_forStat398]; 
        [self match:input tokenType:8 follow:FOLLOW_8_in_forStat400]; 
        [following addObject:FOLLOW_assignStat_in_forStat402];
        [self assignStat];
        [following removeLastObject];


        [self match:input tokenType:7 follow:FOLLOW_7_in_forStat404]; 
        [following addObject:FOLLOW_expr_in_forStat406];
        [self expr];
        [following removeLastObject];


        [self match:input tokenType:7 follow:FOLLOW_7_in_forStat408]; 
        [following addObject:FOLLOW_assignStat_in_forStat410];
        [self assignStat];
        [following removeLastObject];


        [self match:input tokenType:10 follow:FOLLOW_10_in_forStat412]; 
        [following addObject:FOLLOW_block_in_forStat414];
        [self block];
        [following removeLastObject];



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
// $ANTLR end forStat


// $ANTLR start assignStat
// simplec.g:73:1: assignStat : ID '=' expr ;
- (void) assignStat
{
    @try {
        // simplec.g:74:9: ( ID '=' expr ) // ruleBlockSingleAlt
        // simplec.g:74:9: ID '=' expr // alt
        {
        [self match:input tokenType:SimpleC_ID follow:FOLLOW_ID_in_assignStat441]; 
        [self match:input tokenType:17 follow:FOLLOW_17_in_assignStat443]; 
        [following addObject:FOLLOW_expr_in_assignStat445];
        [self expr];
        [following removeLastObject];



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
// $ANTLR end assignStat


// $ANTLR start expr
// simplec.g:77:1: expr : condExpr ;
- (void) expr
{
    @try {
        // simplec.g:77:9: ( condExpr ) // ruleBlockSingleAlt
        // simplec.g:77:9: condExpr // alt
        {
        [following addObject:FOLLOW_condExpr_in_expr467];
        [self condExpr];
        [following removeLastObject];



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
// $ANTLR end expr


// $ANTLR start condExpr
// simplec.g:80:1: condExpr : aexpr ( ('=='|'<') aexpr )? ;
- (void) condExpr
{
    @try {
        // simplec.g:81:9: ( aexpr ( ('=='|'<') aexpr )? ) // ruleBlockSingleAlt
        // simplec.g:81:9: aexpr ( ('=='|'<') aexpr )? // alt
        {
        [following addObject:FOLLOW_aexpr_in_condExpr486];
        [self aexpr];
        [following removeLastObject];


        // simplec.g:81:15: ( ('=='|'<') aexpr )? // block
        int alt8=2;
        int LA8_0 = [input LA:1];
        if ( (LA8_0>=18/*lower*/ && LA8_0<=19/*upper/range*/) ) {
        	alt8 = 1;
        }
        switch (alt8) {
        	case 1 :
        	    // simplec.g:81:17: ('=='|'<') aexpr // alt
        	    {
        	    if (([input LA:1]>=18/*lower*/ && [input LA:1]<=19/*upper/isorange*/)) {
        	    	[input consume];
        	    	errorRecovery = NO;
        	    } else {
        	    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
        	    	[self recoverFromMismatchedSet:input exception:mse follow:FOLLOW_set_in_condExpr491];	@throw mse;
        	    }

        	    [following addObject:FOLLOW_aexpr_in_condExpr498];
        	    [self aexpr];
        	    [following removeLastObject];



        	    }
        	    break;

        }


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
// $ANTLR end condExpr


// $ANTLR start aexpr
// simplec.g:84:1: aexpr : atom ( '+' atom )* ;
- (void) aexpr
{
    @try {
        // simplec.g:85:9: ( atom ( '+' atom )* ) // ruleBlockSingleAlt
        // simplec.g:85:9: atom ( '+' atom )* // alt
        {
        [following addObject:FOLLOW_atom_in_aexpr520];
        [self atom];
        [following removeLastObject];


        do {
            int alt9=2;
            int LA9_0 = [input LA:1];
            if ( LA9_0==20/*atom/Test*/ ) {
            	alt9 = 1;
            }


            switch (alt9) {
        	case 1 :
        	    // simplec.g:85:16: '+' atom // alt
        	    {
        	    [self match:input tokenType:20 follow:FOLLOW_20_in_aexpr524]; 
        	    [following addObject:FOLLOW_atom_in_aexpr526];
        	    [self atom];
        	    [following removeLastObject];



        	    }
        	    break;

        	default :
        	    goto loop9;
            }
        } while (YES); loop9: ;


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
// $ANTLR end aexpr


// $ANTLR start atom
// simplec.g:88:1: atom : ( ID | INT | '(' expr ')' );
- (void) atom
{
    @try {
        // simplec.g:89:7: ( ID | INT | '(' expr ')' ) //ruleblock
        int alt10=3;
        switch ([input LA:1]) {
        	case SimpleC_ID:
        		alt10 = 1;
        		break;
        	case SimpleC_INT:
        		alt10 = 2;
        		break;
        	case 8:
        		alt10 = 3;
        		break;
        default:
         {
            ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:10 state:0 stream:input];
        	@throw nvae;

        	}}
        switch (alt10) {
        	case 1 :
        	    // simplec.g:89:7: ID // alt
        	    {
        	    [self match:input tokenType:SimpleC_ID follow:FOLLOW_ID_in_atom546]; 

        	    }
        	    break;
        	case 2 :
        	    // simplec.g:90:7: INT // alt
        	    {
        	    [self match:input tokenType:SimpleC_INT follow:FOLLOW_INT_in_atom560]; 

        	    }
        	    break;
        	case 3 :
        	    // simplec.g:91:7: '(' expr ')' // alt
        	    {
        	    [self match:input tokenType:8 follow:FOLLOW_8_in_atom574]; 
        	    [following addObject:FOLLOW_expr_in_atom576];
        	    [self expr];
        	    [following removeLastObject];


        	    [self match:input tokenType:10 follow:FOLLOW_10_in_atom578]; 

        	    }
        	    break;

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
// $ANTLR end atom


@end