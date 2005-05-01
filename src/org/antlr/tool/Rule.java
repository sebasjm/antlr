package org.antlr.tool;

import org.antlr.analysis.NFAState;
import org.antlr.stringtemplate.StringTemplate;

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

	/** Do not generate start, stop etc... in a return value struct unless
	 *  somebody references $r.start somewhere in an action.
	 */
	public boolean needPredefinedRuleAttributes = false;

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

	/** If a rule has no user-defined parameters and nobody references
	 *  it's start/stop (predefined attributes), then there is no need to
	 *  define a struct otherwise for now we assume a struct.
	 *  TODO: if only one user-defined type and no one references predefined attrs don't generate struct
	 */
	public boolean getHasMultipleReturnValues() {
		return needPredefinedRuleAttributes || returnScope.attributes.size()>0;
	}

	public String toString() { // used for testing
		if ( modifier!=null ) {
			return modifier+" "+name;
		}
		return name;
	}
}
