package io.dropwizard.jersey.filter;

import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import io.dropwizard.util.Duration;

/**
 * This class adds an "X-Runtime" HTTP response header that includes the time
 * taken to execute the request, in seconds (based on the implementation from
 * Ruby on Rails).
 *
 * @see https://github.com/rack/rack/blob/master/lib/rack/runtime.rb
 */
@Provider
@PreMatching
public class RuntimeFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final float NANOS_IN_SECOND = Duration.seconds(1).toNanoseconds();
    private static final String RUNTIME_HEADER = "X-Runtime";
    private static final String RUNTIME_PROPERTY = "io.dropwizard.jersey.filter.runtime";

    @Override
    public void filter(final ContainerRequestContext request) throws IOException {
        request.setProperty(RUNTIME_PROPERTY, System.nanoTime());
    }

    @Override
    public void filter(final ContainerRequestContext request,
            final ContainerResponseContext response) throws IOException {

        final Long startTime = (Long) request.getProperty(RUNTIME_PROPERTY);
        if (startTime != null) {
            final float seconds = (System.nanoTime() - startTime) / NANOS_IN_SECOND;
            response.getHeaders().putSingle(RUNTIME_HEADER, String.format("%.6f", seconds));
        }
    }
}
