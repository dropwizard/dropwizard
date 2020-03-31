package io.dropwizard.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.util.ArrayTernaryTrie;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * A Jetty router which routes requests based on context path.
 */
public class ContextRoutingHandler extends AbstractHandlerContainer {
    private final ArrayTernaryTrie<Handler> handlers;

    public ContextRoutingHandler(Map<String, ? extends Handler> handlers) {
        this.handlers = new ArrayTernaryTrie<>(false);
        for (Map.Entry<String, ? extends Handler> entry : handlers.entrySet()) {
            if (!this.handlers.put(entry.getKey(), entry.getValue())) {
                throw new IllegalStateException("Too many handlers");
            }
            addBean(entry.getValue());
        }
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        final Handler handler = handlers.getBest(baseRequest.getRequestURI());
        if (handler != null) {
            handler.handle(target, baseRequest, request, response);
        }
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        for (String key : handlers.keySet()) {
            handlers.get(key).start();
        }
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        for (String key : handlers.keySet()) {
            handlers.get(key).stop();
        }
    }

    @Override
    public Handler[] getHandlers() {
        return handlers.entrySet().stream().map(e -> e.getValue()).toArray(Handler[]::new);
    }
}
