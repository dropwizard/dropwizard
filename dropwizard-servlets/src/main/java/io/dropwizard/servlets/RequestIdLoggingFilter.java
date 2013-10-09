package io.dropwizard.servlets;

import io.dropwizard.util.RequestId;
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
 * @see io.dropwizard.util.RequestId
 */
public class RequestIdLoggingFilter implements Filter {
    private static final String DEFAULT_CLIENT_ID_REGEX = "[\\w\\d_-]{0,36}";

    private final String defaultEncoding;
    private final Pattern clientIdPattern;
    private final String clientRequestHeader;
    private final String serviceRequestHeader;

    public RequestIdLoggingFilter(String defaultEncoding, String clientRequestHeader, String serviceRequestHeader) {
        this(defaultEncoding, DEFAULT_CLIENT_ID_REGEX, clientRequestHeader, serviceRequestHeader);
    }

    public RequestIdLoggingFilter(String defaultEncoding,
                                  String clientIdRegex,
                                  String clientRequestHeader,
                                  String serviceRequestHeader) {
        this.defaultEncoding = defaultEncoding;
        this.clientIdPattern = Pattern.compile(clientIdRegex);
        this.clientRequestHeader = clientRequestHeader;
        this.serviceRequestHeader = serviceRequestHeader;
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
        resp.addHeader(serviceRequestHeader, URLEncoder.encode(serviceRequestId, encoding));

        String clientRequestId = req.getHeader(clientRequestHeader);
        if (clientRequestId != null && clientIdPattern.matcher(clientRequestId).matches()) {
          MDC.put(RequestId.CLIENT_REQUEST_ID, clientRequestId);
          resp.addHeader(clientRequestHeader, URLEncoder.encode(clientRequestId, encoding));
        }

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(RequestId.SERVICE_REQUEST_ID);
            MDC.remove(RequestId.CLIENT_REQUEST_ID);
        }
    }
}