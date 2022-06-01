package io.dropwizard.core.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.logging.common.LoggingFactory;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ConfiguredCommandTest {
    private static class TestCommand extends ConfiguredCommand<Configuration> {
        protected TestCommand() {
            super("test", "test");
        }

        @Override
        protected void run(Bootstrap<Configuration> bootstrap, Namespace namespace, Configuration configuration)
                throws Exception {}
    }

    private static class MyApplication extends Application<Configuration> {
        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {}
    }

    private final MyApplication application = new MyApplication();
    private final TestCommand command = new TestCommand();
    private final Bootstrap<Configuration> bootstrap = new Bootstrap<>(application);
    private final Namespace namespace = mock(Namespace.class);

    @SuppressWarnings("unchecked")
    @Test
    void canUseCustomConfigurationFactory() throws Exception {

        Configuration configuration = mock(Configuration.class);
        LoggingFactory loggingFactory = mock(LoggingFactory.class);

        ConfigurationFactory<Configuration> factory = Mockito.mock(ConfigurationFactory.class);
        when(factory.build()).thenReturn(configuration);
        when(configuration.getLoggingFactory()).thenReturn(loggingFactory);

        bootstrap.setConfigurationFactoryFactory((klass, validator, objectMapper, propertyPrefix) -> factory);

        assertThat(command.getConfiguration()).isNull();

        command.run(bootstrap, namespace);

        assertThat(command.getConfiguration()).isEqualTo(configuration);

        Mockito.verify(factory).build();
    }
}
