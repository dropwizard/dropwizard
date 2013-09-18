package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jetty.MutableServletContextHandler;
import com.codahale.dropwizard.server.DefaultHandlerFactory;
import com.codahale.metrics.MetricRegistry;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;

/**
 * A factory for building servlet {@link Handler} instances.
 *
 * @see DefaultHandlerFactory
 */
public interface HandlerFactory {

    Handler build(Server server,
                  MutableServletContextHandler handler,
                  MetricRegistry metricRegistry,
                  String name);
}
