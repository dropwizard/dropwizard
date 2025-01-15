package io.dropwizard.core.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.core.setup.ExceptionMapperBinder;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.common.FileAppenderFactory;
import io.dropwizard.logging.common.SyslogAppenderFactory;
import io.dropwizard.validation.BaseValidator;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.annotation.ResponseMeteredLevel.ALL;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultServerFactoryTest {
    private final Environment environment = new Environment("test");
    private DefaultServerFactory http;

    @BeforeEach
    void setUp() throws Exception {

        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                                                           FileAppenderFactory.class,
                                                           SyslogAppenderFactory.class,
                                                           HttpConnectorFactory.class);

        http = new YamlConfigurationFactory<>(DefaultServerFactory.class,
                                              BaseValidator.newValidator(),
                                              objectMapper, "dw")
                .build(new ResourceConfigurationSourceProvider(), "yaml/server.yml");
    }

    @Test
    void loadsGzipConfig() {
        assertThat(http.getGzipFilterFactory().isEnabled())
                .isFalse();
    }

    @Test
    void hasAMaximumNumberOfThreads() {
        assertThat(http.getMaxThreads())
                .isEqualTo(101);
    }

    @Test
    void hasAMinimumNumberOfThreads() {
        assertThat(http.getMinThreads())
                .isEqualTo(89);
    }

    @Test
    void hasResponseMeteredLevel() {
        assertThat(http.getResponseMeteredLevel())
            .isEqualTo(ALL);
    }

    @Test
    void hasMetricPrefix() {
        assertThat(http.getMetricPrefix())
            .isEqualTo("jetty");
    }

    @Test
    void hasApplicationContextPath() {
        assertThat(http.getApplicationContextPath()).isEqualTo("/app");
    }

    @Test
    void hasAdminContextPath() {
        assertThat(http.getAdminContextPath()).isEqualTo("/admin");
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(DefaultServerFactory.class);
    }

    @Test
    void registersDefaultExceptionMappers() {
        assertThat(http.getRegisterDefaultExceptionMappers()).isTrue();

        http.build(environment);
        assertThat(environment.jersey().getResourceConfig().getSingletons())
            .filteredOn(x -> x instanceof ExceptionMapperBinder).hasSize(1);
    }

    @Test
    void doesNotDefaultExceptionMappers() {
        http.setRegisterDefaultExceptionMappers(false);
        assertThat(http.getRegisterDefaultExceptionMappers()).isFalse();
        Environment environment = new Environment("test");
        http.build(environment);
        assertThat(environment.jersey().getResourceConfig().getSingletons())
            .filteredOn(x -> x instanceof ExceptionMapperBinder).isEmpty();
    }

    @Test
    void defaultsDumpAfterStartFalse() {
        assertThat(http.getDumpAfterStart()).isFalse();
        assertThat(http.build(environment).isDumpAfterStart()).isFalse();
    }

    @Test
    void defaultsDumpBeforeStopFalse() {
        assertThat(http.getDumpBeforeStop()).isFalse();
        assertThat(http.build(environment).isDumpBeforeStop()).isFalse();
    }

    @Test
    void configuresDumpAfterStart() {
        http.setDumpAfterStart(true);
        assertThat(http.build(environment).isDumpAfterStart()).isTrue();
    }

    @Test
    void configuresDumpBeforeExit() {
        http.setDumpBeforeStop(true);
        assertThat(http.build(environment).isDumpBeforeStop()).isTrue();
    }

    @Test
    void defaultsDetailedJsonProcessingExceptionToFalse() {
        http.build(environment);
        assertThat(environment.jersey().getResourceConfig().getSingletons())
            .filteredOn(x -> x instanceof ExceptionMapperBinder)
            .map(x -> (ExceptionMapperBinder) x)
            .singleElement()
            .satisfies(x -> assertThat(x.isShowDetails()).isFalse());
    }

    @Test
    void doesNotDefaultDetailedJsonProcessingExceptionToFalse() {
        http.setDetailedJsonProcessingExceptionMapper(true);

        http.build(environment);
        assertThat(environment.jersey().getResourceConfig().getSingletons())
            .filteredOn(x -> x instanceof ExceptionMapperBinder)
            .map(x -> (ExceptionMapperBinder) x)
            .singleElement()
            .satisfies(x -> assertThat(x.isShowDetails()).isTrue());
    }

    @Test
    void testGracefulShutdown() throws Exception {
        CountDownLatch requestReceived = new CountDownLatch(1);
        CountDownLatch shutdownInvoked = new CountDownLatch(1);

        environment.jersey().register(new TestResource(requestReceived, shutdownInvoked));

        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(3);
        http.configure(environment);
        final Server server = http.build(environment);

        ((AbstractNetworkConnector) server.getConnectors()[0]).setPort(0);
        ((AbstractNetworkConnector) server.getConnectors()[1]).setPort(0);

        ScheduledFuture<Void> cleanup = executor.schedule(() -> {
            if (!server.isStopped()) {
                server.stop();
            }
            executor.shutdownNow();
            return null;
        }, 5, TimeUnit.SECONDS);


        server.start();

        final int port = ((AbstractNetworkConnector) server.getConnectors()[0]).getLocalPort();

        Future<String> futureResult = executor.submit(() -> {
            URL url = new URL("http://localhost:" + port + "/app/test");
            URLConnection connection = url.openConnection();
            connection.connect();
            try (InputStream in = connection.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        });

        requestReceived.await(10, TimeUnit.SECONDS);

        Future<Void> serverStopped = executor.submit(() -> {
            server.stop();
            return null;
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

    @Test
    void testConfiguredEnvironment() {
        http.configure(environment);

        assertThat(environment.getAdminContext().getContextPath()).isEqualTo(http.getAdminContextPath());
        assertThat(environment.getApplicationContext().getContextPath()).isEqualTo(http.getApplicationContextPath());
    }

    @Test
    void testDeserializeWithoutJsonAutoDetect() throws ConfigurationException, IOException {
        final ObjectMapper objectMapper = Jackson.newObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);

        assertThat(new YamlConfigurationFactory<>(
                DefaultServerFactory.class,
                BaseValidator.newValidator(),
                objectMapper,
                "dw"
                ).build(new ResourceConfigurationSourceProvider(), "yaml/server.yml")
                .getMaxThreads())
            .isEqualTo(101);
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
