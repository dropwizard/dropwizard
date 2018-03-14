package io.dropwizard.testing.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.common.Resource;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.validation.Validator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * A JUnit {@link TestRule} for testing Jersey resources.
 */
public class ResourceTestRule implements TestRule {
    /**
     * A {@link ResourceTestRule} builder which enables configuration of a Jersey testing environment.
     */
    public static class Builder extends Resource.Builder<ResourceTestRule.Builder> {
        /**
         * Builds a {@link ResourceTestRule} with a configured Jersey testing environment.
         *
         * @return a new {@link ResourceTestRule}
         */
        public ResourceTestRule build() {
            return new ResourceTestRule(buildResource());
        }
    }

    /**
     * Creates a new Jersey testing environment builder for {@link ResourceTestRule}
     *
     * @return a new {@link ResourceTestRule.Builder}
     */
    public static ResourceTestRule.Builder builder() {
        return new ResourceTestRule.Builder();
    }

    private final Resource resource;

    private ResourceTestRule(Resource resource) {
        this.resource = requireNonNull(resource, "resource");
    }

    public Validator getValidator() {
        return resource.getValidator();
    }

    public ObjectMapper getObjectMapper() {
        return resource.getObjectMapper();
    }

    public Consumer<ClientConfig> getClientConfigurator() {
        return resource.getClientConfigurator();
    }

    /**
     * Creates a web target to be sent to the resource under testing.
     *
     * @param path relative path (from tested application base URI) this web target should point to.
     * @return the created JAX-RS web target.
     */
    public WebTarget target(String path) {
        return resource.target(path);
    }

    /**
     * Returns the pre-configured {@link Client} for this test. For sending
     * requests prefer {@link #target(String)}
     *
     * @return the {@link JerseyTest} configured {@link Client}
     */
    public Client client() {
        return resource.client();
    }

    /**
     * Returns the underlying {@link JerseyTest}. For sending requests prefer
     * {@link #target(String)}.
     *
     * @return the underlying {@link JerseyTest}
     */
    public JerseyTest getJerseyTest() {
        return resource.getJerseyTest();
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    resource.before();
                    base.evaluate();
                } finally {
                    resource.after();
                }
            }
        };
    }
}
