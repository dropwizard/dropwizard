package io.dropwizard.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.server.ServerFactory;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIOException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServerCommandTest {
    private static class MyApplication extends Application<Configuration> {
        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    private final MyApplication application = new MyApplication();
    private final ServerCommand<Configuration> command = new ServerCommand<>(application);
    private final Server server = new Server(0) {
        @Override
        protected void doStop() throws Exception {
            super.doStop();
            if (ServerCommandTest.this.throwException) {
                System.out.println("throw NullPointerException, see Issue#1557");
                throw new NullPointerException();
            }
        }
    };

    private final Environment environment = mock(Environment.class);
    private final Namespace namespace = mock(Namespace.class);
    private final ServerFactory serverFactory = mock(ServerFactory.class);
    private final Configuration configuration = mock(Configuration.class);
    private boolean throwException = false;

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
    void hasAName() {
        assertThat(command.getName())
                .isEqualTo("server");
    }

    @Test
    void hasADescription() {
        assertThat(command.getDescription())
                .isEqualTo("Runs the Dropwizard application as an HTTP server");
    }

    @Test
    void hasTheApplicationsConfigurationClass() {
        assertThat(command.getConfigurationClass())
                .isEqualTo(application.getConfigurationClass());
    }

    @Test
    void buildsAndRunsAConfiguredServer() throws Exception {
        command.run(environment, namespace, configuration);

        assertThat(server.isStarted())
                .isTrue();
    }

    @Test
    void stopsAServerIfThereIsAnErrorStartingIt() {
        this.throwException = true;
        server.addBean(new AbstractLifeCycle() {
            @Override
            protected void doStart() throws Exception {
                throw new IOException("oh crap");
            }
        });

        assertThatIOException()
            .isThrownBy(() -> command.run(environment, namespace, configuration))
            .withMessage("oh crap");

        assertThat(server.isStarted())
                .isFalse();
        this.throwException = false;
    }
}
