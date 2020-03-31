package io.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.util.DataSize;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.http.CookieCompliance;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
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
import java.io.File;
import java.util.Optional;

import static org.apache.commons.lang3.reflect.FieldUtils.getField;
import static org.assertj.core.api.Assertions.assertThat;

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
                .build(new File(Resources.getResource("yaml/http-connector-minimal.yml").toURI()));

        assertThat(http.getPort()).isEqualTo(8080);
        assertThat(http.getBindHost()).isNull();
        assertThat(http.isInheritChannel()).isEqualTo(false);
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
                .build(new File(Resources.getResource("yaml/http-connector.yml").toURI()));

        assertThat(http.getPort()).isEqualTo(9090);
        assertThat(http.getBindHost()).isEqualTo("127.0.0.1");
        assertThat(http.isInheritChannel()).isEqualTo(true);
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
        HttpConnectorFactory http = new HttpConnectorFactory();
        http.setBindHost("127.0.0.1");
        http.setAcceptorThreads(Optional.of(1));
        http.setSelectorThreads(Optional.of(2));
        http.setAcceptQueueSize(1024);
        http.setMinResponseDataPerSecond(DataSize.bytes(200));
        http.setMinRequestDataPerSecond(DataSize.bytes(42));
        http.setRequestCookieCompliance(CookieCompliance.RFC6265);
        http.setResponseCookieCompliance(CookieCompliance.RFC6265);

        Server server = new Server();
        MetricRegistry metrics = new MetricRegistry();
        ThreadPool threadPool = new QueuedThreadPool();

        ServerConnector connector = (ServerConnector) http.build(server, metrics, "test-http-connector", threadPool);

        assertThat(connector.getPort()).isEqualTo(8080);
        assertThat(connector.getHost()).isEqualTo("127.0.0.1");
        assertThat(connector.getAcceptQueueSize()).isEqualTo(1024);
        assertThat(connector.getReuseAddress()).isTrue();
        assertThat(connector.getIdleTimeout()).isEqualTo(30000);
        assertThat(connector.getName()).isEqualTo("test-http-connector");

        assertThat(connector.getServer()).isSameAs(server);
        assertThat(connector.getScheduler()).isInstanceOf(ScheduledExecutorScheduler.class);
        assertThat(connector.getExecutor()).isSameAs(threadPool);

        // That's gross, but unfortunately ArrayByteBufferPool doesn't have API for configuration
        ByteBufferPool byteBufferPool = connector.getByteBufferPool();
        assertThat(byteBufferPool).isInstanceOf(ArrayByteBufferPool.class);
        assertThat(getField(ArrayByteBufferPool.class, "_minCapacity", true).get(byteBufferPool)).isEqualTo(64);
        assertThat(getField(ArrayByteBufferPool.class, "_factor", true).get(byteBufferPool)).isEqualTo(1024);
        assertThat(((Object[]) getField(ArrayByteBufferPool.class, "_direct", true)
                .get(byteBufferPool)).length).isEqualTo(64);

        assertThat(connector.getAcceptors()).isEqualTo(1);
        assertThat(connector.getSelectorManager().getSelectorCount()).isEqualTo(2);

        Jetty93InstrumentedConnectionFactory connectionFactory =
                (Jetty93InstrumentedConnectionFactory) connector.getConnectionFactory("http/1.1");
        assertThat(connectionFactory).isInstanceOf(Jetty93InstrumentedConnectionFactory.class);
        assertThat(connectionFactory.getTimer())
                .isSameAs(metrics.timer("org.eclipse.jetty.server.HttpConnectionFactory.127.0.0.1.8080.connections"));
        HttpConnectionFactory httpConnectionFactory = (HttpConnectionFactory)  connectionFactory.getConnectionFactory();
        assertThat(httpConnectionFactory.getInputBufferSize()).isEqualTo(8192);
        assertThat(httpConnectionFactory.getHttpCompliance()).isEqualByComparingTo(HttpCompliance.RFC7230);

        HttpConfiguration httpConfiguration = httpConnectionFactory.getHttpConfiguration();
        assertThat(httpConfiguration.getHeaderCacheSize()).isEqualTo(512);
        assertThat(httpConfiguration.getOutputBufferSize()).isEqualTo(32768);
        assertThat(httpConfiguration.getRequestHeaderSize()).isEqualTo(8192);
        assertThat(httpConfiguration.getResponseHeaderSize()).isEqualTo(8192);
        assertThat(httpConfiguration.getSendDateHeader()).isTrue();
        assertThat(httpConfiguration.getSendServerVersion()).isFalse();
        assertThat(httpConfiguration.getCustomizers()).noneMatch(customizer -> customizer.getClass().equals(ForwardedRequestCustomizer.class));
        assertThat(httpConfiguration.getMinRequestDataRate()).isEqualTo(42);
        assertThat(httpConfiguration.getMinResponseDataRate()).isEqualTo(200);
        assertThat(httpConfiguration.getRequestCookieCompliance()).isEqualTo(CookieCompliance.RFC6265);
        assertThat(httpConfiguration.getResponseCookieCompliance()).isEqualTo(CookieCompliance.RFC6265);

        connector.stop();
        server.stop();
    }

    @Test
    void testBuildConnectorWithProxyProtocol() throws Exception {
        HttpConnectorFactory http = new HttpConnectorFactory();
        http.setBindHost("127.0.0.1");
        http.setUseProxyProtocol(true);

        Server server = new Server();
        MetricRegistry metrics = new MetricRegistry();
        ThreadPool threadPool = new QueuedThreadPool();

        ServerConnector connector = (ServerConnector) http.build(server, metrics, "test-http-connector-with-proxy-protocol", threadPool);

        assertThat(connector.getConnectionFactories().toArray()[0]).isInstanceOf(ProxyConnectionFactory.class);
        connector.stop();
        server.stop();
    }

    @Test
    void testDefaultAcceptQueueSize() throws Exception {
        HttpConnectorFactory http = new HttpConnectorFactory();
        http.setBindHost("127.0.0.1");
        http.setAcceptorThreads(Optional.of(1));
        http.setSelectorThreads(Optional.of(2));

        Server server = new Server();
        MetricRegistry metrics = new MetricRegistry();
        ThreadPool threadPool = new QueuedThreadPool();

        ServerConnector connector = (ServerConnector) http.build(server, metrics, "test-http-connector", threadPool);
        assertThat(connector.getAcceptQueueSize()).isEqualTo(NetUtil.getTcpBacklog());

        connector.stop();
    }

}
