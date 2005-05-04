package org.antlr.codegen;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.*;
import org.antlr.runtime.Token;

public class ActionTranslator {
	public static final char ATTRIBUTE_REF_CHAR = '$';

	protected CodeGenerator generator;
	protected Grammar grammar;

	public ActionTranslator(CodeGenerator generator)
	{
		this.generator = generator;
		grammar = generator.grammar;
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
						    antlr.Token actionToken)
	{
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
			if ( action.charAt(c)==ATTRIBUTE_REF_CHAR ) {
				// $...
				c++; // skip $
				c = parseAttributeReference(r,action,c,buf,actionToken);
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
			String referencedRuleName = ruleLabel.elementRef.getText();
			Rule referencedRule = grammar.getRule(referencedRuleName);
			return new RuleLabelScope(referencedRule);
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
																 String scopeName,
																 String attribute)
	{
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
			if( RuleLabelScope.predefinedRuleProperties.contains(attribute) ) {
				stName = "ruleLabelPropertyRef_"+attribute;
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
		refST.setAttribute("attr", attribute);
		return refST;
	}

	/** Get $scope.attribute or just $scope if attribute not there.
	 *  Then translate according to scope.
	 */
	protected int parseAttributeReference(Rule r,
										String action,
										int c,
										StringBuffer buf,
										antlr.Token actionToken)
	{
		String attrRef=null;
		String scope = getID(action, c);
		c += scope.length();
		AttributeScope attrScope = resolveScope(r, scope);
		if ( (c+1)<action.length() &&
			 action.charAt(c)=='.' && Character.isLetter(action.charAt(c+1)))
		{
			// $x.y
			int dotIndex = c;
			c++;
			String attribute = getID(action, c);
			if ( attrScope!=null ) {
				// $scope.attribute
				attrRef = translateAttributeReference(r, actionToken, attrScope, scope, attribute);
				c += attribute.length();
			}
			else {
				// $arg.?, $retval.?  Translate before the dot only
				// (could also be $x.y for unknown x outside of a rule)
				attrRef = translateAttributeReference(r, actionToken, scope);
				c = dotIndex;
			}
		}
		else {
			// Isolated $x
			attrRef = translateAttributeReference(r, actionToken, scope);
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
												 antlr.Token actionToken,
												 AttributeScope scope,
												 String scopeName,
												 String attributeName)
	{
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
			else if ( scope instanceof RuleLabelScope) {
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
			getScopedAttributeReferenceTemplate(r,scopeName,attributeName);

		return refST.toString();
	}

	/** Translate $x where x is either a parameter, return value, local
	 *  rule-scope attribute, or token attributeName.
	 */
	protected String translateAttributeReference(Rule r,
												 antlr.Token actionToken,
												 String attributeName)
	{
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
		if ( r.getTokenLabel(attributeName)!=null ) {
			// $tokenLabel
			refST = generator.templates.getInstanceOf("tokenLabelRef");
			refST.setAttribute("label", attributeName);
		}
		else if ( r.getRuleLabel(attributeName)!=null ) {
			ErrorManager.grammarError(ErrorManager.MSG_ISOLATED_RULE_ATTRIBUTE,
									  grammar,
									  actionToken,
									  ref);
			return ref;
		}
		else {
			// $parameter or $returnValue
			AttributeScope scope = r.getAttributeScope(attributeName);
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
			}
			else if ( scope.isDynamicRuleScope ) {
				refST = generator.templates.getInstanceOf("ruleScopeAttributeRef");
				refST.setAttribute("scope", r.name);
			}
			refST.setAttribute("attr", scope.getAttribute(attributeName));
		}
		return refST.toString();
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
