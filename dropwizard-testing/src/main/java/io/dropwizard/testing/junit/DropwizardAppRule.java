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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * A JUnit rule for starting and stopping your application at the start and end of a test class.
 * <p>
 * By default, the {@link Application} will be constructed using reflection to invoke the nullary
 * constructor. If your application does not provide a public nullary constructor, you will need to
 * override the {@link #newApplication()} method to provide your application instance(s).
 * </p>
 *
 * <p>
 * Using DropwizardAppRule at the suite level can speed up test runs, as the application is only started and stopped
 * once for the entire suite:
 * </p>
 *
 * <pre>
 * &#064;RunWith(Suite.class)
 * &#064;SuiteClasses({FooTest.class, BarTest.class})
 * public class MySuite {
 *   &#064;ClassRule
 *   public static final DropwizardAppRule&lt;MyConfig> DROPWIZARD = new DropwizardAppRule&lt;>(...);
 * }
 * </pre>
 *
 * <p>
 * If the same instance of DropwizardAppRule is reused at the suite- and class-level, then the application will be
 * started and stopped once, regardless of whether the entire suite or a single test is executed.
 * </p>
 *
 * <pre>
 * public class FooTest {
 *   &#064;ClassRule public static final DropwizardAppRule&lt;MyConfig> DROPWIZARD = MySuite.DROPWIZARD;
 *
 *   public void testFoo() { ... }
 * }
 *
 * public class BarTest {
 *   &#064;ClassRule public static final DropwizardAppRule&lt;MyConfig> DROPWIZARD = MySuite.DROPWIZARD;
 *
 *   public void testBar() { ... }
 * }
 * </pre>
 *
 * <p>
 *
 * </p>
 *
 * @param <C> the configuration type
 */
public class DropwizardAppRule<C extends Configuration> extends ExternalResource {

    private final DropwizardTestSupport<C> testSupport;

    private final AtomicInteger recursiveCallCount = new AtomicInteger(0);

    public DropwizardAppRule(Class<? extends Application<C>> applicationClass) {
        this(applicationClass, (String) null);
    }

    public DropwizardAppRule(Class<? extends Application<C>> applicationClass,
                             @Nullable String configPath,
                             ConfigOverride... configOverrides) {
        this(applicationClass, configPath, Optional.empty(), configOverrides);
    }

    public DropwizardAppRule(Class<? extends Application<C>> applicationClass, String configPath,
                                 Optional<String> customPropertyPrefix, ConfigOverride... configOverrides) {
        this(new DropwizardTestSupport<>(applicationClass, configPath, customPropertyPrefix,
                configOverrides));
    }

    /**
     * Alternate constructor that allows specifying exact Configuration object to
     * use, instead of reading a resource and binding it as Configuration object.
     *
     * @since 0.9
     */
    public DropwizardAppRule(Class<? extends Application<C>> applicationClass,
            C configuration) {
        this(new DropwizardTestSupport<>(applicationClass, configuration));
    }

    public DropwizardAppRule(DropwizardTestSupport<C> testSupport) {
        this.testSupport = testSupport;
    }

    public DropwizardAppRule<C> addListener(final ServiceListener<C> listener) {
        this.testSupport.addListener(new DropwizardTestSupport.ServiceListener<C>() {
            @Override
            public void onRun(C configuration, Environment environment, DropwizardTestSupport<C> rule) throws Exception {
                listener.onRun(configuration, environment, DropwizardAppRule.this);
            }

            @Override
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
        if (recursiveCallCount.getAndIncrement() == 0) {
            testSupport.before();
        }
    }

    @Override
    protected void after() {
        if (recursiveCallCount.decrementAndGet() == 0) {
            testSupport.after();
        }
    }

    public C getConfiguration() {
        return testSupport.getConfiguration();
    }

    public int getLocalPort() {
        return testSupport.getLocalPort();
    }

    public int getPort(int connectorIndex) {
       return testSupport.getPort(connectorIndex);
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

    public DropwizardTestSupport<C> getTestSupport() {
        return testSupport;
    }
}
