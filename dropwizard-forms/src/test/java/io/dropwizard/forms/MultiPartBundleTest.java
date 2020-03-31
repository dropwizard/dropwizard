package io.dropwizard.forms;

import io.dropwizard.Configuration;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiPartBundleTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Test
    void testRun() {
        final Environment environment = new Environment("multipart-test");

        new MultiPartBundle().run(new Configuration(), environment);

        assertThat(environment.jersey().getResourceConfig().getClasses()).contains(MultiPartFeature.class);
    }
}
