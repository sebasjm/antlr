/*
 [The "BSD licence"]
 Copyright (c) 2005 Terence Parr
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
package org.antlr.codegen;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.*;

import java.util.List;

import antlr.CommonToken;

public class ActionTranslator {
	public static final char ATTRIBUTE_REF_CHAR = '$';

	protected CodeGenerator generator;
	protected Grammar grammar;

	public ActionTranslator(CodeGenerator generator)
	{
		this.generator = generator;
		grammar = generator.grammar;
	}

	public String translate(String ruleName,
						    antlr.Token actionToken,
							int outerAltNum)
	{
		GrammarAST actionAST = new GrammarAST();
		actionAST.initialize(actionToken);
		actionAST.outerAltNum = outerAltNum;
		return translate(ruleName, actionAST);
	}

	/** Given an action string with @x.y and @x references or $x.y/$x, convert it
	 *  to a StringTemplate (that will be inserted into the output StringTemplate)
	 *  Replace @ references to template references.  Targets can then say
	 *  how to translate these references with a template rather than code.
	 *
	 *  Jump from '@'/'$' to '@'/'$' in the action, building up a text buffer
	 *  doing appropriate rewrites to template refs.  Final step, create
	 *  the StringTemplate.
	 */
	public String translate(String ruleName,
						    GrammarAST actionAST)
	{
		antlr.Token actionToken = actionAST.getToken();
		String action = actionToken.getText();
		Rule r = null;
		if ( ruleName!=null ) {
			r = grammar.getRule(ruleName);
		}
		StringBuffer buf = new StringBuffer();
		int c=0;
		while ( c<action.length() ) {
			// Actions get converted to templates; escape raw '<'s in actions
			// as that is the template expression start: <expr>.
			if ( action.charAt(c)=='<' ) {
				buf.append("\\<");
				c++;
				continue;
			}
			if ( action.charAt(c)=='\\' &&
				 (c+1)<action.length() && action.charAt(c+1)=='$' )
			{
				buf.append("$"); // translate \$ to $; not an attribute
				c+=2;
				continue;
			}
			if ( action.charAt(c)==ATTRIBUTE_REF_CHAR ) {
				// $...
				c++; // skip $
				c = parseAttributeReference(r,action,c,buf,actionAST);
			}
			else {
				buf.append(action.charAt(c));
				c++;
			}
		}
		//System.out.println("translated action="+buf.toString());
		return buf.toString();
	}

	/** Given x of $x.y or $x, figure out what scope x is if any (x might
	 *  be a parameter, say, not a scope name).  The valid scopes are
	 *  $rulelabel, $tokenlabel, $rulename, or $scopename.
	 */
	protected AttributeScope resolveScope(Rule r, String scopeName) {
		if ( grammar.getGlobalScope(scopeName)!=null ) {
			// $scopename
			return grammar.getGlobalScope(scopeName);
		}
		Rule scopeRule = grammar.getRule(scopeName);
		if ( scopeRule!=null ) {
			return scopeRule.ruleScope;
		}
		if ( r==null ) { // action outside of rule must be global scope
			return null;
		}
		if ( r.name.equals(scopeName) ) {
			// $rulename
			return r.ruleScope;
		}
		if ( r.getTokenLabel(scopeName)!=null ) {
			// $tokenLabel
			return AttributeScope.tokenScope;
		}
		if ( r.getRuleLabel(scopeName)!=null ) {
			// $ruleLabel
			Grammar.LabelElementPair ruleLabel = r.getRuleLabel(scopeName);
			return new RuleLabelScope(ruleLabel.referencedRule);
		}
		return null;
	}

	/** What is the name of the template used to generate a reference to
	 *  an attribute.  This is perhaps un-OO as the various scopes could
	 *  answer what template generates code for that scope, but I like
	 *  having all the template names encapsulated in one spot and in
	 *  the codegen package.
	 */
	protected StringTemplate getScopedAttributeReferenceTemplate(Rule r,
																 AttributeScope scope,
																 String scopeName,
																 String attribute)
	{
		RuleLabelScope rlScope=null;
		String stName = null;
		if ( grammar.getGlobalScope(scopeName)!=null ) {
			// $scopename
			stName = "globalAttributeRef";
		}
		else if ( grammar.getRule(scopeName)!=null ) {
			stName = "ruleScopeAttributeRef";
		}
		else if ( r.name.equals(scopeName) ) {
			// $rulename
			stName = "ruleScopeAttributeRef";
		}
		else if ( r.getTokenLabel(scopeName)!=null ) {
			// $tokenLabel
			stName = "tokenLabelPropertyRef_"+attribute;
		}
		else if ( r.getRuleLabel(scopeName)!=null ) {
			// $ruleLabel
			rlScope = (RuleLabelScope)scope;
			if( RuleLabelScope.predefinedRuleProperties.contains(attribute) ) {
				stName = "ruleLabelPropertyRef_"+attribute;
				grammar.referenceRuleLabelPredefinedAttribute(rlScope.referencedRule.name);
			}
			else {
				stName = "ruleLabelRef";
			}
		}
		if ( stName==null ) {
			return null;
		}
		StringTemplate refST = generator.templates.getInstanceOf(stName);
		refST.setAttribute("scope", scopeName);
		refST.setAttribute("attr", scope.getAttribute(attribute));
		if ( stName.equals("ruleLabelRef") ) {
			refST.setAttribute("referencedRule", rlScope.referencedRule);
		}
		return refST;
	}

	/** Get $scope.attribute or just $scope if attribute not there.
	 *  Then translate according to scope.
	 */
	protected int parseAttributeReference(Rule r,
										String action,
										int c,
										StringBuffer buf,
										GrammarAST actionAST)
	{
		String attrRef=null;
		String scope = getID(action, c);
		c += scope.length();
		AttributeScope attrScope = resolveScope(r, scope);
		List ruleRefs = null;
		List tokenRefs = null;
		if ( r!=null ) {
			ruleRefs = r.getRuleRefsInAlt(scope, actionAST.outerAltNum);
			tokenRefs = r.getTokenRefsInAlt(scope, actionAST.outerAltNum);
		}
		if ( (c+1)<action.length() &&
			 action.charAt(c)=='.' && Character.isLetter(action.charAt(c+1)))
		{
			// $x.y
			int dotIndex = c;
			c++;
			String attributeName = getID(action, c);
			if ( attrScope!=null ) {
				// $scope.attributeName
				attrRef = translateAttributeReference(r, actionAST, attrScope, scope, attributeName);
				c += attributeName.length();
			}
			else if ( ruleRefs!=null ) {
				// $rule.attributeName
				attrRef =
					translateRuleReference(r, actionAST, scope, attributeName);
				c += attributeName.length();
			}
			else if ( tokenRefs!=null ) {
				// $token.attributeName
				attrRef =
					translateTokenReference(r, actionAST, scope, attributeName);
				c += attributeName.length();
			}
			else {
				// $arg.?, $retval.? $listlabel.? Translate before the dot only
				// (could also be $x.y for unknown x outside of a rule)
				attrRef = translateAttributeReference(r, actionAST, scope);
				c = dotIndex;
			}
		}
		else {
			// Isolated $x
			if ( tokenRefs!=null ) {
				// $token
				attrRef = translateTokenReference(r, actionAST, scope, null);
			}
			else {
				attrRef = translateAttributeReference(r, actionAST, scope);
			}
		}
		buf.append(attrRef);
		return c;
	}

	/** Translate $x.y where x is either a rule label or a token label.
	 *  y must be a valid attribute predefined or otherwise.  If x is
	 *  a rule label, then y must be one of the predefine attributes
	 *  like start, stop, tree or a return value.  If x is a token label,
	 *  then x must be a predefined attribute.
	 */
	protected String translateAttributeReference(Rule r,
												 GrammarAST actionAST,
												 AttributeScope scope,
												 String scopeName,
												 String attributeName)
	{
		antlr.Token actionToken = actionAST.getToken();
		String ref = ATTRIBUTE_REF_CHAR+scopeName+"."+attributeName;
		Attribute attribute = scope.getAttribute(attributeName);
		if ( attribute==null ) {
			int msgID = 0;
			// Spend some effort to generate good messages
			// $x.unknown
			if ( r.getTokenLabel(scopeName)!=null ) {
				// $tokenlabel.unknown, just return what you would for $x
				StringTemplate refST =
					generator.templates.getInstanceOf("tokenLabelRef");
				refST.setAttribute("label", scopeName);
				// we must ignore the unknown; put it in the translated action
                return refST.toString()+"."+attributeName;
			}
			// $rulename.unknown
			if ( scope.isDynamicRuleScope ) {
				msgID = ErrorManager.MSG_UNKNOWN_RULE_ATTRIBUTE;
			}
			// $rulelabel.unknown
			else if ( scope instanceof RuleLabelScope ) {
				Rule referencedRule = ((RuleLabelScope)scope).referencedRule;
				// $rulelabel.parameter
				if ( referencedRule.parameterScope!=null &&
					referencedRule.parameterScope.getAttribute(attributeName)!=null )
				{
					msgID = ErrorManager.MSG_INVALID_RULE_PARAMETER_REF;
				}
				// $rulelabel.dynamicscopeattribute
				else if ( referencedRule.ruleScope!=null &&
					referencedRule.ruleScope.getAttribute(attributeName)!=null )
				{
					msgID = ErrorManager.MSG_INVALID_RULE_SCOPE_ATTRIBUTE_REF;
				}
				// $rulename.unknown
				else if ( scope.getAttribute(attributeName)==null ) {
					msgID = ErrorManager.MSG_UNKNOWN_RULE_ATTRIBUTE;
				}
			}
			// general error for unknown y in x
			else {
				msgID = ErrorManager.MSG_UNKNOWN_ATTRIBUTE_IN_SCOPE;
			}

			ErrorManager.grammarError(msgID,
									  grammar,
									  actionToken,
									  scopeName,
									  attributeName);
			return ref;
		}
		StringTemplate refST =
			getScopedAttributeReferenceTemplate(r,scope,scopeName,attributeName);

		return refST.toString();
	}

	/** Translate $x where x is either a parameter, return value, local
	 *  rule-scope attribute, label, token name, or rule name.
	 *
	 *  $tokenLabel is handled in the $x.y translator as $tokenLabel is seen
	 *  as a valid scope.
	 */
	protected String translateAttributeReference(Rule r,
												 GrammarAST actionAST,
												 String attributeName)
	{
		antlr.Token actionToken = actionAST.getToken();
		String ref = ATTRIBUTE_REF_CHAR+attributeName;
		// cannot have isolated $x expressions outside of a rule
		if ( r==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_ATTRIBUTE_REF_NOT_IN_RULE,
									  grammar,
									  actionToken,
									  ref);
			return ref;
		}
		StringTemplate refST = null;
		AttributeScope scope = r.getAttributeScope(attributeName);
		Grammar.LabelElementPair pair = r.getLabel(attributeName);
		if ( pair!=null ) {
			// $label of some type
			switch ( pair.type ) {
				case Grammar.TOKEN_LABEL :
					// $tokenLabel
					refST = generator.templates.getInstanceOf("tokenLabelRef");
					refST.setAttribute("label", attributeName);
					break;
				case Grammar.LIST_LABEL :
					// $listLabel
					refST = generator.templates.getInstanceOf("tokenLabelRef");
					refST.setAttribute("label", attributeName);
					break;
				case Grammar.RULE_LABEL :
					// $ruleLabel
					ErrorManager.grammarError(ErrorManager.MSG_ISOLATED_RULE_ATTRIBUTE,
											  grammar,
											  actionToken,
											  ref);
					return ref;
			}
		}
		else {
			// ok, it's not a label; have to think a little harder then
			// $parameter or $returnValue
			List ruleRefs = r.getRuleRefsInAlt(attributeName, actionAST.outerAltNum);
			if ( ruleRefs!=null ) {
				ErrorManager.grammarError(ErrorManager.MSG_ISOLATED_RULE_ATTRIBUTE,
										  grammar,
										  actionToken,
										  ref);
				return ref;
			}
			if ( scope==null ) {
				ErrorManager.grammarError(ErrorManager.MSG_UNKNOWN_SIMPLE_ATTRIBUTE,
										  grammar,
										  actionToken,
										  ref);
				return ref;
			}
			if ( scope.isParameterScope ) {
				refST = generator.templates.getInstanceOf("parameterAttributeRef");
			}
			else if ( scope.isReturnScope ) {
				refST = generator.templates.getInstanceOf("returnAttributeRef");
				refST.setAttribute("ruleDescriptor", r);
			}
			else if ( scope.isDynamicRuleScope ) {
				refST = generator.templates.getInstanceOf("ruleScopeAttributeRef");
				refST.setAttribute("scope", r.name);
			}
			refST.setAttribute("attr", scope.getAttribute(attributeName));
		}
		return refST.toString();
	}

	/** For $ID, find unique ID reference and make it think it was
	 *  labeled.  Then just invoke the usual translation routine.
	 */
	protected String translateTokenReference(Rule r,
											 GrammarAST actionAST,
											 String tokenRefName,
											 String attributeName)
	{
		String ref = ATTRIBUTE_REF_CHAR+attributeName;
		List tokenRefs = r.getTokenRefsInAlt(tokenRefName, actionAST.outerAltNum);
		GrammarAST uniqueRefAST = (GrammarAST)tokenRefs.get(0);
		if ( uniqueRefAST.code==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_FORWARD_ELEMENT_REF,
									  grammar,
									  actionAST.getToken(),
									  ref);
			return ref;
		}
		String labelName = tokenRefName+"_"+actionAST.outerAltNum;
		StringTemplate existingLabelST =
			(StringTemplate)uniqueRefAST.code.getAttribute("labelST");
		if ( existingLabelST!=null ) {
			labelName = (String)existingLabelST.getAttribute("label");
		}
		else {
			// create new label
			CommonToken label = new CommonToken(ANTLRParser.ID, labelName);
			grammar.defineTokenRefLabel(r.name, label, uniqueRefAST);
			StringTemplate labelST = generator.templates.getInstanceOf("tokenLabel");
			labelST.setAttribute("label", labelName);
			uniqueRefAST.code.setAttribute("labelST", labelST);
		}
		AttributeScope scope = AttributeScope.tokenScope;
		if ( attributeName==null ) {
			return translateAttributeReference(r, actionAST, labelName);
		}
		else {
			return translateAttributeReference(r, actionAST, scope, labelName, attributeName);
		}
	}

	/** For $expr, find unique expr reference and make it think it was
	 *  labeled.  Then just invoke the usual translation routine.
	 */
	protected String translateRuleReference(Rule r,
											GrammarAST actionAST,
											String ruleRefName,
											String attributeName)
	{
		String ref = ATTRIBUTE_REF_CHAR+ruleRefName+"."+attributeName;
		List ruleRefs = r.getRuleRefsInAlt(ruleRefName, actionAST.outerAltNum);
		GrammarAST uniqueRefAST = (GrammarAST)ruleRefs.get(0);
		if ( uniqueRefAST.code==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_FORWARD_ELEMENT_REF,
									  grammar,
									  actionAST.getToken(),
									  ref);
			return ref;
		}
		String labelName = ruleRefName+"_"+actionAST.outerAltNum;
		String existingLabelName =
			(String)uniqueRefAST.code.getAttribute("label");
		if ( existingLabelName!=null ) {
			labelName = existingLabelName;
		}
		else {
			// create new label
			CommonToken label = new CommonToken(ANTLRParser.ID, labelName);
			grammar.defineRuleRefLabel(r.name, label, uniqueRefAST);
			uniqueRefAST.code.setAttribute("label", labelName);
		}
		Rule referencedRule = grammar.getRule(ruleRefName);
		AttributeScope scope = new RuleLabelScope(referencedRule);
		return translateAttributeReference(r, actionAST, scope, labelName, attributeName);
	}

	protected String getID(String action, int c) {
		int start = c;
		int i = c+1;
		while ( i<action.length() &&
			Character.isLetterOrDigit(action.charAt(i)) )
		{
			i++;
		}
		int end = i-1; // i points at char past first ID
		if ( end<start ) {
			return null;
		}
		return action.substring(start,end+1);
	}
}
