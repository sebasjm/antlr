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

import antlr.CommonToken;
import org.antlr.misc.MutableInteger;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.*;

import java.util.List;

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
 * 			To access deeper (than top of stack) scopes, use the notation:
 *
 * 			$x[-1]::y previous (just under top of stack)
 * 			$x[-i]::y top of stack - i where the '-' MUST BE PRESENT;
 * 					  i.e., i cannot simply be negative without the '-' sign!
 * 			$x[i]::y  absolute index i (0..size-1)
 * 			$x[0]::y  is the absolute 0 indexed element (bottom of the stack)
 *
 *  This is the new syntax as of 11/23/2005 and should simplify things a bit
 *  as all dynamic scope stuff is accessed with a different operator.  Actually
 *  all it does is make the set of scopes to search for $y a bit smaller.  Oh,
 *  and I should be able to be more specific with error messages.
 */
public class ActionTranslator {
	public static final char ATTRIBUTE_REF_CHAR = '$';
	public static final char TEMPLATE_REF_CHAR = '%';

	protected CodeGenerator generator;
	protected Grammar grammar;

	/** The tree node containing the action we're going to translate */
	GrammarAST actionAST;

	String ruleName;

	public ActionTranslator(CodeGenerator generator,
							String ruleName,
						    GrammarAST actionAST)
	{
		this.generator = generator;
		this.actionAST = actionAST;
		this.ruleName = ruleName;
		this.grammar = generator.grammar;
	}

	public ActionTranslator(CodeGenerator generator,
						    String ruleName,
							antlr.Token actionToken,
							int outerAltNum)
	{
		this.generator = generator;
		grammar = generator.grammar;
		actionAST = new GrammarAST();
		actionAST.initialize(actionToken);
		actionAST.outerAltNum = outerAltNum;
		this.ruleName = ruleName;
	}

	/*
	public String translate(String ruleName,
						    antlr.Token actionToken,
							int outerAltNum)
	{
		actionAST = new GrammarAST();
		actionAST.initialize(actionToken);
		actionAST.outerAltNum = outerAltNum;
		return translate(ruleName);
	}
*/

	/** Given an action string with $y, $x.y and $x::y references, convert it
	 *  to a StringTemplate (that will be inserted into the output StringTemplate)
	 *  Replace $ references to template references.  Targets can then say
	 *  how to translate these references with a template rather than code.
	 *
	 *  Jump from $ to $ in the action, building up a text buffer
	 *  doing appropriate rewrites to template refs.  Final step, create
	 *  the StringTemplate.
	 *
	 *  Also handles % template refs now. \% is escape as is \$.
	 */
	public String translate() {
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
			/* 11/30/2005: actually they are not converted to templates!
			 * Might want actions to ref templates later...leave code in
			if ( action.charAt(c)=='<' ) {
				buf.append("\\<");
				c++;
				continue;
			}
			*/
			if ( action.charAt(c)=='\\' &&
				 (c+1)<action.length() && action.charAt(c+1)=='$' )
			{
				buf.append("$"); // translate \$ to $; not an attribute
				c+=2;
				continue;
			}
			if ( action.charAt(c)=='\\' &&
				 (c+1)<action.length() && action.charAt(c+1)=='%' )
			{
				buf.append("%");
				c+=2;
				continue;
			}
			if ( action.charAt(c)==ATTRIBUTE_REF_CHAR ) {
				// $...
				c++; // skip $
				c = parseAttributeReference(r,action,c,buf,actionAST);
			}
			else if ( action.charAt(c)==TEMPLATE_REF_CHAR ) {
				// $...
				c++; // skip %
				c = translateTemplateReference(r,action,c,buf,actionAST);
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
	 *  The attributeName is used to distinguish between $r.predefinedprop
	 *  and $r.dynamicprop.  For example, rule r may define a scope but
	 *  $r.text must return the predefined rule scope not the dynamic scope.
	 */
	protected AttributeScope resolveScope(Rule r,
										  String scopeName,
										  String attributeName)
	{
		if ( r==null ) {
			// action outside of rule can only be dynamic scope
			return resolveDynamicScope(scopeName);
		}
		if ( r.name.equals(scopeName) ) {
			// $enclosingRulename
			if ( RuleLabelScope.predefinedRulePropertiesScope.getAttribute(attributeName) !=null ) {
				// $enclosingRulename.predefinedprop
				return RuleLabelScope.predefinedRulePropertiesScope;
			}
			// Assume enclosingRulename's dynamic scope
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
			RuleLabelScope scope = new RuleLabelScope(refdRule,actionAST.token);
			return scope;
		}
		// might be a dynamic scope
		return resolveDynamicScope(scopeName);
	}

	/** What is the name of the template used to generate a reference to
	 *  an attributeName.  This is perhaps un-OO as the various scopes could
	 *  answer what template generates code for that scope, but I like
	 *  having all the template names encapsulated in one spot and in
	 *  the codegen package.
	 */
	protected StringTemplate getScopedAttributeReferenceTemplate(Rule r,
																 AttributeScope scope,
																 String scopeName,
																 String attributeName)
	{
		RuleLabelScope rlScope=null;
		String stName = null;
		//if ( grammar.getGlobalScope(scopeName)!=null ) {
		if ( scope.isDynamicGlobalScope ) {
			// $scopename
			stName = "globalAttributeRef";
		}
		//else if ( grammar.getRule(scopeName)!=null ) {
		else if ( scope.isDynamicRuleScope ) {
			stName = "ruleScopeAttributeRef";
		}
		else if ( scope.isPredefinedRuleScope ) {
		//	else if ( r.name.equals(scopeName) ) {
			// $rulename
			stName = "rulePropertyRef_"+attributeName;
		}
		else if ( r.getTokenLabel(scopeName)!=null ||
			      r.getTokenListLabel(scopeName)!=null )
		{
			// $tokenLabel.attr
			stName = "tokenLabelPropertyRef_"+attributeName;
		}
		else if ( r.getRuleLabel(scopeName)!=null ||
			      r.getRuleListLabel(scopeName)!=null )
		{
			// $ruleLabel.attr
			rlScope = (RuleLabelScope)scope;
			if( grammar.type==Grammar.LEXER &&
				RuleLabelScope.predefinedRulePropertiesScope.getAttribute(attributeName)!=null )
			{
				stName = "lexerRuleLabelPropertyRef_"+attributeName;
			}
			else if( grammar.type!=Grammar.LEXER &&
					 RuleLabelScope.predefinedRulePropertiesScope.getAttribute(attributeName)!=null )
			{
				stName = "ruleLabelPropertyRef_"+attributeName;
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
		refST.setAttribute("attr", scope.getAttribute(attributeName));
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
		String scopeName = getID(action, c);
		if ( scopeName==null ) {
			ErrorManager.grammarError(ErrorManager.MSG_MISSING_ATTRIBUTE_NAME,
									  grammar,
									  actionAST.getToken());
			return c+1;
		}
		c += scopeName.length();
		List ruleRefsInAlt = null;
		List tokenRefsInAlt = null;
		if ( r!=null ) {
			ruleRefsInAlt = r.getRuleRefsInAlt(scopeName, actionAST.outerAltNum);
			tokenRefsInAlt = r.getTokenRefsInAlt(scopeName, actionAST.outerAltNum);
		}
		int afterScopeIDIndex = c;
		String scopeIndexExpr = null;
		boolean hasDot =
			(c+1)<action.length() &&
				action.charAt(c)=='.' && Character.isLetter(action.charAt(c+1));
		boolean hasDoubleColon =
			(c+2)<action.length() &&
			 action.charAt(c)==':' && action.charAt(c+1)==':' &&
			 Character.isLetter(action.charAt(c+2));
		if ( c<action.length() && action.charAt(c)=='[' ) { // $x[...]::y case
			int rbrack = action.indexOf(']',c);
			scopeIndexExpr = action.substring(c+1,rbrack);
			rbrack++; // jump to '::' hopefully
			if ( (rbrack+2)<action.length() &&
				action.charAt(rbrack)==':' && action.charAt(rbrack+1)==':' &&
				Character.isLetter(action.charAt(rbrack+2)) )
			{
				hasDoubleColon = true;
			}
			afterScopeIDIndex = rbrack; // should point to the colon
		}

		String attributeName = null;
		int dotIndex = 0;
		if ( hasDot ) {
			// $x.y
			dotIndex = c;
			c++;
			attributeName = getID(action, c);
		}

		AttributeScope attrScope = resolveScope(r, scopeName, attributeName);
		if ( attrScope!=null &&
			 (attrScope.isDynamicRuleScope || attrScope.isDynamicGlobalScope) ) {
			return parseDynamicAttribute(hasDoubleColon,
										 afterScopeIDIndex,
										 action,
										 r,
										 actionAST,
										 attrScope,
										 scopeName,
										 scopeIndexExpr,
										 buf,
										 ruleRefsInAlt);
		}

		if ( hasDot ) {
			// $x.y
			//System.out.println("translate: "+scopeName+"."+attributeName);
			if ( attrScope==null && r!=null && r.name.equals(scopeName) ) {
				// Reference to $r from within rule r: $r.arg, $r.retval, ...
				// Just strip off $r as if it didn't exist and pretend it's $arg
				attrRef = translateIsolatedAttributeReference(r, actionAST, attributeName);
				c += attributeName.length();
			}
			else if ( attrScope!=null ) {
				// $scopeName.attributeName (label, ret val, param)
				attrRef = translateAttributeReference(r, actionAST, attrScope, scopeName, attributeName);
				c += attributeName.length();
			}
			else if ( ruleRefsInAlt!=null ) {
				// $rulereference.attributeName
				// decl : type ID {$type.y} ;
				attrRef =
					translateRuleReference(r, actionAST, scopeName, attributeName);
				c += attributeName.length();
			}
			else if ( tokenRefsInAlt!=null ) {
				// $token.attributeName
				// decl : type ID {$ID.text} ;
				attrRef =
					translateTokenReference(r, actionAST, scopeName, attributeName);
				c += attributeName.length();
			}
			else {
				// $arg.?, $retval.? $listlabel.? Translate before the dot only
				// (could also be $x.y for unknown x outside of a rule)
				attrRef = translateIsolatedAttributeReference(r, actionAST, scopeName);
				c = dotIndex;
			}
		}
		else {
			// Isolated $x
			//System.out.println("translate: "+scopeName);
			if ( tokenRefsInAlt!=null ) {
				// $token
				attrRef = translateTokenReference(r, actionAST, scopeName, null);
			}
			else if ( r!=null && r.getTokenListLabel(scopeName)!=null ) {
				// $tokenList
                attrRef = translateElementListReference(scopeName);
			}
			else if ( r!=null && r.getRuleListLabel(scopeName)!=null ) {
				// $ruleList
				attrRef = translateElementListReference(scopeName);
			}
			else {
				attrRef = translateIsolatedAttributeReference(r, actionAST, scopeName);
			}
		}
		buf.append(attrRef);
		return c;
	}

	/** Handle $x::y and plain $x where x is dynamic scopeName.  Warn if
	 *  ambig ref where x is also a rule ref in the enclosing alt.
	 */
	protected int parseDynamicAttribute(boolean hasDoubleColon,
										int c,
										String action,
										Rule r,
										GrammarAST actionAST,
										AttributeScope dynamicScope,
										String scopeName,
										String scopeIndexExpr,
										StringBuffer buf,
										List ruleRefsInAlt)
	{
		String attrRef;
		if ( hasDoubleColon ) {
			// $x::y
			c+=2; // skip over ::
			String attributeName = getID(action, c);
			c += attributeName.length();
			attrRef =
				translateDynamicAttributeReference(r,
												   actionAST,
												   dynamicScope,
												   scopeName,
												   attributeName,
												   scopeIndexExpr);
			buf.append(attrRef);
			return c;
		}
		else if ( ruleRefsInAlt==null ) {
			// isolated $x scopeName ref (means access stack of scopes itself)
			// must not be a rule reference to x in the enclosing alt
			StringTemplate refST =
				generator.templates.getInstanceOf("isolatedDynamicScopeRef");
			refST.setAttribute("scope", scopeName);
			buf.append(refST.toString());
			return c;
		}
		else {
			// ambiguous reference to $rule since rule is also referenced
			// in the enclosing alt.
			// y of $x::y does not exist (not an attribute in scopeName x)
			ErrorManager.grammarError(ErrorManager.MSG_AMBIGUOUS_RULE_SCOPE,
									  grammar,
									  actionAST.getToken(),
									  scopeName);
			return c;
		}
	}

	/** Translate $x::y or $x[i]::y where x is a rule name or a global
	 *  shared dynamic scope name.
	 */
	protected String translateDynamicAttributeReference(Rule r,
														GrammarAST actionAST,
														AttributeScope scope,
														String scopeName,
														String attributeName,
														String scopeIndexExpr)
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
			generator.templates.getInstanceOf("scopeAttributeRef");
		refST.setAttribute("scope", scopeName);
		refST.setAttribute("attr", attribute);
		if ( scopeIndexExpr!=null ) {
			if ( scopeIndexExpr.startsWith("-") ) {
				refST.setAttribute("negIndex", scopeIndexExpr);
			}
			else {
				refST.setAttribute("index", scopeIndexExpr);
			}
		}
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

		if ( scope instanceof RuleLabelScope && grammar.type==Grammar.LEXER &&
			 attribute==null )
		{
			// $RULE ref in lexer is ok, it's a token
			StringTemplate refST =
				generator.templates.getInstanceOf("lexerRuleLabel");
			refST.setAttribute("label", scopeName);
			// we must ignore the unknown; put it in the translated action
			return refST.toString();
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
			// Might be $parameter or $returnValue or rule-scope property
			List ruleRefs = r.getRuleRefsInAlt(attributeName, actionAST.outerAltNum);
			if ( ruleRefs!=null ) {
				String ruleRef = attributeName;
				// isolated reference to a rule referenced in this alt
				if ( grammar.type == Grammar.LEXER ) { // ok in lexer, is token
					if ( ruleRefs.size()>1 ) {
						ErrorManager.grammarError(ErrorManager.MSG_NONUNIQUE_REF,
												  grammar,
												  actionToken,
												  ref);
						return ref;
					}
					return translateRuleReference(r,actionAST,ruleRef,null);
				}
				else {
					ErrorManager.grammarError(ErrorManager.MSG_ISOLATED_RULE_SCOPE,
											  grammar,
											  actionToken,
											  ref);
				}
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
			else if ( RuleLabelScope.predefinedRulePropertiesScope.getAttribute(attributeName)!=null ) {
				// predefined attribute like $template or $tree or $text
				// referenced within that rule
				String stName = "rulePropertyRef_"+attributeName;
				refST = generator.templates.getInstanceOf(stName);
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
		AttributeScope scope = new RuleLabelScope(referencedRule,actionAST.token);
		return translateAttributeReference(r, actionAST, scope, labelName, attributeName);
	}

	/** We found a %-reference.  The possibilities are:
	 *
	 *    %foo(a={},b={},...) ctor (even shorter than $templates::foo(...))
	 *    %({name-expr})(a={},...) indirect template ctor reference
	 *
	 *    The above are parsed by antlr.g and translated by codegen.g
	 *    The following are parsed manually here:
	 *
	 *    %{string-expr} anonymous template from string expr
	 *    %{expr}.y = z; template attribute y of StringTemplate-typed expr to z
	 *    %x.y = z; set template attribute y of x (always set never get attr)
	 *              to z [languages like python without ';' must still use the
	 *              ';' which the code generator is free to remove during code gen]
	 */
	protected int translateTemplateReference(Rule r,
											 String action,
											 int c,
											 StringBuffer buf,
											 GrammarAST actionAST)
	{
		//System.out.println("%ref="+action.substring(c,action.indexOf('\n',c)));
		// compute some possible indexes
		String templateName = getID(action, c);
		// Is it '%foo('?
		boolean isCtor = templateName!=null &&
			(c+templateName.length())<action.length() &&
			 action.charAt(c+templateName.length())=='(';
		int nameIndex = c;
		int endOfAction = getCurlyAction(action, c);
		if ( (c<action.length() && action.charAt(c)=='(') || isCtor) {
			// %foo(...) or %({expr})(...)
			String outputOption = (String)grammar.getOption("output");
			if ( outputOption!=null && !outputOption.equals("template") ) {
				return c+2;
			}
			else {
				MutableInteger nextCharIndexI = new MutableInteger();
				String t =
					generator.translateTemplateConstructor(r.name,
														   actionAST,
														   nameIndex,
														   nextCharIndexI);
				buf.append(t);
				return nextCharIndexI.value;
			}
		}

		if ( endOfAction>c || templateName!=null ) {
			// %{attr-expr}.y = z;
			// %x.y = z;
			// %{string-expr}
			String translatedExprStr = null;
			if ( endOfAction>c ) {
				String exprStr = action.substring(c+1,endOfAction); // stuff in {...}
				ActionTranslator translator =
					new ActionTranslator(generator,
									     r.name,
										 new antlr.CommonToken(ANTLRParser.ACTION,exprStr),1);
				translatedExprStr =
					translator.translate();
			}
			if ( endOfAction>c &&
				 ((endOfAction+1)<action.length() && action.charAt(endOfAction+1)!='.') ||
			     ((endOfAction+1)>=action.length()) )
			{
				// %{string-expr} literal template constructor
				StringTemplate ctorST =
					generator.templates.getInstanceOf("actionStringConstructor");
				// expr might have $x.y in it...translate it.
				ctorST.setAttribute("stringExpr", translatedExprStr);
				buf.append(ctorST.toString());
				return endOfAction+1;
			}
			String st;
			int endOfExpr;
			if ( templateName!=null ) {
				st = templateName;
				endOfExpr = c+templateName.length()-1;
			}
			else {
				st = translatedExprStr;
				endOfExpr = endOfAction;
			}
			int dot = endOfExpr+1;
			if ( (dot<action.length() && action.charAt(dot)!='.') ) {
				ErrorManager.grammarError(ErrorManager.MSG_INVALID_TEMPLATE_ACTION,
										  grammar,
										  actionAST.getToken(),
										  action.substring(c-1,dot+1));
				return c;
			}
			if ( (dot+1)<action.length() && action.charAt(dot+1)==' ') {
				ErrorManager.grammarError(ErrorManager.MSG_INVALID_TEMPLATE_ACTION,
										  grammar,
										  actionAST.getToken(),
										  action.substring(c-1,dot+2));
				return c;
			}
			int equals = action.indexOf('=',endOfExpr);
			int semicolon = action.indexOf(';',endOfExpr);
			if ( !(dot>c && equals>dot && semicolon>equals) ) {
				// not proper form; report and return
				ErrorManager.grammarError(ErrorManager.MSG_INVALID_TEMPLATE_ACTION,
										  grammar,
										  actionAST.getToken(),
										  action.substring(c-1,c+1));
				return c;
			}
			c++; // skip '.'
			String attrName = getID(action, dot+1);
			String expr = action.substring(equals+1,semicolon);
			// We now have values: st.attrName = expr;
			StringTemplate assignST =
				generator.templates.getInstanceOf("actionSetAttribute");
			assignST.setAttribute("st", st);
			assignST.setAttribute("attrName", attrName);
			assignST.setAttribute("expr", expr);
			buf.append(assignST.toString());
			return semicolon+1;
		}

		ErrorManager.grammarError(ErrorManager.MSG_INVALID_TEMPLATE_ACTION,
									  grammar,
									  actionAST.getToken(),
									  action.substring(c-1,c+1));
		return c; // hmm...don't know what it is, just keep going
	}

	protected String translateElementListReference(String labelName) {
		StringTemplate refST =
			generator.templates.getInstanceOf("listLabelRef");
		refST.setAttribute("label", labelName);
		return refST.toString();
	}

	protected String getID(String action, int c) {
		int start = c;
		int i = c;
		while ( i<action.length() &&
			(Character.isLetterOrDigit(action.charAt(i))||
			action.charAt(i)=='_') )
		{
			i++;
		}
		int end = i-1; // i points at char past first ID
		if ( end<start ) {
			return null;
		}
		return action.substring(start,end+1);
	}

	/** Match (nested) curlies {...} action;
	 *  return the terminating '}' char position.  c must point at '{'.
	 */
	protected int getCurlyAction(String action, int c) {
		if ( action.charAt(c)!='{' ) {
			return c; // not an action
		}
		int level = 0;
		int start = c;
		while ( c<action.length() ) {
			if ( action.charAt(c)=='{' ) {
				level++;
			}
			else if ( action.charAt(c)=='}' ) {
				level--;
				if ( level==0 ) {
					return c;
				}
				if ( level<0 ) {
					return start; // error too many '}'
				}
			}
			c++;
		}
		return start; // finished without seeing '}' or possibly no '{'
	}
}
