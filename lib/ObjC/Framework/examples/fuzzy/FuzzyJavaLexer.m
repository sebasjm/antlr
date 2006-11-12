// $ANTLR 3.0b5 /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g 2006-11-12 22:52:36

#import "FuzzyJavaLexer.h"
#pragma mark Cyclic DFAs

/** As per Terence: No returns for lexer rules!
#pragma mark Rule return scopes start
#pragma mark Rule return scopes end
*/
@implementation FuzzyJavaLexer


- (id) initWithCharStream:(id<ANTLRCharStream>)anInput
{
	if (nil!=(self = [super initWithCharStream:anInput])) {









	}
	return self;
}

- (void) dealloc
{
	[super dealloc];
}

- (ANTLRToken *) nextToken
{
    while (YES) {
        if ( [input LA:1] == ANTLRCharStreamEOF ) {
            return nil; // should really be a +eofToken call here -> go figure
        }
        [self setToken:nil];
        tokenStartCharIndex = [self charIndex];
        @try {
            int m = [input mark];
            backtracking = 1;
            failed = NO;
            [self mTokens];
            backtracking = 0;
            if ( failed ) {
                [input rewind:m];
                [input consume]; 
            } else {
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
	ANTLRToken * _name = nil;

	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_IMPORT;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:9:4: ( 'import' WS name= QIDStar ( WS )? ';' ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:9:4: 'import' WS name= QIDStar ( WS )? ';' // alt
		{
		[self matchString:@"import"];
		if (failed) return ;

		[self mWS];
		if (failed) return ;

		int _nameStart = [self charIndex];
		[self mQIDStar];
		if (failed) return ;

		_name = [[ANTLRCommonToken alloc] initWithInput:input tokenType:ANTLRTokenTypeInvalid channel:ANTLRTokenChannelDefault start:_nameStart stop:[self charIndex]];
		[_name setLine:[self line]];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:9:29: ( WS )? // block
		int alt1=2;
		{
			int LA1_0 = [input LA:1];
			if ( (LA1_0>='\t' && LA1_0<='\n')||LA1_0==' ' ) {
				alt1 = 1;
			}
		}
		switch (alt1) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:9:29: WS // alt
			    {
			    [self mWS];
			    if (failed) return ;


			    }
			    break;

		}

		[self matchChar:';'];
		if (failed) return ;


		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		[_name release];
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end IMPORT


- (void) mRETURN
{
	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_RETURN;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:14:4: ( 'return' ( options {greedy=false; } : . )* ';' ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:14:4: 'return' ( options {greedy=false; } : . )* ';' // alt
		{
		[self matchString:@"return"];
		if (failed) return ;

		do {
		    int alt2=2;
		    {
		    	int LA2_0 = [input LA:1];
		    	if ( LA2_0==';' ) {
		    		alt2 = 2;
		    	}
		    	else if ( (LA2_0>=0x0000 && LA2_0<=':')||(LA2_0>='<' && LA2_0<=0xFFFE) ) {
		    		alt2 = 1;
		    	}

		    }
		    switch (alt2) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:14:38: . // alt
			    {
			    [self matchAny];
			    if (failed) return ;


			    }
			    break;

			default :
			    goto loop2;
		    }
		} while (YES); loop2: ;

		[self matchChar:';'];
		if (failed) return ;


		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end RETURN


- (void) mCLASS
{
	ANTLRToken * _name = nil;

	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_CLASS;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:18:4: ( 'class' WS name= ID ( WS )? ( 'extends' WS QID ( WS )? )? ( 'implements' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? '{' ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:18:4: 'class' WS name= ID ( WS )? ( 'extends' WS QID ( WS )? )? ( 'implements' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? '{' // alt
		{
		[self matchString:@"class"];
		if (failed) return ;

		[self mWS];
		if (failed) return ;

		int _nameStart = [self charIndex];
		[self mID];
		if (failed) return ;

		_name = [[ANTLRCommonToken alloc] initWithInput:input tokenType:ANTLRTokenTypeInvalid channel:ANTLRTokenChannelDefault start:_nameStart stop:[self charIndex]];
		[_name setLine:[self line]];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:18:23: ( WS )? // block
		int alt3=2;
		{
			int LA3_0 = [input LA:1];
			if ( (LA3_0>='\t' && LA3_0<='\n')||LA3_0==' ' ) {
				alt3 = 1;
			}
		}
		switch (alt3) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:18:23: WS // alt
			    {
			    [self mWS];
			    if (failed) return ;


			    }
			    break;

		}

		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:18:27: ( 'extends' WS QID ( WS )? )? // block
		int alt5=2;
		{
			int LA5_0 = [input LA:1];
			if ( LA5_0=='e' ) {
				alt5 = 1;
			}
		}
		switch (alt5) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:18:28: 'extends' WS QID ( WS )? // alt
			    {
			    [self matchString:@"extends"];
			    if (failed) return ;

			    [self mWS];
			    if (failed) return ;

			    [self mQID];
			    if (failed) return ;

			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:18:45: ( WS )? // block
			    int alt4=2;
			    {
			    	int LA4_0 = [input LA:1];
			    	if ( (LA4_0>='\t' && LA4_0<='\n')||LA4_0==' ' ) {
			    		alt4 = 1;
			    	}
			    }
			    switch (alt4) {
			    	case 1 :
			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:18:45: WS // alt
			    	    {
			    	    [self mWS];
			    	    if (failed) return ;


			    	    }
			    	    break;

			    }


			    }
			    break;

		}

		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:19:3: ( 'implements' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? // block
		int alt10=2;
		{
			int LA10_0 = [input LA:1];
			if ( LA10_0=='i' ) {
				alt10 = 1;
			}
		}
		switch (alt10) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:19:4: 'implements' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* // alt
			    {
			    [self matchString:@"implements"];
			    if (failed) return ;

			    [self mWS];
			    if (failed) return ;

			    [self mQID];
			    if (failed) return ;

			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:19:24: ( WS )? // block
			    int alt6=2;
			    {
			    	int LA6_0 = [input LA:1];
			    	if ( (LA6_0>='\t' && LA6_0<='\n')||LA6_0==' ' ) {
			    		alt6 = 1;
			    	}
			    }
			    switch (alt6) {
			    	case 1 :
			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:19:24: WS // alt
			    	    {
			    	    [self mWS];
			    	    if (failed) return ;


			    	    }
			    	    break;

			    }

			    do {
			        int alt9=2;
			        {
			        	int LA9_0 = [input LA:1];
			        	if ( LA9_0==',' ) {
			        		alt9 = 1;
			        	}

			        }
			        switch (alt9) {
			    	case 1 :
			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:19:29: ',' ( WS )? QID ( WS )? // alt
			    	    {
			    	    [self matchChar:','];
			    	    if (failed) return ;

			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:19:33: ( WS )? // block
			    	    int alt7=2;
			    	    {
			    	    	int LA7_0 = [input LA:1];
			    	    	if ( (LA7_0>='\t' && LA7_0<='\n')||LA7_0==' ' ) {
			    	    		alt7 = 1;
			    	    	}
			    	    }
			    	    switch (alt7) {
			    	    	case 1 :
			    	    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:19:33: WS // alt
			    	    	    {
			    	    	    [self mWS];
			    	    	    if (failed) return ;


			    	    	    }
			    	    	    break;

			    	    }

			    	    [self mQID];
			    	    if (failed) return ;

			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:19:41: ( WS )? // block
			    	    int alt8=2;
			    	    {
			    	    	int LA8_0 = [input LA:1];
			    	    	if ( (LA8_0>='\t' && LA8_0<='\n')||LA8_0==' ' ) {
			    	    		alt8 = 1;
			    	    	}
			    	    }
			    	    switch (alt8) {
			    	    	case 1 :
			    	    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:19:41: WS // alt
			    	    	    {
			    	    	    [self mWS];
			    	    	    if (failed) return ;


			    	    	    }
			    	    	    break;

			    	    }


			    	    }
			    	    break;

			    	default :
			    	    goto loop9;
			        }
			    } while (YES); loop9: ;


			    }
			    break;

		}

		[self matchChar:'{'];
		if (failed) return ;

		if ( backtracking==1 ) {
		  NSLog(@"found class %@", [_name text]);
		}

		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		[_name release];
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end CLASS


- (void) mMETHOD
{
	ANTLRToken * _name = nil;

	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_METHOD;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:9: ( TYPE WS name= ID ( WS )? '(' ( ARG ( WS )? ( ',' ( WS )? ARG ( WS )? )* )? ')' ( WS )? ( 'throws' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? '{' ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:9: TYPE WS name= ID ( WS )? '(' ( ARG ( WS )? ( ',' ( WS )? ARG ( WS )? )* )? ')' ( WS )? ( 'throws' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? '{' // alt
		{
		[self mTYPE];
		if (failed) return ;

		[self mWS];
		if (failed) return ;

		int _nameStart = [self charIndex];
		[self mID];
		if (failed) return ;

		_name = [[ANTLRCommonToken alloc] initWithInput:input tokenType:ANTLRTokenTypeInvalid channel:ANTLRTokenChannelDefault start:_nameStart stop:[self charIndex]];
		[_name setLine:[self line]];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:25: ( WS )? // block
		int alt11=2;
		{
			int LA11_0 = [input LA:1];
			if ( (LA11_0>='\t' && LA11_0<='\n')||LA11_0==' ' ) {
				alt11 = 1;
			}
		}
		switch (alt11) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:25: WS // alt
			    {
			    [self mWS];
			    if (failed) return ;


			    }
			    break;

		}

		[self matchChar:'('];
		if (failed) return ;

		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:33: ( ARG ( WS )? ( ',' ( WS )? ARG ( WS )? )* )? // block
		int alt16=2;
		{
			int LA16_0 = [input LA:1];
			if ( (LA16_0>='A' && LA16_0<='Z')||LA16_0=='_'||(LA16_0>='a' && LA16_0<='z') ) {
				alt16 = 1;
			}
		}
		switch (alt16) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:35: ARG ( WS )? ( ',' ( WS )? ARG ( WS )? )* // alt
			    {
			    [self mARG];
			    if (failed) return ;

			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:39: ( WS )? // block
			    int alt12=2;
			    {
			    	int LA12_0 = [input LA:1];
			    	if ( (LA12_0>='\t' && LA12_0<='\n')||LA12_0==' ' ) {
			    		alt12 = 1;
			    	}
			    }
			    switch (alt12) {
			    	case 1 :
			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:39: WS // alt
			    	    {
			    	    [self mWS];
			    	    if (failed) return ;


			    	    }
			    	    break;

			    }

			    do {
			        int alt15=2;
			        {
			        	int LA15_0 = [input LA:1];
			        	if ( LA15_0==',' ) {
			        		alt15 = 1;
			        	}

			        }
			        switch (alt15) {
			    	case 1 :
			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:44: ',' ( WS )? ARG ( WS )? // alt
			    	    {
			    	    [self matchChar:','];
			    	    if (failed) return ;

			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:48: ( WS )? // block
			    	    int alt13=2;
			    	    {
			    	    	int LA13_0 = [input LA:1];
			    	    	if ( (LA13_0>='\t' && LA13_0<='\n')||LA13_0==' ' ) {
			    	    		alt13 = 1;
			    	    	}
			    	    }
			    	    switch (alt13) {
			    	    	case 1 :
			    	    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:48: WS // alt
			    	    	    {
			    	    	    [self mWS];
			    	    	    if (failed) return ;


			    	    	    }
			    	    	    break;

			    	    }

			    	    [self mARG];
			    	    if (failed) return ;

			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:56: ( WS )? // block
			    	    int alt14=2;
			    	    {
			    	    	int LA14_0 = [input LA:1];
			    	    	if ( (LA14_0>='\t' && LA14_0<='\n')||LA14_0==' ' ) {
			    	    		alt14 = 1;
			    	    	}
			    	    }
			    	    switch (alt14) {
			    	    	case 1 :
			    	    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:56: WS // alt
			    	    	    {
			    	    	    [self mWS];
			    	    	    if (failed) return ;


			    	    	    }
			    	    	    break;

			    	    }


			    	    }
			    	    break;

			    	default :
			    	    goto loop15;
			        }
			    } while (YES); loop15: ;


			    }
			    break;

		}

		[self matchChar:')'];
		if (failed) return ;

		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:69: ( WS )? // block
		int alt17=2;
		{
			int LA17_0 = [input LA:1];
			if ( (LA17_0>='\t' && LA17_0<='\n')||LA17_0==' ' ) {
				alt17 = 1;
			}
		}
		switch (alt17) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:24:69: WS // alt
			    {
			    [self mWS];
			    if (failed) return ;


			    }
			    break;

		}

		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:25:8: ( 'throws' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* )? // block
		int alt22=2;
		{
			int LA22_0 = [input LA:1];
			if ( LA22_0=='t' ) {
				alt22 = 1;
			}
		}
		switch (alt22) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:25:9: 'throws' WS QID ( WS )? ( ',' ( WS )? QID ( WS )? )* // alt
			    {
			    [self matchString:@"throws"];
			    if (failed) return ;

			    [self mWS];
			    if (failed) return ;

			    [self mQID];
			    if (failed) return ;

			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:25:25: ( WS )? // block
			    int alt18=2;
			    {
			    	int LA18_0 = [input LA:1];
			    	if ( (LA18_0>='\t' && LA18_0<='\n')||LA18_0==' ' ) {
			    		alt18 = 1;
			    	}
			    }
			    switch (alt18) {
			    	case 1 :
			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:25:25: WS // alt
			    	    {
			    	    [self mWS];
			    	    if (failed) return ;


			    	    }
			    	    break;

			    }

			    do {
			        int alt21=2;
			        {
			        	int LA21_0 = [input LA:1];
			        	if ( LA21_0==',' ) {
			        		alt21 = 1;
			        	}

			        }
			        switch (alt21) {
			    	case 1 :
			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:25:30: ',' ( WS )? QID ( WS )? // alt
			    	    {
			    	    [self matchChar:','];
			    	    if (failed) return ;

			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:25:34: ( WS )? // block
			    	    int alt19=2;
			    	    {
			    	    	int LA19_0 = [input LA:1];
			    	    	if ( (LA19_0>='\t' && LA19_0<='\n')||LA19_0==' ' ) {
			    	    		alt19 = 1;
			    	    	}
			    	    }
			    	    switch (alt19) {
			    	    	case 1 :
			    	    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:25:34: WS // alt
			    	    	    {
			    	    	    [self mWS];
			    	    	    if (failed) return ;


			    	    	    }
			    	    	    break;

			    	    }

			    	    [self mQID];
			    	    if (failed) return ;

			    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:25:42: ( WS )? // block
			    	    int alt20=2;
			    	    {
			    	    	int LA20_0 = [input LA:1];
			    	    	if ( (LA20_0>='\t' && LA20_0<='\n')||LA20_0==' ' ) {
			    	    		alt20 = 1;
			    	    	}
			    	    }
			    	    switch (alt20) {
			    	    	case 1 :
			    	    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:25:42: WS // alt
			    	    	    {
			    	    	    [self mWS];
			    	    	    if (failed) return ;


			    	    	    }
			    	    	    break;

			    	    }


			    	    }
			    	    break;

			    	default :
			    	    goto loop21;
			        }
			    } while (YES); loop21: ;


			    }
			    break;

		}

		[self matchChar:'{'];
		if (failed) return ;

		if ( backtracking==1 ) {
		  NSLog(@"found method %@", [_name text]);
		}

		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		[_name release];
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end METHOD


- (void) mFIELD
{
	ANTLRToken * _name = nil;

	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_FIELD;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:30:9: ( TYPE WS name= ID ( '[]' )? ( WS )? (';'|'=')) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:30:9: TYPE WS name= ID ( '[]' )? ( WS )? (';'|'=') // alt
		{
		[self mTYPE];
		if (failed) return ;

		[self mWS];
		if (failed) return ;

		int _nameStart = [self charIndex];
		[self mID];
		if (failed) return ;

		_name = [[ANTLRCommonToken alloc] initWithInput:input tokenType:ANTLRTokenTypeInvalid channel:ANTLRTokenChannelDefault start:_nameStart stop:[self charIndex]];
		[_name setLine:[self line]];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:30:25: ( '[]' )? // block
		int alt23=2;
		{
			int LA23_0 = [input LA:1];
			if ( LA23_0=='[' ) {
				alt23 = 1;
			}
		}
		switch (alt23) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:30:25: '[]' // alt
			    {
			    [self matchString:@"[]"];
			    if (failed) return ;


			    }
			    break;

		}

		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:30:31: ( WS )? // block
		int alt24=2;
		{
			int LA24_0 = [input LA:1];
			if ( (LA24_0>='\t' && LA24_0<='\n')||LA24_0==' ' ) {
				alt24 = 1;
			}
		}
		switch (alt24) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:30:31: WS // alt
			    {
			    [self mWS];
			    if (failed) return ;


			    }
			    break;

		}

		if ([input LA:1]==';'||[input LA:1]=='=') {
			[input consume];
		failed = NO;
		} else {
			if (backtracking > 0) {failed=YES; return ;}
			ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
			[self recover:mse];	@throw mse;
		}

		if ( backtracking==1 ) {
		  NSLog(@"found var %@", [_name text]);
		}

		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		[_name release];
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end FIELD


- (void) mSTAT
{
	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_STAT;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:34:7: ( ( 'if' | 'while' | 'switch' | 'for' ) ( WS )? '(' ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:34:7: ( 'if' | 'while' | 'switch' | 'for' ) ( WS )? '(' // alt
		{
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:34:7: ( 'if' | 'while' | 'switch' | 'for' ) // block
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
		 {
			if (backtracking > 0) {failed=YES; return ;}
		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:25 state:0 stream:input];
			@throw nvae;

			}}
		switch (alt25) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:34:8: 'if' // alt
			    {
			    [self matchString:@"if"];
			    if (failed) return ;


			    }
			    break;
			case 2 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:34:13: 'while' // alt
			    {
			    [self matchString:@"while"];
			    if (failed) return ;


			    }
			    break;
			case 3 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:34:21: 'switch' // alt
			    {
			    [self matchString:@"switch"];
			    if (failed) return ;


			    }
			    break;
			case 4 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:34:30: 'for' // alt
			    {
			    [self matchString:@"for"];
			    if (failed) return ;


			    }
			    break;

		}

		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:34:37: ( WS )? // block
		int alt26=2;
		{
			int LA26_0 = [input LA:1];
			if ( (LA26_0>='\t' && LA26_0<='\n')||LA26_0==' ' ) {
				alt26 = 1;
			}
		}
		switch (alt26) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:34:37: WS // alt
			    {
			    [self mWS];
			    if (failed) return ;


			    }
			    break;

		}

		[self matchChar:'('];
		if (failed) return ;


		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end STAT


- (void) mCALL
{
	ANTLRToken * _name = nil;

	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_CALL;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:37:9: (name= QID ( WS )? '(' ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:37:9: name= QID ( WS )? '(' // alt
		{
		int _nameStart = [self charIndex];
		[self mQID];
		if (failed) return ;

		_name = [[ANTLRCommonToken alloc] initWithInput:input tokenType:ANTLRTokenTypeInvalid channel:ANTLRTokenChannelDefault start:_nameStart stop:[self charIndex]];
		[_name setLine:[self line]];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:37:18: ( WS )? // block
		int alt27=2;
		{
			int LA27_0 = [input LA:1];
			if ( (LA27_0>='\t' && LA27_0<='\n')||LA27_0==' ' ) {
				alt27 = 1;
			}
		}
		switch (alt27) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:37:18: WS // alt
			    {
			    [self mWS];
			    if (failed) return ;


			    }
			    break;

		}

		[self matchChar:'('];
		if (failed) return ;

		if ( backtracking==1 ) {
		  /*ignore if this/super */ NSLog(@"found call %@",[_name text]);
		}

		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		[_name release];
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end CALL


- (void) mCOMMENT
{
	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_COMMENT;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:42:9: ( '/*' ( options {greedy=false; } : . )* '*/' ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:42:9: '/*' ( options {greedy=false; } : . )* '*/' // alt
		{
		[self matchString:@"/*"];
		if (failed) return ;

		do {
		    int alt28=2;
		    {
		    	int LA28_0 = [input LA:1];
		    	if ( LA28_0=='*' ) {
		    		{
		    			int LA28_1 = [input LA:2];
		    			if ( LA28_1=='/' ) {
		    				alt28 = 2;
		    			}
		    			else if ( (LA28_1>=0x0000 && LA28_1<='.')||(LA28_1>='0' && LA28_1<=0xFFFE) ) {
		    				alt28 = 1;
		    			}

		    		}
		    	}
		    	else if ( (LA28_0>=0x0000 && LA28_0<=')')||(LA28_0>='+' && LA28_0<=0xFFFE) ) {
		    		alt28 = 1;
		    	}

		    }
		    switch (alt28) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:42:41: . // alt
			    {
			    [self matchAny];
			    if (failed) return ;


			    }
			    break;

			default :
			    goto loop28;
		    }
		} while (YES); loop28: ;

		[self matchString:@"*/"];
		if (failed) return ;

		if ( backtracking==1 ) {
		  NSLog(@"found comment %@", [self text]);
		}

		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end COMMENT


- (void) mSL_COMMENT
{
	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_SL_COMMENT;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:47:9: ( '//' ( options {greedy=false; } : . )* '\\n' ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:47:9: '//' ( options {greedy=false; } : . )* '\\n' // alt
		{
		[self matchString:@"//"];
		if (failed) return ;

		do {
		    int alt29=2;
		    {
		    	int LA29_0 = [input LA:1];
		    	if ( LA29_0=='\n' ) {
		    		alt29 = 2;
		    	}
		    	else if ( (LA29_0>=0x0000 && LA29_0<='\t')||(LA29_0>=0x000B && LA29_0<=0xFFFE) ) {
		    		alt29 = 1;
		    	}

		    }
		    switch (alt29) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:47:41: . // alt
			    {
			    [self matchAny];
			    if (failed) return ;


			    }
			    break;

			default :
			    goto loop29;
		    }
		} while (YES); loop29: ;

		[self matchChar:'\n'];
		if (failed) return ;

		if ( backtracking==1 ) {
		  NSLog(@"found // comment %@", [self text]);
		}

		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end SL_COMMENT


- (void) mSTRING
{
	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_STRING;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:52:4: ( '\"' ( options {greedy=false; } : ESC | . )* '\"' ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:52:4: '\"' ( options {greedy=false; } : ESC | . )* '\"' // alt
		{
		[self matchChar:'"'];
		if (failed) return ;

		do {
		    int alt30=3;
		    {
		    	int LA30_0 = [input LA:1];
		    	if ( LA30_0=='"' ) {
		    		alt30 = 3;
		    	}
		    	else if ( LA30_0=='\\' ) {
		    		{
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
		    			else if ( (LA30_2>=0x0000 && LA30_2<='!')||(LA30_2>='#' && LA30_2<='&')||(LA30_2>='(' && LA30_2<='[')||(LA30_2>=']' && LA30_2<=0xFFFE) ) {
		    				alt30 = 2;
		    			}

		    		}
		    	}
		    	else if ( (LA30_0>=0x0000 && LA30_0<='!')||(LA30_0>='#' && LA30_0<='[')||(LA30_0>=']' && LA30_0<=0xFFFE) ) {
		    		alt30 = 2;
		    	}

		    }
		    switch (alt30) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:52:34: ESC // alt
			    {
			    [self mESC];
			    if (failed) return ;


			    }
			    break;
			case 2 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:52:40: . // alt
			    {
			    [self matchAny];
			    if (failed) return ;


			    }
			    break;

			default :
			    goto loop30;
		    }
		} while (YES); loop30: ;

		[self matchChar:'"'];
		if (failed) return ;


		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end STRING


- (void) mCHAR
{
	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_CHAR;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:56:4: ( '\\'' ( options {greedy=false; } : ESC | . )* '\\'' ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:56:4: '\\'' ( options {greedy=false; } : ESC | . )* '\\'' // alt
		{
		[self matchChar:'\''];
		if (failed) return ;

		do {
		    int alt31=3;
		    {
		    	int LA31_0 = [input LA:1];
		    	if ( LA31_0=='\'' ) {
		    		alt31 = 3;
		    	}
		    	else if ( LA31_0=='\\' ) {
		    		{
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
		    			else if ( (LA31_2>=0x0000 && LA31_2<='!')||(LA31_2>='#' && LA31_2<='&')||(LA31_2>='(' && LA31_2<='[')||(LA31_2>=']' && LA31_2<=0xFFFE) ) {
		    				alt31 = 2;
		    			}

		    		}
		    	}
		    	else if ( (LA31_0>=0x0000 && LA31_0<='&')||(LA31_0>='(' && LA31_0<='[')||(LA31_0>=']' && LA31_0<=0xFFFE) ) {
		    		alt31 = 2;
		    	}

		    }
		    switch (alt31) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:56:35: ESC // alt
			    {
			    [self mESC];
			    if (failed) return ;


			    }
			    break;
			case 2 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:56:41: . // alt
			    {
			    [self matchAny];
			    if (failed) return ;


			    }
			    break;

			default :
			    goto loop31;
		    }
		} while (YES); loop31: ;

		[self matchChar:'\''];
		if (failed) return ;


		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end CHAR


- (void) mWS
{
	@try {
		ruleNestingLevel++;
		int _type = FuzzyJavaLexer_WS;
		int _start = [self charIndex];
		int _line = [self line];
		int _charPosition = [self charPositionInLine];
		int _channel = [ANTLRToken defaultChannel];
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:59:9: ( ( (' '|'\\t'|'\\n'))+ ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:59:9: ( (' '|'\\t'|'\\n'))+ // alt
		{
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:59:9: ( (' '|'\\t'|'\\n'))+	// positiveClosureBlock
		int cnt32=0;

		do {
		    int alt32=2;
		    {
		    	int LA32_0 = [input LA:1];
		    	if ( (LA32_0>='\t' && LA32_0<='\n')||LA32_0==' ' ) {
		    		alt32 = 1;
		    	}

		    }
		    switch (alt32) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:59:10: (' '|'\\t'|'\\n') // alt
			    {
			    if (([input LA:1]>='\t' && [input LA:1]<='\n')||[input LA:1]==' ') {
			    	[input consume];
			    failed = NO;
			    } else {
			    	if (backtracking > 0) {failed=YES; return ;}
			    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
			    	[self recover:mse];	@throw mse;
			    }


			    }
			    break;

			default :
			    if ( cnt32 >= 1 )  goto loop32;
		            if (backtracking > 0) {failed=YES; return ;}
					ANTLREarlyExitException *eee = [ANTLREarlyExitException exceptionWithStream:input decisionNumber:32];
					@throw eee;
		    }
		    cnt32++;
		} while (YES); loop32: ;


		}

		if ( token == nil && ruleNestingLevel == 1) { [self emitTokenWithType:_type line:_line charPosition:_charPosition channel:_channel start:_start stop:[self charIndex]];}
	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end WS


- (void) mQID
{
	@try {
		ruleNestingLevel++;
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:63:7: ( ID ( '.' ID )* ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:63:7: ID ( '.' ID )* // alt
		{
		[self mID];
		if (failed) return ;

		do {
		    int alt33=2;
		    {
		    	int LA33_0 = [input LA:1];
		    	if ( LA33_0=='.' ) {
		    		alt33 = 1;
		    	}

		    }
		    switch (alt33) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:63:11: '.' ID // alt
			    {
			    [self matchChar:'.'];
			    if (failed) return ;

			    [self mID];
			    if (failed) return ;


			    }
			    break;

			default :
			    goto loop33;
		    }
		} while (YES); loop33: ;


		}

	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end QID


- (void) mQIDStar
{
	@try {
		ruleNestingLevel++;
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:72:4: ( ID ( '.' ID )* ( '.*' )? ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:72:4: ID ( '.' ID )* ( '.*' )? // alt
		{
		[self mID];
		if (failed) return ;

		do {
		    int alt34=2;
		    {
		    	int LA34_0 = [input LA:1];
		    	if ( LA34_0=='.' ) {
		    		{
		    			int LA34_1 = [input LA:2];
		    			if ( (LA34_1>='A' && LA34_1<='Z')||LA34_1=='_'||(LA34_1>='a' && LA34_1<='z') ) {
		    				alt34 = 1;
		    			}

		    		}
		    	}

		    }
		    switch (alt34) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:72:8: '.' ID // alt
			    {
			    [self matchChar:'.'];
			    if (failed) return ;

			    [self mID];
			    if (failed) return ;


			    }
			    break;

			default :
			    goto loop34;
		    }
		} while (YES); loop34: ;

		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:72:17: ( '.*' )? // block
		int alt35=2;
		{
			int LA35_0 = [input LA:1];
			if ( LA35_0=='.' ) {
				alt35 = 1;
			}
		}
		switch (alt35) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:72:17: '.*' // alt
			    {
			    [self matchString:@".*"];
			    if (failed) return ;


			    }
			    break;

		}


		}

	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end QIDStar


- (void) mTYPE
{
	@try {
		ruleNestingLevel++;
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:76:9: ( QID ( '[]' )? ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:76:9: QID ( '[]' )? // alt
		{
		[self mQID];
		if (failed) return ;

		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:76:13: ( '[]' )? // block
		int alt36=2;
		{
			int LA36_0 = [input LA:1];
			if ( LA36_0=='[' ) {
				alt36 = 1;
			}
		}
		switch (alt36) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:76:13: '[]' // alt
			    {
			    [self matchString:@"[]"];
			    if (failed) return ;


			    }
			    break;

		}


		}

	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end TYPE


- (void) mARG
{
	@try {
		ruleNestingLevel++;
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:80:9: ( TYPE WS ID ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:80:9: TYPE WS ID // alt
		{
		[self mTYPE];
		if (failed) return ;

		[self mWS];
		if (failed) return ;

		[self mID];
		if (failed) return ;


		}

	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end ARG


- (void) mID
{
	@try {
		ruleNestingLevel++;
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:84:9: ( ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))* ) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:84:9: ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))* // alt
		{
		if (([input LA:1]>='A' && [input LA:1]<='Z')||[input LA:1]=='_'||([input LA:1]>='a' && [input LA:1]<='z')) {
			[input consume];
		failed = NO;
		} else {
			if (backtracking > 0) {failed=YES; return ;}
			ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
			[self recover:mse];	@throw mse;
		}

		do {
		    int alt37=2;
		    {
		    	int LA37_0 = [input LA:1];
		    	if ( (LA37_0>='0' && LA37_0<='9')||(LA37_0>='A' && LA37_0<='Z')||LA37_0=='_'||(LA37_0>='a' && LA37_0<='z') ) {
		    		alt37 = 1;
		    	}

		    }
		    switch (alt37) {
			case 1 :
			    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:84:34: ('a'..'z'|'A'..'Z'|'_'|'0'..'9') // alt
			    {
			    if (([input LA:1]>='0' && [input LA:1]<='9')||([input LA:1]>='A' && [input LA:1]<='Z')||[input LA:1]=='_'||([input LA:1]>='a' && [input LA:1]<='z')) {
			    	[input consume];
			    failed = NO;
			    } else {
			    	if (backtracking > 0) {failed=YES; return ;}
			    	ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
			    	[self recover:mse];	@throw mse;
			    }


			    }
			    break;

			default :
			    goto loop37;
		    }
		} while (YES); loop37: ;


		}

	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end ID


- (void) mESC
{
	@try {
		ruleNestingLevel++;
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:88:7: ( '\\\\' ('\"'|'\\''|'\\\\')) // ruleBlockSingleAlt
		// /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:88:7: '\\\\' ('\"'|'\\''|'\\\\') // alt
		{
		[self matchChar:'\\'];
		if (failed) return ;

		if ([input LA:1]=='"'||[input LA:1]=='\''||[input LA:1]=='\\') {
			[input consume];
		failed = NO;
		} else {
			if (backtracking > 0) {failed=YES; return ;}
			ANTLRMismatchedSetException *mse = [ANTLRMismatchedSetException exceptionWithSet:nil stream:input];
			[self recover:mse];	@throw mse;
		}


		}

	}
	@finally {
		ruleNestingLevel--;
        // rule cleanup
		// token labels
		// token+rule list labels
		// rule labels
		// rule refs in alts with rewrites

		if ( backtracking == 0 ) {
		}
	}
	return;
}
// $ANTLR end ESC

- (void) mTokens
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:25: ( ( IMPORT )=> IMPORT | ( RETURN )=> RETURN | ( CLASS )=> CLASS | ( METHOD )=> METHOD | ( FIELD )=> FIELD | ( STAT )=> STAT | ( CALL )=> CALL | ( COMMENT )=> COMMENT | ( SL_COMMENT )=> SL_COMMENT | ( STRING )=> STRING | ( CHAR )=> CHAR | ( WS )=> WS ) //ruleblock
    int alt38=12;
    switch ([input LA:1]) {
    	case 'i':
    		{
    			int LA38_1 = [input LA:2];
    			if ( [self evaluateSyntacticPredicate:@selector(synpred1)] ) {
    				alt38 = 1;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred4)] ) {
    				alt38 = 4;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred5)] ) {
    				alt38 = 5;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred6)] ) {
    				alt38 = 6;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred7)] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {failed=YES; return ;}
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:1 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 'r':
    		{
    			int LA38_2 = [input LA:2];
    			if ( [self evaluateSyntacticPredicate:@selector(synpred2)] ) {
    				alt38 = 2;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred4)] ) {
    				alt38 = 4;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred5)] ) {
    				alt38 = 5;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred7)] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {failed=YES; return ;}
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:2 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 'c':
    		{
    			int LA38_3 = [input LA:2];
    			if ( [self evaluateSyntacticPredicate:@selector(synpred3)] ) {
    				alt38 = 3;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred4)] ) {
    				alt38 = 4;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred5)] ) {
    				alt38 = 5;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred7)] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {failed=YES; return ;}
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:3 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 'w':
    		{
    			int LA38_4 = [input LA:2];
    			if ( [self evaluateSyntacticPredicate:@selector(synpred4)] ) {
    				alt38 = 4;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred5)] ) {
    				alt38 = 5;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred6)] ) {
    				alt38 = 6;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred7)] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {failed=YES; return ;}
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:4 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 's':
    		{
    			int LA38_5 = [input LA:2];
    			if ( [self evaluateSyntacticPredicate:@selector(synpred4)] ) {
    				alt38 = 4;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred5)] ) {
    				alt38 = 5;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred6)] ) {
    				alt38 = 6;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred7)] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {failed=YES; return ;}
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:5 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case 'f':
    		{
    			int LA38_6 = [input LA:2];
    			if ( [self evaluateSyntacticPredicate:@selector(synpred4)] ) {
    				alt38 = 4;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred5)] ) {
    				alt38 = 5;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred6)] ) {
    				alt38 = 6;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred7)] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {failed=YES; return ;}
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
    			if ( [self evaluateSyntacticPredicate:@selector(synpred4)] ) {
    				alt38 = 4;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred5)] ) {
    				alt38 = 5;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred7)] ) {
    				alt38 = 7;
    			}
    		else {
    			if (backtracking > 0) {failed=YES; return ;}
    		    ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:7 stream:input];
    			@throw nvae;
    			}
    		}
    		break;
    	case '/':
    		{
    			int LA38_8 = [input LA:2];
    			if ( [self evaluateSyntacticPredicate:@selector(synpred8)] ) {
    				alt38 = 8;
    			}
    			else if ( [self evaluateSyntacticPredicate:@selector(synpred9)] ) {
    				alt38 = 9;
    			}
    		else {
    			if (backtracking > 0) {failed=YES; return ;}
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
     {
    	if (backtracking > 0) {failed=YES; return ;}
        ANTLRNoViableAltException *nvae = [ANTLRNoViableAltException exceptionWithDecision:38 state:0 stream:input];
    	@throw nvae;

    	}}
    switch (alt38) {
    	case 1 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:25: ( IMPORT )=> IMPORT // alt
    	    {
    	    [self mIMPORT];
    	    if (failed) return ;


    	    }
    	    break;
    	case 2 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:42: ( RETURN )=> RETURN // alt
    	    {
    	    [self mRETURN];
    	    if (failed) return ;


    	    }
    	    break;
    	case 3 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:59: ( CLASS )=> CLASS // alt
    	    {
    	    [self mCLASS];
    	    if (failed) return ;


    	    }
    	    break;
    	case 4 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:74: ( METHOD )=> METHOD // alt
    	    {
    	    [self mMETHOD];
    	    if (failed) return ;


    	    }
    	    break;
    	case 5 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:91: ( FIELD )=> FIELD // alt
    	    {
    	    [self mFIELD];
    	    if (failed) return ;


    	    }
    	    break;
    	case 6 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:106: ( STAT )=> STAT // alt
    	    {
    	    [self mSTAT];
    	    if (failed) return ;


    	    }
    	    break;
    	case 7 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:119: ( CALL )=> CALL // alt
    	    {
    	    [self mCALL];
    	    if (failed) return ;


    	    }
    	    break;
    	case 8 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:132: ( COMMENT )=> COMMENT // alt
    	    {
    	    [self mCOMMENT];
    	    if (failed) return ;


    	    }
    	    break;
    	case 9 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:151: ( SL_COMMENT )=> SL_COMMENT // alt
    	    {
    	    [self mSL_COMMENT];
    	    if (failed) return ;


    	    }
    	    break;
    	case 10 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:176: ( STRING )=> STRING // alt
    	    {
    	    [self mSTRING];
    	    if (failed) return ;


    	    }
    	    break;
    	case 11 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:193: ( CHAR )=> CHAR // alt
    	    {
    	    [self mCHAR];
    	    if (failed) return ;


    	    }
    	    break;
    	case 12 :
    	    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:206: ( WS )=> WS // alt
    	    {
    	    [self mWS];
    	    if (failed) return ;


    	    }
    	    break;

    }

}

- (void) synpred1
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:25: ( IMPORT ) // ruleBlockSingleAlt
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:26: IMPORT // alt
    {
    [self mIMPORT];
    if (failed) return ;


    }
}

- (void) synpred2
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:42: ( RETURN ) // ruleBlockSingleAlt
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:43: RETURN // alt
    {
    [self mRETURN];
    if (failed) return ;


    }
}

- (void) synpred3
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:59: ( CLASS ) // ruleBlockSingleAlt
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:60: CLASS // alt
    {
    [self mCLASS];
    if (failed) return ;


    }
}

- (void) synpred4
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:74: ( METHOD ) // ruleBlockSingleAlt
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:75: METHOD // alt
    {
    [self mMETHOD];
    if (failed) return ;


    }
}

- (void) synpred5
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:91: ( FIELD ) // ruleBlockSingleAlt
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:92: FIELD // alt
    {
    [self mFIELD];
    if (failed) return ;


    }
}

- (void) synpred6
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:106: ( STAT ) // ruleBlockSingleAlt
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:107: STAT // alt
    {
    [self mSTAT];
    if (failed) return ;


    }
}

- (void) synpred7
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:119: ( CALL ) // ruleBlockSingleAlt
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:120: CALL // alt
    {
    [self mCALL];
    if (failed) return ;


    }
}

- (void) synpred8
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:132: ( COMMENT ) // ruleBlockSingleAlt
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:133: COMMENT // alt
    {
    [self mCOMMENT];
    if (failed) return ;


    }
}

- (void) synpred9
{
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:151: ( SL_COMMENT ) // ruleBlockSingleAlt
    // /Users/kroepke/Projects/antlr3/code/antlr/main/lib/ObjC/Framework/examples/fuzzy/FuzzyJava.g:1:152: SL_COMMENT // alt
    {
    [self mSL_COMMENT];
    if (failed) return ;


    }
}

@end