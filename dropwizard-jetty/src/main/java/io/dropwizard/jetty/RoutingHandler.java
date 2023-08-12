package io.dropwizard.jetty;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class RoutingHandler extends Handler.AbstractContainer {

    /**
     * We use an array of entries instead of a map here for performance reasons. We're only ever
     * comparing connectors by reference, not by equality, so avoiding the overhead of a map is
     * a lot faster. See RoutingHandlerBenchmark for details, but tested against an
     * ImmutableMap-backed implementation it was ~54us vs. ~4500us for 1,000,000 iterations.
     */
    private record Entry(Connector connector, Handler handler) {
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
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        final Connector connector = request.getConnectionMetaData().getConnector();
        for (Entry entry : entries) {
            // reference equality works fine — none of the connectors implement #equals(Object)
            if (entry.connector == connector) {
                if (entry.handler.handle(request, response, callback)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<Handler> getHandlers() {
        return Arrays.stream(entries).map(Entry::handler).toList();
    }
}

