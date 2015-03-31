package io.dropwizard.testing.junit;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.logging.LoggingFactory;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import javax.servlet.ServletConfig;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Context;
import java.util.Map;
import java.util.Set;

/**
 * A JUnit {@link TestRule} for testing Jersey resources.
 */
public class ResourceTestRule implements TestRule {

    static {
        LoggingFactory.bootstrap();
    }

    public static class Builder {

        private final Set<Object> singletons = Sets.newHashSet();
        private final Set<Class<?>> providers = Sets.newHashSet();
        private final Map<String, Object> properties = Maps.newHashMap();
        private ObjectMapper mapper = Jackson.newObjectMapper();
        private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        private TestContainerFactory testContainerFactory = new InMemoryTestContainerFactory();

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

        public ResourceTestRule build() {
            return new ResourceTestRule(singletons, providers, properties, mapper, validator, testContainerFactory);
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

    private JerseyTest test;

    private ResourceTestRule(Set<Object> singletons,
                             Set<Class<?>> providers,
                             Map<String, Object> properties,
                             ObjectMapper mapper,
                             Validator validator,
                             TestContainerFactory testContainerFactory) {
        this.singletons = singletons;
        this.providers = providers;
        this.properties = properties;
        this.mapper = mapper;
        this.validator = validator;
        this.testContainerFactory = testContainerFactory;
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
        private static final Map<String, ResourceTestRule> RULE_ID_TO_RULE = Maps.newHashMap();

        public ResourceTestResourceConfig(final String ruleId, final ResourceTestRule resourceTestRule) {
            super(true, new MetricRegistry());
            RULE_ID_TO_RULE.put(ruleId, resourceTestRule);
            configure(resourceTestRule);
        }

        public ResourceTestResourceConfig(@Context ServletConfig servletConfig) {
            super(true, new MetricRegistry());
            String ruleId = servletConfig.getInitParameter(RULE_ID);
            Preconditions.checkNotNull(ruleId);

            ResourceTestRule resourceTestRule = RULE_ID_TO_RULE.get(ruleId);
            Preconditions.checkNotNull(resourceTestRule);
            configure(resourceTestRule);
        }

        private void configure(final ResourceTestRule resourceTestRule) {
            for (Class<?> provider : resourceTestRule.providers) {
                register(provider);
            }
            for (Map.Entry<String, Object> property : resourceTestRule.properties.entrySet()) {
                property(property.getKey(), property.getValue());
            }
            register(new JacksonMessageBodyProvider(resourceTestRule.mapper, resourceTestRule.validator));
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
                        protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
                            return testContainerFactory;
                        }

                        @Override
                        protected DeploymentContext configureDeployment() {
                            final ResourceTestResourceConfig resourceConfig = new ResourceTestResourceConfig(ruleId, rule);
                            ServletDeploymentContext deploymentContext = ServletDeploymentContext.builder(resourceConfig)
                                    .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, ResourceTestResourceConfig.class.getName())
                                    .initParam(ResourceTestResourceConfig.RULE_ID, ruleId)
                                    .build();
                            return deploymentContext;
                        }

                        @Override
                        protected void configureClient(final ClientConfig config) {
                            JacksonJsonProvider jsonProvider = new JacksonJsonProvider();
                            jsonProvider.setMapper(mapper);
                            config.register(jsonProvider);
                        }
                    };
                    test.setUp();
                    base.evaluate();
                } finally {
                    ResourceTestResourceConfig.RULE_ID_TO_RULE.remove(ruleId);
                    if (test != null) {
                        test.tearDown();
                    }
                }
            }
        };
    }
}
