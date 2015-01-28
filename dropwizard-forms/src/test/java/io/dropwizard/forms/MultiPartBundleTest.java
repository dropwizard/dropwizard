package io.dropwizard.forms;

import io.dropwizard.Configuration;
import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class MultiPartBundleTest {
    private final Configuration configuration = mock(Configuration.class);

    @Test
    public void testRun() throws Exception {
        final Environment environment = new Environment(
                "multipart-test",
                Jackson.newObjectMapper(),
                null,
                new MetricRegistry(),
                getClass().getClassLoader()
        );

        new MultiPartBundle<Configuration>().run(environment);

        assertThat(environment.jersey().getResourceConfig().getClasses()).contains(MultiPartFeature.class);
    }
}
