package com.codahale.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.PROPERTY,
              property = "type",
              defaultImpl = HttpConnectorFactory.class)
public interface ConnectorFactory {
    Connector build(Server server,
                    MetricRegistry metrics,
                    String name);
}
