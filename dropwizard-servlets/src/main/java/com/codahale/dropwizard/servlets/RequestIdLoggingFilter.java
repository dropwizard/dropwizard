package com.codahale.dropwizard.servlets;

import com.codahale.dropwizard.util.RequestId;
import com.google.common.base.Optional;
import org.slf4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

/**
 * A servlet filter that adds request identifiers to slf4j's mapped diagnostic context (MDC) , it allows logging output
 * at multiple levels of your service to be easily grouped by the originating request.
 *
 * Usage
 * -----
 * environment.servlets()
 *   .addFilter("request-id-logging", new RequestIdLoggingFilter("UTF8"))
 *   .addMappingForUrlPatterns(null, true, "/*")
 *
 * and
 *
 * logFormat: "%-5level [%thread] %logger [%X{Client-Request-Id}|%X{Service-Request-Id}] %message%n"
 * @see com.codahale.dropwizard.util.RequestId
 */
public class RequestIdLoggingFilter implements Filter {
    private static final String DEFAULT_CLIENT_ID_REGEX = "[\\w\\d_-]{0,36}";

    public static final String HEADER_CLIENT_REQUEST = "X-Client-Request-Id";
    public static final String HEADER_SERVICE_REQUEST = "X-Service-Request-Id";

    private final String defaultEncoding;
    private final Pattern clientIdPattern;

    public RequestIdLoggingFilter(String defaultEncoding) {
        this(defaultEncoding, DEFAULT_CLIENT_ID_REGEX);
    }

    public RequestIdLoggingFilter(String defaultEncoding, String clientIdRegex) {
        this.defaultEncoding = defaultEncoding;
        this.clientIdPattern = Pattern.compile(clientIdRegex);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { /* unused */ }

    @Override
    public void destroy() { /* unused */ }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String encoding = Optional.fromNullable(request.getCharacterEncoding()).or(defaultEncoding);

        String serviceRequestId = String.valueOf(ThreadLocalRandom.current().nextLong());
        MDC.put(RequestId.SERVICE_REQUEST_ID, serviceRequestId);
        resp.addHeader(HEADER_SERVICE_REQUEST, URLEncoder.encode(serviceRequestId, encoding));

        String clientRequestId = req.getHeader(HEADER_CLIENT_REQUEST);
        if (clientRequestId != null && clientIdPattern.matcher(clientRequestId).matches()) {
          MDC.put(RequestId.CLIENT_REQUEST_ID, clientRequestId);
          resp.addHeader(HEADER_CLIENT_REQUEST, URLEncoder.encode(clientRequestId, encoding));
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(RequestId.SERVICE_REQUEST_ID);
            MDC.remove(RequestId.CLIENT_REQUEST_ID);
        }
    }
}