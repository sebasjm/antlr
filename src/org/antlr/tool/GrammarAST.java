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
package org.antlr.tool;

import antlr.collections.*;
import antlr.*;
import org.antlr.analysis.DFA;
import org.antlr.analysis.NFA;
import org.antlr.analysis.NFAState;
import org.antlr.misc.IntSet;

import java.util.Map;
import java.util.HashMap;

/** Grammars are first converted to ASTs using this class and then are
 *  converted to NFAs via a tree walker.
 */
public class GrammarAST extends BaseAST {
    Token token = null;
    GrammarAST enclosingBlock = null;
    String enclosingRule = null;

    /** If this is a decision node, what is the lookahead DFA? */
    DFA lookaheadDFA = null;

    /** What NFA was built from this node? */
    NFAState NFAStartState = null;

    /** If this is a SET node, what are the elements? */
    IntSet setValue = null;

    /** If this is a BLOCK node, track options here */
    Map options = null;

    /** What are the default options for a subrule? */
    public static final Map defaultOptions =
            new HashMap() {{put("greedy","true");}};

    public GrammarAST getEnclosingBlock() {
        return enclosingBlock;
    }

    public void initialize(int i, String s) {
        token = new CommonToken(i,s);
    }

    public void initialize(AST ast) {
    }

    public void initialize(Token token) {
        this.token = token;
    }

    public DFA getLookaheadDFA() {
        return lookaheadDFA;
    }

    public void setLookaheadDFA(DFA lookaheadDFA) {
        this.lookaheadDFA = lookaheadDFA;
    }

	public Token getToken() {
		return token;
	}

    public NFAState getNFAStartState() {
        return NFAStartState;
    }

    public void setNFAStartState(NFAState nfaStartState) {
        this.NFAStartState = nfaStartState;
    }

    /** Save the option key/value pair and process it */
    public void setOption(String key, Object value) {
        if ( value instanceof String ) {
            String vs = (String)value;
            if ( vs.charAt(0)=='"' ) {
                value = vs.substring(1,vs.length()-1); // strip quotes
            }
        }
        options.put(key, value);
    }

    public Object getOption(String key) {
        Object v = options.get(key);
        if ( v!=null ) {
            return v;
        }
        return defaultOptions.get(key);
    }

    public void setOptions(Map options) {
        this.options = options;
    }

    public Map getOptions() {
        return options;
    }

    public String getText() {
        if ( token!=null ) {
            return token.getText();
        }
        return "";
    }

    public void setType(int type) {
        token.setType(type);
    }

    public int getType() {
        if ( token!=null ) {
            return token.getType();
        }
        return -1;
    }

    public int getLine() {
        if ( token!=null ) {
            return token.getLine();
        }
        return 0;
    }

    public int getColumn() {
        if ( token!=null ) {
            return token.getColumn();
        }
        return 0;
    }

    public void setLine(int line) {
        token.setLine(line);
    }

    public void setColumn(int col) {
        token.setColumn(col);
    }

    public void setEnclosingBlock(GrammarAST enclosingBlock) {
        this.enclosingBlock = enclosingBlock;
    }

    public void setEnclosingRule(String rule) {
        this.enclosingRule = rule;
    }

    public String getEnclosingRule() {
        return enclosingRule;
    }

    public IntSet getSetValue() {
        return setValue;
    }

    public void setSetValue(IntSet setValue) {
        this.setValue = setValue;
    }

    public GrammarAST getLastChild() {
        return ((GrammarAST)getFirstChild()).getLastSibling();
    }

    public GrammarAST getLastSibling() {
        GrammarAST t = this;
        GrammarAST last = null;
        while ( t!=null ) {
            last = t;
            t = (GrammarAST)t.getNextSibling();
        }
        return last;
    }

    /** Get the ith child from 0 */
    public GrammarAST getChild(int i) {
        int n = 0;
        AST t = getFirstChild();
        while ( t!=null ) {
            if ( n==i ) {
                return (GrammarAST)t;
            }
            n++;
            t = (GrammarAST)t.getNextSibling();
        }
        return null;
    }

    public GrammarAST[] getChildrenAsArray() {
        AST t = getFirstChild();
        GrammarAST[] array = new GrammarAST[getNumberOfChildren()];
        int i = 0;
        while ( t!=null ) {
            array[i] = (GrammarAST)t;
            t = t.getNextSibling();
            i++;
        }
        return array;
    }
}
