package org.antlr.test.unit;

/** Example usage of my jUnit lookalike testing rig.  You can
 *  test this with
 *
 *  $ java org.pageforge.unit.TestRig org.pageforge.unit.SampleTestSuite
 *
 *  The output should be:
FAILED: testThatFails
TRAPPED EXCEPTION: testThatTrapsException
java.lang.NullPointerException
        at org.pageforge.unit.SampleTestSuite.testThatTrapsException(SampleTestSuite.java:30)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
        at java.lang.reflect.Method.invoke(Method.java:324)
        at org.pageforge.unit.TestSuite.runTest(TestSuite.java:30)
        at org.pageforge.unit.SampleTestSuite.runTests(SampleTestSuite.java:18)
        at org.pageforge.unit.TestRig.main(TestRig.java:34)

successes: 1
failures: 2
 */
public class SampleTestSuite extends TestSuite {

    /** Public default constructor used by TestRig */
    public SampleTestSuite() {
    }

    public void runTests() throws Exception {
        runTest("testThatSucceeds");
        runTest("testThatFails");
        runTest("testThatTrapsException");
	}

    public void testThatSucceeds() throws Exception {
        assertTrue(true);
    }

    public void testThatFails() throws Exception {
        assertTrue(false, "I told it to fail");
    }

    public void testThatTrapsException() throws Exception {
        throw new NullPointerException();
    }

}
