package io.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Size;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validator;
import java.io.File;

import static org.apache.commons.lang3.reflect.FieldUtils.getField;
import static org.assertj.core.api.Assertions.assertThat;

public class HttpConnectorFactoryTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator validator = BaseValidator.newValidator();

    @Before
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                FileAppenderFactory.class, SyslogAppenderFactory.class, HttpConnectorFactory.class);
    }

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(HttpConnectorFactory.class);
    }

    @Test
    public void testParseMinimalConfiguration() throws Exception {
        HttpConnectorFactory http =
                new ConfigurationFactory<>(HttpConnectorFactory.class, validator, objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/http-connector-minimal.yml").toURI()));

        assertThat(http.getPort()).isEqualTo(8080);
        assertThat(http.getBindHost()).isNull();
        assertThat(http.isInheritChannel()).isEqualTo(false);
        assertThat(http.getHeaderCacheSize()).isEqualTo(Size.bytes(512));
        assertThat(http.getOutputBufferSize()).isEqualTo(Size.kilobytes(32));
        assertThat(http.getMaxRequestHeaderSize()).isEqualTo(Size.kilobytes(8));
        assertThat(http.getMaxResponseHeaderSize()).isEqualTo(Size.kilobytes(8));
        assertThat(http.getInputBufferSize()).isEqualTo(Size.kilobytes(8));
        assertThat(http.getIdleTimeout()).isEqualTo(Duration.seconds(30));
        assertThat(http.getMinBufferPoolSize()).isEqualTo(Size.bytes(64));
        assertThat(http.getBufferPoolIncrement()).isEqualTo(Size.bytes(1024));
        assertThat(http.getMaxBufferPoolSize()).isEqualTo(Size.kilobytes(64));
        assertThat(http.getAcceptorThreads()).isEqualTo(Math.max(1, Runtime.getRuntime().availableProcessors() / 2));
        assertThat(http.getSelectorThreads()).isEqualTo(Runtime.getRuntime().availableProcessors());
        assertThat(http.getAcceptQueueSize()).isNull();
        assertThat(http.isReuseAddress()).isTrue();
        assertThat(http.getSoLingerTime()).isNull();
        assertThat(http.isUseServerHeader()).isFalse();
        assertThat(http.isUseDateHeader()).isTrue();
        assertThat(http.isUseForwardedHeaders()).isTrue();
    }

    @Test
    public void testParseFullConfiguration() throws Exception {
        HttpConnectorFactory http =
                new ConfigurationFactory<>(HttpConnectorFactory.class, validator, objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/http-connector.yml").toURI()));

        assertThat(http.getPort()).isEqualTo(9090);
        assertThat(http.getBindHost()).isEqualTo("127.0.0.1");
        assertThat(http.isInheritChannel()).isEqualTo(true);
        assertThat(http.getHeaderCacheSize()).isEqualTo(Size.bytes(256));
        assertThat(http.getOutputBufferSize()).isEqualTo(Size.kilobytes(128));
        assertThat(http.getMaxRequestHeaderSize()).isEqualTo(Size.kilobytes(4));
        assertThat(http.getMaxResponseHeaderSize()).isEqualTo(Size.kilobytes(4));
        assertThat(http.getInputBufferSize()).isEqualTo(Size.kilobytes(4));
        assertThat(http.getIdleTimeout()).isEqualTo(Duration.seconds(10));
        assertThat(http.getMinBufferPoolSize()).isEqualTo(Size.bytes(128));
        assertThat(http.getBufferPoolIncrement()).isEqualTo(Size.bytes(500));
        assertThat(http.getMaxBufferPoolSize()).isEqualTo(Size.kilobytes(32));
        assertThat(http.getAcceptorThreads()).isEqualTo(1);
        assertThat(http.getSelectorThreads()).isEqualTo(4);
        assertThat(http.getAcceptQueueSize()).isEqualTo(1024);
        assertThat(http.isReuseAddress()).isFalse();
        assertThat(http.getSoLingerTime()).isEqualTo(Duration.seconds(30));
        assertThat(http.isUseServerHeader()).isTrue();
        assertThat(http.isUseDateHeader()).isFalse();
        assertThat(http.isUseForwardedHeaders()).isFalse();
    }

    @Test
    public void testBuildConnector() throws Exception {
        HttpConnectorFactory http = new HttpConnectorFactory();
        http.setBindHost("127.0.0.1");
        http.setAcceptorThreads(1);
        http.setSelectorThreads(2);
        http.setAcceptQueueSize(1024);
        http.setSoLingerTime(Duration.seconds(30));

        Server server = new Server();
        MetricRegistry metrics = new MetricRegistry();
        ThreadPool threadPool = new QueuedThreadPool();

        ServerConnector connector = (ServerConnector) http.build(server, metrics, "test-http-connector", threadPool);

        assertThat(connector.getPort()).isEqualTo(8080);
        assertThat(connector.getHost()).isEqualTo("127.0.0.1");
        assertThat(connector.getAcceptQueueSize()).isEqualTo(1024);
        assertThat(connector.getReuseAddress()).isTrue();
        assertThat(connector.getSoLingerTime()).isEqualTo(30);
        assertThat(connector.getIdleTimeout()).isEqualTo(30000);
        assertThat(connector.getName()).isEqualTo("test-http-connector");

        assertThat(connector.getServer()).isSameAs(server);
        assertThat(connector.getScheduler()).isInstanceOf(ScheduledExecutorScheduler.class);
        assertThat(connector.getExecutor()).isSameAs(threadPool);

        // That's gross, but unfortunately ArrayByteBufferPool doesn't have public API for configuration
        ByteBufferPool byteBufferPool = connector.getByteBufferPool();
        assertThat(byteBufferPool).isInstanceOf(ArrayByteBufferPool.class);
        assertThat(getField(ArrayByteBufferPool.class, "_min", true).get(byteBufferPool)).isEqualTo(64);
        assertThat(getField(ArrayByteBufferPool.class, "_inc", true).get(byteBufferPool)).isEqualTo(1024);
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

        HttpConfiguration httpConfiguration = httpConnectionFactory.getHttpConfiguration();
        assertThat(httpConfiguration.getHeaderCacheSize()).isEqualTo(512);
        assertThat(httpConfiguration.getOutputBufferSize()).isEqualTo(32768);
        assertThat(httpConfiguration.getRequestHeaderSize()).isEqualTo(8192);
        assertThat(httpConfiguration.getResponseHeaderSize()).isEqualTo(8192);
        assertThat(httpConfiguration.getSendDateHeader()).isTrue();
        assertThat(httpConfiguration.getSendServerVersion()).isFalse();
        assertThat(httpConfiguration.getCustomizers()).hasAtLeastOneElementOfType(ForwardedRequestCustomizer.class);

        connector.stop();
        server.stop();
    }
}
