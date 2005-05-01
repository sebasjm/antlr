package org.antlr.codegen;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.Grammar;
import org.antlr.tool.Rule;
import org.antlr.tool.ErrorManager;
import org.antlr.tool.AttributeScope;
import org.antlr.runtime.Token;

public class ActionTranslator {
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
			if ( action.charAt(c)!=CodeGenerator.LOCAL_AND_LABEL_REF_CHAR &&
				action.charAt(c)!=CodeGenerator.DYNAMIC_ATTRIBUTE_REF_CHAR )
			{
				buf.append(action.charAt(c));
				c++;
				continue;
			}
			c++; // skip $ or @
			// cannot have $ or @ expressions outside of a rule
			if ( r==null ) {
				ErrorManager.grammarError(ErrorManager.MSG_ATTRIBUTE_REF_NOT_IN_RULE,
										  grammar,
										  actionToken);
				continue;
			}
			if ( action.charAt(c)==CodeGenerator.DYNAMIC_ATTRIBUTE_REF_CHAR ) {
				// @...
				c = parseDynamicAttributeRef(r,action,c,buf,actionToken);
			}
            else {
				// $...
				c = parseAttributeRef(r,action,c,buf,actionToken);
			}
		}
		System.out.println("translated action="+buf.toString());
		return buf.toString();
	}

	/** For @x.y, get both x and y */
	protected int parseDynamicAttributeRef(Rule r,
										   String action,
										   int c,
										   StringBuffer buf,
										   antlr.Token actionToken)
	{
		String scope = getID(action, c);
		if ( scope==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_MISSING_ID_IN_ATTRIBUTE_REF,
									  grammar,
									  actionToken,
									  ""+CodeGenerator.DYNAMIC_ATTRIBUTE_REF_CHAR);
			return c;
		}
		int dotIndex = c + scope.length();
		if ( dotIndex < action.length() && action.charAt(dotIndex)!='.' ) {
			ErrorManager.grammarError(ErrorManager.MSG_MISSING_DOT_IN_ATTRIBUTE_REF,
									  grammar,
									  actionToken,
									  scope);
			c = dotIndex;
			return c;
		}
		String attribute = getID(action, dotIndex+1);
		if ( attribute==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_MISSING_ID_IN_ATTRIBUTE_REF,
									  grammar,
									  actionToken,
									  CodeGenerator.DYNAMIC_ATTRIBUTE_REF_CHAR+scope);
			c = dotIndex+1;
			return c;
		}
		c = dotIndex+attribute.length()+1; // move on
		String attrRef = translateAttributeReference(r,actionToken,scope,attribute);
		buf.append(attrRef);
		return c;
	}

	/** For $x or $x.y, get x and/or y
	 *  get y only if x is rule or token label
	 */
	protected int parseAttributeRef(Rule r,
									String action,
									int c,
									StringBuffer buf,
									antlr.Token actionToken)
	{
		String id = getID(action, c);
		c += id.length();
		String attrRef=null;
		if ( r.getLabel(id)!=null ) {
			String label = id;
			if ( c<action.length() && action.charAt(c)=='.' ) {
				int dotIndex = c;
				c++;
				String attribute = getID(action, c);
				c += attribute.length();
				if ( r.getTokenLabel(label)!=null &&
					!Token.predefinedTokenProperties.contains(attribute) )
				{
					attrRef = translateAttributeReference(r,actionToken,label);
					c = dotIndex; // backup...not a token property
				}
				else {
					// $ruleLabel.attribute
					attrRef = translateAttributeReference(r,actionToken,label,attribute);
				}
			}
			else {
				// $tokenLabel
				attrRef = translateAttributeReference(r,actionToken,label);
			}
		}
		else {
			// $x
			attrRef = translateAttributeReference(r,actionToken,id);
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
												 String label,
												 String attribute)
	{
		String ref = CodeGenerator.LOCAL_AND_LABEL_REF_CHAR+label+"."+attribute;
		if ( r.getLabel(label)==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_UNKNOWN_ATTRIBUTE_SCOPE,
									  grammar,
									  actionToken,
									  ref);
			return ref;
		}
		StringTemplate refST = null;
		if ( r.getTokenLabel(label)!=null ) {
			// $tokenRef.property
			refST = generator.templates.getInstanceOf("tokenLabelPropertyRef_"+attribute);
			refST.setAttribute("label",label);
		}
		else {
			if ( Rule.predefinedRuleProperties.contains(attribute) ) {
				// $ruleRef.property
				refST = generator.templates.getInstanceOf("ruleLabelPropertyRef_"+attribute);
				refST.setAttribute("label",label);
			}
			else {
				// $ruleRef.y
				Grammar.LabelElementPair ruleLabel = r.getRuleLabel(label);
				String referencedRuleName = ruleLabel.elementRef.getText();
				Rule referencedRule = grammar.getRule(referencedRuleName);
				AttributeScope scope = referencedRule.getAttributeScope(attribute);
				if ( scope==null ) {
					ErrorManager.grammarError(ErrorManager.MSG_UNKNOWN_RULE_ATTRIBUTE,
											  grammar,
											  actionToken,
											  label,
											  attribute);
					return ref;
				}
				if ( scope==referencedRule.parameterScope ) {
					ErrorManager.grammarError(ErrorManager.MSG_INVALID_RULE_PARAMETER_REF,
											  grammar,
											  actionToken,
											  label,
											  attribute);
					return ref;
				}
				refST = generator.templates.getInstanceOf("ruleLabelRef");
				refST.setAttribute("label", label);
				refST.setAttribute("prop", attribute);
			}
		}
		return refST.toString();
	}

	/** Translate $x where x is either a parameter, return value, or token
	 *  name.
	 */
	protected String translateAttributeReference(Rule r,
												 antlr.Token actionToken,
												 String name)
	{
		String ref = CodeGenerator.LOCAL_AND_LABEL_REF_CHAR+name;
		StringTemplate refST = null;
		if ( r.getTokenLabel(name)!=null ) {
			// $tokenLabel
			refST = generator.templates.getInstanceOf("tokenLabelRef");
			refST.setAttribute("label", name);
		}
		else if ( r.getRuleLabel(name)!=null ) {
			ErrorManager.grammarError(ErrorManager.MSG_ISOLATED_RULE_ATTRIBUTE,
									  grammar,
									  actionToken,
									  ref);
			return ref;
		}
		else {
			// $parameter or $returnValue
			AttributeScope scope = r.getAttributeScope(name);
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
			refST.setAttribute("attr", scope.getAttribute(name));
		}
		return refST.toString();
	}


	/** Translate @x.y where x is either a global scope definition or
	 *  a scope defined within a rule.  y must be an attribute within
	 *  that scope.
	 */
	protected String translateDynamicAttributeReference(Rule r,
														antlr.Token actionToken,
														String scope,
														String attribute)
	{
		String ref = CodeGenerator.DYNAMIC_ATTRIBUTE_REF_CHAR+scope+"."+attribute;
		StringTemplate refST = null;
		AttributeScope attrScope = grammar.getScope(scope);
		if ( attrScope==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_UNKNOWN_ATTRIBUTE_SCOPE,
									  grammar,
									  actionToken,
									  CodeGenerator.DYNAMIC_ATTRIBUTE_REF_CHAR+scope);
			return ref;
		}
		AttributeScope.Attribute attr =
			(AttributeScope.Attribute)attrScope.getAttribute(attribute);
		if ( attr==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_UNKNOWN_DYNAMIC_ATTRIBUTE,
									  grammar,
									  actionToken,
									  ref);
			return ref;
		}
		if ( attrScope.isGlobal ) {
			// @globalscope.attribute
			refST = generator.templates.getInstanceOf("globalAttributeRef");
		}
		else {
			// @rulescope.attribute (this is not a $ruleRefLable.prop ref)
			refST = generator.templates.getInstanceOf("ruleScopeAttributeRef");
		}
		refST.setAttribute("scope",attrScope);
		refST.setAttribute("attr",attr);
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
