package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.jetty.GzipHandlerFactory;
import com.codahale.dropwizard.jetty.NonblockingServletHolder;
import com.codahale.dropwizard.jetty.RequestLogFactory;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.servlets.ThreadNameFilter;
import com.codahale.dropwizard.util.Size;
import com.codahale.dropwizard.util.SizeUnit;
import com.codahale.dropwizard.validation.MinSize;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
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

public abstract class AbstractServerFactory implements ServerFactory {
    @Valid
    @NotNull
    private RequestLogFactory requestLog = new RequestLogFactory();

    @Valid
    @NotNull
    private GzipHandlerFactory gzip = new GzipHandlerFactory();

    @Min(2)
    private int maxThreads = 1024;

    @Min(1)
    private int minThreads = 8;

    @NotNull
    @MinSize(value = 8, unit = SizeUnit.KILOBYTES)
    private Size outputBufferSize = Size.kilobytes(32);

    private int maxQueuedRequests = Integer.MAX_VALUE;

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
    public int getMaxQueuedRequests() {
        return maxQueuedRequests;
    }

    @JsonProperty
    public void setMaxQueuedRequests(int maxQueuedRequests) {
        this.maxQueuedRequests = maxQueuedRequests;
    }

    protected ServletContextHandler createInternalServlet(ServletContextHandler handler,
                                                          MetricRegistry metrics,
                                                          HealthCheckRegistry healthChecks) {
        handler.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY,
                                                 metrics);
        handler.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                                                 healthChecks);
        handler.addServlet(new NonblockingServletHolder(new AdminServlet()), "/*");

        return handler;
    }

    protected ServletContextHandler createExternalServlet(JerseyEnvironment jersey,
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

    protected ThreadPool createThreadPool(MetricRegistry metricRegistry) {
        final BlockingQueue<Runnable> queue = new BlockingArrayQueue<>(minThreads,
                                                                       maxThreads,
                                                                       maxQueuedRequests);
        return new InstrumentedQueuedThreadPool(metricRegistry,
                                                "dw",
                                                maxThreads,
                                                minThreads,
                                                60000,
                                                queue);
    }

    protected Server buildServer(LifecycleEnvironment lifecycle, ThreadPool threadPool) {
        final Server server = new Server(threadPool);
        lifecycle.attach(server);
        final ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setShowStacks(false);
        server.addBean(errorHandler);
        server.setStopAtShutdown(true);
        return server;
    }
}
