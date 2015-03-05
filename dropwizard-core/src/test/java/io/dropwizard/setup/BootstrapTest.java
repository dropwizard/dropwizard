package io.dropwizard.setup;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.FileConfigurationSourceProvider;

import io.dropwizard.validation.valuehandling.OptionalValidatedValueUnwrapper;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class BootstrapTest {
    private final Application<Configuration> application = new Application<Configuration>() {
        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    };
    private Bootstrap<Configuration> bootstrap;

    @Before
    public void setUp() {
        bootstrap = new Bootstrap<>(application);
    }

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
                .contains("jvm.buffers.mapped.capacity", "jvm.threads.count", "jvm.memory.heap.usage", "jvm.attribute.vendor", "jvm.classloader.loaded", "jvm.filedescriptor");
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
                .contains("jvm.buffers.mapped.capacity", "jvm.threads.count", "jvm.memory.heap.usage", "jvm.attribute.vendor", "jvm.classloader.loaded", "jvm.filedescriptor");
    }

    @Test
    public void defaultsToDefaultValidatorFactory() throws Exception {
        assertThat(bootstrap.getValidatorFactory()).isInstanceOf(ValidatorFactoryImpl.class);

        ValidatorFactoryImpl validatorFactory = (ValidatorFactoryImpl)bootstrap.getValidatorFactory();
        assertThat(validatorFactory.getValidatedValueHandlers()).hasSize(1);
        assertThat(validatorFactory.getValidatedValueHandlers().get(0))
                .isInstanceOf(OptionalValidatedValueUnwrapper.class);
    }

    @Test
    public void canUseCustomValidatorFactory() throws Exception {
        ValidatorFactory factory = Validation
                .byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory();
        bootstrap.setValidatorFactory(factory);

        assertThat(bootstrap.getValidatorFactory()).isSameAs(factory);
    }
}
