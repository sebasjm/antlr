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

public class Lexer {
    protected IntegerStream input;
    protected Token token;
    protected int type;
    protected StringBuffer text = new StringBuffer();
    protected int line = 1;
    protected int column = 1;

    protected boolean skipToken = false;

    public Lexer(IntegerStream input) {
        this.input = input;
    }

    public void setType(int tokenType) {
        type = tokenType;
    }

    public void emit() {
        emit(type);
    }

    public void emit(int tokenType) {
        token = new CommonToken(tokenType, text.toString());
        token.setLine(line);
        token.setColumn(line);
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public void match(String s) {
        int i = 0;
        while ( i<s.length() ) {
            if ( input.LA(1)!=s.charAt(i) ) {
                System.err.println("mismatched char: "+
                        (char)input.LA(1)+"; expecting '"+s.charAt(i)+"'");
            }
            text.append((char)input.LA(1));
            i++;
            input.consume();
        }
    }

    public void matchAny() {
        text.append((char)input.LA(1));
        input.consume();
    }

    public void match(int c) {
        if ( input.LA(1)!=c ) {
            System.err.println("mismatched char: "+
                    (char)input.LA(1)+"; expecting "+(char)c+"'");
        }
        text.append((char)input.LA(1));
        input.consume();
    }

    public void matchRange(int x, int y) {
        if ( input.LA(1)<x || input.LA(1)>y ) {
            System.err.println("mismatched char: "+(char)input.LA(1)+"; expecting "+
                    x+".."+y);
        }
        text.append((char)input.LA(1));
        input.consume();
    }

    public void skip() {
        skipToken = true;
    }

    public void newline() {
        line++;
    }

    public int getLine() {
        return line;
    }

    public void setColumn(int c) {
        column = c;
    }

    public int getColumn() {
        return column;
    }
}
