package com.yammer.dropwizard.tests;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class ServiceTest {
    @SuppressWarnings({ "PackageVisibleInnerClass", "EmptyClass" })
    static class FakeConfiguration extends Configuration {

    }

    private class FakeService extends Service<FakeConfiguration> {
        FakeService() {
            addBundle(bundle);
        }

        @Override
        protected void run(FakeConfiguration configuration,
                           Environment environment) {
        }
    }

    private class PoserService extends FakeService {
    }

    private final Bundle bundle = mock(Bundle.class);
    private final FakeService service = new FakeService();

    @Test
    public void hasAReferenceToItsTypeParameter() throws Exception {
        assertThat(service.getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }

    @Test
    public void canDetermineConfiguration() throws Exception {
        assertThat(new PoserService().getConfigurationClass())
                .isSameAs(FakeConfiguration.class);
    }

    @Test
    public void defualtNameIsSimpleNameOfServiceClass() throws Exception {
        assertThat(new FakeService().getName())
                .isEqualTo(FakeService.class.getSimpleName());
        assertThat(new PoserService().getName())
                .isEqualTo(PoserService.class.getSimpleName());
    }
}
