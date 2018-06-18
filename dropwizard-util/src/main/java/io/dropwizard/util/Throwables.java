package io.dropwizard.util;

public final class Throwables {
    private Throwables() {
    }

    /**
     * Returns the innermost cause of {@code throwable}. The first throwable in a chain provides
     * context from when the error or exception was initially detected. Example usage:
     *
     * <pre>
     * assertEquals("Unable to assign a customer id", Throwables.getRootCause(e).getMessage());
     * </pre>
     *
     * @throws IllegalArgumentException if there is a loop in the causal chain
     */
    public static Throwable getRootCause(Throwable throwable) {
        // Keep a second pointer that slowly walks the causal chain. If the fast pointer ever catches
        // the slower pointer, then there's a loop.
        Throwable slowPointer = throwable;
        boolean advanceSlowPointer = false;

        Throwable cause;
        while ((cause = throwable.getCause()) != null) {
            throwable = cause;

            if (throwable == slowPointer) {
                throw new IllegalArgumentException("Loop in causal chain detected.", throwable);
            }
            if (advanceSlowPointer) {
                slowPointer = slowPointer.getCause();
            }
            advanceSlowPointer = !advanceSlowPointer; // only advance every other iteration
        }
        return throwable;
    }
}
