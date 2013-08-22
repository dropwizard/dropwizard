package com.codahale.dropwizard.jetty;

import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.validation.MinDuration;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.servlet.DispatcherType;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;

/**
 * A base class for {@link ServletHandlerFactory} implementations.
 */
public abstract class AbstractServletHandlerFactory
        implements ServletHandlerFactory {

    @Min(2)
    private int maxThreads = 1024;

    @Min(1)
    private int minThreads = 8;

    private int maxQueuedRequests = 1024;

    @MinDuration(1)
    private Duration idleThreadTimeout = Duration.minutes(1);

    @Valid
    @NotNull
    private RequestLogFactory requestLog = new RequestLogFactory();

    @JsonIgnore
    @ValidationMethod(message = "must have a smaller minThreads than maxThreads")
    public boolean isThreadPoolSizedCorrectly() {
        return minThreads <= maxThreads;
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

    // todo: generalize for all Filters
    @Valid
    @NotNull
    private GzipFilterFactory gzip = new GzipFilterFactory();

    @JsonProperty("requestLog")
    public RequestLogFactory getRequestLog() {
        return requestLog;
    }

    @JsonProperty("requestLog")
    public void setRequestLog(RequestLogFactory factory) {
        this.requestLog = factory;
    }

    @JsonProperty("gzip")
    public GzipFilterFactory getGzip() {
        return gzip;
    }

    @JsonProperty("gzip")
    public void setGzip(GzipFilterFactory factory) {
        this.gzip = factory;
    }

    @Override
    public Handler build(Server server,
                         MutableServletContextHandler handler,
                         MetricRegistry metricRegistry,
                         String name) {
        return addInstrumentation(
                metricRegistry,
                addRequestLog(
                        name,
                        configureSessionsAndSecurity(handler, server)));
    }

    public ThreadPool buildThreadPool(MetricRegistry metricRegistry, String name) {
        final BlockingQueue<Runnable> queue = new BlockingArrayQueue<>(minThreads, maxThreads, maxQueuedRequests);
        final InstrumentedQueuedThreadPool threadPool =
                new InstrumentedQueuedThreadPool(metricRegistry, maxThreads, minThreads,
                        (int) idleThreadTimeout.toMilliseconds(), queue);
        threadPool.setName(name);
        return threadPool;
    }

    protected Handler addRequestLog(String name, Handler handler) {
        if (requestLog.isEnabled()) {
            final RequestLogHandler requestLogHandler = new RequestLogHandler();
            requestLogHandler.setRequestLog(requestLog.build(name));
            requestLogHandler.setHandler(handler);
            return requestLogHandler;
        }
        return handler;
    }

    protected Handler addInstrumentation(MetricRegistry metricRegistry, Handler handler) {
        final InstrumentedHandler instrumented = new InstrumentedHandler(metricRegistry);
        instrumented.setHandler(handler);
        return instrumented;
    }

    protected ServletContextHandler addFilters(ServletContextHandler handler) {
        // todo: add ThreadNameFilter somehow??
        if (getGzip().isEnabled()) {
            final FilterHolder holder = new FilterHolder(getGzip().build());
            handler.addFilter(holder, "/*", EnumSet.allOf(DispatcherType.class));
        }
        return handler;
    }

    protected Handler configureSessionsAndSecurity(MutableServletContextHandler handler, Server server) {
        if (handler.isSecurityEnabled()) {
            handler.getSecurityHandler().setServer(server);
        }
        if (handler.isSessionsEnabled()) {
            handler.getSessionHandler().setServer(server);
        }

        return handler;
    }
}
