package io.dropwizard.servlets;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static io.dropwizard.servlets.Servlets.getFullUrl;

/**
 * A servlet filter which adds the request method and URI to the thread name processing the request
 * for the duration of the request.
 */
public class ThreadNameFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { /* unused */ }

    @Override
    public void destroy() { /* unused */ }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest req = (HttpServletRequest) request;
        final Thread current = Thread.currentThread();
        final String oldName = current.getName();
        try {
            current.setName(formatName(req, oldName));
            chain.doFilter(request, response);
        } finally {
            current.setName(oldName);
        }
    }

    private static String formatName(HttpServletRequest req, String oldName) {
        return oldName + " - " + req.getMethod() + ' ' + getFullUrl(req);
    }
}
