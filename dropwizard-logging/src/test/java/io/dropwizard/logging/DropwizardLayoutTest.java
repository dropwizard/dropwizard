package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DropwizardLayoutTest {
    private final LoggerContext context = mock(LoggerContext.class);
    private final TimeZone timeZone = TimeZone.getTimeZone("UTC");
    private final DropwizardLayout layout = new DropwizardLayout(context, timeZone);

    @Test
    void prefixesThrowables() throws Exception {
        assertThat(layout.getDefaultConverterMap().get("ex"))
                .isEqualTo(PrefixedThrowableProxyConverter.class.getName());
    }

    @Test
    void prefixesExtendedThrowables() throws Exception {
        assertThat(layout.getDefaultConverterMap().get("xEx"))
                .isEqualTo(PrefixedExtendedThrowableProxyConverter.class.getName());
    }

    @Test
    void hasAContext() throws Exception {
        assertThat(layout.getContext())
                .isEqualTo(context);
    }

    @Test
    void hasAPatternWithATimeZoneAndExtendedThrowables() throws Exception {
        assertThat(layout.getPattern())
                .isEqualTo("%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx");
    }
}
