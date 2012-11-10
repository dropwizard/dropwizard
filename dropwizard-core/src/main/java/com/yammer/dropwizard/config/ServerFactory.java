package com.yammer.dropwizard.config;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.jetty.BiDiGzipHandler;
import com.yammer.dropwizard.jetty.UnbrandedErrorHandler;
import com.yammer.dropwizard.servlets.ThreadNameFilter;
import com.yammer.dropwizard.tasks.TaskServlet;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.util.Size;
import com.yammer.metrics.HealthChecks;
import com.yammer.metrics.core.HealthCheck;
import com.yammer.metrics.jetty.*;
import com.yammer.metrics.servlet.AdminServlet;
import com.yammer.metrics.util.DeadlockHealthCheck;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.AbstractConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.AbstractNIOConnector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.DispatcherType;
import java.io.File;
import java.security.KeyStore;
import java.util.EnumSet;
import java.util.EventListener;
import java.util.Map;

// TODO: 11/7/11 <coda> -- document ServerFactory
// TODO: 11/7/11 <coda> -- document ServerFactory

public class ServerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerFactory.class);

    private final HttpConfiguration config;
    private final RequestLogHandlerFactory requestLogHandlerFactory;

    public ServerFactory(HttpConfiguration config, String name) {
        this.config = config;
        this.requestLogHandlerFactory = new RequestLogHandlerFactory(config.getRequestLogConfiguration(),
                                                                     name);
    }

    public Server buildServer(Environment env) throws ConfigurationException {
        HealthChecks.defaultRegistry().register(new DeadlockHealthCheck());
        for (HealthCheck healthCheck : env.getHealthChecks()) {
            HealthChecks.defaultRegistry().register(healthCheck);
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

        if (config.getAdminPort() != config.getPort() ) {
            server.addConnector(createInternalConnector());
        }

        server.addBean(new UnbrandedErrorHandler());

        server.setSendDateHeader(config.isDateHeaderEnabled());
        server.setSendServerVersion(config.isServerHeaderEnabled());

        server.setThreadPool(createThreadPool());

        server.setStopAtShutdown(true);

        server.setGracefulShutdown((int) config.getShutdownGracePeriod().toMilliseconds());

        return server;
    }

    private Connector createExternalConnector() {
        final AbstractConnector connector = createConnector(config.getPort());

        connector.setHost(config.getBindHost().orNull());

        connector.setAcceptors(config.getAcceptorThreads());

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

    private AbstractConnector createConnector(int port) {
        final AbstractConnector connector;
        switch (config.getConnectorType()) {
            case BLOCKING:
                connector = new InstrumentedBlockingChannelConnector(port);
                break;
            case LEGACY:
                connector = new InstrumentedSocketConnector(port);
                break;
            case LEGACY_SSL:
                connector = new InstrumentedSslSocketConnector(port);
                break;
            case NONBLOCKING:
                connector = new InstrumentedSelectChannelConnector(port);
                break;
            case NONBLOCKING_SSL:
                connector = new InstrumentedSslSelectChannelConnector(port);
                break;
            default:
                throw new IllegalStateException("Invalid connector type: " + config.getConnectorType());
        }

        if (connector instanceof SslConnector) {
            configureSslContext(((SslConnector) connector).getSslContextFactory());
        }

        if (connector instanceof SelectChannelConnector) {
            ((SelectChannelConnector) connector).setLowResourcesConnections(config.getLowResourcesConnectionThreshold());
        }

        if (connector instanceof AbstractNIOConnector) {
            ((AbstractNIOConnector) connector).setUseDirectBuffers(config.useDirectBuffers());
        }

        return connector;
    }

    private void configureSslContext(SslContextFactory factory) {
        for (File keyStore : config.getSslConfiguration().getKeyStore().asSet()) {
            factory.setKeyStorePath(keyStore.getAbsolutePath());
        }

        for (String password : config.getSslConfiguration().getKeyStorePassword().asSet()) {
            factory.setKeyStorePassword(password);
        }

        for (String password : config.getSslConfiguration().getKeyManagerPassword().asSet()) {
            factory.setKeyManagerPassword(password);
        }

        for (String certAlias : config.getSslConfiguration().getCertAlias().asSet()) {
            factory.setCertAlias(certAlias);
        }

        for (String type : config.getSslConfiguration().getKeyStoreType().asSet()) {
            if (type.startsWith("Windows-")) {
                try {
                    final KeyStore keyStore = KeyStore.getInstance(type);

                    keyStore.load(null, null);
                    factory.setKeyStore(keyStore);

                } catch (Exception e) {
                    throw new IllegalStateException("Windows key store not supported", e);
                }
            } else {
                factory.setKeyStoreType(type);
            }
        }

        factory.setIncludeProtocols(config.getSslConfiguration()
                                          .getSupportedProtocols()
                                          .toArray(new String[config.getSslConfiguration()
                                                                    .getSupportedProtocols()
                                                                    .size()]));
    }


    private Handler createHandler(Environment env) {
        final HandlerCollection collection = new HandlerCollection();

        collection.addHandler(createInternalServlet(env));
        collection.addHandler(createExternalServlet(env));

        if (requestLogHandlerFactory.isEnabled()) {
            collection.addHandler(requestLogHandlerFactory.build());
        }

        return collection;
    }

    private Handler createInternalServlet(Environment env) {
        final ServletContextHandler handler = new ServletContextHandler();
        handler.addServlet(new ServletHolder(new TaskServlet(env.getTasks())), "/tasks/*");
        handler.addServlet(new ServletHolder(new AdminServlet()), "/*");

        if (config.getAdminPort() == config.getPort()) {
            handler.setContextPath("/admin");
            handler.setConnectorNames(new String[]{"main"});
        } else {
            handler.setConnectorNames(new String[]{"internal"});
        }

        if (config.getAdminUsername().isPresent() || config.getAdminPassword().isPresent()) {
            handler.setSecurityHandler(basicAuthHandler(config.getAdminUsername().or(""), config.getAdminPassword().or("")));
        }

        return handler;
    }

    private SecurityHandler basicAuthHandler(String username, String password) {

        final HashLoginService loginService = new HashLoginService();
        loginService.putUser(username, Credential.getCredential(password), new String[] {"user"});
        loginService.setName("admin");

        final Constraint constraint = new Constraint();
        constraint.setName(Constraint.__BASIC_AUTH);
        constraint.setRoles(new String[]{"user"});
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

    private Handler createExternalServlet(Environment env) {
        final ServletContextHandler handler = new ServletContextHandler();
        handler.addFilter(ThreadNameFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));
        handler.setBaseResource(env.getBaseResource());
        if(env.getProtectedTargets().size() > 0) {
            handler.setProtectedTargets(env.getProtectedTargets().toArray(new String[env.getProtectedTargets().size()]));
        }

        for (ImmutableMap.Entry<String, ServletHolder> entry : env.getServlets().entrySet()) {
            handler.addServlet(entry.getValue(), entry.getKey());
        }

        for (ImmutableMap.Entry<String, FilterHolder> entry : env.getFilters().entries()) {
            handler.addFilter(entry.getValue(), entry.getKey(), EnumSet.of(DispatcherType.REQUEST));
        }

        for (EventListener listener : env.getServletListeners()) {
            handler.addEventListener(listener);
        }

        for (Map.Entry<String, String> entry : config.getContextParameters().entrySet()) {
            handler.setInitParameter( entry.getKey(), entry.getValue() );
        }

        handler.setSessionHandler(env.getSessionHandler());

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

            final Optional<ImmutableSet<String>> userAgents = gzip.getExcludedUserAgents();
            if (userAgents.isPresent()) {
                gzipHandler.setExcluded(userAgents.get());
            }

            final Optional<ImmutableSet<String>> mimeTypes = gzip.getCompressedMimeTypes();
            if (mimeTypes.isPresent()) {
                gzipHandler.setMimeTypes(mimeTypes.get());
            }

            return gzipHandler;
        }
        return instrumented;
    }

    private ThreadPool createThreadPool() {
        final InstrumentedQueuedThreadPool pool = new InstrumentedQueuedThreadPool();
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
