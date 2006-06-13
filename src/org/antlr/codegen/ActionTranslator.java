// $ANTLR 3.0ea11 action.g 2006-06-12 17:22:44

package org.antlr.codegen;
import org.antlr.runtime.*;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class ActionTranslator extends Lexer {
    public static final int Synpred20_fragment=53;
    public static final int Synpred17_fragment=50;
    public static final int ISOLATED_DYNAMIC_SCOPE=17;
    public static final int DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR=16;
    public static final int SCOPE_INDEX_EXPR=14;
    public static final int DYNAMIC_SCOPE_ATTR=12;
    public static final int ISOLATED_TOKEN_REF=9;
    public static final int SET_ATTRIBUTE=25;
    public static final int SET_EXPR_ATTRIBUTE=24;
    public static final int TEMPLATE_INSTANCE=20;
    public static final int Synpred12_fragment=45;
    public static final int Synpred21_fragment=54;
    public static final int Synpred6_fragment=39;
    public static final int Synpred3_fragment=36;
    public static final int RULE_SCOPE_ATTR=7;
    public static final int LABEL_REF=8;
    public static final int INT=32;
    public static final int Synpred8_fragment=41;
    public static final int Synpred2_fragment=35;
    public static final int Synpred9_fragment=42;
    public static final int ENCLOSING_RULE_SCOPE_ATTR=5;
    public static final int ID=4;
    public static final int TEMPLATE_EXPR=26;
    public static final int Synpred19_fragment=52;
    public static final int Synpred1_fragment=34;
    public static final int LOCAL_ATTR=11;
    public static final int Synpred15_fragment=48;
    public static final int WS=18;
    public static final int UNKNOWN_SYNTAX=30;
    public static final int ACTION=21;
    public static final int Synpred11_fragment=44;
    public static final int ERROR_X=29;
    public static final int ISOLATED_LEXER_RULE_REF=10;
    public static final int TOKEN_SCOPE_ATTR=6;
    public static final int ESC=27;
    public static final int Synpred16_fragment=49;
    public static final int ATTR_VALUE_EXPR=23;
    public static final int Synpred13_fragment=46;
    public static final int Synpred7_fragment=40;
    public static final int ARG=19;
    public static final int EOF=-1;
    public static final int Synpred5_fragment=38;
    public static final int TEXT=31;
    public static final int Synpred22_fragment=55;
    public static final int Tokens=33;
    public static final int Synpred14_fragment=47;
    public static final int DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR=15;
    public static final int Synpred10_fragment=43;
    public static final int ERROR_SCOPED_XY=13;
    public static final int Synpred18_fragment=51;
    public static final int ERROR_XY=28;
    public static final int Synpred4_fragment=37;
    public static final int INDIRECT_TEMPLATE_INSTANCE=22;

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
        token=null;
        tokenStartCharIndex = getCharIndex();
        while (true) {
            if ( input.LA(1)==CharStream.EOF ) {
                return Token.EOF_TOKEN;
            }
            try {
                int m = input.mark();
                backtracking=1;
                failed=false;
                mTokens();
                backtracking=0;
                input.rewind(m);
                if ( failed ) {
                    input.consume();
                }
                else {
                    mTokens();
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
    // $ANTLR start ENCLOSING_RULE_SCOPE_ATTR
    public void mENCLOSING_RULE_SCOPE_ATTR() throws RecognitionException {
        int ENCLOSING_RULE_SCOPE_ATTR_StartIndex = input.index();
        try {
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
            if ( backtracking==0 ) {

              		StringTemplate st = null;
              		AttributeScope scope = enclosingRule.getLocalAttributeScope(y.getText());
              		if ( scope.isPredefinedRuleScope ) {
              			st = template("rulePropertyRef_"+y.getText());
              			grammar.referenceRuleLabelPredefinedAttribute(x.getText());
              			st.setAttribute("scope", x.getText());
              			st.setAttribute("attr", y.getText());
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

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 1, ENCLOSING_RULE_SCOPE_ATTR_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ENCLOSING_RULE_SCOPE_ATTR


    // $ANTLR start TOKEN_SCOPE_ATTR
    public void mTOKEN_SCOPE_ATTR() throws RecognitionException {
        int TOKEN_SCOPE_ATTR_StartIndex = input.index();
        try {
            int type = TOKEN_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 2) ) { return ; }
            // action.g:228:4: ( '$' x= ID '.' y= ID {...}?)
            // action.g:228:4: '$' x= ID '.' y= ID {...}?
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
            if ( backtracking==0 ) {

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

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 2, TOKEN_SCOPE_ATTR_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end TOKEN_SCOPE_ATTR


    // $ANTLR start RULE_SCOPE_ATTR
    public void mRULE_SCOPE_ATTR() throws RecognitionException {
        int RULE_SCOPE_ATTR_StartIndex = input.index();
        try {
            int type = RULE_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;

            Grammar.LabelElementPair pair=null;
            String refdRuleName=null;

            if ( backtracking>0 && alreadyParsedRule(input, 3) ) { return ; }
            // action.g:259:4: ( '$' x= ID '.' y= ID {...}?{...}?)
            // action.g:259:4: '$' x= ID '.' y= ID {...}?{...}?
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
            if ( backtracking==0 ) {

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
            if ( backtracking==0 ) {

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

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 3, RULE_SCOPE_ATTR_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end RULE_SCOPE_ATTR


    // $ANTLR start LABEL_REF
    public void mLABEL_REF() throws RecognitionException {
        int LABEL_REF_StartIndex = input.index();
        try {
            int type = LABEL_REF;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 4) ) { return ; }
            // action.g:316:4: ( '$' ID {...}?)
            // action.g:316:4: '$' ID {...}?
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
            if ( backtracking==0 ) {

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

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 4, LABEL_REF_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end LABEL_REF


    // $ANTLR start ISOLATED_TOKEN_REF
    public void mISOLATED_TOKEN_REF() throws RecognitionException {
        int ISOLATED_TOKEN_REF_StartIndex = input.index();
        try {
            int type = ISOLATED_TOKEN_REF;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 5) ) { return ; }
            // action.g:335:4: ( '$' ID {...}?)
            // action.g:335:4: '$' ID {...}?
            {
            match('$'); if (failed) return ;
            int ID2Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID2 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID2Start, getCharIndex()-1);
            if ( !(grammar.type!=Grammar.LEXER && enclosingRule!=null && isTokenRefInAlt(ID2.getText())) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "ISOLATED_TOKEN_REF", "grammar.type!=Grammar.LEXER && enclosingRule!=null && isTokenRefInAlt($ID.text)");
            }
            if ( backtracking==0 ) {

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

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 5, ISOLATED_TOKEN_REF_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ISOLATED_TOKEN_REF


    // $ANTLR start ISOLATED_LEXER_RULE_REF
    public void mISOLATED_LEXER_RULE_REF() throws RecognitionException {
        int ISOLATED_LEXER_RULE_REF_StartIndex = input.index();
        try {
            int type = ISOLATED_LEXER_RULE_REF;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 6) ) { return ; }
            // action.g:355:4: ( '$' ID {...}?)
            // action.g:355:4: '$' ID {...}?
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
            if ( backtracking==0 ) {

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

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 6, ISOLATED_LEXER_RULE_REF_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ISOLATED_LEXER_RULE_REF


    // $ANTLR start LOCAL_ATTR
    public void mLOCAL_ATTR() throws RecognitionException {
        int LOCAL_ATTR_StartIndex = input.index();
        try {
            int type = LOCAL_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 7) ) { return ; }
            // action.g:387:4: ( '$' ID {...}?)
            // action.g:387:4: '$' ID {...}?
            {
            match('$'); if (failed) return ;
            int ID4Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID4 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID4Start, getCharIndex()-1);
            if ( !(enclosingRule!=null && enclosingRule.getLocalAttributeScope(ID4.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "LOCAL_ATTR", "enclosingRule!=null && enclosingRule.getLocalAttributeScope($ID.text)!=null");
            }
            if ( backtracking==0 ) {

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

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 7, LOCAL_ATTR_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end LOCAL_ATTR


    // $ANTLR start DYNAMIC_SCOPE_ATTR
    public void mDYNAMIC_SCOPE_ATTR() throws RecognitionException {
        int DYNAMIC_SCOPE_ATTR_StartIndex = input.index();
        try {
            int type = DYNAMIC_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 8) ) { return ; }
            // action.g:423:4: ( '$' x= ID '::' y= ID {...}?)
            // action.g:423:4: '$' x= ID '::' y= ID {...}?
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
            if ( backtracking==0 ) {

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

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 8, DYNAMIC_SCOPE_ATTR_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end DYNAMIC_SCOPE_ATTR


    // $ANTLR start ERROR_SCOPED_XY
    public void mERROR_SCOPED_XY() throws RecognitionException {
        int ERROR_SCOPED_XY_StartIndex = input.index();
        try {
            int type = ERROR_SCOPED_XY;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 9) ) { return ; }
            // action.g:440:4: ( '$' x= ID '::' y= ID )
            // action.g:440:4: '$' x= ID '::' y= ID
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match("::"); if (failed) return ;

            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            if ( backtracking==0 ) {

              		chunks.add(getText());
              		generator.issueInvalidScopeError(x.getText(),y.getText(),
              		                                 enclosingRule,actionToken,
              		                                 outerAltNum);

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 9, ERROR_SCOPED_XY_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ERROR_SCOPED_XY


    // $ANTLR start DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR
    public void mDYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR() throws RecognitionException {
        int DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR_StartIndex = input.index();
        try {
            int type = DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 10) ) { return ; }
            // action.g:458:4: ( '$' x= ID '[' '-' expr= SCOPE_INDEX_EXPR ']' '::' y= ID )
            // action.g:458:4: '$' x= ID '[' '-' expr= SCOPE_INDEX_EXPR ']' '::' y= ID
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
            if ( backtracking==0 ) {

              		StringTemplate st = template("scopeAttributeRef");
              		st.setAttribute("scope",    x.getText());
              		st.setAttribute("attr",     resolveDynamicScope(x.getText()).getAttribute(y.getText()));
              		st.setAttribute("negIndex", expr.getText());

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 10, DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR


    // $ANTLR start DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR
    public void mDYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR() throws RecognitionException {
        int DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR_StartIndex = input.index();
        try {
            int type = DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 11) ) { return ; }
            // action.g:469:4: ( '$' x= ID '[' expr= SCOPE_INDEX_EXPR ']' '::' y= ID )
            // action.g:469:4: '$' x= ID '[' expr= SCOPE_INDEX_EXPR ']' '::' y= ID
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
            if ( backtracking==0 ) {

              		StringTemplate st = template("scopeAttributeRef");
              		st.setAttribute("scope", x.getText());
              		st.setAttribute("attr",  resolveDynamicScope(x.getText()).getAttribute(y.getText()));
              		st.setAttribute("index", expr.getText());

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 11, DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR


    // $ANTLR start SCOPE_INDEX_EXPR
    public void mSCOPE_INDEX_EXPR() throws RecognitionException {
        int SCOPE_INDEX_EXPR_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 12) ) { return ; }
            // action.g:481:4: ( (~ ']' )+ )
            // action.g:481:4: (~ ']' )+
            {
            // action.g:481:4: (~ ']' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);
                if ( (LA1_0>='\u0000' && LA1_0<='\\')||(LA1_0>='^' && LA1_0<='\uFFFE') ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // action.g:481:5: ~ ']'
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
            if ( backtracking>0 ) { memoize(input, 12, SCOPE_INDEX_EXPR_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end SCOPE_INDEX_EXPR


    // $ANTLR start ISOLATED_DYNAMIC_SCOPE
    public void mISOLATED_DYNAMIC_SCOPE() throws RecognitionException {
        int ISOLATED_DYNAMIC_SCOPE_StartIndex = input.index();
        try {
            int type = ISOLATED_DYNAMIC_SCOPE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 13) ) { return ; }
            // action.g:490:4: ( '$' ID {...}?)
            // action.g:490:4: '$' ID {...}?
            {
            match('$'); if (failed) return ;
            int ID5Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID5 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID5Start, getCharIndex()-1);
            if ( !(resolveDynamicScope(ID5.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "ISOLATED_DYNAMIC_SCOPE", "resolveDynamicScope($ID.text)!=null");
            }
            if ( backtracking==0 ) {

              		StringTemplate st = template("isolatedDynamicScopeRef");
              		st.setAttribute("scope", ID5.getText());

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 13, ISOLATED_DYNAMIC_SCOPE_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ISOLATED_DYNAMIC_SCOPE


    // $ANTLR start TEMPLATE_INSTANCE
    public void mTEMPLATE_INSTANCE() throws RecognitionException {
        int TEMPLATE_INSTANCE_StartIndex = input.index();
        try {
            int type = TEMPLATE_INSTANCE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 14) ) { return ; }
            // action.g:503:4: ( '%' ID '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')' )
            // action.g:503:4: '%' ID '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')'
            {
            match('%'); if (failed) return ;
            mID(); if (failed) return ;
            match('('); if (failed) return ;
            // action.g:503:15: ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )?
            int alt6=2;
            int LA6_0 = input.LA(1);
            if ( (LA6_0>='\t' && LA6_0<='\n')||LA6_0==' '||(LA6_0>='A' && LA6_0<='Z')||LA6_0=='_'||(LA6_0>='a' && LA6_0<='z') ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // action.g:503:17: ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )?
                    {
                    // action.g:503:17: ( WS )?
                    int alt2=2;
                    int LA2_0 = input.LA(1);
                    if ( (LA2_0>='\t' && LA2_0<='\n')||LA2_0==' ' ) {
                        alt2=1;
                    }
                    switch (alt2) {
                        case 1 :
                            // action.g:503:17: WS
                            {
                            mWS(); if (failed) return ;

                            }
                            break;

                    }

                    mARG(); if (failed) return ;
                    // action.g:503:25: ( ',' ( WS )? ARG )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);
                        if ( LA4_0==',' ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // action.g:503:26: ',' ( WS )? ARG
                    	    {
                    	    match(','); if (failed) return ;
                    	    // action.g:503:30: ( WS )?
                    	    int alt3=2;
                    	    int LA3_0 = input.LA(1);
                    	    if ( (LA3_0>='\t' && LA3_0<='\n')||LA3_0==' ' ) {
                    	        alt3=1;
                    	    }
                    	    switch (alt3) {
                    	        case 1 :
                    	            // action.g:503:30: WS
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

                    // action.g:503:40: ( WS )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);
                    if ( (LA5_0>='\t' && LA5_0<='\n')||LA5_0==' ' ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // action.g:503:40: WS
                            {
                            mWS(); if (failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }

            match(')'); if (failed) return ;
            if ( backtracking==0 ) {

              		String action = getText().substring(1,getText().length());
              		StringTemplate st =
              			generator.translateTemplateConstructor(enclosingRule.name,
              												   outerAltNum,
              												   actionToken,
              												   action);
              		chunks.add(st);

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 14, TEMPLATE_INSTANCE_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end TEMPLATE_INSTANCE


    // $ANTLR start INDIRECT_TEMPLATE_INSTANCE
    public void mINDIRECT_TEMPLATE_INSTANCE() throws RecognitionException {
        int INDIRECT_TEMPLATE_INSTANCE_StartIndex = input.index();
        try {
            int type = INDIRECT_TEMPLATE_INSTANCE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 15) ) { return ; }
            // action.g:518:4: ( '%' '(' ACTION ')' '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')' )
            // action.g:518:4: '%' '(' ACTION ')' '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')'
            {
            match('%'); if (failed) return ;
            match('('); if (failed) return ;
            mACTION(); if (failed) return ;
            match(')'); if (failed) return ;
            match('('); if (failed) return ;
            // action.g:518:27: ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )?
            int alt11=2;
            int LA11_0 = input.LA(1);
            if ( (LA11_0>='\t' && LA11_0<='\n')||LA11_0==' '||(LA11_0>='A' && LA11_0<='Z')||LA11_0=='_'||(LA11_0>='a' && LA11_0<='z') ) {
                alt11=1;
            }
            switch (alt11) {
                case 1 :
                    // action.g:518:29: ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )?
                    {
                    // action.g:518:29: ( WS )?
                    int alt7=2;
                    int LA7_0 = input.LA(1);
                    if ( (LA7_0>='\t' && LA7_0<='\n')||LA7_0==' ' ) {
                        alt7=1;
                    }
                    switch (alt7) {
                        case 1 :
                            // action.g:518:29: WS
                            {
                            mWS(); if (failed) return ;

                            }
                            break;

                    }

                    mARG(); if (failed) return ;
                    // action.g:518:37: ( ',' ( WS )? ARG )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);
                        if ( LA9_0==',' ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // action.g:518:38: ',' ( WS )? ARG
                    	    {
                    	    match(','); if (failed) return ;
                    	    // action.g:518:42: ( WS )?
                    	    int alt8=2;
                    	    int LA8_0 = input.LA(1);
                    	    if ( (LA8_0>='\t' && LA8_0<='\n')||LA8_0==' ' ) {
                    	        alt8=1;
                    	    }
                    	    switch (alt8) {
                    	        case 1 :
                    	            // action.g:518:42: WS
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

                    // action.g:518:52: ( WS )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);
                    if ( (LA10_0>='\t' && LA10_0<='\n')||LA10_0==' ' ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // action.g:518:52: WS
                            {
                            mWS(); if (failed) return ;

                            }
                            break;

                    }


                    }
                    break;

            }

            match(')'); if (failed) return ;
            if ( backtracking==0 ) {

              		String action = getText().substring(1,getText().length());
              		StringTemplate st =
              			generator.translateTemplateConstructor(enclosingRule.name,
              												   outerAltNum,
              												   actionToken,
              												   action);
              		chunks.add(st);

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 15, INDIRECT_TEMPLATE_INSTANCE_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end INDIRECT_TEMPLATE_INSTANCE


    // $ANTLR start ARG
    public void mARG() throws RecognitionException {
        int ARG_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 16) ) { return ; }
            // action.g:532:7: ( ID '=' ACTION )
            // action.g:532:7: ID '=' ACTION
            {
            mID(); if (failed) return ;
            match('='); if (failed) return ;
            mACTION(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 16, ARG_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ARG


    // $ANTLR start SET_EXPR_ATTRIBUTE
    public void mSET_EXPR_ATTRIBUTE() throws RecognitionException {
        int SET_EXPR_ATTRIBUTE_StartIndex = input.index();
        try {
            int type = SET_EXPR_ATTRIBUTE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 17) ) { return ; }
            // action.g:537:4: ( '%' a= ACTION '.' ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' )
            // action.g:537:4: '%' a= ACTION '.' ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';'
            {
            match('%'); if (failed) return ;
            int aStart = getCharIndex();
            mACTION(); if (failed) return ;
            Token a = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, aStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int ID6Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID6 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID6Start, getCharIndex()-1);
            // action.g:537:24: ( WS )?
            int alt12=2;
            int LA12_0 = input.LA(1);
            if ( (LA12_0>='\t' && LA12_0<='\n')||LA12_0==' ' ) {
                alt12=1;
            }
            switch (alt12) {
                case 1 :
                    // action.g:537:24: WS
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
            if ( backtracking==0 ) {

              		StringTemplate st = template("actionSetAttribute");
              		String action = a.getText();
              		action = action.substring(1,action.length()-1); // stuff inside {...}
              		st.setAttribute("st", translateAction(action));
              		st.setAttribute("attrName", ID6.getText());
              		st.setAttribute("expr", translateAction(expr.getText()));

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 17, SET_EXPR_ATTRIBUTE_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end SET_EXPR_ATTRIBUTE


    // $ANTLR start SET_ATTRIBUTE
    public void mSET_ATTRIBUTE() throws RecognitionException {
        int SET_ATTRIBUTE_StartIndex = input.index();
        try {
            int type = SET_ATTRIBUTE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 18) ) { return ; }
            // action.g:554:4: ( '%' x= ID '.' y= ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' )
            // action.g:554:4: '%' x= ID '.' y= ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';'
            {
            match('%'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            // action.g:554:22: ( WS )?
            int alt13=2;
            int LA13_0 = input.LA(1);
            if ( (LA13_0>='\t' && LA13_0<='\n')||LA13_0==' ' ) {
                alt13=1;
            }
            switch (alt13) {
                case 1 :
                    // action.g:554:22: WS
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
            if ( backtracking==0 ) {

              		StringTemplate st = template("actionSetAttribute");
              		st.setAttribute("st", x.getText());
              		st.setAttribute("attrName", y.getText());
              		st.setAttribute("expr", translateAction(expr.getText()));

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 18, SET_ATTRIBUTE_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end SET_ATTRIBUTE


    // $ANTLR start ATTR_VALUE_EXPR
    public void mATTR_VALUE_EXPR() throws RecognitionException {
        int ATTR_VALUE_EXPR_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 19) ) { return ; }
            // action.g:566:4: ( (~ ';' )+ )
            // action.g:566:4: (~ ';' )+
            {
            // action.g:566:4: (~ ';' )+
            int cnt14=0;
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);
                if ( (LA14_0>='\u0000' && LA14_0<=':')||(LA14_0>='<' && LA14_0<='\uFFFE') ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // action.g:566:5: ~ ';'
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
            if ( backtracking>0 ) { memoize(input, 19, ATTR_VALUE_EXPR_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ATTR_VALUE_EXPR


    // $ANTLR start TEMPLATE_EXPR
    public void mTEMPLATE_EXPR() throws RecognitionException {
        int TEMPLATE_EXPR_StartIndex = input.index();
        try {
            int type = TEMPLATE_EXPR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 20) ) { return ; }
            // action.g:571:4: ( '%' a= ACTION )
            // action.g:571:4: '%' a= ACTION
            {
            match('%'); if (failed) return ;
            int aStart = getCharIndex();
            mACTION(); if (failed) return ;
            Token a = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, aStart, getCharIndex()-1);
            if ( backtracking==0 ) {

              		StringTemplate st = template("actionStringConstructor");
              		String action = a.getText();
              		action = action.substring(1,action.length()-1); // stuff inside {...}
              		st.setAttribute("stringExpr", translateAction(action));

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 20, TEMPLATE_EXPR_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end TEMPLATE_EXPR


    // $ANTLR start ACTION
    public void mACTION() throws RecognitionException {
        int ACTION_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 21) ) { return ; }
            // action.g:583:4: ( '{' ( options {greedy=false; } : . )* '}' )
            // action.g:583:4: '{' ( options {greedy=false; } : . )* '}'
            {
            match('{'); if (failed) return ;
            // action.g:583:8: ( options {greedy=false; } : . )*
            loop15:
            do {
                int alt15=2;
                int LA15_0 = input.LA(1);
                if ( LA15_0=='}' ) {
                    alt15=2;
                }
                else if ( (LA15_0>='\u0000' && LA15_0<='|')||(LA15_0>='~' && LA15_0<='\uFFFE') ) {
                    alt15=1;
                }


                switch (alt15) {
            	case 1 :
            	    // action.g:583:33: .
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
            if ( backtracking>0 ) { memoize(input, 21, ACTION_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ACTION


    // $ANTLR start ESC
    public void mESC() throws RecognitionException {
        int ESC_StartIndex = input.index();
        try {
            int type = ESC;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 22) ) { return ; }
            // action.g:586:9: ( '\\\\' '$' | '\\\\' '%' )
            int alt16=2;
            int LA16_0 = input.LA(1);
            if ( LA16_0=='\\' ) {
                int LA16_1 = input.LA(2);
                if ( LA16_1=='%' ) {
                    alt16=2;
                }
                else if ( LA16_1=='$' ) {
                    alt16=1;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("586:1: ESC : ( '\\\\' '$' | '\\\\' '%' );", 16, 1, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("586:1: ESC : ( '\\\\' '$' | '\\\\' '%' );", 16, 0, input);

                throw nvae;
            }
            switch (alt16) {
                case 1 :
                    // action.g:586:9: '\\\\' '$'
                    {
                    match('\\'); if (failed) return ;
                    match('$'); if (failed) return ;
                    if ( backtracking==0 ) {
                      chunks.add("$");
                    }

                    }
                    break;
                case 2 :
                    // action.g:587:4: '\\\\' '%'
                    {
                    match('\\'); if (failed) return ;
                    match('%'); if (failed) return ;
                    if ( backtracking==0 ) {
                      chunks.add("%");
                    }

                    }
                    break;

            }
            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 22, ESC_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ESC


    // $ANTLR start ERROR_XY
    public void mERROR_XY() throws RecognitionException {
        int ERROR_XY_StartIndex = input.index();
        try {
            int type = ERROR_XY;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 23) ) { return ; }
            // action.g:591:4: ( '$' x= ID '.' y= ID )
            // action.g:591:4: '$' x= ID '.' y= ID
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            if ( backtracking==0 ) {

              		chunks.add(getText());
              		generator.issueInvalidAttributeError(x.getText(),y.getText(),
              		                                     enclosingRule,actionToken,
              		                                     outerAltNum);

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 23, ERROR_XY_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ERROR_XY


    // $ANTLR start ERROR_X
    public void mERROR_X() throws RecognitionException {
        int ERROR_X_StartIndex = input.index();
        try {
            int type = ERROR_X;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 24) ) { return ; }
            // action.g:601:4: ( '$' x= ID )
            // action.g:601:4: '$' x= ID
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            if ( backtracking==0 ) {

              		chunks.add(getText());
              		generator.issueInvalidAttributeError(x.getText(),
              		                                     enclosingRule,actionToken,
              		                                     outerAltNum);

            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 24, ERROR_X_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ERROR_X


    // $ANTLR start UNKNOWN_SYNTAX
    public void mUNKNOWN_SYNTAX() throws RecognitionException {
        int UNKNOWN_SYNTAX_StartIndex = input.index();
        try {
            int type = UNKNOWN_SYNTAX;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 25) ) { return ; }
            // action.g:611:4: ( '$' | '%' ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )* )
            int alt18=2;
            int LA18_0 = input.LA(1);
            if ( LA18_0=='$' ) {
                alt18=1;
            }
            else if ( LA18_0=='%' ) {
                alt18=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("610:1: UNKNOWN_SYNTAX : ( '$' | '%' ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )* );", 18, 0, input);

                throw nvae;
            }
            switch (alt18) {
                case 1 :
                    // action.g:611:4: '$'
                    {
                    match('$'); if (failed) return ;
                    if ( backtracking==0 ) {

                      		chunks.add(getText());
                      		// shouldn't need an error here.  Just accept $ if it doesn't look like anything

                    }

                    }
                    break;
                case 2 :
                    // action.g:616:4: '%' ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )*
                    {
                    match('%'); if (failed) return ;
                    // action.g:616:8: ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )*
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
                    	    // action.g:616:9: ID
                    	    {
                    	    mID(); if (failed) return ;

                    	    }
                    	    break;
                    	case 2 :
                    	    // action.g:616:12: '.'
                    	    {
                    	    match('.'); if (failed) return ;

                    	    }
                    	    break;
                    	case 3 :
                    	    // action.g:616:16: '('
                    	    {
                    	    match('('); if (failed) return ;

                    	    }
                    	    break;
                    	case 4 :
                    	    // action.g:616:20: ')'
                    	    {
                    	    match(')'); if (failed) return ;

                    	    }
                    	    break;
                    	case 5 :
                    	    // action.g:616:24: ','
                    	    {
                    	    match(','); if (failed) return ;

                    	    }
                    	    break;
                    	case 6 :
                    	    // action.g:616:28: '{'
                    	    {
                    	    match('{'); if (failed) return ;

                    	    }
                    	    break;
                    	case 7 :
                    	    // action.g:616:32: '}'
                    	    {
                    	    match('}'); if (failed) return ;

                    	    }
                    	    break;
                    	case 8 :
                    	    // action.g:616:36: '\"'
                    	    {
                    	    match('\"'); if (failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop17;
                        }
                    } while (true);

                    if ( backtracking==0 ) {

                      		chunks.add(getText());
                      		ErrorManager.grammarError(ErrorManager.MSG_INVALID_TEMPLATE_ACTION,
                      								  grammar,
                      								  actionToken,
                      								  getText());

                    }

                    }
                    break;

            }
            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 25, UNKNOWN_SYNTAX_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end UNKNOWN_SYNTAX


    // $ANTLR start TEXT
    public void mTEXT() throws RecognitionException {
        int TEXT_StartIndex = input.index();
        try {
            int type = TEXT;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            if ( backtracking>0 && alreadyParsedRule(input, 26) ) { return ; }
            // action.g:626:7: ( (~ ('$'|'%'|'\\\\'))+ )
            // action.g:626:7: (~ ('$'|'%'|'\\\\'))+
            {
            // action.g:626:7: (~ ('$'|'%'|'\\\\'))+
            int cnt19=0;
            loop19:
            do {
                int alt19=2;
                int LA19_0 = input.LA(1);
                if ( (LA19_0>='\u0000' && LA19_0<='#')||(LA19_0>='&' && LA19_0<='[')||(LA19_0>=']' && LA19_0<='\uFFFE') ) {
                    alt19=1;
                }


                switch (alt19) {
            	case 1 :
            	    // action.g:626:7: ~ ('$'|'%'|'\\\\')
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

            if ( backtracking==0 ) {
              chunks.add(getText());
            }

            }

            if ( token==null ) {emit(type,line,charPosition,channel,start,getCharIndex()-1);}
        }
        finally {
            if ( backtracking>0 ) { memoize(input, 26, TEXT_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end TEXT


    // $ANTLR start ID
    public void mID() throws RecognitionException {
        int ID_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 27) ) { return ; }
            // action.g:630:9: ( ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))* )
            // action.g:630:9: ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))*
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

            // action.g:630:33: ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))*
            loop20:
            do {
                int alt20=2;
                int LA20_0 = input.LA(1);
                if ( (LA20_0>='0' && LA20_0<='9')||(LA20_0>='A' && LA20_0<='Z')||LA20_0=='_'||(LA20_0>='a' && LA20_0<='z') ) {
                    alt20=1;
                }


                switch (alt20) {
            	case 1 :
            	    // action.g:630:34: ('a'..'z'|'A'..'Z'|'_'|'0'..'9')
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
            if ( backtracking>0 ) { memoize(input, 27, ID_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end ID


    // $ANTLR start INT
    public void mINT() throws RecognitionException {
        int INT_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 28) ) { return ; }
            // action.g:634:7: ( ( '0' .. '9' )+ )
            // action.g:634:7: ( '0' .. '9' )+
            {
            // action.g:634:7: ( '0' .. '9' )+
            int cnt21=0;
            loop21:
            do {
                int alt21=2;
                int LA21_0 = input.LA(1);
                if ( (LA21_0>='0' && LA21_0<='9') ) {
                    alt21=1;
                }


                switch (alt21) {
            	case 1 :
            	    // action.g:634:7: '0' .. '9'
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
            if ( backtracking>0 ) { memoize(input, 28, INT_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end INT


    // $ANTLR start WS
    public void mWS() throws RecognitionException {
        int WS_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 29) ) { return ; }
            // action.g:638:6: ( ( (' '|'\\t'|'\\n'))+ )
            // action.g:638:6: ( (' '|'\\t'|'\\n'))+
            {
            // action.g:638:6: ( (' '|'\\t'|'\\n'))+
            int cnt22=0;
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);
                if ( (LA22_0>='\t' && LA22_0<='\n')||LA22_0==' ' ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // action.g:638:7: (' '|'\\t'|'\\n')
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
            if ( backtracking>0 ) { memoize(input, 29, WS_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end WS

    public void mTokens() throws RecognitionException {
        // action.g:1:25: ( ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( LOCAL_ATTR )=> LOCAL_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT )
        int alt23=22;
        int LA23_0 = input.LA(1);
        if ( LA23_0=='$' ) {
            int LA23_1 = input.LA(2);
            if ( synpred(input, Synpred1) ) {
                alt23=1;
            }
            else if ( synpred(input, Synpred2) ) {
                alt23=2;
            }
            else if ( synpred(input, Synpred3) ) {
                alt23=3;
            }
            else if ( synpred(input, Synpred4) ) {
                alt23=4;
            }
            else if ( synpred(input, Synpred5) ) {
                alt23=5;
            }
            else if ( synpred(input, Synpred6) ) {
                alt23=6;
            }
            else if ( synpred(input, Synpred7) ) {
                alt23=7;
            }
            else if ( synpred(input, Synpred8) ) {
                alt23=8;
            }
            else if ( synpred(input, Synpred9) ) {
                alt23=9;
            }
            else if ( synpred(input, Synpred10) ) {
                alt23=10;
            }
            else if ( synpred(input, Synpred11) ) {
                alt23=11;
            }
            else if ( synpred(input, Synpred12) ) {
                alt23=12;
            }
            else if ( synpred(input, Synpred19) ) {
                alt23=19;
            }
            else if ( synpred(input, Synpred20) ) {
                alt23=20;
            }
            else if ( synpred(input, Synpred21) ) {
                alt23=21;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("1:1: Tokens options {k=1; } : ( ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( LOCAL_ATTR )=> LOCAL_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT );", 23, 1, input);

                throw nvae;
            }
        }
        else if ( LA23_0=='%' ) {
            int LA23_2 = input.LA(2);
            if ( synpred(input, Synpred13) ) {
                alt23=13;
            }
            else if ( synpred(input, Synpred14) ) {
                alt23=14;
            }
            else if ( synpred(input, Synpred15) ) {
                alt23=15;
            }
            else if ( synpred(input, Synpred16) ) {
                alt23=16;
            }
            else if ( synpred(input, Synpred17) ) {
                alt23=17;
            }
            else if ( synpred(input, Synpred21) ) {
                alt23=21;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("1:1: Tokens options {k=1; } : ( ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( LOCAL_ATTR )=> LOCAL_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT );", 23, 2, input);

                throw nvae;
            }
        }
        else if ( LA23_0=='\\' ) {
            alt23=18;
        }
        else if ( (LA23_0>='\u0000' && LA23_0<='#')||(LA23_0>='&' && LA23_0<='[')||(LA23_0>=']' && LA23_0<='\uFFFE') ) {
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


    // $ANTLR start Synpred1_fragment
    public void mSynpred1_fragment() throws RecognitionException {
        int Synpred1_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 31) ) { return ; }
            // action.g:1:25: ( ENCLOSING_RULE_SCOPE_ATTR )
            // action.g:1:26: ENCLOSING_RULE_SCOPE_ATTR
            {
            mENCLOSING_RULE_SCOPE_ATTR(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 31, Synpred1_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred1_fragment


    // $ANTLR start Synpred2_fragment
    public void mSynpred2_fragment() throws RecognitionException {
        int Synpred2_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 32) ) { return ; }
            // action.g:1:80: ( TOKEN_SCOPE_ATTR )
            // action.g:1:81: TOKEN_SCOPE_ATTR
            {
            mTOKEN_SCOPE_ATTR(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 32, Synpred2_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred2_fragment


    // $ANTLR start Synpred3_fragment
    public void mSynpred3_fragment() throws RecognitionException {
        int Synpred3_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 33) ) { return ; }
            // action.g:1:117: ( RULE_SCOPE_ATTR )
            // action.g:1:118: RULE_SCOPE_ATTR
            {
            mRULE_SCOPE_ATTR(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 33, Synpred3_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred3_fragment


    // $ANTLR start Synpred4_fragment
    public void mSynpred4_fragment() throws RecognitionException {
        int Synpred4_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 34) ) { return ; }
            // action.g:1:152: ( LABEL_REF )
            // action.g:1:153: LABEL_REF
            {
            mLABEL_REF(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 34, Synpred4_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred4_fragment


    // $ANTLR start Synpred5_fragment
    public void mSynpred5_fragment() throws RecognitionException {
        int Synpred5_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 35) ) { return ; }
            // action.g:1:175: ( ISOLATED_TOKEN_REF )
            // action.g:1:176: ISOLATED_TOKEN_REF
            {
            mISOLATED_TOKEN_REF(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 35, Synpred5_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred5_fragment


    // $ANTLR start Synpred6_fragment
    public void mSynpred6_fragment() throws RecognitionException {
        int Synpred6_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 36) ) { return ; }
            // action.g:1:216: ( ISOLATED_LEXER_RULE_REF )
            // action.g:1:217: ISOLATED_LEXER_RULE_REF
            {
            mISOLATED_LEXER_RULE_REF(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 36, Synpred6_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred6_fragment


    // $ANTLR start Synpred7_fragment
    public void mSynpred7_fragment() throws RecognitionException {
        int Synpred7_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 37) ) { return ; }
            // action.g:1:267: ( LOCAL_ATTR )
            // action.g:1:268: LOCAL_ATTR
            {
            mLOCAL_ATTR(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 37, Synpred7_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred7_fragment


    // $ANTLR start Synpred8_fragment
    public void mSynpred8_fragment() throws RecognitionException {
        int Synpred8_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 38) ) { return ; }
            // action.g:1:292: ( DYNAMIC_SCOPE_ATTR )
            // action.g:1:293: DYNAMIC_SCOPE_ATTR
            {
            mDYNAMIC_SCOPE_ATTR(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 38, Synpred8_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred8_fragment


    // $ANTLR start Synpred9_fragment
    public void mSynpred9_fragment() throws RecognitionException {
        int Synpred9_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 39) ) { return ; }
            // action.g:1:333: ( ERROR_SCOPED_XY )
            // action.g:1:334: ERROR_SCOPED_XY
            {
            mERROR_SCOPED_XY(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 39, Synpred9_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred9_fragment


    // $ANTLR start Synpred10_fragment
    public void mSynpred10_fragment() throws RecognitionException {
        int Synpred10_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 40) ) { return ; }
            // action.g:1:368: ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )
            // action.g:1:369: DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR
            {
            mDYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 40, Synpred10_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred10_fragment


    // $ANTLR start Synpred11_fragment
    public void mSynpred11_fragment() throws RecognitionException {
        int Synpred11_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 41) ) { return ; }
            // action.g:1:443: ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )
            // action.g:1:444: DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR
            {
            mDYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 41, Synpred11_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred11_fragment


    // $ANTLR start Synpred12_fragment
    public void mSynpred12_fragment() throws RecognitionException {
        int Synpred12_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 42) ) { return ; }
            // action.g:1:518: ( ISOLATED_DYNAMIC_SCOPE )
            // action.g:1:519: ISOLATED_DYNAMIC_SCOPE
            {
            mISOLATED_DYNAMIC_SCOPE(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 42, Synpred12_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred12_fragment


    // $ANTLR start Synpred13_fragment
    public void mSynpred13_fragment() throws RecognitionException {
        int Synpred13_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 43) ) { return ; }
            // action.g:1:567: ( TEMPLATE_INSTANCE )
            // action.g:1:568: TEMPLATE_INSTANCE
            {
            mTEMPLATE_INSTANCE(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 43, Synpred13_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred13_fragment


    // $ANTLR start Synpred14_fragment
    public void mSynpred14_fragment() throws RecognitionException {
        int Synpred14_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 44) ) { return ; }
            // action.g:1:606: ( INDIRECT_TEMPLATE_INSTANCE )
            // action.g:1:607: INDIRECT_TEMPLATE_INSTANCE
            {
            mINDIRECT_TEMPLATE_INSTANCE(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 44, Synpred14_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred14_fragment


    // $ANTLR start Synpred15_fragment
    public void mSynpred15_fragment() throws RecognitionException {
        int Synpred15_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 45) ) { return ; }
            // action.g:1:663: ( SET_EXPR_ATTRIBUTE )
            // action.g:1:664: SET_EXPR_ATTRIBUTE
            {
            mSET_EXPR_ATTRIBUTE(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 45, Synpred15_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred15_fragment


    // $ANTLR start Synpred16_fragment
    public void mSynpred16_fragment() throws RecognitionException {
        int Synpred16_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 46) ) { return ; }
            // action.g:1:704: ( SET_ATTRIBUTE )
            // action.g:1:705: SET_ATTRIBUTE
            {
            mSET_ATTRIBUTE(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 46, Synpred16_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred16_fragment


    // $ANTLR start Synpred17_fragment
    public void mSynpred17_fragment() throws RecognitionException {
        int Synpred17_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 47) ) { return ; }
            // action.g:1:735: ( TEMPLATE_EXPR )
            // action.g:1:736: TEMPLATE_EXPR
            {
            mTEMPLATE_EXPR(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 47, Synpred17_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred17_fragment


    // $ANTLR start Synpred18_fragment
    public void mSynpred18_fragment() throws RecognitionException {
        int Synpred18_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 48) ) { return ; }
            // action.g:1:766: ( ESC )
            // action.g:1:767: ESC
            {
            mESC(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 48, Synpred18_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred18_fragment


    // $ANTLR start Synpred19_fragment
    public void mSynpred19_fragment() throws RecognitionException {
        int Synpred19_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 49) ) { return ; }
            // action.g:1:777: ( ERROR_XY )
            // action.g:1:778: ERROR_XY
            {
            mERROR_XY(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 49, Synpred19_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred19_fragment


    // $ANTLR start Synpred20_fragment
    public void mSynpred20_fragment() throws RecognitionException {
        int Synpred20_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 50) ) { return ; }
            // action.g:1:798: ( ERROR_X )
            // action.g:1:799: ERROR_X
            {
            mERROR_X(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 50, Synpred20_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred20_fragment


    // $ANTLR start Synpred21_fragment
    public void mSynpred21_fragment() throws RecognitionException {
        int Synpred21_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 51) ) { return ; }
            // action.g:1:817: ( UNKNOWN_SYNTAX )
            // action.g:1:818: UNKNOWN_SYNTAX
            {
            mUNKNOWN_SYNTAX(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 51, Synpred21_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred21_fragment


    // $ANTLR start Synpred22_fragment
    public void mSynpred22_fragment() throws RecognitionException {
        int Synpred22_fragment_StartIndex = input.index();
        try {
            if ( backtracking>0 && alreadyParsedRule(input, 52) ) { return ; }
            // action.g:1:850: ( TEXT )
            // action.g:1:851: TEXT
            {
            mTEXT(); if (failed) return ;

            }

        }
        finally {
            if ( backtracking>0 ) { memoize(input, 52, Synpred22_fragment_StartIndex); }
            if ( backtracking==0 ) {
            }
        }
        return ;
    }
    // $ANTLR end Synpred22_fragment

    class Synpred1Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred1_fragment();}
    }
    Synpred1Ptr Synpred1 = new Synpred1Ptr();
    class Synpred2Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred2_fragment();}
    }
    Synpred2Ptr Synpred2 = new Synpred2Ptr();
    class Synpred3Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred3_fragment();}
    }
    Synpred3Ptr Synpred3 = new Synpred3Ptr();
    class Synpred4Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred4_fragment();}
    }
    Synpred4Ptr Synpred4 = new Synpred4Ptr();
    class Synpred5Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred5_fragment();}
    }
    Synpred5Ptr Synpred5 = new Synpred5Ptr();
    class Synpred6Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred6_fragment();}
    }
    Synpred6Ptr Synpred6 = new Synpred6Ptr();
    class Synpred7Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred7_fragment();}
    }
    Synpred7Ptr Synpred7 = new Synpred7Ptr();
    class Synpred8Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred8_fragment();}
    }
    Synpred8Ptr Synpred8 = new Synpred8Ptr();
    class Synpred9Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred9_fragment();}
    }
    Synpred9Ptr Synpred9 = new Synpred9Ptr();
    class Synpred10Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred10_fragment();}
    }
    Synpred10Ptr Synpred10 = new Synpred10Ptr();
    class Synpred11Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred11_fragment();}
    }
    Synpred11Ptr Synpred11 = new Synpred11Ptr();
    class Synpred12Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred12_fragment();}
    }
    Synpred12Ptr Synpred12 = new Synpred12Ptr();
    class Synpred13Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred13_fragment();}
    }
    Synpred13Ptr Synpred13 = new Synpred13Ptr();
    class Synpred14Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred14_fragment();}
    }
    Synpred14Ptr Synpred14 = new Synpred14Ptr();
    class Synpred15Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred15_fragment();}
    }
    Synpred15Ptr Synpred15 = new Synpred15Ptr();
    class Synpred16Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred16_fragment();}
    }
    Synpred16Ptr Synpred16 = new Synpred16Ptr();
    class Synpred17Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred17_fragment();}
    }
    Synpred17Ptr Synpred17 = new Synpred17Ptr();
    class Synpred18Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred18_fragment();}
    }
    Synpred18Ptr Synpred18 = new Synpred18Ptr();
    class Synpred19Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred19_fragment();}
    }
    Synpred19Ptr Synpred19 = new Synpred19Ptr();
    class Synpred20Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred20_fragment();}
    }
    Synpred20Ptr Synpred20 = new Synpred20Ptr();
    class Synpred21Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred21_fragment();}
    }
    Synpred21Ptr Synpred21 = new Synpred21Ptr();
    class Synpred22Ptr implements GrammarFragmentPtr {
        public void invoke() throws RecognitionException {mSynpred22_fragment();}
    }
    Synpred22Ptr Synpred22 = new Synpred22Ptr();




}