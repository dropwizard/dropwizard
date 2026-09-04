package io.dropwizard.request.logging.layout;

import ch.qos.logback.access.common.pattern.RequestParameterConverter;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.DynamicConverter;
import org.junit.jupiter.api.Test;

import java.util.TimeZone;
import java.util.function.Supplier;

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

    // Verifies that the Dropwizard override installed in LogbackAccessRequestLayout's static initializer:
    // reqParameter / requestParameter must resolve to SafeRequestParameterConverter, not upstream's
    // RequestParameterConverter (which reads live request state and is unsafe under async appenders).
    @Test
    @SuppressWarnings("NullAway")
    void reqParameterUsesSafeConverter() {
        // when
        final Supplier<DynamicConverter> reqParamSupplier = layout.getEffectiveConverterMap().get("reqParameter");
        final Supplier<DynamicConverter> requestParamSupplier
            = layout.getEffectiveConverterMap().get("requestParameter");

        // then
        //    Invoke each supplier to materialize the converter instance and check its class.
        assertThat(reqParamSupplier)
            .isNotNull();
        assertThat(requestParamSupplier)
            .isNotNull();
        assertThat(reqParamSupplier.get())
            .isInstanceOf(SafeRequestParameterConverter.class)
            .isNotInstanceOf(RequestParameterConverter.class);
        assertThat(requestParamSupplier.get())
            .isInstanceOf(SafeRequestParameterConverter.class)
            .isNotInstanceOf(RequestParameterConverter.class);
    }
}
