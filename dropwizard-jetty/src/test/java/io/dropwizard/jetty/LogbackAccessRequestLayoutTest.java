package io.dropwizard.jetty;

import ch.qos.logback.core.Context;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class LogbackAccessRequestLayoutTest {

    @Test
    public void outputPatternAsHeaderIsFalse() {
        final Context context = mock(Context.class);
        final String pattern = "pattern";
        final LogbackAccessRequestLayout layout = new LogbackAccessRequestLayout(context, pattern);

        assertThat(layout.isOutputPatternAsHeader()).isFalse();
        assertThat(layout.getContext()).isEqualTo(context);
        assertThat(layout.getPattern()).isEqualTo(pattern);
    }
}
