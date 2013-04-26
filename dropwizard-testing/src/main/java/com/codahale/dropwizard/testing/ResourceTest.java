package com.codahale.dropwizard.testing;

import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.jersey.DropwizardResourceConfig;
import com.codahale.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import com.codahale.dropwizard.logging.LoggingFactory;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.junit.After;
import org.junit.Before;

import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Map;
import java.util.Set;

/**
 * A base test class for testing Dropwizard resources.
 */
public abstract class ResourceTest {
    static {
        LoggingFactory.bootstrap();
    }

    private final Set<Object> singletons = Sets.newHashSet();
    private final Set<Class<?>> providers = Sets.newHashSet();
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Map<String, Boolean> features = Maps.newHashMap();
    private final Map<String, Object> properties = Maps.newHashMap();

    private JerseyTest test;
    private Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    protected abstract void setUpResources() throws Exception;

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    protected void addResource(Object resource) {
        singletons.add(resource);
    }

    public void addProvider(Class<?> klass) {
        providers.add(klass);
    }

    public void addProvider(Object provider) {
        singletons.add(provider);
    }

    protected ObjectMapper getObjectMapperFactory() {
        return objectMapper;
    }

    protected void addFeature(String feature, Boolean value) {
        features.put(feature, value);
    }

    protected void addProperty(String property, Object value) {
        properties.put(property, value);
    }
    
    protected Client client() {
        return test.client();
    }

    protected JerseyTest getJerseyTest() {
        return test;
    }

    @Before
    public final void setUpJersey() throws Exception {
        setUpResources();
        this.test = new JerseyTest() {
            @Override
            protected AppDescriptor configure() {
                final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
                for (Class<?> provider : providers) {
                    config.getClasses().add(provider);
                }
                for (Map.Entry<String, Boolean> feature : features.entrySet()) {
                    config.getFeatures().put(feature.getKey(), feature.getValue());
                }
                for (Map.Entry<String, Object> property : properties.entrySet()) {
                    config.getProperties().put(property.getKey(), property.getValue());
                }
                config.getSingletons().add(new JacksonMessageBodyProvider(objectMapper, validator));
                config.getSingletons().addAll(singletons);
                return new LowLevelAppDescriptor.Builder(config).build();
            }
        };
        test.setUp();
    }

    @After
    public final void tearDownJersey() throws Exception {
        if (test != null) {
            test.tearDown();
        }
    }
}
