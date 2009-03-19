package org.antlr.gunit.swingui.parsers;

import org.antlr.gunit.swingui.model.*;

/**
 * Adapter class for gunit parser to save information into testsuite object.
 * @author Shaoting
 */
public class TestSuiteAdapter {

    private TestSuite model ;
    private Rule currentRule;

    public TestSuiteAdapter(TestSuite testSuite) {
        model = testSuite;
    }

    public void setGrammarName(String name) {
        model.setGrammarName(name);
    }

    public void startRule(String name) {
        currentRule = new Rule(name);
    }

    public void endRule() {
        model.addRule(currentRule);
        currentRule = null;
    }

    public void addTestCase(ITestCaseInput in, ITestCaseOutput out) {
        TestCase testCase = new TestCase(in, out);
        currentRule.addTestCase(testCase);
    }

    private static String trimChars(String text, int numOfChars) {
        return text.substring(numOfChars, text.length() - numOfChars);
    }

    public static ITestCaseInput createFileInput(String fileName) {
        if(fileName == null) throw new IllegalArgumentException("null");
        return new TestCaseInputFile(fileName);
    }

    public static ITestCaseInput createStringInput(String line) {
        if(line == null) throw new IllegalArgumentException("null");
        // trim double quotes
        return new TestCaseInputString(trimChars(line, 1));
    }

    public static ITestCaseInput createMultiInput(String text) {
        if(text == null) throw new IllegalArgumentException("null");
        // trim << and >>
        return new TestCaseInputMultiString(trimChars(text, 2));
    }

    public static ITestCaseOutput createBoolOutput(boolean bool) {
        return new TestCaseOutputResult(bool);
    }

    public static ITestCaseOutput createAstOutput(String ast) {
        if(ast == null) throw new IllegalArgumentException("null");
        return new TestCaseOutputAST(ast);
    }

    public static ITestCaseOutput createStdOutput(String text) {
        if(text == null) throw new IllegalArgumentException("null");
        // trim double quotes
        return new TestCaseOutputStdOut(trimChars(text, 1));
    }

    public static ITestCaseOutput createReturnOutput(String text) {
        if(text == null) throw new IllegalArgumentException("null");
        // trim square brackets
        return new TestCaseOutputReturn(trimChars(text, 1));
    }    
}
