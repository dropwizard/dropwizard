package com.codahale.dropwizard.servlets;

import com.google.common.net.HttpHeaders;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Adds a no-cache header to all responses.
 */
public class CacheBustingFilter implements Filter {
    private static final String CACHE_SETTINGS = "must-revalidate,no-cache,no-store";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            final HttpServletResponse resp = (HttpServletResponse) response;
            resp.setHeader(HttpHeaders.CACHE_CONTROL, CACHE_SETTINGS);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { /* unused */ }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { /* unused */ }
}
