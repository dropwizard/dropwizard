package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.config.GzipConfiguration;
import com.codahale.dropwizard.config.ServerConfiguration;
import com.codahale.dropwizard.configuration.ConfigurationException;
import com.codahale.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import com.codahale.dropwizard.jetty.*;
import com.codahale.dropwizard.servlets.ThreadNameFilter;
import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.util.Size;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.codahale.metrics.jetty9.InstrumentedQueuedThreadPool;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.concurrent.BlockingQueue;

import static com.codahale.metrics.MetricRegistry.name;

/*
 * A factory for creating instances of {@link org.eclipse.jetty.server.Server} and configuring Servlets
 * 
 * Registers {@link com.codahale.metrics.core.HealthCheck}s, both default and user defined
 * 
 * Creates instances of {@link org.eclipse.jetty.server.Connector},
 * configured by {@link com.codahale.dropwizard.config.HttpConfiguration} for external and admin port
 * 
 * Registers {@link org.eclipse.jetty.server.Handler}s for admin and service Servlets.
 * {@link TaskServlet} 
 * {@link AdminServlet}
 * {@link com.sun.jersey.spi.container.servlet.ServletContainer} with all resources in {@link DropwizardResourceConfig} 
 * 
 * */
public class ServerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFactory.class);

    private final ServerConfiguration config;
    private final RequestLogHandlerFactory requestLogHandlerFactory;

    public ServerFactory(ServerConfiguration config, String name) {
        this.config = config;
        this.requestLogHandlerFactory = new RequestLogHandlerFactory(name,
                                                                     config.getRequestLogConfiguration()
                                                                           .getOutputs(),
                                                                     config.getRequestLogConfiguration()
                                                                           .getTimeZone());
    }

    public Server build(Environment env) throws ConfigurationException {
        env.getHealthCheckRegistry().register("deadlocks", new ThreadDeadlockHealthCheck());
        return createServer(env);
    }

    private Server createServer(Environment env) {
        final ThreadPool threadPool = createThreadPool(env.getMetricRegistry());
        final Server server = new Server(threadPool);
        env.getLifecycleEnvironment().attach(server);

        final ServletContextHandler applicationHandler = createExternalServlet(env);
        final ServletContextHandler adminHandler = createInternalServlet(env);

        final Connector applicationConnector = createApplicationConnector(server, env);
        server.addConnector(applicationConnector);

        final Connector adminConnector;
        // if we're dynamically allocating ports, no worries if they are the same (i.e. 0)
        if (config.getAdminPort() == 0 || (config.getAdminPort() != config.getPort())) {
            adminConnector = createAdminConnector(server);
            server.addConnector(adminConnector);
        } else {
            adminConnector = applicationConnector;
        }

        if (config.getAdminUsername().isPresent() || config.getAdminPassword().isPresent()) {
            adminHandler.setSecurityHandler(basicAuthHandler(config.getAdminUsername().or(""),
                                                             config.getAdminPassword().or("")));
        }

        final Handler handler = createHandler(applicationConnector,
                                              applicationHandler,
                                              adminConnector,
                                              adminHandler);
        if (requestLogHandlerFactory.isEnabled()) {
            final RequestLogHandler requestLogHandler = requestLogHandlerFactory.build();
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

    private ServletContextHandler createInternalServlet(Environment env) {
        final ServletContextHandler handler = env.getAdminContext();
        handler.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                                                 env.getHealthCheckRegistry());
        handler.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY,
                                                 env.getMetricRegistry());
        handler.addServlet(new ServletHolder(AdminServlet.class), "/*");

        if (config.getAdminUsername().isPresent() || config.getAdminPassword().isPresent()) {
            handler.setSecurityHandler(basicAuthHandler(config.getAdminUsername().or(""),
                                                        config.getAdminPassword().or("")));
        }

        return handler;
    }

    private ServletContextHandler createExternalServlet(Environment env) {
        final ServletContextHandler handler = env.getServletContext();
        handler.addFilter(ThreadNameFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        final ServletContainer jerseyContainer = env.getJerseyServletContainer();
        if (jerseyContainer != null) {
            env.getJerseyEnvironment().addProvider(
                    new JacksonMessageBodyProvider(env.getObjectMapper(),
                                                   env.getValidator())
            );
            final ServletHolder jerseyHolder = new NonblockingServletHolder(jerseyContainer);
            jerseyHolder.setInitOrder(Integer.MAX_VALUE);
            handler.addServlet(jerseyHolder, env.getJerseyEnvironment().getUrlPattern());
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
        connector.setHost(config.getBindHost().orNull());
        connector.setPort(config.getAdminPort());
        connector.setName("admin");

        return connector;
    }

    private Connector createApplicationConnector(Server server, Environment env) {
        // TODO: 4/24/13 <coda> -- add support for SSL, SPDY, etc.

        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setHeaderCacheSize((int) config.getHeaderCacheSize().toBytes());
        httpConfig.setOutputBufferSize((int) config.getOutputBufferSize().toBytes());
        httpConfig.setRequestHeaderSize((int) config.getMaxRequestHeaderSize().toBytes());
        httpConfig.setResponseHeaderSize((int) config.getMaxResponseHeaderSize().toBytes());
        httpConfig.setSendDateHeader(config.isDateHeaderEnabled());
        httpConfig.setSendServerVersion(config.isServerHeaderEnabled());

        if (config.useForwardedHeaders()) {
            httpConfig.addCustomizer(new ForwardedRequestCustomizer());
        }

        final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfig);
        httpConnectionFactory.setInputBufferSize((int) config.getInputBufferSize().toBytes());

        final Scheduler scheduler = new ScheduledExecutorScheduler();

        final ByteBufferPool bufferPool = config.useDirectBuffers() ?
                new MappedByteBufferPool() :
                new ArrayByteBufferPool((int) config.getMinBufferPoolSize().toBytes(),
                                        (int) config.getBufferPoolIncrement().toBytes(),
                                        (int) config.getMaxBufferPoolSize().toBytes());

        final Timer httpTimer = env.getMetricRegistry()
                                   .timer(name(HttpConnectionFactory.class,
                                               Integer.toString(config.getPort()),
                                               "connections"));
        final InstrumentedConnectionFactory instrumentedHttp = new InstrumentedConnectionFactory(
                httpConnectionFactory,
                httpTimer);
        final ServerConnector connector = new ServerConnector(server,
                                                              null,
                                                              scheduler,
                                                              bufferPool,
                                                              config.getAcceptorThreads(),
                                                              config.getSelectorThreads(),
                                                              instrumentedHttp);
        connector.setPort(config.getPort());
        connector.setHost(config.getBindHost().orNull());
        connector.setAcceptQueueSize(config.getAcceptQueueSize());
        connector.setReuseAddress(config.isReuseAddressEnabled());
        for (Duration linger : config.getSoLingerTime().asSet()) {
            connector.setSoLingerTime((int) linger.toSeconds());
        }
        connector.setIdleTimeout(config.getIdleTimeout().toMilliseconds());
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
//        final InstrumentedHandler instrumented = new InstrumentedHandler(handler);
        final GzipConfiguration gzip = config.getGzipConfiguration();
        if (gzip.isEnabled()) {
            final BiDiGzipHandler gzipHandler = new BiDiGzipHandler(handler);

            final Size minEntitySize = gzip.getMinimumEntitySize();
            gzipHandler.setMinGzipSize((int) minEntitySize.toBytes());

            final Size bufferSize = gzip.getBufferSize();
            gzipHandler.setBufferSize((int) bufferSize.toBytes());

            final ImmutableSet<String> userAgents = gzip.getExcludedUserAgents();
            if (!userAgents.isEmpty()) {
                gzipHandler.setExcluded(userAgents);
            }

            final ImmutableSet<String> mimeTypes = gzip.getCompressedMimeTypes();
            if (!mimeTypes.isEmpty()) {
                gzipHandler.setMimeTypes(mimeTypes);
            }

            return gzipHandler;
        }

        return handler;
    }

    private ThreadPool createThreadPool(MetricRegistry metricRegistry) {
        final BlockingQueue<Runnable> queue = new BlockingArrayQueue<>(config.getMinThreads(),
                                                                       config.getMaxThreads(),
                                                                       config.getMaxQueuedRequests()
                                                                             .or(Integer.MAX_VALUE));
        return new InstrumentedQueuedThreadPool(metricRegistry,
                                                "dw",
                                                config.getMaxThreads(),
                                                config.getMinThreads(),
                                                60000,
                                                queue);
    }
}
