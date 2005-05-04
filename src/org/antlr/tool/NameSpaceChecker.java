package org.antlr.tool;

import org.antlr.analysis.Label;

import java.util.Iterator;
import java.util.List;

public class NameSpaceChecker {
	protected Grammar grammar;

	public NameSpaceChecker(Grammar grammar) {
		this.grammar = grammar;
	}

	public void checkConflicts() {
		for (int i = 0; i < grammar.ruleIndexToRuleList.size(); i++) {
			String ruleName = (String) grammar.ruleIndexToRuleList.elementAt(i);
			if ( ruleName==null ) {
				continue;
			}
			Rule r = grammar.getRule(ruleName);
			// walk all token labels for Rule r
			if ( r.tokenLabels!=null ) {
				Iterator it = r.tokenLabels.values().iterator();
				while ( it.hasNext() ) {
					Grammar.LabelElementPair pair = (Grammar.LabelElementPair) it.next();
					checkForLabelConflict(r, pair.label);
				}
			}
			// walk all rule labels for Rule r
			if ( r.ruleLabels!=null ) {
				Iterator it = r.ruleLabels.values().iterator();
				while ( it.hasNext() ) {
					Grammar.LabelElementPair pair = (Grammar.LabelElementPair) it.next();
					checkForLabelConflict(r, pair.label);
				}
			}
			// walk rule scope attributes for Rule r
			if ( r.ruleScope!=null ) {
				List attributes = r.ruleScope.getAttributes();
				for (int j = 0; j < attributes.size(); j++) {
					Attribute attribute = (Attribute) attributes.get(j);
					checkForRuleScopeAttributeConflict(r, attribute);
				}
			}
			checkForRuleDefinitionProblems(r);
		}
		// check all global scopes against tokens
		Iterator it = grammar.getGlobalScopes().values().iterator();
		while (it.hasNext()) {
			AttributeScope scope = (AttributeScope) it.next();
			checkForGlobalScopeTokenConflict(scope);
		}
	}

	protected void checkForRuleDefinitionProblems(Rule r) {
		String ruleName = r.name;
		antlr.Token ruleToken = r.tree.getToken();
		int msgID = 0;
		if ( grammar.type==grammar.PARSER && Character.isUpperCase(ruleName.charAt(0)) ) {
			msgID = ErrorManager.MSG_LEXER_RULES_NOT_ALLOWED;
        }
        else if ( grammar.type==grammar.LEXER && Character.isLowerCase(ruleName.charAt(0)) ) {
			msgID = ErrorManager.MSG_PARSER_RULES_NOT_ALLOWED;
        }
		else if ( grammar.getGlobalScope(ruleName)!=null ) {
			msgID = ErrorManager.MSG_SYMBOL_CONFLICTS_WITH_GLOBAL_SCOPE;
		}
		if ( msgID!=0 ) {
			ErrorManager.grammarError(msgID, grammar, ruleToken, ruleName);
		}
	}

	protected void checkForGlobalScopeTokenConflict(AttributeScope scope) {
		if ( grammar.getTokenType(scope.name)!=Label.INVALID ) {
			ErrorManager.grammarError(ErrorManager.MSG_SYMBOL_CONFLICTS_WITH_GLOBAL_SCOPE,
									  grammar, null, scope.name);
		}
	}

	/** Check for collision of a rule-scope dynamic attribute with:
	 *  arg, return value, rule name itself.  Labels are checked elsewhere.
	 */
	public void checkForRuleScopeAttributeConflict(Rule r, Attribute attribute) {
		int msgID = 0;
		Object arg2 = null;
		String attrName = attribute.name;
		if ( r.name.equals(attrName) ) {
			msgID = ErrorManager.MSG_ATTRIBUTE_CONFLICTS_WITH_RULE;
			arg2 = r.name;
		}
		else if ( (r.returnScope!=null&&r.returnScope.getAttribute(attrName)!=null) ||
				  (r.parameterScope!=null&&r.parameterScope.getAttribute(attrName)!=null) )
		{
			msgID = ErrorManager.MSG_ATTRIBUTE_CONFLICTS_WITH_RULE_ARG_RETVAL;
			arg2 = r.name;
		}
		if ( msgID!=0 ) {
			ErrorManager.grammarError(msgID,grammar,r.tree.getToken(),attrName,arg2);
		}
	}

	/** Make sure a label doesn't conflict with another symbol.
	 *  Labels must not conflict with: rules, tokens, scope names,
	 *  return values, parameters, and rule-scope dynamic attributes
	 *  defined in surrounding rule.
	 */
	protected void checkForLabelConflict(Rule r, antlr.Token label) {
		int msgID = 0;
		Object arg2 = null;
		if ( grammar.getGlobalScope(label.getText())!=null ) {
			msgID = ErrorManager.MSG_SYMBOL_CONFLICTS_WITH_GLOBAL_SCOPE;
		}
		else if ( grammar.getRule(label.getText())!=null ) {
			msgID = ErrorManager.MSG_LABEL_CONFLICTS_WITH_RULE;
		}
		else if ( grammar.getTokenType(label.getText())!=Label.INVALID ) {
			msgID = ErrorManager.MSG_LABEL_CONFLICTS_WITH_TOKEN;
		}
		else if ( r.ruleScope!=null && r.ruleScope.getAttribute(label.getText())!=null ) {
			msgID = ErrorManager.MSG_LABEL_CONFLICTS_WITH_RULE_SCOPE_ATTRIBUTE;
			arg2 = r.name;
		}
		else if ( (r.returnScope!=null&&r.returnScope.getAttribute(label.getText())!=null) ||
				  (r.parameterScope!=null&&r.parameterScope.getAttribute(label.getText())!=null) )
		{
			msgID = ErrorManager.MSG_LABEL_CONFLICTS_WITH_RULE_ARG_RETVAL;
			arg2 = r.name;
		}
		if ( msgID!=0 ) {
			ErrorManager.grammarError(msgID,grammar,label,label.getText(),arg2);
		}
	}
}
