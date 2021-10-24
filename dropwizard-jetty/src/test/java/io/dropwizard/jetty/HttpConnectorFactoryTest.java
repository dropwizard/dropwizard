package io.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.util.DataSize;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.eclipse.jetty.http.CookieCompliance;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ProxyConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.validation.Validator;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class HttpConnectorFactoryTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator validator = BaseValidator.newValidator();

    @BeforeEach
    void setUp() {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                FileAppenderFactory.class, SyslogAppenderFactory.class, HttpConnectorFactory.class);
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(HttpConnectorFactory.class);
    }

    @Test
    void testParseMinimalConfiguration() throws Exception {
        HttpConnectorFactory http =
                new YamlConfigurationFactory<>(HttpConnectorFactory.class, validator, objectMapper, "dw")
                        .build(new ResourceConfigurationSourceProvider(), "yaml/http-connector-minimal.yml");

        assertThat(http.getPort()).isEqualTo(8080);
        assertThat(http.getBindHost()).isNull();
        assertThat(http.isInheritChannel()).isFalse();
        assertThat(http.getHeaderCacheSize()).isEqualTo(DataSize.bytes(512));
        assertThat(http.getOutputBufferSize()).isEqualTo(DataSize.kibibytes(32));
        assertThat(http.getMaxRequestHeaderSize()).isEqualTo(DataSize.kibibytes(8));
        assertThat(http.getMaxResponseHeaderSize()).isEqualTo(DataSize.kibibytes(8));
        assertThat(http.getInputBufferSize()).isEqualTo(DataSize.kibibytes(8));
        assertThat(http.getIdleTimeout()).isEqualTo(Duration.seconds(30));
        assertThat(http.getMinBufferPoolSize()).isEqualTo(DataSize.bytes(64));
        assertThat(http.getBufferPoolIncrement()).isEqualTo(DataSize.bytes(1024));
        assertThat(http.getMaxBufferPoolSize()).isEqualTo(DataSize.kibibytes(64));
        assertThat(http.getMinRequestDataPerSecond()).isEqualTo(DataSize.bytes(0));
        assertThat(http.getMinResponseDataPerSecond()).isEqualTo(DataSize.bytes(0));
        assertThat(http.getAcceptorThreads()).isEmpty();
        assertThat(http.getSelectorThreads()).isEmpty();
        assertThat(http.getAcceptQueueSize()).isNull();
        assertThat(http.isReuseAddress()).isTrue();
        assertThat(http.isUseServerHeader()).isFalse();
        assertThat(http.isUseDateHeader()).isTrue();
        assertThat(http.isUseForwardedHeaders()).isFalse();
        assertThat(http.getHttpCompliance()).isEqualTo(HttpCompliance.RFC7230);
        assertThat(http.getRequestCookieCompliance()).isEqualTo(CookieCompliance.RFC6265);
        assertThat(http.getResponseCookieCompliance()).isEqualTo(CookieCompliance.RFC6265);
    }

    @Test
    void testParseFullConfiguration() throws Exception {
        HttpConnectorFactory http =
                new YamlConfigurationFactory<>(HttpConnectorFactory.class, validator, objectMapper, "dw")
                        .build(new ResourceConfigurationSourceProvider(), "yaml/http-connector.yml");

        assertThat(http.getPort()).isEqualTo(9090);
        assertThat(http.getBindHost()).isEqualTo("127.0.0.1");
        assertThat(http.isInheritChannel()).isTrue();
        assertThat(http.getHeaderCacheSize()).isEqualTo(DataSize.bytes(256));
        assertThat(http.getOutputBufferSize()).isEqualTo(DataSize.kibibytes(128));
        assertThat(http.getMaxRequestHeaderSize()).isEqualTo(DataSize.kibibytes(4));
        assertThat(http.getMaxResponseHeaderSize()).isEqualTo(DataSize.kibibytes(4));
        assertThat(http.getInputBufferSize()).isEqualTo(DataSize.kibibytes(4));
        assertThat(http.getIdleTimeout()).isEqualTo(Duration.seconds(10));
        assertThat(http.getMinBufferPoolSize()).isEqualTo(DataSize.bytes(128));
        assertThat(http.getBufferPoolIncrement()).isEqualTo(DataSize.bytes(500));
        assertThat(http.getMaxBufferPoolSize()).isEqualTo(DataSize.kibibytes(32));
        assertThat(http.getMinRequestDataPerSecond()).isEqualTo(DataSize.bytes(42));
        assertThat(http.getMinResponseDataPerSecond()).isEqualTo(DataSize.bytes(200));
        assertThat(http.getAcceptorThreads()).contains(1);
        assertThat(http.getSelectorThreads()).contains(4);
        assertThat(http.getAcceptQueueSize()).isEqualTo(1024);
        assertThat(http.isReuseAddress()).isFalse();
        assertThat(http.isUseServerHeader()).isTrue();
        assertThat(http.isUseDateHeader()).isFalse();
        assertThat(http.isUseForwardedHeaders()).isTrue();
        HttpConfiguration httpConfiguration = http.buildHttpConfiguration();
        assertThat(httpConfiguration.getCustomizers()).hasAtLeastOneElementOfType(ForwardedRequestCustomizer.class);
        assertThat(http.getHttpCompliance()).isEqualTo(HttpCompliance.RFC2616);
        assertThat(http.getRequestCookieCompliance()).isEqualTo(CookieCompliance.RFC2965);
        assertThat(http.getResponseCookieCompliance()).isEqualTo(CookieCompliance.RFC6265);
    }

    @Test
    void testBuildConnector() throws Exception {
        HttpConnectorFactory http = spy(new HttpConnectorFactory());
        http.setBindHost("127.0.0.1");
        http.setAcceptorThreads(Optional.of(1));
        http.setSelectorThreads(Optional.of(2));
        http.setAcceptQueueSize(1024);
        http.setMinResponseDataPerSecond(DataSize.bytes(200));
        http.setMinRequestDataPerSecond(DataSize.bytes(42));
        http.setRequestCookieCompliance(CookieCompliance.RFC6265);
        http.setResponseCookieCompliance(CookieCompliance.RFC6265);

        MetricRegistry metrics = new MetricRegistry();
        ThreadPool threadPool = new QueuedThreadPool();
        Server server = null;
        ServerConnector connector = null;

        try {
            server = new Server();
            connector = (ServerConnector) http.build(server, metrics, "test-http-connector", threadPool);

            assertThat(connector.getPort()).isEqualTo(8080);
            assertThat(connector.getHost()).isEqualTo("127.0.0.1");
            assertThat(connector.getAcceptQueueSize()).isEqualTo(1024);
            assertThat(connector.getReuseAddress()).isTrue();
            assertThat(connector.getIdleTimeout()).isEqualTo(30000);
            assertThat(connector.getName()).isEqualTo("test-http-connector");

            assertThat(connector.getServer()).isSameAs(server);
            assertThat(connector.getScheduler()).isInstanceOf(ScheduledExecutorScheduler.class);
            assertThat(connector.getExecutor()).isSameAs(threadPool);

            verify(http).buildBufferPool(64, 1024, 64 * 1024);

            assertThat(connector.getAcceptors()).isEqualTo(1);
            assertThat(connector.getSelectorManager().getSelectorCount()).isEqualTo(2);

            InstrumentedConnectionFactory connectionFactory =
                    (InstrumentedConnectionFactory) connector.getConnectionFactory("http/1.1");
            assertThat(connectionFactory).isInstanceOf(InstrumentedConnectionFactory.class);
            assertThat(connectionFactory)
                    .extracting("connectionFactory")
                    .asInstanceOf(InstanceOfAssertFactories.type(HttpConnectionFactory.class))
                    .satisfies(factory -> {
                        assertThat(factory.getInputBufferSize()).isEqualTo(8192);
                        assertThat(factory.getHttpCompliance()).isEqualByComparingTo(HttpCompliance.RFC7230);
                    })
                    .extracting(HttpConnectionFactory::getHttpConfiguration)
                    .satisfies(config -> {
                        assertThat(config.getHeaderCacheSize()).isEqualTo(512);
                        assertThat(config.getOutputBufferSize()).isEqualTo(32768);
                        assertThat(config.getRequestHeaderSize()).isEqualTo(8192);
                        assertThat(config.getResponseHeaderSize()).isEqualTo(8192);
                        assertThat(config.getSendDateHeader()).isTrue();
                        assertThat(config.getSendServerVersion()).isFalse();
                        assertThat(config.getCustomizers()).noneMatch(customizer -> customizer.getClass().equals(ForwardedRequestCustomizer.class));
                        assertThat(config.getMinRequestDataRate()).isEqualTo(42);
                        assertThat(config.getMinResponseDataRate()).isEqualTo(200);
                        assertThat(config.getRequestCookieCompliance()).isEqualTo(CookieCompliance.RFC6265);
                        assertThat(config.getResponseCookieCompliance()).isEqualTo(CookieCompliance.RFC6265);
                    });
        } finally {
            if (connector != null) {
                connector.stop();
            }
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    void testBuildConnectorWithProxyProtocol() throws Exception {
        HttpConnectorFactory http = new HttpConnectorFactory();
        http.setBindHost("127.0.0.1");
        http.setUseProxyProtocol(true);

        MetricRegistry metrics = new MetricRegistry();
        ThreadPool threadPool = new QueuedThreadPool();
        Server server = null;
        ServerConnector connector = null;

        try {
            server = new Server();
            connector = (ServerConnector) http.build(server, metrics, "test-http-connector-with-proxy-protocol", threadPool);

            assertThat(connector.getConnectionFactories().toArray()[0]).isInstanceOf(ProxyConnectionFactory.class);
        } finally {
            if (connector != null) {
                connector.stop();
            }
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    void testDefaultAcceptQueueSize() throws Exception {
        HttpConnectorFactory http = new HttpConnectorFactory();
        http.setBindHost("127.0.0.1");
        http.setAcceptorThreads(Optional.of(1));
        http.setSelectorThreads(Optional.of(2));

        MetricRegistry metrics = new MetricRegistry();
        ThreadPool threadPool = new QueuedThreadPool();
        Server server = null;
        ServerConnector connector = null;

        try {
            server = new Server();
            connector = (ServerConnector) http.build(server, metrics, "test-http-connector", threadPool);
            assertThat(connector.getAcceptQueueSize()).isEqualTo(NetUtil.getTcpBacklog());
        } finally {
            if (connector != null) {
                connector.stop();
            }
            if (server != null) {
                server.stop();
            }
        }
    }
}
