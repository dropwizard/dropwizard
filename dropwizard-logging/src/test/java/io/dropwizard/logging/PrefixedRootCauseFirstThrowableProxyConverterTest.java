package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ThrowableProxy;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests {@link PrefixedRootCauseFirstThrowableProxyConverter}.
 */
public class PrefixedRootCauseFirstThrowableProxyConverterTest {

    private final PrefixedRootCauseFirstThrowableProxyConverter converter
            = new PrefixedRootCauseFirstThrowableProxyConverter();

    private final ThrowableProxy proxy = new ThrowableProxy(getException());

    private Exception getException() {
        try {
            throwOuterWrapper();
        } catch (Exception e) {
            return e;
        }

        return null; // unpossible, tell the type-system
    }

    private void throwRoot() throws SocketTimeoutException {
        throw new SocketTimeoutException("Timed-out reading from socket");
    }

    private void throwInnerWrapper() throws IOException {
        try {
            throwRoot();
        } catch (SocketTimeoutException ste) {
            throw new IOException("Fairly general error doing some IO", ste);
        }
    }

    private void throwOuterWrapper() {
        try {
            throwInnerWrapper();
        } catch (IOException e) {
            throw new RuntimeException("Very general error doing something", e);
        }
    }

    @Test
    public void prefixesExceptionsWithExclamationMarks() throws Exception {
        assertThat(converter.throwableProxyToString(proxy))
                .startsWith(String.format(
                        "! java.net.SocketTimeoutException: Timed-out reading from socket%n" +
                        "! at io.dropwizard.logging.PrefixedRootCauseFirstThrowableProxyConverterTest.throwRoot(PrefixedRootCauseFirstThrowableProxyConverterTest.java:32)%n" +
                        "! at io.dropwizard.logging.PrefixedRootCauseFirstThrowableProxyConverterTest.throwInnerWrapper(PrefixedRootCauseFirstThrowableProxyConverterTest.java:37)%n" +
                        "! Causing: java.io.IOException: Fairly general error doing some IO%n" +
                        "! at io.dropwizard.logging.PrefixedRootCauseFirstThrowableProxyConverterTest.throwInnerWrapper(PrefixedRootCauseFirstThrowableProxyConverterTest.java:39)%n" +
                        "! at io.dropwizard.logging.PrefixedRootCauseFirstThrowableProxyConverterTest.throwOuterWrapper(PrefixedRootCauseFirstThrowableProxyConverterTest.java:45)%n" +
                        "! Causing: java.lang.RuntimeException: Very general error doing something%n" +
                        "! at io.dropwizard.logging.PrefixedRootCauseFirstThrowableProxyConverterTest.throwOuterWrapper(PrefixedRootCauseFirstThrowableProxyConverterTest.java:47)%n" +
                        "! at io.dropwizard.logging.PrefixedRootCauseFirstThrowableProxyConverterTest.getException(PrefixedRootCauseFirstThrowableProxyConverterTest.java:23)%n" +
                        "! at io.dropwizard.logging.PrefixedRootCauseFirstThrowableProxyConverterTest.<init>(PrefixedRootCauseFirstThrowableProxyConverterTest.java:19)%n"));
    }

    /**
     * This test uses a regular expression to ensure that the final frame in the printed stack trace
     * is the "main" function.
     */
    @Test
    public void finalFrameIsMain() throws Exception {
        assertThat(converter.throwableProxyToString(proxy))
                .matches(String.format("^[\\s\\S]+! at \\S+\\.([^.]+)\\.main\\(\\1\\.java:\\d+\\)\\s*$"));
    }
}
