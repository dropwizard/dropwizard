package io.dropwizard.core.cli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.server.ServerFactory;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.logging.common.LoggingFactory;
import io.dropwizard.util.JarLocation;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InheritedServerCommandTest {
    private static class ApiCommand extends ServerCommand<Configuration> {

        protected ApiCommand(final Application<Configuration> application) {
            super(application, "api", "Runs the Dropwizard application as an API HTTP server");
        }
    }

    private static class MyApplication extends Application<Configuration> {
        @Override
        public void initialize(final Bootstrap<Configuration> bootstrap) {
            bootstrap.addCommand(new ApiCommand(this));
            super.initialize(bootstrap);
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {}
    }

    private final MyApplication application = new MyApplication();
    private final ApiCommand command = new ApiCommand(application);
    private final Server server = new Server(0);

    private final Environment environment = mock(Environment.class);
    private final Namespace namespace = mock(Namespace.class);
    private final ServerFactory serverFactory = mock(ServerFactory.class);
    private final Configuration configuration = mock(Configuration.class);

    @BeforeEach
    void setUp() throws Exception {
        when(serverFactory.build(environment)).thenReturn(server);
        when(configuration.getServerFactory()).thenReturn(serverFactory);
    }

    @AfterEach
    void tearDown() throws Exception {
        server.stop();
    }

    @Test
    void hasAName() throws Exception {
        assertThat(command.getName()).isEqualTo("api");
    }

    @Test
    void hasADescription() throws Exception {
        assertThat(command.getDescription()).isEqualTo("Runs the Dropwizard application as an API HTTP server");
    }

    @Test
    void buildsAndRunsAConfiguredServer() throws Exception {
        command.run(environment, namespace, configuration);

        assertThat(server.isStarted()).isTrue();
    }

    @Test
    void usesDefaultConfigPath() throws Exception {

        class SingletonConfigurationFactory implements ConfigurationFactory<Configuration> {
            @Override
            public Configuration build(final ConfigurationSourceProvider provider, final String path) {
                return configuration;
            }

            @Override
            public Configuration build() {
                throw new AssertionError("Didn't use the default config path variable");
            }
        }

        when(configuration.getLoggingFactory()).thenReturn(mock(LoggingFactory.class));

        final Bootstrap<Configuration> bootstrap = new Bootstrap<>(application);

        bootstrap.setConfigurationFactoryFactory(
                (klass, validator, objectMapper, propertyPrefix) -> new SingletonConfigurationFactory());

        bootstrap.addCommand(new ConfiguredCommand<Configuration>("test", "a test command") {

            @Override
            protected void run(
                    final Bootstrap<Configuration> bootstrap,
                    final Namespace namespace,
                    final Configuration configuration) {
                assertThat(namespace.getString("file")).isNotEmpty().isEqualTo("yaml/server.yml");
            }

            @Override
            protected Argument addFileArgument(final Subparser subparser) {
                return super.addFileArgument(subparser).setDefault("yaml/server.yml");
            }
        });

        final JarLocation location = mock(JarLocation.class);

        when(location.toString()).thenReturn("dw-thing.jar");
        when(location.getVersion()).thenReturn(Optional.of("1.0.0"));

        Cli cli = new Cli(location, bootstrap, System.out, System.err);
        cli.run("test");
    }
}
