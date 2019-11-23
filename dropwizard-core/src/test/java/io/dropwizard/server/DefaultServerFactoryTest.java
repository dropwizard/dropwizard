package io.dropwizard.server;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.ServerPushFilterFactory;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.setup.Environment;
import io.dropwizard.setup.ExceptionMapperBinder;
import io.dropwizard.util.CharStreams;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.server.AbstractNetworkConnector;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.NetworkConnector;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultServerFactoryTest {
    private Environment environment = new Environment("test");
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
                .build(new File(Resources.getResource("yaml/server.yml").toURI()));
    }

    @Test
    void loadsGzipConfig() throws Exception {
        assertThat(http.getGzipFilterFactory().isEnabled())
                .isFalse();
    }

    @Test
    void loadsServerPushConfig() throws Exception {
        final ServerPushFilterFactory serverPush = http.getServerPush();
        assertThat(serverPush.isEnabled()).isTrue();
        assertThat(serverPush.getRefererHosts()).contains("dropwizard.io");
        assertThat(serverPush.getRefererPorts()).contains(8445);
    }

    @Test
    void hasAMaximumNumberOfThreads() throws Exception {
        assertThat(http.getMaxThreads())
                .isEqualTo(101);
    }

    @Test
    void hasAMinimumNumberOfThreads() throws Exception {
        assertThat(http.getMinThreads())
                .isEqualTo(89);
    }

    @Test
    void hasApplicationContextPath() throws Exception {
        assertThat(http.getApplicationContextPath()).isEqualTo("/app");
    }

    @Test
    void hasAdminContextPath() throws Exception {
        assertThat(http.getAdminContextPath()).isEqualTo("/admin");
    }

    @Test
    void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(DefaultServerFactory.class);
    }

    @Test
    void registersDefaultExceptionMappers() throws Exception {
        assertThat(http.getRegisterDefaultExceptionMappers()).isTrue();

        http.build(environment);
        assertThat(environment.jersey().getResourceConfig().getSingletons())
            .filteredOn(x -> x instanceof ExceptionMapperBinder).hasSize(1);
    }

    @Test
    void doesNotDefaultExceptionMappers() throws Exception {
        http.setRegisterDefaultExceptionMappers(false);
        assertThat(http.getRegisterDefaultExceptionMappers()).isFalse();
        Environment environment = new Environment("test");
        http.build(environment);
        assertThat(environment.jersey().getResourceConfig().getSingletons())
            .filteredOn(x -> x instanceof ExceptionMapperBinder).isEmpty();
    }

    @Test
    void defaultsDumpAfterStartFalse() throws Exception {
        assertThat(http.getDumpAfterStart()).isFalse();
        assertThat(http.build(environment).isDumpAfterStart()).isFalse();
    }

    @Test
    void defaultsDumpBeforeStopFalse() throws Exception {
        assertThat(http.getDumpBeforeStop()).isFalse();
        assertThat(http.build(environment).isDumpBeforeStop()).isFalse();
    }

    @Test
    void configuresDumpAfterStart() throws Exception {
        http.setDumpAfterStart(true);
        assertThat(http.build(environment).isDumpAfterStart()).isTrue();
    }

    @Test
    void configuresDumpBeforeExit() throws Exception {
        http.setDumpBeforeStop(true);
        assertThat(http.build(environment).isDumpBeforeStop()).isTrue();
    }

    @Test
    void defaultsDetailedJsonProcessingExceptionToFalse() throws Exception {
        http.build(environment);
        assertThat(environment.jersey().getResourceConfig().getSingletons())
            .filteredOn(x -> x instanceof ExceptionMapperBinder)
            .hasOnlyOneElementSatisfying(x ->
                assertThat(((ExceptionMapperBinder) x).isShowDetails()).isFalse());
    }

    @Test
    void doesNotDefaultDetailedJsonProcessingExceptionToFalse() throws Exception {
        http.setDetailedJsonProcessingExceptionMapper(true);

        http.build(environment);
        assertThat(environment.jersey().getResourceConfig().getSingletons())
            .filteredOn(x -> x instanceof ExceptionMapperBinder)
            .hasOnlyOneElementSatisfying(x ->
                assertThat(((ExceptionMapperBinder) x).isShowDetails()).isTrue());
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

            return CharStreams.toString(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
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

        assertEquals(http.getAdminContextPath(), environment.getAdminContext().getContextPath());
        assertEquals(http.getApplicationContextPath(), environment.getApplicationContext().getContextPath());
    }

    @Test
    void testDeserializeWithoutJsonAutoDetect() {
        final ObjectMapper objectMapper = Jackson.newObjectMapper()
            .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);

        assertThatCode(() -> new YamlConfigurationFactory<>(
            DefaultServerFactory.class,
            BaseValidator.newValidator(),
            objectMapper,
            "dw"
            ).build(new File(Resources.getResource("yaml/server.yml").toURI()))
        ).doesNotThrowAnyException();
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
