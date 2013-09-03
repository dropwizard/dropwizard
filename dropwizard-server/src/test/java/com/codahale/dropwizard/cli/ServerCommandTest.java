package com.codahale.dropwizard.cli;
 
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.server.ServerApplication;
import com.codahale.dropwizard.server.ServerCommand;
import com.codahale.dropwizard.server.ServerConfiguration;
import com.codahale.dropwizard.server.ServerEnvironment;
import com.codahale.dropwizard.server.ServerFactory;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;

import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.*;

public class ServerCommandTest {
    private static class MyApplication extends ServerApplication<ServerConfiguration> {
        @Override
        public void initializeServer(Bootstrap<ServerConfiguration> bootstrap) {
        }

        @Override
        public void run(ServerConfiguration configuration, ServerEnvironment environment) throws Exception {
        }
    }

    private final MyApplication application = new MyApplication();
    private final ServiceCommand<ServerConfiguration> command = new ServerCommand<ServerConfiguration>(application);
    private final Server server = new Server(0);

    private final Environment environment = mock(Environment.class);
    private final Namespace namespace = mock(Namespace.class);
    private final ServerFactory serverFactory = mock(ServerFactory.class);
    private final ServerConfiguration configuration = mock(ServerConfiguration.class);

    @Before
    public void setUp() throws Exception {
        when(serverFactory.build(environment)).thenReturn(server);
        when(environment.lifecycle()).thenReturn(new LifecycleEnvironment());
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
