// $ANTLR 3.0b1 action.g 2006-06-24 16:13:40

package org.antlr.codegen;
import org.antlr.runtime.*;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class ActionTranslator extends Lexer {
    public static final int LOCAL_ATTR=11;
    public static final int ISOLATED_DYNAMIC_SCOPE=17;
    public static final int WS=18;
    public static final int UNKNOWN_SYNTAX=30;
    public static final int DYNAMIC_SCOPE_ATTR=12;
    public static final int SCOPE_INDEX_EXPR=14;
    public static final int DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR=16;
    public static final int ISOLATED_TOKEN_REF=9;
    public static final int SET_ATTRIBUTE=25;
    public static final int SET_EXPR_ATTRIBUTE=24;
    public static final int ACTION=21;
    public static final int ERROR_X=29;
    public static final int TEMPLATE_INSTANCE=20;
    public static final int TOKEN_SCOPE_ATTR=6;
    public static final int ISOLATED_LEXER_RULE_REF=10;
    public static final int ESC=27;
    public static final int ATTR_VALUE_EXPR=23;
    public static final int RULE_SCOPE_ATTR=7;
    public static final int LABEL_REF=8;
    public static final int INT=32;
    public static final int ARG=19;
    public static final int EOF=-1;
    public static final int TEXT=31;
    public static final int Tokens=33;
    public static final int DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR=15;
    public static final int ERROR_SCOPED_XY=13;
    public static final int ENCLOSING_RULE_SCOPE_ATTR=5;
    public static final int ERROR_XY=28;
    public static final int TEMPLATE_EXPR=26;
    public static final int INDIRECT_TEMPLATE_INSTANCE=22;
    public static final int ID=4;

    public List chunks = new ArrayList();
    Rule enclosingRule;
    int outerAltNum;
    Grammar grammar;
    CodeGenerator generator;
    antlr.Token actionToken;

    	public ActionTranslator(CodeGenerator generator,
    							String ruleName,
    						    GrammarAST actionAST)
    	{
    		this(new ANTLRStringStream(actionAST.token.getText()));
    		this.generator = generator;
    		this.grammar = generator.grammar;
    	    this.enclosingRule = grammar.getRule(ruleName);
    	    this.actionToken = actionAST.token;
    	    this.outerAltNum = actionAST.outerAltNum;
    	}

    	public ActionTranslator(CodeGenerator generator,
    						    String ruleName,
    							antlr.Token actionToken,
    							int outerAltNum)
    	{
    		this(new ANTLRStringStream(actionToken.getText()));
    		this.generator = generator;
    		grammar = generator.grammar;
    	    this.enclosingRule = grammar.getRule(ruleName);
    	    this.actionToken = actionToken;
    		this.outerAltNum = outerAltNum;
    	}

    /*
    public ActionTranslator(CharStream input, CodeGenerator generator,
                  Grammar grammar, Rule enclosingRule,
                  antlr.Token actionToken, int outerAltNum)
    {
        this(input);
        this.grammar = grammar;
        this.generator = generator;
        this.enclosingRule = enclosingRule;
        this.actionToken = actionToken;
        this.outerAltNum = outerAltNum;
    }
    */

    /** Return a list of strings and StringTemplate objects that
     *  represent the translated action.
     */
    public List translateToChunks() {
    	// System.out.println("###\naction="+action);
    	Token t;
    	do {
    		t = nextToken();
    	} while ( t.getType()!= Token.EOF );
    	return chunks;
    }

    public String translate() {
    	List theChunks = translateToChunks();
    	//System.out.println("chunks="+a.chunks);
    	StringBuffer buf = new StringBuffer();
    	for (int i = 0; i < theChunks.size(); i++) {
    		Object o = (Object) theChunks.get(i);
    		buf.append(o);
    	}
    	//System.out.println("translated: "+buf.toString());
    	return buf.toString();
    }

    public List translateAction(String action) {
        ActionTranslator translator =
            new ActionTranslator(generator,
                                 enclosingRule.name,
                                 new antlr.CommonToken(ANTLRParser.ACTION,action),1);
        return translator.translateToChunks();
    }

    public boolean isTokenRefInAlt(String id) {
        return enclosingRule.getTokenRefsInAlt(id, outerAltNum)!=null;
    }
    public boolean isRuleRefInAlt(String id) {
        return enclosingRule.getRuleRefsInAlt(id, outerAltNum)!=null;
    }
    public Grammar.LabelElementPair getElementLabel(String id) {
        return enclosingRule.getLabel(id);
    }

    public void checkElementRefUniqueness(String ref, boolean isToken) {
    		List refs = null;
    		if ( isToken ) {
    		    refs = enclosingRule.getTokenRefsInAlt(ref, outerAltNum);
    		}
    		else {
    		    refs = enclosingRule.getRuleRefsInAlt(ref, outerAltNum);
    		}
    		if ( refs!=null && refs.size()>1 ) {
    			ErrorManager.grammarError(ErrorManager.MSG_NONUNIQUE_REF,
    									  grammar,
    									  actionToken,
    									  ref);
    		}
    }

    /** For $rulelabel.name, return the Attribute found for name.  It
     *  will be a predefined property or a return value.
     */
    public Attribute getRuleLabelAttribute(String ruleName, String attrName) {
    	Rule r = grammar.getRule(ruleName);
    	AttributeScope scope = null;
    	// is it a return value?
    	if ( r.returnScope!=null && r.returnScope.getAttribute(attrName)!=null ) {
    		scope = r.returnScope;
    	}
    	else if ( grammar.type != Grammar.LEXER &&
    		 RuleLabelScope.predefinedRulePropertiesScope.getAttribute(attrName)!=null )
    	{
    		scope = RuleLabelScope.predefinedRulePropertiesScope;
    	}
    	else if ( grammar.type == Grammar.LEXER &&
    		 RuleLabelScope.predefinedLexerRulePropertiesScope.getAttribute(attrName)!=null )
    	{
    		scope = RuleLabelScope.predefinedLexerRulePropertiesScope;
    	}
    	if ( scope!=null ) {
    		return scope.getAttribute(attrName);
    	}
    	return null;
    }

    AttributeScope resolveDynamicScope(String scopeName) {
    	if ( grammar.getGlobalScope(scopeName)!=null ) {
    		return grammar.getGlobalScope(scopeName);
    	}
    	Rule scopeRule = grammar.getRule(scopeName);
    	if ( scopeRule!=null ) {
    		return scopeRule.ruleScope;
    	}
    	return null; // not a valid dynamic scope
    }

    protected StringTemplate template(String name) {
    	StringTemplate st = generator.getTemplates().getInstanceOf(name);
    	chunks.add(st);
    	return st;
    }



    public ActionTranslator() {;}
    public ActionTranslator(CharStream input) {
        super(input);
        ruleMemo = new Map[52+1];
     }
    public String getGrammarFileName() { return "action.g"; }

    public Token nextToken() {
        while (true) {
            if ( input.LA(1)==CharStream.EOF ) {
                return Token.EOF_TOKEN;
            }
            token=null;
            tokenStartCharIndex = getCharIndex();
            try {
                int m = input.mark();
                backtracking=1;
                failed=false;
                mTokens();
                backtracking=0;

                if ( failed ) {
                    input.rewind(m);
                    input.consume();
                }
                else {
                    return token;
                }
            }
            catch (RecognitionException re) {
                // shouldn't happen in backtracking mode, but...
                reportError(re);
                recover(re);
            }
        }
    }

    public void memoize(IntStream input,
    		int ruleIndex,
    		int ruleStartIndex)
    {
    if ( backtracking>1 ) super.memoize(input, ruleIndex, ruleStartIndex);
    }

    public boolean alreadyParsedRule(IntStream input, int ruleIndex) {
    if ( backtracking>1 ) return super.alreadyParsedRule(input, ruleIndex);
    return false;
    }// $ANTLR start ENCLOSING_RULE_SCOPE_ATTR
    public void mENCLOSING_RULE_SCOPE_ATTR() throws RecognitionException {
        int ENCLOSING_RULE_SCOPE_ATTR_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = ENCLOSING_RULE_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 1) ) { return ; }
            // action.g:201:4: ( '$' x= ID '.' y= ID {...}?)
            // action.g:201:4: '$' x= ID '.' y= ID {...}?
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            if ( !(enclosingRule!=null &&
            	                         x.getText().equals(enclosingRule.name) &&
            	                         enclosingRule.getLocalAttributeScope(y.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "ENCLOSING_RULE_SCOPE_ATTR", "enclosingRule!=null &&\n\t                         $x.text.equals(enclosingRule.name) &&\n\t                         enclosingRule.getLocalAttributeScope($y.text)!=null");
            }
            if ( backtracking==1 ) {

              		StringTemplate st = null;
              		AttributeScope scope = enclosingRule.getLocalAttributeScope(y.getText());
              		if ( scope.isPredefinedRuleScope ) {
              			st = template("rulePropertyRef_"+y.getText());
              			grammar.referenceRuleLabelPredefinedAttribute(x.getText());
              			st.setAttribute("scope", x.getText());
              			st.setAttribute("attr", y.getText());
              		}
              	    else if ( scope.isPredefinedLexerRuleScope ) {
              	    	// perhaps not the most precise error message to use, but...
              			ErrorManager.grammarError(ErrorManager.MSG_RULE_HAS_NO_ARGS,
              									  grammar,
              									  actionToken,
              									  x.getText());
              	    }
              		else if ( scope.isParameterScope ) {
              			st = template("parameterAttributeRef");
              			st.setAttribute("attr", scope.getAttribute(y.getText()));
              		}
              		else { // must be return value
              			st = template("returnAttributeRef");
              			st.setAttribute("ruleDescriptor", enclosingRule);
              			st.setAttribute("attr", scope.getAttribute(y.getText()));
              		}

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 1, ENCLOSING_RULE_SCOPE_ATTR_StartIndex); }
        }
    }
    // $ANTLR end ENCLOSING_RULE_SCOPE_ATTR

    // $ANTLR start TOKEN_SCOPE_ATTR
    public void mTOKEN_SCOPE_ATTR() throws RecognitionException {
        int TOKEN_SCOPE_ATTR_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = TOKEN_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 2) ) { return ; }
            // action.g:235:4: ( '$' x= ID '.' y= ID {...}?)
            // action.g:235:4: '$' x= ID '.' y= ID {...}?
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            if ( !(enclosingRule!=null &&
            	                         (enclosingRule.getTokenLabel(x.getText())!=null||
            	                          isTokenRefInAlt(x.getText())) &&
            	                         AttributeScope.tokenScope.getAttribute(y.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "TOKEN_SCOPE_ATTR", "enclosingRule!=null &&\n\t                         (enclosingRule.getTokenLabel($x.text)!=null||\n\t                          isTokenRefInAlt($x.text)) &&\n\t                         AttributeScope.tokenScope.getAttribute($y.text)!=null");
            }
            if ( backtracking==1 ) {

              		String label = x.getText();
              		if ( enclosingRule.getTokenLabel(x.getText())==null ) {
              			// $tokenref.attr  gotta get old label or compute new one
              			checkElementRefUniqueness(x.getText(), true);
              			label = enclosingRule.getElementLabel(x.getText(), outerAltNum, generator);
              			if ( label==null ) {
              				ErrorManager.grammarError(ErrorManager.MSG_FORWARD_ELEMENT_REF,
              										  grammar,
              										  actionToken,
              										  "$"+x.getText()+"."+y.getText());
              				label = x.getText();
              			}
              		}
              		StringTemplate st = template("tokenLabelPropertyRef_"+y.getText());
              		st.setAttribute("scope", label);
              		st.setAttribute("attr", AttributeScope.tokenScope.getAttribute(y.getText()));

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 2, TOKEN_SCOPE_ATTR_StartIndex); }
        }
    }
    // $ANTLR end TOKEN_SCOPE_ATTR

    // $ANTLR start RULE_SCOPE_ATTR
    public void mRULE_SCOPE_ATTR() throws RecognitionException {
        int RULE_SCOPE_ATTR_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = RULE_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;

            Grammar.LabelElementPair pair=null;
            String refdRuleName=null;

            if ( backtracking>0 && alreadyParsedRule(input, 3) ) { return ; }
            // action.g:266:4: ( '$' x= ID '.' y= ID {...}?{...}?)
            // action.g:266:4: '$' x= ID '.' y= ID {...}?{...}?
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            if ( !(enclosingRule!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "RULE_SCOPE_ATTR", "enclosingRule!=null");
            }
            if ( backtracking==1 ) {

              		pair = enclosingRule.getRuleLabel(x.getText());
              		refdRuleName = x.getText();
              		if ( pair!=null ) {
              			refdRuleName = pair.referencedRuleName;
              		}

            }
            if ( !((enclosingRule.getRuleLabel(x.getText())!=null || isRuleRefInAlt(x.getText())) &&
            	      getRuleLabelAttribute(enclosingRule.getRuleLabel(x.getText())!=null?enclosingRule.getRuleLabel(x.getText()).referencedRuleName:x.getText(),y.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "RULE_SCOPE_ATTR", "(enclosingRule.getRuleLabel($x.text)!=null || isRuleRefInAlt($x.text)) &&\n\t      getRuleLabelAttribute(enclosingRule.getRuleLabel($x.text)!=null?enclosingRule.getRuleLabel($x.text).referencedRuleName:$x.text,$y.text)!=null");
            }
            if ( backtracking==1 ) {

              		String label = x.getText();
              		if ( pair==null ) {
              			// $ruleref.attr  gotta get old label or compute new one
              			checkElementRefUniqueness(x.getText(), false);
              			label = enclosingRule.getElementLabel(x.getText(), outerAltNum, generator);
              			if ( label==null ) {
              				ErrorManager.grammarError(ErrorManager.MSG_FORWARD_ELEMENT_REF,
              										  grammar,
              										  actionToken,
              										  "$"+x.getText()+"."+y.getText());
              				label = x.getText();
              			}
              		}
              		StringTemplate st;
              		Rule refdRule = grammar.getRule(refdRuleName);
              		AttributeScope scope = refdRule.getLocalAttributeScope(y.getText());
              		if ( scope.isPredefinedRuleScope ) {
              			st = template("ruleLabelPropertyRef_"+y.getText());
              			grammar.referenceRuleLabelPredefinedAttribute(refdRuleName);
              			st.setAttribute("scope", label);
              			st.setAttribute("attr", y.getText());
              		}
              		else if ( scope.isPredefinedLexerRuleScope ) {
              			st = template("lexerRuleLabelPropertyRef_"+y.getText());
              			grammar.referenceRuleLabelPredefinedAttribute(refdRuleName);
              			st.setAttribute("scope", label);
              			st.setAttribute("attr", y.getText());
              		}
              		else if ( scope.isParameterScope ) {
              			// TODO: error!
              		}
              		else {
              			st = template("ruleLabelRef");
              			st.setAttribute("referencedRule", refdRule);
              			st.setAttribute("scope", label);
              			st.setAttribute("attr", scope.getAttribute(y.getText()));
              		}

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 3, RULE_SCOPE_ATTR_StartIndex); }
        }
    }
    // $ANTLR end RULE_SCOPE_ATTR

    // $ANTLR start LABEL_REF
    public void mLABEL_REF() throws RecognitionException {
        int LABEL_REF_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = LABEL_REF;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 4) ) { return ; }
            // action.g:324:4: ( '$' ID {...}?)
            // action.g:324:4: '$' ID {...}?
            {
            match('$'); if (failed) return ;
            int ID1Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID1 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID1Start, getCharIndex()-1);
            if ( !(enclosingRule!=null &&
            	            getElementLabel(ID1.getText())!=null &&
            		        enclosingRule.getRuleLabel(ID1.getText())==null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "LABEL_REF", "enclosingRule!=null &&\n\t            getElementLabel($ID.text)!=null &&\n\t\t        enclosingRule.getRuleLabel($ID.text)==null");
            }
            if ( backtracking==1 ) {

              		StringTemplate st;
              		Grammar.LabelElementPair pair = getElementLabel(ID1.getText());
              		if ( pair.type==Grammar.TOKEN_LABEL ) {
              			st = template("tokenLabelRef");
              		}
              		else {
              			st = template("listLabelRef");
              		}
              		st.setAttribute("label", ID1.getText());

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 4, LABEL_REF_StartIndex); }
        }
    }
    // $ANTLR end LABEL_REF

    // $ANTLR start ISOLATED_TOKEN_REF
    public void mISOLATED_TOKEN_REF() throws RecognitionException {
        int ISOLATED_TOKEN_REF_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = ISOLATED_TOKEN_REF;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 5) ) { return ; }
            // action.g:343:4: ( '$' ID {...}?)
            // action.g:343:4: '$' ID {...}?
            {
            match('$'); if (failed) return ;
            int ID2Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID2 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID2Start, getCharIndex()-1);
            if ( !(grammar.type!=Grammar.LEXER && enclosingRule!=null && isTokenRefInAlt(ID2.getText())) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "ISOLATED_TOKEN_REF", "grammar.type!=Grammar.LEXER && enclosingRule!=null && isTokenRefInAlt($ID.text)");
            }
            if ( backtracking==1 ) {

              		String label = enclosingRule.getElementLabel(ID2.getText(), outerAltNum, generator);
              		checkElementRefUniqueness(ID2.getText(), true);
              		if ( label==null ) {
              			ErrorManager.grammarError(ErrorManager.MSG_FORWARD_ELEMENT_REF,
              									  grammar,
              									  actionToken,
              									  ID2.getText());
              		}
              		else {
              			StringTemplate st = template("tokenLabelRef");
              			st.setAttribute("label", label);
              		}

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 5, ISOLATED_TOKEN_REF_StartIndex); }
        }
    }
    // $ANTLR end ISOLATED_TOKEN_REF

    // $ANTLR start ISOLATED_LEXER_RULE_REF
    public void mISOLATED_LEXER_RULE_REF() throws RecognitionException {
        int ISOLATED_LEXER_RULE_REF_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = ISOLATED_LEXER_RULE_REF;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 6) ) { return ; }
            // action.g:363:4: ( '$' ID {...}?)
            // action.g:363:4: '$' ID {...}?
            {
            match('$'); if (failed) return ;
            int ID3Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID3 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID3Start, getCharIndex()-1);
            if ( !(grammar.type==Grammar.LEXER &&
            	             enclosingRule!=null &&
            	             isRuleRefInAlt(ID3.getText())) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "ISOLATED_LEXER_RULE_REF", "grammar.type==Grammar.LEXER &&\n\t             enclosingRule!=null &&\n\t             isRuleRefInAlt($ID.text)");
            }
            if ( backtracking==1 ) {

              		String label = enclosingRule.getElementLabel(ID3.getText(), outerAltNum, generator);
              		checkElementRefUniqueness(ID3.getText(), false);
              		if ( label==null ) {
              			ErrorManager.grammarError(ErrorManager.MSG_FORWARD_ELEMENT_REF,
              									  grammar,
              									  actionToken,
              									  ID3.getText());
              		}
              		else {
              			StringTemplate st = template("lexerRuleLabel");
              			st.setAttribute("label", label);
              		}

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 6, ISOLATED_LEXER_RULE_REF_StartIndex); }
        }
    }
    // $ANTLR end ISOLATED_LEXER_RULE_REF

    // $ANTLR start LOCAL_ATTR
    public void mLOCAL_ATTR() throws RecognitionException {
        int LOCAL_ATTR_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = LOCAL_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 7) ) { return ; }
            // action.g:395:4: ( '$' ID {...}?)
            // action.g:395:4: '$' ID {...}?
            {
            match('$'); if (failed) return ;
            int ID4Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID4 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID4Start, getCharIndex()-1);
            if ( !(enclosingRule!=null && enclosingRule.getLocalAttributeScope(ID4.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "LOCAL_ATTR", "enclosingRule!=null && enclosingRule.getLocalAttributeScope($ID.text)!=null");
            }
            if ( backtracking==1 ) {

              		StringTemplate st;
              		AttributeScope scope = enclosingRule.getLocalAttributeScope(ID4.getText());
              		if ( scope.isPredefinedRuleScope ) {
              			st = template("rulePropertyRef_"+ID4.getText());
              			grammar.referenceRuleLabelPredefinedAttribute(enclosingRule.name);
              			st.setAttribute("scope", enclosingRule.name);
              			st.setAttribute("attr", ID4.getText());
              		}
              		else if ( scope.isParameterScope ) {
              			st = template("parameterAttributeRef");
              			st.setAttribute("attr", scope.getAttribute(ID4.getText()));
              		}
              		else {
              			st = template("returnAttributeRef");
              			st.setAttribute("ruleDescriptor", enclosingRule);
              			st.setAttribute("attr", scope.getAttribute(ID4.getText()));
              		}

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 7, LOCAL_ATTR_StartIndex); }
        }
    }
    // $ANTLR end LOCAL_ATTR

    // $ANTLR start DYNAMIC_SCOPE_ATTR
    public void mDYNAMIC_SCOPE_ATTR() throws RecognitionException {
        int DYNAMIC_SCOPE_ATTR_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = DYNAMIC_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 8) ) { return ; }
            // action.g:431:4: ( '$' x= ID '::' y= ID {...}?)
            // action.g:431:4: '$' x= ID '::' y= ID {...}?
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match("::"); if (failed) return ;

            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            if ( !(resolveDynamicScope(x.getText())!=null &&
            						     resolveDynamicScope(x.getText()).getAttribute(y.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "DYNAMIC_SCOPE_ATTR", "resolveDynamicScope($x.text)!=null &&\n\t\t\t\t\t\t     resolveDynamicScope($x.text).getAttribute($y.text)!=null");
            }
            if ( backtracking==1 ) {

              		AttributeScope scope = resolveDynamicScope(x.getText());
              		if ( scope!=null ) {
              			StringTemplate st = template("scopeAttributeRef");
              			st.setAttribute("scope", x.getText());
              			st.setAttribute("attr",  scope.getAttribute(y.getText()));
              		}
              		else {
              			// error: invalid dynamic attribute
              		}

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 8, DYNAMIC_SCOPE_ATTR_StartIndex); }
        }
    }
    // $ANTLR end DYNAMIC_SCOPE_ATTR

    // $ANTLR start ERROR_SCOPED_XY
    public void mERROR_SCOPED_XY() throws RecognitionException {
        int ERROR_SCOPED_XY_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = ERROR_SCOPED_XY;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 9) ) { return ; }
            // action.g:448:4: ( '$' x= ID '::' y= ID )
            // action.g:448:4: '$' x= ID '::' y= ID
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match("::"); if (failed) return ;

            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            if ( backtracking==1 ) {

              		chunks.add(getText());
              		generator.issueInvalidScopeError(x.getText(),y.getText(),
              		                                 enclosingRule,actionToken,
              		                                 outerAltNum);

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 9, ERROR_SCOPED_XY_StartIndex); }
        }
    }
    // $ANTLR end ERROR_SCOPED_XY

    // $ANTLR start DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR
    public void mDYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR() throws RecognitionException {
        int DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 10) ) { return ; }
            // action.g:466:4: ( '$' x= ID '[' '-' expr= SCOPE_INDEX_EXPR ']' '::' y= ID )
            // action.g:466:4: '$' x= ID '[' '-' expr= SCOPE_INDEX_EXPR ']' '::' y= ID
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('['); if (failed) return ;
            match('-'); if (failed) return ;
            int exprStart = getCharIndex();
            mSCOPE_INDEX_EXPR(); if (failed) return ;
            Token expr = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, exprStart, getCharIndex()-1);
            match(']'); if (failed) return ;
            match("::"); if (failed) return ;

            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            if ( backtracking==1 ) {

              		StringTemplate st = template("scopeAttributeRef");
              		st.setAttribute("scope",    x.getText());
              		st.setAttribute("attr",     resolveDynamicScope(x.getText()).getAttribute(y.getText()));
              		st.setAttribute("negIndex", expr.getText());

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 10, DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR_StartIndex); }
        }
    }
    // $ANTLR end DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR

    // $ANTLR start DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR
    public void mDYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR() throws RecognitionException {
        int DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 11) ) { return ; }
            // action.g:477:4: ( '$' x= ID '[' expr= SCOPE_INDEX_EXPR ']' '::' y= ID )
            // action.g:477:4: '$' x= ID '[' expr= SCOPE_INDEX_EXPR ']' '::' y= ID
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('['); if (failed) return ;
            int exprStart = getCharIndex();
            mSCOPE_INDEX_EXPR(); if (failed) return ;
            Token expr = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, exprStart, getCharIndex()-1);
            match(']'); if (failed) return ;
            match("::"); if (failed) return ;

            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            if ( backtracking==1 ) {

              		StringTemplate st = template("scopeAttributeRef");
              		st.setAttribute("scope", x.getText());
              		st.setAttribute("attr",  resolveDynamicScope(x.getText()).getAttribute(y.getText()));
              		st.setAttribute("index", expr.getText());

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 11, DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR_StartIndex); }
        }
    }
    // $ANTLR end DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR

    // $ANTLR start SCOPE_INDEX_EXPR
    public void mSCOPE_INDEX_EXPR() throws RecognitionException {
        int SCOPE_INDEX_EXPR_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            if ( backtracking>0 && alreadyParsedRule(input, 12) ) { return ; }
            // action.g:489:4: ( (~ ']' )+ )
            // action.g:489:4: (~ ']' )+
            {
            // action.g:489:4: (~ ']' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);
                if ( ((LA1_0>='\u0000' && LA1_0<='\\')||(LA1_0>='^' && LA1_0<='\uFFFE')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // action.g:489:5: ~ ']'
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='\\')||(input.LA(1)>='^' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();
            	    failed=false;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return ;}
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);


            }

        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 12, SCOPE_INDEX_EXPR_StartIndex); }
        }
    }
    // $ANTLR end SCOPE_INDEX_EXPR

    // $ANTLR start ISOLATED_DYNAMIC_SCOPE
    public void mISOLATED_DYNAMIC_SCOPE() throws RecognitionException {
        int ISOLATED_DYNAMIC_SCOPE_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = ISOLATED_DYNAMIC_SCOPE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 13) ) { return ; }
            // action.g:498:4: ( '$' ID {...}?)
            // action.g:498:4: '$' ID {...}?
            {
            match('$'); if (failed) return ;
            int ID5Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID5 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID5Start, getCharIndex()-1);
            if ( !(resolveDynamicScope(ID5.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "ISOLATED_DYNAMIC_SCOPE", "resolveDynamicScope($ID.text)!=null");
            }
            if ( backtracking==1 ) {

              		StringTemplate st = template("isolatedDynamicScopeRef");
              		st.setAttribute("scope", ID5.getText());

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 13, ISOLATED_DYNAMIC_SCOPE_StartIndex); }
        }
    }
    // $ANTLR end ISOLATED_DYNAMIC_SCOPE

    // $ANTLR start TEMPLATE_INSTANCE
    public void mTEMPLATE_INSTANCE() throws RecognitionException {
        int TEMPLATE_INSTANCE_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = TEMPLATE_INSTANCE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 14) ) { return ; }
            // action.g:511:4: ( '%' ID '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')' )
            // action.g:511:4: '%' ID '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')'
            {
            match('%'); if (failed) return ;
            mID(); if (failed) return ;
            match('('); if (failed) return ;
            // action.g:511:15: ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )?
            int alt6=2;
            int LA6_0 = input.LA(1);
            if ( ((LA6_0>='\t' && LA6_0<='\n')||LA6_0==' '||(LA6_0>='A' && LA6_0<='Z')||LA6_0=='_'||(LA6_0>='a' && LA6_0<='z')) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // action.g:511:17: ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )?
                    {
                    // action.g:511:17: ( WS )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);
                    if ( ((LA2_0>='\t' && LA2_0<='\n')||LA2_0==' ') ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // action.g:511:17: WS
                            {
                            mWS(); if (failed) return ;

                            }
                            break;

                    }

                    mARG(); if (failed) return ;
                    // action.g:511:25: ( ',' ( WS )? ARG )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);
                        if ( (LA4_0==',') ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // action.g:511:26: ',' ( WS )? ARG
                    	    {
                    	    match(','); if (failed) return ;
                    	    // action.g:511:30: ( WS )?
                    	    int alt3=2;
                    	    int LA3_0 = input.LA(1);
                    	    if ( ((LA3_0>='\t' && LA3_0<='\n')||LA3_0==' ') ) {
                    	        alt3=1;
                    	    }
                    	    switch (alt3) {
                    	        case 1 :
                    	            // action.g:511:30: WS
                    	            {
                    	            mWS(); if (failed) return ;

                    	            }
                    	            break;

                    	    }

                    	    mARG(); if (failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop4;
                        }
                    } while (true);

                    // action.g:511:40: ( WS )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);
                    if ( ((LA5_0>='\t' && LA5_0<='\n')||LA5_0==' ') ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // action.g:511:40: WS
                            {
                            mWS(); if (failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }

            match(')'); if (failed) return ;
            if ( backtracking==1 ) {

              		String action = getText().substring(1,getText().length());
              		StringTemplate st =
              			generator.translateTemplateConstructor(enclosingRule.name,
              												   outerAltNum,
              												   actionToken,
              												   action);
              		chunks.add(st);

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 14, TEMPLATE_INSTANCE_StartIndex); }
        }
    }
    // $ANTLR end TEMPLATE_INSTANCE

    // $ANTLR start INDIRECT_TEMPLATE_INSTANCE
    public void mINDIRECT_TEMPLATE_INSTANCE() throws RecognitionException {
        int INDIRECT_TEMPLATE_INSTANCE_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = INDIRECT_TEMPLATE_INSTANCE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 15) ) { return ; }
            // action.g:526:4: ( '%' '(' ACTION ')' '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')' )
            // action.g:526:4: '%' '(' ACTION ')' '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')'
            {
            match('%'); if (failed) return ;
            match('('); if (failed) return ;
            mACTION(); if (failed) return ;
            match(')'); if (failed) return ;
            match('('); if (failed) return ;
            // action.g:526:27: ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )?
            int alt11=2;
            int LA11_0 = input.LA(1);
            if ( ((LA11_0>='\t' && LA11_0<='\n')||LA11_0==' '||(LA11_0>='A' && LA11_0<='Z')||LA11_0=='_'||(LA11_0>='a' && LA11_0<='z')) ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // action.g:526:29: ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )?
                    {
                    // action.g:526:29: ( WS )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);
                    if ( ((LA7_0>='\t' && LA7_0<='\n')||LA7_0==' ') ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // action.g:526:29: WS
                            {
                            mWS(); if (failed) return ;

                            }
                            break;

                    }

                    mARG(); if (failed) return ;
                    // action.g:526:37: ( ',' ( WS )? ARG )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);
                        if ( (LA9_0==',') ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // action.g:526:38: ',' ( WS )? ARG
                    	    {
                    	    match(','); if (failed) return ;
                    	    // action.g:526:42: ( WS )?
                    	    int alt8=2;
                    	    int LA8_0 = input.LA(1);
                    	    if ( ((LA8_0>='\t' && LA8_0<='\n')||LA8_0==' ') ) {
                    	        alt8=1;
                    	    }
                    	    switch (alt8) {
                    	        case 1 :
                    	            // action.g:526:42: WS
                    	            {
                    	            mWS(); if (failed) return ;

                    	            }
                    	            break;

                    	    }

                    	    mARG(); if (failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);

                    // action.g:526:52: ( WS )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);
                    if ( ((LA10_0>='\t' && LA10_0<='\n')||LA10_0==' ') ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // action.g:526:52: WS
                            {
                            mWS(); if (failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }

            match(')'); if (failed) return ;
            if ( backtracking==1 ) {

              		String action = getText().substring(1,getText().length());
              		StringTemplate st =
              			generator.translateTemplateConstructor(enclosingRule.name,
              												   outerAltNum,
              												   actionToken,
              												   action);
              		chunks.add(st);

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 15, INDIRECT_TEMPLATE_INSTANCE_StartIndex); }
        }
    }
    // $ANTLR end INDIRECT_TEMPLATE_INSTANCE

    // $ANTLR start ARG
    public void mARG() throws RecognitionException {
        int ARG_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            if ( backtracking>0 && alreadyParsedRule(input, 16) ) { return ; }
            // action.g:540:7: ( ID '=' ACTION )
            // action.g:540:7: ID '=' ACTION
            {
            mID(); if (failed) return ;
            match('='); if (failed) return ;
            mACTION(); if (failed) return ;

            }

        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 16, ARG_StartIndex); }
        }
    }
    // $ANTLR end ARG

    // $ANTLR start SET_EXPR_ATTRIBUTE
    public void mSET_EXPR_ATTRIBUTE() throws RecognitionException {
        int SET_EXPR_ATTRIBUTE_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = SET_EXPR_ATTRIBUTE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 17) ) { return ; }
            // action.g:545:4: ( '%' a= ACTION '.' ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' )
            // action.g:545:4: '%' a= ACTION '.' ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';'
            {
            match('%'); if (failed) return ;
            int aStart = getCharIndex();
            mACTION(); if (failed) return ;
            Token a = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, aStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int ID6Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID6 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID6Start, getCharIndex()-1);
            // action.g:545:24: ( WS )?
            int alt12=2;
            int LA12_0 = input.LA(1);
            if ( ((LA12_0>='\t' && LA12_0<='\n')||LA12_0==' ') ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // action.g:545:24: WS
                    {
                    mWS(); if (failed) return ;

                    }
                    break;

            }

            match('='); if (failed) return ;
            int exprStart = getCharIndex();
            mATTR_VALUE_EXPR(); if (failed) return ;
            Token expr = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, exprStart, getCharIndex()-1);
            match(';'); if (failed) return ;
            if ( backtracking==1 ) {

              		StringTemplate st = template("actionSetAttribute");
              		String action = a.getText();
              		action = action.substring(1,action.length()-1); // stuff inside {...}
              		st.setAttribute("st", translateAction(action));
              		st.setAttribute("attrName", ID6.getText());
              		st.setAttribute("expr", translateAction(expr.getText()));

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 17, SET_EXPR_ATTRIBUTE_StartIndex); }
        }
    }
    // $ANTLR end SET_EXPR_ATTRIBUTE

    // $ANTLR start SET_ATTRIBUTE
    public void mSET_ATTRIBUTE() throws RecognitionException {
        int SET_ATTRIBUTE_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = SET_ATTRIBUTE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 18) ) { return ; }
            // action.g:562:4: ( '%' x= ID '.' y= ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' )
            // action.g:562:4: '%' x= ID '.' y= ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';'
            {
            match('%'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            // action.g:562:22: ( WS )?
            int alt13=2;
            int LA13_0 = input.LA(1);
            if ( ((LA13_0>='\t' && LA13_0<='\n')||LA13_0==' ') ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // action.g:562:22: WS
                    {
                    mWS(); if (failed) return ;

                    }
                    break;

            }

            match('='); if (failed) return ;
            int exprStart = getCharIndex();
            mATTR_VALUE_EXPR(); if (failed) return ;
            Token expr = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, exprStart, getCharIndex()-1);
            match(';'); if (failed) return ;
            if ( backtracking==1 ) {

              		StringTemplate st = template("actionSetAttribute");
              		st.setAttribute("st", x.getText());
              		st.setAttribute("attrName", y.getText());
              		st.setAttribute("expr", translateAction(expr.getText()));

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 18, SET_ATTRIBUTE_StartIndex); }
        }
    }
    // $ANTLR end SET_ATTRIBUTE

    // $ANTLR start ATTR_VALUE_EXPR
    public void mATTR_VALUE_EXPR() throws RecognitionException {
        int ATTR_VALUE_EXPR_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            if ( backtracking>0 && alreadyParsedRule(input, 19) ) { return ; }
            // action.g:574:4: ( (~ ';' )+ )
            // action.g:574:4: (~ ';' )+
            {
            // action.g:574:4: (~ ';' )+
            int cnt14=0;
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);
                if ( ((LA14_0>='\u0000' && LA14_0<=':')||(LA14_0>='<' && LA14_0<='\uFFFE')) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // action.g:574:5: ~ ';'
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<=':')||(input.LA(1)>='<' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();
            	    failed=false;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return ;}
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt14 >= 1 ) break loop14;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(14, input);
                        throw eee;
                }
                cnt14++;
            } while (true);


            }

        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 19, ATTR_VALUE_EXPR_StartIndex); }
        }
    }
    // $ANTLR end ATTR_VALUE_EXPR

    // $ANTLR start TEMPLATE_EXPR
    public void mTEMPLATE_EXPR() throws RecognitionException {
        int TEMPLATE_EXPR_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = TEMPLATE_EXPR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 20) ) { return ; }
            // action.g:579:4: ( '%' a= ACTION )
            // action.g:579:4: '%' a= ACTION
            {
            match('%'); if (failed) return ;
            int aStart = getCharIndex();
            mACTION(); if (failed) return ;
            Token a = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, aStart, getCharIndex()-1);
            if ( backtracking==1 ) {

              		StringTemplate st = template("actionStringConstructor");
              		String action = a.getText();
              		action = action.substring(1,action.length()-1); // stuff inside {...}
              		st.setAttribute("stringExpr", translateAction(action));

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 20, TEMPLATE_EXPR_StartIndex); }
        }
    }
    // $ANTLR end TEMPLATE_EXPR

    // $ANTLR start ACTION
    public void mACTION() throws RecognitionException {
        int ACTION_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            if ( backtracking>0 && alreadyParsedRule(input, 21) ) { return ; }
            // action.g:591:4: ( '{' ( options {greedy=false; } : . )* '}' )
            // action.g:591:4: '{' ( options {greedy=false; } : . )* '}'
            {
            match('{'); if (failed) return ;
            // action.g:591:8: ( options {greedy=false; } : . )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);
                if ( (LA15_0=='}') ) {
                    alt15=2;
                }
                else if ( ((LA15_0>='\u0000' && LA15_0<='|')||(LA15_0>='~' && LA15_0<='\uFFFE')) ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // action.g:591:33: .
            	    {
            	    matchAny(); if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);

            match('}'); if (failed) return ;

            }

        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 21, ACTION_StartIndex); }
        }
    }
    // $ANTLR end ACTION

    // $ANTLR start ESC
    public void mESC() throws RecognitionException {
        int ESC_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = ESC;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 22) ) { return ; }
            // action.g:594:9: ( '\\\\' '$' | '\\\\' '%' )
            int alt16=2;
            int LA16_0 = input.LA(1);
            if ( (LA16_0=='\\') ) {
                int LA16_1 = input.LA(2);
                if ( (LA16_1=='%') ) {
                    alt16=2;
                }
                else if ( (LA16_1=='$') ) {
                    alt16=1;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("594:1: ESC : ( '\\\\' '$' | '\\\\' '%' );", 16, 1, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("594:1: ESC : ( '\\\\' '$' | '\\\\' '%' );", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // action.g:594:9: '\\\\' '$'
                    {
                    match('\\'); if (failed) return ;
                    match('$'); if (failed) return ;
                    if ( backtracking==1 ) {
                      chunks.add("$");
                    }

                    }
                    break;
                case 2 :
                    // action.g:595:4: '\\\\' '%'
                    {
                    match('\\'); if (failed) return ;
                    match('%'); if (failed) return ;
                    if ( backtracking==1 ) {
                      chunks.add("%");
                    }

                    }
                    break;

            }

            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 22, ESC_StartIndex); }
        }
    }
    // $ANTLR end ESC

    // $ANTLR start ERROR_XY
    public void mERROR_XY() throws RecognitionException {
        int ERROR_XY_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = ERROR_XY;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 23) ) { return ; }
            // action.g:599:4: ( '$' x= ID '.' y= ID )
            // action.g:599:4: '$' x= ID '.' y= ID
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            if ( backtracking==1 ) {

              		chunks.add(getText());
              		generator.issueInvalidAttributeError(x.getText(),y.getText(),
              		                                     enclosingRule,actionToken,
              		                                     outerAltNum);

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 23, ERROR_XY_StartIndex); }
        }
    }
    // $ANTLR end ERROR_XY

    // $ANTLR start ERROR_X
    public void mERROR_X() throws RecognitionException {
        int ERROR_X_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = ERROR_X;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 24) ) { return ; }
            // action.g:609:4: ( '$' x= ID )
            // action.g:609:4: '$' x= ID
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            if ( backtracking==1 ) {

              		chunks.add(getText());
              		generator.issueInvalidAttributeError(x.getText(),
              		                                     enclosingRule,actionToken,
              		                                     outerAltNum);

            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 24, ERROR_X_StartIndex); }
        }
    }
    // $ANTLR end ERROR_X

    // $ANTLR start UNKNOWN_SYNTAX
    public void mUNKNOWN_SYNTAX() throws RecognitionException {
        int UNKNOWN_SYNTAX_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = UNKNOWN_SYNTAX;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 25) ) { return ; }
            // action.g:619:4: ( '$' | '%' ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )* )
            int alt18=2;
            int LA18_0 = input.LA(1);
            if ( (LA18_0=='$') ) {
                alt18=1;
            }
            else if ( (LA18_0=='%') ) {
                alt18=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("618:1: UNKNOWN_SYNTAX : ( '$' | '%' ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )* );", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // action.g:619:4: '$'
                    {
                    match('$'); if (failed) return ;
                    if ( backtracking==1 ) {

                      		chunks.add(getText());
                      		// shouldn't need an error here.  Just accept $ if it doesn't look like anything

                    }

                    }
                    break;
                case 2 :
                    // action.g:624:4: '%' ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )*
                    {
                    match('%'); if (failed) return ;
                    // action.g:624:8: ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )*
                    loop17:
                    do {
                        int alt17=9;
                        switch ( input.LA(1) ) {
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
                        case 'c':
                        case 'd':
                        case 'e':
                        case 'f':
                        case 'g':
                        case 'h':
                        case 'i':
                        case 'j':
                        case 'k':
                        case 'l':
                        case 'm':
                        case 'n':
                        case 'o':
                        case 'p':
                        case 'q':
                        case 'r':
                        case 's':
                        case 't':
                        case 'u':
                        case 'v':
                        case 'w':
                        case 'x':
                        case 'y':
                        case 'z':
                            alt17=1;
                            break;
                        case '.':
                            alt17=2;
                            break;
                        case '(':
                            alt17=3;
                            break;
                        case ')':
                            alt17=4;
                            break;
                        case ',':
                            alt17=5;
                            break;
                        case '{':
                            alt17=6;
                            break;
                        case '}':
                            alt17=7;
                            break;
                        case '\"':
                            alt17=8;
                            break;

                        }

                        switch (alt17) {
                    	case 1 :
                    	    // action.g:624:9: ID
                    	    {
                    	    mID(); if (failed) return ;

                    	    }
                    	    break;
                    	case 2 :
                    	    // action.g:624:12: '.'
                    	    {
                    	    match('.'); if (failed) return ;

                    	    }
                    	    break;
                    	case 3 :
                    	    // action.g:624:16: '('
                    	    {
                    	    match('('); if (failed) return ;

                    	    }
                    	    break;
                    	case 4 :
                    	    // action.g:624:20: ')'
                    	    {
                    	    match(')'); if (failed) return ;

                    	    }
                    	    break;
                    	case 5 :
                    	    // action.g:624:24: ','
                    	    {
                    	    match(','); if (failed) return ;

                    	    }
                    	    break;
                    	case 6 :
                    	    // action.g:624:28: '{'
                    	    {
                    	    match('{'); if (failed) return ;

                    	    }
                    	    break;
                    	case 7 :
                    	    // action.g:624:32: '}'
                    	    {
                    	    match('}'); if (failed) return ;

                    	    }
                    	    break;
                    	case 8 :
                    	    // action.g:624:36: '\"'
                    	    {
                    	    match('\"'); if (failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop17;
                        }
                    } while (true);

                    if ( backtracking==1 ) {

                      		chunks.add(getText());
                      		ErrorManager.grammarError(ErrorManager.MSG_INVALID_TEMPLATE_ACTION,
                      								  grammar,
                      								  actionToken,
                      								  getText());

                    }

                    }
                    break;

            }

            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 25, UNKNOWN_SYNTAX_StartIndex); }
        }
    }
    // $ANTLR end UNKNOWN_SYNTAX

    // $ANTLR start TEXT
    public void mTEXT() throws RecognitionException {
        int TEXT_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            int type = TEXT;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 26) ) { return ; }
            // action.g:634:7: ( (~ ('$'|'%'|'\\\\'))+ )
            // action.g:634:7: (~ ('$'|'%'|'\\\\'))+
            {
            // action.g:634:7: (~ ('$'|'%'|'\\\\'))+
            int cnt19=0;
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);
                if ( ((LA19_0>='\u0000' && LA19_0<='#')||(LA19_0>='&' && LA19_0<='[')||(LA19_0>=']' && LA19_0<='\uFFFE')) ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // action.g:634:7: ~ ('$'|'%'|'\\\\')
            	    {
            	    if ( (input.LA(1)>='\u0000' && input.LA(1)<='#')||(input.LA(1)>='&' && input.LA(1)<='[')||(input.LA(1)>=']' && input.LA(1)<='\uFFFE') ) {
            	        input.consume();
            	    failed=false;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return ;}
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt19 >= 1 ) break loop19;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(19, input);
                        throw eee;
                }
                cnt19++;
            } while (true);

            if ( backtracking==1 ) {
              chunks.add(getText());
            }

            }


            if ( token==null && ruleNestingLevel==1 ) {
                emit(type,line,charPosition,channel,start,getCharIndex()-1);
            }
        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 26, TEXT_StartIndex); }
        }
    }
    // $ANTLR end TEXT

    // $ANTLR start ID
    public void mID() throws RecognitionException {
        int ID_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            if ( backtracking>0 && alreadyParsedRule(input, 27) ) { return ; }
            // action.g:638:9: ( ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))* )
            // action.g:638:9: ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();
            failed=false;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                MismatchedSetException mse =
                    new MismatchedSetException(null,input);
                recover(mse);    throw mse;
            }

            // action.g:638:33: ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);
                if ( ((LA20_0>='0' && LA20_0<='9')||(LA20_0>='A' && LA20_0<='Z')||LA20_0=='_'||(LA20_0>='a' && LA20_0<='z')) ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // action.g:638:34: ('a'..'z'|'A'..'Z'|'_'|'0'..'9')
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();
            	    failed=false;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return ;}
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop20;
                }
            } while (true);


            }

        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 27, ID_StartIndex); }
        }
    }
    // $ANTLR end ID

    // $ANTLR start INT
    public void mINT() throws RecognitionException {
        int INT_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            if ( backtracking>0 && alreadyParsedRule(input, 28) ) { return ; }
            // action.g:642:7: ( ( '0' .. '9' )+ )
            // action.g:642:7: ( '0' .. '9' )+
            {
            // action.g:642:7: ( '0' .. '9' )+
            int cnt21=0;
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);
                if ( ((LA21_0>='0' && LA21_0<='9')) ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // action.g:642:7: '0' .. '9'
            	    {
            	    matchRange('0','9'); if (failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt21 >= 1 ) break loop21;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(21, input);
                        throw eee;
                }
                cnt21++;
            } while (true);


            }

        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 28, INT_StartIndex); }
        }
    }
    // $ANTLR end INT

    // $ANTLR start WS
    public void mWS() throws RecognitionException {
        int WS_StartIndex = input.index();
        try {
            ruleNestingLevel++;
            if ( backtracking>0 && alreadyParsedRule(input, 29) ) { return ; }
            // action.g:646:6: ( ( (' '|'\\t'|'\\n'))+ )
            // action.g:646:6: ( (' '|'\\t'|'\\n'))+
            {
            // action.g:646:6: ( (' '|'\\t'|'\\n'))+
            int cnt22=0;
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);
                if ( ((LA22_0>='\t' && LA22_0<='\n')||LA22_0==' ') ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // action.g:646:7: (' '|'\\t'|'\\n')
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)==' ' ) {
            	        input.consume();
            	    failed=false;
            	    }
            	    else {
            	        if (backtracking>0) {failed=true; return ;}
            	        MismatchedSetException mse =
            	            new MismatchedSetException(null,input);
            	        recover(mse);    throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt22 >= 1 ) break loop22;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(22, input);
                        throw eee;
                }
                cnt22++;
            } while (true);


            }

        }
        finally {
            ruleNestingLevel--;
            if ( backtracking>0 ) { memoize(input, 29, WS_StartIndex); }
        }
    }
    // $ANTLR end WS

    public void mTokens() throws RecognitionException {
        // action.g:1:25: ( ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( LOCAL_ATTR )=> LOCAL_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT )
        int alt23=22;
        int LA23_0 = input.LA(1);
        if ( (LA23_0=='$') ) {
            if ( (synpred1()) ) {
                alt23=1;
            }
            else if ( (synpred2()) ) {
                alt23=2;
            }
            else if ( (synpred3()) ) {
                alt23=3;
            }
            else if ( (synpred4()) ) {
                alt23=4;
            }
            else if ( (synpred5()) ) {
                alt23=5;
            }
            else if ( (synpred6()) ) {
                alt23=6;
            }
            else if ( (synpred7()) ) {
                alt23=7;
            }
            else if ( (synpred8()) ) {
                alt23=8;
            }
            else if ( (synpred9()) ) {
                alt23=9;
            }
            else if ( (synpred10()) ) {
                alt23=10;
            }
            else if ( (synpred11()) ) {
                alt23=11;
            }
            else if ( (synpred12()) ) {
                alt23=12;
            }
            else if ( (synpred19()) ) {
                alt23=19;
            }
            else if ( (synpred20()) ) {
                alt23=20;
            }
            else if ( (synpred21()) ) {
                alt23=21;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("1:1: Tokens options {k=1; } : ( ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( LOCAL_ATTR )=> LOCAL_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT );", 23, 1, input);

                throw nvae;
            }
        }
        else if ( (LA23_0=='%') ) {
            if ( (synpred13()) ) {
                alt23=13;
            }
            else if ( (synpred14()) ) {
                alt23=14;
            }
            else if ( (synpred15()) ) {
                alt23=15;
            }
            else if ( (synpred16()) ) {
                alt23=16;
            }
            else if ( (synpred17()) ) {
                alt23=17;
            }
            else if ( (synpred21()) ) {
                alt23=21;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("1:1: Tokens options {k=1; } : ( ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( LOCAL_ATTR )=> LOCAL_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT );", 23, 2, input);

                throw nvae;
            }
        }
        else if ( (LA23_0=='\\') ) {
            alt23=18;
        }
        else if ( ((LA23_0>='\u0000' && LA23_0<='#')||(LA23_0>='&' && LA23_0<='[')||(LA23_0>=']' && LA23_0<='\uFFFE')) ) {
            alt23=22;
        }
        else {
            if (backtracking>0) {failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("1:1: Tokens options {k=1; } : ( ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( LOCAL_ATTR )=> LOCAL_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT );", 23, 0, input);

            throw nvae;
        }
        switch (alt23) {
            case 1 :
                // action.g:1:25: ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR
                {

                mENCLOSING_RULE_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 2 :
                // action.g:1:80: ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR
                {

                mTOKEN_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 3 :
                // action.g:1:117: ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR
                {

                mRULE_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 4 :
                // action.g:1:152: ( LABEL_REF )=> LABEL_REF
                {

                mLABEL_REF(); if (failed) return ;

                }
                break;
            case 5 :
                // action.g:1:175: ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF
                {

                mISOLATED_TOKEN_REF(); if (failed) return ;

                }
                break;
            case 6 :
                // action.g:1:216: ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF
                {

                mISOLATED_LEXER_RULE_REF(); if (failed) return ;

                }
                break;
            case 7 :
                // action.g:1:267: ( LOCAL_ATTR )=> LOCAL_ATTR
                {

                mLOCAL_ATTR(); if (failed) return ;

                }
                break;
            case 8 :
                // action.g:1:292: ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR
                {

                mDYNAMIC_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 9 :
                // action.g:1:333: ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY
                {

                mERROR_SCOPED_XY(); if (failed) return ;

                }
                break;
            case 10 :
                // action.g:1:368: ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR
                {

                mDYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 11 :
                // action.g:1:443: ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR
                {

                mDYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 12 :
                // action.g:1:518: ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE
                {

                mISOLATED_DYNAMIC_SCOPE(); if (failed) return ;

                }
                break;
            case 13 :
                // action.g:1:567: ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE
                {

                mTEMPLATE_INSTANCE(); if (failed) return ;

                }
                break;
            case 14 :
                // action.g:1:606: ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE
                {

                mINDIRECT_TEMPLATE_INSTANCE(); if (failed) return ;

                }
                break;
            case 15 :
                // action.g:1:663: ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE
                {

                mSET_EXPR_ATTRIBUTE(); if (failed) return ;

                }
                break;
            case 16 :
                // action.g:1:704: ( SET_ATTRIBUTE )=> SET_ATTRIBUTE
                {

                mSET_ATTRIBUTE(); if (failed) return ;

                }
                break;
            case 17 :
                // action.g:1:735: ( TEMPLATE_EXPR )=> TEMPLATE_EXPR
                {

                mTEMPLATE_EXPR(); if (failed) return ;

                }
                break;
            case 18 :
                // action.g:1:766: ( ESC )=> ESC
                {

                mESC(); if (failed) return ;

                }
                break;
            case 19 :
                // action.g:1:777: ( ERROR_XY )=> ERROR_XY
                {

                mERROR_XY(); if (failed) return ;

                }
                break;
            case 20 :
                // action.g:1:798: ( ERROR_X )=> ERROR_X
                {

                mERROR_X(); if (failed) return ;

                }
                break;
            case 21 :
                // action.g:1:817: ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX
                {

                mUNKNOWN_SYNTAX(); if (failed) return ;

                }
                break;
            case 22 :
                // action.g:1:850: ( TEXT )=> TEXT
                {

                mTEXT(); if (failed) return ;

                }
                break;

        }

    }

    // $ANTLR start synpred1_fragment
    public void synpred1_fragment() throws RecognitionException {
        // action.g:1:25: ( ENCLOSING_RULE_SCOPE_ATTR )
        // action.g:1:26: ENCLOSING_RULE_SCOPE_ATTR
        {
        mENCLOSING_RULE_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred1_fragment

    // $ANTLR start synpred2_fragment
    public void synpred2_fragment() throws RecognitionException {
        // action.g:1:80: ( TOKEN_SCOPE_ATTR )
        // action.g:1:81: TOKEN_SCOPE_ATTR
        {
        mTOKEN_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred2_fragment

    // $ANTLR start synpred3_fragment
    public void synpred3_fragment() throws RecognitionException {
        // action.g:1:117: ( RULE_SCOPE_ATTR )
        // action.g:1:118: RULE_SCOPE_ATTR
        {
        mRULE_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred3_fragment

    // $ANTLR start synpred4_fragment
    public void synpred4_fragment() throws RecognitionException {
        // action.g:1:152: ( LABEL_REF )
        // action.g:1:153: LABEL_REF
        {
        mLABEL_REF(); if (failed) return ;

        }
    }
    // $ANTLR end synpred4_fragment

    // $ANTLR start synpred5_fragment
    public void synpred5_fragment() throws RecognitionException {
        // action.g:1:175: ( ISOLATED_TOKEN_REF )
        // action.g:1:176: ISOLATED_TOKEN_REF
        {
        mISOLATED_TOKEN_REF(); if (failed) return ;

        }
    }
    // $ANTLR end synpred5_fragment

    // $ANTLR start synpred6_fragment
    public void synpred6_fragment() throws RecognitionException {
        // action.g:1:216: ( ISOLATED_LEXER_RULE_REF )
        // action.g:1:217: ISOLATED_LEXER_RULE_REF
        {
        mISOLATED_LEXER_RULE_REF(); if (failed) return ;

        }
    }
    // $ANTLR end synpred6_fragment

    // $ANTLR start synpred7_fragment
    public void synpred7_fragment() throws RecognitionException {
        // action.g:1:267: ( LOCAL_ATTR )
        // action.g:1:268: LOCAL_ATTR
        {
        mLOCAL_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred7_fragment

    // $ANTLR start synpred8_fragment
    public void synpred8_fragment() throws RecognitionException {
        // action.g:1:292: ( DYNAMIC_SCOPE_ATTR )
        // action.g:1:293: DYNAMIC_SCOPE_ATTR
        {
        mDYNAMIC_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred8_fragment

    // $ANTLR start synpred9_fragment
    public void synpred9_fragment() throws RecognitionException {
        // action.g:1:333: ( ERROR_SCOPED_XY )
        // action.g:1:334: ERROR_SCOPED_XY
        {
        mERROR_SCOPED_XY(); if (failed) return ;

        }
    }
    // $ANTLR end synpred9_fragment

    // $ANTLR start synpred10_fragment
    public void synpred10_fragment() throws RecognitionException {
        // action.g:1:368: ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )
        // action.g:1:369: DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR
        {
        mDYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred10_fragment

    // $ANTLR start synpred11_fragment
    public void synpred11_fragment() throws RecognitionException {
        // action.g:1:443: ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )
        // action.g:1:444: DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR
        {
        mDYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred11_fragment

    // $ANTLR start synpred12_fragment
    public void synpred12_fragment() throws RecognitionException {
        // action.g:1:518: ( ISOLATED_DYNAMIC_SCOPE )
        // action.g:1:519: ISOLATED_DYNAMIC_SCOPE
        {
        mISOLATED_DYNAMIC_SCOPE(); if (failed) return ;

        }
    }
    // $ANTLR end synpred12_fragment

    // $ANTLR start synpred13_fragment
    public void synpred13_fragment() throws RecognitionException {
        // action.g:1:567: ( TEMPLATE_INSTANCE )
        // action.g:1:568: TEMPLATE_INSTANCE
        {
        mTEMPLATE_INSTANCE(); if (failed) return ;

        }
    }
    // $ANTLR end synpred13_fragment

    // $ANTLR start synpred14_fragment
    public void synpred14_fragment() throws RecognitionException {
        // action.g:1:606: ( INDIRECT_TEMPLATE_INSTANCE )
        // action.g:1:607: INDIRECT_TEMPLATE_INSTANCE
        {
        mINDIRECT_TEMPLATE_INSTANCE(); if (failed) return ;

        }
    }
    // $ANTLR end synpred14_fragment

    // $ANTLR start synpred15_fragment
    public void synpred15_fragment() throws RecognitionException {
        // action.g:1:663: ( SET_EXPR_ATTRIBUTE )
        // action.g:1:664: SET_EXPR_ATTRIBUTE
        {
        mSET_EXPR_ATTRIBUTE(); if (failed) return ;

        }
    }
    // $ANTLR end synpred15_fragment

    // $ANTLR start synpred16_fragment
    public void synpred16_fragment() throws RecognitionException {
        // action.g:1:704: ( SET_ATTRIBUTE )
        // action.g:1:705: SET_ATTRIBUTE
        {
        mSET_ATTRIBUTE(); if (failed) return ;

        }
    }
    // $ANTLR end synpred16_fragment

    // $ANTLR start synpred17_fragment
    public void synpred17_fragment() throws RecognitionException {
        // action.g:1:735: ( TEMPLATE_EXPR )
        // action.g:1:736: TEMPLATE_EXPR
        {
        mTEMPLATE_EXPR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred17_fragment

    // $ANTLR start synpred18_fragment
    public void synpred18_fragment() throws RecognitionException {
        // action.g:1:766: ( ESC )
        // action.g:1:767: ESC
        {
        mESC(); if (failed) return ;

        }
    }
    // $ANTLR end synpred18_fragment

    // $ANTLR start synpred19_fragment
    public void synpred19_fragment() throws RecognitionException {
        // action.g:1:777: ( ERROR_XY )
        // action.g:1:778: ERROR_XY
        {
        mERROR_XY(); if (failed) return ;

        }
    }
    // $ANTLR end synpred19_fragment

    // $ANTLR start synpred20_fragment
    public void synpred20_fragment() throws RecognitionException {
        // action.g:1:798: ( ERROR_X )
        // action.g:1:799: ERROR_X
        {
        mERROR_X(); if (failed) return ;

        }
    }
    // $ANTLR end synpred20_fragment

    // $ANTLR start synpred21_fragment
    public void synpred21_fragment() throws RecognitionException {
        // action.g:1:817: ( UNKNOWN_SYNTAX )
        // action.g:1:818: UNKNOWN_SYNTAX
        {
        mUNKNOWN_SYNTAX(); if (failed) return ;

        }
    }
    // $ANTLR end synpred21_fragment

    // $ANTLR start synpred22_fragment
    public void synpred22_fragment() throws RecognitionException {
        // action.g:1:850: ( TEXT )
        // action.g:1:851: TEXT
        {
        mTEXT(); if (failed) return ;

        }
    }
    // $ANTLR end synpred22_fragment

    public boolean synpred1() {
        backtracking++;
        int start = input.mark();
        try {
            synpred1_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred2() {
        backtracking++;
        int start = input.mark();
        try {
            synpred2_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred3() {
        backtracking++;
        int start = input.mark();
        try {
            synpred3_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred4() {
        backtracking++;
        int start = input.mark();
        try {
            synpred4_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred5() {
        backtracking++;
        int start = input.mark();
        try {
            synpred5_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred6() {
        backtracking++;
        int start = input.mark();
        try {
            synpred6_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred7() {
        backtracking++;
        int start = input.mark();
        try {
            synpred7_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred8() {
        backtracking++;
        int start = input.mark();
        try {
            synpred8_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred9() {
        backtracking++;
        int start = input.mark();
        try {
            synpred9_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred10() {
        backtracking++;
        int start = input.mark();
        try {
            synpred10_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred11() {
        backtracking++;
        int start = input.mark();
        try {
            synpred11_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred12() {
        backtracking++;
        int start = input.mark();
        try {
            synpred12_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred13() {
        backtracking++;
        int start = input.mark();
        try {
            synpred13_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred14() {
        backtracking++;
        int start = input.mark();
        try {
            synpred14_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred15() {
        backtracking++;
        int start = input.mark();
        try {
            synpred15_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred16() {
        backtracking++;
        int start = input.mark();
        try {
            synpred16_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred17() {
        backtracking++;
        int start = input.mark();
        try {
            synpred17_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred18() {
        backtracking++;
        int start = input.mark();
        try {
            synpred18_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred19() {
        backtracking++;
        int start = input.mark();
        try {
            synpred19_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred20() {
        backtracking++;
        int start = input.mark();
        try {
            synpred20_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred21() {
        backtracking++;
        int start = input.mark();
        try {
            synpred21_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }
    public boolean synpred22() {
        backtracking++;
        int start = input.mark();
        try {
            synpred22_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !failed;
        input.rewind(start);
        backtracking--;
        failed=false;
        return success;
    }




}