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
package org.antlr;

import org.antlr.tool.DOTGenerator;
import org.antlr.tool.Grammar;
import org.antlr.codegen.CodeGenerator;
import org.antlr.stringtemplate.StringTemplate;

import java.io.*;

/** The main ANTLR entry point.  Read a grammar and generate a parser. */
public class Tool {
    /** If hasError, cannot continue processing */
    protected boolean hasError;

    public static final String Version = "3.0 prototype";

    // Input parameters / option

    protected String grammarFileName;
    protected String outputDir = ".";

    public static void main(String[] args) {
        System.err.println("ANTLR Parser Generator   Version " +
                Version + "   1989-2004");
        try {
            Tool antlr = new Tool();
            antlr.processArgs(args);
            antlr.process();
        }
        catch (Exception e) {
            System.err.println(System.getProperty("line.separator") +
                    System.getProperty("line.separator"));
            System.err.println("#$%%*&@# internal error: " + e.toString());
            System.err.println("[complain to nearest government official");
            System.err.println(" or send hate-mail to terence@parr.us;");
            System.err.println(" please send stack trace with report.]" +
                    System.getProperty("line.separator"));
            e.printStackTrace();
        }
        System.exit(0);
    }

    protected void processArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o")) {
                if (i + 1 >= args.length) {
                    System.err.println("missing output directory with -o option; ignoring");
                }
                else {
                    i++;
                    outputDir = args[i];
                }
            }
            else {
                if (args[i].charAt(0) != '-') {
                    // Must be the grammar file
                    grammarFileName = args[i];
                }
            }
        }
    }

    /*
    protected void checkForInvalidArguments(String[] args, BitSet cmdLineArgValid) {
        // check for invalid command line args
        for (int a = 0; a < args.length; a++) {
            if (!cmdLineArgValid.member(a)) {
                System.err.println("invalid command-line argument: " + args[a] + "; ignored");
            }
        }
    }
    */

    protected void process()  {
        try {
			StringTemplate.setLintMode(true);
            FileReader fr = new FileReader(grammarFileName);
            BufferedReader br = new BufferedReader(fr);
            Grammar grammar = new Grammar(br);
            br.close();
            fr.close();

			processGrammar(grammar);

			// now handle the lexer if one was created for a merged spec

			String lexerGrammarStr = grammar.getLexerGrammar();
			if ( lexerGrammarStr!=null ) {
				System.out.println("lexer="+lexerGrammarStr);
				StringReader sr = new StringReader(lexerGrammarStr);
				Grammar lexerGrammar = new Grammar(sr);
				sr.close();
				processGrammar(lexerGrammar);
			}
		}
        catch (Exception e) {
            toolError("error processing file "+grammarFileName, e);
        }
    }

	protected void processGrammar(Grammar grammar) throws IOException {
		String language = (String)grammar.getOption("language");
		if ( language!=null ) {
			CodeGenerator generator = new CodeGenerator(this, grammar, language);
			grammar.setCodeGenerator(generator);

			if ( grammar.getType()==Grammar.LEXER ) {
				grammar.addArtificialMatchTokensRule();
			}

			generator.genRecognizer();
		}

		new DOTGenerator(grammar).writeDOTFilesForAllRuleNFAs();
	}

	private static void help() {
        System.err.println("usage: java org.antlr.Tool [args] file.g");
        System.err.println("  -o outputDir       specify output directory where all output generated.");
        System.err.println("  -glib inputDir     specify location of token files, grammars");
    }

    /** This method is used by all code generators to create new output
     *  files. If the outputDir set by -o is not present it will be created.
     */
    public FileWriter getOutputFile(String fileName) throws IOException {
        if( !outputDir.equals(".") ) {
            File outDir = new File(outputDir);
            if( !outDir.exists() ) {
                outDir.mkdirs();
            }
        }
        return new FileWriter(outputDir + "/"+ fileName);
    }

    // E R R O R  R O U T I N E S

    public void toolError(String s, Exception e) {
        hasError = true;
        System.err.println("error: " + s);
        e.printStackTrace(System.err);
    }

}
