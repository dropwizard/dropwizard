package io.dropwizard.testing.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.annotation.Nullable;
import javax.validation.Validator;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class Resource {

    static {
        BootstrapLogging.bootstrap();
    }

    /**
     * A {@link Resource} builder which enables configuration of a Jersey testing environment.
     */
    @SuppressWarnings("unchecked")
    public static class Builder<B extends Builder<B>> {

        private final Set<Supplier<?>> singletons = new HashSet<>();
        private final Set<Class<?>> providers = new HashSet<>();
        private final Map<String, Object> properties = new HashMap<>();
        private ObjectMapper mapper = Jackson.newObjectMapper();
        private Validator validator = Validators.newValidator();
        private Consumer<ClientConfig> clientConfigurator = c -> {
        };
        private TestContainerFactory testContainerFactory = new InMemoryTestContainerFactory();
        private boolean registerDefaultExceptionMappers = true;

        public B setMapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return (B) this;
        }

        public B setValidator(Validator validator) {
            this.validator = validator;
            return (B) this;
        }

        public B setClientConfigurator(Consumer<ClientConfig> clientConfigurator) {
            this.clientConfigurator = clientConfigurator;
            return (B) this;
        }

        public B addResource(Object resource) {
            return addResource(() -> resource);
        }

        public B addResource(Supplier<Object> resourceSupplier) {
            singletons.add(resourceSupplier);
            return (B) this;
        }

        public B addProvider(Class<?> klass) {
            providers.add(klass);
            return (B) this;
        }

        public B addProvider(Supplier<Object> providerSupplier) {
            singletons.add(providerSupplier);
            return (B) this;
        }

        public B addProvider(Object provider) {
            return addProvider(() -> provider);
        }

        public B addProperty(String property, Object value) {
            properties.put(property, value);
            return (B) this;
        }

        public B setTestContainerFactory(TestContainerFactory factory) {
            this.testContainerFactory = factory;
            return (B) this;
        }

        public B setRegisterDefaultExceptionMappers(boolean value) {
            registerDefaultExceptionMappers = value;
            return (B) this;
        }

        /**
         * Builds a {@link Resource} with a configured Jersey testing environment.
         *
         * @return a new {@link Resource}
         */
        protected Resource buildResource() {
            return new Resource(new ResourceTestJerseyConfiguration(
                singletons, providers, properties, mapper, validator,
                clientConfigurator, testContainerFactory, registerDefaultExceptionMappers));
        }
    }

    /**
     * Creates a new Jersey testing environment builder for {@link ResourceExtension}
     *
     * @return a new {@link ResourceExtension.Builder}
     */
    public static ResourceExtension.Builder builder() {
        return new ResourceExtension.Builder();
    }

    private ResourceTestJerseyConfiguration configuration;

    @Nullable
    private JerseyTest test;

    private Resource(ResourceTestJerseyConfiguration configuration) {
        this.configuration = configuration;
    }

    public Validator getValidator() {
        return configuration.validator;
    }

    public ObjectMapper getObjectMapper() {
        return configuration.mapper;
    }

    public Consumer<ClientConfig> getClientConfigurator() {
        return configuration.clientConfigurator;
    }

    /**
     * Creates a web target to be sent to the resource under testing.
     *
     * @param path relative path (from tested application base URI) this web target should point to.
     * @return the created JAX-RS web target.
     */
    public WebTarget target(String path) {
        return getJerseyTest().target(path);
    }

    /**
     * Returns the pre-configured {@link Client} for this test. For sending
     * requests prefer {@link #target(String)}
     *
     * @return the {@link JerseyTest} configured {@link Client}
     */
    public Client client() {
        return getJerseyTest().client();
    }

    /**
     * Returns the underlying {@link JerseyTest}. For sending requests prefer
     * {@link #target(String)}.
     *
     * @return the underlying {@link JerseyTest}
     */
    public JerseyTest getJerseyTest() {
        return requireNonNull(test);
    }

    public void before() throws Throwable {
        DropwizardTestResourceConfig.CONFIGURATION_REGISTRY.put(configuration.getId(), configuration);

        test = new JerseyTest() {
            @Override
            protected TestContainerFactory getTestContainerFactory() {
                return configuration.testContainerFactory;
            }

            @Override
            protected DeploymentContext configureDeployment() {
                return ServletDeploymentContext.builder(new DropwizardTestResourceConfig(configuration))
                    .initParam(ServletProperties.JAXRS_APPLICATION_CLASS,
                        DropwizardTestResourceConfig.class.getName())
                    .initParam(DropwizardTestResourceConfig.CONFIGURATION_ID, configuration.getId())
                    .build();
            }

            @Override
            protected void configureClient(ClientConfig clientConfig) {
                final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
                jsonProvider.setMapper(configuration.mapper);
                configuration.clientConfigurator.accept(clientConfig);
                clientConfig.register(jsonProvider);
            }
        };
        test.setUp();
    }

    public void after() throws Throwable {
        DropwizardTestResourceConfig.CONFIGURATION_REGISTRY.remove(configuration.getId());
        requireNonNull(test).tearDown();
    }
}
