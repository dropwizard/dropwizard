package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jetty.ConnectorFactory;
import com.codahale.dropwizard.jetty.HttpConnectorFactory;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * The default implementation of {@link HandlerFactory}, which allows for
 * multiple {@link Connector}s, all running on separate ports.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code connectors}</td>
 *         <td>An {@link HttpConnectorFactory HTTP connector}.</td>
 *         <td>A set of {@link ConnectorFactory connectors} to listen for application requests.</td>
 *     </tr>
 * </table>
 * <p/>
 * For more configuration parameters, see {@link AbstractHandlerFactory}.
 *
 * @see HandlerFactory
 * @see AbstractHandlerFactory
 */
public class DefaultHandlerFactory extends AbstractHandlerFactory {

    public static DefaultHandlerFactory forConnectors(ConnectorFactory... connectors) {
        return forConnectors(ImmutableList.copyOf(connectors));
    }

    public static DefaultHandlerFactory forConnectors(ImmutableList<ConnectorFactory> connectors) {
        final DefaultHandlerFactory factory = new DefaultHandlerFactory();
        factory.connectors = connectors;
        return factory;
    }

    @Valid
    @NotNull
    private ImmutableList<ConnectorFactory> connectors =
            ImmutableList.of(HttpConnectorFactory.application());

    @JsonProperty("connectors")
    public ImmutableList<ConnectorFactory> getConnectors() {
        return connectors;
    }

    @JsonProperty("connectors")
    public void setConnectors(ImmutableList<ConnectorFactory> factories) {
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
