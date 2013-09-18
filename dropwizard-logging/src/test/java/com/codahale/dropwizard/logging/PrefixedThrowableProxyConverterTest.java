package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.spi.ThrowableProxy;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

public class PrefixedThrowableProxyConverterTest {
    private final PrefixedThrowableProxyConverter converter = new PrefixedThrowableProxyConverter();
    private final ThrowableProxy proxy = new ThrowableProxy(new IOException("noo"));

    @Test
    public void prefixesExceptionsWithExclamationMarks() throws Exception {
        assertThat(converter.throwableProxyToString(proxy))
                .startsWith(String.format("! java.io.IOException: noo%n" +
                                                  "! at com.codahale.dropwizard.logging.PrefixedThrowableProxyConverterTest.<init>(PrefixedThrowableProxyConverterTest.java:12)%n"));
    }
}
