package io.dropwizard.jersey.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class adds an "X-Request-Id" HTTP response header and logs the following
 * information: request method, request path, request ID, response status,
 * response length (or -1 if not known).
 *
 * @see <a href="https://devcenter.heroku.com/articles/http-request-id">Heroku - HTTP Request IDs</a>
 */
@Provider
@Priority(Priorities.USER)
public class RequestIdFilter implements ContainerResponseFilter {

    private static final String REQUEST_ID = "X-Request-Id";

    private Logger logger = LoggerFactory.getLogger(RequestIdFilter.class);

    void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void filter(final ContainerRequestContext request,
            final ContainerResponseContext response) throws IOException {

        String id = Optional.ofNullable(request.getHeaderString(REQUEST_ID))
            .filter(header -> !header.isEmpty())
            .orElseGet(() -> generateRandomUuid().toString());

        logger.trace("method={} path={} request_id={} status={} length={}",
                request.getMethod(), request.getUriInfo().getPath(), id,
                response.getStatus(), response.getLength());
        response.getHeaders().putSingle(REQUEST_ID, id);
    }

    /**
     * Generate a random UUID v4 that will perform reasonably when used by
     * multiple threads under load.
     *
     * @see <a href="https://github.com/Netflix/netflix-commons/blob/v0.3.0/netflix-commons-util/src/main/java/com/netflix/util/concurrent/ConcurrentUUIDFactory.java">ConcurrentUUIDFactory</a>
     * @return random UUID
     */
    private static UUID generateRandomUuid() {
        final Random rnd = ThreadLocalRandom.current();
        long mostSig  = rnd.nextLong();
        long leastSig = rnd.nextLong();

        // Identify this as a version 4 UUID, that is one based on a random value.
        mostSig &= 0xffffffffffff0fffL;
        mostSig |= 0x0000000000004000L;

        // Set the variant identifier as specified for version 4 UUID values.  The two
        // high order bits of the lower word are required to be one and zero, respectively.
        leastSig &= 0x3fffffffffffffffL;
        leastSig |= 0x8000000000000000L;

        return new UUID(mostSig, leastSig);
    }
}
