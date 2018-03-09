package io.dropwizard.jersey.filter;

import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.util.Duration;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.function.Supplier;

/**
 * This class adds an "X-Runtime" HTTP response header that includes the time
 * taken to execute the request, in seconds (based on the implementation from
 * Ruby on Rails).
 *
 * @see <a href="https://github.com/rack/rack/blob/2.0.0/lib/rack/runtime.rb">Rack::Runtime</a>
 */
@Provider
@PreMatching
public class RuntimeFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final float NANOS_IN_SECOND = Duration.seconds(1).toNanoseconds();
    private static final String RUNTIME_HEADER = "X-Runtime";
    private static final String RUNTIME_PROPERTY = "io.dropwizard.jersey.filter.runtime";

    private Supplier<Long> currentTimeProvider = System::nanoTime;

    @VisibleForTesting
    void setCurrentTimeProvider(Supplier<Long> currentTimeProvider) {
        this.currentTimeProvider = currentTimeProvider;
    }

    @Override
    public void filter(final ContainerRequestContext request) throws IOException {
        request.setProperty(RUNTIME_PROPERTY, currentTimeProvider.get());
    }

    @Override
    public void filter(final ContainerRequestContext request,
            final ContainerResponseContext response) throws IOException {

        final Long startTime = (Long) request.getProperty(RUNTIME_PROPERTY);
        if (startTime != null) {
            final float seconds = (currentTimeProvider.get() - startTime) / NANOS_IN_SECOND;
            response.getHeaders().putSingle(RUNTIME_HEADER, String.format("%.6f", seconds));
        }
    }
}
