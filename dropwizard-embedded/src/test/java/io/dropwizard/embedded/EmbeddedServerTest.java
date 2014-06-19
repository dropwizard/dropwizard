package io.dropwizard.embedded;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.google.common.io.Resources;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.metrics.MetricsFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import java.io.File;
import java.io.FileNotFoundException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class EmbeddedServerTest {
    // actual objects
    private final String configFile = "yaml/server.yml";
    private final Service<Configuration> testService = new Service<Configuration>(configFile) {
        @Override
        public void initialize(EmbeddedBootstrap<Configuration> bootstrap) {
            // no bundles
        }

        @Override
        protected void start(Configuration configuration, Environment environment) throws Exception {
            // start does nothing
        }
    };

    private final EmbeddedServer<Configuration> embeddedServer = new EmbeddedServer<>(testService);
    private final Server server = new Server(0);

    // mocks
    private final Service<Configuration> mockService = mock(Service.class);
    private final Server mockServer = mock(Server.class);
    private final ServerFactory serverFactory = mock(ServerFactory.class);
    private final Configuration configuration = mock(Configuration.class);
    private final EmbeddedBootstrap<Configuration> bootstrap = mock(EmbeddedBootstrap.class);
    private final ValidatorFactory validatorFactory = mock(ValidatorFactory.class);
    private final Validator validator = mock(Validator.class);
    private final MetricsFactory metricsFactory = mock(MetricsFactory.class);
    private final MetricRegistry metricRegistry = mock(MetricRegistry.class);

    @Before
    public void setUp() throws Exception {
        when(configuration.getServerFactory()).thenReturn(serverFactory);
        when(serverFactory.build(any(Environment.class))).thenReturn(server);
        when(bootstrap.getValidatorFactory()).thenReturn(validatorFactory);
        when(validatorFactory.getValidator()).thenReturn(validator);
        when(configuration.getMetricsFactory()).thenReturn(metricsFactory);
        when(bootstrap.getMetricRegistry()).thenReturn(metricRegistry);
        when(metricRegistry.register(anyString(), any(Metric.class))).thenReturn(null);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void buildsAndRunsAConfiguredServer() throws Exception {
        embeddedServer.start(bootstrap, configuration);
        assertThat(server.isStarted())
                .isTrue();
    }

    @Test
    public void stopsStartedServer() throws Exception {
        embeddedServer.start(bootstrap, configuration);

        embeddedServer.stop();
        assertThat(server.isStopped());
    }

    @Test
    public void failsToStopStoppedServer() throws Exception {
        Server spyServer = spy(server);
        when(serverFactory.build(any(Environment.class))).thenReturn(spyServer);

        embeddedServer.start(bootstrap, configuration);
        embeddedServer.stop();
        embeddedServer.stop();

        verify(spyServer, times(1)).stop();
    }

    @Test
    public void failsToStopUnkillableServer() throws Exception {
        Server spyServer = spy(server);
        when(serverFactory.build(any(Environment.class))).thenReturn(spyServer);

        embeddedServer.start(bootstrap, configuration);
        embeddedServer.stop();

        try {
            // make the server "unkillable"
            doReturn(Boolean.FALSE).when(spyServer).isStopped();

            embeddedServer.stop();
            failBecauseExceptionWasNotThrown(Exception.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class)
                    .hasMessageStartingWith("Failed to stop server after graceful shutdown period")
                    .hasNoCause();
        }
    }

    @Test
    public void testConfigParsing() throws Exception {
        String configFullPath = new File(Resources.getResource(configFile).toURI()).getPath();

        EmbeddedBootstrap<Configuration> realBootstrap = new EmbeddedBootstrap<>(testService);
        EmbeddedBootstrap<Configuration> spyBootstrap = spy(realBootstrap);

        ArgumentCaptor<Configuration> configArgument = ArgumentCaptor.forClass(Configuration.class);
        embeddedServer.start(spyBootstrap, configFullPath);
        verify(spyBootstrap).run(configArgument.capture(), any(Environment.class));

        assertThat(configArgument.getValue().getLoggingFactory().getAppenders().size()).isEqualTo(1);

        assertThat(configArgument.getValue().getServerFactory()).isInstanceOf(DefaultServerFactory.class);
        DefaultServerFactory dfltServerFactory = (DefaultServerFactory) configArgument.getValue().getServerFactory();
        assertThat(dfltServerFactory.getGzipFilterFactory().isEnabled()).isFalse();
        assertThat(dfltServerFactory.getApplicationConnectors().size()).isEqualTo(1);

        assertThat(dfltServerFactory.getApplicationConnectors().get(0)).isInstanceOf(HttpConnectorFactory.class);
        HttpConnectorFactory httpConnectorFactory = (HttpConnectorFactory) dfltServerFactory.getApplicationConnectors().get(0);
        assertThat(httpConnectorFactory.getPort()).isEqualTo(0);
        assertThat(httpConnectorFactory.getBindHost()).isEqualTo("localhost");

        assertThat(dfltServerFactory.getMinThreads()).isEqualTo(89);
        assertThat(dfltServerFactory.getMaxThreads()).isEqualTo(101);
    }

    @Test
    public void testConfigFileNotValid() throws Exception {
        EmbeddedBootstrap<Configuration> realBootstrap = new EmbeddedBootstrap<>(testService);
        String invalidConfig = "THIS_FILE_DOES_NOT_EXIST";
        try {
            embeddedServer.start(realBootstrap, invalidConfig);
            failBecauseExceptionWasNotThrown(FileNotFoundException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(FileNotFoundException.class);
        }
    }

    @Test
    public void testDefaultConfig() throws Exception {
        EmbeddedBootstrap<Configuration> realBootstrap = new EmbeddedBootstrap<>(testService);
        EmbeddedBootstrap<Configuration> spyBootstrap = spy(realBootstrap);

        String invalidConfig = null;

        ArgumentCaptor<Configuration> configArgument = ArgumentCaptor.forClass(Configuration.class);
        embeddedServer.start(spyBootstrap, invalidConfig);
        verify(spyBootstrap).run(configArgument.capture(), any(Environment.class));

        assertThat(configArgument.getValue().getServerFactory()).isInstanceOf(DefaultServerFactory.class);
        DefaultServerFactory dfltServerFactory = (DefaultServerFactory) configArgument.getValue().getServerFactory();
        assertThat(dfltServerFactory.getApplicationConnectors().size()).isGreaterThan(0);

        assertThat(dfltServerFactory.getApplicationConnectors().get(0)).isInstanceOf(HttpConnectorFactory.class);
        HttpConnectorFactory httpConnectorFactory = (HttpConnectorFactory) dfltServerFactory.getApplicationConnectors().get(0);
        assertThat(httpConnectorFactory.getPort()).isEqualTo(8080);
    }
}
