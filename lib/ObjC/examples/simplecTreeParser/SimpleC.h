// $ANTLR 3.0b4 simplec.g 2006-09-13 21:39:45

#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>



#pragma mark Cyclic DFA
@interface SimpleCDFA2 : ANTLRDFA {} @end


#pragma mark Tokens
#define SimpleC_FUNC_DEF	8
#define SimpleC_WS	20
#define SimpleC_CHAR	15
#define SimpleC_EQ	11
#define SimpleC_FUNC_HDR	6
#define SimpleC_LT	18
#define SimpleC_ARG_DEF	5
#define SimpleC_EQEQ	17
#define SimpleC_BLOCK	9
#define SimpleC_INT	12
#define SimpleC_EOF	-1
#define SimpleC_VOID	16
#define SimpleC_FOR	13
#define SimpleC_PLUS	19
#define SimpleC_FUNC_DECL	7
#define SimpleC_INT_TYPE	14
#define SimpleC_VAR_DEF	4
#define SimpleC_ID	10

#pragma mark Dynamic Scopes

#pragma mark Rule return scopes start
@interface SimpleCprogram_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCdeclaration_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCvariable_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCdeclarator_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCfunctionHeader_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCformalParameter_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCtype_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCblock_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCstat_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCforStat_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCassignStat_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCexpr_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCcondExpr_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCaexpr_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end
@interface SimpleCatom_return : ANTLRParserRuleReturnScope {
    id tree;
}
- (id) tree;
- (void) setTree:()aTree;
@end

#pragma mark Rule return scopes end


@interface SimpleC : ANTLRParser {

	SimpleCDFA2 *dfa2;

	Class adaptor;

 }


- (SimpleCprogram_return *) program;
- (SimpleCdeclaration_return *) declaration;
- (SimpleCvariable_return *) variable;
- (SimpleCdeclarator_return *) declarator;
- (SimpleCfunctionHeader_return *) functionHeader;
- (SimpleCformalParameter_return *) formalParameter;
- (SimpleCtype_return *) type;
- (SimpleCblock_return *) block;
- (SimpleCstat_return *) stat;
- (SimpleCforStat_return *) forStat;
- (SimpleCassignStat_return *) assignStat;
- (SimpleCexpr_return *) expr;
- (SimpleCcondExpr_return *) condExpr;
- (SimpleCaexpr_return *) aexpr;
- (SimpleCatom_return *) atom;


- (Class) adaptor;
- (void) setAdaptor:(Class)theAdaptor;

@end