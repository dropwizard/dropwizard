package io.dropwizard.logging.common;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TruncatingThrowableHandlingConverterTest {
    private static final int CHAR_LIMIT = 200; // kept small to make it simpler to test the limit boundaries

    private ILoggingEvent event;
    private PrefixedThrowableProxyConverter delegateConverter;
    private TruncatingThrowableHandlingConverter converter;

    @BeforeEach
    void setUp() {
        delegateConverter = Mockito.spy(new PrefixedThrowableProxyConverter());
        converter = new TruncatingThrowableHandlingConverter(delegateConverter, CHAR_LIMIT);
        converter.start();
        event = Mockito.mock(ILoggingEvent.class);
    }

    @Test
    void truncatesExceptionsThatAreTooLong() throws Exception {
        ThrowableProxy proxy = new ThrowableProxy(new Exception("message"));
        when(event.getThrowableProxy()).thenReturn(proxy);
        // uses a regex pattern to match the line number in the exception stack trace
        Pattern expected = Pattern.compile(
                "! java.lang.Exception: message\n"
                + "! at io.dropwizard.logging.common.TruncatingThrowableHandlingConverterTest.truncatesExceptionsThatAreTooLong\\(TruncatingThrowableHandlingConverterTest.java:\\d+\\)\r?\n"
                + "! at \\w+…"
            );

        String actual = converter.convert(event);

        assertThat(actual).matches(expected);
        assertThat(actual.length()).isLessThanOrEqualTo(CHAR_LIMIT);
        assertThat(actual).endsWith("…");
    }

    @Test
    void passesThroughExceptionsThatAreBelowTheLimit() throws Exception {
        String message = "message";
        when(event.getThrowableProxy()).thenReturn(new ThrowableProxyVO());
        when(delegateConverter.throwableProxyToString(any())).thenReturn(message);

        String actual = converter.convert(event);

        assertThat(actual).isEqualTo(message);
    }

    @Test
    void returnsATruncatedExceptionWithSplitOnAUnicodeCharacter() {
        int characterLimit = 15;
        String givenMessage = "test error - 😁message";
        String expectedMessage = "test error - …";
        when(event.getThrowableProxy()).thenReturn(new ThrowableProxyVO());
        when(delegateConverter.throwableProxyToString(any())).thenReturn(givenMessage);
        converter = new TruncatingThrowableHandlingConverter(delegateConverter, characterLimit);
        converter.start();

        String actual = converter.convert(event);

        assertThat(actual).isEqualTo(expectedMessage);
    }
}
