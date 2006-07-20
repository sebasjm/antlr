// $ANTLR 3.0ea9 java.g 2006-05-08 00:43:11

#import <Cocoa/Cocoa.h>
#import <ANTLR/ANTLR.h>

#pragma mark Cyclic DFA start
// Cyclic DFA state subclasses
#pragma mark C y c l i c   D F A   S t a t e s


// Cyclic DFAs newstyle
#pragma mark C y c l i c   D F A

#pragma mark Cyclic DFA end

#pragma mark Tokens
#define FuzzyJava_Synpred1_fragment	23
#define FuzzyJava_QIDStar	5
#define FuzzyJava_TYPE	11
#define FuzzyJava_STAT	15
#define FuzzyJava_WS	4
#define FuzzyJava_CHAR	21
#define FuzzyJava_QID	9
#define FuzzyJava_STRING	20
#define FuzzyJava_METHOD	13
#define FuzzyJava_COMMENT	17
#define FuzzyJava_Synpred11_fragment	33
#define FuzzyJava_ESC	19
#define FuzzyJava_IMPORT	6
#define FuzzyJava_FIELD	14
#define FuzzyJava_Synpred12_fragment	34
#define FuzzyJava_Synpred6_fragment	28
#define FuzzyJava_CLASS	10
#define FuzzyJava_Synpred3_fragment	25
#define FuzzyJava_Synpred7_fragment	29
#define FuzzyJava_RETURN	7
#define FuzzyJava_Synpred8_fragment	30
#define FuzzyJava_Synpred9_fragment	31
#define FuzzyJava_Synpred2_fragment	24
#define FuzzyJava_ARG	12
#define FuzzyJava_Synpred5_fragment	27
#define FuzzyJava_EOF	-1
#define FuzzyJava_CALL	16
#define FuzzyJava_Tokens	22
#define FuzzyJava_Synpred10_fragment	32
#define FuzzyJava_SL_COMMENT	18
#define FuzzyJava_Synpred4_fragment	26
#define FuzzyJava_ID	8

@interface FuzzyJavaLexer : ANTLRLexer {
	SEL Synpred1SyntacticPredicate;
	SEL Synpred2SyntacticPredicate;
	SEL Synpred3SyntacticPredicate;
	SEL Synpred4SyntacticPredicate;
	SEL Synpred5SyntacticPredicate;
	SEL Synpred6SyntacticPredicate;
	SEL Synpred7SyntacticPredicate;
	SEL Synpred8SyntacticPredicate;
	SEL Synpred9SyntacticPredicate;
	SEL Synpred10SyntacticPredicate;
	SEL Synpred11SyntacticPredicate;
	SEL Synpred12SyntacticPredicate;

}

- (void) mIMPORT; // TODO: parameterScope
- (void) mRETURN; // TODO: parameterScope
- (void) mCLASS; // TODO: parameterScope
- (void) mMETHOD; // TODO: parameterScope
- (void) mFIELD; // TODO: parameterScope
- (void) mSTAT; // TODO: parameterScope
- (void) mCALL; // TODO: parameterScope
- (void) mCOMMENT; // TODO: parameterScope
- (void) mSL_COMMENT; // TODO: parameterScope
- (void) mSTRING; // TODO: parameterScope
- (void) mCHAR; // TODO: parameterScope
- (void) mWS; // TODO: parameterScope
- (void) mQID; // TODO: parameterScope
- (void) mQIDStar; // TODO: parameterScope
- (void) mTYPE; // TODO: parameterScope
- (void) mARG; // TODO: parameterScope
- (void) mID; // TODO: parameterScope
- (void) mESC; // TODO: parameterScope
- (void) mTokens; // TODO: parameterScope
- (void) mSynpred1_fragment; // TODO: parameterScope
- (void) mSynpred2_fragment; // TODO: parameterScope
- (void) mSynpred3_fragment; // TODO: parameterScope
- (void) mSynpred4_fragment; // TODO: parameterScope
- (void) mSynpred5_fragment; // TODO: parameterScope
- (void) mSynpred6_fragment; // TODO: parameterScope
- (void) mSynpred7_fragment; // TODO: parameterScope
- (void) mSynpred8_fragment; // TODO: parameterScope
- (void) mSynpred9_fragment; // TODO: parameterScope
- (void) mSynpred10_fragment; // TODO: parameterScope
- (void) mSynpred11_fragment; // TODO: parameterScope
- (void) mSynpred12_fragment; // TODO: parameterScope



@end