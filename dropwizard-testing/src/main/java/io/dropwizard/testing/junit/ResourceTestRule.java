package io.dropwizard.testing.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.validation.Validator;
import javax.ws.rs.client.Client;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A JUnit {@link TestRule} for testing Jersey resources.
 */
public class ResourceTestRule implements TestRule {

    static {
        BootstrapLogging.bootstrap();
    }

    /**
     * A {@link ResourceTestRule} builder which enables configuration of a Jersey testing environment.
     */
    public static class Builder {

        private final Set<Object> singletons = new HashSet<>();
        private final Set<Class<?>> providers = new HashSet<>();
        private final Map<String, Object> properties = new HashMap<>();
        private ObjectMapper mapper = Jackson.newObjectMapper();
        private Validator validator = Validators.newValidator();
        private Consumer<ClientConfig> clientConfigurator = c -> {};
        private TestContainerFactory testContainerFactory = new InMemoryTestContainerFactory();
        private boolean registerDefaultExceptionMappers = true;

        public Builder setMapper(ObjectMapper mapper) {
            this.mapper = mapper;
            return this;
        }

        public Builder setValidator(Validator validator) {
            this.validator = validator;
            return this;
        }

        public Builder setClientConfigurator(Consumer<ClientConfig> clientConfigurator) {
            this.clientConfigurator = clientConfigurator;
            return this;
        }

        public Builder addResource(Object resource) {
            singletons.add(resource);
            return this;
        }

        public Builder addProvider(Class<?> klass) {
            providers.add(klass);
            return this;
        }

        public Builder addProvider(Object provider) {
            singletons.add(provider);
            return this;
        }

        public Builder addProperty(String property, Object value) {
            properties.put(property, value);
            return this;
        }

        public Builder setTestContainerFactory(TestContainerFactory factory) {
            this.testContainerFactory = factory;
            return this;
        }

        public Builder setRegisterDefaultExceptionMappers(boolean value) {
            registerDefaultExceptionMappers = value;
            return this;
        }

        /**
         * Builds a {@link ResourceTestRule} with a configured Jersey testing environment.
         *
         * @return a new {@link ResourceTestRule}
         */
        public ResourceTestRule build() {
            return new ResourceTestRule(new ResourceTestJerseyConfiguration(
                singletons, providers, properties, mapper, validator,
                clientConfigurator, testContainerFactory, registerDefaultExceptionMappers));
        }
    }

    /**
     * Creates a new Jersey testing environment builder for {@link ResourceTestRule}
     *
     * @return a new {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    private ResourceTestJerseyConfiguration configuration;
    private JerseyTest test;

    private ResourceTestRule(ResourceTestJerseyConfiguration configuration) {
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

    public Client client() {
        return test.client();
    }

    public JerseyTest getJerseyTest() {
        return test;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                DropwizardTestResourceConfig.CONFIGURATION_REGISTRY.put(configuration.getId(), configuration);
                try {
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
                    base.evaluate();
                } finally {
                    DropwizardTestResourceConfig.CONFIGURATION_REGISTRY.remove(configuration.getId());
                    test.tearDown();
                }
            }
        };
    }
}
