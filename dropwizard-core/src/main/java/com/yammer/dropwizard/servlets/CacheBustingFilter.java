package com.yammer.dropwizard.servlets;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CacheBustingFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { /* unused */ }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader("Cache-Control",
                                                       "must-revalidate,no-cache,no-store");
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() { /* unused */ }
}
