// $ANTLR 3.0ea9 java.g 2006-05-08 00:43:11

#import "FuzzyJava.h"
#pragma mark Cyclic DFA start
#pragma mark Cyclic DFA end

@implementation FuzzyJavaLexer


- (id) initWithCharStream:(id<ANTLRCharStream>)anInput
{
	if (nil!=(self = [super initWithCharStream:anInput])) {
		// init memoize facility
		Synpred1SyntacticPredicate = @selector(mSynpred1_fragment);
		Synpred2SyntacticPredicate = @selector(mSynpred2_fragment);
		Synpred3SyntacticPredicate = @selector(mSynpred3_fragment);
		Synpred4SyntacticPredicate = @selector(mSynpred4_fragment);
		Synpred5SyntacticPredicate = @selector(mSynpred5_fragment);
		Synpred6SyntacticPredicate = @selector(mSynpred6_fragment);
		Synpred7SyntacticPredicate = @selector(mSynpred7_fragment);
		Synpred8SyntacticPredicate = @selector(mSynpred8_fragment);
		Synpred9SyntacticPredicate = @selector(mSynpred9_fragment);
		Synpred10SyntacticPredicate = @selector(mSynpred10_fragment);
		Synpred11SyntacticPredicate = @selector(mSynpred11_fragment);
		Synpred12SyntacticPredicate = @selector(mSynpred12_fragment);
	}
	return self;
}

- (ANTLRToken *) nextToken 
{
	[self setToken:nil];
    tokenStartCharIndex = [self charIndex];
    while (YES) {
        if ( [input LA:1] == ANTLRCharStreamEOF ) {
            return nil; // should really be a +eofToken call here -> go figure
        }
        @try {
            int m = [input mark];
            backtracking = 1;
            failed = NO;
            [self mTokens];
            backtracking = 0;
            [input rewind:m];
            if ( failed ) {
                [input consume]; 
            } else {
                [self mTokens];
                return token;
            }
        }
        @catch (ANTLRRecognitionException *re) {
            // shouldn't happen in backtracking mode, but...
            [self reportError:re];
            [self recover:re];
        }
    }
}
- (void) mIMPORT
{
    int type = FuzzyJava_IMPORT;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:5:17: ( 'import' WS name= QIDStar ( WS )? ';' ) // ruleBlockSingleAlt
    // java.g:5:17: 'import' WS name= QIDStar ( WS )? ';' // alt
    {
    [self matchString:@"import"];
    if (failed) return;

    [self mWS];
    if (failed) return;

    int nameStart = [self charIndex];
    [self mQIDStar];
    if (failed) return;

    ANTLRToken *name = [[ANTLRCommonToken alloc] initWithInput:input tokenType:ANTLRTokenTypeInvalid channel:ANTLRTokenChannelDefault start:nameStart stop:[self charIndex]];
    // java.g:5:42: ( WS )? // block
    int alt1=2;
    {
    	int LA1_0 = [input LA:1];
    	if ( (LA1_0>='\t' && LA1_0<='\n')
    	||LA1_0==' ' ) {
    		alt1 = 1;
    	}
    	else if ( LA1_0==';' ) {
    		alt1 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:1 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt1) {
    	case 1 :
    	    // java.g:5:42: WS // alt
    	    {
    	    [self mWS];
    	    if (failed) return;


    	    }
    	    break;

    }

    [self matchChar:';'];
    if (failed) return;


    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end IMPORT

- (void) mRETURN
{
    int type = FuzzyJava_RETURN;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:10:17: ( 'return' ( options {greedy=false; } : . )* ';' ) // ruleBlockSingleAlt
    // java.g:10:17: 'return' ( options {greedy=false; } : . )* ';' // alt
    {
    [self matchString:@"return"];
    if (failed) return;

    do {
        int alt2=2;
        int LA2_0 = [input LA:1];
        if ( LA2_0==';' ) {
        	alt2 = 2;
        }
        else if ( (LA2_0>=0x0000 && LA2_0<=':')
        ||(LA2_0>='<' && LA2_0<=0xFFFE)
         ) {
        	alt2 = 1;
        }


        switch (alt2) {
    	case 1 :
    	    // java.g:10:51: . // alt
    	    {
    	    [self matchAny];
    	    if (failed) return;


    	    }
    	    break;

    	default :
    	    goto loop2;
        }
    } while (true); loop2: ;

    [self matchChar:';'];
    if (failed) return;


    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end RETURN

- (void) mCLASS
{
    int type = FuzzyJava_CLASS;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:14:17: ( 'class' WS name= ID ( WS )? ( 'extends' WS QID ( WS )? )? ( 'implements' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? '{' ) // ruleBlockSingleAlt
    // java.g:14:17: 'class' WS name= ID ( WS )? ( 'extends' WS QID ( WS )? )? ( 'implements' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? '{' // alt
    {
    [self matchString:@"class"];
    if (failed) return;

    [self mWS];
    if (failed) return;

    int nameStart = [self charIndex];
    [self mID];
    if (failed) return;

    ANTLRToken *name = [[ANTLRCommonToken alloc] initWithInput:input tokenType:ANTLRTokenTypeInvalid channel:ANTLRTokenChannelDefault start:nameStart stop:[self charIndex]];
    // java.g:14:36: ( WS )? // block
    int alt3=2;
    {
    	int LA3_0 = [input LA:1];
    	if ( (LA3_0>='\t' && LA3_0<='\n')
    	||LA3_0==' ' ) {
    		alt3 = 1;
    	}
    	else if ( LA3_0=='e'||LA3_0=='i'||LA3_0=='{' ) {
    		alt3 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:3 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt3) {
    	case 1 :
    	    // java.g:14:36: WS // alt
    	    {
    	    [self mWS];
    	    if (failed) return;


    	    }
    	    break;

    }

    // java.g:14:40: ( 'extends' WS QID ( WS )? )? // block
    int alt5=2;
    {
    	int LA5_0 = [input LA:1];
    	if ( LA5_0=='e' ) {
    		alt5 = 1;
    	}
    	else if ( LA5_0=='i'||LA5_0=='{' ) {
    		alt5 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:5 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt5) {
    	case 1 :
    	    // java.g:14:41: 'extends' WS QID ( WS )? // alt
    	    {
    	    [self matchString:@"extends"];
    	    if (failed) return;

    	    [self mWS];
    	    if (failed) return;

    	    [self mQID];
    	    if (failed) return;

    	    // java.g:14:58: ( WS )? // block
    	    int alt4=2;
    	    {
    	    	int LA4_0 = [input LA:1];
    	    	if ( (LA4_0>='\t' && LA4_0<='\n')
    	    	||LA4_0==' ' ) {
    	    		alt4 = 1;
    	    	}
    	    	else if ( LA4_0=='i'||LA4_0=='{' ) {
    	    		alt4 = 2;
    	    	}
    	    else {
    	    	if (backtracking > 0) {
    	    		failed = YES;
    	    		return;
    	    	}
    	    #warning Think about exceptions!
    	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:4 state:0 stream:input];
    	    	@throw nvae;
    	    	}
    	    }
    	    switch (alt4) {
    	    	case 1 :
    	    	    // java.g:14:58: WS // alt
    	    	    {
    	    	    [self mWS];
    	    	    if (failed) return;


    	    	    }
    	    	    break;

    	    }


    	    }
    	    break;

    }

    // java.g:15:17: ( 'implements' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? // block
    int alt10=2;
    {
    	int LA10_0 = [input LA:1];
    	if ( LA10_0=='i' ) {
    		alt10 = 1;
    	}
    	else if ( LA10_0=='{' ) {
    		alt10 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:10 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt10) {
    	case 1 :
    	    // java.g:15:18: 'implements' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* // alt
    	    {
    	    [self matchString:@"implements"];
    	    if (failed) return;

    	    [self mWS];
    	    if (failed) return;

    	    [self mQID];
    	    if (failed) return;

    	    // java.g:15:38: ( WS )? // block
    	    int alt6=2;
    	    {
    	    	int LA6_0 = [input LA:1];
    	    	if ( (LA6_0>='\t' && LA6_0<='\n')
    	    	||LA6_0==' ' ) {
    	    		alt6 = 1;
    	    	}
    	    	else if ( LA6_0==','||LA6_0=='{' ) {
    	    		alt6 = 2;
    	    	}
    	    else {
    	    	if (backtracking > 0) {
    	    		failed = YES;
    	    		return;
    	    	}
    	    #warning Think about exceptions!
    	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:6 state:0 stream:input];
    	    	@throw nvae;
    	    	}
    	    }
    	    switch (alt6) {
    	    	case 1 :
    	    	    // java.g:15:38: WS // alt
    	    	    {
    	    	    [self mWS];
    	    	    if (failed) return;


    	    	    }
    	    	    break;

    	    }

    	    do {
    	        int alt9=2;
    	        int LA9_0 = [input LA:1];
    	        if ( LA9_0==',' ) {
    	        	alt9 = 1;
    	        }


    	        switch (alt9) {
    	    	case 1 :
    	    	    // java.g:15:43: ',' ( WS )? QID ( WS )? // alt
    	    	    {
    	    	    [self matchChar:','];
    	    	    if (failed) return;

    	    	    // java.g:15:47: ( WS )? // block
    	    	    int alt7=2;
    	    	    {
    	    	    	int LA7_0 = [input LA:1];
    	    	    	if ( (LA7_0>='\t' && LA7_0<='\n')
    	    	    	||LA7_0==' ' ) {
    	    	    		alt7 = 1;
    	    	    	}
    	    	    	else if ( (LA7_0>='A' && LA7_0<='Z')
    	    	    	||LA7_0=='_'||(LA7_0>='a' && LA7_0<='z')
    	    	    	 ) {
    	    	    		alt7 = 2;
    	    	    	}
    	    	    else {
    	    	    	if (backtracking > 0) {
    	    	    		failed = YES;
    	    	    		return;
    	    	    	}
    	    	    #warning Think about exceptions!
    	    	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:7 state:0 stream:input];
    	    	    	@throw nvae;
    	    	    	}
    	    	    }
    	    	    switch (alt7) {
    	    	    	case 1 :
    	    	    	    // java.g:15:47: WS // alt
    	    	    	    {
    	    	    	    [self mWS];
    	    	    	    if (failed) return;


    	    	    	    }
    	    	    	    break;

    	    	    }

    	    	    [self mQID];
    	    	    if (failed) return;

    	    	    // java.g:15:55: ( WS )? // block
    	    	    int alt8=2;
    	    	    {
    	    	    	int LA8_0 = [input LA:1];
    	    	    	if ( (LA8_0>='\t' && LA8_0<='\n')
    	    	    	||LA8_0==' ' ) {
    	    	    		alt8 = 1;
    	    	    	}
    	    	    	else if ( LA8_0==','||LA8_0=='{' ) {
    	    	    		alt8 = 2;
    	    	    	}
    	    	    else {
    	    	    	if (backtracking > 0) {
    	    	    		failed = YES;
    	    	    		return;
    	    	    	}
    	    	    #warning Think about exceptions!
    	    	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:8 state:0 stream:input];
    	    	    	@throw nvae;
    	    	    	}
    	    	    }
    	    	    switch (alt8) {
    	    	    	case 1 :
    	    	    	    // java.g:15:55: WS // alt
    	    	    	    {
    	    	    	    [self mWS];
    	    	    	    if (failed) return;


    	    	    	    }
    	    	    	    break;

    	    	    }


    	    	    }
    	    	    break;

    	    	default :
    	    	    goto loop9;
    	        }
    	    } while (true); loop9: ;


    	    }
    	    break;

    }

    [self matchChar:'{'];
    if (failed) return;

    if ( backtracking==0 ) {
      NSLog(@"found class %@", [name text]);
    }

    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end CLASS

- (void) mMETHOD
{
    int type = FuzzyJava_METHOD;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:20:9: ( TYPE WS name= ID ( WS )? '(' ( ARG ( WS )? ( ',' ( WS )? ARG ( WS )? )* )? ')' ( WS )? ( 'throws' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? '{' ) // ruleBlockSingleAlt
    // java.g:20:9: TYPE WS name= ID ( WS )? '(' ( ARG ( WS )? ( ',' ( WS )? ARG ( WS )? )* )? ')' ( WS )? ( 'throws' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? '{' // alt
    {
    [self mTYPE];
    if (failed) return;

    [self mWS];
    if (failed) return;

    int nameStart = [self charIndex];
    [self mID];
    if (failed) return;

    ANTLRToken *name = [[ANTLRCommonToken alloc] initWithInput:input tokenType:ANTLRTokenTypeInvalid channel:ANTLRTokenChannelDefault start:nameStart stop:[self charIndex]];
    // java.g:20:25: ( WS )? // block
    int alt11=2;
    {
    	int LA11_0 = [input LA:1];
    	if ( (LA11_0>='\t' && LA11_0<='\n')
    	||LA11_0==' ' ) {
    		alt11 = 1;
    	}
    	else if ( LA11_0=='(' ) {
    		alt11 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:11 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt11) {
    	case 1 :
    	    // java.g:20:25: WS // alt
    	    {
    	    [self mWS];
    	    if (failed) return;


    	    }
    	    break;

    }

    [self matchChar:'('];
    if (failed) return;

    // java.g:20:33: ( ARG ( WS )? ( ',' ( WS )? ARG ( WS )? )* )? // block
    int alt16=2;
    {
    	int LA16_0 = [input LA:1];
    	if ( (LA16_0>='A' && LA16_0<='Z')
    	||LA16_0=='_'||(LA16_0>='a' && LA16_0<='z')
    	 ) {
    		alt16 = 1;
    	}
    	else if ( LA16_0==')' ) {
    		alt16 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:16 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt16) {
    	case 1 :
    	    // java.g:20:35: ARG ( WS )? ( ',' ( WS )? ARG ( WS )? )* // alt
    	    {
    	    [self mARG];
    	    if (failed) return;

    	    // java.g:20:39: ( WS )? // block
    	    int alt12=2;
    	    {
    	    	int LA12_0 = [input LA:1];
    	    	if ( (LA12_0>='\t' && LA12_0<='\n')
    	    	||LA12_0==' ' ) {
    	    		alt12 = 1;
    	    	}
    	    	else if ( LA12_0==')'||LA12_0==',' ) {
    	    		alt12 = 2;
    	    	}
    	    else {
    	    	if (backtracking > 0) {
    	    		failed = YES;
    	    		return;
    	    	}
    	    #warning Think about exceptions!
    	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:12 state:0 stream:input];
    	    	@throw nvae;
    	    	}
    	    }
    	    switch (alt12) {
    	    	case 1 :
    	    	    // java.g:20:39: WS // alt
    	    	    {
    	    	    [self mWS];
    	    	    if (failed) return;


    	    	    }
    	    	    break;

    	    }

    	    do {
    	        int alt15=2;
    	        int LA15_0 = [input LA:1];
    	        if ( LA15_0==',' ) {
    	        	alt15 = 1;
    	        }


    	        switch (alt15) {
    	    	case 1 :
    	    	    // java.g:20:44: ',' ( WS )? ARG ( WS )? // alt
    	    	    {
    	    	    [self matchChar:','];
    	    	    if (failed) return;

    	    	    // java.g:20:48: ( WS )? // block
    	    	    int alt13=2;
    	    	    {
    	    	    	int LA13_0 = [input LA:1];
    	    	    	if ( (LA13_0>='\t' && LA13_0<='\n')
    	    	    	||LA13_0==' ' ) {
    	    	    		alt13 = 1;
    	    	    	}
    	    	    	else if ( (LA13_0>='A' && LA13_0<='Z')
    	    	    	||LA13_0=='_'||(LA13_0>='a' && LA13_0<='z')
    	    	    	 ) {
    	    	    		alt13 = 2;
    	    	    	}
    	    	    else {
    	    	    	if (backtracking > 0) {
    	    	    		failed = YES;
    	    	    		return;
    	    	    	}
    	    	    #warning Think about exceptions!
    	    	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:13 state:0 stream:input];
    	    	    	@throw nvae;
    	    	    	}
    	    	    }
    	    	    switch (alt13) {
    	    	    	case 1 :
    	    	    	    // java.g:20:48: WS // alt
    	    	    	    {
    	    	    	    [self mWS];
    	    	    	    if (failed) return;


    	    	    	    }
    	    	    	    break;

    	    	    }

    	    	    [self mARG];
    	    	    if (failed) return;

    	    	    // java.g:20:56: ( WS )? // block
    	    	    int alt14=2;
    	    	    {
    	    	    	int LA14_0 = [input LA:1];
    	    	    	if ( (LA14_0>='\t' && LA14_0<='\n')
    	    	    	||LA14_0==' ' ) {
    	    	    		alt14 = 1;
    	    	    	}
    	    	    	else if ( LA14_0==')'||LA14_0==',' ) {
    	    	    		alt14 = 2;
    	    	    	}
    	    	    else {
    	    	    	if (backtracking > 0) {
    	    	    		failed = YES;
    	    	    		return;
    	    	    	}
    	    	    #warning Think about exceptions!
    	    	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:14 state:0 stream:input];
    	    	    	@throw nvae;
    	    	    	}
    	    	    }
    	    	    switch (alt14) {
    	    	    	case 1 :
    	    	    	    // java.g:20:56: WS // alt
    	    	    	    {
    	    	    	    [self mWS];
    	    	    	    if (failed) return;


    	    	    	    }
    	    	    	    break;

    	    	    }


    	    	    }
    	    	    break;

    	    	default :
    	    	    goto loop15;
    	        }
    	    } while (true); loop15: ;


    	    }
    	    break;

    }

    [self matchChar:')'];
    if (failed) return;

    // java.g:20:69: ( WS )? // block
    int alt17=2;
    {
    	int LA17_0 = [input LA:1];
    	if ( (LA17_0>='\t' && LA17_0<='\n')
    	||LA17_0==' ' ) {
    		alt17 = 1;
    	}
    	else if ( LA17_0=='t'||LA17_0=='{' ) {
    		alt17 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:17 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt17) {
    	case 1 :
    	    // java.g:20:69: WS // alt
    	    {
    	    [self mWS];
    	    if (failed) return;


    	    }
    	    break;

    }

    // java.g:21:8: ( 'throws' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? // block
    int alt22=2;
    {
    	int LA22_0 = [input LA:1];
    	if ( LA22_0=='t' ) {
    		alt22 = 1;
    	}
    	else if ( LA22_0=='{' ) {
    		alt22 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:22 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt22) {
    	case 1 :
    	    // java.g:21:9: 'throws' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* // alt
    	    {
    	    [self matchString:@"throws"];
    	    if (failed) return;

    	    [self mWS];
    	    if (failed) return;

    	    [self mQID];
    	    if (failed) return;

    	    // java.g:21:25: ( WS )? // block
    	    int alt18=2;
    	    {
    	    	int LA18_0 = [input LA:1];
    	    	if ( (LA18_0>='\t' && LA18_0<='\n')
    	    	||LA18_0==' ' ) {
    	    		alt18 = 1;
    	    	}
    	    	else if ( LA18_0==','||LA18_0=='{' ) {
    	    		alt18 = 2;
    	    	}
    	    else {
    	    	if (backtracking > 0) {
    	    		failed = YES;
    	    		return;
    	    	}
    	    #warning Think about exceptions!
    	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:18 state:0 stream:input];
    	    	@throw nvae;
    	    	}
    	    }
    	    switch (alt18) {
    	    	case 1 :
    	    	    // java.g:21:25: WS // alt
    	    	    {
    	    	    [self mWS];
    	    	    if (failed) return;


    	    	    }
    	    	    break;

    	    }

    	    do {
    	        int alt21=2;
    	        int LA21_0 = [input LA:1];
    	        if ( LA21_0==',' ) {
    	        	alt21 = 1;
    	        }


    	        switch (alt21) {
    	    	case 1 :
    	    	    // java.g:21:30: ',' ( WS )? QID ( WS )? // alt
    	    	    {
    	    	    [self matchChar:','];
    	    	    if (failed) return;

    	    	    // java.g:21:34: ( WS )? // block
    	    	    int alt19=2;
    	    	    {
    	    	    	int LA19_0 = [input LA:1];
    	    	    	if ( (LA19_0>='\t' && LA19_0<='\n')
    	    	    	||LA19_0==' ' ) {
    	    	    		alt19 = 1;
    	    	    	}
    	    	    	else if ( (LA19_0>='A' && LA19_0<='Z')
    	    	    	||LA19_0=='_'||(LA19_0>='a' && LA19_0<='z')
    	    	    	 ) {
    	    	    		alt19 = 2;
    	    	    	}
    	    	    else {
    	    	    	if (backtracking > 0) {
    	    	    		failed = YES;
    	    	    		return;
    	    	    	}
    	    	    #warning Think about exceptions!
    	    	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:19 state:0 stream:input];
    	    	    	@throw nvae;
    	    	    	}
    	    	    }
    	    	    switch (alt19) {
    	    	    	case 1 :
    	    	    	    // java.g:21:34: WS // alt
    	    	    	    {
    	    	    	    [self mWS];
    	    	    	    if (failed) return;


    	    	    	    }
    	    	    	    break;

    	    	    }

    	    	    [self mQID];
    	    	    if (failed) return;

    	    	    // java.g:21:42: ( WS )? // block
    	    	    int alt20=2;
    	    	    {
    	    	    	int LA20_0 = [input LA:1];
    	    	    	if ( (LA20_0>='\t' && LA20_0<='\n')
    	    	    	||LA20_0==' ' ) {
    	    	    		alt20 = 1;
    	    	    	}
    	    	    	else if ( LA20_0==','||LA20_0=='{' ) {
    	    	    		alt20 = 2;
    	    	    	}
    	    	    else {
    	    	    	if (backtracking > 0) {
    	    	    		failed = YES;
    	    	    		return;
    	    	    	}
    	    	    #warning Think about exceptions!
    	    	        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:20 state:0 stream:input];
    	    	    	@throw nvae;
    	    	    	}
    	    	    }
    	    	    switch (alt20) {
    	    	    	case 1 :
    	    	    	    // java.g:21:42: WS // alt
    	    	    	    {
    	    	    	    [self mWS];
    	    	    	    if (failed) return;


    	    	    	    }
    	    	    	    break;

    	    	    }


    	    	    }
    	    	    break;

    	    	default :
    	    	    goto loop21;
    	        }
    	    } while (true); loop21: ;


    	    }
    	    break;

    }

    [self matchChar:'{'];
    if (failed) return;

    if ( backtracking==0 ) {
      NSLog(@"found method %@", [name text]);
    }

    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end METHOD

- (void) mFIELD
{
    int type = FuzzyJava_FIELD;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:26:9: ( TYPE WS name= ID ( '[]' )? ( WS )? (';'|'=')) // ruleBlockSingleAlt
    // java.g:26:9: TYPE WS name= ID ( '[]' )? ( WS )? (';'|'=') // alt
    {
    [self mTYPE];
    if (failed) return;

    [self mWS];
    if (failed) return;

    int nameStart = [self charIndex];
    [self mID];
    if (failed) return;

    ANTLRToken *name = [[ANTLRCommonToken alloc] initWithInput:input tokenType:ANTLRTokenTypeInvalid channel:ANTLRTokenChannelDefault start:nameStart stop:[self charIndex]];
    // java.g:26:25: ( '[]' )? // block
    int alt23=2;
    {
    	int LA23_0 = [input LA:1];
    	if ( LA23_0=='[' ) {
    		alt23 = 1;
    	}
    	else if ( (LA23_0>='\t' && LA23_0<='\n')
    	||LA23_0==' '||LA23_0==';'||LA23_0=='=' ) {
    		alt23 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:23 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt23) {
    	case 1 :
    	    // java.g:26:25: '[]' // alt
    	    {
    	    [self matchString:@"[]"];
    	    if (failed) return;


    	    }
    	    break;

    }

    // java.g:26:31: ( WS )? // block
    int alt24=2;
    {
    	int LA24_0 = [input LA:1];
    	if ( (LA24_0>='\t' && LA24_0<='\n')
    	||LA24_0==' ' ) {
    		alt24 = 1;
    	}
    	else if ( LA24_0==';'||LA24_0=='=' ) {
    		alt24 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:24 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt24) {
    	case 1 :
    	    // java.g:26:31: WS // alt
    	    {
    	    [self mWS];
    	    if (failed) return;


    	    }
    	    break;

    }

    if ([input LA:1]==';'||[input LA:1]=='=') {
    	[input consume];
    	errorRecovery = NO;
    failed = NO;
    } else {
    if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
    	[self recover:mse];	@throw mse;
    }

    if ( backtracking==0 ) {
      NSLog(@"found var %@", [name text]);
    }

    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end FIELD

- (void) mSTAT
{
    int type = FuzzyJava_STAT;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:30:9: ( ( 'if' | 'while' | 'switch' | 'for' ) ( WS )? '(' ) // ruleBlockSingleAlt
    // java.g:30:9: ( 'if' | 'while' | 'switch' | 'for' ) ( WS )? '(' // alt
    {
    // java.g:30:9: ( 'if' | 'while' | 'switch' | 'for' ) // block
    int alt25=4;
    switch ([input LA:1]) {
    	case 'i':
    		alt25 = 1;
    		break;
    	case 'w':
    		alt25 = 2;
    		break;
    	case 's':
    		alt25 = 3;
    		break;
    	case 'f':
    		alt25 = 4;
    		break;
    default:
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:25 state:0 stream:input];
    	@throw nvae;
    }
    switch (alt25) {
    	case 1 :
    	    // java.g:30:10: 'if' // alt
    	    {
    	    [self matchString:@"if"];
    	    if (failed) return;


    	    }
    	    break;
    	case 2 :
    	    // java.g:30:15: 'while' // alt
    	    {
    	    [self matchString:@"while"];
    	    if (failed) return;


    	    }
    	    break;
    	case 3 :
    	    // java.g:30:23: 'switch' // alt
    	    {
    	    [self matchString:@"switch"];
    	    if (failed) return;


    	    }
    	    break;
    	case 4 :
    	    // java.g:30:32: 'for' // alt
    	    {
    	    [self matchString:@"for"];
    	    if (failed) return;


    	    }
    	    break;

    }

    // java.g:30:39: ( WS )? // block
    int alt26=2;
    {
    	int LA26_0 = [input LA:1];
    	if ( (LA26_0>='\t' && LA26_0<='\n')
    	||LA26_0==' ' ) {
    		alt26 = 1;
    	}
    	else if ( LA26_0=='(' ) {
    		alt26 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:26 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt26) {
    	case 1 :
    	    // java.g:30:39: WS // alt
    	    {
    	    [self mWS];
    	    if (failed) return;


    	    }
    	    break;

    }

    [self matchChar:'('];
    if (failed) return;


    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end STAT

- (void) mCALL
{
    int type = FuzzyJava_CALL;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:33:9: (name= QID ( WS )? '(' ) // ruleBlockSingleAlt
    // java.g:33:9: name= QID ( WS )? '(' // alt
    {
    int nameStart = [self charIndex];
    [self mQID];
    if (failed) return;

    ANTLRToken *name = [[ANTLRCommonToken alloc] initWithInput:input tokenType:ANTLRTokenTypeInvalid channel:ANTLRTokenChannelDefault start:nameStart stop:[self charIndex]];
    // java.g:33:18: ( WS )? // block
    int alt27=2;
    {
    	int LA27_0 = [input LA:1];
    	if ( (LA27_0>='\t' && LA27_0<='\n')
    	||LA27_0==' ' ) {
    		alt27 = 1;
    	}
    	else if ( LA27_0=='(' ) {
    		alt27 = 2;
    	}
    else {
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:27 state:0 stream:input];
    	@throw nvae;
    	}
    }
    switch (alt27) {
    	case 1 :
    	    // java.g:33:18: WS // alt
    	    {
    	    [self mWS];
    	    if (failed) return;


    	    }
    	    break;

    }

    [self matchChar:'('];
    if (failed) return;

    if ( backtracking==0 ) {
      /*ignore if this/super */ NSLog(@"found call %@",[name text]);
    }

    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end CALL

- (void) mCOMMENT
{
    int type = FuzzyJava_COMMENT;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:38:9: ( '/*' ( options {greedy=false; } : . )* '*/' ) // ruleBlockSingleAlt
    // java.g:38:9: '/*' ( options {greedy=false; } : . )* '*/' // alt
    {
    [self matchString:@"/*"];
    if (failed) return;

    do {
        int alt28=2;
        int LA28_0 = [input LA:1];
        if ( LA28_0=='*' ) {
        	int LA28_1 = [input LA:2];
        	if ( LA28_1=='/' ) {
        		alt28 = 2;
        	}
        	else if ( (LA28_1>=0x0000 && LA28_1<='.')
        	||(LA28_1>='0' && LA28_1<=0xFFFE)
        	 ) {
        		alt28 = 1;
        	}


        }
        else if ( (LA28_0>=0x0000 && LA28_0<=')')
        ||(LA28_0>='+' && LA28_0<=0xFFFE)
         ) {
        	alt28 = 1;
        }


        switch (alt28) {
    	case 1 :
    	    // java.g:38:41: . // alt
    	    {
    	    [self matchAny];
    	    if (failed) return;


    	    }
    	    break;

    	default :
    	    goto loop28;
        }
    } while (true); loop28: ;

    [self matchString:@"*/"];
    if (failed) return;

    if ( backtracking==0 ) {
      NSLog(@"found comment %@", [self text]);
    }

    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end COMMENT

- (void) mSL_COMMENT
{
    int type = FuzzyJava_SL_COMMENT;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:43:9: ( '//' ( options {greedy=false; } : . )* '\n' ) // ruleBlockSingleAlt
    // java.g:43:9: '//' ( options {greedy=false; } : . )* '\n' // alt
    {
    [self matchString:@"//"];
    if (failed) return;

    do {
        int alt29=2;
        int LA29_0 = [input LA:1];
        if ( LA29_0=='\n' ) {
        	alt29 = 2;
        }
        else if ( (LA29_0>=0x0000 && LA29_0<='\t')
        ||(LA29_0>=0x000B && LA29_0<=0xFFFE)
         ) {
        	alt29 = 1;
        }


        switch (alt29) {
    	case 1 :
    	    // java.g:43:41: . // alt
    	    {
    	    [self matchAny];
    	    if (failed) return;


    	    }
    	    break;

    	default :
    	    goto loop29;
        }
    } while (true); loop29: ;

    [self matchChar:'\n'];
    if (failed) return;

    if ( backtracking==0 ) {
      NSLog(@"found // comment %@", [self text]);
    }

    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end SL_COMMENT

- (void) mSTRING
{
    int type = FuzzyJava_STRING;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:48:17: ( '"' ( options {greedy=false; } : ESC | . )* '"' ) // ruleBlockSingleAlt
    // java.g:48:17: '"' ( options {greedy=false; } : ESC | . )* '"' // alt
    {
    [self matchChar:'"'];
    if (failed) return;

    do {
        int alt30=3;
        int LA30_0 = [input LA:1];
        if ( LA30_0=='"' ) {
        	alt30 = 3;
        }
        else if ( LA30_0=='\\' ) {
        	int LA30_2 = [input LA:2];
        	if ( LA30_2=='"' ) {
        		alt30 = 1;
        	}
        	else if ( LA30_2=='\\' ) {
        		alt30 = 1;
        	}
        	else if ( LA30_2=='\'' ) {
        		alt30 = 1;
        	}
        	else if ( (LA30_2>=0x0000 && LA30_2<='!')
        	||(LA30_2>='#' && LA30_2<='&')
        	||(LA30_2>='(' && LA30_2<='[')
        	||(LA30_2>=']' && LA30_2<=0xFFFE)
        	 ) {
        		alt30 = 2;
        	}


        }
        else if ( (LA30_0>=0x0000 && LA30_0<='!')
        ||(LA30_0>='#' && LA30_0<='[')
        ||(LA30_0>=']' && LA30_0<=0xFFFE)
         ) {
        	alt30 = 2;
        }


        switch (alt30) {
    	case 1 :
    	    // java.g:48:47: ESC // alt
    	    {
    	    [self mESC];
    	    if (failed) return;


    	    }
    	    break;
    	case 2 :
    	    // java.g:48:53: . // alt
    	    {
    	    [self matchAny];
    	    if (failed) return;


    	    }
    	    break;

    	default :
    	    goto loop30;
        }
    } while (true); loop30: ;

    [self matchChar:'"'];
    if (failed) return;


    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end STRING

- (void) mCHAR
{
    int type = FuzzyJava_CHAR;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:52:17: ( '\'' ( options {greedy=false; } : ESC | . )* '\'' ) // ruleBlockSingleAlt
    // java.g:52:17: '\'' ( options {greedy=false; } : ESC | . )* '\'' // alt
    {
    [self matchChar:'\''];
    if (failed) return;

    do {
        int alt31=3;
        int LA31_0 = [input LA:1];
        if ( LA31_0=='\'' ) {
        	alt31 = 3;
        }
        else if ( LA31_0=='\\' ) {
        	int LA31_2 = [input LA:2];
        	if ( LA31_2=='\'' ) {
        		alt31 = 1;
        	}
        	else if ( LA31_2=='\\' ) {
        		alt31 = 1;
        	}
        	else if ( LA31_2=='"' ) {
        		alt31 = 1;
        	}
        	else if ( (LA31_2>=0x0000 && LA31_2<='!')
        	||(LA31_2>='#' && LA31_2<='&')
        	||(LA31_2>='(' && LA31_2<='[')
        	||(LA31_2>=']' && LA31_2<=0xFFFE)
        	 ) {
        		alt31 = 2;
        	}


        }
        else if ( (LA31_0>=0x0000 && LA31_0<='&')
        ||(LA31_0>='(' && LA31_0<='[')
        ||(LA31_0>=']' && LA31_0<=0xFFFE)
         ) {
        	alt31 = 2;
        }


        switch (alt31) {
    	case 1 :
    	    // java.g:52:48: ESC // alt
    	    {
    	    [self mESC];
    	    if (failed) return;


    	    }
    	    break;
    	case 2 :
    	    // java.g:52:54: . // alt
    	    {
    	    [self matchAny];
    	    if (failed) return;


    	    }
    	    break;

    	default :
    	    goto loop31;
        }
    } while (true); loop31: ;

    [self matchChar:'\''];
    if (failed) return;


    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end CHAR

- (void) mWS
{
    int type = FuzzyJava_WS;
    int start = [self charIndex];
    int line = [self line];
    int charPosition = [self charPositionInLine];
    int channel = [ANTLRToken defaultChannel];
    // java.g:55:9: ( ( (' '|'\t'|'\n'))+ ) // ruleBlockSingleAlt
    // java.g:55:9: ( (' '|'\t'|'\n'))+ // alt
    {
    // java.g:55:9: ( (' '|'\t'|'\n'))+	// positiveClosureBlock
    int cnt32=0;

    do {
        int alt32=2;
        int LA32_0 = [input LA:1];
        if ( (LA32_0>='\t' && LA32_0<='\n')
        ||LA32_0==' ' ) {
        	alt32 = 1;
        }


        switch (alt32) {
    	case 1 :
    	    // java.g:55:10: (' '|'\t'|'\n') // alt
    	    {
    	    if (([input LA:1]>='\t' && [input LA:1]<='\n')||[input LA:1]==' ') {
    	    	[input consume];
    	    	errorRecovery = NO;
    	    failed = NO;
    	    } else {
    	    if (backtracking > 0) {
    	    		failed = YES;
    	    		return;
    	    	}
    	    #warning Think about exceptions!
    	    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
    	    	[self recover:mse];	@throw mse;
    	    }


    	    }
    	    break;

    	default :
    	    if ( cnt32 >= 1 )  goto loop32;
                if (backtracking>0) { failed = YES; return; }
    #warning Not thought about exceptions, yet!
    //            EarlyExitException eee =
    //                new EarlyExitException(32, input);
    //            throw eee;
        }
        cnt32++;
    } while (true); loop32: ;


    }

    if ( token == nil ) { [self emitTokenWithType:type line:line charPosition:charPosition channel:channel start:start stop:[self charIndex]];}
}
// $ANTLR end WS

- (void) mQID
{
    // java.g:59:9: ( ID ( '.' ID )* ) // ruleBlockSingleAlt
    // java.g:59:9: ID ( '.' ID )* // alt
    {
    [self mID];
    if (failed) return;

    do {
        int alt33=2;
        int LA33_0 = [input LA:1];
        if ( LA33_0=='.' ) {
        	alt33 = 1;
        }


        switch (alt33) {
    	case 1 :
    	    // java.g:59:13: '.' ID // alt
    	    {
    	    [self matchChar:'.'];
    	    if (failed) return;

    	    [self mID];
    	    if (failed) return;


    	    }
    	    break;

    	default :
    	    goto loop33;
        }
    } while (true); loop33: ;


    }

}
// $ANTLR end QID

- (void) mQIDStar
{
    // java.g:68:17: ( ID ( '.' ID )* ( '.*' )? ) // ruleBlockSingleAlt
    // java.g:68:17: ID ( '.' ID )* ( '.*' )? // alt
    {
    [self mID];
    if (failed) return;

    do {
        int alt34=2;
        int LA34_0 = [input LA:1];
        if ( LA34_0=='.' ) {
        	int LA34_1 = [input LA:2];
        	if ( (LA34_1>='A' && LA34_1<='Z')
        	||LA34_1=='_'||(LA34_1>='a' && LA34_1<='z')
        	 ) {
        		alt34 = 1;
        	}


        }


        switch (alt34) {
    	case 1 :
    	    // java.g:68:21: '.' ID // alt
    	    {
    	    [self matchChar:'.'];
    	    if (failed) return;

    	    [self mID];
    	    if (failed) return;


    	    }
    	    break;

    	default :
    	    goto loop34;
        }
    } while (true); loop34: ;

    // java.g:68:30: ( '.*' )? // block
    int alt35=2;
    {
    	int LA35_0 = [input LA:1];
    	if ( LA35_0=='.' ) {
    		alt35 = 1;
    	}
    else {
    	alt35 = 2;	}
    }
    switch (alt35) {
    	case 1 :
    	    // java.g:68:30: '.*' // alt
    	    {
    	    [self matchString:@".*"];
    	    if (failed) return;


    	    }
    	    break;

    }


    }

}
// $ANTLR end QIDStar

- (void) mTYPE
{
    // java.g:72:9: ( QID ( '[]' )? ) // ruleBlockSingleAlt
    // java.g:72:9: QID ( '[]' )? // alt
    {
    [self mQID];
    if (failed) return;

    // java.g:72:13: ( '[]' )? // block
    int alt36=2;
    {
    	int LA36_0 = [input LA:1];
    	if ( LA36_0=='[' ) {
    		alt36 = 1;
    	}
    else {
    	alt36 = 2;	}
    }
    switch (alt36) {
    	case 1 :
    	    // java.g:72:13: '[]' // alt
    	    {
    	    [self matchString:@"[]"];
    	    if (failed) return;


    	    }
    	    break;

    }


    }

}
// $ANTLR end TYPE

- (void) mARG
{
    // java.g:76:9: ( TYPE WS ID ) // ruleBlockSingleAlt
    // java.g:76:9: TYPE WS ID // alt
    {
    [self mTYPE];
    if (failed) return;

    [self mWS];
    if (failed) return;

    [self mID];
    if (failed) return;


    }

}
// $ANTLR end ARG

- (void) mID
{
    // java.g:80:9: ( ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))* ) // ruleBlockSingleAlt
    // java.g:80:9: ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))* // alt
    {
    if (([input LA:1]>='A' && [input LA:1]<='Z')||[input LA:1]=='_'||([input LA:1]>='a' && [input LA:1]<='z')) {
    	[input consume];
    	errorRecovery = NO;
    failed = NO;
    } else {
    if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
    	[self recover:mse];	@throw mse;
    }

    do {
        int alt37=2;
        int LA37_0 = [input LA:1];
        if ( (LA37_0>='0' && LA37_0<='9')
        ||(LA37_0>='A' && LA37_0<='Z')
        ||LA37_0=='_'||(LA37_0>='a' && LA37_0<='z')
         ) {
        	alt37 = 1;
        }


        switch (alt37) {
    	case 1 :
    	    // java.g:80:34: ('a'..'z'|'A'..'Z'|'_'|'0'..'9') // alt
    	    {
    	    if (([input LA:1]>='0' && [input LA:1]<='9')||([input LA:1]>='A' && [input LA:1]<='Z')||[input LA:1]=='_'||([input LA:1]>='a' && [input LA:1]<='z')) {
    	    	[input consume];
    	    	errorRecovery = NO;
    	    failed = NO;
    	    } else {
    	    if (backtracking > 0) {
    	    		failed = YES;
    	    		return;
    	    	}
    	    #warning Think about exceptions!
    	    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
    	    	[self recover:mse];	@throw mse;
    	    }


    	    }
    	    break;

    	default :
    	    goto loop37;
        }
    } while (true); loop37: ;


    }

}
// $ANTLR end ID

- (void) mESC
{
    // java.g:84:17: ( '\\' ('"'|'\''|'\\')) // ruleBlockSingleAlt
    // java.g:84:17: '\\' ('"'|'\''|'\\') // alt
    {
    [self matchChar:'\\'];
    if (failed) return;

    if ([input LA:1]=='"'||[input LA:1]=='\''||[input LA:1]=='\\') {
    	[input consume];
    	errorRecovery = NO;
    failed = NO;
    } else {
    if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
    	[self recover:mse];	@throw mse;
    }


    }

}
// $ANTLR end ESC

- (void) mTokens
{
    // java.g:1:25: ( ( IMPORT )=> IMPORT | ( RETURN )=> RETURN | ( CLASS )=> CLASS | ( METHOD )=> METHOD | ( FIELD )=> FIELD | ( STAT )=> STAT | ( CALL )=> CALL | ( COMMENT )=> COMMENT | ( SL_COMMENT )=> SL_COMMENT | ( STRING )=> STRING | ( CHAR )=> CHAR | ( WS )=> WS ) //ruleblock
    int alt38=12;
    switch ([input LA:1]) {
    	case 'i':
    		{
    			int LA38_1 = [input LA:2];
    			if ( [self synpred:Synpred1SyntacticPredicate stream:input] ) {
    				alt38 = 1;
    			}
    			else if ( [self synpred:Synpred4SyntacticPredicate stream:input] ) {
    				alt38 = 4;
    			}
    			else if ( [self synpred:Synpred5SyntacticPredicate stream:input] ) {
    				alt38 = 5;
    			}
    			else if ( [self synpred:Synpred6SyntacticPredicate stream:input] ) {
    				alt38 = 6;
    			}
    			else if ( [self synpred:Synpred7SyntacticPredicate stream:input] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {
    				failed = YES;
    				return;
    			}
    		#warning Think about exceptions!
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:1 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 'r':
    		{
    			int LA38_2 = [input LA:2];
    			if ( [self synpred:Synpred2SyntacticPredicate stream:input] ) {
    				alt38 = 2;
    			}
    			else if ( [self synpred:Synpred4SyntacticPredicate stream:input] ) {
    				alt38 = 4;
    			}
    			else if ( [self synpred:Synpred5SyntacticPredicate stream:input] ) {
    				alt38 = 5;
    			}
    			else if ( [self synpred:Synpred7SyntacticPredicate stream:input] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {
    				failed = YES;
    				return;
    			}
    		#warning Think about exceptions!
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:2 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 'c':
    		{
    			int LA38_3 = [input LA:2];
    			if ( [self synpred:Synpred3SyntacticPredicate stream:input] ) {
    				alt38 = 3;
    			}
    			else if ( [self synpred:Synpred4SyntacticPredicate stream:input] ) {
    				alt38 = 4;
    			}
    			else if ( [self synpred:Synpred5SyntacticPredicate stream:input] ) {
    				alt38 = 5;
    			}
    			else if ( [self synpred:Synpred7SyntacticPredicate stream:input] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {
    				failed = YES;
    				return;
    			}
    		#warning Think about exceptions!
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:3 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 'w':
    		{
    			int LA38_4 = [input LA:2];
    			if ( [self synpred:Synpred4SyntacticPredicate stream:input] ) {
    				alt38 = 4;
    			}
    			else if ( [self synpred:Synpred5SyntacticPredicate stream:input] ) {
    				alt38 = 5;
    			}
    			else if ( [self synpred:Synpred6SyntacticPredicate stream:input] ) {
    				alt38 = 6;
    			}
    			else if ( [self synpred:Synpred7SyntacticPredicate stream:input] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {
    				failed = YES;
    				return;
    			}
    		#warning Think about exceptions!
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:4 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 's':
    		{
    			int LA38_5 = [input LA:2];
    			if ( [self synpred:Synpred4SyntacticPredicate stream:input] ) {
    				alt38 = 4;
    			}
    			else if ( [self synpred:Synpred5SyntacticPredicate stream:input] ) {
    				alt38 = 5;
    			}
    			else if ( [self synpred:Synpred6SyntacticPredicate stream:input] ) {
    				alt38 = 6;
    			}
    			else if ( [self synpred:Synpred7SyntacticPredicate stream:input] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {
    				failed = YES;
    				return;
    			}
    		#warning Think about exceptions!
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:5 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 'f':
    		{
    			int LA38_6 = [input LA:2];
    			if ( [self synpred:Synpred4SyntacticPredicate stream:input] ) {
    				alt38 = 4;
    			}
    			else if ( [self synpred:Synpred5SyntacticPredicate stream:input] ) {
    				alt38 = 5;
    			}
    			else if ( [self synpred:Synpred6SyntacticPredicate stream:input] ) {
    				alt38 = 6;
    			}
    			else if ( [self synpred:Synpred7SyntacticPredicate stream:input] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {
    				failed = YES;
    				return;
    			}
    		#warning Think about exceptions!
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:6 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 'A':
    	case 'B':
    	case 'C':
    	case 'D':
    	case 'E':
    	case 'F':
    	case 'G':
    	case 'H':
    	case 'I':
    	case 'J':
    	case 'K':
    	case 'L':
    	case 'M':
    	case 'N':
    	case 'O':
    	case 'P':
    	case 'Q':
    	case 'R':
    	case 'S':
    	case 'T':
    	case 'U':
    	case 'V':
    	case 'W':
    	case 'X':
    	case 'Y':
    	case 'Z':
    	case '_':
    	case 'a':
    	case 'b':
    	case 'd':
    	case 'e':
    	case 'g':
    	case 'h':
    	case 'j':
    	case 'k':
    	case 'l':
    	case 'm':
    	case 'n':
    	case 'o':
    	case 'p':
    	case 'q':
    	case 't':
    	case 'u':
    	case 'v':
    	case 'x':
    	case 'y':
    	case 'z':
    		{
    			int LA38_7 = [input LA:2];
    			if ( [self synpred:Synpred4SyntacticPredicate stream:input] ) {
    				alt38 = 4;
    			}
    			else if ( [self synpred:Synpred5SyntacticPredicate stream:input] ) {
    				alt38 = 5;
    			}
    			else if ( [self synpred:Synpred7SyntacticPredicate stream:input] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {
    				failed = YES;
    				return;
    			}
    		#warning Think about exceptions!
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:7 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case '/':
    		{
    			int LA38_8 = [input LA:2];
    			if ( [self synpred:Synpred8SyntacticPredicate stream:input] ) {
    				alt38 = 8;
    			}
    			else if ( [self synpred:Synpred9SyntacticPredicate stream:input] ) {
    				alt38 = 9;
    			}
    		else {
    			if (backtracking > 0) {
    				failed = YES;
    				return;
    			}
    		#warning Think about exceptions!
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:8 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case '"':
    		alt38 = 10;
    		break;
    	case '\'':
    		alt38 = 11;
    		break;
    	case '\t':
    	case '\n':
    	case ' ':
    		alt38 = 12;
    		break;
    default:
    	if (backtracking > 0) {
    		failed = YES;
    		return;
    	}
    #warning Think about exceptions!
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:0 stream:input];
    	@throw nvae;
    }
    switch (alt38) {
    	case 1 :
    	    // java.g:1:25: ( IMPORT )=> IMPORT // alt
    	    {

    	    [self mIMPORT];
    	    if (failed) return;


    	    }
    	    break;
    	case 2 :
    	    // java.g:1:42: ( RETURN )=> RETURN // alt
    	    {

    	    [self mRETURN];
    	    if (failed) return;


    	    }
    	    break;
    	case 3 :
    	    // java.g:1:59: ( CLASS )=> CLASS // alt
    	    {

    	    [self mCLASS];
    	    if (failed) return;


    	    }
    	    break;
    	case 4 :
    	    // java.g:1:74: ( METHOD )=> METHOD // alt
    	    {

    	    [self mMETHOD];
    	    if (failed) return;


    	    }
    	    break;
    	case 5 :
    	    // java.g:1:91: ( FIELD )=> FIELD // alt
    	    {

    	    [self mFIELD];
    	    if (failed) return;


    	    }
    	    break;
    	case 6 :
    	    // java.g:1:106: ( STAT )=> STAT // alt
    	    {

    	    [self mSTAT];
    	    if (failed) return;


    	    }
    	    break;
    	case 7 :
    	    // java.g:1:119: ( CALL )=> CALL // alt
    	    {

    	    [self mCALL];
    	    if (failed) return;


    	    }
    	    break;
    	case 8 :
    	    // java.g:1:132: ( COMMENT )=> COMMENT // alt
    	    {

    	    [self mCOMMENT];
    	    if (failed) return;


    	    }
    	    break;
    	case 9 :
    	    // java.g:1:151: ( SL_COMMENT )=> SL_COMMENT // alt
    	    {

    	    [self mSL_COMMENT];
    	    if (failed) return;


    	    }
    	    break;
    	case 10 :
    	    // java.g:1:176: ( STRING )=> STRING // alt
    	    {

    	    [self mSTRING];
    	    if (failed) return;


    	    }
    	    break;
    	case 11 :
    	    // java.g:1:193: ( CHAR )=> CHAR // alt
    	    {

    	    [self mCHAR];
    	    if (failed) return;


    	    }
    	    break;
    	case 12 :
    	    // java.g:1:206: ( WS )=> WS // alt
    	    {

    	    [self mWS];
    	    if (failed) return;


    	    }
    	    break;

    }

}

- (void) mSynpred1_fragment
{
    // java.g:1:25: ( IMPORT ) // ruleBlockSingleAlt
    // java.g:1:26: IMPORT // alt
    {
    [self mIMPORT];
    if (failed) return;


    }

}
// $ANTLR end Synpred1_fragment

- (void) mSynpred2_fragment
{
    // java.g:1:42: ( RETURN ) // ruleBlockSingleAlt
    // java.g:1:43: RETURN // alt
    {
    [self mRETURN];
    if (failed) return;


    }

}
// $ANTLR end Synpred2_fragment

- (void) mSynpred3_fragment
{
    // java.g:1:59: ( CLASS ) // ruleBlockSingleAlt
    // java.g:1:60: CLASS // alt
    {
    [self mCLASS];
    if (failed) return;


    }

}
// $ANTLR end Synpred3_fragment

- (void) mSynpred4_fragment
{
    // java.g:1:74: ( METHOD ) // ruleBlockSingleAlt
    // java.g:1:75: METHOD // alt
    {
    [self mMETHOD];
    if (failed) return;


    }

}
// $ANTLR end Synpred4_fragment

- (void) mSynpred5_fragment
{
    // java.g:1:91: ( FIELD ) // ruleBlockSingleAlt
    // java.g:1:92: FIELD // alt
    {
    [self mFIELD];
    if (failed) return;


    }

}
// $ANTLR end Synpred5_fragment

- (void) mSynpred6_fragment
{
    // java.g:1:106: ( STAT ) // ruleBlockSingleAlt
    // java.g:1:107: STAT // alt
    {
    [self mSTAT];
    if (failed) return;


    }

}
// $ANTLR end Synpred6_fragment

- (void) mSynpred7_fragment
{
    // java.g:1:119: ( CALL ) // ruleBlockSingleAlt
    // java.g:1:120: CALL // alt
    {
    [self mCALL];
    if (failed) return;


    }

}
// $ANTLR end Synpred7_fragment

- (void) mSynpred8_fragment
{
    // java.g:1:132: ( COMMENT ) // ruleBlockSingleAlt
    // java.g:1:133: COMMENT // alt
    {
    [self mCOMMENT];
    if (failed) return;


    }

}
// $ANTLR end Synpred8_fragment

- (void) mSynpred9_fragment
{
    // java.g:1:151: ( SL_COMMENT ) // ruleBlockSingleAlt
    // java.g:1:152: SL_COMMENT // alt
    {
    [self mSL_COMMENT];
    if (failed) return;


    }

}
// $ANTLR end Synpred9_fragment

- (void) mSynpred10_fragment
{
    // java.g:1:176: ( STRING ) // ruleBlockSingleAlt
    // java.g:1:177: STRING // alt
    {
    [self mSTRING];
    if (failed) return;


    }

}
// $ANTLR end Synpred10_fragment

- (void) mSynpred11_fragment
{
    // java.g:1:193: ( CHAR ) // ruleBlockSingleAlt
    // java.g:1:194: CHAR // alt
    {
    [self mCHAR];
    if (failed) return;


    }

}
// $ANTLR end Synpred11_fragment

- (void) mSynpred12_fragment
{
    // java.g:1:206: ( WS ) // ruleBlockSingleAlt
    // java.g:1:207: WS // alt
    {
    [self mWS];
    if (failed) return;


    }

}
// $ANTLR end Synpred12_fragment

@end