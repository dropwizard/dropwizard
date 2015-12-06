package io.dropwizard.jersey.filter;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AllowedMethodsFilter implements Filter {

    public static final String ALLOWED_METHODS_PARAM = "allowedMethods";
    public static final Set<String> DEFAULT_ALLOWED_METHODS = ImmutableSet.of(
            "GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "PATCH"
    );

    private static final Logger LOG = LoggerFactory.getLogger(AllowedMethodsFilter.class);

    private Set<String> allowedMethods = new HashSet<>();

    @Override
    public void init(FilterConfig config) {
        final String allowedMethodsConfig = config.getInitParameter(ALLOWED_METHODS_PARAM);
        if (allowedMethodsConfig == null) {
            allowedMethods.addAll(DEFAULT_ALLOWED_METHODS);
        } else {
            allowedMethods.addAll(Arrays.asList(allowedMethodsConfig.split(",")));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        handle((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (allowedMethods.contains(request.getMethod())) {
            chain.doFilter(request, response);
        } else {
            LOG.debug("Request with disallowed method {} blocked", request.getMethod());
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    public void destroy() {
        allowedMethods.clear();
    }
}
