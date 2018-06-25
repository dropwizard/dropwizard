package io.dropwizard.servlets;

import io.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.function.Supplier;

import static io.dropwizard.servlets.Servlets.getFullUrl;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * A servlet filter which logs the methods and URIs of requests which take longer than a given
 * duration of time to complete.
 */
@SuppressWarnings("UnusedDeclaration")
public class SlowRequestFilter implements Filter {
    private final long threshold;

    private Supplier<Long> currentTimeProvider = System::nanoTime;
    private Logger logger = LoggerFactory.getLogger(SlowRequestFilter.class);

    /**
     * Creates a filter which logs requests which take longer than 1 second.
     */
    public SlowRequestFilter() {
        this(Duration.seconds(1));
    }

    /**
     * Creates a filter which logs requests which take longer than the given duration.
     *
     * @param threshold    the threshold for considering a request slow
     */
    public SlowRequestFilter(Duration threshold) {
        this.threshold = threshold.toNanoseconds();
    }

    void setCurrentTimeProvider(Supplier<Long> currentTimeProvider) {
        this.currentTimeProvider = currentTimeProvider;
    }

    void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { /* unused */ }

    @Override
    public void destroy() { /* unused */ }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final long startTime = currentTimeProvider.get();
        try {
            chain.doFilter(request, response);
        } finally {
            final long elapsedNS = currentTimeProvider.get() - startTime;
            final long elapsedMS = NANOSECONDS.toMillis(elapsedNS);
            if (elapsedNS >= threshold) {
                logger.warn("Slow request: {} {} ({}ms)",
                            req.getMethod(),
                            getFullUrl(req), elapsedMS);
            }
        }
    }
}
