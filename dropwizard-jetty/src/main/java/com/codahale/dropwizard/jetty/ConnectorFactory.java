package com.codahale.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface ConnectorFactory {
    Connector build(Server server,
                    MetricRegistry metrics,
                    String name,
                    ThreadPool threadPool);
}
