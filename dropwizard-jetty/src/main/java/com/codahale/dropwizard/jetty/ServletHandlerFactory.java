package com.codahale.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

/**
 * A factory for building servlet {@link Handler} instances.
 *
 * @see DefaultServletHandlerFactory
 */
public interface ServletHandlerFactory {

    Handler build(Server server,
                  MutableServletContextHandler handler,
                  MetricRegistry metricRegistry,
                  String name);
}
