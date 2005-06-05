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
package org.antlr;

import org.antlr.tool.*;
import org.antlr.codegen.CodeGenerator;
import org.antlr.analysis.*;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

/** The main ANTLR entry point.  Read a grammar and generate a parser. */
public class Tool {
    /** If hasError, cannot continue processing */
    protected boolean hasError;

    public static final String VERSION = "3.0ea2";

    // Input parameters / option

    protected List grammarFileNames = new ArrayList();
	protected boolean generate_NFA_dot = false;
	protected boolean generate_DFA_dot = false;
	protected String outputDirectory = ".";
	protected String libDirectory = ".";
	protected boolean debug = false;
	protected boolean trace = false;
	protected boolean profile = false;
	protected boolean report = false;

    public static void main(String[] args) {
        ErrorManager.info("ANTLR Parser Generator   Early Access Version " +
                VERSION + " (June 1, 2005)  1989-2005");
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
            System.err.println(" or send hate-mail to parrt@antlr.org;");
            System.err.println(" please send stack trace with report.]" +
                    System.getProperty("line.separator"));
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void processArgs(String[] args) {
		if ( args.length==0 ) {
			help();
			return;
		}
        for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-o")) {
				if (i + 1 >= args.length) {
					System.err.println("missing output directory with -o option; ignoring");
				}
				else {
					i++;
					outputDirectory = args[i];
					if ( outputDirectory.endsWith("/") ||
						 outputDirectory.endsWith("\\") )
					{
						outputDirectory =
							outputDirectory.substring(0,outputDirectory.length()-1);
					}
				}
			}
			else if (args[i].equals("-lib")) {
				if (i + 1 >= args.length) {
					System.err.println("missing library directory with -lib option; ignoring");
				}
				else {
					i++;
					libDirectory = args[i];
					if ( libDirectory.endsWith("/") ||
						 libDirectory.endsWith("\\") )
					{
						libDirectory =
							libDirectory.substring(0,libDirectory.length()-1);
					}
					File outDir = new File(libDirectory);
					if( !outDir.exists() ) {
						ErrorManager.error(ErrorManager.MSG_DIR_NOT_FOUND,libDirectory);
						libDirectory = ".";
					}
				}
			}
			else if (args[i].equals("-verbose")) {
				DecisionProbe.verbose=true;
			}
			else if (args[i].equals("-nfa")) {
				generate_NFA_dot=true;
			}
			else if (args[i].equals("-dfa")) {
				generate_DFA_dot=true;
			}
			else if (args[i].equals("-debug")) {
				debug=true;
			}
			else if (args[i].equals("-trace")) {
				trace=true;
			}
			else if (args[i].equals("-report")) {
				report=true;
			}
			else if (args[i].equals("-profile")) {
				profile=true;
			}
            else {
                if (args[i].charAt(0) != '-') {
                    // Must be the grammar file
                    grammarFileNames.add(args[i]);
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
		for (int i = 0; i < grammarFileNames.size(); i++) {
			String grammarFileName = (String) grammarFileNames.get(i);
			try {
				//StringTemplate.setLintMode(true);
				FileReader fr = new FileReader(grammarFileName);
				BufferedReader br = new BufferedReader(fr);
				Grammar grammar = new Grammar(this,grammarFileName,br);
				br.close();
				fr.close();

				processGrammar(grammar);

				// now handle the lexer if one was created for a merged spec

				String lexerGrammarStr = grammar.getLexerGrammar();
				if ( grammar.type==Grammar.COMBINED && lexerGrammarStr!=null ) {
					Writer w = getOutputFile(grammar,grammar.name+".lexer.g");
					w.write(lexerGrammarStr);
					w.close();
					StringReader sr = new StringReader(lexerGrammarStr);
					Grammar lexerGrammar = new Grammar();
					lexerGrammar.setFileName("<internally-generated-lexer>");
					lexerGrammar.importTokenVocabulary(grammar);
					lexerGrammar.setGrammarContent(sr);
					sr.close();
					processGrammar(lexerGrammar);
				}

				if ( generate_NFA_dot ) {
					generateNFAs(grammar);
				}
				if ( generate_DFA_dot ) {
					generateDFAs(grammar);
				}
				if ( report ) {
					String report = new GrammarReport(grammar).toString();
					System.out.println(report);
				}
				GrammarReport report = new GrammarReport(grammar);
				GrammarReport.writeReport(GrammarReport.GRAMMAR_STATS_FILENAME,
										  report.toNotifyString());
			}
			catch (Exception e) {
				ErrorManager.error(ErrorManager.MSG_INTERNAL_ERROR, grammarFileName, e);
			}
		}
    }

	protected void processGrammar(Grammar grammar)
		throws IOException
	{
		String language = (String)grammar.getOption("language");
		if ( language!=null ) {
			CodeGenerator generator = new CodeGenerator(this, grammar, language);
			grammar.setCodeGenerator(generator);
			generator.setDebug(debug);
			// for this release, everybody does profile unless
			// debugging
			if ( !debug ) {
				generator.setProfile(true);
			}
			if ( profile ) {
				generator.setDumpProfile(true);
			}
			generator.setTrace(trace);

			if ( grammar.type==Grammar.LEXER ) {
				grammar.addArtificialMatchTokensRule();
			}

			generator.genRecognizer();
		}
	}

	protected void generateDFAs(Grammar g) {
		for (int d=1; d<=g.getNumberOfDecisions(); d++) {
			DFA dfa = g.getLookaheadDFA(d);
			DOTGenerator dotGenerator = new DOTGenerator(g);
			String dot = dotGenerator.getDOT( dfa.startState );
			String dotFileName = g.name+"_dec-"+d;
			try {
				writeDOTFile(g, dotFileName, dot);
			}
			catch(IOException ioe) {
				ErrorManager.error(ErrorManager.MSG_CANNOT_GEN_DOT_FILE,
								   dotFileName,
								   ioe);
			}
		}
	}

	protected void generateNFAs(Grammar g) {
		DOTGenerator dotGenerator = new DOTGenerator(g);
		Collection rules = g.getRules();
		for (Iterator itr = rules.iterator(); itr.hasNext();) {
			Rule r = (Rule) itr.next();
			String ruleName = r.name;
			try {
				writeDOTFile(
					g,
					ruleName,
					dotGenerator.getDOT(g.getRuleStartState(ruleName)));
			}
			catch (IOException ioe) {
				ErrorManager.error(ErrorManager.MSG_CANNOT_WRITE_FILE, ioe);
			}
		}
	}

	protected void writeDOTFile(Grammar g, String name, String dot) throws IOException {
		Writer fw = getOutputFile(g, name+".dot");
		fw.write(dot);
		fw.close();
	}

	private static void help() {
        System.err.println("usage: java org.antlr.Tool [args] file.g [file2.g file3.g ...]");
		System.err.println("  -o outputDir   specify output directory where all output is generated");
		System.err.println("  -lib dir       specify location of token files");
		System.err.println("  -report        print out a report about the grammar(s) processed");
		System.err.println("  -debug         generate a parser that emits debugging events");
		System.err.println("  -profile       generate a parser that computes profiling information");
		System.err.println("  -nfa           generate an NFA for each rule");
		System.err.println("  -dfa           generate a DFA for each decision point");
    }

	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

    /** This method is used by all code generators to create new output
     *  files. If the outputDir set by -o is not present it will be created.
	 *  The final filename is sensitive to the output directory and
	 *  the directory where the grammar file was found.  If -o is /tmp
	 *  and the original grammar file was foo/t.g then output files
	 *  go in /tmp/foo.
	 *
	 *  If outputDirectory==null then write a String and toss away (for now);
	 *  like writing to /dev/null.
     */
    public Writer getOutputFile(Grammar g, String fileName) throws IOException {
		if ( outputDirectory==null ) {
			return new StringWriter();
		}
		String fullName;
		String fileDir = g.getFileDirectory();
		if ( fileDir==null ) {
			fullName =
				outputDirectory+File.separator+
				fileName;
		}
		else {
			fullName =
				outputDirectory+File.separator+
				fileDir+File.separator+
				fileName;
		}
		File outDir = new File(fullName).getParentFile();
		if( !outDir.exists() ) {
			outDir.mkdirs();
		}
        FileWriter fw = new FileWriter(fullName);
		return new BufferedWriter(fw);
    }

	public String getOutputDirectory() {
		return outputDirectory;
	}

	/** Open a file in the -lib dir.  For now, it's just .tokens files */
	public BufferedReader getLibraryFile(String fileName) throws IOException {
		String fullName = libDirectory+File.separator+fileName;
		FileReader fr = new FileReader(fullName);
		BufferedReader br = new BufferedReader(fr);
		return br;
	}
}
