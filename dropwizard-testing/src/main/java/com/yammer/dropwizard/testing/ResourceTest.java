package com.yammer.dropwizard.testing;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.dropwizard.bundles.JavaBundle;
import com.yammer.dropwizard.config.DropwizardResourceConfig;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import org.codehaus.jackson.map.Module;
import org.junit.After;
import org.junit.Before;

import java.util.List;
import java.util.Set;

/**
 * A base test class for testing Dropwizard resources.
 */
public abstract class ResourceTest {
    private final Set<Object> singletons = Sets.newHashSet();
    private final List<Module> modules = Lists.newArrayList();

    private JerseyTest test;

    protected abstract void setUpResources() throws Exception;

    protected void addResource(Object resource) {
        singletons.add(resource);
    }

    protected void addJacksonModule(Module module) {
        modules.add(module);
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
                config.getSingletons().add(new JacksonMessageBodyProvider(modules));
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
