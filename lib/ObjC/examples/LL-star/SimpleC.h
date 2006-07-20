// $ANTLR 3.0b3 simplec.g 2006-07-20 00:49:52

#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>


#pragma mark Cyclic DFA start
@interface SimpleCDFA2 : ANTLRDFA {} @end

#pragma mark Cyclic DFA end

#pragma mark Tokens
#define SimpleC_INT	5
#define SimpleC_WS	6
#define SimpleC_EOF	-1
#define SimpleC_ID	4

@interface SimpleC : ANTLRParser {

	SimpleCDFA2 *dfa2;

 }


  - (void) program;
   - (void) declaration;
   - (void) variable;
   - (void) declarator;
   - (NSString*) functionHeader;
   - (void) formalParameter;
   - (void) type;
   - (void) block;
   - (void) stat;
   - (void) forStat;
   - (void) assignStat;
   - (void) expr;
   - (void) condExpr;
   - (void) aexpr;
   - (void) atom;
  


@end