package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class DropwizardLayoutTest {
    private final LoggerContext context = mock(LoggerContext.class);
    private final TimeZone timeZone = TimeZone.getTimeZone("UTC");
    private final DropwizardLayout layout = new DropwizardLayout(context, timeZone);

    @Test
    void prefixesThrowables() throws Exception {
        assertThat(layout.getDefaultConverterMap())
                .containsEntry("ex", PrefixedThrowableProxyConverter.class.getName());
        assertThat(layout.getDefaultConverterMap())
                .containsEntry("exception", PrefixedThrowableProxyConverter.class.getName());
        assertThat(layout.getDefaultConverterMap())
                .containsEntry("throwable", PrefixedThrowableProxyConverter.class.getName());
    }

    @Test
    void prefixesExtendedThrowables() throws Exception {
        assertThat(layout.getDefaultConverterMap())
                .containsEntry("xEx", PrefixedExtendedThrowableProxyConverter.class.getName());
        assertThat(layout.getDefaultConverterMap())
                .containsEntry("xException", PrefixedExtendedThrowableProxyConverter.class.getName());
        assertThat(layout.getDefaultConverterMap())
                .containsEntry("xThrowable", PrefixedExtendedThrowableProxyConverter.class.getName());
    }

    @Test
    void prefixesRootThrowables() throws Exception {
        assertThat(layout.getDefaultConverterMap())
                .containsEntry("rEx", PrefixedRootCauseFirstThrowableProxyConverter.class.getName());
        assertThat(layout.getDefaultConverterMap())
                .containsEntry("rootException", PrefixedRootCauseFirstThrowableProxyConverter.class.getName());
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
