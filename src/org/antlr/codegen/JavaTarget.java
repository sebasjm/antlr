package org.antlr.codegen;

import org.antlr.Tool;
import org.antlr.codegen.bytecode.ClassFile;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.Grammar;

import java.io.IOException;

public class JavaTarget extends Target {
	protected void genCyclicDFAFile(Tool tool,
									CodeGenerator generator,
									Grammar grammar,
									StringTemplate cyclicDFAST)
		throws IOException
	{
		ClassFile code = new ClassFile(tool,
									   null,
									   grammar.getName()+"_DFA",
									   "java/lang/Object",
									   "/tmp",
									   cyclicDFAST.toString());
		code.write();
	}
}

