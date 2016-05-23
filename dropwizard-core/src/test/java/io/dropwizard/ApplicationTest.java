package io.dropwizard;

import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationTest {
    private static class FakeConfiguration extends Configuration {}

    private static class FakeApplication extends Application<FakeConfiguration> {
        boolean fatalError = false;

        @Override
        public void run(FakeConfiguration configuration, Environment environment) {}

        @Override
        protected void onFatalError() {
            fatalError = true;
        }
    }

    private static class PoserApplication extends FakeApplication {}

    private static class WrapperApplication<C extends FakeConfiguration> extends Application<C> {
        private final Application<C> application;

        private WrapperApplication(Application<C> application) {
            this.application = application;
        }

        @Override
        public void initialize(Bootstrap<C> bootstrap) {
            this.application.initialize(bootstrap);
        }

        @Override
        public void run(C configuration, Environment environment) throws Exception {
            this.application.run(configuration, environment);
        }
    }

    @Test
    public void hasAReferenceToItsTypeParameter() throws Exception {
        assertThat(new FakeApplication().getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }

    @Test
    public void canDetermineConfiguration() throws Exception {
        assertThat(new PoserApplication().getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }

    @Test
    public void canDetermineWrappedConfiguration() throws Exception {
        final PoserApplication application = new PoserApplication();
        assertThat(new WrapperApplication<>(application).getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }

    @Test
    public void exitWithFatalErrorWhenCommandFails() throws Exception {
        final File configFile = File.createTempFile("dropwizard-invalid-config", ".yml");
        try {
            final FakeApplication application = new FakeApplication();
            application.run("server", configFile.getAbsolutePath());
            assertThat(application.fatalError).isTrue();
        }
        finally {
            configFile.delete();
        }
    }
}
