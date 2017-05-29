package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import org.junit.Test;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class DropwizardLayoutTest {
    private final LoggerContext context = mock(LoggerContext.class);
    private final TimeZone timeZone = TimeZone.getTimeZone("UTC");
    private final DropwizardLayout layout = new DropwizardLayout(context, timeZone);

    @Test
    public void prefixesThrowables() throws Exception {
        assertThat(layout.getDefaultConverterMap().get("ex"))
                .isEqualTo(PrefixedThrowableProxyConverter.class.getName());
    }

    @Test
    public void prefixesExtendedThrowables() throws Exception {
        assertThat(layout.getDefaultConverterMap().get("xEx"))
                .isEqualTo(PrefixedExtendedThrowableProxyConverter.class.getName());
    }

    @Test
    public void hasAContext() throws Exception {
        assertThat(layout.getContext())
                .isEqualTo(context);
    }

    @Test
    public void hasAPatternWithATimeZoneAndExtendedThrowables() throws Exception {
        assertThat(layout.getPattern())
                .isEqualTo("%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx");
    }

    @Test
    public void canOverrideDefaultPattern() throws Exception {
        DropwizardLayout.PatternFactory oldDefault = DropwizardLayout.getDefaultPattern();
        try {
            final String format = "DropwizardLayoutTest - overridden default format";
            DropwizardLayout.setDefaultPattern(
                new DropwizardLayout.PatternFactory() {
                    @Override public String build(TimeZone timeZone) {
                        return format;
                    }
                }
            );
            DropwizardLayout layout2 = new DropwizardLayout(context, timeZone);
            assertThat(layout2.getPattern())
                    .isEqualTo(format);
        } finally {
            DropwizardLayout.setDefaultPattern(oldDefault);
        }
    }
}
