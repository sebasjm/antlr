package org.antlr.codegen;

import org.antlr.tool.Grammar;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.Tool;

import java.io.IOException;

/** The code generator for ANTLR can usually be retargeted just by providing
 *  a new X.stg file for language X, however, sometimes the files that must
 *  be generated vary enough that some X-specific functionality is required.
 *  For example, in C, you must generate header files whereas in Java you do not.
 *  On the other hand, Java must generate a separate file for cyclic DFAs
 *  so it can access bytecode's goto instruction.  Other languages may want
 *  to keep these DFA separate from the main generated recognizer file also.
 *
 *  The notion of a Code Generator target abstracts out the creation
 *  of the various files.  As new language targets get added to the ANTLR
 *  system, this target class may have to be altered to handle more
 *  functionality.  Eventually, just about all language generation issues
 *  will be expressible in terms of these methods.
 *
 *  If org.antlr.codegen.XTarget class exists, it is used else
 *  Target base class is used.  I am using a superclass rather than an
 *  interface for this target concept because I can add functionality
 *  later without breaking previously written targets (extra interface
 *  methods would force adding dummy functions to all code generator
 *  target classes).
 *
 */
public class Target {
	protected void genRecognizerFile(Tool tool,
									CodeGenerator generator,
									Grammar grammar,
									StringTemplate outputFileST)
		throws IOException
	{
		String fileName = generator.getRecognizerFileName();
		generator.write(outputFileST, fileName);
	}

	protected void genRecognizerHeaderFile(Tool tool,
										   CodeGenerator generator,
										   Grammar grammar,
										   StringTemplate headerFileST)
		throws IOException
	{
		// no header file by default
	}

	protected void performGrammarAnalysis(CodeGenerator generator,
										  Grammar grammar)
	{
		// Build NFAs from the grammar AST
		grammar.createNFAs();

		// Create the DFA predictors for each decision
		grammar.createLookaheadDFAs();
	}

	/** Convert from an ANTLR char literal as read by antlr.g to a literal
	 *  suitable for the target language.
	 */
	protected String getEscapedCharLiteral(String literal) {
		return CodeGenerator.getJavaEscapedCharFromANTLRLiteral(literal);
	}

	/** Convert from an ANTLR string literal as read by antlr.g to a literal
	 *  suitable for the target language.
	 */
	protected String getEscapedStringLiteral(String literal) {
		return CodeGenerator.getJavaEscapedStringFromANTLRLiteral(literal);
	}

	/** Some targets only support ASCII or 8-bit chars/strings. */
	protected int getMaxCharValue() {
		return '\uFFFF';
	}
}
