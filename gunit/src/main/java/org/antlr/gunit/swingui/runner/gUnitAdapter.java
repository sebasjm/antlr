/**
 * 
 */
package org.antlr.gunit.swingui.runner;

import java.io.File;
import java.io.IOException;
import org.antlr.runtime.*;
import org.antlr.runtime.CharStream;
import org.antlr.gunit.*;
import org.antlr.gunit.swingui.model.TestSuite;

/**
 * Adapter between gUnitEditor Swing GUI and gUnit command-line tool.
 * @author scai
 */
public class gUnitAdapter {

    private ParserLoader loader ;
    private TestSuite testSuite;

    public gUnitAdapter(TestSuite suite) throws IOException {
        int i = 3;
        loader = new ParserLoader(suite.getGrammarName(), 
                                  suite.getTestSuiteFile().getParent());
        testSuite = suite;
    }

    public void run() {
        if (testSuite == null)
            throw new IllegalArgumentException("Null testsuite.");
        
        
        try {

            // Parse gUnit test suite file
            final CharStream input = new ANTLRFileStream(testSuite.getTestSuiteFile().getCanonicalPath());
            final gUnitLexer lexer = new gUnitLexer(input);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);
            final GrammarInfo grammarInfo = new GrammarInfo();
            final gUnitParser parser = new gUnitParser(tokens, grammarInfo);
            parser.gUnitDef();	// parse gunit script and save elements to grammarInfo

            // Execute test suite
            final gUnitExecutor executer = new NotifiedTestExecuter(
                    grammarInfo, loader, 
                    testSuite.getTestSuiteFile().getParent(), testSuite);
            executer.execTest();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
