package io.dropwizard.server;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.errors.EarlyEofExceptionMapper;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.jersey.validation.ConstraintViolationExceptionMapper;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.setup.HttpEnvironment;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.ExceptionMapper;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultServerFactoryTest {
    private DefaultServerFactory http;

    @Before
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                                                           FileAppenderFactory.class,
                                                           SyslogAppenderFactory.class,
                                                           HttpConnectorFactory.class);

        this.http = new ConfigurationFactory<>(DefaultServerFactory.class,
                                               Validation.buildDefaultValidatorFactory()
                                                                 .getValidator(),
                                               objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/server.yml").toURI()));
    }

    @Test
    public void loadsGzipConfig() throws Exception {
        assertThat(http.getGzipFilterFactory().isEnabled())
                .isFalse();
    }

    @Test
    public void hasAMaximumNumberOfThreads() throws Exception {
        assertThat(http.getMaxThreads())
                .isEqualTo(101);
    }

    @Test
    public void hasAMinimumNumberOfThreads() throws Exception {
        assertThat(http.getMinThreads())
                .isEqualTo(89);
    }

    @Test
    public void hasApplicationContextPath() throws Exception {
        assertThat(http.getApplicationContextPath()).isEqualTo("/app");
    }

    @Test
    public void hasAdminContextPath() throws Exception {
        assertThat(http.getAdminContextPath()).isEqualTo("/admin");
    }

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(DefaultServerFactory.class);
    }

    @Test
    public void registersDefaultExceptionMappers() throws Exception {
        assertThat(http.getRegisterDefaultExceptionMappers()).isTrue();
        HttpEnvironment environment = new HttpEnvironment("test", Jackson.newObjectMapper(),
                Validation.buildDefaultValidatorFactory().getValidator(), new MetricRegistry(),
                ClassLoader.getSystemClassLoader());
        http.build(environment);
        Set<Object> singletons = environment.jersey().getResourceConfig().getSingletons();
        assertThat(singletons).hasAtLeastOneElementOfType(LoggingExceptionMapper.class);
        assertThat(singletons).hasAtLeastOneElementOfType(ConstraintViolationExceptionMapper.class);
        assertThat(singletons).hasAtLeastOneElementOfType(JsonProcessingExceptionMapper.class);
        assertThat(singletons).hasAtLeastOneElementOfType(EarlyEofExceptionMapper.class);

    }

    @Test
    public void doesNotDefaultExceptionMappers() throws Exception {
        http.setRegisterDefaultExceptionMappers(false);
        assertThat(http.getRegisterDefaultExceptionMappers()).isFalse();
        HttpEnvironment environment = new HttpEnvironment("test", Jackson.newObjectMapper(),
                Validation.buildDefaultValidatorFactory().getValidator(), new MetricRegistry(),
                ClassLoader.getSystemClassLoader());
        http.build(environment);
        for (Object singleton : environment.jersey().getResourceConfig().getSingletons()) {
            assertThat(singleton).isNotInstanceOf(ExceptionMapper.class);
        }
    }

    @Test
    public void testGracefulShutdown() throws Exception {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        MetricRegistry metricRegistry = new MetricRegistry();
        HttpEnvironment environment = new HttpEnvironment("test", objectMapper, validator, metricRegistry,
                ClassLoader.getSystemClassLoader());

        CountDownLatch requestReceived = new CountDownLatch(1);
        CountDownLatch shutdownInvoked = new CountDownLatch(1);

        environment.jersey().register(new TestResource(requestReceived, shutdownInvoked));

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        final Server server = http.build(environment);
        
        ((AbstractNetworkConnector)server.getConnectors()[0]).setPort(0);

        ScheduledFuture<Void> cleanup = executor.schedule(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (!server.isStopped()) {
                    server.stop();
                }
                executor.shutdownNow();
                return null;
            }
        }, 5, TimeUnit.SECONDS);


        server.start();

        final int port = ((AbstractNetworkConnector) server.getConnectors()[0]).getLocalPort();

        Future<String> futureResult = executor.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                URL url = new URL("http://localhost:" + port + "/app/test");
                URLConnection connection = url.openConnection();
                connection.connect();
                return CharStreams.toString(new InputStreamReader(connection.getInputStream()));
            }
        });

        requestReceived.await();

        Future<Void> serverStopped = executor.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                server.stop();
                return null;
            }
        });

        Connector[] connectors = server.getConnectors();
        assertThat(connectors).isNotEmpty();
        assertThat(connectors[0]).isInstanceOf(NetworkConnector.class);
        NetworkConnector connector = (NetworkConnector) connectors[0];

        // wait for server to close the connectors
        while (true) {
            if (!connector.isOpen()) {
                shutdownInvoked.countDown();
                break;
            }
            Thread.sleep(5);
        }

        String result = futureResult.get();
        assertThat(result).isEqualTo("test");

        serverStopped.get();

        // cancel the cleanup future since everything succeeded
        cleanup.cancel(false);
        executor.shutdownNow();
    }

    @Path("/test")
    @Produces("text/plain")
    public static class TestResource {

        private final CountDownLatch requestReceived;
        private final CountDownLatch shutdownInvoked;

        public TestResource(CountDownLatch requestReceived, CountDownLatch shutdownInvoked) {
            this.requestReceived = requestReceived;
            this.shutdownInvoked = shutdownInvoked;
        }

        @GET
        public String get() throws Exception {
            requestReceived.countDown();
            shutdownInvoked.await();
            return "test";
        }
    }
}
