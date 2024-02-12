package io.dropwizard.servlets;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

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
        if (response instanceof HttpServletResponse resp) {
            resp.setHeader("Cache-Control", CACHE_SETTINGS);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { /* unused */ }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { /* unused */ }
}
