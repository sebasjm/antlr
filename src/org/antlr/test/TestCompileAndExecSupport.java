package org.antlr.test;

import org.antlr.tool.Grammar;
import org.antlr.Tool;
import org.antlr.stringtemplate.StringTemplate;

import java.io.*;

public class TestCompileAndExecSupport {
	public static final String jikes = "/usr/bin/jikes";
	public static final String pathSep = System.getProperty("path.separator");
	public static final String CLASSPATH = System.getProperty("java.class.path");
	public static final String tmpdir = System.getProperty("java.io.tmpdir");

	protected static boolean compile(String fileNames) {
		String compiler = "javac";
		String classpathOption = "-classpath";

		if (jikes!=null) {
			compiler = jikes;
			classpathOption = "-bootclasspath";
		}

		String cmdLine = compiler+" -d "+tmpdir+" "+classpathOption+" "+tmpdir+pathSep+CLASSPATH+" "+fileNames;
		//System.out.println("compile: "+cmdLine);
		File outputDir = new File(tmpdir);
		try {
			Process process =
				Runtime.getRuntime().exec(cmdLine , null, outputDir);
			StreamVacuum stdout = new StreamVacuum(process.getInputStream());
			StreamVacuum stderr = new StreamVacuum(process.getErrorStream());
			stdout.start();
			stderr.start();
			process.waitFor();
			if ( stdout.toString().length()>0 ) {
				System.err.println("compile stderr from: "+cmdLine);
				System.err.println(stdout);
			}
			if ( stderr.toString().length()>0 ) {
				System.err.println("compile stderr from: "+cmdLine);
				System.err.println(stderr);
			}
			int ret = process.exitValue();
			return ret==0;
		}
		catch (Exception e) {
			System.err.println("can't exec compilation");
			e.printStackTrace(System.err);
			return false;
		}
	}

	public static void antlr(String fileName, String grammarStr) {
		writeFile(tmpdir, fileName, grammarStr);
		try {
			Grammar g = new Grammar(grammarStr);
			Tool antlr = new Tool(
				new String[] {"-o",
							  tmpdir,
							  new File(tmpdir,g.name+".g").toString()}
			);
			antlr.process();
		}
		catch (Exception e) {
			System.err.println("problems building grammar: "+e);
			e.printStackTrace(System.err);
		}
	}

	public static String execParser(String grammarFileName,
									String grammarStr,
									String parserName,
									String lexerName,
									String startRuleName,
									String input)
	{
		eraseFiles(".class");
		antlr(grammarFileName, grammarStr);
		compile(parserName+".java "+lexerName+".java");
		writeFile(tmpdir, "input", input);
		writeTestFile(parserName, lexerName, startRuleName);
		compile("Test.java");
		try {
			// wow...took me an hour to figure out that stupid java wants
			// the /tmp dir after the CLASSPATH; wouldn't run! :(
			String cmdLine = "java -classpath "+CLASSPATH+pathSep+tmpdir+" Test /tmp/input";
			//System.out.println("execParser: "+cmdLine);
			Process process =
				Runtime.getRuntime().exec(cmdLine, null, new File(tmpdir));
			StreamVacuum stdout = new StreamVacuum(process.getInputStream());
			StreamVacuum stderr = new StreamVacuum(process.getErrorStream());
			stdout.start();
			stderr.start();
			process.waitFor();
			String output = null;
			output = stdout.toString();
			if ( stderr.toString().length()>0 ) {
				System.err.println("exec parser stderr: "+stderr);
			}
			return output;
		}
		catch (Exception e) {
			System.err.println("can't exec parser");
			e.printStackTrace(System.err);
		}
		return null;
	}

	public static class StreamVacuum implements Runnable {
		StringBuffer buf = new StringBuffer();
		BufferedReader in;
		public StreamVacuum(InputStream in) {
			this.in = new BufferedReader( new InputStreamReader(in) );
		}
		public void start() {
			new Thread(this).start();
		}
		public void run() {
			try {
				String line = in.readLine();
				while (line!=null) {
					buf.append(line);
					buf.append('\n');
					line = in.readLine();
				}
			}
			catch (IOException ioe) {
				System.err.println("can't read output from process");
			}
		}
		public String toString() {
			return buf.toString();
		}
	}

	public static void writeFile(String dir, String fileName, String content) {
		try {
			File f = new File(dir, fileName);
			FileWriter w = new FileWriter(f);
			BufferedWriter bw = new BufferedWriter(w);
			bw.write(content);
			bw.close();
			w.close();
		}
		catch (IOException ioe) {
			System.err.println("can't write file");
			ioe.printStackTrace(System.err);
		}
	}

	public static void writeTestFile(String parserName, String lexerName, String startRuleName) {
		StringTemplate outputFileST = new StringTemplate(
			"import org.antlr.runtime.*;\n" +
			"import org.antlr.runtime.tree.*;\n" +
			"\n" +
			"public class Test {\n" +
			"        public static void main(String[] args) throws Exception {\n" +
			"                CharStream input = new ANTLRFileStream(args[0]);\n" +
			"                $lexerName$ lex = new $lexerName$(input);\n" +
			"                CommonTokenStream tokens = new CommonTokenStream(lex);\n" +
			"                $parserName$ parser = new $parserName$(tokens);\n" +
			"                $parserName$.$startRuleName$_return r = parser.$startRuleName$();\n" +
			"                if ( r.tree!=null )\n" +
			"                    System.out.println(((CommonTree)r.tree).toStringTree());\n" +
			"        }\n" +
			"}"
			);
		outputFileST.setAttribute("parserName", parserName);
		outputFileST.setAttribute("lexerName", lexerName);
		outputFileST.setAttribute("startRuleName", startRuleName);
		writeFile(tmpdir, "Test.java", outputFileST.toString());
	}

	public static void eraseFiles(final String filesEndingWith) {
		File tmpdirF = new File(tmpdir);
		String[] files = tmpdirF.list(
			new FilenameFilter() {
				public boolean accept(java.io.File f, String name) {
					return f.getName().endsWith(filesEndingWith);
				}
			}
		);
		for(int i = 0; i < files.length; i++) {
        	new File(files[i]).delete();
		}
	}

}
