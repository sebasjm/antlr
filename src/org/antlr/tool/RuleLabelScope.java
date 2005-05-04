package org.antlr.tool;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;

import java.util.Set;
import java.util.HashSet;

public class RuleLabelScope extends AttributeScope {
	public static final Set predefinedRuleProperties = new HashSet();
	static {
		predefinedRuleProperties.add("text");
		predefinedRuleProperties.add("start");
		predefinedRuleProperties.add("stop");
		predefinedRuleProperties.add("tree");
	}

	public Rule referencedRule;

	public RuleLabelScope(Rule referencedRule) {
		super("ref_"+referencedRule.name);
		this.referencedRule = referencedRule;
	}

	/** If you label a rule reference, you can access that rule's
	 *  return values as well as any predefined attributes.
	 */
	public Attribute getAttribute(String name) {
		if ( predefinedRuleProperties.contains(name) ) {
			return new Attribute(name, null);
		}
		if ( referencedRule.returnScope!=null ) {
			return referencedRule.returnScope.getAttribute(name);
		}
		return null;
	}

	public String getAttributeReferenceTemplateName(String scope, String attribute) {
		return "ruleLabelRef";
	}
}
