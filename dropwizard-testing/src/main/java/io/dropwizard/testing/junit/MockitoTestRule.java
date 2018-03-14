package io.dropwizard.testing.junit;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.junit.MockitoRule;

import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

/**
 * A crude hack to allow using {@link MockitoRule} with {@link org.junit.rules.RuleChain}
 * for a defined run order.
 *
 * Example:
 *
 * <pre><code>
 * private final MockitoTestRule mockitoRule = new MockitoTestRule(this, MockitoJUnit.rule());
 * private final ResourceTestRule resourceRule = ResourceTestRule.builder()
 *     .addResource(PersonResource::new)
 *     .build();
 *
 * {@literal @}Rule
 * public final RuleChain ruleChain = RuleChain.outerRule(mockitoRule).around(resourceRule);
 * </code></pre>
 *
 * @see MockitoRule
 * @see org.junit.rules.RuleChain
 * @see <a href="https://github.com/junit-team/junit4/issues/351">#351: missing current instance in TestRule apply()</a>
 * @see <a href="https://github.com/mockito/mockito/issues/997">#997: ClassRule/TestRule version of Mockito's JUnitRule</a>
 */
public class MockitoTestRule implements TestRule {
    private final Object testInstance;
    private final MockitoRule delegate;

    /**
     * Create a new adapter for a {@link MockitoRule} instance.
     *
     * @param testInstance The instance of the test class (which is most likely {@code this})
     * @param delegate The instance of {@link MockitoRule} to wrap around
     */
    public MockitoTestRule(Object testInstance, MockitoRule delegate) {
        this.testInstance = requireNonNull(testInstance, "test instance");
        this.delegate = requireNonNull(delegate, "MockitoRule");
    }

    @Override
    public Statement apply(Statement base, Description description) {
        if (description.isEmpty()) {
            return base;
        } else {
            final Class<?> testClass = description.getTestClass();
            final FrameworkMethod frameworkMethod;
            try {
                final Method method = testClass.getMethod(description.getMethodName());
                frameworkMethod = new FrameworkMethod(method);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException(e);
            }

            return delegate.apply(base, frameworkMethod, testInstance);
        }
    }
}
