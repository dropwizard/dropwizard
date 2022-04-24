package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ThrowableProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.Objects;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link PrefixedRootCauseFirstThrowableProxyConverter}.
 */
class PrefixedRootCauseFirstThrowableProxyConverterTest {

    private final PrefixedRootCauseFirstThrowableProxyConverter converter
            = new PrefixedRootCauseFirstThrowableProxyConverter();

    private final ThrowableProxy proxy = new ThrowableProxy(getException());

    @Nullable
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

    @BeforeEach
    void setup() {
        converter.setOptionList(Collections.singletonList("full"));
        converter.start();
    }

    @Test
    void prefixesExceptionsWithExclamationMarks()  {
        assertThat(converter.throwableProxyToString(proxy).split("\\R"))
                .filteredOn(Objects::nonNull)
                .filteredOn(s -> !s.isEmpty())
                .isNotEmpty()
                .allSatisfy(line -> assertThat(line).startsWith("!"));
    }

    @Test
    void placesRootCauseIsFirst() {
        assertThat(converter.throwableProxyToString(proxy)).matches(Pattern.compile(".+" +
                "java\\.net\\.SocketTimeoutException: Timed-out reading from socket.+" +
                "java\\.io\\.IOException: Fairly general error doing some IO.+" +
                "java\\.lang\\.RuntimeException: Very general error doing something" +
                ".+", Pattern.DOTALL));
    }
}
