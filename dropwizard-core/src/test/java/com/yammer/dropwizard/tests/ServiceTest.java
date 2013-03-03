package com.yammer.dropwizard.tests;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ServiceTest {
    private static class FakeConfiguration extends Configuration {}

    private static class FakeService extends Service<FakeConfiguration> {
        @Override
        public void initialize(Bootstrap<FakeConfiguration> bootstrap) {}

        @Override
        public void run(FakeConfiguration configuration, Environment environment) {}
    }

    private static class PoserService extends FakeService {}

    private static class WrapperService<C extends FakeConfiguration> extends Service<C> {
        private final Service<C> service;

        private WrapperService(Service<C> service) {
            this.service = service;
        }

        @Override
        public void initialize(Bootstrap<C> bootstrap) {
            this.service.initialize(bootstrap);
        }

        @Override
        public void run(C configuration, Environment environment) throws Exception {
            this.service.run(configuration, environment);
        }
    }

    @Test
    public void hasAReferenceToItsTypeParameter() throws Exception {
        assertThat(new FakeService().getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }

    @Test
    public void canDetermineConfiguration() throws Exception {
        assertThat(new PoserService().getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }

    @Test
    public void canDetermineWrappedConfiguration() throws Exception {
        final PoserService service = new PoserService();
        assertThat(new WrapperService<FakeConfiguration>(service).getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }
}
