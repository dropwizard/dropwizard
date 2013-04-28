package com.codahale.dropwizard.jetty;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.ArrayTernaryTrie;
import org.eclipse.jetty.util.Trie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A Jetty router which routes requests based on context path.
 */
public class ContextRoutingHandler extends AbstractHandler {
    private final Trie<ContextHandler> handlers;

    public ContextRoutingHandler(ContextHandler... handlers) {
        this.handlers = new ArrayTernaryTrie<>(false, handlers.length);
        for (ContextHandler handler : handlers) {
            this.handlers.put(handler.getContextPath(), handler);
            addBean(handler);
        }
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        final ContextHandler handler = handlers.getBest(baseRequest.getRequestURI());
        if (handler != null) {
            handler.handle(target, baseRequest, request, response);
        }
    }
}
