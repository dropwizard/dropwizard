package com.yammer.dropwizard.servlets;

import com.yammer.dropwizard.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.yammer.dropwizard.util.Servlets.getFullUrl;

/**
 * A servlet filter which logs the methods and URIs of requests which take longer than a given
 * duration of time to complete.
 */
@SuppressWarnings("UnusedDeclaration")
public class SlowRequestFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SlowRequestFilter.class);
    private final long threshold;

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

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { /* unused */ }

    @Override
    public void destroy() { /* unused */ }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final long startTime = System.nanoTime();
        try {
            chain.doFilter(request, response);
        } finally {
            final long elapsedMS = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            if (elapsedMS >= threshold) {
                LOGGER.warn("Slow request: {} {} ({}ms)",
                            req.getMethod(),
                            getFullUrl(req), elapsedMS);
            }
        }
    }
}
