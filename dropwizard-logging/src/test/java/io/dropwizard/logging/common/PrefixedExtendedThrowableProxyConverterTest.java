package io.dropwizard.logging.common;

import ch.qos.logback.classic.spi.ThrowableProxy;
import io.dropwizard.logging.common.PrefixedExtendedThrowableProxyConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class PrefixedExtendedThrowableProxyConverterTest {
    private final PrefixedExtendedThrowableProxyConverter converter = new PrefixedExtendedThrowableProxyConverter();
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
                                                  "! at io.dropwizard.logging.common.PrefixedExtendedThrowableProxyConverterTest.<init>(PrefixedExtendedThrowableProxyConverterTest.java:14)%n"));
    }
}
