package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ThrowableProxy;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;

public class PrefixedExtendedThrowableProxyConverterTest {
    private final PrefixedExtendedThrowableProxyConverter converter = new PrefixedExtendedThrowableProxyConverter();
    private final ThrowableProxy proxy = new ThrowableProxy(new IOException("noo"));

    @Before
    public void setup() {
        converter.setOptionList(Lists.newArrayList("full"));
        converter.start();
    }

    @Test
    public void prefixesExceptionsWithExclamationMarks() throws Exception {
        assertThat(converter.throwableProxyToString(proxy))
                .startsWith(String.format("! java.io.IOException: noo%n" +
                                                  "! at io.dropwizard.logging.PrefixedExtendedThrowableProxyConverterTest.<init>(PrefixedExtendedThrowableProxyConverterTest.java:14)%n"));
    }
}
