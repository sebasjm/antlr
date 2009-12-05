package org.antlr.test;

import org.junit.Test;
import org.antlr.tool.Grammar;
import org.antlr.tool.Interpreter;
import org.antlr.runtime.*;

/** This actually tests new (12/4/09) buffered but on-demand fetching stream */
public class TestCommonTokenStream extends BaseTest {
    @Test public void testFirstToken() throws Exception {
        Grammar g = new Grammar(
            "lexer grammar t;\n"+
            "ID : 'a'..'z'+;\n" +
            "INT : '0'..'9'+;\n" +
            "SEMI : ';';\n" +
            "ASSIGN : '=';\n" +
            "PLUS : '+';\n" +
            "MULT : '*';\n" +
            "WS : ' '+;\n");
        // Tokens: 012345678901234567
        // Input:  x = 3 * 0 + 2 * 0;
        CharStream input = new ANTLRStringStream("x = 3 * 0 + 2 * 0;");
        Interpreter lexEngine = new Interpreter(g, input);
        BufferedTokenStream tokens = new BufferedTokenStream(lexEngine);

        String result = tokens.LT(1).getText();
        String expecting = "x";
        assertEquals(expecting, result);
    }

    @Test public void test2ndToken() throws Exception {
        Grammar g = new Grammar(
            "lexer grammar t;\n"+
            "ID : 'a'..'z'+;\n" +
            "INT : '0'..'9'+;\n" +
            "SEMI : ';';\n" +
            "ASSIGN : '=';\n" +
            "PLUS : '+';\n" +
            "MULT : '*';\n" +
            "WS : ' '+;\n");
        // Tokens: 012345678901234567
        // Input:  x = 3 * 0 + 2 * 0;
        CharStream input = new ANTLRStringStream("x = 3 * 0 + 2 * 0;");
        Interpreter lexEngine = new Interpreter(g, input);
        BufferedTokenStream tokens = new BufferedTokenStream(lexEngine);

        String result = tokens.LT(2).getText();
        String expecting = " ";
        assertEquals(expecting, result);
    }

    @Test public void testCompleteBuffer() throws Exception {
        Grammar g = new Grammar(
            "lexer grammar t;\n"+
            "ID : 'a'..'z'+;\n" +
            "INT : '0'..'9'+;\n" +
            "SEMI : ';';\n" +
            "ASSIGN : '=';\n" +
            "PLUS : '+';\n" +
            "MULT : '*';\n" +
            "WS : ' '+;\n");
        // Tokens: 012345678901234567
        // Input:  x = 3 * 0 + 2 * 0;
        CharStream input = new ANTLRStringStream("x = 3 * 0 + 2 * 0;");
        Interpreter lexEngine = new Interpreter(g, input);
        BufferedTokenStream tokens = new BufferedTokenStream(lexEngine);

        int i = 1;
        Token t = tokens.LT(i);
        while ( t.getType()!=Token.EOF ) {
            i++;
            t = tokens.LT(i);
        }
        tokens.LT(i++); // push it past end
        tokens.LT(i++);

        String result = tokens.toString();
        String expecting = "x = 3 * 0 + 2 * 0;";
        assertEquals(expecting, result);
    }

    @Test public void testCompleteBufferAfterConsuming() throws Exception {
        Grammar g = new Grammar(
            "lexer grammar t;\n"+
            "ID : 'a'..'z'+;\n" +
            "INT : '0'..'9'+;\n" +
            "SEMI : ';';\n" +
            "ASSIGN : '=';\n" +
            "PLUS : '+';\n" +
            "MULT : '*';\n" +
            "WS : ' '+;\n");
        // Tokens: 012345678901234567
        // Input:  x = 3 * 0 + 2 * 0;
        CharStream input = new ANTLRStringStream("x = 3 * 0 + 2 * 0;");
        Interpreter lexEngine = new Interpreter(g, input);
        BufferedTokenStream tokens = new BufferedTokenStream(lexEngine);

        Token t = tokens.LT(1);
        while ( t.getType()!=Token.EOF ) {
            tokens.consume();
            t = tokens.LT(1);
        }
        tokens.consume();
        tokens.LT(1); // push it past end
        tokens.consume();
        tokens.LT(1);

        String result = tokens.toString();
        String expecting = "x = 3 * 0 + 2 * 0;";
        assertEquals(expecting, result);
    }

    @Test public void testLookback() throws Exception {
        Grammar g = new Grammar(
            "lexer grammar t;\n"+
            "ID : 'a'..'z'+;\n" +
            "INT : '0'..'9'+;\n" +
            "SEMI : ';';\n" +
            "ASSIGN : '=';\n" +
            "PLUS : '+';\n" +
            "MULT : '*';\n" +
            "WS : ' '+;\n");
        // Tokens: 012345678901234567
        // Input:  x = 3 * 0 + 2 * 0;
        CharStream input = new ANTLRStringStream("x = 3 * 0 + 2 * 0;");
        Interpreter lexEngine = new Interpreter(g, input);
        BufferedTokenStream tokens = new BufferedTokenStream(lexEngine);

        tokens.consume(); // get x into buffer
        Token t = tokens.LT(-1);
        assertEquals("x", t.getText());

        tokens.consume();
        tokens.consume(); // consume '='
        t = tokens.LT(-3);
        assertEquals("x", t.getText());
        t = tokens.LT(-2);
        assertEquals(" ", t.getText());
        t = tokens.LT(-1);
        assertEquals("=", t.getText());
    }

    @Test public void testOffChannel() throws Exception {
        TokenSource lexer = // simulate input " x =34  ;\n"
            new TokenSource() {
                int i = 0;
                Token[] tokens = {
                    new CommonToken(1," "),
                    new CommonToken(1,"x"),
                    new CommonToken(1," "),
                    new CommonToken(1,"="),
                    new CommonToken(1,"34"),
                    new CommonToken(1," "),
                    new CommonToken(1," "),
                    new CommonToken(1,";"),
                    new CommonToken(1,"\n"),
                    new CommonToken(Token.EOF,"")
                };
                {
                    tokens[0].setChannel(Lexer.HIDDEN);
                    tokens[2].setChannel(Lexer.HIDDEN);
                    tokens[5].setChannel(Lexer.HIDDEN);
                    tokens[6].setChannel(Lexer.HIDDEN);
                    tokens[8].setChannel(Lexer.HIDDEN);
                }
                public Token nextToken() {
                    return tokens[i++];
                }
                public String getSourceName() { return "test"; }
            };

        CommonTokenStream tokens = new CommonTokenStream(lexer);

        assertEquals("x", tokens.LT(1).getText()); // must skip first off channel token
        tokens.consume();
        assertEquals("=", tokens.LT(1).getText());
        assertEquals("x", tokens.LT(-1).getText());

        tokens.consume();
        assertEquals("34", tokens.LT(1).getText());
        assertEquals("=", tokens.LT(-1).getText());

        tokens.consume();
        assertEquals(";", tokens.LT(1).getText());
        assertEquals("34", tokens.LT(-1).getText());

        tokens.consume();
        assertEquals(Token.EOF, tokens.LA(1));
        assertEquals(";", tokens.LT(-1).getText());

        assertEquals("34", tokens.LT(-2).getText());
        assertEquals("=", tokens.LT(-3).getText());
        assertEquals("x", tokens.LT(-4).getText());
    }
}