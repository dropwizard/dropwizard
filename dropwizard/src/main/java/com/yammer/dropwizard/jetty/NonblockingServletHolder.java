package com.yammer.dropwizard.jetty;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.*;
import java.io.IOException;

// TODO: 10/12/11 <coda> -- write tests for NonblockingServletHolder
// TODO: 10/12/11 <coda> -- write docs for NonblockingServletHolder

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
        } else {
            try {
                servlet.service(request, response);
            } finally {
                baseRequest.setAsyncSupported(asyncSupported);
            }
        }
    }
}
