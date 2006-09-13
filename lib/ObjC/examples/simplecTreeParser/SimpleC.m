// $ANTLR 3.0b4 simplec.g 2006-09-13 21:39:45

#import "SimpleC.h"

#import <ANTLR/ANTLR.h>


#pragma mark Cyclic DFA
@implementation SimpleCDFA2
const static int SimpleCdfa2_eot[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static int SimpleCdfa2_eof[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static unichar SimpleCdfa2_min[13] =
    {10,10,21,10,0,10,21,23,0,0,10,10,23};
const static unichar SimpleCdfa2_max[13] =
    {16,10,22,24,0,10,25,24,0,0,16,10,24};
const static int SimpleCdfa2_accept[13] =
    {-1,-1,-1,-1,1,-1,-1,-1,3,2,-1,-1,-1};
const static int SimpleCdfa2_special[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static int SimpleCdfa2_transition[] = {};
const static int SimpleCdfa2_transition0[] = {2};
const static int SimpleCdfa2_transition1[] = {4, 3};
const static int SimpleCdfa2_transition2[] = {10, 6};
const static int SimpleCdfa2_transition3[] = {1, -1, -1, -1, 1, 1, 1};
const static int SimpleCdfa2_transition4[] = {7};
const static int SimpleCdfa2_transition5[] = {12};
const static int SimpleCdfa2_transition6[] = {5, -1, -1, -1, 5, 5, 5, -1, 
	-1, -1, -1, -1, -1, -1, 6};
const static int SimpleCdfa2_transition7[] = {11, -1, -1, -1, 11, 11, 11};
const static int SimpleCdfa2_transition8[] = {9, -1, -1, -1, 8};


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
		transition[0] = SimpleCdfa2_transition3;
		transition[1] = SimpleCdfa2_transition0;
		transition[2] = SimpleCdfa2_transition1;
		transition[3] = SimpleCdfa2_transition6;
		transition[4] = SimpleCdfa2_transition;
		transition[5] = SimpleCdfa2_transition4;
		transition[6] = SimpleCdfa2_transition8;
		transition[7] = SimpleCdfa2_transition2;
		transition[8] = SimpleCdfa2_transition;
		transition[9] = SimpleCdfa2_transition;
		transition[10] = SimpleCdfa2_transition7;
		transition[11] = SimpleCdfa2_transition5;
		transition[12] = SimpleCdfa2_transition2;
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
	return @"20:1: declaration : ( variable | functionHeader ';' -> ^( FUNC_DECL functionHeader ) | functionHeader block -> ^( FUNC_DEF functionHeader block ) );";
}

@end



#pragma mark Bitsets
const static unsigned long long FOLLOW_declaration_in_program85_data[] = {0x000000000001C402LL};
static ANTLRBitSet *FOLLOW_declaration_in_program85;
const static unsigned long long FOLLOW_variable_in_declaration105_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_variable_in_declaration105;
const static unsigned long long FOLLOW_functionHeader_in_declaration115_data[] = {0x0000000000200000LL};
static ANTLRBitSet *FOLLOW_functionHeader_in_declaration115;
const static unsigned long long FOLLOW_21_in_declaration117_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_21_in_declaration117;
const static unsigned long long FOLLOW_functionHeader_in_declaration135_data[] = {0x0000000002000000LL};
static ANTLRBitSet *FOLLOW_functionHeader_in_declaration135;
const static unsigned long long FOLLOW_block_in_declaration137_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_block_in_declaration137;
const static unsigned long long FOLLOW_type_in_variable166_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_type_in_variable166;
const static unsigned long long FOLLOW_declarator_in_variable168_data[] = {0x0000000000200000LL};
static ANTLRBitSet *FOLLOW_declarator_in_variable168;
const static unsigned long long FOLLOW_21_in_variable170_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_21_in_variable170;
const static unsigned long long FOLLOW_SimpleC_ID_in_declarator199_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_SimpleC_ID_in_declarator199;
const static unsigned long long FOLLOW_type_in_functionHeader219_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_type_in_functionHeader219;
const static unsigned long long FOLLOW_SimpleC_ID_in_functionHeader221_data[] = {0x0000000000400000LL};
static ANTLRBitSet *FOLLOW_SimpleC_ID_in_functionHeader221;
const static unsigned long long FOLLOW_22_in_functionHeader223_data[] = {0x000000000101C400LL};
static ANTLRBitSet *FOLLOW_22_in_functionHeader223;
const static unsigned long long FOLLOW_formalParameter_in_functionHeader227_data[] = {0x0000000001800000LL};
static ANTLRBitSet *FOLLOW_formalParameter_in_functionHeader227;
const static unsigned long long FOLLOW_23_in_functionHeader231_data[] = {0x000000000001C400LL};
static ANTLRBitSet *FOLLOW_23_in_functionHeader231;
const static unsigned long long FOLLOW_formalParameter_in_functionHeader233_data[] = {0x0000000001800000LL};
static ANTLRBitSet *FOLLOW_formalParameter_in_functionHeader233;
const static unsigned long long FOLLOW_24_in_functionHeader241_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_24_in_functionHeader241;
const static unsigned long long FOLLOW_type_in_formalParameter281_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_type_in_formalParameter281;
const static unsigned long long FOLLOW_declarator_in_formalParameter283_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_declarator_in_formalParameter283;
const static unsigned long long FOLLOW_set_in_type312_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_set_in_type312;
const static unsigned long long FOLLOW_25_in_block376_data[] = {0x000000000661F400LL};
static ANTLRBitSet *FOLLOW_25_in_block376;
const static unsigned long long FOLLOW_variable_in_block390_data[] = {0x000000000661F400LL};
static ANTLRBitSet *FOLLOW_variable_in_block390;
const static unsigned long long FOLLOW_stat_in_block405_data[] = {0x0000000006603400LL};
static ANTLRBitSet *FOLLOW_stat_in_block405;
const static unsigned long long FOLLOW_26_in_block416_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_26_in_block416;
const static unsigned long long FOLLOW_forStat_in_stat449_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_forStat_in_stat449;
const static unsigned long long FOLLOW_expr_in_stat457_data[] = {0x0000000000200000LL};
static ANTLRBitSet *FOLLOW_expr_in_stat457;
const static unsigned long long FOLLOW_21_in_stat459_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_21_in_stat459;
const static unsigned long long FOLLOW_block_in_stat468_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_block_in_stat468;
const static unsigned long long FOLLOW_assignStat_in_stat476_data[] = {0x0000000000200000LL};
static ANTLRBitSet *FOLLOW_assignStat_in_stat476;
const static unsigned long long FOLLOW_21_in_stat478_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_21_in_stat478;
const static unsigned long long FOLLOW_21_in_stat487_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_21_in_stat487;
const static unsigned long long FOLLOW_SimpleC_FOR_in_forStat507_data[] = {0x0000000000400000LL};
static ANTLRBitSet *FOLLOW_SimpleC_FOR_in_forStat507;
const static unsigned long long FOLLOW_22_in_forStat509_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_22_in_forStat509;
const static unsigned long long FOLLOW_assignStat_in_forStat513_data[] = {0x0000000000200000LL};
static ANTLRBitSet *FOLLOW_assignStat_in_forStat513;
const static unsigned long long FOLLOW_21_in_forStat515_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_21_in_forStat515;
const static unsigned long long FOLLOW_expr_in_forStat517_data[] = {0x0000000000200000LL};
static ANTLRBitSet *FOLLOW_expr_in_forStat517;
const static unsigned long long FOLLOW_21_in_forStat519_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_21_in_forStat519;
const static unsigned long long FOLLOW_assignStat_in_forStat523_data[] = {0x0000000001000000LL};
static ANTLRBitSet *FOLLOW_assignStat_in_forStat523;
const static unsigned long long FOLLOW_24_in_forStat525_data[] = {0x0000000002000000LL};
static ANTLRBitSet *FOLLOW_24_in_forStat525;
const static unsigned long long FOLLOW_block_in_forStat527_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_block_in_forStat527;
const static unsigned long long FOLLOW_SimpleC_ID_in_assignStat570_data[] = {0x0000000000000800LL};
static ANTLRBitSet *FOLLOW_SimpleC_ID_in_assignStat570;
const static unsigned long long FOLLOW_SimpleC_EQ_in_assignStat572_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_SimpleC_EQ_in_assignStat572;
const static unsigned long long FOLLOW_expr_in_assignStat574_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_expr_in_assignStat574;
const static unsigned long long FOLLOW_condExpr_in_expr598_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_condExpr_in_expr598;
const static unsigned long long FOLLOW_aexpr_in_condExpr617_data[] = {0x0000000000060002LL};
static ANTLRBitSet *FOLLOW_aexpr_in_condExpr617;
const static unsigned long long FOLLOW_SimpleC_EQEQ_in_condExpr622_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_SimpleC_EQEQ_in_condExpr622;
const static unsigned long long FOLLOW_SimpleC_LT_in_condExpr627_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_SimpleC_LT_in_condExpr627;
const static unsigned long long FOLLOW_aexpr_in_condExpr631_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_aexpr_in_condExpr631;
const static unsigned long long FOLLOW_atom_in_aexpr653_data[] = {0x0000000000080002LL};
static ANTLRBitSet *FOLLOW_atom_in_aexpr653;
const static unsigned long long FOLLOW_SimpleC_PLUS_in_aexpr657_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_SimpleC_PLUS_in_aexpr657;
const static unsigned long long FOLLOW_atom_in_aexpr660_data[] = {0x0000000000080002LL};
static ANTLRBitSet *FOLLOW_atom_in_aexpr660;
const static unsigned long long FOLLOW_SimpleC_ID_in_atom680_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_SimpleC_ID_in_atom680;
const static unsigned long long FOLLOW_SimpleC_INT_in_atom694_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_SimpleC_INT_in_atom694;
const static unsigned long long FOLLOW_22_in_atom708_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_22_in_atom708;
const static unsigned long long FOLLOW_expr_in_atom710_data[] = {0x0000000001000000LL};
static ANTLRBitSet *FOLLOW_expr_in_atom710;
const static unsigned long long FOLLOW_24_in_atom712_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_24_in_atom712;


#pragma mark Scopes

#pragma mark Rule return scopes start
@implementation SimpleCprogram_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCdeclaration_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCvariable_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCdeclarator_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCfunctionHeader_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCformalParameter_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCtype_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCblock_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCstat_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCforStat_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCassignStat_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCexpr_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCcondExpr_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCaexpr_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end
@implementation SimpleCatom_return
- (id) tree
{
	return tree;
}
- (void) setTree:(id)aTree
{
	if (tree != aTree) {
		[aTree retain];
		[tree release];
		tree = aTree;
	}
}
@end


@implementation SimpleC

+ (void) initialize
{
	FOLLOW_declaration_in_program85 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_declaration_in_program85_data count:1];
	FOLLOW_variable_in_declaration105 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_variable_in_declaration105_data count:1];
	FOLLOW_functionHeader_in_declaration115 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_functionHeader_in_declaration115_data count:1];
	FOLLOW_21_in_declaration117 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_21_in_declaration117_data count:1];
	FOLLOW_functionHeader_in_declaration135 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_functionHeader_in_declaration135_data count:1];
	FOLLOW_block_in_declaration137 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_block_in_declaration137_data count:1];
	FOLLOW_type_in_variable166 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_variable166_data count:1];
	FOLLOW_declarator_in_variable168 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_declarator_in_variable168_data count:1];
	FOLLOW_21_in_variable170 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_21_in_variable170_data count:1];
	FOLLOW_SimpleC_ID_in_declarator199 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleC_ID_in_declarator199_data count:1];
	FOLLOW_type_in_functionHeader219 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_functionHeader219_data count:1];
	FOLLOW_SimpleC_ID_in_functionHeader221 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleC_ID_in_functionHeader221_data count:1];
	FOLLOW_22_in_functionHeader223 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_22_in_functionHeader223_data count:1];
	FOLLOW_formalParameter_in_functionHeader227 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_formalParameter_in_functionHeader227_data count:1];
	FOLLOW_23_in_functionHeader231 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_23_in_functionHeader231_data count:1];
	FOLLOW_formalParameter_in_functionHeader233 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_formalParameter_in_functionHeader233_data count:1];
	FOLLOW_24_in_functionHeader241 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_24_in_functionHeader241_data count:1];
	FOLLOW_type_in_formalParameter281 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_formalParameter281_data count:1];
	FOLLOW_declarator_in_formalParameter283 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_declarator_in_formalParameter283_data count:1];
	FOLLOW_set_in_type312 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_set_in_type312_data count:1];
	FOLLOW_25_in_block376 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_25_in_block376_data count:1];
	FOLLOW_variable_in_block390 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_variable_in_block390_data count:1];
	FOLLOW_stat_in_block405 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_stat_in_block405_data count:1];
	FOLLOW_26_in_block416 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_26_in_block416_data count:1];
	FOLLOW_forStat_in_stat449 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_forStat_in_stat449_data count:1];
	FOLLOW_expr_in_stat457 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_stat457_data count:1];
	FOLLOW_21_in_stat459 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_21_in_stat459_data count:1];
	FOLLOW_block_in_stat468 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_block_in_stat468_data count:1];
	FOLLOW_assignStat_in_stat476 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_assignStat_in_stat476_data count:1];
	FOLLOW_21_in_stat478 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_21_in_stat478_data count:1];
	FOLLOW_21_in_stat487 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_21_in_stat487_data count:1];
	FOLLOW_SimpleC_FOR_in_forStat507 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleC_FOR_in_forStat507_data count:1];
	FOLLOW_22_in_forStat509 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_22_in_forStat509_data count:1];
	FOLLOW_assignStat_in_forStat513 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_assignStat_in_forStat513_data count:1];
	FOLLOW_21_in_forStat515 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_21_in_forStat515_data count:1];
	FOLLOW_expr_in_forStat517 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_forStat517_data count:1];
	FOLLOW_21_in_forStat519 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_21_in_forStat519_data count:1];
	FOLLOW_assignStat_in_forStat523 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_assignStat_in_forStat523_data count:1];
	FOLLOW_24_in_forStat525 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_24_in_forStat525_data count:1];
	FOLLOW_block_in_forStat527 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_block_in_forStat527_data count:1];
	FOLLOW_SimpleC_ID_in_assignStat570 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleC_ID_in_assignStat570_data count:1];
	FOLLOW_SimpleC_EQ_in_assignStat572 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleC_EQ_in_assignStat572_data count:1];
	FOLLOW_expr_in_assignStat574 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_assignStat574_data count:1];
	FOLLOW_condExpr_in_expr598 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_condExpr_in_expr598_data count:1];
	FOLLOW_aexpr_in_condExpr617 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_aexpr_in_condExpr617_data count:1];
	FOLLOW_SimpleC_EQEQ_in_condExpr622 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleC_EQEQ_in_condExpr622_data count:1];
	FOLLOW_SimpleC_LT_in_condExpr627 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleC_LT_in_condExpr627_data count:1];
	FOLLOW_aexpr_in_condExpr631 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_aexpr_in_condExpr631_data count:1];
	FOLLOW_atom_in_aexpr653 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_atom_in_aexpr653_data count:1];
	FOLLOW_SimpleC_PLUS_in_aexpr657 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleC_PLUS_in_aexpr657_data count:1];
	FOLLOW_atom_in_aexpr660 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_atom_in_aexpr660_data count:1];
	FOLLOW_SimpleC_ID_in_atom680 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleC_ID_in_atom680_data count:1];
	FOLLOW_SimpleC_INT_in_atom694 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleC_INT_in_atom694_data count:1];
	FOLLOW_22_in_atom708 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_22_in_atom708_data count:1];
	FOLLOW_expr_in_atom710 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_atom710_data count:1];
	FOLLOW_24_in_atom712 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_24_in_atom712_data count:1];

}

- (id) initWithTokenStream:(id<ANTLRTokenStream>)aStream
{
	if ((self = [super initWithTokenStream:aStream])) {
		tokenNames = [[NSArray alloc] initWithObjects:@"<invalid>", @"<EOR>", @"<DOWN>", @"<UP>",     @"VAR_DEF",     @"ARG_DEF",     @"FUNC_HDR",     @"FUNC_DECL",     @"FUNC_DEF",     @"BLOCK",     @"ID",     @"EQ",     @"INT",     @"FOR",     @"INT_TYPE",     @"CHAR",     @"VOID",     @"EQEQ",     @"LT",     @"PLUS",     @"WS",     @"';'",     @"'('",     @"','",     @"')'",     @"'{'",     @"'}'", nil];
		dfa2 = [[SimpleCDFA2 alloc] init];
		[self setAdaptor:[ANTLRCommonTreeAdaptor class]];
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
// simplec.g:16:1: program : ( declaration )+ ;
- (SimpleCprogram_return *) program
{
    SimpleCprogram_return * retval = [[SimpleCprogram_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    SimpleCdeclaration_return * declaration1 = nil;



    @try {
        // simplec.g:17:9: ( ( declaration )+ ) // ruleBlockSingleAlt
        // simplec.g:17:9: ( declaration )+ // alt
        {
        root_0 = (id)[adaptor emptyTree];

        // simplec.g:17:9: ( declaration )+	// positiveClosureBlock
        int cnt1=0;

        do {
            int alt1=2;
            int LA1_0 = [input LA:1];
            if ( LA1_0==SimpleC_ID||(LA1_0>=SimpleC_INT_TYPE && LA1_0<=SimpleC_VOID) ) {
            	alt1 = 1;
            }


            switch (alt1) {
        	case 1 :
        	    // simplec.g:17:9: declaration // alt
        	    {
        	    id root_1 = (id)[adaptor emptyTree];

        	    [following addObject:FOLLOW_declaration_in_program85];
        	    declaration1 = [self declaration];
        	    [following removeLastObject];


        	    [adaptor addChild:[declaration1 tree] toTree:root_1];

        	    [adaptor addChild:root_1 toTree:root_0];

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
        [declaration1 release];
        // rule refs in alts with rewrites
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end program


// $ANTLR start declaration
// simplec.g:20:1: declaration : ( variable | functionHeader ';' -> ^( FUNC_DECL functionHeader ) | functionHeader block -> ^( FUNC_DEF functionHeader block ) );
- (SimpleCdeclaration_return *) declaration
{
    SimpleCdeclaration_return * retval = [[SimpleCdeclaration_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * char_literal4 = nil;
    SimpleCvariable_return * variable2 = nil;

    SimpleCfunctionHeader_return * functionHeader3 = nil;

    SimpleCfunctionHeader_return * functionHeader5 = nil;

    SimpleCblock_return * block6 = nil;

    NSMutableArray *list_functionHeader = [[NSMutableArray alloc] init];
    NSMutableArray *list_block = [[NSMutableArray alloc] init];
    NSMutableArray *list_21 = [[NSMutableArray alloc] init];
    id char_literal4_tree = nil;

    @try {
        // simplec.g:21:9: ( variable | functionHeader ';' -> ^( FUNC_DECL functionHeader ) | functionHeader block -> ^( FUNC_DEF functionHeader block ) ) //ruleblock
        int alt2=3;
        alt2 = [dfa2 predict:input];
        switch (alt2) {
        	case 1 :
        	    // simplec.g:21:9: variable // alt
        	    {
        	    root_0 = (id)[adaptor emptyTree];

        	    [following addObject:FOLLOW_variable_in_declaration105];
        	    variable2 = [self variable];
        	    [following removeLastObject];


        	    [adaptor addChild:[variable2 tree] toTree:root_0];

        	    }
        	    break;
        	case 2 :
        	    // simplec.g:22:9: functionHeader ';' // alt
        	    {
        	    [following addObject:FOLLOW_functionHeader_in_declaration115];
        	    functionHeader3 = [self functionHeader];
        	    [following removeLastObject];


        	    [list_functionHeader addObject:[functionHeader3 tree]];
        	    char_literal4=(ANTLRToken *)[input LT:1];
        	    [self match:input tokenType:21 follow:FOLLOW_21_in_declaration117]; 
        	    [list_21 addObject:char_literal4];


        	    // AST REWRITE
        	    int i_0 = 0;
        	    [retval setTree:root_0];
        	    root_0 = (id)[adaptor emptyTree];
        	    // 22:28: -> ^( FUNC_DECL functionHeader )
        	    {
        	        // simplec.g:22:31: ^( FUNC_DECL functionHeader )
        	        {
        	        id root_1 = (id)[adaptor emptyTree];
        	        root_1 = (id)[adaptor makeNode:[adaptor newTreeWithTokenType:SimpleC_FUNC_DECL text:[tokenNames objectAtIndex:SimpleC_FUNC_DECL]] parentOf:root_1];

        	        [adaptor addChild:(id<ANTLRTree>)[list_functionHeader objectAtIndex:i_0] toTree:root_1];

        	        [adaptor addChild:root_1 toTree:root_0];
        	        }

        	    }



        	    }
        	    break;
        	case 3 :
        	    // simplec.g:23:9: functionHeader block // alt
        	    {
        	    [following addObject:FOLLOW_functionHeader_in_declaration135];
        	    functionHeader5 = [self functionHeader];
        	    [following removeLastObject];


        	    [list_functionHeader addObject:[functionHeader5 tree]];
        	    [following addObject:FOLLOW_block_in_declaration137];
        	    block6 = [self block];
        	    [following removeLastObject];


        	    [list_block addObject:[block6 tree]];

        	    // AST REWRITE
        	    int i_0 = 0;
        	    [retval setTree:root_0];
        	    root_0 = (id)[adaptor emptyTree];
        	    // 23:30: -> ^( FUNC_DEF functionHeader block )
        	    {
        	        // simplec.g:23:33: ^( FUNC_DEF functionHeader block )
        	        {
        	        id root_1 = (id)[adaptor emptyTree];
        	        root_1 = (id)[adaptor makeNode:[adaptor newTreeWithTokenType:SimpleC_FUNC_DEF text:[tokenNames objectAtIndex:SimpleC_FUNC_DEF]] parentOf:root_1];

        	        [adaptor addChild:(id<ANTLRTree>)[list_functionHeader objectAtIndex:i_0] toTree:root_1];
        	        [adaptor addChild:(id<ANTLRTree>)[list_block objectAtIndex:i_0] toTree:root_1];

        	        [adaptor addChild:root_1 toTree:root_0];
        	        }

        	    }



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
        [char_literal4 release];
        // token+rule list labels
        // rule labels
        [variable2 release];
        [functionHeader3 release];
        [functionHeader5 release];
        [block6 release];
        // rule refs in alts with rewrites
        [list_functionHeader release];
        [list_block release];
        [list_21 release];
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end declaration


// $ANTLR start variable
// simplec.g:26:1: variable : type declarator ';' -> ^( VAR_DEF type declarator ) ;
- (SimpleCvariable_return *) variable
{
    SimpleCvariable_return * retval = [[SimpleCvariable_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * char_literal9 = nil;
    SimpleCtype_return * type7 = nil;

    SimpleCdeclarator_return * declarator8 = nil;

    NSMutableArray *list_type = [[NSMutableArray alloc] init];
    NSMutableArray *list_declarator = [[NSMutableArray alloc] init];
    NSMutableArray *list_21 = [[NSMutableArray alloc] init];
    id char_literal9_tree = nil;

    @try {
        // simplec.g:27:9: ( type declarator ';' -> ^( VAR_DEF type declarator ) ) // ruleBlockSingleAlt
        // simplec.g:27:9: type declarator ';' // alt
        {
        [following addObject:FOLLOW_type_in_variable166];
        type7 = [self type];
        [following removeLastObject];


        [list_type addObject:[type7 tree]];
        [following addObject:FOLLOW_declarator_in_variable168];
        declarator8 = [self declarator];
        [following removeLastObject];


        [list_declarator addObject:[declarator8 tree]];
        char_literal9=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:21 follow:FOLLOW_21_in_variable170]; 
        [list_21 addObject:char_literal9];


        // AST REWRITE
        int i_0 = 0;
        [retval setTree:root_0];
        root_0 = (id)[adaptor emptyTree];
        // 27:29: -> ^( VAR_DEF type declarator )
        {
            // simplec.g:27:32: ^( VAR_DEF type declarator )
            {
            id root_1 = (id)[adaptor emptyTree];
            root_1 = (id)[adaptor makeNode:[adaptor newTreeWithTokenType:SimpleC_VAR_DEF text:[tokenNames objectAtIndex:SimpleC_VAR_DEF]] parentOf:root_1];

            [adaptor addChild:(id<ANTLRTree>)[list_type objectAtIndex:i_0] toTree:root_1];
            [adaptor addChild:(id<ANTLRTree>)[list_declarator objectAtIndex:i_0] toTree:root_1];

            [adaptor addChild:root_1 toTree:root_0];
            }

        }



        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        // token labels
        [char_literal9 release];
        // token+rule list labels
        // rule labels
        [type7 release];
        [declarator8 release];
        // rule refs in alts with rewrites
        [list_type release];
        [list_declarator release];
        [list_21 release];
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end variable


// $ANTLR start declarator
// simplec.g:30:1: declarator : ID ;
- (SimpleCdeclarator_return *) declarator
{
    SimpleCdeclarator_return * retval = [[SimpleCdeclarator_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * ID10 = nil;

    id ID10_tree = nil;

    @try {
        // simplec.g:31:9: ( ID ) // ruleBlockSingleAlt
        // simplec.g:31:9: ID // alt
        {
        root_0 = (id)[adaptor emptyTree];

        ID10=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:SimpleC_ID follow:FOLLOW_SimpleC_ID_in_declarator199]; 
        ID10_tree = (id)[adaptor newTreeWithToken:ID10];
        [adaptor addChild:ID10_tree toTree:root_0];


        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        // token labels
        [ID10 release];
        // token+rule list labels
        // rule labels
        // rule refs in alts with rewrites
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end declarator


// $ANTLR start functionHeader
// simplec.g:34:1: functionHeader : type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' -> ^( FUNC_HDR type ID ( formalParameter )+ ) ;
- (SimpleCfunctionHeader_return *) functionHeader
{
    SimpleCfunctionHeader_return * retval = [[SimpleCfunctionHeader_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * ID12 = nil;
    ANTLRToken * char_literal13 = nil;
    ANTLRToken * char_literal15 = nil;
    ANTLRToken * char_literal17 = nil;
    SimpleCtype_return * type11 = nil;

    SimpleCformalParameter_return * formalParameter14 = nil;

    SimpleCformalParameter_return * formalParameter16 = nil;

    NSMutableArray *list_formalParameter = [[NSMutableArray alloc] init];
    NSMutableArray *list_type = [[NSMutableArray alloc] init];
    NSMutableArray *list_24 = [[NSMutableArray alloc] init];
    NSMutableArray *list_SimpleC_ID = [[NSMutableArray alloc] init];
    NSMutableArray *list_22 = [[NSMutableArray alloc] init];
    NSMutableArray *list_23 = [[NSMutableArray alloc] init];
    id ID12_tree = nil;
    id char_literal13_tree = nil;
    id char_literal15_tree = nil;
    id char_literal17_tree = nil;

    @try {
        // simplec.g:35:9: ( type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' -> ^( FUNC_HDR type ID ( formalParameter )+ ) ) // ruleBlockSingleAlt
        // simplec.g:35:9: type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' // alt
        {
        [following addObject:FOLLOW_type_in_functionHeader219];
        type11 = [self type];
        [following removeLastObject];


        [list_type addObject:[type11 tree]];
        ID12=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:SimpleC_ID follow:FOLLOW_SimpleC_ID_in_functionHeader221]; 
        [list_SimpleC_ID addObject:ID12];

        char_literal13=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:22 follow:FOLLOW_22_in_functionHeader223]; 
        [list_22 addObject:char_literal13];

        // simplec.g:35:21: ( formalParameter ( ',' formalParameter )* )? // block
        int alt4=2;
        int LA4_0 = [input LA:1];
        if ( LA4_0==SimpleC_ID||(LA4_0>=SimpleC_INT_TYPE && LA4_0<=SimpleC_VOID) ) {
        	alt4 = 1;
        }
        switch (alt4) {
        	case 1 :
        	    // simplec.g:35:23: formalParameter ( ',' formalParameter )* // alt
        	    {
        	    [following addObject:FOLLOW_formalParameter_in_functionHeader227];
        	    formalParameter14 = [self formalParameter];
        	    [following removeLastObject];


        	    [list_formalParameter addObject:[formalParameter14 tree]];
        	    do {
        	        int alt3=2;
        	        int LA3_0 = [input LA:1];
        	        if ( LA3_0==23 ) {
        	        	alt3 = 1;
        	        }


        	        switch (alt3) {
        	    	case 1 :
        	    	    // simplec.g:35:41: ',' formalParameter // alt
        	    	    {
        	    	    char_literal15=(ANTLRToken *)[input LT:1];
        	    	    [self match:input tokenType:23 follow:FOLLOW_23_in_functionHeader231]; 
        	    	    [list_23 addObject:char_literal15];

        	    	    [following addObject:FOLLOW_formalParameter_in_functionHeader233];
        	    	    formalParameter16 = [self formalParameter];
        	    	    [following removeLastObject];


        	    	    [list_formalParameter addObject:[formalParameter16 tree]];

        	    	    }
        	    	    break;

        	    	default :
        	    	    goto loop3;
        	        }
        	    } while (YES); loop3: ;


        	    }
        	    break;

        }

        char_literal17=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:24 follow:FOLLOW_24_in_functionHeader241]; 
        [list_24 addObject:char_literal17];


        // AST REWRITE
        int i_0 = 0;
        [retval setTree:root_0];
        root_0 = (id)[adaptor emptyTree];
        // 36:9: -> ^( FUNC_HDR type ID ( formalParameter )+ )
        {
            // simplec.g:36:12: ^( FUNC_HDR type ID ( formalParameter )+ )
            {
            id root_1 = (id)[adaptor emptyTree];
            root_1 = (id)[adaptor makeNode:[adaptor newTreeWithTokenType:SimpleC_FUNC_HDR text:[tokenNames objectAtIndex:SimpleC_FUNC_HDR]] parentOf:root_1];

            [adaptor addChild:(id<ANTLRTree>)[list_type objectAtIndex:i_0] toTree:root_1];
            [adaptor addTokenAsChild:(ANTLRToken *)[list_SimpleC_ID objectAtIndex:i_0] toTree:root_1];
            // simplec.g:36:31: ( formalParameter )+
            {
            int n_1 = list_formalParameter == nil ? 0 : [list_formalParameter count];
             


            if ( n_1==0 ) @throw [NSException exceptionWithName:@"ANTLRTreeRewriteException" reason:@"Must have more than one element for (...)+ loops" userInfo:nil];
            for (int i_1=0; i_1<n_1; i_1++) {
                [adaptor addChild:(id<ANTLRTree>)[list_formalParameter objectAtIndex:i_1] toTree:root_1];

            }
            }

            [adaptor addChild:root_1 toTree:root_0];
            }

        }



        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        // token labels
        [ID12 release];
        [char_literal13 release];
        [char_literal15 release];
        [char_literal17 release];
        // token+rule list labels
        // rule labels
        [type11 release];
        [formalParameter14 release];
        [formalParameter16 release];
        // rule refs in alts with rewrites
        [list_formalParameter release];
        [list_type release];
        [list_24 release];
        [list_SimpleC_ID release];
        [list_22 release];
        [list_23 release];
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end functionHeader


// $ANTLR start formalParameter
// simplec.g:39:1: formalParameter : type declarator -> ^( ARG_DEF type declarator ) ;
- (SimpleCformalParameter_return *) formalParameter
{
    SimpleCformalParameter_return * retval = [[SimpleCformalParameter_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    SimpleCtype_return * type18 = nil;

    SimpleCdeclarator_return * declarator19 = nil;

    NSMutableArray *list_type = [[NSMutableArray alloc] init];
    NSMutableArray *list_declarator = [[NSMutableArray alloc] init];

    @try {
        // simplec.g:40:9: ( type declarator -> ^( ARG_DEF type declarator ) ) // ruleBlockSingleAlt
        // simplec.g:40:9: type declarator // alt
        {
        [following addObject:FOLLOW_type_in_formalParameter281];
        type18 = [self type];
        [following removeLastObject];


        [list_type addObject:[type18 tree]];
        [following addObject:FOLLOW_declarator_in_formalParameter283];
        declarator19 = [self declarator];
        [following removeLastObject];


        [list_declarator addObject:[declarator19 tree]];

        // AST REWRITE
        int i_0 = 0;
        [retval setTree:root_0];
        root_0 = (id)[adaptor emptyTree];
        // 40:25: -> ^( ARG_DEF type declarator )
        {
            // simplec.g:40:28: ^( ARG_DEF type declarator )
            {
            id root_1 = (id)[adaptor emptyTree];
            root_1 = (id)[adaptor makeNode:[adaptor newTreeWithTokenType:SimpleC_ARG_DEF text:[tokenNames objectAtIndex:SimpleC_ARG_DEF]] parentOf:root_1];

            [adaptor addChild:(id<ANTLRTree>)[list_type objectAtIndex:i_0] toTree:root_1];
            [adaptor addChild:(id<ANTLRTree>)[list_declarator objectAtIndex:i_0] toTree:root_1];

            [adaptor addChild:root_1 toTree:root_0];
            }

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
        [type18 release];
        [declarator19 release];
        // rule refs in alts with rewrites
        [list_type release];
        [list_declarator release];
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end formalParameter


// $ANTLR start type
// simplec.g:43:1: type : ('int'|'char'|'void'|ID);
- (SimpleCtype_return *) type
{
    SimpleCtype_return * retval = [[SimpleCtype_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * set20 = nil;

    id set20_tree = nil;

    @try {
        // simplec.g:44:5: ( ('int'|'char'|'void'|ID)) // ruleBlockSingleAlt
        // simplec.g:44:9: ('int'|'char'|'void'|ID) // alt
        {
        root_0 = (id)[adaptor emptyTree];

        set20 = (ANTLRToken *)[input LT:1];
        if ([input LA:1]==SimpleC_ID||([input LA:1]>=SimpleC_INT_TYPE && [input LA:1]<=SimpleC_VOID)) {
        	[adaptor addChild:[adaptor newTreeWithToken:set20] toTree:root_0];
        	[input consume];
        	errorRecovery = NO;
        } else {
        	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
        	[self recoverFromMismatchedSet:input exception:mse follow:FOLLOW_set_in_type312];	@throw mse;
        }


        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        // token labels
        [set20 release];
        // token+rule list labels
        // rule labels
        // rule refs in alts with rewrites
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end type


// $ANTLR start block
// simplec.g:50:1: block : lc= '{' ( variable )* ( stat )* '}' -> ^( BLOCK[$lc,"BLOCK"] ( variable )* ( stat )* ) ;
- (SimpleCblock_return *) block
{
    SimpleCblock_return * retval = [[SimpleCblock_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * lc = nil;
    ANTLRToken * char_literal23 = nil;
    SimpleCvariable_return * variable21 = nil;

    SimpleCstat_return * stat22 = nil;

    NSMutableArray *list_stat = [[NSMutableArray alloc] init];
    NSMutableArray *list_variable = [[NSMutableArray alloc] init];
    NSMutableArray *list_26 = [[NSMutableArray alloc] init];
    NSMutableArray *list_25 = [[NSMutableArray alloc] init];
    id lc_tree = nil;
    id char_literal23_tree = nil;

    @try {
        // simplec.g:51:9: (lc= '{' ( variable )* ( stat )* '}' -> ^( BLOCK[$lc,\"BLOCK\"] ( variable )* ( stat )* ) ) // ruleBlockSingleAlt
        // simplec.g:51:9: lc= '{' ( variable )* ( stat )* '}' // alt
        {
        lc=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:25 follow:FOLLOW_25_in_block376]; 
        [list_25 addObject:lc];

        do {
            int alt5=2;
            int LA5_0 = [input LA:1];
            if ( LA5_0==SimpleC_ID ) {
            	int LA5_2 = [input LA:2];
            	if ( LA5_2==SimpleC_ID ) {
            		alt5 = 1;
            	}


            }
            else if ( (LA5_0>=SimpleC_INT_TYPE && LA5_0<=SimpleC_VOID) ) {
            	alt5 = 1;
            }


            switch (alt5) {
        	case 1 :
        	    // simplec.g:52:13: variable // alt
        	    {
        	    [following addObject:FOLLOW_variable_in_block390];
        	    variable21 = [self variable];
        	    [following removeLastObject];


        	    [list_variable addObject:[variable21 tree]];

        	    }
        	    break;

        	default :
        	    goto loop5;
            }
        } while (YES); loop5: ;

        do {
            int alt6=2;
            int LA6_0 = [input LA:1];
            if ( LA6_0==SimpleC_ID||(LA6_0>=SimpleC_INT && LA6_0<=SimpleC_FOR)||(LA6_0>=21 && LA6_0<=22)||LA6_0==25 ) {
            	alt6 = 1;
            }


            switch (alt6) {
        	case 1 :
        	    // simplec.g:53:13: stat // alt
        	    {
        	    [following addObject:FOLLOW_stat_in_block405];
        	    stat22 = [self stat];
        	    [following removeLastObject];


        	    [list_stat addObject:[stat22 tree]];

        	    }
        	    break;

        	default :
        	    goto loop6;
            }
        } while (YES); loop6: ;

        char_literal23=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:26 follow:FOLLOW_26_in_block416]; 
        [list_26 addObject:char_literal23];


        // AST REWRITE
        int i_0 = 0;
        [retval setTree:root_0];
        root_0 = (id)[adaptor emptyTree];
        // 55:9: -> ^( BLOCK[$lc,\"BLOCK\"] ( variable )* ( stat )* )
        {
            // simplec.g:55:12: ^( BLOCK[$lc,\"BLOCK\"] ( variable )* ( stat )* )
            {
            id root_1 = (id)[adaptor emptyTree];
            root_1 = (id)[adaptor makeNode:[adaptor newTreeWithTokenType:SimpleC_BLOCK text:[lc text]] parentOf:root_1];

            // simplec.g:55:33: ( variable )*
            {
            int n_1 = list_variable == nil ? 0 : [list_variable count];
             


            for (int i_1=0; i_1<n_1; i_1++) {
                [adaptor addChild:(id<ANTLRTree>)[list_variable objectAtIndex:i_1] toTree:root_1];

            }
            }
            // simplec.g:55:43: ( stat )*
            {
            int n_1 = list_stat == nil ? 0 : [list_stat count];
             


            for (int i_1=0; i_1<n_1; i_1++) {
                [adaptor addChild:(id<ANTLRTree>)[list_stat objectAtIndex:i_1] toTree:root_1];

            }
            }

            [adaptor addChild:root_1 toTree:root_0];
            }

        }



        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        // token labels
        [lc release];
        [char_literal23 release];
        // token+rule list labels
        // rule labels
        [variable21 release];
        [stat22 release];
        // rule refs in alts with rewrites
        [list_stat release];
        [list_variable release];
        [list_26 release];
        [list_25 release];
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end block


// $ANTLR start stat
// simplec.g:58:1: stat : ( forStat | expr ';'! | block | assignStat ';'! | ';'! );
- (SimpleCstat_return *) stat
{
    SimpleCstat_return * retval = [[SimpleCstat_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * char_literal26 = nil;
    ANTLRToken * char_literal29 = nil;
    ANTLRToken * char_literal30 = nil;
    SimpleCforStat_return * forStat24 = nil;

    SimpleCexpr_return * expr25 = nil;

    SimpleCblock_return * block27 = nil;

    SimpleCassignStat_return * assignStat28 = nil;


    id char_literal26_tree = nil;
    id char_literal29_tree = nil;
    id char_literal30_tree = nil;

    @try {
        // simplec.g:58:7: ( forStat | expr ';'! | block | assignStat ';'! | ';'! ) //ruleblock
        int alt7=5;
        switch ([input LA:1]) {
        	case SimpleC_FOR:
        		alt7 = 1;
        		break;
        	case SimpleC_ID:
        		{
        			int LA7_2 = [input LA:2];
        			if ( LA7_2==SimpleC_EQ ) {
        				alt7 = 4;
        			}
        			else if ( (LA7_2>=SimpleC_EQEQ && LA7_2<=SimpleC_PLUS)||LA7_2==21 ) {
        				alt7 = 2;
        			}
        		else {
        		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:7 state:2 stream:input];
        			@throw nvae;
        			}
        		}
        		break;
        	case SimpleC_INT:
        	case 22:
        		alt7 = 2;
        		break;
        	case 25:
        		alt7 = 3;
        		break;
        	case 21:
        		alt7 = 5;
        		break;
        default:
         {
            ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:7 state:0 stream:input];
        	@throw nvae;

        	}}
        switch (alt7) {
        	case 1 :
        	    // simplec.g:58:7: forStat // alt
        	    {
        	    root_0 = (id)[adaptor emptyTree];

        	    [following addObject:FOLLOW_forStat_in_stat449];
        	    forStat24 = [self forStat];
        	    [following removeLastObject];


        	    [adaptor addChild:[forStat24 tree] toTree:root_0];

        	    }
        	    break;
        	case 2 :
        	    // simplec.g:59:7: expr ';'! // alt
        	    {
        	    root_0 = (id)[adaptor emptyTree];

        	    [following addObject:FOLLOW_expr_in_stat457];
        	    expr25 = [self expr];
        	    [following removeLastObject];


        	    [adaptor addChild:[expr25 tree] toTree:root_0];
        	    char_literal26=(ANTLRToken *)[input LT:1];
        	    [self match:input tokenType:21 follow:FOLLOW_21_in_stat459]; 

        	    }
        	    break;
        	case 3 :
        	    // simplec.g:60:7: block // alt
        	    {
        	    root_0 = (id)[adaptor emptyTree];

        	    [following addObject:FOLLOW_block_in_stat468];
        	    block27 = [self block];
        	    [following removeLastObject];


        	    [adaptor addChild:[block27 tree] toTree:root_0];

        	    }
        	    break;
        	case 4 :
        	    // simplec.g:61:7: assignStat ';'! // alt
        	    {
        	    root_0 = (id)[adaptor emptyTree];

        	    [following addObject:FOLLOW_assignStat_in_stat476];
        	    assignStat28 = [self assignStat];
        	    [following removeLastObject];


        	    [adaptor addChild:[assignStat28 tree] toTree:root_0];
        	    char_literal29=(ANTLRToken *)[input LT:1];
        	    [self match:input tokenType:21 follow:FOLLOW_21_in_stat478]; 

        	    }
        	    break;
        	case 5 :
        	    // simplec.g:62:7: ';'! // alt
        	    {
        	    root_0 = (id)[adaptor emptyTree];

        	    char_literal30=(ANTLRToken *)[input LT:1];
        	    [self match:input tokenType:21 follow:FOLLOW_21_in_stat487]; 

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
        [char_literal26 release];
        [char_literal29 release];
        [char_literal30 release];
        // token+rule list labels
        // rule labels
        [forStat24 release];
        [expr25 release];
        [block27 release];
        [assignStat28 release];
        // rule refs in alts with rewrites
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end stat


// $ANTLR start forStat
// simplec.g:65:1: forStat : 'for' '(' start= assignStat ';' expr ';' next= assignStat ')' block -> ^( 'for' $start expr $next block ) ;
- (SimpleCforStat_return *) forStat
{
    SimpleCforStat_return * retval = [[SimpleCforStat_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * string_literal31 = nil;
    ANTLRToken * char_literal32 = nil;
    ANTLRToken * char_literal33 = nil;
    ANTLRToken * char_literal35 = nil;
    ANTLRToken * char_literal36 = nil;
    SimpleCassignStat_return * start = nil;

    SimpleCassignStat_return * next = nil;

    SimpleCexpr_return * expr34 = nil;

    SimpleCblock_return * block37 = nil;

    NSMutableArray *list_expr = [[NSMutableArray alloc] init];
    NSMutableArray *list_assignStat = [[NSMutableArray alloc] init];
    NSMutableArray *list_block = [[NSMutableArray alloc] init];
    NSMutableArray *list_21 = [[NSMutableArray alloc] init];
    NSMutableArray *list_24 = [[NSMutableArray alloc] init];
    NSMutableArray *list_22 = [[NSMutableArray alloc] init];
    NSMutableArray *list_SimpleC_FOR = [[NSMutableArray alloc] init];
    id string_literal31_tree = nil;
    id char_literal32_tree = nil;
    id char_literal33_tree = nil;
    id char_literal35_tree = nil;
    id char_literal36_tree = nil;

    @try {
        // simplec.g:66:9: ( 'for' '(' start= assignStat ';' expr ';' next= assignStat ')' block -> ^( 'for' $start expr $next block ) ) // ruleBlockSingleAlt
        // simplec.g:66:9: 'for' '(' start= assignStat ';' expr ';' next= assignStat ')' block // alt
        {
        string_literal31=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:SimpleC_FOR follow:FOLLOW_SimpleC_FOR_in_forStat507]; 
        [list_SimpleC_FOR addObject:string_literal31];

        char_literal32=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:22 follow:FOLLOW_22_in_forStat509]; 
        [list_22 addObject:char_literal32];

        [following addObject:FOLLOW_assignStat_in_forStat513];
        start = [self assignStat];
        [following removeLastObject];


        [list_assignStat addObject:[start tree]];
        char_literal33=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:21 follow:FOLLOW_21_in_forStat515]; 
        [list_21 addObject:char_literal33];

        [following addObject:FOLLOW_expr_in_forStat517];
        expr34 = [self expr];
        [following removeLastObject];


        [list_expr addObject:[expr34 tree]];
        char_literal35=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:21 follow:FOLLOW_21_in_forStat519]; 
        [list_21 addObject:char_literal35];

        [following addObject:FOLLOW_assignStat_in_forStat523];
        next = [self assignStat];
        [following removeLastObject];


        [list_assignStat addObject:[next tree]];
        char_literal36=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:24 follow:FOLLOW_24_in_forStat525]; 
        [list_24 addObject:char_literal36];

        [following addObject:FOLLOW_block_in_forStat527];
        block37 = [self block];
        [following removeLastObject];


        [list_block addObject:[block37 tree]];

        // AST REWRITE
        int i_0 = 0;
        [retval setTree:root_0];
        root_0 = (id)[adaptor emptyTree];
        // 67:9: -> ^( 'for' $start expr $next block )
        {
            // simplec.g:67:12: ^( 'for' $start expr $next block )
            {
            id root_1 = (id)[adaptor emptyTree];
            root_1 = (id)[adaptor makeToken:(ANTLRToken *)[list_SimpleC_FOR objectAtIndex:i_0] parentOf:root_1];

            [adaptor addChild:[start tree] toTree:root_1];
            [adaptor addChild:(id<ANTLRTree>)[list_expr objectAtIndex:i_0] toTree:root_1];
            [adaptor addChild:[next tree] toTree:root_1];
            [adaptor addChild:(id<ANTLRTree>)[list_block objectAtIndex:i_0] toTree:root_1];

            [adaptor addChild:root_1 toTree:root_0];
            }

        }



        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        // token labels
        [string_literal31 release];
        [char_literal32 release];
        [char_literal33 release];
        [char_literal35 release];
        [char_literal36 release];
        // token+rule list labels
        // rule labels
        [start release];
        [next release];
        [expr34 release];
        [block37 release];
        // rule refs in alts with rewrites
        [list_expr release];
        [list_assignStat release];
        [list_block release];
        [list_21 release];
        [list_24 release];
        [list_22 release];
        [list_SimpleC_FOR release];
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end forStat


// $ANTLR start assignStat
// simplec.g:70:1: assignStat : ID EQ expr -> ^( EQ ID expr ) ;
- (SimpleCassignStat_return *) assignStat
{
    SimpleCassignStat_return * retval = [[SimpleCassignStat_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * ID38 = nil;
    ANTLRToken * EQ39 = nil;
    SimpleCexpr_return * expr40 = nil;

    NSMutableArray *list_expr = [[NSMutableArray alloc] init];
    NSMutableArray *list_SimpleC_ID = [[NSMutableArray alloc] init];
    NSMutableArray *list_SimpleC_EQ = [[NSMutableArray alloc] init];
    id ID38_tree = nil;
    id EQ39_tree = nil;

    @try {
        // simplec.g:71:9: ( ID EQ expr -> ^( EQ ID expr ) ) // ruleBlockSingleAlt
        // simplec.g:71:9: ID EQ expr // alt
        {
        ID38=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:SimpleC_ID follow:FOLLOW_SimpleC_ID_in_assignStat570]; 
        [list_SimpleC_ID addObject:ID38];

        EQ39=(ANTLRToken *)[input LT:1];
        [self match:input tokenType:SimpleC_EQ follow:FOLLOW_SimpleC_EQ_in_assignStat572]; 
        [list_SimpleC_EQ addObject:EQ39];

        [following addObject:FOLLOW_expr_in_assignStat574];
        expr40 = [self expr];
        [following removeLastObject];


        [list_expr addObject:[expr40 tree]];

        // AST REWRITE
        int i_0 = 0;
        [retval setTree:root_0];
        root_0 = (id)[adaptor emptyTree];
        // 71:20: -> ^( EQ ID expr )
        {
            // simplec.g:71:23: ^( EQ ID expr )
            {
            id root_1 = (id)[adaptor emptyTree];
            root_1 = (id)[adaptor makeToken:(ANTLRToken *)[list_SimpleC_EQ objectAtIndex:i_0] parentOf:root_1];

            [adaptor addTokenAsChild:(ANTLRToken *)[list_SimpleC_ID objectAtIndex:i_0] toTree:root_1];
            [adaptor addChild:(id<ANTLRTree>)[list_expr objectAtIndex:i_0] toTree:root_1];

            [adaptor addChild:root_1 toTree:root_0];
            }

        }



        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        // token labels
        [ID38 release];
        [EQ39 release];
        // token+rule list labels
        // rule labels
        [expr40 release];
        // rule refs in alts with rewrites
        [list_expr release];
        [list_SimpleC_ID release];
        [list_SimpleC_EQ release];
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end assignStat


// $ANTLR start expr
// simplec.g:74:1: expr : condExpr ;
- (SimpleCexpr_return *) expr
{
    SimpleCexpr_return * retval = [[SimpleCexpr_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    SimpleCcondExpr_return * condExpr41 = nil;



    @try {
        // simplec.g:74:9: ( condExpr ) // ruleBlockSingleAlt
        // simplec.g:74:9: condExpr // alt
        {
        root_0 = (id)[adaptor emptyTree];

        [following addObject:FOLLOW_condExpr_in_expr598];
        condExpr41 = [self condExpr];
        [following removeLastObject];


        [adaptor addChild:[condExpr41 tree] toTree:root_0];

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
        [condExpr41 release];
        // rule refs in alts with rewrites
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end expr


// $ANTLR start condExpr
// simplec.g:77:1: condExpr : aexpr ( ( '=='^^ | '<'^^ ) aexpr )? ;
- (SimpleCcondExpr_return *) condExpr
{
    SimpleCcondExpr_return * retval = [[SimpleCcondExpr_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * string_literal43 = nil;
    ANTLRToken * char_literal44 = nil;
    SimpleCaexpr_return * aexpr42 = nil;

    SimpleCaexpr_return * aexpr45 = nil;


    id string_literal43_tree = nil;
    id char_literal44_tree = nil;

    @try {
        // simplec.g:78:9: ( aexpr ( ( '=='^^ | '<'^^ ) aexpr )? ) // ruleBlockSingleAlt
        // simplec.g:78:9: aexpr ( ( '=='^^ | '<'^^ ) aexpr )? // alt
        {
        root_0 = (id)[adaptor emptyTree];

        [following addObject:FOLLOW_aexpr_in_condExpr617];
        aexpr42 = [self aexpr];
        [following removeLastObject];


        [adaptor addChild:[aexpr42 tree] toTree:root_0];
        // simplec.g:78:15: ( ( '=='^^ | '<'^^ ) aexpr )? // block
        int alt9=2;
        int LA9_0 = [input LA:1];
        if ( (LA9_0>=SimpleC_EQEQ && LA9_0<=SimpleC_LT) ) {
        	alt9 = 1;
        }
        switch (alt9) {
        	case 1 :
        	    // simplec.g:78:17: ( '=='^^ | '<'^^ ) aexpr // alt
        	    {
        	    id root_1 = (id)[adaptor emptyTree];

        	    // simplec.g:78:17: ( '=='^^ | '<'^^ ) // block
        	    int alt8=2;
        	    {
        	    	int LA8_0 = [input LA:1];
        	    	if ( LA8_0==SimpleC_EQEQ ) {
        	    		alt8 = 1;
        	    	}
        	    	else if ( LA8_0==SimpleC_LT ) {
        	    		alt8 = 2;
        	    	}
        	    else {
        	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:8 state:0 stream:input];
        	    	@throw nvae;
        	    	}
        	    }
        	    switch (alt8) {
        	    	case 1 :
        	    	    // simplec.g:78:18: '=='^^ // alt
        	    	    {
        	    	    id root_2 = (id)[adaptor emptyTree];

        	    	    string_literal43=(ANTLRToken *)[input LT:1];
        	    	    [self match:input tokenType:SimpleC_EQEQ follow:FOLLOW_SimpleC_EQEQ_in_condExpr622]; 
        	    	    string_literal43_tree = (id)[adaptor newTreeWithToken:string_literal43];
        	    	    root_0 = (id)[adaptor makeNode:string_literal43_tree parentOf:root_0];


        	    	    [adaptor addChild:root_2 toTree:root_1];

        	    	    }
        	    	    break;
        	    	case 2 :
        	    	    // simplec.g:78:27: '<'^^ // alt
        	    	    {
        	    	    id root_2 = (id)[adaptor emptyTree];

        	    	    char_literal44=(ANTLRToken *)[input LT:1];
        	    	    [self match:input tokenType:SimpleC_LT follow:FOLLOW_SimpleC_LT_in_condExpr627]; 
        	    	    char_literal44_tree = (id)[adaptor newTreeWithToken:char_literal44];
        	    	    root_0 = (id)[adaptor makeNode:char_literal44_tree parentOf:root_0];


        	    	    [adaptor addChild:root_2 toTree:root_1];

        	    	    }
        	    	    break;

        	    }

        	    [following addObject:FOLLOW_aexpr_in_condExpr631];
        	    aexpr45 = [self aexpr];
        	    [following removeLastObject];


        	    [adaptor addChild:[aexpr45 tree] toTree:root_1];

        	    [adaptor addChild:root_1 toTree:root_0];

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
        // token labels
        [string_literal43 release];
        [char_literal44 release];
        // token+rule list labels
        // rule labels
        [aexpr42 release];
        [aexpr45 release];
        // rule refs in alts with rewrites
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end condExpr


// $ANTLR start aexpr
// simplec.g:81:1: aexpr : atom ( '+'^^ atom )* ;
- (SimpleCaexpr_return *) aexpr
{
    SimpleCaexpr_return * retval = [[SimpleCaexpr_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * char_literal47 = nil;
    SimpleCatom_return * atom46 = nil;

    SimpleCatom_return * atom48 = nil;


    id char_literal47_tree = nil;

    @try {
        // simplec.g:82:9: ( atom ( '+'^^ atom )* ) // ruleBlockSingleAlt
        // simplec.g:82:9: atom ( '+'^^ atom )* // alt
        {
        root_0 = (id)[adaptor emptyTree];

        [following addObject:FOLLOW_atom_in_aexpr653];
        atom46 = [self atom];
        [following removeLastObject];


        [adaptor addChild:[atom46 tree] toTree:root_0];
        do {
            int alt10=2;
            int LA10_0 = [input LA:1];
            if ( LA10_0==SimpleC_PLUS ) {
            	alt10 = 1;
            }


            switch (alt10) {
        	case 1 :
        	    // simplec.g:82:16: '+'^^ atom // alt
        	    {
        	    id root_1 = (id)[adaptor emptyTree];

        	    char_literal47=(ANTLRToken *)[input LT:1];
        	    [self match:input tokenType:SimpleC_PLUS follow:FOLLOW_SimpleC_PLUS_in_aexpr657]; 
        	    char_literal47_tree = (id)[adaptor newTreeWithToken:char_literal47];
        	    root_0 = (id)[adaptor makeNode:char_literal47_tree parentOf:root_0];

        	    [following addObject:FOLLOW_atom_in_aexpr660];
        	    atom48 = [self atom];
        	    [following removeLastObject];


        	    [adaptor addChild:[atom48 tree] toTree:root_1];

        	    [adaptor addChild:root_1 toTree:root_0];

        	    }
        	    break;

        	default :
        	    goto loop10;
            }
        } while (YES); loop10: ;


        }

    }
    @catch (ANTLRRecognitionException *re) {
        [self reportError:re];
        [self recover:input exception:re];
    }
    @finally {
        // token labels
        [char_literal47 release];
        // token+rule list labels
        // rule labels
        [atom46 release];
        [atom48 release];
        // rule refs in alts with rewrites
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end aexpr


// $ANTLR start atom
// simplec.g:85:1: atom : ( ID | INT | '(' expr ')' -> expr );
- (SimpleCatom_return *) atom
{
    SimpleCatom_return * retval = [[SimpleCatom_return alloc] init]; 
    [retval setStart:[input LT:1]];

    id root_0 = nil;

    ANTLRToken * ID49 = nil;
    ANTLRToken * INT50 = nil;
    ANTLRToken * char_literal51 = nil;
    ANTLRToken * char_literal53 = nil;
    SimpleCexpr_return * expr52 = nil;

    NSMutableArray *list_expr = [[NSMutableArray alloc] init];
    NSMutableArray *list_24 = [[NSMutableArray alloc] init];
    NSMutableArray *list_22 = [[NSMutableArray alloc] init];
    id ID49_tree = nil;
    id INT50_tree = nil;
    id char_literal51_tree = nil;
    id char_literal53_tree = nil;

    @try {
        // simplec.g:86:7: ( ID | INT | '(' expr ')' -> expr ) //ruleblock
        int alt11=3;
        switch ([input LA:1]) {
        	case SimpleC_ID:
        		alt11 = 1;
        		break;
        	case SimpleC_INT:
        		alt11 = 2;
        		break;
        	case 22:
        		alt11 = 3;
        		break;
        default:
         {
            ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:11 state:0 stream:input];
        	@throw nvae;

        	}}
        switch (alt11) {
        	case 1 :
        	    // simplec.g:86:7: ID // alt
        	    {
        	    root_0 = (id)[adaptor emptyTree];

        	    ID49=(ANTLRToken *)[input LT:1];
        	    [self match:input tokenType:SimpleC_ID follow:FOLLOW_SimpleC_ID_in_atom680]; 
        	    ID49_tree = (id)[adaptor newTreeWithToken:ID49];
        	    [adaptor addChild:ID49_tree toTree:root_0];


        	    }
        	    break;
        	case 2 :
        	    // simplec.g:87:7: INT // alt
        	    {
        	    root_0 = (id)[adaptor emptyTree];

        	    INT50=(ANTLRToken *)[input LT:1];
        	    [self match:input tokenType:SimpleC_INT follow:FOLLOW_SimpleC_INT_in_atom694]; 
        	    INT50_tree = (id)[adaptor newTreeWithToken:INT50];
        	    [adaptor addChild:INT50_tree toTree:root_0];


        	    }
        	    break;
        	case 3 :
        	    // simplec.g:88:7: '(' expr ')' // alt
        	    {
        	    char_literal51=(ANTLRToken *)[input LT:1];
        	    [self match:input tokenType:22 follow:FOLLOW_22_in_atom708]; 
        	    [list_22 addObject:char_literal51];

        	    [following addObject:FOLLOW_expr_in_atom710];
        	    expr52 = [self expr];
        	    [following removeLastObject];


        	    [list_expr addObject:[expr52 tree]];
        	    char_literal53=(ANTLRToken *)[input LT:1];
        	    [self match:input tokenType:24 follow:FOLLOW_24_in_atom712]; 
        	    [list_24 addObject:char_literal53];


        	    // AST REWRITE
        	    int i_0 = 0;
        	    [retval setTree:root_0];
        	    root_0 = (id)[adaptor emptyTree];
        	    // 88:20: -> expr
        	    {
        	        [adaptor addChild:(id<ANTLRTree>)[list_expr objectAtIndex:i_0] toTree:root_0];

        	    }



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
        [ID49 release];
        [INT50 release];
        [char_literal51 release];
        [char_literal53 release];
        // token+rule list labels
        // rule labels
        [expr52 release];
        // rule refs in alts with rewrites
        [list_expr release];
        [list_24 release];
        [list_22 release];
        [retval setStop:[input LT:-1]];

            [retval setTree:(id)[adaptor postProcessTree:root_0]];
            [adaptor setBoundariesForTree:[retval tree] fromToken:[retval start] toToken:[retval stop]];

    }
    return retval;
}
// $ANTLR end atom


- (Class) adaptor
{
	return adaptor;
}

- (void) setAdaptor:(Class)theAdaptor
{
	adaptor = theAdaptor;
}

@end