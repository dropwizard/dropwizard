package io.dropwizard.logging.json;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import io.dropwizard.logging.json.layout.ExceptionFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class EventJsonLayoutBaseFactoryTest {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    // Use unique spelling to guard against false positive matches
    private ThrowableProxy proxy = new ThrowableProxy(new RuntimeException("wrapp3d", new IOException("r00t")));
    private ILoggingEvent event = Mockito.mock(ILoggingEvent.class);

    @BeforeEach
    void before() {
        when(event.getThrowableProxy()).thenReturn(proxy);
    }

    @Test
    void testCreateThrowableProxyConverter_Configured() {
        ExceptionFormat format = new ExceptionFormat();
        format.setDepth("8");
        format.setRootFirst(false);
        String packageFilter = "org.junit";
        format.setEvaluators(Collections.singletonList(packageFilter));

        EventJsonLayoutBaseFactory factory = new EventJsonLayoutBaseFactory();
        factory.setExceptionFormat(format);

        ThrowableHandlingConverter converter = factory.createThrowableProxyConverter(new LoggerContext());
        converter.start();

        // Verify the original stack includes the excluded packages
        assertThat(proxy.getThrowable()).hasStackTraceContaining(packageFilter);

        String conversion = converter.convert(event);

        // Verify the conversion depth
        // 2 messages and 8 lines of stack per throwable
        assertThat(conversion).hasLineCount(18);

        // Verify that the root is not first
        assertThat(conversion).containsSubsequence("wrapp3d", "r00t");

        // Verify the conversion does not includes the excluded packages and contains skipped lines
        assertThat(conversion).doesNotContain(packageFilter);
        assertThat(conversion).containsPattern("\\[\\d+ skipped\\]");
    }

    @Test
    void testCreateThrowableProxyConverter_Default() throws Exception {
        EventJsonLayoutBaseFactory factory = new EventJsonLayoutBaseFactory();

        ThrowableHandlingConverter converter = factory.createThrowableProxyConverter(new LoggerContext());
        converter.start();

        String conversion = converter.convert(event);

        int originalSize = getStackTraceAsString(proxy.getThrowable()).split(LINE_SEPARATOR).length;

        // Verify that the full stack is included
        assertThat(conversion).hasLineCount(originalSize);

        // Verify that the root is first
        assertThat(conversion).containsSubsequence("r00t", "wrapp3d");
    }

    private static String getStackTraceAsString(Throwable throwable) throws IOException {
        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        }
    }
}
