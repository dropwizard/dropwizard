package io.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;

/**
 * A factory for creating Jetty {@link Connector}s.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface ConnectorFactory extends Discoverable {
    /**
     * Create a new connector.
     *
     * @param server     the application's {@link Server} instance
     * @param metrics    the application's metrics
     * @param name       the application's name
     * @param threadPool the application's thread pool
     * @return a {@link Connector}
     */
    Connector build(Server server,
                    MetricRegistry metrics,
                    String name,
                    ThreadPool threadPool);
}
