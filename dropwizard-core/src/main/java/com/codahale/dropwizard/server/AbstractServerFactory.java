package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.jetty.GzipFilterFactory;
import com.codahale.dropwizard.jetty.MutableServletContextHandler;
import com.codahale.dropwizard.jetty.NonblockingServletHolder;
import com.codahale.dropwizard.jetty.RequestLogFactory;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.servlets.ThreadNameFilter;
import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.validation.MinDuration;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.DispatcherType;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

// TODO: 5/15/13 <coda> -- add tests for AbstractServerFactory

/**
 * A base class for {@link ServerFactory} implementations.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code requestLog}</td>
 *         <td></td>
 *         <td>The {@link RequestLogFactory request log} configuration.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code gzip}</td>
 *         <td></td>
 *         <td>The {@link GzipFilterFactory GZIP} configuration.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxThreads}</td>
 *         <td>1024</td>
 *         <td>The maximum number of threads to use for requests.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code minThreads}</td>
 *         <td>8</td>
 *         <td>The minimum number of threads to use for requests.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxQueuedRequests}</td>
 *         <td>1024</td>
 *         <td>The maximum number of requests to queue before blocking the acceptors.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code idleThreadTimeout}</td>
 *         <td>1 minute</td>
 *         <td>The amount of time a worker thread can be idle before being stopped.</td>
 *     </tr>
 * </table>
 *
 * @see DefaultServerFactory
 * @see SimpleServerFactory
 */
public abstract class AbstractServerFactory implements ServerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFactory.class);
    private static final Pattern WINDOWS_NEWLINE = Pattern.compile("\\r\\n?");

    @Valid
    @NotNull
    private RequestLogFactory requestLog = new RequestLogFactory();

    @Valid
    @NotNull
    private GzipFilterFactory gzip = new GzipFilterFactory();

    @Min(2)
    private int maxThreads = 1024;

    @Min(1)
    private int minThreads = 8;

    private int maxQueuedRequests = 1024;

    @MinDuration(1)
    private Duration idleThreadTimeout = Duration.minutes(1);

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
    public GzipFilterFactory getGzipFilterFactory() {
        return gzip;
    }

    @JsonProperty("gzip")
    public void setGzipFilterFactory(GzipFilterFactory gzip) {
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
    public int getMaxQueuedRequests() {
        return maxQueuedRequests;
    }

    @JsonProperty
    public void setMaxQueuedRequests(int maxQueuedRequests) {
        this.maxQueuedRequests = maxQueuedRequests;
    }

    @JsonProperty
    public Duration getIdleThreadTimeout() {
        return idleThreadTimeout;
    }

    @JsonProperty
    public void setIdleThreadTimeout(Duration idleThreadTimeout) {
        this.idleThreadTimeout = idleThreadTimeout;
    }

    protected Handler createAdminServlet(Server server,
                                         MutableServletContextHandler handler,
                                         MetricRegistry metrics,
                                         HealthCheckRegistry healthChecks) {
        configureSessionsAndSecurity(handler, server);
        handler.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, metrics);
        handler.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY, healthChecks);
        handler.addServlet(new NonblockingServletHolder(new AdminServlet()), "/*");
        return handler;
    }

    private void configureSessionsAndSecurity(MutableServletContextHandler handler, Server server) {
        if (handler.isSecurityEnabled()) {
            handler.getSecurityHandler().setServer(server);
        }
        if (handler.isSessionsEnabled()) {
            handler.getSessionHandler().setServer(server);
        }
    }

    protected Handler createAppServlet(Server server,
                                       JerseyEnvironment jersey,
                                       ObjectMapper objectMapper,
                                       Validator validator,
                                       MutableServletContextHandler handler,
                                       @Nullable ServletContainer jerseyContainer,
                                       MetricRegistry metricRegistry) {
        configureSessionsAndSecurity(handler, server);
        handler.addFilter(ThreadNameFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        if (gzip.isEnabled()) {
            final FilterHolder holder = new FilterHolder(gzip.build());
            handler.addFilter(holder, "/*", EnumSet.allOf(DispatcherType.class));
        }
        if (jerseyContainer != null) {
            jersey.addProvider(new JacksonMessageBodyProvider(objectMapper, validator));
            handler.addServlet(new NonblockingServletHolder(jerseyContainer), jersey.getUrlPattern());
        }
        final InstrumentedHandler instrumented = new InstrumentedHandler(metricRegistry);
        instrumented.setHandler(handler);
        return instrumented;
    }

    protected ThreadPool createThreadPool(MetricRegistry metricRegistry) {
        final BlockingQueue<Runnable> queue = new BlockingArrayQueue<>(minThreads, maxThreads, maxQueuedRequests);
        final InstrumentedQueuedThreadPool threadPool =
                new InstrumentedQueuedThreadPool(metricRegistry, maxThreads, minThreads,
                                                 (int) idleThreadTimeout.toMilliseconds(), queue);
        threadPool.setName("dw");
        return threadPool;
    }

    protected Server buildServer(LifecycleEnvironment lifecycle,
                                 ThreadPool threadPool) {
        final Server server = new Server(threadPool);
        lifecycle.attach(server);
        final ErrorHandler errorHandler = new ErrorHandler();
        errorHandler.setShowStacks(false);
        server.addBean(errorHandler);
        server.setStopAtShutdown(true);
        return server;
    }

    protected Handler addRequestLog(Handler handler, String name) {
        if (requestLog.isEnabled()) {
            final RequestLogHandler requestLogHandler = new RequestLogHandler();
            requestLogHandler.setRequestLog(requestLog.build(name));
            requestLogHandler.setHandler(handler);
            return requestLogHandler;
        }
        return handler;
    }

    protected void printBanner(String name) {
        try {
            final String banner = WINDOWS_NEWLINE.matcher(Resources.toString(Resources.getResource("banner.txt"),
                                                                             Charsets.UTF_8))
                                                 .replaceAll("\n")
                                                 .replace("\n", String.format("%n"));
            LOGGER.info(String.format("Starting {}%n{}"), name, banner);
        } catch (IllegalArgumentException | IOException ignored) {
            // don't display the banner if there isn't one
            LOGGER.info("Starting {}", name);
        }
    }
}
