package io.dropwizard.testing.app;

import io.dropwizard.testing.junit.ResourceTestRule;
import io.dropwizard.testing.junit.TestResource;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceTestRuleWithoutLoggingBootstrap {
    @Rule
    public final ResourceTestRule resourceTestRule = ResourceTestRule.builder()
            .addResource(new TestResource())
            .bootstrapLogging(false)
            .build();

    @Test
    public void testResource() {
        assertThat(resourceTestRule.target("test").request()
                .get(String.class))
                .isEqualTo("Default message");
    }
}
