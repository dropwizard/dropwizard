package io.dropwizard.testing.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import org.junit.rules.ExternalResource;

import javax.annotation.Nullable;


/**
 * A JUnit rule for starting and stopping your application at the start and end of a test class.
 * <p>
 * By default, the {@link Application} will be constructed using reflection to invoke the nullary
 * constructor. If your application does not provide a public nullary constructor, you will need to
 * override the {@link #newApplication()} method to provide your application instance(s).
 * </p>
 *
 * @param <C> the configuration type
 */
public class DropwizardAppRule<C extends Configuration> extends ExternalResource {

    private final DropwizardTestSupport<C> testSupport;

    public DropwizardAppRule(Class<? extends Application<C>> applicationClass,
                             @Nullable String configPath,
                             ConfigOverride... configOverrides) {
        this.testSupport = new DropwizardTestSupport<>(applicationClass, configPath, configOverrides);

    }

    public DropwizardAppRule(DropwizardTestSupport<C> testSupport) {
        this.testSupport = testSupport;
    }

    public DropwizardAppRule<C> addListener(final ServiceListener<C> listener) {
        this.testSupport.addListener(new DropwizardTestSupport.ServiceListener<C>(){

            public void onRun(C configuration, Environment environment, DropwizardTestSupport<C> rule) throws Exception {
                listener.onRun(configuration, environment, DropwizardAppRule.this);
            }

            public void onStop(DropwizardTestSupport<C> rule) throws Exception {
                listener.onStop(DropwizardAppRule.this);
            }

        });
        return this;
    }

    public DropwizardAppRule<C> manage(final Managed managed) {
        return addListener(new ServiceListener<C>() {
            @Override
            public void onRun(C configuration, Environment environment, DropwizardAppRule<C> rule) throws Exception {
                environment.lifecycle().manage(managed);
            }
        });
    }

    @Override
    protected void before() {
        testSupport.before();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void after() {
        testSupport.after();
    }

    public C getConfiguration() {
        return testSupport.getConfiguration();
    }

    public int getLocalPort() {
        return testSupport.getLocalPort();
    }

    public int getAdminPort() {
        return testSupport.getAdminPort();
    }

    public Application<C> newApplication() {
        return testSupport.newApplication();
    }

    public <A extends Application<C>> A getApplication() {
        return testSupport.getApplication();
    }

    public Environment getEnvironment() {
        return testSupport.getEnvironment();
    }

    public ObjectMapper getObjectMapper() {
        return testSupport.getObjectMapper();
    }

    public abstract static class ServiceListener<T extends Configuration> {

        public void onRun(T configuration, Environment environment, DropwizardAppRule<T> rule) throws Exception {
            // Default NOP
        }

        public void onStop(DropwizardAppRule<T> rule) throws Exception {
            // Default NOP
        }
    }

    public DropwizardTestSupport<C> getTestSupport(){
        return testSupport;
    }
}
