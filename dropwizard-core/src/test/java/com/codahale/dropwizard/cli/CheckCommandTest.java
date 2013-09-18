package com.codahale.dropwizard.cli;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

public class CheckCommandTest {
    private static class MyApplication extends Application<Configuration> {
        @Override
        public void initialize(Bootstrap<Configuration> bootstrap) {
        }

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
        }
    }

    private final MyApplication application = new MyApplication();
    private final CheckCommand<Configuration> command = new CheckCommand<>(application);

    @SuppressWarnings("unchecked")
    private final Bootstrap<Configuration> bootstrap = mock(Bootstrap.class);
    private final Namespace namespace = mock(Namespace.class);
    private final Configuration configuration = mock(Configuration.class);

    @Test
    public void hasAName() throws Exception {
        assertThat(command.getName())
                .isEqualTo("check");
    }

    @Test
    public void hasADescription() throws Exception {
        assertThat(command.getDescription())
                .isEqualTo("Parses and validates the configuration file");
    }

    @Test
    public void doesNotInteractWithAnything() throws Exception {
        command.run(bootstrap, namespace, configuration);

        verifyZeroInteractions(bootstrap, namespace, configuration);
    }
}
