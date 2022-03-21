package io.dropwizard.logging.common;

import ch.qos.logback.classic.spi.ThrowableProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixedThrowableProxyConverterTest {
    private final PrefixedThrowableProxyConverter converter = new PrefixedThrowableProxyConverter();
    private final ThrowableProxy proxy = new ThrowableProxy(new IOException("noo"));

    @BeforeEach
    void setup() {
        converter.setOptionList(Collections.singletonList("full"));
        converter.start();
    }

    @Test
    void prefixesExceptionsWithExclamationMarks() throws Exception {
        assertThat(converter.throwableProxyToString(proxy))
                .startsWith(String.format("! java.io.IOException: noo%n" +
                                                  "! at io.dropwizard.logging.common.PrefixedThrowableProxyConverterTest.<init>(PrefixedThrowableProxyConverterTest.java:14)%n"));
    }
}
