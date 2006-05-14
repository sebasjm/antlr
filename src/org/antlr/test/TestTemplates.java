package org.antlr.test;

import org.antlr.tool.*;
import org.antlr.Tool;
import org.antlr.test.unit.TestSuite;
import org.antlr.test.unit.FailedAssertionException;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.antlr.codegen.CodeGenerator;
import org.antlr.codegen.ActionTranslator;

/** Test templates in actions; %... shorthands */
public class TestTemplates extends TestSuite {

	public void testTemplateConstructor() throws Exception {
		String action = "x = %foo(name={$ID.text});";
		String expecting = "x = templateLib.getInstanceOf(\"foo\",\n" +
			"  new STAttrMap().put(\"name\", ID1.getText()));";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n" +
				"options {\n" +
				"    output=template;\n" +
				"}\n" +
				"\n" +
				"a : ID {"+action+"}\n" +
				"  ;\n" +
				"\n" +
				"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer(); // forces load of templates
		ActionTranslator translator =
			new ActionTranslator(generator,
								 "a",
								 new antlr.CommonToken(ANTLRParser.ACTION,action),1);
		String rawTranslation =
			translator.translate();
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);

		assertEqual(found, expecting);
	}

	public void testIndirectTemplateConstructor() throws Exception {
		String action = "x = %({\"foo\"})(name={$ID.text});";
		String expecting = "x = templateLib.getInstanceOf(\"foo\",\n" +
			"  new STAttrMap().put(\"name\", ID1.getText()));";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n" +
				"options {\n" +
				"    output=template;\n" +
				"}\n" +
				"\n" +
				"a : ID {"+action+"}\n" +
				"  ;\n" +
				"\n" +
				"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer(); // forces load of templates
		ActionTranslator translator =
			new ActionTranslator(generator,
								 "a",
								 new antlr.CommonToken(ANTLRParser.ACTION,action),1);
		String rawTranslation =
			translator.translate();
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);

		assertEqual(found, expecting);
	}

	public void testStringConstructor() throws Exception {
		String action = "x = %{$ID.text};";
		String expecting = "x = new StringTemplate(templateLib,ID1.getText());";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n" +
				"options {\n" +
				"    output=template;\n" +
				"}\n" +
				"\n" +
				"a : ID {"+action+"}\n" +
				"  ;\n" +
				"\n" +
				"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer(); // forces load of templates
		ActionTranslator translator = new ActionTranslator(generator,
														   "a",
								 new antlr.CommonToken(ANTLRParser.ACTION,action),1);
		String rawTranslation =
			translator.translate();
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);

		assertEqual(found, expecting);
	}

	public void testSetAttr() throws Exception {
		String action = "%x.y = z;";
		String expecting = "(x).setAttribute(\"y\", z);";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n" +
				"options {\n" +
				"    output=template;\n" +
				"}\n" +
				"\n" +
				"a : ID {"+action+"}\n" +
				"  ;\n" +
				"\n" +
				"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer(); // forces load of templates
		ActionTranslator translator =
			new ActionTranslator(generator,
								 "a",
								 new antlr.CommonToken(ANTLRParser.ACTION,action),1);
		String rawTranslation =
			translator.translate();
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);

		assertEqual(found, expecting);
	}

	public void testSetAttrOfExpr() throws Exception {
		String action = "%{foo($ID.text).getST()}.y = z;";
		String expecting = "(foo(ID1.getText()).getST()).setAttribute(\"y\", z);";

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n" +
				"options {\n" +
				"    output=template;\n" +
				"}\n" +
				"\n" +
				"a : ID {"+action+"}\n" +
				"  ;\n" +
				"\n" +
				"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer(); // forces load of templates
		ActionTranslator translator = new ActionTranslator(generator,
								 "a",
								 new antlr.CommonToken(ANTLRParser.ACTION,action),1);
		String rawTranslation =
			translator.translate();
		StringTemplateGroup templates =
			new StringTemplateGroup(".", AngleBracketTemplateLexer.class);
		StringTemplate actionST = new StringTemplate(templates, rawTranslation);
		String found = actionST.toString();

		assertTrue(equeue.errors.size()==0, "unexpected errors: "+equeue);

		assertEqual(found, expecting);
	}

	public void testCannotHaveSpaceBeforeDot() throws Exception {
		String action = "%x .y = z;";
		String expecting = null;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n" +
				"options {\n" +
				"    output=template;\n" +
				"}\n" +
				"\n" +
				"a : ID {"+action+"}\n" +
				"  ;\n" +
				"\n" +
				"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer(); // forces load of templates

		int expectedMsgID = ErrorManager.MSG_INVALID_TEMPLATE_ACTION;
		Object expectedArg = "%x ";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	public void testCannotHaveSpaceAfterDot() throws Exception {
		String action = "%x. y = z;";
		String expecting = null;

		ErrorQueue equeue = new ErrorQueue();
		ErrorManager.setErrorListener(equeue);
		Grammar g = new Grammar(
			"grammar t;\n" +
				"options {\n" +
				"    output=template;\n" +
				"}\n" +
				"\n" +
				"a : ID {"+action+"}\n" +
				"  ;\n" +
				"\n" +
				"ID : 'a';\n");
		Tool antlr = new Tool();
		CodeGenerator generator = new CodeGenerator(antlr, g, "Java");
		g.setCodeGenerator(generator);
		generator.genRecognizer(); // forces load of templates

		int expectedMsgID = ErrorManager.MSG_INVALID_TEMPLATE_ACTION;
		Object expectedArg = "%x. ";
		GrammarSemanticsMessage expectedMessage =
			new GrammarSemanticsMessage(expectedMsgID, g, null, expectedArg);
		checkError(equeue, expectedMessage);
	}

	protected void checkError(ErrorQueue equeue,
							  GrammarSemanticsMessage expectedMessage)
		throws FailedAssertionException
	{
		/*
		System.out.println(equeue.infos);
		System.out.println(equeue.warnings);
		System.out.println(equeue.errors);
		*/
		Message foundMsg = null;
		for (int i = 0; i < equeue.errors.size(); i++) {
			Message m = (Message)equeue.errors.get(i);
			if (m.msgID==expectedMessage.msgID ) {
				foundMsg = m;
			}
		}
		assertTrue(equeue.errors.size()>0, "no error; "+expectedMessage.msgID+" expected");
		assertTrue(equeue.errors.size()<=1, "too many errors; "+equeue.errors);
		assertTrue(foundMsg!=null, "couldn't find expected error: "+expectedMessage.msgID);
		assertTrue(foundMsg instanceof GrammarSemanticsMessage,
				   "error is not a GrammarSemanticsMessage");
		assertEqual(foundMsg.arg, expectedMessage.arg);
		assertEqual(foundMsg.arg2, expectedMessage.arg2);
	}
}
