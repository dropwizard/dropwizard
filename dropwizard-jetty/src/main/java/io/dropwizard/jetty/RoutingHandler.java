package io.dropwizard.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class RoutingHandler extends AbstractHandler {
    /**
     * We use an array of entries instead of a map here for performance reasons. We're only ever
     * comparing connectors by reference, not by equality, so avoiding the overhead of a map is
     * a lot faster. See RoutingHandlerBenchmark for details, but tested against an
     * ImmutableMap-backed implementation it was ~54us vs. ~4500us for 1,000,000 iterations.
     */
    private static class Entry {
        private final Connector connector;
        private final Handler handler;

        private Entry(Connector connector, Handler handler) {
            this.connector = connector;
            this.handler = handler;
        }
    }

    private final Entry[] entries;

    public RoutingHandler(Map<Connector, Handler> handlers) {
        this.entries = new Entry[handlers.size()];
        int i = 0;
        for (Map.Entry<Connector, Handler> entry : handlers.entrySet()) {
            this.entries[i++] = new Entry(entry.getKey(), entry.getValue());
            addBean(entry.getValue());
        }
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
        final Connector connector = baseRequest.getHttpChannel().getConnector();
        for (Entry entry : entries) {
            // reference equality works fine — none of the connectors implement #equals(Object)
            if (entry.connector == connector) {
                entry.handler.handle(target, baseRequest, request, response);
                return;
            }
        }
    }
}

