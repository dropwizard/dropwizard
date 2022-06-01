package io.dropwizard.jetty;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandlerContainer;
import org.eclipse.jetty.util.Index;

/**
 * A Jetty router which routes requests based on context path.
 */
public class ContextRoutingHandler extends AbstractHandlerContainer {
    private final Index<Handler> handlers;

    public ContextRoutingHandler(Map<String, ? extends Handler> handlers) {
        Index.Builder<Handler> builder = new Index.Builder<Handler>().caseSensitive(false);
        for (Map.Entry<String, ? extends Handler> entry : handlers.entrySet()) {
            builder.with(entry.getKey(), entry.getValue());
            addBean(entry.getValue());
        }
        this.handlers = builder.build();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
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
        return handlers.keySet().stream().map(handlers::get).toArray(Handler[]::new);
    }

    @Override
    protected void expandChildren(List<Handler> list, Class<?> byClass) {
        Handler[] handlerArray = getHandlers();
        if (handlerArray != null) {
            for (Handler h : handlerArray) {
                expandHandler(h, list, byClass);
            }
        }
    }
}
