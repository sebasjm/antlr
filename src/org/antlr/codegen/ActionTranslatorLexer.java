// $ANTLR 3.0b5 ActionTranslator.g 2006-10-26 03:21:06

package org.antlr.codegen;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.runtime.*;
import org.antlr.tool.*;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
public class ActionTranslatorLexer extends Lexer {
    public static final int LOCAL_ATTR=15;
    public static final int SET_DYNAMIC_SCOPE_ATTR=16;
    public static final int ISOLATED_DYNAMIC_SCOPE=22;
    public static final int WS=5;
    public static final int UNKNOWN_SYNTAX=33;
    public static final int DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR=21;
    public static final int DYNAMIC_SCOPE_ATTR=17;
    public static final int SCOPE_INDEX_EXPR=19;
    public static final int ISOLATED_TOKEN_REF=12;
    public static final int SET_ATTRIBUTE=28;
    public static final int SET_EXPR_ATTRIBUTE=27;
    public static final int ACTION=25;
    public static final int ERROR_X=32;
    public static final int TEMPLATE_INSTANCE=24;
    public static final int TOKEN_SCOPE_ATTR=9;
    public static final int ISOLATED_LEXER_RULE_REF=13;
    public static final int ESC=30;
    public static final int SET_ENCLOSING_RULE_SCOPE_ATTR=7;
    public static final int ATTR_VALUE_EXPR=6;
    public static final int RULE_SCOPE_ATTR=10;
    public static final int LABEL_REF=11;
    public static final int INT=35;
    public static final int ARG=23;
    public static final int EOF=-1;
    public static final int SET_LOCAL_ATTR=14;
    public static final int TEXT=34;
    public static final int Tokens=36;
    public static final int DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR=20;
    public static final int ERROR_SCOPED_XY=18;
    public static final int ENCLOSING_RULE_SCOPE_ATTR=8;
    public static final int ERROR_XY=31;
    public static final int TEMPLATE_EXPR=29;
    public static final int INDIRECT_TEMPLATE_INSTANCE=26;
    public static final int ID=4;

    public List chunks = new ArrayList();
    Rule enclosingRule;
    int outerAltNum;
    Grammar grammar;
    CodeGenerator generator;
    antlr.Token actionToken;

    	public ActionTranslatorLexer(CodeGenerator generator,
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

    	public ActionTranslatorLexer(CodeGenerator generator,
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
    public ActionTranslatorLexer(CharStream input, CodeGenerator generator,
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
        ActionTranslatorLexer translator =
            new ActionTranslatorLexer(generator,
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



    public ActionTranslatorLexer() {;} 
    public ActionTranslatorLexer(CharStream input) {
        super(input);
        ruleMemo = new HashMap[58+1];
     }
    public String getGrammarFileName() { return "ActionTranslator.g"; }

    public Token nextToken() {
        while (true) {
            if ( input.LA(1)==CharStream.EOF ) {
                return Token.EOF_TOKEN;
            }
            token = null;
            tokenStartCharIndex = getCharIndex();
    	text = null;
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
    }// $ANTLR start SET_ENCLOSING_RULE_SCOPE_ATTR
    public void mSET_ENCLOSING_RULE_SCOPE_ATTR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = SET_ENCLOSING_RULE_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:201:4: ( '$' x= ID '.' y= ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' {...}?)
            // ActionTranslator.g:201:4: '$' x= ID '.' y= ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' {...}?
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            // ActionTranslator.g:201:22: ( WS )?
            int alt1=2;
            int LA1_0 = input.LA(1);
            if ( ((LA1_0>='\t' && LA1_0<='\n')||LA1_0==' ') ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // ActionTranslator.g:201:22: WS
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
            if ( !(enclosingRule!=null &&
            	                         x.getText().equals(enclosingRule.name) &&
            	                         enclosingRule.getLocalAttributeScope(y.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "SET_ENCLOSING_RULE_SCOPE_ATTR", "enclosingRule!=null &&\n\t                         $x.text.equals(enclosingRule.name) &&\n\t                         enclosingRule.getLocalAttributeScope($y.text)!=null");
            }
            if ( backtracking==1 ) {

              		StringTemplate st = null;
              		AttributeScope scope = enclosingRule.getLocalAttributeScope(y.getText());
              		if ( scope.isPredefinedRuleScope ) {
              			if ( y.getText() == "text") {
              				ErrorManager.grammarError(ErrorManager. MSG_WRITE_TO_READONLY_ATTR,
              										  grammar,
              										  actionToken,
              										  x.getText(),
              										  y.getText());
              			} else {
              				st = template("ruleSetPropertyRef_"+y.getText());
              				grammar.referenceRuleLabelPredefinedAttribute(x.getText());
              				st.setAttribute("scope", x.getText());
              				st.setAttribute("attr", y.getText());
              				st.setAttribute("expr", translateAction(expr.getText()));
              			}
              		}
              	    else if ( scope.isPredefinedLexerRuleScope ) {
              	    	// perhaps not the most precise error message to use, but...
              			ErrorManager.grammarError(ErrorManager.MSG_RULE_HAS_NO_ARGS,
              									  grammar,
              									  actionToken,
              									  x.getText());
              	    }
              		else if ( scope.isParameterScope ) {
              			// TODO: do we want to support write access to parameter scope?
              			st = template("parameterAttributeRef");
              			st.setAttribute("attr", scope.getAttribute(y.getText()));
              		}
              		else { // must be return value
              			st = template("returnSetAttributeRef");
              			st.setAttribute("ruleDescriptor", enclosingRule);
              			st.setAttribute("attr", scope.getAttribute(y.getText()));
              			st.setAttribute("expr", translateAction(expr.getText()));
              		}
              		
            }

            }


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end SET_ENCLOSING_RULE_SCOPE_ATTR

    // $ANTLR start ENCLOSING_RULE_SCOPE_ATTR
    public void mENCLOSING_RULE_SCOPE_ATTR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = ENCLOSING_RULE_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:245:4: ( '$' x= ID '.' y= ID {...}?)
            // ActionTranslator.g:245:4: '$' x= ID '.' y= ID {...}?
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ENCLOSING_RULE_SCOPE_ATTR

    // $ANTLR start TOKEN_SCOPE_ATTR
    public void mTOKEN_SCOPE_ATTR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = TOKEN_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:279:4: ( '$' x= ID '.' y= ID {...}?)
            // ActionTranslator.g:279:4: '$' x= ID '.' y= ID {...}?
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end TOKEN_SCOPE_ATTR

    // $ANTLR start RULE_SCOPE_ATTR
    public void mRULE_SCOPE_ATTR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = RULE_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;

            Grammar.LabelElementPair pair=null;
            String refdRuleName=null;

            // ActionTranslator.g:310:4: ( '$' x= ID '.' y= ID {...}?{...}?)
            // ActionTranslator.g:310:4: '$' x= ID '.' y= ID {...}?{...}?
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end RULE_SCOPE_ATTR

    // $ANTLR start LABEL_REF
    public void mLABEL_REF() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = LABEL_REF;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:368:4: ( '$' ID {...}?)
            // ActionTranslator.g:368:4: '$' ID {...}?
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end LABEL_REF

    // $ANTLR start ISOLATED_TOKEN_REF
    public void mISOLATED_TOKEN_REF() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = ISOLATED_TOKEN_REF;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:387:4: ( '$' ID {...}?)
            // ActionTranslator.g:387:4: '$' ID {...}?
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ISOLATED_TOKEN_REF

    // $ANTLR start ISOLATED_LEXER_RULE_REF
    public void mISOLATED_LEXER_RULE_REF() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = ISOLATED_LEXER_RULE_REF;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:407:4: ( '$' ID {...}?)
            // ActionTranslator.g:407:4: '$' ID {...}?
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ISOLATED_LEXER_RULE_REF

    // $ANTLR start SET_LOCAL_ATTR
    public void mSET_LOCAL_ATTR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = SET_LOCAL_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:439:5: ( '$' ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' {...}?)
            // ActionTranslator.g:439:5: '$' ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' {...}?
            {
            match('$'); if (failed) return ;
            int ID4Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID4 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID4Start, getCharIndex()-1);
            // ActionTranslator.g:439:12: ( WS )?
            int alt2=2;
            int LA2_0 = input.LA(1);
            if ( ((LA2_0>='\t' && LA2_0<='\n')||LA2_0==' ') ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // ActionTranslator.g:439:12: WS
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
            if ( !(enclosingRule!=null && enclosingRule.getLocalAttributeScope(ID4.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "SET_LOCAL_ATTR", "enclosingRule!=null && enclosingRule.getLocalAttributeScope($ID.text)!=null");
            }
            if ( backtracking==1 ) {

               		StringTemplate st;
               		AttributeScope scope = enclosingRule.getLocalAttributeScope(ID4.getText());
               		if ( scope.isPredefinedRuleScope ) {
               			st = template("ruleSetPropertyRef_"+ID4.getText());
               			grammar.referenceRuleLabelPredefinedAttribute(enclosingRule.name);
               			st.setAttribute("scope", enclosingRule.name);
               			st.setAttribute("attr", ID4.getText());
               			st.setAttribute("expr", translateAction(expr.getText()));
               		}
               		else if ( scope.isParameterScope ) {
               			st = template("parameterAttributeRef");
               			st.setAttribute("attr", scope.getAttribute(ID4.getText()));
               		}
               		else {
               			st = template("returnSetAttributeRef");
               			st.setAttribute("ruleDescriptor", enclosingRule);
               			st.setAttribute("attr", scope.getAttribute(ID4.getText()));
              			st.setAttribute("expr", translateAction(expr.getText()));
               		}
               		
            }

            }


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end SET_LOCAL_ATTR

    // $ANTLR start LOCAL_ATTR
    public void mLOCAL_ATTR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = LOCAL_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:464:4: ( '$' ID {...}?)
            // ActionTranslator.g:464:4: '$' ID {...}?
            {
            match('$'); if (failed) return ;
            int ID5Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID5 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID5Start, getCharIndex()-1);
            if ( !(enclosingRule!=null && enclosingRule.getLocalAttributeScope(ID5.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "LOCAL_ATTR", "enclosingRule!=null && enclosingRule.getLocalAttributeScope($ID.text)!=null");
            }
            if ( backtracking==1 ) {

              		StringTemplate st;
              		AttributeScope scope = enclosingRule.getLocalAttributeScope(ID5.getText());
              		if ( scope.isPredefinedRuleScope ) {
              			st = template("rulePropertyRef_"+ID5.getText());
              			grammar.referenceRuleLabelPredefinedAttribute(enclosingRule.name);
              			st.setAttribute("scope", enclosingRule.name);
              			st.setAttribute("attr", ID5.getText());
              		}
              		else if ( scope.isParameterScope ) {
              			st = template("parameterAttributeRef");
              			st.setAttribute("attr", scope.getAttribute(ID5.getText()));
              		}
              		else {
              			st = template("returnAttributeRef");
              			st.setAttribute("ruleDescriptor", enclosingRule);
              			st.setAttribute("attr", scope.getAttribute(ID5.getText()));
              		}
              		
            }

            }


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end LOCAL_ATTR

    // $ANTLR start SET_DYNAMIC_SCOPE_ATTR
    public void mSET_DYNAMIC_SCOPE_ATTR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = SET_DYNAMIC_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:500:4: ( '$' x= ID '::' y= ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' {...}?)
            // ActionTranslator.g:500:4: '$' x= ID '::' y= ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' {...}?
            {
            match('$'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match("::"); if (failed) return ;

            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            // ActionTranslator.g:500:23: ( WS )?
            int alt3=2;
            int LA3_0 = input.LA(1);
            if ( ((LA3_0>='\t' && LA3_0<='\n')||LA3_0==' ') ) {
                alt3=1;
            }
            switch (alt3) {
                case 1 :
                    // ActionTranslator.g:500:23: WS
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
            if ( !(resolveDynamicScope(x.getText())!=null &&
            						     resolveDynamicScope(x.getText()).getAttribute(y.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "SET_DYNAMIC_SCOPE_ATTR", "resolveDynamicScope($x.text)!=null &&\n\t\t\t\t\t\t     resolveDynamicScope($x.text).getAttribute($y.text)!=null");
            }
            if ( backtracking==1 ) {

              		AttributeScope scope = resolveDynamicScope(x.getText());
              		if ( scope!=null ) {
              			StringTemplate st = template("scopeSetAttributeRef");
              			st.setAttribute("scope", x.getText());
              			st.setAttribute("attr",  scope.getAttribute(y.getText()));
              			st.setAttribute("expr",  translateAction(expr.getText()));
              		}
              		else {
              			// error: invalid dynamic attribute
              		}
              		
            }

            }


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end SET_DYNAMIC_SCOPE_ATTR

    // $ANTLR start DYNAMIC_SCOPE_ATTR
    public void mDYNAMIC_SCOPE_ATTR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = DYNAMIC_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:519:4: ( '$' x= ID '::' y= ID {...}?)
            // ActionTranslator.g:519:4: '$' x= ID '::' y= ID {...}?
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end DYNAMIC_SCOPE_ATTR

    // $ANTLR start ERROR_SCOPED_XY
    public void mERROR_SCOPED_XY() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = ERROR_SCOPED_XY;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:538:4: ( '$' x= ID '::' y= ID )
            // ActionTranslator.g:538:4: '$' x= ID '::' y= ID
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ERROR_SCOPED_XY

    // $ANTLR start DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR
    public void mDYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:556:4: ( '$' x= ID '[' '-' expr= SCOPE_INDEX_EXPR ']' '::' y= ID )
            // ActionTranslator.g:556:4: '$' x= ID '[' '-' expr= SCOPE_INDEX_EXPR ']' '::' y= ID
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR

    // $ANTLR start DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR
    public void mDYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:567:4: ( '$' x= ID '[' expr= SCOPE_INDEX_EXPR ']' '::' y= ID )
            // ActionTranslator.g:567:4: '$' x= ID '[' expr= SCOPE_INDEX_EXPR ']' '::' y= ID
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR

    // $ANTLR start SCOPE_INDEX_EXPR
    public void mSCOPE_INDEX_EXPR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            // ActionTranslator.g:579:4: ( (~ ']' )+ )
            // ActionTranslator.g:579:4: (~ ']' )+
            {
            // ActionTranslator.g:579:4: (~ ']' )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);
                if ( ((LA4_0>='\u0000' && LA4_0<='\\')||(LA4_0>='^' && LA4_0<='\uFFFE')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // ActionTranslator.g:579:5: ~ ']'
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
            	    if ( cnt4 >= 1 ) break loop4;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);


            }

        }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end SCOPE_INDEX_EXPR

    // $ANTLR start ISOLATED_DYNAMIC_SCOPE
    public void mISOLATED_DYNAMIC_SCOPE() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = ISOLATED_DYNAMIC_SCOPE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:588:4: ( '$' ID {...}?)
            // ActionTranslator.g:588:4: '$' ID {...}?
            {
            match('$'); if (failed) return ;
            int ID6Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID6 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID6Start, getCharIndex()-1);
            if ( !(resolveDynamicScope(ID6.getText())!=null) ) {
                if (backtracking>0) {failed=true; return ;}
                throw new FailedPredicateException(input, "ISOLATED_DYNAMIC_SCOPE", "resolveDynamicScope($ID.text)!=null");
            }
            if ( backtracking==1 ) {

              		StringTemplate st = template("isolatedDynamicScopeRef");
              		st.setAttribute("scope", ID6.getText());
              		
            }

            }


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ISOLATED_DYNAMIC_SCOPE

    // $ANTLR start TEMPLATE_INSTANCE
    public void mTEMPLATE_INSTANCE() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = TEMPLATE_INSTANCE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:601:4: ( '%' ID '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')' )
            // ActionTranslator.g:601:4: '%' ID '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')'
            {
            match('%'); if (failed) return ;
            mID(); if (failed) return ;
            match('('); if (failed) return ;
            // ActionTranslator.g:601:15: ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )?
            int alt9=2;
            int LA9_0 = input.LA(1);
            if ( ((LA9_0>='\t' && LA9_0<='\n')||LA9_0==' '||(LA9_0>='A' && LA9_0<='Z')||LA9_0=='_'||(LA9_0>='a' && LA9_0<='z')) ) {
                alt9=1;
            }
            switch (alt9) {
                case 1 :
                    // ActionTranslator.g:601:17: ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )?
                    {
                    // ActionTranslator.g:601:17: ( WS )?
                    int alt5=2;
                    int LA5_0 = input.LA(1);
                    if ( ((LA5_0>='\t' && LA5_0<='\n')||LA5_0==' ') ) {
                        alt5=1;
                    }
                    switch (alt5) {
                        case 1 :
                            // ActionTranslator.g:601:17: WS
                            {
                            mWS(); if (failed) return ;

                            }
                            break;

                    }

                    mARG(); if (failed) return ;
                    // ActionTranslator.g:601:25: ( ',' ( WS )? ARG )*
                    loop7:
                    do {
                        int alt7=2;
                        int LA7_0 = input.LA(1);
                        if ( (LA7_0==',') ) {
                            alt7=1;
                        }


                        switch (alt7) {
                    	case 1 :
                    	    // ActionTranslator.g:601:26: ',' ( WS )? ARG
                    	    {
                    	    match(','); if (failed) return ;
                    	    // ActionTranslator.g:601:30: ( WS )?
                    	    int alt6=2;
                    	    int LA6_0 = input.LA(1);
                    	    if ( ((LA6_0>='\t' && LA6_0<='\n')||LA6_0==' ') ) {
                    	        alt6=1;
                    	    }
                    	    switch (alt6) {
                    	        case 1 :
                    	            // ActionTranslator.g:601:30: WS
                    	            {
                    	            mWS(); if (failed) return ;

                    	            }
                    	            break;

                    	    }

                    	    mARG(); if (failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop7;
                        }
                    } while (true);

                    // ActionTranslator.g:601:40: ( WS )?
                    int alt8=2;
                    int LA8_0 = input.LA(1);
                    if ( ((LA8_0>='\t' && LA8_0<='\n')||LA8_0==' ') ) {
                        alt8=1;
                    }
                    switch (alt8) {
                        case 1 :
                            // ActionTranslator.g:601:40: WS
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
              		String ruleName = "<outside-of-rule>";
              		if ( enclosingRule!=null ) {
              			ruleName = enclosingRule.name;
              		}
              		StringTemplate st =
              			generator.translateTemplateConstructor(ruleName,
              												   outerAltNum,
              												   actionToken,
              												   action);
              		if ( st!=null ) {
              			chunks.add(st);
              		}
              		
            }

            }


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end TEMPLATE_INSTANCE

    // $ANTLR start INDIRECT_TEMPLATE_INSTANCE
    public void mINDIRECT_TEMPLATE_INSTANCE() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = INDIRECT_TEMPLATE_INSTANCE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:622:4: ( '%' '(' ACTION ')' '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')' )
            // ActionTranslator.g:622:4: '%' '(' ACTION ')' '(' ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )? ')'
            {
            match('%'); if (failed) return ;
            match('('); if (failed) return ;
            mACTION(); if (failed) return ;
            match(')'); if (failed) return ;
            match('('); if (failed) return ;
            // ActionTranslator.g:622:27: ( ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )? )?
            int alt14=2;
            int LA14_0 = input.LA(1);
            if ( ((LA14_0>='\t' && LA14_0<='\n')||LA14_0==' '||(LA14_0>='A' && LA14_0<='Z')||LA14_0=='_'||(LA14_0>='a' && LA14_0<='z')) ) {
                alt14=1;
            }
            switch (alt14) {
                case 1 :
                    // ActionTranslator.g:622:29: ( WS )? ARG ( ',' ( WS )? ARG )* ( WS )?
                    {
                    // ActionTranslator.g:622:29: ( WS )?
                    int alt10=2;
                    int LA10_0 = input.LA(1);
                    if ( ((LA10_0>='\t' && LA10_0<='\n')||LA10_0==' ') ) {
                        alt10=1;
                    }
                    switch (alt10) {
                        case 1 :
                            // ActionTranslator.g:622:29: WS
                            {
                            mWS(); if (failed) return ;

                            }
                            break;

                    }

                    mARG(); if (failed) return ;
                    // ActionTranslator.g:622:37: ( ',' ( WS )? ARG )*
                    loop12:
                    do {
                        int alt12=2;
                        int LA12_0 = input.LA(1);
                        if ( (LA12_0==',') ) {
                            alt12=1;
                        }


                        switch (alt12) {
                    	case 1 :
                    	    // ActionTranslator.g:622:38: ',' ( WS )? ARG
                    	    {
                    	    match(','); if (failed) return ;
                    	    // ActionTranslator.g:622:42: ( WS )?
                    	    int alt11=2;
                    	    int LA11_0 = input.LA(1);
                    	    if ( ((LA11_0>='\t' && LA11_0<='\n')||LA11_0==' ') ) {
                    	        alt11=1;
                    	    }
                    	    switch (alt11) {
                    	        case 1 :
                    	            // ActionTranslator.g:622:42: WS
                    	            {
                    	            mWS(); if (failed) return ;

                    	            }
                    	            break;

                    	    }

                    	    mARG(); if (failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop12;
                        }
                    } while (true);

                    // ActionTranslator.g:622:52: ( WS )?
                    int alt13=2;
                    int LA13_0 = input.LA(1);
                    if ( ((LA13_0>='\t' && LA13_0<='\n')||LA13_0==' ') ) {
                        alt13=1;
                    }
                    switch (alt13) {
                        case 1 :
                            // ActionTranslator.g:622:52: WS
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end INDIRECT_TEMPLATE_INSTANCE

    // $ANTLR start ARG
    public void mARG() throws RecognitionException {
        try {
            ruleNestingLevel++;
            // ActionTranslator.g:636:7: ( ID '=' ACTION )
            // ActionTranslator.g:636:7: ID '=' ACTION
            {
            mID(); if (failed) return ;
            match('='); if (failed) return ;
            mACTION(); if (failed) return ;

            }

        }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ARG

    // $ANTLR start SET_EXPR_ATTRIBUTE
    public void mSET_EXPR_ATTRIBUTE() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = SET_EXPR_ATTRIBUTE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:641:4: ( '%' a= ACTION '.' ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' )
            // ActionTranslator.g:641:4: '%' a= ACTION '.' ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';'
            {
            match('%'); if (failed) return ;
            int aStart = getCharIndex();
            mACTION(); if (failed) return ;
            Token a = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, aStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int ID7Start = getCharIndex();
            mID(); if (failed) return ;
            Token ID7 = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, ID7Start, getCharIndex()-1);
            // ActionTranslator.g:641:24: ( WS )?
            int alt15=2;
            int LA15_0 = input.LA(1);
            if ( ((LA15_0>='\t' && LA15_0<='\n')||LA15_0==' ') ) {
                alt15=1;
            }
            switch (alt15) {
                case 1 :
                    // ActionTranslator.g:641:24: WS
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
              		st.setAttribute("attrName", ID7.getText());
              		st.setAttribute("expr", translateAction(expr.getText()));
              		
            }

            }


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end SET_EXPR_ATTRIBUTE

    // $ANTLR start SET_ATTRIBUTE
    public void mSET_ATTRIBUTE() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = SET_ATTRIBUTE;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:658:4: ( '%' x= ID '.' y= ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';' )
            // ActionTranslator.g:658:4: '%' x= ID '.' y= ID ( WS )? '=' expr= ATTR_VALUE_EXPR ';'
            {
            match('%'); if (failed) return ;
            int xStart = getCharIndex();
            mID(); if (failed) return ;
            Token x = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, xStart, getCharIndex()-1);
            match('.'); if (failed) return ;
            int yStart = getCharIndex();
            mID(); if (failed) return ;
            Token y = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.DEFAULT_CHANNEL, yStart, getCharIndex()-1);
            // ActionTranslator.g:658:22: ( WS )?
            int alt16=2;
            int LA16_0 = input.LA(1);
            if ( ((LA16_0>='\t' && LA16_0<='\n')||LA16_0==' ') ) {
                alt16=1;
            }
            switch (alt16) {
                case 1 :
                    // ActionTranslator.g:658:22: WS
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end SET_ATTRIBUTE

    // $ANTLR start ATTR_VALUE_EXPR
    public void mATTR_VALUE_EXPR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            // ActionTranslator.g:670:4: ( (~ ';' )+ )
            // ActionTranslator.g:670:4: (~ ';' )+
            {
            // ActionTranslator.g:670:4: (~ ';' )+
            int cnt17=0;
            loop17:
            do {
                int alt17=2;
                int LA17_0 = input.LA(1);
                if ( ((LA17_0>='\u0000' && LA17_0<=':')||(LA17_0>='<' && LA17_0<='\uFFFE')) ) {
                    alt17=1;
                }


                switch (alt17) {
            	case 1 :
            	    // ActionTranslator.g:670:5: ~ ';'
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
            	    if ( cnt17 >= 1 ) break loop17;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(17, input);
                        throw eee;
                }
                cnt17++;
            } while (true);


            }

        }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ATTR_VALUE_EXPR

    // $ANTLR start TEMPLATE_EXPR
    public void mTEMPLATE_EXPR() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = TEMPLATE_EXPR;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:675:4: ( '%' a= ACTION )
            // ActionTranslator.g:675:4: '%' a= ACTION
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end TEMPLATE_EXPR

    // $ANTLR start ACTION
    public void mACTION() throws RecognitionException {
        try {
            ruleNestingLevel++;
            // ActionTranslator.g:687:4: ( '{' ( options {greedy=false; } : . )* '}' )
            // ActionTranslator.g:687:4: '{' ( options {greedy=false; } : . )* '}'
            {
            match('{'); if (failed) return ;
            // ActionTranslator.g:687:8: ( options {greedy=false; } : . )*
            loop18:
            do {
                int alt18=2;
                int LA18_0 = input.LA(1);
                if ( (LA18_0=='}') ) {
                    alt18=2;
                }
                else if ( ((LA18_0>='\u0000' && LA18_0<='|')||(LA18_0>='~' && LA18_0<='\uFFFE')) ) {
                    alt18=1;
                }


                switch (alt18) {
            	case 1 :
            	    // ActionTranslator.g:687:33: .
            	    {
            	    matchAny(); if (failed) return ;

            	    }
            	    break;

            	default :
            	    break loop18;
                }
            } while (true);

            match('}'); if (failed) return ;

            }

        }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ACTION

    // $ANTLR start ESC
    public void mESC() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = ESC;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:690:9: ( '\\\\' '$' | '\\\\' '%' | '\\\\' ~ ('$'|'%'))
            int alt19=3;
            int LA19_0 = input.LA(1);
            if ( (LA19_0=='\\') ) {
                int LA19_1 = input.LA(2);
                if ( ((LA19_1>='\u0000' && LA19_1<='#')||(LA19_1>='&' && LA19_1<='\uFFFE')) ) {
                    alt19=3;
                }
                else if ( (LA19_1=='%') ) {
                    alt19=2;
                }
                else if ( (LA19_1=='$') ) {
                    alt19=1;
                }
                else {
                    if (backtracking>0) {failed=true; return ;}
                    NoViableAltException nvae =
                        new NoViableAltException("690:1: ESC : ( '\\\\' '$' | '\\\\' '%' | '\\\\' ~ ('$'|'%'));", 19, 1, input);

                    throw nvae;
                }
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("690:1: ESC : ( '\\\\' '$' | '\\\\' '%' | '\\\\' ~ ('$'|'%'));", 19, 0, input);

                throw nvae;
            }
            switch (alt19) {
                case 1 :
                    // ActionTranslator.g:690:9: '\\\\' '$'
                    {
                    match('\\'); if (failed) return ;
                    match('$'); if (failed) return ;
                    if ( backtracking==1 ) {
                      chunks.add("$");
                    }

                    }
                    break;
                case 2 :
                    // ActionTranslator.g:691:4: '\\\\' '%'
                    {
                    match('\\'); if (failed) return ;
                    match('%'); if (failed) return ;
                    if ( backtracking==1 ) {
                      chunks.add("%");
                    }

                    }
                    break;
                case 3 :
                    // ActionTranslator.g:692:4: '\\\\' ~ ('$'|'%')
                    {
                    match('\\'); if (failed) return ;
                    if ( (input.LA(1)>='\u0000' && input.LA(1)<='#')||(input.LA(1)>='&' && input.LA(1)<='\uFFFE') ) {
                        input.consume();
                    failed=false;
                    }
                    else {
                        if (backtracking>0) {failed=true; return ;}
                        MismatchedSetException mse =
                            new MismatchedSetException(null,input);
                        recover(mse);    throw mse;
                    }

                    if ( backtracking==1 ) {
                      chunks.add(getText());
                    }

                    }
                    break;

            }

            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ESC

    // $ANTLR start ERROR_XY
    public void mERROR_XY() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = ERROR_XY;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:696:4: ( '$' x= ID '.' y= ID )
            // ActionTranslator.g:696:4: '$' x= ID '.' y= ID
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ERROR_XY

    // $ANTLR start ERROR_X
    public void mERROR_X() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = ERROR_X;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:706:4: ( '$' x= ID )
            // ActionTranslator.g:706:4: '$' x= ID
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


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ERROR_X

    // $ANTLR start UNKNOWN_SYNTAX
    public void mUNKNOWN_SYNTAX() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = UNKNOWN_SYNTAX;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:716:4: ( '$' | '%' ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )* )
            int alt21=2;
            int LA21_0 = input.LA(1);
            if ( (LA21_0=='$') ) {
                alt21=1;
            }
            else if ( (LA21_0=='%') ) {
                alt21=2;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("715:1: UNKNOWN_SYNTAX : ( '$' | '%' ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )* );", 21, 0, input);

                throw nvae;
            }
            switch (alt21) {
                case 1 :
                    // ActionTranslator.g:716:4: '$'
                    {
                    match('$'); if (failed) return ;
                    if ( backtracking==1 ) {

                      		chunks.add(getText());
                      		// shouldn't need an error here.  Just accept $ if it doesn't look like anything
                      		
                    }

                    }
                    break;
                case 2 :
                    // ActionTranslator.g:721:4: '%' ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )*
                    {
                    match('%'); if (failed) return ;
                    // ActionTranslator.g:721:8: ( ID | '.' | '(' | ')' | ',' | '{' | '}' | '\"' )*
                    loop20:
                    do {
                        int alt20=9;
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
                            alt20=1;
                            break;
                        case '.':
                            alt20=2;
                            break;
                        case '(':
                            alt20=3;
                            break;
                        case ')':
                            alt20=4;
                            break;
                        case ',':
                            alt20=5;
                            break;
                        case '{':
                            alt20=6;
                            break;
                        case '}':
                            alt20=7;
                            break;
                        case '\"':
                            alt20=8;
                            break;

                        }

                        switch (alt20) {
                    	case 1 :
                    	    // ActionTranslator.g:721:9: ID
                    	    {
                    	    mID(); if (failed) return ;

                    	    }
                    	    break;
                    	case 2 :
                    	    // ActionTranslator.g:721:12: '.'
                    	    {
                    	    match('.'); if (failed) return ;

                    	    }
                    	    break;
                    	case 3 :
                    	    // ActionTranslator.g:721:16: '('
                    	    {
                    	    match('('); if (failed) return ;

                    	    }
                    	    break;
                    	case 4 :
                    	    // ActionTranslator.g:721:20: ')'
                    	    {
                    	    match(')'); if (failed) return ;

                    	    }
                    	    break;
                    	case 5 :
                    	    // ActionTranslator.g:721:24: ','
                    	    {
                    	    match(','); if (failed) return ;

                    	    }
                    	    break;
                    	case 6 :
                    	    // ActionTranslator.g:721:28: '{'
                    	    {
                    	    match('{'); if (failed) return ;

                    	    }
                    	    break;
                    	case 7 :
                    	    // ActionTranslator.g:721:32: '}'
                    	    {
                    	    match('}'); if (failed) return ;

                    	    }
                    	    break;
                    	case 8 :
                    	    // ActionTranslator.g:721:36: '\"'
                    	    {
                    	    match('\"'); if (failed) return ;

                    	    }
                    	    break;

                    	default :
                    	    break loop20;
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

            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end UNKNOWN_SYNTAX

    // $ANTLR start TEXT
    public void mTEXT() throws RecognitionException {
        try {
            ruleNestingLevel++;
            int type = TEXT;
            int start = getCharIndex();
            int line = getLine();
            int charPosition = getCharPositionInLine();
            int channel = Token.DEFAULT_CHANNEL;
            // ActionTranslator.g:731:7: ( (~ ('$'|'%'|'\\\\'))+ )
            // ActionTranslator.g:731:7: (~ ('$'|'%'|'\\\\'))+
            {
            // ActionTranslator.g:731:7: (~ ('$'|'%'|'\\\\'))+
            int cnt22=0;
            loop22:
            do {
                int alt22=2;
                int LA22_0 = input.LA(1);
                if ( ((LA22_0>='\u0000' && LA22_0<='#')||(LA22_0>='&' && LA22_0<='[')||(LA22_0>=']' && LA22_0<='\uFFFE')) ) {
                    alt22=1;
                }


                switch (alt22) {
            	case 1 :
            	    // ActionTranslator.g:731:7: ~ ('$'|'%'|'\\\\')
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
            	    if ( cnt22 >= 1 ) break loop22;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(22, input);
                        throw eee;
                }
                cnt22++;
            } while (true);

            if ( backtracking==1 ) {
              chunks.add(getText());
            }

            }


            if ( backtracking==1 ) {

                      if ( token==null && ruleNestingLevel==1 ) {
                          emit(type,line,charPosition,channel,start,getCharIndex()-1);
                      }

                      
            }    }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end TEXT

    // $ANTLR start ID
    public void mID() throws RecognitionException {
        try {
            ruleNestingLevel++;
            // ActionTranslator.g:735:9: ( ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))* )
            // ActionTranslator.g:735:9: ('a'..'z'|'A'..'Z'|'_') ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))*
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

            // ActionTranslator.g:735:33: ( ('a'..'z'|'A'..'Z'|'_'|'0'..'9'))*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);
                if ( ((LA23_0>='0' && LA23_0<='9')||(LA23_0>='A' && LA23_0<='Z')||LA23_0=='_'||(LA23_0>='a' && LA23_0<='z')) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // ActionTranslator.g:735:34: ('a'..'z'|'A'..'Z'|'_'|'0'..'9')
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
            	    break loop23;
                }
            } while (true);


            }

        }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end ID

    // $ANTLR start INT
    public void mINT() throws RecognitionException {
        try {
            ruleNestingLevel++;
            // ActionTranslator.g:739:7: ( ( '0' .. '9' )+ )
            // ActionTranslator.g:739:7: ( '0' .. '9' )+
            {
            // ActionTranslator.g:739:7: ( '0' .. '9' )+
            int cnt24=0;
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);
                if ( ((LA24_0>='0' && LA24_0<='9')) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // ActionTranslator.g:739:7: '0' .. '9'
            	    {
            	    matchRange('0','9'); if (failed) return ;

            	    }
            	    break;

            	default :
            	    if ( cnt24 >= 1 ) break loop24;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(24, input);
                        throw eee;
                }
                cnt24++;
            } while (true);


            }

        }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end INT

    // $ANTLR start WS
    public void mWS() throws RecognitionException {
        try {
            ruleNestingLevel++;
            // ActionTranslator.g:743:6: ( ( (' '|'\\t'|'\\n'))+ )
            // ActionTranslator.g:743:6: ( (' '|'\\t'|'\\n'))+
            {
            // ActionTranslator.g:743:6: ( (' '|'\\t'|'\\n'))+
            int cnt25=0;
            loop25:
            do {
                int alt25=2;
                int LA25_0 = input.LA(1);
                if ( ((LA25_0>='\t' && LA25_0<='\n')||LA25_0==' ') ) {
                    alt25=1;
                }


                switch (alt25) {
            	case 1 :
            	    // ActionTranslator.g:743:7: (' '|'\\t'|'\\n')
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
            	    if ( cnt25 >= 1 ) break loop25;
            	    if (backtracking>0) {failed=true; return ;}
                        EarlyExitException eee =
                            new EarlyExitException(25, input);
                        throw eee;
                }
                cnt25++;
            } while (true);


            }

        }
        finally {
            ruleNestingLevel--;
        }
    }
    // $ANTLR end WS

    public void mTokens() throws RecognitionException {
        // ActionTranslator.g:1:25: ( ( SET_ENCLOSING_RULE_SCOPE_ATTR )=> SET_ENCLOSING_RULE_SCOPE_ATTR | ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( SET_LOCAL_ATTR )=> SET_LOCAL_ATTR | ( LOCAL_ATTR )=> LOCAL_ATTR | ( SET_DYNAMIC_SCOPE_ATTR )=> SET_DYNAMIC_SCOPE_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT )
        int alt26=25;
        int LA26_0 = input.LA(1);
        if ( (LA26_0=='$') ) {
            if ( (synpred1()) ) {
                alt26=1;
            }
            else if ( (synpred2()) ) {
                alt26=2;
            }
            else if ( (synpred3()) ) {
                alt26=3;
            }
            else if ( (synpred4()) ) {
                alt26=4;
            }
            else if ( (synpred5()) ) {
                alt26=5;
            }
            else if ( (synpred6()) ) {
                alt26=6;
            }
            else if ( (synpred7()) ) {
                alt26=7;
            }
            else if ( (synpred8()) ) {
                alt26=8;
            }
            else if ( (synpred9()) ) {
                alt26=9;
            }
            else if ( (synpred10()) ) {
                alt26=10;
            }
            else if ( (synpred11()) ) {
                alt26=11;
            }
            else if ( (synpred12()) ) {
                alt26=12;
            }
            else if ( (synpred13()) ) {
                alt26=13;
            }
            else if ( (synpred14()) ) {
                alt26=14;
            }
            else if ( (synpred15()) ) {
                alt26=15;
            }
            else if ( (synpred22()) ) {
                alt26=22;
            }
            else if ( (synpred23()) ) {
                alt26=23;
            }
            else if ( (synpred24()) ) {
                alt26=24;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("1:1: Tokens options {k=1; } : ( ( SET_ENCLOSING_RULE_SCOPE_ATTR )=> SET_ENCLOSING_RULE_SCOPE_ATTR | ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( SET_LOCAL_ATTR )=> SET_LOCAL_ATTR | ( LOCAL_ATTR )=> LOCAL_ATTR | ( SET_DYNAMIC_SCOPE_ATTR )=> SET_DYNAMIC_SCOPE_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT );", 26, 1, input);

                throw nvae;
            }
        }
        else if ( (LA26_0=='%') ) {
            if ( (synpred16()) ) {
                alt26=16;
            }
            else if ( (synpred17()) ) {
                alt26=17;
            }
            else if ( (synpred18()) ) {
                alt26=18;
            }
            else if ( (synpred19()) ) {
                alt26=19;
            }
            else if ( (synpred20()) ) {
                alt26=20;
            }
            else if ( (synpred24()) ) {
                alt26=24;
            }
            else {
                if (backtracking>0) {failed=true; return ;}
                NoViableAltException nvae =
                    new NoViableAltException("1:1: Tokens options {k=1; } : ( ( SET_ENCLOSING_RULE_SCOPE_ATTR )=> SET_ENCLOSING_RULE_SCOPE_ATTR | ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( SET_LOCAL_ATTR )=> SET_LOCAL_ATTR | ( LOCAL_ATTR )=> LOCAL_ATTR | ( SET_DYNAMIC_SCOPE_ATTR )=> SET_DYNAMIC_SCOPE_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT );", 26, 2, input);

                throw nvae;
            }
        }
        else if ( (LA26_0=='\\') ) {
            alt26=21;
        }
        else if ( ((LA26_0>='\u0000' && LA26_0<='#')||(LA26_0>='&' && LA26_0<='[')||(LA26_0>=']' && LA26_0<='\uFFFE')) ) {
            alt26=25;
        }
        else {
            if (backtracking>0) {failed=true; return ;}
            NoViableAltException nvae =
                new NoViableAltException("1:1: Tokens options {k=1; } : ( ( SET_ENCLOSING_RULE_SCOPE_ATTR )=> SET_ENCLOSING_RULE_SCOPE_ATTR | ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR | ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR | ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR | ( LABEL_REF )=> LABEL_REF | ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF | ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF | ( SET_LOCAL_ATTR )=> SET_LOCAL_ATTR | ( LOCAL_ATTR )=> LOCAL_ATTR | ( SET_DYNAMIC_SCOPE_ATTR )=> SET_DYNAMIC_SCOPE_ATTR | ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR | ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY | ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR | ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR | ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE | ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE | ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE | ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE | ( SET_ATTRIBUTE )=> SET_ATTRIBUTE | ( TEMPLATE_EXPR )=> TEMPLATE_EXPR | ( ESC )=> ESC | ( ERROR_XY )=> ERROR_XY | ( ERROR_X )=> ERROR_X | ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX | ( TEXT )=> TEXT );", 26, 0, input);

            throw nvae;
        }
        switch (alt26) {
            case 1 :
                // ActionTranslator.g:1:25: ( SET_ENCLOSING_RULE_SCOPE_ATTR )=> SET_ENCLOSING_RULE_SCOPE_ATTR
                {
                mSET_ENCLOSING_RULE_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 2 :
                // ActionTranslator.g:1:88: ( ENCLOSING_RULE_SCOPE_ATTR )=> ENCLOSING_RULE_SCOPE_ATTR
                {
                mENCLOSING_RULE_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 3 :
                // ActionTranslator.g:1:143: ( TOKEN_SCOPE_ATTR )=> TOKEN_SCOPE_ATTR
                {
                mTOKEN_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 4 :
                // ActionTranslator.g:1:180: ( RULE_SCOPE_ATTR )=> RULE_SCOPE_ATTR
                {
                mRULE_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 5 :
                // ActionTranslator.g:1:215: ( LABEL_REF )=> LABEL_REF
                {
                mLABEL_REF(); if (failed) return ;

                }
                break;
            case 6 :
                // ActionTranslator.g:1:238: ( ISOLATED_TOKEN_REF )=> ISOLATED_TOKEN_REF
                {
                mISOLATED_TOKEN_REF(); if (failed) return ;

                }
                break;
            case 7 :
                // ActionTranslator.g:1:279: ( ISOLATED_LEXER_RULE_REF )=> ISOLATED_LEXER_RULE_REF
                {
                mISOLATED_LEXER_RULE_REF(); if (failed) return ;

                }
                break;
            case 8 :
                // ActionTranslator.g:1:330: ( SET_LOCAL_ATTR )=> SET_LOCAL_ATTR
                {
                mSET_LOCAL_ATTR(); if (failed) return ;

                }
                break;
            case 9 :
                // ActionTranslator.g:1:363: ( LOCAL_ATTR )=> LOCAL_ATTR
                {
                mLOCAL_ATTR(); if (failed) return ;

                }
                break;
            case 10 :
                // ActionTranslator.g:1:388: ( SET_DYNAMIC_SCOPE_ATTR )=> SET_DYNAMIC_SCOPE_ATTR
                {
                mSET_DYNAMIC_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 11 :
                // ActionTranslator.g:1:437: ( DYNAMIC_SCOPE_ATTR )=> DYNAMIC_SCOPE_ATTR
                {
                mDYNAMIC_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 12 :
                // ActionTranslator.g:1:478: ( ERROR_SCOPED_XY )=> ERROR_SCOPED_XY
                {
                mERROR_SCOPED_XY(); if (failed) return ;

                }
                break;
            case 13 :
                // ActionTranslator.g:1:513: ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )=> DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR
                {
                mDYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 14 :
                // ActionTranslator.g:1:588: ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )=> DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR
                {
                mDYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR(); if (failed) return ;

                }
                break;
            case 15 :
                // ActionTranslator.g:1:663: ( ISOLATED_DYNAMIC_SCOPE )=> ISOLATED_DYNAMIC_SCOPE
                {
                mISOLATED_DYNAMIC_SCOPE(); if (failed) return ;

                }
                break;
            case 16 :
                // ActionTranslator.g:1:712: ( TEMPLATE_INSTANCE )=> TEMPLATE_INSTANCE
                {
                mTEMPLATE_INSTANCE(); if (failed) return ;

                }
                break;
            case 17 :
                // ActionTranslator.g:1:751: ( INDIRECT_TEMPLATE_INSTANCE )=> INDIRECT_TEMPLATE_INSTANCE
                {
                mINDIRECT_TEMPLATE_INSTANCE(); if (failed) return ;

                }
                break;
            case 18 :
                // ActionTranslator.g:1:808: ( SET_EXPR_ATTRIBUTE )=> SET_EXPR_ATTRIBUTE
                {
                mSET_EXPR_ATTRIBUTE(); if (failed) return ;

                }
                break;
            case 19 :
                // ActionTranslator.g:1:849: ( SET_ATTRIBUTE )=> SET_ATTRIBUTE
                {
                mSET_ATTRIBUTE(); if (failed) return ;

                }
                break;
            case 20 :
                // ActionTranslator.g:1:880: ( TEMPLATE_EXPR )=> TEMPLATE_EXPR
                {
                mTEMPLATE_EXPR(); if (failed) return ;

                }
                break;
            case 21 :
                // ActionTranslator.g:1:911: ( ESC )=> ESC
                {
                mESC(); if (failed) return ;

                }
                break;
            case 22 :
                // ActionTranslator.g:1:922: ( ERROR_XY )=> ERROR_XY
                {
                mERROR_XY(); if (failed) return ;

                }
                break;
            case 23 :
                // ActionTranslator.g:1:943: ( ERROR_X )=> ERROR_X
                {
                mERROR_X(); if (failed) return ;

                }
                break;
            case 24 :
                // ActionTranslator.g:1:962: ( UNKNOWN_SYNTAX )=> UNKNOWN_SYNTAX
                {
                mUNKNOWN_SYNTAX(); if (failed) return ;

                }
                break;
            case 25 :
                // ActionTranslator.g:1:995: ( TEXT )=> TEXT
                {
                mTEXT(); if (failed) return ;

                }
                break;

        }

    }

    // $ANTLR start synpred1
    public void synpred1_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:25: ( SET_ENCLOSING_RULE_SCOPE_ATTR )
        // ActionTranslator.g:1:26: SET_ENCLOSING_RULE_SCOPE_ATTR
        {
        mSET_ENCLOSING_RULE_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred1

    // $ANTLR start synpred2
    public void synpred2_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:88: ( ENCLOSING_RULE_SCOPE_ATTR )
        // ActionTranslator.g:1:89: ENCLOSING_RULE_SCOPE_ATTR
        {
        mENCLOSING_RULE_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred2

    // $ANTLR start synpred3
    public void synpred3_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:143: ( TOKEN_SCOPE_ATTR )
        // ActionTranslator.g:1:144: TOKEN_SCOPE_ATTR
        {
        mTOKEN_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred3

    // $ANTLR start synpred4
    public void synpred4_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:180: ( RULE_SCOPE_ATTR )
        // ActionTranslator.g:1:181: RULE_SCOPE_ATTR
        {
        mRULE_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred4

    // $ANTLR start synpred5
    public void synpred5_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:215: ( LABEL_REF )
        // ActionTranslator.g:1:216: LABEL_REF
        {
        mLABEL_REF(); if (failed) return ;

        }
    }
    // $ANTLR end synpred5

    // $ANTLR start synpred6
    public void synpred6_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:238: ( ISOLATED_TOKEN_REF )
        // ActionTranslator.g:1:239: ISOLATED_TOKEN_REF
        {
        mISOLATED_TOKEN_REF(); if (failed) return ;

        }
    }
    // $ANTLR end synpred6

    // $ANTLR start synpred7
    public void synpred7_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:279: ( ISOLATED_LEXER_RULE_REF )
        // ActionTranslator.g:1:280: ISOLATED_LEXER_RULE_REF
        {
        mISOLATED_LEXER_RULE_REF(); if (failed) return ;

        }
    }
    // $ANTLR end synpred7

    // $ANTLR start synpred8
    public void synpred8_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:330: ( SET_LOCAL_ATTR )
        // ActionTranslator.g:1:331: SET_LOCAL_ATTR
        {
        mSET_LOCAL_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred8

    // $ANTLR start synpred9
    public void synpred9_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:363: ( LOCAL_ATTR )
        // ActionTranslator.g:1:364: LOCAL_ATTR
        {
        mLOCAL_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred9

    // $ANTLR start synpred10
    public void synpred10_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:388: ( SET_DYNAMIC_SCOPE_ATTR )
        // ActionTranslator.g:1:389: SET_DYNAMIC_SCOPE_ATTR
        {
        mSET_DYNAMIC_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred10

    // $ANTLR start synpred11
    public void synpred11_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:437: ( DYNAMIC_SCOPE_ATTR )
        // ActionTranslator.g:1:438: DYNAMIC_SCOPE_ATTR
        {
        mDYNAMIC_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred11

    // $ANTLR start synpred12
    public void synpred12_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:478: ( ERROR_SCOPED_XY )
        // ActionTranslator.g:1:479: ERROR_SCOPED_XY
        {
        mERROR_SCOPED_XY(); if (failed) return ;

        }
    }
    // $ANTLR end synpred12

    // $ANTLR start synpred13
    public void synpred13_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:513: ( DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR )
        // ActionTranslator.g:1:514: DYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR
        {
        mDYNAMIC_NEGATIVE_INDEXED_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred13

    // $ANTLR start synpred14
    public void synpred14_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:588: ( DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR )
        // ActionTranslator.g:1:589: DYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR
        {
        mDYNAMIC_ABSOLUTE_INDEXED_SCOPE_ATTR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred14

    // $ANTLR start synpred15
    public void synpred15_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:663: ( ISOLATED_DYNAMIC_SCOPE )
        // ActionTranslator.g:1:664: ISOLATED_DYNAMIC_SCOPE
        {
        mISOLATED_DYNAMIC_SCOPE(); if (failed) return ;

        }
    }
    // $ANTLR end synpred15

    // $ANTLR start synpred16
    public void synpred16_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:712: ( TEMPLATE_INSTANCE )
        // ActionTranslator.g:1:713: TEMPLATE_INSTANCE
        {
        mTEMPLATE_INSTANCE(); if (failed) return ;

        }
    }
    // $ANTLR end synpred16

    // $ANTLR start synpred17
    public void synpred17_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:751: ( INDIRECT_TEMPLATE_INSTANCE )
        // ActionTranslator.g:1:752: INDIRECT_TEMPLATE_INSTANCE
        {
        mINDIRECT_TEMPLATE_INSTANCE(); if (failed) return ;

        }
    }
    // $ANTLR end synpred17

    // $ANTLR start synpred18
    public void synpred18_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:808: ( SET_EXPR_ATTRIBUTE )
        // ActionTranslator.g:1:809: SET_EXPR_ATTRIBUTE
        {
        mSET_EXPR_ATTRIBUTE(); if (failed) return ;

        }
    }
    // $ANTLR end synpred18

    // $ANTLR start synpred19
    public void synpred19_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:849: ( SET_ATTRIBUTE )
        // ActionTranslator.g:1:850: SET_ATTRIBUTE
        {
        mSET_ATTRIBUTE(); if (failed) return ;

        }
    }
    // $ANTLR end synpred19

    // $ANTLR start synpred20
    public void synpred20_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:880: ( TEMPLATE_EXPR )
        // ActionTranslator.g:1:881: TEMPLATE_EXPR
        {
        mTEMPLATE_EXPR(); if (failed) return ;

        }
    }
    // $ANTLR end synpred20

    // $ANTLR start synpred22
    public void synpred22_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:922: ( ERROR_XY )
        // ActionTranslator.g:1:923: ERROR_XY
        {
        mERROR_XY(); if (failed) return ;

        }
    }
    // $ANTLR end synpred22

    // $ANTLR start synpred23
    public void synpred23_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:943: ( ERROR_X )
        // ActionTranslator.g:1:944: ERROR_X
        {
        mERROR_X(); if (failed) return ;

        }
    }
    // $ANTLR end synpred23

    // $ANTLR start synpred24
    public void synpred24_fragment() throws RecognitionException {   
        // ActionTranslator.g:1:962: ( UNKNOWN_SYNTAX )
        // ActionTranslator.g:1:963: UNKNOWN_SYNTAX
        {
        mUNKNOWN_SYNTAX(); if (failed) return ;

        }
    }
    // $ANTLR end synpred24

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
    public boolean synpred23() {
        backtracking++;
        int start = input.mark();
        try {
            synpred23_fragment(); // can never throw exception
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
    public boolean synpred24() {
        backtracking++;
        int start = input.mark();
        try {
            synpred24_fragment(); // can never throw exception
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


 

}