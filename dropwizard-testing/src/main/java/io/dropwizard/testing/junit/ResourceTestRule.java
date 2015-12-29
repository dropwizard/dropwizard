package io.dropwizard.testing.junit;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.errors.EarlyEofExceptionMapper;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.jersey.validation.HibernateValidationFeature;
import io.dropwizard.jersey.validation.JerseyViolationExceptionMapper;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.servlet.ServletConfig;
import javax.validation.Validator;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * A JUnit {@link TestRule} for testing Jersey resources.
 */
public class ResourceTestRule implements TestRule {

    static {
        BootstrapLogging.bootstrap();
    }

    public static class Builder {

        private final Set<Object> singletons = new HashSet<>();
        private final Set<Class<?>> providers = new HashSet<>();
        private final Map<String, Object> properties = new HashMap<>();
        private ObjectMapper mapper = Jackson.newObjectMapper();
        private Validator validator = Validators.newValidator();
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

        public ResourceTestRule build() {
            return new ResourceTestRule(singletons, providers, properties, mapper, validator, testContainerFactory, registerDefaultExceptionMappers);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Set<Object> singletons;
    private final Set<Class<?>> providers;
    private final Map<String, Object> properties;
    private final ObjectMapper mapper;
    private final Validator validator;
    private final TestContainerFactory testContainerFactory;
    private final boolean registerDefaultExceptionMappers;

    private JerseyTest test;

    private ResourceTestRule(Set<Object> singletons,
                             Set<Class<?>> providers,
                             Map<String, Object> properties,
                             ObjectMapper mapper,
                             Validator validator,
                             TestContainerFactory testContainerFactory,
                             boolean registerDefaultExceptionMappers) {
        this.singletons = singletons;
        this.providers = providers;
        this.properties = properties;
        this.mapper = mapper;
        this.validator = validator;
        this.testContainerFactory = testContainerFactory;
        this.registerDefaultExceptionMappers = registerDefaultExceptionMappers;
    }

    public Validator getValidator() {
        return validator;
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }

    public Client client() {
        return test.client();
    }

    public JerseyTest getJerseyTest() {
        return test;
    }

    public static class ResourceTestResourceConfig extends DropwizardResourceConfig {
        private static final String RULE_ID = "io.dropwizard.testing.junit.resourceTestRuleId";
        private static final Map<String, ResourceTestRule> RULE_ID_TO_RULE = new HashMap<>();

        public ResourceTestResourceConfig(final String ruleId, final ResourceTestRule resourceTestRule) {
            super(true, new MetricRegistry());
            RULE_ID_TO_RULE.put(ruleId, resourceTestRule);
            configure(resourceTestRule);
        }

        public ResourceTestResourceConfig(@Context ServletConfig servletConfig) {
            super(true, new MetricRegistry());
            final String ruleId = servletConfig.getInitParameter(RULE_ID);
            requireNonNull(ruleId);

            final ResourceTestRule resourceTestRule = RULE_ID_TO_RULE.get(ruleId);
            requireNonNull(resourceTestRule);
            configure(resourceTestRule);
        }

        private void configure(final ResourceTestRule resourceTestRule) {
            if (resourceTestRule.registerDefaultExceptionMappers) {
                register(new LoggingExceptionMapper<Throwable>() {
                });
                register(new JerseyViolationExceptionMapper());
                register(new JsonProcessingExceptionMapper());
                register(new EarlyEofExceptionMapper());
            }
            for (Class<?> provider : resourceTestRule.providers) {
                register(provider);
            }
            property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
            for (Map.Entry<String, Object> property : resourceTestRule.properties.entrySet()) {
                property(property.getKey(), property.getValue());
            }
            register(new JacksonMessageBodyProvider(resourceTestRule.mapper));
            register(new HibernateValidationFeature(resourceTestRule.validator));
            for (Object singleton : resourceTestRule.singletons) {
                register(singleton);
            }
        }
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        final ResourceTestRule rule = this;
        final String ruleId = String.valueOf(rule.hashCode());
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    test = new JerseyTest() {
                        @Override
                        protected TestContainerFactory getTestContainerFactory() {
                            return testContainerFactory;
                        }

                        @Override
                        protected DeploymentContext configureDeployment() {
                            return ServletDeploymentContext.builder(new ResourceTestResourceConfig(ruleId, rule))
                                    .initParam(ServletProperties.JAXRS_APPLICATION_CLASS,
                                            ResourceTestResourceConfig.class.getName())
                                    .initParam(ResourceTestResourceConfig.RULE_ID, ruleId)
                                    .build();
                        }

                        @Override
                        protected void configureClient(final ClientConfig config) {
                            final JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
                            jsonProvider.setMapper(mapper);
                            config.register(jsonProvider);
                        }
                    };
                    test.setUp();
                    base.evaluate();
                } finally {
                    ResourceTestResourceConfig.RULE_ID_TO_RULE.remove(ruleId);
                    test.tearDown();
                }
            }
        };
    }
}
