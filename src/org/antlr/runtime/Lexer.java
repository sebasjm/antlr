/*
 [The "BSD licence"]
 Copyright (c) 2004 Terence Parr
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:
 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.antlr.runtime;

public abstract class Lexer implements TokenSource {
    protected CharStream input;
    protected Token token;

	protected StringBuffer inputBuffer;

	public Lexer(CharStream input) {
		this.input = input;
	}

	public CharStream getCharStream() {
		return input;
	}
	
	public void emit(int tokenType,
					 int line, int charPosition,
					 int channel,
					 int start, int stop) {
		Token token = new CommonToken(tokenType, channel, start, stop);
		token.setLine(line);
		token.setCharPositionInLine(charPosition);
		emit(token);
	}

	public void emit(Token token) {
		this.token = token;
	}

    public void match(String s) throws MismatchedTokenException {
        int i = 0;
        while ( i<s.length() ) {
            if ( input.LA(1)!=s.charAt(i) ) {
				throw new MismatchedTokenException(s.charAt(i));
            }
            i++;
            input.consume();
        }
    }

    public void matchAny() {
        input.consume();
    }

    public void match(int c) throws MismatchedTokenException {
        if ( input.LA(1)!=c ) {
			throw new MismatchedTokenException(c);
        }
        input.consume();
    }

    public void matchRange(int a, int b) throws MismatchedRangeException {
        if ( input.LA(1)<a || input.LA(1)>b ) {
            throw new MismatchedRangeException(a,b);
        }
        input.consume();
    }

    public int getLine() {
        return input.getLine();
    }

    public int getCharPositionInLine() {
        return input.getCharPositionInLine();
    }

	/** What is the index of the current character of lookahead? */
	public int getCharIndex() {
		return input.index();
	}

	/** Report a recognition problem.  Java is not polymorphic on the
	 *  argument types so you have to check the type of exception yourself.
	 *  That's not very clean but it's better than generating a bunch of
	 *  catch clauses in each rule and makes it easy to extend with
	 *  more exceptions w/o breaking old code.
	 */
	public void reportError(RecognitionException e) {
		System.err.print(Parser.getRuleInvocationStack(e,getClass().getName())+
						 ": line "+input.getLine()+" ");
		if ( e instanceof MismatchedTokenException ) {
			MismatchedTokenException mte = (MismatchedTokenException)e;
			System.err.println("mismatched char: '"+
							   (char)input.LA(1)+
							   "' on line "+getLine()+
							   "; expecting char '"+(char)mte.expecting+"'");
		}
		else if ( e instanceof NoViableAltException ) {
			NoViableAltException nvae = (NoViableAltException)e;
			System.err.println(nvae.grammarDecisionDescription+
							   " state "+nvae.stateNumber+
							   " (decision="+nvae.decisionNumber+
							   ") no viable alt line "+getLine()+":"+getCharPositionInLine()+"; char='"+
							   (char)input.LA(1)+"'");
		}
		else if ( e instanceof EarlyExitException ) {
			EarlyExitException eee = (EarlyExitException)e;
			System.err.println("required (...)+ loop (decision="+
							   eee.decisionNumber+
							   ") did not match anything; on line "+
							   getLine()+":"+getCharPositionInLine()+" char="+
							   (char)input.LA(1)+"'");
		}
		else if ( e instanceof MismatchedSetException ) {
			MismatchedSetException mse = (MismatchedSetException)e;
			System.err.println("mismatched char: '"+
							   (char)input.LA(1)+
							   "' on line "+getLine()+
							   ":"+getCharPositionInLine()+
							   "; expecting set "+mse.expecting);
		}
		else if ( e instanceof MismatchedNotSetException ) {
			MismatchedSetException mse = (MismatchedSetException)e;
			System.err.println("mismatched char: '"+
							   (char)input.LA(1)+
							   "' on line "+getLine()+
							   ":"+getCharPositionInLine()+
							   "; expecting set "+mse.expecting);
		}
		else if ( e instanceof MismatchedRangeException ) {
			MismatchedRangeException mre = (MismatchedRangeException)e;
			System.err.println("mismatched char: '"+
							   (char)input.LA(1)+
							   "' on line "+getLine()+
							   ":"+getCharPositionInLine()+
							   "; expecting set '"+(char)mre.a+"'..'"+
							   (char)mre.b+"'");
		}
	}

	/** Lexers can normally about any char in it's vocabulary after matching
	 *  a token, so do the easy thing and just kill a character and hope
	 *  it all works out.  You can instead use the rule invocation stack
	 *  to do sophisticated error recovery if you are in a fragment rule.
	 */
	public void recover(RecognitionException re) {
		System.out.println("consuming char "+(char)input.LA(1)+" during recovery");
		input.consume();
	}
}
