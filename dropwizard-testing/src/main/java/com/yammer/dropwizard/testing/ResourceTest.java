package com.yammer.dropwizard.testing;

import com.fasterxml.jackson.databind.Module;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.dropwizard.bundles.BasicBundle;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.json.Json;
import org.junit.After;
import org.junit.Before;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A base test class for testing Dropwizard resources.
 */
public abstract class ResourceTest {
    private final Set<Object> singletons = Sets.newHashSet();
    private final Set<Class<?>> providers = Sets.newHashSet();
    private final List<Module> modules = Lists.newArrayList();
    private final Map<String, Boolean> features = Maps.newHashMap();
    private final Map<String, Object> properties = Maps.newHashMap();

    private JerseyTest test;

    protected abstract void setUpResources() throws Exception;

    protected void addResource(Object resource) {
        singletons.add(resource);
    }

    public void addProvider(Class<?> klass) {
        providers.add(klass);
    }

    protected void addJacksonModule(Module module) {
        modules.add(module);
    }

    protected void addFeature(String feature, Boolean value) {
        features.put(feature, value);
    }

    protected void addProperty(String property, Object value) {
        properties.put(property, value);
    }

    protected Json getJson() {
        final Json json = new Json();
        for (Module module : modules) {
            json.registerModule(module);
        }
        return json;
    }
    
    protected Client client() {
        return test.client();
    }

    @Before
    public void setUpJersey() throws Exception {
        setUpResources();
        this.test = new JerseyTest() {
            @Override
            protected AppDescriptor configure() {
                final DropwizardResourceConfig config = new DropwizardResourceConfig(true);
                for (Class<?> provider : BasicBundle.DEFAULT_PROVIDERS) { // sorry, Scala folks
                    config.getClasses().add(provider);
                }
                for (Class<?> provider : providers) {
                    config.getClasses().add(provider);
                }
                final Json json = getJson();
                for (Map.Entry<String, Boolean> feature : features.entrySet()) {
                    config.getFeatures().put(feature.getKey(), feature.getValue());
                }
                for (Map.Entry<String, Object> property : properties.entrySet()) {
                    config.getProperties().put(property.getKey(), property.getValue());
                }
                config.getSingletons().add(new JacksonMessageBodyProvider(json));
                config.getSingletons().addAll(singletons);
                return new LowLevelAppDescriptor.Builder(config).build();
            }
        };
        test.setUp();
    }

    @After
    public void tearDownJersey() throws Exception {
        if (test != null) {
            test.tearDown();
        }
    }
}
