package io.dropwizard.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ConfiguredCommandTest {
    private static class TestCommand extends ConfiguredCommand<Configuration> {
        protected TestCommand() {
            super("test", "test");
        }

        @Override
        protected void run(Bootstrap<Configuration> bootstrap, Namespace namespace, Configuration configuration) throws Exception {

        }
    }

    private static class MyApplication extends Application<Configuration> {
        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    private final MyApplication application = new MyApplication();
    private final TestCommand command = new TestCommand();
    private final Bootstrap<Configuration> bootstrap = new Bootstrap<>(application);
    private final Namespace namespace = mock(Namespace.class);

    @SuppressWarnings("unchecked")
    @Test
    public void canUseCustomConfigurationFactory() throws Exception {

        ConfigurationFactory<Configuration> factory = Mockito.mock(ConfigurationFactory.class);
        when(factory.build()).thenReturn(null);

        bootstrap.setConfigurationFactoryFactory(
            (klass, validator, objectMapper, propertyPrefix) -> factory
        );

        command.run(bootstrap, namespace);

        Mockito.verify(factory).build();
    }
}
