package io.dropwizard.forms;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MultiPartBundleTest {

    @Test
    public void testRun() throws Exception {
        final Environment environment = new Environment(
                "multipart-test",
                Jackson.newObjectMapper(),
                null,
                new MetricRegistry(),
                getClass().getClassLoader()
        );

        new MultiPartBundle().run(environment);

        assertThat(environment.jersey().getResourceConfig().getClasses()).contains(MultiPartFeature.class);
    }
}