/*
 [The "BSD licence"]
 Copyright (c) 2004 Terence Parr
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
package org.antlr.test;

import org.antlr.test.unit.TestSuite;
import org.antlr.test.unit.FailedAssertionException;
import org.antlr.tool.*;
import org.antlr.analysis.Label;
import org.antlr.codegen.CodeGenerator;
import org.antlr.codegen.ActionTranslator;
import org.antlr.Tool;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.debug.RemoteDebugEventSocketListener;

import java.util.*;

import antlr.Token;

/** Check the $x, $x.y, and @x.y attributes.  For checking the actual
 *  translation, assume the Java target.  This is still a great test
 *  for the semantics of the $x.y stuff regardless of the target.
 */
public class TestAttributes extends TestSuite {

	static class ErrorQueue implements ANTLRErrorListener {
		List infos = new LinkedList();
		List errors = new LinkedList();
		List warnings = new LinkedList();

		public void info(String msg) {
			infos.add(msg);
		}

		public void error(Message msg) {
			errors.add(msg);
		}

		public void warning(Message msg) {
			warnings.add(msg);
		}

		public void error(ToolMessage msg) {
			errors.add(msg);
		}

		public String toString() {
			return "infos: "+infos+
				   "errors: "+errors+
				   "warnings: "+warnings;
		}
	};

    /** Public default constructor used by TestRig */
    public TestAttributes() {
    }

	public void testEscapedLessThanInAction() throws Exception {
		Grammar g = new Grammar();
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String action = "i<3; \"<xmltag>\"";
		String expecting = action;
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);
	}

	public void testArguments() throws Exception {
		String action = "$i; $i.x; $u; $u.x";
		String expecting = "i; i.x; u; u.x";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a[User u, int i]\n" +
			"        : {"+action+"}\n" +
			"        ;");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testInvalidArguments() throws Exception {
		String action = "$x";
		String expecting = action;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a[User u, int i]\n" +
			"        : {"+action+"}\n" +
			"        ;");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_UNKNOWN_SIMPLE_ATTRIBUTE;
		Object expectedArg = "$x";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testReturnValues() throws Exception {
		String action = "$i; $i.x; $u; $u.x";
		String expecting = "retval.i; retval.i.x; retval.u; retval.u.x";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a returns [User u, int i]\n" +
			"        : {"+action+"}\n" +
			"        ;");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testInvalidReturnValues() throws Exception {
		String action = "$x";
		String expecting = action;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a returns [User u, int i]\n" +
			"        : {"+action+"}\n" +
			"        ;");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_UNKNOWN_SIMPLE_ATTRIBUTE;
		Object expectedArg = "$x";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testTokenLabels() throws Exception {
		String action = "$id; $f; $id.text; $id.getText(); $id.dork " +
			"$id.type; $id.line; $id.pos; " +
			"$id.channel; $id.index;";
		String expecting = "id; f; id.getText(); id.getText(); id.dork " +
			"id.getType(); id.getLine(); id.getCharPositionInLine(); " +
			"id.getChannel(); id.getTokenIndex();";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a : id=ID f=FLOAT {"+action+"}\n" +
			"  ;");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testRuleLabels() throws Exception {
		String action = "$r.x; $r.start; $r.stop; $r.tree";
		String expecting = "r.x; r.start; r.stop; r.tree";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a returns [int x]\n" +
			"  :\n" +
			"  ;\n"+
			"b : r=a[3] {"+action+"}\n" +
			"  ;");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("b",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testInvalidRuleLabelAccessesParameter() throws Exception {
		String action = "$r.z";
		String expecting = action;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a[int z] returns [int x]\n" +
			"  :\n" +
			"  ;\n"+
			"b : r=a[3] {"+action+"}\n" +
			"  ;");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("b",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_INVALID_RULE_PARAMETER_REF;
		Object expectedArg = "r";
		Object expectedArg2 = "z";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testInvalidRuleLabelAccessesScopeAttribute() throws Exception {
		String action = "$r.n";
		String expecting = action;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a\n" +
			"scope { int n; }\n" +
			"  :\n" +
			"  ;\n"+
			"b : r=a[3] {"+action+"}\n" +
			"  ;");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("b",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_INVALID_RULE_SCOPE_ATTRIBUTE_REF;
		Object expectedArg = "r";
		Object expectedArg2 = "n";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testInvalidRuleAttribute() throws Exception {
		String action = "$r.blort";
		String expecting = action;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a[int z] returns [int x]\n" +
			"  :\n" +
			"  ;\n"+
			"b : r=a[3] {"+action+"}\n" +
			"  ;");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("b",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_UNKNOWN_RULE_ATTRIBUTE;
		Object expectedArg = "r";
		Object expectedArg2 = "blort";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testMissingRuleAttribute() throws Exception {
		String action = "$r";
		String expecting = action;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"a[int z] returns [int x]\n" +
			"  :\n" +
			"  ;\n"+
			"b : r=a[3] {"+action+"}\n" +
			"  ;");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("b",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_ISOLATED_RULE_ATTRIBUTE;
		Object expectedArg = "$r";
		Object expectedArg2 = null;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testNonDynamicAttributeOutsideRule() throws Exception {
		String action = "public void foo() { $x; }";
		String expecting = action;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"{\"+action+\"}\n" +
			"a : ;\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate(null,
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_ATTRIBUTE_REF_NOT_IN_RULE;
		Object expectedArg = "$x";
		Object expectedArg2 = null;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testNonDynamicAttributeOutsideRule2() throws Exception {
		String action = "public void foo() { $x.y; }";
		String expecting = action;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"parser grammar t;\n"+
			"{\"+action+\"}\n" +
			"a : ;\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate(null,
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_ATTRIBUTE_REF_NOT_IN_RULE;
		Object expectedArg = "$x";
		Object expectedArg2 = null;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	// D Y N A M I C A L L Y  S C O P E D  A T T R I B U T E S

	public void testBasicGlobalScope() throws Exception {
		String action = "$Symbols.names.add($id.text);";
		String expecting = "((Symbols)Symbols_stack.peek()).names.add(id.getText());";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"scope Symbols {\n" +
			"  int n;\n" +
			"  List names;\n" +
			"}\n" +
			"a : (id=ID ';' {"+action+"} )+\n" +
			"  ;\n" +
			"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testGlobalScopeOutsideRule() throws Exception {
		String action = "public void foo() {$Symbols.names.add(\"foo\");}";
		String expecting = "public void foo() {((Symbols)Symbols_stack.peek()).names.add(\"foo\");}";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"scope Symbols {\n" +
			"  int n;\n" +
			"  List names;\n" +
			"}\n" +
			"{\"+action+\"}\n" +
			"a : \n" +
			"  ;\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testRuleScopeOutsideRule() throws Exception {
		String action = "public void foo() {$a.name;}";
		String expecting = "public void foo() {((a_scope)a_stack.peek()).name;}";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"{\"+action+\"}\n" +
			"a\n" +
			"scope { int name; }\n" +
			"  : {foo();}\n" +
			"  ;\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testBasicRuleScope() throws Exception {
		String action = "$a.n; $n;"; // both ok
		String expecting = "((a_scope)a_stack.peek()).n; ((a_scope)a_stack.peek()).n;";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a\n" +
			"scope {\n" +
			"  int n;\n" +
			"} : {"+action+"}\n" +
			"  ;\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testRuleScopeFromAnotherRule() throws Exception {
		String action = "$a.n;"; // must be qualified
		String expecting = "((a_scope)a_stack.peek()).n;";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a\n" +
			"scope {\n" +
			"  int n;\n" +
			"} : b\n" +
			"  ;\n" +
			"b : {\"+action+\"}\n" +
			"  ;\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("b",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testUnknownDynamicAttribute() throws Exception {
		String action = "$a.x";
		String expecting = action;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a\n" +
			"scope {\n" +
			"  int n;\n" +
			"} : {"+action+"}\n" +
			"  ;\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_UNKNOWN_RULE_ATTRIBUTE;
		Object expectedArg = "a";
		Object expectedArg2 = "x";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testUnknownGlobalDynamicAttribute() throws Exception {
		String action = "$Symbols.x";
		String expecting = action;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"scope Symbols {\n" +
			"  int n;\n" +
			"}\n" +
			"a : {\"+action+\"}\n" +
			"  ;\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_UNKNOWN_ATTRIBUTE_IN_SCOPE;
		Object expectedArg = "Symbols";
		Object expectedArg2 = "x";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testUnqualifiedRuleScopeAttribute() throws Exception {
		String action = "$n;"; // must be qualified
		String expecting = "$n;";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a\n" +
			"scope {\n" +
			"  int n;\n" +
			"} : b\n" +
			"  ;\n" +
			"b : {\"+action+\"}\n" +
			"  ;\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("b",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		int expectedMsgID = ErrorManager.MSG_UNKNOWN_SIMPLE_ATTRIBUTE;
		Object expectedArg = "$n";
		Object expectedArg2 = null;
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testRuleAndTokenLabelTypeMismatch() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : id=ID id=b\n" +
			"  ;\n" +
			"b : ;\n");
		int expectedMsgID = ErrorManager.MSG_LABEL_TYPE_CONFLICT;
		Object expectedArg = "id";
		Object expectedArg2 = "rule!=token";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testListAndTokenLabelTypeMismatch() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a : ids+=ID ids=ID\n" +
			"  ;\n" +
			"b : ;\n");
		int expectedMsgID = ErrorManager.MSG_LABEL_TYPE_CONFLICT;
		Object expectedArg = "ids";
		Object expectedArg2 = "token!=list";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testArgReturnValueMismatch() throws Exception {
		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n"+
			"a[int i] returns [int x, int i]\n" +
			"  : \n" +
			"  ;\n" +
			"b : ;\n");
		int expectedMsgID = ErrorManager.MSG_ARG_RETVAL_CONFLICT;
		Object expectedArg = "i";
		Object expectedArg2 = "a";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg, expectedArg2);
		checkError(equeue, expectedMessage);
	}

	public void testSimplePlusEqualLabel() throws Exception {
		String action = "$ids.size();"; // must be qualified
		String expecting = "ids.size();";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
				"parser grammar t;\n"+
				"a : ids+=ID ( COMMA ids+=ID {"+action+"})* ;\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testPlusEqualStringLabel() throws Exception {
		String action = "$ids.size();"; // must be qualified
		String expecting = "ids.size();";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
				"grammar t;\n"+
				"a : ids+=\"if\" ( ',' ids+=ID {"+action+"})* ;" +
				"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testPlusEqualSetLabel() throws Exception {
		String action = "$ids.size();"; // must be qualified
		String expecting = "ids.size();";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
				"grammar t;\n"+
				"a : ids+=('a'|'b') ( ',' ids+=ID {"+action+"})* ;" +
				"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	public void testPlusEqualWildcardLabel() throws Exception {
		String action = "$ids.size();"; // must be qualified
		String expecting = "ids.size();";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
				"grammar t;\n"+
				"a : ids+=. ( ',' ids+=ID {"+action+"})* ;" +
				"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		ActionTranslator translator = new ActionTranslator(generator);
		String rawTranslation =
			translator.translate("a",
							 new antlr.CommonToken(ANTLRParser.ACTION,action));
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();
		assertEqual(found, expecting);

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);
	}

	// S U P P O R T

	protected void checkError(ErrorQueue equeue,
							  GrammarSemanticsMessage expectedMessage)
		throws FailedAssertionException
	{
		/*
		System.out.println(equeue.infos);
		System.out.println(equeue.warnings);
		System.out.println(equeue.errors);
		assertTrue(equeue.errors.size()==n,
				   "number of errors mismatch; expecting "+n+"; found "+
				   equeue.errors.size());
		*/
		Message foundMsg = null;
		for (int i = 0; i < equeue.errors.size(); i++) {
			Message m = (Message)equeue.errors.get(i);
			if (m.msgID==expectedMessage.msgID ) {
				foundMsg = m;
			}
		}
		assertTrue(foundMsg!=null, "no error; "+expectedMessage.msgID+" expected");
		assertTrue(foundMsg instanceof GrammarSemanticsMessage,
				   "error is not a GrammarSemanticsMessage");
		assertEqual(foundMsg.arg, expectedMessage.arg);
		assertEqual(foundMsg.arg2, expectedMessage.arg2);
	}
}