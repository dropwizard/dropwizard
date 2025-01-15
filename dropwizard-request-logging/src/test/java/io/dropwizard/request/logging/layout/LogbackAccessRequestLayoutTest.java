package io.dropwizard.request.logging.layout;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Context;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LogbackAccessRequestLayoutTest {
    final Context context = mock(LoggerContext.class);
    private final TimeZone timeZone = TimeZone.getTimeZone("UTC");
    final LogbackAccessRequestLayout layout = new LogbackAccessRequestLayout(context, timeZone);

    @Test
    void outputPatternAsHeaderIsFalse() {
        assertThat(layout.isOutputPatternAsHeader()).isFalse();
    }

    @Test
    void hasAContext() {
        assertThat(layout.getContext())
            .isEqualTo(context);
    }

    @Test
    void hasAPatternWithATimeZone() {
        assertThat(layout.getPattern())
            .isEqualTo("%h %l %u [%t{dd/MMM/yyyy:HH:mm:ss Z,UTC}] \"%r\" %s %b \"%i{Referer}\" \"%i{User-Agent}\" %D");
    }
}
