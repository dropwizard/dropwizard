package com.yammer.dropwizard.tests;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class ServiceTest {
    @SuppressWarnings({"PackageVisibleInnerClass", "EmptyClass"})
    static class FakeConfiguration extends Configuration {

    }

    private class FakeService extends Service<FakeConfiguration> {
        FakeService() {
            super("test");
            addBundle(bundle);
        }

        @Override
        protected void initialize(FakeConfiguration configuration,
                                  Environment environment) {
        }
    }

    private final Bundle bundle = mock(Bundle.class);
    private final FakeService service = new FakeService();

    @Test
    public void hasAReferenceToItsTypeParameter() throws Exception {
        assertThat(service.getConfigurationClass(),
                   is(sameInstance(FakeConfiguration.class)));
    }
}
