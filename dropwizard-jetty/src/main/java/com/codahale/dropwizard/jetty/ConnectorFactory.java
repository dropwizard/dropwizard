package com.codahale.dropwizard.jetty;

import com.codahale.dropwizard.jackson.Discoverable;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface ConnectorFactory extends Discoverable {
    Connector build(Server server,
                    MetricRegistry metrics,
                    String name,
                    ThreadPool threadPool);
}
