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
package org.antlr.tool;

import org.antlr.analysis.NFAState;
import org.antlr.stringtemplate.StringTemplate;

import java.util.*;

/** Combine the info associated with a rule */
public class Rule {
	public String name;
	public int index;
	public String modifier;
	public Map options;
	public NFAState startState;
	public NFAState stopState;

	/** The AST representing the whole rule */
	public GrammarAST tree;

	/** For convenience, track the argument def AST action node if any */
	public GrammarAST argActionAST;

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

	/** A list of all LabelElementPair like ids+=ID */
	public LinkedHashMap listLabels;

	/** All labels go in here (plus being split per the above lists) to
	 *  catch dup label and label type mismatches.
	 */
	protected Map labelNameSpace = new HashMap();

	public int numberOfAlts;

	/** Each alt has a Map<tokenRefName,List<tokenRefAST>>; range 1..numberOfAlts.
	 *  So, if there are 3 ID refs in a rule's alt number 2, you'll have
	 *  altToTokenRef[2].get("ID").size()==3.
	 */
	protected Map[] altToTokenRefMap;

	/** Each alt has a Map<ruleRefName,List<ruleRefAST>>; range 1..numberOfAlts
	 *  So, if there are 3 expr refs in a rule's alt number 2, you'll have
	 *  altToRuleRef[2].get("expr").size()==3.
	 */
	protected Map[] altToRuleRefMap;

	/** Do not generate start, stop etc... in a return value struct unless
	 *  somebody references $r.start somewhere in an action.
	 */
	public boolean needPredefinedRuleAttributes = false;

	public Rule(String ruleName, int ruleIndex, int numberOfAlts) {
		this.name = ruleName;
		this.index = ruleIndex;
		this.numberOfAlts = numberOfAlts;
		altToTokenRefMap = new Map[numberOfAlts+1];
		altToRuleRefMap = new Map[numberOfAlts+1];
		for (int alt=1; alt<=numberOfAlts; alt++) {
			altToTokenRefMap[alt] = new HashMap();
			altToRuleRefMap[alt] = new HashMap();
		}
	}

	public Grammar.LabelElementPair getLabel(String name) {
		return (Grammar.LabelElementPair)labelNameSpace.get(name);
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

	public Grammar.LabelElementPair getListLabel(String name) {
		Grammar.LabelElementPair pair = null;
		if ( listLabels!=null ) {
			return (Grammar.LabelElementPair)listLabels.get(name);
		}
		return pair;
	}

	public List getTokenRefsInAlt(String ref, int altNum) {
		if ( altToTokenRefMap[altNum]!=null ) {
			List tokenRefASTs = (List)altToTokenRefMap[altNum].get(ref);
			return tokenRefASTs;
		}
		return null;
	}

	public List getRuleRefsInAlt(String ref, int altNum) {
		if ( altToRuleRefMap[altNum]!=null ) {
			List ruleRefASTs = (List)altToRuleRefMap[altNum].get(ref);
			return ruleRefASTs;
		}
		return null;
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
		if ( ruleScope!=null && ruleScope.attributes.get(name)!=null ) {
			scope = ruleScope;
		}
		return scope;
	}

	/** If a rule has no user-defined return values and nobody references
	 *  it's start/stop (predefined attributes), then there is no need to
	 *  define a struct; otherwise for now we assume a struct.
	 *  TODO: if only one user-defined type and no one references predefined attrs don't generate struct
	 */
	public boolean getHasMultipleReturnValues() {
		return
			needPredefinedRuleAttributes ||
			(returnScope!=null && returnScope.attributes.size()>0);
	}

	public String toString() { // used for testing
		if ( modifier!=null ) {
			return modifier+" "+name;
		}
		return name;
	}
}
