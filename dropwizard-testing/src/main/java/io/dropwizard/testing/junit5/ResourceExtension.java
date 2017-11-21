package io.dropwizard.testing.junit5;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.common.Resource;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;

import javax.validation.Validator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.util.function.Consumer;

/**
 * An extension for testing Jersey resources.
 */
public class ResourceExtension implements DropwizardExtension {
    /**
     * A {@link ResourceExtension} builder which enables configuration of a Jersey testing environment.
     */
    public static class Builder extends Resource.Builder<Builder> {
        /**
         * Builds a {@link ResourceExtension} with a configured Jersey testing environment.
         *
         * @return a new {@link ResourceExtension}
         */
        public ResourceExtension build() {
            return new ResourceExtension(buildResource());
        }
    }

    /**
     * Creates a new Jersey testing environment builder for {@link ResourceExtension}
     *
     * @return a new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    private final Resource resource;

    private ResourceExtension(Resource resource) {
        this.resource = resource;
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
    public void before() throws Throwable {
        resource.before();
    }

    @Override
    public void after() throws Throwable {
        resource.after();
    }
}
