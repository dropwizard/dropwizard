package io.dropwizard.core.server;

import com.codahale.metrics.annotation.ResponseMeteredLevel;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyContainerHolder;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests that the {@link JerseyEnvironment#getUrlPattern()} is set by the following priority order:
 * <ol>
 *     <li>YAML defined value</li>
 *     <li>{@link Application#run(Configuration, Environment)} defined value</li>
 *     <li>Default value defined by {@link DropwizardResourceConfig#urlPattern}</li>
 * </ol>
 */
class AbstractServerFactoryTest {

    private final JerseyContainerHolder holder = mock(JerseyContainerHolder.class);
    private final DropwizardResourceConfig config = new DropwizardResourceConfig();
    private final JerseyEnvironment jerseyEnvironment = new JerseyEnvironment(holder, config);
    private final Environment environment = mock(Environment.class, RETURNS_DEEP_STUBS);
    private final TestServerFactory serverFactory = new TestServerFactory();

    private static final String DEFAULT_PATTERN = "/*";
    private static final String RUN_SET_PATTERN = "/set/from/run/*";
    private static final String YAML_SET_PATTERN = "/set/from/yaml/*";

    @BeforeEach
    void before() {
        when(environment.jersey()).thenReturn(jerseyEnvironment);
        when(environment.getApplicationContext()).thenReturn(new MutableServletContextHandler());
    }

    @Test
    void usesYamlDefinedPattern() {
        serverFactory.setJerseyRootPath(YAML_SET_PATTERN);
        jerseyEnvironment.setUrlPattern(RUN_SET_PATTERN);

        serverFactory.build(environment);

        assertThat(jerseyEnvironment.getUrlPattern()).isEqualTo(YAML_SET_PATTERN);
    }

    @Test
    void usesRunDefinedPatternWhenNoYaml() {
        jerseyEnvironment.setUrlPattern(RUN_SET_PATTERN);

        serverFactory.build(environment);

        assertThat(jerseyEnvironment.getUrlPattern()).isEqualTo(RUN_SET_PATTERN);
    }

    @Test
    void usesDefaultPatternWhenNoneSet() {
        serverFactory.build(environment);

        assertThat(jerseyEnvironment.getUrlPattern()).isEqualTo(DEFAULT_PATTERN);
    }

    @Test
    void usesDefaultResponseMeteredLevelWhenNotSet() {
        assertThat(serverFactory.getResponseMeteredLevel()).isEqualTo(ResponseMeteredLevel.COARSE);
    }

    @Test
    void usesDefaultMetricPrefixWhenNotSet() {
        assertThat(serverFactory.getMetricPrefix()).isNull();
    }

    /**
     * Test implementation of {@link AbstractServerFactory} used to run {@link #createAppServlet}, which triggers the
     * setting of {@link JerseyEnvironment#setUrlPattern(String)}.
     */
    public static class TestServerFactory extends AbstractServerFactory {
        @Override
        public Server build(Environment environment) {
            // mimics the current default + simple server factory build() methods
            ThreadPool threadPool = createThreadPool(environment.metrics());
            Server server = buildServer(environment.lifecycle(), threadPool);
            createAppServlet(server,
                                  environment.jersey(),
                                  environment.getObjectMapper(),
                                  environment.getValidator(),
                                  environment.getApplicationContext(),
                                  environment.getJerseyServletContainer(),
                                  environment.metrics());
            return server;
        }

        @Override
        public void configure(Environment environment) {
            // left blank intentionally
        }
    }
}
