package io.dropwizard.jersey.errors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import java.io.IOException;
import javax.ws.rs.ext.WriterInterceptorContext;
import org.eclipse.jetty.io.EofException;
import org.junit.jupiter.api.Test;

class EofExceptionWriterInterceptorTest {
    @SuppressWarnings("NullAway")
    @Test
    void shouldSwallowEofException() throws IOException {
        MetricRegistry metricRegistry = new MetricRegistry();
        EofExceptionWriterInterceptor interceptor = new EofExceptionWriterInterceptor(metricRegistry);
        WriterInterceptorContext context = mock(WriterInterceptorContext.class);
        doThrow(EofException.class).when(context).proceed();
        interceptor.aroundWriteTo(context);

        verify(context, only()).proceed();

        Counter counter = metricRegistry
                .getCounters()
                .get("io.dropwizard.jersey.errors.EofExceptionWriterInterceptor.eof-exceptions");
        assertThat(counter).isNotNull();
        assertThat(counter.getCount()).isEqualTo(1L);
    }
}
