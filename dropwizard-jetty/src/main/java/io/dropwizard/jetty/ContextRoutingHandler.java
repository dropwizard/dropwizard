package io.dropwizard.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.eclipse.jetty.util.Index;

import java.util.List;
import java.util.Map;

/**
 * A Jetty router which routes requests based on context path.
 */
public class ContextRoutingHandler extends Handler.AbstractContainer {
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
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        final Handler handler = handlers.getBest(request.getHttpURI().getPath());
        if (handler != null) {
            return handler.handle(request, response, callback);
        }
        return false;
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
    public List<Handler> getHandlers() {
        return handlers.keySet().stream().map(handlers::get).toList();
    }
}
