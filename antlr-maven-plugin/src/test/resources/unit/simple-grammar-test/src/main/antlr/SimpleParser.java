// $ANTLR 3.1.1 Simple.g 2008-10-25 13:56:24

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SimpleParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "T"
    };
    public static final int T=4;
    public static final int EOF=-1;

    // delegates
    // delegators


        public SimpleParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public SimpleParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return SimpleParser.tokenNames; }
    public String getGrammarFileName() { return "Simple.g"; }



    // $ANTLR start "t"
    // Simple.g:3:1: t : T ;
    public final void t() throws RecognitionException {
        try {
            // Simple.g:3:2: ( T )
            // Simple.g:3:4: T
            {
            match(input,T,FOLLOW_T_in_t9); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return ;
    }
    // $ANTLR end "t"

    // Delegated rules


 

    public static final BitSet FOLLOW_T_in_t9 = new BitSet(new long[]{0x0000000000000002L});

}