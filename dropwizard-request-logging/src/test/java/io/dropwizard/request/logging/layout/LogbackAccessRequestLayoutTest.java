package io.dropwizard.request.logging.layout;

import ch.qos.logback.access.common.spi.AccessEvent;
import ch.qos.logback.access.common.spi.ServerAdapter;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Context;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogbackAccessRequestLayoutTest {
    private final Context context = mock(LoggerContext.class);
    private final TimeZone timeZone = TimeZone.getTimeZone("UTC");
    private final LogbackAccessRequestLayout layout = new LogbackAccessRequestLayout(context, timeZone);

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

    @ParameterizedTest
    @ValueSource(strings = { "requestParameter", "reqParameter" })
    void requestParametersAreConvertedUsingSafeRequestParameterConverter(String patternKey) {
        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        HttpServletResponse httpServletResponse = mock(HttpServletResponse.class);
        ServerAdapter serverAdapter = mock(ServerAdapter.class);
        AccessEvent accessEvent = mock(
                AccessEvent.class,
                delegatesTo(new AccessEvent(context, httpServletRequest, httpServletResponse, serverAdapter)));

        layout.setPattern("%" + patternKey + "{parameterName}");
        layout.start();
        layout.doLayout(accessEvent);

        assertThat(layout.getEffectiveConverterMap())
                .hasEntrySatisfying(patternKey, supplier -> assertThat(supplier.get()).isInstanceOf(SafeRequestParameterConverter.class));

        verify(accessEvent).getRequestParameterMap();
    }

}
