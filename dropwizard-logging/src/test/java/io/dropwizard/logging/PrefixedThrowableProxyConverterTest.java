package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ThrowableProxy;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class PrefixedThrowableProxyConverterTest {
    private final PrefixedThrowableProxyConverter converter = new PrefixedThrowableProxyConverter();
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
                                                  "! at io.dropwizard.logging.PrefixedThrowableProxyConverterTest.<init>(PrefixedThrowableProxyConverterTest.java:14)%n"));
    }
}
