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

/** This class embodies the translation of actions written in the target
 *  language but containing special references that must be translated
 *  for reasons of terseness and isolation from underlying implementation.
 *
 *  There are three forms of interest:
 *
 * 	$y		return value, parameter, rule or token label, predefined
 * 			rule property, or token or rule reference within the enclosing
 * 			rule's outermost alt. y must be a "local" reference; i.e., it
 * 			must be referring to something defined within the enclosing rule.
 *
 * 			r[int i] returns [int j]
 * 				:	{$i, $j, $start, $stop, $template, $tree}
 * 					(ids+=ID)+ {$ids.size()} // .size is not used by antlr here
 * 				;
 *
 * 			Can also mean y is a rule's dynamic scope or a global shared scope.
 * 			Isolated $rulename is not allowed unless it has a dynamic scope *and*
 * 			there is no reference to rulename in the enclosing alternative,
 * 			which would be ambiguous.  See TestAttributes.testAmbiguousRuleRef()
 *
 * 	$x.y	if x is enclosing rule, y is a return value, parameter, or
 * 			predefined property.  If x is a token label, y is predefined
 * 			property of a token.  If x is a rule label, y is a return value
 * 			of the invoked rule or a predefined rule property such as "stop".
 * 			If x is a token referenced in that alt, it behaves like a token
 * 			label.  If x is a rule reference in that alt, it behaves like
 * 			a rule label.
 *
 * 			r[int i] returns [int j]
 * 				:	{$r.i, $r.j, $r.start, $r.stop, $r.template, $r.tree}
 * 					ID {$ID.text} s {$s.start; $s.k}
 * 				;
 * 			s returns [int k] : ... ;
 *
 * 	$x::y	the only way to access the attributes within a dynamic scope
 * 			regardless of whether or not you are in the defining rule.
 *
 * 			scope Symbols { List names; }
 * 			r
 * 			scope {int i;}
 * 			scope Symbols;
 * 				:	{$r::i=3;} s {$Symbols::names;}
 * 				;
 * 			s	:	{$r::i; $Symbols::names;}
 * 				;
 *
 *  This is the new syntax as of 11/23/2005 and should simplify things a bit
 *  as all dynamic scope stuff is accessed with a different operator.  Actually
 *  all it does is make the set of scopes to search for $y a bit smaller.  Oh,
 *  and I should be able to be more specific with error messages.
 */
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

	/** Given an action string with $y, $x.y and $x::y references, convert it
	 *  to a StringTemplate (that will be inserted into the output StringTemplate)
	 *  Replace $ references to template references.  Targets can then say
	 *  how to translate these references with a template rather than code.
	 *
	 *  Jump from $ to $ in the action, building up a text buffer
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

	/** Given x of $x::y or just $x, figure out what scope x is if any (x might
	 *  be a parameter, say, not a scope name).  The valid scopes are rule names
	 *  and global shared dynamic scopes.
	 */
	protected AttributeScope resolveDynamicScope(String scopeName) {
		if ( grammar.getGlobalScope(scopeName)!=null ) {
			return grammar.getGlobalScope(scopeName);
		}
		Rule scopeRule = grammar.getRule(scopeName);
		if ( scopeRule!=null ) {
			return scopeRule.ruleScope;
		}
		return null; // not a valid dynamic scope
	}

	/** Given x of $x.y or $x, figure out what scope x is if any (x might
	 *  be a parameter, say, not a scope name).  The valid scopes are
	 *  $rulelabel, $tokenlabel, $currentrulename.
	 *  List labels like ids+=ID have no scope as they are a list of scopes
	 *  in a sense.
	 */
	protected AttributeScope resolveScope(Rule r, String scopeName) {
		if ( r==null ) { // action outside of rule has no scope
			return null;
		}
		if ( r.name.equals(scopeName) ) {
			// $enclosingRulename
			return r.ruleScope;
		}
		if ( r.getTokenLabel(scopeName)!=null ) {
			// $tokenLabel
			return AttributeScope.tokenScope;
		}
		if ( r.getRuleLabel(scopeName)!=null ) {
			// $ruleLabel
			Grammar.LabelElementPair ruleLabel = r.getRuleLabel(scopeName);
			Rule refdRule = grammar.getRule(ruleLabel.referencedRuleName);
			return new RuleLabelScope(refdRule);
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
		else if ( r.getTokenLabel(scopeName)!=null ||
			      r.getTokenListLabel(scopeName)!=null )
		{
			// $tokenLabel.attr
			stName = "tokenLabelPropertyRef_"+attribute;
		}
		else if ( r.getRuleLabel(scopeName)!=null ||
			      r.getRuleListLabel(scopeName)!=null )
		{
			// $ruleLabel.attr
			rlScope = (RuleLabelScope)scope;
			if( RuleLabelScope.predefinedRulePropertiesScope.getAttribute(attribute)!=null ) {
				stName = "ruleLabelPropertyRef_"+attribute;
				grammar.referenceRuleLabelPredefinedAttribute(rlScope.referencedRule.name);
			}
			else {
				stName = "ruleLabelRef";
			}
		}
		else if ( RuleLabelScope.predefinedRulePropertiesScope.getAttribute(attribute)!=null ) {

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

	/** We found a $-reference.  Get the y of $x.y if it exists, then
	 *  translate according to scope x.  Peel off $x::y references to
	 *  a separate method.
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
		List ruleRefsInAlt = null;
		List tokenRefsInAlt = null;
		if ( r!=null ) {
			ruleRefsInAlt = r.getRuleRefsInAlt(scope, actionAST.outerAltNum);
			tokenRefsInAlt = r.getTokenRefsInAlt(scope, actionAST.outerAltNum);
		}
		boolean hasDot =
			(c+1)<action.length() &&
			 action.charAt(c)=='.' && Character.isLetter(action.charAt(c+1));
		boolean hasDoubleColon =
			(c+2)<action.length() &&
			 action.charAt(c)==':' && action.charAt(c+1)==':' &&
			 Character.isLetter(action.charAt(c+2));
		AttributeScope dynamicScope = resolveDynamicScope(scope);
		if ( dynamicScope!=null ) {
			return parseDynamicAttribute(hasDoubleColon,
										 c,
										 action,
										 r,
										 actionAST,
										 dynamicScope,
										 scope,
										 buf,
										 ruleRefsInAlt);
		}

		AttributeScope attrScope = resolveScope(r, scope);
		if ( hasDot ) {
			// $x.y
			int dotIndex = c;
			c++;
			String attributeName = getID(action, c);
			//System.out.println("translate: "+scope+"."+attributeName);
			if ( attrScope==null && r!=null && r.name.equals(scope) ) {
				// Reference to $r from within rule r: $r.arg, $r.retval, ...
				// Just strip off $r as if it didn't exist and pretend it's $arg
				attrRef = translateIsolatedAttributeReference(r, actionAST, attributeName);
				c += attributeName.length();
			}
			else if ( attrScope!=null ) {
				// $scope.attributeName (label, ret val, param)
				attrRef = translateAttributeReference(r, actionAST, attrScope, scope, attributeName);
				c += attributeName.length();
			}
			else if ( ruleRefsInAlt!=null ) {
				// $rulereference.attributeName
				// decl : type ID {$type.y} ;
				attrRef =
					translateRuleReference(r, actionAST, scope, attributeName);
				c += attributeName.length();
			}
			else if ( tokenRefsInAlt!=null ) {
				// $token.attributeName
				// decl : type ID {$ID.text} ;
				attrRef =
					translateTokenReference(r, actionAST, scope, attributeName);
				c += attributeName.length();
			}
			else {
				// $arg.?, $retval.? $listlabel.? Translate before the dot only
				// (could also be $x.y for unknown x outside of a rule)
				attrRef = translateIsolatedAttributeReference(r, actionAST, scope);
				c = dotIndex;
			}
		}
		else {
			// Isolated $x
			//System.out.println("translate: "+scope);
			if ( tokenRefsInAlt!=null ) {
				// $token
				attrRef = translateTokenReference(r, actionAST, scope, null);
			}
			else if ( r!=null && r.getTokenListLabel(scope)!=null ) {
				// $tokenList
                attrRef = translateElementListReference(scope);
			}
			else if ( r!=null && r.getRuleListLabel(scope)!=null ) {
				// $ruleList
				attrRef = translateElementListReference(scope);
			}
			else {
				attrRef = translateIsolatedAttributeReference(r, actionAST, scope);
			}
		}
		buf.append(attrRef);
		return c;
	}

	/** Handle $x::y and plain $x where x is dynamic scope.  Warn if
	 *  ambig ref where x is also a rule ref in the enclosing alt.
	 */
	protected int parseDynamicAttribute(boolean hasDoubleColon, int c, String action, Rule r, GrammarAST actionAST, AttributeScope dynamicScope, String scope, StringBuffer buf, List ruleRefsInAlt) {
		String attrRef;
		if ( hasDoubleColon ) {
			// $x::y
			c+=2; // skip over ::
			String attributeName = getID(action, c);
			c += attributeName.length();
			attrRef =
				translateDynamicAttributeReference(r, actionAST, dynamicScope, scope, attributeName);
			buf.append(attrRef);
			return c;
		}
		else if ( ruleRefsInAlt==null ) {
			// isolated $x scope ref (means access stack of scopes itself)
			// must not be a rule reference to x in the enclosing alt
			StringTemplate refST =
				generator.templates.getInstanceOf("isolatedDynamicScopeRef");
			refST.setAttribute("scope", scope);
			buf.append(refST.toString());
			return c;
		}
		else {
			// ambiguous reference to $rule since rule is also referenced
			// in the enclosing alt.
			// y of $x::y does not exist (not an attribute in scope x)
			ErrorManager.grammarError(ErrorManager.MSG_AMBIGUOUS_RULE_SCOPE,
									  grammar,
									  actionAST.getToken(),
									  scope);
			return c;
		}
	}

	/** Translate $x::y where x is a rule name or a global shared dynamic
	 *  scope name.
	 */
	protected String translateDynamicAttributeReference(Rule r,
														GrammarAST actionAST,
														AttributeScope scope,
														String scopeName,
														String attributeName)
	{
		antlr.Token actionToken = actionAST.getToken();
		String ref = ATTRIBUTE_REF_CHAR+scopeName+"::"+attributeName;
		//System.out.println("translate "+ref);
		if ( scope==null ) {
			// x of $x::y does not exist (not a rule nor global scope name)
			ErrorManager.grammarError(ErrorManager.MSG_UNKNOWN_DYNAMIC_SCOPE,
									  grammar,
									  actionToken,
									  scopeName,
									  attributeName);
			return ref;
		}
		Attribute attribute = scope.getAttribute(attributeName);
		if ( attribute==null ) {
			// y of $x::y does not exist (not an attribute in scope x)
			ErrorManager.grammarError(ErrorManager.MSG_UNKNOWN_DYNAMIC_SCOPE_ATTRIBUTE,
									  grammar,
									  actionToken,
									  scopeName,
									  attributeName);
			return ref;
		}
		// y is valid attribute in scope x at this point.
		StringTemplate refST =
			generator.templates.getInstanceOf("ruleScopeAttributeRef");
		refST.setAttribute("scope", scopeName);
		refST.setAttribute("attr", attribute);
		return refST.toString();
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
		//System.out.println("translate "+ref);
		Attribute attribute = scope.getAttribute(attributeName);
		if ( attribute!=null ) {
			// y exists in scope x; all is well
			StringTemplate refST =
				getScopedAttributeReferenceTemplate(r,scope,scopeName,attributeName);
			return refST.toString();
		}

		// $tokenlabel.unknown
		if ( r.getTokenLabel(scopeName)!=null ) {
			// just return what you would for $x
			StringTemplate refST =
				generator.templates.getInstanceOf("tokenLabelRef");
			refST.setAttribute("label", scopeName);
			// we must ignore the unknown; put it in the translated action
			return refST.toString()+"."+attributeName;
		}

		// Spend some effort to generate good messages
		int msgID = 0;
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

	/** Translate isolated $x where x is either a parameter, return value, local
	 *  rule-scope attribute, label, token name, or rule name.
	 *
	 *  $tokenLabel is handled in the $x.y translator as $tokenLabel is seen
	 *  as a valid scope.
	 */
	protected String translateIsolatedAttributeReference(Rule r,
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
		AttributeScope attrScope = r.getAttributeScope(attributeName);
		Grammar.LabelElementPair pair = r.getLabel(attributeName);
		if ( pair!=null ) {
			// $label of some type
			switch ( pair.type ) {
				case Grammar.TOKEN_LABEL :
					// $tokenLabel
					refST = generator.templates.getInstanceOf("tokenLabelRef");
					refST.setAttribute("label", attributeName);
					break;
				case Grammar.RULE_LIST_LABEL :
				case Grammar.TOKEN_LIST_LABEL :
					// $listLabel
					refST = generator.templates.getInstanceOf("listLabelRef");
					refST.setAttribute("label", attributeName);
					break;
				case Grammar.RULE_LABEL :
					// $ruleLabel
					ErrorManager.grammarError(ErrorManager.MSG_ISOLATED_RULE_SCOPE,
											  grammar,
											  actionToken,
											  ref);
					return ref;
			}
		}
		else {
			// ok, it's not a label; have to think a little harder then.
			// Might be $parameter or $returnValue
			List ruleRefs = r.getRuleRefsInAlt(attributeName, actionAST.outerAltNum);
			if ( ruleRefs!=null ) {
				// isolated reference to a rule referenced in this alt
				ErrorManager.grammarError(ErrorManager.MSG_ISOLATED_RULE_SCOPE,
										  grammar,
										  actionToken,
										  ref);
				return ref;
			}
			if ( r.name.equals(attributeName) ) {
				// isolated ref to this rule that does not have a dynamic attrScope
				ErrorManager.grammarError(ErrorManager.MSG_ISOLATED_RULE_SCOPE,
										  grammar,
										  actionToken,
										  ref);
				return ref;
			}
			if ( attrScope==null ) {
				ErrorManager.grammarError(ErrorManager.MSG_UNKNOWN_SIMPLE_ATTRIBUTE,
										  grammar,
										  actionToken,
										  ref);
				return ref;
			}
			if ( attrScope.isParameterScope ) {
				refST = generator.templates.getInstanceOf("parameterAttributeRef");
			}
			else if ( attrScope.isReturnScope ) {
				refST = generator.templates.getInstanceOf("returnAttributeRef");
				refST.setAttribute("ruleDescriptor", r);
			}
			else if ( attrScope.isDynamicRuleScope ) {
				ErrorManager.grammarError(ErrorManager.MSG_ISOLATED_RULE_ATTRIBUTE,
										  grammar,
										  actionToken,
										  attributeName);
				return ref;
			}
			else {
				// must be predefined attribute like $template or $tree
				refST = generator.templates.getInstanceOf("rulePropertyRef");
			}
			refST.setAttribute("attr", attrScope.getAttribute(attributeName));
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
		String ref = ATTRIBUTE_REF_CHAR+tokenRefName+"."+attributeName;
		//System.out.println("translate token ref "+ref);
		List tokenRefs = r.getTokenRefsInAlt(tokenRefName, actionAST.outerAltNum);
		GrammarAST uniqueRefAST = (GrammarAST)tokenRefs.get(0);
		if ( uniqueRefAST.code==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_FORWARD_ELEMENT_REF,
									  grammar,
									  actionAST.getToken(),
									  ref);
			return ref;
		}
		String labelName = null;
		String existingLabelName =
			(String)uniqueRefAST.code.getAttribute("label");
		// reuse any label or list label if it exists
		if ( existingLabelName!=null ) {
			labelName = existingLabelName;
		}
		else {
			// else create new label
			labelName = generator.createUniqueLabel(tokenRefName);
			CommonToken label = new CommonToken(ANTLRParser.ID, labelName);
			grammar.defineTokenRefLabel(r.name, label, uniqueRefAST);
			uniqueRefAST.code.setAttribute("label", labelName);
		}
		AttributeScope scope = AttributeScope.tokenScope;
		if ( attributeName==null ) {
			return translateIsolatedAttributeReference(r, actionAST, labelName);
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
		//System.out.println("translate rule ref "+ref);
		List ruleRefs = r.getRuleRefsInAlt(ruleRefName, actionAST.outerAltNum);
		GrammarAST uniqueRefAST = (GrammarAST)ruleRefs.get(0);
		if ( uniqueRefAST.code==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_FORWARD_ELEMENT_REF,
									  grammar,
									  actionAST.getToken(),
									  ref);
			return ref;
		}
		String labelName = null;
		String existingLabelName =
			(String)uniqueRefAST.code.getAttribute("label");
		if ( existingLabelName!=null ) {
			labelName = existingLabelName; // ok to reuse even if list label
		}
		if ( labelName==null ) {
			// create new label
			labelName = generator.createUniqueLabel(ruleRefName);
			CommonToken label = new CommonToken(ANTLRParser.ID, labelName);
			grammar.defineRuleRefLabel(r.name, label, uniqueRefAST);
			uniqueRefAST.code.setAttribute("label", labelName);
		}
		Rule referencedRule = grammar.getRule(ruleRefName);
		AttributeScope scope = new RuleLabelScope(referencedRule);
		return translateAttributeReference(r, actionAST, scope, labelName, attributeName);
	}

	protected String translateElementListReference(String labelName) {
		StringTemplate refST =
			generator.templates.getInstanceOf("listLabelRef");
		refST.setAttribute("label", labelName);
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
