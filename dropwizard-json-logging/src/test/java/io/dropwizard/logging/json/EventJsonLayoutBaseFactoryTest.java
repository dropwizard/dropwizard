package io.dropwizard.logging.json;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.pattern.ThrowableHandlingConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.google.common.base.Throwables;
import io.dropwizard.logging.json.layout.ExceptionFormat;
import java.io.IOException;
import java.util.Collections;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class EventJsonLayoutBaseFactoryTest {

    private static final String LINE_SEPERATOR = System.getProperty("line.separator");
    // Use unique spelling to guard against false positive matches
    private ThrowableProxy proxy = new ThrowableProxy(new RuntimeException("wrapp3d", new IOException("r00t")));
    private String packageFilter = "org.junit";
    private ILoggingEvent event = Mockito.mock(ILoggingEvent.class);

    @Before
    public void before() {
        when(event.getThrowableProxy()).thenReturn(proxy);
    }

    @Test
    public void testCreateThrowableProxyConverter_Configured() {
        ExceptionFormat format = new ExceptionFormat();
        format.setDepth("8");
        format.setRootFirst(false);
        format.setEvaluators(Collections.singletonList(packageFilter));

        EventJsonLayoutBaseFactory factory = new EventJsonLayoutBaseFactory();
        factory.setExceptionFormat(format);

        ThrowableHandlingConverter converter = factory.createThrowableProxyConverter(new LoggerContext());
        converter.start();

        // Verify the original stack includes the excluded packages
        assertThat(Throwables.getStackTraceAsString(proxy.getThrowable())).contains(packageFilter);

        String conversion = converter.convert(event);

        // Verify the conversion depth
        // 2 messages and 8 lines of stack per throwable
        assertThat(conversion.split(LINE_SEPERATOR)).hasSize(18);

        // Verify that the root is not first
        assertThat(conversion).containsPattern(Pattern.compile("wrapp3d.*r00t", Pattern.DOTALL));

        // Verify the conversion does not includes the excluded packages and contains skipped lines
        assertThat(conversion).doesNotContain(packageFilter);
        assertThat(conversion).containsPattern(Pattern.compile("\\[\\d+ skipped\\]"));
    }

    @Test
    public void testCreateThrowableProxyConverter_Default() {
        EventJsonLayoutBaseFactory factory = new EventJsonLayoutBaseFactory();

        ThrowableHandlingConverter converter = factory.createThrowableProxyConverter(new LoggerContext());
        converter.start();

        String conversion = converter.convert(event);

        int originalSize = Throwables.getStackTraceAsString(proxy.getThrowable()).split(LINE_SEPERATOR).length;

        // Verify that the full stack is included
        assertThat(conversion.split(LINE_SEPERATOR)).hasSize(originalSize);

        // Verify that the root is first
        assertThat(conversion).containsPattern(Pattern.compile("r00t.*wrapp3d", Pattern.DOTALL));
    }
}
