package io.dropwizard.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerCommandTest {
    private static class MyApplication extends Application<Configuration> {
        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    private final MyApplication application = new MyApplication();
    private final ServerCommand<Configuration> command = new ServerCommand<>(application);
    private final Server server = new Server(0);

    private final Environment environment = mock(Environment.class);
    private final Namespace namespace = mock(Namespace.class);
    private final ServerFactory serverFactory = mock(ServerFactory.class);
    private final Configuration configuration = mock(Configuration.class);

    @Before
    public void setUp() throws Exception {
        when(serverFactory.build(environment)).thenReturn(server);
        when(configuration.getServerFactory()).thenReturn(serverFactory);
    }

    @After
    public void tearDown() throws Exception {
        server.stop();
    }

    @Test
    public void hasAName() throws Exception {
        assertThat(command.getName())
                .isEqualTo("server");
    }

    @Test
    public void hasADescription() throws Exception {
        assertThat(command.getDescription())
                .isEqualTo("Runs the Dropwizard application as an HTTP server");
    }

    @Test
    public void hasTheApplicationsConfigurationClass() throws Exception {
        assertThat(command.getConfigurationClass())
                .isEqualTo(application.getConfigurationClass());
    }

    @Test
    public void buildsAndRunsAConfiguredServer() throws Exception {
        command.run(environment, namespace, configuration);

        assertThat(server.isStarted())
                .isTrue();
    }

    @Test
    public void stopsAServerIfThereIsAnErrorStartingIt() throws Exception {
        server.addBean(new AbstractLifeCycle() {
            @Override
            protected void doStart() throws Exception {
                throw new IOException("oh crap");
            }
        });

        try {
            command.run(environment, namespace, configuration);
            failBecauseExceptionWasNotThrown(IOException.class);
        } catch (IOException e) {
            assertThat(e.getMessage())
                    .isEqualTo("oh crap");
        }

        assertThat(server.isStarted())
                .isFalse();
    }
}
