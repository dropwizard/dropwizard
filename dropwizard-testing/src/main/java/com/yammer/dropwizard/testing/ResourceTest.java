package com.yammer.dropwizard.testing;

import java.util.List;
import java.util.Set;

import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import org.codehaus.jackson.map.Module;
import org.junit.After;
import org.junit.Before;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.dropwizard.bundles.JavaBundle;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.json.Json;

/**
 * A base test class for testing Dropwizard resources.
 */
public abstract class ResourceTest {
    private final Set<Object> singletons = Sets.newHashSet();
    private final Set<Class<?>> providers = Sets.newHashSet();
    private final List<Module> modules = Lists.newArrayList();

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

    protected Json getJson() {
        return new Json();
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
                final DropwizardResourceConfig config = new DropwizardResourceConfig();
                for (Object provider : JavaBundle.DEFAULT_PROVIDERS) { // sorry, Scala folks
                    config.getSingletons().add(provider);
                }
                for (Class<?> provider : providers) {
                    config.getClasses().add(provider);
                }
                Json json = getJson();
                for (Module module : modules) {
                    json.registerModule(module);
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
        test.tearDown();
    }
}
