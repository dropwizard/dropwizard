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
        public void initialize(Bootstrap<FakeConfiguration> bootstrap) {

        }

        @Override
        public void run(FakeConfiguration configuration,
                           Environment environment) {
        }
    }

    private static class PoserService extends FakeService {}

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
}
