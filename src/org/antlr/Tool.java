/*
 [The "BSD licence"]
 Copyright (c) 2005-2006 Terence Parr
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
import java.util.*;

/** The main ANTLR entry point.  Read a grammar and generate a parser. */
public class Tool {
    /** If hasError, cannot continue processing */
    protected boolean hasError;

	public static final String VERSION = "3.0b1";

	public static final String UNINITIALIZED_DIR = "<unset-dir>";

    // Input parameters / option

    protected List grammarFileNames = new ArrayList();
	protected boolean generate_NFA_dot = false;
	protected boolean generate_DFA_dot = false;
	protected String outputDirectory = UNINITIALIZED_DIR;
	protected String libDirectory = ".";
	protected boolean debug = false;
	protected boolean trace = false;
	protected boolean profile = false;
	protected boolean report = false;
	protected boolean memo = true;
	protected boolean printGrammar = false;

	// the internal options are for my use on the command line during dev

	public static boolean internalOption_PrintGrammarTree = false;
	public static boolean internalOption_PrintDFA = false;
	public static boolean internalOption_ShowNFConfigsInDFA = false;
	public static boolean internalOption_watchNFAConversion = false;


    public static void main(String[] args) {
		ErrorManager.info("ANTLR Parser Generator   Early Access Version " +
						  VERSION + " (?)  1989-2006");
		Tool antlr = new Tool(args);
		antlr.process();
		System.exit(0);
	}

	public Tool() {
	}

	public Tool(String[] args) {
		processArgs(args);
	}

	public void processArgs(String[] args) {
		if ( args==null || args.length==0 ) {
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
					File outDir = new File(outputDirectory);
					if( outDir.exists() && !outDir.isDirectory() ) {
						ErrorManager.error(ErrorManager.MSG_OUTPUT_DIR_IS_FILE,outputDirectory);
						libDirectory = ".";
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
			else if (args[i].equals("-nomemo")) {
				memo=false;
			}
			else if (args[i].equals("-print")) {
				printGrammar = true;
			}
			else if (args[i].equals("-Igrtree")) {
				internalOption_PrintGrammarTree=true; // print grammar tree
			}
			else if (args[i].equals("-Idfa")) {
				internalOption_PrintDFA=true;
			}
			else if (args[i].equals("-Inoprune")) {
				DFAOptimizer.PRUNE_EBNF_EXIT_BRANCHES=false;
			}
			else if (args[i].equals("-Inocollapse")) {
				DFAOptimizer.COLLAPSE_ALL_PARALLEL_EDGES=false;
			}
			else if (args[i].equals("-Idbgconversion")) {
				NFAToDFAConverter.debug = true;
			}
			else if (args[i].equals("-Imultithreaded")) {
				NFAToDFAConverter.SINGLE_THREADED_NFA_CONVERSION = false;
			}
			else if (args[i].equals("-Inomergestopstates")) {
				DFAOptimizer.MERGE_STOP_STATES = false;
			}
			else if (args[i].equals("-Idfaverbose")) {
				internalOption_ShowNFConfigsInDFA = true;
			}
			else if (args[i].equals("-Iwatchconversion")) {
				internalOption_watchNFAConversion = true;
			}
			else if (args[i].equals("-Im")) {
				if (i + 1 >= args.length) {
					System.err.println("missing max recursion with -Im option; ignoring");
				}
				else {
					i++;
					NFAContext.MAX_RECURSIVE_INVOCATIONS = Integer.parseInt(args[i]);
				}
			}
			else if (args[i].equals("-ImaxtimeforDFA")) {
				if (i + 1 >= args.length) {
					System.err.println("missing max time in ms -ImaxtimeforDFA option; ignoring");
				}
				else {
					i++;
					DFA.MAX_TIME_PER_DFA_CREATION = Integer.parseInt(args[i]);
				}
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

    public void process()  {
		for (int i = 0; i < grammarFileNames.size(); i++) {
			String grammarFileName = (String) grammarFileNames.get(i);
			try {
				//StringTemplate.setLintMode(true);
				FileReader fr = null;
				try {
					fr = new FileReader(grammarFileName);
				}
				catch (IOException ioe) {
					ErrorManager.error(ErrorManager.MSG_CANNOT_OPEN_FILE,
									   grammarFileName);
					continue;
				}
				BufferedReader br = new BufferedReader(fr);
				Grammar grammar = new Grammar(this,grammarFileName,br);
				grammar.setWatchNFAConversion(internalOption_watchNFAConversion);
				br.close();
				fr.close();

				processGrammar(grammar);

				if ( printGrammar ) {
					grammar.printGrammar(System.out);
				}

				// now handle the lexer if one was created for a merged spec

				String lexerGrammarStr = grammar.getLexerGrammar();
				if ( grammar.type==Grammar.COMBINED && lexerGrammarStr!=null ) {
					String lexerGrammarFileName = grammar.name+".lexer.g";
					Writer w = getOutputFile(grammar,lexerGrammarFileName);
					w.write(lexerGrammarStr);
					w.close();
					StringReader sr = new StringReader(lexerGrammarStr);
					Grammar lexerGrammar = new Grammar();
					lexerGrammar.setTool(this);
					File lexerGrammarFullFile =
						new File(grammar.getFileDirectory(),lexerGrammarFileName);
					lexerGrammar.setFileName(lexerGrammarFullFile.toString());
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
				if ( profile ) {
					GrammarReport report = new GrammarReport(grammar);
					GrammarReport.writeReport(GrammarReport.GRAMMAR_STATS_FILENAME,
											  report.toNotifyString());
				}
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
			generator.setProfile(profile);
			generator.setTrace(trace);
			generator.setMemoize(memo);

			/*
			if ( grammar.type==Grammar.LEXER ) {
				grammar.addArtificialMatchTokensRule();
			}
			*/

			generator.genRecognizer();
		}
	}

	protected void generateDFAs(Grammar g) {
		for (int d=1; d<=g.getNumberOfDecisions(); d++) {
			DFA dfa = g.getLookaheadDFA(d);
			if ( dfa==null ) {
				continue; // not there for some reason, ignore
			}
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
		System.err.println("  -print         print out the grammar without actions");
		System.err.println("  -debug         generate a parser that emits debugging events");
		System.err.println("  -profile       generate a parser that computes profiling information");
		System.err.println("  -nomemo        when backtracking don't generate memoization code");
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
	 *  The output dir -o spec takes precedence if it's absolute.
	 *  E.g., if the grammar file dir is absolute the output dir is given
	 *  precendence. "-o /tmp /usr/lib/t.g" results in "/tmp/T.java" as
	 *  output (assuming t.g holds T.java).
	 *
	 *  If no -o is specified, then just write to the directory where the
	 *  grammar file was found.
	 *
	 *  If outputDirectory==null then write a String.
     */
    public Writer getOutputFile(Grammar g, String fileName) throws IOException {
		if ( outputDirectory==null ) {
			return new StringWriter();
		}
		File outputDir = new File(outputDirectory);
		String fileDirectory = g.getFileDirectory();
		if ( outputDirectory!=UNINITIALIZED_DIR ) {
			// -o /tmp /var/lib/t.g => /tmp/T.java
			// -o subdir/output /usr/lib/t.g => subdir/output/T.java
			// -o . /usr/lib/t.g => ./T.java
			if ( fileDirectory!=null &&
				 (new File(fileDirectory).isAbsolute() ||
				  fileDirectory.startsWith("~")) ) // isAbsolute doesn't count this :(
			{
				// somebody set the dir, it takes precendence; write new file there
				outputDir = new File(outputDirectory);
			}
			else {
				// -o /tmp subdir/t.g => /tmp/subdir/t.g
				if ( fileDirectory!=null ) {
					outputDir = new File(outputDirectory, fileDirectory);
				}
				else {
					outputDir = new File(outputDirectory);
				}
			}
		}
		else {
			// they didn't specify a -o dir so just write to location
			// where grammar is, absolute or relative
			String dir = ".";
			if ( fileDirectory!=null ) {
				dir = fileDirectory;
			}
			outputDir = new File(dir);
		}

		if( !outputDir.exists() ) {
			outputDir.mkdirs();
		}
        FileWriter fw = new FileWriter(new File(outputDir, fileName));
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

	public String getLibraryDirectory() {
		return libDirectory;
	}

	/** If the tool needs to panic/exit, how do we do that? */
	public void panic() {
		throw new Error("ANTLR panic");
	}

	/** Return a time stamp string accurate to sec: yyyy-mm-dd hh:mm:ss */
	public static String getCurrentTimeStamp() {
		GregorianCalendar calendar = new java.util.GregorianCalendar();
		int y = calendar.get(Calendar.YEAR);
		int m = calendar.get(Calendar.MONTH)+1; // zero-based for months
		int d = calendar.get(Calendar.DAY_OF_MONTH);
		int h = calendar.get(Calendar.HOUR_OF_DAY);
		int min = calendar.get(Calendar.MINUTE);
		int sec = calendar.get(Calendar.SECOND);
		String sy = String.valueOf(y);
		String sm = m<10?"0"+m:String.valueOf(m);
		String sd = d<10?"0"+d:String.valueOf(d);
		String sh = h<10?"0"+h:String.valueOf(h);
		String smin = min<10?"0"+min:String.valueOf(min);
		String ssec = sec<10?"0"+sec:String.valueOf(sec);
		return new StringBuffer().append(sy).append("-").append(sm).append("-")
			.append(sd).append(" ").append(sh).append(":").append(smin)
			.append(":").append(ssec).toString();
	}

}
