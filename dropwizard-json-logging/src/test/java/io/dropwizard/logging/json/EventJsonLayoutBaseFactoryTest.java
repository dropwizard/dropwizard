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
    // Use unique spelling to guard against false positive matches
    private final ThrowableProxy proxy = new ThrowableProxy(new RuntimeException("wrapp3d", new IOException("r00t")));
    private final ILoggingEvent event = Mockito.mock(ILoggingEvent.class);

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

        assertThat(converter.isStarted()).isTrue();

        // Verify the original stack includes the excluded packages
        assertThat(proxy.getThrowable()).hasStackTraceContaining(packageFilter);

        // Verify the conversion depth
        assertThat(converter.convert(event))
            .hasLineCount(18) // 2 messages and 8 lines of stack per throwable
            .containsSubsequence("wrapp3d", "r00t") // the root is not first
            .doesNotContain(packageFilter) // does not include the excluded packages
            .containsPattern("\\[\\d+ skipped\\]"); // and contains no skipped lines
    }

    @Test
    void testCreateThrowableProxyConverter_Default() throws Exception {
        EventJsonLayoutBaseFactory factory = new EventJsonLayoutBaseFactory();

        ThrowableHandlingConverter converter = factory.createThrowableProxyConverter(new LoggerContext());
        converter.start();

        assertThat(converter.isStarted()).isTrue();

        int originalSize = (int)getStackTraceAsString(proxy.getThrowable()).lines().count();

        assertThat(converter.convert(event))
            .hasLineCount(originalSize) // Verify that the full stack is included
            .containsSubsequence("r00t", "wrapp3d"); // Verify that the root is first
    }

    private static String getStackTraceAsString(Throwable throwable) throws IOException {
        try (StringWriter stringWriter = new StringWriter();
             PrintWriter printWriter = new PrintWriter(stringWriter)) {
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        }
    }
}
