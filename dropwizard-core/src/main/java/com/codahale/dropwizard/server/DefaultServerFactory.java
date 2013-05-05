package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.jetty.*;
import com.codahale.dropwizard.jetty.ConnectorFactory;
import com.codahale.dropwizard.jetty.HttpConnectorFactory;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.servlets.ThreadNameFilter;
import com.codahale.dropwizard.util.Size;
import com.codahale.dropwizard.util.SizeUnit;
import com.codahale.dropwizard.validation.MinSize;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.servlet.DispatcherType;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;

/**
 * An object representation of the {@code server} section of the YAML configuration file.
 */
@JsonTypeName("default")
public class DefaultServerFactory implements ServerFactory {
    @Valid
    @NotNull
    private RequestLogFactory requestLog = new RequestLogFactory();

    @Valid
    @NotNull
    private GzipHandlerFactory gzip = new GzipHandlerFactory();

    @Valid
    @NotNull
    private ConnectorFactory applicationConnector = HttpConnectorFactory.application();

    @Valid
    @NotNull
    private ConnectorFactory adminConnector = HttpConnectorFactory.admin();

    @Min(2)
    private int maxThreads = 1024;

    @Min(1)
    private int minThreads = 8;

    @NotNull
    @MinSize(value = 8, unit = SizeUnit.KILOBYTES)
    private Size outputBufferSize = Size.kilobytes(32);

    @NotNull
    private Optional<Integer> maxQueuedRequests = Optional.absent();

    @JsonIgnore
    @ValidationMethod(message = "must have a smaller minThreads than maxThreads")
    public boolean isThreadPoolSizedCorrectly() {
        return minThreads <= maxThreads;
    }

    @JsonProperty("requestLog")
    public RequestLogFactory getRequestLogFactory() {
        return requestLog;
    }

    @JsonProperty("requestLog")
    public void setRequestLogFactory(RequestLogFactory requestLog) {
        this.requestLog = requestLog;
    }

    @JsonProperty("gzip")
    public GzipHandlerFactory getGzipHandlerFactory() {
        return gzip;
    }

    @JsonProperty("gzip")
    public void setGzipHandlerFactory(GzipHandlerFactory gzip) {
        this.gzip = gzip;
    }

    @JsonProperty
    public ConnectorFactory getApplicationConnector() {
        return applicationConnector;
    }

    @JsonProperty
    public void setApplicationConnector(ConnectorFactory applicationConnector) {
        this.applicationConnector = applicationConnector;
    }

    @JsonProperty
    public ConnectorFactory getAdminConnector() {
        return adminConnector;
    }

    @JsonProperty
    public void setAdminConnector(ConnectorFactory adminConnector) {
        this.adminConnector = adminConnector;
    }

    @JsonProperty
    public int getMaxThreads() {
        return maxThreads;
    }

    @JsonProperty
    public void setMaxThreads(int count) {
        this.maxThreads = count;
    }

    @JsonProperty
    public int getMinThreads() {
        return minThreads;
    }

    @JsonProperty
    public void setMinThreads(int count) {
        this.minThreads = count;
    }

    @JsonProperty
    public Size getOutputBufferSize() {
        return outputBufferSize;
    }

    @JsonProperty
    public void setOutputBufferSize(Size outputBufferSize) {
        this.outputBufferSize = outputBufferSize;
    }

    @JsonProperty
    public Optional<Integer> getMaxQueuedRequests() {
        return maxQueuedRequests;
    }

    @JsonProperty
    public void setMaxQueuedRequests(Optional<Integer> maxQueuedRequests) {
        this.maxQueuedRequests = maxQueuedRequests;
    }

    @Override
    public Server build(String name,
                        MetricRegistry metricRegistry,
                        HealthCheckRegistry healthChecks,
                        LifecycleEnvironment lifecycle,
                        ServletContextHandler applicationContext,
                        ServletContainer jerseyContainer,
                        ServletContextHandler adminContext,
                        JerseyEnvironment jersey,
                        ObjectMapper objectMapper,
                        Validator validator) {
        healthChecks.register("deadlocks", new ThreadDeadlockHealthCheck());

        final ThreadPool threadPool = createThreadPool(metricRegistry);
        final Server server = new Server(threadPool);
        lifecycle.attach(server);

        final ServletContextHandler applicationHandler = createExternalServlet(jersey,
                                                                               objectMapper,
                                                                               validator,
                                                                               applicationContext,
                                                                               jerseyContainer);
        final ServletContextHandler adminHandler = createInternalServlet(adminContext,
                                                                         metricRegistry,
                                                                         healthChecks);

        final Connector appConnector = applicationConnector.build(server, metricRegistry, "application");
        server.addConnector(appConnector);

        final Connector adConnector = adminConnector.build(server, metricRegistry, "admin");
        server.addConnector(adConnector);

        final Handler handler = createHandler(appConnector, applicationHandler,
                                              adConnector, adminHandler);
        if (getRequestLogFactory().isEnabled()) {
            final RequestLogHandler requestLogHandler = getRequestLogFactory().build(name);
            requestLogHandler.setHandler(handler);
            server.setHandler(requestLogHandler);
        } else {
            server.setHandler(handler);
        }

        final ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setShowStacks(false);
        server.addBean(errorHandler);

        server.setStopAtShutdown(true);

        return server;
    }



    private ServletContextHandler createInternalServlet(ServletContextHandler handler,
                                                        MetricRegistry metrics,
                                                        HealthCheckRegistry healthChecks) {
        handler.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY,
                                                 metrics);
        handler.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                                                 healthChecks);
        handler.addServlet(new NonblockingServletHolder(new AdminServlet()), "/*");

        return handler;
    }

    private ServletContextHandler createExternalServlet(JerseyEnvironment jersey,
                                                        ObjectMapper objectMapper,
                                                        Validator validator,
                                                        ServletContextHandler handler,
                                                        ServletContainer jerseyContainer) {
        handler.addFilter(ThreadNameFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        if (jerseyContainer != null) {
            jersey.addProvider(new JacksonMessageBodyProvider(objectMapper, validator));
            final ServletHolder jerseyHolder = new NonblockingServletHolder(jerseyContainer);
            jerseyHolder.setInitOrder(Integer.MAX_VALUE);
            handler.addServlet(jerseyHolder, jersey.getUrlPattern());
        }

        return handler;
    }

    private Handler createRoutingHandler(Connector applicationConnector,
                                         ServletContextHandler applicationHandler,
                                         Connector adminConnector,
                                         ServletContextHandler adminHandler) {


        // if we're on the same connector, route by context path
        if (applicationConnector == adminConnector) {
            return new ContextRoutingHandler(applicationHandler, adminHandler);
        }

        // otherwise, route by connector
        return new RoutingHandler(ImmutableMap.<Connector, Handler>of(
                applicationConnector, applicationHandler,
                adminConnector, adminHandler
        ));
    }

    private Handler createHandler(Connector applicationConnector,
                                  ServletContextHandler applicationHandler,
                                  Connector adminConnector,
                                  ServletContextHandler adminHandler) {
        final Handler handler = createRoutingHandler(applicationConnector,
                                                     applicationHandler,
                                                     adminConnector,
                                                     adminHandler);

        // TODO: 4/15/13 <coda> -- re-add instrumentation

        return getGzipHandlerFactory().wrapHandler(handler);
    }

    private ThreadPool createThreadPool(MetricRegistry metricRegistry) {
        final BlockingQueue<Runnable> queue =
                new BlockingArrayQueue<>(getMinThreads(),
                                         getMaxThreads(),
                                         getMaxQueuedRequests().or(Integer.MAX_VALUE));
        return new InstrumentedQueuedThreadPool(metricRegistry,
                                                "dw",
                                                getMaxThreads(),
                                                getMinThreads(),
                                                60000,
                                                queue);
    }
}
