package org.antlr.codegen;

import org.antlr.Tool;
import org.antlr.codegen.bytecode.ClassFile;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.tool.Grammar;

import java.io.IOException;

public class JavaTarget extends Target {
	protected StringTemplate chooseWhereCyclicDFAsGo(Tool tool,
													 CodeGenerator generator,
													 Grammar grammar,
													 StringTemplate recognizerST,
													 StringTemplate cyclicDFAST)
	{
		return cyclicDFAST;
	}

	protected void genCyclicDFAFile(Tool tool,
									CodeGenerator generator,
									Grammar grammar,
									StringTemplate cyclicDFAST)
		throws IOException
	{
		generator.write(cyclicDFAST, grammar.getName()+".bytecode");
		ClassFile code = new ClassFile(tool,
									   null, // TODO: how to get package?
									   grammar.getName()+"_DFA",
									   "java/lang/Object",
									   tool.getOutputDirectory(),
									   cyclicDFAST.toString());
		code.write();
	}
}

