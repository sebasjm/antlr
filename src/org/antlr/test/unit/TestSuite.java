package org.antlr.test.unit;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/** A TestSuite is a set of tests that the TestRig can invoke.  If you
 *  define a runTests() method in a subclass then TestRig will call that.
 *  If you don't, TestRig will run any methods in your suite that begin with
 *  prefix "test".
 */
public abstract class TestSuite {
    public String testName = null;

    int failures = 0, successes=0;

    public void assertTrue(boolean test) throws FailedAssertionException {
        assertTrue(test, null);
    }

    public void assertEqual(Object result, Object expecting) throws FailedAssertionException {
        if ( result==null && expecting!=null ) {
            throw new FailedAssertionException("expecting \""+expecting+"\"; found null");
        }
        assertTrue(result.equals(expecting), "expecting \""+expecting+"\"; found \""+result+"\"");
    }

    public void assertEqual(int result, int expecting) throws FailedAssertionException {
        assertTrue(result==expecting,
                "expecting \""+expecting+"\"; found \""+result+"\"");
    }

    public void assertTrue(boolean test, String message) throws FailedAssertionException {
        if ( !test ) {
            if ( message!=null ) {
                throw new FailedAssertionException(message);
            }
            else {
                throw new FailedAssertionException("assertTrue failed");
            }
        }
    }

    public void time(String name, int n) throws Exception {
        System.gc();
        long start = System.currentTimeMillis();
        System.out.print("TIME: "+name);
        for (int i=1; i<=n; i++) {
            invokeTest(name);
        }
        long finish = System.currentTimeMillis();
        long t = (finish-start);
        System.out.println("; n="+n+" "+t+"ms ("+((((double)t)/n)*1000.0)+" microsec/eval)");
    }

    public void runTest(String name) {
        try {
            System.out.println("TEST: "+name);
            invokeTest(name);
            successes++;
        }
        catch (InvocationTargetException ite) {
            failures++;
            try {
                throw ite.getCause();
            }
            catch (FailedAssertionException fae) {
                System.err.println(name+" failed: "+fae.getMessage());
            }
            catch (Throwable e) {
                System.err.print("exception during test "+name+":");
                e.printStackTrace();
            }
        }
    }

    public void invokeTest(String name)
            throws InvocationTargetException
    {
        testName = name;
        try {
            Class c = this.getClass();
            Method m = c.getMethod(name,null);
            m.invoke(this,null);
        }
        catch (IllegalAccessException iae) {
            System.err.println("no permission to exec test "+name);
        }
        catch (NoSuchMethodException nsme) {
            System.err.println("no such test "+name);
        }
    }

    public int getFailures() {
        return failures;
    }

    public int getSuccesses() {
        return successes;
    }

}
