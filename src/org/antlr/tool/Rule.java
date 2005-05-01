package org.antlr.tool;

import org.antlr.analysis.NFAState;

import java.util.*;

/** Combine the info associated with a rule */
public class Rule {
	public static final Set predefinedRuleProperties = new HashSet();
	static {
		predefinedRuleProperties.add("start");
		predefinedRuleProperties.add("stop");
		predefinedRuleProperties.add("tree");
	}
	public String name;
	public int index;
	public String modifier;
	public Map options;
	public NFAState startState;
	public NFAState stopState;
	public GrammarAST tree;
	public GrammarAST EORNode;
	public GrammarAST lexerAction;
	/** The return values of a rule and predefined rule attributes */
	public AttributeScope returnScope;
	public AttributeScope parameterScope;
	/** the attributes defined with "scope {...}" inside a rule */
	public AttributeScope ruleScope;
	/** A list of scope names (String) used by this rule */
	public List useScopes;
	/** A list of all LabelElementPair attached to tokens like id=ID */
	public LinkedHashMap tokenLabels;
	/** A list of all LabelElementPair attached to rule references like f=field */
	public LinkedHashMap ruleLabels;

	public Grammar.LabelElementPair getLabel(String name) {
		Grammar.LabelElementPair pair = null;
		if ( tokenLabels!=null ) {
			pair = (Grammar.LabelElementPair)tokenLabels.get(name);
		}
		if ( pair==null && ruleLabels!=null ) {
			pair = (Grammar.LabelElementPair)ruleLabels.get(name);
		}
		return pair;
	}

	public Grammar.LabelElementPair getTokenLabel(String name) {
		Grammar.LabelElementPair pair = null;
		if ( tokenLabels!=null ) {
			return (Grammar.LabelElementPair)tokenLabels.get(name);
		}
		return pair;
	}

	public Grammar.LabelElementPair getRuleLabel(String name) {
		Grammar.LabelElementPair pair = null;
		if ( ruleLabels!=null ) {
			return (Grammar.LabelElementPair)ruleLabels.get(name);
		}
		return pair;
	}

	/** Return the scope containing name */
	public AttributeScope getAttributeScope(String name) {
		AttributeScope scope = null;
		if ( returnScope!=null && returnScope.attributes.get(name)!=null ) {
			scope = returnScope;
		}
		if ( parameterScope!=null && parameterScope.attributes.get(name)!=null ) {
			scope = parameterScope;
		}
		return scope;
	}

	/*
	public AttributeScope getScopeContainingAttribute(String scopeName,
													  String attrName)
	{
		if ( r==null ) { // must be action not in a rule
			if ( scopeName==null ) {
				System.erprintln("no scope: "+scopeName);
				return null;
			}
			AttributeScope scope = getScope(scopeName);
			return scope;
		}
		if ( scopeName!=null && !scopeName.equals(name) ) {
			AttributeScope scope = getScope(scopeName);
			// TODO: what to do if no attrName in scope?
			if ( scope.attributes.get(attrName)==null ) {
				System.erprintln("no "+attrName+" in scope "+scopeName);
			}
			return scope;
		}
		if ( returnScope!=null && returnScope.attributes.get(attrName)!=null ) {
			return returnScope;
		}
		if ( parameterScope!=null && parameterScope.attributes.get(attrName)!=null ) {
			return parameterScope;
		}
		if ( ruleScope!=null && ruleScope.attributes.get(attrName)!=null ) {
			return ruleScope;
		}
		System.erprintln("no scope for "+attrName+" (tried scope "+scopeName+")");
		return null;
	}
	*/

	public String toString() { // used for testing
		if ( modifier!=null ) {
			return modifier+" "+name;
		}
		return name;
	}
}
