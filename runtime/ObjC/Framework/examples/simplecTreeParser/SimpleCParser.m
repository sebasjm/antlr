// $ANTLR 3.0b6 SimpleC.g 2007-02-01 01:28:01

#import "SimpleCParser.h"

#import <ANTLR/ANTLR.h>


#pragma mark Cyclic DFA
@implementation SimpleCParserDFA2
const static int SimpleCParserdfa2_eot[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static int SimpleCParserdfa2_eof[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static unichar SimpleCParserdfa2_min[13] =
    {10,10,21,10,0,10,21,23,0,0,10,10,23};
const static unichar SimpleCParserdfa2_max[13] =
    {16,10,22,24,0,10,25,24,0,0,16,10,24};
const static int SimpleCParserdfa2_accept[13] =
    {-1,-1,-1,-1,1,-1,-1,-1,3,2,-1,-1,-1};
const static int SimpleCParserdfa2_special[13] =
    {-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1};
const static int SimpleCParserdfa2_transition[] = {};
const static int SimpleCParserdfa2_transition0[] = {2};
const static int SimpleCParserdfa2_transition1[] = {4, 3};
const static int SimpleCParserdfa2_transition2[] = {10, 6};
const static int SimpleCParserdfa2_transition3[] = {1, -1, -1, -1, 1, 1, 
	1};
const static int SimpleCParserdfa2_transition4[] = {7};
const static int SimpleCParserdfa2_transition5[] = {12};
const static int SimpleCParserdfa2_transition6[] = {5, -1, -1, -1, 5, 5, 
	5, -1, -1, -1, -1, -1, -1, -1, 6};
const static int SimpleCParserdfa2_transition7[] = {11, -1, -1, -1, 11, 
	11, 11};
const static int SimpleCParserdfa2_transition8[] = {9, -1, -1, -1, 8};


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
const static unsigned long long FOLLOW_SimpleCParser_ID_in_declarator199_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_ID_in_declarator199;
const static unsigned long long FOLLOW_type_in_functionHeader219_data[] = {0x0000000000000400LL};
static ANTLRBitSet *FOLLOW_type_in_functionHeader219;
const static unsigned long long FOLLOW_SimpleCParser_ID_in_functionHeader221_data[] = {0x0000000000400000LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_ID_in_functionHeader221;
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
const static unsigned long long FOLLOW_SimpleCParser_FOR_in_forStat507_data[] = {0x0000000000400000LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_FOR_in_forStat507;
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
const static unsigned long long FOLLOW_SimpleCParser_ID_in_assignStat570_data[] = {0x0000000000000800LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_ID_in_assignStat570;
const static unsigned long long FOLLOW_SimpleCParser_EQ_in_assignStat572_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_EQ_in_assignStat572;
const static unsigned long long FOLLOW_expr_in_assignStat574_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_expr_in_assignStat574;
const static unsigned long long FOLLOW_condExpr_in_expr598_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_condExpr_in_expr598;
const static unsigned long long FOLLOW_aexpr_in_condExpr617_data[] = {0x0000000000060002LL};
static ANTLRBitSet *FOLLOW_aexpr_in_condExpr617;
const static unsigned long long FOLLOW_SimpleCParser_EQEQ_in_condExpr622_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_EQEQ_in_condExpr622;
const static unsigned long long FOLLOW_SimpleCParser_LT_in_condExpr627_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_LT_in_condExpr627;
const static unsigned long long FOLLOW_aexpr_in_condExpr631_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_aexpr_in_condExpr631;
const static unsigned long long FOLLOW_atom_in_aexpr653_data[] = {0x0000000000080002LL};
static ANTLRBitSet *FOLLOW_atom_in_aexpr653;
const static unsigned long long FOLLOW_SimpleCParser_PLUS_in_aexpr657_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_PLUS_in_aexpr657;
const static unsigned long long FOLLOW_atom_in_aexpr660_data[] = {0x0000000000080002LL};
static ANTLRBitSet *FOLLOW_atom_in_aexpr660;
const static unsigned long long FOLLOW_SimpleCParser_ID_in_atom680_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_ID_in_atom680;
const static unsigned long long FOLLOW_SimpleCParser_INT_in_atom694_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_SimpleCParser_INT_in_atom694;
const static unsigned long long FOLLOW_22_in_atom708_data[] = {0x0000000000401400LL};
static ANTLRBitSet *FOLLOW_22_in_atom708;
const static unsigned long long FOLLOW_expr_in_atom710_data[] = {0x0000000001000000LL};
static ANTLRBitSet *FOLLOW_expr_in_atom710;
const static unsigned long long FOLLOW_24_in_atom712_data[] = {0x0000000000000002LL};
static ANTLRBitSet *FOLLOW_24_in_atom712;


#pragma mark Dynamic Global Scopes

#pragma mark Dynamic Rule Scopes

#pragma mark Rule return scopes start
@implementation SimpleCParser_program_return
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
@implementation SimpleCParser_declaration_return
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
@implementation SimpleCParser_variable_return
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
@implementation SimpleCParser_declarator_return
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
@implementation SimpleCParser_functionHeader_return
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
@implementation SimpleCParser_formalParameter_return
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
@implementation SimpleCParser_type_return
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
@implementation SimpleCParser_block_return
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
@implementation SimpleCParser_stat_return
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
@implementation SimpleCParser_forStat_return
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
@implementation SimpleCParser_assignStat_return
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
@implementation SimpleCParser_expr_return
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
@implementation SimpleCParser_condExpr_return
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
@implementation SimpleCParser_aexpr_return
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
@implementation SimpleCParser_atom_return
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


@implementation SimpleCParser

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
	FOLLOW_SimpleCParser_ID_in_declarator199 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_ID_in_declarator199_data count:1];
	FOLLOW_type_in_functionHeader219 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_type_in_functionHeader219_data count:1];
	FOLLOW_SimpleCParser_ID_in_functionHeader221 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_ID_in_functionHeader221_data count:1];
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
	FOLLOW_SimpleCParser_FOR_in_forStat507 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_FOR_in_forStat507_data count:1];
	FOLLOW_22_in_forStat509 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_22_in_forStat509_data count:1];
	FOLLOW_assignStat_in_forStat513 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_assignStat_in_forStat513_data count:1];
	FOLLOW_21_in_forStat515 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_21_in_forStat515_data count:1];
	FOLLOW_expr_in_forStat517 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_forStat517_data count:1];
	FOLLOW_21_in_forStat519 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_21_in_forStat519_data count:1];
	FOLLOW_assignStat_in_forStat523 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_assignStat_in_forStat523_data count:1];
	FOLLOW_24_in_forStat525 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_24_in_forStat525_data count:1];
	FOLLOW_block_in_forStat527 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_block_in_forStat527_data count:1];
	FOLLOW_SimpleCParser_ID_in_assignStat570 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_ID_in_assignStat570_data count:1];
	FOLLOW_SimpleCParser_EQ_in_assignStat572 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_EQ_in_assignStat572_data count:1];
	FOLLOW_expr_in_assignStat574 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_assignStat574_data count:1];
	FOLLOW_condExpr_in_expr598 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_condExpr_in_expr598_data count:1];
	FOLLOW_aexpr_in_condExpr617 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_aexpr_in_condExpr617_data count:1];
	FOLLOW_SimpleCParser_EQEQ_in_condExpr622 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_EQEQ_in_condExpr622_data count:1];
	FOLLOW_SimpleCParser_LT_in_condExpr627 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_LT_in_condExpr627_data count:1];
	FOLLOW_aexpr_in_condExpr631 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_aexpr_in_condExpr631_data count:1];
	FOLLOW_atom_in_aexpr653 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_atom_in_aexpr653_data count:1];
	FOLLOW_SimpleCParser_PLUS_in_aexpr657 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_PLUS_in_aexpr657_data count:1];
	FOLLOW_atom_in_aexpr660 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_atom_in_aexpr660_data count:1];
	FOLLOW_SimpleCParser_ID_in_atom680 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_ID_in_atom680_data count:1];
	FOLLOW_SimpleCParser_INT_in_atom694 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_SimpleCParser_INT_in_atom694_data count:1];
	FOLLOW_22_in_atom708 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_22_in_atom708_data count:1];
	FOLLOW_expr_in_atom710 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_expr_in_atom710_data count:1];
	FOLLOW_24_in_atom712 = [[ANTLRBitSet alloc] initWithBits:FOLLOW_24_in_atom712_data count:1];

}

- (id) initWithTokenStream:(id<ANTLRTokenStream>)aStream
{
	if ((self = [super initWithTokenStream:aStream])) {

		tokenNames = [[NSArray alloc] initWithObjects:@"<invalid>", @"<EOR>", @"<DOWN>", @"<UP>", 
	@"VAR_DEF", @"ARG_DEF", @"FUNC_HDR", @"FUNC_DECL", @"FUNC_DEF", @"BLOCK", 
	@"ID", @"EQ", @"INT", @"FOR", @"INT_TYPE", @"CHAR", @"VOID", @"EQEQ", @"LT", 
	@"PLUS", @"WS", @"';'", @"'('", @"','", @"')'", @"'{'", @"'}'", nil];
		dfa2 = [[SimpleCParserDFA2 alloc] initWithRecognizer:self];
																																
		[self setTreeAdaptor:[[[ANTLRCommonTreeAdaptor alloc] init] autorelease]];
	}
	return self;
}

- (void) dealloc
{
	[tokenNames release];
	[dfa2 release];
	[self setTreeAdaptor:nil];

	[super dealloc];
}

- (NSString *) grammarFileName
{
	return @"SimpleC.g";
}


// $ANTLR start program
// SimpleC.g:16:1: program : ( declaration )+ ;
- (SimpleCParser_program_return *) program
{
    SimpleCParser_program_return * _retval = [[SimpleCParser_program_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    // token+rule list labels
    // rule labels
    SimpleCParser_declaration_return * _declaration1 = nil;

    // rule list labels
    // rule refs in alts with rewrites


    @try {
        // SimpleC.g:17:9: ( ( declaration )+ ) // ruleBlockSingleAlt
        // SimpleC.g:17:9: ( declaration )+ // alt
        {
        root_0 = (id)[treeAdaptor emptyTree];

        // SimpleC.g:17:9: ( declaration )+	// positiveClosureBlock
        int cnt1=0;

        do {
            int alt1=2;
            {
            	int LA1_0 = [input LA:1];
            	if ( LA1_0==SimpleCParser_ID||(LA1_0>=SimpleCParser_INT_TYPE && LA1_0<=SimpleCParser_VOID) ) {
            		alt1 = 1;
            	}

            }
            switch (alt1) {
        	case 1 :
        	    // SimpleC.g:17:9: declaration // alt
        	    {
        	    [following addObject:FOLLOW_declaration_in_program85];
        	    _declaration1 = [self declaration];
        	    [following removeLastObject];


        	    [treeAdaptor addChild:[_declaration1 tree] toTree:root_0];

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
		[_declaration1 release];
		// rule refs in alts with rewrites
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end program

// $ANTLR start declaration
// SimpleC.g:20:1: declaration : ( variable | functionHeader ';' -> ^( FUNC_DECL functionHeader ) | functionHeader block -> ^( FUNC_DEF functionHeader block ) );
- (SimpleCParser_declaration_return *) declaration
{
    SimpleCParser_declaration_return * _retval = [[SimpleCParser_declaration_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _char_literal4 = nil;
    // token+rule list labels
    // rule labels
    SimpleCParser_variable_return * _variable2 = nil;

    SimpleCParser_functionHeader_return * _functionHeader3 = nil;

    SimpleCParser_functionHeader_return * _functionHeader5 = nil;

    SimpleCParser_block_return * _block6 = nil;

    // rule list labels
    // rule refs in alts with rewrites
    NSMutableArray *_list_functionHeader = [[NSMutableArray alloc] init];
    NSMutableArray *_list_block = [[NSMutableArray alloc] init];
    NSMutableArray *_list_21 = [[NSMutableArray alloc] init];
    id _char_literal4_tree = nil;

    @try {
        // SimpleC.g:21:9: ( variable | functionHeader ';' -> ^( FUNC_DECL functionHeader ) | functionHeader block -> ^( FUNC_DEF functionHeader block ) ) //ruleblock
        int alt2=3;
        alt2 = [dfa2 predict];
        switch (alt2) {
        	case 1 :
        	    // SimpleC.g:21:9: variable // alt
        	    {
        	    root_0 = (id)[treeAdaptor emptyTree];

        	    [following addObject:FOLLOW_variable_in_declaration105];
        	    _variable2 = [self variable];
        	    [following removeLastObject];


        	    [treeAdaptor addChild:[_variable2 tree] toTree:root_0];

        	    }
        	    break;
        	case 2 :
        	    // SimpleC.g:22:9: functionHeader ';' // alt
        	    {
        	    [following addObject:FOLLOW_functionHeader_in_declaration115];
        	    _functionHeader3 = [self functionHeader];
        	    [following removeLastObject];


        	    [_list_functionHeader addObject:[_functionHeader3 tree]];
        	    _char_literal4=(ANTLRToken *)[input LT:1];
        	    [_char_literal4 retain];
        	    [self match:input tokenType:21 follow:FOLLOW_21_in_declaration117]; 
        	    [_list_21 addObject:_char_literal4];


        	    // AST REWRITE
        	    int i_0 = 0;
        	    [_retval setTree:root_0];
        	    root_0 = (id)[treeAdaptor emptyTree];
        	    // 22:28: -> ^( FUNC_DECL functionHeader )
        	    {
        	        // SimpleC.g:22:31: ^( FUNC_DECL functionHeader )
        	        {
        	        id root_1 = (id)[treeAdaptor emptyTree];
        	        root_1 = (id)[treeAdaptor makeNode:[treeAdaptor newTreeWithTokenType:SimpleCParser_FUNC_DECL text:[tokenNames objectAtIndex:SimpleCParser_FUNC_DECL]] parentOf:root_1];

        	        [treeAdaptor addChild:(id<ANTLRTree>)[_list_functionHeader objectAtIndex:i_0] toTree:root_1];

        	        [treeAdaptor addChild:root_1 toTree:root_0];
        	        }

        	    }



        	    }
        	    break;
        	case 3 :
        	    // SimpleC.g:23:9: functionHeader block // alt
        	    {
        	    [following addObject:FOLLOW_functionHeader_in_declaration135];
        	    _functionHeader5 = [self functionHeader];
        	    [following removeLastObject];


        	    [_list_functionHeader addObject:[_functionHeader5 tree]];
        	    [following addObject:FOLLOW_block_in_declaration137];
        	    _block6 = [self block];
        	    [following removeLastObject];


        	    [_list_block addObject:[_block6 tree]];

        	    // AST REWRITE
        	    int i_0 = 0;
        	    [_retval setTree:root_0];
        	    root_0 = (id)[treeAdaptor emptyTree];
        	    // 23:30: -> ^( FUNC_DEF functionHeader block )
        	    {
        	        // SimpleC.g:23:33: ^( FUNC_DEF functionHeader block )
        	        {
        	        id root_1 = (id)[treeAdaptor emptyTree];
        	        root_1 = (id)[treeAdaptor makeNode:[treeAdaptor newTreeWithTokenType:SimpleCParser_FUNC_DEF text:[tokenNames objectAtIndex:SimpleCParser_FUNC_DEF]] parentOf:root_1];

        	        [treeAdaptor addChild:(id<ANTLRTree>)[_list_functionHeader objectAtIndex:i_0] toTree:root_1];
        	        [treeAdaptor addChild:(id<ANTLRTree>)[_list_block objectAtIndex:i_0] toTree:root_1];

        	        [treeAdaptor addChild:root_1 toTree:root_0];
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
		[_char_literal4 release];
		// token+rule list labels
		// rule labels
		[_variable2 release];
		[_functionHeader3 release];
		[_functionHeader5 release];
		[_block6 release];
		// rule refs in alts with rewrites
		[_list_functionHeader release];
		[_list_block release];
		[_list_21 release];
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end declaration

// $ANTLR start variable
// SimpleC.g:26:1: variable : type declarator ';' -> ^( VAR_DEF type declarator ) ;
- (SimpleCParser_variable_return *) variable
{
    SimpleCParser_variable_return * _retval = [[SimpleCParser_variable_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _char_literal9 = nil;
    // token+rule list labels
    // rule labels
    SimpleCParser_type_return * _type7 = nil;

    SimpleCParser_declarator_return * _declarator8 = nil;

    // rule list labels
    // rule refs in alts with rewrites
    NSMutableArray *_list_type = [[NSMutableArray alloc] init];
    NSMutableArray *_list_declarator = [[NSMutableArray alloc] init];
    NSMutableArray *_list_21 = [[NSMutableArray alloc] init];
    id _char_literal9_tree = nil;

    @try {
        // SimpleC.g:27:9: ( type declarator ';' -> ^( VAR_DEF type declarator ) ) // ruleBlockSingleAlt
        // SimpleC.g:27:9: type declarator ';' // alt
        {
        [following addObject:FOLLOW_type_in_variable166];
        _type7 = [self type];
        [following removeLastObject];


        [_list_type addObject:[_type7 tree]];
        [following addObject:FOLLOW_declarator_in_variable168];
        _declarator8 = [self declarator];
        [following removeLastObject];


        [_list_declarator addObject:[_declarator8 tree]];
        _char_literal9=(ANTLRToken *)[input LT:1];
        [_char_literal9 retain];
        [self match:input tokenType:21 follow:FOLLOW_21_in_variable170]; 
        [_list_21 addObject:_char_literal9];


        // AST REWRITE
        int i_0 = 0;
        [_retval setTree:root_0];
        root_0 = (id)[treeAdaptor emptyTree];
        // 27:29: -> ^( VAR_DEF type declarator )
        {
            // SimpleC.g:27:32: ^( VAR_DEF type declarator )
            {
            id root_1 = (id)[treeAdaptor emptyTree];
            root_1 = (id)[treeAdaptor makeNode:[treeAdaptor newTreeWithTokenType:SimpleCParser_VAR_DEF text:[tokenNames objectAtIndex:SimpleCParser_VAR_DEF]] parentOf:root_1];

            [treeAdaptor addChild:(id<ANTLRTree>)[_list_type objectAtIndex:i_0] toTree:root_1];
            [treeAdaptor addChild:(id<ANTLRTree>)[_list_declarator objectAtIndex:i_0] toTree:root_1];

            [treeAdaptor addChild:root_1 toTree:root_0];
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
		[_char_literal9 release];
		// token+rule list labels
		// rule labels
		[_type7 release];
		[_declarator8 release];
		// rule refs in alts with rewrites
		[_list_type release];
		[_list_declarator release];
		[_list_21 release];
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end variable

// $ANTLR start declarator
// SimpleC.g:30:1: declarator : ID ;
- (SimpleCParser_declarator_return *) declarator
{
    SimpleCParser_declarator_return * _retval = [[SimpleCParser_declarator_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _ID10 = nil;
    // token+rule list labels
    // rule labels
    // rule list labels
    // rule refs in alts with rewrites

    id _ID10_tree = nil;

    @try {
        // SimpleC.g:31:9: ( ID ) // ruleBlockSingleAlt
        // SimpleC.g:31:9: ID // alt
        {
        root_0 = (id)[treeAdaptor emptyTree];

        _ID10=(ANTLRToken *)[input LT:1];
        [_ID10 retain];
        [self match:input tokenType:SimpleCParser_ID follow:FOLLOW_SimpleCParser_ID_in_declarator199]; 
        _ID10_tree = (id)[treeAdaptor newTreeWithToken:_ID10];
        [treeAdaptor addChild:_ID10_tree toTree:root_0];


        }

    }
	@catch (ANTLRRecognitionException *re) {
		[self reportError:re];
		[self recover:input exception:re];
	}
	@finally {
		// token labels
		[_ID10 release];
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end declarator

// $ANTLR start functionHeader
// SimpleC.g:34:1: functionHeader : type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' -> ^( FUNC_HDR type ID ( formalParameter )+ ) ;
- (SimpleCParser_functionHeader_return *) functionHeader
{
    SimpleCParser_functionHeader_return * _retval = [[SimpleCParser_functionHeader_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _ID12 = nil;
    ANTLRToken * _char_literal13 = nil;
    ANTLRToken * _char_literal15 = nil;
    ANTLRToken * _char_literal17 = nil;
    // token+rule list labels
    // rule labels
    SimpleCParser_type_return * _type11 = nil;

    SimpleCParser_formalParameter_return * _formalParameter14 = nil;

    SimpleCParser_formalParameter_return * _formalParameter16 = nil;

    // rule list labels
    // rule refs in alts with rewrites
    NSMutableArray *_list_formalParameter = [[NSMutableArray alloc] init];
    NSMutableArray *_list_type = [[NSMutableArray alloc] init];
    NSMutableArray *_list_24 = [[NSMutableArray alloc] init];
    NSMutableArray *_list_SimpleCParser_ID = [[NSMutableArray alloc] init];
    NSMutableArray *_list_22 = [[NSMutableArray alloc] init];
    NSMutableArray *_list_23 = [[NSMutableArray alloc] init];
    id _ID12_tree = nil;
    id _char_literal13_tree = nil;
    id _char_literal15_tree = nil;
    id _char_literal17_tree = nil;

    @try {
        // SimpleC.g:35:9: ( type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' -> ^( FUNC_HDR type ID ( formalParameter )+ ) ) // ruleBlockSingleAlt
        // SimpleC.g:35:9: type ID '(' ( formalParameter ( ',' formalParameter )* )? ')' // alt
        {
        [following addObject:FOLLOW_type_in_functionHeader219];
        _type11 = [self type];
        [following removeLastObject];


        [_list_type addObject:[_type11 tree]];
        _ID12=(ANTLRToken *)[input LT:1];
        [_ID12 retain];
        [self match:input tokenType:SimpleCParser_ID follow:FOLLOW_SimpleCParser_ID_in_functionHeader221]; 
        [_list_SimpleCParser_ID addObject:_ID12];

        _char_literal13=(ANTLRToken *)[input LT:1];
        [_char_literal13 retain];
        [self match:input tokenType:22 follow:FOLLOW_22_in_functionHeader223]; 
        [_list_22 addObject:_char_literal13];

        // SimpleC.g:35:21: ( formalParameter ( ',' formalParameter )* )? // block
        int alt4=2;
        {
        	int LA4_0 = [input LA:1];
        	if ( LA4_0==SimpleCParser_ID||(LA4_0>=SimpleCParser_INT_TYPE && LA4_0<=SimpleCParser_VOID) ) {
        		alt4 = 1;
        	}
        }
        switch (alt4) {
        	case 1 :
        	    // SimpleC.g:35:23: formalParameter ( ',' formalParameter )* // alt
        	    {
        	    [following addObject:FOLLOW_formalParameter_in_functionHeader227];
        	    _formalParameter14 = [self formalParameter];
        	    [following removeLastObject];


        	    [_list_formalParameter addObject:[_formalParameter14 tree]];
        	    do {
        	        int alt3=2;
        	        {
        	        	int LA3_0 = [input LA:1];
        	        	if ( LA3_0==23 ) {
        	        		alt3 = 1;
        	        	}

        	        }
        	        switch (alt3) {
        	    	case 1 :
        	    	    // SimpleC.g:35:41: ',' formalParameter // alt
        	    	    {
        	    	    _char_literal15=(ANTLRToken *)[input LT:1];
        	    	    [_char_literal15 retain];
        	    	    [self match:input tokenType:23 follow:FOLLOW_23_in_functionHeader231]; 
        	    	    [_list_23 addObject:_char_literal15];

        	    	    [following addObject:FOLLOW_formalParameter_in_functionHeader233];
        	    	    _formalParameter16 = [self formalParameter];
        	    	    [following removeLastObject];


        	    	    [_list_formalParameter addObject:[_formalParameter16 tree]];

        	    	    }
        	    	    break;

        	    	default :
        	    	    goto loop3;
        	        }
        	    } while (YES); loop3: ;


        	    }
        	    break;

        }

        _char_literal17=(ANTLRToken *)[input LT:1];
        [_char_literal17 retain];
        [self match:input tokenType:24 follow:FOLLOW_24_in_functionHeader241]; 
        [_list_24 addObject:_char_literal17];


        // AST REWRITE
        int i_0 = 0;
        [_retval setTree:root_0];
        root_0 = (id)[treeAdaptor emptyTree];
        // 36:9: -> ^( FUNC_HDR type ID ( formalParameter )+ )
        {
            // SimpleC.g:36:12: ^( FUNC_HDR type ID ( formalParameter )+ )
            {
            id root_1 = (id)[treeAdaptor emptyTree];
            root_1 = (id)[treeAdaptor makeNode:[treeAdaptor newTreeWithTokenType:SimpleCParser_FUNC_HDR text:[tokenNames objectAtIndex:SimpleCParser_FUNC_HDR]] parentOf:root_1];

            [treeAdaptor addChild:(id<ANTLRTree>)[_list_type objectAtIndex:i_0] toTree:root_1];
            [treeAdaptor addTokenAsChild:(ANTLRToken *)[_list_SimpleCParser_ID objectAtIndex:i_0] toTree:root_1];
            // SimpleC.g:36:31: ( formalParameter )+
            {
            int n_1 = _list_formalParameter == nil ? 0 : [_list_formalParameter count];
             


            if ( n_1==0 ) @throw [NSException exceptionWithName:@"ANTLRTreeRewriteException" reason:@"Must have more than one element for (...)+ loops" userInfo:nil];
            int i_1;
            for (i_1=0; i_1<n_1; i_1++) {
                [treeAdaptor addChild:(id<ANTLRTree>)[_list_formalParameter objectAtIndex:i_1] toTree:root_1];

            }
            }

            [treeAdaptor addChild:root_1 toTree:root_0];
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
		[_ID12 release];
		[_char_literal13 release];
		[_char_literal15 release];
		[_char_literal17 release];
		// token+rule list labels
		// rule labels
		[_type11 release];
		[_formalParameter14 release];
		[_formalParameter16 release];
		// rule refs in alts with rewrites
		[_list_formalParameter release];
		[_list_type release];
		[_list_24 release];
		[_list_SimpleCParser_ID release];
		[_list_22 release];
		[_list_23 release];
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end functionHeader

// $ANTLR start formalParameter
// SimpleC.g:39:1: formalParameter : type declarator -> ^( ARG_DEF type declarator ) ;
- (SimpleCParser_formalParameter_return *) formalParameter
{
    SimpleCParser_formalParameter_return * _retval = [[SimpleCParser_formalParameter_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    // token+rule list labels
    // rule labels
    SimpleCParser_type_return * _type18 = nil;

    SimpleCParser_declarator_return * _declarator19 = nil;

    // rule list labels
    // rule refs in alts with rewrites
    NSMutableArray *_list_type = [[NSMutableArray alloc] init];
    NSMutableArray *_list_declarator = [[NSMutableArray alloc] init];

    @try {
        // SimpleC.g:40:9: ( type declarator -> ^( ARG_DEF type declarator ) ) // ruleBlockSingleAlt
        // SimpleC.g:40:9: type declarator // alt
        {
        [following addObject:FOLLOW_type_in_formalParameter281];
        _type18 = [self type];
        [following removeLastObject];


        [_list_type addObject:[_type18 tree]];
        [following addObject:FOLLOW_declarator_in_formalParameter283];
        _declarator19 = [self declarator];
        [following removeLastObject];


        [_list_declarator addObject:[_declarator19 tree]];

        // AST REWRITE
        int i_0 = 0;
        [_retval setTree:root_0];
        root_0 = (id)[treeAdaptor emptyTree];
        // 40:25: -> ^( ARG_DEF type declarator )
        {
            // SimpleC.g:40:28: ^( ARG_DEF type declarator )
            {
            id root_1 = (id)[treeAdaptor emptyTree];
            root_1 = (id)[treeAdaptor makeNode:[treeAdaptor newTreeWithTokenType:SimpleCParser_ARG_DEF text:[tokenNames objectAtIndex:SimpleCParser_ARG_DEF]] parentOf:root_1];

            [treeAdaptor addChild:(id<ANTLRTree>)[_list_type objectAtIndex:i_0] toTree:root_1];
            [treeAdaptor addChild:(id<ANTLRTree>)[_list_declarator objectAtIndex:i_0] toTree:root_1];

            [treeAdaptor addChild:root_1 toTree:root_0];
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
		[_type18 release];
		[_declarator19 release];
		// rule refs in alts with rewrites
		[_list_type release];
		[_list_declarator release];
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end formalParameter

// $ANTLR start type
// SimpleC.g:43:1: type : ('int'|'char'|'void'|ID);
- (SimpleCParser_type_return *) type
{
    SimpleCParser_type_return * _retval = [[SimpleCParser_type_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _set20 = nil;
    // token+rule list labels
    // rule labels
    // rule list labels
    // rule refs in alts with rewrites

    id _set20_tree = nil;

    @try {
        // SimpleC.g:44:5: ( ('int'|'char'|'void'|ID)) // ruleBlockSingleAlt
        // SimpleC.g:44:9: ('int'|'char'|'void'|ID) // alt
        {
        root_0 = (id)[treeAdaptor emptyTree];

        _set20 = (ANTLRToken *)[input LT:1];
        if ([input LA:1]==SimpleCParser_ID||([input LA:1]>=SimpleCParser_INT_TYPE && [input LA:1]<=SimpleCParser_VOID)) {
        	[treeAdaptor addChild:[treeAdaptor newTreeWithToken:_set20] toTree:root_0];
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
		[_set20 release];
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end type

// $ANTLR start block
// SimpleC.g:50:1: block : lc= '{' ( variable )* ( stat )* '}' -> ^( BLOCK[$lc,\"BLOCK\"] ( variable )* ( stat )* ) ;
- (SimpleCParser_block_return *) block
{
    SimpleCParser_block_return * _retval = [[SimpleCParser_block_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _lc = nil;
    ANTLRToken * _char_literal23 = nil;
    // token+rule list labels
    // rule labels
    SimpleCParser_variable_return * _variable21 = nil;

    SimpleCParser_stat_return * _stat22 = nil;

    // rule list labels
    // rule refs in alts with rewrites
    NSMutableArray *_list_stat = [[NSMutableArray alloc] init];
    NSMutableArray *_list_variable = [[NSMutableArray alloc] init];
    NSMutableArray *_list_26 = [[NSMutableArray alloc] init];
    NSMutableArray *_list_25 = [[NSMutableArray alloc] init];
    id _lc_tree = nil;
    id _char_literal23_tree = nil;

    @try {
        // SimpleC.g:51:9: (lc= '{' ( variable )* ( stat )* '}' -> ^( BLOCK[$lc,\"BLOCK\"] ( variable )* ( stat )* ) ) // ruleBlockSingleAlt
        // SimpleC.g:51:9: lc= '{' ( variable )* ( stat )* '}' // alt
        {
        _lc=(ANTLRToken *)[input LT:1];
        [_lc retain];
        [self match:input tokenType:25 follow:FOLLOW_25_in_block376]; 
        [_list_25 addObject:_lc];

        do {
            int alt5=2;
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
            	else if ( (LA5_0>=SimpleCParser_INT_TYPE && LA5_0<=SimpleCParser_VOID) ) {
            		alt5 = 1;
            	}

            }
            switch (alt5) {
        	case 1 :
        	    // SimpleC.g:52:13: variable // alt
        	    {
        	    [following addObject:FOLLOW_variable_in_block390];
        	    _variable21 = [self variable];
        	    [following removeLastObject];


        	    [_list_variable addObject:[_variable21 tree]];

        	    }
        	    break;

        	default :
        	    goto loop5;
            }
        } while (YES); loop5: ;

        do {
            int alt6=2;
            {
            	int LA6_0 = [input LA:1];
            	if ( LA6_0==SimpleCParser_ID||(LA6_0>=SimpleCParser_INT && LA6_0<=SimpleCParser_FOR)||(LA6_0>=21 && LA6_0<=22)||LA6_0==25 ) {
            		alt6 = 1;
            	}

            }
            switch (alt6) {
        	case 1 :
        	    // SimpleC.g:53:13: stat // alt
        	    {
        	    [following addObject:FOLLOW_stat_in_block405];
        	    _stat22 = [self stat];
        	    [following removeLastObject];


        	    [_list_stat addObject:[_stat22 tree]];

        	    }
        	    break;

        	default :
        	    goto loop6;
            }
        } while (YES); loop6: ;

        _char_literal23=(ANTLRToken *)[input LT:1];
        [_char_literal23 retain];
        [self match:input tokenType:26 follow:FOLLOW_26_in_block416]; 
        [_list_26 addObject:_char_literal23];


        // AST REWRITE
        int i_0 = 0;
        [_retval setTree:root_0];
        root_0 = (id)[treeAdaptor emptyTree];
        // 55:9: -> ^( BLOCK[$lc,\"BLOCK\"] ( variable )* ( stat )* )
        {
            // SimpleC.g:55:12: ^( BLOCK[$lc,\"BLOCK\"] ( variable )* ( stat )* )
            {
            id root_1 = (id)[treeAdaptor emptyTree];
            root_1 = (id)[treeAdaptor makeNode:[treeAdaptor newTreeWithTokenType:SimpleCParser_BLOCK text:[_lc text]] parentOf:root_1];

            // SimpleC.g:55:33: ( variable )*
            {
            int n_1 = _list_variable == nil ? 0 : [_list_variable count];
             


            int i_1;
            for (i_1=0; i_1<n_1; i_1++) {
                [treeAdaptor addChild:(id<ANTLRTree>)[_list_variable objectAtIndex:i_1] toTree:root_1];

            }
            }
            // SimpleC.g:55:43: ( stat )*
            {
            int n_1 = _list_stat == nil ? 0 : [_list_stat count];
             


            int i_1;
            for (i_1=0; i_1<n_1; i_1++) {
                [treeAdaptor addChild:(id<ANTLRTree>)[_list_stat objectAtIndex:i_1] toTree:root_1];

            }
            }

            [treeAdaptor addChild:root_1 toTree:root_0];
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
		[_lc release];
		[_char_literal23 release];
		// token+rule list labels
		// rule labels
		[_variable21 release];
		[_stat22 release];
		// rule refs in alts with rewrites
		[_list_stat release];
		[_list_variable release];
		[_list_26 release];
		[_list_25 release];
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end block

// $ANTLR start stat
// SimpleC.g:58:1: stat : ( forStat | expr ';'! | block | assignStat ';'! | ';'! );
- (SimpleCParser_stat_return *) stat
{
    SimpleCParser_stat_return * _retval = [[SimpleCParser_stat_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _char_literal26 = nil;
    ANTLRToken * _char_literal29 = nil;
    ANTLRToken * _char_literal30 = nil;
    // token+rule list labels
    // rule labels
    SimpleCParser_forStat_return * _forStat24 = nil;

    SimpleCParser_expr_return * _expr25 = nil;

    SimpleCParser_block_return * _block27 = nil;

    SimpleCParser_assignStat_return * _assignStat28 = nil;

    // rule list labels
    // rule refs in alts with rewrites

    id _char_literal26_tree = nil;
    id _char_literal29_tree = nil;
    id _char_literal30_tree = nil;

    @try {
        // SimpleC.g:58:7: ( forStat | expr ';'! | block | assignStat ';'! | ';'! ) //ruleblock
        int alt7=5;
        switch ([input LA:1]) {
        	case SimpleCParser_FOR:
        		alt7 = 1;
        		break;
        	case SimpleCParser_ID:
        		{
        			int LA7_2 = [input LA:2];
        			if ( LA7_2==SimpleCParser_EQ ) {
        				alt7 = 4;
        			}
        			else if ( (LA7_2>=SimpleCParser_EQEQ && LA7_2<=SimpleCParser_PLUS)||LA7_2==21 ) {
        				alt7 = 2;
        			}
        		else {
        		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:7 state:2 stream:input];
        			@throw nvae;
        			}
        		}
        		break;
        	case SimpleCParser_INT:
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
        	    // SimpleC.g:58:7: forStat // alt
        	    {
        	    root_0 = (id)[treeAdaptor emptyTree];

        	    [following addObject:FOLLOW_forStat_in_stat449];
        	    _forStat24 = [self forStat];
        	    [following removeLastObject];


        	    [treeAdaptor addChild:[_forStat24 tree] toTree:root_0];

        	    }
        	    break;
        	case 2 :
        	    // SimpleC.g:59:7: expr ';'! // alt
        	    {
        	    root_0 = (id)[treeAdaptor emptyTree];

        	    [following addObject:FOLLOW_expr_in_stat457];
        	    _expr25 = [self expr];
        	    [following removeLastObject];


        	    [treeAdaptor addChild:[_expr25 tree] toTree:root_0];
        	    _char_literal26=(ANTLRToken *)[input LT:1];
        	    [_char_literal26 retain];
        	    [self match:input tokenType:21 follow:FOLLOW_21_in_stat459]; 

        	    }
        	    break;
        	case 3 :
        	    // SimpleC.g:60:7: block // alt
        	    {
        	    root_0 = (id)[treeAdaptor emptyTree];

        	    [following addObject:FOLLOW_block_in_stat468];
        	    _block27 = [self block];
        	    [following removeLastObject];


        	    [treeAdaptor addChild:[_block27 tree] toTree:root_0];

        	    }
        	    break;
        	case 4 :
        	    // SimpleC.g:61:7: assignStat ';'! // alt
        	    {
        	    root_0 = (id)[treeAdaptor emptyTree];

        	    [following addObject:FOLLOW_assignStat_in_stat476];
        	    _assignStat28 = [self assignStat];
        	    [following removeLastObject];


        	    [treeAdaptor addChild:[_assignStat28 tree] toTree:root_0];
        	    _char_literal29=(ANTLRToken *)[input LT:1];
        	    [_char_literal29 retain];
        	    [self match:input tokenType:21 follow:FOLLOW_21_in_stat478]; 

        	    }
        	    break;
        	case 5 :
        	    // SimpleC.g:62:7: ';'! // alt
        	    {
        	    root_0 = (id)[treeAdaptor emptyTree];

        	    _char_literal30=(ANTLRToken *)[input LT:1];
        	    [_char_literal30 retain];
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
		[_char_literal26 release];
		[_char_literal29 release];
		[_char_literal30 release];
		// token+rule list labels
		// rule labels
		[_forStat24 release];
		[_expr25 release];
		[_block27 release];
		[_assignStat28 release];
		// rule refs in alts with rewrites
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end stat

// $ANTLR start forStat
// SimpleC.g:65:1: forStat : 'for' '(' start= assignStat ';' expr ';' next= assignStat ')' block -> ^( 'for' $start expr $next block ) ;
- (SimpleCParser_forStat_return *) forStat
{
    SimpleCParser_forStat_return * _retval = [[SimpleCParser_forStat_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _string_literal31 = nil;
    ANTLRToken * _char_literal32 = nil;
    ANTLRToken * _char_literal33 = nil;
    ANTLRToken * _char_literal35 = nil;
    ANTLRToken * _char_literal36 = nil;
    // token+rule list labels
    // rule labels
    SimpleCParser_assignStat_return * _start = nil;

    SimpleCParser_assignStat_return * _next = nil;

    SimpleCParser_expr_return * _expr34 = nil;

    SimpleCParser_block_return * _block37 = nil;

    // rule list labels
    // rule refs in alts with rewrites
    NSMutableArray *_list_expr = [[NSMutableArray alloc] init];
    NSMutableArray *_list_assignStat = [[NSMutableArray alloc] init];
    NSMutableArray *_list_block = [[NSMutableArray alloc] init];
    NSMutableArray *_list_21 = [[NSMutableArray alloc] init];
    NSMutableArray *_list_24 = [[NSMutableArray alloc] init];
    NSMutableArray *_list_SimpleCParser_FOR = [[NSMutableArray alloc] init];
    NSMutableArray *_list_22 = [[NSMutableArray alloc] init];
    id _string_literal31_tree = nil;
    id _char_literal32_tree = nil;
    id _char_literal33_tree = nil;
    id _char_literal35_tree = nil;
    id _char_literal36_tree = nil;

    @try {
        // SimpleC.g:66:9: ( 'for' '(' start= assignStat ';' expr ';' next= assignStat ')' block -> ^( 'for' $start expr $next block ) ) // ruleBlockSingleAlt
        // SimpleC.g:66:9: 'for' '(' start= assignStat ';' expr ';' next= assignStat ')' block // alt
        {
        _string_literal31=(ANTLRToken *)[input LT:1];
        [_string_literal31 retain];
        [self match:input tokenType:SimpleCParser_FOR follow:FOLLOW_SimpleCParser_FOR_in_forStat507]; 
        [_list_SimpleCParser_FOR addObject:_string_literal31];

        _char_literal32=(ANTLRToken *)[input LT:1];
        [_char_literal32 retain];
        [self match:input tokenType:22 follow:FOLLOW_22_in_forStat509]; 
        [_list_22 addObject:_char_literal32];

        [following addObject:FOLLOW_assignStat_in_forStat513];
        _start = [self assignStat];
        [following removeLastObject];


        [_list_assignStat addObject:[_start tree]];
        _char_literal33=(ANTLRToken *)[input LT:1];
        [_char_literal33 retain];
        [self match:input tokenType:21 follow:FOLLOW_21_in_forStat515]; 
        [_list_21 addObject:_char_literal33];

        [following addObject:FOLLOW_expr_in_forStat517];
        _expr34 = [self expr];
        [following removeLastObject];


        [_list_expr addObject:[_expr34 tree]];
        _char_literal35=(ANTLRToken *)[input LT:1];
        [_char_literal35 retain];
        [self match:input tokenType:21 follow:FOLLOW_21_in_forStat519]; 
        [_list_21 addObject:_char_literal35];

        [following addObject:FOLLOW_assignStat_in_forStat523];
        _next = [self assignStat];
        [following removeLastObject];


        [_list_assignStat addObject:[_next tree]];
        _char_literal36=(ANTLRToken *)[input LT:1];
        [_char_literal36 retain];
        [self match:input tokenType:24 follow:FOLLOW_24_in_forStat525]; 
        [_list_24 addObject:_char_literal36];

        [following addObject:FOLLOW_block_in_forStat527];
        _block37 = [self block];
        [following removeLastObject];


        [_list_block addObject:[_block37 tree]];

        // AST REWRITE
        int i_0 = 0;
        [_retval setTree:root_0];
        root_0 = (id)[treeAdaptor emptyTree];
        // 67:9: -> ^( 'for' $start expr $next block )
        {
            // SimpleC.g:67:12: ^( 'for' $start expr $next block )
            {
            id root_1 = (id)[treeAdaptor emptyTree];
            root_1 = (id)[treeAdaptor makeToken:(ANTLRToken *)[_list_SimpleCParser_FOR objectAtIndex:i_0] parentOf:root_1];

            [treeAdaptor addChild:[_start tree] toTree:root_1];
            [treeAdaptor addChild:(id<ANTLRTree>)[_list_expr objectAtIndex:i_0] toTree:root_1];
            [treeAdaptor addChild:[_next tree] toTree:root_1];
            [treeAdaptor addChild:(id<ANTLRTree>)[_list_block objectAtIndex:i_0] toTree:root_1];

            [treeAdaptor addChild:root_1 toTree:root_0];
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
		[_string_literal31 release];
		[_char_literal32 release];
		[_char_literal33 release];
		[_char_literal35 release];
		[_char_literal36 release];
		// token+rule list labels
		// rule labels
		[_start release];
		[_next release];
		[_expr34 release];
		[_block37 release];
		// rule refs in alts with rewrites
		[_list_expr release];
		[_list_assignStat release];
		[_list_block release];
		[_list_21 release];
		[_list_24 release];
		[_list_SimpleCParser_FOR release];
		[_list_22 release];
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end forStat

// $ANTLR start assignStat
// SimpleC.g:70:1: assignStat : ID EQ expr -> ^( EQ ID expr ) ;
- (SimpleCParser_assignStat_return *) assignStat
{
    SimpleCParser_assignStat_return * _retval = [[SimpleCParser_assignStat_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _ID38 = nil;
    ANTLRToken * _EQ39 = nil;
    // token+rule list labels
    // rule labels
    SimpleCParser_expr_return * _expr40 = nil;

    // rule list labels
    // rule refs in alts with rewrites
    NSMutableArray *_list_expr = [[NSMutableArray alloc] init];
    NSMutableArray *_list_SimpleCParser_EQ = [[NSMutableArray alloc] init];
    NSMutableArray *_list_SimpleCParser_ID = [[NSMutableArray alloc] init];
    id _ID38_tree = nil;
    id _EQ39_tree = nil;

    @try {
        // SimpleC.g:71:9: ( ID EQ expr -> ^( EQ ID expr ) ) // ruleBlockSingleAlt
        // SimpleC.g:71:9: ID EQ expr // alt
        {
        _ID38=(ANTLRToken *)[input LT:1];
        [_ID38 retain];
        [self match:input tokenType:SimpleCParser_ID follow:FOLLOW_SimpleCParser_ID_in_assignStat570]; 
        [_list_SimpleCParser_ID addObject:_ID38];

        _EQ39=(ANTLRToken *)[input LT:1];
        [_EQ39 retain];
        [self match:input tokenType:SimpleCParser_EQ follow:FOLLOW_SimpleCParser_EQ_in_assignStat572]; 
        [_list_SimpleCParser_EQ addObject:_EQ39];

        [following addObject:FOLLOW_expr_in_assignStat574];
        _expr40 = [self expr];
        [following removeLastObject];


        [_list_expr addObject:[_expr40 tree]];

        // AST REWRITE
        int i_0 = 0;
        [_retval setTree:root_0];
        root_0 = (id)[treeAdaptor emptyTree];
        // 71:20: -> ^( EQ ID expr )
        {
            // SimpleC.g:71:23: ^( EQ ID expr )
            {
            id root_1 = (id)[treeAdaptor emptyTree];
            root_1 = (id)[treeAdaptor makeToken:(ANTLRToken *)[_list_SimpleCParser_EQ objectAtIndex:i_0] parentOf:root_1];

            [treeAdaptor addTokenAsChild:(ANTLRToken *)[_list_SimpleCParser_ID objectAtIndex:i_0] toTree:root_1];
            [treeAdaptor addChild:(id<ANTLRTree>)[_list_expr objectAtIndex:i_0] toTree:root_1];

            [treeAdaptor addChild:root_1 toTree:root_0];
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
		[_ID38 release];
		[_EQ39 release];
		// token+rule list labels
		// rule labels
		[_expr40 release];
		// rule refs in alts with rewrites
		[_list_expr release];
		[_list_SimpleCParser_EQ release];
		[_list_SimpleCParser_ID release];
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end assignStat

// $ANTLR start expr
// SimpleC.g:74:1: expr : condExpr ;
- (SimpleCParser_expr_return *) expr
{
    SimpleCParser_expr_return * _retval = [[SimpleCParser_expr_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    // token+rule list labels
    // rule labels
    SimpleCParser_condExpr_return * _condExpr41 = nil;

    // rule list labels
    // rule refs in alts with rewrites


    @try {
        // SimpleC.g:74:9: ( condExpr ) // ruleBlockSingleAlt
        // SimpleC.g:74:9: condExpr // alt
        {
        root_0 = (id)[treeAdaptor emptyTree];

        [following addObject:FOLLOW_condExpr_in_expr598];
        _condExpr41 = [self condExpr];
        [following removeLastObject];


        [treeAdaptor addChild:[_condExpr41 tree] toTree:root_0];

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
		[_condExpr41 release];
		// rule refs in alts with rewrites
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end expr

// $ANTLR start condExpr
// SimpleC.g:77:1: condExpr : aexpr ( ( '=='^ | '<'^ ) aexpr )? ;
- (SimpleCParser_condExpr_return *) condExpr
{
    SimpleCParser_condExpr_return * _retval = [[SimpleCParser_condExpr_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _string_literal43 = nil;
    ANTLRToken * _char_literal44 = nil;
    // token+rule list labels
    // rule labels
    SimpleCParser_aexpr_return * _aexpr42 = nil;

    SimpleCParser_aexpr_return * _aexpr45 = nil;

    // rule list labels
    // rule refs in alts with rewrites

    id _string_literal43_tree = nil;
    id _char_literal44_tree = nil;

    @try {
        // SimpleC.g:78:9: ( aexpr ( ( '=='^ | '<'^ ) aexpr )? ) // ruleBlockSingleAlt
        // SimpleC.g:78:9: aexpr ( ( '=='^ | '<'^ ) aexpr )? // alt
        {
        root_0 = (id)[treeAdaptor emptyTree];

        [following addObject:FOLLOW_aexpr_in_condExpr617];
        _aexpr42 = [self aexpr];
        [following removeLastObject];


        [treeAdaptor addChild:[_aexpr42 tree] toTree:root_0];
        // SimpleC.g:78:15: ( ( '=='^ | '<'^ ) aexpr )? // block
        int alt9=2;
        {
        	int LA9_0 = [input LA:1];
        	if ( (LA9_0>=SimpleCParser_EQEQ && LA9_0<=SimpleCParser_LT) ) {
        		alt9 = 1;
        	}
        }
        switch (alt9) {
        	case 1 :
        	    // SimpleC.g:78:17: ( '=='^ | '<'^ ) aexpr // alt
        	    {
        	    // SimpleC.g:78:17: ( '=='^ | '<'^ ) // block
        	    int alt8=2;
        	    {
        	    	int LA8_0 = [input LA:1];
        	    	if ( LA8_0==SimpleCParser_EQEQ ) {
        	    		alt8 = 1;
        	    	}
        	    	else if ( LA8_0==SimpleCParser_LT ) {
        	    		alt8 = 2;
        	    	}
        	    else {
        	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:8 state:0 stream:input];
        	    	@throw nvae;
        	    	}
        	    }
        	    switch (alt8) {
        	    	case 1 :
        	    	    // SimpleC.g:78:18: '=='^ // alt
        	    	    {
        	    	    _string_literal43=(ANTLRToken *)[input LT:1];
        	    	    [_string_literal43 retain];
        	    	    [self match:input tokenType:SimpleCParser_EQEQ follow:FOLLOW_SimpleCParser_EQEQ_in_condExpr622]; 
        	    	    _string_literal43_tree = (id)[treeAdaptor newTreeWithToken:_string_literal43];
        	    	    root_0 = (id)[treeAdaptor makeNode:_string_literal43_tree parentOf:root_0];


        	    	    }
        	    	    break;
        	    	case 2 :
        	    	    // SimpleC.g:78:26: '<'^ // alt
        	    	    {
        	    	    _char_literal44=(ANTLRToken *)[input LT:1];
        	    	    [_char_literal44 retain];
        	    	    [self match:input tokenType:SimpleCParser_LT follow:FOLLOW_SimpleCParser_LT_in_condExpr627]; 
        	    	    _char_literal44_tree = (id)[treeAdaptor newTreeWithToken:_char_literal44];
        	    	    root_0 = (id)[treeAdaptor makeNode:_char_literal44_tree parentOf:root_0];


        	    	    }
        	    	    break;

        	    }

        	    [following addObject:FOLLOW_aexpr_in_condExpr631];
        	    _aexpr45 = [self aexpr];
        	    [following removeLastObject];


        	    [treeAdaptor addChild:[_aexpr45 tree] toTree:root_0];

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
		[_string_literal43 release];
		[_char_literal44 release];
		// token+rule list labels
		// rule labels
		[_aexpr42 release];
		[_aexpr45 release];
		// rule refs in alts with rewrites
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end condExpr

// $ANTLR start aexpr
// SimpleC.g:81:1: aexpr : atom ( '+'^ atom )* ;
- (SimpleCParser_aexpr_return *) aexpr
{
    SimpleCParser_aexpr_return * _retval = [[SimpleCParser_aexpr_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _char_literal47 = nil;
    // token+rule list labels
    // rule labels
    SimpleCParser_atom_return * _atom46 = nil;

    SimpleCParser_atom_return * _atom48 = nil;

    // rule list labels
    // rule refs in alts with rewrites

    id _char_literal47_tree = nil;

    @try {
        // SimpleC.g:82:9: ( atom ( '+'^ atom )* ) // ruleBlockSingleAlt
        // SimpleC.g:82:9: atom ( '+'^ atom )* // alt
        {
        root_0 = (id)[treeAdaptor emptyTree];

        [following addObject:FOLLOW_atom_in_aexpr653];
        _atom46 = [self atom];
        [following removeLastObject];


        [treeAdaptor addChild:[_atom46 tree] toTree:root_0];
        do {
            int alt10=2;
            {
            	int LA10_0 = [input LA:1];
            	if ( LA10_0==SimpleCParser_PLUS ) {
            		alt10 = 1;
            	}

            }
            switch (alt10) {
        	case 1 :
        	    // SimpleC.g:82:16: '+'^ atom // alt
        	    {
        	    _char_literal47=(ANTLRToken *)[input LT:1];
        	    [_char_literal47 retain];
        	    [self match:input tokenType:SimpleCParser_PLUS follow:FOLLOW_SimpleCParser_PLUS_in_aexpr657]; 
        	    _char_literal47_tree = (id)[treeAdaptor newTreeWithToken:_char_literal47];
        	    root_0 = (id)[treeAdaptor makeNode:_char_literal47_tree parentOf:root_0];

        	    [following addObject:FOLLOW_atom_in_aexpr660];
        	    _atom48 = [self atom];
        	    [following removeLastObject];


        	    [treeAdaptor addChild:[_atom48 tree] toTree:root_0];

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
		[_char_literal47 release];
		// token+rule list labels
		// rule labels
		[_atom46 release];
		[_atom48 release];
		// rule refs in alts with rewrites
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end aexpr

// $ANTLR start atom
// SimpleC.g:85:1: atom : ( ID | INT | '(' expr ')' -> expr );
- (SimpleCParser_atom_return *) atom
{
    SimpleCParser_atom_return * _retval = [[SimpleCParser_atom_return alloc] init]; 
    [_retval setStart:[input LT:1]];

    id root_0 = nil;

    // token labels
    ANTLRToken * _ID49 = nil;
    ANTLRToken * _INT50 = nil;
    ANTLRToken * _char_literal51 = nil;
    ANTLRToken * _char_literal53 = nil;
    // token+rule list labels
    // rule labels
    SimpleCParser_expr_return * _expr52 = nil;

    // rule list labels
    // rule refs in alts with rewrites
    NSMutableArray *_list_expr = [[NSMutableArray alloc] init];
    NSMutableArray *_list_24 = [[NSMutableArray alloc] init];
    NSMutableArray *_list_22 = [[NSMutableArray alloc] init];
    id _ID49_tree = nil;
    id _INT50_tree = nil;
    id _char_literal51_tree = nil;
    id _char_literal53_tree = nil;

    @try {
        // SimpleC.g:86:7: ( ID | INT | '(' expr ')' -> expr ) //ruleblock
        int alt11=3;
        switch ([input LA:1]) {
        	case SimpleCParser_ID:
        		alt11 = 1;
        		break;
        	case SimpleCParser_INT:
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
        	    // SimpleC.g:86:7: ID // alt
        	    {
        	    root_0 = (id)[treeAdaptor emptyTree];

        	    _ID49=(ANTLRToken *)[input LT:1];
        	    [_ID49 retain];
        	    [self match:input tokenType:SimpleCParser_ID follow:FOLLOW_SimpleCParser_ID_in_atom680]; 
        	    _ID49_tree = (id)[treeAdaptor newTreeWithToken:_ID49];
        	    [treeAdaptor addChild:_ID49_tree toTree:root_0];


        	    }
        	    break;
        	case 2 :
        	    // SimpleC.g:87:7: INT // alt
        	    {
        	    root_0 = (id)[treeAdaptor emptyTree];

        	    _INT50=(ANTLRToken *)[input LT:1];
        	    [_INT50 retain];
        	    [self match:input tokenType:SimpleCParser_INT follow:FOLLOW_SimpleCParser_INT_in_atom694]; 
        	    _INT50_tree = (id)[treeAdaptor newTreeWithToken:_INT50];
        	    [treeAdaptor addChild:_INT50_tree toTree:root_0];


        	    }
        	    break;
        	case 3 :
        	    // SimpleC.g:88:7: '(' expr ')' // alt
        	    {
        	    _char_literal51=(ANTLRToken *)[input LT:1];
        	    [_char_literal51 retain];
        	    [self match:input tokenType:22 follow:FOLLOW_22_in_atom708]; 
        	    [_list_22 addObject:_char_literal51];

        	    [following addObject:FOLLOW_expr_in_atom710];
        	    _expr52 = [self expr];
        	    [following removeLastObject];


        	    [_list_expr addObject:[_expr52 tree]];
        	    _char_literal53=(ANTLRToken *)[input LT:1];
        	    [_char_literal53 retain];
        	    [self match:input tokenType:24 follow:FOLLOW_24_in_atom712]; 
        	    [_list_24 addObject:_char_literal53];


        	    // AST REWRITE
        	    int i_0 = 0;
        	    [_retval setTree:root_0];
        	    root_0 = (id)[treeAdaptor emptyTree];
        	    // 88:20: -> expr
        	    {
        	        [treeAdaptor addChild:(id<ANTLRTree>)[_list_expr objectAtIndex:i_0] toTree:root_0];

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
		[_ID49 release];
		[_INT50 release];
		[_char_literal51 release];
		[_char_literal53 release];
		// token+rule list labels
		// rule labels
		[_expr52 release];
		// rule refs in alts with rewrites
		[_list_expr release];
		[_list_24 release];
		[_list_22 release];
		[_retval setStop:[input LT:-1]];

		    [_retval setTree:(id)[treeAdaptor postProcessTree:root_0]];
		    [treeAdaptor setBoundariesForTree:[_retval tree] fromToken:[_retval start] toToken:[_retval stop]];

	}
	return _retval;
}
// $ANTLR end atom


- (id<ANTLRTreeAdaptor>) treeAdaptor
{
	return treeAdaptor;
}

- (void) setTreeAdaptor:(id<ANTLRTreeAdaptor>)aTreeAdaptor
{
	if (aTreeAdaptor != treeAdaptor) {
		[aTreeAdaptor retain];
		[treeAdaptor release];
		treeAdaptor = aTreeAdaptor;
	}
}

@end