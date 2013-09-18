package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jetty.GzipFilterFactory;
import com.codahale.dropwizard.jetty.MutableServletContextHandler;
import com.codahale.dropwizard.jetty.RequestLogFactory;
import com.codahale.dropwizard.servlets.ThreadNameFilter;
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
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.servlet.DispatcherType;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;

/**
 * A base class for {@link HandlerFactory} implementations.
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
 * @see DefaultHandlerFactory
 * @see ContextHandlerFactory
 */
public abstract class AbstractHandlerFactory
        implements HandlerFactory {

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

    // todo: generalize for all Filters
    @Valid
    @NotNull
    private GzipFilterFactory gzip = new GzipFilterFactory();

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
                        addFilters(
                                configureSessionsAndSecurity(server, handler))));
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
        handler.addFilter(ThreadNameFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        if (getGzip().isEnabled()) {
            final FilterHolder holder = new FilterHolder(getGzip().build());
            handler.addFilter(holder, "/*", EnumSet.allOf(DispatcherType.class));
        }
        return handler;
    }

    protected MutableServletContextHandler configureSessionsAndSecurity(Server server, MutableServletContextHandler handler) {
        if (handler.isSecurityEnabled()) {
            handler.getSecurityHandler().setServer(server);
        }
        if (handler.isSessionsEnabled()) {
            handler.getSessionHandler().setServer(server);
        }

        return handler;
    }
}
