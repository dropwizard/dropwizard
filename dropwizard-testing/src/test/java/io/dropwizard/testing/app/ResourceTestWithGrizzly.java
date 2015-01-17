package io.dropwizard.testing.app;

import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.test.grizzly.GrizzlyTestContainerFactory;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests {@link io.dropwizard.testing.junit.ResourceTestRule} with a different
 * test container factory.
 */
public class ResourceTestWithGrizzly {
    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addResource(new ContextInjectionResource())
            .setTestContainerFactory(new GrizzlyTestContainerFactory())
            .build();

    @Test
    public void testResource() {
        assertThat(resources.getJerseyTest().target("test").request()
                .get(String.class))
                .isEqualTo("test");
    }
}
