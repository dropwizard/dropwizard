package com.yammer.dropwizard.servlets;

import org.eclipse.jetty.http.HttpHeaders;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// TODO: 10/12/11 <coda> -- write tests for CacheBustingFilter
// TODO: 10/12/11 <coda> -- write docs for CacheBustingFilter

@SuppressWarnings("UnusedDeclaration")
public class CacheBustingFilter implements Filter {
    private static final String MUST_REVALIDATE_NO_CACHE_NO_STORE = "must-revalidate,no-cache,no-store";

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            final HttpServletResponse resp = (HttpServletResponse) response;
            resp.setHeader(HttpHeaders.CACHE_CONTROL, MUST_REVALIDATE_NO_CACHE_NO_STORE);
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { /* unused */ }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { /* unused */ }
}
