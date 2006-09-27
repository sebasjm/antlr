// $ANTLR 3.0b5 SimpleCTP.g 2006-09-27 02:50:36

#import "SimpleCTPTreeParser.h"

#pragma mark Cyclic DFA

#pragma mark Bitsets
const static unsigned long long FOLLOW_declaration_in_program37_data[] = {0x0000000000000192LL};
static ANTLRBitSet *FOLLOW_declaration_in_program37;
const static unsigned long long FOLLOW_variable_in_declaration57_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_variable_in_declaration57;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_FUNC_DECL_in_declaration68_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_FUNC_DECL_in_declaration68;
const static unsigned long long FOLLOW_functionHeader_in_declaration70_data[] = {0x0000000000000008LL};
static ANTLRBitSet *FOLLOW_functionHeader_in_declaration70;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_FUNC_DEF_in_declaration82_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_FUNC_DEF_in_declaration82;
const static unsigned long long FOLLOW_functionHeader_in_declaration84_data[] = {0x0000000000000200LL};
static ANTLRBitSet *FOLLOW_functionHeader_in_declaration84;
const static unsigned long long FOLLOW_block_in_declaration86_data[] = {0x0000000000000008LL};
static ANTLRBitSet *FOLLOW_block_in_declaration86;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_VAR_DEF_in_variable107_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_VAR_DEF_in_variable107;
const static unsigned long long FOLLOW_type_in_variable109_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_type_in_variable109;
const static unsigned long long FOLLOW_declarator_in_variable111_data[] = {0x0000000000000008LL};
static ANTLRBitSet *FOLLOW_declarator_in_variable111;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_ID_in_declarator131_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_ID_in_declarator131;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_FUNC_HDR_in_functionHeader152_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_FUNC_HDR_in_functionHeader152;
const static unsigned long long FOLLOW_type_in_functionHeader154_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_type_in_functionHeader154;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_ID_in_functionHeader156_data[] = {0x0000000000000020LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_ID_in_functionHeader156;
const static unsigned long long FOLLOW_formalParameter_in_functionHeader158_data[] = {0x0000000000000028LL};
static ANTLRBitSet *FOLLOW_formalParameter_in_functionHeader158;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_ARG_DEF_in_formalParameter180_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_ARG_DEF_in_formalParameter180;
const static unsigned long long FOLLOW_type_in_formalParameter182_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_type_in_formalParameter182;
const static unsigned long long FOLLOW_declarator_in_formalParameter184_data[] = {0x0000000000000008LL};
static ANTLRBitSet *FOLLOW_declarator_in_formalParameter184;
const static unsigned long long FOLLOW_set_in_type204_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_set_in_type204;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_BLOCK_in_block262_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_BLOCK_in_block262;
const static unsigned long long FOLLOW_variable_in_block264_data[] = {0x00000000000E3E18LL};
static ANTLRBitSet *FOLLOW_variable_in_block264;
const static unsigned long long FOLLOW_stat_in_block267_data[] = {0x00000000000E3E08LL};
static ANTLRBitSet *FOLLOW_stat_in_block267;
const static unsigned long long FOLLOW_forStat_in_stat281_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_forStat_in_stat281;
const static unsigned long long FOLLOW_expr_in_stat289_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_expr_in_stat289;
const static unsigned long long FOLLOW_block_in_stat297_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_block_in_stat297;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_FOR_in_forStat317_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_FOR_in_forStat317;
const static unsigned long long FOLLOW_expr_in_forStat319_data[] = {0x00000000000E1C00LL};
static ANTLRBitSet *FOLLOW_expr_in_forStat319;
const static unsigned long long FOLLOW_expr_in_forStat321_data[] = {0x00000000000E1C00LL};
static ANTLRBitSet *FOLLOW_expr_in_forStat321;
const static unsigned long long FOLLOW_expr_in_forStat323_data[] = {0x0000000000000200LL};
static ANTLRBitSet *FOLLOW_expr_in_forStat323;
const static unsigned long long FOLLOW_block_in_forStat325_data[] = {0x0000000000000008LL};
static ANTLRBitSet *FOLLOW_block_in_forStat325;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_EQEQ_in_expr341_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_EQEQ_in_expr341;
const static unsigned long long FOLLOW_expr_in_expr343_data[] = {0x00000000000E1C00LL};
static ANTLRBitSet *FOLLOW_expr_in_expr343;
const static unsigned long long FOLLOW_expr_in_expr345_data[] = {0x0000000000000008LL};
static ANTLRBitSet *FOLLOW_expr_in_expr345;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_LT_in_expr357_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_LT_in_expr357;
const static unsigned long long FOLLOW_expr_in_expr359_data[] = {0x00000000000E1C00LL};
static ANTLRBitSet *FOLLOW_expr_in_expr359;
const static unsigned long long FOLLOW_expr_in_expr361_data[] = {0x0000000000000008LL};
static ANTLRBitSet *FOLLOW_expr_in_expr361;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_PLUS_in_expr373_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_PLUS_in_expr373;
const static unsigned long long FOLLOW_expr_in_expr375_data[] = {0x00000000000E1C00LL};
static ANTLRBitSet *FOLLOW_expr_in_expr375;
const static unsigned long long FOLLOW_expr_in_expr377_data[] = {0x0000000000000008LL};
static ANTLRBitSet *FOLLOW_expr_in_expr377;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_EQ_in_expr389_data[] = {0x0000000000000004LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_EQ_in_expr389;
const static unsigned long long FOLLOW_SimpleCTPTreeParser_ID_in_expr391_data[] = {0x00000000000E1C00LL};
static ANTLRBitSet *FOLLOW_SimpleCTPTreeParser_ID_in_expr391;
const static unsigned long long FOLLOW_expr_in_expr393_data[] = {0x0000000000000008LL};
static ANTLRBitSet *FOLLOW_expr_in_expr393;
const static unsigned long long FOLLOW_atom_in_expr404_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_atom_in_expr404;
const static unsigned long long FOLLOW_set_in_atom421_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_set_in_atom421;


#pragma mark Scopes

#pragma mark Rule return scopes start

@implementation SimpleCTPTreeParser

+ (void) initialize
{
	FOLLOW_declaration_in_program37 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_declaration_in_program37_data count:1];
	FOLLOW_variable_in_declaration57 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_variable_in_declaration57_data count:1];
	FOLLOW_SimpleCTPTreeParser_FUNC_DECL_in_declaration68 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_FUNC_DECL_in_declaration68_data count:1];
	FOLLOW_functionHeader_in_declaration70 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_functionHeader_in_declaration70_data count:1];
	FOLLOW_SimpleCTPTreeParser_FUNC_DEF_in_declaration82 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_FUNC_DEF_in_declaration82_data count:1];
	FOLLOW_functionHeader_in_declaration84 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_functionHeader_in_declaration84_data count:1];
	FOLLOW_block_in_declaration86 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_block_in_declaration86_data count:1];
	FOLLOW_SimpleCTPTreeParser_VAR_DEF_in_variable107 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_VAR_DEF_in_variable107_data count:1];
	FOLLOW_type_in_variable109 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_variable109_data count:1];
	FOLLOW_declarator_in_variable111 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_declarator_in_variable111_data count:1];
	FOLLOW_SimpleCTPTreeParser_ID_in_declarator131 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_ID_in_declarator131_data count:1];
	FOLLOW_SimpleCTPTreeParser_FUNC_HDR_in_functionHeader152 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_FUNC_HDR_in_functionHeader152_data count:1];
	FOLLOW_type_in_functionHeader154 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_functionHeader154_data count:1];
	FOLLOW_SimpleCTPTreeParser_ID_in_functionHeader156 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_ID_in_functionHeader156_data count:1];
	FOLLOW_formalParameter_in_functionHeader158 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_formalParameter_in_functionHeader158_data count:1];
	FOLLOW_SimpleCTPTreeParser_ARG_DEF_in_formalParameter180 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_ARG_DEF_in_formalParameter180_data count:1];
	FOLLOW_type_in_formalParameter182 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_formalParameter182_data count:1];
	FOLLOW_declarator_in_formalParameter184 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_declarator_in_formalParameter184_data count:1];
	FOLLOW_set_in_type204 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_set_in_type204_data count:1];
	FOLLOW_SimpleCTPTreeParser_BLOCK_in_block262 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_BLOCK_in_block262_data count:1];
	FOLLOW_variable_in_block264 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_variable_in_block264_data count:1];
	FOLLOW_stat_in_block267 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_stat_in_block267_data count:1];
	FOLLOW_forStat_in_stat281 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_forStat_in_stat281_data count:1];
	FOLLOW_expr_in_stat289 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_stat289_data count:1];
	FOLLOW_block_in_stat297 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_block_in_stat297_data count:1];
	FOLLOW_SimpleCTPTreeParser_FOR_in_forStat317 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_FOR_in_forStat317_data count:1];
	FOLLOW_expr_in_forStat319 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_forStat319_data count:1];
	FOLLOW_expr_in_forStat321 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_forStat321_data count:1];
	FOLLOW_expr_in_forStat323 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_forStat323_data count:1];
	FOLLOW_block_in_forStat325 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_block_in_forStat325_data count:1];
	FOLLOW_SimpleCTPTreeParser_EQEQ_in_expr341 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_EQEQ_in_expr341_data count:1];
	FOLLOW_expr_in_expr343 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_expr343_data count:1];
	FOLLOW_expr_in_expr345 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_expr345_data count:1];
	FOLLOW_SimpleCTPTreeParser_LT_in_expr357 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_LT_in_expr357_data count:1];
	FOLLOW_expr_in_expr359 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_expr359_data count:1];
	FOLLOW_expr_in_expr361 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_expr361_data count:1];
	FOLLOW_SimpleCTPTreeParser_PLUS_in_expr373 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_PLUS_in_expr373_data count:1];
	FOLLOW_expr_in_expr375 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_expr375_data count:1];
	FOLLOW_expr_in_expr377 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_expr377_data count:1];
	FOLLOW_SimpleCTPTreeParser_EQ_in_expr389 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_EQ_in_expr389_data count:1];
	FOLLOW_SimpleCTPTreeParser_ID_in_expr391 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCTPTreeParser_ID_in_expr391_data count:1];
	FOLLOW_expr_in_expr393 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_expr393_data count:1];
	FOLLOW_atom_in_expr404 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_atom_in_expr404_data count:1];
	FOLLOW_set_in_atom421 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_set_in_atom421_data count:1];

}


- (id) initWithTreeNodeStream:(id<ANTLRTreeNodeStream>)aStream
{
	if ((self = [super initWithTreeNodeStream:aStream])) {

		tokenNames = [[NSArray alloc] initWithObjects:@"<invalid>", @"<EOR>", @"<DOWN>", @"<UP>",     @"VAR_DEF",     @"ARG_DEF",     @"FUNC_HDR",     @"FUNC_DECL",     @"FUNC_DEF",     @"BLOCK",     @"ID",     @"EQ",     @"INT",     @"FOR",     @"INT_TYPE",     @"CHAR",     @"VOID",     @"EQEQ",     @"LT",     @"PLUS",     @"WS",     @"T21",     @"T22",     @"T23",     @"T24",     @"T25",     @"T26",     @"Tokens", nil];

	}
	return self;
}

- (void) dealloc
{
	[tokenNames release];

	[super dealloc];
}



// $ANTLR start program
// SimpleCTP.g:7:1: program : ( declaration )+ ;
- (void) program
{
    @try {
        // SimpleCTP.g:8:9: ( ( declaration )+ ) // ruleBlockSingleAlt
        // SimpleCTP.g:8:9: ( declaration )+ // alt
        {
        // SimpleCTP.g:8:9: ( declaration )+	// positiveClosureBlock
        int cnt1=0;

        do {
            int alt1=2;
            int LA1_0 = [input LA:1];
            if ( LA1_0==SimpleCTPTreeParser_VAR_DEF||(LA1_0>=SimpleCTPTreeParser_FUNC_DECL && LA1_0<=SimpleCTPTreeParser_FUNC_DEF) ) {
            	alt1 = 1;
            }


            switch (alt1) {
        	case 1 :
        	    // SimpleCTP.g:8:9: declaration // alt
        	    {
        	    [following addObject:FOLLOW_declaration_in_program37];
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
        // token labels
        // token+rule list labels
        // rule labels
        // rule refs in alts with rewrites

    }
    return ;
}
// $ANTLR end program


// $ANTLR start declaration
// SimpleCTP.g:11:1: declaration : ( variable | ^( FUNC_DECL functionHeader ) | ^( FUNC_DEF functionHeader block ) );
- (void) declaration
{
    @try {
        // SimpleCTP.g:12:9: ( variable | ^( FUNC_DECL functionHeader ) | ^( FUNC_DEF functionHeader block ) ) //ruleblock
        int alt2=3;
        switch ([input LA:1]) {
        	case SimpleCTPTreeParser_VAR_DEF:
        		alt2 = 1;
        		break;
        	case SimpleCTPTreeParser_FUNC_DECL:
        		alt2 = 2;
        		break;
        	case SimpleCTPTreeParser_FUNC_DEF:
        		alt2 = 3;
        		break;
        default:
         {
            ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:2 state:0 stream:input];
        	@throw nvae;

        	}}
        switch (alt2) {
        	case 1 :
        	    // SimpleCTP.g:12:9: variable // alt
        	    {
        	    [following addObject:FOLLOW_variable_in_declaration57];
        	    [self variable];
        	    [following removeLastObject];



        	    }
        	    break;
        	case 2 :
        	    // SimpleCTP.g:13:9: ^( FUNC_DECL functionHeader ) // alt
        	    {
        	    [self match:input tokenType:SimpleCTPTreeParser_FUNC_DECL follow:FOLLOW_SimpleCTPTreeParser_FUNC_DECL_in_declaration68]; 

        	    [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
        	    [following addObject:FOLLOW_functionHeader_in_declaration70];
        	    [self functionHeader];
        	    [following removeLastObject];



        	    [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 

        	    }
        	    break;
        	case 3 :
        	    // SimpleCTP.g:14:9: ^( FUNC_DEF functionHeader block ) // alt
        	    {
        	    [self match:input tokenType:SimpleCTPTreeParser_FUNC_DEF follow:FOLLOW_SimpleCTPTreeParser_FUNC_DEF_in_declaration82]; 

        	    [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
        	    [following addObject:FOLLOW_functionHeader_in_declaration84];
        	    [self functionHeader];
        	    [following removeLastObject];


        	    [following addObject:FOLLOW_block_in_declaration86];
        	    [self block];
        	    [following removeLastObject];



        	    [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 

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
    return ;
}
// $ANTLR end declaration


// $ANTLR start variable
// SimpleCTP.g:17:1: variable : ^( VAR_DEF type declarator ) ;
- (void) variable
{
    @try {
        // SimpleCTP.g:18:9: ( ^( VAR_DEF type declarator ) ) // ruleBlockSingleAlt
        // SimpleCTP.g:18:9: ^( VAR_DEF type declarator ) // alt
        {
        [self match:input tokenType:SimpleCTPTreeParser_VAR_DEF follow:FOLLOW_SimpleCTPTreeParser_VAR_DEF_in_variable107]; 

        [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
        [following addObject:FOLLOW_type_in_variable109];
        [self type];
        [following removeLastObject];


        [following addObject:FOLLOW_declarator_in_variable111];
        [self declarator];
        [following removeLastObject];



        [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 

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
// $ANTLR end variable


// $ANTLR start declarator
// SimpleCTP.g:21:1: declarator : ID ;
- (void) declarator
{
    @try {
        // SimpleCTP.g:22:9: ( ID ) // ruleBlockSingleAlt
        // SimpleCTP.g:22:9: ID // alt
        {
        [self match:input tokenType:SimpleCTPTreeParser_ID follow:FOLLOW_SimpleCTPTreeParser_ID_in_declarator131]; 

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
// $ANTLR end declarator


// $ANTLR start functionHeader
// SimpleCTP.g:25:1: functionHeader : ^( FUNC_HDR type ID ( formalParameter )+ ) ;
- (void) functionHeader
{
    @try {
        // SimpleCTP.g:26:9: ( ^( FUNC_HDR type ID ( formalParameter )+ ) ) // ruleBlockSingleAlt
        // SimpleCTP.g:26:9: ^( FUNC_HDR type ID ( formalParameter )+ ) // alt
        {
        [self match:input tokenType:SimpleCTPTreeParser_FUNC_HDR follow:FOLLOW_SimpleCTPTreeParser_FUNC_HDR_in_functionHeader152]; 

        [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
        [following addObject:FOLLOW_type_in_functionHeader154];
        [self type];
        [following removeLastObject];


        [self match:input tokenType:SimpleCTPTreeParser_ID follow:FOLLOW_SimpleCTPTreeParser_ID_in_functionHeader156]; 
        // SimpleCTP.g:26:28: ( formalParameter )+	// positiveClosureBlock
        int cnt3=0;

        do {
            int alt3=2;
            int LA3_0 = [input LA:1];
            if ( LA3_0==SimpleCTPTreeParser_ARG_DEF ) {
            	alt3 = 1;
            }


            switch (alt3) {
        	case 1 :
        	    // SimpleCTP.g:26:28: formalParameter // alt
        	    {
        	    [following addObject:FOLLOW_formalParameter_in_functionHeader158];
        	    [self formalParameter];
        	    [following removeLastObject];



        	    }
        	    break;

        	default :
        	    if ( cnt3 >= 1 )  goto loop3;
        			ANTLREarlyExitException *eee = [ANTLREarlyExitException exceptionWithStream:input decisionNumber:3];
        			@throw eee;
            }
            cnt3++;
        } while (YES); loop3: ;


        [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 

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
// $ANTLR end functionHeader


// $ANTLR start formalParameter
// SimpleCTP.g:29:1: formalParameter : ^( ARG_DEF type declarator ) ;
- (void) formalParameter
{
    @try {
        // SimpleCTP.g:30:9: ( ^( ARG_DEF type declarator ) ) // ruleBlockSingleAlt
        // SimpleCTP.g:30:9: ^( ARG_DEF type declarator ) // alt
        {
        [self match:input tokenType:SimpleCTPTreeParser_ARG_DEF follow:FOLLOW_SimpleCTPTreeParser_ARG_DEF_in_formalParameter180]; 

        [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
        [following addObject:FOLLOW_type_in_formalParameter182];
        [self type];
        [following removeLastObject];


        [following addObject:FOLLOW_declarator_in_formalParameter184];
        [self declarator];
        [following removeLastObject];



        [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 

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
// $ANTLR end formalParameter


// $ANTLR start type
// SimpleCTP.g:33:1: type : (INT_TYPE|CHAR|VOID|ID);
- (void) type
{
    @try {
        // SimpleCTP.g:34:5: ( (INT_TYPE|CHAR|VOID|ID)) // ruleBlockSingleAlt
        // SimpleCTP.g:34:9: (INT_TYPE|CHAR|VOID|ID) // alt
        {
        if ([input LA:1]==SimpleCTPTreeParser_ID||([input LA:1]>=SimpleCTPTreeParser_INT_TYPE && [input LA:1]<=SimpleCTPTreeParser_VOID)) {
        	[input consume];
        	errorRecovery = NO;
        } else {
        	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
        	[self recoverFromMismatchedSet:input exception:mse follow:FOLLOW_set_in_type204];	@throw mse;
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
    return ;
}
// $ANTLR end type


// $ANTLR start block
// SimpleCTP.g:40:1: block : ^( BLOCK ( variable )* ( stat )* ) ;
- (void) block
{
    @try {
        // SimpleCTP.g:41:9: ( ^( BLOCK ( variable )* ( stat )* ) ) // ruleBlockSingleAlt
        // SimpleCTP.g:41:9: ^( BLOCK ( variable )* ( stat )* ) // alt
        {
        [self match:input tokenType:SimpleCTPTreeParser_BLOCK follow:FOLLOW_SimpleCTPTreeParser_BLOCK_in_block262]; 

        if ( [input LA:1] == ANTLRTokenTypeDOWN ) {
            [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
            do {
                int alt4=2;
                int LA4_0 = [input LA:1];
                if ( LA4_0==SimpleCTPTreeParser_VAR_DEF ) {
                	alt4 = 1;
                }


                switch (alt4) {
            	case 1 :
            	    // SimpleCTP.g:41:17: variable // alt
            	    {
            	    [following addObject:FOLLOW_variable_in_block264];
            	    [self variable];
            	    [following removeLastObject];



            	    }
            	    break;

            	default :
            	    goto loop4;
                }
            } while (YES); loop4: ;

            do {
                int alt5=2;
                int LA5_0 = [input LA:1];
                if ( (LA5_0>=SimpleCTPTreeParser_BLOCK && LA5_0<=SimpleCTPTreeParser_FOR)||(LA5_0>=SimpleCTPTreeParser_EQEQ && LA5_0<=SimpleCTPTreeParser_PLUS) ) {
                	alt5 = 1;
                }


                switch (alt5) {
            	case 1 :
            	    // SimpleCTP.g:41:27: stat // alt
            	    {
            	    [following addObject:FOLLOW_stat_in_block267];
            	    [self stat];
            	    [following removeLastObject];



            	    }
            	    break;

            	default :
            	    goto loop5;
                }
            } while (YES); loop5: ;


            [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 
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
    return ;
}
// $ANTLR end block


// $ANTLR start stat
// SimpleCTP.g:44:1: stat : ( forStat | expr | block );
- (void) stat
{
    @try {
        // SimpleCTP.g:44:7: ( forStat | expr | block ) //ruleblock
        int alt6=3;
        switch ([input LA:1]) {
        	case SimpleCTPTreeParser_FOR:
        		alt6 = 1;
        		break;
        	case SimpleCTPTreeParser_ID:
        	case SimpleCTPTreeParser_EQ:
        	case SimpleCTPTreeParser_INT:
        	case SimpleCTPTreeParser_EQEQ:
        	case SimpleCTPTreeParser_LT:
        	case SimpleCTPTreeParser_PLUS:
        		alt6 = 2;
        		break;
        	case SimpleCTPTreeParser_BLOCK:
        		alt6 = 3;
        		break;
        default:
         {
            ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:6 state:0 stream:input];
        	@throw nvae;

        	}}
        switch (alt6) {
        	case 1 :
        	    // SimpleCTP.g:44:7: forStat // alt
        	    {
        	    [following addObject:FOLLOW_forStat_in_stat281];
        	    [self forStat];
        	    [following removeLastObject];



        	    }
        	    break;
        	case 2 :
        	    // SimpleCTP.g:45:7: expr // alt
        	    {
        	    [following addObject:FOLLOW_expr_in_stat289];
        	    [self expr];
        	    [following removeLastObject];



        	    }
        	    break;
        	case 3 :
        	    // SimpleCTP.g:46:7: block // alt
        	    {
        	    [following addObject:FOLLOW_block_in_stat297];
        	    [self block];
        	    [following removeLastObject];



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
    return ;
}
// $ANTLR end stat


// $ANTLR start forStat
// SimpleCTP.g:49:1: forStat : ^( FOR expr expr expr block ) ;
- (void) forStat
{
    @try {
        // SimpleCTP.g:50:9: ( ^( FOR expr expr expr block ) ) // ruleBlockSingleAlt
        // SimpleCTP.g:50:9: ^( FOR expr expr expr block ) // alt
        {
        [self match:input tokenType:SimpleCTPTreeParser_FOR follow:FOLLOW_SimpleCTPTreeParser_FOR_in_forStat317]; 

        [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
        [following addObject:FOLLOW_expr_in_forStat319];
        [self expr];
        [following removeLastObject];


        [following addObject:FOLLOW_expr_in_forStat321];
        [self expr];
        [following removeLastObject];


        [following addObject:FOLLOW_expr_in_forStat323];
        [self expr];
        [following removeLastObject];


        [following addObject:FOLLOW_block_in_forStat325];
        [self block];
        [following removeLastObject];



        [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 

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
// $ANTLR end forStat


// $ANTLR start expr
// SimpleCTP.g:53:1: expr : ( ^( EQEQ expr expr ) | ^( LT expr expr ) | ^( PLUS expr expr ) | ^( EQ ID expr ) | atom );
- (void) expr
{
    @try {
        // SimpleCTP.g:53:9: ( ^( EQEQ expr expr ) | ^( LT expr expr ) | ^( PLUS expr expr ) | ^( EQ ID expr ) | atom ) //ruleblock
        int alt7=5;
        switch ([input LA:1]) {
        	case SimpleCTPTreeParser_EQEQ:
        		alt7 = 1;
        		break;
        	case SimpleCTPTreeParser_LT:
        		alt7 = 2;
        		break;
        	case SimpleCTPTreeParser_PLUS:
        		alt7 = 3;
        		break;
        	case SimpleCTPTreeParser_EQ:
        		alt7 = 4;
        		break;
        	case SimpleCTPTreeParser_ID:
        	case SimpleCTPTreeParser_INT:
        		alt7 = 5;
        		break;
        default:
         {
            ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:7 state:0 stream:input];
        	@throw nvae;

        	}}
        switch (alt7) {
        	case 1 :
        	    // SimpleCTP.g:53:9: ^( EQEQ expr expr ) // alt
        	    {
        	    [self match:input tokenType:SimpleCTPTreeParser_EQEQ follow:FOLLOW_SimpleCTPTreeParser_EQEQ_in_expr341]; 

        	    [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
        	    [following addObject:FOLLOW_expr_in_expr343];
        	    [self expr];
        	    [following removeLastObject];


        	    [following addObject:FOLLOW_expr_in_expr345];
        	    [self expr];
        	    [following removeLastObject];



        	    [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 

        	    }
        	    break;
        	case 2 :
        	    // SimpleCTP.g:54:9: ^( LT expr expr ) // alt
        	    {
        	    [self match:input tokenType:SimpleCTPTreeParser_LT follow:FOLLOW_SimpleCTPTreeParser_LT_in_expr357]; 

        	    [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
        	    [following addObject:FOLLOW_expr_in_expr359];
        	    [self expr];
        	    [following removeLastObject];


        	    [following addObject:FOLLOW_expr_in_expr361];
        	    [self expr];
        	    [following removeLastObject];



        	    [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 

        	    }
        	    break;
        	case 3 :
        	    // SimpleCTP.g:55:9: ^( PLUS expr expr ) // alt
        	    {
        	    [self match:input tokenType:SimpleCTPTreeParser_PLUS follow:FOLLOW_SimpleCTPTreeParser_PLUS_in_expr373]; 

        	    [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
        	    [following addObject:FOLLOW_expr_in_expr375];
        	    [self expr];
        	    [following removeLastObject];


        	    [following addObject:FOLLOW_expr_in_expr377];
        	    [self expr];
        	    [following removeLastObject];



        	    [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 

        	    }
        	    break;
        	case 4 :
        	    // SimpleCTP.g:56:9: ^( EQ ID expr ) // alt
        	    {
        	    [self match:input tokenType:SimpleCTPTreeParser_EQ follow:FOLLOW_SimpleCTPTreeParser_EQ_in_expr389]; 

        	    [self match:input tokenType:ANTLRTokenTypeDOWN follow:nil]; 
        	    [self match:input tokenType:SimpleCTPTreeParser_ID follow:FOLLOW_SimpleCTPTreeParser_ID_in_expr391]; 
        	    [following addObject:FOLLOW_expr_in_expr393];
        	    [self expr];
        	    [following removeLastObject];



        	    [self match:input tokenType:ANTLRTokenTypeUP follow:nil]; 

        	    }
        	    break;
        	case 5 :
        	    // SimpleCTP.g:57:9: atom // alt
        	    {
        	    [following addObject:FOLLOW_atom_in_expr404];
        	    [self atom];
        	    [following removeLastObject];



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
    return ;
}
// $ANTLR end expr


// $ANTLR start atom
// SimpleCTP.g:60:1: atom : (ID|INT);
- (void) atom
{
    @try {
        // SimpleCTP.g:61:5: ( (ID|INT)) // ruleBlockSingleAlt
        // SimpleCTP.g:61:7: (ID|INT) // alt
        {
        if ([input LA:1]==SimpleCTPTreeParser_ID||[input LA:1]==SimpleCTPTreeParser_INT) {
        	[input consume];
        	errorRecovery = NO;
        } else {
        	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
        	[self recoverFromMismatchedSet:input exception:mse follow:FOLLOW_set_in_atom421];	@throw mse;
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
    return ;
}
// $ANTLR end atom



@end