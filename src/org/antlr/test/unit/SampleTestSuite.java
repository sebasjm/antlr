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
