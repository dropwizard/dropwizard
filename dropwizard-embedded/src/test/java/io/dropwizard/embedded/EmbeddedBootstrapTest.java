package io.dropwizard.embedded;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.FileConfigurationSourceProvider;

import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class EmbeddedBootstrapTest {
    private final Service<Configuration> service = new Service<Configuration>("./yaml/server.yml") {
        @Override
        public void initialize(EmbeddedBootstrap<Configuration> bootstrap) {
            // no bundles
        }

        @Override
        protected void start(Configuration configuration, Environment environment) throws Exception {
            // start does nothing
        }
    };

    private final EmbeddedBootstrap<Configuration> bootstrap = new EmbeddedBootstrap<>(service);

    @Test
    public void hasNoApplication() {
        try {
            Application<Configuration> app = bootstrap.getApplication();
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("not applicable")
                    .hasNoCause();
        }
    }

    @Test
    public void cannotAddCommands() {
        try {
            ConfiguredCommand<Configuration> cmd = new ConfiguredCommand<Configuration>("example", "example") {
                @Override
                protected void run(Bootstrap<Configuration> bootstrap, Namespace namespace, Configuration configuration) throws Exception {
                    // run does nothing
                }
            };
            bootstrap.addCommand(cmd);
            failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
        } catch (Exception e) {
            assertThat(e).isInstanceOf(UnsupportedOperationException.class)
                    .hasMessage("not applicable")
                    .hasNoCause();
        }
    }

    @Test
    public void hasAnObjectMapper() throws Exception {
        assertThat(bootstrap.getObjectMapper())
                .isNotNull();
    }

    @Test
    public void defaultsToUsingFilesForConfiguration() throws Exception {
        assertThat(bootstrap.getConfigurationSourceProvider())
                .isInstanceOfAny(FileConfigurationSourceProvider.class);
    }

    @Test
    public void defaultsToUsingTheDefaultClassLoader() throws Exception {
        assertThat(bootstrap.getClassLoader())
                .isEqualTo(Thread.currentThread().getContextClassLoader());
    }

    @Test
    public void comesWithJvmInstrumentation() throws Exception {
        assertThat(bootstrap.getMetricRegistry().getNames())
                .contains("jvm.buffers.mapped.capacity", "jvm.threads.count", "jvm.memory.heap.usage");
    }

    @Test
    public void defaultsToDefaultConfigurationFactoryFactory() throws Exception {
        assertThat(bootstrap.getConfigurationFactoryFactory())
                .isInstanceOf(DefaultConfigurationFactoryFactory.class);
    }

    @Test
    public void testBYOMetrics() {
        final MetricRegistry newRegistry = new MetricRegistry();
        Bootstrap<Configuration> newBootstrap = new EmbeddedBootstrap<Configuration>(service) {
            @Override
            public MetricRegistry getMetricRegistry() {
                return super.getMetricRegistry();
            }
        };

        assertThat(newBootstrap.getMetricRegistry().getNames())
                .contains("jvm.buffers.mapped.capacity", "jvm.threads.count", "jvm.memory.heap.usage");
    }
}
