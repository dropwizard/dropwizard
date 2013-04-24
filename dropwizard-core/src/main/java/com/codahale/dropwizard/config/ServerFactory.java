package com.codahale.dropwizard.config;

import com.codahale.dropwizard.jersey.JacksonMessageBodyProvider;
import com.codahale.dropwizard.jetty.BiDiGzipHandler;
import com.codahale.dropwizard.jetty.NonblockingServletHolder;
import com.codahale.dropwizard.jetty.UnbrandedErrorHandler;
import com.codahale.dropwizard.servlets.ThreadNameFilter;
import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.util.Size;
import com.codahale.metrics.Clock;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jetty8.*;
import com.codahale.metrics.servlets.AdminServlet;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.sun.jersey.spi.container.servlet.ServletContainer;
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
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.servlet.DispatcherType;
import java.io.File;
import java.net.URI;
import java.security.KeyStore;
import java.util.EnumSet;

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
    private final HttpConfiguration config;
    private final RequestLogHandlerFactory requestLogHandlerFactory;

    public ServerFactory(HttpConfiguration config, String name) {
        this.config = config;
        this.requestLogHandlerFactory = new RequestLogHandlerFactory(config.getRequestLogConfiguration(),
                                                                     name);
    }

    public Server buildServer(Environment env) throws ConfigurationException {
        env.getHealthCheckRegistry().register("deadlocks", new ThreadDeadlockHealthCheck());
        final Server server = createServer(env);
        server.setHandler(createHandler(env));
        return server;
    }

    private Server createServer(Environment env) {
        final Server server = env.getServer();

        server.addConnector(createExternalConnector(env));

        // if we're dynamically allocating ports, no worries if they are the same (i.e. 0)
        if (config.getAdminPort() == 0 || (config.getAdminPort() != config.getPort()) ) {
            server.addConnector(createInternalConnector());
        }

        server.addBean(new UnbrandedErrorHandler());

        server.setSendDateHeader(config.isDateHeaderEnabled());
        server.setSendServerVersion(config.isServerHeaderEnabled());

        server.setThreadPool(createThreadPool(env.getMetricRegistry()));

        server.setStopAtShutdown(true);

        server.setGracefulShutdown((int) config.getShutdownGracePeriod().toMilliseconds());

        return server;
    }

    private Connector createExternalConnector(Environment env) {
        final AbstractConnector connector = createConnector(env.getMetricRegistry(), config.getPort());

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

    private AbstractConnector createConnector(MetricRegistry metricRegistry, int port) {
        final AbstractConnector connector;
        switch (config.getConnectorType()) {
            case BLOCKING:
                connector = new InstrumentedBlockingChannelConnector(metricRegistry,
                                                                     port,
                                                                     Clock.defaultClock());
                break;
            case LEGACY:
                connector = new InstrumentedSocketConnector(metricRegistry,
                                                            port,
                                                            Clock.defaultClock());
                break;
            case LEGACY_SSL:
                connector = new InstrumentedSslSocketConnector(metricRegistry,
                                                               port,
                                                               configureSslContext(),
                                                               Clock.defaultClock());
                break;
            case NONBLOCKING:
                connector = new InstrumentedSelectChannelConnector(metricRegistry,
                                                                   port,
                                                                   Clock.defaultClock());
                break;
            case NONBLOCKING_SSL:
                connector = new InstrumentedSslSelectChannelConnector(metricRegistry,
                                                                      port,
                                                                      configureSslContext(),
                                                                      Clock.defaultClock());
                break;
            default:
                throw new IllegalStateException("Invalid connector type: " + config.getConnectorType());
        }

        if (connector instanceof SelectChannelConnector) {
            ((SelectChannelConnector) connector).setLowResourcesConnections(config.getLowResourcesConnectionThreshold());
        }

        if (connector instanceof AbstractNIOConnector) {
            ((AbstractNIOConnector) connector).setUseDirectBuffers(config.useDirectBuffers());
        }

        return connector;
    }

    private SslContextFactory configureSslContext() {
        final SslContextFactory factory = new SslContextFactory();

        final SslConfiguration sslConfig = config.getSslConfiguration();

        for (File keyStore : sslConfig.getKeyStore().asSet()) {
            factory.setKeyStorePath(keyStore.getAbsolutePath());
        }

        for (String password : sslConfig.getKeyStorePassword().asSet()) {
            factory.setKeyStorePassword(password);
        }

        for (String password : sslConfig.getKeyManagerPassword().asSet()) {
            factory.setKeyManagerPassword(password);
        }

        for (String certAlias : sslConfig.getCertAlias().asSet()) {
            factory.setCertAlias(certAlias);
        }

        final String keyStoreType = sslConfig.getKeyStoreType();
        if (keyStoreType.startsWith("Windows-")) {
            try {
                final KeyStore keyStore = KeyStore.getInstance(keyStoreType);

                keyStore.load(null, null);
                factory.setKeyStore(keyStore);

            } catch (Exception e) {
                throw new IllegalStateException("Windows key store not supported", e);
            }
        } else {
            factory.setKeyStoreType(keyStoreType);
        }

        for (File trustStore : sslConfig.getTrustStore().asSet()) {
            factory.setTrustStore(trustStore.getAbsolutePath());
        }

        for (String password : sslConfig.getTrustStorePassword().asSet()) {
            factory.setTrustStorePassword(password);
        }

        final String trustStoreType = sslConfig.getTrustStoreType();
        if (trustStoreType.startsWith("Windows-")) {
            try {
                final KeyStore keyStore = KeyStore.getInstance(trustStoreType);

                keyStore.load(null, null);
                factory.setTrustStore(keyStore);

            } catch (Exception e) {
                throw new IllegalStateException("Windows key store not supported", e);
            }
        } else {
            factory.setTrustStoreType(trustStoreType);
        }

        for (Boolean needClientAuth : sslConfig.getNeedClientAuth().asSet()) {
            factory.setNeedClientAuth(needClientAuth);
        }

        for (Boolean wantClientAuth : sslConfig.getWantClientAuth().asSet()) {
            factory.setWantClientAuth(wantClientAuth);
        }

        for (Boolean allowRenegotiate : sslConfig.getAllowRenegotiate().asSet()) {
            factory.setAllowRenegotiate(allowRenegotiate);
        }

        for (File crlPath : sslConfig.getCrlPath().asSet()) {
            factory.setCrlPath(crlPath.getAbsolutePath());
        }

        for (Boolean enable : sslConfig.getCrldpEnabled().asSet()) {
            factory.setEnableCRLDP(enable);
        }

        for (Boolean enable : sslConfig.getOcspEnabled().asSet()) {
            factory.setEnableOCSP(enable);
        }

        for (Integer length : sslConfig.getMaxCertPathLength().asSet()) {
            factory.setMaxCertPathLength(length);
        }

        for (URI uri : sslConfig.getOcspResponderUrl().asSet()) {
            factory.setOcspResponderURL(uri.toASCIIString());
        }

        for (String provider : sslConfig.getJceProvider().asSet()) {
            factory.setProvider(provider);
        }

        for (Boolean validate : sslConfig.getValidatePeers().asSet()) {
            factory.setValidatePeerCerts(validate);
        }

        factory.setIncludeProtocols(Iterables.toArray(sslConfig.getSupportedProtocols(),
                                                      String.class));

        return factory;
    }


    private Handler createHandler(Environment env) {
        final HandlerCollection collection = new HandlerCollection();

        collection.addHandler(createExternalServlet(env));
        collection.addHandler(createInternalServlet(env));

        if (requestLogHandlerFactory.isEnabled()) {
            collection.addHandler(requestLogHandlerFactory.build());
        }

        return collection;
    }

    private Handler createInternalServlet(Environment env) {
        final ServletContextHandler handler = env.getAdminContext();
        handler.getServletContext().setAttribute(HealthCheckServlet.HEALTH_CHECK_REGISTRY,
                                                 env.getHealthCheckRegistry());
        handler.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY,
                                                 env.getMetricRegistry());
        handler.addServlet(new ServletHolder(AdminServlet.class), "/*");

        if (config.getAdminPort() != 0 && config.getAdminPort() == config.getPort()) {
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
        final ServletContextHandler handler = env.getServletContext();
        handler.addFilter(ThreadNameFilter.class, "/*", EnumSet.of(DispatcherType.REQUEST));

        final ServletContainer jerseyContainer = env.getJerseyServletContainer();
        if (jerseyContainer != null) {
            env.getJerseyEnvironment().addProvider(
                    new JacksonMessageBodyProvider(env.getJsonEnvironment().build(),
                                                   env.getValidator())
            );
            final ServletHolder jerseyHolder = new NonblockingServletHolder(jerseyContainer);
            jerseyHolder.setInitOrder(Integer.MAX_VALUE);
            handler.addServlet(jerseyHolder, env.getJerseyEnvironment().getUrlPattern());
        }

        handler.setConnectorNames(new String[]{"main"});

        return wrapHandler(env.getMetricRegistry(), handler);
    }

    private Handler wrapHandler(MetricRegistry metricRegistry, ServletContextHandler handler) {
        final InstrumentedHandler instrumented = new InstrumentedHandler(metricRegistry, handler);
        final GzipConfiguration gzip = config.getGzipConfiguration();
        if (gzip.isEnabled()) {
            final BiDiGzipHandler gzipHandler = new BiDiGzipHandler(instrumented);

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
        return instrumented;
    }

    private ThreadPool createThreadPool(MetricRegistry metricRegistry) {
        final InstrumentedQueuedThreadPool pool = new InstrumentedQueuedThreadPool(metricRegistry);
        pool.setMinThreads(config.getMinThreads());
        pool.setMaxThreads(config.getMaxThreads());
        return pool;
    }

    private Connector createInternalConnector() {
        final SocketConnector connector = new SocketConnector();
        connector.setHost(config.getBindHost().orNull());
        connector.setPort(config.getAdminPort());
        connector.setName("internal");
        connector.setThreadPool(new QueuedThreadPool(8));
        return connector;
    }
}
