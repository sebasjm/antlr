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

import org.antlr.tool.Grammar;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.Tool;
import org.antlr.analysis.Label;

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
	 *  suitable for the target language.  ANTLR uses Grammar.defineToken
	 *  to define char literals as tokens and stores the original text
	 *  (found in the grammar) in the char literals table for use when
	 *  displaying tokens and generating the token names table in the
	 *  generated parser.  Any escaped chars must be converted to their
	 *  intended value for use during analysis, however.  For example, '\n'
	 *  should be stored as the 4 char sequence '\n' in the literals table,
	 *  but the value of 10 (newline) must be used during analysis.
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
	public int getMaxCharValue() {
		return Label.MAX_CHAR_VALUE;
	}
}
