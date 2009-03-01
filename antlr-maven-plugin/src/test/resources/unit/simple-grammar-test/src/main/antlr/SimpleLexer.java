// $ANTLR 3.1.1 Simple.g 2008-10-25 13:56:24

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class SimpleLexer extends Lexer {
    public static final int T=4;
    public static final int EOF=-1;

    // delegates
    // delegators

    public SimpleLexer() {;} 
    public SimpleLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public SimpleLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "Simple.g"; }

    // $ANTLR start "T"
    public final void mT() throws RecognitionException {
        try {
            int _type = T;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // Simple.g:5:2: ( 'T' )
            // Simple.g:5:4: 'T'
            {
            match('T'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T"

    public void mTokens() throws RecognitionException {
        // Simple.g:1:8: ( T )
        // Simple.g:1:10: T
        {
        mT(); 

        }


    }


 

}