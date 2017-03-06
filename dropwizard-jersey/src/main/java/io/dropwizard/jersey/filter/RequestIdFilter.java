package io.dropwizard.jersey.filter;

import java.io.IOException;
import java.util.UUID;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class adds a "X-Request-Id" HTTP response header and logs the following
 * information: request method, request path, request ID, response status,
 * response length (or -1 if not known).
 *
 * @see https://devcenter.heroku.com/articles/http-request-id
 */
@Provider
@Priority(Priorities.USER)
public class RequestIdFilter implements ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestIdFilter.class);
    private static final String REQUEST_ID = "X-Request-Id";

    @Override
    public void filter(final ContainerRequestContext request,
            final ContainerResponseContext response) throws IOException {

        final UUID id = UUID.randomUUID();
        LOGGER.info("method={} path={} request_id={} status={} length={}",
                request.getMethod(), request.getUriInfo().getPath(), id,
                response.getStatus(), response.getLength());
        response.getHeaders().add(REQUEST_ID, id);
    }
}
