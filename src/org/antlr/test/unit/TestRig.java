package org.antlr.test.unit;

import java.lang.reflect.Method;

/** Terence's own version of a test case.  Got sick of trying to figure out
 *  the quirks of junit...this is pretty much the same functionality and
 *  only took me a few minutes to write.  Only missing the gui I guess from junit.
 *
 *  This class is the main testing rig.  It executes all the tests within a
 *  TestSuite.  Invoke like this:
 *
 *  $ java org.antlr.test.unit.TestRig yourTestSuiteClassName {any-testMethod}
 *
 *  $ java org.antlr.test.unit.TestRig org.antlr.test.TestIntervalSet
 *
 *  $ java org.antlr.test.unit.TestRig org.antlr.test.TestIntervalSet testNotSet
 *
 *  Another benefit to building my own test rig is that users of ANTLR or any
 *  of my other software don't have to download yet another package to make
 *  this code work.  Reducing library dependencies is good.  Also, I can make
 *  this TestRig specific to my needs, hence, making me more productive.
 */
public class TestRig {
    protected Class testCaseClass = null;

    /** Testing program */
    public static void main(String[] args) throws Exception {
        if ( args.length==0 ) {
            System.err.println("Please pass in a test to run; must be class with runTests() method");
        }
        String className = args[0];
        TestSuite test = null;
        try {
            Class c;
            try {
                c = Class.forName(className);
                test = (TestSuite)c.newInstance();
            }
            catch (Exception e) {
                System.out.println("Cannot load class: "+className);
                e.printStackTrace();
                return;
            }
			if ( args.length>1 ) {
				// run the specific test
				String testName = args[1];
				test.runTest(testName);
			}
            else {
				// run them all
                // if they define a runTests, just call it
                Method m;
                try {
                    m = c.getMethod("runTests",null);
                    m.invoke(test, null);
                }
                catch (NoSuchMethodException nsme) {
                    // else just call runTest on all methods with "test" prefix
                    Method methods[] = c.getMethods();
                    for (int i = 0; i < methods.length; i++) {
                        Method testMethod = methods[i];
                        if ( testMethod.getName().startsWith("test") ) {
                            test.runTest(testMethod.getName());
                        }
                    }
                }
			}
        }
        catch (Exception e) {
            System.out.println("Exception during test "+test.testName);
            e.printStackTrace();
        }
        System.out.println();
        System.out.println("successes: "+test.getSuccesses());
        System.out.println("failures: "+test.getFailures());
    }
}
