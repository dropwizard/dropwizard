package io.dropwizard.setup;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.FileConfigurationSourceProvider;

import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class BootstrapTest {
    private final Application<Configuration> application = new Application<Configuration>() {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    };
    private final Bootstrap<Configuration> bootstrap = new Bootstrap<Configuration>(application);

    @Test
    public void hasAnApplication() throws Exception {
        assertThat(bootstrap.getApplication())
                .isEqualTo(application);
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
        Bootstrap<Configuration> newBootstrap = new Bootstrap<Configuration>(application) {
            @Override
            public MetricRegistry getMetricRegistry() {
                return super.getMetricRegistry();
            }
        };
        
        assertThat(newBootstrap.getMetricRegistry().getNames())
                .contains("jvm.buffers.mapped.capacity", "jvm.threads.count", "jvm.memory.heap.usage");
    }
}
