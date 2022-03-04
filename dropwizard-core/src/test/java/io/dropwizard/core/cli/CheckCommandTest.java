package io.dropwizard.core.cli;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class CheckCommandTest {
    private static class MyApplication extends Application<Configuration> {
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
    void hasAName() {
        assertThat(command.getName())
                .isEqualTo("check");
    }

    @Test
    void hasADescription() {
        assertThat(command.getDescription())
                .isEqualTo("Parses and validates the configuration file");
    }

    @Test
    void doesNotInteractWithAnything() throws Exception {
        command.run(bootstrap, namespace, configuration);

        verifyNoInteractions(bootstrap, namespace, configuration);
    }
}
