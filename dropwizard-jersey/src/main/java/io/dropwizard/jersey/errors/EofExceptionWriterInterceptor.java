package io.dropwizard.jersey.errors;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.eclipse.jetty.io.EofException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * A {@link WriterInterceptor} to swallow {@link org.eclipse.jetty.io.EofException} which occurs when a client
 * disconnects before the complete response could be sent.
 *
 * @see EarlyEofExceptionMapper
 * @see EofException
 */
@Provider
@Priority(Integer.MAX_VALUE)
public class EofExceptionWriterInterceptor implements WriterInterceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EofExceptionWriterInterceptor.class);

    private final Counter exceptionCounter;

    @Inject
    public EofExceptionWriterInterceptor(MetricRegistry metricRegistry) {
        this.exceptionCounter = metricRegistry.counter(MetricRegistry.name(getClass(), "eof-exceptions"));
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        try {
            context.proceed();
        } catch (EofException e) {
            LOGGER.debug("Client disconnected while processing and sending response", e);
            exceptionCounter.inc();
        }
    }
}
