package com.yammer.dropwizard.config;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.jetty.BiDiGzipHandler;
import com.yammer.dropwizard.jetty.QuietErrorHandler;
import com.yammer.dropwizard.tasks.TaskServlet;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.util.Size;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.DeadlockHealthCheck;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.jetty.InstrumentedHandler;
import com.yammer.metrics.reporting.MetricsServlet;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.AbstractNIOConnector;
import org.eclipse.jetty.server.nio.BlockingChannelConnector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static com.yammer.dropwizard.config.HttpConfiguration.GzipConfiguration;

// TODO: 11/7/11 <coda> -- document ServerFactory
// TODO: 11/7/11 <coda> -- document ServerFactory

public class ServerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFactory.class);

    private final HttpConfiguration config;
    private final RequestLogHandlerFactory requestLogHandlerFactory;

    public ServerFactory(HttpConfiguration config) {
        this.config = config;
        this.requestLogHandlerFactory = new RequestLogHandlerFactory(config.getRequestLogConfiguration());
    }

    public Server buildServer(Environment env) throws ConfigurationException {
        HealthChecks.register(new DeadlockHealthCheck());
        for (HealthCheck healthCheck : env.getHealthChecks()) {
            HealthChecks.register(healthCheck);
        }

        if (env.getHealthChecks().isEmpty()) {
            LOGGER.warn('\n' +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                "!    THIS SERVICE HAS NO HEALTHCHECKS. THIS MEANS YOU WILL NEVER KNOW IF IT    !\n" +
                "!    DIES IN PRODUCTION, WHICH MEANS YOU WILL NEVER KNOW IF YOU'RE LETTING     !\n" +
                "!     YOUR USERS DOWN. YOU SHOULD ADD A HEALTHCHECK FOR EACH DEPENDENCY OF     !\n" +
                "!     YOUR SERVICE WHICH FULLY (BUT LIGHTLY) TESTS YOUR SERVICE'S ABILITY TO   !\n" +
                "!      USE THAT SERVICE. THINK OF IT AS A CONTINUOUS INTEGRATION TEST.         !\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
            );
        }

        final Server server = createServer();
        server.setHandler(createHandler(env));
        server.addBean(env);
        return server;
    }

    private Server createServer() {
        final Server server = new Server();

        server.addConnector(createExternalConnector());
        server.addConnector(createInternalConnector());

        server.addBean(new QuietErrorHandler());

        server.setSendDateHeader(config.isDateHeaderEnabled());
        server.setSendServerVersion(config.isServerHeaderEnabled());

        server.setThreadPool(createThreadPool());

        server.setStopAtShutdown(true);

        server.setGracefulShutdown((int) config.getShutdownGracePeriod().toMilliseconds());

        return server;
    }

    private Connector createExternalConnector() {
        final AbstractConnector connector = createConnector();

        connector.setHost(config.getBindHost().orNull());

        connector.setAcceptors(config.getAcceptorThreadCount());

        connector.setForwarded(config.useForwardedHeaders());

        connector.setMaxIdleTime((int) config.getMaxIdleTime().toMilliseconds());

        connector.setLowResourcesMaxIdleTime((int) config.getLowResourcesMaxIdleTime()
                                                         .toMilliseconds());

        connector.setAcceptorPriorityOffset(config.getAcceptorThreadPriorityOffset());

        connector.setAcceptQueueSize(config.getAcceptQueueSize());

        connector.setMaxBuffers(config.getMaxBufferCount());

        connector.setRequestBufferSize((int) config.getRequestBufferSize().toBytes());

        connector.setRequestHeaderSize((int) config.getRequestHeaderBufferSize().toBytes());

        connector.setResponseBufferSize((int) config.getResponseBufferSize().toBytes());

        connector.setResponseHeaderSize((int) config.getResponseHeaderBufferSize().toBytes());

        connector.setReuseAddress(config.isReuseAddressEnabled());
        
        final Optional<Duration> lingerTime = config.getSoLingerTime();

        if (lingerTime.isPresent()) {
            connector.setSoLingerTime((int) lingerTime.get().toMilliseconds());
        }

        connector.setPort(config.getPort());
        connector.setName("main");

        return connector;
    }

    private AbstractConnector createConnector() {
        final AbstractConnector connector;
        switch (config.getConnectorType()) {
            case BLOCKING_CHANNEL:
                connector = new BlockingChannelConnector();
                break;
            case SOCKET:
                connector = new SocketConnector();
                break;
            case SELECT_CHANNEL:
                connector = new SelectChannelConnector();
                ((SelectChannelConnector) connector).setLowResourcesConnections(config.getLowResourcesConnectionThreshold());
                break;
            default:
                throw new IllegalStateException("Invalid connector type: " + config.getConnectorType());
        }

        if (connector instanceof AbstractNIOConnector) {
            ((AbstractNIOConnector) connector).setUseDirectBuffers(config.useDirectBuffers());
        }

        return connector;
    }


    private Handler createHandler(Environment env) {
        final HandlerCollection collection = new HandlerCollection();

        collection.addHandler(createExternalServlet(env.getServlets(), env.getFilters()));
        collection.addHandler(createInternalServlet(env));

        if (requestLogHandlerFactory.isEnabled()) {
            collection.addHandler(requestLogHandlerFactory.build());
        }

        return collection;
    }

    private static Handler createInternalServlet(Environment env) {
        final ServletContextHandler handler = new ServletContextHandler();
        handler.addServlet(new ServletHolder(new TaskServlet(env.getTasks())), "/tasks/*");
        handler.addServlet(new ServletHolder(new MetricsServlet()), "/*");
        handler.setConnectorNames(new String[]{"internal"});
        return handler;
    }

    private Handler createExternalServlet(Map<String, ServletHolder> servlets,
                                          Map<String, FilterHolder> filters) {
        final ServletContextHandler handler = new ServletContextHandler();
        handler.setBaseResource(Resource.newClassPathResource("."));

        for (Map.Entry<String, ServletHolder> entry : servlets.entrySet()) {
            handler.addServlet(entry.getValue(), entry.getKey());
        }

        for (Map.Entry<String, FilterHolder> entry : filters.entrySet()) {
            handler.addFilter(entry.getValue(), entry.getKey(), EnumSet.of(DispatcherType.REQUEST));
        }

        handler.setConnectorNames(new String[]{"main"});

        return wrapHandler(handler);
    }

    private Handler wrapHandler(ServletContextHandler handler) {
        final InstrumentedHandler instrumented = new InstrumentedHandler(handler);
        final GzipConfiguration gzip = config.getGzipConfiguration();
        if (gzip.isEnabled()) {
            final BiDiGzipHandler gzipHandler = new BiDiGzipHandler(instrumented);

            final Optional<Size> minEntitySize = gzip.getMinimumEntitySize();
            if (minEntitySize.isPresent()) {
                gzipHandler.setMinGzipSize((int) minEntitySize.get().toBytes());
            }

            final Optional<Size> bufferSize = gzip.getBufferSize();
            if (bufferSize.isPresent()) {
                gzipHandler.setBufferSize((int) bufferSize.get().toBytes());
            }

            final Optional<List<String>> userAgents = gzip.getExcludedUserAgents();
            if (userAgents.isPresent()) {
                gzipHandler.setExcluded(ImmutableSet.copyOf(userAgents.get()));
            }

            final Optional<List<String>> mimeTypes = gzip.getCompressedMimeTypes();
            if (mimeTypes.isPresent()) {
                gzipHandler.setMimeTypes(ImmutableSet.copyOf(mimeTypes.get()));
            }

            return gzipHandler;
        }
        return instrumented;
    }

    private ThreadPool createThreadPool() {
        final QueuedThreadPool pool = new QueuedThreadPool();
        pool.setMinThreads(config.getMinThreads());
        pool.setMaxThreads(config.getMaxThreads());
        return pool;
    }

    private Connector createInternalConnector() {
        final SocketConnector connector = new SocketConnector();
        connector.setPort(config.getAdminPort());
        connector.setName("internal");
        connector.setThreadPool(new QueuedThreadPool(8));
        return connector;
    }
}
