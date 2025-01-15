package io.dropwizard.util;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static io.dropwizard.util.Throwables.findThrowableInChain;
import static org.assertj.core.api.Assertions.assertThat;

class ThrowablesTest {
    @Test
    void findsNothingFromNull() {
        assertThat(findThrowableInChain(t -> true, null)).isEmpty();
    }

    @Test
    void findsSimpleException() {
        final RuntimeException e = new RuntimeException();

        assertThat(findThrowableInChain(t -> t instanceof RuntimeException, e)).contains(e);
        assertThat(findThrowableInChain(t -> false, e)).isEmpty();
    }

    @Test
    void findsChainedException() {
        final RuntimeException first = new RuntimeException("first");
        final RuntimeException second = new RuntimeException("second", first);
        final RuntimeException third = new RuntimeException("third", second);

        assertThat(findThrowableInChain(t -> "third".equals(t.getMessage()), third)).contains(third);
        assertThat(findThrowableInChain(t -> "second".equals(t.getMessage()), third)).contains(second);
        assertThat(findThrowableInChain(t -> "first".equals(t.getMessage()), third)).contains(first);
        assertThat(findThrowableInChain(t -> false, third)).isEmpty();
    }

    @Test
    void ignoresCircularChains() {
        // fifth -> fourth -> third -> second -> first
        //                      ^                  /
        //                       \-----------------
        final LateBoundCauseException first = new LateBoundCauseException("first");
        final RuntimeException second = new RuntimeException("second", first);
        final RuntimeException third = new RuntimeException("third", second);
        first.setCause(third);
        final RuntimeException fourth = new RuntimeException("fourth", third);
        final RuntimeException fifth = new RuntimeException("fifth", fourth);

        assertThat(findThrowableInChain(t -> "fifth".equals(t.getMessage()), fifth)).contains(fifth);
        assertThat(findThrowableInChain(t -> "fourth".equals(t.getMessage()), fifth)).contains(fourth);
        assertThat(findThrowableInChain(t -> "third".equals(t.getMessage()), fifth)).contains(third);
        assertThat(findThrowableInChain(t -> "second".equals(t.getMessage()), fifth)).contains(second);
        assertThat(findThrowableInChain(t -> "first".equals(t.getMessage()), fifth)).contains(first);
        assertThat(findThrowableInChain(t -> false, fifth)).isEmpty();

        // Starting in the loop
        assertThat(findThrowableInChain(t -> "third".equals(t.getMessage()), second)).contains(third);
        assertThat(findThrowableInChain(t -> "fourth".equals(t.getMessage()), second)).isEmpty();
    }

    /**
     * An Exception which allows the cause to be overridden after creation
     */
    private static class LateBoundCauseException extends RuntimeException {
        @Nullable
        private Throwable cause;

        LateBoundCauseException(@Nullable String message) {
            super(message);
        }

        void setCause(@Nullable Throwable cause) {
            this.cause = cause;
        }

        @Override
        @Nullable
        public Throwable getCause() {
            return cause;
        }
    }

}
