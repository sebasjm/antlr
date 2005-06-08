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
package org.antlr.tool;

import org.antlr.Tool;
import org.antlr.analysis.DecisionProbe;
import org.antlr.analysis.DFAState;
import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.StringTemplateErrorListener;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.lang.reflect.Field;

import antlr.LLkParser;
import antlr.Token;
import antlr.RecognitionException;

/** Defines all the errors ANTLR can generator for both the tool and for
 *  issues with a grammar.
 *
 *  Here is a list of language names:
 *
 *  http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt
 *
 *  Here is a list of country names:
 *
 *  http://www.chemie.fu-berlin.de/diverse/doc/ISO_3166.html
 *
 *  I use constants not strings to identify messages as the compiler will
 *  find any errors/mismatches rather than leaving a mistyped string in
 *  the code to be found randomly in the future.  Further, Intellij can
 *  do field name expansion to save me some typing.  I have to map
 *  int constants to template names, however, which could introduce a mismatch.
 *  Someone could provide a .stg file that had a template name wrong.  When
 *  I load the group, then, I must verify that all messages are there.
 *
 *  This is essentially the functionality of the resource bundle stuff Java
 *  has, but I don't want to load a property file--I want to load a template
 *  group file and this is so simple, why mess with their junk.
 *
 *  I use the default Locale as defined by java to compute a group file name
 *  in the org/antlr/tool/templates/messages dir called en_US.stg and so on.
 *
 *  Normally we want to use the default locale, but often a message file will
 *  not exist for it so we must fall back on the US local.
 *
 *  During initialization of this class, all errors go straight to System.err.
 *  There is no way around this.  If I have not set up the error system, how
 *  can I do errors properly?  For example, if the string template group file
 *  full of messages has an error, how could I print to anything but System.err?
 *
 *  TODO: how to map locale to a file encoding for the stringtemplate group file?
 *  StringTemplate knows how to pay attention to the default encoding so it
 *  should probably just work unless a GUI sets the local to some chinese
 *  variation but System.getProperty("file.encoding") is US.  Hmm...
 *
 *  TODO: get antlr.g etc.. parsing errors to come here.
 */
public class ErrorManager {
	// TOOL ERRORS
	// file errors
	public static final int MSG_CANNOT_WRITE_FILE = 1;
	public static final int MSG_CANNOT_CLOSE_FILE = 2;
	public static final int MSG_CANNOT_FIND_TOKENS_FILE = 3;
	public static final int MSG_ERROR_READING_TOKENS_FILE = 4;
	public static final int MSG_DIR_NOT_FOUND = 5;
	public static final int MSG_OUTPUT_DIR_IS_FILE = 6;

	public static final int MSG_INTERNAL_ERROR = 10;
	public static final int MSG_INTERNAL_WARNING = 11;
	public static final int MSG_ERROR_CREATING_ARTIFICIAL_RULE = 12;
	public static final int MSG_TOKENS_FILE_SYNTAX_ERROR = 13;
	public static final int MSG_CANNOT_GEN_DOT_FILE = 14;
	public static final int MSG_BAD_AST_STRUCTURE = 15;
	public static final int MSG_BAD_ACTION_AST_STRUCTURE = 16;

	// code gen errors
	public static final int MSG_MISSING_CODE_GEN_TEMPLATES = 20;
	public static final int MSG_MISSING_CYCLIC_DFA_CODE_GEN_TEMPLATES = 21;
	public static final int MSG_CODE_GEN_TEMPLATES_INCOMPLETE = 22;
	public static final int MSG_CANNOT_CREATE_TARGET_GENERATOR = 23;
	public static final int MSG_CANNOT_COMPUTE_SAMPLE_INPUT_SEQ = 24;

	// GRAMMAR ERRORS
	public static final int MSG_SYNTAX_ERROR = 100;
	public static final int MSG_RULE_REDEFINITION = 101;
	public static final int MSG_LEXER_RULES_NOT_ALLOWED = 102;
	public static final int MSG_PARSER_RULES_NOT_ALLOWED = 103;
	public static final int MSG_CANNOT_FIND_ATTRIBUTE_NAME_IN_DECL = 104;
	public static final int MSG_NO_TOKEN_DEFINITION = 105;
	public static final int MSG_UNDEFINED_RULE_REF = 106;
	public static final int MSG_LITERAL_NOT_ASSOCIATED_WITH_LEXER_RULE = 107;
	public static final int MSG_CANNOT_ALIAS_TOKENS_IN_LEXER = 108;
	public static final int MSG_ATTRIBUTE_REF_NOT_IN_RULE = 111;
	public static final int MSG_INVALID_RULE_SCOPE_ATTRIBUTE_REF = 112;
	public static final int MSG_UNKNOWN_ATTRIBUTE_IN_SCOPE = 113;
	public static final int MSG_UNKNOWN_SIMPLE_ATTRIBUTE = 114;
	public static final int MSG_INVALID_RULE_PARAMETER_REF = 115;
	public static final int MSG_UNKNOWN_RULE_ATTRIBUTE = 116;
	public static final int MSG_ISOLATED_RULE_ATTRIBUTE = 117;
	public static final int MSG_SYMBOL_CONFLICTS_WITH_GLOBAL_SCOPE = 118;
	public static final int MSG_LABEL_CONFLICTS_WITH_RULE = 119;
	public static final int MSG_LABEL_CONFLICTS_WITH_TOKEN = 120;
	public static final int MSG_LABEL_CONFLICTS_WITH_RULE_SCOPE_ATTRIBUTE = 121;
	public static final int MSG_LABEL_CONFLICTS_WITH_RULE_ARG_RETVAL = 122;
	public static final int MSG_ATTRIBUTE_CONFLICTS_WITH_RULE = 123;
	public static final int MSG_ATTRIBUTE_CONFLICTS_WITH_RULE_ARG_RETVAL = 124;
	public static final int MSG_LABEL_TYPE_CONFLICT = 125;
	public static final int MSG_ARG_RETVAL_CONFLICT = 126;
	public static final int MSG_NONUNIQUE_REF = 127;
	public static final int MSG_FORWARD_ELEMENT_REF = 128;
	public static final int MSG_MISSING_RULE_ARGS = 129;
	public static final int MSG_RULE_HAS_NO_ARGS = 130;
	public static final int MSG_ARGS_ON_TOKEN_REF = 131;
	//public static final int MSG_NONCHAR_RANGE = 132;
	public static final int MSG_ILLEGAL_OPTION = 133;


	// GRAMMAR WARNINGS
	public static final int MSG_GRAMMAR_NONDETERMINISM = 200; // A predicts alts 1,2
	public static final int MSG_UNREACHABLE_ALTS = 201;       // nothing predicts alt i
	public static final int MSG_DANGLING_STATE = 202;        // no edges out of state
	public static final int MSG_INSUFFICIENT_PREDICATES = 203;
	public static final int MSG_DUPLICATE_SET_ENTRY = 204;    // (A|A)
	public static final int MSG_ANALYSIS_ABORTED = 205;

	public static final int MAX_MESSAGE_NUMBER = 205;

	/** Messages should be sensitive to the locale. */
	private static Locale locale;

	/** Each thread might need it's own error listener; e.g., a GUI with
	 *  multiple window frames holding multiple grammars.
	 */
	private static Map threadToListenerMap = new HashMap();

	static class ErrorCount {
		public int errors;
		public int warnings;
		public int infos;
	}

	/** Track the number of errors regardless of the listener but track
	 *  per thread.
	 */
	private static Map threadToErrorCountMap = new HashMap();

	/** Each thread has its own ptr to a Tool object, which knows how
	 *  to panic, for example.  In a GUI, the thread might just throw an Error
	 *  to exit rather than the suicide System.exit.
	 */
	private static Map threadToToolMap = new HashMap();

	/** The group of templates that represent all possible ANTLR errors. */
	private static StringTemplateGroup messages;

	/** From a msgID how can I get the name of the template that describes
	 *  the error or warning?
	 */
	private static String[] idToMessageTemplateName = new String[MAX_MESSAGE_NUMBER+1];

	static ANTLRErrorListener theDefaultErrorListener = new ANTLRErrorListener() {
		public void info(String msg) {
			System.err.println(msg);
		}

		public void error(Message msg) {
			System.err.println(msg);
		}

		public void warning(Message msg) {
			System.err.println(msg);
		}

		public void error(ToolMessage msg) {
			System.err.println(msg);
		}
	};

	/** Handle all ST error listeners here (code gen, Grammar, and this class
	 *  use templates.
	 */
	static StringTemplateErrorListener initSTListener =
		new StringTemplateErrorListener() {
			public void error(String s, Throwable e) {
				System.err.println("ErrorManager init error: "+s);
				if ( e!=null ) {
					System.err.println("exception: "+e);
				}
				/*
				if ( e!=null ) {
					e.printStackTrace(System.err);
				}
				*/
			}
			public void warning(String s) {
				System.err.println("ErrorManager init warning: "+s);
			}
			public void debug(String s) {}
		};

	/** During verification of the messages group file, don't gen errors.
	 *  I'll handle them here.  This is used only after file has loaded ok
	 *  and only for the messages STG.
	 */
	static StringTemplateErrorListener blankSTListener =
		new StringTemplateErrorListener() {
			public void error(String s, Throwable e) {}
			public void warning(String s) {}
			public void debug(String s) {}
		};

	/** Errors during initialization related to ST must all go to System.err.
	 */
	static StringTemplateErrorListener theDefaultSTListener =
		new StringTemplateErrorListener() {
		public void error(String s, Throwable e) {
			ErrorManager.error(ErrorManager.MSG_INTERNAL_ERROR, s, e);
		}
		public void warning(String s) {
			ErrorManager.warning(ErrorManager.MSG_INTERNAL_WARNING, s);
		}
		public void debug(String s) {
		}
	};

	// make sure that this class is ready to use after loading
	static {
		initIdToMessageNameMapping();
		// it is inefficient to set the default locale here if another
		// piece of code is going to set the locale, but that would
		// require that a user call an init() function or something.  I prefer
		// that this class be ready to go when loaded as I'm absentminded ;)
		setLocale(Locale.getDefault());
	}

    public static StringTemplateErrorListener getStringTemplateErrorListener() {
		return theDefaultSTListener;
	}

	/** We really only need a single locale for entire running ANTLR code
	 *  in a single VM.  Only pay attention to the language, not the country
	 *  so that French Canadians and French Frenchies all get the same
	 *  template file, fr.stg.  Just easier this way.
	 */
	public static void setLocale(Locale locale) {
		ErrorManager.locale = locale;
		String language = locale.getLanguage();
		String fileName = "org/antlr/tool/templates/messages/"+language+".stg";
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		InputStream is = cl.getResourceAsStream(fileName);
		if ( is==null && language.equals(Locale.US.getLanguage()) ) {
			rawError("ANTLR installation corrupted; cannot find English messages file "+fileName);
			panic();
		}
		else if ( is==null ) {
			rawError("no such locale file "+fileName+" retrying with English locale");
			setLocale(Locale.US); // recurse on this rule, trying the US locale
			return;
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			messages = new StringTemplateGroup(br,
											   AngleBracketTemplateLexer.class,
											   initSTListener);
			br.close();
		}
		catch (IOException ioe) {
           	rawError("cannot close message file "+fileName, ioe);
		}

		messages.setErrorListener(blankSTListener);
		boolean messagesOK = verifyMessages();
		if ( !messagesOK && language.equals(Locale.US.getLanguage()) ) {
			rawError("ANTLR installation corrupted; English messages file "+language+".stg incomplete");
			panic();
		}
		else if ( !messagesOK ) {
			setLocale(Locale.US); // try US to see if that will work
		}
	}

	/** In general, you'll want all errors to go to a single spot.
	 *  However, in a GUI, you might have two frames up with two
	 *  different grammars.  Two threads might launch to process the
	 *  grammars--you would want errors to go to different objects
	 *  depending on the thread.  I store a single listener per
	 *  thread.
	 */
	public static void setErrorListener(ANTLRErrorListener listener) {
		threadToListenerMap.put(Thread.currentThread(), listener);
	}

	public static void setTool(Tool tool) {
		threadToToolMap.put(Thread.currentThread(), tool);
	}

	/** Given a message ID, return a StringTemplate that somebody can fill
	 *  with data.  We need to convert the int ID to the name of a template
	 *  in the messages ST group.
	 */
	public static StringTemplate getMessage(int msgID) {
        String msgName = idToMessageTemplateName[msgID];
		return messages.getInstanceOf(msgName);
	}

	public static ANTLRErrorListener getErrorListener() {
		ANTLRErrorListener el =
			(ANTLRErrorListener)threadToListenerMap.get(Thread.currentThread());
		if ( el==null ) {
			return theDefaultErrorListener;
		}
		return el;
	}

	public static ErrorCount getErrorCount() {
		ErrorCount ec =
			(ErrorCount)threadToErrorCountMap.get(Thread.currentThread());
		if ( ec==null ) {
			ec = new ErrorCount();
			threadToErrorCountMap.put(Thread.currentThread(), ec);
		}
		return ec;
	}

	public static void info(String msg) {
		getErrorCount().infos++;
		getErrorListener().info(msg);
	}

	public static void error(int msgID) {
		getErrorCount().errors++;
		getErrorListener().error(new ToolMessage(msgID));
	}

	public static void error(int msgID, Throwable e) {
		getErrorCount().errors++;
		getErrorListener().error(new ToolMessage(msgID,e));
	}

	public static void error(int msgID, Object arg) {
		getErrorCount().errors++;
		getErrorListener().error(new ToolMessage(msgID, arg));
	}

	public static void error(int msgID, Object arg, Object arg2) {
		getErrorCount().errors++;
		getErrorListener().error(new ToolMessage(msgID, arg, arg2));
	}

	public static void error(int msgID, Object arg, Throwable e) {
		getErrorCount().errors++;
		getErrorListener().error(new ToolMessage(msgID, arg, e));
	}

	public static void warning(int msgID, Object arg) {
		getErrorCount().warnings++;
		getErrorListener().warning(new ToolMessage(msgID, arg));
	}

	public static void nondeterminism(DecisionProbe probe,
									  DFAState d)
	{
		getErrorCount().warnings++;
		getErrorListener().warning(
			new GrammarNonDeterminismMessage(probe,d)
		);
	}

	public static void danglingState(DecisionProbe probe,
									 DFAState d)
	{
		getErrorCount().warnings++;
		getErrorListener().warning(
			new GrammarDanglingStateMessage(probe,d)
		);
	}

	public static void analysisAborted(DecisionProbe probe)
	{
		getErrorCount().warnings++;
		getErrorListener().warning(
			new GrammarAnalysisAbortedMessage(probe)
		);
	}

	public static void unreachableAlts(DecisionProbe probe,
									   List alts)
	{
		getErrorCount().warnings++;
		getErrorListener().warning(
			new GrammarUnreachableAltsMessage(probe,alts)
		);
	}

	public static void insufficientPredicates(DecisionProbe probe,
											  List alts)
	{
		getErrorCount().warnings++;
		getErrorListener().warning(
			new GrammarInsufficientPredicatesMessage(probe,alts)
		);
	}

	public static void grammarError(int msgID,
									Grammar g,
									Token token,
									Object arg,
									Object arg2)
	{
		getErrorCount().errors++;
		getErrorListener().error(
			new GrammarSemanticsMessage(msgID,g,token,arg,arg2)
		);
	}

	public static void grammarError(int msgID,
									Grammar g,
									Token token,
									Object arg)
	{
		grammarError(msgID,g,token,arg,null);
	}

	public static void grammarError(int msgID,
									Grammar g,
									Token token)
	{
		grammarError(msgID,g,token,null,null);
	}

	public static void syntaxError(int msgID,
								   Token token,
								   Object arg,
								   antlr.RecognitionException re)
	{
		getErrorCount().errors++;
		getErrorListener().error(
			new GrammarSyntaxMessage(msgID,token,arg,re)
		);
	}

	public static void internalError(Object error, Throwable e) {
		StackTraceElement location = getLastNonErrorManagerCodeLocation(e);
		String msg = "Exception "+e+"@"+location+": "+error;
		error(MSG_INTERNAL_ERROR, msg);
	}

	public static void internalError(Object error) {
		StackTraceElement location =
			getLastNonErrorManagerCodeLocation(new Exception());
		String msg = location+": "+error;
		error(MSG_INTERNAL_ERROR, msg);
	}

	/** Return first non ErrorManager code location for generating messages */
	private static StackTraceElement getLastNonErrorManagerCodeLocation(Throwable e) {
		StackTraceElement[] stack = e.getStackTrace();
		int i = 0;
		for (; i < stack.length; i++) {
			StackTraceElement t = stack[i];
			if ( t.toString().indexOf("ErrorManager")<0 ) {
				break;
			}
		}
		StackTraceElement location = stack[i];
		return location;
	}

	// A S S E R T I O N  C O D E

	public static void assertTrue(boolean condition, String message) {
		if ( !condition ) {
			internalError(message);
		}
	}

	// S U P P O R T  C O D E

	protected static boolean initIdToMessageNameMapping() {
		// make sure a message exists, even if it's just to indicate a problem
		for (int i = 0; i < idToMessageTemplateName.length; i++) {
			idToMessageTemplateName[i] = "INVALID MESSAGE ID: "+i;
		}
		// get list of fields and use it to fill in idToMessageTemplateName mapping
		Field[] fields = ErrorManager.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			String fieldName = f.getName();
			String templateName =
				fieldName.substring("MSG_".length(),fieldName.length());
			int msgID = 0;
			try {
				// get the constant value from this class object
				msgID = f.getInt(ErrorManager.class);
			}
			catch (IllegalAccessException iae) {
				System.err.println("cannot get const value for "+f.getName());
				continue;
			}
			if ( fieldName.startsWith("MSG_") ) {
                idToMessageTemplateName[msgID] = templateName;
			}
		}
		return true;
	}

	/** Use reflection to find list of MSG_ fields and then verify a
	 *  template exists for each one from the locale's group.
	 */
	protected static boolean verifyMessages() {
		boolean ok = true;
		Field[] fields = ErrorManager.class.getFields();
		for (int i = 0; i < fields.length; i++) {
			Field f = fields[i];
			String fieldName = f.getName();
			String templateName =
				fieldName.substring("MSG_".length(),fieldName.length());
			if ( fieldName.startsWith("MSG_") ) {
				if ( !messages.isDefined(templateName) ) {
					System.err.println("Message "+templateName+" in locale "+
									   locale+" not found");
					ok = false;
				}
			}
		}
		return ok;
	}

	/** If there are errors during ErrorManager init, we have no choice
	 *  but to go to System.err.
	 */
	static void rawError(String msg) {
		System.err.println(msg);
	}

	static void rawError(String msg, Throwable e) {
		System.err.println(msg);
		e.printStackTrace(System.err);
	}

	/** I *think* this will allow Tool subclasses to exit gracefully
	 *  for GUIs etc...
	 */
	public static void panic() {
		Tool tool = (Tool)threadToToolMap.get(Thread.currentThread());
		if ( tool==null ) {
			// no tool registered, exit
			System.exit(-1);
		}
		else {
			tool.panic();
		}
	}
}
