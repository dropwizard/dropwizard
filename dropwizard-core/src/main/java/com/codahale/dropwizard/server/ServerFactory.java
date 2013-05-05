package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.jetty.*;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.servlets.ThreadNameFilter;
import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.util.Size;
import com.codahale.dropwizard.util.SizeUnit;
import com.codahale.dropwizard.validation.MinDuration;
import com.codahale.dropwizard.validation.MinSize;
import com.codahale.dropwizard.validation.PortRange;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.io.MappedByteBufferPool;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.BlockingArrayQueue;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.servlet.DispatcherType;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * An object representation of the {@code server} section of the YAML configuration file.
 */
public class ServerFactory {
    @Valid
    @NotNull
    private RequestLogFactory requestLog = new RequestLogFactory();

    @Valid
    @NotNull
    private GzipHandlerFactory gzip = new GzipHandlerFactory();

    @PortRange
    private int port = 8080;

    @PortRange
    private int adminPort = 8081;

    @Min(2)
    private int maxThreads = 1024;

    @Min(1)
    private int minThreads = 8;

    @Min(1)
    private int acceptorThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

    @Min(1)
    private int selectorThreads = Runtime.getRuntime().availableProcessors();

    @Min(-1)
    private int acceptQueueSize = -1;

    private boolean reuseAddress = true;
    private Duration soLingerTime = null;
    private boolean useServerHeader = false;
    private boolean useDateHeader = true;
    private boolean useForwardedHeaders = true;
    private boolean useDirectBuffers = false;
    private String bindHost = null;
    private String adminUsername = null;
    private String adminPassword = null;

    @NotNull
    @MinSize(128)
    private Size headerCacheSize = Size.bytes(512);

    @NotNull
    @MinSize(value = 8, unit = SizeUnit.KILOBYTES)
    private Size outputBufferSize = Size.kilobytes(32);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.KILOBYTES)
    private Size maxRequestHeaderSize = Size.kilobytes(8);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.KILOBYTES)
    private Size maxResponseHeaderSize = Size.kilobytes(8);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.KILOBYTES)
    private Size inputBufferSize = Size.kilobytes(8);

    @NotNull
    @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
    private Duration idleTimeout = Duration.seconds(30);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.BYTES)
    private Size minBufferPoolSize = Size.bytes(64);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.BYTES)
    private Size bufferPoolIncrement = Size.bytes(1024);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.BYTES)
    private Size maxBufferPoolSize = Size.kilobytes(64);

    @NotNull
    private Optional<Integer> maxQueuedRequests = Optional.absent();

    @JsonIgnore
    @ValidationMethod(message = "must have a smaller minThreads than maxThreads")
    public boolean isThreadPoolSizedCorrectly() {
        return minThreads <= maxThreads;
    }

    @JsonIgnore
    @ValidationMethod(message = "must have adminUsername if adminPassword is defined")
    public boolean isAdminUsernameDefined() {
        return (adminPassword == null) || (adminUsername != null);
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
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public int getAdminPort() {
        return adminPort;
    }

    @JsonProperty
    public void setAdminPort(int port) {
        this.adminPort = port;
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
    public int getAcceptorThreads() {
        return acceptorThreads;
    }

    @JsonProperty
    public void setAcceptorThreads(int count) {
        this.acceptorThreads = count;
    }

    @JsonProperty
    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    @JsonProperty
    public void setAcceptQueueSize(int size) {
        this.acceptQueueSize = size;
    }

    @JsonProperty("reuseAddress")
    public boolean isReuseAddressEnabled() {
        return reuseAddress;
    }

    @JsonProperty
    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    @JsonProperty
    public Optional<Duration> getSoLingerTime() {
        return Optional.fromNullable(soLingerTime);
    }

    @JsonProperty
    public void setSoLingerTime(Duration duration) {
        this.soLingerTime = duration;
    }

    @JsonProperty("useForwardedHeaders")
    public boolean useForwardedHeaders() {
        return useForwardedHeaders;
    }

    @JsonProperty
    public void setUseForwardedHeaders(boolean useForwardedHeaders) {
        this.useForwardedHeaders = useForwardedHeaders;
    }

    @JsonProperty("useDirectBuffers")
    public boolean useDirectBuffers() {
        return useDirectBuffers;
    }

    @JsonProperty
    public void setUseDirectBuffers(boolean useDirectBuffers) {
        this.useDirectBuffers = useDirectBuffers;
    }

    @JsonProperty
    public Optional<String> getBindHost() {
        return Optional.fromNullable(bindHost);
    }

    @JsonProperty
    public void setBindHost(String host) {
        this.bindHost = host;
    }

    @JsonProperty("useDateHeader")
    public boolean isDateHeaderEnabled() {
        return useDateHeader;
    }

    @JsonProperty
    public void setUseDateHeader(boolean useDateHeader) {
        this.useDateHeader = useDateHeader;
    }

    @JsonProperty("useServerHeader")
    public boolean isServerHeaderEnabled() {
        return useServerHeader;
    }

    @JsonProperty
    public void setUseServerHeader(boolean useServerHeader) {
        this.useServerHeader = useServerHeader;
    }

    @JsonProperty
    public Optional<String> getAdminUsername() {
        return Optional.fromNullable(adminUsername);
    }

    @JsonProperty
    public void setAdminUsername(String username) {
        this.adminUsername = username;
    }

    @JsonProperty
    public Optional<String> getAdminPassword() {
        return Optional.fromNullable(adminPassword);
    }

    @JsonProperty
    public void setAdminPassword(String password) {
        this.adminPassword = password;
    }

    @JsonProperty
    public Size getHeaderCacheSize() {
        return headerCacheSize;
    }

    @JsonProperty
    public void setHeaderCacheSize(Size headerCacheSize) {
        this.headerCacheSize = headerCacheSize;
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
    public Size getMaxRequestHeaderSize() {
        return maxRequestHeaderSize;
    }

    @JsonProperty
    public void setMaxRequestHeaderSize(Size maxRequestHeaderSize) {
        this.maxRequestHeaderSize = maxRequestHeaderSize;
    }

    @JsonProperty
    public Size getMaxResponseHeaderSize() {
        return maxResponseHeaderSize;
    }

    @JsonProperty
    public void setMaxResponseHeaderSize(Size maxResponseHeaderSize) {
        this.maxResponseHeaderSize = maxResponseHeaderSize;
    }

    @JsonProperty
    public Size getInputBufferSize() {
        return inputBufferSize;
    }

    @JsonProperty
    public void setInputBufferSize(Size inputBufferSize) {
        this.inputBufferSize = inputBufferSize;
    }

    @JsonProperty
    public int getSelectorThreads() {
        return selectorThreads;
    }

    @JsonProperty
    public void setSelectorThreads(int selectorThreads) {
        this.selectorThreads = selectorThreads;
    }

    @JsonProperty
    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    @JsonProperty
    public void setIdleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @JsonProperty
    public Size getMinBufferPoolSize() {
        return minBufferPoolSize;
    }

    @JsonProperty
    public void setMinBufferPoolSize(Size minBufferPoolSize) {
        this.minBufferPoolSize = minBufferPoolSize;
    }

    @JsonProperty
    public Size getBufferPoolIncrement() {
        return bufferPoolIncrement;
    }

    @JsonProperty
    public void setBufferPoolIncrement(Size bufferPoolIncrement) {
        this.bufferPoolIncrement = bufferPoolIncrement;
    }

    @JsonProperty
    public Size getMaxBufferPoolSize() {
        return maxBufferPoolSize;
    }

    @JsonProperty
    public void setMaxBufferPoolSize(Size maxBufferPoolSize) {
        this.maxBufferPoolSize = maxBufferPoolSize;
    }

    @JsonProperty
    public Optional<Integer> getMaxQueuedRequests() {
        return maxQueuedRequests;
    }

    @JsonProperty
    public void setMaxQueuedRequests(Optional<Integer> maxQueuedRequests) {
        this.maxQueuedRequests = maxQueuedRequests;
    }

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

        final Connector applicationConnector = createApplicationConnector(server, metricRegistry);
        server.addConnector(applicationConnector);

        final Connector adminConnector;
        // if we're dynamically allocating ports, no worries if they are the same (i.e. 0)
        if (getAdminPort() == 0 || (getAdminPort() != getPort())) {
            adminConnector = createAdminConnector(server);
            server.addConnector(adminConnector);
        } else {
            adminConnector = applicationConnector;
        }

        if (getAdminUsername().isPresent() || getAdminPassword().isPresent()) {
            adminHandler.setSecurityHandler(basicAuthHandler(getAdminUsername().or(""),
                                                             getAdminPassword().or("")));
        }

        final Handler handler = createHandler(applicationConnector,
                                              applicationHandler,
                                              adminConnector,
                                              adminHandler);
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

    private SecurityHandler basicAuthHandler(String username, String password) {
        final HashLoginService loginService = new HashLoginService();
        loginService.putUser(username, Credential.getCredential(password), new String[]{ "user" });
        loginService.setName("admin");

        final Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{ "user" });
        constraint.setAuthenticate(true);

        final ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        final ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.setAuthenticator(new BasicAuthenticator());
        csh.setRealmName("admin");
        csh.addConstraintMapping(constraintMapping);
        csh.setLoginService(loginService);

        return csh;
    }

    private ServletContextHandler createInternalServlet(ServletContextHandler handler,
                                                        MetricRegistry metrics,
                                                        HealthCheckRegistry healthChecks) {
        handler.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY,
                                                 metrics);
        handler.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                                                 healthChecks);
        handler.addServlet(new NonblockingServletHolder(new AdminServlet()), "/*");

        if (getAdminUsername().isPresent() || getAdminPassword().isPresent()) {
            handler.setSecurityHandler(basicAuthHandler(getAdminUsername().or(""),
                                                        getAdminPassword().or("")));
        }

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

    private Connector createAdminConnector(Server server) {
        final QueuedThreadPool threadPool = new QueuedThreadPool(16, 1);
        threadPool.setName("dw-admin");

        final ServerConnector connector = new ServerConnector(server, threadPool, null, null, 1, 1,
                                                              new HttpConnectionFactory());
        connector.setHost(getBindHost().orNull());
        connector.setPort(getAdminPort());
        connector.setName("admin");

        return connector;
    }

    private Connector createApplicationConnector(Server server, MetricRegistry metrics) {
        // TODO: 4/24/13 <coda> -- add support for SSL, SPDY, etc.

        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setHeaderCacheSize((int) getHeaderCacheSize().toBytes());
        httpConfig.setOutputBufferSize((int) getOutputBufferSize().toBytes());
        httpConfig.setRequestHeaderSize((int) getMaxRequestHeaderSize().toBytes());
        httpConfig.setResponseHeaderSize((int) getMaxResponseHeaderSize().toBytes());
        httpConfig.setSendDateHeader(isDateHeaderEnabled());
        httpConfig.setSendServerVersion(isServerHeaderEnabled());

        if (useForwardedHeaders()) {
            httpConfig.addCustomizer(new ForwardedRequestCustomizer());
        }

        final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfig);
        httpConnectionFactory.setInputBufferSize((int) getInputBufferSize().toBytes());

        final Scheduler scheduler = new ScheduledExecutorScheduler();

        final ByteBufferPool bufferPool = useDirectBuffers() ?
                new MappedByteBufferPool() :
                new ArrayByteBufferPool((int) getMinBufferPoolSize().toBytes(),
                                        (int) getBufferPoolIncrement().toBytes(),
                                        (int) getMaxBufferPoolSize().toBytes());

        final Timer httpTimer = metrics.timer(name(HttpConnectionFactory.class,
                                                   Integer.toString(getPort()),
                                                   "connections"));
        final InstrumentedConnectionFactory instrumentedHttp = new InstrumentedConnectionFactory(
                httpConnectionFactory,
                httpTimer);
        final ServerConnector connector = new ServerConnector(server,
                                                              null,
                                                              scheduler,
                                                              bufferPool,
                                                              getAcceptorThreads(),
                                                              getSelectorThreads(),
                                                              instrumentedHttp);
        connector.setPort(getPort());
        connector.setHost(getBindHost().orNull());
        connector.setAcceptQueueSize(getAcceptQueueSize());
        connector.setReuseAddress(isReuseAddressEnabled());
        for (Duration linger : getSoLingerTime().asSet()) {
            connector.setSoLingerTime((int) linger.toSeconds());
        }
        connector.setIdleTimeout(getIdleTimeout().toMilliseconds());
        connector.setName("application");
        return connector;
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
        final BlockingQueue<Runnable> queue = new BlockingArrayQueue<>(getMinThreads(),
                                                                       getMaxThreads(),
                                                                       getMaxQueuedRequests().or(
                                                                               Integer.MAX_VALUE));
        return new InstrumentedQueuedThreadPool(metricRegistry,
                                                "dw",
                                                getMaxThreads(),
                                                getMinThreads(),
                                                60000,
                                                queue);
    }
}
