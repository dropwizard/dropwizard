package com.codahale.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/** TODO: Document */
public class DefaultServletHandlerFactory extends AbstractServletHandlerFactory {

    @Valid
    @NotNull
    private List<ConnectorFactory> connectors =
            Lists.newArrayList(HttpConnectorFactory.application());

    @JsonProperty("connectors")
    public List<ConnectorFactory> getConnectors() {
        return connectors;
    }

    @JsonProperty("connectors")
    public void setConnectors(List<ConnectorFactory> factories) {
        this.connectors = factories;
    }

    public List<Connector> buildConnectors(MetricRegistry metricRegistry,
                                           Server server,
                                           ThreadPool threadPool,
                                           String name) {
        final List<Connector> connectors = Lists.newArrayList();
        for (ConnectorFactory factory : getConnectors()) {
            connectors.add(factory.build(server, metricRegistry, name, threadPool));
        }
        return connectors;
    }
}
