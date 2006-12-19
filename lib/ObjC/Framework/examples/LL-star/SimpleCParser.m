// $ANTLR 3.0b5 /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g 2006-12-12 20:50:18

#import "SimpleCParser.h"

#pragma mark Cyclic DFA
@implementation SimpleCParserDFA2
const static int SimpleCParserdfa2_eot[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static int SimpleCParserdfa2_eof[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static unichar SimpleCParserdfa2_min[13] =
    {4,4,7,4,0,4,7,9,0,0,4,4,9};
const static unichar SimpleCParserdfa2_max[13] =
    {13,4,8,13,0,4,14,10,0,0,13,4,10};
const static int SimpleCParserdfa2_accept[13] =
    {-1,-1,-1,-1,1,-1,-1,-1,3,2,-1,-1,-1};
const static int SimpleCParserdfa2_special[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static int SimpleCParserdfa2_transition[] = {};
const static int SimpleCParserdfa2_transition0[] = {2};
const static int SimpleCParserdfa2_transition1[] = {4, 3};
const static int SimpleCParserdfa2_transition2[] = {10, 6};
const static int SimpleCParserdfa2_transition3[] = {1, -1, -1, -1, -1, -1, 
	-1, 1, 1, 1};
const static int SimpleCParserdfa2_transition4[] = {7};
const static int SimpleCParserdfa2_transition5[] = {12};
const static int SimpleCParserdfa2_transition6[] = {5, -1, -1, -1, -1, -1, 
	6, 5, 5, 5};
const static int SimpleCParserdfa2_transition7[] = {11, -1, -1, -1, -1, 
	-1, -1, 11, 11, 11};
const static int SimpleCParserdfa2_transition8[] = {9, -1, -1, -1, -1, -1, 
	-1, 8};


- (id) initWithRecognizer:(ANTLRBaseRecognizer *) theRecognizer
{
	if ((self = [super initWithRecognizer:theRecognizer]) != nil) {
		eot = SimpleCParserdfa2_eot;
		eof = SimpleCParserdfa2_eof;
		min = SimpleCParserdfa2_min;
		max = SimpleCParserdfa2_max;
		accept = SimpleCParserdfa2_accept;
		special = SimpleCParserdfa2_special;
		if (!(transition = calloc(13, sizeof(void*)))) {
			[self release];
			return nil;
		}
		transition[0] = SimpleCParserdfa2_transition3;
		transition[1] = SimpleCParserdfa2_transition0;
		transition[2] = SimpleCParserdfa2_transition1;
		transition[3] = SimpleCParserdfa2_transition6;
		transition[4] = SimpleCParserdfa2_transition;
		transition[5] = SimpleCParserdfa2_transition4;
		transition[6] = SimpleCParserdfa2_transition8;
		transition[7] = SimpleCParserdfa2_transition2;
		transition[8] = SimpleCParserdfa2_transition;
		transition[9] = SimpleCParserdfa2_transition;
		transition[10] = SimpleCParserdfa2_transition7;
		transition[11] = SimpleCParserdfa2_transition5;
		transition[12] = SimpleCParserdfa2_transition2;
	}
	return self;
}

- (void) dealloc
{
	free(transition);
	[super dealloc];
}

- (NSString *) description
{
	return @"11:1: declaration : ( variable | functionHeader ';' | functionHeader block );";
}

-(void) error:(ANTLRNoViableAltException *)nvae
{
    [[recognizer debugListener] recognitionException:nvae];
}

@end



#pragma mark Bitsets
const static unsigned long long FOLLOW_declaration_in_program27_data[] = {0x0000000000003812LL};
static ANTLRBitSet *FOLLOW_declaration_in_program27;
const static unsigned long long FOLLOW_variable_in_declaration49_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_variable_in_declaration49;
const static unsigned long long FOLLOW_functionHeader_in_declaration59_data[] = {0x0000000000000080LL};
static ANTLRBitSet *FOLLOW_functionHeader_in_declaration59;
const static unsigned long long FOLLOW_7_in_declaration61_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_7_in_declaration61;
const static unsigned long long FOLLOW_functionHeader_in_declaration74_data[] = {0x0000000000004000LL};
static ANTLRBitSet *FOLLOW_functionHeader_in_declaration74;
const static unsigned long long FOLLOW_block_in_declaration76_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_block_in_declaration76;
const static unsigned long long FOLLOW_type_in_variable98_data[] = {0x0000000000000010LL};
static ANTLRBitSet *FOLLOW_type_in_variable98;
const static unsigned long long FOLLOW_declarator_in_variable100_data[] = {0x0000000000000080LL};
static ANTLRBitSet *FOLLOW_declarator_in_variable100;
const static unsigned long long FOLLOW_7_in_variable102_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_7_in_variable102;
const static unsigned long long FOLLOW_SimpleCParser_ID_in_declarator121_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_ID_in_declarator121;
const static unsigned long long FOLLOW_type_in_functionHeader150_data[] = {0x0000000000000010LL};
static ANTLRBitSet *FOLLOW_type_in_functionHeader150;
const static unsigned long long FOLLOW_SimpleCParser_ID_in_functionHeader152_data[] = {0x0000000000000100LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_ID_in_functionHeader152;
const static unsigned long long FOLLOW_8_in_functionHeader154_data[] = {0x0000000000003C10LL};
static ANTLRBitSet *FOLLOW_8_in_functionHeader154;
const static unsigned long long FOLLOW_formalParameter_in_functionHeader158_data[] = {0x0000000000000600LL};
static ANTLRBitSet *FOLLOW_formalParameter_in_functionHeader158;
const static unsigned long long FOLLOW_9_in_functionHeader162_data[] = {0x0000000000003810LL};
static ANTLRBitSet *FOLLOW_9_in_functionHeader162;
const static unsigned long long FOLLOW_formalParameter_in_functionHeader164_data[] = {0x0000000000000600LL};
static ANTLRBitSet *FOLLOW_formalParameter_in_functionHeader164;
const static unsigned long long FOLLOW_10_in_functionHeader172_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_10_in_functionHeader172;
const static unsigned long long FOLLOW_type_in_formalParameter194_data[] = {0x0000000000000010LL};
static ANTLRBitSet *FOLLOW_type_in_formalParameter194;
const static unsigned long long FOLLOW_declarator_in_formalParameter196_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_declarator_in_formalParameter196;
const static unsigned long long FOLLOW_set_in_type223_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_set_in_type223;
const static unsigned long long FOLLOW_14_in_block285_data[] = {0x000000000001F9B0LL};
static ANTLRBitSet *FOLLOW_14_in_block285;
const static unsigned long long FOLLOW_variable_in_block299_data[] = {0x000000000001F9B0LL};
static ANTLRBitSet *FOLLOW_variable_in_block299;
const static unsigned long long FOLLOW_stat_in_block314_data[] = {0x000000000001C1B0LL};
static ANTLRBitSet *FOLLOW_stat_in_block314;
const static unsigned long long FOLLOW_15_in_block325_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_15_in_block325;
const static unsigned long long FOLLOW_forStat_in_stat337_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_forStat_in_stat337;
const static unsigned long long FOLLOW_expr_in_stat345_data[] = {0x0000000000000080LL};
static ANTLRBitSet *FOLLOW_expr_in_stat345;
const static unsigned long long FOLLOW_7_in_stat347_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_7_in_stat347;
const static unsigned long long FOLLOW_block_in_stat361_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_block_in_stat361;
const static unsigned long long FOLLOW_assignStat_in_stat369_data[] = {0x0000000000000080LL};
static ANTLRBitSet *FOLLOW_assignStat_in_stat369;
const static unsigned long long FOLLOW_7_in_stat371_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_7_in_stat371;
const static unsigned long long FOLLOW_7_in_stat379_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_7_in_stat379;
const static unsigned long long FOLLOW_16_in_forStat398_data[] = {0x0000000000000100LL};
static ANTLRBitSet *FOLLOW_16_in_forStat398;
const static unsigned long long FOLLOW_8_in_forStat400_data[] = {0x0000000000000010LL};
static ANTLRBitSet *FOLLOW_8_in_forStat400;
const static unsigned long long FOLLOW_assignStat_in_forStat402_data[] = {0x0000000000000080LL};
static ANTLRBitSet *FOLLOW_assignStat_in_forStat402;
const static unsigned long long FOLLOW_7_in_forStat404_data[] = {0x0000000000000130LL};
static ANTLRBitSet *FOLLOW_7_in_forStat404;
const static unsigned long long FOLLOW_expr_in_forStat406_data[] = {0x0000000000000080LL};
static ANTLRBitSet *FOLLOW_expr_in_forStat406;
const static unsigned long long FOLLOW_7_in_forStat408_data[] = {0x0000000000000010LL};
static ANTLRBitSet *FOLLOW_7_in_forStat408;
const static unsigned long long FOLLOW_assignStat_in_forStat410_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_assignStat_in_forStat410;
const static unsigned long long FOLLOW_10_in_forStat412_data[] = {0x0000000000004000LL};
static ANTLRBitSet *FOLLOW_10_in_forStat412;
const static unsigned long long FOLLOW_block_in_forStat414_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_block_in_forStat414;
const static unsigned long long FOLLOW_SimpleCParser_ID_in_assignStat441_data[] = {0x0000000000020000LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_ID_in_assignStat441;
const static unsigned long long FOLLOW_17_in_assignStat443_data[] = {0x0000000000000130LL};
static ANTLRBitSet *FOLLOW_17_in_assignStat443;
const static unsigned long long FOLLOW_expr_in_assignStat445_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_expr_in_assignStat445;
const static unsigned long long FOLLOW_condExpr_in_expr467_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_condExpr_in_expr467;
const static unsigned long long FOLLOW_aexpr_in_condExpr486_data[] = {0x00000000000C0002LL};
static ANTLRBitSet *FOLLOW_aexpr_in_condExpr486;
const static unsigned long long FOLLOW_set_in_condExpr491_data[] = {0x0000000000000130LL};
static ANTLRBitSet *FOLLOW_set_in_condExpr491;
const static unsigned long long FOLLOW_aexpr_in_condExpr498_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_aexpr_in_condExpr498;
const static unsigned long long FOLLOW_atom_in_aexpr520_data[] = {0x0000000000100002LL};
static ANTLRBitSet *FOLLOW_atom_in_aexpr520;
const static unsigned long long FOLLOW_20_in_aexpr524_data[] = {0x0000000000000130LL};
static ANTLRBitSet *FOLLOW_20_in_aexpr524;
const static unsigned long long FOLLOW_atom_in_aexpr526_data[] = {0x0000000000100002LL};
static ANTLRBitSet *FOLLOW_atom_in_aexpr526;
const static unsigned long long FOLLOW_SimpleCParser_ID_in_atom546_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_ID_in_atom546;
const static unsigned long long FOLLOW_SimpleCParser_INT_in_atom560_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_INT_in_atom560;
const static unsigned long long FOLLOW_8_in_atom574_data[] = {0x0000000000000130LL};
static ANTLRBitSet *FOLLOW_8_in_atom574;
const static unsigned long long FOLLOW_expr_in_atom576_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_expr_in_atom576;
const static unsigned long long FOLLOW_10_in_atom578_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_10_in_atom578;


#pragma mark Dynamic Global Scopes

#pragma mark Dynamic Rule Scopes

#pragma mark Rule return scopes start

@implementation SimpleCParser

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
	FOLLOW_SimpleCParser_ID_in_declarator121 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_ID_in_declarator121_data count:1];
	FOLLOW_type_in_functionHeader150 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_functionHeader150_data count:1];
	FOLLOW_SimpleCParser_ID_in_functionHeader152 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_ID_in_functionHeader152_data count:1];
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
	FOLLOW_SimpleCParser_ID_in_assignStat441 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_ID_in_assignStat441_data count:1];
	FOLLOW_17_in_assignStat443 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_17_in_assignStat443_data count:1];
	FOLLOW_expr_in_assignStat445 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_assignStat445_data count:1];
	FOLLOW_condExpr_in_expr467 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_condExpr_in_expr467_data count:1];
	FOLLOW_aexpr_in_condExpr486 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_aexpr_in_condExpr486_data count:1];
	FOLLOW_set_in_condExpr491 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_set_in_condExpr491_data count:1];
	FOLLOW_aexpr_in_condExpr498 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_aexpr_in_condExpr498_data count:1];
	FOLLOW_atom_in_aexpr520 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_atom_in_aexpr520_data count:1];
	FOLLOW_20_in_aexpr524 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_20_in_aexpr524_data count:1];
	FOLLOW_atom_in_aexpr526 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_atom_in_aexpr526_data count:1];
	FOLLOW_SimpleCParser_ID_in_atom546 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_ID_in_atom546_data count:1];
	FOLLOW_SimpleCParser_INT_in_atom560 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_INT_in_atom560_data count:1];
	FOLLOW_8_in_atom574 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_8_in_atom574_data count:1];
	FOLLOW_expr_in_atom576 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_atom576_data count:1];
	FOLLOW_10_in_atom578 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_10_in_atom578_data count:1];

}

- (id) initWithTokenStream:(id<ANTLRTokenStream>)aStream
{
	if ((self = [super initWithTokenStream:aStream])) {

		tokenNames = [[NSArray alloc] initWithObjects:@"<invalid>", @"<EOR>", @"<DOWN>", @"<UP>", 
	@"ID", @"INT", @"WS", @"';'", @"'('", @"','", @"')'", @"'int'", @"'char'", 
	@"'void'", @"'{'", @"'}'", @"'for'", @"'='", @"'=='", @"'<'", @"'+'", nil];
		dfa2 = [[SimpleCParserDFA2 alloc] initWithRecognizer:self];
																																
		ruleNames = [[NSArray alloc] initWithObjects:@"program", @"declaration", 
			@"variable", @"declarator", @"functionHeader", @"formalParameter", @"type", 
			@"block", @"stat", @"forStat", @"assignStat", @"expr", @"condExpr", @"aexpr", 
			@"atom", nil];

	}
	return self;
}

- (void) dealloc
{
	[tokenNames release];
	[dfa2 release];
	[ruleNames release];

	[super dealloc];
}

- (NSString *) grammarFileName
{
	return @"/Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g";
}


// $ANTLR start program
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:7:1: program : ( declaration )+ ;
- (void) program
{
    @try { [debugListener enterRule:@"program"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:7 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:8:9: ( ( declaration )+ ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:8:9: ( declaration )+ // alt
        {
        [debugListener locationLine:8 column:9];
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:8:9: ( declaration )+	// positiveClosureBlock
        int cnt1=0;
        @try { [debugListener enterSubRule:1];


        do {
            int alt1=2;
            @try { [debugListener enterDecision:1];

            {
            	int LA1_0 = [input LA:1];
            	if ( LA1_0==SimpleCParser_ID||(LA1_0>=11 && LA1_0<=13) ) {
            		alt1 = 1;
            	}

            }
            } @finally { [debugListener exitDecision:1]; }

            switch (alt1) {
        	case 1 :
        	    [debugListener enterAlt:1];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:8:9: declaration // alt
        	    {
        	    [debugListener locationLine:8 column:9];
        	    [following addObject:FOLLOW_declaration_in_program27];
        	    [self declaration];
        	    [following removeLastObject];



        	    }
        	    break;

        	default :
        	    if ( cnt1 >= 1 )  goto loop1;
        			ANTLREarlyExitException *eee = [ANTLREarlyExitException exceptionWithStream:input decisionNumber:1];
        			[debugListener recognitionException:eee];

        			@throw eee;
            }
            cnt1++;
        } while (YES); loop1: ;
        } @finally { [debugListener exitSubRule:1]; }


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
	[debugListener locationLine:9 column:5];

	}
	@finally {
	    [debugListener exitRule:@"program"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end program

// $ANTLR start declaration
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:11:1: declaration : ( variable | functionHeader ';' | functionHeader block );
- (void) declaration
{
    NSString* _functionHeader1 = nil;

    NSString* _functionHeader2 = nil;


    @try { [debugListener enterRule:@"declaration"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:11 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:21:9: ( variable | functionHeader ';' | functionHeader block ) //ruleblock
        int alt2=3;
        @try { [debugListener enterDecision:2];

        @try {
            // isCyclicDecision is only necessary for the Profiler. Which I didn't do, yet.
            // isCyclicDecision = YES;
            alt2 = [dfa2 predict];
        }
        @catch (ANTLRNoViableAltException *nvae) {
            [debugListener recognitionException:nvae];
            @throw nvae;
        }
        } @finally { [debugListener exitDecision:2]; }

        switch (alt2) {
        	case 1 :
        	    [debugListener enterAlt:1];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:21:9: variable // alt
        	    {
        	    [debugListener locationLine:21 column:9];
        	    [following addObject:FOLLOW_variable_in_declaration49];
        	    [self variable];
        	    [following removeLastObject];



        	    }
        	    break;
        	case 2 :
        	    [debugListener enterAlt:2];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:22:9: functionHeader ';' // alt
        	    {
        	    [debugListener locationLine:22 column:9];
        	    [following addObject:FOLLOW_functionHeader_in_declaration59];
        	    _functionHeader1 = [self functionHeader];
        	    [following removeLastObject];


        	    [debugListener locationLine:22 column:24];
        	    [self match:input tokenType:7 follow:FOLLOW_7_in_declaration61]; 
        	    [debugListener locationLine:23 column:2];
        	    NSLog(@"%@ is a declaration", _functionHeader1);

        	    }
        	    break;
        	case 3 :
        	    [debugListener enterAlt:3];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:24:9: functionHeader block // alt
        	    {
        	    [debugListener locationLine:24 column:9];
        	    [following addObject:FOLLOW_functionHeader_in_declaration74];
        	    _functionHeader2 = [self functionHeader];
        	    [following removeLastObject];


        	    [debugListener locationLine:24 column:24];
        	    [following addObject:FOLLOW_block_in_declaration76];
        	    [self block];
        	    [following removeLastObject];


        	    [debugListener locationLine:25 column:2];
        	    NSLog(@"%@ is a definition", _functionHeader2);

        	    }
        	    break;

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
		[_functionHeader1 release];
		[_functionHeader2 release];
		// rule refs in alts with rewrites

	}
	[debugListener locationLine:26 column:5];

	}
	@finally {
	    [debugListener exitRule:@"declaration"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end declaration

// $ANTLR start variable
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:28:1: variable : type declarator ';' ;
- (void) variable
{
    @try { [debugListener enterRule:@"variable"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:28 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:29:9: ( type declarator ';' ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:29:9: type declarator ';' // alt
        {
        [debugListener locationLine:29 column:9];
        [following addObject:FOLLOW_type_in_variable98];
        [self type];
        [following removeLastObject];


        [debugListener locationLine:29 column:14];
        [following addObject:FOLLOW_declarator_in_variable100];
        [self declarator];
        [following removeLastObject];


        [debugListener locationLine:29 column:25];
        [self match:input tokenType:7 follow:FOLLOW_7_in_variable102]; 

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
	[debugListener locationLine:30 column:5];

	}
	@finally {
	    [debugListener exitRule:@"variable"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end variable

// $ANTLR start declarator
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:32:1: declarator : ID ;
- (void) declarator
{
    @try { [debugListener enterRule:@"declarator"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:32 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:33:9: ( ID ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:33:9: ID // alt
        {
        [debugListener locationLine:33 column:9];
        [self match:input tokenType:SimpleCParser_ID follow:FOLLOW_SimpleCParser_ID_in_declarator121]; 

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
	[debugListener locationLine:34 column:5];

	}
	@finally {
	    [debugListener exitRule:@"declarator"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end declarator

// $ANTLR start functionHeader
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:36:1: functionHeader returns [NSString* name] : type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' ;
- (NSString*) functionHeader
{
    NSString* _name;
    ANTLRToken * _ID3 = nil;


        _name =nil;
    // double check this after beta release!
    [_name retain]; // for now you must init here rather than in 'returns'

    @try { [debugListener enterRule:@"functionHeader"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:36 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:40:9: ( type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:40:9: type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' // alt
        {
        [debugListener locationLine:40 column:9];
        [following addObject:FOLLOW_type_in_functionHeader150];
        [self type];
        [following removeLastObject];


        [debugListener locationLine:40 column:14];
        _ID3=(ANTLRToken *)[input LT:1];
        [_ID3 retain];
        [self match:input tokenType:SimpleCParser_ID follow:FOLLOW_SimpleCParser_ID_in_functionHeader152]; 
        [debugListener locationLine:40 column:17];
        [self match:input tokenType:8 follow:FOLLOW_8_in_functionHeader154]; 
        [debugListener locationLine:40 column:21];
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:40:21: ( formalParameter ( ',' formalParameter )* )? // block
        int alt4=2;
        @try { [debugListener enterSubRule:4];
        @try { [debugListener enterDecision:4];

        {
        	int LA4_0 = [input LA:1];
        	if ( LA4_0==SimpleCParser_ID||(LA4_0>=11 && LA4_0<=13) ) {
        		alt4 = 1;
        	}
        }
        } @finally { [debugListener exitDecision:4]; }

        switch (alt4) {
        	case 1 :
        	    [debugListener enterAlt:1];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:40:23: formalParameter ( ',' formalParameter )* // alt
        	    {
        	    [debugListener locationLine:40 column:23];
        	    [following addObject:FOLLOW_formalParameter_in_functionHeader158];
        	    [self formalParameter];
        	    [following removeLastObject];


        	    [debugListener locationLine:40 column:39];
        	    @try { [debugListener enterSubRule:3];

        	    do {
        	        int alt3=2;
        	        @try { [debugListener enterDecision:3];

        	        {
        	        	int LA3_0 = [input LA:1];
        	        	if ( LA3_0==9 ) {
        	        		alt3 = 1;
        	        	}

        	        }
        	        } @finally { [debugListener exitDecision:3]; }

        	        switch (alt3) {
        	    	case 1 :
        	    	    [debugListener enterAlt:1];

        	    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:40:41: ',' formalParameter // alt
        	    	    {
        	    	    [debugListener locationLine:40 column:41];
        	    	    [self match:input tokenType:9 follow:FOLLOW_9_in_functionHeader162]; 
        	    	    [debugListener locationLine:40 column:45];
        	    	    [following addObject:FOLLOW_formalParameter_in_functionHeader164];
        	    	    [self formalParameter];
        	    	    [following removeLastObject];



        	    	    }
        	    	    break;

        	    	default :
        	    	    goto loop3;
        	        }
        	    } while (YES); loop3: ;
        	    } @finally { [debugListener exitSubRule:3]; }


        	    }
        	    break;

        }
        } @finally { [debugListener exitSubRule:4]; }

        [debugListener locationLine:40 column:67];
        [self match:input tokenType:10 follow:FOLLOW_10_in_functionHeader172]; 
        [debugListener locationLine:41 column:2];
        _name = [_ID3 text];
        // double check this after beta release!
        [_name retain]; 

        }

    }
	@catch (ANTLRRecognitionException *re) {
		[self reportError:re];
		[self recover:input exception:re];
	}
	@finally {
		// token labels
		[_ID3 release];
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	[debugListener locationLine:42 column:5];

	}
	@finally {
	    [debugListener exitRule:@"functionHeader"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return _name;
}
// $ANTLR end functionHeader

// $ANTLR start formalParameter
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:44:1: formalParameter : type declarator ;
- (void) formalParameter
{
    @try { [debugListener enterRule:@"formalParameter"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:44 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:45:9: ( type declarator ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:45:9: type declarator // alt
        {
        [debugListener locationLine:45 column:9];
        [following addObject:FOLLOW_type_in_formalParameter194];
        [self type];
        [following removeLastObject];


        [debugListener locationLine:45 column:14];
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
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	[debugListener locationLine:46 column:5];

	}
	@finally {
	    [debugListener exitRule:@"formalParameter"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end formalParameter

// $ANTLR start type
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:48:1: type : ('int'|'char'|'void'|ID);
- (void) type
{
    @try { [debugListener enterRule:@"type"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:48 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:49:5: ( ('int'|'char'|'void'|ID)) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:49:9: ('int'|'char'|'void'|ID) // alt
        {
        [debugListener locationLine:49 column:9];
        if ([input LA:1]==SimpleCParser_ID||([input LA:1]>=11 && [input LA:1]<=13)) {
        	[input consume];
        	errorRecovery = NO;
        } else {
        	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
        	[debugListener recognitionException:mse];
        	[self recoverFromMismatchedSet:input exception:mse follow:FOLLOW_set_in_type223];	@throw mse;
        }


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
	[debugListener locationLine:53 column:5];

	}
	@finally {
	    [debugListener exitRule:@"type"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end type

// $ANTLR start block
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:55:1: block : '{' ( variable )* ( stat )* '}' ;
- (void) block
{
    @try { [debugListener enterRule:@"block"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:55 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:56:9: ( '{' ( variable )* ( stat )* '}' ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:56:9: '{' ( variable )* ( stat )* '}' // alt
        {
        [debugListener locationLine:56 column:9];
        [self match:input tokenType:14 follow:FOLLOW_14_in_block285]; 
        [debugListener locationLine:57 column:13];
        @try { [debugListener enterSubRule:5];

        do {
            int alt5=2;
            @try { [debugListener enterDecision:5];

            {
            	int LA5_0 = [input LA:1];
            	if ( LA5_0==SimpleCParser_ID ) {
            		{
            			int LA5_2 = [input LA:2];
            			if ( LA5_2==SimpleCParser_ID ) {
            				alt5 = 1;
            			}

            		}
            	}
            	else if ( (LA5_0>=11 && LA5_0<=13) ) {
            		alt5 = 1;
            	}

            }
            } @finally { [debugListener exitDecision:5]; }

            switch (alt5) {
        	case 1 :
        	    [debugListener enterAlt:1];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:57:13: variable // alt
        	    {
        	    [debugListener locationLine:57 column:13];
        	    [following addObject:FOLLOW_variable_in_block299];
        	    [self variable];
        	    [following removeLastObject];



        	    }
        	    break;

        	default :
        	    goto loop5;
            }
        } while (YES); loop5: ;
        } @finally { [debugListener exitSubRule:5]; }

        [debugListener locationLine:58 column:13];
        @try { [debugListener enterSubRule:6];

        do {
            int alt6=2;
            @try { [debugListener enterDecision:6];

            {
            	int LA6_0 = [input LA:1];
            	if ( (LA6_0>=SimpleCParser_ID && LA6_0<=SimpleCParser_INT)||(LA6_0>=7 && LA6_0<=8)||LA6_0==14||LA6_0==16 ) {
            		alt6 = 1;
            	}

            }
            } @finally { [debugListener exitDecision:6]; }

            switch (alt6) {
        	case 1 :
        	    [debugListener enterAlt:1];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:58:13: stat // alt
        	    {
        	    [debugListener locationLine:58 column:13];
        	    [following addObject:FOLLOW_stat_in_block314];
        	    [self stat];
        	    [following removeLastObject];



        	    }
        	    break;

        	default :
        	    goto loop6;
            }
        } while (YES); loop6: ;
        } @finally { [debugListener exitSubRule:6]; }

        [debugListener locationLine:59 column:9];
        [self match:input tokenType:15 follow:FOLLOW_15_in_block325]; 

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
	[debugListener locationLine:60 column:5];

	}
	@finally {
	    [debugListener exitRule:@"block"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end block

// $ANTLR start stat
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:62:1: stat : ( forStat | expr ';' | block | assignStat ';' | ';' );
- (void) stat
{
    @try { [debugListener enterRule:@"stat"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:62 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:62:7: ( forStat | expr ';' | block | assignStat ';' | ';' ) //ruleblock
        int alt7=5;
        @try { [debugListener enterDecision:7];

        switch ([input LA:1]) {
        	case 16:
        		alt7 = 1;
        		break;
        	case SimpleCParser_ID:
        		{
        			int LA7_2 = [input LA:2];
        			if ( LA7_2==17 ) {
        				alt7 = 4;
        			}
        			else if ( LA7_2==7||(LA7_2>=18 && LA7_2<=20) ) {
        				alt7 = 2;
        			}
        		else {
        		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:7 state:2 stream:input];
        			[debugListener recognitionException:nvae];
        			@throw nvae;
        			}
        		}
        		break;
        	case SimpleCParser_INT:
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
        	[debugListener recognitionException:nvae];
        	@throw nvae;

        	}}
        } @finally { [debugListener exitDecision:7]; }

        switch (alt7) {
        	case 1 :
        	    [debugListener enterAlt:1];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:62:7: forStat // alt
        	    {
        	    [debugListener locationLine:62 column:7];
        	    [following addObject:FOLLOW_forStat_in_stat337];
        	    [self forStat];
        	    [following removeLastObject];



        	    }
        	    break;
        	case 2 :
        	    [debugListener enterAlt:2];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:63:7: expr ';' // alt
        	    {
        	    [debugListener locationLine:63 column:7];
        	    [following addObject:FOLLOW_expr_in_stat345];
        	    [self expr];
        	    [following removeLastObject];


        	    [debugListener locationLine:63 column:12];
        	    [self match:input tokenType:7 follow:FOLLOW_7_in_stat347]; 

        	    }
        	    break;
        	case 3 :
        	    [debugListener enterAlt:3];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:64:7: block // alt
        	    {
        	    [debugListener locationLine:64 column:7];
        	    [following addObject:FOLLOW_block_in_stat361];
        	    [self block];
        	    [following removeLastObject];



        	    }
        	    break;
        	case 4 :
        	    [debugListener enterAlt:4];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:65:7: assignStat ';' // alt
        	    {
        	    [debugListener locationLine:65 column:7];
        	    [following addObject:FOLLOW_assignStat_in_stat369];
        	    [self assignStat];
        	    [following removeLastObject];


        	    [debugListener locationLine:65 column:18];
        	    [self match:input tokenType:7 follow:FOLLOW_7_in_stat371]; 

        	    }
        	    break;
        	case 5 :
        	    [debugListener enterAlt:5];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:66:7: ';' // alt
        	    {
        	    [debugListener locationLine:66 column:7];
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
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	[debugListener locationLine:67 column:5];

	}
	@finally {
	    [debugListener exitRule:@"stat"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end stat

// $ANTLR start forStat
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:69:1: forStat : 'for' '(' assignStat ';' expr ';' assignStat ')' block ;
- (void) forStat
{
    @try { [debugListener enterRule:@"forStat"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:69 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:70:9: ( 'for' '(' assignStat ';' expr ';' assignStat ')' block ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:70:9: 'for' '(' assignStat ';' expr ';' assignStat ')' block // alt
        {
        [debugListener locationLine:70 column:9];
        [self match:input tokenType:16 follow:FOLLOW_16_in_forStat398]; 
        [debugListener locationLine:70 column:15];
        [self match:input tokenType:8 follow:FOLLOW_8_in_forStat400]; 
        [debugListener locationLine:70 column:19];
        [following addObject:FOLLOW_assignStat_in_forStat402];
        [self assignStat];
        [following removeLastObject];


        [debugListener locationLine:70 column:30];
        [self match:input tokenType:7 follow:FOLLOW_7_in_forStat404]; 
        [debugListener locationLine:70 column:34];
        [following addObject:FOLLOW_expr_in_forStat406];
        [self expr];
        [following removeLastObject];


        [debugListener locationLine:70 column:39];
        [self match:input tokenType:7 follow:FOLLOW_7_in_forStat408]; 
        [debugListener locationLine:70 column:43];
        [following addObject:FOLLOW_assignStat_in_forStat410];
        [self assignStat];
        [following removeLastObject];


        [debugListener locationLine:70 column:54];
        [self match:input tokenType:10 follow:FOLLOW_10_in_forStat412]; 
        [debugListener locationLine:70 column:58];
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
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	[debugListener locationLine:71 column:5];

	}
	@finally {
	    [debugListener exitRule:@"forStat"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end forStat

// $ANTLR start assignStat
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:73:1: assignStat : ID '=' expr ;
- (void) assignStat
{
    @try { [debugListener enterRule:@"assignStat"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:73 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:74:9: ( ID '=' expr ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:74:9: ID '=' expr // alt
        {
        [debugListener locationLine:74 column:9];
        [self match:input tokenType:SimpleCParser_ID follow:FOLLOW_SimpleCParser_ID_in_assignStat441]; 
        [debugListener locationLine:74 column:12];
        [self match:input tokenType:17 follow:FOLLOW_17_in_assignStat443]; 
        [debugListener locationLine:74 column:16];
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
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	[debugListener locationLine:75 column:5];

	}
	@finally {
	    [debugListener exitRule:@"assignStat"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end assignStat

// $ANTLR start expr
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:77:1: expr : condExpr ;
- (void) expr
{
    @try { [debugListener enterRule:@"expr"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:77 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:77:9: ( condExpr ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:77:9: condExpr // alt
        {
        [debugListener locationLine:77 column:9];
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
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	[debugListener locationLine:78 column:5];

	}
	@finally {
	    [debugListener exitRule:@"expr"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end expr

// $ANTLR start condExpr
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:80:1: condExpr : aexpr ( ('=='|'<') aexpr )? ;
- (void) condExpr
{
    @try { [debugListener enterRule:@"condExpr"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:80 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:81:9: ( aexpr ( ('=='|'<') aexpr )? ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:81:9: aexpr ( ('=='|'<') aexpr )? // alt
        {
        [debugListener locationLine:81 column:9];
        [following addObject:FOLLOW_aexpr_in_condExpr486];
        [self aexpr];
        [following removeLastObject];


        [debugListener locationLine:81 column:15];
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:81:15: ( ('=='|'<') aexpr )? // block
        int alt8=2;
        @try { [debugListener enterSubRule:8];
        @try { [debugListener enterDecision:8];

        {
        	int LA8_0 = [input LA:1];
        	if ( (LA8_0>=18 && LA8_0<=19) ) {
        		alt8 = 1;
        	}
        }
        } @finally { [debugListener exitDecision:8]; }

        switch (alt8) {
        	case 1 :
        	    [debugListener enterAlt:1];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:81:17: ('=='|'<') aexpr // alt
        	    {
        	    [debugListener locationLine:81 column:17];
        	    if (([input LA:1]>=18 && [input LA:1]<=19)) {
        	    	[input consume];
        	    	errorRecovery = NO;
        	    } else {
        	    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
        	    	[debugListener recognitionException:mse];
        	    	[self recoverFromMismatchedSet:input exception:mse follow:FOLLOW_set_in_condExpr491];	@throw mse;
        	    }

        	    [debugListener locationLine:81 column:30];
        	    [following addObject:FOLLOW_aexpr_in_condExpr498];
        	    [self aexpr];
        	    [following removeLastObject];



        	    }
        	    break;

        }
        } @finally { [debugListener exitSubRule:8]; }


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
	[debugListener locationLine:82 column:5];

	}
	@finally {
	    [debugListener exitRule:@"condExpr"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end condExpr

// $ANTLR start aexpr
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:84:1: aexpr : atom ( '+' atom )* ;
- (void) aexpr
{
    @try { [debugListener enterRule:@"aexpr"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:84 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:85:9: ( atom ( '+' atom )* ) // ruleBlockSingleAlt
        [debugListener enterAlt:1];

        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:85:9: atom ( '+' atom )* // alt
        {
        [debugListener locationLine:85 column:9];
        [following addObject:FOLLOW_atom_in_aexpr520];
        [self atom];
        [following removeLastObject];


        [debugListener locationLine:85 column:14];
        @try { [debugListener enterSubRule:9];

        do {
            int alt9=2;
            @try { [debugListener enterDecision:9];

            {
            	int LA9_0 = [input LA:1];
            	if ( LA9_0==20 ) {
            		alt9 = 1;
            	}

            }
            } @finally { [debugListener exitDecision:9]; }

            switch (alt9) {
        	case 1 :
        	    [debugListener enterAlt:1];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:85:16: '+' atom // alt
        	    {
        	    [debugListener locationLine:85 column:16];
        	    [self match:input tokenType:20 follow:FOLLOW_20_in_aexpr524]; 
        	    [debugListener locationLine:85 column:20];
        	    [following addObject:FOLLOW_atom_in_aexpr526];
        	    [self atom];
        	    [following removeLastObject];



        	    }
        	    break;

        	default :
        	    goto loop9;
            }
        } while (YES); loop9: ;
        } @finally { [debugListener exitSubRule:9]; }


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
	[debugListener locationLine:86 column:5];

	}
	@finally {
	    [debugListener exitRule:@"aexpr"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end aexpr

// $ANTLR start atom
// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:88:1: atom : ( ID | INT | '(' expr ')' );
- (void) atom
{
    @try { [debugListener enterRule:@"atom"];
    if ( ruleLevel==0 ) [debugListener commence];
    ruleLevel++;
    [debugListener locationLine:88 column:1];

    @try {
        // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:89:7: ( ID | INT | '(' expr ')' ) //ruleblock
        int alt10=3;
        @try { [debugListener enterDecision:10];

        switch ([input LA:1]) {
        	case SimpleCParser_ID:
        		alt10 = 1;
        		break;
        	case SimpleCParser_INT:
        		alt10 = 2;
        		break;
        	case 8:
        		alt10 = 3;
        		break;
        default:
         {
            ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:10 state:0 stream:input];
        	[debugListener recognitionException:nvae];
        	@throw nvae;

        	}}
        } @finally { [debugListener exitDecision:10]; }

        switch (alt10) {
        	case 1 :
        	    [debugListener enterAlt:1];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:89:7: ID // alt
        	    {
        	    [debugListener locationLine:89 column:7];
        	    [self match:input tokenType:SimpleCParser_ID follow:FOLLOW_SimpleCParser_ID_in_atom546]; 

        	    }
        	    break;
        	case 2 :
        	    [debugListener enterAlt:2];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:90:7: INT // alt
        	    {
        	    [debugListener locationLine:90 column:7];
        	    [self match:input tokenType:SimpleCParser_INT follow:FOLLOW_SimpleCParser_INT_in_atom560]; 

        	    }
        	    break;
        	case 3 :
        	    [debugListener enterAlt:3];

        	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/LL-star/SimpleC.g:91:7: '(' expr ')' // alt
        	    {
        	    [debugListener locationLine:91 column:7];
        	    [self match:input tokenType:8 follow:FOLLOW_8_in_atom574]; 
        	    [debugListener locationLine:91 column:11];
        	    [following addObject:FOLLOW_expr_in_atom576];
        	    [self expr];
        	    [following removeLastObject];


        	    [debugListener locationLine:91 column:16];
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
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

	}
	[debugListener locationLine:92 column:5];

	}
	@finally {
	    [debugListener exitRule:@"atom"];
	    ruleLevel--;
	    if ( ruleLevel==0 ) [debugListener terminate];
	}

	return ;
}
// $ANTLR end atom


-(BOOL) evalPredicate:(NSString *)predicate matched:(BOOL)result
{
	[debugListener semanticPredicate:predicate matched:result];
	return result;
}


@end