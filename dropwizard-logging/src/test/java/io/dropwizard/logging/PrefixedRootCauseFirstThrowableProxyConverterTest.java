package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ThrowableProxy;
import com.google.common.base.Splitter;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

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

    private static void throwRoot() throws SocketTimeoutException {
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

    @Before
    public void setup() {
        converter.setOptionList(Collections.singletonList("full"));
        converter.start();
    }

    @Test
    public void prefixesExceptionsWithExclamationMarks()  {
        final List<String> stackTrace = Splitter.on(System.lineSeparator()).omitEmptyStrings()
                .splitToList(converter.throwableProxyToString(proxy));
        assertThat(stackTrace).isNotEmpty();
        for (String line : stackTrace) {
            assertThat(line).startsWith("!");
        }
    }

    @Test
    public void placesRootCauseIsFirst() {
        assertThat(converter.throwableProxyToString(proxy)).matches(Pattern.compile(".+" +
                "java\\.net\\.SocketTimeoutException: Timed-out reading from socket.+" +
                "java\\.io\\.IOException: Fairly general error doing some IO.+" +
                "java\\.lang\\.RuntimeException: Very general error doing something" +
                ".+", Pattern.DOTALL));
    }
}
