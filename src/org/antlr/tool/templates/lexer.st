import java.io.*;

class Lexer {
	public static final int EOF = -1;
    protected InputStream input = null;
    protected int c;
    protected int p;
	protected static boolean[] isStopState = new boolean[<dfa.numberOfStates>];
	static {
		<dfa.stopStates:{isStopState[<attr.stateNumber>]=true;\n}>
    };

    public Lexer(InputStream input) {
        this.input = input;
    }

    public void consume() throws IOException {
        c = input.read();
        p++;
    }

    public void error(int c) {
        System.err.println("error upon '"+c+"' at position "+p);
    }

    public void match() throws IOException {
        p = 0; 	       // input position 0
        int state = <dfa.startState.stateNumber>;
        matching:
        while (true) {
            switch (state) {
				<dfa.orderedStateList:lexer_state(); separator="\n">
            }
        }
		if ( c==EOF && isStopState[state] ) {
			System.out.println("matched input");
		}
		else {
			System.out.println("failed to match input from position "+p+" onwards");
		}
    }
}
