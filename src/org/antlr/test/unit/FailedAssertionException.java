package org.antlr.test.unit;

public class FailedAssertionException extends Exception {
    public FailedAssertionException() {
    }

    public FailedAssertionException(String msg) {
        super(msg);
    }
}
