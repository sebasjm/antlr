package org.antlr.codegen;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.Grammar;
import org.antlr.Tool;

import java.io.IOException;

public class CTarget extends Target {
	protected void genRecognizerHeaderFile(Tool tool,
										   CodeGenerator generator,
										   Grammar grammar,
										   StringTemplate headerFileST)
		throws IOException
	{
		generator.write(headerFileST, grammar.getName()+".h");
	}
}

