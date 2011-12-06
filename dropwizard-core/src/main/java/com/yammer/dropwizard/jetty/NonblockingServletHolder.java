package com.yammer.dropwizard.jetty;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.*;
import java.io.IOException;

/**
 * A {@link ServletHolder} subclass which removes the synchronization around servlet initialization
 * by requiring a pre-initialized servlet holder.
 */
public class NonblockingServletHolder extends ServletHolder {
    private final Servlet servlet;

    public NonblockingServletHolder(Servlet servlet) {
        super(servlet);
        this.servlet = servlet;
    }

    @Override
    public Servlet getServlet() throws ServletException {
        return servlet;
    }

    @Override
    public void handle(Request baseRequest,
                       ServletRequest request,
                       ServletResponse response) throws ServletException, IOException {
        final boolean asyncSupported = baseRequest.isAsyncSupported();
        if (!isAsyncSupported()) {
            baseRequest.setAsyncSupported(false);
        }
        try {
            servlet.service(request, response);
        } finally {
            baseRequest.setAsyncSupported(asyncSupported);
        }
    }
}
