package io.dropwizard.forms;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.logging.common.BootstrapLogging;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.junit.jupiter.api.Test;

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
